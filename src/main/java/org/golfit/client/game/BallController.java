package org.golfit.client.game;

import javafx.scene.layout.Pane;
import org.golfit.client.game.physics.Ball;
import javafx.scene.paint.Color;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

//wires mouse events → physics → rendering loop
public class BallController extends Application {
    Ball ball;
    BallRender ballRender;
    Pane root;

    public BallController() {
        // default constructor for JavaFX
    }

    public BallController(Pane root, double Ix, double Iy, Color color) {
        ball = new Ball((int) Ix, (int) Iy);
        ballRender = new BallRender(root, ball, color);
    }

    @Override
    public void start(Stage primaryStage) {
        GameView gameView = new GameView();
        Pane canvas = gameView.getRoot();

        // Create a ball for testing
        BallController testController = new BallController(canvas, 100, 100, Color.BLUE);

        Scene scene = new Scene(canvas, 800, 600);
        primaryStage.setTitle("Ball Physics Test");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
