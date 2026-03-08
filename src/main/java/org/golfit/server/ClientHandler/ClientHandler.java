package org.golfit.server.ClientHandler;

import org.golfit.shared.protocol.Protocol;
import org.golfit.shared.protocol.channelManager.ChannelManager;
import org.slf4j.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.*;

public class ClientHandler implements Runnable {
    private final int Id;
    private final String key;
    private String username;


    private final DatagramSocket socket;
    private final InetAddress address;
    private final int port;
    private final BlockingQueue<byte[]> messageQueue = new LinkedBlockingQueue<>();

    private ChannelManager channelManager;

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ClientHandler.class);

    public ClientHandler(String key, DatagramSocket socket, InetAddress address, int port, int Id) {
        this.key = key;
        this.socket = socket;
        this.address = address;
        this.port = port;
        this.Id = Id;
    }

    public void queueMessage(byte [] raw) {
        messageQueue.offer(raw);
    }

    @Override
    public void run() {
        ScheduledExecutorService ticker = Executors.newSingleThreadScheduledExecutor();
        ticker.scheduleAtFixedRate(()->{
            try {
                channelManager.tick();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        },0, 100, TimeUnit.MILLISECONDS);


        try {
            while (true) {
             byte[] raw = messageQueue.take();
             channelManager.onPacketReceived(raw);
            }
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        } finally {
            ticker.shutdown();
        }
    }

    public void setDispatcher(Protocol.PacketDispatcher dispatcher) {
        this.channelManager = new ChannelManager(
                (data) -> {
                    DatagramPacket pkg = new DatagramPacket(data, data.length, address, port);
                    socket.send(pkg);
                },
                dispatcher
        );
    }

    public ClientHandler getClientHandler() {
        return this;
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

}
