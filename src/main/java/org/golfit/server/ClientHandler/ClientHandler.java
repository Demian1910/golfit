package org.golfit.server.ClientHandler;

import org.golfit.shared.constants.Constants;
import org.golfit.shared.messageParser.MessageParser;
import org.golfit.shared.protocol.Protocol;
import org.golfit.shared.protocol.channelManager.ChannelManager;
import org.slf4j.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.*;

public class ClientHandler implements Runnable {
    private final int Id;
    private final String key;
    private String username;

    private Protocol.PacketDispatcher dispatcher;
    private final DatagramSocket socket;
    private final InetAddress address;
    private final int port;
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    private ChannelManager channelManager;
    private long lastPing = System.currentTimeMillis();

    private Thread thread;

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ClientHandler.class);

    public ClientHandler(String key, DatagramSocket socket, InetAddress address, int port, int Id, Protocol.PacketDispatcher dispatcher) {
        this.key = key;
        this.socket = socket;
        this.address = address;
        this.port = port;
        this.Id = Id;
        this.dispatcher = dispatcher;
        this.channelManager = new ChannelManager((data) -> {
            byte[] byteData = data.getBytes(StandardCharsets.UTF_8);
            DatagramPacket pkg = new DatagramPacket(byteData, byteData.length, address, port);
            socket.send(pkg);
        }, this::dispatch);
    }

    public void queueMessage(String raw) {
        messageQueue.offer(raw);
    }

    @Override
    public void run() {
        thread = Thread.currentThread();
        ScheduledExecutorService ticker = Executors.newSingleThreadScheduledExecutor();
        ticker.scheduleAtFixedRate(()->{
            try {
                channelManager.tick();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        },0, 100, TimeUnit.MILLISECONDS);

        try {
            while (!thread.isInterrupted()) {
             String raw = messageQueue.take();
             channelManager.onPacketReceived(raw);
            }
        } catch (InterruptedException e){
            logger.info("ClientHandler thread interrupted");
        }
        catch (Exception e) {
            logger.error("Error: " + e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
        } finally {
            ticker.shutdown();
        }
    }

    private void dispatch(MessageParser.Message message) throws Exception {
        dispatcher.dispatch(message);
    }
    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public int getId() {
        return Id;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void updateLastPing() {
        lastPing = System.currentTimeMillis();
    }
    public boolean isTimedOut() {
        return System.currentTimeMillis() - lastPing > Constants.TIMEOUT_PING_MS;
    }
    public String getUsername() {
        return username;
    }
    public void shutdown() {
        thread.interrupt();
    }
    public String getClientKey(){
        return key;
    }

}
