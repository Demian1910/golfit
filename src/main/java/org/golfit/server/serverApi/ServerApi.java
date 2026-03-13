package org.golfit.server.serverApi;

import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.ServerContext;
import org.golfit.shared.lobby.Lobby;
import org.golfit.shared.protocol.Protocol;

import java.util.Collection;
import java.util.Map;


public class ServerApi implements IServerApi{
    private final ServerContext serverContext;

    public ServerApi(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    @Override
    public void sendConnected(ClientHandler clientHandler, String payload) throws Exception {
        for(ClientHandler client : serverContext.getClients().values()) {
        }
        clientHandler.getChannelManager().send(Protocol.SYS_CONNECTED, Protocol.CH_SYSTEM, Protocol.ID_SERVER, payload);
    }

    @Override
    public void sendPong(ClientHandler clientHandler) throws Exception {
        String payload = String.valueOf(serverContext.getSessionId());
        clientHandler.getChannelManager().send( Protocol.SYS_PONG, Protocol.CH_PINGS, Protocol.ID_SERVER, payload);
    }

    public void disconnectClient(ClientHandler clientHandler) {
        serverContext.getUsernameSet().remove(clientHandler.getUsername());
        clientHandler.shutdown();
        serverContext.getClients().remove(clientHandler.getClientKey());
    }

    @Override
    public void createLobby(ClientHandler clientHandler, String lobbyId) throws Exception {
        System.out.println("Sending SYS_LOBBY_CREATED to " + clientHandler.getUsername()); // add this
        clientHandler.getChannelManager().send(Protocol.SYS_LOBBY_CREATED, Protocol.CH_SYSTEM, Protocol.ID_SERVER, lobbyId);
    }

    @Override
    public void sendLobbyList(ClientHandler clientHandler) throws Exception {
        StringBuilder payload = new StringBuilder();
        Collection<Lobby> lobbies = serverContext.getLobbies();
        for (Lobby lobby : lobbies) {
            payload.append(lobby.getId()).append(":");
        }
        clientHandler.getChannelManager().send( Protocol.SYS_LOBBY_LIST,Protocol.CH_SYSTEM, Protocol.ID_SERVER, payload.toString());
    }

    public void joinLobby(ClientHandler clientHandler, String lobbyId) throws Exception {
        clientHandler.getChannelManager().send(Protocol.SYS_LOBBY_JOINED, Protocol.CH_SYSTEM, Protocol.ID_SERVER, lobbyId);
    }

    public void sendLeaveLobby(ClientHandler clientHandler, String lobbyId) throws Exception {
        clientHandler.getChannelManager().send(Protocol.SYS_LOBBY_LEFT, Protocol.CH_SYSTEM, Protocol.ID_SERVER, lobbyId);
    }

    @Override
    public void sendPrivateChat(ClientHandler clientHandler, int targetId, String data) throws Exception {
        clientHandler.getChannelManager().send( Protocol .SYS_CHAT_PRIVATE,Protocol.CH_SYSTEM, Protocol.ID_SERVER, data);
    }

    @Override
    public void broadcastChat(int senderId, String data) throws Exception {
        for(ClientHandler client : serverContext.getClients().values()) {
            client.getChannelManager().send( Protocol.SYS_CHAT,Protocol.CH_CHAT, senderId, data);
        }
    }

    @Override
    public void broadcastMovement() throws Exception {
        for(ClientHandler client : serverContext.getClients().values()) {
//            if(client.getId() == senderId) continue;
            //TODO: Convert game object to byte[]
            client.getChannelManager().send(Protocol.MOVE_UPDATE,Protocol.CH_MOVEMENT, Protocol.ID_SERVER, "");
        }
    }

    public boolean claimUsername(String username) {
        return serverContext.getUsernameSet().add(username);
    }

    public void releaseUsername(String username) {
        serverContext.getUsernameSet().remove(username);
    }

    public Map<String, ClientHandler> getClients() {
        return serverContext.getClients();
    }
}
