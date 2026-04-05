package org.golfit.server.game;

import org.golfit.server.serverApi.ServerApi;
import org.golfit.shared.constants.MapData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.golfit.shared.constants.Constants.*;

/**
 * Manages the server-side physics simulation for all active players.
 * Handles movement ticks, terrain-based friction, and water hazard resets.
 */
public class ServerGameSimulation {
    private static final Logger logger = LoggerFactory.getLogger(ServerGameSimulation.class);

    private final ServerApi serverApi;
    private final Map<Integer, ServerBall> balls = new ConcurrentHashMap<>();
    private ScheduledExecutorService ticker;

    private static final int TICK_RATE_MS = 40;
    private boolean forceUpdate = false;

    public ServerGameSimulation(ServerApi serverApi) {
        this.serverApi = serverApi;
    }

    /**
     * Starts the simulation ticker at the fixed rate.
     */
    public void start() {
        ticker = Executors.newSingleThreadScheduledExecutor();
        ticker.scheduleAtFixedRate(this::tick, 0, TICK_RATE_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the simulation ticker.
     */
    public void stop() {
        if (ticker != null) {
            ticker.shutdown();
        }
    }

    /**
     * Adds a new player to the simulation.
     * Automatically converts screen coordinates to grid coordinates.
     */
    public void addPlayer(int playerId, double startX, double startY) {
        double gridX = startX / (1100.0 / MapData.COLS);
        double gridY = startY / (600.0 / MapData.ROWS);
        balls.put(playerId, new ServerBall(gridX, gridY));
        forceUpdate = true;
    }

    /**
     * Removes a player from the simulation.
     */
    public void removePlayer(int playerId) {
        balls.remove(playerId);
        forceUpdate = true;
    }

    /**
     * Applies a stroke force to a specific player's ball.
     */
    public void strokeBall(int playerId, double forceX, double forceY) {
        ServerBall ball = balls.get(playerId);
        if (ball != null) {
            ball.stroke(forceX, forceY);
        } else {
            logger.warn("Received stroke for unknown player ID: {}", playerId);
        }
    }

    /**
     * Main simulation loop. Calculates physics for all balls and broadcasts updates.
     */
    private void tick() {
        try {
            double deltaTime = TICK_RATE_MS / 1000.0;
            boolean anyBallNeedsUpdate = false;
            StringBuilder payloadBuilder = new StringBuilder();

            for (Map.Entry<Integer, ServerBall> entry : balls.entrySet()) {
                int playerId = entry.getKey();
                ServerBall ball = entry.getValue();

                // 1. Terrain and Logic Processing
                if (ball.isMoving() || forceUpdate) {
                    int col = (int) ball.getPx();
                    int row = (int) ball.getPy();

                    if (col >= 0 && col < MapData.COLS && row >= 0 && row < MapData.ROWS) {
                        int terrainType = MapData.SERVER_MAP[row][col];

                        if (terrainType == 4) { // Water Hazard
                            ball.resetToLastPosition();
                            this.forceUpdate = true; // Ensure reset is visible to client
                            logger.info("Player {} hit water and was reset.", playerId);
                        } else {
                            // Update friction based on surface
                            double friction = switch (terrainType) {
                                case 2 -> FRICTION_SAND;
                                case 3 -> FRICTION_ICE;
                                default -> FRICTION_GRASS;
                            };
                            ball.changeFriction(friction);
                        }
                    }

                    // 2. Physics Update
                    List<MapData.ServerShape> nearby = getNearbyShapes(ball.getPx(), ball.getPy(), 3.0);
                    ball.update(deltaTime, nearby);

                    if (ball.isMoving()) {
                        anyBallNeedsUpdate = true;
                    }
                }

                // 3. Generate Update Payload (ID:X,Y;)
                payloadBuilder.append(playerId).append(":")
                        .append(String.format(java.util.Locale.US, "%.2f", ball.getPx())).append(",")
                        .append(String.format(java.util.Locale.US, "%.2f", ball.getPy())).append(";");
            }

            // Broadcast only if there is movement, a forced update, or player changes
            if (payloadBuilder.length() > 0 && (anyBallNeedsUpdate || forceUpdate)) {
                serverApi.broadcastMovement(payloadBuilder.toString());
                forceUpdate = false;
            }

        } catch (Exception e) {
            logger.error("Error in physics tick", e);
        }
    }

    /**
     * Retrieves static obstacles near the specified coordinates to optimize collision detection.
     * * @param worldX Current X grid position.
     * @param worldY Current Y grid position.
     * @param searchRadius Distance in grid units to check for obstacles.
     * @return List of nearby shapes.
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
                    if (ss != null) {
                        nearbyShapes.add(ss);
                        foundIds.add(id);
                    }
                }
            }
        }
        return nearbyShapes;
    }

    /**
     * Checks if the center of the ball is within the bounds of a specific shape.
     */
    private boolean isBallOverShape(ServerBall ball, MapData.ServerShape shape) {
        double bx = ball.getPx();
        double by = ball.getPy();

        if (shape.formType == MapData.ServerShape.RECT) {
            return bx >= shape.x && bx <= shape.x + shape.w &&
                    by >= shape.y && by <= shape.y + shape.h;
        }
        return false;
    }
}