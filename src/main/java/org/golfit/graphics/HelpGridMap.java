package org.golfit.graphics;

import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

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

    /**
     * Rasterizes the course.
     * Uses int[] userData: [0] = Terrain Type, [1] = Partner ID pointer.
     */
    public void updateGrids() {
        double w = container.getWidth();
        double h = container.getHeight();
        if (w <= 0 || h <= 0) return;

        double cellW = w / (double) COLS;
        double cellH = h / (double) ROWS;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = EMPTY;
                idGrid[r][c] = -1;
            }
        }

        for (int i = 0; i < obstacles.size(); i++) {
            Shape s = obstacles.get(i);
            Object userData = s.getUserData();

            // Extract terrain type from the first index of the array
            if (!(userData instanceof int[] data)) continue;
            int type = data[0];

            Bounds b = s.getBoundsInParent();

            int startCol = (int) Math.floor((b.getMinX() + 0.1) / cellW);
            int endCol = (int) Math.floor((b.getMaxX() - 0.1) / cellW);
            int startRow = (int) Math.floor((b.getMinY() + 0.1) / cellH);
            int endRow = (int) Math.floor((b.getMaxY() - 0.1) / cellH);

            for (int r = startRow; r <= endRow; r++) {
                if (r < 0 || r >= ROWS) continue;
                for (int c = startCol; c <= endCol; c++) {
                    if (c < 0 || c >= COLS) continue;

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

    /**
     * Prints the Java code for the Server.
     * Note: cellW and cellH are forced to 1100/600 for export consistency.
     */
    public void printServerData() {
        double cellW = 1100.0 / (double) COLS;
        double cellH = 600.0 / (double) ROWS;

        System.out.println("// --- COPY BELOW INTO MapData.java ---");
        System.out.println("public static final List<ServerShape> LEVEL_1_OBSTACLES = List.of(");

        for (int i = 0; i < obstacles.size(); i++) {
            Shape s = obstacles.get(i);
            int[] data = (int[]) s.getUserData();
            int terrain = data[0];
            int partner = data[1];

            String line = "";
            if (s instanceof Rectangle r) {
                line = String.format("    new ServerShape(%d, %d, %d, ServerShape.RECT, %.4f, %.4f, %.4f, %.4f)",
                        i, terrain, partner, r.getX() / cellW, r.getY() / cellH, r.getWidth() / cellW, r.getHeight() / cellH);
            } else if (s instanceof Ellipse e) {
                line = String.format("    new ServerShape(%d, %d, %d, ServerShape.ELLIPSE, %.4f, %.4f, %.4f, %.4f)",
                        i, terrain, partner, (e.getCenterX() - e.getRadiusX()) / cellW, (e.getCenterY() - e.getRadiusY()) / cellH, (e.getRadiusX() * 2) / cellW, (e.getRadiusY() * 2) / cellH);
            } else if (s instanceof Polygon p) {
                StringBuilder pts = new StringBuilder("new double[]{");
                for (int j = 0; j < p.getPoints().size(); j++) {
                    double val = p.getPoints().get(j) / (j % 2 == 0 ? cellW : cellH);
                    pts.append(String.format("%.4f", val));
                    if (j < p.getPoints().size() - 1) pts.append(", ");
                }
                pts.append("}");
                line = String.format("    new ServerShape(%d, %d, %d, ServerShape.POLYGON, %s)", i, terrain, partner, pts.toString());
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

    /** Visual Debuggers **/
    public void verifyIdMapping() {
        container.getChildren().removeIf(node -> "ID_VERIFY".equals(node.getUserData()));
        double w = container.getWidth();
        double h = container.getHeight();
        if (w <= 0 || h <= 0) return;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int id = idGrid[r][c];
                if (id != -1) {
                    Rectangle debugTile = new Rectangle(c * (w/COLS), r * (h/ROWS), w/COLS, h/ROWS);
                    debugTile.setFill(Color.hsb((Integer.hashCode(id) % 360 + 360) % 360, 0.5, 0.9, 0.4));
                    debugTile.setUserData("ID_VERIFY");
                    debugTile.setMouseTransparent(true);
                    container.getChildren().add(debugTile);
                }
            }
        }
    }
}