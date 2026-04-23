package org.golfit.server;

import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.ServerInit.ServerInit;
import org.golfit.server.game.ServerGameSimulation;
import org.golfit.server.handlers.MovementHandler;
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

/**
 * The GolfIt game server. Listens for UDP packets on a given port,
 * creates a {@link ClientHandler} per unique client address, and dispatches
 * incoming messages to the appropriate handlers (system, movement, chat).
 */
public class Server implements ServerContext{
    private final int sessionId = generateUniqueId();
    /**
     * server context
     */
    private final ServerApi serverApi = new ServerApi(this);

    /**
     * server handlers
     */
    private final SystemHandler systemHandler = new SystemHandler(serverApi, this);
    private final ServerGameSimulation gameSimulation = new ServerGameSimulation(serverApi);
    private final MovementHandler movementHandler = new MovementHandler(serverApi, gameSimulation);

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

        // Wire simulation → handler callbacks
        systemHandler.setGameSimulation(gameSimulation);

        // On every stroke: broadcast updated stroke counts to the player's lobby
        gameSimulation.setStrokeListener(playerId -> {
            ClientHandler ch = clients.values().stream()
                    .filter(c -> c.getId() == playerId)
                    .findFirst().orElse(null);
            if (ch == null) return;
            Lobby lobby = systemHandler.getLobbyForPlayer(ch);
            if (lobby == null) return;
            String payload = systemHandler.buildStrokePayload(lobby);
            try {
                serverApi.sendStrokeUpdate(lobby, payload);
            } catch (Exception e) {
                System.err.println("Error sending stroke update: " + e.getMessage());
            }
        });

        // On game end: delegate to handleGameEnd
        gameSimulation.setGameEndListener(systemHandler::handleGameEnd);
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
            System.out.println("Server listening on port " + port);
            gameSimulation.start();
            byte[] buffer = new byte[Constants.MAX_PACKET_SIZE];

            ScheduledExecutorService cleanUp = Executors.newSingleThreadScheduledExecutor();
            cleanUp.scheduleAtFixedRate(() -> {
                clients.entrySet().removeIf(entry -> {
                    if(entry.getValue().isTimedOut()) {
                        System.out.println("Client timed out: " + entry.getKey());
                        String timedOutUsername = entry.getValue().getUsername();
                        if (timedOutUsername != null) {
                            serverApi.releaseUsername(timedOutUsername);
                        }
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

    /**
     * Generates a unique integer client ID based on timing, memory, and random data.
     *
     * <p>The result is always positive and never {@code 0} ({@code ID_INVALID})
     * or {@code 1} ({@code ID_SERVER}).
     *
     * @return a unique positive integer client ID
     */
    @Override
    public int generateUniqueId() {
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
        switch (message.type) {
            case Protocol.SYS_CONNECT -> {
                systemHandler.onConnect(clientHandler, message);
                // Assign a temporary spawn position when connected
                gameSimulation.addPlayer(clientHandler.getId(), 2000,2000);
            }
            case Protocol.SYS_PING -> systemHandler.onPing(clientHandler, message);
            case Protocol.SYS_DISCONNECT -> systemHandler.onDisconnect(clientHandler);
            case Protocol.SYS_CHAT_BROADCAST -> systemHandler.onChatBroadcast(clientHandler, message);
            case Protocol.SYS_CHAT_PRIVATE -> systemHandler.onChatPrivate(clientHandler, message);
            case Protocol.SYS_CHAT_LOBBY -> systemHandler.onChatLobby(clientHandler, message);
            case Protocol.SYS_LOBBY_CREATE -> systemHandler.onCreateLobby(clientHandler, message);
            case Protocol.SYS_LOBBY_LIST -> systemHandler.onLobbyList(clientHandler);
            case Protocol.SYS_LOBBY_JOIN -> systemHandler.onLobbyJoin(clientHandler, message);
            case Protocol.SYS_LOBBY_LEAVE -> systemHandler.onLobbyLeave(clientHandler, message);
            case Protocol.MOVE_INPUT -> movementHandler.onMoveInput(clientHandler, message);
            case Protocol.SYS_PLAYERS_LIST -> systemHandler.onPlayerList(clientHandler, message);
            case Protocol.SYS_LOBBY_START  -> systemHandler.onLobbyStart(clientHandler, message);
            case Protocol.SYS_HESOYAM -> systemHandler.onHesoyam(clientHandler, message);
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
     * Returns the map of active lobbies.
     *
     * @return map of {@link Lobby} instances
     */
    @Override
    public Map<Integer, Lobby> getLobbies() {
        return lobbies;
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

    /**
     * Returns the unique session ID for this server instance.
     * Used to identify the server in client connections.
     *
     * @return the session ID
     */
    @Override
    public int getSessionId() {
        return sessionId;
    }
}