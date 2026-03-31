package org.golfit.client.clientLogic;

/** Defines the client-side API for sending protocol messages to the server. */
public interface IClientApi {
    void connect(String username) throws Exception;
    void ping() throws Exception;
    void lobbyList() throws Exception;
    void createLobby() throws Exception;
    void joinLobby(String lobbyName) throws Exception;
    void leaveLobby(String lobbyId) throws Exception;
    void sendChatBroadcast(String data) throws Exception;
    void sendPrivateChat(String data) throws Exception;
    void sendMovement(double forceX, double forceY) throws Exception;
    void sendLobbyChat(String data) throws Exception;
}
