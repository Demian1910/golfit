package org.golfit.server.game;

import org.golfit.shared.constants.Constants;
import org.golfit.shared.constants.MapData;

import java.util.List;
import java.util.Locale;

import static org.golfit.shared.constants.Constants.FRICTION_GRASS;

public class ServerBall {
    private double px, py;
    private double vx, vy;
    private final double radius;
    private final double STOPPING_VELOCITY = Constants.STOPPING_VELOCITY;
    private double friction = FRICTION_GRASS;

    private double lastNormalX, lastNormalY;
    private double overlap;


    private final double BASE_WIDTH = 1100.0;
    private final double BASE_HEIGHT = 600.0;

    public ServerBall(double px, double py) {
        this.px = (px > MapData.COLS) ? px / (BASE_WIDTH / MapData.COLS) : px;
        this.py = (py > MapData.ROWS) ? py / (BASE_HEIGHT / MapData.ROWS) : py;

        this.vx = 0;
        this.vy = 0;
        this.radius = 0.5; // Halbe Grid-Zelle
    }

    public void stroke(double initialVx, double initialVy) {
        this.vx = initialVx;
        this.vy = initialVy;

        // REALISMUS-CHECK: Ein Hard-Limit für die physikalische Stabilität.
        // Verhindert, dass der Ball durch Wände glitched, wenn der Client manipulierte Werte sendet.
        double maxGridSpeed = 60.0;
        double currentSpeed = Math.sqrt(vx * vx + vy * vy);

        if (currentSpeed > maxGridSpeed) {
            double ratio = maxGridSpeed / currentSpeed;
            this.vx *= ratio;
            this.vy *= ratio;
        }

        System.out.println(String.format(Locale.US,
                "[DEBUG] Stroke processed. Speed: %.2f | Grid-Vel: %.2f / %.2f",
                Math.sqrt(vx*vx + vy*vy), vx, vy));
    }

    public void update(double deltaTime, List<MapData.ServerShape> nearbyShapes) {
        // In ServerBall.update()
        double currentFriction = this.friction;

        // Realismus-Trick: Wenn der Ball langsamer als 3 Einheiten/s ist,
        // greift die Bodenhaftung stärker (höhere Reibung = kleinerer Wert).
        double speed = Math.sqrt(vx * vx + vy * vy);
        if (speed < 3.0) {
            // Progressiv stärker bremsen, je langsamer er wird
            currentFriction *= 0.85;
        }

        double frictionFactor = Math.pow(currentFriction, deltaTime);
        vx *= frictionFactor;
        vy *= frictionFactor;

        // Sehr niedrige Stopping Velocity für sanftes Ausrollen
        if (speed < 0.15) {
            vx = 0;
            vy = 0;
        }
        // Debugging für Reibung (nur wenn der Ball sich bewegt)
        if (isMoving()) {
            System.out.format(Locale.US, "[PHYSICS] Grid: %.2f,%.2f | Vel: %.2f,%.2f | Friction: %.3f%n",
                    px, py, vx, vy, friction);
        }

        if (Math.abs(vx) < STOPPING_VELOCITY && Math.abs(vy) < STOPPING_VELOCITY) {
            vx = 0;
            vy = 0;
            return;
        }

        // 2. Bewegung ausführen
        px += vx * deltaTime;
        py += vy * deltaTime;

        // 3. Kollisionsprüfung (NUR für Wände/Bounce-Objekte)
        for (MapData.ServerShape shape : nearbyShapes) {

            // Check: Ist das Objekt überhaupt eine Wand?
            if (shape.terrainType == 1 || shape.terrainType == 5) {

                if (checkCollision(shape)) {
                    // Detail-Log für die Kollision
                    System.out.println("[COLLISION] Hit Wall ID: " + shape.terrainType  +
                            " at Pos: " + shape.x + "/" + shape.y +
                            " | Ball Overlap: " + String.format("%.4f", overlap));

                    resolveCollision();

                    // Log nach dem Abprall
                    System.out.format(Locale.US, "[BOUNCE] New Velocity: %.2f,%.2f%n", vx, vy);
                    break;
                }
            } else {
                // Dies ist Terrain (Sand/Eis/etc.) -> Wir ignorieren die physische Kollision hier
                // Aber wir können loggen, dass wir es sehen, aber nicht davon abprallen
                // System.out.println("[DEBUG] Passing over Terrain Type: " + shape.userData);
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

    public void changeFriction(double currentFriction) {
        this.friction = currentFriction;
    }
}