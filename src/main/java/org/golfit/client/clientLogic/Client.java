package org.golfit.client.clientLogic;

import org.golfit.client.cli.ClientCli;
import org.golfit.client.cli.ConsoleManager;
import org.golfit.client.handlers.MovementHandler;
import org.golfit.client.handlers.SystemHandler;
import org.golfit.client.pingService.PingService;
import org.golfit.shared.constants.Constants;
import org.golfit.shared.messageParser.MessageParser;
import org.golfit.shared.protocol.Protocol;
import org.golfit.shared.protocol.channelManager.ChannelManager;
import org.slf4j.Logger;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main client class for the GolfIt network client.
 *
 * <p>Manages the lifecycle of a GolfIt client session, including:
 * <ul>
 *   <li>UDP socket communication with the server</li>
 *   <li>Reliable message delivery via {@link ChannelManager}</li>
 *   <li>CLI input processing via {@link ClientCli}</li>
 *   <li>Keepalive via {@link PingService}</li>
 *   <li>Dispatching incoming messages to the appropriate handlers</li>
 * </ul>
 *
 * <p>The client runs three concurrent components:
 * <ul>
 *   <li>A scheduled ticker that drives the channel manager's retry logic every 100ms</li>
 *   <li>An input thread that reads user commands from the console</li>
 *   <li>A receive thread that listens for incoming UDP packets</li>
 * </ul>
 */

public class Client {
    /** Public API for the client **/
    public ClientApi clientApi;
    /** Service for sending pings **/
    public PingService pingService;
    /** CLI interface for user input **/
    ClientCli clientCli;

    /** Handlers for different message types **/
    private SystemHandler systemHandler;
    private MovementHandler movementHandler;

    /** Server hostname and port  **/
    final private String host;

    /** Server assigned identifier for the client (Default 0) **/
    volatile int id = 0;

    int lobbyId = 0;
    LinkedHashSet<String> lobbyPlayers = new LinkedHashSet<>();

    final private int port;
    DatagramSocket socket;

    /** Username for the client **/
    String username;

    /** Channel manager for reliable message delivery **/
    private ChannelManager channelManager;

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Client.class);

    /**
     * Constructs a new Client.
     *
     * @param host     the server hostname or IP address
     * @param port     the server port
     * @param username the desired username; if null, defaults to the OS username
     */
    public Client(String host, int port, String username) {
        this.host = host;
        this.port = port;
    }

    /**
     * Starts the client and initiates a connection to the server.
     *
     * @throws Exception if the socket cannot be opened or the connect message fails to send
     */
    public void start() throws Exception {
        System.out.println("Type /help for a list of commands");
        socket = new DatagramSocket();
        ConsoleManager.init();

        channelManager = new ChannelManager((data) -> {
            byte[] byteData = data.getBytes(StandardCharsets.UTF_8);
            DatagramPacket pkg = new DatagramPacket(byteData, byteData.length, InetAddress.getByName(host), port);
            socket.send(pkg);
        }, this::dispatch, id);

        clientApi = new ClientApi(channelManager, this);

        pingService = new PingService(clientApi);
        clientCli = new ClientCli(clientApi);
        systemHandler = new SystemHandler(clientApi);
        movementHandler = new MovementHandler(clientApi);

        // initiates ticker for resending messages
        ScheduledExecutorService ticker = Executors.newSingleThreadScheduledExecutor();
        ticker.scheduleAtFixedRate(() -> {
            try {
                channelManager.tick();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        Thread receiveThread = new Thread(() -> {
            while (true) {
                try {
                    byte[] raw = new byte[Constants.MAX_PACKET_SIZE];
                    DatagramPacket datagramPacket = new DatagramPacket(raw, raw.length);
                    socket.receive(datagramPacket);
                    System.arraycopy(datagramPacket.getData(), 0, raw, 0, datagramPacket.getLength());
                    channelManager
                            .onPacketReceived(new String(raw, 0, datagramPacket.getLength(), StandardCharsets.UTF_8));
                } catch (Exception e) {
                    if (socket.isClosed()) break;
                    logger.error(e.getMessage());
                }
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();

        Thread inputThread = new Thread(() -> {
            while (true) {
                try {
                    String line = org.golfit.client.cli.ConsoleManager.readLine("> ");
                    if (line != null && !line.isBlank()) {
                        clientCli.processCommand(line.trim());
                    }
                } catch (org.jline.reader.UserInterruptException | org.jline.reader.EndOfFileException e) {
                    clientApi.shutdown();
                    break;
                } catch (Exception e) {
                    logger.error("Input error: " + e.getMessage());
                }
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();
    }

    /** Returns the movement handler for wiring game pane events. */
    public MovementHandler getMovementHandler() {
        return movementHandler;
    }

    /** Returns the system handler for protocol message processing. */
    public SystemHandler getSystemHandler() {
        return systemHandler;
    }

    /** Returns the public client API. */
    public ClientApi getClientApi() {
        return clientApi;
    }
    /**
     * Dispatches an incoming parsed message to the appropriate handler.
     *
     * @param message the parsed incoming message
     */
    private void dispatch(MessageParser.Message message) throws Exception {
        switch (message.type) {
            case Protocol.SYS_CONNECTED -> systemHandler.onConnected(message);
            case Protocol.SYS_PONG -> pingService.onPong(message);
            case Protocol.SYS_CHAT_BROADCAST -> systemHandler.onChatBroadcast(message);
            case Protocol.SYS_CHAT_PRIVATE -> systemHandler.onChatPrivate(message);
            case Protocol.SYS_CHAT_LOBBY -> systemHandler.onChatLobby(message);
            case Protocol.SYS_LOBBY_LIST -> systemHandler.onLobbyList(message);
            case Protocol.SYS_LOBBY_CREATED -> systemHandler.onLobbyCreated(message);
            case Protocol.SYS_LOBBY_JOINED -> systemHandler.onLobbyJoined(message);
            case Protocol.SYS_LOBBY_INFO -> systemHandler.onLobbyInfo(message);
            case Protocol.SYS_LOBBY_LEFT -> systemHandler.onLobbyLeft(message);
            case Protocol.MOVE_UPDATE -> movementHandler.onMoveUpdate(message);
            case Protocol.SYS_PLAYERS_LIST -> systemHandler.onPlayerList(message);
            case Protocol.SYS_GAME_START   -> systemHandler.onGameStart(message);
            case Protocol.SYS_STROKE_UPDATE -> systemHandler.onStrokeUpdate(message);
            case Protocol.SYS_GAME_END     -> systemHandler.onGameEnd(message);
            case Protocol.SYS_OBSTACLE_UPDATE -> systemHandler.onObstacleUpdate(message);
            case Protocol.SYS_SCORE_UPDATE -> systemHandler.onScoreUpdate(message);
        }
    }
}