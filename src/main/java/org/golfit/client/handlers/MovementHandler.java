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

        // CALCULATE BASED ON GRID!
        double currentWidth = gamePane.getWidth() > 0 ? gamePane.getWidth() : 1100.0;
        double currentHeight = gamePane.getHeight() > 0 ? gamePane.getHeight() : 600.0;

        double scaleX = currentWidth / 80.0;  // COLS = 80
        double scaleY = currentHeight / 45.0; // ROWS = 45

        String[] playerUpdates = payload.split(";");
        for (String update : playerUpdates) {
            String[] parts = update.split(":");
            if (parts.length == 2) {
                try {
                    int playerId = Integer.parseInt(parts[0]);
                    String[] coords = parts[1].split(",");

                    // Das sind die GRID-Koordinaten vom Server (z.B. 40.5)
                    double gridX = Double.parseDouble(coords[0]);
                    double gridY = Double.parseDouble(coords[1]);

                    // UMRECHNUNG: Grid -> Pixel
                    double pixelX = gridX * scaleX;
                    double pixelY = gridY * scaleY;

                    Platform.runLater(() -> {
                        BallRender render = players.get(playerId);
                        if (render == null) {
                            // Falls der Ball neu ist, mit Pixel-Werten erstellen
                            Ball initialBall = new Ball(pixelX, pixelY);
                            render = new BallRender(gamePane, initialBall);
                            players.put(playerId, render);
                        } else {
                            // Bestehenden Ball auf die neuen Pixel-Koordinaten schieben
                            Ball tempBall = new Ball(pixelX, pixelY);
                            render.updatePosition(tempBall);
                        }
                    });
                } catch (NumberFormatException e) {
                    // Ignore malformed numbers
                }
            }
        }
    }
}
