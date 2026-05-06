package org.golfit.graphics;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

import java.util.List;
import java.util.Random;

/**
 * Defines the mini-golf course layout by placing terrain obstacles (walls, sand, ice, water,
 * trees, switches, and the golf hole) onto an 80x45 grid within a fixed 1100x600 pane.
 */
public class Course {

    private static final int COLS = 80;
    private static final int ROWS = 45;
    private Pane container;
    private Color backgroundColor = Color.rgb(121, 213, 33);
    private AddObstacle addObstacle;
    int TILE_SIZE = 16;

    public Course() {
        container = new Pane();

        // LOCK the internal world size to 1100x600
        container.setPrefSize(1100, 600);
        container.setMinSize(1100, 600);
        container.setMaxSize(1100, 600);

        addObstacle = new AddObstacle(container);
        addObstacle.clearObstacles();

        // --- BACKGROUND ---
        // Covers the full 1100px width (COLS = 80)
        addTiledGrass();

        // --- PATH 2 START ---
        addObstacle.addWall(1, 22, 3, 2);
        addObstacle.addWall(13, 22, 3, 2);
        addObstacle.addSand(7, 22, 4, 2);

        // --- PATH 2 TOP LEFT ROOM ---
        addObstacle.addIce(1, 15, 3, 7);
        addObstacle.addIce(13, 15, 8, 7);
        addObstacle.addWall(1, 10, 8, 3);
        addObstacle.addIce(9, 1, 12, 12);

        // Trees top left
        addObstacle.addTree(7, 18);
        addObstacle.addTree(11, 18);
        addObstacle.addTree(3, 4);
        addObstacle.addTree(7, 4);
        addObstacle.addTree(3, 8);
        addObstacle.addTree(7, 8);

        // --- PATH 2 BARRIER & POLYGONS ---
        addObstacle.addWall(21, 5, 4, 20);
        addObstacle.addPolygon(1, 15, 1, 12, 5, 12);

        // --- PATH 2 ROOM 2 ---
        addObstacle.addWater(29, 5, 10, 10);
        addObstacle.addSand(29, 1, 10, 4);
        addObstacle.addIce(29, 15, 10, 4);
        addObstacle.addPolygon(25, 16, 25, 19, 28, 19);

        // --- GOLF AREA ---
        addObstacle.addIce(57, 1, 12, 6);
        addObstacle.addWater(72, 4, 4, 3);
        addObstacle.addGolfHole(64, 8);
        addObstacle.addPolygon(76, 1, 79, 1, 79, 3);

        // Golf wall
        addObstacle.addWall(COLS - 14, ROWS - 34, 4, 18);
        addObstacle.addWall(COLS - 21, ROWS - 34, 7, 2);
        addObstacle.addWall(COLS - 21, ROWS - 38, 3, 6);

        // --- PATH 1 ---
        addObstacle.addIce(31, ROWS - 21, 11, 20);
        addObstacle.addSand(1, ROWS - 4, 17, 3);
        addObstacle.addSand(17, ROWS - 7, 21, 6);
        addObstacle.addWall(42, ROWS - 11, 4, 10);

        // Path 1 ice-sand stripes
        for (int i = 0; i < 8; i++) {
            int yPos = ROWS - 20 - (i * 2);
            if (i % 2 == 0) addObstacle.addIce(COLS - 10, yPos, 9, 2);
            else addObstacle.addSand(COLS - 10, yPos, 9, 2);
        }

        // Path 1 lever-walls
        addObstacle.addWall(11, ROWS - 9, 27, 2);
        addObstacle.addWall(16, ROWS - 9, 3, 5);
        addObstacle.addSwitch(13, ROWS - 6);

        addObstacle.addWall(31, ROWS - 26, 11, 2);
        addObstacle.addWall(31, ROWS - 26, 3, 5);
        addObstacle.addSwitch(35, ROWS - 23);

        // --- PATH 1.1 ---
        addObstacle.addTree(63, ROWS - 2);
        addObstacle.addTree(67, ROWS - 2);
        addObstacle.addIce(46, ROWS - 11, 15, 10);
        addObstacle.addWater(46, ROWS - 9, 13, 8);
        addObstacle.addPolygon(71, ROWS - 1, 71, ROWS - 4, 79, ROWS - 11, 79, ROWS - 1);

        // --- PATH 1.2 ---
        addObstacle.addBall(42, ROWS - 14);
        addObstacle.addBall(42, ROWS - 17);
        addObstacle.addBall(42, ROWS - 20);

        // Path 1.2 End
        addObstacle.addIce(COLS - 20, ROWS - 26, 6, 10);
        addObstacle.addWall(COLS - 21, ROWS - 26, 3, 10);
        addObstacle.addPolygon(COLS - 18, ROWS - 32, COLS - 13, ROWS - 32, COLS - 13, ROWS - 28);

        // Pool
        addObstacle.addRect(16, ROWS - 26, 16, 17, Color.GRAY);
        addObstacle.addWater(17, ROWS - 25, 14, 15);

        // Central Forest
        int[][] trees = {{50, 4}, {56, 12}, {48, 16}, {43, 10}, {45, 4}, {46, 11}, {56, 6}, {56, 15}, {44, 16}};
        for (int[] t : trees) addObstacle.addTree(t[0], t[1]);

        // 2x3 Forest
        for (int x : new int[]{50, 53}) {
            for (int y : new int[]{22, 26, 30}) addObstacle.addTree(x, y);
        }

        // --- MAP BORDERS ---
        addObstacle.addWall(0, 0, COLS, 1);          // Top
        addObstacle.addWall(0, ROWS - 1, COLS, 1);   // Bottom
        addObstacle.addWall(0, 0, 1, ROWS);          // Left
        addObstacle.addWall(79, 0, 1, ROWS);         // Right
        addObstacle.addBridge(17, ROWS-15, 14, 2);         // Bridge
        addObstacle.addBarrier(21, 1, 4, 4);         // Barrier wall
    }

    /** Returns the pane containing all course elements. */
    public Pane getPane() {
        return container;
    }

    private void addTiledGrass() {
        try {
            Image sheet = new Image(
                    getClass().getResourceAsStream("/images/GolfCourseTiles.png")
            );

            double paneW = 1100, paneH = 600;
            double tileW = paneW / COLS;
            double tileH = paneH / ROWS;

            Canvas canvas = new Canvas(paneW, paneH);
            GraphicsContext gc = canvas.getGraphicsContext2D();

            int[] grassCols = {8, 9, 10, 11};
            int grassRow = 10;
            Random random = new Random(42);

            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    int tileCol = grassCols[random.nextInt(grassCols.length)];
                    int padding = 1;
                    gc.drawImage(sheet,
                            tileCol * TILE_SIZE + padding, grassRow * TILE_SIZE + padding,
                            TILE_SIZE - padding * 2, TILE_SIZE - padding * 2,  // crop inward
                            col * tileW, row * tileH, tileW, tileH
                    );
                }
            }

            container.getChildren().add(0, canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideObstacle(int id) {
        if (id >= 0 && id < addObstacle.getObstacleList().size()) {
            Shape shape = addObstacle.getObstacleList().get(id);
            if (shape != null) {
                // Toggle visibility so it can be "unhidden" by switches
                boolean isNowVisible = !shape.isVisible();
                shape.setVisible(isNowVisible);

                // Log for debugging
                System.out.println("[GUI] Obstacle " + id + " is now " + (isNowVisible ? "VISIBLE" : "HIDDEN"));
            }
        }
    }

    /** Returns the list of obstacle shapes for grid mapping and collision export. */
    public List<Shape> getObstacles() {
        return addObstacle.getObstacleList();
    }
}