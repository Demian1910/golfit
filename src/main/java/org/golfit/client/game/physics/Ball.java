package org.golfit.client.game.physics;

import org.golfit.shared.constants.Constants;

// Holds position and velocity; physics are applied by the Controller
public class Ball {
    private double px, py;
    private double vx, vy;
    private final double STOPPING_VELOCITY = Constants.STOPPING_VELOCITY;

    public Ball(double px, double py) {
        this.px = px;
        this.py = py;
        this.vx = 0;
        this.vy = 0;
    }

    public void stroke(double initialVx, double initialVy) {
        this.vx = initialVx;
        this.vy = initialVy;
    }

    /**
     * Called every frame by the Controller.
     *
     * @param deltaTime seconds since last frame
     * @param friction  terrain friction coefficient — fraction of velocity retained
     *                  per second [0–1]
     */
    public void update(double deltaTime, double friction) {
        // 1. Apply friction: fraction of velocity retained after 1 second
        double frictionFactor = Math.pow(friction, deltaTime);
        vx *= frictionFactor;
        vy *= frictionFactor;

        // 2. If velocity is too low, stop completely to avoid infinite micro-movement
        if (Math.abs(vx) < STOPPING_VELOCITY && Math.abs(vy) < STOPPING_VELOCITY) {
            vx = 0;
            vy = 0;
        }

        // 3. Apply velocity to position
        px += vx * deltaTime;
        py += vy * deltaTime;
    }

    public double getPx() {
        return px;
    }

    public double getPy() {
        return py;
    }
}
