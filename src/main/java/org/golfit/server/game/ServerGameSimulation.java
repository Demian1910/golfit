package org.golfit.server.game;

import org.golfit.server.serverApi.ServerApi;
import org.golfit.shared.constants.Constants;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerGameSimulation {
    private static final Logger logger = LoggerFactory.getLogger(ServerGameSimulation.class);
    private final ServerApi serverApi;
    private final Map<Integer, ServerBall> balls = new ConcurrentHashMap<>();
    private ScheduledExecutorService ticker;

    // number of ticks per second
    private static final int TICK_RATE_MS = 100;

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

    private void tick() {
        try {
            double deltaTime = TICK_RATE_MS / 1000.0;
            boolean anyMoving = false;
            StringBuilder payloadBuilder = new StringBuilder();

            for (Map.Entry<Integer, ServerBall> entry : balls.entrySet()) {
                int playerId = entry.getKey();
                ServerBall ball = entry.getValue();

                if (ball.isMoving()) {
                    ball.update(deltaTime);
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
