package org.golfit.client.gui.settings;

import javafx.scene.paint.Color;

/** Client-side preference state, persisted for the lifetime of the process. */
public class ClientPreferences {

    private static boolean soundEnabled = true;
    private static Color preferredBallColor = null;

    /** Returns {@code true} if sound effects are enabled. */
    public static boolean isSoundEnabled() { return soundEnabled; }

    /** Enables or disables sound effects. */
    public static void setSoundEnabled(boolean enabled) { soundEnabled = enabled; }

    /**
     * Returns the player's preferred ball color, or {@code null} if none has been set
     * (server-assigned color is used in that case).
     */
    public static Color getPreferredBallColor() { return preferredBallColor; }

    /** Sets the player's preferred ball color. Pass {@code null} to revert to server-assigned color. */
    public static void setPreferredBallColor(Color color) { preferredBallColor = color; }
}
