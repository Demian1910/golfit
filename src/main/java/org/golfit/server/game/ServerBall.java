package org.golfit.server.game;

import org.golfit.shared.constants.Constants;
import org.golfit.shared.constants.MapData;
import java.util.ArrayList;
import java.util.List;

import static org.golfit.shared.constants.Constants.BALL_RADIUS;
import static org.golfit.shared.constants.Constants.FRICTION_GRASS;

public class ServerBall {
    private double px, py;
    private double vx, vy;
    private final double radius = BALL_RADIUS;

    private double lastStaticX;
    private double lastStaticY;

    private int strokeCount = 0;
    private boolean finished = false;

    private final double STOPPING_VELOCITY = Constants.STOPPING_VELOCITY;
    private double friction = FRICTION_GRASS;

    private double lastNormalX, lastNormalY;
    private double overlap;

    // Queue for objects to be removed by the server manager
    private final List<Integer> shapesToRemove = new ArrayList<>();

    public ServerBall(double px, double py) {
        this.px = (px > MapData.COLS) ? px / (1100.0 / MapData.COLS) : px;
        this.py = (py > MapData.ROWS) ? py / (600.0 / MapData.ROWS) : py;

        this.lastStaticX = this.px;
        this.lastStaticY = this.py;
        this.vx = 0;
        this.vy = 0;
    }

    public void stroke(double initialVx, double initialVy) {
        if (finished) return;
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

    public void update(double deltaTime, List<MapData.ServerShape> nearbyShapes) {
        double speed = Math.sqrt(vx * vx + vy * vy);
        if (speed <= 0 && vx == 0 && vy == 0) return;

        double frictionFactor = Math.pow(this.friction, deltaTime);
        vx *= frictionFactor;
        vy *= frictionFactor;

        if (Math.sqrt(vx * vx + vy * vy) < STOPPING_VELOCITY) {
            vx = 0;
            vy = 0;
            return;
        }

        double maxStepDist = radius * 0.5;
        double distanceToTravel = Math.sqrt(vx * vx + vy * vy) * deltaTime;
        int numSteps = (int) Math.ceil(distanceToTravel / maxStepDist);
        double subDeltaTime = deltaTime / numSteps;

        for (int i = 0; i < numSteps; i++) {
            px += vx * subDeltaTime;
            py += vy * subDeltaTime;

            // Inside the sub-stepping loop of ServerBall.java
            for (MapData.ServerShape shape : nearbyShapes) {
                if (checkCollision(shape)) {
                    // If it's a Wall (1) or Bumper (5), we ALWAYS bounce.
                    if (shape.terrainType == 1 || shape.terrainType == 5) {
                        resolveCollision();

                        // Only queue removal if it's a "breakable" part (like a Tree)
                        // If it's a standard wall, partnerId will be -1, so nothing else happens.
                        if (shape.partnerId != -1) {
                            queueRemoval(shape);
                        }
                    }
                }
            }
        }
    }

    private void queueRemoval(MapData.ServerShape shape) {
        if (!shapesToRemove.contains(shape.id)) {
            shapesToRemove.add(shape.id);
            if (shape.partnerId != -1) {
                shapesToRemove.add(shape.partnerId);
            }
        }
    }

    public List<Integer> getPendingRemovals() {
        List<Integer> out = new ArrayList<>(shapesToRemove);
        shapesToRemove.clear();
        return out;
    }

    private void resolveCollision() {
        px += lastNormalX * (overlap + 0.005);
        py += lastNormalY * (overlap + 0.005);

        double dotProduct = vx * lastNormalX + vy * lastNormalY;

        if (dotProduct < 0) {
            double bounciness = 0.75;
            vx = (vx - 2 * dotProduct * lastNormalX) * bounciness;
            vy = (vy - 2 * dotProduct * lastNormalY) * bounciness;
        }
    }

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

    private boolean updateCollisionData(double nearestX, double nearestY) {
        double dx = px - nearestX;
        double dy = py - nearestY;
        double distSq = dx * dx + dy * dy;

        if (distSq < radius * radius) {
            double dist = Math.sqrt(distSq);
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

    public void resetToLastPosition() {
        this.px = lastStaticX;
        this.py = lastStaticY;
        this.vx = 0;
        this.vy = 0;
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