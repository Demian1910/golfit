package org.golfit.server.serverApi;

import org.golfit.server.ClientHandler.ClientHandler;

/** Defines the server-side API for sending protocol messages to clients. */
public interface IServerApi {
    public void sendConnected(ClientHandler clientHandler, String payload) throws Exception;
    public void sendPong(ClientHandler clientHandler) throws Exception;
    public void createLobby(ClientHandler clientHandler, String lobbyId) throws Exception;
    public void sendLobbyList(ClientHandler clientHandler) throws Exception;
    public void sendPrivateChat(ClientHandler clientHandler, int targetId, String data) throws Exception;
    public void broadcastChat(int senderId, String data) throws Exception;
    public void broadcastMovement() throws Exception;


}
