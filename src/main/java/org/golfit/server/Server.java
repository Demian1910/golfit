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
import org.slf4j.LoggerFactory;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * The GolfIt game server. Listens for UDP packets on a given port,
 * creates a {@link ClientHandler} per unique client address, and dispatches
 * incoming messages to the appropriate handlers.
 */
public class Server implements ServerContext {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final int sessionId = generateUniqueId();
    private final ServerApi serverApi = new ServerApi(this);

    // Handlers
    private final SystemHandler systemHandler = new SystemHandler(serverApi, this);
    private final ServerGameSimulation gameSimulation = new ServerGameSimulation(serverApi);
    private final MovementHandler movementHandler = new MovementHandler(serverApi, gameSimulation);

    // State Tracking
    private final Set<String> usernames = ConcurrentHashMap.newKeySet();
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final Map<Integer, Lobby> lobbies = new ConcurrentHashMap<>();

    private final int port;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    /**
     * Constructs a Server instance and wires simulation callbacks.
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
                logger.error("Error sending stroke update for player {}: {}", playerId, e.getMessage());
            }
        });

        // On game end: delegate results processing to SystemHandler
        gameSimulation.setGameEndListener(systemHandler::handleGameEnd);
    }

    /**
     * Starts the server main loop, ticker, and cleanup tasks.
     */
    public void start() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            ServerInit.serverInit(port);
            System.out.println("Server listening on port " + port);

            gameSimulation.start();
            byte[] buffer = new byte[Constants.MAX_PACKET_SIZE];

            // Cleanup task: remove timed-out clients every 10 seconds
            ScheduledExecutorService cleanUp = Executors.newSingleThreadScheduledExecutor();
            cleanUp.scheduleAtFixedRate(() -> {
                clients.entrySet().removeIf(entry -> {
                    ClientHandler handler = entry.getValue();
                    if (handler.isTimedOut()) {
                        System.out.println("Client timed out: " + entry.getKey());
                        String timedOutUsername = handler.getUsername();
                        if (timedOutUsername != null) {
                            serverApi.releaseUsername(timedOutUsername);
                        }
                        gameSimulation.removePlayer(handler.getId());
                        handler.shutdown();
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
            logger.error("Fatal Server Error: {}", e.getMessage());
        }
    }

    /**
     * Dispatches parsed messages to the correct logical handler.
     */
    private void dispatch(String clientKey, MessageParser.Message message) throws Exception {
        ClientHandler clientHandler = clients.get(clientKey);
        if (clientHandler == null) return;

        switch (message.type) {
            case Protocol.SYS_CONNECT -> {
                systemHandler.onConnect(clientHandler, message);
                // Initialize player in simulation at lobby 0 (Global/Idle state)
                gameSimulation.addPlayer(clientHandler.getId(), 0);
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
            case Protocol.SYS_LOBBY_START -> systemHandler.onLobbyStart(clientHandler, message);
            case Protocol.SYS_HESOYAM      -> systemHandler.onHesoyam(clientHandler, message);
            case Protocol.SYS_PLAYER_COLOR -> systemHandler.onPlayerColor(clientHandler, message);
            default -> logger.warn("Unknown protocol type: {}", message.type);
        }
    }

    @Override
    public int generateUniqueId() {
        long time = System.nanoTime();
        long unix = System.currentTimeMillis();
        long heap = Runtime.getRuntime().totalMemory();
        long rnd = ThreadLocalRandom.current().nextLong();

        int hash = (int) (time ^ unix ^ heap ^ rnd);
        hash &= 0x7FFFFFFF;
        if (hash == 0 || hash == 1) hash = 2;
        return hash;
    }

    @Override
    public Map<String, ClientHandler> getClients() { return clients; }

    @Override
    public Map<Integer, Lobby> getLobbies() { return lobbies; }

    @Override
    public Set<String> getUsernameSet() { return usernames; }

    @Override
    public int getSessionId() { return sessionId; }
}