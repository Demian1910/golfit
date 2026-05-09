package org.golfit.client.gui.settings;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientPreferencesTest {

    @BeforeEach
    void reset() {
        // Restore known state before each test
        ClientPreferences.setSoundEnabled(true);
        ClientPreferences.setVolume(1.0f);
        ClientPreferences.setPreferredBallColor(null);
        ClientPreferences.clearLobbyColors();
    }

    // ── Sound ──────────────────────────────────────────────────────────────────

    @Test
    void soundEnabled_defaultIsTrue() {
        assertTrue(ClientPreferences.isSoundEnabled());
    }

    @Test
    void setSoundEnabled_togglesValue() {
        ClientPreferences.setSoundEnabled(false);
        assertFalse(ClientPreferences.isSoundEnabled());
        ClientPreferences.setSoundEnabled(true);
        assertTrue(ClientPreferences.isSoundEnabled());
    }

    // ── Volume ─────────────────────────────────────────────────────────────────

    @Test
    void volume_defaultIsOne() {
        assertEquals(1.0f, ClientPreferences.getVolume(), 0.001f);
    }

    @Test
    void setVolume_negativeClampsToZero() {
        ClientPreferences.setVolume(-0.5f);
        assertEquals(0.0f, ClientPreferences.getVolume(), 0.001f);
    }

    @Test
    void setVolume_aboveOneClampsToOne() {
        ClientPreferences.setVolume(2.0f);
        assertEquals(1.0f, ClientPreferences.getVolume(), 0.001f);
    }

    @Test
    void setVolume_midRangeStoredExactly() {
        ClientPreferences.setVolume(0.42f);
        assertEquals(0.42f, ClientPreferences.getVolume(), 0.001f);
    }

    @Test
    void setVolume_zeroIsValid() {
        ClientPreferences.setVolume(0.0f);
        assertEquals(0.0f, ClientPreferences.getVolume(), 0.001f);
    }

    // ── Preferred ball color ───────────────────────────────────────────────────

    @Test
    void preferredBallColor_defaultIsNull() {
        assertNull(ClientPreferences.getPreferredBallColor());
    }

    @Test
    void setPreferredBallColor_storesAndRetrieves() {
        ClientPreferences.setPreferredBallColor(Color.RED);
        assertEquals(Color.RED, ClientPreferences.getPreferredBallColor());
    }

    @Test
    void setPreferredBallColor_nullClearsValue() {
        ClientPreferences.setPreferredBallColor(Color.BLUE);
        ClientPreferences.setPreferredBallColor(null);
        assertNull(ClientPreferences.getPreferredBallColor());
    }

    // ── Lobby colors ───────────────────────────────────────────────────────────

    @Test
    void getLobbyColor_unknownPlayerReturnsNull() {
        assertNull(ClientPreferences.getLobbyColor("nobody"));
    }

    @Test
    void setAndGetLobbyColor_roundTrip() {
        ClientPreferences.setLobbyColor("alice", Color.GREEN);
        assertEquals(Color.GREEN, ClientPreferences.getLobbyColor("alice"));
    }

    @Test
    void removeLobbyColor_removesEntry() {
        ClientPreferences.setLobbyColor("bob", Color.BLUE);
        ClientPreferences.removeLobbyColor("bob");
        assertNull(ClientPreferences.getLobbyColor("bob"));
    }

    @Test
    void clearLobbyColors_removesAllEntries() {
        ClientPreferences.setLobbyColor("alice", Color.RED);
        ClientPreferences.setLobbyColor("bob", Color.BLUE);
        ClientPreferences.clearLobbyColors();
        assertNull(ClientPreferences.getLobbyColor("alice"));
        assertNull(ClientPreferences.getLobbyColor("bob"));
    }

    @Test
    void setLobbyColor_overwritesExistingEntry() {
        ClientPreferences.setLobbyColor("charlie", Color.RED);
        ClientPreferences.setLobbyColor("charlie", Color.GOLD);
        assertEquals(Color.GOLD, ClientPreferences.getLobbyColor("charlie"));
    }
}
