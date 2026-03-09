package org.golfit.client.pingService;

import org.golfit.client.ClientApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public class PingService {
    private final ClientApi clientApi;
    private long pingSentAt = 0;
    private ScheduledExecutorService scheduler;
    private final Logger logger = LoggerFactory.getLogger("ping");
    private final Logger reconnectLogger = LoggerFactory.getLogger(PingService.class);

    public PingService(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

    public void start() {
        scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(this::sendPing, 0, 5000, java.util.concurrent.TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::checkTimeout, 0, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    public void sendPing() {
        try {
            if (pingSentAt == 0) {
                pingSentAt = System.currentTimeMillis();
            }
            clientApi.ping();
        } catch (Exception e) {
            reconnectLogger.error("Ping failed: " + e.getMessage());
        }
    }

    public void onPong() {
        if(pingSentAt != 0) {
            logger.info("Latency: {}ms", System.currentTimeMillis() - pingSentAt);
            pingSentAt  = 0;
        };
    }

    public void checkTimeout() {
            try{
                if(pingSentAt != 0 && System.currentTimeMillis() - pingSentAt > 10000) {
                    pingSentAt = 0;
                    reconnectLogger.error("Server not responding");
                    clientApi.resetChannels();
                    clientApi.connect(clientApi.getUsername());
                    reconnectLogger.info("Reconnection...");
            }} catch (Exception e) {
                reconnectLogger.error("Reconnection failed: "+e.getMessage());
            }
    }
    public void stop() {
        if(scheduler != null) {
            scheduler.shutdown();
        }
    }
}
