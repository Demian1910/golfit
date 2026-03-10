package org.golfit.server.handlers;

import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.serverApi.ServerApi;
import org.golfit.shared.messageParser.MessageParser;

public class SystemHandler {

    private final ServerApi serverApi;

    public SystemHandler(ServerApi serverApi) {
        this.serverApi = serverApi;
    }

    public void onConnect(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        clientHandler.getChannelManager().resetChannels();
        String username = (message.payload != null && !message.payload.isEmpty())
                ? message.payload : "player";

        if (!serverApi.claimUsername(username)) {
            String base = username;
            int counter = 1;
            do {
                username = base + "_" + String.format("%03d", counter);
                counter++;
            } while (!serverApi.claimUsername(username));
        }

        clientHandler.setUsername(username);
        System.out.println("Client " + clientHandler.getId() + " connected with username " + username);

        // Send back: assignedId|finalUsername
        String payload = clientHandler.getId() + "|" + username;
        serverApi.sendConnected(clientHandler, payload);
    }
    public void onPing(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        clientHandler.updateLastPing();
        serverApi.sendPong(clientHandler);
    }
}
