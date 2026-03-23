package org.golfit.client.game;

// Holds position and velocity; physics are applied by the Controller
public class Ball {
    private double px, py;

    public Ball(double px, double py) {
        this.px = px;
        this.py = py;
    }

    public double getPx() {
        return px;
    }

    public double getPy() {
        return py;
    }
}
