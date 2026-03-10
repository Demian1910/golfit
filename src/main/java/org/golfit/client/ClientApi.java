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
    public void  connect(String username) throws Exception {
        if(username != null) {
            String payload = username;
            System.out.println("Connecting with username: " + username);
            channelManager.send( Protocol.SYS_CONNECT,Protocol.CH_SYSTEM, Protocol.ID_SERVER, payload);
        }
    }

    @Override
    public void disconnect() throws Exception {
        channelManager.send(Protocol.SYS_DISCONNECT,Protocol.CH_SYSTEM, Protocol.ID_SERVER,"");
    }

    @Override
    public void ping() throws Exception {
        channelManager.send( Protocol.SYS_PING, Protocol.CH_PINGS,Protocol.ID_SERVER, "");
    }

    @Override
    public void requestLobbies() throws Exception {
        channelManager.send( Protocol.SYS_LOBBY_LIST, Protocol.CH_SYSTEM,Protocol.ID_SERVER, "");
    }

    @Override
    public void createLobby() throws Exception {
        channelManager.send( Protocol.SYS_CREATE_LOBBY,Protocol.CH_SYSTEM, Protocol.ID_SERVER, "");
    }

    @Override
    public void joinLobby(int lobbyId) throws Exception {
        String buffer = lobbyId + "";
        channelManager.send(Protocol.SYS_JOIN_LOBBY, Protocol.CH_SYSTEM, Protocol.ID_SERVER, buffer);
    }
    @Override
    public void leaveLobby() throws Exception {
        //TODO: Send lobby id as payload, get it from client via ge
        channelManager.send(Protocol.SYS_LEAVE_LOBBY, Protocol.CH_SYSTEM, Protocol.ID_SERVER, "");
    }

    @Override
    public void sendChat(String data) throws Exception {
        channelManager.send( Protocol.SYS_CHAT,Protocol.CH_CHAT, Protocol.ID_SERVER, data);
    }

    @Override
    public void sendPrivateChat(String data, int targetId) throws Exception {
        channelManager.send( Protocol.SYS_CHAT_PRIVATE,Protocol.CH_CHAT, Protocol.ID_SERVER, data);
    }

    @Override
    public void sendMovement(Object movementData) throws Exception {
        //TODO: Decide on movement data datatype to add to function parameters
        //TODO: Convert  movement data to byte[]
        channelManager.send( Protocol.MOVE_INPUT,Protocol.CH_MOVEMENT,
                Protocol.ID_SERVER, ""); //TODO: Add movement data as last parameter
    }

    public void setId(int id) {
        client.id = id;
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
}
