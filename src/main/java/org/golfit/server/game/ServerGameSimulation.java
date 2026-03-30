package org.golfit.server.game;

import org.golfit.server.serverApi.ServerApi;
import org.golfit.shared.constants.Constants;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.golfit.shared.constants.MapData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public void addPlayer(int playerId, double startX, double startY) {
        balls.put(playerId, new ServerBall(startX, startY));
        forceUpdate = true;
    }

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

    private void tick() {
        try {
            double deltaTime = TICK_RATE_MS / 1000.0;
            boolean anyMoving = false;
            StringBuilder payloadBuilder = new StringBuilder();

            for (Map.Entry<Integer, ServerBall> entry : balls.entrySet()) {
                int playerId = entry.getKey();
                ServerBall ball = entry.getValue();

                if (ball.isMoving()) {
                    List<MapData.ServerShape> nearby = getNearbyShapes(
                            ball.getPx(),
                            ball.getPy(),
                            1.5 // 1.5 grids
                    );

                    ball.update(deltaTime, nearby);
                    anyMoving = true;
                }

                payloadBuilder.append(playerId).append(":")
                        .append(ball.getPx()).append(",")
                        .append(ball.getPy()).append(";");
            }

            if (payloadBuilder.length() > 0 && (anyMoving || forceUpdate)) {
                serverApi.broadcastMovement(payloadBuilder.toString());
                forceUpdate = false;
            }

        } catch (Exception e) {
            logger.error("Error in physics tick", e);
        }
    }
}
