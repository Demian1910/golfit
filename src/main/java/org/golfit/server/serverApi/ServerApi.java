package org.golfit.server.serverApi;

import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.ServerContext;
import org.golfit.shared.lobby.Lobby;
import org.golfit.shared.protocol.Protocol;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;


/** Implements server-side actions: sending responses to clients, managing usernames, and broadcasting messages. */
public class ServerApi implements IServerApi{
    private final ServerContext serverContext;

    public ServerApi(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    /** Sends a SYS_CONNECTED response to the client with their assigned ID and username. */
    @Override
    public void sendConnected(ClientHandler clientHandler, String payload) throws Exception {
        clientHandler.getChannelManager().send(Protocol.SYS_CONNECTED, Protocol.CH_SYSTEM, Protocol.ID_SERVER, payload);
    }

    /** Sends a pong response with the current session ID. */
    @Override
    public void sendPong(ClientHandler clientHandler) throws Exception {
        String payload = String.valueOf(serverContext.getSessionId());
        clientHandler.getChannelManager().send(Protocol.SYS_PONG, Protocol.CH_PINGS, Protocol.ID_SERVER, payload);
    }

    /** Removes a client from the server: releases their username, stops their handler, and removes them from the client map. */
    public void disconnectClient(ClientHandler clientHandler) {
        serverContext.getUsernameSet().remove(clientHandler.getUsername());
        clientHandler.shutdown();
        serverContext.getClients().remove(clientHandler.getClientKey());
    }

    /** Creates a lobby and sends a SYS_LOBBY_CREATED confirmation to the requesting client. */
    @Override
    public void sendCreateLobby(ClientHandler clientHandler, String lobbyId) throws Exception {
        clientHandler.getChannelManager().send(Protocol.SYS_LOBBY_CREATED, Protocol.CH_SYSTEM, Protocol.ID_SERVER, lobbyId);
    }

    /** Sends the list of available lobbies to the requesting client. */
    @Override
    public void sendLobbyList(ClientHandler clientHandler) throws Exception {
        String payload = serverContext.getLobbies().values().stream()
                .map(Lobby::getLobbyListContext)
                .collect(Collectors.joining("&")); // no trailing &

        clientHandler.getChannelManager().send(
                Protocol.SYS_LOBBY_LIST, Protocol.CH_SYSTEM, Protocol.ID_SERVER, payload
        );
    }
    /** Sends a lobby leave confirmation to the client. */
    public void sendLeaveLobby(ClientHandler clientHandler, String lobbyId) throws Exception {
        clientHandler.getChannelManager().send(Protocol.SYS_LOBBY_LEFT, Protocol.CH_SYSTEM, Protocol.ID_SERVER, lobbyId);
    }

    public void sendLobbyInfo(ClientHandler clientHandler, String lobbyId) throws Exception {
        Lobby lobbyContext = serverContext.getLobbies().get(Integer.parseInt(lobbyId));
        if (lobbyContext == null) return;
        String payload = lobbyContext.getLobbyContext();
        clientHandler.getChannelManager().send(Protocol.SYS_LOBBY_INFO, Protocol.CH_SYSTEM, Protocol.ID_SERVER, payload);
    }

    /** Sends a private chat message to the specified client. */
    @Override
    public void sendPrivateChat(ClientHandler clientHandler, int targetId, String data) throws Exception {
        clientHandler.getChannelManager().send( Protocol.SYS_CHAT_PRIVATE, Protocol.CH_SYSTEM, targetId, data);
    }

    /** Broadcasts a chat message to all connected clients. */
    @Override
    public void broadcastChat(int senderId, String data) throws Exception {
        for(ClientHandler client : serverContext.getClients().values()) {
            client.getChannelManager().send( Protocol.SYS_CHAT_BROADCAST,Protocol.CH_CHAT, senderId, data);
        }
    }

    @Override
    public void sendLobbyChat(ClientHandler clientHandler, String data) throws Exception {
        clientHandler.getChannelManager().send(Protocol.SYS_CHAT_LOBBY,Protocol.CH_SYSTEM, Protocol.ID_SERVER, data);
    }

    /** Broadcasts movement updates to all connected clients. */
    @Override
    public void broadcastMovement(String payload) throws Exception {
        for(ClientHandler client : serverContext.getClients().values()) {
            client.getChannelManager().send(Protocol.MOVE_UPDATE, Protocol.CH_MOVEMENT, Protocol.ID_SERVER, payload);
        }
    }

    /** Attempts to claim a username. Returns true if the username was available and successfully claimed. */
    public boolean claimUsername(String username) {
        return serverContext.getUsernameSet().add(username);
    }

    /** Releases a previously claimed username so it can be used by another client. */
    public void releaseUsername(String username) {
        serverContext.getUsernameSet().remove(username);
    }

    public Map<String, ClientHandler> getClients() {
        return serverContext.getClients();
    }
}
