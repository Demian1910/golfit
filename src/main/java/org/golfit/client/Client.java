package org.golfit.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    final private String host;
    final private int port;
    private String username;


    public Client(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    public void start() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String message = scanner.nextLine();
                byte[] data = message.getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
                socket.send(packet);
                System.out.println("Message sent: " + message);

                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Received: " + receivedMessage);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }


}