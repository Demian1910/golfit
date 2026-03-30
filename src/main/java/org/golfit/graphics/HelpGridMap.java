package org.golfit.graphics;

import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.golfit.shared.constants.MapData;

import java.util.ArrayList;
import java.util.List;

public class HelpGridMap {
    private static final int EMPTY = 0;
    private static final int COLS = 80;
    private static final int ROWS = 45;

    private List<Shape> obstacles;
    private int[][] grid;    // Für Terrain (SERVER_MAP[45][80])
    private int[][] idGrid;  // Für IDs (SHAPE_ID_MAP[45][80])
    private Pane container;
    private List<MapData.ServerShape> serverShapeList = MapData.LEVEL_1_OBSTACLES;

    public HelpGridMap(Course course) {
        this.container = course.getPane();
        this.obstacles = course.getObstacles();
        // Initialisierung passend zu deiner MapData Struktur [row][col]
        this.grid = new int[ROWS][COLS];
        this.idGrid = new int[ROWS][COLS];
    }

    public void updateGrids() {
        if (container.getWidth() <= 0 || container.getHeight() <= 0) return;

        double cellW = container.getWidth() / COLS;
        double cellH = container.getHeight() / ROWS;

        // Grids leeren
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = EMPTY;
                idGrid[r][c] = -1;
            }
        }

        for (int i = 0; i < obstacles.size(); i++) {
            Shape s = obstacles.get(i);
            Integer type = (Integer) s.getUserData();
            if (type == null) continue;

            Bounds b = s.getBoundsInParent();

            // Sicherheits-Begrenzung (Clamping), um Index 45 zu verhindern
            int startCol = (int) Math.max(0, Math.floor(b.getMinX() / cellW));
            int endCol   = (int) Math.min(COLS - 1, (int) Math.floor((b.getMaxX() - 0.01) / cellW));
            int startRow = (int) Math.max(0, Math.floor(b.getMinY() / cellH));
            int endRow   = (int) Math.min(ROWS - 1, (int) Math.floor((b.getMaxY() - 0.01) / cellH));

            for (int r = startRow; r <= endRow; r++) {
                for (int c = startCol; c <= endCol; c++) {
                    double px = (c + 0.5) * cellW;
                    double py = (r + 0.5) * cellH;

                    // Nutzt sceneToLocal für skalierte Polygone
                    if (s.contains(s.sceneToLocal(px, py))) {
                        grid[r][c] = type;
                        idGrid[r][c] = i; // i ist der Index in der Liste
                    }
                }
            }
        }
    }

    public void debugViewGrid() {
        container.getChildren().removeIf(node -> "DEBUG_GRID".equals(node.getUserData()));
        double cellW = container.getWidth() / COLS;
        double cellH = container.getHeight() / ROWS;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] != EMPTY) {
                    Rectangle rect = new Rectangle(c * cellW, r * cellH, cellW, cellH);
                    rect.setFill(Color.rgb(0, 0, 0, 0.6));
                    rect.setStroke(Color.WHITE);
                    rect.setStrokeWidth(0.5);
                    rect.setMouseTransparent(true);
                    rect.setUserData("DEBUG_GRID");
                    container.getChildren().add(rect);
                }
            }
        }
    }

    public void printServerData() {
        double cellW = container.getWidth() / COLS;
        double cellH = container.getHeight() / ROWS;

        // 1. Export der LEVEL_1_OBSTACLES Liste
        System.out.println("public static final List<ServerShape> LEVEL_1_OBSTACLES = List.of(");
        for (int i = 0; i < obstacles.size(); i++) {
            Shape s = obstacles.get(i);
            int terrain = (int) s.getUserData();
            String line = "";

            if (s instanceof Rectangle r) {
                line = String.format("    new ServerShape(%d, %d, ServerShape.RECT, %.4f, %.4f, %.4f, %.4f)",
                        i, terrain, r.getX() / cellW, r.getY() / cellH, r.getWidth() / cellW, r.getHeight() / cellH);
            } else if (s instanceof Ellipse e) {
                double x = (e.getCenterX() - e.getRadiusX()) / cellW;
                double y = (e.getCenterY() - e.getRadiusY()) / cellH;
                double w = (e.getRadiusX() * 2) / cellW;
                double h = (e.getRadiusY() * 2) / cellH;
                line = String.format("    new ServerShape(%d, %d, ServerShape.ELLIPSE, %.4f, %.4f, %.4f, %.4f)",
                        i, terrain, x, y, w, h);
            } else if (s instanceof Polygon p) {
                StringBuilder pts = new StringBuilder("new double[]{");
                for (int j = 0; j < p.getPoints().size(); j++) {
                    pts.append(String.format("%.2f", p.getPoints().get(j)));
                    if (j < p.getPoints().size() - 1) pts.append(", ");
                }
                pts.append("}");
                line = String.format("    new ServerShape(%d, %d, %s)", i, terrain, pts.toString());
            }
            System.out.print(line + (i < obstacles.size() - 1 ? ",\n" : "\n"));
        }
        System.out.println(");\n");

        // 2. Grids drucken [r][c]
        printGridExport("SERVER_MAP", grid);
        printGridExport("SHAPE_ID_MAP", idGrid);
    }

    private void printGridExport(String varName, int[][] targetGrid) {
        System.out.println("public static final int[][] " + varName + " = {");
        for (int r = 0; r < ROWS; r++) {
            System.out.print("    {");
            for (int c = 0; c < COLS; c++) {
                System.out.print(targetGrid[r][c] + (c < COLS - 1 ? ", " : ""));
            }
            System.out.println("}" + (r < ROWS - 1 ? "," : ""));
        }
        System.out.println("};");
    }
    public void verifyIdMapping() {
        // 1. Alte Test-Elemente (Kacheln und Text) entfernen
        container.getChildren().removeIf(node -> "ID_VERIFY".equals(node.getUserData()));

        double cellW = container.getWidth() / COLS;
        double cellH = container.getHeight() / ROWS;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int id = idGrid[r][c];

                if (id != -1) {
                    // 2. Hintergrund-Rechteck (Farbe pro ID)
                    Rectangle debugTile = new Rectangle(c * cellW, r * cellH, cellW, cellH);
                    int colorHash = Integer.hashCode(id) * 0x45d9f3b;
                    debugTile.setFill(Color.hsb((colorHash % 360 + 360) % 360, 0.5, 0.9, 0.3));
                    debugTile.setStroke(Color.web("white", 0.2));
                    debugTile.setStrokeWidth(0.5);
                    debugTile.setUserData("ID_VERIFY");
                    debugTile.setMouseTransparent(true);
                    container.getChildren().add(debugTile);

                    // 3. Text-ID direkt in die Zelle
                    javafx.scene.text.Text t = new javafx.scene.text.Text(String.valueOf(id));
                    // Positionierung: Mitte der Zelle
                    t.setX(c * cellW + (cellW / 4));
                    t.setY(r * cellH + (cellH / 1.5));

                    t.setFont(new javafx.scene.text.Font("Arial", 7)); // Sehr klein
                    t.setFill(Color.WHITE);
                    t.setUserData("ID_VERIFY");
                    t.setMouseTransparent(true);
                    t.setManaged(false); // Wichtig für Performance!

                    container.getChildren().add(t);
                }
            }
        }
    }
    public void setupResizing() {
        // Sobald sich die Breite oder Höhe des Panes ändert, Grid neu berechnen
        container.widthProperty().addListener((obs, oldVal, newVal) -> updateGrids());
        container.heightProperty().addListener((obs, oldVal, newVal) -> updateGrids());
    }
}