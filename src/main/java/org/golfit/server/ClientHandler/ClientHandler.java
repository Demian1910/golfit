package org.golfit.server.ClientHandler;

import org.slf4j.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandler implements Runnable {
    private final String clientKey;
    private final DatagramSocket socket;
    private final InetAddress address;
    private final int port;
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ClientHandler.class);

    public ClientHandler(String clientKey, DatagramSocket socket, InetAddress address, int port) {
        this.clientKey = clientKey;
        this.socket = socket;
        this.address = address;
        this.port = port;
    }

    public void queueMessage(String message) {
        messageQueue.offer(message);
    }
    @Override
    public void run() {
        logger.info("client: " + clientKey + " connected");
        try {
            while (true) {
                String message = messageQueue.take();
                if(message.equals("ping")) {
                    byte[] data = "pong".getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                    socket.send(packet);
                    continue;
                }

                logger.info("[" + clientKey + "] " + message);

                String response = "Echo: " + message;
                byte[] responseBytes = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, address, port);
                socket.send(responsePacket);
            }
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }
}
