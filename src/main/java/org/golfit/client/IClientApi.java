package org.golfit.client;

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
    void sendMovement(Object movementData) throws Exception;
    void sendLobbyChat(String data) throws Exception;
}
