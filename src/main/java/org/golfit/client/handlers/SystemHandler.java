package org.golfit.client.handlers;

import org.golfit.client.clientLogic.ClientApi;
import org.golfit.client.cli.ConsoleManager;
import org.golfit.client.eventBus.EventBus;
import org.golfit.client.gui.Sidebar;
import org.golfit.shared.constants.Constants;
import org.golfit.shared.messageParser.MessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/** Handles system-level protocol responses on the client side (connection confirmations, chat, lobbies, movement updates). */
public class SystemHandler {
    private final ClientApi clientApi;
    private final Logger logger = LoggerFactory.getLogger(SystemHandler.class);
    private Sidebar sidebar;

    /**
     * Creates a new client-side SystemHandler.
     *
     * @param clientApi the client API for accessing client state
     */
    public SystemHandler(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

    /** Sets the sidebar reference for legacy direct message display. */
    public void setSidebar(Sidebar sidebar) {
        this.sidebar = sidebar;
    }


    /** Handles the SYS_CONNECTED response by extracting the assigned client ID and username from the payload. */
    public void onConnected(MessageParser.Message message) {
        if (message.payload != null && !message.payload.isEmpty()) {
            String[] parts = message.payload.split(":");
            if (parts.length >= 1) {
                int assignedId = Integer.parseInt(parts[0]);
                clientApi.setId(assignedId);
                clientApi.getChannelManager().updateLocalId(assignedId);
                logger.info("Connected with ID: " + assignedId);
            }
            if (parts.length >= 2) {
                String assignedUsername = parts[1];
                clientApi.setUsername(assignedUsername);
                logger.info("Connected as: " + assignedUsername);
            }
        }
    }

    /** Handles chat messages*/
    public void onChatBroadcast(MessageParser.Message message) {
        String[] parts = message.payload.split(":", 2);
        if (parts.length < 2) return;
//        String display = "[" + parts[0] + "] " + (parts.length > 1 ? parts[1] : "");
//        ConsoleManager.println(display);
//        if (sidebar != null) sidebar.addMessage(display);
        EventBus.get().emit(Constants.BROADCAST_MESSAGE, parts);
    }

    /** Handles an incoming private chat message. */
    public void onChatPrivate(MessageParser.Message message) {
       String[] parts = message.payload.split(":", 2);
        if (parts.length < 2) return;
//        String display = "Whisper from [" + parts[0] + "] " + parts[1];
//        ConsoleManager.println(display);
//        if (sidebar != null) sidebar.addMessage(display);
        EventBus.get().emit(Constants.PRIVATE_MESSAGE, parts);
    }

    /** Handles an incoming lobby chat message and emits it on the EventBus. */
    public void onChatLobby(MessageParser.Message message) {
        String[] parts = message.payload.split(":", 2);
        if (parts.length < 2) return;
//        String display = "Lobby [" + parts[0] + "] " + parts[1];
//        ConsoleManager.println(display);
//        if (sidebar != null) sidebar.addMessage(display);
        EventBus.get().emit(Constants.LOBBY_MESSAGE, parts);
    }

    /** Handles the lobby list response from the server. */
    public void onLobbyList(MessageParser.Message message) throws Exception {
        String[] lobbies = message.payload.split("&");
        EventBus.get().emit(Constants.LOBBY_LIST, lobbies);
    }

    /**
     * Handles the creation of a lobby
     * @param message The message containing the lobby name in payload
     */
    public void onLobbyCreated(MessageParser.Message message) throws Exception {
        ConsoleManager.println("Lobby created with id: "+message.payload);
    }

    /** Handles confirmation that the client has joined a lobby. */
    public void onLobbyJoined(MessageParser.Message message) {
        ConsoleManager.println("Joined lobby with id: "+message.payload);
        clientApi.setLobbyId(Integer.parseInt(message.payload));
        EventBus.get().emit(Constants.LOBBY_JOINED, message.payload);
    }

    /** Handles the leaving of a lobby*/
    public void onLobbyLeft(MessageParser.Message message) throws Exception {
        clientApi.setLobbyId(-1);
        ConsoleManager.println("Left lobby");
        EventBus.get().emit(Constants.LOBBY_LEFT, null);
    }

    /** Handles a lobby info update with the current player list. */
    public void onLobbyInfo(MessageParser.Message message) throws Exception {
        String[] lobbyInfo = message.payload.split(":", 2);
        String lobbyId = lobbyInfo[0];
        String[] players   = lobbyInfo[1].split(",");
        ConsoleManager.println("Lobby Update: " + lobbyId + "//" + lobbyInfo[1]);
        clientApi.setLobbyId(Integer.parseInt(lobbyId));
        EventBus.get().emit(Constants.LOBBY_INFO, players);

        clientApi.getLobbyPlayers().clear();
        Collections.addAll(clientApi.getLobbyPlayers(), players);
    }

    /** Handles the full player list response and emits it on the EventBus. */
    public void onPlayerList(MessageParser.Message message) throws Exception {
        String[] players = message.payload.split(":");
        EventBus.get().emit(Constants.PLAYERS_LIST, players);
    }

    /** Handles a movement update from the server. */
    public void onMoveUpdate(MessageParser.Message message) {
        ConsoleManager.println("Move update");
    }


    /** Handles a game-start broadcast from the server — fires the GAME_START EventBus event. */
    public void onGameStart(MessageParser.Message message) {
        System.out.println("[DEBUG] [SYSTEM HANDLER] Received SYS_GAME_START! Payload: " + message.payload);
        EventBus.get().emit(Constants.GAME_START, message.payload);
    }

    /** Handles a stroke-update broadcast — fires STROKE_UPDATE with "user:strokes,..." payload. */
    public void onStrokeUpdate(MessageParser.Message message) {
        EventBus.get().emit(Constants.STROKE_UPDATE, message.payload);
    }

    /** Handles a game-end broadcast — fires GAME_END with "winner=X;user1:s,..." payload. */
    public void onGameEnd(MessageParser.Message message) {
        System.out.println("[GAME END] " + message.payload);
        EventBus.get().emit(Constants.GAME_END, message.payload);
    }

    /** Handles an obstacle-update broadcast — fires OBSTACLE_UPDATE with "x,y,..." payload. */
    public void onObstacleUpdate(MessageParser.Message message) {
        System.out.println("[OBSTACLE CHANGE] " + message.payload);
        EventBus.get().emit(Constants.OBSTACLE_UPDATE, message.payload);
    }

    /** Handles a score-update broadcast — fires SCORE_UPDATE with "user:score,..." payload. */
    public void onScoreUpdate(MessageParser.Message message) {
        // Persist the latest scores locally
//        try {
//            java.nio.file.Files.writeString(
//                    java.nio.file.Path.of("results.csv"),
//                    message.payload,
//                    java.nio.file.StandardOpenOption.CREATE,
//                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
//            );
//        } catch (java.io.IOException e) {
//            System.err.println("Failed to write local results.csv: " + e.getMessage());
//        }

        EventBus.get().emit(Constants.SCORE_UPDATE, message.payload);
    }
}
