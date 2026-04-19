package org.golfit.graphics;

import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import java.util.ArrayList;
import java.util.List;

public class AddObstacle {
    private static final int COLS = 80;
    private static final int ROWS = 45;

    // Terrain Type Constants
    private static final int EMPTY = 0;
    private static final int WALL = 1;
    private static final int SAND = 2;
    private static final int ICE = 3;
    private static final int WATER = 4;
    private static final int BOUNCE = 5;
    private static final int SWITCH = 6;

    private final Pane container;
    public List<Shape> obstacles = new ArrayList<>();

    public AddObstacle(Pane container) {
        this.container = container;
    }

    /**
     * Prevents JavaFX layout logic from interfering with our fixed 1100x600 coordinate system.
     */
    private void setupShape(Shape s) {
        s.setManaged(false);
        container.getChildren().add(s);
    }

    /** --- Tree Logic (Linked Shapes) --- **/
    public void addTree(int col, int row) {
        int trunkId = obstacles.size();
        int canopyId = trunkId + 1;

        // Trunk (Rectangle)
        Rectangle trunk = new Rectangle();
        trunk.setFill(Color.rgb(101, 67, 33));
        trunk.setUserData(new int[]{WALL, canopyId}); // Points to leaves
        bindRect(trunk, col, row, 1, 1);

        // Canopy (Polygon)
        Polygon canopy = new Polygon();
        canopy.setFill(Color.rgb(34, 139, 34));
        canopy.setUserData(new int[]{WALL, trunkId}); // Points to trunk

        // Define Triangle relative to trunk: 3 cells wide, 2 cells high
        updatePolygon(canopy, col - 1.0, row, col + 2.0, row, col + 0.5, row - 2);

        obstacles.add(trunk);
        obstacles.add(canopy);
        setupShape(trunk);
        setupShape(canopy);
    }

    /** --- Standard Terrain Types --- **/
    public void addWall(int col, int row, int w, int h) {
        Rectangle r = createTerrainRect(col, row, w, h, Color.rgb(124, 63, 0), WALL);
        obstacles.add(r);
        setupShape(r);
    }

    public void addSand(int col, int row, int w, int h) {
        Rectangle r = createTerrainRect(col, row, w, h, Color.rgb(203, 189, 147), SAND);
        obstacles.add(r);
        setupShape(r);
    }

    public void addIce(int col, int row, int w, int h) {
        Rectangle r = createTerrainRect(col, row, w, h, Color.rgb(148, 247, 255), ICE);
        obstacles.add(r);
        setupShape(r);
    }

    public void addWater(int col, int row, int w, int h) {
        Rectangle r = createTerrainRect(col, row, w, h, Color.rgb(35, 137, 218), WATER);
        obstacles.add(r);
        setupShape(r);
    }

    /** --- Interactive & Special Elements --- **/
    public void addSwitch(int col, int row) {
        // Background plate for the switch
        addRect(col, row, 2, 1, Color.GRAY);

        // The actual interactive Ellipse
        Ellipse s = createEllipse(col, row, 2, 1, Color.RED);
        s.setUserData(new int[]{SWITCH, -1});
        obstacles.add(s);
        setupShape(s);
    }

    public void addGolfHole(int col, int row) {
        Ellipse hole = createEllipse(col, row, 2, 2, Color.BLACK);
        hole.setUserData(new int[]{0, -1}); // Type 0 usually represents goal
        setupShape(hole);
    }

    public void addBall(int col, int row) {

        Ellipse b = createEllipse(col, row, 3, 3, Color.rgb(255, 16, 240));
        b.setUserData(new int[]{BOUNCE, -1});
        obstacles.add(b);
        setupShape(b);
    }

    /** --- Helper Creation Methods --- **/
    private Rectangle createTerrainRect(int col, int row, int w, int h, Color color, int type) {
        Rectangle r = new Rectangle();
        r.setFill(color);
        r.setUserData(new int[]{type, -1});
        bindRect(r, col, row, w, h);
        return r;
    }

    public void addRect(int col, int row, int w, int h, Color color) {
        Rectangle r = new Rectangle();
        r.setFill(color);
        bindRect(r, col, row, w, h);
        setupShape(r);
    }

    public void addPolygon(double... coords) {
        Polygon p = new Polygon();
        p.setFill(Color.rgb(124, 63, 0));
        p.setUserData(new int[]{WALL, -1});
        updatePolygon(p, coords);
        obstacles.add(p);
        setupShape(p);
    }

    private Ellipse createEllipse(int col, int row, double w, double h, Color color) {
        Ellipse e = new Ellipse();
        e.setFill(color);
        e.centerXProperty().bind(container.widthProperty().divide(COLS).multiply(col + w / 2.0));
        e.centerYProperty().bind(container.heightProperty().divide(ROWS).multiply(row + h / 2.0));
        e.radiusXProperty().bind(container.widthProperty().divide(COLS).multiply(w / 2.0));
        e.radiusYProperty().bind(container.heightProperty().divide(ROWS).multiply(h / 2.0));
        return e;
    }

    private void bindRect(Rectangle r, int c, int row, int w, int h) {
        r.xProperty().bind(container.widthProperty().divide(COLS).multiply(c));
        r.yProperty().bind(container.heightProperty().divide(ROWS).multiply(row));
        r.widthProperty().bind(container.widthProperty().divide(COLS).multiply(w));
        r.heightProperty().bind(container.heightProperty().divide(ROWS).multiply(h));
    }

    private void updatePolygon(Polygon p, double... coords) {
        Runnable r = () -> {
            double unitW = container.getWidth() / COLS;
            double unitH = container.getHeight() / ROWS;


            if (unitW <= 0 || unitH <= 0) return;

            List<Double> pts = new ArrayList<>();
            for (int i = 0; i < coords.length; i += 2) {
                pts.add(coords[i] * unitW);
                pts.add(coords[i + 1] * unitH);
            }
            p.getPoints().setAll(pts);
        };


        container.widthProperty().addListener((o, old, v) -> r.run());
        container.heightProperty().addListener((o, old, v) -> r.run());


        Platform.runLater(r);
    }

    public List<Shape> getObstacleList() { return obstacles; }
}