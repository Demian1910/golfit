package org.golfit.client.handlers;

import org.golfit.client.ClientApi;
import org.golfit.shared.packetParser.PacketParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemHandler {
    private final ClientApi clientApi;
    private final Logger logger = LoggerFactory.getLogger(SystemHandler.class);

    public SystemHandler(ClientApi clientApi) {
        this.clientApi = clientApi;
    }
    public void onConnected(PacketParser.Packet packet) {
        logger.info("Yaaaaay, you count as connected user");
    }

    public void onPong(){

    }
}
