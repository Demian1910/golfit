package org.golfit.client.game;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

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

        BallController controller = new BallController(canvas, 400, 300, Color.BLUE);

        Scene scene = new Scene(canvas, 800, 600);
        primaryStage.setTitle("Ball Physics Test — click & drag to shoot");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            controller.stop();
            Platform.exit();
        });
        primaryStage.show();
        primaryStage.toFront();
        primaryStage.requestFocus();

        controller.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
