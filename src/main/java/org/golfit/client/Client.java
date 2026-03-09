package org.golfit.client;

import org.golfit.client.handlers.ChatHandler;
import org.golfit.client.handlers.MovementHandler;
import org.golfit.client.handlers.SystemHandler;
import org.golfit.client.pingService.PingService;
import org.golfit.shared.constants.Constants;
import org.golfit.shared.packetBuilder.PacketBuilder;
import org.golfit.shared.packetParser.PacketParser;
import org.golfit.shared.protocol.Protocol;
import org.golfit.shared.protocol.channelManager.ChannelManager;
import org.slf4j.Logger;

import java.net.*;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Client {
    public ClientApi clientApi;
    private PingService pingService;

    private SystemHandler systemHandler;
    private MovementHandler movementHandler;
    private ChatHandler chatHandler;

    final private String host;
    int id = 0;
    final private int port;
    DatagramSocket socket;


    private String username;

    private ChannelManager channelManager;

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Client.class);


    public Client(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    public void start() throws Exception {
        socket = new DatagramSocket();

        channelManager = new ChannelManager((data) -> {
            DatagramPacket pkg = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
            socket.send(pkg);
        }, this::dispatch);

        clientApi = new ClientApi(channelManager, this);
        pingService = new PingService(clientApi);


        systemHandler = new SystemHandler(clientApi);
        movementHandler = new MovementHandler(clientApi);
        chatHandler = new ChatHandler(clientApi);


        ScheduledExecutorService ticker = Executors.newSingleThreadScheduledExecutor();
        ticker.scheduleAtFixedRate(() -> {
            try {
                channelManager.tick();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        byte[] buffer = new byte[Constants.MAX_PACKET_SIZE];
        DatagramPacket packet;
        Scanner input = new Scanner(System.in);

        ScheduledExecutorService pingThread = Executors.newSingleThreadScheduledExecutor();
//        pingThread.scheduleAtFixedRate(pingService::sendPing(), 0, 10000, TimeUnit.MILLISECONDS);

        Thread receiveThread = new Thread(()-> {
            while (true) {
                try {
                    byte[] raw = new byte[Constants.MAX_PACKET_SIZE];
                    DatagramPacket datagramPacket = new DatagramPacket(raw, raw.length);
                    socket.receive(datagramPacket);
                    System.arraycopy(datagramPacket.getData(), 0, raw, 0, datagramPacket.getLength());
                    channelManager.onPacketReceived(raw);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();

        clientApi.connect(username);

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

    private void dispatch(PacketParser.Packet packet) {
        switch(packet.type) {
            case Protocol.PKG_SYS_CONNECTED -> systemHandler.onConnected(packet);
        }
    }
}