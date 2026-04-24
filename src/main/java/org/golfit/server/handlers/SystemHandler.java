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

        String payload = clientHandler.getId() + ":" + username;
        serverApi.sendConnected(clientHandler, payload);
        serverApi.sendLobbyList(clientHandler);

        String history = buildScoreHistory();
        if (!history.isEmpty()) {
            serverApi.sendScoreUpdate(clientHandler, history);
        }
    }

    public void onPing(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        clientHandler.updateLastPing();
        serverApi.sendPong(clientHandler);
    }

    public void onChatBroadcast(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        String payload = clientHandler.getUsername() + ":" + message.payload;
        serverApi.broadcastChat(Protocol.ID_SERVER, payload);
    }

    public void onChatPrivate(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        String[] parts = message.payload.split(":", 2);
        if (parts.length < 2) return;

        String targetUsername = parts[0];
        String data = clientHandler.getUsername() + ":" + parts[1];

        serverApi.getClients().values().forEach(client -> {
            if (client.getUsername().equals(targetUsername)) {
                try {
                    serverApi.sendPrivateChat(client, client.getId(), data);
                } catch (Exception ignored) {}
            }
        });
    }

    public void onChatLobby(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        String[] parts = message.payload.split(":", 2);
        if (parts.length < 2) return;
        int lobbyId = Integer.parseInt(parts[0]);
        String payload = clientHandler.getUsername() + ":" + parts[1];

        Lobby lobby = serverContext.getLobbies().get(lobbyId);
        if (lobby != null) {
            lobby.getClients().forEach(client -> {
                try { serverApi.sendLobbyChat(client, payload); } catch (Exception ignored) {}
            });
        }
    }

    public void onDisconnect(ClientHandler clientHandler) {
        System.out.println("Client " + clientHandler.getUsername() + " disconnected");
        serverApi.disconnectClient(clientHandler);
    }

    public void onCreateLobby(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        int lobbyId = serverContext.generateUniqueId();
        serverContext.getLobbies().put(lobbyId, new Lobby(lobbyId, clientHandler));

        refreshLobbyLists();

        clientHandler.getChannelManager().send(Protocol.SYS_LOBBY_JOINED, Protocol.CH_SYSTEM, Protocol.ID_SERVER, String.valueOf(lobbyId));
        serverApi.sendLobbyInfo(clientHandler, String.valueOf(lobbyId));
    }

    public void onLobbyList(ClientHandler clientHandler) throws Exception {
        serverApi.sendLobbyList(clientHandler);
    }

    public void onLobbyJoin(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        Lobby lobby = serverContext.getLobbies().get(Integer.parseInt(message.payload));
        if (lobby == null) return;

        lobby.addClient(clientHandler);
        clientHandler.getChannelManager().send(Protocol.SYS_LOBBY_JOINED, Protocol.CH_SYSTEM, Protocol.ID_SERVER, message.payload);

        lobby.getClients().forEach(client -> {
            try { serverApi.sendLobbyInfo(client, message.payload); } catch (Exception ignored) {}
        });
        refreshLobbyLists();
    }

    public void onLobbyLeave(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        Lobby lobby = serverContext.getLobbies().get(Integer.parseInt(message.payload));
        if (lobby == null) return;

        lobby.removeClient(clientHandler);
        if (lobby.getClients().isEmpty()) {
            serverContext.getLobbies().remove(lobby.getId());
        } else {
            lobby.getClients().forEach(client -> {
                try { serverApi.sendLobbyInfo(client, message.payload); } catch (Exception ignored) {}
            });
        }
        serverApi.sendLeaveLobby(clientHandler, message.payload);
        refreshLobbyLists();
    }

    public void onMoveInput(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        // Position updates are handled via the game simulation tick
    }

    public void onPlayerList(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        String payload = serverContext.getClients().values().stream()
                .map(ClientHandler::getUsername)
                .collect(Collectors.joining(":"));
        serverApi.sendPlayerList(clientHandler, payload);
    }

    public void onLobbyStart(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        if (message.payload == null || message.payload.isEmpty()) return;

        int lobbyId = Integer.parseInt(message.payload.trim());
        Lobby lobby = serverContext.getLobbies().get(lobbyId);

        if (lobby == null || lobby.getClients().isEmpty()) return;

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

    public void handleGameEnd(int lobbyId, Map<Integer, Integer> strokeResults) {
        Lobby lobby = serverContext.getLobbies().get(lobbyId);
        if (lobby == null) return;

        // Build username map preserving finish order
        Map<String, Integer> namedResults = new LinkedHashMap<>();
        Map<Integer, String> idToUsername = new HashMap<>();
        for (ClientHandler client : lobby.getClients()) {
            idToUsername.put(client.getId(), client.getUsername());
        }

        for (Map.Entry<Integer, Integer> entry : strokeResults.entrySet()) {
            String username = idToUsername.get(entry.getKey());
            if (username != null) namedResults.put(username, entry.getValue());
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
        } catch (Exception ignored) {}

        lobby.setStatus(Lobby.LobbyStatus.open);

        // Push updated history to everyone
        String scorePayload = buildScoreHistory();
        for (ClientHandler client : serverContext.getClients().values()) {
            try { serverApi.sendScoreUpdate(client, scorePayload); } catch (Exception ignored) {}
        }

        refreshLobbyLists();
    }

    public void onHesoyam(ClientHandler clientHandler, MessageParser.Message message) throws Exception {
        if (gameSimulation != null) {
            gameSimulation.cheatTeleport(clientHandler.getId());
        }
    }

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

    private String buildScoreHistory() {
        File csvFile = new File("results.csv");
        if (!csvFile.exists()) return "";
        StringBuilder scores = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
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
        } catch (IOException ignored) {}
        return scores.toString().trim();
    }

    private void refreshLobbyLists() {
        serverContext.getClients().values().forEach(client -> {
            try { serverApi.sendLobbyList(client); } catch (Exception ignored) {}
        });
    }

    public void setGameSimulation(ServerGameSimulation sim) { this.gameSimulation = sim; }

    public Lobby getLobbyForPlayer(ClientHandler clientHandler) {
        for (Lobby lobby : serverContext.getLobbies().values()) {
            if (lobby.getClients().contains(clientHandler)) return lobby;
        }
        return null;
    }

    public String buildStrokePayload(Lobby lobby) {
        if (gameSimulation == null) return "";
        StringBuilder sb = new StringBuilder();
        for (ClientHandler client : lobby.getClients()) {
            if (sb.length() > 0) sb.append(",");
            sb.append(client.getUsername()).append(":").append(gameSimulation.getStrokeCount(client.getId()));
        }
        return sb.toString();
    }
}