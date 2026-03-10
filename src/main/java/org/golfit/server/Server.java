package org.golfit.server;

import org.golfit.server.handlers.MovementHandler;
import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.ServerInit.ServerInit;
import org.golfit.server.handlers.ChatHandler;
import org.golfit.server.handlers.SystemHandler;
import org.golfit.server.serverApi.ServerApi;
import org.golfit.shared.constants.Constants;
import org.golfit.shared.lobby.Lobby;
import org.golfit.shared.messageParser.MessageParser;
import org.golfit.shared.protocol.Protocol;
import org.slf4j.Logger;


import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class Server implements ServerContext{
    /**
     * server context
     */
    private final ServerApi serverApi = new ServerApi(this);

    /**
     * server handlers
     */
    private final SystemHandler systemHandler = new SystemHandler(serverApi);
    private final ChatHandler chatHandler = new ChatHandler(serverApi);
    private final MovementHandler movementHandler = new MovementHandler(serverApi);

    /**
     * Set of saved usernames
     */
    private final Set<String> usernames = ConcurrentHashMap.newKeySet();

    /** Server port*/
    private final int port;

    /** Map of connected clients */
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    /** Map of active lobbies */
    private final Map<Integer, Lobby> lobbies = new ConcurrentHashMap<>();

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Server.class);

    /** Thread pool for handling client requests */
    private final ExecutorService threadPool = Executors.newCachedThreadPool();



    /**
     * Constructs a Server instance with the specified port.
     * @param port The sets number to bind the server to.
     */
    public Server(int port) {
        this.port = port;
    }

    /**
     * Starts the server main loop.
     *
     * <p>This method:
     * <ol>
     *   <li>Opens a {@link DatagramSocket} on {@link #port}</li>
     *   <li>Schedules a cleanup task every 10 seconds to remove timed-out clients</li>
     *   <li>Enters an infinite receive loop — each incoming packet is routed to its
     *       {@link ClientHandler}, creating one if this is a new client address</li>
     * </ol>
     *
     * <p>A client is identified by its {@code "ip:port"} string. If a packet arrives
     * from an unknown address, a new {@link ClientHandler} is created and submitted
     * to the thread pool.
     */
    public void start() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            ServerInit.serverInit(port);
            byte[] buffer = new byte[Constants.MAX_PACKET_SIZE];

            ScheduledExecutorService cleanUp = Executors.newSingleThreadScheduledExecutor();
            cleanUp.scheduleAtFixedRate(() -> {
                clients.entrySet().removeIf(entry -> {
                    if(entry.getValue().isTimedOut()) {
                        System.out.println("Client timed out: " + entry.getKey());
                        String timedOutUsername = entry.getValue().getUsername();
                        if (timedOutUsername != null) {
                            usernames.remove(timedOutUsername);
                        }
                        entry.getValue().shutdown();
                        return true;
                    }
                    return false;
                });
            }, 0, 10000, TimeUnit.SECONDS);

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
                clientHandler.queueMessage(new String(raw, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    /**
     * Generates a unique integer client ID based on timing, memory, and random data.
     *
     * <p>The result is always positive and never {@code 0} ({@code ID_INVALID})
     * or {@code 1} ({@code ID_SERVER}).
     *
     * @return a unique positive integer client ID
     */
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

    /**
     * Dispatches a parsed message from a client to the appropriate handler.
     *
     * <p>Routing is based on message type:
     * <ul>
     *   <li>{@code SYS_CONNECT} — handled by {@link SystemHandler#onConnect}, assigns username and ID</li>
     *   <li>{@code SYS_PING} — handled by {@link SystemHandler#onPing}, updates last ping time and sends pong</li>
     * </ul>
     *
     * @param clientKey the {@code "ip:port"} key identifying the sender
     * @param message   the parsed incoming message
     * @throws Exception if a handler throws during processing
     */
    private void dispatch(String clientKey, MessageParser.Message message) throws Exception {
        ClientHandler clientHandler = clients.get(clientKey);
        switch(message.type) {
            case Protocol.SYS_CONNECT -> systemHandler.onConnect(clientHandler, message);
            case Protocol.SYS_PING -> systemHandler.onPing(clientHandler, message);
        }
    }

    /**
     * Returns the map of all currently connected clients.
     *
     * @return map of client key ({@code "ip:port"}) to {@link ClientHandler}
     */
    @Override
    public Map<String, ClientHandler> getClients() {
        return clients;
    }

    /**
     * Returns the collection of active lobbies.
     *
     * @return collection of {@link Lobby} instances
     */
    @Override
    public Collection<Lobby> getLobbies() {
        return Map.copyOf(lobbies).values();
    }

    /**
     * Returns the set of currently claimed usernames.
     * Used by {@link ServerApi#claimUsername} to check and register new usernames.
     *
     * @return the live set of active usernames
     */
    @Override
    public Set<String> getUsernameSet() {
        return usernames;
    }
}