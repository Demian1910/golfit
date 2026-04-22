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

    /** Visual radius of the golf ball. Unit: grid */
    public static final double BALL_RADIUS = 0.4;

    /** Geometry
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

    /** GUI
     */

    public static final int INIT_WIDTH = 1280;
    public static final int INIT_HEIGHT = 720;


    /**
     * Events
     */
    public static final String BROADCAST_MESSAGE = "BROADCAST_MESSAGE";
    public static final String PRIVATE_MESSAGE= "PRIVATE_MESSAGE";
    public static final String LOBBY_MESSAGE = "LOBBY_MESSAGE";
    public static final String LOBBY_LIST =  "LOBBY_LIST";
    public static final String LOBBY_JOINED = "LOBBY_JOINED";
    public static final String LOBBY_LEFT = "LOBBY_LEFT";
    public static final String LOBBY_INFO =  "LOBBY_INFO";
    public static final String PLAYERS_LIST =  "PLAYERS_LIST";
    public static final String GAME_START    =  "GAME_START";
    public static final String STROKE_UPDATE =  "STROKE_UPDATE"; // EventBus event for stroke count updates
    public static final String GAME_END      =  "GAME_END";      // EventBus event for game-over results
    public static final String OBSTACLE_UPDATE = "OBSTACLE_UPDATE";

    /** Golf hole position in grid coordinates (matches Course.java addGolfHole(65, 9) with width=3, height=1) */
    public static final double HOLE_CENTER_X    = 66.5;
    public static final double HOLE_CENTER_Y    = 9.5;
    public static final double HOLE_CAPTURE_RADIUS = 1.3;



    // Frictions
    public static final double FRICTION_GRASS = 0.3;
    public static final double FRICTION_SAND  = 0.07;
    public static final double FRICTION_ICE   = 0.7;
    public static final double FRICTION_WATER = 0.00;
    public static final double FRICTION_BOUNCE = 0.6;
    /**
     * Both velocity components must drop below this to stop the ball completely.
     * Unit: grid/s
     */
    public static final double STOPPING_VELOCITY = 1.5;

}