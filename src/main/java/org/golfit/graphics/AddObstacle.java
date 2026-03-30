package org.golfit.graphics;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Scale;

import java.util.ArrayList;
import java.util.List;

public class AddObstacle {
    private static final int COLS = 80;
    private static final int ROWS = 45;
    private static final int COURSE_COLS = 65;
    private static final int EMPTY = 0;
    private static final int WALL = 1;
    private static final int SAND = 2;
    private static final int ICE = 3;
    private static final int WATER = 4;
    private static final int BOUNCE = 5;
    private static final int SWITCH = 6;


    public List<Shape> obstacles = new ArrayList<>();

    Pane scene;
    Pane container;

    public AddObstacle(Pane scene, Pane container){
        this.scene = scene;
        this.container = container;
    }




    /** ================= Helper Functions ================= **/
    /**
     * Adds a rectangle to the course at a specified grid position.
     */
    public Rectangle addRect(Pane scene, int col, int row, int width, int height, Color color) {
        Rectangle rect = new Rectangle();
        rect.setFill(color);
        bindGrid(rect, scene, col, row, width, height);
        container.getChildren().add(rect);
        return rect;
    }




    /**
     * Adds a wall to the course at a specified grid position.
     */
    public void addWall(Pane scene, int col, int row, int width, int height) {
        Rectangle rect = addRect(scene, col, row, width, height, Color.rgb(124, 63, 0));
        rect.setUserData(WALL);
        obstacles.add(rect);
    }

    /**
     * Adds sand terrain to the course at a specified grid position.
     *
     */
    public void addSand(Pane scene, int col, int row, int width, int height) {
        Rectangle rect = addRect(scene, col, row, width, height, Color.rgb(203, 189, 147));
        rect.setUserData(SAND);
        obstacles.add(rect);
    }

    /**
     * Adds ice terrain to the course at a specified grid position.
     */

    public void addIce(Pane scene, int col, int row, int width, int height) {
        Rectangle rect = addRect(scene, col, row, width, height, Color.rgb(148, 247, 255));
        rect.setUserData(ICE);
        obstacles.add(rect);
    }

    /**
     * Adds water terrain to the course at a specified grid position.
     */

    public void addWater(Pane scene, int col, int row, int width, int height) {
        Rectangle rect = addRect(scene, col, row, width, height, Color.rgb(35, 137, 218));
        rect.setUserData(WATER);
        obstacles.add(rect);
    }


    /**
     * Adds the golf hole to a specified grid position.
     */
    public void addGolfHole(Pane scene, int col, int row) {
        addEllipse(scene, col, row, 3, 1, Color.BLACK);
    }

    /**
     * Adds a switch to the course at a specified grid position.
     */
    public void addSwitch(Pane scene, int col, int row) {
        addRect(scene, col, row, 2, 1, Color.GRAY);
        Ellipse ellipse = addEllipse(scene, col, row, 2, 1, Color.RED);
        ellipse.setUserData(SWITCH);
        obstacles.add(ellipse);

    }

    /**
     * Creates a ball at the given grid position
     *
     */
    public void addBall(Pane scene, int col, int row) {
        Ellipse ellipse = addEllipse(scene, col, row, 3, 3, Color.rgb(255, 16, 240));
        ellipse.setUserData(BOUNCE);
        obstacles.add(ellipse);
    }

    /**
     * Adds an ellipse to the course at a specified grid position.
     */
    public Ellipse addEllipse(Pane scene, int col, int row, int width, int height, Color color) {
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
    public void addPolygon(Pane scene, int... coords) {
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
    public void addTree(Pane scene, int col, int row) {
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
    public void showGrid(Pane scene) {
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
    public void bindGrid(Rectangle rect, Pane scene, int col, int row, int width, int height) {
        rect.xProperty().bind(scene.widthProperty().divide(COLS).multiply(col));
        rect.yProperty().bind(scene.heightProperty().divide(ROWS).multiply(row));
        rect.widthProperty().bind(scene.widthProperty().divide(COLS).multiply(width));
        rect.heightProperty().bind(scene.heightProperty().divide(ROWS).multiply(height));
    }

    public List<Shape> getObstacleList(){return obstacles;}

}
