package org.golfit.graphics;

import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import org.golfit.shared.constants.MapData;

import java.util.ArrayList;
import java.util.List;

public class HelpGridMap {
    public List<Shape> obstacles = new ArrayList<>();
    List<MapData.ServerShape> serverShapeList = MapData.LEVEL_1_OBSTACLES;
    private static final int EMPTY = 0;
    private static final int WALL  = 1;
    private static final int SAND  = 2;
    private static final int ICE   = 3;
    private static final int WATER = 4;
    private static final int BOUNCE = 5;
    private static final int SWITCH = 6;
    private static final int COLS = 80;
    private static final int ROWS = 45;
    private static final int COURSE_COLS = 65;
    int[][] grid;
    Pane container;
    Course course;

    public HelpGridMap(Course course ){
        this.container = course.getPane();
        this.course = course;
        this.obstacles = course.getObstacles();
        this.grid = MapData.SERVER_MAP;
    }

    public void updateGridFromList() {
        if (container.getWidth() <= 0 || container.getHeight() <= 0) return;

        double cellW = container.getWidth() / COLS;
        double cellH = container.getHeight() / ROWS;

        // 1. Grid leeren
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                grid[i][j] = EMPTY;
            }
        }

        // 2. Shapes prüfen
        for (Shape s : obstacles) {
            Object userData = s.getUserData();
            if (!(userData instanceof Integer)) continue;
            int type = (int) userData;

            Bounds b = s.getBoundsInParent();

            // Grobe Vorauswahl der betroffenen Zellen
            int startCol = (int) Math.max(0, Math.floor(b.getMinX() / cellW));
            int endCol   = (int) Math.min(COLS - 1, Math.floor(b.getMaxX() / cellW));
            int startRow = (int) Math.max(0, Math.floor(b.getMinY() / cellH));
            int endRow   = (int) Math.min(ROWS - 1, Math.floor(b.getMaxY() / cellH));

            for (int c = startCol; c <= endCol; c++) {
                for (int r = startRow; r <= endRow; r++) {
                    // Mittelpunkt der Gitterzelle in Pixeln
                    double px = (c + 0.5) * cellW;
                    double py = (r + 0.5) * cellH;

                    // WICHTIG: sceneToLocal konvertiert den Pixel-Punkt in das
                    // lokale System des Shapes (inklusive Scale-Transformation)
                    if (s.contains(s.sceneToLocal(px, py))) {
                        grid[c][r] = type;
                    }
                }
            }
        }
    }
    public void debugViewGrid() {
        double cellW = container.getWidth() / COLS;
        double cellH = container.getHeight() / ROWS;

        for (int c = 0; c < COLS-1; c++) {
            for (int r = 0; r < 45; r++) {
                // Innerhalb der Schleifen in toggleDebugView:
                if (grid[r][c] != EMPTY) {
                    // Quadrat erstellen
                    Rectangle rect = new Rectangle(c * cellW, r * cellH, cellW, cellH);

                    // Alles Schwarz (mit leichter Transparenz, damit man das Original noch sieht)
                    rect.setFill(Color.rgb(0, 0, 0, 0.8));

                    // Weißer Rahmen, um die Gitterzellen zu trennen
                    rect.setStroke(Color.WHITE);
                    rect.setStrokeWidth(0.5);

                    rect.setMouseTransparent(true);

                    container.getChildren().add(rect);

                }
            }
        }
    }
    public void printHardcodedArray() {
        System.out.println("public static final int[][] SERVER_MAP = {");
        for (int r = 0; r < ROWS; r++) {
            System.out.print("    {");
            for (int c = 0; c < COLS; c++) {
                System.out.print(grid[c][r] + (c < COLS - 1 ? ", " : ""));
            }
            System.out.println("}" + (r < ROWS - 1 ? "," : ""));
        }
        System.out.println("};");
    }

    public void printServerShapes() {
        double cellW = container.getWidth() / COLS;
        double cellH = container.getHeight() / ROWS;

        System.out.println("public static final List<ServerShape> LEVEL_1_OBSTACLES = List.of(");

        for (int i = 0; i < obstacles.size(); i++) {
            Shape s = obstacles.get(i);
            int terrain = (int) s.getUserData();
            String line = "";

            if (s instanceof Rectangle r) {
                // Rechtecke nutzen X/Y Properties -> müssen normalisiert werden
                line = String.format("    new ServerShape(%d, ServerShape.RECT, %.4f, %.4f, %.4f, %.4f)",
                        terrain, r.getX() / cellW, r.getY() / cellH, r.getWidth() / cellW, r.getHeight() / cellH);
            }
            else if (s instanceof Ellipse e) {
                double x = (e.getCenterX() - e.getRadiusX()) / cellW;
                double y = (e.getCenterY() - e.getRadiusY()) / cellH;
                double w = (e.getRadiusX() * 2) / cellW;
                double h = (e.getRadiusY() * 2) / cellH;
                line = String.format("    new ServerShape(%d, ServerShape.ELLIPSE, %.4f, %.4f, %.4f, %.4f)",
                        terrain, x, y, w, h);
            }
            else if (s instanceof Polygon p) {
                StringBuilder pts = new StringBuilder("new double[]{");
                for (int j = 0; j < p.getPoints().size(); j++) {
                    // Polygon-Punkte sind bei dir bereits im Grid-Format (z.B. 1.0, 15.0)
                    // Daher hier KEINE Division mehr nötig!
                    double val = p.getPoints().get(j);
                    pts.append(String.format("%.2f", val));
                    if (j < p.getPoints().size() - 1) pts.append(", ");
                }
                pts.append("}");
                line = String.format("    new ServerShape(%d, ServerShape.POLYGON, %s)",
                        terrain, pts.toString());
            }

            if (!line.isEmpty()) {
                System.out.print(line + (i < obstacles.size() - 1 ? ",\n" : "\n"));
            }
        }
        System.out.println(");");
    }

    public void verifyServerData() {
        // Alte rote Testrahmen entfernen
        container.getChildren().removeIf(node -> "VERIFY".equals(node.getUserData()));

        double cellW = container.getWidth() / COLS;
        double cellH = container.getHeight() / ROWS;

        for (MapData.ServerShape ss : serverShapeList) {
            Shape debugShape;
            if (ss.formType == MapData.ServerShape.RECT || ss.formType == MapData.ServerShape.ELLIPSE) {
                Rectangle r = new Rectangle(ss.x * cellW, ss.y * cellH, ss.w * cellW, ss.h * cellH);
                if (ss.formType == MapData.ServerShape.ELLIPSE) {
                    r.setArcWidth(ss.w * cellW);
                    r.setArcHeight(ss.h * cellH);
                }
                debugShape = r;
            } else {
                double[] scaledPoints = new double[ss.points.length];
                for (int i = 0; i < ss.points.length; i++) {
                    scaledPoints[i] = ss.points[i] * (i % 2 == 0 ? cellW : cellH);
                }
                debugShape = new Polygon(scaledPoints);
            }

            debugShape.setStroke(Color.RED);
            debugShape.setStrokeWidth(1.5);
            debugShape.getStrokeDashArray().addAll(5.0, 5.0); // Gestrichelt für bessere Sichtbarkeit
            debugShape.setFill(Color.rgb(255, 0, 0, 0.15));
            debugShape.setMouseTransparent(true);
            debugShape.setUserData("VERIFY");
            container.getChildren().add(debugShape);
        }
    }

}
