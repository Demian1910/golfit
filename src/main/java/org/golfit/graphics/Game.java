package org.golfit.graphics;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class Game {

    private Pane root;
    private Course course;

    /**
     * Initiates the main pane for the game
     *
     * @param scene the JavaFX scene
     */
    public Game(Scene scene) {
        root = new Pane();
        course = new Course(scene);
        root.getChildren().add(course.getPane());
    }

    /**
     * returns the main pane
     */
    public Pane getRoot() {
        return root;
    }
}