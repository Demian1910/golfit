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

    public int[][] grid = new int[COLS][ROWS];

    public List<Shape> obstacles;
    AddObstacle addObstacle;


    public Course(Pane scene) {


        container = new Pane();
        addObstacle = new AddObstacle(scene, container);

        // Background
        addObstacle.addRect(scene, 0, 0, COURSE_COLS, ROWS, backgroundColor);

        // Map Borders
        addObstacle.addWall(scene, 0, 0, COURSE_COLS, 1);          // top
        addObstacle.addWall(scene, 0, ROWS - 1, COURSE_COLS, 1);   // bottom
        addObstacle.addWall(scene, 0, 0, 1, ROWS);                 // left
        addObstacle.addWall(scene, COURSE_COLS - 1, 0, 1, ROWS);   // right
        // Path 2 start
        addObstacle.addWall(scene, 1, 22, 3, 2);
        addObstacle.addWall(scene, 12, 22, 3, 2);
        addObstacle.addSand(scene, 7, 22, 2, 2);

        // Path 2 top left room
        addObstacle.addIce(scene, 1, 15, 3, 7);
        addObstacle.addIce(scene, 12, 15, 8, 7);
        addObstacle.addWall(scene, 1, 10, 7, 3);
        addObstacle.addIce(scene, 8, 1, 12, 12);
        // Trees between ice
        addObstacle.addTree(scene, 5, 18);
        addObstacle.addTree(scene, 9, 18);
        // Trees top left
        addObstacle.addTree(scene, 2, 4);
        addObstacle.addTree(scene, 6, 4);
        addObstacle.addTree(scene, 2, 8);
        addObstacle.addTree(scene, 6, 8);
        // Trees in ice
        addObstacle.addTree(scene, 12, 7);
        addObstacle.addTree(scene, 16, 7);

        // Path 2 barrier wall
        addObstacle.addWall(scene, 20, 5, 3, 20);
        addObstacle.addPolygon(scene,
                1, 15,
                1, 13,
                4, 13
        );

        // Path 2 room 2
        addObstacle.addWater(scene, 28, 5, 10, 10);
        addObstacle.addSand(scene, 28, 1, 10, 4);
        addObstacle.addIce(scene, 28, 15, 10, 4);
        addObstacle.addPolygon(scene,
                23, 17,
                23, 19,
                25, 19
        );
        // Golf area
        addObstacle.addIce(scene, 45, 1, 12, 6);
        addObstacle.addWater(scene, 58, 4, 4, 3);
        addObstacle.addGolfHole(scene, 51, 9);
        addObstacle.addPolygon(scene,
                62, 1,
                64, 1,
                64, 3
        );
        // Top right trees
        addObstacle.addTree(scene, 45, 10);
        addObstacle.addTree(scene, 42, 7);
        addObstacle.addTree(scene, 42, 12);
        addObstacle.addTree(scene, 41, 17);
        addObstacle.addTree(scene, 46, 15);

        // Golf wall
        addObstacle.addWall(scene, COURSE_COLS - 10, ROWS - 34, 2, 18);
        addObstacle.addWall(scene, COURSE_COLS - 15, ROWS - 34, 5, 2);
        addObstacle.addWall(scene, COURSE_COLS - 17, ROWS - 38, 2, 6);


        // Path 1
        addObstacle.addIce(scene, 30, ROWS - 21, 10, 20);
        addObstacle.addSand(scene, 1, ROWS - 4, 15, 3);
        addObstacle.addSand(scene, 16, ROWS - 7, 20, 6);
        addObstacle.addWall(scene, 40, ROWS - 11, 3, 10);


        // Path 1 ice-sand stripe
        addObstacle.addIce(scene, COURSE_COLS - 10, ROWS - 20, 9, 2);
        addObstacle.addSand(scene, COURSE_COLS - 10, ROWS - 22, 9, 2);
        addObstacle.addIce(scene, COURSE_COLS - 10, ROWS - 24, 9, 2);
        addObstacle.addSand(scene, COURSE_COLS - 10, ROWS - 26, 9, 2);
        addObstacle.addIce(scene, COURSE_COLS - 10, ROWS - 28, 9, 2);
        addObstacle.addSand(scene, COURSE_COLS - 10, ROWS - 30, 9, 2);
        addObstacle.addIce(scene, COURSE_COLS - 10, ROWS - 32, 9, 2);
        addObstacle.addSand(scene, COURSE_COLS - 10, ROWS - 34, 9, 2);

        // Path 1 lever-wall
        addObstacle.addWall(scene, 10, ROWS - 9, 26, 2);
        addObstacle.addWall(scene, 15, ROWS - 9, 2, 5);
        addObstacle.addSwitch(scene, 12, ROWS - 6);

        // Path 1 lever-wall 2
        addObstacle.addWall(scene, 30, ROWS - 26, 10, 2);
        addObstacle.addWall(scene, 30, ROWS - 26, 2, 5);
        addObstacle.addSwitch(scene, 33, ROWS - 23);

        // Path 1.1 ice
        addObstacle.addIce(scene, 43, ROWS - 11, 14, 10);
        addObstacle.addWater(scene, 43, ROWS - 9, 12, 8);
        addObstacle.addPolygon(scene,
                57, ROWS - 1,
                57, ROWS - 4,
                64, ROWS - 11,
                64, ROWS - 1
        );

        // Path 1.2
        addObstacle.addBall(scene, 40, ROWS - 14);
        addObstacle.addBall(scene, 40, ROWS - 17);
        addObstacle.addBall(scene, 40, ROWS - 20);

        addObstacle.addIce(scene, COURSE_COLS - 15, ROWS - 26, 5, 10);
        addObstacle.addWall(scene, COURSE_COLS - 17, ROWS - 26, 2, 10);
        addObstacle.addPolygon(scene,
                COURSE_COLS - 13, ROWS - 32,
                COURSE_COLS - 10, ROWS - 32,
                COURSE_COLS - 10, ROWS - 29
        );

        addObstacle.addRect(scene, 15, ROWS - 26, 15, 19, Color.GREY);
        addObstacle.addWater(scene, 16, ROWS - 25, 13, 17);


        // GUI Box on right side
        addObstacle.addRect(scene, 65, 0, 15, ROWS, Color.BLUE);

        // White rects for text
        addObstacle.addRect(scene, 66, 1, 13, 2, Color.WHITE); // strokes
        addObstacle.addRect(scene, 66, 4, 13, 12, Color.WHITE); // leaderboard
        addObstacle.addRect(scene, 66, 17, 13, 20, Color.WHITE); // chat
        addObstacle.addRect(scene, 66, 38, 13, 2, Color.WHITE); // enter text
        addObstacle.addRect(scene, 66, 42, 10, 2, Color.WHITE); // time
        addObstacle.addRect(scene, 77, 42, 2, 2, Color.WHITE); // settings
        obstacles = addObstacle.getObstacleList();
        // Debug grid
        addObstacle.showGrid(scene);
    }




    public int[][] getGrid() {return grid;}

    public Pane getPane(){ return container;}


    public List<Shape> getObstacles() {return obstacles;}
}
