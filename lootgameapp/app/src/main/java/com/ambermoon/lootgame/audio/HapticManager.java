package com.ambermoon.lootgame.audio;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.ambermoon.lootgame.core.GamePreferences;

/**
 * Centralized haptic feedback manager for the Loot Game app.
 *
 * Pattern types (from design doc):
 *   MINOR     - 30-100ms, light intensity  (UI clicks, item pickups)
 *   GREATER   - 80-300ms, medium-strong     (attacks, chest opens, crafting)
 *   INTRICATE - 500-2000ms, variable        (chest opening sequence, jackpots)
 */
public class HapticManager {
    private static HapticManager instance;
    private Vibrator vibrator;

    public static HapticManager getInstance() {
        if (instance == null) instance = new HapticManager();
        return instance;
    }

    public void init(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private boolean canVibrate() {
        return vibrator != null && vibrator.hasVibrator() && GamePreferences.isVibrationEnabled();
    }

    // ─── Core pattern methods ───────────────────────────────────────────

    /** Single short pulse (30-100ms). Good for taps, item pickups, small feedback. */
    public void vibrateMinor(long ms) {
        if (!canVibrate()) return;
        long duration = Math.max(30, Math.min(100, ms));
        vibrate(duration, 80);  // light amplitude
    }

    /** Single medium-strong pulse (80-300ms). Good for crafting, chest opening, hits. */
    public void vibrateGreater(long ms) {
        if (!canVibrate()) return;
        long duration = Math.max(80, Math.min(300, ms));
        vibrate(duration, 160);  // medium-strong amplitude
    }

    /** Multi-step pattern with variable intensity. Good for big moments. */
    public void vibrateIntricate(long[] timings, int[] amplitudes) {
        if (!canVibrate()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect effect = VibrationEffect.createWaveform(timings, amplitudes, -1);
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(timings, -1);
        }
    }

    // ─── Convenience methods for specific game events ───────────────────

    /** Light tap feedback for button presses, tab switches. */
    public void tap() {
        vibrateMinor(40);
    }

    /** Item drops into vault — small satisfying tick per item. */
    public void itemDrop() {
        vibrateMinor(50);
    }

    /** Daily chest opening — hearty thump as the lid opens. */
    public void chestOpenDaily() {
        vibrateGreater(200);
    }

    /** Monthly chest opening — longer rumble for the big chest. */
    public void chestOpenMonthly() {
        // Dramatic 3-phase rumble: build-up, burst, settle
        long[] timings  = {0, 120, 60, 200, 80, 100};
        int[]  amps     = {0, 100, 0, 200, 0, 60};
        vibrateIntricate(timings, amps);
    }

    /** Crafting success on the alchemy table. */
    public void craftSuccess() {
        // Rising double-tap: short then strong
        long[] timings = {0, 60, 40, 120};
        int[]  amps    = {0, 100, 0, 200};
        vibrateIntricate(timings, amps);
    }

    /** Deconstruction success — reverse crafting feel. */
    public void deconstructSuccess() {
        // Descending double-tap: strong then fading
        long[] timings = {0, 120, 40, 60};
        int[]  amps    = {0, 200, 0, 80};
        vibrateIntricate(timings, amps);
    }

    /** Slot machine pull lever — satisfying click. */
    public void slotPull() {
        vibrateMinor(100);
    }

    /** Individual reel stopping — light click per reel. */
    public void reelStop() {
        vibrateMinor(50);
    }

    /** Slot double match win — two quick pulses. */
    public void slotWinDouble() {
        long[] timings = {0, 50, 40, 50};
        int[]  amps    = {0, 140, 0, 140};
        vibrateIntricate(timings, amps);
    }

    /** Slot triple match win — three strong pulses. */
    public void slotWinTriple() {
        long[] timings = {0, 80, 40, 80, 40, 80};
        int[]  amps    = {0, 200, 0, 200, 0, 200};
        vibrateIntricate(timings, amps);
    }

    /** Slot JACKPOT (3x Crown) — long dramatic vibration. */
    public void slotJackpot() {
        long[] timings = {0, 100, 50, 100, 50, 100, 80, 500};
        int[]  amps    = {0, 120, 0, 160, 0, 200, 0, 255};
        vibrateIntricate(timings, amps);
    }

    /** Error buzz — insufficient coins, invalid action. */
    public void errorBuzz() {
        vibrateMinor(30);
    }

    // ─── Internal ───────────────────────────────────────────────────────

    private void vibrate(long ms, int amplitude) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, amplitude));
        } else {
            vibrator.vibrate(ms);
        }
    }

    public void release() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}
