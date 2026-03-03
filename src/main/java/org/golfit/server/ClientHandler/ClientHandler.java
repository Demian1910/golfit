package org.golfit.server.ClientHandler;

import org.golfit.shared.protocol.channelManager.ChannelManager;
import org.slf4j.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandler implements Runnable {
    private final String clientKey;
//    private final String username;
//    private final int lobbyId;
    private final int Id;
    private final ChannelManager channelManager;
    private final DatagramSocket socket;
    private final InetAddress address;
    private final int port;
    private final BlockingQueue<byte[]> messageQueue = new LinkedBlockingQueue<>();
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ClientHandler.class);

    public ClientHandler(String clientKey, DatagramSocket socket, InetAddress address, int port, int Id) {
        this.clientKey = clientKey;
        this.socket = socket;
        this.address = address;
        this.port = port;
        this.Id = Id;
        this.channelManager = new ChannelManager((data)-> {
            DatagramPacket pkg = new DatagramPacket(data, data.length, address, port);
            socket.send(pkg);
        });
    }

    public void queueMessage(byte [] raw) {
        messageQueue.offer(raw);
    }
    @Override
    public void run() {
        logger.info("client: " + clientKey + " connected");
        try {
            while (true) {
             byte[] raw = messageQueue.take();
             channelManager.onPackageReceived(raw);
            }
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }
}
