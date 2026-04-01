package org.golfit.client.gui.lobby;

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
    private final Button leaveLobbyBtn;

    public LobbyMenu(ClientApi clientApi) {
        this.clientApi = clientApi;

        EventBus.get().on(Constants.LOBBY_LIST, data -> {
            String[] lobbyList = (String[]) data;

            javafx.application.Platform.runLater(() -> {
                lobbyVBox.getChildren().clear(); // clear old rows first
                for (String lobbyString : lobbyList) {
                    String[] lobby = lobbyString.split(":");
                    createLobbyRow(lobby);
                }
            });
        });

        EventBus.get().on(Constants.LOBBY_JOINED, data -> setInLobby(true));
        EventBus.get().on(Constants.LOBBY_LEFT, data -> setInLobby(false));

        this.setSpacing(8);
        this.setPadding(new Insets(8));
        Label title = new Label("Lobbies");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Create Lobby button
        Button createLobbyBtn = new Button("Create Lobby");
        createLobbyBtn.setMaxWidth(Double.MAX_VALUE);
        createLobbyBtn.setOnAction(e -> {
            if (clientApi.getLobbyId() > 0) return;
            try {
                clientApi.createLobby();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        // Leave Lobby button — only enabled when in a lobby
        leaveLobbyBtn = new Button("Leave Lobby");
        leaveLobbyBtn.setMaxWidth(Double.MAX_VALUE);
        leaveLobbyBtn.setStyle("-fx-opacity: 0.6;");
        leaveLobbyBtn.setDisable(true);
        leaveLobbyBtn.setOnAction(e -> {
            try {
                clientApi.leaveLobby(String.valueOf(clientApi.getLobbyId()));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        HBox lobbyButtons = new HBox(6, createLobbyBtn, leaveLobbyBtn);
        HBox.setHgrow(createLobbyBtn, Priority.ALWAYS);
        HBox.setHgrow(leaveLobbyBtn, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(lobbyVBox);
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        this.getChildren().addAll(title, lobbyButtons, scroll);
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

        Label nameLabel   = new Label(lobby[1]);
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
}
