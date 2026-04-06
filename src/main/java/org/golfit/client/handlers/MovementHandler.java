package org.golfit.client.handlers;

import org.golfit.client.clientLogic.ClientApi;
import org.golfit.shared.messageParser.MessageParser;
import org.golfit.client.game.BallRender;
import org.golfit.client.game.Ball;
import javafx.scene.layout.Pane;
import javafx.application.Platform;

import java.util.HashMap;
import java.util.Map;

public class MovementHandler {
    private final ClientApi clientApi;
    public Map<Integer, BallRender> players = new HashMap<>();
    private Pane gamePane;

    public MovementHandler(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

    public void setGamePane(Pane pane) {
        this.gamePane = pane;
    }

    public void onMoveUpdate(MessageParser.Message message) {
        if (gamePane == null) return;

        String payload = message.payload;
        if (payload == null || payload.isEmpty()) return;

        // FIXED: Re-added the fallback so the ball spawns immediately even if
        // the layout pass hasn't finished (width is 0).
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

                    // GRID-Coordinates of Server (e.g. 40.5)
                    double gridX = Double.parseDouble(coords[0]);
                    double gridY = Double.parseDouble(coords[1]);

                    // CONVERSION: Grid -> Pixel
                    double pixelX = gridX * scaleX;
                    double pixelY = gridY * scaleY;

                    Platform.runLater(() -> {
                        BallRender render = players.get(playerId);
                        if (render == null) {
                            // If the ball is new, calculate Pixel-Value
                            Ball initialBall = new Ball(pixelX, pixelY);
                            render = new BallRender(gamePane, initialBall);
                            players.put(playerId, render);

                            // Debug to see if it's being created
                            System.out.println("Ball created for ID " + playerId + " at " + pixelX + "," + pixelY);
                        } else {
                            // Move the existing ball to the new pixel coordinates
                            Ball tempBall = new Ball(pixelX, pixelY);
                            render.updatePosition(tempBall);
                        }
                    });
                } catch (Exception e) {
                    // Silently catch parsing errors
                }
            }
        }

        // Cleanup: Remove balls that are no longer in the server payload (e.g., they went into the hole)
        Platform.runLater(() -> {
            players.entrySet().removeIf(entry -> {
                if (!receivedIds.contains(entry.getKey())) {
                    // Remove both the circle and the aim line from the UI
                    gamePane.getChildren().removeAll(
                        entry.getValue().getCircle()
                    );
                    return true; // remove from map
                }
                return false;
            });
        });
    }
}
