package org.golfit.server.serverApi;

import org.golfit.server.ClientHandler.ClientHandler;

public interface IServerApi {
    public void sendConnected(ClientHandler clientHandler, int assignedId) throws Exception;

    public void sendPong(ClientHandler clientHandler) throws Exception;

    public void sendLobbyList(ClientHandler clientHandler) throws Exception;

    public void sendPrivateChat(ClientHandler clientHandler, int targetId, String data) throws Exception;

    public void broadcastChat(int senderId, String data) throws Exception;

    public void broadcastMovement(String payload) throws Exception;

}
