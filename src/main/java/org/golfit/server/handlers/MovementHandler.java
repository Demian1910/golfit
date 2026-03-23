package org.golfit.server.handlers;

import org.golfit.server.serverApi.ServerApi;
import org.golfit.shared.packetParser.MessageParser;
import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.game.ServerGameSimulation;

public class MovementHandler {
    private final ServerApi serverApi;
    private final ServerGameSimulation simulation;

    public MovementHandler(ServerApi serverContext, ServerGameSimulation simulation) {
        this.serverApi = serverContext;
        this.simulation = simulation;
    }

    public void onMoveInput(ClientHandler clientHandler, MessageParser.Message message) {
        String payload = message.payload;
        if (payload == null || payload.isEmpty())
            return;

        // payload expects: "forceX,forceY"
        String[] parts = payload.split(",");
        if (parts.length == 2) {
            try {
                double forceX = Double.parseDouble(parts[0]);
                double forceY = Double.parseDouble(parts[1]);

                // Always use the authoritative ID from the connected client
                simulation.strokeBall(clientHandler.getId(), forceX, forceY);
            } catch (NumberFormatException e) {
                // ignore invalid format
            }
        }
    }
}
