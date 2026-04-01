package org.golfit.server.handlers;

import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.ServerContext;
import org.golfit.server.serverApi.ServerApi;
import org.golfit.shared.lobby.Lobby;
import org.golfit.shared.messageParser.MessageParser;
import org.golfit.shared.protocol.Protocol;

import java.util.LinkedHashSet;
import java.util.Map;

/** Handles all system-level protocol messages on the server side (connect, disconnect, chat, lobbies, movement). */
public class SystemHandler {

    private final ServerApi serverApi;
    private final ServerContext serverContext;

    public SystemHandler(ServerApi serverApi, ServerContext serverContext) {
        this.serverApi = serverApi;
        this.serverContext = serverContext;
    }

    /** Handles a client connection or username change. Releases any old username, claims the new one, and sends back the assignment. */
    public void onConnect(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        //Release the old username so it can be reclaimed (during /changeUsername)
        String previousUsername = clientHandler.getUsername();
        if(previousUsername != null) {
            serverApi.releaseUsername(previousUsername);
        }

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

        // Send back: assignedId:finalUsername
        String payload = clientHandler.getId() + ":" + username;
        serverApi.sendConnected(clientHandler, payload);
    }
    /** Handles a ping from a client by updating their last activity timestamp and responding with a pong. */
    public void onPing(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        clientHandler.updateLastPing();
        serverApi.sendPong(clientHandler);
    }

    /** Handles a public chat message by prefixing the sender's username and broadcasting to all clients. */
    public void onChatBroadcast(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
       String payload = clientHandler.getUsername() + ":" + message.payload;
        serverApi.broadcastChat(Protocol.ID_SERVER, payload);
    }

    /** Handles a private chat message by forwarding it to the target client. */
    public void onChatPrivate(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        String[] parts = message.payload.split(":", 2);
        if (parts.length < 2) {
            System.out.println("Invalid private chat format from " + clientHandler.getUsername());
            return;
        }
        String targetUsername = parts[0];
        String data = clientHandler.getUsername() + ":" + parts[1];
        Map<String, ClientHandler> targetClientHandler = serverApi.getClients();
        for(ClientHandler client : targetClientHandler.values()) {
            if(client.getUsername().equals(targetUsername)) {
                serverApi.sendPrivateChat(client, client.getId(), data);
            }
        }
    }


    public void onChatLobby(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        String[] parts = message.payload.split(":", 2);
        if (parts.length < 2) return;
        int lobbyId = Integer.parseInt(parts[0]);
        String payload = clientHandler.getUsername() + ":" + parts[1];

        Lobby lobby = serverContext.getLobbies().get(lobbyId);
        if (lobby != null) {
            lobby.getClients().forEach(client -> {
                try {
                    serverApi.sendLobbyChat(client, payload);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * Handles client disconnection by notifying the server API.
     */
    public void onDisconnect(ClientHandler clientHandler) {
        System.out.println("Client " + clientHandler.getUsername() + " disconnected");
        serverApi.disconnectClient(clientHandler);
    }

    /** Handles a lobby creation request. */
    public void onCreateLobby(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        if (serverContext.getLobbies().size() >= 10) {
            System.out.println("Max lobby limit reached (10)");
            return;
        }
        // Find the lowest available ID from 1-10
        int lobbyId = 1;
        while (serverContext.getLobbies().containsKey(lobbyId) && lobbyId <= 10) lobbyId++;
        serverContext.getLobbies().put(lobbyId, new Lobby(lobbyId, clientHandler));
        serverApi.getClients().values().forEach(client -> {
            try {
                serverApi.sendLobbyList(client);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** Handles a request for the list of available lobbies. */
    public void onLobbyList(ClientHandler clientHandler) throws Exception {
        serverApi.sendLobbyList(clientHandler);
    }

    /** Handles a request to join a lobby. */
    public void onLobbyJoin(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        Lobby lobby = serverContext.getLobbies().get(Integer.parseInt(message.payload));
        if (lobby == null) {
            System.out.println("Lobby not found: " + message.payload);
            return;
        }
        lobby.addClient(clientHandler);
        // Send join confirmation to the joining client
        clientHandler.getChannelManager().send(
                Protocol.SYS_LOBBY_JOINED, Protocol.CH_SYSTEM, Protocol.ID_SERVER, message.payload);
        LinkedHashSet<ClientHandler> clients = lobby.getClients();
        clients.forEach((client -> {
            try {
                serverApi.sendLobbyInfo(client,message.payload);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
        serverApi.getClients().values().forEach(client -> {
            try {
                serverApi.sendLobbyList(client);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** Handles a request to leave the current lobby. */
    public void onLobbyLeave(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        Lobby lobby = serverContext.getLobbies().get(Integer.parseInt(message.payload));
        if (lobby == null) {
            System.out.println("Lobby not found: " + message.payload);
            return;
        }
        lobby.removeClient(clientHandler);
        if (lobby.getClients().isEmpty()) {
            serverContext.getLobbies().remove(lobby.getId());
        } else {
            lobby.getClients().forEach((client -> {
                try {
                    serverApi.sendLobbyInfo(client, message.payload);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        serverApi.sendLeaveLobby(clientHandler, message.payload);
        serverApi.getClients().values().forEach(client -> {
            try {
                serverApi.sendLobbyList(client);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** Handles movement input from a client. */
    public void onMoveInput(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        System.out.println("Client " + clientHandler.getUsername() + " sent move input");
    }
}
