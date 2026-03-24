package org.golfit.graphics;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class Window extends Application {

    // Initial window size
    public static final int INIT_WIDTH = 1280;
    public static final int INIT_HEIGHT = 720;

    /**
     * Creates a new window and initializes main pane
     *
     * @param stage the javaFX stage
     */
    @Override
    public void start(Stage stage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, INIT_WIDTH, INIT_HEIGHT);
        Game game = new Game(scene);
        root.getChildren().add((game.getRoot()));


        // Show stage
        stage.setTitle("Golfit");
        stage.setScene(scene);
        stage.show();
    }


}