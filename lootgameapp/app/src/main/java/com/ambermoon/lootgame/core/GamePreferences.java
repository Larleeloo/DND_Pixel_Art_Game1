package com.ambermoon.lootgame.core;

import android.content.Context;
import android.content.SharedPreferences;

public class GamePreferences {
    private static final String PREFS_NAME = "loot_game_prefs";
    private static SharedPreferences prefs;

    public static void init(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static String getGoogleAccessToken() { return prefs.getString("google_access_token", ""); }
    public static void setGoogleAccessToken(String token) { prefs.edit().putString("google_access_token", token).apply(); }

    public static boolean isLoggedIn() { return !getGoogleAccessToken().isEmpty(); }

    public static void logout() { prefs.edit().remove("google_access_token").apply(); }

    public static float getSfxVolume() { return prefs.getFloat("sfx_volume", 0.8f); }
    public static void setSfxVolume(float v) { prefs.edit().putFloat("sfx_volume", Math.max(0, Math.min(1, v))).apply(); }

    public static boolean isVibrationEnabled() { return prefs.getBoolean("vibration", true); }
    public static void setVibrationEnabled(boolean b) { prefs.edit().putBoolean("vibration", b).apply(); }

    public static boolean isDeveloperMode() { return prefs.getBoolean("dev_mode", false); }
    public static void setDeveloperMode(boolean b) { prefs.edit().putBoolean("dev_mode", b).apply(); }
}
