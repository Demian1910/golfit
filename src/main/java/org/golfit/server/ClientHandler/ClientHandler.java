package org.golfit.server.ClientHandler;

import org.golfit.shared.constants.Constants;
import org.golfit.shared.messageParser.MessageParser;
import org.golfit.shared.protocol.Protocol;
import org.golfit.shared.protocol.channelManager.ChannelManager;
import org.slf4j.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.*;

/** Handles a single connected client on the server side. Runs its own thread with a message queue and periodic channel tick. */
public class ClientHandler implements Runnable {
    /** Tracks whether a client is in the menu or actively playing a game. */
    enum ClientState {
        IN_MENU,
        IN_GAME
    }

    private final int Id;
    private final String key;
    private String username;
    private ClientState clientState =  ClientState.IN_MENU;

    private Protocol.PacketDispatcher dispatcher;
    private final DatagramSocket socket;
    private final InetAddress address;
    private final int port;
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    private ChannelManager channelManager;
    private long lastPing = System.currentTimeMillis();

    private Thread thread;

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ClientHandler.class);

    /**
     * Creates a new ClientHandler for a connected client.
     *
     * @param key        the client identifier in "ip:port" format
     * @param socket     the server's UDP socket used for sending responses
     * @param address    the client's IP address
     * @param port       the client's port number
     * @param Id         the unique client ID
     * @param dispatcher callback to dispatch parsed messages to server handlers
     */
    public ClientHandler(String key, DatagramSocket socket, InetAddress address, int port, int Id, Protocol.PacketDispatcher dispatcher) {
        this.key = key;
        this.socket = socket;
        this.address = address;
        this.port = port;
        this.Id = Id;
        this.dispatcher = dispatcher;
        this.channelManager = new ChannelManager((data) -> {
            byte[] byteData = data.getBytes(StandardCharsets.UTF_8);
            DatagramPacket pkg = new DatagramPacket(byteData, byteData.length, address, port);
            socket.send(pkg);
        }, this::dispatch, Protocol.ID_SERVER);
    }

    /** Enqueues a raw packet string for processing by this handler's thread. */
    public void queueMessage(String raw) {
        messageQueue.offer(raw);
    }

    /** Main loop: starts a 100ms channel tick and processes queued messages until interrupted. */
    @Override
    public void run() {
        thread = Thread.currentThread();
        ScheduledExecutorService ticker = Executors.newSingleThreadScheduledExecutor();
        ticker.scheduleAtFixedRate(()->{
            try {
                channelManager.tick();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        },0, 100, TimeUnit.MILLISECONDS);

        try {
            while (!thread.isInterrupted()) {
             String raw = messageQueue.take();
             channelManager.onPacketReceived(raw);
            }
        } catch (InterruptedException e){
            logger.info("ClientHandler thread interrupted");
        }
        catch (Exception e) {
            logger.error("Error: " + e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
        } finally {
            ticker.shutdown();
        }
    }

    /** Forwards a parsed message to the server's dispatch logic. */
    private void dispatch(MessageParser.Message message) throws Exception {
        dispatcher.dispatch(message);
    }
    /** Returns the channel manager handling reliable delivery for this client. */
    public ChannelManager getChannelManager() {
        return channelManager;
    }

    /** Returns the unique client ID assigned by the server. */
    public int getId() {
        return Id;
    }
    /** Sets this client's username. */
    public void setUsername(String username) {
        this.username = username;
    }
    /** Updates the last ping timestamp to the current time. */
    public void updateLastPing() {
        lastPing = System.currentTimeMillis();
    }
    /** Returns true if no ping has been received within the timeout window. */
    public boolean isTimedOut() {
        return System.currentTimeMillis() - lastPing > Constants.TIMEOUT_PING_MS;
    }
    /** Returns this client's username. */
    public String getUsername() {
        return username;
    }
    /** Interrupts this handler's processing thread. */
    public void shutdown() {
        thread.interrupt();
    }
    /** Returns the client's network key in {@code "ip:port"} format. */
    public String getClientKey(){
        return key;
    }

}
