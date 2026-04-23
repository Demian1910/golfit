package org.golfit.client.clientLogic;

import org.golfit.shared.protocol.Protocol;
import org.golfit.shared.protocol.channelManager.ChannelManager;

import java.util.LinkedHashSet;

/**
 * Client API for interacting with the game server.
 *
 * Provides methods for connecting, pinging, requesting lobbies, creating lobbies, and joining lobbies.
 */
public class ClientApi implements IClientApi {
    private final ChannelManager channelManager;
    private final Client client;

    /** Creates a new ClientApi instance.
     * @param channelManager The channel manager to use for sending messages.
     * @param client The client instance.*/
    public ClientApi(ChannelManager channelManager, Client client) {
        this.channelManager = channelManager;
        this.client = client;
    }

    /**
     * Sends a ping message to the server.
     *
     * @throws Exception if the ping fails
     */
    @Override
    public void ping() throws Exception {
        channelManager.send( Protocol.SYS_PING, Protocol.CH_PINGS,getId(), "");
    }

    /**
     * Connects the client to the game server with the specified username.
     *
     * @param username The username to use for connecting.
     * @throws Exception if the connection fails
     */
    @Override
    public void  connect(String username) throws Exception {
        if(username != null) {
            channelManager.send( Protocol.SYS_CONNECT,Protocol.CH_SYSTEM, getId() , username);
        }
    }

    /**
     * Requests a list of available lobbies from the server.
     *
     * @throws Exception if the request fails
     */
    @Override
    public void lobbyList() throws Exception {
        channelManager.send( Protocol.SYS_LOBBY_LIST, Protocol.CH_SYSTEM,getId(), "");
    }

    /**
     * Creates a new lobby on the server.
     *
     * @throws Exception if the lobby creation fails
     */
    @Override
    public void createLobby() throws Exception {
        channelManager.send( Protocol.SYS_LOBBY_CREATE,Protocol.CH_SYSTEM, getId(), "");
    }

    /**
     * Joins the specified lobby on the server.
     * @throws Exception if the lobby join fails
     */
    @Override
    public void joinLobby(String lobbyName) throws Exception {
        channelManager.send(Protocol.SYS_LOBBY_JOIN, Protocol.CH_SYSTEM, getId(), lobbyName);
    }

    /**
     * Leaves the current lobby on the server.
     *
     * @throws Exception if the lobby leave fails
     */
    @Override
    public void leaveLobby(String lobbyId) throws Exception {
        channelManager.send(Protocol.SYS_LOBBY_LEAVE, Protocol.CH_SYSTEM, getId(), lobbyId);
    }

    /**
     * Sends a private chat message to the specified target.
     *
     * @param data The chat message content
     * @throws Exception if the private chat fails
     */
    @Override
    public void sendChatBroadcast(String data) throws Exception {
        channelManager.send( Protocol.SYS_CHAT_BROADCAST,Protocol.CH_CHAT, getId(), data);
    }

    /**
     * Sends a private chat message to the server for routing.
     *
     * @throws Exception if the private chat fails
     */
    @Override
    public void sendPrivateChat(String data) throws Exception {
        channelManager.send( Protocol.SYS_CHAT_PRIVATE,Protocol.CH_CHAT, getId(), data);
    }

    /** Sends a chat message to the current lobby. */
    @Override
    public void sendLobbyChat(String data) throws Exception {
        String payload = getLobbyId() + ":" + data;
        channelManager.send(Protocol.SYS_CHAT_LOBBY,Protocol.CH_CHAT, getId(), payload);
    }

    /** Requests the list of all connected players from the server. */
    public void requestPlayersList() throws Exception {
        channelManager.send(Protocol.SYS_PLAYERS_LIST, Protocol.CH_SYSTEM, getId(), "");
    }

    /**
     * Sends a game-start request to the server for the current lobby.
     * The server will validate that the lobby has 4 players before broadcasting SYS_GAME_START.
     *
     * @throws Exception if sending fails
     */
    public void startGame() throws Exception {
        channelManager.send(Protocol.SYS_LOBBY_START, Protocol.CH_SYSTEM, getId(), String.valueOf(getLobbyId()));
    }

    /**
     * Sends a game-start request to the server for the current lobby.
     * The server will validate that the lobby has 4 players before broadcasting SYS_GAME_START.
     * This is a cheat code that instantly teleports the player to the center of the map.
     *
     * @throws Exception if sending fails
     */
    public void sendHesoyam() throws Exception {
        channelManager.send(Protocol.SYS_HESOYAM, Protocol.CH_SYSTEM, getId(), "");
    }


    /**
     * Sends movement data to the server.
     *
     * @param forceX the horizontal force component
     * @param forceY the vertical force component
     * @throws Exception if the movement data cannot be sent
     */
    @Override
    public void sendMovement(double forceX, double forceY) throws Exception {
        String payload = forceX + "," + forceY;
        channelManager.send(Protocol.MOVE_INPUT, Protocol.CH_MOVEMENT, Protocol.ID_SERVER, payload);
    }

    /** Sets the server-assigned client ID. */
    public void setId(int id) {
        client.id = id;
    }
    /** Returns the server-assigned client ID. */
    public int getId() {
        return client.id;
    }

    /** Resets all channel sequence counters and pending ACKs (used on reconnect). */
    public void resetChannels(){
        channelManager.resetChannels();
    }

    /** Returns the current username. */
    public String getUsername() {
        return client.username;
    }

    /** Sets the current username. */
    public void setUsername(String username) {
        client.username = username;
    }

    /** Sets the ID of the lobby the client is currently in. */
    public void setLobbyId(int lobbyId){
        client.lobbyId = lobbyId;
    }

    /** Returns the set of player usernames in the current lobby. */
    public LinkedHashSet<String> getLobbyPlayers() {
        return client.lobbyPlayers;
    }

    /** Returns the ID of the lobby the client is currently in, or 0 if not in a lobby. */
    public int getLobbyId(){
        return client.lobbyId;
    }

    /** Returns the underlying channel manager. */
    public ChannelManager getChannelManager() {
        return channelManager;
    }

    /** Sends a disconnect message to the server, stops the ping service, and closes the socket. */
    public void shutdown(){
        try {
            channelManager.send(Protocol.SYS_DISCONNECT, Protocol.CH_SYSTEM, getId(), "");
        } catch (Exception ignored) {}
        client.pingService.stop();
        client.socket.close();
    }


}
