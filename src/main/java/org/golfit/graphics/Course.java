package org.golfit.graphics;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.Scene;

public class Course {
    Color background_color = Color.rgb(121,213,33);
    private Pane container;

    public Course(Scene scene) {
        container = new Pane();
        // Create background

        Rectangle background = new Rectangle();
        background.setFill(background_color);

        // Bind background
        bindRect(background,scene,0,0,1,1);
        // Create walls
        Rectangle topBorder = createWall();
        Rectangle bottomBorder = createWall();
        Rectangle leftBorder = createWall();
        Rectangle rightBorder = createWall();

        // Bind walls to scene size
        bindRect(topBorder, scene, 0, 0, 1, 1.0/90);
        bindRect(bottomBorder, scene, 0, 1-1.0/90, 1, 1.0/90);
        bindRect(leftBorder, scene, 0, 0, 1.0/160, 1);
        bindRect(rightBorder, scene, 1-1.0/160, 0, 1.0/160, 1);

        container.getChildren().addAll(background, topBorder, bottomBorder, leftBorder, rightBorder);
    }

    public Pane getPane() {
        return container;
    }

    private Rectangle createWall() {
        Rectangle rect = new Rectangle();
        rect.setFill(Color.rgb(124,63,0));
        return rect;
    }

    private void bindRect(Rectangle rect, Scene scene, double xRatio, double yRatio, double widthRatio, double heightRatio) {
        rect.xProperty().bind(scene.widthProperty().multiply(xRatio));
        rect.yProperty().bind(scene.heightProperty().multiply(yRatio));
        rect.widthProperty().bind(scene.widthProperty().multiply(widthRatio));
        rect.heightProperty().bind(scene.heightProperty().multiply(heightRatio));
    }
}