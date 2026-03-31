package org.golfit.graphics;

import javafx.geometry.Bounds;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.golfit.shared.constants.MapData;
import java.util.List;

public class HelpGridMap {
    private static final int EMPTY = 0;
    private static final int COLS = 80;
    private static final int ROWS = 45;

    private List<Shape> obstacles;
    private int[][] grid;
    private int[][] idGrid;
    private Pane container;

    public HelpGridMap(Course course) {
        this.container = course.getPane();
        this.obstacles = course.getObstacles();
        this.grid = new int[ROWS][COLS];
        this.idGrid = new int[ROWS][COLS];
    }

    public void updateGrids() {
        double w = container.getWidth();
        double h = container.getHeight();
        if (w <= 0 || h <= 0) return;

        double cellW = w / (double)COLS;
        double cellH = h / (double)ROWS;

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

            // Wir bestimmen den Bereich im Grid basierend auf den Pixel-Bounds
            int startCol = (int) Math.floor((b.getMinX() + 0.1) / cellW);
            int endCol   = (int) Math.floor((b.getMaxX() - 0.1) / cellW);
            int startRow = (int) Math.floor((b.getMinY() + 0.1) / cellH);
            int endRow   = (int) Math.floor((b.getMaxY() - 0.1) / cellH);

            for (int r = startRow; r <= endRow; r++) {
                for (int c = startCol; c <= endCol; c++) {
                    // Punkt-Test in der exakten Mitte der Grid-Zelle
                    double px = (c + 0.5) * cellW;
                    double py = (r + 0.5) * cellH;

                    if (s.contains(s.parentToLocal(px, py))) {
                        grid[r][c] = type;
                        idGrid[r][c] = i;
                    }
                }
            }
        }
    }
    public void debugViewGrid() {
        container.getChildren().removeIf(node -> "DEBUG_GRID".equals(node.getUserData()));

        double cellW = container.getWidth() / (double)COLS;
        double cellH = container.getHeight() / (double)ROWS;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] != EMPTY) {
                    Rectangle rect = new Rectangle(c * cellW, r * cellH, cellW, cellH);
                    rect.setStroke(Color.WHITE);
                    rect.setStrokeWidth(0.5);
                    rect.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);
                    rect.setFill(Color.rgb(0, 0, 0, 0.5));
                    rect.setMouseTransparent(true);
                    rect.setUserData("DEBUG_GRID");
                    container.getChildren().add(rect);
                }
            }
        }
    }

    public void verifyIdMapping() {
        // 1. Alte Elemente entfernen
        container.getChildren().removeIf(node -> "ID_VERIFY".equals(node.getUserData()));

        double w = container.getWidth();
        double h = container.getHeight();
        if (w <= 0 || h <= 0) return;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int id = idGrid[r][c];

                if (id != -1) {
                    // 2. Pixel-Grenzen exakt berechnen (kein schleichender Versatz)
                    double x1 = Math.round(c * w / (double)COLS);
                    double x2 = Math.round((c + 1) * w / (double)COLS);
                    double y1 = Math.round(r * h / (double)ROWS);
                    double y2 = Math.round((r + 1) * h / (double)ROWS);

                    // Das Rechteck füllt jetzt exakt den Raum zwischen den Pixel-Kanten
                    Rectangle debugTile = new Rectangle(x1, y1, x2 - x1, y2 - y1);

                    // Stroke nach innen, damit die 0.5px Breite nicht nach außen addiert wird
                    debugTile.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);
                    debugTile.setStroke(Color.web("white", 0.3));
                    debugTile.setStrokeWidth(0.5);

                    // Farbe basierend auf ID
                    int colorHash = Integer.hashCode(id) * 0x45d9f3b;
                    debugTile.setFill(Color.hsb((colorHash % 360 + 360) % 360, 0.5, 0.9, 0.4));

                    debugTile.setUserData("ID_VERIFY");
                    debugTile.setMouseTransparent(true);
                    container.getChildren().add(debugTile);

                    // 3. Text-ID positionieren
                    Text t = new Text(String.valueOf(id));
                    // Position innerhalb der gerundeten Zelle
                    t.setX(x1 + (x2 - x1) * 0.1);
                    t.setY(y1 + (y2 - y1) * 0.8);

                    t.setFont(new Font("Arial", 7));
                    t.setFill(Color.WHITE);
                    t.setUserData("ID_VERIFY");
                    t.setMouseTransparent(true);
                    t.setManaged(false);
                    container.getChildren().add(t);
                }
            }
        }
    }
    public void printServerData() {
        double cellW = container.getWidth() / (double)COLS;
        double cellH = container.getHeight() / (double)ROWS;

        System.out.println("public static final List<ServerShape> LEVEL_1_OBSTACLES = List.of(");
        for (int i = 0; i < obstacles.size(); i++) {
            Shape s = obstacles.get(i);
            int terrain = (s.getUserData() instanceof Integer) ? (int) s.getUserData() : 0;
            String line = "";

            if (s instanceof Rectangle r) {
                line = String.format("    new ServerShape(%d, %d, ServerShape.RECT, %.4f, %.4f, %.4f, %.4f)",
                        i, terrain, r.getX() / cellW, r.getY() / cellH, r.getWidth() / cellW, r.getHeight() / cellH);
            } else if (s instanceof Ellipse e) {
                line = String.format("    new ServerShape(%d, %d, ServerShape.ELLIPSE, %.4f, %.4f, %.4f, %.4f)",
                        i, terrain, (e.getCenterX()-e.getRadiusX())/cellW, (e.getCenterY()-e.getRadiusY())/cellH, (e.getRadiusX()*2)/cellW, (e.getRadiusY()*2)/cellH);
            } else if (s instanceof Polygon p) {
                StringBuilder pts = new StringBuilder("new double[]{");
                for (int j = 0; j < p.getPoints().size(); j++) {
                    double val = p.getPoints().get(j) / (j % 2 == 0 ? cellW : cellH);
                    pts.append(String.format("%.4f", val));
                    if (j < p.getPoints().size() - 1) pts.append(", ");
                }
                pts.append("}");
                line = String.format("    new ServerShape(%d, %d, ServerShape.POLYGON, %s)", i, terrain, pts.toString());
            }
            System.out.print(line + (i < obstacles.size() - 1 ? ",\n" : "\n"));
        }
        System.out.println(");\n");

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
}