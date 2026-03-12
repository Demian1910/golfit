package org.golfit.client.pingService;

import org.golfit.client.ClientApi;
import org.golfit.shared.messageParser.MessageParser;
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
    private volatile boolean reconnecting = false;
    private String knownSessionId = null;

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


    public void onPong(MessageParser.Message message) {
        String sessionId = message.payload;

        if(knownSessionId == null) {
            knownSessionId = sessionId;
        } else if(!knownSessionId.equals(sessionId)) {
            knownSessionId = sessionId;
            reconnectLogger.info("Server session changed, reconnecting...");
            try {
                reconnecting = true;
                pingSentAt = 0;
                clientApi.resetChannels();
                clientApi.connect(clientApi.getUsername());
            } catch (Exception e) {
                reconnectLogger.error("Reconnect on session change failed", e);
            }
            return;
        }

        if(pingSentAt != 0) {
            logger.info("Latency: {}ms", System.currentTimeMillis() - pingSentAt);
            pingSentAt = 0;
        }
        reconnecting = false;
    }

    public void checkTimeout() {
        try {
            if(!reconnecting && pingSentAt != 0 && System.currentTimeMillis() - pingSentAt > 10000) {
                reconnecting = true;
                pingSentAt = 0;
                reconnectLogger.info("Server not responding");
                clientApi.resetChannels();
                clientApi.connect(clientApi.getUsername());
                reconnectLogger.info("Reconnecting...");
            }
        } catch (Exception e) {
            reconnectLogger.error("checkTimeout failed", e);
        }
    }

    public void stop() {
        if(scheduler != null) {
            scheduler.shutdown();
        }
    }
}
