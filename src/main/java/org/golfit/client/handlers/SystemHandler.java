package org.golfit.client.handlers;

import org.golfit.client.ClientApi;
import org.golfit.client.cli.ConsoleManager;
import org.golfit.shared.messageParser.MessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashSet;

/** Handles system-level protocol responses on the client side (connection confirmations, chat, lobbies, movement updates). */
public class SystemHandler {
    private final ClientApi clientApi;
    private final Logger logger = LoggerFactory.getLogger(SystemHandler.class);

    public SystemHandler(ClientApi clientApi) {
        this.clientApi = clientApi;
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
        ConsoleManager.println("["+parts[0]+"]" + (parts.length > 1 ? parts[1] : ""));
    }

    /** Handles an incoming private chat message. */
    public void onChatPrivate(MessageParser.Message message) {
       String[] parts = message.payload.split(":", 2);
       ConsoleManager.println("Whisper from "+"["+parts[0]+"]" + parts[1]);
    }

    public void onChatLobby(MessageParser.Message message) {
        String[] parts = message.payload.split(":", 2);
        ConsoleManager.println("Lobby message from ["+parts[0]+"]: " + parts[1]);
    }

    /** Handles the lobby list response from the server. */
    public void onLobbyList(MessageParser.Message message) throws Exception {
        String[] lobbies = message.payload.split(":");
        ConsoleManager.println("Available lobbies:");
        for(String lobby : lobbies) {
            ConsoleManager.println("Lobby: "+lobby);
        }
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
