package org.golfit.client.handlers;

import org.golfit.client.ClientApi;
import org.golfit.client.pingService.PingService;
import org.golfit.shared.packetParser.MessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemHandler {
    private final ClientApi clientApi;
    private final Logger logger = LoggerFactory.getLogger(SystemHandler.class);

    public SystemHandler(ClientApi clientApi) {
        this.clientApi = clientApi;
    }
    public void onConnected(MessageParser.Message message) {
        logger.info("Yaaaaay, you count as connected user");
    }
}
