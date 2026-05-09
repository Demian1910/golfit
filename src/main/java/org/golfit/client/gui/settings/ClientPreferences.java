package org.golfit.client.gui.settings;

import javafx.scene.paint.Color;

/** Client-side preference state, persisted for the lifetime of the process. */
public class ClientPreferences {

    private static boolean soundEnabled = true;
    private static Color preferredBallColor = null;

    public static boolean isSoundEnabled() { return soundEnabled; }
    public static void setSoundEnabled(boolean enabled) { soundEnabled = enabled; }

    public static Color getPreferredBallColor() { return preferredBallColor; }
    public static void setPreferredBallColor(Color color) { preferredBallColor = color; }
}
