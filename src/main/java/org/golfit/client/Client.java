package org.golfit.client;

import org.golfit.shared.packetBuilder.PacketBuilder;
import org.golfit.shared.protocol.Protocol;
import org.slf4j.Logger;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    final private String host;
    final private int id = 0;
    final private int port;
    private String username;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Client.class);
    private static final Logger pingLogger = org.slf4j.LoggerFactory.getLogger("ping");


    public Client(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    public void start() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();

            DatagramSocket pingSocket = socket;
            Thread pingThread = new Thread(() -> pingLoop(pingSocket));
            pingThread.setDaemon(true);
            pingThread.start();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                String message = scanner.nextLine();
                byte[] data = message.getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
                socket.send(packet);
                logger.info("Sent: " + message);

                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                logger.info("Received: " + receivedMessage);
            }
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
    private void pingLoop(DatagramSocket socket) {
        try{
            while(!socket.isClosed()) {
                long start = System.currentTimeMillis();

                byte[] packet = new PacketBuilder().buildPacket(Protocol.PKG_SYS_PING, Protocol.CH_SYSTEM, id, 0);

//                byte[] data = "ping".getBytes();
//                DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
//                socket.send(packet);
//
//                byte[] receiveData = new byte[1024];
//                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//                socket.receive(receivePacket);
//                String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
//                if(response.equals("pong")) {
//                    long ping = System.currentTimeMillis() - start;
//                    pingLogger.debug("Ping: " + ping + "ms");
//                }
//                Thread.sleep(5000);
            }
        }
        catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }

    }

}