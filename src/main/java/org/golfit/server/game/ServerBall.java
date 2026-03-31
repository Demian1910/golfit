package org.golfit.server.game;

import org.golfit.shared.constants.Constants;
import org.golfit.shared.constants.MapData;

import java.util.List;
import java.util.Locale;

import static org.golfit.shared.constants.Constants.GRASS_FRICTION;

public class ServerBall {
    private double px, py;
    private double vx, vy;
    private final double radius;
    private final double STOPPING_VELOCITY = Constants.STOPPING_VELOCITY;
    private double friction = GRASS_FRICTION;

    // Hilfsvariablen für Kollisionen
    private double lastNormalX, lastNormalY;
    private double overlap;

    // Notfall-Konstanten für die Umrechnung (Pixel zu Grid)
    private final double BASE_WIDTH = 1100.0;
    private final double BASE_HEIGHT = 600.0;

    public ServerBall(double px, double py) {
        // Falls die Startwerte in Pixeln kommen (z.B. > 80), direkt umrechnen
        this.px = (px > MapData.COLS) ? px / (BASE_WIDTH / MapData.COLS) : px;
        this.py = (py > MapData.ROWS) ? py / (BASE_HEIGHT / MapData.ROWS) : py;

        this.vx = 0;
        this.vy = 0;
        this.radius = 0.5; // Halbe Grid-Zelle
    }

    public void stroke(double initialVx, double initialVy) {
        // Falls die Kraft in Pixel/s kommt, auch hier herunterskalieren
        double scaleX = BASE_WIDTH / MapData.COLS;
        double scaleY = BASE_HEIGHT / MapData.ROWS;

        this.vx = (Math.abs(initialVx) > MapData.COLS) ? initialVx / scaleX : initialVx;
        this.vy = (Math.abs(initialVy) > MapData.ROWS) ? initialVy / scaleY : initialVy;

        System.out.println(String.format(Locale.US, "[DEBUG] Stroke received. Grid-Vel: %.2f / %.2f", vx, vy));
    }

    public void update(double deltaTime, List<MapData.ServerShape> nearbyShapes) {
        // NOTFALL-KONVERTIERUNG: Wenn px/py Pixel sind, mache Grid-Einheiten daraus
        if (this.px > 80) {
            this.px /= 13.75; // 1100 / 80
        }
        if (this.py > 45) {
            this.py /= 13.33; // 600 / 45
        }

        // 1. Reibung anwenden
        double frictionFactor = Math.pow(friction, deltaTime);
        vx *= frictionFactor;
        vy *= frictionFactor;

        if (Math.abs(vx) < STOPPING_VELOCITY && Math.abs(vy) < STOPPING_VELOCITY) {
            vx = 0;
            vy = 0;
            return;
        }

        // 2. Bewegung ausführen (in Grid-Einheiten)
        px += vx * deltaTime;
        py += vy * deltaTime;

        // 3. Kollisionsprüfung
        // DEBUG: Zeige aktuelle Grid-Position und wie viele Objekte der Server in der Nähe sieht
        if (isMoving()) {
            System.out.println(String.format(Locale.US, "[DEBUG] Ball Grid-Pos: %.2f / %.2f | Nearby: %d", px, py, nearbyShapes.size()));
        }

        for (MapData.ServerShape shape : nearbyShapes) {
            if (checkCollision(shape)) {
                String type = switch (shape.formType) {
                    case MapData.ServerShape.RECT -> "Rectangle/Wall";
                    case MapData.ServerShape.POLYGON -> "Polygon";
                    case MapData.ServerShape.ELLIPSE -> "Ellipse/Hole";
                    default -> "Unknown";
                };

                System.out.println("[DEBUG] !!! COLLISION !!! with " + type + " at " + shape.x + "/" + shape.y);
                resolveCollision();
                break;
            }
        }
    }

    private void resolveCollision() {
        // Korrektur: Ball aus dem Objekt schieben
        px += lastNormalX * overlap;
        py += lastNormalY * overlap;

        // Reflektion (Abprallen)
        double dotProduct = vx * lastNormalX + vy * lastNormalY;

        if (dotProduct < 0) {
            double bounciness = 0.7;
            vx = (vx - 2 * dotProduct * lastNormalX) * bounciness;
            vy = (vy - 2 * dotProduct * lastNormalY) * bounciness;
            System.out.println(String.format(Locale.US, "[DEBUG] Bounce! New Grid-Vel: %.2f / %.2f", vx, vy));
        }
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

    public double getPx() { return px; }
    public double getPy() { return py; }
    public boolean isMoving() { return vx != 0 || vy != 0; }
}