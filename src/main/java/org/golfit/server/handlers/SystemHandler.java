package org.golfit.server.handlers;

import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.server.ServerContext;
import org.golfit.server.game.ServerGameSimulation;
import org.golfit.server.serverApi.ServerApi;
import org.golfit.shared.lobby.Lobby;
import org.golfit.shared.messageParser.MessageParser;
import org.golfit.shared.protocol.Protocol;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles all system-level protocol messages on the server side (connect,
 * disconnect, chat, lobbies, movement).
 */
public class SystemHandler {

    private final ServerApi serverApi;
    private final ServerContext serverContext;
    private ServerGameSimulation gameSimulation;

    /**
     * Creates a new SystemHandler.
     *
     * @param serverApi      the server API for sending responses
     * @param serverContext   provides access to shared server state
     */
    public SystemHandler(ServerApi serverApi, ServerContext serverContext) {
        this.serverApi = serverApi;
        this.serverContext = serverContext;
    }

    /** Handles a client connection or username change. Releases any old username, claims the new one, and sends back the assignment. */
    public void onConnect(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        //Release the old username so it can be reclaimed (during /changeUsername)
        String previousUsername = clientHandler.getUsername();
        if(previousUsername != null) {
            serverApi.releaseUsername(previousUsername);
        }

        String username = (message.payload != null && !message.payload.isEmpty())
                ? message.payload : "player";

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

        // Send back: assignedId:finalUsername
        String payload = clientHandler.getId() + ":" + username;
        serverApi.sendConnected(clientHandler, payload);
        serverApi.sendLobbyList(clientHandler);

        String history = buildScoreHistory();
        if (!history.isEmpty()) {
            serverApi.sendScoreUpdate(clientHandler, history);
        }
    }
    /** Handles a ping from a client by updating their last activity timestamp and responding with a pong. */
    public void onPing(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        clientHandler.updateLastPing();
        serverApi.sendPong(clientHandler);
    }

    /** Handles a public chat message by prefixing the sender's username and broadcasting to all clients. */
    public void onChatBroadcast(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
       String payload = clientHandler.getUsername() + ":" + message.payload;
        serverApi.broadcastChat(Protocol.ID_SERVER, payload);
    }

    /** Handles a private chat message by forwarding it to the target client. */
    public void onChatPrivate(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        String[] parts = message.payload.split(":", 2);
        if (parts.length < 2) {
            System.out.println("Invalid private chat format from " + clientHandler.getUsername());
            return;
        }
        String targetUsername = parts[0];
        String data = clientHandler.getUsername() + ":" + parts[1];
        Map<String, ClientHandler> targetClientHandler = serverApi.getClients();
        for(ClientHandler client : targetClientHandler.values()) {
            if(client.getUsername().equals(targetUsername)) {
                serverApi.sendPrivateChat(client, client.getId(), data);
            }
        }
    }

    /** Handles a lobby chat message by forwarding it to all members of the target lobby. */
    public void onChatLobby(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        String[] parts = message.payload.split(":", 2);
        if (parts.length < 2) return;
        int lobbyId = Integer.parseInt(parts[0]);
        String payload = clientHandler.getUsername() + ":" + parts[1];

        Lobby lobby = serverContext.getLobbies().get(lobbyId);
        if (lobby != null) {
            lobby.getClients().forEach(client -> {
                try {
                    serverApi.sendLobbyChat(client, payload);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * Handles client disconnection by notifying the server API.
     */
    public void onDisconnect(ClientHandler clientHandler) {
        System.out.println("Client " + clientHandler.getUsername() + " disconnected");
        serverApi.disconnectClient(clientHandler);
    }

    /** Handles a lobby creation request. */
    public void onCreateLobby(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        int lobbyId = serverContext.generateUniqueId();

        serverContext.getLobbies().put(lobbyId, new Lobby(lobbyId, clientHandler));
        serverApi.getClients().values().forEach(client -> {
            try {
                serverApi.sendLobbyList(client);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        clientHandler.getChannelManager().send(Protocol.SYS_LOBBY_JOINED, Protocol.CH_SYSTEM, Protocol.ID_SERVER, String.valueOf(lobbyId));
        serverApi.sendLobbyInfo(clientHandler, String.valueOf(lobbyId));
    }

    /** Handles a request for the list of available lobbies. */
    public void onLobbyList(ClientHandler clientHandler) throws Exception {
        serverApi.sendLobbyList(clientHandler);
    }

    /** Handles a request to join a lobby. */
    public void onLobbyJoin(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        Lobby lobby = serverContext.getLobbies().get(Integer.parseInt(message.payload));
        if (lobby == null) {
            System.out.println("Lobby not found: " + message.payload);
            return;
        }
        lobby.addClient(clientHandler);
        // Send join confirmation to the joining client
        clientHandler.getChannelManager().send(
                Protocol.SYS_LOBBY_JOINED, Protocol.CH_SYSTEM, Protocol.ID_SERVER, message.payload);
        LinkedHashSet<ClientHandler> clients = lobby.getClients();
        clients.forEach((client -> {
            try {
                serverApi.sendLobbyInfo(client,message.payload);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
        serverApi.getClients().values().forEach(client -> {
            try {
                serverApi.sendLobbyList(client);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** Handles a request to leave the current lobby. */
    public void onLobbyLeave(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        Lobby lobby = serverContext.getLobbies().get(Integer.parseInt(message.payload));
        if (lobby == null) {
            System.out.println("Lobby not found: " + message.payload);
            return;
        }
        lobby.removeClient(clientHandler);
        if (lobby.getClients().isEmpty()) {
            serverContext.getLobbies().remove(lobby.getId());
        } else {
            lobby.getClients().forEach((client -> {
                try {
                    serverApi.sendLobbyInfo(client, message.payload);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        serverApi.sendLeaveLobby(clientHandler, message.payload);
        serverApi.getClients().values().forEach(client -> {
            try {
                serverApi.sendLobbyList(client);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** Handles movement input from a client. */
    public void onMoveInput(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        System.out.println("Client " + clientHandler.getUsername() + " sent move input");
    }

    /** Handles a request for the list of all connected players. */
    public void onPlayerList(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        String payload = serverContext.getClients().values().stream().map(ClientHandler::getUsername).collect(Collectors.joining(":"));
        serverApi.sendPlayerList(clientHandler, payload);
    }

    /**
     * Handles a request to start the game for a lobby.
     */
    public void onLobbyStart(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        if (message.payload == null || message.payload.isEmpty())
            return;
        int lobbyId;
        try {
            lobbyId = Integer.parseInt(message.payload.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid lobby ID in SYS_LOBBY_START: " + message.payload);
            return;
        }
        Lobby lobby = serverContext.getLobbies().get(lobbyId);
        if (lobby == null) {
            System.err.println("Lobby not found for start request: " + lobbyId);
            return;
        }
        if (lobby.getClients().size() < 1) {
            System.out.println("Start rejected: lobby " + lobbyId + " is empty.");
            return;
        }
        lobby.setStatus(Lobby.LobbyStatus.ongoing);
        System.out.println("Game starting in lobby " + lobbyId);

        // Register each player to this lobby in the simulation
        if (gameSimulation != null) {
            for (ClientHandler client : lobby.getClients()) {
                gameSimulation.registerPlayerLobby(client.getId(), lobbyId);
            }
        }

        serverApi.sendGameStart(lobby);

        // Broadcast updated lobby list so others see status "ongoing"
        serverContext.getClients().values().forEach(client -> {
            try {
                serverApi.sendLobbyList(client);
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * Sets the game simulation reference so we can register players and set callbacks.
     */
    public void setGameSimulation(ServerGameSimulation sim) {
        this.gameSimulation = sim;
    }

    /**
     * Finds which lobby a ClientHandler belongs to. Returns null if not in any lobby.
     */
    public Lobby getLobbyForPlayer(ClientHandler clientHandler) {
        for (Lobby lobby : serverContext.getLobbies().values()) {
            if (lobby.getClients().contains(clientHandler)) return lobby;
        }
        return null;
    }

    /**
     * Builds a stroke-update payload string for all clients in a lobby.
     * Format: "username1:strokes,username2:strokes,..."
     */
    public String buildStrokePayload(Lobby lobby) {
        if (gameSimulation == null) return "";
        StringBuilder sb = new StringBuilder();
        for (ClientHandler client : lobby.getClients()) {
            if (sb.length() > 0) sb.append(",");
            sb.append(client.getUsername()).append(":").append(gameSimulation.getStrokeCount(client.getId()));
        }
        return sb.toString();
    }

    /**
     * Called by the game simulation when all players in a lobby have finished.
     * Computes the winner, prints it to terminal, saves to results.csv, broadcasts SYS_GAME_END,
     * then removes all players from the lobby and deletes the lobby.
     */
    public void handleGameEnd(int lobbyId, Map<Integer, Integer> strokeResults) {
        Lobby lobby = serverContext.getLobbies().get(lobbyId);
        if (lobby == null) return;

        // Build username → strokes map in finish order (so ties are broken by who finished first).
        // We iterate strokeResults (which is ordered by finish time) and look up the username for each ID.
        Map<String, Integer> namedResults = new LinkedHashMap<>();
        Map<Integer, String> idToUsername = new HashMap<>();
        for (ClientHandler client : lobby.getClients()) {
            idToUsername.put(client.getId(), client.getUsername());
        }
        for (Map.Entry<Integer, Integer> entry : strokeResults.entrySet()) {
            String username = idToUsername.get(entry.getKey());
            if (username != null) namedResults.put(username, entry.getValue());
        }

        // Find winner (fewest strokes; 0 strokes means they never hit — treat as worst)
        String winner = namedResults.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(namedResults.keySet().stream().findFirst().orElse("Nobody"));

        // Build result strings
        String resultsList = namedResults.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(","));
        String payload = "winner=" + winner + ";" + resultsList;

        // Print to terminal
        System.out.println("=== GAME OVER [Lobby " + lobbyId + "] ===");
        System.out.println("Winner: " + winner);
        namedResults.forEach((u, s) -> System.out.println("  " + u + ": " + s + " strokes"));
        System.out.println("========================================");

        // Persist to results.csv
        saveResults(lobbyId, winner, namedResults);

        // Broadcast game-end to all lobby clients
        try {
            serverApi.sendGameEnd(lobby, payload);
        } catch (Exception e) {
            System.err.println("Error sending game end: " + e.getMessage());
        }


        lobby.setStatus(Lobby.LobbyStatus.open);

        String scorePayload = buildScoreHistory();
        for (ClientHandler client : serverContext.getClients().values()) {
            try {
                serverApi.sendScoreUpdate(client, scorePayload);
            } catch (Exception e) {
                System.err.println("Failed to send score update: " + e.getMessage());
            }
        }

        serverContext.getClients().values().forEach(client -> {
            try { serverApi.sendLobbyList(client); } catch (Exception ignored) {}
        });
    }

    public void onHesoyam(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        gameSimulation.cheatTeleport(clientHandler.getId());
    }

    /**
     * Appends a game result to results.csv.
     */
    private void saveResults(int lobbyId, String winner, Map<String, Integer> results) {
         try (PrintWriter pw = new PrintWriter(new FileWriter("results.csv", true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String row = timestamp + "," + lobbyId + "," + winner + "," +
                results.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining("|"));
            pw.println(row);
        } catch (IOException e) {
            System.err.println("Failed to save results: " + e.getMessage());
        }
    }

    /**
     * Builds a score history string from results.csv.
     */
    private String buildScoreHistory() {
        File csvFile = new File("results.csv");
        if (!csvFile.exists()) return "";
        StringBuilder scores = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // Format: timestamp,lobbyId,winner,player1:strokes|player2:strokes
                String[] cols = line.split(",", 4);
                if (cols.length < 4) continue;
                String winner = cols[2].trim();
                for (String entry : cols[3].split("\\|")) {
                    String[] parts = entry.trim().split(":");
                    if (parts.length < 2) continue;
                    if (parts[0].trim().equalsIgnoreCase(winner)) {
                        scores.append(winner).append(":").append(parts[1].trim()).append("\n");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read results.csv: " + e.getMessage());
        }
        return scores.toString().trim();
    }
}
