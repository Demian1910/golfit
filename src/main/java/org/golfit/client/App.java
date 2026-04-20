package org.golfit.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
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
 * and manages transitions between lobby and game states with responsive scaling.
 */
public class App extends Application {
    private Client client;
    private Stage primaryStage;

    /** The center content area — starts as SplashView, replaced by the scaled game on GAME_START. */
    private StackPane centerArea;

    @Override
    public void start(Stage stage) {
        List<String> params = getParameters().getRaw();
        if (params.size() < 3) {
            System.err.println("Usage: App <host> <port> <username>");
            System.exit(1);
        }

        String host = params.get(0);
        int port = Integer.parseInt(params.get(1));
        String username = params.get(2);

        this.primaryStage = stage;
        stage.setTitle("GolfIt - Sultans of Swing");

        // Ensure the stage is resizable
        stage.setResizable(true);

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

            // Initial view
            SplashView splash = new SplashView();
            centerArea = new StackPane(splash);

            Sidebar sidebar = new Sidebar(client.getClientApi());

            HBox root = new HBox(centerArea, sidebar);
            HBox.setHgrow(centerArea, Priority.ALWAYS);

            // Default starting size
            primaryStage.setScene(new Scene(root, 1350, 600));

            client.getClientApi().connect(username);
            client.pingService.start();

            // --- Listen for GAME_START: swap splash -> SCALED game map ---
            EventBus.get().on(Constants.GAME_START, data -> {
                Platform.runLater(() -> {
                    try {

                        StackPane gameArea = new StackPane();
                        gameArea.setPrefSize(1100, 600);
                        gameArea.setMinSize(1100, 600);
                        gameArea.setMaxSize(1100, 600);
                        gameArea.setClip(new javafx.scene.shape.Rectangle(1100, 600));

                        Game game = new Game(gameArea);
                        int localId = client.getClientApi().getId();

                        GameView gameView = new GameView(client.getMovementHandler(), localId);
                        Pane overlayPane = gameView.getRoot();

                        AimIndicator aimIndicator = new AimIndicator(overlayPane);
                        gameView.setAimIndicator(aimIndicator);

                        gameArea.getChildren().addAll(game.getRoot(), overlayPane);
                        client.getMovementHandler().setGamePane(overlayPane);

                        // ScalingGroup
                        Group scalingGroup = new Group(gameArea);

                        // Group in new Stackpane for reizing
                        StackPane scalingContainer = new StackPane(scalingGroup);
                        scalingContainer.setStyle("-fx-background-color: #1a1a1a;");

                        // Auto resizing
                        scalingContainer.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
                            double width = newBounds.getWidth();
                            double height = newBounds.getHeight();

                            // Maintain aspect ratio
                            double scale = Math.min(width / 1100.0, height / 600.0);

                            scalingGroup.setScaleX(scale);
                            scalingGroup.setScaleY(scale);
                        });

                        // Swap content
                        centerArea.getChildren().setAll(scalingContainer);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });

            // --- Listen for GAME_END: swap game map -> splash ---
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