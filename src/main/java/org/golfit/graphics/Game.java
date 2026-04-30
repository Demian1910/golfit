package org.golfit.graphics;

import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import org.golfit.client.eventBus.EventBus;
import org.golfit.shared.constants.Constants;


/**
 * Manages the fixed-size 1100x600 game world and scales it to fit the available screen space.
 * Contains the {@link Course} and applies a dynamic {@link Scale} transform based on the parent pane size.
 */
public class Game {

    private Pane root; // This is the fixed 1100x600 logical world
    private Course course;

    private Scale scale = new Scale();

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

        // 3. Apply the scaling transformation
        root.getTransforms().add(scale);

        // 4. Listen for resizing (when the Sidebar takes up space)
        parentPane.widthProperty().addListener((obs, old, newVal) -> updateScale(parentPane));
        parentPane.heightProperty().addListener((obs, old, newVal) -> updateScale(parentPane));

        EventBus.get().on(Constants.OBSTACLE_UPDATE, data -> {
            try {
                // 1. Parse the ID (comes as a String from the network)
                int id;
                if (data instanceof Integer) {
                    id = (Integer) data;
                } else {
                    id = Integer.parseInt(data.toString());
                }

                course.hideObstacle(id);

                // 2. Update the UI (Must be wrapped in Platform.runLater)
                Platform.runLater(() -> {
                    // This function hides the shape from the screen

                });

            } catch (Exception e) {
                System.err.println("[GUI] Failed to process obstacle update: " + e.getMessage());
            }
        });
        // 5. Run the map export logic

        Platform.runLater(() -> {
            updateScale(parentPane);
            HelpGridMap helpGridMap = new HelpGridMap(course);
            helpGridMap.updateGrids();
            helpGridMap.verifyIdMapping();
            helpGridMap.printServerData();
        });




    }



    /**
     * Adjusts the visual "zoom" so the 1100px world fits into the available screen space.
     */
    private void updateScale(Pane parent) {
        double availableW = parent.getWidth();
        double availableH = parent.getHeight();

        if (availableW > 0 && availableH > 0) {

            scale.setX(availableW / 1100.0);
            scale.setY(availableH / 600.0);
        }
    }

    /** Returns the root pane containing the scaled game world. */
    public Pane getRoot() {
        return root;
    }

}