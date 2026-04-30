package org.golfit.server.game;

import org.golfit.server.serverApi.ServerApi;
import org.golfit.shared.constants.MapData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.golfit.shared.constants.Constants.*;

/**
 * Manages isolated physics instances per lobby.
 * Handles movement ticks, terrain friction, obstacle destruction, and staggered spawn offsets.
 * * This class serves as the central engine for server-side physics, calculating ball trajectories,
 * collisions, and dynamic map changes like bridge/barrier toggles.
 */
public class ServerGameSimulation {
    private static final Logger logger = LoggerFactory.getLogger(ServerGameSimulation.class);

    private final ServerApi serverApi;

    /** Active balls per player (PlayerID -> Ball object) */
    private final Map<Integer, ServerBall> balls = new ConcurrentHashMap<>();

    /** Per-lobby physics maps for dynamic/destructible environments */
    private final Map<Integer, MapData> lobbyPhysicsMaps = new ConcurrentHashMap<>();

    private ScheduledExecutorService ticker;
    private static final int TICK_RATE_MS = 40;
    private boolean forceUpdate = false;

    // ---- Game state tracking ----
    private final Map<Integer, Integer> playerToLobby = new ConcurrentHashMap<>();
    private final Set<Integer> finishedPlayers = ConcurrentHashMap.newKeySet();
    private final List<Integer> finishOrder = Collections.synchronizedList(new ArrayList<>());

    /**
     * Listener interface for stroke events.
     */
    @FunctionalInterface
    public interface StrokeListener {
        void onStroke(int playerId);
    }

    /**
     * Listener interface for game completion events.
     */
    @FunctionalInterface
    public interface GameEndListener {
        void onGameEnd(int lobbyId, Map<Integer, Integer> strokeResults);
    }

    private StrokeListener strokeListener;
    private GameEndListener gameEndListener;

    /**
     * Constructs a new simulation engine.
     * @param serverApi The API interface for broadcasting state to clients.
     */
    public ServerGameSimulation(ServerApi serverApi) {
        this.serverApi = serverApi;
    }

    // ---------------- LOBBY SETUP ----------------

    /**
     * Initializes a unique physics map for a specific lobby.
     */
    public void initLobbyPhysics(int lobbyId, MapData mapInstance) {
        this.lobbyPhysicsMaps.put(lobbyId, mapInstance);
    }

    public void setStrokeListener(StrokeListener listener) {
        this.strokeListener = listener;
    }

    public void setGameEndListener(GameEndListener listener) {
        this.gameEndListener = listener;
    }

    // ---------------- LIFECYCLE ----------------

    /**
     * Starts the physics ticker at the constant TICK_RATE_MS.
     */
    public void start() {
        ticker = Executors.newSingleThreadScheduledExecutor();
        ticker.scheduleAtFixedRate(this::tick, 0, TICK_RATE_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the simulation and shuts down the executor.
     */
    public void stop() {
        if (ticker != null) ticker.shutdown();
    }

    // ---------------- PLAYER MANAGEMENT ----------------

    /**
     * Adds a player to the simulation at the default start position.
     */
    public void addPlayer(int playerId, int lobbyId) {
        balls.put(playerId, new ServerBall(MapData.BALL_START_X, MapData.BALL_START_Y));
        if (lobbyId != 0) playerToLobby.put(playerId, lobbyId);
        forceUpdate = true;
    }

    /**
     * Registers a player to a lobby with staggered spawn coordinates to prevent overlapping.
     */
    public void registerPlayerLobby(int playerId, int lobbyId) {
        long index = playerToLobby.values().stream()
                .filter(id -> id == lobbyId)
                .count();

        double startX = MapData.BALL_START_X + (index % 2) * MapData.BALL_SPAWN_SPACING;
        double startY = MapData.BALL_START_Y + (index / 2) * MapData.BALL_SPAWN_SPACING;

        logger.info("Registering player {} in lobby {} at ({}, {})", playerId, lobbyId, startX, startY);

        playerToLobby.put(playerId, lobbyId);
        finishedPlayers.remove(playerId);
        finishOrder.remove(Integer.valueOf(playerId));

        balls.put(playerId, new ServerBall(startX, startY));
        forceUpdate = true;
    }

    /**
     * Removes a player from the simulation.
     */
    public void removePlayer(int playerId) {
        balls.remove(playerId);
        playerToLobby.remove(playerId);
        finishedPlayers.remove(playerId);
        finishOrder.remove(Integer.valueOf(playerId));
        forceUpdate = true;
    }

    // ---------------- GAME ACTIONS ----------------

    /**
     * Applies an impulse force to a player's ball.
     */
    public void strokeBall(int playerId, double fx, double fy) {
        ServerBall ball = balls.get(playerId);
        if (ball != null && !ball.isFinished()) {
            ball.stroke(fx, fy);
            if (strokeListener != null) strokeListener.onStroke(playerId);
        } else if (ball == null) {
            logger.warn("Stroke for unknown player {}", playerId);
        }
    }

    /**
     * Instantly moves a ball to the hole (cheat/debug tool).
     */
    public void cheatTeleport(int playerId) {
        ServerBall ball = balls.get(playerId);
        if (ball != null && !ball.isFinished()) {
            ball.teleportToHole();
            forceUpdate = true;
        }
    }

    public int getStrokeCount(int playerId) {
        ServerBall ball = balls.get(playerId);
        return ball != null ? ball.getStrokeCount() : 0;
    }

    // ---------------- MAIN TICK ----------------

    /**
     * The core simulation loop. Handles hole detection, terrain friction,
     * collision resolution, and dynamic obstacle logic.
     */
    private void tick() {
        try {
            double dt = TICK_RATE_MS / 1000.0;
            Map<Integer, StringBuilder> payloads = new HashMap<>();
            Set<Integer> dirtyLobbies = new HashSet<>();

            for (var entry : balls.entrySet()) {
                int playerId = entry.getKey();
                ServerBall ball = entry.getValue();
                Integer lobbyId = playerToLobby.get(playerId);

                if (lobbyId == null) continue;

                MapData map = lobbyPhysicsMaps.get(lobbyId);
                if (map == null) continue;

                StringBuilder payload = payloads.computeIfAbsent(lobbyId, k -> new StringBuilder());
                boolean needsUpdate = forceUpdate;

                if (ball.isFinished()) continue;

                // ---- HOLE DETECTION ----
                double dx = ball.getPx() - HOLE_CENTER_X;
                double dy = ball.getPy() - HOLE_CENTER_Y;

                if (Math.sqrt(dx * dx + dy * dy) < HOLE_CAPTURE_RADIUS) {
                    ball.setFinished(true);
                    finishedPlayers.add(playerId);
                    finishOrder.add(playerId);
                    dirtyLobbies.add(lobbyId);

                    logger.info("Player {} finished in {} strokes", playerId, ball.getStrokeCount());
                    checkGameEnd(playerId);
                    continue;
                }

                // ---- PHYSICS & TERRAIN ----
                if (ball.isMoving() || forceUpdate) {
                    int col = (int) ball.getPx();
                    int row = (int) ball.getPy();

                    if (col >= 0 && col < MapData.COLS && row >= 0 && row < MapData.ROWS) {
                        int terrain = map.SERVER_MAP[row][col];

                        if (terrain == 4) { // Water/Out of Bounds
                            ball.resetToLastPosition();
                            needsUpdate = true;
                        } else {
                            ball.changeFriction(getFrictionForTerrain(terrain));
                        }
                    }

                    List<MapData.ServerShape> nearby = getNearbyShapes(map, ball.getPx(), ball.getPy(), 3.0);
                    ball.update(dt, nearby);

                    // ---- DYNAMIC & DESTRUCTIBLE OBJECTS ----
                    List<Integer> removed = ball.getPendingRemovals();
                    for (int id : removed) {
                        MapData.ServerShape shape = map.worldShapes.get(id);

                        if (shape != null && shape.terrainType == 6) { // Switch Trigger
                            List<ServerBall> lobbyBalls = balls.entrySet().stream()
                                    .filter(e -> lobbyId.equals(playerToLobby.get(e.getKey())))
                                    .map(Map.Entry::getValue)
                                    .toList();

                            // Toggle Bridge and Barrier states
                            map.changeBridge(false, lobbyBalls);
                            map.changeBarrier(true, lobbyBalls);

                            serverApi.sendObstacleChange(lobbyId, "97");
                            serverApi.sendObstacleChange(lobbyId, "98");

                            // Revert switch effect after 5 seconds
                            ticker.schedule(() -> revertSwitchEffect(lobbyId, lobbyBalls), 5, TimeUnit.SECONDS);

                            logger.info("Switch hit! Bridge despawned, Barrier spawned in lobby {}", lobbyId);
                        }

                        // Clean up the object from the server grid
                        removeShapeFromLobbyGrid(map, id);
                        serverApi.sendObstacleChange(lobbyId, String.valueOf(id));
                        needsUpdate = true;
                    }

                    if (ball.isMoving()) needsUpdate = true;
                }

                if (needsUpdate) dirtyLobbies.add(lobbyId);

                payload.append(playerId).append(":")
                        .append(String.format(Locale.US, "%.2f", ball.getPx())).append(",")
                        .append(String.format(Locale.US, "%.2f", ball.getPy()))
                        .append(";");
            }

            // ---- BROADCAST MOVEMENT ----
            for (var entry : payloads.entrySet()) {
                if (dirtyLobbies.contains(entry.getKey())) {
                    serverApi.broadcastMovementToLobby(entry.getKey(), entry.getValue().toString());
                }
            }

            forceUpdate = false;

        } catch (Exception e) {
            logger.error("Physics tick error", e);
        }
    }

    /**
     * Resets map obstacles to their original state after a switch trigger expires.
     */
    private void revertSwitchEffect(int lobbyId, List<ServerBall> lobbyBalls) {
        MapData lobbyMap = lobbyPhysicsMaps.get(lobbyId);

        if (lobbyMap != null) {
            lobbyMap.changeBridge(true, lobbyBalls);
            lobbyMap.changeBarrier(false, lobbyBalls);

            try {
                serverApi.sendObstacleChange(lobbyId, "97");
                serverApi.sendObstacleChange(lobbyId, "98");
            } catch (Exception e) {
                System.err.println("Failed to sync revert for lobby " + lobbyId + ": " + e.getMessage());
            }

            this.forceUpdate = true;
        }
    }

    // ---------------- HELPERS ----------------

    /**
     * Maps terrain IDs to physical friction constants.
     */
    private double getFrictionForTerrain(int t) {
        return switch (t) {
            case 2 -> FRICTION_SAND;
            case 3 -> FRICTION_ICE;
            default -> FRICTION_GRASS;
        };
    }

    /**
     * Fetches shapes within a specific radius of a coordinate for collision checking.
     */
    public List<MapData.ServerShape> getNearbyShapes(MapData map, double x, double y, double r) {
        List<MapData.ServerShape> shapes = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();

        int sc = (int) Math.max(0, Math.floor(x - r));
        int ec = (int) Math.min(MapData.COLS - 1, Math.floor(x + r));
        int sr = (int) Math.max(0, Math.floor(y - r));
        int er = (int) Math.min(MapData.ROWS - 1, Math.floor(y + r));

        for (int row = sr; row <= er; row++) {
            for (int col = sc; col <= ec; col++) {
                int id = map.SHAPE_ID_MAP[row][col];
                if (id != -1 && seen.add(id)) {
                    MapData.ServerShape s = map.worldShapes.get(id);
                    if (s != null) shapes.add(s);
                }
            }
        }
        return shapes;
    }

    /**
     * Wipes a specific shape ID from the grid and collision maps.
     */
    private void removeShapeFromLobbyGrid(MapData map, int id) {
        for (int r = 0; r < MapData.ROWS; r++) {
            for (int c = 0; c < MapData.COLS; c++) {
                if (map.SHAPE_ID_MAP[r][c] == id) {
                    map.SERVER_MAP[r][c] = 0;
                    map.SHAPE_ID_MAP[r][c] = -1;
                }
            }
        }
    }

    // ---------------- GAME END ----------------

    /**
     * Checks if all players in a lobby have finished and triggers the end-game listener.
     */
    private void checkGameEnd(int finishedPlayerId) {
        if (gameEndListener == null) return;

        Integer lobbyId = playerToLobby.get(finishedPlayerId);
        if (lobbyId == null) return;

        boolean allFinished = playerToLobby.entrySet().stream()
                .filter(e -> e.getValue().equals(lobbyId))
                .allMatch(e -> finishedPlayers.contains(e.getKey()));

        if (!allFinished) return;

        Map<Integer, Integer> results = new LinkedHashMap<>();

        synchronized (finishOrder) {
            for (int pid : finishOrder) {
                if (lobbyId.equals(playerToLobby.get(pid))) {
                    ServerBall ball = balls.get(pid);
                    if (ball != null) {
                        results.put(pid, ball.getStrokeCount());
                    }
                }
            }
        }

        gameEndListener.onGameEnd(lobbyId, results);

        // Cleanup lobby data
        lobbyPhysicsMaps.remove(lobbyId);
        playerToLobby.entrySet().removeIf(e -> e.getValue().equals(lobbyId));

        results.keySet().forEach(pid -> {
            finishedPlayers.remove(pid);
            finishOrder.remove(Integer.valueOf(pid));
        });
    }
}