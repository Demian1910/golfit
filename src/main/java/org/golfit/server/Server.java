package org.golfit.server;

import org.golfit.server.ClientHandler.ClientHandler;

import java.io.*;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final int port;
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();


    public Server(int port) {
        this.port = port;
    }

    public void start() {
        // create datagram socket

        try(DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("Server running on port: " + port);
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String clientKey = packet.getAddress().getHostAddress() + ":" + packet.getPort();
                String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);

                ClientHandler clientHandler = clients.computeIfAbsent(clientKey, key -> {
                    ClientHandler handler = new ClientHandler(key, socket, packet.getAddress(), packet.getPort());
                    new Thread(handler).start();
                    return handler;
                });

                clientHandler.queueMessage(message);

            }
        } catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}