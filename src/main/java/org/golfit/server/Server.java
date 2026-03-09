package org.golfit.server;

import org.golfit.server.handlers.MovementHandler;
import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.ServerInit.ServerInit;
import org.golfit.server.handlers.ChatHandler;
import org.golfit.server.handlers.SystemHandler;
import org.golfit.server.serverApi.ServerApi;
import org.golfit.shared.constants.Constants;
import org.golfit.shared.lobby.Lobby;
import org.golfit.shared.packetParser.PacketParser;
import org.golfit.shared.protocol.Protocol;
import org.slf4j.Logger;


import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class Server implements ServerContext{
    private final ServerApi serverApi = new ServerApi(this);

    private final SystemHandler systemHandler = new SystemHandler(serverApi);
    private final ChatHandler chatHandler = new ChatHandler(serverApi);
    private final MovementHandler movementHandler = new MovementHandler(serverApi);

    private final Set<String> usernames = ConcurrentHashMap.newKeySet();

    private final int port;
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Server.class);
    private final ExecutorService threadPool = Executors.newCachedThreadPool();


    public Server(int port) {
        this.port = port;
    }

    public void start() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            ServerInit.serverInit(port);
            byte[] buffer = new byte[Constants.MAX_PACKET_SIZE];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                byte[] raw = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, raw, 0, packet.getLength());

                String clientKey = packet.getAddress().getHostAddress() + ":" + packet.getPort();

                ClientHandler clientHandler = clients.computeIfAbsent(clientKey, key -> {
                    ClientHandler handler = new ClientHandler(
                            key, socket, packet.getAddress(), packet.getPort(),
                            generateUniqueId(), (pkt)-> this.dispatch(key, pkt));
                    threadPool.submit(handler);
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

    private void dispatch(String clientKey, PacketParser.Packet packet) throws Exception {
        ClientHandler clientHandler = clients.get(clientKey);
        switch(packet.type) {
            case Protocol.PKG_SYS_CONNECT -> systemHandler.onConnect(clientHandler, packet);
        }
    }

    @Override
    public Map<String, ClientHandler> getClients() {
        return clients;
    }

    @Override
    public Collection<Lobby> getLobbies() {
        return List.of();
    }

    @Override
    public Set<String> getUsernameSet() {
        return usernames;
    }
}