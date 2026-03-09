package org.golfit.server.serverApi;

import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.ServerContext;
import org.golfit.shared.lobby.Lobby;
import org.golfit.shared.protocol.Protocol;

import java.math.BigInteger;
import java.util.Collection;

import static org.golfit.shared.protocol.Protocol.CH_MOVEMENT;
import static org.golfit.shared.protocol.Protocol.MOVE_UPDATE;

public class ServerApi implements IServerApi{
    private final ServerContext serverContext;

    public ServerApi(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    @Override
    public void sendConnected(ClientHandler clientHandler, int assignedId) throws Exception {
        for(ClientHandler client : serverContext.getClients().values()) {
            System.out.println("Client Connected" + client.getUsername());
        }
        String payload =  assignedId + "";
        clientHandler.getChannelManager().send(Protocol.SYS_CONNECTED, Protocol.CH_SYSTEM, Protocol.ID_SERVER, payload);
    }

    @Override
    public void sendPong(ClientHandler clientHandler) throws Exception {
        clientHandler.getChannelManager().send( Protocol.SYS_PONG, Protocol.CH_PINGS, Protocol.ID_SERVER, "");
    }

    public void disconnectClient(ClientHandler clientHandler) {
        serverContext.getClients().remove(clientHandler.getId());
    }

    @Override
    public void sendLobbyList(ClientHandler clientHandler) throws Exception {
        //TODO: Convert lobbies to byte[]
        Collection<Lobby> lobbies = serverContext.getLobbies();
        clientHandler.getChannelManager().send( Protocol.SYS_LOBBY_LIST,Protocol.CH_SYSTEM, Protocol.ID_SERVER, "");
    }

    @Override
    public void sendPrivateChat(ClientHandler clientHandler, int targetId, String data) throws Exception {
        //TODO: include targetId
        byte[] payload = data.getBytes();
        clientHandler.getChannelManager().send( Protocol .SYS_CHAT_PRIVATE,Protocol.CH_SYSTEM, Protocol.ID_SERVER, "");
    }

    @Override
    public void broadcastChat(int senderId, String data) throws Exception {
        for(ClientHandler client : serverContext.getClients().values()) {
            client.getChannelManager().send( Protocol.SYS_CHAT,Protocol.CH_CHAT, senderId, data);
        }
    }

    @Override
    public void broadcastMovement(int senderId, Object payload) throws Exception {
        for(ClientHandler client : serverContext.getClients().values()) {
            if(client.getId() == senderId) continue;
            //TODO: Convert game object to byte[]
            client.getChannelManager().send(Protocol.MOVE_UPDATE,Protocol.CH_MOVEMENT, senderId, "");
        }
    }

    public boolean claimUsername(String username) {
        return serverContext.getUsernameSet().add(username);
    }

    public void releaseUsername(String username) {
        serverContext.getUsernameSet().remove(username);
    }
}
