package org.golfit.client;

import org.golfit.client.cli.ClientCli;
import org.golfit.client.cli.ConsoleManager;
import org.golfit.client.handlers.SystemHandler;
import org.golfit.client.pingService.PingService;
import org.golfit.shared.constants.Constants;
import org.golfit.shared.messageParser.MessageParser;
import org.golfit.shared.protocol.Protocol;
import org.golfit.shared.protocol.channelManager.ChannelManager;
import org.slf4j.Logger;
import org.jline.reader.UserInterruptException;

import java.net.*;
import java.nio.charset.StandardCharsets;
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
    PingService pingService;
    /** CLI interface for user input **/
    ClientCli clientCli;

    /** Handlers for different message types **/
    private SystemHandler systemHandler;

    /** Server hostname and port  **/
    final private String host;

    /** Server assigned identifier for the client (Default 0) **/
    int id = 0;

    int lobbyId = 0;

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
        this.username = username != null ? username : System.getProperty("user.name");
    }

    /**
     * Starts the client and initiates a connection to the server.
     *
     * <p>This method:
     * <ol>
     *   <li>Opens a UDP socket</li>
     *   <li>Initializes the console, channel manager, and all handlers</li>
     *   <li>Starts the channel manager ticker, input thread, and receive thread</li>
     *   <li>Sends the initial {@code SYS_CONNECT} message to the server</li>
     *   <li>Starts the ping service</li>
     * </ol>
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

        // initiates ticker for resending messages
        ScheduledExecutorService ticker = Executors.newSingleThreadScheduledExecutor();
        ticker.scheduleAtFixedRate(() -> {
            try {
                channelManager.tick();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        //initiates input thread
        Thread inputThread = new Thread(() -> {
            while (true) {
                try {
                    String line = ConsoleManager.readLine("> ");
                    if (line != null) clientCli.processCommand(line);
                } catch(UserInterruptException e) {
                    break;
                }
                catch (Exception e) {
                    break;
                }
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();

        // initiates receive thread
        Thread receiveThread = new Thread(()-> {
            while (true) {
                try {
                    byte[] raw = new byte[Constants.MAX_PACKET_SIZE];
                    DatagramPacket datagramPacket = new DatagramPacket(raw, raw.length);
                    socket.receive(datagramPacket);
                    System.arraycopy(datagramPacket.getData(), 0, raw, 0, datagramPacket.getLength());
                    channelManager.onPacketReceived(new String(raw, 0, datagramPacket.getLength(), StandardCharsets.UTF_8));
                } catch (Exception e) {
                    if (socket.isClosed()) break;
                    logger.error(e.getMessage());
                }
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();

        clientApi.connect(username);
        pingService.start();
    }

    /**
     * Dispatches an incoming parsed message to the appropriate handler.
     *
     * <p>Routing is based on message type:
     * <ul>
     *   <li>{@code SYS_CONNECTED} — handled by {@link SystemHandler}, updates client ID and username</li>
     *   <li>{@code SYS_PONG} — handled by {@link PingService}, resets the ping timer</li>
     * </ul>
     *
     * @param message the parsed incoming message
     */
    private void dispatch(MessageParser.Message message) throws Exception {
        switch(message.type) {
            case Protocol.SYS_CONNECTED -> systemHandler.onConnected(message);
            case Protocol.SYS_PONG -> pingService.onPong(message);
            case Protocol.SYS_CHAT -> systemHandler.onChat(message);
            case Protocol.SYS_CHAT_PRIVATE -> systemHandler.onChatPrivate(message);
            case Protocol.SYS_LOBBY_LIST -> systemHandler.onLobbyList(message);
            case Protocol.SYS_LOBBY_CREATED -> systemHandler.onLobbyCreated(message);
            case Protocol.SYS_LOBBY_JOINED -> systemHandler.onLobbyJoined(message);
            case Protocol.SYS_LOBBY_LEAVE -> systemHandler.onLobbyLeft(message);
            case Protocol.MOVE_UPDATE -> systemHandler.onMoveUpdate(message);

        }
    }
}