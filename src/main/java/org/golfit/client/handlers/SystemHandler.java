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
        if (message.payload != null && !message.payload.isEmpty()) {
            try {
                int id = Integer.parseInt(message.payload);
                clientApi.setId(id);
                logger.info("Yaaaaay, you count as connected user with ID: " + id);
            } catch (NumberFormatException e) {
                logger.error("Failed to parse assigned ID: " + message.payload);
            }
        }
    }
}
