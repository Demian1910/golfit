package org.golfit.server.game;

import org.golfit.shared.constants.Constants;
import org.golfit.shared.constants.MapData;

import java.util.List;

import static org.golfit.shared.constants.Constants.BALL_RADIUS;
import static org.golfit.shared.constants.Constants.FRICTION_GRASS;

/**
 * Represents the server-side physical state and behavior of a golf ball.
 * Optimized to prevent tunneling via dynamic sub-stepping and continuous resolution.
 */
public class ServerBall {
    private double px, py;
    private double vx, vy;
    private final double radius = BALL_RADIUS;

    private double lastStaticX;
    private double lastStaticY;

    /** Number of strokes taken by this ball's player. */
    private int strokeCount = 0;
    /** True once the ball has entered the hole and the player has finished. */
    private boolean finished = false;

    private final double STOPPING_VELOCITY = Constants.STOPPING_VELOCITY;
    private double friction = FRICTION_GRASS;

    private double lastNormalX, lastNormalY;
    private double overlap;

    /**
     * Creates a new server ball at the given position.
     * Coordinates are normalized to grid units if they appear to be raw pixels.
     *
     * @param px the initial x position
     * @param py the initial y position
     */
    public ServerBall(double px, double py) {
        // Normalizing coordinates if they come in as raw pixels
        this.px = (px > MapData.COLS) ? px / (1100.0 / MapData.COLS) : px;
        this.py = (py > MapData.ROWS) ? py / (600.0 / MapData.ROWS) : py;

        this.lastStaticX = this.px;
        this.lastStaticY = this.py;
        this.vx = 0;
        this.vy = 0;
    }

    /**
     * Applies a stroke to the ball with the given initial velocity.
     * Saves the current position for water hazard resets and increments the stroke counter.
     *
     * @param initialVx the initial x velocity in grid units per second
     * @param initialVy the initial y velocity in grid units per second
     */
    public void stroke(double initialVx, double initialVy) {
        if (finished) return; // Cannot stroke a finished ball
        // Save position BEFORE the stroke for reset purposes (e.g., water hazards)
        this.lastStaticX = this.px;
        this.lastStaticY = this.py;

        this.vx = initialVx;
        this.vy = initialVy;
        strokeCount++;

        double maxGridSpeed = 60.0;
        double currentSpeed = Math.sqrt(vx * vx + vy * vy);

        if (currentSpeed > maxGridSpeed) {
            double ratio = maxGridSpeed / currentSpeed;
            this.vx *= ratio;
            this.vy *= ratio;
        }
    }

    /**
     * Advances the ball's physics by one tick. Applies friction, performs sub-stepped
     * movement to prevent tunneling, and resolves collisions with nearby obstacles.
     *
     * @param deltaTime    the time step in seconds
     * @param nearbyShapes the obstacles to check for collision
     */
    public void update(double deltaTime, List<MapData.ServerShape> nearbyShapes) {
        double speed = Math.sqrt(vx * vx + vy * vy);
        if (speed <= 0 && vx == 0 && vy == 0) return;

        // 1. Apply Friction
        double currentFriction = this.friction;
        if (speed < 3.0) currentFriction *= 0.85;
        double frictionFactor = Math.pow(currentFriction, deltaTime);
        vx *= frictionFactor;
        vy *= frictionFactor;

        if (Math.sqrt(vx * vx + vy * vy) < STOPPING_VELOCITY) {
            vx = 0;
            vy = 0;
            return;
        }

        // 2. Dynamic Sub-stepping Logic
        // We ensure no sub-step moves the ball further than half its radius.
        double maxStepDist = radius * 0.5;
        double distanceToTravel = Math.sqrt(vx * vx + vy * vy) * deltaTime;
        int numSteps = (int) Math.ceil(distanceToTravel / maxStepDist);
        double subDeltaTime = deltaTime / numSteps;

        for (int i = 0; i < numSteps; i++) {
            px += vx * subDeltaTime;
            py += vy * subDeltaTime;

            // Check collisions for the current sub-position
            for (MapData.ServerShape shape : nearbyShapes) {
                // terrainType 1 = Wall, 5 = Bumper/Object
                if (shape.terrainType == 1 || shape.terrainType == 5) {
                    if (checkCollision(shape)) {
                        resolveCollision();

                        // We do NOT break here. After resolution, the ball is
                        // outside the wall with a new velocity. We allow it to
                        // finish the loop/remaining steps with its new vector.
                    }
                }
            }
        }
    }

    /** Resolves a detected collision by pushing the ball out and reflecting its velocity. */
    private void resolveCollision() {
        // Push ball out of the object by the overlap amount plus a small buffer
        px += lastNormalX * (overlap + 0.005);
        py += lastNormalY * (overlap + 0.005);

        // Reflect velocity vector based on collision normal
        double dotProduct = vx * lastNormalX + vy * lastNormalY;

        // Only reflect if the ball is moving TOWARDS the face (prevents getting stuck inside)
        if (dotProduct < 0) {
            double bounciness = 0.75;
            vx = (vx - 2 * dotProduct * lastNormalX) * bounciness;
            vy = (vy - 2 * dotProduct * lastNormalY) * bounciness;
        }
    }

    /** Resets the ball to its last stationary position (used for water hazard resets). */
    public void resetToLastPosition() {
        this.px = lastStaticX;
        this.py = lastStaticY;
        this.vx = 0;
        this.vy = 0;
    }

    /**
     * Checks whether this ball collides with the given shape.
     * Supports rectangle, polygon, and ellipse shapes.
     *
     * @param shape the obstacle shape to test against
     * @return true if a collision is detected
     */
    public boolean checkCollision(MapData.ServerShape shape) {
        if (shape.formType == MapData.ServerShape.RECT) {
            double closestX = Math.max(shape.x, Math.min(px, shape.x + shape.w));
            double closestY = Math.max(shape.y, Math.min(py, shape.y + shape.h));
            return updateCollisionData(closestX, closestY);
        } else if (shape.formType == MapData.ServerShape.POLYGON) {
            double[] pts = shape.points;
            boolean hit = false;
            for (int i = 0; i < pts.length; i += 2) {
                double x1 = pts[i], y1 = pts[i + 1];
                double x2 = pts[(i + 2) % pts.length], y2 = pts[(i + 3) % pts.length];

                double dx = x2 - x1, dy = y2 - y1;
                double l2 = dx * dx + dy * dy;
                if (l2 == 0) continue;

                double t = Math.max(0, Math.min(1, ((px - x1) * dx + (py - y1) * dy) / l2));
                if (updateCollisionData(x1 + t * dx, y1 + t * dy)) hit = true;
            }
            return hit;
        } else if (shape.formType == MapData.ServerShape.ELLIPSE) {
            return updateCollisionData(shape.x + shape.w / 2.0, shape.y + shape.h / 2.0);
        }
        return false;
    }

    /**
     * Tests if the ball overlaps the nearest point and updates collision normal/overlap data.
     *
     * @param nearestX the x coordinate of the nearest point on the shape
     * @param nearestY the y coordinate of the nearest point on the shape
     * @return true if the ball overlaps the point (collision detected)
     */
    private boolean updateCollisionData(double nearestX, double nearestY) {
        double dx = px - nearestX;
        double dy = py - nearestY;
        double distSq = dx * dx + dy * dy;

        if (distSq < radius * radius) {
            double dist = Math.sqrt(distSq);

            // Safeguard against center-on-edge collision (division by zero)
            if (dist < 0.0001) {
                // If velocity is zero, push up. Otherwise, push opposite to movement.
                double mag = Math.sqrt(vx * vx + vy * vy);
                lastNormalX = (mag > 0) ? -vx / mag : 0;
                lastNormalY = (mag > 0) ? -vy / mag : -1;
                overlap = radius;
            } else {
                lastNormalX = dx / dist;
                lastNormalY = dy / dist;
                overlap = radius - dist;
            }
            return true;
        }
        return false;
    }

    /** Returns the current x position in grid coordinates. */
    public double getPx() {
        return px;
    }

    /** Returns the current y position in grid coordinates. */
    public double getPy() {
        return py;
    }

    /** Returns true if the ball has any non-zero velocity. */
    public boolean isMoving() {
        return vx != 0 || vy != 0;
    }

    /** Sets the current friction coefficient based on the terrain under the ball. */
    public void changeFriction(double currentFriction) {
        this.friction = currentFriction;
    }
    /** Returns the number of strokes taken by this ball's player. */
    public int getStrokeCount() { return strokeCount; }
    /** Returns true if this ball has entered the hole. */
    public boolean isFinished() { return finished; }
    /** Sets whether this ball has finished (entered the hole). */
    public void setFinished(boolean finished) { this.finished = finished; }

    /** Resets the ball to the default spawn position with zero velocity and stroke count. */
    public void reset() {
        this.px = 9.8;
        this.py = 42.4;
        this.lastStaticX = this.px;
        this.lastStaticY = this.py;
        this.vx = 0;
        this.vy = 0;
        this.strokeCount = 0;
        this.finished = false;
    }
}