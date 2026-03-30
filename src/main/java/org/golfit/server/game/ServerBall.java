package org.golfit.server.game;

import org.golfit.shared.constants.Constants;
import org.golfit.shared.constants.MapData;

import java.util.List;

import static org.golfit.shared.constants.Constants.GRASS_FRICTION;

public class ServerBall {
    private double px, py;
    private double vx, vy;
    private final double radius;
    private final double STOPPING_VELOCITY = Constants.STOPPING_VELOCITY;
    private double friction = GRASS_FRICTION;

    // Help var for collisions
    private double lastNormalX, lastNormalY;
    private double overlap;

    public ServerBall(double px, double py) {
        this.px = px;
        this.py = py;
        this.vx = 0;
        this.vy = 0;
        this.radius = 0.5; // based on grid!
    }

    public void stroke(double initialVx, double initialVy) {
        this.vx = initialVx;
        this.vy = initialVy;
    }

    public void update(double deltaTime, List<MapData.ServerShape> nearbyShapes ) {
        // Apply friction
        double frictionFactor = Math.pow(friction, deltaTime);
        vx *= frictionFactor;
        vy *= frictionFactor;

        if (Math.abs(vx) < STOPPING_VELOCITY && Math.abs(vy) < STOPPING_VELOCITY) {
            vx = 0;
            vy = 0;
            return;
        }

        // Check collision
        for (MapData.ServerShape shape : nearbyShapes) {
            if (checkCollision(shape)) {
                resolveCollision();
                break;
            }
        }

        // 3. Apply movement
        px += vx * deltaTime;
        py += vy * deltaTime;
    }

    private boolean checkCollision(MapData.ServerShape shape) {
        if (shape.formType == MapData.ServerShape.RECT) {
            double closestX = Math.max(shape.x, Math.min(px, shape.x + shape.w));
            double closestY = Math.max(shape.y, Math.min(py, shape.y + shape.h));
            return updateCollisionData(closestX, closestY);
        }
        else if (shape.formType == MapData.ServerShape.POLYGON) {
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
        }
        else if (shape.formType == MapData.ServerShape.ELLIPSE) {
            // TODO include switch mechanic for ellipse
            return updateCollisionData(shape.x + shape.w / 2.0, shape.y + shape.h / 2.0);
        }
        return false;
    }

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

    private void resolveCollision() {
        // Correct position
        px += lastNormalX * overlap;
        py += lastNormalY * overlap;

        // Reflect vel
        double dotProduct = vx * lastNormalX + vy * lastNormalY;
        if (dotProduct < 0) {
            double bounciness = 0.7;
            vx = (vx - 2 * dotProduct * lastNormalX) * bounciness;
            vy = (vy - 2 * dotProduct * lastNormalY) * bounciness;
        }
    }


    public double getPx() { return px; }
    public double getPy() { return py; }
    public boolean isMoving() { return vx != 0 || vy != 0; }
    public void changeFriction(double newFriction) { friction = newFriction; }
}