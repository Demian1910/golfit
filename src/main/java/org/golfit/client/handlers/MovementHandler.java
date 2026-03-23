package org.golfit.client.handlers;

import org.golfit.client.ClientApi;
import org.golfit.shared.packetParser.MessageParser;
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
        if (gamePane == null)
            return; // UI not ready

        String payload = message.payload;
        if (payload == null || payload.isEmpty())
            return;

        String[] playerUpdates = payload.split(";");
        for (String update : playerUpdates) {
            if (update.isEmpty())
                continue;
            String[] parts = update.split(":");
            if (parts.length == 2) {
                try {
                    int playerId = Integer.parseInt(parts[0]);
                    String[] coords = parts[1].split(",");
                    double x = Double.parseDouble(coords[0]);
                    double y = Double.parseDouble(coords[1]);

                    Platform.runLater(() -> {
                        BallRender render = players.get(playerId);
                        if (render == null) {
                            // Player not seen yet, create it
                            // Using a default color, we could use different colors based on ID
                            Ball initialBall = new Ball(x, y);
                            render = new BallRender(gamePane, initialBall);
                            players.put(playerId, render);
                        } else {
                            // Update existing
                            Ball tempBall = new Ball(x, y);
                            render.updatePosition(tempBall);
                        }
                    });
                } catch (NumberFormatException e) {
                }
            }
        }
    }
}
