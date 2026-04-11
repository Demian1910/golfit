package org.golfit.client.game;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import org.golfit.shared.constants.Constants;
import org.golfit.shared.constants.MapData;

/** Renders a golf ball as a JavaFX {@link Circle} on the game pane, scaling to grid size. */
public class BallRender {
    private Circle circle;
    // Der logische Radius im Grid (0.5 = halbe Zelle)
    private static final double GRID_RADIUS = Constants.BALL_RADIUS;
    public Color ballColor = Color.BLUE;
    private Pane root;

    /**
     * Creates and renders a ball on the given pane at the ball's position.
     *
     * @param root the parent pane to add the circle to
     * @param ball the ball model with initial pixel coordinates
     */
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
     * Calculates how many pixels correspond to 0.5 grid units.
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

    /** Updates the circle's position and radius to match the ball's current pixel coordinates. */
    public void updatePosition(Ball ball) {
        // If the window was resized, we adjust the radius
        this.circle.setRadius(calculatePixelRadius());

        this.circle.setCenterX(ball.getPx());
        this.circle.setCenterY(ball.getPy());
    }

    /** Returns the current ball color. */
    public Color getColor() {
        return ballColor;
    }

    /** Changes the ball's fill color. */
    public void changeColor(Color newColor) {
        ballColor = newColor;
        if (this.circle != null) {
            this.circle.setFill(newColor);
        }
    }

    /** Returns the underlying JavaFX circle node. */
    public Circle getCircle() {
        return circle;
    }
}