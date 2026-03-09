package org.golfit.client.pingService;

import org.golfit.client.ClientApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingService {
    private final ClientApi clientApi;
    private long pingSentAt = 0;
    private final Logger logger = LoggerFactory.getLogger("ping");

    public PingService(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

    public void sendPing() {
        try{
            pingSentAt = System.currentTimeMillis();
            clientApi.ping();

        } catch (Exception e) {
            logger.error("Ping failed: "+e.getMessage());
        }
    }
}
