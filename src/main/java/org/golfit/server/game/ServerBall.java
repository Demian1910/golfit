package org.golfit.server.game;

import org.golfit.shared.constants.Constants;
import org.golfit.shared.constants.MapData;
import java.util.ArrayList;
import java.util.List;

import static org.golfit.shared.constants.Constants.*;

/**
 * Represents the server-side physical state and behavior of a golf ball.
 * This class handles sub-stepping to prevent tunneling through thin walls,
 * applies friction, and manages collision resolution for various shape types.
 */
public class ServerBall {
    // Current position and velocity
    private double px, py;
    private double vx, vy;
    private final double radius = BALL_RADIUS;

    // Stores the position before the last stroke for "out of bounds" resets
    private double lastStaticX;
    private double lastStaticY;

    /** Number of strokes taken by this ball's player. */
    private int strokeCount = 0;

    /** True once the ball has entered the hole and the player has finished. */
    private boolean finished = false;

    private final double STOPPING_VELOCITY = Constants.STOPPING_VELOCITY;
    private double friction = FRICTION_GRASS;

    // Temporary storage for collision resolution data
    private double lastNormalX, lastNormalY;
    private double overlap;

    /** Queue for object IDs to be removed by the server manager (e.g., triggered switches) */
    private final List<Integer> shapesToRemove = new ArrayList<>();

    private int debugTick = 0;

    /**
     * Creates a new server ball at the given position.
     * Normalizes coordinates to grid units if they are passed as raw pixels.
     * * @param px Initial X position.
     * @param py Initial Y position.
     */
    public ServerBall(double px, double py) {
        this.px = (px > MapData.COLS) ? px / (1100.0 / MapData.COLS) : px;
        this.py = (py > MapData.ROWS) ? py / (600.0 / MapData.ROWS) : py;

        this.lastStaticX = this.px;
        this.lastStaticY = this.py;
        this.vx = 0;
        this.vy = 0;
    }

    /** * Teleports the ball to the center of the hole and stops movement.
     */
    public void teleportToHole() {
        this.px = HOLE_CENTER_X;
        this.py = HOLE_CENTER_Y;
        this.vx = 0;
        this.vy = 0;
    }

    /**
     * Instantly moves the ball to a new coordinate and kills velocity.
     * Used for "Safety Shoves" when obstacles appear/disappear.
     */
    public void teleport(double newX, double newY) {
        this.px = newX;
        this.py = newY;
        this.vx = 0;
        this.vy = 0;
    }

    /** * Applies a stroke impulse to the ball and increments stroke count.
     * * @param initialVx X component of the impulse.
     * @param initialVy Y component of the impulse.
     */
    public void stroke(double initialVx, double initialVy) {
        if (finished) return;

        this.lastStaticX = this.px;
        this.lastStaticY = this.py;

        this.vx = initialVx;
        this.vy = initialVy;
        strokeCount++;

        // Clamp speed to prevent extreme physics glitches
        double maxGridSpeed = 60.0;
        double currentSpeed = Math.sqrt(vx * vx + vy * vy);

        if (currentSpeed > maxGridSpeed) {
            double ratio = maxGridSpeed / currentSpeed;
            this.vx *= ratio;
            this.vy *= ratio;
        }
    }

    /** * Advances physics by one tick, performing sub-stepping and collision checks.
     * * @param deltaTime Time elapsed since last tick (seconds).
     * @param nearbyShapes List of shapes in the ball's immediate vicinity.
     */
    public void update(double deltaTime, List<MapData.ServerShape> nearbyShapes) {
        double speed = Math.sqrt(vx * vx + vy * vy);
        if (speed <= 0 && vx == 0 && vy == 0) return;

        // 1. Apply Friction
        double currentFriction = this.friction;
        if (speed < 3.0) currentFriction *= 0.85; // Assist stopping at low speeds

        double frictionFactor = Math.pow(currentFriction, deltaTime);
        vx *= frictionFactor;
        vy *= frictionFactor;

        if (Math.sqrt(vx * vx + vy * vy) < STOPPING_VELOCITY) {
            vx = 0;
            vy = 0;
            return;
        }

        // 2. Sub-stepping logic to prevent tunneling (fast balls moving through thin walls)
        double maxStepDist = radius * 0.5;
        double distanceToTravel = Math.sqrt(vx * vx + vy * vy) * deltaTime;
        int numSteps = (int) Math.ceil(distanceToTravel / maxStepDist);
        double subDeltaTime = deltaTime / numSteps;

        debugTick++;

        for (int i = 0; i < numSteps; i++) {
            px += vx * subDeltaTime;
            py += vy * subDeltaTime;

            for (MapData.ServerShape shape : nearbyShapes) {
                if (checkCollision(shape)) {
                    // Type 1: Wall, Type 5: Bumper
                    if (shape.terrainType == 1 || shape.terrainType == 5) {
                        resolveCollision(shape.terrainType);

                        // Handle removal for destructible objects
                        if (shape.partnerId != -1) {
                            queueRemoval(shape);
                        }
                    }

                    // Type 6: Floor Switch/Trigger
                    if (shape.terrainType == 6) {
                        queueRemoval(shape);
                    }
                }
            }
        }
    }

    /** * Adjusts position to resolve overlap and reflects velocity based on the surface normal.
     * * @param terrainType The type of terrain hit (used to calculate bounciness).
     */
    private void resolveCollision(int terrainType) {
        // Push the ball out of the object
        px += lastNormalX * (overlap + 0.005);
        py += lastNormalY * (overlap + 0.005);

        double dotProduct = vx * lastNormalX + vy * lastNormalY;

        // Only bounce if the ball is moving into the surface
        if (dotProduct < 0) {
            double bounciness = (terrainType == 5) ? 1.2 : 0.75;

            vx = (vx - 2 * dotProduct * lastNormalX) * bounciness;
            vy = (vy - 2 * dotProduct * lastNormalY) * bounciness;
        }
    }

    /**
     * Internal method to track shapes that need to be removed from the server world.
     */
    private void queueRemoval(MapData.ServerShape shape) {
        if (!shapesToRemove.contains(shape.id)) {
            shapesToRemove.add(shape.id);
            if (shape.partnerId != -1) {
                shapesToRemove.add(shape.partnerId);
            }
        }
    }

    /** * Returns the IDs of shapes hit during this tick and clears the internal queue.
     */
    public List<Integer> getPendingRemovals() {
        List<Integer> out = new ArrayList<>(shapesToRemove);
        shapesToRemove.clear();
        return out;
    }

    /**
     * Main collision dispatcher. Routes the check based on the shape's form type.
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
            double centerX = shape.x + shape.w / 2.0;
            double centerY = shape.y + shape.h / 2.0;
            double dx = px - centerX;
            double dy = py - centerY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance == 0) return updateCollisionData(centerX, centerY);

            double radiusX = shape.w / 2.0;
            double radiusY = shape.h / 2.0;
            double closestX = centerX + (dx / distance) * radiusX;
            double closestY = centerY + (dy / distance) * radiusY;

            return updateCollisionData(closestX, closestY);
        }
        return false;
    }

    /**
     * Calculates the distance from the ball to the nearest point on a shape
     * and updates the normal/overlap data if a collision is detected.
     */
    private boolean updateCollisionData(double nearestX, double nearestY) {
        double dx = px - nearestX;
        double dy = py - nearestY;
        double distSq = dx * dx + dy * dy;

        if (distSq < radius * radius) {
            double dist = Math.sqrt(distSq);
            // Handle edge case where ball center is exactly on the shape edge
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

    /** * Teleports the ball back to the position before the most recent stroke.
     */
    public void resetToLastPosition() {
        this.px = lastStaticX;
        this.py = lastStaticY;
        this.vx = 0;
        this.vy = 0;
    }

    /** * Fully resets the ball state to the default start position.
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

    public double getPx() { return px; }
    public double getPy() { return py; }
    public boolean isMoving() { return vx != 0 || vy != 0; }
    public void changeFriction(double currentFriction) { this.friction = currentFriction; }
    public int getStrokeCount() { return strokeCount; }
    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }
}