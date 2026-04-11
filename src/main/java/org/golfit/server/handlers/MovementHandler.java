package org.golfit.server.handlers;

import org.golfit.server.serverApi.ServerApi;
import org.golfit.shared.messageParser.MessageParser;
import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.game.ServerGameSimulation;

/**
 * Handles movement input messages from clients on the server side.
 * Parses force vectors and applies them to the physics simulation.
 */
public class MovementHandler {
    private final ServerApi serverApi;
    private final ServerGameSimulation simulation;

    /**
     * Creates a new MovementHandler.
     *
     * @param serverContext the server API for sending responses
     * @param simulation    the game simulation to apply forces to
     */
    public MovementHandler(ServerApi serverContext, ServerGameSimulation simulation) {
        this.serverApi = serverContext;
        this.simulation = simulation;
    }

    /**
     * Handles a movement input from a client. Parses the {@code "forceX,forceY"} payload
     * and applies the stroke to the player's ball in the simulation.
     *
     * @param clientHandler the client that sent the input
     * @param message       the parsed message containing the force payload
     */
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
