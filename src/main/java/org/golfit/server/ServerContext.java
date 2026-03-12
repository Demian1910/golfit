package org.golfit.server;

import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.shared.lobby.Lobby;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ServerContext {
    Map<String, ClientHandler> getClients();
    Collection<Lobby> getLobbies();
    Set<String> getUsernameSet();
    int getSessionId();
}