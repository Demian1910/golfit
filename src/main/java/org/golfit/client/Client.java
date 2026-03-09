package org.golfit.client;

import org.golfit.client.handlers.ChatHandler;
import org.golfit.client.handlers.MovementHandler;
import org.golfit.client.handlers.SystemHandler;
import org.golfit.client.pingService.PingService;
import org.golfit.shared.constants.Constants;
import org.golfit.shared.packetBuilder.MessageBuilder;
import org.golfit.shared.packetParser.MessageParser;
import org.golfit.shared.protocol.Protocol;
import org.golfit.shared.protocol.channelManager.ChannelManager;
import org.slf4j.Logger;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
            byte[] byteData = data.getBytes(StandardCharsets.UTF_8);
            DatagramPacket pkg = new DatagramPacket(byteData, byteData.length, InetAddress.getByName(host), port);
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


        Scanner scanner = new Scanner(System.in);
        Thread inputThread = new Thread(()-> {
            while (true) {
               String line = scanner.nextLine();
                MessageParser.Message messageParsed = MessageParser.parseMessage(line);
                assert messageParsed != null;
                try {
                    channelManager.send(messageParsed.type, messageParsed.channel, messageParsed.senderId,messageParsed.payload);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();



        Thread receiveThread = new Thread(()-> {
            while (true) {
                try {
                    byte[] raw = new byte[Constants.MAX_PACKET_SIZE];
                    DatagramPacket datagramPacket = new DatagramPacket(raw, raw.length);
                    socket.receive(datagramPacket);
                    System.arraycopy(datagramPacket.getData(), 0, raw, 0, datagramPacket.getLength());
                    channelManager.onPacketReceived(new String(raw, 0, datagramPacket.getLength(), StandardCharsets.UTF_8));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();

        clientApi.connect(username);
        pingService.start(Executors.newSingleThreadScheduledExecutor());
    }


    private void dispatch(MessageParser.Message message) {
        switch(message.type) {
            case Protocol.SYS_CONNECTED -> systemHandler.onConnected(message);
            case Protocol.SYS_PONG -> pingService.onPong();
        }
    }
}