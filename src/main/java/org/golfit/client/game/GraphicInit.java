package org.golfit.client.game;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.golfit.client.Client;

/**
 * Standalone JavaFX test application for BallController.
 * Run with: ./gradlew runBallTest
 *
 * Click and drag on the canvas to shoot the ball.
 */
public class BallTestApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        GameView gameView = new GameView();
        Pane canvas = gameView.getRoot();

        java.util.List<String> args = getParameters().getRaw();
        String host = "localhost";
        int port = 1234;
        String username = "PlayerUI";

        if (args.size() > 0 && "client".equals(args.get(0))) {
            if (args.size() > 1) {
                String[] hostPort = args.get(1).split(":");
                host = hostPort[0];
                port = Integer.parseInt(hostPort[1]);
            }
            if (args.size() > 2) {
                username = args.get(2);
            }
        }

        // Start Client Networking in Background
        Client client = new Client(host, port, username);
        try {
            client.start();
            // We do NOT call client.startConsoleInput() to avoid console blocking

            // Link Pane to MovementHandler so it can draw balls
            client.getMovementHandler().setGamePane(canvas);

            // Connect to server
            client.clientApi.connect(username);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Controller just sends forces now, no local physics rendering needed
        BallController controller = new BallController(canvas, client.clientApi);

        Scene scene = new Scene(canvas, 800, 600);
        primaryStage.setTitle("GolfIt Client - click & drag to shoot");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            try {
                client.clientApi.disconnect();
            } catch (Exception ex) {
            }
            Platform.exit();
        });
        primaryStage.show();
        primaryStage.toFront();
        primaryStage.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
