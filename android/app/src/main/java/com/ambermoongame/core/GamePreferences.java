package com.ambermoongame.core;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages game preferences and settings using Android SharedPreferences.
 * Singleton pattern for global access.
 */
public class GamePreferences {

    private static final String PREFS_NAME = "amber_moon_prefs";

    // Preference keys
    private static final String KEY_MUSIC_VOLUME = "music_volume";
    private static final String KEY_SFX_VOLUME = "sfx_volume";
    private static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
    private static final String KEY_CLOUD_SYNC_ENABLED = "cloud_sync_enabled";
    private static final String KEY_GITHUB_TOKEN = "github_token";
    private static final String KEY_GITHUB_USER_ID = "github_user_id";
    private static final String KEY_SHOW_TOUCH_CONTROLS = "show_touch_controls";
    private static final String KEY_TOUCH_OPACITY = "touch_opacity";
    private static final String KEY_DEVELOPER_MODE = "developer_mode";
    private static final String KEY_DEBUG_MODE = "debug_mode";
    private static final String KEY_LAST_SYNC_TIME = "last_sync_time";

    private static SharedPreferences prefs;
    private static GamePreferences instance;

    private GamePreferences() {}

    /**
     * Initialize preferences with application context.
     * Must be called before accessing any preferences.
     */
    public static void initialize(Context context) {
        if (prefs == null) {
            prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            instance = new GamePreferences();
        }
    }

    public static GamePreferences getInstance() {
        if (instance == null) {
            throw new IllegalStateException("GamePreferences not initialized. Call initialize() first.");
        }
        return instance;
    }

    // ==================== Audio Settings ====================

    public static float getMusicVolume() {
        return prefs.getFloat(KEY_MUSIC_VOLUME, 0.7f);
    }

    public static void setMusicVolume(float volume) {
        prefs.edit().putFloat(KEY_MUSIC_VOLUME, Math.max(0, Math.min(1, volume))).apply();
    }

    public static float getSfxVolume() {
        return prefs.getFloat(KEY_SFX_VOLUME, 0.8f);
    }

    public static void setSfxVolume(float volume) {
        prefs.edit().putFloat(KEY_SFX_VOLUME, Math.max(0, Math.min(1, volume))).apply();
    }

    // ==================== Vibration Settings ====================

    public static boolean isVibrationEnabled() {
        return prefs.getBoolean(KEY_VIBRATION_ENABLED, true);
    }

    public static void setVibrationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply();
    }

    // ==================== Cloud Sync Settings ====================

    public static boolean isCloudSyncEnabled() {
        return prefs.getBoolean(KEY_CLOUD_SYNC_ENABLED, false);
    }

    public static void setCloudSyncEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_CLOUD_SYNC_ENABLED, enabled).apply();
    }

    public static String getGitHubToken() {
        return prefs.getString(KEY_GITHUB_TOKEN, "");
    }

    public static void setGitHubToken(String token) {
        prefs.edit().putString(KEY_GITHUB_TOKEN, token).apply();
    }

    public static String getGitHubUserId() {
        return prefs.getString(KEY_GITHUB_USER_ID, "");
    }

    public static void setGitHubUserId(String userId) {
        prefs.edit().putString(KEY_GITHUB_USER_ID, userId).apply();
    }

    public static long getLastSyncTime() {
        return prefs.getLong(KEY_LAST_SYNC_TIME, 0);
    }

    public static void setLastSyncTime(long time) {
        prefs.edit().putLong(KEY_LAST_SYNC_TIME, time).apply();
    }

    // ==================== Touch Control Settings ====================

    public static boolean isShowTouchControls() {
        return prefs.getBoolean(KEY_SHOW_TOUCH_CONTROLS, true);
    }

    public static void setShowTouchControls(boolean show) {
        prefs.edit().putBoolean(KEY_SHOW_TOUCH_CONTROLS, show).apply();
    }

    public static float getTouchOpacity() {
        return prefs.getFloat(KEY_TOUCH_OPACITY, 0.5f);
    }

    public static void setTouchOpacity(float opacity) {
        prefs.edit().putFloat(KEY_TOUCH_OPACITY, Math.max(0.1f, Math.min(1, opacity))).apply();
    }

    // ==================== Debug Settings ====================

    public static boolean isDeveloperMode() {
        return prefs.getBoolean(KEY_DEVELOPER_MODE, false);
    }

    public static void setDeveloperMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DEVELOPER_MODE, enabled).apply();
    }

    public static boolean isDebugMode() {
        return prefs.getBoolean(KEY_DEBUG_MODE, false);
    }

    public static void setDebugMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DEBUG_MODE, enabled).apply();
    }

    // ==================== Utility Methods ====================

    /**
     * Reset all preferences to defaults.
     */
    public static void resetToDefaults() {
        prefs.edit()
            .putFloat(KEY_MUSIC_VOLUME, 0.7f)
            .putFloat(KEY_SFX_VOLUME, 0.8f)
            .putBoolean(KEY_VIBRATION_ENABLED, true)
            .putBoolean(KEY_SHOW_TOUCH_CONTROLS, true)
            .putFloat(KEY_TOUCH_OPACITY, 0.5f)
            .putBoolean(KEY_DEBUG_MODE, false)
            .apply();
    }

    /**
     * Clear all saved data (for debugging/reset).
     */
    public static void clearAll() {
        prefs.edit().clear().apply();
    }
}
