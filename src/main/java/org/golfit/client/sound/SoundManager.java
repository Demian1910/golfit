package org.golfit.client.sound;

import org.golfit.client.gui.settings.ClientPreferences;

import javax.sound.sampled.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Generates and plays synthesised sound effects using {@link javax.sound.sampled}.
 * No external audio files are required — all sounds are produced programmatically.
 * Each public method is non-blocking; audio is played on a daemon thread via {@link Clip}.
 */
public class SoundManager {

    private static final int SAMPLE_RATE = 44100;

    private SoundManager() {}

    /** Downward sweep played when the local player fires a shot. */
    public static void playShot() {
        if (!ClientPreferences.isSoundEnabled()) return;
        async(() -> playBuf(buildSweep(700, 200, 140, 0.7f)));
    }

    /** Short ascending jingle played the instant the local player sinks the ball. */
    public static void playHoleIn() {
        if (!ClientPreferences.isSoundEnabled()) return;
        async(() -> playBuf(concat(
                buildTone(523, 100, 0.55f),
                buildTone(659, 100, 0.55f),
                buildTone(784, 220, 0.65f)
        )));
    }

    /** Ascending arpeggio played when the game starts. */
    public static void playGameStart() {
        if (!ClientPreferences.isSoundEnabled()) return;
        async(() -> playBuf(concat(
                buildTone(440, 90,  0.45f),
                buildTone(550, 90,  0.45f),
                buildTone(660, 160, 0.55f)
        )));
    }

    /** Victory fanfare played when the game ends (all players finished). */
    public static void playGameEnd() {
        if (!ClientPreferences.isSoundEnabled()) return;
        async(() -> playBuf(concat(
                buildTone(523,  110, 0.5f),
                buildTone(659,  110, 0.5f),
                buildTone(784,  110, 0.5f),
                buildTone(1047, 280, 0.6f)
        )));
    }

    // ── Buffer builders ───────────────────────────────────────────────────────

    private static byte[] buildTone(int hz, int ms, float amplitude) {
        float vol = amplitude * ClientPreferences.getVolume();
        int frames = SAMPLE_RATE * ms / 1000;
        byte[] buf = new byte[frames * 2];
        for (int i = 0; i < frames; i++) {
            double angle = 2 * Math.PI * i * hz / SAMPLE_RATE;
            short s = (short)(Math.sin(angle) * Short.MAX_VALUE * vol * envelope(i, frames));
            buf[i * 2]     = (byte)(s & 0xFF);
            buf[i * 2 + 1] = (byte)((s >> 8) & 0xFF);
        }
        return buf;
    }

    private static byte[] buildSweep(int startHz, int endHz, int ms, float amplitude) {
        float vol = amplitude * ClientPreferences.getVolume();
        int frames = SAMPLE_RATE * ms / 1000;
        byte[] buf = new byte[frames * 2];
        double phase = 0;
        for (int i = 0; i < frames; i++) {
            double freq = startHz + (endHz - startHz) * ((double) i / frames);
            double env  = envelope(i, frames);
            phase += 2 * Math.PI * freq / SAMPLE_RATE;
            short s = (short)(Math.sin(phase) * Short.MAX_VALUE * vol * env);
            buf[i * 2]     = (byte)(s & 0xFF);
            buf[i * 2 + 1] = (byte)((s >> 8) & 0xFF);
        }
        return buf;
    }

    private static byte[] concat(byte[]... arrays) {
        int total = 0;
        for (byte[] a : arrays) total += a.length;
        byte[] out = new byte[total];
        int pos = 0;
        for (byte[] a : arrays) { System.arraycopy(a, 0, out, pos, a.length); pos += a.length; }
        return out;
    }

    /** 5 ms fade-in + 15 ms fade-out to eliminate clicks at note boundaries. */
    private static double envelope(int frame, int total) {
        int fadeIn  = SAMPLE_RATE * 5  / 1000;
        int fadeOut = SAMPLE_RATE * 15 / 1000;
        if (frame < fadeIn)          return (double) frame / fadeIn;
        if (frame > total - fadeOut) return (double)(total - frame) / fadeOut;
        return 1.0;
    }

    // ── Playback ──────────────────────────────────────────────────────────────

    private static void async(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();
    }

    private static void playBuf(byte[] buf) {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            Clip clip = AudioSystem.getClip();
            clip.open(format, buf, 0, buf.length);
            CountDownLatch done = new CountDownLatch(1);
            clip.addLineListener(e -> { if (e.getType() == LineEvent.Type.STOP) done.countDown(); });
            clip.start();
            // Keep the daemon thread alive until the clip finishes; on macOS the audio
            // backend stops a clip when its initiating thread exits before playback ends.
            done.await(10, TimeUnit.SECONDS);
            clip.close();
        } catch (Exception e) {
            System.err.println("[SoundManager] " + e.getMessage());
        }
    }
}
