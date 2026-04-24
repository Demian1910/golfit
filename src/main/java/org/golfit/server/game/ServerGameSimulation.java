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
 * Manages server-side physics simulation.
 * Supports isolated physics instances per lobby to allow obstacle destruction.
 */
public class ServerGameSimulation {
    private static final Logger logger = LoggerFactory.getLogger(ServerGameSimulation.class);

    private final ServerApi serverApi;
    private final Map<Integer, ServerBall> balls = new ConcurrentHashMap<>();

    // Isolated MapData instances per lobby to track destroyed obstacles individually
    private final Map<Integer, MapData> lobbyPhysicsMaps = new ConcurrentHashMap<>();

    private ScheduledExecutorService ticker;
    private static final int TICK_RATE_MS = 40;
    private boolean forceUpdate = false;

    private final Map<Integer, Integer> playerToLobby = new ConcurrentHashMap<>();
    private final Set<Integer> finishedPlayers = ConcurrentHashMap.newKeySet();
    private final List<Integer> finishOrder = Collections.synchronizedList(new ArrayList<>());

    @FunctionalInterface public interface StrokeListener { void onStroke(int playerId); }
    @FunctionalInterface public interface GameEndListener { void onGameEnd(int lobbyId, Map<Integer, Integer> strokeResults); }

    private StrokeListener strokeListener;
    private GameEndListener gameEndListener;

    public ServerGameSimulation(ServerApi serverApi) {
        this.serverApi = serverApi;
    }

    /** * Initializes unique map data for a lobby.
     * Required for object destruction to stay isolated per game.
     */
    public void initLobbyPhysics(int lobbyId, MapData mapInstance) {
        this.lobbyPhysicsMaps.put(lobbyId, mapInstance);
    }

    public void setStrokeListener(StrokeListener listener) { this.strokeListener = listener; }
    public void setGameEndListener(GameEndListener listener) { this.gameEndListener = listener; }

    public void start() {
        ticker = Executors.newSingleThreadScheduledExecutor();
        ticker.scheduleAtFixedRate(this::tick, 0, TICK_RATE_MS, TimeUnit.MILLISECONDS);
    }

    public void stop() { if (ticker != null) ticker.shutdown(); }

    public void addPlayer(int playerId, double startX, double startY) {
        double gridX = startX / (1100.0 / MapData.COLS);
        double gridY = startY / (600.0 / MapData.ROWS);
        balls.put(playerId, new ServerBall(gridX, gridY));
        forceUpdate = true;
    }

    public void registerPlayerLobby(int playerId, int lobbyId) {
        playerToLobby.put(playerId, lobbyId);
        finishedPlayers.remove(playerId);
        finishOrder.remove(Integer.valueOf(playerId));
        ServerBall ball = balls.get(playerId);
        if (ball != null) ball.reset();
        forceUpdate = true;
    }

    public void removePlayer(int playerId) {
        balls.remove(playerId);
        playerToLobby.remove(playerId);
        finishedPlayers.remove(playerId);
        finishOrder.remove(Integer.valueOf(playerId));
        forceUpdate = true;
    }

    public void strokeBall(int playerId, double forceX, double forceY) {
        ServerBall ball = balls.get(playerId);
        if (ball != null && !ball.isFinished()) {
            ball.stroke(forceX, forceY);
            if (strokeListener != null) strokeListener.onStroke(playerId);
        }
    }

    public int getStrokeCount(int playerId) {
        ServerBall ball = balls.get(playerId);
        return ball != null ? ball.getStrokeCount() : 0;
    }

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
                MapData lobbyMap = lobbyPhysicsMaps.get(lobbyId);
                if (lobbyMap == null) continue;

                StringBuilder payloadBuilder = lobbyPayloads.computeIfAbsent(lobbyId, k -> new StringBuilder());
                boolean ballNeedsUpdate = forceUpdate;

                if (ball.isFinished()) {
                    // Still include finished balls in updates if forced, to ensure clients see them in the hole
                    if (forceUpdate) lobbiesRequiringUpdate.add(lobbyId);
                    continue;
                }

                // 1. Hole Detection
                double dx = ball.getPx() - HOLE_CENTER_X;
                double dy = ball.getPy() - HOLE_CENTER_Y;
                double distanceSquared = dx * dx + dy * dy;
                double captureRadius = 1.0;

                if (distanceSquared < (captureRadius * captureRadius)) {
                    ball.setFinished(true);
                    finishedPlayers.add(playerId);
                    finishOrder.add(playerId);
                    lobbiesRequiringUpdate.add(lobbyId);
                    checkGameEnd(playerId);
                    continue;
                }

                // 2. Physics & Terrain
                if (ball.isMoving() || forceUpdate) {
                    int col = (int) ball.getPx();
                    int row = (int) ball.getPy();

                    if (col >= 0 && col < MapData.COLS && row >= 0 && row < MapData.ROWS) {
                        int terrainType = lobbyMap.SERVER_MAP[row][col];
                        if (terrainType == 4) { // Water
                            ball.resetToLastPosition();
                            ballNeedsUpdate = true;
                        } else {
                            ball.changeFriction(getFrictionForTerrain(terrainType));
                        }
                    }

                    // Collision with nearby shapes in the isolated lobby map
                    List<MapData.ServerShape> nearby = getNearbyShapes(lobbyMap, ball.getPx(), ball.getPy(), 3.0);
                    ball.update(deltaTime, nearby);

                    // Handle Obstacle Destruction
                    List<Integer> toRemove = ball.getPendingRemovals();
                    if (!toRemove.isEmpty()) {
                        for (int shapeId : toRemove) {
                            removeShapeFromLobbyGrid(lobbyMap, shapeId);
                            try {
                                // Sync removal to clients
                                serverApi.sendObstacleChange(lobbyId, String.valueOf(shapeId));
                            } catch (Exception e) {
                                logger.error("Failed to broadcast obstacle removal", e);
                            }
                        }
                        ballNeedsUpdate = true;
                    }
                    if (ball.isMoving()) ballNeedsUpdate = true;
                }

                if (ballNeedsUpdate) lobbiesRequiringUpdate.add(lobbyId);

                payloadBuilder.append(playerId).append(":")
                        .append(String.format(java.util.Locale.US, "%.2f", ball.getPx())).append(",")
                        .append(String.format(java.util.Locale.US, "%.2f", ball.getPy())).append(";");
            }

            for (Map.Entry<Integer, StringBuilder> entry : lobbyPayloads.entrySet()) {
                if (lobbiesRequiringUpdate.contains(entry.getKey())) {
                    serverApi.broadcastMovementToLobby(entry.getKey(), entry.getValue().toString());
                }
            }
            forceUpdate = false;
        } catch (Exception e) {
            logger.error("Error in physics tick", e);
        }
    }

    public List<MapData.ServerShape> getNearbyShapes(MapData map, double worldX, double worldY, double searchRadius) {
        List<MapData.ServerShape> nearbyShapes = new ArrayList<>();
        Set<Integer> foundIds = new HashSet<>();

        int startCol = (int) Math.max(0, Math.floor(worldX - searchRadius));
        int endCol   = (int) Math.min(MapData.COLS - 1, (int) Math.floor(worldX + searchRadius));
        int startRow = (int) Math.max(0, Math.floor(worldY - searchRadius));
        int endRow   = (int) Math.min(MapData.ROWS - 1, (int) Math.floor(worldY + searchRadius));

        for (int r = startRow; r <= endRow; r++) {
            for (int c = startCol; c <= endCol; c++) {
                int id = map.SHAPE_ID_MAP[r][c];
                if (id != -1 && !foundIds.contains(id)) {
                    // Access lobby-specific shape from the map's worldShapes list/map
                    MapData.ServerShape ss = map.worldShapes.get(id);
                    if (ss != null) {
                        nearbyShapes.add(ss);
                        foundIds.add(id);
                    }
                }
            }
        }
        return nearbyShapes;
    }

    private void removeShapeFromLobbyGrid(MapData map, int shapeId) {
        for (int r = 0; r < MapData.ROWS; r++) {
            for (int c = 0; c < MapData.COLS; c++) {
                if (map.SHAPE_ID_MAP[r][c] == shapeId) {
                    map.SERVER_MAP[r][c] = 0; // Set to grass
                    map.SHAPE_ID_MAP[r][c] = -1;
                }
            }
        }
    }

    private double getFrictionForTerrain(int terrainType) {
        return switch (terrainType) {
            case 2 -> FRICTION_SAND;
            case 3 -> FRICTION_ICE;
            default -> FRICTION_GRASS;
        };
    }

    private void checkGameEnd(int finishedPlayerId) {
        if (gameEndListener == null) return;
        Integer lobbyId = playerToLobby.get(finishedPlayerId);
        if (lobbyId == null) return;

        boolean allFinished = playerToLobby.entrySet().stream()
                .filter(e -> e.getValue().equals(lobbyId))
                .allMatch(e -> finishedPlayers.contains(e.getKey()));

        if (allFinished) {
            Map<Integer, Integer> results = new LinkedHashMap<>();
            synchronized (finishOrder) {
                for (int pid : finishOrder) {
                    if (lobbyId.equals(playerToLobby.get(pid))) {
                        results.put(pid, getStrokeCount(pid));
                    }
                }
            }
            gameEndListener.onGameEnd(lobbyId, results);

            // Cleanup lobby resources
            lobbyPhysicsMaps.remove(lobbyId);
            playerToLobby.entrySet().removeIf(e -> e.getValue().equals(lobbyId));
            results.keySet().forEach(pid -> {
                finishedPlayers.remove(pid);
                finishOrder.remove(Integer.valueOf(pid));
            });
        }
    }

    /** Cheat: instantly moves a player's ball to the hole. */
    public void cheatTeleport(int playerId) {
        ServerBall ball = balls.get(playerId);
        if (ball != null && !ball.isFinished()) {
            ball.teleportToHole();
            forceUpdate = true;
        }
    }

}