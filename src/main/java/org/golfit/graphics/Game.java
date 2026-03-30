package org.golfit.graphics;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import org.golfit.client.game.Ball;
import org.golfit.client.game.BallRender;

public class Game {

    private Pane root;
    private Course course;
    private Ball ball;
    private BallRender ballRender;;

    /**
     * Initiates the main pane for the game
     *
     * @param scene the JavaFX scene
     */
    public Game(Pane scene) {
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
    public Course getCourse(){return course;}
}