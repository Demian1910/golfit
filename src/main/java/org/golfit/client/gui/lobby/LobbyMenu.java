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
import org.golfit.shared.constants.Constants;


public class LobbyMenu extends VBox {
    ClientApi clientApi;
    VBox lobbyVBox = new VBox(5);
    private VBox playerList;
    private Button leaveLobbyBtn;

    private final VBox lobbyListView;
    private final VBox inLobbyView;

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
                    if (lobbyString == null || lobbyString.isBlank()) continue;
                    String[] lobby = lobbyString.split(":");
                    if (lobby.length < 4) continue;
                    createLobbyRow(lobby);
                }
            });
        });
        EventBus.get().on(Constants.LOBBY_INFO, data -> {
            String[] players = (String[]) data;
            updatePlayers(players);
        });
        EventBus.get().on(Constants.LOBBY_JOINED, data -> showInLobbyView());
        EventBus.get().on(Constants.LOBBY_LEFT,   data -> showLobbyListView());
    }

    public void setInLobby(boolean inLobby) {
        javafx.application.Platform.runLater(() -> {
            leaveLobbyBtn.setDisable(!inLobby);
            leaveLobbyBtn.setStyle(inLobby ? "" : "-fx-opacity: 0.6;");
        });
    }

    public HBox createLobbyRow(String[] lobby) {
        HBox row = new HBox(6);
        row.setPadding(new Insets(4));
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label nameLabel   = new Label(lobby[1] + "'s lobby");
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
            if (clientApi.getLobbyId() > 0) return;
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

    private VBox buildLobbyListView() {
        Label title = new Label("Lobbies");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Button createLobbyBtn = new Button("Create Lobby");
        createLobbyBtn.setMaxWidth(Double.MAX_VALUE);
        createLobbyBtn.setOnAction(e -> {
            if (clientApi.getLobbyId() > 0) return;
            try { clientApi.createLobby(); }
            catch (Exception ex) { throw new RuntimeException(ex); }
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

    private VBox buildInLobbyView() {
        Label title = new Label("Current Lobby");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");


        Label playersLabel = new Label("Players:");
        playersLabel.setStyle("-fx-text-fill: white;");
        playerList = new VBox(4);

        leaveLobbyBtn = new Button("Leave Lobby");
        leaveLobbyBtn.setMaxWidth(Double.MAX_VALUE);
        leaveLobbyBtn.setOnAction(e -> {
            try { clientApi.leaveLobby(String.valueOf(clientApi.getLobbyId())); }
            catch (Exception ex) { throw new RuntimeException(ex); }
        });

        VBox view = new VBox(8, title, playersLabel, playerList, leaveLobbyBtn);
        VBox.setVgrow(view, Priority.ALWAYS);
        return view;
    }

    private void showInLobbyView() {
        javafx.application.Platform.runLater(() -> {
            lobbyListView.setVisible(false);
            lobbyListView.setManaged(false);
            inLobbyView.setVisible(true);
            inLobbyView.setManaged(true);
        });
    }

    private void showLobbyListView() {
        javafx.application.Platform.runLater(() -> {
            inLobbyView.setVisible(false);
            inLobbyView.setManaged(false);
            lobbyListView.setVisible(true);
            lobbyListView.setManaged(true);
        });
    }

    public void updatePlayers(String[] players) {
        javafx.application.Platform.runLater(() -> {
            playerList.getChildren().clear();
            for (String player : players) {
                if (player == null || player.isBlank()) continue;
                Label playerLabel = new Label("👤 " + player);
                playerLabel.setStyle("-fx-text-fill: white;");
                playerList.getChildren().add(playerLabel);
            }
        });
    }
}
