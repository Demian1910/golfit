package org.golfit.graphics;

import javafx.beans.binding.DoubleBinding;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.List;

public class AddObstacle {
    private static final int COLS = 80;
    private static final int ROWS = 45;

    // Terrain Constants
    private static final int WALL = 1;
    private static final int SAND = 2;
    private static final int ICE = 3;
    private static final int WATER = 4;
    private static final int BOUNCE = 5;
    private static final int SWITCH = 6;


    public List<Shape> obstacles = new ArrayList<>();
    private final Pane container;

    public AddObstacle(Pane container) {
        this.container = container;
    }

    /**
     * Internal setup to ensure shapes don't mess with layout logic
     */
    private void setupShape(Shape s) {
        s.setManaged(false);
        container.getChildren().add(s);
    }

    /**
     * ================= Helper Functions =================
     **/

    public Rectangle addRect(int col, int row, int width, int height, Color color) {
        Rectangle rect = new Rectangle();
        rect.setFill(color);
        // Bind only to the container, NOT the scene
        bindGrid(rect, col, row, width, height);
        setupShape(rect);
        return rect;
    }

    public void addWall(int col, int row, int width, int height) {
        Rectangle rect = addRect(col, row, width, height, Color.rgb(124, 63, 0));
        rect.setUserData(WALL);
        obstacles.add(rect);
    }

    public void addSand(int col, int row, int width, int height) {
        Rectangle rect = addRect(col, row, width, height, Color.rgb(203, 189, 147));
        rect.setUserData(SAND);
        obstacles.add(rect);
    }

    public void addIce(int col, int row, int width, int height) {
        Rectangle rect = addRect(col, row, width, height, Color.rgb(148, 247, 255));
        rect.setUserData(ICE);
        obstacles.add(rect);
    }

    public void addWater(int col, int row, int width, int height) {
        Rectangle rect = addRect(col, row, width, height, Color.rgb(35, 137, 218));
        rect.setUserData(WATER);
        obstacles.add(rect);
    }

    public void addGolfHole(int col, int row) {
        // Golf holes are typically small, 1.5x1.5 or 2x2
        Ellipse ellipse = addEllipse(col, row, 2, 2, Color.BLACK);
        ellipse.setUserData(0); // Goal or specific ID
    }

    public void addSwitch(int col, int row) {
        addRect(col, row, 2, 1, Color.GRAY);
        Ellipse ellipse = addEllipse(col, row, 2, 1, Color.RED);
        ellipse.setUserData(SWITCH);
        obstacles.add(ellipse);

    }

    public void addBall(int col, int row) {
        Ellipse ellipse = addEllipse(col, row, 3, 3, Color.rgb(255, 16, 240));
        ellipse.setUserData(BOUNCE);
        obstacles.add(ellipse);
    }

    public Ellipse addEllipse(int col, int row, double width, double height, Color color) {
        Ellipse ellipse = new Ellipse();
        ellipse.setFill(color);

        // Center X = (col + width/2) * unitWidth
        ellipse.centerXProperty().bind(container.widthProperty().divide(COLS).multiply(col + width / 2.0));
        ellipse.centerYProperty().bind(container.heightProperty().divide(ROWS).multiply(row + height / 2.0));
        ellipse.radiusXProperty().bind(container.widthProperty().divide(COLS).multiply(width / 2.0));
        ellipse.radiusYProperty().bind(container.heightProperty().divide(ROWS).multiply(height / 2.0));

        setupShape(ellipse);
        return ellipse;
    }

    public void addPolygon(double... coords) {
        Polygon poly = new Polygon();
        DoubleBinding unitW = container.widthProperty().divide((double) COLS);
        DoubleBinding unitH = container.heightProperty().divide((double) ROWS);

        Runnable update = () -> {
            double w = unitW.get();
            double h = unitH.get();
            if (w == 0 || h == 0) return;
            List<Double> newPoints = new ArrayList<>();
            for (int i = 0; i < coords.length; i += 2) {
                newPoints.add(coords[i] * w);
                newPoints.add(coords[i + 1] * h);
            }
            poly.getPoints().setAll(newPoints);
        };

        container.widthProperty().addListener((obs, ov, nv) -> update.run());
        container.heightProperty().addListener((obs, ov, nv) -> update.run());

        poly.setFill(Color.rgb(124, 63, 0));
        poly.setUserData(WALL);
        setupShape(poly);
        obstacles.add(poly);
        // Initial call
        javafx.application.Platform.runLater(update);
    }

    public void addTree(int col, int row) {
        Rectangle trunk = new Rectangle();
        trunk.setFill(Color.rgb(101, 67, 33));
        trunk.setUserData(WALL);

        Polygon canopy = new Polygon();
        canopy.setFill(Color.rgb(34, 139, 34));
        canopy.setUserData(WALL);

        DoubleBinding unitW = container.widthProperty().divide((double) COLS);
        DoubleBinding unitH = container.heightProperty().divide((double) ROWS);

        Runnable updateTree = () -> {
            double w = unitW.get();
            double h = unitH.get();
            if (w == 0 || h == 0) return;

            trunk.setX(col * w);
            trunk.setY(row * h);
            trunk.setWidth(w);
            trunk.setHeight(h);

            // Position crown (exactly the same 'w' and 'h' values)
            canopy.getPoints().setAll(
                    (col - 1.0) * w, (double)row * h,          // Bottom left
                    (col + 2.0) * w, (double)row * h,          // Bottom right
                    (col + 0.5) * w, (double)(row - 2) * h     // Top center tip
            );
        };

        // Set listeners on the container
        container.widthProperty().addListener((obs, oldVal, newVal) -> updateTree.run());
        container.heightProperty().addListener((obs, oldVal, newVal) -> updateTree.run());

        setupShape(trunk);
        setupShape(canopy);
        obstacles.add(trunk);
        obstacles.add(canopy);

        // Execute immediately
        javafx.application.Platform.runLater(updateTree);
    }

    public void bindGrid(Rectangle rect, int col, int row, int width, int height) {
        rect.xProperty().bind(container.widthProperty().divide(COLS).multiply(col));
        rect.yProperty().bind(container.heightProperty().divide(ROWS).multiply(row));
        rect.widthProperty().bind(container.widthProperty().divide(COLS).multiply(width));
        rect.heightProperty().bind(container.heightProperty().divide(ROWS).multiply(height));
    }

    public List<Shape> getObstacleList() {
        return obstacles;
    }
}
