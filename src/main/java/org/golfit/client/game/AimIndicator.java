package org.golfit.client.game;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * Draws a dynamic aim indicator showing ball power and direction.
 *
 * <p>When the player holds down the mouse to charge a stroke, this class renders
 * up to {@value #MAX_TILES} trapezoidal segments arranged in a cone that:
 * <ul>
 *   <li>Points in the direction the ball will travel,</li>
 *   <li>Is placed <em>behind</em> the ball along the shot vector,</li>
 *   <li>Grows in count and shifts from green to red as power increases.</li>
 * </ul>
 */
public class AimIndicator {

    public static final int MAX_TILES = 6;
    private static final double CONE_HALF_ANGLE = Math.toRadians(13.0);
    private static final double GAP_FROM_BALL = 6.0;
    private static final double SEGMENT_GAP = 4.0;
    private static final double SEGMENT_LENGTH = 12.0;
    private static final double TILE_OPACITY = 0.80;
    private static final double MIN_POWER_THRESHOLD = 0.03;

    private final Pane pane;
    private final Polygon[] tiles = new Polygon[MAX_TILES];

    public AimIndicator(Pane pane) {
        this.pane = pane;
        for (int i = 0; i < MAX_TILES; i++) {
            Polygon p = new Polygon();
            p.setOpacity(0);
            p.setMouseTransparent(true);
            p.setManaged(false);
            tiles[i] = p;
            pane.getChildren().add(0, p);
        }
    }

    public void update(double ballPx, double ballPy, double ballRadius, double angleRad, double ratio) {
        int activeTiles = (ratio < MIN_POWER_THRESHOLD) ? 0 : (int) Math.ceil(ratio * MAX_TILES);
        activeTiles = Math.min(activeTiles, MAX_TILES);

        double innerR = ballRadius + GAP_FROM_BALL;

        for (int i = 0; i < MAX_TILES; i++) {
            if (i >= activeTiles) {
                tiles[i].setOpacity(0);
                continue;
            }

            double rInner = innerR + i * (SEGMENT_LENGTH + SEGMENT_GAP);
            double rOuter = rInner + SEGMENT_LENGTH;
            double wInner = rInner * Math.tan(CONE_HALF_ANGLE);
            double wOuter = rOuter * Math.tan(CONE_HALF_ANGLE);

            double px = Math.cos(angleRad + Math.PI / 2.0);
            double py = Math.sin(angleRad + Math.PI / 2.0);
            double ax = Math.cos(angleRad);
            double ay = Math.sin(angleRad);

            if (i == 0) {
                tiles[i].getPoints().setAll(
                        ballPx + ax * rInner,
                        ballPy + ay * rInner,
                        ballPx + ax * rOuter - px * wOuter,
                        ballPy + ay * rOuter - py * wOuter,
                        ballPx + ax * rOuter + px * wOuter,
                        ballPy + ay * rOuter + py * wOuter
                );
            } else {
                tiles[i].getPoints().setAll(
                        ballPx + ax * rInner - px * wInner,
                        ballPy + ay * rInner - py * wInner,
                        ballPx + ax * rOuter - px * wOuter,
                        ballPy + ay * rOuter - py * wOuter,
                        ballPx + ax * rOuter + px * wOuter,
                        ballPy + ay * rOuter + py * wOuter,
                        ballPx + ax * rInner + px * wInner,
                        ballPy + ay * rInner + py * wInner
                );
            }

            double t = (MAX_TILES == 1) ? 0.0 : (double) i / (MAX_TILES - 1);
            Color color = Color.hsb(120.0 * (1.0 - t), 0.90, 1.0);
            tiles[i].setFill(color);
            tiles[i].setStroke(color.darker());
            tiles[i].setStrokeWidth(0.8);
            tiles[i].setOpacity(TILE_OPACITY);
        }
    }

    public void hide() {
        for (Polygon tile : tiles) tile.setOpacity(0);
    }

    public void detach() {
        pane.getChildren().removeAll(tiles);
    }
}
