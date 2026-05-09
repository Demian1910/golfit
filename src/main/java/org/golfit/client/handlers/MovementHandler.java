package org.golfit.client.handlers;

import org.golfit.client.clientLogic.ClientApi;
import org.golfit.client.eventBus.EventBus;
import org.golfit.client.gui.settings.ClientPreferences;
import org.golfit.shared.constants.Constants;
import org.golfit.shared.messageParser.MessageParser;
import org.golfit.client.game.BallRender;
import org.golfit.client.game.Ball;
import javafx.scene.layout.Pane;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles movement update messages from the server on the client side.
 * Converts server grid coordinates to pixel positions and manages ball rendering.
 */
public class MovementHandler {
    private final ClientApi clientApi;

    /** Thread-safe player render map */
    public Map<Integer, BallRender> players = new ConcurrentHashMap<>();

    private Pane gamePane;

    private static final javafx.scene.paint.Color[] PLAYER_COLORS = {
            javafx.scene.paint.Color.RED,
            javafx.scene.paint.Color.BLUE,
            javafx.scene.paint.Color.GREEN,
            javafx.scene.paint.Color.GOLD,
            javafx.scene.paint.Color.PURPLE,
            javafx.scene.paint.Color.ORANGE
    };

    private static final long STILL_TIME_THRESHOLD_MS = 150;
    private final Map<Integer, Long> lastUpdateTime = new ConcurrentHashMap<>();
    
    private final List<Integer> joinOrder = new ArrayList<>();

    private MessageParser.Message lastMessage = null;

    public MovementHandler(ClientApi clientApi) {
        this.clientApi = clientApi;
        EventBus.get().on(Constants.BALL_COLOR_CHANGED, data -> {
            Color newColor = (Color) data;
            Platform.runLater(() -> {
                BallRender myRender = players.get(clientApi.getId());
                if (myRender != null) myRender.changeColor(newColor);
            });
        });
    }

    public void setGamePane(Pane pane) {
        this.gamePane = pane;
        this.players.clear();
        this.lastUpdateTime.clear();
        this.joinOrder.clear();

        if (lastMessage != null) {
            onMoveUpdate(lastMessage);
        }
    }

    public void onMoveUpdate(MessageParser.Message message) {
        this.lastMessage = message;
        if (gamePane == null) return;

        String payload = message.payload;
        if (payload == null || payload.isEmpty()) return;

        double width = gamePane.getWidth() > 0 ? gamePane.getWidth() : 1100.0;
        double height = gamePane.getHeight() > 0 ? gamePane.getHeight() : 600.0;

        double scaleX = width / 80.0;
        double scaleY = height / 45.0;

        Map<Integer, double[]> parsedUpdates = new HashMap<>();
        Set<Integer> receivedIds = new HashSet<>();

        for (String update : payload.split(";")) {
            String[] parts = update.split(":");
            if (parts.length != 2) continue;

            try {
                int playerId = Integer.parseInt(parts[0]);
                String[] coords = parts[1].split(",");

                double x = Double.parseDouble(coords[0]) * scaleX;
                double y = Double.parseDouble(coords[1]) * scaleY;

                if (!joinOrder.contains(playerId)) {
                    joinOrder.add(playerId);
                }

                parsedUpdates.put(playerId, new double[]{x, y});
                receivedIds.add(playerId);

            } catch (Exception e) {
                System.err.println("Failed to parse movement update: " + update);
            }
        }

        // 🔥 Single UI update instead of many
        Platform.runLater(() -> {

            // Update / create players
            for (var entry : parsedUpdates.entrySet()) {
                int playerId = entry.getKey();
                double pixelX = entry.getValue()[0];
                double pixelY = entry.getValue()[1];

                BallRender render = players.get(playerId);
                boolean moved = true;

                if (render == null) {
                    int index = joinOrder.indexOf(playerId);
                    var color = PLAYER_COLORS[index % PLAYER_COLORS.length];
                    if (playerId == clientApi.getId()
                            && ClientPreferences.getPreferredBallColor() != null) {
                        color = ClientPreferences.getPreferredBallColor();
                    }
                    render = new BallRender(gamePane, new Ball(pixelX, pixelY), color);
                    players.put(playerId, render);
                } else {
                    double dx = render.getCircle().getCenterX() - pixelX;
                    double dy = render.getCircle().getCenterY() - pixelY;

                    if (Math.abs(dx) < 0.1 && Math.abs(dy) < 0.1) {
                        moved = false;
                    } else {
                        render.updatePosition(new Ball(pixelX, pixelY));
                    }
                }

                if (moved || !lastUpdateTime.containsKey(playerId)) {
                    lastUpdateTime.put(playerId, System.currentTimeMillis());
                }
            }

            // Remove missing players
            players.entrySet().removeIf(entry -> {
                if (receivedIds.contains(entry.getKey())) {
                    return false;
                } else {
                    entry.getValue().remove();
                    lastUpdateTime.remove(entry.getKey());
                    return true;
                }
            });
        });
    }

    public boolean isPlayerBallStill(int playerId) {
        Long lastUpdate = lastUpdateTime.get(playerId);
        return lastUpdate != null &&
                (System.currentTimeMillis() - lastUpdate > STILL_TIME_THRESHOLD_MS);
    }
}