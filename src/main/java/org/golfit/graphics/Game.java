package org.golfit.graphics;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class Game {

    private Pane root;
    private Course course;

    public Game(Scene scene) {
        root = new Pane();
        course = new Course(scene);
        root.getChildren().add(course.getPane());
    }

    public Pane getRoot() {
        return root;
    }
}