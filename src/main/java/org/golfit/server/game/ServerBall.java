package org.golfit.server.game;

import org.golfit.shared.constants.Constants;
import org.golfit.shared.constants.MapData;
import java.util.List;

import static org.golfit.shared.constants.Constants.BALL_RADIUS;
import static org.golfit.shared.constants.Constants.FRICTION_GRASS;

/**
 * Represents the server-side physical state and behavior of a golf ball.
 * Handles movement, friction, collision detection, and position resets.
 */
public class ServerBall {
    private double px, py;
    private double vx, vy;
    private final double radius = BALL_RADIUS;

    // Position storage for water-hazard resets (position prior to last stroke)
    private double lastStaticX;
    private double lastStaticY;

    /** Number of strokes taken by this ball's player. */
    private int strokeCount = 0;
    /** True once the ball has entered the hole and the player has finished. */
    private boolean finished = false;

    private final double STOPPING_VELOCITY = Constants.STOPPING_VELOCITY;
    private double friction = FRICTION_GRASS;

    // Helper variables for collision resolution
    private double lastNormalX, lastNormalY;
    private double overlap;

    /**
     * Initializes a new ball at the specified coordinates.
     * Automatically converts pixel coordinates to grid coordinates if necessary.
     */
    public ServerBall(double px, double py) {
        this.px = (px > MapData.COLS) ? px / (1100.0 / MapData.COLS) : px;
        this.py = (py > MapData.ROWS) ? py / (600.0 / MapData.ROWS) : py;

        this.lastStaticX = this.px;
        this.lastStaticY = this.py;
        this.vx = 0;
        this.vy = 0;
    }

    /**
     * Executes a stroke by setting the initial velocity.
     * Saves the current position as a safe spot for potential resets.
     *
     * @param initialVx Initial velocity component on the X-axis.
     * @param initialVy Initial velocity component on the Y-axis.
     */
    public void stroke(double initialVx, double initialVy) {
        if (finished) return; // Cannot stroke a finished ball
        // Save position BEFORE the stroke for reset purposes (e.g., water hazards)
        this.lastStaticX = this.px;
        this.lastStaticY = this.py;

        this.vx = initialVx;
        this.vy = initialVy;
        strokeCount++;

        // Hard limit to maintain physical stability and prevent wall-glitching
        double maxGridSpeed = 60.0;
        double currentSpeed = Math.sqrt(vx * vx + vy * vy);

        if (currentSpeed > maxGridSpeed) {
            double ratio = maxGridSpeed / currentSpeed;
            this.vx *= ratio;
            this.vy *= ratio;
        }
    }

    /**
     * Updates the ball's physics state including movement, friction, and collisions.
     *
     * @param deltaTime Elapsed time since the last update.
     * @param nearbyShapes List of physical objects in the vicinity of the ball.
     */
    public void update(double deltaTime, List<MapData.ServerShape> nearbyShapes) {
        double speed = Math.sqrt(vx * vx + vy * vy);
        if (speed <= 0 && vx == 0 && vy == 0) return;

        // 1. Friction Logic
        double currentFriction = this.friction;
        if (speed < 3.0) currentFriction *= 0.85;
        double frictionFactor = Math.pow(currentFriction, deltaTime);
        vx *= frictionFactor;
        vy *= frictionFactor;

        if (Math.sqrt(vx * vx + vy * vy) < STOPPING_VELOCITY) {
            vx = 0; vy = 0; return;
        }

        // SUB-STEPPING
        double stepSize = 0.1;
        double distanceToTravel = Math.sqrt(vx * vx + vy * vy) * deltaTime;
        int numSteps = (int) Math.ceil(distanceToTravel / stepSize);
        double subDeltaTime = deltaTime / numSteps;

        for (int i = 0; i < numSteps; i++) {
            px += vx * subDeltaTime;
            py += vy * subDeltaTime;

            boolean hitDetected = false;
            for (MapData.ServerShape shape : nearbyShapes) {
                if (shape.terrainType == 1 || shape.terrainType == 5) {
                    if (checkCollision(shape)) {

                        // --- DETAILED COLLISION DEBUG ---
                        // This prints the ball's center and the wall's exact box
                        System.out.printf(java.util.Locale.US,
                                "[HIT] Ball(%.3f,%.3f) | Wall[x:%.1f, y:%.1f, w:%.1f, h:%.1f] | Overlap: %.4f | Normal: %.1f,%.1f%n",
                                px, py, shape.x, shape.y, shape.w, shape.h, overlap, lastNormalX, lastNormalY);

                        resolveCollision();

                        vx *= 0.8;
                        vy *= 0.8;

                        hitDetected = true;
                        break;
                    }
                }
            }
            if (hitDetected) break;
        }
    }

    private void resolveCollision() {
        // Save position before resolution for debug comparison
        double preResolveX = px;
        double preResolveY = py;

        // Push ball out of the object
        px += lastNormalX * (overlap + 0.001);
        py += lastNormalY * (overlap + 0.001);

        // Reflect velocity
        double dotProduct = vx * lastNormalX + vy * lastNormalY;
        if (dotProduct < 0) {
            double bounciness = 0.7;
            vx = (vx - 2 * dotProduct * lastNormalX) * bounciness;
            vy = (vy - 2 * dotProduct * lastNormalY) * bounciness;

            // --- DEBUG START ---
            System.out.printf(java.util.Locale.US,
                    "[RESOLVE] Adjusted Pos: %.3f,%.3f (Shifted: %.4f) | New Vel: %.2f,%.2f%n",
                    px, py, Math.sqrt(Math.pow(px-preResolveX, 2) + Math.pow(py-preResolveY, 2)), vx, vy);
            // --- DEBUG END ---
        }
    }

    /**
     * Resets the ball to the position it occupied before the last stroke.
     * Used when the ball enters a non-playable area like water.
     */
    public void resetToLastPosition() {
        this.px = lastStaticX;
        this.py = lastStaticY;
        this.vx = 0;
        this.vy = 0;
    }


    /**
     * Checks for collision with a specific shape based on its geometry.
     * * @param shape The shape to test against.
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
            // Ellipses are currently treated as circular points (e.g., for holes)
            return updateCollisionData(shape.x + shape.w / 2.0, shape.y + shape.h / 2.0);
        }
        return false;
    }

    /**
     * Updates collision normal and overlap data based on the nearest point of a shape.
     */
    private boolean updateCollisionData(double nearestX, double nearestY) {
        double dx = px - nearestX;
        double dy = py - nearestY;
        double distSq = dx * dx + dy * dy;

        if (distSq < radius * radius && distSq > 0) {
            double dist = Math.sqrt(distSq);
            lastNormalX = dx / dist;
            lastNormalY = dy / dist;
            overlap = radius - dist;
            return true;
        }
        return false;
    }

    public double getPx() { return px; }
    public double getPy() { return py; }
    public boolean isMoving() { return vx != 0 || vy != 0; }
    public void changeFriction(double currentFriction) { this.friction = currentFriction; }
    public int getStrokeCount() { return strokeCount; }
    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }

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