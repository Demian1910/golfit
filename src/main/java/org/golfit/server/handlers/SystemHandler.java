package org.golfit.server.handlers;

import org.golfit.client.Client;
import org.golfit.client.ClientApi;
import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.serverApi.ServerApi;
import org.golfit.shared.messageParser.MessageParser;
import org.golfit.shared.protocol.Protocol;

public class SystemHandler {

    private final ServerApi serverApi;

    public SystemHandler(ServerApi serverApi) {
        this.serverApi = serverApi;
    }

    public void onConnect(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
//        clientHandler.getChannelManager().resetChannels();
        String username = (message.payload != null && !message.payload.isEmpty())
                ? message.payload : "player";

        //Release the old username so it can be reclaimed (during /changeUsername)
        String oldUsername = clientHandler.getUsername();
        if (oldUsername != null) {
            serverApi.releaseUsername(oldUsername);
        }

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

        // Send back: assignedId:finalUsername
        String payload = clientHandler.getId() + ":" + username;
        serverApi.sendConnected(clientHandler, payload);
    }
    public void onPing(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        clientHandler.updateLastPing();
        serverApi.sendPong(clientHandler);
    }

    public void onChat(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
       String payload = clientHandler.getUsername() + ":" + message.payload;
        serverApi.broadcastChat(Protocol.ID_SERVER, payload);
    }

    public void onChatPrivate(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        serverApi.sendPrivateChat(clientHandler, Protocol.ID_SERVER, message.payload);
    }

    /**
     * Handles client disconnection by notifying the server API.
     */
    public void onDisconnect(ClientHandler clientHandler) {
        System.out.println("Client " + clientHandler.getUsername() + " disconnected");
        serverApi.disconnectClient(clientHandler);
    }

    public void onCreateLobby(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        serverApi.createLobby(clientHandler, "");
    }

    public void onLobbyList(ClientHandler clientHandler) throws Exception {
        serverApi.sendLobbyList(clientHandler);
    }

    public void onLobbyJoin(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        serverApi.joinLobby(clientHandler, message.payload);
    }

    public void onLobbyLeave(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        System.out.println("Client " + clientHandler.getUsername() + " left lobby");
    }

    public void onMoveInput(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        System.out.println("Client " + clientHandler.getUsername() + " sent move input");
    }
}
