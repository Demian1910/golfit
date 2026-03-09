package org.golfit.server.handlers;

import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.serverApi.ServerApi;
import org.golfit.shared.packetParser.MessageParser;

public class SystemHandler {

    private final ServerApi serverApi;

    public SystemHandler(ServerApi serverApi) {
        this.serverApi = serverApi;
    }

    public void onConnect(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        if(message.payload != null) {
            String username = message.payload;
            clientHandler.setUsername(username);
            System.out.println("Client " + clientHandler.getId() + " connected with username " + username);
        }
        serverApi.sendConnected(clientHandler, clientHandler.getId());
    }
    public void onPing(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        clientHandler.updateLastPing();
        serverApi.sendPong(clientHandler);
    }
}
