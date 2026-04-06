package org.golfit.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.golfit.client.clientLogic.Client;
import org.golfit.client.game.GameView;
import org.golfit.client.gui.Sidebar;
import org.golfit.client.gui.SplashView;
import org.golfit.client.eventBus.EventBus;
import org.golfit.graphics.Game;
import org.golfit.shared.constants.Constants;

import java.util.List;

public class App extends Application {
    private Client client;
    private Stage primaryStage;

    /** The center content area — starts as SplashView, replaced by the game on GAME_START. */
    private StackPane centerArea;

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

            // — Splash screen shown initially —
            SplashView splash = new SplashView();
            centerArea = new StackPane(splash);

            Sidebar sidebar = new Sidebar(client.getClientApi());

            HBox root = new HBox(centerArea, sidebar);
            HBox.setHgrow(centerArea, Priority.ALWAYS);

            primaryStage.setScene(new Scene(root, 1350, 600));

            // Connect AFTER scene/sidebar are fully set up so all EventBus listeners are registered
            client.getClientApi().connect(username);
            client.pingService.start();

            // — Listen for GAME_START: swap splash → game map —
            EventBus.get().on(Constants.GAME_START, data -> {
                Platform.runLater(() -> {
                    try {
                        StackPane gameArea = new StackPane();
                        Game game = new Game(gameArea);
                        GameView gameView = new GameView();
                        gameArea.getChildren().addAll(game.getRoot(), gameView.getRoot());
                        client.getMovementHandler().setGamePane(gameView.getRoot());

                        centerArea.getChildren().setAll(gameArea);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });

            // — Listen for GAME_END: swap game map → splash —
            EventBus.get().on(Constants.GAME_END, data -> {
                Platform.runLater(() -> {
                    SplashView newSplash = new SplashView();
                    centerArea.getChildren().setAll(newSplash);
                });
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        if (client != null) {
            client.getClientApi().shutdown();
        }
        System.exit(0);
    }
}