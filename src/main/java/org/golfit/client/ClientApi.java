package org.golfit.client;

import org.golfit.shared.protocol.Protocol;
import org.golfit.shared.protocol.channelManager.ChannelManager;

import java.math.BigInteger;

public class ClientApi implements IClientApi {
    private final ChannelManager channelManager;
    private final Client client;

    public ClientApi(ChannelManager channelManager, Client client) {
        this.channelManager = channelManager;
        this.client = client;
    }

    @Override
    public void connect(String username) throws Exception {
        if(username != null) {
            byte[] payload = username.getBytes();
            channelManager.send(Protocol.CH_SYSTEM, Protocol.PKG_SYS_CONNECT, Protocol.ID_SERVER, payload);
        }
    }

    @Override
    public void disconnect() throws Exception {
        channelManager.send(Protocol.CH_SYSTEM, Protocol.PKG_SYS_DISCONNECT, Protocol.ID_SERVER);
    }

    @Override
    public void ping() throws Exception {
        channelManager.send(Protocol.CH_SYSTEM, Protocol.PKG_SYS_PING, Protocol.ID_SERVER);
    }

    @Override
    public void requestLobbies() throws Exception {
        channelManager.send(Protocol.CH_SYSTEM, Protocol.PKG_SYS_LOBBY_LIST, Protocol.ID_SERVER);
    }

    @Override
    public void createLobby() throws Exception {
        channelManager.send(Protocol.CH_SYSTEM, Protocol.PKG_SYS_CREATE_LOBBY, Protocol.ID_SERVER);
    }

    @Override
    public void joinLobby(int lobbyId) throws Exception {
        byte[] buffer = BigInteger.valueOf(lobbyId).toByteArray();
        channelManager.send(Protocol.CH_SYSTEM, Protocol.PKG_SYS_CREATE_LOBBY, Protocol.ID_SERVER,buffer);
    }

    @Override
    public void sendChat(String data) throws Exception {
        byte[] buffer = data.getBytes();
        channelManager.send(Protocol.CH_CHAT, Protocol.PKG_SYS_CHAT, Protocol.ID_SERVER, buffer);
    }

    @Override
    public void sendPrivateChat(String data, int targetId) throws Exception {
        byte[] buffer = data.getBytes();
        channelManager.send(Protocol.CH_CHAT, Protocol.PKG_SYS_CHAT_PRIVATE, Protocol.ID_SERVER, buffer);
    }

    @Override
    public void sendMovement(Object movementData) throws Exception {
        //TODO: Decide on movement data datatype to add to function parameters
        //TODO: Convert  movement data to byte[]
        channelManager.send(Protocol.CH_MOVEMENT, Protocol.PKG_MOVE_INPUT, Protocol.ID_SERVER); //TODO: Add movement data as last parameter
    }

    public void setId(int id) {
        client.id = id;
    }
}
