package org.golfit.server.serverApi;

import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.ServerContext;
import org.golfit.shared.lobby.Lobby;
import org.golfit.shared.protocol.Protocol;

import java.math.BigInteger;
import java.util.Collection;

import static org.golfit.shared.protocol.Protocol.CH_MOVEMENT;
import static org.golfit.shared.protocol.Protocol.PKG_MOVE_UPDATE;

public class ServerApi implements IServerApi{
    private final ServerContext serverContext;

    public ServerApi(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    @Override
    public void sendConnected(ClientHandler clientHandler, int assignedId) throws Exception {
        byte[] payload = BigInteger.valueOf(assignedId).toByteArray();
        clientHandler.getChannelManager().send(Protocol.CH_SYSTEM, Protocol.PKG_SYS_CONNECTED, Protocol.ID_SERVER, payload);
    }

    @Override
    public void sendPong(ClientHandler clientHandler) throws Exception {
        clientHandler.getChannelManager().send(Protocol.CH_SYSTEM, Protocol.PKG_SYS_PONG, Protocol.ID_SERVER);
    }

    public void disconnectClient(ClientHandler clientHandler) {
        serverContext.getClients().remove(clientHandler.getId());
    }

    @Override
    public void sendLobbyList(ClientHandler clientHandler) throws Exception {
        //TODO: Convert lobbies to byte[]
        Collection<Lobby> lobbies = serverContext.getLobbies();
        byte[] payload = new byte[0];
        clientHandler.getChannelManager().send(Protocol.CH_SYSTEM, Protocol.PKG_SYS_LOBBY_LIST, Protocol.ID_SERVER, payload);
    }

    @Override
    public void sendPrivateChat(ClientHandler clientHandler, int targetId, String data) throws Exception {
        //TODO: include targetId
        byte[] payload = data.getBytes();
        clientHandler.getChannelManager().send(Protocol.CH_SYSTEM, Protocol.PKG_SYS_CHAT_PRIVATE, Protocol.ID_SERVER, payload);
    }

    @Override
    public void broadcastChat(int senderId, String data) throws Exception {
        for(ClientHandler client : serverContext.getClients().values()) {
            client.getChannelManager().send(Protocol.CH_CHAT, Protocol.PKG_SYS_CHAT, senderId, data.getBytes());
        }
    }

    @Override
    public void broadcastMovement(int senderId, Object payload) throws Exception {
        for(ClientHandler client : serverContext.getClients().values()) {
            if(client.getId() == senderId) continue;
            //TODO: Convert game object to byte[]
            client.getChannelManager().send(CH_MOVEMENT, PKG_MOVE_UPDATE, senderId, new byte[1024]);
        }
    }

    public boolean claimUsername(String username) {
        return serverContext.getUsernameSet().add(username);
    }

    public void releaseUsername(String username) {
        serverContext.getUsernameSet().remove(username);
    }
}
