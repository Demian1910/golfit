package org.golfit.shared.lobby;

import org.golfit.server.ClientHandler.ClientHandler;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Lobby {
   public enum LobbyStatus {
        open,
        ongoing,
        finished
    }

    public int id;
    private LobbyStatus status;
    private ClientHandler lobbyHost;
    private final LinkedHashSet<ClientHandler> clients = new LinkedHashSet<>();
    private int playerCount;


    public Lobby(int id, ClientHandler lobbyHost) {
        this.id = id;
        this.lobbyHost = lobbyHost;
        this.clients.add(lobbyHost);
        setStatus(LobbyStatus.open);
    }

    public int getId() {
        return id;
    }

    public void setStatus(LobbyStatus status) {
        this.status = status;
    }

    public LobbyStatus getStatus() {
        return status;
    }

    public LinkedHashSet<ClientHandler> getClients() {
        return clients;
    }

    public boolean addClient(ClientHandler client) {
        if(clients.contains(client)) {
            System.err.println("Client already exists!");
            return false;
        }
        clients.add(client);
        return true;
    }

    public boolean removeClient(ClientHandler client) {
        if(!clients.contains(client)) {
            System.err.println("Client doesn't exists!");
        }
        clients.remove(client);
        return true;
    }

    public void setLobbyHost(ClientHandler lobbyHost) {
        if(clients.contains(lobbyHost)) {
            this.lobbyHost = lobbyHost;
        }
        else{
            System.err.println("Client doesn't exists!");
        }

    }

    public ClientHandler getLobbyHost() {
        if(clients.contains(lobbyHost)) {
            return lobbyHost;
        }

        System.err.println("Client doesn't exists!");
        return null;
    }

    public String getLobbyContext() {
        String clientString = clients.stream().map(ClientHandler::getUsername).collect(Collectors.joining(","));
        return getId() + ":" +
                clientString;

    }
    public String getLobbyListContext() {
        return getId() + ":" + getLobbyHost().getUsername() + ":" + clients.size() + ":" + getStatus();
    }
}
