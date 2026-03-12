package org.golfit.graphics;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

public class Course {

    private static final int COLS = 80;
    private static final int ROWS = 45;

    private Pane container;
    private Color backgroundColor = Color.rgb(121,213,33);

    public Course(Scene scene) {

        container = new Pane();

        // Background
        addRect(scene, 0, 0, COLS, ROWS, backgroundColor);

        // Borders
        addWall(scene, 0, 0, COLS, 1);          // top
        addWall(scene, 0, ROWS - 1, COLS, 1);   // bottom
        addWall(scene, 0, 0, 1, ROWS);          // left
        addWall(scene, COLS - 1, 0, 1, ROWS);   // right

        // Path 1 sand
        addSand(scene, 1, ROWS - 3, 20, 2);
        addSand(scene, 21, ROWS - 7, 25, 6);

        // Path 1 ice
        addIce(scene, 41, ROWS - 21, 10, 20);

        // Path 1 wall
        addWall(scene, 51, ROWS - 11, 4, 10);

        // Path 1.1 water
        addWater(scene, 55, ROWS - 9, 15, 8);

        // Path 1.1 ice
        addIce(scene, 55, ROWS - 11, 17, 10);

        // Path 1.2 ramp
        addPolygon(scene,
                72, ROWS - 1,
                72, ROWS - 4,
                79, ROWS - 11,
                79, ROWS - 1
        );

        // Path 1 ice-sand stripe
        addIce(scene, COLS - 12, ROWS - 20, 11, 2);
        addSand(scene, COLS - 12, ROWS - 22, 11, 2);
        addIce(scene, COLS - 12, ROWS - 24, 11, 2);
        addSand(scene, COLS - 12, ROWS - 26, 11, 2);
        addIce(scene, COLS - 12, ROWS - 28, 11, 2);
        addSand(scene, COLS - 12, ROWS - 30, 11, 2);
        addIce(scene, COLS - 12, ROWS - 32, 11, 2);
        addSand(scene, COLS - 12, ROWS - 34, 11, 2);



        // Golf wall

        addWall(scene, COLS-14, ROWS-34, 2,18);
        // Debug grid
        showGrid(scene);
    }

    /** ================= Helper Functions ================= **/

    private void addRect(Scene scene, int col, int row, int width, int height, Color color) {
        Rectangle rect = new Rectangle();
        rect.setFill(color);
        bindGrid(rect, scene, col, row, width, height);
        container.getChildren().add(rect);
    }

    private void addWall(Scene scene, int col, int row, int width, int height) {
        addRect(scene, col, row, width, height, Color.rgb(124,63,0));
    }

    private void addSand(Scene scene, int col, int row, int width, int height) {
        addRect(scene, col, row, width, height, Color.rgb(203,189,147));
    }

    private void addIce(Scene scene, int col, int row, int width, int height) {
        addRect(scene, col, row, width, height, Color.rgb(148,247,255));
    }

    private void addWater(Scene scene, int col, int row, int width, int height) {
        addRect(scene, col, row, width, height, Color.rgb(35,137,218));
    }

    private void addPolygon(Scene scene, int... coords) {
        Polygon poly = new Polygon();
        for (int i = 0; i < coords.length; i += 2) {
            int col = coords[i];
            int row = coords[i + 1];
            double x = col * scene.getWidth() / COLS;
            double y = row * scene.getHeight() / ROWS;
            poly.getPoints().addAll(x, y);
        }
        poly.setFill(Color.rgb(124,63,0)); // wall color
        container.getChildren().add(poly);
    }

    /** Bind a rectangle to the grid */
    private void bindGrid(Rectangle rect, Scene scene, int col, int row, int width, int height) {
        rect.xProperty().bind(scene.widthProperty().divide(COLS).multiply(col));
        rect.yProperty().bind(scene.heightProperty().divide(ROWS).multiply(row));
        rect.widthProperty().bind(scene.widthProperty().divide(COLS).multiply(width));
        rect.heightProperty().bind(scene.heightProperty().divide(ROWS).multiply(height));
    }

    public Pane getPane() {
        return container;
    }

    /** Draw grid lines for debugging */
    private void showGrid(Scene scene) {

        for (int c = 0; c <= COLS; c++) {
            Line vLine = new Line();
            vLine.startXProperty().bind(scene.widthProperty().divide(COLS).multiply(c));
            vLine.endXProperty().bind(scene.widthProperty().divide(COLS).multiply(c));
            vLine.setStartY(0);
            vLine.endYProperty().bind(scene.heightProperty());
            vLine.setStroke(Color.rgb(0,0,0,0.25));
            container.getChildren().add(vLine);
        }

        for (int r = 0; r <= ROWS; r++) {
            Line hLine = new Line();
            hLine.startYProperty().bind(scene.heightProperty().divide(ROWS).multiply(r));
            hLine.endYProperty().bind(scene.heightProperty().divide(ROWS).multiply(r));
            hLine.setStartX(0);
            hLine.endXProperty().bind(scene.widthProperty());
            hLine.setStroke(Color.rgb(0,0,0,0.25));
            container.getChildren().add(hLine);
        }
    }
}