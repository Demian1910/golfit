package org.golfit.server.game;

import org.golfit.shared.constants.Constants;
import org.golfit.shared.constants.MapData;

import java.util.List;

import static org.golfit.shared.constants.Constants.BALL_RADIUS;
import static org.golfit.shared.constants.Constants.FRICTION_GRASS;

/**
 * Represents the server-side physical state and behavior of a golf ball.
 * <p>
 * This class handles movement, friction, collision detection, and resolution.
 * It is optimized to prevent "tunneling" (passing through walls at high speeds)
 * via dynamic sub-stepping and continuous collision detection logic.
 * </p>
 */
public class ServerBall {
    /** The horizontal position in grid units. */
    private double px, py;
    /** The horizontal and vertical velocity components. */
    private double vx, vy;
    /** The radius of the ball, used for collision boundaries. */
    private final double radius = BALL_RADIUS;

    /** The X-coordinate of the last stable (non-moving) position, used for resets. */
    private double lastStaticX;
    /** The Y-coordinate of the last stable (non-moving) position, used for resets. */
    private double lastStaticY;

    /** Number of strokes taken by this ball's player in the current hole. */
    private int strokeCount = 0;
    /** Flag indicating if the ball has successfully entered the hole. */
    private boolean finished = false;

    /** The velocity threshold below which the ball is considered stopped. */
    private final double STOPPING_VELOCITY = Constants.STOPPING_VELOCITY;
    /** The current friction coefficient being applied to the ball. */
    private double friction = FRICTION_GRASS;

    /** The X component of the collision normal from the most recent collision. */
    private double lastNormalX;
    /** The Y component of the collision normal from the most recent collision. */
    private double lastNormalY;
    /** The depth of penetration into an object during a collision. */
    private double overlap;

    /**
     * Constructs a new ServerBall and normalizes coordinates if they are provided in raw pixels.
     * * @param px Initial X position (pixels or grid units).
     * @param py Initial Y position (pixels or grid units).
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
     * Applies an impulse to the ball, starting its movement.
     * * @param initialVx The horizontal velocity to apply.
     * @param initialVy The vertical velocity to apply.
     */
    public void stroke(double initialVx, double initialVy) {
        if (finished) return;

        // Save position BEFORE the stroke for reset purposes (e.g., water hazards)
        this.lastStaticX = this.px;
        this.lastStaticY = this.py;

        this.vx = initialVx;
        this.vy = initialVy;
        strokeCount++;

        // Cap the maximum speed to ensure physics stability
        double maxGridSpeed = 60.0;
        double currentSpeed = Math.sqrt(vx * vx + vy * vy);

        if (currentSpeed > maxGridSpeed) {
            double ratio = maxGridSpeed / currentSpeed;
            this.vx *= ratio;
            this.vy *= ratio;
        }
    }

    /**
     * Updates the ball's position and velocity based on physics and collisions.
     * Uses sub-stepping to ensure the ball does not skip over thin walls.
     * * @param deltaTime The time elapsed since the last update.
     * @param nearbyShapes A list of collision shapes in the ball's vicinity.
     */
    public void update(double deltaTime, List<MapData.ServerShape> nearbyShapes) {
        double speed = Math.sqrt(vx * vx + vy * vy);
        if (speed <= 0 && vx == 0 && vy == 0) return;

        // 1. Apply Friction
        double currentFriction = this.friction;
        if (speed < 3.0) currentFriction *= 0.85; // Extra "grip" at very low speeds
        double frictionFactor = Math.pow(currentFriction, deltaTime);
        vx *= frictionFactor;
        vy *= frictionFactor;

        // Stop the ball if it falls below the minimum velocity
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
                    }
                }
            }
        }
    }

    /**
     * Resolves a collision by pushing the ball out of the object and reflecting its velocity.
     */
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

    /**
     * Resets the ball to its last static position (e.g., before the last stroke).
     */
    public void resetToLastPosition() {
        this.px = lastStaticX;
        this.py = lastStaticY;
        this.vx = 0;
        this.vy = 0;
    }

    /**
     * Checks if the ball is colliding with a specific shape.
     * * @param shape The shape to check against (Rect, Polygon, or Ellipse).
     * @return true if a collision occurred.
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
            // Simplified: Treats ellipse as a circle for the purposes of normal calculation
            return updateCollisionData(shape.x + shape.w / 2.0, shape.y + shape.h / 2.0);
        }
        return false;
    }

    /**
     * Calculates the normal vector and overlap distance for a collision point.
     * * @param nearestX The closest X point on the shape to the ball center.
     * @param nearestY The closest Y point on the shape to the ball center.
     * @return true if the ball overlaps with the point.
     */
    private boolean updateCollisionData(double nearestX, double nearestY) {
        double dx = px - nearestX;
        double dy = py - nearestY;
        double distSq = dx * dx + dy * dy;

        if (distSq < radius * radius) {
            double dist = Math.sqrt(distSq);

            // Safeguard against center-on-edge collision (division by zero)
            if (dist < 0.0001) {
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

    public double getPx() { return px; }
    public double getPy() { return py; }

    /** @return true if the ball has non-zero velocity. */
    public boolean isMoving() {
        return vx != 0 || vy != 0;
    }

    /**
     * Updates the friction coefficient (e.g., when moving from grass to sand).
     * @param currentFriction The new friction value.
     */
    public void changeFriction(double currentFriction) {
        this.friction = currentFriction;
    }

    public int getStrokeCount() { return strokeCount; }
    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }

    /**
     * Resets the ball to the starting tee position and clears stroke data.
     */
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