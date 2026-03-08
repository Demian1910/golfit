package org.golfit.server.handlers;

import org.golfit.server.Server;
import org.golfit.server.ServerContext;
import org.golfit.server.serverApi.ServerApi;

public class ChatHandler {
    private final ServerApi serverApi;
    public ChatHandler(ServerApi serverContext) {
        this.serverApi = serverContext;
    }
}
