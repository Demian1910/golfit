package org.golfit.client.handlers;

import org.golfit.client.clientLogic.ClientApi;
import org.golfit.shared.messageParser.MessageParser;
import org.golfit.client.game.BallRender;
import org.golfit.client.game.Ball;
import javafx.scene.layout.Pane;
import javafx.application.Platform;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles movement update messages from the server on the client side.
 * Converts server grid coordinates to pixel positions and manages ball rendering.
 */
public class MovementHandler {
    private final ClientApi clientApi;
    public Map<Integer, BallRender> players = new HashMap<>();
    private Pane gamePane;

    private static final long STILL_TIME_THRESHOLD_MS = 150;
    private final Map<Integer, Long> lastUpdateTime = new ConcurrentHashMap<>();
    private MessageParser.Message lastMessage = null;

    public MovementHandler(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

    public void setGamePane(Pane pane) {
        this.gamePane = pane;
        this.players.clear();
        this.lastUpdateTime.clear();
        
        if (lastMessage != null) {
            onMoveUpdate(lastMessage);
        }
    }

    public void onMoveUpdate(MessageParser.Message message) {
        this.lastMessage = message;
        if (gamePane == null) return;

        String payload = message.payload;
        if (payload == null || payload.isEmpty()) return;

        double currentWidth = gamePane.getWidth() > 0 ? gamePane.getWidth() : 1100.0;
        double currentHeight = gamePane.getHeight() > 0 ? gamePane.getHeight() : 600.0;

        double scaleX = currentWidth / 80.0;
        double scaleY = currentHeight / 45.0;

        java.util.Set<Integer> receivedIds = new java.util.HashSet<>();
        String[] playerUpdates = payload.isEmpty() ? new String[0] : payload.split(";");

        for (String update : playerUpdates) {
            String[] parts = update.split(":");
            if (parts.length == 2) {
                try {
                    int playerId = Integer.parseInt(parts[0]);
                    receivedIds.add(playerId);
                    String[] coords = parts[1].split(",");

                    double gridX = Double.parseDouble(coords[0]);
                    double gridY = Double.parseDouble(coords[1]);

                    double pixelX = gridX * scaleX;
                    double pixelY = gridY * scaleY;

                    Platform.runLater(() -> {
                        BallRender render = players.get(playerId);
                        boolean genuinelyMoved = true;

                        if (render == null) {
                            render = new BallRender(gamePane, new Ball(pixelX, pixelY));
                            players.put(playerId, render);
                        } else {
                            // Check if the difference is substantial enough to disrupt "stillness"
                            double dx = render.getCircle().getCenterX() - pixelX;
                            double dy = render.getCircle().getCenterY() - pixelY;
                            if (Math.abs(dx) < 0.1 && Math.abs(dy) < 0.1) {
                                genuinelyMoved = false;
                            } else {
                                render.updatePosition(new Ball(pixelX, pixelY));
                            }
                        }

                        if (genuinelyMoved || !lastUpdateTime.containsKey(playerId)) {
                            lastUpdateTime.put(playerId, System.currentTimeMillis());
                        }
                    });
                } catch (Exception ignored) {}
            }
        }

        Platform.runLater(() -> {
            players.entrySet().removeIf(entry -> {
                if (!receivedIds.contains(entry.getKey())) {
                    entry.getValue().remove();
                    lastUpdateTime.remove(entry.getKey());
                    return true;
                }
                return false;
            });
        });
    }

    /**
     * Determines if a player's ball has come to a complete physical stop on the map.
     * <p>
     * <b>Why we wait for a timeout instead of checking Velocity:</b><br>
     * The client is visually "dumb" and runs no physics logic internally. Sending live velocity
     * (vx, vy) data continuously over the network strictly to check if a ball has stopped
     * would drastically inflate bandwidth consumption. Instead, the server automatically 
     * optimizes the network by completely dropping stationary balls from its physics broadcasts.
     * Because the server halts updates naturally when a ball stops, the client securely infers
     * global stillness if the ball's coordinates haven't changed in {@value #STILL_TIME_THRESHOLD_MS}ms.
     *
     * @param playerId the server-assigned player ID
     * @return true if the ball has not visibly moved recently, false otherwise
     */
    public boolean isPlayerBallStill(int playerId) {
        Long lastUpdate = lastUpdateTime.get(playerId);
        return lastUpdate != null && (System.currentTimeMillis() - lastUpdate > STILL_TIME_THRESHOLD_MS);
    }
}
