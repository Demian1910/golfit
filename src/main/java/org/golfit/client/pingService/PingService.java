package org.golfit.client.pingService;

import org.golfit.client.ClientApi;
import org.golfit.shared.messageParser.MessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/** Periodically pings the server to measure latency and detect disconnections or server restarts. Automatically reconnects when needed. */
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

    /** Starts the ping scheduler: sends pings every 5 seconds and checks for timeouts every second. */
    public void start() {
        scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(this::sendPing, 0, 5000, java.util.concurrent.TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::checkTimeout, 0, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /** Sends a single ping to the server and records the send timestamp. */
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


    /** Handles a pong response. Detects server session changes and triggers reconnection if needed. */
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

    /** Checks if the server has stopped responding and initiates a reconnection after 10 seconds of silence. */
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

    /** Shuts down the ping scheduler. */
    public void stop() {
        if(scheduler != null) {
            scheduler.shutdown();
        }
    }
}
