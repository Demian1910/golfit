package org.golfit.client.game;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import org.golfit.shared.constants.Constants;
import org.golfit.shared.constants.MapData;

/**
 * Handles the visual representation of a golf ball in the JavaFX client.
 * <p>
 * This class translates the ball's logical coordinates and radius into
 * screen-space pixels, ensuring the ball scales correctly when the
 * game window is resized.
 * </p>
 */
public class BallRender {
    /** The JavaFX Shape used to draw the ball on screen. */
    private Circle circle;

    /** The logical radius of the ball in grid units (defined in shared constants). */
    private static final double GRID_RADIUS = Constants.BALL_RADIUS;

    /** The current fill color of the ball. */
    public Color ballColor = Color.BLUE;

    /** The parent container where the ball is rendered. */
    private Pane root;

    /**
     * Constructs a new BallRender and adds it to the specified Pane.
     * * @param root The JavaFX {@link Pane} where the ball should be drawn.
     * @param ball The logical {@link Ball} object containing position data.
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
     * Calculates the pixel radius equivalent of the logical {@code GRID_RADIUS}.
     * <p>
     * It determines the scale by dividing the current pane width by the
     * total number of columns in the map data.
     * </p>
     * * @return The radius in pixels for the current window size.
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

    /**
     * Synchronizes the visual circle with the logical ball's state.
     * <p>
     * This should be called during every frame update. It updates both the
     * center coordinates and the radius (to account for window resizing).
     * </p>
     * * @param ball The logical {@link Ball} providing the new coordinates.
     */
    public void updatePosition(Ball ball) {
        // If the window was resized, we adjust the radius
        this.circle.setRadius(calculatePixelRadius());

        this.circle.setCenterX(ball.getPx());
        this.circle.setCenterY(ball.getPy());
    }

    /**
     * @return The current {@link Color} assigned to this ball.
     */
    public Color getColor() {
        return ballColor;
    }

    /**
     * Updates the ball's color and immediately applies it to the visual circle.
     * * @param newColor The new {@link Color} to apply.
     */
    public void changeColor(Color newColor) {
        ballColor = newColor;
        if (this.circle != null) {
            this.circle.setFill(newColor);
        }
    }

    /**
     * Provides access to the underlying JavaFX Circle.
     * * @return The {@link Circle} object.
     */
    public Circle getCircle() {
        return circle;
    }
}