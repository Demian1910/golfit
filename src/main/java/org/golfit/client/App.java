package org.golfit.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.golfit.client.clientLogic.Client;
import org.golfit.client.eventBus.EventBus;
import org.golfit.client.sound.SoundManager;
import org.golfit.client.game.AimIndicator;
import org.golfit.client.game.GameView;
import org.golfit.client.game.GamepadInput;
import org.golfit.client.gui.Sidebar;
import org.golfit.client.gui.SplashView;
import org.golfit.graphics.Game;
import org.golfit.shared.constants.Constants;

import java.util.List;

/**
 * JavaFX application entry point for the GolfIt client.
 * Handles splash screen, game view, scaling, cheat codes, and end-game results.
 */
public class App extends Application {

    private Client client;
    private Stage primaryStage;
    private GamepadInput gamepadInput;

    /** Center content (Splash → Game → Splash) */
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

            // --- Initial Splash ---
            SplashView splash = new SplashView();
            centerArea = new StackPane(splash);

            Sidebar sidebar = new Sidebar(client.getClientApi());

            HBox root = new HBox(centerArea, sidebar);
            HBox.setHgrow(centerArea, Priority.ALWAYS);

            primaryStage.setScene(new Scene(root, 1350, 600));

            // Connect AFTER UI setup
            client.getClientApi().connect(username);
            client.pingService.start();

            // =========================
            // GAME START
            // =========================
            EventBus.get().on(Constants.GAME_START, data -> {
                SoundManager.playGameStart();
                Platform.runLater(() -> {
                    try {
                        // Fixed internal resolution
                        StackPane gameArea = new StackPane();
                        gameArea.setPrefSize(1100, 600);
                        gameArea.setMinSize(1100, 600);
                        gameArea.setMaxSize(1100, 600);
                        gameArea.setClip(new javafx.scene.shape.Rectangle(1100, 600));

                        Game game = new Game(gameArea);
                        int localId = client.getClientApi().getId();

                        GameView gameView = new GameView(client.getMovementHandler(), localId);
                        try {
                            gamepadInput = new GamepadInput(gameView);
                            gamepadInput.start();
                        } catch (Throwable t) {
                            System.err.println("Gamepad unavailable, continuing with mouse only: " + t.getMessage());
                            gamepadInput = null;
                        }
                        Pane overlayPane = gameView.getRoot();

                        AimIndicator aimIndicator = new AimIndicator(overlayPane);
                        gameView.setAimIndicator(aimIndicator);

                        gameArea.getChildren().addAll(game.getRoot(), overlayPane);
                        client.getMovementHandler().setGamePane(overlayPane);

                        // --- Scaling system ---
                        Group scalingGroup = new Group(gameArea);
                        StackPane scalingContainer = new StackPane(scalingGroup);
                        scalingContainer.setStyle("-fx-background-color: #1a1a1a;");

                        scalingContainer.layoutBoundsProperty().addListener((obs, oldB, newB) -> {
                            double scale = Math.min(
                                    newB.getWidth() / 1100.0,
                                    newB.getHeight() / 600.0
                            );
                            scalingGroup.setScaleX(scale);
                            scalingGroup.setScaleY(scale);
                        });

                        centerArea.getChildren().setAll(scalingContainer);

                        // --- Cheat code ---
                        final String CHEAT_CODE = "hesoyam";
                        final StringBuilder keyBuffer = new StringBuilder();

                        primaryStage.getScene().setOnKeyPressed(event -> {
                            keyBuffer.append(event.getText().toLowerCase());

                            if (keyBuffer.length() > CHEAT_CODE.length()) {
                                keyBuffer.delete(0,
                                        keyBuffer.length() - CHEAT_CODE.length());
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

            // =========================
            // GAME END
            // =========================
            EventBus.get().on(Constants.GAME_END, data -> {
                SoundManager.playGameEnd();
                Platform.runLater(() -> {

                    // remove cheat listener
                    primaryStage.getScene().setOnKeyPressed(null);

                    SplashView splashView = new SplashView();

                    String payload = (String) data;
                    String[] sections = payload.split(";", 2);
                    String winner = sections[0].replace("winner=", "").trim();

                    VBox resultsBox = new VBox(10);
                    resultsBox.setAlignment(javafx.geometry.Pos.CENTER);
                    resultsBox.setStyle("""
                        -fx-background-color: rgba(0,0,0,0.6);
                        -fx-background-radius: 16;
                        -fx-padding: 28 40 28 40;
                    """);

                    javafx.scene.text.Text winnerText =
                            new javafx.scene.text.Text("🏆 " + winner + " wins!");
                    winnerText.setFont(javafx.scene.text.Font.font("Apple Chancery",
                            javafx.scene.text.FontWeight.BOLD, 36));
                    winnerText.setFill(javafx.scene.paint.Color.web("#fff176"));

                    resultsBox.getChildren().add(winnerText);

                    // Scoreboard
                    if (sections.length > 1 && !sections[1].isBlank()) {
                        String[] entries = sections[1].split(",");

                        for (int i = 0; i < entries.length; i++) {
                            String[] parts = entries[i].trim().split(":");
                            if (parts.length < 2) continue;

                            String name = parts[0];
                            String strokes = parts[1];

                            javafx.scene.control.Label row =
                                    new javafx.scene.control.Label(
                                            String.format("%d. %-16s %s strokes",
                                                    i + 1, name, strokes));

                            row.setTextFill(i == 0
                                    ? javafx.scene.paint.Color.web("#fff176")
                                    : javafx.scene.paint.Color.web("#e8f5e9"));

                            resultsBox.getChildren().add(row);
                        }
                    }

                    // Click to dismiss overlay
                    resultsBox.setOnMouseClicked(e ->
                            centerArea.getChildren().remove(resultsBox));

                    if (gamepadInput != null) {
                        gamepadInput.shutdown();
                        gamepadInput = null;
                    }
                    centerArea.getChildren().setAll(splashView, resultsBox);
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
        if (gamepadInput != null) {
            gamepadInput.shutdown();
        }
        System.exit(0);
    }
}