package org.golfit.server.serverApi;

import org.golfit.server.ClientHandler.ClientHandler;

/** Defines the server-side API for sending protocol messages to clients. */
public interface IServerApi {
    void sendConnected(ClientHandler clientHandler, String payload) throws Exception;
    void sendPong(ClientHandler clientHandler) throws Exception;
    void sendCreateLobby(ClientHandler clientHandler, String lobbyId) throws Exception;
    void sendLobbyList(ClientHandler clientHandler) throws Exception;
    void sendPrivateChat(ClientHandler clientHandler, int targetId, String data) throws Exception;
    void broadcastChat(int senderId, String data) throws Exception;
    void sendLobbyChat(ClientHandler clientHandler, String data) throws Exception;
    void broadcastMovement(String payload) throws Exception;


}
