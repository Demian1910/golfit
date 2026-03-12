package org.golfit.client;

public interface IClientApi {
    void connect(String username) throws Exception;
    void ping() throws Exception;
    void lobbyList() throws Exception;
    void createLobby() throws Exception;
    void joinLobby() throws Exception;
    void leaveLobby() throws Exception;
    void sendChat(String data) throws Exception;
    void sendPrivateChat(String data) throws Exception;
    void sendMovement(Object movementData) throws Exception;
}
