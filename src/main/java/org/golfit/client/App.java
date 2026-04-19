package org.golfit.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.golfit.client.clientLogic.Client;
import org.golfit.client.game.AimIndicator;
import org.golfit.client.game.GameView;
import org.golfit.client.gui.Sidebar;
import org.golfit.client.gui.SplashView;
import org.golfit.client.eventBus.EventBus;
import org.golfit.graphics.Game;
import org.golfit.shared.constants.Constants;

import java.util.List;

/**
 * JavaFX application entry point for the GolfIt client.
 * Sets up the primary stage with a splash screen, sidebar, and game view,
 * and manages transitions between lobby and game states.
 */
public class App extends Application {
    private Client client;
    private Stage primaryStage;

    /** The center content area — starts as SplashView, replaced by the game on GAME_START. */
    private StackPane centerArea;

    /** Initializes the JavaFX stage, parses connection parameters, and launches the game. */
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

    /**
     * Creates the client, connects to the server, builds the UI layout,
     * and registers EventBus listeners for game state transitions.
     */
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

                        // GameView now receives the movement handler and local player ID
                        // so the drag handler can look up the ball's live pixel position.
                        int localId = client.getClientApi().getId();
                        GameView gameView = new GameView(
                                client.getMovementHandler(), localId);

                        // The game pane (gameView.getRoot()) is a transparent overlay.
                        // AimIndicator adds its polygons to that pane at z-index 0,
                        // so ball circles (added later by MovementHandler) render on top.
                        Pane overlayPane = gameView.getRoot();
                        AimIndicator aimIndicator = new AimIndicator(overlayPane);
                        gameView.setAimIndicator(aimIndicator);

                        gameArea.getChildren().addAll(game.getRoot(), overlayPane);
                        client.getMovementHandler().setGamePane(overlayPane);

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

    /** Shuts down the client connection and exits the application. */
    @Override
    public void stop() throws Exception {
        if (client != null) {
            client.getClientApi().shutdown();
        }
        System.exit(0);
    }
}