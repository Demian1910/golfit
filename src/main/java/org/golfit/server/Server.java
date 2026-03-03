package org.golfit.server;

import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.ServerInit.ServerInit;
import org.slf4j.Logger;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Server {
    private final int port;
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Server.class);


    public Server(int port) {
        this.port = port;
    }

    public void start() {

        try(DatagramSocket socket = new DatagramSocket(port)) {
            ServerInit.serverInit(port);
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                byte[] raw = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, raw, 0, packet.getLength());

                String clientKey = packet.getAddress().getHostAddress() + ":" + packet.getPort();

                String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);

                ClientHandler clientHandler = clients.computeIfAbsent(clientKey, key -> {
                    ClientHandler handler = new ClientHandler(key, socket, packet.getAddress(), packet.getPort(), generateUniqueId());
                    new Thread(handler).start();
                    return handler;
                });

                clientHandler.queueMessage(raw);
            }
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    public static int generateUniqueId() {
        long time = System.nanoTime();
        long unix = System.currentTimeMillis();
        long heap = Runtime.getRuntime().totalMemory();
        long rnd  = ThreadLocalRandom.current().nextLong();

        int hash = (int) (time ^ unix ^ heap ^ rnd);
        hash &= 0x7FFFFFFF;
        if(hash == 0 || hash == 1) hash = 2;
        return hash;
    }
}