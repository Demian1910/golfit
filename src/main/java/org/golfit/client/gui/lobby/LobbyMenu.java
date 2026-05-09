package org.golfit.client.gui.lobby;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.golfit.client.clientLogic.ClientApi;
import org.golfit.client.eventBus.EventBus;
import org.golfit.client.gui.settings.ClientPreferences;
import org.golfit.client.gui.settings.SettingsMenu;
import org.golfit.shared.constants.Constants;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Sidebar component for lobby management. Shows either the lobby list (with create/join)
 * or the current lobby view (with player list, stroke counts, start, and leave buttons).
 * Listens to EventBus events for lobby updates, game start/end, and stroke updates.
 */
public class LobbyMenu extends VBox {
    ClientApi clientApi;
    VBox lobbyVBox = new VBox(5);
    private VBox playerList;
    private Button leaveLobbyBtn;
    private Button startBtn;

    private final VBox lobbyListView;
    private final VBox inLobbyView;

    private final Map<String, Integer> strokesMap = new HashMap<>();
    private final List<String> currentPlayers = new ArrayList<>();
    
    private static final String[] PLAYER_EMOJIS = {
            "🔴", "🔵", "🟢", "🟡", "🟣", "🟠"
    };

    public LobbyMenu(ClientApi clientApi) {
        this.clientApi = clientApi;

        lobbyListView = buildLobbyListView();

        inLobbyView = buildInLobbyView();
        inLobbyView.setVisible(false);
        inLobbyView.setManaged(false);

        this.setSpacing(8);
        this.setPadding(new Insets(8));
        this.getChildren().addAll(lobbyListView, inLobbyView);

        // Events
        EventBus.get().on(Constants.LOBBY_LIST, data -> {
            String[] lobbyList = (String[]) data;

            javafx.application.Platform.runLater(() -> {
                lobbyVBox.getChildren().clear();
                for (String lobbyString : lobbyList) {
                    if (lobbyString == null || lobbyString.isBlank())
                        continue;
                    String[] lobby = lobbyString.split(":");
                    if (lobby.length < 4)
                        continue;
                    createLobbyRow(lobby);
                }
            });
        });
        EventBus.get().on(Constants.LOBBY_INFO, data -> {
            String[] players = (String[]) data;
            updatePlayers(players);
        });
        EventBus.get().on(Constants.LOBBY_JOINED, data -> {
             strokesMap.clear();
             showInLobbyView();
        });
        EventBus.get().on(Constants.LOBBY_LEFT, data -> {
             strokesMap.clear();
             ClientPreferences.clearLobbyColors();
             showLobbyListView();
        });

        EventBus.get().on(Constants.GAME_START, data -> {
            Platform.runLater(() -> {
                startBtn.setVisible(false);
                startBtn.setManaged(false);
                leaveLobbyBtn.setVisible(false);
                leaveLobbyBtn.setManaged(false);
            });
        });

        EventBus.get().on(Constants.STROKE_UPDATE, data -> {
            String payload = (String) data;
            if (payload != null && !payload.isEmpty()) {
                String[] parts = payload.split(",");
                for (String part : parts) {
                    String[] kv = part.split(":");
                    if (kv.length == 2) {
                        try { strokesMap.put(kv[0], Integer.parseInt(kv[1])); } catch (NumberFormatException ignored) {}
                    }
                }
                updatePlayers(currentPlayers.toArray(new String[0]));
            }
        });

        EventBus.get().on(Constants.GAME_END, data -> {
            Platform.runLater(() -> {
                startBtn.setVisible(true);
                startBtn.setManaged(true);
                leaveLobbyBtn.setVisible(true);
                leaveLobbyBtn.setManaged(true);
                strokesMap.clear();
                updatePlayers(currentPlayers.toArray(new String[0]));
            });
        });

        EventBus.get().on(Constants.BALL_COLOR_CHANGED, data ->
                updatePlayers(currentPlayers.toArray(new String[0])));
    }

    /** Enables or disables the leave button based on lobby membership. */
    public void setInLobby(boolean inLobby) {
        javafx.application.Platform.runLater(() -> {
            leaveLobbyBtn.setDisable(!inLobby);
            leaveLobbyBtn.setStyle(inLobby ? "" : "-fx-opacity: 0.6;");
        });
    }

    /** Creates a UI row for a lobby entry showing host name, player count, status, and join button. */
    public HBox createLobbyRow(String[] lobby) {
        HBox row = new HBox(6);
        row.setPadding(new Insets(4));
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label nameLabel = new Label(lobby[1] + "'s lobby");
        nameLabel.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        Label playerCount = new Label(lobby[2] + "/4");
        playerCount.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        Label status = new Label(lobby[3]);
        status.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        switch (lobby[3]) {
            case "open":
                status.setStyle("-fx-text-fill: green");
                break;
            case "ongoing":
                status.setStyle("-fx-text-fill: yellow");
                break;
            case "finished":
                status.setStyle("-fx-text-fill: red");
                break;
            default:
                status.setStyle("-fx-text-fill: white");
        }
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button joinBtn = new Button("Join");
        joinBtn.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);

        joinBtn.setOnAction(e -> {
            if (clientApi.getLobbyId() > 0)
                return;
            try {
                clientApi.joinLobby(lobby[0]);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        row.getChildren().addAll(nameLabel, playerCount, status, spacer, joinBtn);
        lobbyVBox.getChildren().add(row);
        return row;
    }

    /** Builds the lobby list view with a create button and scrollable lobby rows. */
    private VBox buildLobbyListView() {
        Label title = new Label("Lobbies");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Button createLobbyBtn = new Button("Create Lobby");
        createLobbyBtn.setMaxWidth(Double.MAX_VALUE);
        createLobbyBtn.setOnAction(e -> {
            if (clientApi.getLobbyId() > 0)
                return;
            try {
                clientApi.createLobby();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        HBox buttons = new HBox(6, createLobbyBtn);
        HBox.setHgrow(createLobbyBtn, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(lobbyVBox);
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox view = new VBox(8, title, buttons, scroll);
        VBox.setVgrow(view, Priority.ALWAYS);
        return view;
    }

    /** Builds the in-lobby view with player list, start button, and leave button. */
    private VBox buildInLobbyView() {
        Label title = new Label("Current Lobby");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label playersLabel = new Label("Players:");
        playersLabel.setStyle("-fx-text-fill: white;");
        playerList = new VBox(4);

        startBtn = new Button("▶ START");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setDisable(true);
        startBtn.setStyle("-fx-opacity: 0.5;");
        startBtn.setOnAction(e -> {
            try {
                clientApi.startGame();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        leaveLobbyBtn = new Button("Leave Lobby");
        leaveLobbyBtn.setMaxWidth(Double.MAX_VALUE);
        leaveLobbyBtn.setOnAction(e -> {
            try {
                clientApi.leaveLobby(String.valueOf(clientApi.getLobbyId()));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        VBox view = new VBox(8, title, playersLabel, playerList, startBtn, leaveLobbyBtn);
        VBox.setVgrow(view, Priority.ALWAYS);
        return view;
    }

    /** Switches the display to the in-lobby view. */
    private void showInLobbyView() {
        javafx.application.Platform.runLater(() -> {
            lobbyListView.setVisible(false);
            lobbyListView.setManaged(false);
            inLobbyView.setVisible(true);
            inLobbyView.setManaged(true);
        });
    }

    /** Switches the display back to the lobby list view. */
    private void showLobbyListView() {
        javafx.application.Platform.runLater(() -> {
            inLobbyView.setVisible(false);
            inLobbyView.setManaged(false);
            lobbyListView.setVisible(true);
            lobbyListView.setManaged(true);
        });
    }

    /** Returns the emoji matching one of the 6 preset ball colors, or the first emoji as fallback. */
    private String emojiForColor(javafx.scene.paint.Color color) {
        for (int i = 0; i < SettingsMenu.PRESET_COLORS.length; i++) {
            if (SettingsMenu.PRESET_COLORS[i].equals(color)) return PLAYER_EMOJIS[i];
        }
        return PLAYER_EMOJIS[0];
    }

    /** Updates the player list display with current usernames and stroke counts. */
    public void updatePlayers(String[] players) {
        javafx.application.Platform.runLater(() -> {
            currentPlayers.clear();
            for (String p : players) {
                if (p != null && !p.isBlank()) currentPlayers.add(p);
            }

            playerList.getChildren().clear();
            for (int i = 0; i < currentPlayers.size(); i++) {
                String player = currentPlayers.get(i);
                HBox pRow = new HBox(8);

                Color serverColor = ClientPreferences.getLobbyColor(player);
                String emoji = serverColor != null
                        ? emojiForColor(serverColor)
                        : PLAYER_EMOJIS[i % PLAYER_EMOJIS.length];

                String displayName = player.equals(clientApi.getUsername())
                        ? player + " (you)" : player;
                Label playerLabel = new Label(emoji + " " + displayName);
                playerLabel.setStyle("-fx-text-fill: white;");

                javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                int strokes = strokesMap.getOrDefault(player, 0);
                Label strokeLabel = new Label(strokes + " strokes");
                strokeLabel.setStyle("-fx-text-fill: yellow;");
                
                // Only show stroke label if game is active (buttons hidden)
                strokeLabel.setVisible(!startBtn.isVisible());

                pRow.getChildren().addAll(playerLabel, spacer, strokeLabel);
                playerList.getChildren().add(pRow);
            }
            // Enable START only when exactly 4 players are in the lobby
            boolean canStart = true; // playerList.getChildren().size() == 4;
            startBtn.setDisable(!canStart);
            startBtn.setStyle(canStart ? "-fx-base: #27ae60;" : "-fx-opacity: 0.5;");
        });
    }
}
