package org.golfit.client;

public interface IClientApi {
    void connect(String username) throws Exception;

    void disconnect() throws Exception;
    void ping() throws Exception;
    void requestLobbies() throws Exception;
    void createLobby() throws Exception;
    void joinLobby(int lobbyId) throws Exception;
    void sendChat(String data) throws Exception;
    void sendPrivateChat(String data, int targetId) throws Exception;
    void sendMovement(Object movementData) throws Exception;
}
