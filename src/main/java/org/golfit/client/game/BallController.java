package org.golfit.client.game;

import org.golfit.client.game.physics.Ball;
import org.golfit.shared.constants.Constants;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * Wires mouse events → physics → rendering loop.
 *
 * Usage:
 * BallController controller = new BallController(pane, 100, 100, Color.BLUE);
 * controller.start();
 */
public class BallController {

    /**
     * Converts drag distance to initial ball speed. Unit: (px/s) per px of drag.
     */
    private static final double STROKE_SCALE = 3.0;

    /**
     * Current terrain friction — changes when the ball moves to a different
     * surface.
     */
    private double currentFriction = Constants.GRASS_FRICTION;

    private final Ball ball;
    private final BallRender ballRender;
    private final Pane root;
    private AnimationTimer gameLoop;

    // Drag tracking
    private double dragStartX;
    private double dragStartY;

    public BallController(Pane root, double initialX, double initialY, Color color) {
        this.root = root;
        this.ball = new Ball(initialX, initialY);
        this.ballRender = new BallRender(root, ball, color);

        registerMouseEvents();
        buildGameLoop();
    }

    // ── Public API ──────────────────────────────────────────────────────────────

    /** Starts the physics + rendering loop. */
    public void start() {
        gameLoop.start();
    }

    /** Stops the physics + rendering loop. */
    public void stop() {
        gameLoop.stop();
    }

    /** Call this when the ball enters a new terrain type (e.g. sand, grass). */
    public void setTerrain(double friction) {
        this.currentFriction = friction;
    }

    private void registerMouseEvents() {
        // Record where the drag started
        root.setOnMousePressed(e -> {
            dragStartX = e.getX();
            dragStartY = e.getY();
        });

        // On release: direction is opposite to drag (like a real golf stroke)
        root.setOnMouseReleased(e -> {
            double dx = dragStartX - e.getX();
            double dy = dragStartY - e.getY();
            ball.stroke(dx * STROKE_SCALE, dy * STROKE_SCALE);
        });
    }

    /**
     * Initializes the Game Loop.
     * Every frame: calculates elapsed time, updates the ball's physical position,
     * and synchronizes the circle rendering on the UI.
     */
    private void buildGameLoop() {
        long[] lastTime = { -1 };

        // AnimationTimer is synchronized with the refresh rate of the screen
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long nowNanos) {
                if (lastTime[0] < 0) {
                    lastTime[0] = nowNanos;
                    return;
                }

                // refresh rate does not have always the same value
                double deltaTime = (nowNanos - lastTime[0]) / 1_000_000_000.0;
                lastTime[0] = nowNanos;

                ball.update(deltaTime, currentFriction);
                ballRender.updatePosition(ball);
            }
        };
    }
}
