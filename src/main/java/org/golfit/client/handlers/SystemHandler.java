package org.golfit.client.handlers;

import org.golfit.client.ClientApi;
import org.golfit.shared.messageParser.MessageParser;
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
            String[] parts = message.payload.split(":");
            if (parts.length >= 1) {
                int assignedId = Integer.parseInt(parts[0]);
                clientApi.setId(assignedId);
            }
            if (parts.length >= 2) {
                String assignedUsername = parts[1];
                clientApi.setUsername(assignedUsername);
                logger.info("Connected as: " + assignedUsername);
            }
        }
        logger.info("yAaaAY! Successfully connected to server");
    }
}
