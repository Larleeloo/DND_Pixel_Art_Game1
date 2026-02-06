package com.ambermoon.lootgame.save;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class SaveManager {
    private static final String TAG = "SaveManager";
    private static final String SAVE_FILE = "loot_game_save.json";
    private static SaveManager instance;
    private Context context;
    private SaveData data;

    public static SaveManager getInstance() { return instance; }

    public static void init(Context ctx) {
        instance = new SaveManager();
        instance.context = ctx.getApplicationContext();
        instance.load();
    }

    public SaveData getData() { return data; }

    public void save() {
        data.lastModified = System.currentTimeMillis();
        try {
            String json = serializeToJson(data);
            FileOutputStream fos = context.openFileOutput(SAVE_FILE, Context.MODE_PRIVATE);
            fos.write(json.getBytes("UTF-8"));
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "Save failed: " + e.getMessage());
        }
    }

    public void load() {
        try {
            FileInputStream fis = context.openFileInput(SAVE_FILE);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = fis.read(buf)) != -1) baos.write(buf, 0, n);
            fis.close();
            data = deserializeFromJson(baos.toString("UTF-8"));
        } catch (FileNotFoundException e) {
            data = new SaveData();
            checkDailyStreak();
        } catch (Exception e) {
            Log.e(TAG, "Load failed: " + e.getMessage());
            data = new SaveData();
        }
    }

    // --- Cooldown checks ---

    public boolean canOpenDailyChest() {
        if (com.ambermoon.lootgame.core.GamePreferences.isDeveloperMode()) return true;
        return System.currentTimeMillis() - data.dailyChestLastOpened >= 24L * 60 * 60 * 1000;
    }

    public boolean canOpenMonthlyChest() {
        if (com.ambermoon.lootgame.core.GamePreferences.isDeveloperMode()) return true;
        return System.currentTimeMillis() - data.monthlyChestLastOpened >= 30L * 24 * 60 * 60 * 1000;
    }

    public long getDailyTimeRemaining() {
        long elapsed = System.currentTimeMillis() - data.dailyChestLastOpened;
        return Math.max(0, 24L * 60 * 60 * 1000 - elapsed);
    }

    public long getMonthlyTimeRemaining() {
        long elapsed = System.currentTimeMillis() - data.monthlyChestLastOpened;
        return Math.max(0, 30L * 24 * 60 * 60 * 1000 - elapsed);
    }

    public void recordDailyChestOpened() { data.dailyChestLastOpened = System.currentTimeMillis(); }
    public void recordMonthlyChestOpened() { data.monthlyChestLastOpened = System.currentTimeMillis(); }

    public void addCoins(int amount) {
        data.coins += amount;
        data.totalCoinsEarned += amount;
    }

    public boolean spendCoins(int amount) {
        if (data.coins < amount) return false;
        data.coins -= amount;
        data.totalCoinsSpent += amount;
        return true;
    }

    public void addVaultItem(String itemId, int count) {
        for (SaveData.VaultItem vi : data.vaultItems) {
            if (vi.itemId.equals(itemId)) {
                vi.stackCount += count;
                return;
            }
        }
        data.vaultItems.add(new SaveData.VaultItem(itemId, count));
    }

    public boolean removeVaultItem(String itemId, int count) {
        for (int i = 0; i < data.vaultItems.size(); i++) {
            SaveData.VaultItem vi = data.vaultItems.get(i);
            if (vi.itemId.equals(itemId)) {
                if (vi.stackCount < count) return false;
                vi.stackCount -= count;
                if (vi.stackCount <= 0) data.vaultItems.remove(i);
                return true;
            }
        }
        return false;
    }

    public int getVaultItemCount(String itemId) {
        for (SaveData.VaultItem vi : data.vaultItems) {
            if (vi.itemId.equals(itemId)) return vi.stackCount;
        }
        return 0;
    }

    private void checkDailyStreak() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        if (!today.equals(data.lastLoginDate)) {
            // Check if yesterday
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -1);
            String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
            if (yesterday.equals(data.lastLoginDate)) {
                data.consecutiveDays++;
            } else {
                data.consecutiveDays = 1;
            }
            data.lastLoginDate = today;
        }
    }

    // --- JSON serialization (manual, no external deps) ---

    private String serializeToJson(SaveData d) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"version\": ").append(d.version).append(",\n");
        sb.append("  \"platform\": \"").append(d.platform).append("\",\n");
        sb.append("  \"lastModified\": ").append(d.lastModified).append(",\n");
        sb.append("  \"coins\": ").append(d.coins).append(",\n");
        sb.append("  \"totalCoinsEarned\": ").append(d.totalCoinsEarned).append(",\n");
        sb.append("  \"totalCoinsSpent\": ").append(d.totalCoinsSpent).append(",\n");
        sb.append("  \"dailyChestLastOpened\": ").append(d.dailyChestLastOpened).append(",\n");
        sb.append("  \"monthlyChestLastOpened\": ").append(d.monthlyChestLastOpened).append(",\n");
        sb.append("  \"totalItemsCollected\": ").append(d.totalItemsCollected).append(",\n");
        sb.append("  \"legendaryItemsFound\": ").append(d.legendaryItemsFound).append(",\n");
        sb.append("  \"mythicItemsFound\": ").append(d.mythicItemsFound).append(",\n");
        sb.append("  \"consecutiveDays\": ").append(d.consecutiveDays).append(",\n");
        sb.append("  \"lastLoginDate\": \"").append(d.lastLoginDate).append("\",\n");
        sb.append("  \"slotMachinePulls\": ").append(d.slotMachinePulls).append(",\n");
        sb.append("  \"biggestJackpot\": ").append(d.biggestJackpot).append(",\n");
        sb.append("  \"vaultItems\": [\n");
        for (int i = 0; i < d.vaultItems.size(); i++) {
            SaveData.VaultItem vi = d.vaultItems.get(i);
            sb.append("    {\"itemId\": \"").append(vi.itemId).append("\", \"stackCount\": ").append(vi.stackCount).append("}");
            if (i < d.vaultItems.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }

    private SaveData deserializeFromJson(String json) {
        SaveData d = new SaveData();
        d.version = extractInt(json, "version", 1);
        d.lastModified = extractLong(json, "lastModified", 0);
        d.coins = extractInt(json, "coins", 500);
        d.totalCoinsEarned = extractLong(json, "totalCoinsEarned", 500);
        d.totalCoinsSpent = extractLong(json, "totalCoinsSpent", 0);
        d.dailyChestLastOpened = extractLong(json, "dailyChestLastOpened", 0);
        d.monthlyChestLastOpened = extractLong(json, "monthlyChestLastOpened", 0);
        d.totalItemsCollected = extractInt(json, "totalItemsCollected", 0);
        d.legendaryItemsFound = extractInt(json, "legendaryItemsFound", 0);
        d.mythicItemsFound = extractInt(json, "mythicItemsFound", 0);
        d.consecutiveDays = extractInt(json, "consecutiveDays", 0);
        d.lastLoginDate = extractString(json, "lastLoginDate", "");
        d.slotMachinePulls = extractInt(json, "slotMachinePulls", 0);
        d.biggestJackpot = extractInt(json, "biggestJackpot", 0);

        // Parse vault items array
        int vaultStart = json.indexOf("\"vaultItems\"");
        if (vaultStart != -1) {
            int arrStart = json.indexOf('[', vaultStart);
            int arrEnd = json.indexOf(']', arrStart);
            if (arrStart != -1 && arrEnd != -1) {
                String arr = json.substring(arrStart + 1, arrEnd);
                int objStart = 0;
                while ((objStart = arr.indexOf('{', objStart)) != -1) {
                    int objEnd = arr.indexOf('}', objStart);
                    if (objEnd == -1) break;
                    String obj = arr.substring(objStart, objEnd + 1);
                    String itemId = extractString(obj, "itemId", "");
                    int count = extractInt(obj, "stackCount", 1);
                    if (!itemId.isEmpty()) {
                        d.vaultItems.add(new SaveData.VaultItem(itemId, count));
                    }
                    objStart = objEnd + 1;
                }
            }
        }

        checkDailyStreak();
        return d;
    }

    private static int extractInt(String json, String key, int def) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx == -1) return def;
        int colon = json.indexOf(':', idx);
        if (colon == -1) return def;
        StringBuilder num = new StringBuilder();
        for (int i = colon + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (Character.isDigit(c) || c == '-') num.append(c);
            else if (num.length() > 0) break;
        }
        try { return Integer.parseInt(num.toString()); } catch (Exception e) { return def; }
    }

    private static long extractLong(String json, String key, long def) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx == -1) return def;
        int colon = json.indexOf(':', idx);
        if (colon == -1) return def;
        StringBuilder num = new StringBuilder();
        for (int i = colon + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (Character.isDigit(c) || c == '-') num.append(c);
            else if (num.length() > 0) break;
        }
        try { return Long.parseLong(num.toString()); } catch (Exception e) { return def; }
    }

    private static String extractString(String json, String key, String def) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx == -1) return def;
        int colon = json.indexOf(':', idx);
        if (colon == -1) return def;
        int q1 = json.indexOf('"', colon + 1);
        if (q1 == -1) return def;
        int q2 = json.indexOf('"', q1 + 1);
        if (q2 == -1) return def;
        return json.substring(q1 + 1, q2);
    }

    public String toJson() { return serializeToJson(data); }
    public void fromJson(String json) { data = deserializeFromJson(json); }
}
