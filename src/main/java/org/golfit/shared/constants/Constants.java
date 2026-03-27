package org.golfit.shared.constants;

/** Global configuration constants for timeouts, packet sizes, and retry limits. */
public class Constants {
    /**
     * Protocol
     */
    public static final int RECT = 0;
    public static final int ELLIPSE = 1;
    public static final int POLYGON = 2;
    private static final int EMPTY = 0;
    private static final int WALL  = 1;
    private static final int SAND  = 2;
    private static final int ICE   = 3;
    private static final int WATER = 4;
    private static final int BOUNCE = 5;
    private static final int SWITCH = 6;
    private static final int COLS = 80;
    private static final int ROWS = 45;
    private static final int COURSE_COLS = 65;
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