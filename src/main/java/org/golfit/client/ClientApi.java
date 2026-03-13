package org.golfit.client;

import org.golfit.shared.protocol.Protocol;
import org.golfit.shared.protocol.channelManager.ChannelManager;

import java.math.BigInteger;

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
     * Connects the client to the game server with the specified username.
     *
     * @param username The username to use for connecting.
     * @throws Exception if the connection fails
     */
    @Override
    public void  connect(String username) throws Exception {
        if(username != null) {
            String payload = username;
            channelManager.send( Protocol.SYS_CONNECT,Protocol.CH_SYSTEM, getId() , payload);
        }
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
    public void joinLobby() throws Exception {
        String buffer = "";
        channelManager.send(Protocol.SYS_LOBBY_JOIN, Protocol.CH_SYSTEM, getId(), buffer);
    }

    /**
     * Leaves the current lobby on the server.
     *
     * @throws Exception if the lobby leave fails
     */
    @Override
    public void leaveLobby() throws Exception {
        //TODO: Send lobby id as payload, get it from client via ge
        channelManager.send(Protocol.SYS_LOBBY_LEAVE, Protocol.CH_SYSTEM, getId(), "");
    }

    /**
     * Sends a private chat message to the specified target.
     *
     * @param data The chat message content
     * @throws Exception if the private chat fails
     */
    @Override
    public void sendChat(String data) throws Exception {
        channelManager.send( Protocol.SYS_CHAT,Protocol.CH_CHAT, getId(), data);
    }

    /**
     * Sends a private chat message to the specified target.
     *
     * @throws Exception if the private chat fails
     */
    @Override
    public void sendPrivateChat(String data) throws Exception {
        channelManager.send( Protocol.SYS_CHAT_PRIVATE,Protocol.CH_CHAT, getId(), data);
    }

    /**
     * Sends movement data to the server.
     *
     * @param movementData The movement data to be sent
     * @throws Exception if the movement data cannot be sent
     */
    @Override
    public void sendMovement(Object movementData) throws Exception {
        //TODO: Decide on movement data datatype to add to function parameters
        //TODO: Convert  movement data to byte[]
        channelManager.send( Protocol.MOVE_INPUT,Protocol.CH_SYSTEM,
                getId(), ""); //TODO: Add movement data as last parameter
    }

    public void setId(int id) {
        client.id = id;
    }
    public int getId() {
        return client.id;
    }

    public void resetChannels(){
        channelManager.resetChannels();
    }

    public String getUsername() {
        return client.username;
    }

    public void setUsername(String username) {
        client.username = username;
    }

    public void shutdown(){
        try {
            channelManager.send(Protocol.SYS_DISCONNECT, Protocol.CH_PINGS, getId(), "");
        } catch (Exception ignored) {}
        client.pingService.stop();
        client.socket.close();
        System.exit(0);
    }
}
