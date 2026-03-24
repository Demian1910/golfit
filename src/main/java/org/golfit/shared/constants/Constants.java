package org.golfit.shared.constants;

/** Global configuration constants for timeouts, packet sizes, and retry limits. */
public class Constants {
    /**
     * Protocol
     */
    public static final int TIMEOUT_MS = 2000;
    public static final int MAX_PACKET_SIZE = 8192;
    public static final int TIMEOUT_PING_MS = 10000;
    public static final int MAX_RETRIES = 3;

    /** Visual radius of the golf ball. Unit: px. */
    public static final double BALL_RADIUS = 10;

    /**
     * Fraction of velocity retained per second on grass (0 = instant stop, 1 = no
     * friction). Unit: dimensionless [0–1].
     */
    public static final double GRASS_FRICTION = 0.5;
    public static final double SAND_FRICTION = 0.2;

    /**
     * Both velocity components must drop below this to stop the ball completely.
     * Unit: px/s.
     */
    public static final double STOPPING_VELOCITY = 20;
}