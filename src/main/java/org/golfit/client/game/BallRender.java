package org.golfit.client.game;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import org.golfit.shared.constants.Constants;
import org.golfit.shared.constants.MapData;

public class BallRender {
    private Circle circle;
    // The logical radius in the grid (0.5 = half cell)
    private static final double GRID_RADIUS = Constants.BALL_RADIUS;
    public Color ballColor = Color.BLUE;
    private Pane root;

    public BallRender(Pane root, Ball ball) {
        this.root = root;
        // Initial radius is calculated based on the current pane size
        double initialPixelRadius = calculatePixelRadius();

        this.circle = new Circle(initialPixelRadius, ballColor);
        this.circle.setCenterX(ball.getPx());
        this.circle.setCenterY(ball.getPy());

        root.getChildren().add(this.circle);
    }

    /**
     * Berechnet, wie viele Pixel 0.5 Grid-Einheiten entsprechen.
     */
    private double calculatePixelRadius() {
        if (root == null || root.getWidth() <= 0) {
            // Fallback to standard size (1100 / 80 * 0.5)
            return (1100.0 / MapData.COLS) * GRID_RADIUS;
        }
        // We take the X-scaling as the basis for the radius
        double scaleX = root.getWidth() / MapData.COLS;
        return scaleX * GRID_RADIUS;
    }

    public void updatePosition(Ball ball) {
        // If the window was resized, we adjust the radius
        this.circle.setRadius(calculatePixelRadius());

        this.circle.setCenterX(ball.getPx());
        this.circle.setCenterY(ball.getPy());
    }

    public Color getColor() {
        return ballColor;
    }

    public void changeColor(Color newColor) {
        ballColor = newColor;
        if (this.circle != null) {
            this.circle.setFill(newColor);
        }
    }
}