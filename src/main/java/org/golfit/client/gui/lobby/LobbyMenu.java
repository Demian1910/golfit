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

import java.util.Arrays;

public class LobbyMenu extends VBox {
    ClientApi clientApi;
    VBox lobbyVBox = new VBox(5);

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

        this.setSpacing(8);
        this.setPadding(new Insets(8));
        Label title = new Label("Lobbies");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        ScrollPane scroll = new ScrollPane(lobbyVBox);
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        this.getChildren().addAll(title, scroll);
    }

    public HBox createLobbyRow(String[] lobby) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(4));

        Label nameLabel   = new Label(lobby[1] + "'s lobby");
        Label playerCount = new Label(lobby[2] + "/4");
        Label status = new Label(lobby[3]);
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
        Button joinBtn    = new Button("Join");

        joinBtn.setOnAction(e -> {
            try {
                clientApi.joinLobby(lobby[0]);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        row.getChildren().addAll(nameLabel, playerCount,status, joinBtn);
        lobbyVBox.getChildren().add(row);
        return row;
    }
}
