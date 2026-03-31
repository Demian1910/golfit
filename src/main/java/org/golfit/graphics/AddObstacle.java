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
        Ellipse ellipse = addEllipse(scene, col, row, 4, 3, Color.rgb(255, 16, 240));
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
    public void addPolygon(Pane scene, double... coords) {
        Polygon poly = new Polygon();

        // Wir nutzen DoubleBindings für die aktuelle Zellengröße
        DoubleBinding unitW = container.widthProperty().divide((double)COLS);
        DoubleBinding unitH = container.heightProperty().divide((double)ROWS);

        // Diese Funktion berechnet die Punkte basierend auf den aktuellen Maßen neu
        Runnable update = () -> {
            double w = unitW.get();
            double h = unitH.get();
            List<Double> newPoints = new ArrayList<>();
            for (int i = 0; i < coords.length; i += 2) {
                newPoints.add(coords[i] * w);     // X-Koordinate * aktuelle Zellenbreite
                newPoints.add(coords[i+1] * h);   // Y-Koordinate * aktuelle Zellenhöhe
            }
            poly.getPoints().setAll(newPoints);
        };

        // WICHTIG: Die Listener müssen an den Container gebunden sein
        container.widthProperty().addListener((obs, ov, nv) -> update.run());
        container.heightProperty().addListener((obs, ov, nv) -> update.run());

        poly.setFill(Color.rgb(124, 63, 0));
        poly.setUserData(WALL);
        container.getChildren().add(poly);
        obstacles.add(poly);

        // Initialer Aufruf
        javafx.application.Platform.runLater(update);
    }

    /**
     * Creates a tree at the specified grid position
     */
    public void addTree(Pane scene, int col, int row) {
        // 1. Stamm (Trunk) - Wir erstellen ein nacktes Rectangle ohne bindGrid!
        Rectangle trunk = new Rectangle();
        trunk.setFill(Color.rgb(124, 63, 0));
        trunk.setUserData(WALL);

        // 2. Krone (Canopy)
        Polygon canopy = new Polygon();
        canopy.setFill(Color.rgb(34, 139, 34));
        canopy.setUserData(WALL);

        // Wir nutzen den Container für die Berechnung
        DoubleBinding unitW = container.widthProperty().divide((double)COLS);
        DoubleBinding unitH = container.heightProperty().divide((double)ROWS);

        // EIN gemeinsames Update für beide Teile
        Runnable updateTree = () -> {
            double w = unitW.get();
            double h = unitH.get();

            // Stamm positionieren (Manuelle Berechnung statt Binding)
            trunk.setX(col * w);
            trunk.setY(row * h);
            trunk.setWidth(w);   // 1 Zelle breit
            trunk.setHeight(h);  // 1 Zelle hoch

            // Krone positionieren (Exakt die gleichen 'w' und 'h' Werte)
            canopy.getPoints().setAll(
                    (col - 1.0) * w, (double)row * h,          // Links unten
                    (col + 2.0) * w, (double)row * h,          // Rechts unten
                    (col + 0.5) * w, (double)(row - 2) * h     // Spitze oben Mitte
            );
        };

        // Listener auf den Container setzen
        container.widthProperty().addListener((obs, oldVal, newVal) -> updateTree.run());
        container.heightProperty().addListener((obs, oldVal, newVal) -> updateTree.run());

        container.getChildren().addAll(trunk, canopy);
        obstacles.add(trunk);
        obstacles.add(canopy);

        // Sofort ausführen
        javafx.application.Platform.runLater(updateTree);
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
        for (int c = 0; c <= COLS; c++) {
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
