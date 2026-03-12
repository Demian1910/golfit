package org.golfit.graphics;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

public class Course {

    private static final int COLS = 80;   // total columns
    private static final int ROWS = 45;   // total rows

    private static final int COURSE_COLS = 65; // course uses left 70 columns
    private Pane container;
    private Color backgroundColor = Color.rgb(121,213,33);

    public Course(Scene scene) {

        container = new Pane();

        // Background
        addRect(scene, 0, 0, COURSE_COLS, ROWS, backgroundColor);

        // Borders
        addWall(scene, 0, 0, COURSE_COLS, 1);          // top
        addWall(scene, 0, ROWS - 1, COURSE_COLS, 1);   // bottom
        addWall(scene, 0, 0, 1, ROWS);                 // left
        addWall(scene, COURSE_COLS - 1, 0, 1, ROWS);   // right
        // Path 2 start
        addWall(scene, 1, 22, 3,2);
        addWall(scene, 12, 22, 3,2);
        addSand(scene, 7, 22, 2,2);

        // Path 2 top left room
        addIce(scene, 1, 15, 3,7);
        addIce(scene, 12, 15, 8,7);
        addWall(scene,1,10,7,3);
        addIce(scene,8,1,12,12);
        // between ice
        addTree(scene, 5, 18);
        addTree(scene, 9, 18);
        // top left
        addTree(scene, 2, 4);
        addTree(scene, 6, 4);
        addTree(scene, 2, 8);
        addTree(scene, 6, 8);
        //in ice
        addTree(scene, 12, 7);
        addTree(scene, 16, 7);

        // Path 2 barrier wall
        addWall(scene,20,5,3,20);
        addPolygon(scene,
                1,15,
                1,13,
                4,13
        );

        // Path 2 room 2
        addWater(scene, 28, 5,10,10);
        addSand(scene, 28, 1,10,4);
        addIce(scene, 28, 15,10,4);
        addPolygon(scene,
                23,17,
                23, 19,
                25,19
        );
        // Golf area
        addIce(scene, 45, 1,12,6);
        addWater(scene,58,4, 4,3);
        addGolfHole(scene, 51, 9);
        addPolygon(scene,
                62, 1,
                64,1,
                64 ,3
        );
        addTree(scene,45,10);
        addTree(scene,42,7);
        addTree(scene,42,12);
        addTree(scene,41,17);
        addTree(scene,46,15);


        // Path 1 ice
        addIce(scene, 30, ROWS - 21, 10, 20);

        // Path 1.3 water pool border
        addRect(scene, 15, ROWS - 26, 15, 19, Color.GREY );
        // Path 1.3 water pool
        addWater(scene, 16, ROWS - 25, 13, 17);



        // Path 1
        addSand(scene, 1, ROWS - 4, 15, 3);
        addSand(scene, 16, ROWS - 7, 20, 6);
        addWall(scene, 40, ROWS - 11, 3, 10);

        // Path 1 lever-wall
        addWall(scene, 10, ROWS - 9, 26, 2);
        addWall(scene, 15, ROWS - 9, 2, 5);
        addSwitch(scene, 12, ROWS-6);

        // Path 1 lever-wall 2
        addWall(scene, 30, ROWS - 26,10, 2);
        addWall(scene, 30, ROWS - 26, 2, 5);
        addSwitch(scene, 33, ROWS-23);

        // Path 1.1 ice
        addIce(scene, 43, ROWS - 11, 14, 10);
        addWater(scene, 43, ROWS - 9, 12, 8);
        addPolygon(scene,
                57, ROWS - 1,
                57, ROWS - 4,
                64, ROWS - 11,
                64, ROWS - 1
        );
        // Path 1.2 balls
        addBall(scene, 40, ROWS-14);
        addBall(scene, 40, ROWS-17);
        addBall(scene, 40, ROWS-20);

        // Path 1 ice-sand stripe
        addIce(scene, COURSE_COLS - 10, ROWS - 20, 9, 2);
        addSand(scene, COURSE_COLS - 10, ROWS - 22, 9, 2);
        addIce(scene, COURSE_COLS - 10, ROWS - 24, 9, 2);
        addSand(scene, COURSE_COLS - 10, ROWS - 26, 9, 2);
        addIce(scene, COURSE_COLS - 10, ROWS - 28, 9, 2);
        addSand(scene, COURSE_COLS - 10, ROWS - 30, 9, 2);
        addIce(scene, COURSE_COLS - 10, ROWS - 32, 9, 2);
        addSand(scene, COURSE_COLS - 10, ROWS - 34, 9, 2);
        // Golf wall
        addWall(scene, COURSE_COLS - 10, ROWS - 34, 2, 18);
        addWall(scene, COURSE_COLS - 15, ROWS - 34, 5, 2);
        addWall(scene, COURSE_COLS - 17, ROWS - 38, 2, 6);



        // Path 1.2 ice
        addIce(scene, COURSE_COLS - 15, ROWS - 26, 5, 10 );
        addWall(scene, COURSE_COLS - 17, ROWS - 26, 2, 10 );
        addPolygon(scene,
                COURSE_COLS - 13, ROWS-32,
                COURSE_COLS - 10, ROWS-32,
                COURSE_COLS - 10, ROWS-29
        );


        // GUI Box on right side
        addRect(scene, 65, 0, 15, ROWS, Color.BLUE);
        // White boxes for text
        addRect(scene, 66, 1, 13, 2, Color.WHITE); // strokes
        addRect(scene, 66, 4, 13, 12, Color.WHITE); // leaderboard
        addRect(scene, 66, 17, 13, 20, Color.WHITE); // chat
        addRect(scene, 66, 38, 13, 2, Color.WHITE); // enter text
        addRect(scene, 66, 42, 10, 2, Color.WHITE); // time
        addRect(scene, 77, 42, 2, 2, Color.WHITE); // enter text
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
        container.getChildren().add(poly);

        // Bind each point dynamically
        for (int i = 0; i < coords.length; i += 2) {
            int col = coords[i];
            int row = coords[i + 1];

            final int index = i; // <-- make a final copy

            javafx.beans.property.DoubleProperty xProp = new javafx.beans.property.SimpleDoubleProperty();
            javafx.beans.property.DoubleProperty yProp = new javafx.beans.property.SimpleDoubleProperty();

            xProp.bind(scene.widthProperty().divide(COLS).multiply(col));
            yProp.bind(scene.heightProperty().divide(ROWS).multiply(row));

            poly.getPoints().addAll(xProp.get(), yProp.get());

            // Use 'index' inside the lambda
            xProp.addListener((obs, oldVal, newVal) -> poly.getPoints().set(index, newVal.doubleValue()));
            yProp.addListener((obs, oldVal, newVal) -> poly.getPoints().set(index + 1, newVal.doubleValue()));
        }

        poly.setFill(Color.rgb(124,63,0)); // wall color
    }

    private void addGolfHole(Scene scene, int col, int row){
        addEllipse(scene, col, row, 3, 1, Color.BLACK);
    }
    private void addSwitch(Scene scene, int col, int row){
        addRect(scene, col,row, 2,1, Color.GRAY);
        addEllipse(scene, col, row, 2, 1, Color.RED);
    }
    private void addEllipse(Scene scene, int col, int row, int widthInCols, int heightInRows, Color color) {
        Ellipse ellipse = new Ellipse();
        ellipse.setFill(color);

        ellipse.centerXProperty().bind(
                scene.widthProperty().divide(COLS).multiply(col + widthInCols / 2.0)
        );
        ellipse.centerYProperty().bind(
                scene.heightProperty().divide(ROWS).multiply(row + heightInRows / 2.0)
        );
        ellipse.radiusXProperty().bind(
                scene.widthProperty().divide(COLS).multiply(widthInCols / 2.0)
        );
        ellipse.radiusYProperty().bind(
                scene.heightProperty().divide(ROWS).multiply(heightInRows / 2.0)
        );

        container.getChildren().add(ellipse);
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
        for (int c = 0; c <= COURSE_COLS; c++) {
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

    private void addTree(Scene scene, int col, int row) {
        // Trunk
        int trunkWidth = 1;
        int trunkHeight = 1;


        addRect(scene, col, row, trunkWidth, trunkHeight, Color.rgb(124, 63, 0)); // brown trunk

        // Canopy as triangle (polygon)
        int canopyWidth = 3;
        int canopyHeight = 2;
        Polygon canopy = new Polygon();


        // Coordinates of triangle in grid coordinates
        double leftX = col - canopyWidth / 2.0 + trunkWidth / 2.0;
        double rightX = col + canopyWidth / 2.0 + trunkWidth / 2.0;
        double topX = col + trunkWidth / 2.0;

        int topRow = row - canopyHeight;

        // Bind polygon points to scene width/height
        javafx.beans.property.DoubleProperty leftXProp = new javafx.beans.property.SimpleDoubleProperty();
        javafx.beans.property.DoubleProperty rightXProp = new javafx.beans.property.SimpleDoubleProperty();
        javafx.beans.property.DoubleProperty topXProp = new javafx.beans.property.SimpleDoubleProperty();
        javafx.beans.property.DoubleProperty topYProp = new javafx.beans.property.SimpleDoubleProperty();
        javafx.beans.property.DoubleProperty bottomYProp = new javafx.beans.property.SimpleDoubleProperty();

        leftXProp.bind(scene.widthProperty().divide(COLS).multiply(leftX));
        rightXProp.bind(scene.widthProperty().divide(COLS).multiply(rightX));
        topXProp.bind(scene.widthProperty().divide(COLS).multiply(topX));
        topYProp.bind(scene.heightProperty().divide(ROWS).multiply(topRow));
        bottomYProp.bind(scene.heightProperty().divide(ROWS).multiply(row));

        canopy.getPoints().addAll(
                leftXProp.get(), bottomYProp.get(),
                rightXProp.get(), bottomYProp.get(),
                topXProp.get(), topYProp.get()
        );

        // Add listeners to scale polygon on resize
        leftXProp.addListener((obs, oldVal, newVal) -> canopy.getPoints().set(0, newVal.doubleValue()));
        bottomYProp.addListener((obs, oldVal, newVal) -> canopy.getPoints().set(1, newVal.doubleValue()));
        rightXProp.addListener((obs, oldVal, newVal) -> canopy.getPoints().set(2, newVal.doubleValue()));
        bottomYProp.addListener((obs, oldVal, newVal) -> canopy.getPoints().set(3, newVal.doubleValue()));
        topXProp.addListener((obs, oldVal, newVal) -> canopy.getPoints().set(4, newVal.doubleValue()));
        topYProp.addListener((obs, oldVal, newVal) -> canopy.getPoints().set(5, newVal.doubleValue()));

        canopy.setFill(Color.rgb(34,139,34)); // green color
        container.getChildren().add(canopy);
    }

    private void addBall(Scene scene, int col, int row){
        addEllipse(scene, col, row,3,3, Color.rgb(255, 16, 240));
    }
}