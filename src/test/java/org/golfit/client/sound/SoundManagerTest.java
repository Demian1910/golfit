package org.golfit.client.sound;

import org.golfit.client.gui.settings.ClientPreferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Verifies that SoundManager methods are safe to call regardless of
 * audio hardware availability. Audio is played on daemon threads and
 * errors are swallowed internally, so nothing should propagate to callers.
 */
class SoundManagerTest {

    @BeforeEach
    void enableSound() {
        ClientPreferences.setSoundEnabled(true);
        ClientPreferences.setVolume(1.0f);
    }

    @AfterEach
    void restoreDefaults() {
        ClientPreferences.setSoundEnabled(true);
        ClientPreferences.setVolume(1.0f);
    }

    @Test
    void playShot_soundEnabled_doesNotThrow() {
        assertDoesNotThrow(SoundManager::playShot);
    }

    @Test
    void playHoleIn_soundEnabled_doesNotThrow() {
        assertDoesNotThrow(SoundManager::playHoleIn);
    }

    @Test
    void playGameStart_soundEnabled_doesNotThrow() {
        assertDoesNotThrow(SoundManager::playGameStart);
    }

    @Test
    void playGameEnd_soundEnabled_doesNotThrow() {
        assertDoesNotThrow(SoundManager::playGameEnd);
    }

    @Test
    void playShot_soundDisabled_doesNotThrow() {
        ClientPreferences.setSoundEnabled(false);
        assertDoesNotThrow(SoundManager::playShot);
    }

    @Test
    void allMethods_volumeZero_doesNotThrow() {
        ClientPreferences.setVolume(0.0f);
        assertDoesNotThrow(SoundManager::playShot);
        assertDoesNotThrow(SoundManager::playHoleIn);
        assertDoesNotThrow(SoundManager::playGameStart);
        assertDoesNotThrow(SoundManager::playGameEnd);
    }
}
