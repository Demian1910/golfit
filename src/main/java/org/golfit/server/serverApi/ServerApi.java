package org.golfit.server.serverApi;

import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.ServerContext;
import org.golfit.shared.lobby.Lobby;
import org.golfit.shared.protocol.Protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implements server-side actions: sending responses to clients, managing usernames,
 * and broadcasting messages across lobbies or the entire server.
 */
public class ServerApi implements IServerApi {
    private final ServerContext serverContext;

    /**
     * Creates a new ServerApi backed by the given server context.
     *
     * @param serverContext provides access to clients, lobbies, and usernames
     */
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

    /** Removes a client from the server, cleans up lobby memberships, and releases the username. */
    public void disconnectClient(ClientHandler clientHandler) {
        // Remove client from any lobby they're in. Collect empty lobby IDs first
        // and drop them after iteration to avoid ConcurrentModificationException.
        List<Integer> emptyLobbyIds = new ArrayList<>();
        for (Lobby lobby : serverContext.getLobbies().values()) {
            if (lobby.getClients().contains(clientHandler)) {
                lobby.removeClient(clientHandler);
                if (lobby.getClients().isEmpty()) {
                    emptyLobbyIds.add(lobby.getId());
                }
            }
        }
        for (Integer id : emptyLobbyIds) {
            serverContext.getLobbies().remove(id);
        }

        if (clientHandler.getUsername() != null) {
            serverContext.getUsernameSet().remove(clientHandler.getUsername());
        }

        clientHandler.shutdown();

        if (clientHandler.getClientKey() != null) {
            serverContext.getClients().remove(clientHandler.getClientKey());
        }

        // Notify remaining clients of the updated lobby state
        for (ClientHandler client : serverContext.getClients().values()) {
            try {
                sendLobbyList(client);
            } catch (Exception ignored) {}
        }
    }

    /** Sends a lobby creation confirmation. */
    @Override
    public void sendCreateLobby(ClientHandler clientHandler, String lobbyId) throws Exception {
        clientHandler.getChannelManager().send(Protocol.SYS_LOBBY_CREATED, Protocol.CH_SYSTEM, Protocol.ID_SERVER, lobbyId);
    }

    /** Sends the list of available lobbies to the client. */
    @Override
    public void sendLobbyList(ClientHandler clientHandler) throws Exception {
        String payload = serverContext.getLobbies().values().stream()
                .sorted((a, b) -> Integer.compare(a.getId(), b.getId()))
                .map(Lobby::getLobbyListContext)
                .collect(Collectors.joining("&"));

        clientHandler.getChannelManager().send(
                Protocol.SYS_LOBBY_LIST, Protocol.CH_SYSTEM, Protocol.ID_SERVER, payload
        );
    }

    /** Sends a lobby leave confirmation. */
    public void sendLeaveLobby(ClientHandler clientHandler, String lobbyId) throws Exception {
        clientHandler.getChannelManager().send(Protocol.SYS_LOBBY_LEFT, Protocol.CH_SYSTEM, Protocol.ID_SERVER, lobbyId);
    }

    /** Sends specific lobby information (like player lists) to a client. */
    public void sendLobbyInfo(ClientHandler clientHandler, String lobbyId) throws Exception {
        Lobby lobbyContext = serverContext.getLobbies().get(Integer.parseInt(lobbyId));
        if (lobbyContext == null) return;
        String payload = lobbyContext.getLobbyContext();
        clientHandler.getChannelManager().send(Protocol.SYS_LOBBY_INFO, Protocol.CH_SYSTEM, Protocol.ID_SERVER, payload);
    }

    /** Sends a private chat message. */
    @Override
    public void sendPrivateChat(ClientHandler clientHandler, int targetId, String data) throws Exception {
        clientHandler.getChannelManager().send(Protocol.SYS_CHAT_PRIVATE, Protocol.CH_SYSTEM, targetId, data);
    }

    /** Broadcasts a chat message to every connected client. */
    @Override
    public void broadcastChat(int senderId, String data) throws Exception {
        for (ClientHandler client : serverContext.getClients().values()) {
            client.getChannelManager().send(Protocol.SYS_CHAT_BROADCAST, Protocol.CH_CHAT, senderId, data);
        }
    }

    /** Sends a lobby-specific chat message. */
    @Override
    public void sendLobbyChat(ClientHandler clientHandler, String data) throws Exception {
        clientHandler.getChannelManager().send(Protocol.SYS_CHAT_LOBBY, Protocol.CH_SYSTEM, Protocol.ID_SERVER, data);
    }

    /** Broadcasts movement updates to all connected clients globally. */
    @Override
    public void broadcastMovement(String payload) throws Exception {
        for (ClientHandler client : serverContext.getClients().values()) {
            client.getChannelManager().send(Protocol.MOVE_UPDATE, Protocol.CH_MOVEMENT, Protocol.ID_SERVER, payload);
        }
    }

    /** Broadcasts movement updates only to clients within a specific lobby. */
    public void broadcastMovementToLobby(int lobbyId, String payload) throws Exception {
        Lobby lobby = serverContext.getLobbies().get(lobbyId);
        if (lobby == null) return;
        for (ClientHandler client : lobby.getClients()) {
            client.getChannelManager().send(Protocol.MOVE_UPDATE, Protocol.CH_MOVEMENT, Protocol.ID_SERVER, payload);
        }
    }

    /** Sends the global player list. */
    public void sendPlayerList(ClientHandler clientHandler, String payload) throws Exception {
        clientHandler.getChannelManager().send(Protocol.SYS_PLAYERS_LIST, Protocol.CH_SYSTEM, Protocol.ID_SERVER, payload);
    }

    /** Notifies all lobby members that the game has started. */
    public void sendGameStart(Lobby lobby) throws Exception {
        for (ClientHandler client : lobby.getClients()) {
            client.getChannelManager().send(
                    Protocol.SYS_GAME_START, Protocol.CH_SYSTEM, Protocol.ID_SERVER, String.valueOf(lobby.getId()));
        }
    }

    /** Synchronizes stroke counts for all players in a lobby. */
    public void sendStrokeUpdate(Lobby lobby, String payload) throws Exception {
        for (ClientHandler client : lobby.getClients()) {
            client.getChannelManager().send(
                    Protocol.SYS_STROKE_UPDATE, Protocol.CH_SYSTEM, Protocol.ID_SERVER, payload);
        }
    }

    /** Notifies all lobby members of the final game results. */
    public void sendGameEnd(Lobby lobby, String payload) throws Exception {
        for (ClientHandler client : lobby.getClients()) {
            client.getChannelManager().send(
                    Protocol.SYS_GAME_END, Protocol.CH_SYSTEM, Protocol.ID_SERVER, payload);
        }
    }

    /** Sends historical/total score updates to a specific client. */
    public void sendScoreUpdate(ClientHandler client, String payload) throws Exception {
        client.getChannelManager().send(
                Protocol.SYS_SCORE_UPDATE, Protocol.CH_SYSTEM, Protocol.ID_SERVER, payload);
    }

    /** Broadcasts obstacle changes to all clients in the specified lobby. */
    public void sendObstacleChange(int lobbyId, String payload) throws Exception {
        // Wir holen uns das Objekt hier lokal, damit der Aufrufer es nicht tun muss
        Lobby lobby = serverContext.getLobbies().get(lobbyId);

        if (lobby != null) {
            for (ClientHandler client : lobby.getClients()) {
                client.getChannelManager().send(
                        Protocol.SYS_OBSTACLE_UPDATE, Protocol.CH_SYSTEM, Protocol.ID_SERVER, payload);
            }
        } else {
            System.err.println("Error: ObstacleChange could not be sent. Lobby " + lobbyId + " not found.");
        }
    }

    /** Attempts to claim a username in the server context. */
    public boolean claimUsername(String username) {
        return serverContext.getUsernameSet().add(username);
    }

    /** Releases a username so it can be reclaimed. */
    public void releaseUsername(String username) {
        serverContext.getUsernameSet().remove(username);
    }

    /** Returns the current map of active ClientHandlers. */
    public Map<String, ClientHandler> getClients() {
        return serverContext.getClients();
    }
}