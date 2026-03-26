package org.golfit.graphics;

import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.List;

public class Course {

    private static final int COLS = 80;
    private static final int ROWS = 45;
    private static final int COURSE_COLS = 65;
    private Pane container;
    private Color backgroundColor = Color.rgb(121, 213, 33);

    private static final int EMPTY = 0;
    private static final int WALL  = 1;
    private static final int SAND  = 2;
    private static final int ICE   = 3;
    private static final int WATER = 4;
    private static final int BOUNCE = 5;
    private static final int SWITCH = 6;



    public List<Shape> obstacles = new ArrayList<>();
    public Shape[][] shapeGrid = new Shape[COLS][ROWS];

    public Course(Scene scene) {

        container = new Pane();
        container.widthProperty().addListener((obs, oldVal, newVal) -> updateGridFromList());
        container.heightProperty().addListener((obs, oldVal, newVal) -> updateGridFromList());

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
     *
     * @param scene  the JavaFX scene to add the object to
     * @param col    the column in the grid
     * @param row    the row in the grid
     * @param width  width in grid units
     * @param height height in grid units
     * @param color  the color of the shape
     */
    private Rectangle addRect(Scene scene, int col, int row, int width, int height, Color color) {
        Rectangle rect = new Rectangle();
        rect.setFill(color);
        bindGrid(rect, scene, col, row, width, height);
        container.getChildren().add(rect);
        return rect;
    }

    /**
     * Binds a rectangle to the grid (makes it scalable with window size)
     *
     * @param rect   the given rectangle
     * @param scene  y
     * @param col    the column in the grid
     * @param row    the row in the grid
     * @param width  ellipse width in grid size
     * @param height ellipse height in grid size
     */


    /**
     * Adds a wall to the course at a specified grid position.
     *
     * @param scene  the JavaFX scene to add the object to
     * @param col    the column in the grid
     * @param row    the row in the grid
     * @param width  width in grid units (if applicable)
     * @param height height in grid units (if applicable)
     */
    private void addWall(Scene scene, int col, int row, int width, int height) {
        Rectangle rect = addRect(scene, col, row, width, height, Color.rgb(124, 63, 0));
        rect.setUserData(WALL);
        obstacles.add(rect);
    }

    /**
     * Adds sand terrain to the course at a specified grid position.
     *
     * @param scene  the JavaFX scene to add the object to
     * @param col    the column in the grid
     * @param row    the row in the grid
     * @param width  width in grid units (if applicable)
     * @param height height in grid units (if applicable)
     */
    private void addSand(Scene scene, int col, int row, int width, int height) {
        Rectangle rect = addRect(scene, col, row, width, height, Color.rgb(203, 189, 147));
        rect.setUserData(SAND);
        obstacles.add(rect);
    }

    /**
     * Adds ice terrain to the course at a specified grid position.
     *
     * @param scene  the JavaFX scene to add the object to
     * @param col    the column in the grid
     * @param row    the row in the grid
     * @param width  width in grid units (if applicable)
     * @param height height in grid units (if applicable)
     */

    private void addIce(Scene scene, int col, int row, int width, int height) {
        Rectangle rect = addRect(scene, col, row, width, height, Color.rgb(148, 247, 255));
        rect.setUserData(ICE);
        obstacles.add(rect);
    }

    /**
     * Adds water terrain to the course at a specified grid position.
     *
     * @param scene  the JavaFX scene to add the object to
     * @param col    the column in the grid
     * @param row    the row in the grid
     * @param width  width in grid units (if applicable)
     * @param height height in grid units (if applicable)
     */

    private void addWater(Scene scene, int col, int row, int width, int height) {
        Rectangle rect = addRect(scene, col, row, width, height, Color.rgb(35, 137, 218));
        rect.setUserData(WATER);
        obstacles.add(rect);
    }


    /**
     * Adds the golf hole to a specified grid position.
     *
     * @param scene the JavaFX scene to add the object to
     * @param col   the column in the grid
     * @param row   the row in the grid
     */
    private void addGolfHole(Scene scene, int col, int row) {
        addEllipse(scene, col, row, 3, 1, Color.BLACK);
    }

    /**
     * Adds a switch to the course at a specified grid position.
     *
     * @param scene the JavaFX scene to add the object to
     * @param col   the column in the grid
     * @param row   the row in the grid
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
     * @param scene the given JavaFX scene
     * @param col   the column of the position
     * @param row   the row of the position
     */
    private void addBall(Scene scene, int col, int row) {
        Ellipse ellipse = addEllipse(scene, col, row, 3, 3, Color.rgb(255, 16, 240));
        ellipse.setUserData(BOUNCE);
        obstacles.add(ellipse);
    }

    /**
     * Adds an ellipse to the course at a specified grid position.
     *
     * @param scene  the JavaFX scene to add the object to
     * @param col    the column in the grid
     * @param row    the row in the grid
     * @param width  ellipse width in grid size
     * @param height ellipse height in grid size
     * @param color  color of the ellipse
     * @return
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
     *
     * @param scene  the JavaFX scene to add the object to
     * @param coords coordinates of the polygon points
     */
    private void addPolygon(Scene scene, double... coords) {
        Polygon poly = new Polygon();

        // Bind each point dynamically
        for (int i = 0; i < coords.length; i += 2) {
            final int index = i;
            double gridX = coords[i];
            double gridY = coords[i + 1];

            // Wir fügen Initialwerte hinzu
            poly.getPoints().addAll(0.0, 0.0);

            // X-Koordinate binden
            scene.widthProperty().addListener((obs, oldVal, newVal) ->
                    poly.getPoints().set(index, newVal.doubleValue() / COLS * gridX));

            // Y-Koordinate binden
            scene.heightProperty().addListener((obs, oldVal, newVal) ->
                    poly.getPoints().set(index + 1, newVal.doubleValue() / ROWS * gridY));

            // Sofort berechnen für den aktuellen Stand
            if (scene.getWidth() > 0) {
                poly.getPoints().set(index, scene.getWidth() / COLS * gridX);
                poly.getPoints().set(index + 1, scene.getHeight() / ROWS * gridY);
            }
        }

        poly.setFill(Color.rgb(124, 63, 0)); // Standardfarbe
        poly.setUserData(WALL);
        obstacles.add(poly);
        container.getChildren().add(poly);
    }

    /**
     * Creates a tree at the specified grid position
     *
     * @param scene the given JavaFX scene
     * @param col   the column of the position
     * @param row   the row of the position
     */
    private void addTree(Scene scene, int col, int row) {
        // 1. Stamm
        addWall(scene, col, row, 1, 1);

        // 2. Krone Maße
        double canopyWidth = 3.0;
        double canopyHeight = 2.0;

        // Präzise Koordinaten berechnen
        double leftX   = col - canopyWidth / 2.0 + 0.5;
        double rightX  = col + canopyWidth / 2.0 + 0.5;
        double centerX = col + 0.5;
        double bottomY = row;
        double topY    = row - canopyHeight;

        // 3. Krone hinzufügen
        addPolygon(scene,
                leftX, bottomY,   // Punkt unten links
                rightX, bottomY,  // Punkt unten rechts
                centerX, topY     // Punkt oben mitte
        );

        // Letztes hinzugefügtes Shape ist die Krone -> Farbe anpassen
        Shape canopy = obstacles.get(obstacles.size() - 1);
        canopy.setFill(Color.FORESTGREEN);
    }

    /**
     * returns the created pane
     */
    public Pane getPane() {
        return container;
    }

    /**
     * Creates a visible grid
     *
     * @param scene the JavaFX scene to add the grid to
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
        // In deiner start-Methode oder im Konstruktor:

    }
    /**
     * Updates the gridlist for distance calculation
     */
    public void updateGridFromList() {
        double cellW = container.getWidth() / COLS;
        double cellH = container.getHeight() / ROWS;

        if (cellW <= 0 || cellH <= 0) return;

        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                shapeGrid[i][j] = null;
            }
        }

        for (Shape s : obstacles) {
            Bounds b = s.getBoundsInParent();

            int startCol = (int) Math.max(0, Math.floor((b.getMinX() + 0.1) / cellW));
            int endCol   = (int) Math.min(COLS - 1, Math.floor((b.getMaxX() - 0.1) / cellW));
            int startRow = (int) Math.max(0, Math.floor((b.getMinY() + 0.1) / cellH));
            int endRow   = (int) Math.min(ROWS - 1, Math.floor((b.getMaxY() - 0.1) / cellH));

            for (int c = startCol; c <= endCol; c++) {
                for (int r = startRow; r <= endRow; r++) {
                    // NEU: Berechne den Mittelpunkt dieser Zelle in Pixeln
                    double centerX = (c + 0.5) * cellW;
                    double centerY = (r + 0.5) * cellH;

                    // Prüfe, ob dieser Punkt WIRKLICH innerhalb des Shapes liegt
                    if (s.contains(centerX, centerY)) {
                        shapeGrid[c][r] = s;
                    }
                }
            }
        }
    }

    /**
     * Creates a visible grid to test gridlist
     *
     */
    // Liste oben in der Klasse definieren, um die Kästchen wieder löschen zu können
    private List<Rectangle> debugShapes = new ArrayList<>();

    public void debugViewGrid() {
        // 1. Alte Debug-Shapes entfernen, bevor neue gezeichnet werden
        container.getChildren().removeAll(debugShapes);
        debugShapes.clear();

        double cellW = container.getWidth() / COLS;
        double cellH = container.getHeight() / ROWS;

        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS; r++) {
                // WICHTIG: Prüfen, ob ein Objekt EXISTIERT ( != null )
                if (shapeGrid[c][r] != null) {
                    System.out.println("Zelle " + c + "/" + r + " belegt durch: " + shapeGrid[c][r].getUserData());

                    Rectangle rect = new Rectangle(c * cellW, r * cellH, cellW, cellH);

                    // Schwarz für belegte Zellen
                    rect.setFill(Color.rgb(0, 0, 0, 0.8));

                    // Weißer Rahmen zur Abgrenzung
                    rect.setStroke(Color.WHITE);
                    rect.setStrokeWidth(0.5);

                    // Damit der Ball nicht gegen die Debug-Kästchen prallt
                    rect.setMouseTransparent(true);

                    debugShapes.add(rect);
                    container.getChildren().add(rect);
                }
            }
        }
    }

    public void printShapeGrid() {
        System.out.println("--- ShapeGrid Analyse ---");
        for (int r = 0; r < ROWS; r++) {
            StringBuilder rowString = new StringBuilder();
            for (int c = 0; c < COLS; c++) {
                if (shapeGrid[c][r] != null) {
                    rowString.append("X"); // Hier liegt ein Objekt
                } else {
                    rowString.append("."); // Hier ist leer
                }
            }
            System.out.println(rowString.toString());
        }
    }

    public Shape[][] getGrid() { return shapeGrid;}




}