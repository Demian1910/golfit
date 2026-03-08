package org.golfit.server.handlers;

import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.ServerContext;
import org.golfit.server.serverApi.ServerApi;
import org.golfit.shared.packetParser.PacketParser;

public class SystemHandler {

    private final ServerApi serverApi;

    public SystemHandler(ServerApi serverApi) {
        this.serverApi = serverApi;
    }

    public void onConnect(ClientHandler clientHandler, PacketParser.Packet packet) throws Exception {
        String username = new String(packet.payload);
        clientHandler.setUsername(username);
        serverApi.sendConnected(clientHandler, clientHandler.getId());
    }
}
