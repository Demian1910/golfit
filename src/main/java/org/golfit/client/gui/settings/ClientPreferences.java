package org.golfit.client.gui.settings;

import javafx.scene.paint.Color;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Client-side preference state, persisted for the lifetime of the process. */
public class ClientPreferences {

    private static boolean soundEnabled = true;
    private static float volume = 1.0f;
    private static Color preferredBallColor = null;

    /** Server-confirmed username → color map for all players in the current lobby. */
    private static final Map<String, Color> lobbyColors = new ConcurrentHashMap<>();

    /** Returns {@code true} if sound effects are enabled. */
    public static boolean isSoundEnabled() { return soundEnabled; }

    /** Enables or disables sound effects. */
    public static void setSoundEnabled(boolean enabled) { soundEnabled = enabled; }

    /** Returns the master volume in the range {@code 0.0}–{@code 1.0}. */
    public static float getVolume() { return volume; }

    /** Sets the master volume; {@code v} is clamped to {@code [0.0, 1.0]}. */
    public static void setVolume(float v) { volume = Math.max(0f, Math.min(1f, v)); }

    /**
     * Returns the player's preferred ball color, or {@code null} if none has been set
     * (server-assigned color is used in that case).
     */
    public static Color getPreferredBallColor() { return preferredBallColor; }

    /** Sets the player's preferred ball color. Pass {@code null} to revert to server-assigned color. */
    public static void setPreferredBallColor(Color color) { preferredBallColor = color; }

    /** Returns the server-confirmed color for a player by username, or {@code null} if unset. */
    public static Color getLobbyColor(String username) { return lobbyColors.get(username); }

    /** Stores the server-confirmed color for a player. */
    public static void setLobbyColor(String username, Color color) { lobbyColors.put(username, color); }

    /** Removes a player's color entry (e.g. on disconnect or default reset). */
    public static void removeLobbyColor(String username) { lobbyColors.remove(username); }

    /** Clears all server-confirmed lobby colors (call on lobby leave). */
    public static void clearLobbyColors() { lobbyColors.clear(); }
}
