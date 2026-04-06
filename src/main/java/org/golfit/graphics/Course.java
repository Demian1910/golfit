package org.golfit.graphics;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import java.util.List;

public class Course {

    private static final int COLS = 80;
    private static final int ROWS = 45;
    private Pane container;
    private Color backgroundColor = Color.rgb(121, 213, 33);

    public int[][] grid = new int[COLS][ROWS];
    public List<Shape> obstacles;
    AddObstacle addObstacle;

    public Course(Pane scene) {
        container = new Pane();
        addObstacle = new AddObstacle(scene, container);

        // Background (+1 Breite)
        addObstacle.addRect(scene, 0, 0, COLS + 1, ROWS, backgroundColor);



        // Path 2 start (Breite +1)
        addObstacle.addWall(scene, 2, 22, 3 + 1, 2);
        addObstacle.addWall(scene, 13, 22, 3 + 1, 2);
        addObstacle.addSand(scene, 8, 22, 2 + 1, 2);

        // Path 2 top left room (Breite +1)
        addObstacle.addIce(scene, 2, 15, 3 + 1, 7);
        addObstacle.addIce(scene, 13, 15, 8 + 1, 7);
        addObstacle.addWall(scene, 2, 10, 7 + 1, 3);
        addObstacle.addIce(scene, 9, 1, 12 + 1, 12);

        // Trees top left
        addObstacle.addTree(scene, 7, 18);
        addObstacle.addTree(scene, 11, 18);
        addObstacle.addTree(scene, 3, 4);
        addObstacle.addTree(scene, 7, 4);
        addObstacle.addTree(scene, 3, 8);
        addObstacle.addTree(scene, 7, 8);

        // Path 2 barrier wall (Breite +1)
        addObstacle.addWall(scene, 22, 6, 4, 20);
        addObstacle.addPolygon(scene,
                1, 15,
                1, 12,
                5, 12 // X-endpoint shifted for more thickness
        );

        // Path 2 room 2 (Breite +1)
        addObstacle.addWater(scene, 29, 5, 10 , 10);
        addObstacle.addSand(scene, 29, 1, 10 , 4);
        addObstacle.addIce(scene, 29, 15, 10 , 4);
        addObstacle.addPolygon(scene,
                25, 16,
                25, 20,
                28 + 1, 20 // Breite +1
        );

        // Golf area (Breite +1)
        addObstacle.addIce(scene, 57, 1, 12 + 1, 6);
        addObstacle.addWater(scene, 72, 4, 4 + 1, 3);
        addObstacle.addGolfHole(scene, 65, 9);
        addObstacle.addPolygon(scene,
                75, 1,
                77 + 1, 1,
                77 + 1, 3
        );


        // Golf wall (Breite +1)
        addObstacle.addWall(scene, COLS - 14, ROWS - 34, 2 + 2, 18);
        addObstacle.addWall(scene, COLS - 21, ROWS - 34, 5 + 2, 2);
        addObstacle.addWall(scene, COLS - 21, ROWS - 38, 2 + 1, 6);

        // Path 1 (Breite +1)
        addObstacle.addIce(scene, 31, ROWS - 21, 10 + 1, 20);
        addObstacle.addSand(scene, 2, ROWS - 4, 15 + 1, 3);
        addObstacle.addSand(scene, 17, ROWS - 7, 20 + 1, 6);
        addObstacle.addWall(scene, 42, ROWS - 11, 3 + 1, 10);

        // Path 1 ice-sand stripe (Breite +1)
        addObstacle.addIce(scene, COLS -10, ROWS - 20, 9 , 2);
        addObstacle.addSand(scene, COLS - 10, ROWS - 22, 9, 2);
        addObstacle.addIce(scene, COLS - 10, ROWS - 24, 9, 2);
        addObstacle.addSand(scene, COLS - 10, ROWS - 26, 9, 2);
        addObstacle.addIce(scene, COLS - 10, ROWS - 28, 9, 2);
        addObstacle.addSand(scene, COLS - 10, ROWS - 30, 9, 2);
        addObstacle.addIce(scene, COLS - 10, ROWS - 32, 9, 2);
        addObstacle.addSand(scene, COLS - 10, ROWS - 34, 9, 2);

        // Path 1 lever-wall (Breite +1)
        addObstacle.addWall(scene, 11, ROWS - 9, 26 + 1, 2);
        addObstacle.addWall(scene, 16, ROWS - 9, 2 + 1, 5);
        addObstacle.addSwitch(scene, 13, ROWS - 6);

        // Path 1 lever-wall 2 (Breite +1)
        addObstacle.addWall(scene, 31, ROWS - 26, 10 + 1, 2);
        addObstacle.addWall(scene, 31, ROWS - 26, 2 + 1, 5);
        addObstacle.addSwitch(scene, 35, ROWS - 23);

        // Path 1.1 ice (Breite +1)
        addObstacle.addTree(scene, 63, ROWS-2); //
        addObstacle.addTree(scene, 67   , ROWS-2); //
        addObstacle.addIce(scene, 46, ROWS - 11, 14 + 1, 10);
        addObstacle.addWater(scene, 46, ROWS - 9, 12 + 1, 8);
        addObstacle.addPolygon(scene,
                57+13, ROWS - 1,
                57+13, ROWS - 4,
                64+13 + 1, ROWS - 11, // X-Punkt +1
                64+13 + 1, ROWS - 1  // X-Punkt +1
        );

        // Path 1.2
        addObstacle.addBall(scene, 42, ROWS - 14);
        addObstacle.addBall(scene, 42, ROWS - 17);
        addObstacle.addBall(scene, 42, ROWS - 20);

        // Path 1.2 Ende
        addObstacle.addIce(scene, COLS - 20, ROWS - 26, 5 + 1, 10);
        addObstacle.addWall(scene, COLS - 21, ROWS - 26, 2 + 1, 10);
        addObstacle.addPolygon(scene,
                COLS - 19, ROWS - 32,
                COLS - 14, ROWS - 32, // X-Punkt +1
                COLS - 14, ROWS - 28  // X-Punkt +1
        );

        // Pool
        addObstacle.addRect(scene, 16, ROWS - 26, 15 + 1, 17, Color.GREY);
        addObstacle.addWater(scene, 17, ROWS - 25, 13 + 1, 15);

        // trees
        addObstacle.addTree(scene, 50, 8);  //
        addObstacle.addTree(scene, 54, 12); //
        addObstacle.addTree(scene, 48, 16); //
        addObstacle.addTree(scene, 43, 10); //
        addObstacle.addTree(scene, 45, 8); //
        addObstacle.addTree(scene, 46, 11); //
        addObstacle.addTree(scene, 54, 6); //
        addObstacle.addTree(scene, 56, 15); //
        addObstacle.addTree(scene, 44, 16); //
        //2x3 trees
        addObstacle.addTree(scene, 53, 22); //
        addObstacle.addTree(scene, 53, 26); //
        addObstacle.addTree(scene, 53, 30); //
        addObstacle.addTree(scene, 50, 22); //
        addObstacle.addTree(scene, 50, 26); //
        addObstacle.addTree(scene, 50, 30); //


        // Map Borders (Nur Breite +1)
        addObstacle.addWall(scene, 0, 0, COLS, 1);          // top
        addObstacle.addWall(scene, 0, ROWS - 1, COLS, 1);   // bottom
        addObstacle.addWall(scene, 0, 0, 1 + 1, ROWS);                 // left (Dicke 1 -> 2)
        addObstacle.addWall(scene, COLS -1, 0, 2, ROWS);   // right (Dicke 1 -> 2)
        // Debug grid
        addObstacle.showGrid(scene);

    }

    public int[][] getGrid() {return grid;}
    public Pane getPane(){ return container;}
    public List<Shape> getObstacles() {return addObstacle.getObstacleList();}
}