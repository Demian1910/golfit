package org.golfit.graphics;

import javafx.application.Platform;
import javafx.scene.layout.Pane;

/**
 * Manages the fixed-size 1100x600 game world.
 * The scaling is handled by the parent container in App.java.
 */
public class Game {

    private final Pane root;
    private final Course course;

    /**
     * @param parentPane The StackPane (gameArea) from App.java
     */
    public Game(Pane parentPane) {
        // 1. Create a fixed-size internal Pane (The Server's World)
        root = new Pane();
        root.setPrefSize(1100, 600);
        root.setMinSize(1100, 600);
        root.setMaxSize(1100, 600);

        // 2. Initialize the course inside this fixed pane
        course = new Course();
        root.getChildren().add(course.getPane());

        /*3. Run the map export logic
        Platform.runLater(() -> {
            HelpGridMap helpGridMap = new HelpGridMap(course);
            helpGridMap.updateGrids();
            helpGridMap.verifyIdMapping();
            helpGridMap.printServerData();
        });

         */
    }

    /** Returns the root pane containing the game world. */
    public Pane getRoot() {
        return root;
    }
}