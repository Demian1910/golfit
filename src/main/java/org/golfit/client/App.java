package org.golfit.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.golfit.client.clientLogic.Client;
import org.golfit.client.game.GameView;
import org.golfit.graphics.Game;

import java.util.List;

public class App extends Application {
    private Client client;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        List<String> params = getParameters().getRaw();
        String host = params.get(0);
        int port = Integer.parseInt(params.get(1));
        String username = params.get(2);

        this.primaryStage = stage;
        stage.setTitle("GolfIt - Sultans of Swing");
        stage.setOnCloseRequest(e -> {
            try { stop(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        launchGame(host, port, username);
        stage.show();
        stage.toFront();
        stage.requestFocus();
    }


    private void launchGame(String ip, int port, String username) {
        try {
            client = new Client(ip, port, username);
            org.golfit.Main.currentClient = client;
            client.start();

            StackPane gameArea = new StackPane();
            Game game = new Game(gameArea);
            GameView gameView = new GameView();
            gameArea.getChildren().addAll(game.getRoot(), gameView.getRoot());

            client.getMovementHandler().setGamePane(gameView.getRoot());
            
            // Connect to server only AFTER pane is ready to avoid missing initial spawn packets
            client.getClientApi().connect(username);
            client.pingService.start();

            VBox sidebar = new VBox(10);
            sidebar.setPrefWidth(250);
            sidebar.setPadding(new Insets(10));
            sidebar.getChildren().addAll(
                    new Label("Lobby"),
                    new ListView<String>(),
                    new Label("Chat"),
                    new ListView<String>(),
                    new TextField(),
                    new Button("Send")
            );

            HBox root = new HBox(gameArea, sidebar);
            HBox.setHgrow(gameArea, Priority.ALWAYS);

            primaryStage.setScene(new Scene(root, 1100, 600));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        if (client != null) {
            client.getClientApi().shutdown();
            Platform.exit();
        }
    }
}