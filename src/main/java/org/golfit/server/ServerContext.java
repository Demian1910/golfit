package org.golfit.server;

import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.shared.lobby.Lobby;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/** Provides access to shared server state: connected clients, lobbies, usernames, and session ID. */
public interface ServerContext {
    int generateUniqueId();
    Map<String, ClientHandler> getClients();
    Map<Integer, Lobby> getLobbies();
    Set<String> getUsernameSet();
    int getSessionId();
}