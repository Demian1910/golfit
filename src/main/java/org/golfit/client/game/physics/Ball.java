package org.golfit.client.game.physics;

//position and velocity
public class Ball {
    private double px, py;
    private double vx, vy;

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

    // Called every fraction of a second by the Controller
    public void update(double deltaTime, double currentFriction) {
        // 1. Calculate how much to slow down (friction)
        // If friction is dynamic, you just pass a different value in here!
        double frictionFactor = Math.pow(currentFriction, deltaTime);
        vx *= frictionFactor;
        vy *= frictionFactor;

        // 2. If velocity is extremely low, clamp it to 0 so it stops completely
        if (Math.abs(vx) < 0.1 && Math.abs(vy) < 0.1) {
            vx = 0;
            vy = 0;
        }

        // 3. Apply velocity to position
        px += vx * deltaTime;
        py += vy * deltaTime;
    }

    public double getPx(){
        return px;
    }

    public double getPy(){
        return py;
    }
}
