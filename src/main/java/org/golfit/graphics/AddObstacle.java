package org.golfit.graphics;

import javafx.beans.binding.DoubleBinding;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating and placing grid-aligned obstacle shapes on the course pane.
 * All positions and sizes are specified in grid units (80 columns x 45 rows) and
 * bound to the container's dimensions so they scale automatically.
 */
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


    public static List<Shape> obstacles = new ArrayList<>();
    private final Pane container;

    /**
     * Creates an obstacle factory attached to the given container pane.
     *
     * @param container the pane to add shapes to
     */
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

    /** Adds a colored rectangle at the given grid position and returns it. */
    public Rectangle addRect(int col, int row, int width, int height, Color color) {
        Rectangle rect = new Rectangle();
        rect.setFill(color);
        // Bind only to the container, NOT the scene
        bindGrid(rect, col, row, width, height);
        setupShape(rect);
        return rect;
    }

    /** Adds a wall obstacle (brown rectangle) at the given grid position. */
    public void addWall(int col, int row, int width, int height) {
        Rectangle rect = addRect(col, row, width, height, Color.rgb(124, 63, 0));
        rect.setUserData(WALL);
        obstacles.add(rect);
    }

    /** Adds a sand terrain patch at the given grid position. */
    public void addSand(int col, int row, int width, int height) {
        Rectangle rect = addRect(col, row, width, height, Color.rgb(203, 189, 147));
        rect.setUserData(SAND);
        obstacles.add(rect);
    }

    /** Adds an ice terrain patch at the given grid position. */
    public void addIce(int col, int row, int width, int height) {
        Rectangle rect = addRect(col, row, width, height, Color.rgb(148, 247, 255));
        rect.setUserData(ICE);
        obstacles.add(rect);
    }

    /** Adds a water hazard at the given grid position. */
    public void addWater(int col, int row, int width, int height) {
        Rectangle rect = addRect(col, row, width, height, Color.rgb(35, 137, 218));
        rect.setUserData(WATER);
        obstacles.add(rect);
    }

    /** Adds the golf hole (black ellipse) at the given grid position. */
    public void addGolfHole(int col, int row) {
        // Golf holes are typically small, 1.5x1.5 or 2x2
        Ellipse ellipse = addEllipse(col, row, 2, 2, Color.BLACK);
        ellipse.setUserData(0); // Goal or specific ID
    }

    /** Adds a switch obstacle (gray base with red ellipse) at the given grid position. */
    public void addSwitch(int col, int row) {
        addRect(col, row, 2, 1, Color.GRAY);
        Ellipse ellipse = addEllipse(col, row, 2, 1, Color.RED);
        ellipse.setUserData(SWITCH);
        obstacles.add(ellipse);

    }

    /** Adds a bouncy ball obstacle (pink ellipse) at the given grid position. */
    public void addBall(int col, int row) {
        Ellipse ellipse = addEllipse(col, row, 3, 3, Color.rgb(255, 16, 240));
        ellipse.setUserData(BOUNCE);
        obstacles.add(ellipse);
    }

    /** Adds a colored ellipse at the given grid position and returns it. */
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

    /** Adds a wall polygon with the given grid-coordinate vertices. */
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

    /** Adds a tree (trunk rectangle + triangular canopy) at the given grid position. */
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

    /** Binds a rectangle's position and size to the container so it scales with the grid. */
    public void bindGrid(Rectangle rect, int col, int row, int width, int height) {
        rect.xProperty().bind(container.widthProperty().divide(COLS).multiply(col));
        rect.yProperty().bind(container.heightProperty().divide(ROWS).multiply(row));
        rect.widthProperty().bind(container.widthProperty().divide(COLS).multiply(width));
        rect.heightProperty().bind(container.heightProperty().divide(ROWS).multiply(height));
    }

    /** Returns all obstacle shapes added to this factory. */
    public List<Shape> getObstacleList() {
        return obstacles;
    }
    public static void hideObstacle(int id) {
        // Assuming your shapes are stored in a List where index = ID
        if (id >= 0 && id < obstacles.size()) {
            Shape shape = obstacles.get(id);
            if (shape != null) {
                shape.setVisible(false);
                // Optional: Disable its layout so it doesn't interfere with other elements
                shape.setManaged(false);
                System.out.println("[GUI] Obstacle " + id + " hidden.");
            }
        } else {
            System.out.println("[GUI] Warning: Received ID " + id + " out of bounds.");
        }
    }



}
