package org.golfit.shared.lobby;

import org.golfit.server.ClientHandler.ClientHandler;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a game lobby that groups players together before and during a match.
 * Each lobby has a host, a set of clients, and a status indicating its current phase.
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
    private int playerCount;


    /**
     * Creates a new lobby with the given ID and host.
     *
     * @param id        the unique lobby identifier
     * @param lobbyHost the client who created and initially hosts the lobby
     */
    public Lobby(int id, ClientHandler lobbyHost) {
        this.id = id;
        this.lobbyHost = lobbyHost;
        clients.add(lobbyHost);
        setStatus(LobbyStatus.open);
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

    /** Returns the set of clients currently in this lobby, in join order. */
    public LinkedHashSet<ClientHandler> getClients() {
        return clients;
    }

    /**
     * Adds a client to this lobby.
     *
     * @param client the client to add
     * @return true if the client was added, false if they were already present
     */
    public boolean addClient(ClientHandler client) {
        if(clients.contains(client)) {
            System.err.println("Client already exists!");
            return false;
        }
        clients.add(client);
        return true;
    }

    /**
     * Removes a client from this lobby. If the removed client was the host,
     * the next client in join order becomes the new host.
     *
     * @param client the client to remove
     * @return true after removal
     */
    public boolean removeClient(ClientHandler client) {
        if(!clients.contains(client)) {
            System.err.println("Client doesn't exists!");
        }
        clients.remove(client);
        // If the host left, reassign to next client (or null if empty)
        if (client.equals(lobbyHost) && !clients.isEmpty()) {
            lobbyHost = clients.iterator().next();
        }
        return true;
    }

    /**
     * Sets the lobby host, provided the client is already in the lobby.
     *
     * @param lobbyHost the client to promote to host
     */
    public void setLobbyHost(ClientHandler lobbyHost) {
        if(clients.contains(lobbyHost)) {
            this.lobbyHost = lobbyHost;
        }
        else{
            System.err.println("Client doesn't exists!");
        }

    }

    /** Returns the current lobby host, or null if the host is no longer in the lobby. */
    public ClientHandler getLobbyHost() {
        if(clients.contains(lobbyHost)) {
            return lobbyHost;
        }

        System.err.println("Client doesn't exists!");
        return null;
    }

    /** Returns a string representation of the lobby for info updates: {@code "lobbyId:player1,player2,..."}. */
    public String getLobbyContext() {
        String clientString = clients.stream().map(ClientHandler::getUsername).collect(Collectors.joining(","));
        return getId() + ":" +
                clientString;

    }
    /** Returns a summary string for lobby list display: {@code "lobbyId:hostName:playerCount:status"}. */
    public String getLobbyListContext() {
        return getId() + ":" + getLobbyHost().getUsername() + ":" + clients.size() + ":" + getStatus();
    }
}
