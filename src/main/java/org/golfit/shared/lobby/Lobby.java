package org.golfit.shared.lobby;

import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.shared.constants.MapData;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * Represents a game lobby. Each lobby maintains its own unique instance
 * of MapData to ensure physics and breakable objects stay isolated.
 */
public class Lobby {
    /** Lifecycle states of a lobby. */
    public enum LobbyStatus {
        open,
        ongoing,
        finished
    }

    public int id;
    private LobbyStatus status;
    private ClientHandler lobbyHost;
    private final LinkedHashSet<ClientHandler> clients = new LinkedHashSet<>();

    // Each lobby gets its own physics map instance
    private final MapData physicsMap;

    /**
     * Creates a new lobby and initializes a fresh map instance.
     */
    public Lobby(int id, ClientHandler lobbyHost) {
        this.id = id;
        this.lobbyHost = lobbyHost;
        this.clients.add(lobbyHost);
        this.status = LobbyStatus.open;

        // Even if there is only 1 map design, we create a 'new' instance
        // so that broken objects/state are unique to THIS lobby.
        this.physicsMap = new MapData();
    }

    /** Returns the unique lobby ID. */
    public int getId() {
        return id;
    }

    /** Sets the lobby's lifecycle status. */
    public void setStatus(LobbyStatus status) {
        this.status = status;
    }

    /** Returns the current lobby status. */
    public LobbyStatus getStatus() {
        return status;
    }

    /** Returns the physics map instance specifically for this lobby. */
    public MapData getPhysicsMap() {
        return physicsMap;
    }

    /** Returns the set of clients currently in this lobby. */
    public LinkedHashSet<ClientHandler> getClients() {
        return clients;
    }

    /** Adds a client to this lobby. */
    public boolean addClient(ClientHandler client) {
        if (clients.contains(client)) {
            System.err.println("Client already exists in lobby!");
            return false;
        }
        clients.add(client);
        return true;
    }

    /** Removes a client and reassigns host if necessary. */
    public boolean removeClient(ClientHandler client) {
        if (!clients.contains(client)) {
            System.err.println("Client doesn't exist in lobby!");
            return false;
        }
        clients.remove(client);
        if (client.equals(lobbyHost) && !clients.isEmpty()) {
            lobbyHost = clients.iterator().next();
        }
        return true;
    }

    /** Promotes a client to host. */
    public void setLobbyHost(ClientHandler lobbyHost) {
        if (clients.contains(lobbyHost)) {
            this.lobbyHost = lobbyHost;
        }
    }

    /** Returns the current lobby host. */
    public ClientHandler getLobbyHost() {
        return clients.contains(lobbyHost) ? lobbyHost : null;
    }

    /** Returns string: "lobbyId:player1,player2,..." */
    public String getLobbyContext() {
        String clientString = clients.stream()
                .map(ClientHandler::getUsername)
                .collect(Collectors.joining(","));
        return id + ":" + clientString;
    }

    /** Returns string: "lobbyId:hostName:playerCount:status" */
    public String getLobbyListContext() {
        String hostName = (getLobbyHost() != null) ? getLobbyHost().getUsername() : "None";
        return id + ":" + hostName + ":" + clients.size() + ":" + status;
    }
}