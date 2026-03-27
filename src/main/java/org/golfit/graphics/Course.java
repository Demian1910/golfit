package org.golfit.graphics;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.shape.*;
import javafx.scene.transform.Scale;

import java.util.ArrayList;
import java.util.List;

public class Course {

    private static final int COLS = 80;
    private static final int ROWS = 45;
    private static final int COURSE_COLS = 65;
    private Pane container;
    private Color backgroundColor = Color.rgb(121, 213, 33);

    private static final int EMPTY = 0;
    private static final int WALL = 1;
    private static final int SAND = 2;
    private static final int ICE = 3;
    private static final int WATER = 4;
    private static final int BOUNCE = 5;
    private static final int SWITCH = 6;

    public int[][] grid = new int[COLS][ROWS];

    public List<Shape> obstacles = new ArrayList<>();


    public Course(Scene scene) {

        container = new Pane();

        // Background
        addRect(scene, 0, 0, COURSE_COLS, ROWS, backgroundColor);

        // Map Borders
        addWall(scene, 0, 0, COURSE_COLS, 1);          // top
        addWall(scene, 0, ROWS - 1, COURSE_COLS, 1);   // bottom
        addWall(scene, 0, 0, 1, ROWS);                 // left
        addWall(scene, COURSE_COLS - 1, 0, 1, ROWS);   // right
        // Path 2 start
        addWall(scene, 1, 22, 3, 2);
        addWall(scene, 12, 22, 3, 2);
        addSand(scene, 7, 22, 2, 2);

        // Path 2 top left room
        addIce(scene, 1, 15, 3, 7);
        addIce(scene, 12, 15, 8, 7);
        addWall(scene, 1, 10, 7, 3);
        addIce(scene, 8, 1, 12, 12);
        // Trees between ice
        addTree(scene, 5, 18);
        addTree(scene, 9, 18);
        // Trees top left
        addTree(scene, 2, 4);
        addTree(scene, 6, 4);
        addTree(scene, 2, 8);
        addTree(scene, 6, 8);
        // Trees in ice
        addTree(scene, 12, 7);
        addTree(scene, 16, 7);

        // Path 2 barrier wall
        addWall(scene, 20, 5, 3, 20);
        addPolygon(scene,
                1, 15,
                1, 13,
                4, 13
        );

        // Path 2 room 2
        addWater(scene, 28, 5, 10, 10);
        addSand(scene, 28, 1, 10, 4);
        addIce(scene, 28, 15, 10, 4);
        addPolygon(scene,
                23, 17,
                23, 19,
                25, 19
        );
        // Golf area
        addIce(scene, 45, 1, 12, 6);
        addWater(scene, 58, 4, 4, 3);
        addGolfHole(scene, 51, 9);
        addPolygon(scene,
                62, 1,
                64, 1,
                64, 3
        );
        // Top right trees
        addTree(scene, 45, 10);
        addTree(scene, 42, 7);
        addTree(scene, 42, 12);
        addTree(scene, 41, 17);
        addTree(scene, 46, 15);

        // Golf wall
        addWall(scene, COURSE_COLS - 10, ROWS - 34, 2, 18);
        addWall(scene, COURSE_COLS - 15, ROWS - 34, 5, 2);
        addWall(scene, COURSE_COLS - 17, ROWS - 38, 2, 6);


        // Path 1
        addIce(scene, 30, ROWS - 21, 10, 20);
        addSand(scene, 1, ROWS - 4, 15, 3);
        addSand(scene, 16, ROWS - 7, 20, 6);
        addWall(scene, 40, ROWS - 11, 3, 10);


        // Path 1 ice-sand stripe
        addIce(scene, COURSE_COLS - 10, ROWS - 20, 9, 2);
        addSand(scene, COURSE_COLS - 10, ROWS - 22, 9, 2);
        addIce(scene, COURSE_COLS - 10, ROWS - 24, 9, 2);
        addSand(scene, COURSE_COLS - 10, ROWS - 26, 9, 2);
        addIce(scene, COURSE_COLS - 10, ROWS - 28, 9, 2);
        addSand(scene, COURSE_COLS - 10, ROWS - 30, 9, 2);
        addIce(scene, COURSE_COLS - 10, ROWS - 32, 9, 2);
        addSand(scene, COURSE_COLS - 10, ROWS - 34, 9, 2);

        // Path 1 lever-wall
        addWall(scene, 10, ROWS - 9, 26, 2);
        addWall(scene, 15, ROWS - 9, 2, 5);
        addSwitch(scene, 12, ROWS - 6);

        // Path 1 lever-wall 2
        addWall(scene, 30, ROWS - 26, 10, 2);
        addWall(scene, 30, ROWS - 26, 2, 5);
        addSwitch(scene, 33, ROWS - 23);

        // Path 1.1 ice
        addIce(scene, 43, ROWS - 11, 14, 10);
        addWater(scene, 43, ROWS - 9, 12, 8);
        addPolygon(scene,
                57, ROWS - 1,
                57, ROWS - 4,
                64, ROWS - 11,
                64, ROWS - 1
        );

        // Path 1.2
        addBall(scene, 40, ROWS - 14);
        addBall(scene, 40, ROWS - 17);
        addBall(scene, 40, ROWS - 20);

        addIce(scene, COURSE_COLS - 15, ROWS - 26, 5, 10);
        addWall(scene, COURSE_COLS - 17, ROWS - 26, 2, 10);
        addPolygon(scene,
                COURSE_COLS - 13, ROWS - 32,
                COURSE_COLS - 10, ROWS - 32,
                COURSE_COLS - 10, ROWS - 29
        );

        addRect(scene, 15, ROWS - 26, 15, 19, Color.GREY);
        addWater(scene, 16, ROWS - 25, 13, 17);


        // GUI Box on right side
        addRect(scene, 65, 0, 15, ROWS, Color.BLUE);

        // White rects for text
        addRect(scene, 66, 1, 13, 2, Color.WHITE); // strokes
        addRect(scene, 66, 4, 13, 12, Color.WHITE); // leaderboard
        addRect(scene, 66, 17, 13, 20, Color.WHITE); // chat
        addRect(scene, 66, 38, 13, 2, Color.WHITE); // enter text
        addRect(scene, 66, 42, 10, 2, Color.WHITE); // time
        addRect(scene, 77, 42, 2, 2, Color.WHITE); // settings

        // Debug grid
        showGrid(scene);
    }

    /** ================= Helper Functions ================= **/
    /**
     * Adds a rectangle to the course at a specified grid position.
     */
    private Rectangle addRect(Scene scene, int col, int row, int width, int height, Color color) {
        Rectangle rect = new Rectangle();
        rect.setFill(color);
        bindGrid(rect, scene, col, row, width, height);
        container.getChildren().add(rect);
        return rect;
    }




    /**
     * Adds a wall to the course at a specified grid position.
     */
    private void addWall(Scene scene, int col, int row, int width, int height) {
        Rectangle rect = addRect(scene, col, row, width, height, Color.rgb(124, 63, 0));
        rect.setUserData(WALL);
        obstacles.add(rect);
    }

    /**
     * Adds sand terrain to the course at a specified grid position.
     *
     */
    private void addSand(Scene scene, int col, int row, int width, int height) {
        Rectangle rect = addRect(scene, col, row, width, height, Color.rgb(203, 189, 147));
        rect.setUserData(SAND);
        obstacles.add(rect);
    }

    /**
     * Adds ice terrain to the course at a specified grid position.
     */

    private void addIce(Scene scene, int col, int row, int width, int height) {
        Rectangle rect = addRect(scene, col, row, width, height, Color.rgb(148, 247, 255));
        rect.setUserData(ICE);
        obstacles.add(rect);
    }

    /**
     * Adds water terrain to the course at a specified grid position.
     */

    private void addWater(Scene scene, int col, int row, int width, int height) {
        Rectangle rect = addRect(scene, col, row, width, height, Color.rgb(35, 137, 218));
        rect.setUserData(WATER);
        obstacles.add(rect);
    }


    /**
     * Adds the golf hole to a specified grid position.
     */
    private void addGolfHole(Scene scene, int col, int row) {
        addEllipse(scene, col, row, 3, 1, Color.BLACK);
    }

    /**
     * Adds a switch to the course at a specified grid position.
     */
    private void addSwitch(Scene scene, int col, int row) {
        addRect(scene, col, row, 2, 1, Color.GRAY);
        Ellipse ellipse = addEllipse(scene, col, row, 2, 1, Color.RED);
        ellipse.setUserData(SWITCH);
        obstacles.add(ellipse);

    }

    /**
     * Creates a ball at the given grid position
     *
     */
    private void addBall(Scene scene, int col, int row) {
        Ellipse ellipse = addEllipse(scene, col, row, 3, 3, Color.rgb(255, 16, 240));
        ellipse.setUserData(BOUNCE);
        obstacles.add(ellipse);
    }

    /**
     * Adds an ellipse to the course at a specified grid position.
     */
    private Ellipse addEllipse(Scene scene, int col, int row, int width, int height, Color color) {
        Ellipse ellipse = new Ellipse();
        ellipse.setFill(color);

        ellipse.centerXProperty().bind(
                scene.widthProperty().divide(COLS).multiply(col + width / 2.0)
        );
        ellipse.centerYProperty().bind(
                scene.heightProperty().divide(ROWS).multiply(row + height / 2.0)
        );
        ellipse.radiusXProperty().bind(
                scene.widthProperty().divide(COLS).multiply(width / 2.0)
        );
        ellipse.radiusYProperty().bind(
                scene.heightProperty().divide(ROWS).multiply(height / 2.0)
        );

        container.getChildren().add(ellipse);
        return ellipse;
    }

    /**
     * Adds a polygon shape to the course at a specified grid position.
     */
    private void addPolygon(Scene scene, int... coords) {
        Polygon poly = new Polygon();

        // 1. Punkte im "Grid-Format" hinzufügen (1 Einheit = 1 Pixel Basis)
        for (int i = 0; i < coords.length; i++) {
            poly.getPoints().add((double) coords[i]);
        }

        // 2. Erstelle eine neue Skalierungs-Transformation
        Scale scale = new Scale();

        // 3. Binde die Skalierung an die Container-Größe
        // Wichtig: Wir nutzen (double) COLS, um Integer-Fehler zu vermeiden
        scale.xProperty().bind(container.widthProperty().divide((double)COLS));
        scale.yProperty().bind(container.heightProperty().divide((double)ROWS));

        // 4. Setze den Pivot-Punkt auf 0,0, damit von oben links skaliert wird
        scale.setPivotX(0);
        scale.setPivotY(0);

        // 5. Füge die Transformation dem Polygon hinzu
        poly.getTransforms().add(scale);

        // Optik & Daten
        poly.setFill(Color.rgb(124, 63, 0));
        poly.setUserData(WALL);

        container.getChildren().add(poly);
        obstacles.add(poly);
    }

    /**
     * Creates a tree at the specified grid position
     */
    private void addTree(Scene scene, int col, int row) {
        // 1. Stamm (Trunk) - Nutzt addRect, was bereits funktioniert
        Rectangle trunk = addRect(scene, col, row, 1, 1, Color.rgb(124, 63, 0));
        trunk.setUserData(WALL);
        obstacles.add(trunk);

        // 2. Krone (Canopy) - Wir berechnen die Eckpunkte im GRID-System
        // Beispiel: Ein Baum an (5, 18) mit Breite 3 und Höhe 2
        int canopyWidth = 3;
        int canopyHeight = 2;

        // Wir berechnen die Grid-Punkte (Ganzzahlen oder .5 Werte sind okay)
        double topX = col + 0.5;           // Mitte vom Stamm
        double topY = row - canopyHeight;  // Über dem Stamm
        double leftX = col - 1.0;          // Links daneben
        double rightX = col + 2.0;         // Rechts daneben
        double bottomY = row;              // Basis der Krone

        Polygon canopy = new Polygon();
        // Punkte als "rohe" Grid-Werte hinzufügen
        canopy.getPoints().addAll(
                leftX, bottomY,
                rightX, bottomY,
                topX, topY
        );

        // Jetzt dieselbe Scale-Logik wie in addPolygon
        Scale scale = new Scale();
        scale.xProperty().bind(container.widthProperty().divide((double)COLS));
        scale.yProperty().bind(container.heightProperty().divide((double)ROWS));
        scale.setPivotX(0);
        scale.setPivotY(0);

        canopy.getTransforms().add(scale);
        canopy.setFill(Color.rgb(34, 139, 34));
        canopy.setUserData(WALL);

        container.getChildren().add(canopy);
        obstacles.add(canopy);
    }

    /**
     * returns the created pane
     */
    public Pane getPane() {
        return container;
    }

    /**
     * Creates a visible grid
     */
    private void showGrid(Scene scene) {
        for (int c = 0; c <= COURSE_COLS; c++) {
            Line vLine = new Line();
            vLine.startXProperty().bind(scene.widthProperty().divide(COLS).multiply(c));
            vLine.endXProperty().bind(scene.widthProperty().divide(COLS).multiply(c));
            vLine.setStartY(0);
            vLine.endYProperty().bind(scene.heightProperty());
            vLine.setStroke(Color.rgb(0, 0, 0, 0.25));
            container.getChildren().add(vLine);
        }

        for (int r = 0; r <= ROWS; r++) {
            Line hLine = new Line();
            hLine.startYProperty().bind(scene.heightProperty().divide(ROWS).multiply(r));
            hLine.endYProperty().bind(scene.heightProperty().divide(ROWS).multiply(r));
            hLine.setStartX(0);
            hLine.endXProperty().bind(scene.widthProperty());
            hLine.setStroke(Color.rgb(0, 0, 0, 0.25));
            container.getChildren().add(hLine);
        }
    }

    /**
     * Binds rect to grid for resizing
     */
    private void bindGrid(Rectangle rect, Scene scene, int col, int row, int width, int height) {
        rect.xProperty().bind(scene.widthProperty().divide(COLS).multiply(col));
        rect.yProperty().bind(scene.heightProperty().divide(ROWS).multiply(row));
        rect.widthProperty().bind(scene.widthProperty().divide(COLS).multiply(width));
        rect.heightProperty().bind(scene.heightProperty().divide(ROWS).multiply(height));
    }

    /**

    /**
     * Creates a visible grid to test gridlist
     *
     */


    public int[][] getGrid() {return grid;}


    public List<Shape> getObstacles() {return obstacles;}
}
