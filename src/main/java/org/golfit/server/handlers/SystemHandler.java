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
        if(packet.payload != null) {
            String username = new String(packet.payload);
            clientHandler.setUsername(username);
            System.out.println("Client " + clientHandler.getId() + " connected with username " + username);
        }
        serverApi.sendConnected(clientHandler, clientHandler.getId());
    }
}
