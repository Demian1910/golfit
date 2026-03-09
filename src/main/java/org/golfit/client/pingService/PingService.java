package org.golfit.client.pingService;

import org.golfit.client.ClientApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.concurrent.ScheduledExecutorService;

public class PingService {
    private final ClientApi clientApi;
    private long pingSentAt = 0;
    private final Logger logger = LoggerFactory.getLogger("ping");

    public PingService(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

    public void start(ScheduledExecutorService scheduler) {
        scheduler.scheduleAtFixedRate(this::sendPing, 0, 5000, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    public void sendPing() {
        try{
            pingSentAt = System.currentTimeMillis();
            clientApi.ping();

        } catch (Exception e) {
            logger.error("Ping failed: "+e.getMessage());
        }
    }

    public void onPong() {
        if(pingSentAt != 0) {
            logger.info("Latency: {}ms", System.currentTimeMillis() - pingSentAt);
            pingSentAt = 0;
        };
    }
}
