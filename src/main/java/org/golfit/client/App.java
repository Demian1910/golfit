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

                        int localId = client.getClientApi().getId();
                        GameView gameView = new GameView(client.getMovementHandler(), localId);

                        Pane overlayPane = gameView.getRoot();
                        AimIndicator aimIndicator = new AimIndicator(overlayPane);
                        gameView.setAimIndicator(aimIndicator);

                        gameArea.getChildren().addAll(game.getRoot(), overlayPane);
                        client.getMovementHandler().setGamePane(overlayPane);

                        centerArea.getChildren().setAll(gameArea);

                        // --- Cheat code listener ---
                        final String CHEAT_CODE = "hesoyam";
                        final StringBuilder keyBuffer = new StringBuilder();

                        primaryStage.getScene().setOnKeyPressed(event -> {
                            keyBuffer.append(event.getText().toLowerCase());
                            if (keyBuffer.length() > CHEAT_CODE.length()) {
                                keyBuffer.delete(0, keyBuffer.length() - CHEAT_CODE.length());
                            }
                            if (keyBuffer.toString().equals(CHEAT_CODE)) {
                                keyBuffer.setLength(0);
                                try {
                                    client.getClientApi().sendHesoyam();
                                } catch (Exception e) {
                                    System.err.println("Cheat failed: " + e.getMessage());
                                }
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });

// — Listen for GAME_END: swap game map → splash, clear key listener —
            EventBus.get().on(Constants.GAME_END, data -> {
                Platform.runLater(() -> {
                    primaryStage.getScene().setOnKeyPressed(null);

                    SplashView newSplash = new SplashView();

                    // Parse payload: "winner=tosha;tosha:1,Groot:1"
                    String payload = (String) data;
                    String[] sections = payload.split(";", 2);
                    String winner = sections[0].replace("winner=", "").trim();

                    // Build results overlay shown on top of splash
                    VBox resultsBox = new VBox(10);
                    resultsBox.setAlignment(javafx.geometry.Pos.CENTER);
                    resultsBox.setStyle("""
                -fx-background-color: rgba(0,0,0,0.6);
                -fx-background-radius: 16;
                -fx-padding: 28 40 28 40;
                """);
                    resultsBox.setMaxWidth(400);
                    resultsBox.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

                    javafx.scene.text.Text winnerText = new javafx.scene.text.Text("🏆 " + winner + " wins!");
                    winnerText.setFont(javafx.scene.text.Font.font("Apple Chancery",
                            javafx.scene.text.FontWeight.BOLD, 36));
                    winnerText.setFill(javafx.scene.paint.Color.web("#fff176"));
                    resultsBox.getChildren().add(winnerText);

                    // Full results table from payload
                    if (sections.length > 1 && !sections[1].isBlank()) {
                        String[] entries = sections[1].split(",");
                        for (int i = 0; i < entries.length; i++) {
                            String[] parts = entries[i].trim().split(":");
                            if (parts.length < 2) continue;
                            String name = parts[0].trim();
                            String strokes = parts[1].trim();
                            String line = String.format("  %d.  %-16s %s strokes", i + 1, name, strokes);
                            javafx.scene.control.Label row = new javafx.scene.control.Label(line);
                            row.setFont(javafx.scene.text.Font.font("Courier New",
                                    javafx.scene.text.FontWeight.NORMAL, 16));
                            row.setTextFill(i == 0
                                    ? javafx.scene.paint.Color.web("#fff176")
                                    : javafx.scene.paint.Color.web("#e8f5e9"));
                            resultsBox.getChildren().add(row);
                        }
                    }

                    // Dismiss overlay on click
                    resultsBox.setOnMouseClicked(e -> centerArea.getChildren().remove(resultsBox));

                    javafx.scene.control.Label dismiss = new javafx.scene.control.Label("(click to dismiss)");
                    dismiss.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 12px;");
                    resultsBox.getChildren().add(dismiss);

                    centerArea.getChildren().setAll(newSplash, resultsBox);
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