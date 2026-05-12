package org.golfit.server.handlers;

import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.ServerContext;
import org.golfit.server.game.ServerGameSimulation;
import org.golfit.server.serverApi.ServerApi;
import org.golfit.shared.lobby.Lobby;
import org.golfit.shared.messageParser.MessageParser;
import org.golfit.shared.protocol.Protocol;
import org.golfit.shared.constants.MapData;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles all system-level protocol messages on the server side (connect,
 * disconnect, chat, lobbies, movement, and scoring).
 */
public class SystemHandler {

    private final ServerApi serverApi;
    private final ServerContext serverContext;
    private ServerGameSimulation gameSimulation;

    /**
     * Creates a new server-side SystemHandler.
     *
     * @param serverApi     the server API for sending responses and managing state
     * @param serverContext the shared context providing access to clients and
     *                      lobbies
     */
    public SystemHandler(ServerApi serverApi, ServerContext serverContext) {
        this.serverApi = serverApi;
        this.serverContext = serverContext;
    }

    /**
     * Handles client connection. Releases old username, claims new one,
     * and sends back connection info plus score history.
     */
    public void onConnect(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        String previousUsername = clientHandler.getUsername();
        if (previousUsername != null) {
            serverApi.releaseUsername(previousUsername);
        }

        String username = (message.payload != null && !message.payload.isEmpty())
                ? message.payload
                : "player";

        if (!serverApi.claimUsername(username)) {
            String base = username;
            int counter = 1;
            do {
                username = base + "_" + String.format("%03d", counter);
                counter++;
            } while (!serverApi.claimUsername(username));
        }

        clientHandler.setUsername(username);
        System.out.println("Client " + clientHandler.getId() + " connected with username " + username);

        String payload = clientHandler.getId() + ":" + username;
        serverApi.sendConnected(clientHandler, payload);
        serverApi.sendLobbyList(clientHandler);

        String history = buildScoreHistory();
        if (!history.isEmpty()) {
            serverApi.sendScoreUpdate(clientHandler, history);
        }
    }

    /**
     * Responds to a ping by refreshing the client's last-seen timestamp and sending
     * a pong.
     */
    public void onPing(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        clientHandler.updateLastPing();
        serverApi.sendPong(clientHandler);
    }

    /** Broadcasts a chat message from the sender to all connected clients. */
    public void onChatBroadcast(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        String payload = clientHandler.getUsername() + ":" + message.payload;
        serverApi.broadcastChat(Protocol.ID_SERVER, payload);
    }

    /**
     * Routes a private message to the named target player. Silently ignores unknown
     * targets.
     */
    public void onChatPrivate(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        String[] parts = message.payload.split(":", 2);
        if (parts.length < 2)
            return;

        String targetUsername = parts[0];
        String data = clientHandler.getUsername() + ":" + parts[1];

        serverApi.getClients().values().forEach(client -> {
            if (client.getUsername().equals(targetUsername)) {
                try {
                    serverApi.sendPrivateChat(client, client.getId(), data);
                } catch (Exception ignored) {
                }
            }
        });
    }

    /** Relays a chat message to every member of the sender's lobby. */
    public void onChatLobby(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        String[] parts = message.payload.split(":", 2);
        if (parts.length < 2)
            return;
        int lobbyId = Integer.parseInt(parts[0]);
        String payload = clientHandler.getUsername() + ":" + parts[1];

        Lobby lobby = serverContext.getLobbies().get(lobbyId);
        if (lobby != null) {
            lobby.getClients().forEach(client -> {
                try {
                    serverApi.sendLobbyChat(client, payload);
                } catch (Exception ignored) {
                }
            });
        }
    }

    /**
     * Cleans up after a client disconnects: removes them from lobbies and releases
     * their username.
     */
    public void onDisconnect(ClientHandler clientHandler) {
        System.out.println("Client " + clientHandler.getUsername() + " disconnected");
        serverApi.disconnectClient(clientHandler);
    }

    /**
     * Creates a new lobby with the requesting client as host, then broadcasts the
     * updated lobby list.
     */
    public void onCreateLobby(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        int lobbyId = serverContext.generateUniqueId();
        serverContext.getLobbies().put(lobbyId, new Lobby(lobbyId, clientHandler));

        refreshLobbyLists();

        clientHandler.getChannelManager().send(Protocol.SYS_LOBBY_JOINED, Protocol.CH_SYSTEM, Protocol.ID_SERVER,
                String.valueOf(lobbyId));
        serverApi.sendLobbyInfo(clientHandler, String.valueOf(lobbyId));
    }

    /** Sends the current lobby list to the requesting client. */
    public void onLobbyList(ClientHandler clientHandler) throws Exception {
        serverApi.sendLobbyList(clientHandler);
    }

    /**
     * Adds the client to the requested lobby and notifies all members with an
     * updated player list.
     */
    public void onLobbyJoin(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        Lobby lobby = serverContext.getLobbies().get(Integer.parseInt(message.payload));
        if (lobby == null)
            return;

        if (!lobby.addClient(clientHandler))
            return;

        clientHandler.getChannelManager().send(Protocol.SYS_LOBBY_JOINED, Protocol.CH_SYSTEM, Protocol.ID_SERVER,
                message.payload);

        lobby.getClients().forEach(client -> {
            try {
                serverApi.sendLobbyInfo(client, message.payload);
            } catch (Exception ignored) {
            }
        });
        refreshLobbyLists();
    }

    /**
     * Removes the client from a lobby, notifies remaining members, and confirms the
     * leave to the client.
     */
    public void onLobbyLeave(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        Lobby lobby = serverContext.getLobbies().get(Integer.parseInt(message.payload));
        if (lobby == null)
            return;

        lobby.removeClient(clientHandler);
        if (lobby.getClients().isEmpty()) {
            serverContext.getLobbies().remove(lobby.getId());
        } else {
            lobby.getClients().forEach(client -> {
                try {
                    serverApi.sendLobbyInfo(client, message.payload);
                } catch (Exception ignored) {
                }
            });
        }
        serverApi.sendLeaveLobby(clientHandler, message.payload);
        refreshLobbyLists();
    }

    /**
     * Handles a movement-input packet; actual physics are driven by the game
     * simulation tick.
     */
    public void onMoveInput(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        // Position updates are handled via the game simulation tick
    }

    /**
     * Sends the list of all currently connected players to the requesting client.
     */
    public void onPlayerList(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        String payload = serverContext.getClients().values().stream()
                .map(ClientHandler::getUsername)
                .collect(Collectors.joining(":"));
        serverApi.sendPlayerList(clientHandler, payload);
    }

    /**
     * Starts the game in the specified lobby: initialises physics, registers
     * players, and broadcasts SYS_GAME_START.
     */
    public void onLobbyStart(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        if (message.payload == null || message.payload.isEmpty())
            return;

        int lobbyId = Integer.parseInt(message.payload.trim());
        Lobby lobby = serverContext.getLobbies().get(lobbyId);

        if (lobby == null || lobby.getClients().isEmpty())
            return;

        lobby.setStatus(Lobby.LobbyStatus.ongoing);
        System.out.println("Game starting in lobby " + lobbyId);

        if (gameSimulation != null) {
            // physics initialization with unique map instance
            MapData lobbyMap = new MapData();
            gameSimulation.initLobbyPhysics(lobbyId, lobbyMap);

            for (ClientHandler client : lobby.getClients()) {
                gameSimulation.registerPlayerLobby(client.getId(), lobbyId);
            }
        }

        serverApi.sendGameStart(lobby);
        refreshLobbyLists();
    }

    /**
     * Concludes a game for the given lobby: determines the winner, persists
     * results,
     * broadcasts the outcome, and pushes the updated score history to all clients.
     *
     * @param lobbyId       the ID of the lobby whose game just finished
     * @param strokeResults map of player ID → total stroke count
     */
    public void handleGameEnd(int lobbyId, Map<Integer, Integer> strokeResults) {
        Lobby lobby = serverContext.getLobbies().get(lobbyId);
        if (lobby == null)
            return;

        // Build username map preserving finish order
        Map<String, Integer> namedResults = new LinkedHashMap<>();
        Map<Integer, String> idToUsername = new HashMap<>();
        for (ClientHandler client : lobby.getClients()) {
            idToUsername.put(client.getId(), client.getUsername());
        }

        for (Map.Entry<Integer, Integer> entry : strokeResults.entrySet()) {
            String username = idToUsername.get(entry.getKey());
            if (username != null)
                namedResults.put(username, entry.getValue());
        }

        String winner = namedResults.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Nobody");

        String resultsList = namedResults.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(","));
        String payload = "winner=" + winner + ";" + resultsList;

        System.out.println("=== GAME OVER [Lobby " + lobbyId + "] ===");
        System.out.println("Winner: " + winner);
        saveResults(lobbyId, winner, namedResults);

        try {
            serverApi.sendGameEnd(lobby, payload);
        } catch (Exception ignored) {
        }

        lobby.setStatus(Lobby.LobbyStatus.open);

        // Push updated history to everyone
        String scorePayload = buildScoreHistory();
        for (ClientHandler client : serverContext.getClients().values()) {
            try {
                serverApi.sendScoreUpdate(client, scorePayload);
            } catch (Exception ignored) {
            }
        }

        refreshLobbyLists();
    }

    /**
     * Handles a color-preference packet from a client.
     * Stores the choice, then pushes updated {@code LOBBY_INFO} and
     * {@code SYS_PLAYER_COLOR}
     * to all members of the sender's lobby so every client sees the same colors.
     */
    public void onPlayerColor(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        String hex = (message.payload == null || message.payload.isBlank()
                || "DEFAULT".equals(message.payload)) ? null : message.payload;
        clientHandler.setColorHex(hex);

        Lobby lobby = getLobbyForPlayer(clientHandler);
        if (lobby == null)
            return;

        String lobbyId = String.valueOf(lobby.getId());
        for (ClientHandler member : lobby.getClients()) {
            serverApi.sendLobbyInfo(member, lobbyId);
        }
        serverApi.broadcastPlayerColors(lobby);
    }

    /**
     * Handles the cheat-code packet by instantly teleporting the requesting player
     * to the hole.
     */
    public void onHesoyam(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        if (gameSimulation != null) {
            gameSimulation.cheatTeleport(clientHandler.getId());
        }
    }

    /** Appends a CSV row for the finished game to {@code results.csv}. */
    private void saveResults(int lobbyId, String winner, Map<String, Integer> results) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("results.csv", true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String row = timestamp + "," + lobbyId + "," + winner + "," +
                    results.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue())
                            .collect(Collectors.joining("|"));
            pw.println(row);
        } catch (IOException e) {
            System.err.println("Failed to save results: " + e.getMessage());
        }
    }

    /**
     * Reads {@code results.csv} and returns a newline-separated
     * {@code "winner:strokes"} history string.
     */
    private String buildScoreHistory() {
        File csvFile = new File("results.csv");
        if (!csvFile.exists())
            return "";
        List<int[]> sortKeys = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                String[] cols = line.split(",", 4);
                if (cols.length < 4)
                    continue;
                String timestamp = cols[0].trim();
                String winner = cols[2].trim();
                String[] allEntries = cols[3].split("\\|");

                // Collect winner entry first, then all others
                String winnerEntry = null;
                int winnerStrokes = Integer.MAX_VALUE;
                List<String> otherEntries = new ArrayList<>();
                for (String entry : allEntries) {
                    String[] parts = entry.trim().split(":");
                    if (parts.length < 2)
                        continue;
                    String playerName = parts[0].trim();
                    String strokes = parts[1].trim();
                    if (playerName.equalsIgnoreCase(winner)) {
                        winnerEntry = playerName + ":" + strokes;
                        try {
                            winnerStrokes = Integer.parseInt(strokes);
                        } catch (NumberFormatException ignored2) {
                        }
                    } else {
                        otherEntries.add(playerName + ":" + strokes);
                    }
                }

                if (winnerEntry == null)
                    continue; // skip malformed rows
                StringBuilder row = new StringBuilder(timestamp).append("~").append(winnerEntry);
                for (String other : otherEntries) {
                    row.append("|").append(other);
                }
                lines.add(row.toString());
                sortKeys.add(new int[] { winnerStrokes, lines.size() - 1 });
            }
        } catch (IOException ignored) {
        }

        // Sort ascending by winner stroke count (best game first)
        sortKeys.sort(java.util.Comparator.comparingInt(k -> k[0]));

        StringBuilder result = new StringBuilder();
        for (int[] key : sortKeys) {
            result.append(lines.get(key[1])).append("\n");
        }
        return result.toString().trim();
    }

    /** Pushes an updated lobby list to every connected client. */
    private void refreshLobbyLists() {
        serverContext.getClients().values().forEach(client -> {
            try {
                serverApi.sendLobbyList(client);
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * Wires in the physics simulation so lobby-start and game-end events can drive
     * it.
     */
    public void setGameSimulation(ServerGameSimulation sim) {
        this.gameSimulation = sim;
    }

    /**
     * Returns the lobby that currently contains the given client, or {@code null}
     * if not in any lobby.
     */
    public Lobby getLobbyForPlayer(ClientHandler clientHandler) {
        for (Lobby lobby : serverContext.getLobbies().values()) {
            if (lobby.getClients().contains(clientHandler))
                return lobby;
        }
        return null;
    }

    /**
     * Builds the {@code "username:strokes,..."} payload for a stroke-update
     * broadcast.
     */
    public String buildStrokePayload(Lobby lobby) {
        if (gameSimulation == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (ClientHandler client : lobby.getClients()) {
            if (sb.length() > 0)
                sb.append(",");
            sb.append(client.getUsername()).append(":").append(gameSimulation.getStrokeCount(client.getId()));
        }
        return sb.toString();
    }
}