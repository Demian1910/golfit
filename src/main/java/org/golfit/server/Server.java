package org.golfit.server;

import org.golfit.server.handlers.MovementHandler;
import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.ServerInit.ServerInit;
import org.golfit.server.handlers.ChatHandler;
import org.golfit.server.handlers.SystemHandler;
import org.golfit.server.serverApi.ServerApi;
import org.golfit.shared.constants.Constants;
import org.golfit.shared.lobby.Lobby;
import org.golfit.shared.packetParser.MessageParser;
import org.golfit.shared.protocol.Protocol;
import org.slf4j.Logger;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class Server implements ServerContext {
    private final ServerApi serverApi = new ServerApi(this);

    private final SystemHandler systemHandler = new SystemHandler(serverApi);
    private final ChatHandler chatHandler = new ChatHandler(serverApi);

    private final org.golfit.server.game.ServerGameSimulation gameSimulation = new org.golfit.server.game.ServerGameSimulation(
            serverApi);
    private final MovementHandler movementHandler = new MovementHandler(serverApi, gameSimulation);

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
            gameSimulation.start();
            byte[] buffer = new byte[Constants.MAX_PACKET_SIZE];
            ScheduledExecutorService cleanUp = Executors.newSingleThreadScheduledExecutor();
            cleanUp.scheduleAtFixedRate(() -> {
                clients.entrySet().removeIf(entry -> {
                    if (entry.getValue().isTimedOut()) {
                        System.out.println("Client timed out: " + entry.getKey());
                        gameSimulation.removePlayer(entry.getValue().getId());
                        entry.getValue().shutdown();
                        return true;
                    }
                    return false;
                });
            }, 0, 10, TimeUnit.SECONDS);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                byte[] raw = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, raw, 0, packet.getLength());

                String clientKey = packet.getAddress().getHostAddress() + ":" + packet.getPort();

                ClientHandler clientHandler = clients.computeIfAbsent(clientKey, key -> {
                    ClientHandler handler = new ClientHandler(
                            key, socket, packet.getAddress(), packet.getPort(),
                            generateUniqueId(), (pkt) -> this.dispatch(key, pkt));
                    threadPool.submit(handler);
                    return handler;
                });
                clientHandler.queueMessage(new String(raw, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    public static int generateUniqueId() {
        long time = System.nanoTime();
        long unix = System.currentTimeMillis();
        long heap = Runtime.getRuntime().totalMemory();
        long rnd = ThreadLocalRandom.current().nextLong();

        int hash = (int) (time ^ unix ^ heap ^ rnd);
        hash &= 0x7FFFFFFF;
        if (hash == 0 || hash == 1)
            hash = 2;
        return hash;
    }

    private void dispatch(String clientKey, MessageParser.Message message) throws Exception {
        ClientHandler clientHandler = clients.get(clientKey);
        switch (message.type) {
            case Protocol.SYS_CONNECT -> {
                systemHandler.onConnect(clientHandler, message);
                // Assign a temporary spawn position when connected
                gameSimulation.addPlayer(clientHandler.getId(), 400.0, 300.0);
            }
            case Protocol.SYS_PING -> systemHandler.onPing(clientHandler, message);
            case Protocol.MOVE_INPUT -> movementHandler.onMoveInput(clientHandler, message);
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