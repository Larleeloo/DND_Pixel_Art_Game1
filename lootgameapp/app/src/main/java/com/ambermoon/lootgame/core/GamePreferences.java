package com.ambermoon.lootgame.core;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GamePreferences {
    private static final String PREFS_NAME = "loot_game_prefs";
    private static SharedPreferences prefs;

    public static void init(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static float getSfxVolume() { return prefs.getFloat("sfx_volume", 0.8f); }
    public static void setSfxVolume(float v) { prefs.edit().putFloat("sfx_volume", Math.max(0, Math.min(1, v))).apply(); }

    public static boolean isVibrationEnabled() { return prefs.getBoolean("vibration", true); }
    public static void setVibrationEnabled(boolean b) { prefs.edit().putBoolean("vibration", b).apply(); }

    public static boolean isDeveloperMode() { return prefs.getBoolean("dev_mode", false); }
    public static void setDeveloperMode(boolean b) { prefs.edit().putBoolean("dev_mode", b).apply(); }

    // --- Username ---
    public static String getUsername() { return prefs.getString("username", ""); }
    public static void setUsername(String name) {
        prefs.edit().putString("username", name).apply();
        // Track in recent usernames list
        Set<String> recent = new HashSet<>(prefs.getStringSet("recent_usernames", new HashSet<>()));
        recent.add(name);
        prefs.edit().putStringSet("recent_usernames", recent).apply();
    }
    public static List<String> getRecentUsernames() {
        return new ArrayList<>(prefs.getStringSet("recent_usernames", new HashSet<>()));
    }

    // --- Google Drive cloud sync ---
    private static final String DEFAULT_WEB_APP_URL = "https://script.google.com/macros/s/AKfycbxKACmep7-LzmbTyzCe0A0I_5QeXVTchVkv5HEaFZR-0Vm3J_ikxGbsmRxQEjwHpWodoQ/exec";

    public static String getWebAppUrl() {
        String stored = prefs.getString("web_app_url", "");
        return stored.isEmpty() ? DEFAULT_WEB_APP_URL : stored;
    }
    public static void setWebAppUrl(String url) { prefs.edit().putString("web_app_url", url).apply(); }

    public static boolean isCloudSyncEnabled() {
        return !getWebAppUrl().isEmpty() && !getUsername().isEmpty();
    }

    public static long getLastSyncTime() { return prefs.getLong("last_sync_time", 0); }
    public static void setLastSyncTime(long t) { prefs.edit().putLong("last_sync_time", t).apply(); }
}
