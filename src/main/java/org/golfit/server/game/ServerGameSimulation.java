package org.golfit.server.game;

import org.golfit.server.serverApi.ServerApi;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.golfit.shared.constants.MapData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.golfit.shared.constants.Constants.*;

public class ServerGameSimulation {
    private static final Logger logger = LoggerFactory.getLogger(ServerGameSimulation.class);
    private final ServerApi serverApi;
    private final Map<Integer, ServerBall> balls = new ConcurrentHashMap<>();
    private ScheduledExecutorService ticker;

    // number of ticks per second
    private static final int TICK_RATE_MS = 40;

    public ServerGameSimulation(ServerApi serverApi) {
        this.serverApi = serverApi;
    }

    public void start() {
        ticker = Executors.newSingleThreadScheduledExecutor();
        ticker.scheduleAtFixedRate(this::tick, 0, TICK_RATE_MS, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (ticker != null)
            ticker.shutdown();
    }

    private boolean forceUpdate = false;


    public void removePlayer(int playerId) {
        balls.remove(playerId);
        forceUpdate = true;
    }

    public void strokeBall(int playerId, double forceX, double forceY) {
        ServerBall ball = balls.get(playerId);
        if (ball != null) {
            ball.stroke(forceX, forceY);
        } else {
            logger.warn("Received stroke for unknown player ID: " + playerId);
        }
    }


    /**
     Gathers all nearby shapes
     * @return A list of all shapes in specified area
     */
    public static List<MapData.ServerShape> getNearbyShapes(double worldX, double worldY, double searchRadius) {
        List<MapData.ServerShape> nearbyShapes = new ArrayList<>();


        Set<Integer> foundIds = new HashSet<>();

        int startCol = (int) Math.max(0, Math.floor(worldX - searchRadius));
        int endCol   = (int) Math.min(MapData.COLS - 1, (int) Math.floor(worldX + searchRadius));
        int startRow = (int) Math.max(0, Math.floor(worldY - searchRadius));
        int endRow   = (int) Math.min(MapData.ROWS - 1, (int) Math.floor(worldY + searchRadius));

        for (int r = startRow; r <= endRow; r++) {
            for (int c = startCol; c <= endCol; c++) {

                int id = MapData.SHAPE_ID_MAP[r][c];

                if (id != -1 && !foundIds.contains(id)) {
                    MapData.ServerShape ss = MapData.LEVEL_1_OBSTACLES.get(id);
                    nearbyShapes.add(ss);
                    foundIds.add(id);
                }
            }
        }

        return nearbyShapes;
    }

    // In ServerGameSimulation.java
    private void tick() {
        try {
            double deltaTime = TICK_RATE_MS / 1000.0;
            boolean anyMoving = false;
            StringBuilder payloadBuilder = new StringBuilder();

            for (Map.Entry<Integer, ServerBall> entry : balls.entrySet()) {
                int playerId = entry.getKey();
                ServerBall ball = entry.getValue();

                if (ball.isMoving() || forceUpdate) {
                    // Aktuelle Grid-Position
                    int col = (int) ball.getPx();
                    int row = (int) ball.getPy();

                    // 1. TERRAIN-DEBUGGING
                    if (col >= 0 && col < MapData.COLS && row >= 0 && row < MapData.ROWS) {
                        int terrainType = MapData.SERVER_MAP[row][col];

                        double friction = switch (terrainType) {
                            case 2 -> FRICTION_SAND; // SAND
                            case 3 -> FRICTION_ICE; // ICE
                            default -> 0.15; // GRASS
                        };

                        // Nur loggen, wenn der Ball sich bewegt, um den Spam zu begrenzen
                        if (ball.isMoving()) {
                            String terrainName = switch(terrainType) {
                                case 2 -> "SAND";
                                case 3 -> "ICE";
                                case 4 -> "WATER";
                                default -> "GRASS";
                            };
                            // Dieser Log zeigt dir, ob der Server "weiß", wo der Ball gerade rollt
                            System.out.println("[TICK] Player " + playerId + " is on " + terrainName +
                                    " (Grid: " + col + "/" + row + ") -> Friction: " + friction);
                        }

                        ball.changeFriction(friction);
                    } else {
                        // Falls der Ball außerhalb des Grids ist
                        System.err.println("[TICK] WARNING: Ball out of bounds at " + col + "/" + row);
                    }

                    // 2. PHYSIK-UPDATE
                    List<MapData.ServerShape> nearby = getNearbyShapes(ball.getPx(), ball.getPy(), 3.0);
                    ball.update(deltaTime, nearby);

                    if (ball.isMoving()) anyMoving = true;
                }

                // Payload-Bau (wie vorher)
                payloadBuilder.append(playerId).append(":")
                        .append(String.format(java.util.Locale.US, "%.2f", ball.getPx())).append(",")
                        .append(String.format(java.util.Locale.US, "%.2f", ball.getPy())).append(";");
            }

            // Broadcast (wie vorher)
            if (payloadBuilder.length() > 0 && (anyMoving || forceUpdate)) {
                serverApi.broadcastMovement(payloadBuilder.toString());
                forceUpdate = false;
            }
        } catch (Exception e) {
            logger.error("Error in physics tick", e);
        }
    }

    // Auch beim Hinzufügen direkt umrechnen!
    public void addPlayer(int playerId, double startX, double startY) {
        // Wenn der Client 550 (Mitte) schickt, speichert der Server 40.0
        double gridX = startX / (1100.0 / 80.0);
        double gridY = startY / (600.0 / 45.0);
        balls.put(playerId, new ServerBall(gridX, gridY));
        forceUpdate = true;
    }

    /**
     * Hilfsmethode: Ist der Mittelpunkt des Balls innerhalb der Shape?
     */
    private boolean isBallOverShape(ServerBall ball, MapData.ServerShape shape) {
        double bx = ball.getPx();
        double by = ball.getPy();

        if (shape.formType == MapData.ServerShape.RECT) {
            return bx >= shape.x && bx <= shape.x + shape.w &&
                    by >= shape.y && by <= shape.y + shape.h;
        }
        // Für Polygons/Ellipsen kannst du hier eine vereinfachte Distanzprüfung machen
        return false;
    }


}
