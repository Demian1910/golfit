package org.golfit.client.handlers;

import org.golfit.client.clientLogic.ClientApi;
import org.golfit.client.cli.ConsoleManager;
import org.golfit.client.eventBus.EventBus;
import org.golfit.client.gui.Sidebar;
import org.golfit.shared.constants.Constants;
import org.golfit.shared.messageParser.MessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.constant.Constable;
import java.util.Collections;

/** Handles system-level protocol responses on the client side (connection confirmations, chat, lobbies, movement updates). */
public class SystemHandler {
    private final ClientApi clientApi;
    private final Logger logger = LoggerFactory.getLogger(SystemHandler.class);
    private Sidebar sidebar;

    public SystemHandler(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

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
//        ConsoleManager.println("Available lobbies:");
//        for(String lobby : lobbies) {
//            ConsoleManager.println("Lobby: "+lobby);
//        }
        EventBus.get().emit(Constants.LOBBY_LIST, lobbies);
    }

    /**
     * Handles the creation of a lobby
     * @param message The message containing the lobby name in payload
     */
    public void onLobbyCreated(MessageParser.Message message) throws Exception {
        ConsoleManager.println("Lobby created with id: "+message.payload);
        clientApi.setLobbyId(Integer.parseInt(message.payload));
    }

    /** Handles confirmation that the client has joined a lobby. */
    public void onLobbyJoined(MessageParser.Message message) {
        ConsoleManager.println("Joined lobby with id: "+message.payload);
        clientApi.setLobbyId(Integer.parseInt(message.payload));
    }

    /** Handles the leaving of a lobby*/
    public void onLobbyLeft(MessageParser.Message message) throws Exception {
        clientApi.setLobbyId(-1);
        ConsoleManager.println("Left lobby");
    }

    public void onLobbyInfo(MessageParser.Message message) throws Exception {
        String[] lobbyInfo = message.payload.split(":", 2);
        if (lobbyInfo.length < 2) return;
        ConsoleManager.println("Lobby Update: " + lobbyInfo[0] + "//" + lobbyInfo[1]);
        clientApi.setLobbyId(Integer.parseInt(lobbyInfo[0]));

        String[] players = lobbyInfo[1].split(",");
        clientApi.getLobbyPlayers().clear();
        Collections.addAll(clientApi.getLobbyPlayers(), players);
    }

    /** Handles a movement update from the server. */
    public void onMoveUpdate(MessageParser.Message message) {
        ConsoleManager.println("Move update");
    }
}
