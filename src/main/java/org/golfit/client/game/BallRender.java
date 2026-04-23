package org.golfit.client.game;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.golfit.shared.constants.Constants;
import org.golfit.shared.constants.MapData;

/** Renders a golf ball as a JavaFX {@link Circle} on the game pane, scaling to grid size. */
public class BallRender {
    private Circle circle;
    private ImageView imageView;
    private static final double GRID_RADIUS = Constants.BALL_RADIUS;
    public Color ballColor = Color.BLUE;
    private Pane root;
    private double totalDistance = 0;

    private static final int FRAME_COUNT = 4;
    private static final Image[] FRAMES = loadFrames();

    /** Preloads all ball frame images once, shared across all instances. */
    private static Image[] loadFrames() {
        Image[] frames = new Image[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            try {
                var stream = BallRender.class.getResourceAsStream("/images/ball/ball_" + i + ".png");
                frames[i] = stream != null ? new Image(stream) : null;
            } catch (Exception e) {
                frames[i] = null;
            }
        }
        return frames;
    }

    /** Returns true if all frames loaded successfully. */
    private static boolean hasImages() {
        for (Image f : FRAMES) if (f == null) return false;
        return true;
    }

    /**
     * Creates and renders a ball on the given pane at the ball's position.
     *
     * @param root the parent pane to add the circle to
     * @param ball the ball model with initial pixel coordinates
     */
    public BallRender(Pane root, Ball ball) {
        this.root = root;
        double initialPixelRadius = calculatePixelRadius();

        // Circle is always kept in sync — used by getCircle() callers
        this.circle = new Circle(initialPixelRadius, ballColor);
        this.circle.setCenterX(ball.getPx());
        this.circle.setCenterY(ball.getPy());

        if (hasImages()) {
            imageView = new ImageView(FRAMES[0]);
            syncImageToCircle(initialPixelRadius, ball.getPx(), ball.getPy());

            // Re-sync once pane has a real width (avoids small-ball-on-start issue)
            root.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.getWidth() > 0) {
                    double radius = calculatePixelRadius();
                    circle.setRadius(radius);
                    syncImageToCircle(radius, ball.getPx(), ball.getPy());
                }
            });

            root.getChildren().add(imageView);
        } else {
            root.getChildren().add(circle);
        }
    }

    /**
     * Calculates how many pixels correspond to 0.5 grid units.
     */
    private double calculatePixelRadius() {
        if (root == null || root.getWidth() <= 0) {
            return (1100.0 / MapData.COLS) * GRID_RADIUS;
        }
        double scaleX = root.getWidth() / MapData.COLS;
        return scaleX * GRID_RADIUS;
    }

    /** Positions and sizes the ImageView to match the circle's center and radius. */
    private void syncImageToCircle(double radius, double centerX, double centerY) {
        double size = radius * 2;
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setX(centerX - radius);
        imageView.setY(centerY - radius);
    }

    /** Updates the circle's position and radius to match the ball's current pixel coordinates. */
    public void updatePosition(Ball ball) {
        double radius = calculatePixelRadius();

        this.circle.setRadius(radius);
        this.circle.setCenterX(ball.getPx());
        this.circle.setCenterY(ball.getPy());

        if (imageView != null) {
            double dx = ball.getPx() - (imageView.getX() + imageView.getFitWidth() / 2);
            double dy = ball.getPy() - (imageView.getY() + imageView.getFitHeight() / 2);
            totalDistance += Math.sqrt(dx * dx + dy * dy);

            // Swap frame based on distance traveled
            int frame = (int)(totalDistance / 4) % FRAME_COUNT;
            imageView.setImage(FRAMES[frame]);

            syncImageToCircle(radius, ball.getPx(), ball.getPy());
        }
    }

    /** Returns the current ball color. */
    public Color getColor() {
        return ballColor;
    }

    /** Changes the ball's fill color. */
    public void changeColor(Color newColor) {
        ballColor = newColor;
        if (imageView == null && this.circle != null) {
            this.circle.setFill(newColor);
        }
    }

    /** Returns the underlying JavaFX circle node. */
    public Circle getCircle() {
        return circle;
    }
}