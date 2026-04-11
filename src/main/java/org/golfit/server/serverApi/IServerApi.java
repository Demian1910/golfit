package org.golfit.server.serverApi;

import org.golfit.server.ClientHandler.ClientHandler;

/** Defines the server-side API for sending protocol messages to clients. */
public interface IServerApi {
    /** Sends a connection confirmation to the client with their assigned ID and username. */
    void sendConnected(ClientHandler clientHandler, String payload) throws Exception;

    /** Sends a pong response to the client. */
    void sendPong(ClientHandler clientHandler) throws Exception;

    /** Sends a lobby-created confirmation to the requesting client. */
    void sendCreateLobby(ClientHandler clientHandler, String lobbyId) throws Exception;

    /** Sends the list of available lobbies to the client. */
    void sendLobbyList(ClientHandler clientHandler) throws Exception;

    /** Sends a private chat message to the specified client. */
    void sendPrivateChat(ClientHandler clientHandler, int targetId, String data) throws Exception;

    /** Broadcasts a chat message to all connected clients. */
    void broadcastChat(int senderId, String data) throws Exception;

    /** Sends a lobby chat message to the specified client. */
    void sendLobbyChat(ClientHandler clientHandler, String data) throws Exception;

    /** Broadcasts movement updates to all connected clients. */
    void broadcastMovement(String payload) throws Exception;


}
