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
 * Manages the server-side physics simulation for all active players.
 * Handles movement ticks, terrain-based friction, and water hazard resets.
 * black hole detection, stroke counting, and game-end detection.
 */
public class ServerGameSimulation {
    private static final Logger logger = LoggerFactory.getLogger(ServerGameSimulation.class);

    private final ServerApi serverApi;
    private final Map<Integer, ServerBall> balls = new ConcurrentHashMap<>();
    private ScheduledExecutorService ticker;

    private static final int TICK_RATE_MS = 40;
    private boolean forceUpdate = false;

    // ---- Game state tracking ----
    /** Maps playerId → lobbyId (registered when a game starts). */
    private final Map<Integer, Integer> playerToLobby = new ConcurrentHashMap<>();
    /** Set of playerIds who have finished (ball in hole). */
    private final Set<Integer> finishedPlayers = ConcurrentHashMap.newKeySet();
    /** Ordered list of playerIds in the order they finished (holed out). Used for tie-breaking. */
    private final List<Integer> finishOrder = Collections.synchronizedList(new ArrayList<>());

    /** Callback fired when a player's ball enters the hole. */
    @FunctionalInterface
    public interface StrokeListener {
        void onStroke(int playerId);
    }

    /** Cheat: instantly moves a player's ball to the hole. */
    public void cheatTeleport(int playerId) {
        ServerBall ball = balls.get(playerId);
        if (ball != null && !ball.isFinished()) {
            ball.teleportToHole();
            forceUpdate = true;
        }
    }

    /** Callback fired when all players in a lobby have finished. */
    @FunctionalInterface
    public interface GameEndListener {
        void onGameEnd(int lobbyId, Map<Integer, Integer> strokeResults);
    }

    private StrokeListener strokeListener;
    private GameEndListener gameEndListener;

    /**
     * Creates a new game simulation.
     *
     * @param serverApi the server API used for broadcasting movement updates
     */
    public ServerGameSimulation(ServerApi serverApi) {
        this.serverApi = serverApi;
    }

    /** Sets the callback invoked after each stroke. */
    public void setStrokeListener(StrokeListener listener) { this.strokeListener = listener; }
    /** Sets the callback invoked when all players in a lobby have finished. */
    public void setGameEndListener(GameEndListener listener) { this.gameEndListener = listener; }

    /**
     * Starts the simulation ticker at the fixed rate.
     */
    public void start() {
        ticker = Executors.newSingleThreadScheduledExecutor();
        ticker.scheduleAtFixedRate(this::tick, 0, TICK_RATE_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the simulation ticker.
     */
    public void stop() {
        if (ticker != null) {
            ticker.shutdown();
        }
    }

    /**
     * Adds a new player to the simulation.
     * Automatically converts screen coordinates to grid coordinates.
     */
    public void addPlayer(int playerId, int lobbyId) {
        balls.put(playerId, new ServerBall(MapData.BALL_START_X, MapData.BALL_START_Y));
        if (lobbyId != 0) playerToLobby.put(playerId, lobbyId);
        forceUpdate = true;
    }


    /**
     * Registers a player as belonging to a specific lobby game.
     * Called when a lobby game starts so we can track game-end per lobby.
     */
    public void registerPlayerLobby(int playerId, int lobbyId) {
        // Count players already in this lobby to compute spawn offset
        long playerIndex = playerToLobby.values().stream()
                .filter(id -> id == lobbyId)
                .count();

        logger.info("Registering player {} in lobby {} at index {}", playerId, lobbyId, playerIndex);

        double startX = MapData.BALL_START_X + (playerIndex % 2) * MapData.BALL_SPAWN_SPACING;
        double startY = MapData.BALL_START_Y + (playerIndex / 2) * MapData.BALL_SPAWN_SPACING;

        playerToLobby.put(playerId, lobbyId);
        finishedPlayers.remove(playerId);
        finishOrder.remove(Integer.valueOf(playerId));

        // Replace ball with correctly positioned one
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

    /**
     * Applies a stroke force to a specific player's ball.
     * Ignores the stroke if the ball has already finished.
     */
    public void strokeBall(int playerId, double forceX, double forceY) {
        ServerBall ball = balls.get(playerId);
        if (ball != null && !ball.isFinished()) {
            ball.stroke(forceX, forceY);
            if (strokeListener != null) {
                strokeListener.onStroke(playerId);
            }
        } else if (ball == null) {
            logger.warn("Received stroke for unknown player ID: {}", playerId);
        }
    }

    /** Returns the current stroke count for a player, or 0 if not found. */
    public int getStrokeCount(int playerId) {
        ServerBall ball = balls.get(playerId);
        return ball != null ? ball.getStrokeCount() : 0;
    }

    /**
     * Main simulation loop. Calculates physics for all balls and broadcasts updates.
     */
    private void tick() {
        try {
            double deltaTime = TICK_RATE_MS / 1000.0;
            Map<Integer, StringBuilder> lobbyPayloads = new HashMap<>();
            Set<Integer> lobbiesRequiringUpdate = new HashSet<>();

            for (Map.Entry<Integer, ServerBall> entry : balls.entrySet()) {
                int playerId = entry.getKey();
                ServerBall ball = entry.getValue();
                Integer lobbyId = playerToLobby.get(playerId);
                if (lobbyId == null) continue;

                StringBuilder payloadBuilder = lobbyPayloads.computeIfAbsent(lobbyId, k -> new StringBuilder());
                boolean ballNeedsUpdate = forceUpdate;

                if (ball.isFinished()) {
                    continue;
                }

                // 1. Check if ball is within the golf hole capture radius
                double dx = ball.getPx() - HOLE_CENTER_X;
                double dy = ball.getPy() - HOLE_CENTER_Y;
                if (Math.sqrt(dx * dx + dy * dy) < HOLE_CAPTURE_RADIUS) {
                    ball.setFinished(true);
                    finishedPlayers.add(playerId);
                    finishOrder.add(playerId); // record finish order for tie-breaking
                    lobbiesRequiringUpdate.add(lobbyId); // One final broadcast to remove them client-side
                    logger.info("Player {} holed out in {} strokes!", playerId, ball.getStrokeCount());
                    checkGameEnd(playerId);
                    continue;
                }

                // 2. Terrain and Logic Processing
                if (ball.isMoving() || forceUpdate) {
                    int col = (int) ball.getPx();
                    int row = (int) ball.getPy();

                    if (col >= 0 && col < MapData.COLS && row >= 0 && row < MapData.ROWS) {
                        int terrainType = MapData.SERVER_MAP[row][col];

                        if (terrainType == 4) { // Water Hazard
                            ball.resetToLastPosition();
                            ballNeedsUpdate = true;
                            logger.info("Player {} hit water and was reset.", playerId);
                        } else {
                            double friction = switch (terrainType) {
                                case 2 -> FRICTION_SAND;
                                case 3 -> FRICTION_ICE;
                                default -> FRICTION_GRASS;
                            };
                            ball.changeFriction(friction);
                        }
                    }

                    // 3. Physics Update
                    List<MapData.ServerShape> nearby = getNearbyShapes(ball.getPx(), ball.getPy(), 3.0);
                    ball.update(deltaTime, nearby);

                    if (ball.isMoving()) {
                        ballNeedsUpdate = true;
                    }
                }

                if (ballNeedsUpdate) {
                    lobbiesRequiringUpdate.add(lobbyId);
                }

                // 4. Generate Update Payload (ID:X,Y;)
                payloadBuilder.append(playerId).append(":")
                        .append(String.format(java.util.Locale.US, "%.2f", ball.getPx())).append(",")
                        .append(String.format(java.util.Locale.US, "%.2f", ball.getPy())).append(";");
            }

            // Broadcast only to lobbies that have movement, forced update, or player changes
            for (Map.Entry<Integer, StringBuilder> entry : lobbyPayloads.entrySet()) {
                int lobbyId = entry.getKey();
                String payload = entry.getValue().toString();
                if (lobbiesRequiringUpdate.contains(lobbyId) || forceUpdate) {
                    serverApi.broadcastMovementToLobby(lobbyId, payload);
                }
            }
            forceUpdate = false;

        } catch (Exception e) {
            logger.error("Error in physics tick", e);
        }
    }

    /**
     * Called when a player finishes. Checks if all players in the same lobby are done.
     */
    private void checkGameEnd(int finishedPlayerId) {
        if (gameEndListener == null) return;

        Integer lobbyId = playerToLobby.get(finishedPlayerId);
        if (lobbyId == null) return;

        // Check if every player in this lobby has finished
        boolean allFinished = playerToLobby.entrySet().stream()
                .filter(e -> e.getValue().equals(lobbyId))
                .allMatch(e -> finishedPlayers.contains(e.getKey()));

        if (allFinished) {
            // Build results in finish order so tie-breaking always favours the earliest finisher.
            Map<Integer, Integer> results = new LinkedHashMap<>();
            synchronized (finishOrder) {
                for (int pid : finishOrder) {
                    if (lobbyId.equals(playerToLobby.get(pid))) {
                        ServerBall ball = balls.get(pid);
                        if (ball != null) results.put(pid, ball.getStrokeCount());
                    }
                }
            }

            gameEndListener.onGameEnd(lobbyId, results);

            // Cleanup lobby's tracking data
            playerToLobby.entrySet().removeIf(e -> e.getValue().equals(lobbyId));
            results.keySet().forEach(pid -> {
                finishedPlayers.remove(pid);
                finishOrder.remove(pid);
            });
        }
    }

    /**
     * Retrieves static obstacles near the specified coordinates to optimize collision detection.
     *
     * @param worldX Current X grid position.
     * @param worldY Current Y grid position.
     * @param searchRadius Distance in grid units to check for obstacles.
     * @return List of nearby shapes.
     */
    public static List<MapData.ServerShape> getNearbyShapes(double worldX, double worldY, double searchRadius) {
        List<MapData.ServerShape> nearbyShapes = new ArrayList<>();
        Set<Integer> foundIds = new HashSet<>();

        int startCol = (int) Math.max(0, Math.floor(worldX - searchRadius));
        int endCol   = (int) Math.min(MapData.COLS - 1, (int) Math.floor(worldX + searchRadius));
        int startRow = (int) Math.max(0, Math.floor(worldY - searchRadius));
        int endRow   = (int) Math.min(MapData.ROWS - 1, (int) Math.floor(worldY + searchRadius));

        for (int r = startRow; r <= endRow; r++) {
            for (int c = startCol; c <= endCol; c++) {
                int id = MapData.SHAPE_ID_MAP[r][c];
                if (id != -1 && !foundIds.contains(id)) {
                    MapData.ServerShape ss = MapData.LEVEL_1_OBSTACLES.get(id);
                    if (ss != null) {
                        nearbyShapes.add(ss);
                        foundIds.add(id);
                    }
                }
            }
        }
        return nearbyShapes;
    }

    /**
     * Checks if the center of the ball is within the bounds of a specific shape.
     */
    private boolean isBallOverShape(ServerBall ball, MapData.ServerShape shape) {
        double bx = ball.getPx();
        double by = ball.getPy();

        if (shape.formType == MapData.ServerShape.RECT) {
            return bx >= shape.x && bx <= shape.x + shape.w &&
                    by >= shape.y && by <= shape.y + shape.h;
        }
        return false;
    }
}