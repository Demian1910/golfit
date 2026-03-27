package org.golfit.server.game;

import org.golfit.shared.constants.Constants;

import static org.golfit.shared.constants.Constants.GRASS_FRICTION;

public class ServerBall {
    private double px, py;
    private double vx, vy;
    private final double STOPPING_VELOCITY = Constants.STOPPING_VELOCITY;
    private double friction = GRASS_FRICTION;

    public ServerBall(double px, double py) {
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
     * Updates the ball's position and velocity over a given time step.
     * <p>
     * This method applies a time-adjusted friction factor to the ball's velocity components (vx, vy),
     * gradually slowing it down. If the velocity drops below a predefined {@code STOPPING_VELOCITY} threshold,
     * it is clamped to exactly zero to completely halt movement and prevent micro-drifting.
     * Finally, the ball's physical position (px, py) is updated based on the
     * resulting velocity and the elapsed time since the last frame.
     * </p>
     *
     * @param deltaTime The time elapsed since the last update tick, used to calculate
     *                  proportional friction and displacement frame-independently.
     */
    public void update(double deltaTime) {
        double frictionFactor = Math.pow(friction, deltaTime);
        vx *= frictionFactor;
        vy *= frictionFactor;

        if (Math.abs(vx) < STOPPING_VELOCITY && Math.abs(vy) < STOPPING_VELOCITY) {
            vx = 0;
            vy = 0;
        }

        px += vx * deltaTime;
        py += vy * deltaTime;
    }
    public void collectNearbyShapes(double ballX, double Y){

    }

    public double getPx() {
        return px;
    }

    public double getPy() {
        return py;
    }

    public boolean isMoving() {
        return vx != 0 || vy != 0;
    }

    public void changeFriction(double newFriction) {
        friction = newFriction;
    }
}
