package org.golfit.server.handlers;

import org.golfit.server.serverApi.ServerApi;

public class MovementHandler {
    private final ServerApi serverApi;

    public MovementHandler(ServerApi serverContext) {
        this.serverApi = serverContext;
    }
}
