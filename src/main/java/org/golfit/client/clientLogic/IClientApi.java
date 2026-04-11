package org.golfit.client.clientLogic;

/** Defines the client-side API for sending protocol messages to the server. */
public interface IClientApi {
    /** Sends a connection request with the given username. */
    void connect(String username) throws Exception;

    /** Sends a ping to the server. */
    void ping() throws Exception;

    /** Requests the list of available lobbies. */
    void lobbyList() throws Exception;

    /** Requests creation of a new lobby. */
    void createLobby() throws Exception;

    /** Requests to join the lobby with the given name/ID. */
    void joinLobby(String lobbyName) throws Exception;

    /** Requests to leave the lobby with the given ID. */
    void leaveLobby(String lobbyId) throws Exception;

    /** Sends a broadcast chat message to all players. */
    void sendChatBroadcast(String data) throws Exception;

    /** Sends a private chat message (payload includes target). */
    void sendPrivateChat(String data) throws Exception;

    /** Sends a movement input with the given force vector. */
    void sendMovement(double forceX, double forceY) throws Exception;

    /** Sends a chat message to the current lobby. */
    void sendLobbyChat(String data) throws Exception;
}
