package org.golfit.client.game;

/** Client-side ball model holding pixel position. Physics are computed server-side. */
public class Ball {
    private double px, py;

    /**
     * Creates a ball at the given pixel position.
     *
     * @param px the x pixel coordinate
     * @param py the y pixel coordinate
     */
    public Ball(double px, double py) {
        this.px = px;
        this.py = py;
    }


    /** Returns the x pixel coordinate. */
    public double getPx() {
        return px;
    }

    /** Returns the y pixel coordinate. */
    public double getPy() {
        return py;
    }
}
