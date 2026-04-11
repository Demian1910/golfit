package org.golfit.server;

import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.shared.lobby.Lobby;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/** Provides access to shared server state: connected clients, lobbies, usernames, and session ID. */
public interface ServerContext {
    /** Generates a unique positive integer ID for a new client. */
    int generateUniqueId();

    /** Returns the map of all connected clients, keyed by {@code "ip:port"}. */
    Map<String, ClientHandler> getClients();

    /** Returns the map of active lobbies, keyed by lobby ID. */
    Map<Integer, Lobby> getLobbies();

    /** Returns the set of currently claimed usernames. */
    Set<String> getUsernameSet();

    /** Returns the unique session ID for this server instance. */
    int getSessionId();
}