package com.ambermoon.lootgame.save;

import android.content.Context;
import android.util.Log;

import com.ambermoon.lootgame.entity.ItemRegistry;
import com.ambermoon.lootgame.entity.RecipeManager;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class SaveManager {
    private static final String TAG = "SaveManager";
    private static final String SAVE_FILE_PREFIX = "loot_game_save_";
    private static final String SAVE_FILE_SUFFIX = ".json";
    private static SaveManager instance;
    private Context context;
    private SaveData data;
    private String currentUsername;

    public static SaveManager getInstance() { return instance; }

    public static void init(Context ctx) {
        instance = new SaveManager();
        instance.context = ctx.getApplicationContext();
        instance.currentUsername = com.ambermoon.lootgame.core.GamePreferences.getUsername();
        instance.load();
        instance.validateSaveData();
    }

    /**
     * Returns the save filename for the current user.
     */
    private String getSaveFileName() {
        if (currentUsername == null || currentUsername.isEmpty()) {
            return "loot_game_save.json"; // fallback
        }
        return SAVE_FILE_PREFIX + currentUsername + SAVE_FILE_SUFFIX;
    }

    public String getCurrentUsername() { return currentUsername; }

    public SaveData getData() { return data; }

    /**
     * Save locally AND upload to cloud automatically.
     * Call this after any gameplay action (chest, craft, slot, etc).
     */
    public void save() {
        saveLocal();
        // Auto-upload to cloud in the background
        if (com.ambermoon.lootgame.core.GamePreferences.isCloudSyncEnabled()) {
            GoogleDriveSyncManager.getInstance().syncToCloud(null);
        }
    }

    /**
     * Save locally only (no cloud upload).
     * Used internally after cloud download to avoid re-uploading what was just fetched.
     */
    public void saveLocal() {
        data.lastModified = System.currentTimeMillis();
        data.lastModifiedDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.US).format(new Date(data.lastModified));
        data.appVersion = com.ambermoon.lootgame.core.UsernameActivity.APP_VERSION;
        try {
            String json = serializeToJson(data);
            FileOutputStream fos = context.openFileOutput(getSaveFileName(), Context.MODE_PRIVATE);
            fos.write(json.getBytes("UTF-8"));
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "Save failed: " + e.getMessage());
        }
    }

    public void load() {
        try {
            FileInputStream fis = context.openFileInput(getSaveFileName());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = fis.read(buf)) != -1) baos.write(buf, 0, n);
            fis.close();
            data = deserializeFromJson(baos.toString("UTF-8"));
            // Persist streak update immediately so it survives even if
            // the user closes the app without performing a save action.
            saveLocal();
        } catch (FileNotFoundException e) {
            data = new SaveData();
            checkDailyStreak();
            saveLocal();
        } catch (Exception e) {
            Log.e(TAG, "Load failed: " + e.getMessage());
            data = new SaveData();
            checkDailyStreak();
            saveLocal();
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

    // --- Loadout operations (25-slot inventory for Amber Moon transfer) ---

    public static final int MAX_LOADOUT_SLOTS = 25;

    public int getLoadoutSlotCount() {
        return data.loadoutItems.size();
    }

    public int getLoadoutItemCount(String itemId) {
        for (SaveData.VaultItem vi : data.loadoutItems) {
            if (vi.itemId.equals(itemId)) return vi.stackCount;
        }
        return 0;
    }

    /**
     * Move one item from vault to loadout.
     * Each item in loadout occupies one slot (no stacking). Max 25 slots.
     */
    public boolean moveToLoadout(String itemId) {
        if (data.loadoutItems.size() >= MAX_LOADOUT_SLOTS) return false;
        if (!removeVaultItem(itemId, 1)) return false;
        // Each item in loadout is a separate single-count entry (one per slot)
        data.loadoutItems.add(new SaveData.VaultItem(itemId, 1));
        save();
        return true;
    }

    /**
     * Move one item from loadout back to vault.
     * Removes by index since loadout slots are positional.
     */
    public boolean moveFromLoadout(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= data.loadoutItems.size()) return false;
        SaveData.VaultItem removed = data.loadoutItems.remove(slotIndex);
        addVaultItem(removed.itemId, 1);
        save();
        return true;
    }

    /**
     * Move one item from loadout back to vault by item ID (removes first match).
     */
    public boolean moveFromLoadoutByItemId(String itemId) {
        for (int i = 0; i < data.loadoutItems.size(); i++) {
            if (data.loadoutItems.get(i).itemId.equals(itemId)) {
                return moveFromLoadout(i);
            }
        }
        return false;
    }

    public void addLearnedRecipe(String id, String name, java.util.List<String> ingredients, String result, int resultCount) {
        if (id == null || id.isEmpty()) return;
        for (SaveData.LearnedRecipe lr : data.learnedRecipes) {
            if (lr.id.equals(id)) return; // already learned
        }
        data.learnedRecipes.add(new SaveData.LearnedRecipe(id, name, ingredients, result, resultCount));
    }

    public boolean isRecipeLearned(String recipeId) {
        for (SaveData.LearnedRecipe lr : data.learnedRecipes) {
            if (lr.id.equals(recipeId)) return true;
        }
        return false;
    }

    // --- Background unlocks ---

    public boolean unlockBackground(String backgroundId) {
        if (data.unlockedBackgrounds.contains(backgroundId)) return false;
        data.unlockedBackgrounds.add(backgroundId);
        return true;
    }

    public boolean isBackgroundUnlocked(String backgroundId) {
        return data.unlockedBackgrounds.contains(backgroundId);
    }

    public java.util.Set<String> getUnlockedBackgroundIds() {
        return new java.util.HashSet<>(data.unlockedBackgrounds);
    }

    /**
     * Validates all save data against the current ItemRegistry and RecipeManager.
     * Removes vault/loadout/listing entries whose item IDs no longer exist,
     * and removes learned recipes whose ingredients, result, or recipe definition
     * have been changed or removed.
     * Called automatically on init() after load().
     * Returns the total number of entries removed.
     */
    public int validateSaveData() {
        int removed = 0;

        // --- Purge vault items with invalid IDs ---
        Iterator<SaveData.VaultItem> vaultIt = data.vaultItems.iterator();
        while (vaultIt.hasNext()) {
            if (!ItemRegistry.exists(vaultIt.next().itemId)) {
                vaultIt.remove();
                removed++;
            }
        }

        // --- Purge loadout items with invalid IDs ---
        Iterator<SaveData.VaultItem> loadoutIt = data.loadoutItems.iterator();
        while (loadoutIt.hasNext()) {
            if (!ItemRegistry.exists(loadoutIt.next().itemId)) {
                loadoutIt.remove();
                removed++;
            }
        }

        // --- Purge player listings with invalid IDs ---
        Iterator<SaveData.PlayerListing> listingIt = data.playerListings.iterator();
        while (listingIt.hasNext()) {
            if (!ItemRegistry.exists(listingIt.next().itemId)) {
                listingIt.remove();
                removed++;
            }
        }

        // --- Purge learned recipes that are no longer valid ---
        Iterator<SaveData.LearnedRecipe> recipeIt = data.learnedRecipes.iterator();
        while (recipeIt.hasNext()) {
            SaveData.LearnedRecipe lr = recipeIt.next();
            if (!isLearnedRecipeValid(lr)) {
                recipeIt.remove();
                removed++;
            }
        }

        if (removed > 0) {
            Log.d(TAG, "Validated save data: removed " + removed + " invalid entries");
            save();
        }
        return removed;
    }

    /**
     * Checks whether a learned recipe is still valid:
     * - Result item must exist in ItemRegistry
     * - All ingredient items must exist in ItemRegistry
     * - A matching recipe must still exist in RecipeManager
     */
    private boolean isLearnedRecipeValid(SaveData.LearnedRecipe lr) {
        if (lr.result == null || lr.result.isEmpty()) return false;
        if (!ItemRegistry.exists(lr.result)) return false;

        if (lr.ingredients == null || lr.ingredients.isEmpty()) return false;
        for (String ing : lr.ingredients) {
            if (ing == null || !ItemRegistry.exists(ing)) return false;
        }

        // Verify the recipe still exists in RecipeManager with matching definition
        RecipeManager.Recipe current = RecipeManager.findRecipe(lr.ingredients);
        if (current == null) return false;
        if (!lr.result.equals(current.result)) return false;

        return true;
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
        sb.append("  \"appVersion\": \"").append(d.appVersion).append("\",\n");
        sb.append("  \"platform\": \"").append(d.platform).append("\",\n");
        sb.append("  \"pin\": \"").append(d.pin).append("\",\n");
        sb.append("  \"lastModified\": ").append(d.lastModified).append(",\n");
        sb.append("  \"lastModifiedDate\": \"").append(d.lastModifiedDate).append("\",\n");
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
        sb.append("  ],\n");
        sb.append("  \"learnedRecipes\": [\n");
        for (int i = 0; i < d.learnedRecipes.size(); i++) {
            SaveData.LearnedRecipe lr = d.learnedRecipes.get(i);
            sb.append("    {\"id\": \"").append(lr.id).append("\", ");
            sb.append("\"name\": \"").append(lr.name).append("\", ");
            sb.append("\"ingredients\": [");
            for (int j = 0; j < lr.ingredients.size(); j++) {
                sb.append("\"").append(lr.ingredients.get(j)).append("\"");
                if (j < lr.ingredients.size() - 1) sb.append(", ");
            }
            sb.append("], ");
            sb.append("\"result\": \"").append(lr.result).append("\", ");
            sb.append("\"resultCount\": ").append(lr.resultCount).append("}");
            if (i < d.learnedRecipes.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");
        // Player marketplace listings
        sb.append("  \"playerListings\": [\n");
        for (int i = 0; i < d.playerListings.size(); i++) {
            SaveData.PlayerListing pl = d.playerListings.get(i);
            sb.append("    {\"itemId\": \"").append(pl.itemId).append("\", ");
            sb.append("\"price\": ").append(pl.price).append(", ");
            sb.append("\"sellerUsername\": \"").append(pl.sellerUsername).append("\", ");
            sb.append("\"listTimestamp\": ").append(pl.listTimestamp).append("}");
            if (i < d.playerListings.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");
        // Sell timestamps (for weekly limit)
        sb.append("  \"sellTimestamps\": [");
        for (int i = 0; i < d.sellTimestamps.size(); i++) {
            sb.append(d.sellTimestamps.get(i));
            if (i < d.sellTimestamps.size() - 1) sb.append(", ");
        }
        sb.append("],\n");
        sb.append("  \"pendingTradeCoins\": ").append(d.pendingTradeCoins).append(",\n");
        // Loadout items
        sb.append("  \"loadoutItems\": [\n");
        for (int i = 0; i < d.loadoutItems.size(); i++) {
            SaveData.VaultItem li = d.loadoutItems.get(i);
            sb.append("    {\"itemId\": \"").append(li.itemId).append("\", \"stackCount\": ").append(li.stackCount).append("}");
            if (i < d.loadoutItems.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");
        sb.append("  \"selectedBackgroundId\": \"").append(d.selectedBackgroundId).append("\",\n");
        sb.append("  \"unlockedBackgrounds\": [");
        for (int i = 0; i < d.unlockedBackgrounds.size(); i++) {
            sb.append("\"").append(d.unlockedBackgrounds.get(i)).append("\"");
            if (i < d.unlockedBackgrounds.size() - 1) sb.append(", ");
        }
        sb.append("]\n");
        sb.append("}");
        return sb.toString();
    }

    private SaveData deserializeFromJson(String json) {
        SaveData d = new SaveData();
        d.version = extractInt(json, "version", 1);
        d.appVersion = extractString(json, "appVersion", "");
        d.pin = extractString(json, "pin", "");
        d.lastModified = extractLong(json, "lastModified", 0);
        d.lastModifiedDate = extractString(json, "lastModifiedDate", "");
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

        // Parse learned recipes array (objects with nested ingredients arrays)
        int recipesStart = json.indexOf("\"learnedRecipes\"");
        if (recipesStart != -1) {
            int outerArrStart = json.indexOf('[', recipesStart);
            if (outerArrStart != -1) {
                int outerArrEnd = findMatchingBracket(json, outerArrStart);
                if (outerArrEnd != -1) {
                    String arr = json.substring(outerArrStart + 1, outerArrEnd);
                    int objStart = 0;
                    while ((objStart = arr.indexOf('{', objStart)) != -1) {
                        int objEnd = findMatchingBrace(arr, objStart);
                        if (objEnd == -1) break;
                        String obj = arr.substring(objStart, objEnd + 1);
                        SaveData.LearnedRecipe lr = new SaveData.LearnedRecipe();
                        lr.id = extractString(obj, "id", "");
                        lr.name = extractString(obj, "name", "");
                        lr.result = extractString(obj, "result", "");
                        lr.resultCount = extractInt(obj, "resultCount", 1);
                        // Parse ingredients sub-array
                        int ingStart = obj.indexOf("\"ingredients\"");
                        if (ingStart != -1) {
                            int ingArrStart = obj.indexOf('[', ingStart);
                            int ingArrEnd = obj.indexOf(']', ingArrStart);
                            if (ingArrStart != -1 && ingArrEnd != -1) {
                                String ingArr = obj.substring(ingArrStart + 1, ingArrEnd);
                                int q1 = 0;
                                while ((q1 = ingArr.indexOf('"', q1)) != -1) {
                                    int q2 = ingArr.indexOf('"', q1 + 1);
                                    if (q2 == -1) break;
                                    String ing = ingArr.substring(q1 + 1, q2);
                                    if (!ing.isEmpty()) lr.ingredients.add(ing);
                                    q1 = q2 + 1;
                                }
                            }
                        }
                        if (!lr.id.isEmpty() && !lr.result.isEmpty()) {
                            d.learnedRecipes.add(lr);
                        }
                        objStart = objEnd + 1;
                    }
                }
            }
        }

        // Parse player listings array
        int listingsStart = json.indexOf("\"playerListings\"");
        if (listingsStart != -1) {
            int plArrStart = json.indexOf('[', listingsStart);
            int plArrEnd = json.indexOf(']', plArrStart);
            if (plArrStart != -1 && plArrEnd != -1) {
                String arr = json.substring(plArrStart + 1, plArrEnd);
                int objStart = 0;
                while ((objStart = arr.indexOf('{', objStart)) != -1) {
                    int objEnd = arr.indexOf('}', objStart);
                    if (objEnd == -1) break;
                    String obj = arr.substring(objStart, objEnd + 1);
                    String itemId = extractString(obj, "itemId", "");
                    int price = extractInt(obj, "price", 0);
                    String seller = extractString(obj, "sellerUsername", "");
                    long ts = extractLong(obj, "listTimestamp", 0);
                    if (!itemId.isEmpty() && price > 0) {
                        d.playerListings.add(new SaveData.PlayerListing(itemId, price, seller, ts));
                    }
                    objStart = objEnd + 1;
                }
            }
        }

        // Parse sell timestamps array
        int sellTsStart = json.indexOf("\"sellTimestamps\"");
        if (sellTsStart != -1) {
            int tsArrStart = json.indexOf('[', sellTsStart);
            int tsArrEnd = json.indexOf(']', tsArrStart);
            if (tsArrStart != -1 && tsArrEnd != -1) {
                String arr = json.substring(tsArrStart + 1, tsArrEnd);
                StringBuilder num = new StringBuilder();
                for (int i = 0; i < arr.length(); i++) {
                    char c = arr.charAt(i);
                    if (Character.isDigit(c) || c == '-') {
                        num.append(c);
                    } else if (num.length() > 0) {
                        try { d.sellTimestamps.add(Long.parseLong(num.toString())); } catch (Exception ignored) {}
                        num.setLength(0);
                    }
                }
                if (num.length() > 0) {
                    try { d.sellTimestamps.add(Long.parseLong(num.toString())); } catch (Exception ignored) {}
                }
            }
        }

        d.pendingTradeCoins = extractInt(json, "pendingTradeCoins", 0);

        // Parse loadout items array
        int loadoutStart = json.indexOf("\"loadoutItems\"");
        if (loadoutStart != -1) {
            int loadoutArrStart = json.indexOf('[', loadoutStart);
            int loadoutArrEnd = json.indexOf(']', loadoutArrStart);
            if (loadoutArrStart != -1 && loadoutArrEnd != -1) {
                String loadoutArr = json.substring(loadoutArrStart + 1, loadoutArrEnd);
                int loadoutObjStart = 0;
                while ((loadoutObjStart = loadoutArr.indexOf('{', loadoutObjStart)) != -1) {
                    int loadoutObjEnd = loadoutArr.indexOf('}', loadoutObjStart);
                    if (loadoutObjEnd == -1) break;
                    String obj = loadoutArr.substring(loadoutObjStart, loadoutObjEnd + 1);
                    String itemId = extractString(obj, "itemId", "");
                    int count = extractInt(obj, "stackCount", 1);
                    if (!itemId.isEmpty()) {
                        d.loadoutItems.add(new SaveData.VaultItem(itemId, count));
                    }
                    loadoutObjStart = loadoutObjEnd + 1;
                }
            }
        }

        d.selectedBackgroundId = extractString(json, "selectedBackgroundId", "none");

        // Parse unlocked backgrounds array
        int ubStart = json.indexOf("\"unlockedBackgrounds\"");
        if (ubStart != -1) {
            int ubArrStart = json.indexOf('[', ubStart);
            int ubArrEnd = json.indexOf(']', ubArrStart);
            if (ubArrStart != -1 && ubArrEnd != -1) {
                String ubArr = json.substring(ubArrStart + 1, ubArrEnd);
                int q1 = 0;
                while ((q1 = ubArr.indexOf('"', q1)) != -1) {
                    int q2 = ubArr.indexOf('"', q1 + 1);
                    if (q2 == -1) break;
                    String bgId = ubArr.substring(q1 + 1, q2);
                    if (!bgId.isEmpty()) d.unlockedBackgrounds.add(bgId);
                    q1 = q2 + 1;
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

    private static int findMatchingBracket(String str, int openIndex) {
        if (openIndex == -1 || openIndex >= str.length() || str.charAt(openIndex) != '[') return -1;
        int depth = 1;
        for (int i = openIndex + 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') { depth--; if (depth == 0) return i; }
        }
        return -1;
    }

    private static int findMatchingBrace(String str, int openIndex) {
        if (openIndex == -1 || openIndex >= str.length() || str.charAt(openIndex) != '{') return -1;
        int depth = 1;
        boolean inString = false;
        for (int i = openIndex + 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '"' && (i == 0 || str.charAt(i - 1) != '\\')) inString = !inString;
            else if (!inString) {
                if (c == '{') depth++;
                else if (c == '}') { depth--; if (depth == 0) return i; }
            }
        }
        return -1;
    }

    // --- Weekly sell limit ---

    private static final int MAX_SELLS_PER_WEEK = 5;

    /**
     * Count how many items this player has sold in the current calendar week
     * (Monday through Sunday).
     */
    public int getWeeklySellCount() {
        long weekStart = getStartOfWeekMillis();
        int count = 0;
        for (Long ts : data.sellTimestamps) {
            if (ts >= weekStart) count++;
        }
        return count;
    }

    public boolean canSellThisWeek() {
        return getWeeklySellCount() < MAX_SELLS_PER_WEEK;
    }

    public int getMaxSellsPerWeek() {
        return MAX_SELLS_PER_WEEK;
    }

    private long getStartOfWeekMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        // If today is before Monday in the calendar's week, go back 7 days
        if (cal.getTimeInMillis() > System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, -7);
        }
        return cal.getTimeInMillis();
    }

    /**
     * Record a sell timestamp for the weekly limit tracker.
     * Also prunes timestamps older than 2 weeks to keep save data clean.
     */
    private void recordSell() {
        data.sellTimestamps.add(System.currentTimeMillis());
        // Prune old timestamps (keep only those within the last 2 weeks)
        long twoWeeksAgo = System.currentTimeMillis() - 14L * 24 * 60 * 60 * 1000;
        Iterator<Long> it = data.sellTimestamps.iterator();
        while (it.hasNext()) {
            if (it.next() < twoWeeksAgo) it.remove();
        }
    }

    /**
     * List an item for sale on the player marketplace.
     * Removes 1 from vault, adds to shared marketplace file, records sell timestamp.
     * Returns true if successful.
     */
    public boolean listItemForSale(String itemId, int price) {
        if (!canSellThisWeek()) return false;
        if (price <= 0) return false;
        if (!removeVaultItem(itemId, 1)) return false;

        String seller = currentUsername;
        long timestamp = System.currentTimeMillis();
        SaveData.PlayerListing listing = new SaveData.PlayerListing(itemId, price, seller, timestamp);

        // Add to player's own listing tracker
        data.playerListings.add(listing);
        recordSell();
        save();

        // Add to shared marketplace file
        List<SaveData.PlayerListing> marketplace = loadMarketplaceListings();
        marketplace.add(listing);
        saveMarketplaceListings(marketplace);

        return true;
    }

    /**
     * Cancel a listing and return the item to the player's vault.
     */
    public boolean cancelListing(String itemId, long listTimestamp) {
        // Find and remove from player's listing tracker
        SaveData.PlayerListing found = null;
        for (SaveData.PlayerListing pl : data.playerListings) {
            if (pl.itemId.equals(itemId) && pl.listTimestamp == listTimestamp) {
                found = pl;
                break;
            }
        }
        if (found == null) return false;

        data.playerListings.remove(found);
        addVaultItem(itemId, 1);
        save();

        // Remove from shared marketplace
        List<SaveData.PlayerListing> marketplace = loadMarketplaceListings();
        Iterator<SaveData.PlayerListing> it = marketplace.iterator();
        while (it.hasNext()) {
            SaveData.PlayerListing pl = it.next();
            if (pl.itemId.equals(itemId) && pl.listTimestamp == listTimestamp
                    && pl.sellerUsername.equals(currentUsername)) {
                it.remove();
                break;
            }
        }
        saveMarketplaceListings(marketplace);
        return true;
    }

    /**
     * Update the price of an active listing.
     */
    public boolean updateListingPrice(String itemId, long listTimestamp, int newPrice) {
        if (newPrice <= 0) return false;

        // Update in player's local listing tracker
        boolean foundLocal = false;
        for (SaveData.PlayerListing pl : data.playerListings) {
            if (pl.itemId.equals(itemId) && pl.listTimestamp == listTimestamp) {
                pl.price = newPrice;
                foundLocal = true;
                break;
            }
        }
        if (!foundLocal) return false;
        save();

        // Update in shared marketplace
        List<SaveData.PlayerListing> marketplace = loadMarketplaceListings();
        for (SaveData.PlayerListing pl : marketplace) {
            if (pl.itemId.equals(itemId) && pl.listTimestamp == listTimestamp
                    && pl.sellerUsername.equals(currentUsername)) {
                pl.price = newPrice;
                break;
            }
        }
        saveMarketplaceListings(marketplace);
        return true;
    }

    /**
     * Purchase a player listing from the marketplace.
     * Deducts coins from buyer, adds item to buyer's vault,
     * records pending coins for the seller in the marketplace file.
     */
    public boolean purchasePlayerListing(SaveData.PlayerListing listing) {
        if (listing.sellerUsername.equals(currentUsername)) return false; // can't buy own listing
        if (!spendCoins(listing.price)) return false;

        addVaultItem(listing.itemId, 1);
        data.totalItemsCollected++;
        save();

        // Remove listing from marketplace and credit seller's pending coins
        List<SaveData.PlayerListing> marketplace = loadMarketplaceListings();
        Map<String, Integer> pendingCoins = loadMarketplacePendingCoins();
        Iterator<SaveData.PlayerListing> it = marketplace.iterator();
        while (it.hasNext()) {
            SaveData.PlayerListing pl = it.next();
            if (pl.itemId.equals(listing.itemId) && pl.listTimestamp == listing.listTimestamp
                    && pl.sellerUsername.equals(listing.sellerUsername)) {
                it.remove();
                // Credit seller
                int current = pendingCoins.containsKey(pl.sellerUsername) ? pendingCoins.get(pl.sellerUsername) : 0;
                pendingCoins.put(pl.sellerUsername, current + pl.price);
                break;
            }
        }
        saveMarketplaceData(marketplace, pendingCoins);
        return true;
    }

    /**
     * Collect any pending trade coins for the current user from the marketplace.
     * Called automatically during marketplace sync.
     * Returns the amount collected.
     */
    public int collectPendingTradeCoins() {
        Map<String, Integer> pendingCoins = loadMarketplacePendingCoins();
        int amount = pendingCoins.containsKey(currentUsername) ? pendingCoins.get(currentUsername) : 0;
        if (amount > 0) {
            addCoins(amount);
            pendingCoins.remove(currentUsername);
            // Also remove any sold listings from the player's local tracker
            List<SaveData.PlayerListing> marketplace = loadMarketplaceListings();
            List<String> marketItemIds = new ArrayList<>();
            for (SaveData.PlayerListing pl : marketplace) {
                if (pl.sellerUsername.equals(currentUsername)) {
                    marketItemIds.add(pl.itemId + "_" + pl.listTimestamp);
                }
            }
            Iterator<SaveData.PlayerListing> it = data.playerListings.iterator();
            while (it.hasNext()) {
                SaveData.PlayerListing pl = it.next();
                if (!marketItemIds.contains(pl.itemId + "_" + pl.listTimestamp)) {
                    it.remove(); // was sold, no longer in marketplace
                }
            }
            save();
            saveMarketplaceData(marketplace, pendingCoins);
        }
        return amount;
    }

    public String toJson() { return serializeToJson(data); }
    public void fromJson(String json) { data = deserializeFromJson(json); }

    // --- Shared Shop Data (separate file, read by all, written by Lars) ---

    private static final String SHOP_FILE = "loot_game_shop.json";

    /**
     * Loads shared shop items from the separate shop file.
     * All users can call this to see what's available for purchase.
     */
    public List<SaveData.ShopItem> loadShopItems() {
        List<SaveData.ShopItem> items = new ArrayList<>();
        try {
            FileInputStream fis = context.openFileInput(SHOP_FILE);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = fis.read(buf)) != -1) baos.write(buf, 0, n);
            fis.close();
            items = parseShopItems(baos.toString("UTF-8"));
        } catch (FileNotFoundException e) {
            // No shop file yet - empty shop
        } catch (Exception e) {
            Log.e(TAG, "Failed to load shop: " + e.getMessage());
        }
        return items;
    }

    /**
     * Saves shared shop items locally AND uploads to Google Drive.
     * Only Lars should call this.
     */
    public void saveShopItems(List<SaveData.ShopItem> items) {
        String json = serializeShopItems(items);
        // Save locally
        try {
            FileOutputStream fos = context.openFileOutput(SHOP_FILE, android.content.Context.MODE_PRIVATE);
            fos.write(json.getBytes("UTF-8"));
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "Failed to save shop locally: " + e.getMessage());
        }
        // Upload to Google Drive in background
        if (com.ambermoon.lootgame.core.GamePreferences.isCloudSyncEnabled()) {
            GoogleDriveSyncManager.getInstance().uploadShopToCloud(json, null);
        }
    }

    /**
     * Downloads the latest shop data from Google Drive and saves it locally.
     * Any user can call this to get the latest shop Lars configured.
     * Callback is invoked on the main thread with the loaded items.
     */
    public void syncShopFromCloud(ShopSyncCallback callback) {
        if (!com.ambermoon.lootgame.core.GamePreferences.isCloudSyncEnabled()) {
            if (callback != null) callback.onShopLoaded(loadShopItems());
            return;
        }
        GoogleDriveSyncManager.getInstance().downloadShopFromCloud((json, error) -> {
            if (json != null) {
                // Save cloud shop data locally
                try {
                    FileOutputStream fos = context.openFileOutput(SHOP_FILE, android.content.Context.MODE_PRIVATE);
                    fos.write(json.getBytes("UTF-8"));
                    fos.close();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to cache cloud shop locally: " + e.getMessage());
                }
                List<SaveData.ShopItem> items = parseShopItems(json);
                if (callback != null) callback.onShopLoaded(items);
            } else {
                // No cloud data or error — fall back to local
                if (callback != null) callback.onShopLoaded(loadShopItems());
            }
        });
    }

    public interface ShopSyncCallback {
        void onShopLoaded(List<SaveData.ShopItem> items);
    }

    private String serializeShopItems(List<SaveData.ShopItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"shopItems\": [\n");
        for (int i = 0; i < items.size(); i++) {
            SaveData.ShopItem si = items.get(i);
            sb.append("    {\"itemId\": \"").append(si.itemId).append("\", \"price\": ").append(si.price).append("}");
            if (i < items.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n}");
        return sb.toString();
    }

    private List<SaveData.ShopItem> parseShopItems(String json) {
        List<SaveData.ShopItem> items = new ArrayList<>();
        int arrStart = json.indexOf("\"shopItems\"");
        if (arrStart == -1) return items;
        int brStart = json.indexOf('[', arrStart);
        int brEnd = json.indexOf(']', brStart);
        if (brStart == -1 || brEnd == -1) return items;
        String arr = json.substring(brStart + 1, brEnd);
        int objStart = 0;
        while ((objStart = arr.indexOf('{', objStart)) != -1) {
            int objEnd = arr.indexOf('}', objStart);
            if (objEnd == -1) break;
            String obj = arr.substring(objStart, objEnd + 1);
            String itemId = extractString(obj, "itemId", "");
            int price = extractInt(obj, "price", 0);
            if (!itemId.isEmpty() && price > 0) {
                items.add(new SaveData.ShopItem(itemId, price));
            }
            objStart = objEnd + 1;
        }
        return items;
    }

    /**
     * Read the PIN from a local save file without loading it as the active save.
     * Returns empty string if no save exists or no PIN is set.
     */
    public static String readLocalPin(Context ctx, String username) {
        String filename;
        if (username == null || username.isEmpty()) {
            filename = "loot_game_save.json";
        } else {
            filename = SAVE_FILE_PREFIX + username + SAVE_FILE_SUFFIX;
        }
        try {
            FileInputStream fis = ctx.openFileInput(filename);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = fis.read(buf)) != -1) baos.write(buf, 0, n);
            fis.close();
            return extractString(baos.toString("UTF-8"), "pin", "");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Check if a local save file exists for the given username.
     */
    public static boolean localSaveExists(Context ctx, String username) {
        String filename;
        if (username == null || username.isEmpty()) {
            filename = "loot_game_save.json";
        } else {
            filename = SAVE_FILE_PREFIX + username + SAVE_FILE_SUFFIX;
        }
        try {
            FileInputStream fis = ctx.openFileInput(filename);
            fis.close();
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract PIN from a raw JSON string (for cloud save validation).
     */
    public static String extractPinFromJson(String json) {
        return extractString(json, "pin", "");
    }

    /**
     * Extract appVersion from a raw JSON string (for version validation).
     */
    public static String extractAppVersionFromJson(String json) {
        return extractString(json, "appVersion", "");
    }

    // --- Shared Marketplace Data (separate file, read/written by all players) ---

    private static final String MARKETPLACE_FILE = "loot_game_marketplace.json";

    /**
     * Load all active marketplace listings from the local marketplace file.
     */
    public List<SaveData.PlayerListing> loadMarketplaceListings() {
        String json = readMarketplaceFile();
        if (json == null) return new ArrayList<>();
        return parseMarketplaceListings(json);
    }

    /**
     * Load pending coins map from the marketplace file.
     */
    public Map<String, Integer> loadMarketplacePendingCoins() {
        String json = readMarketplaceFile();
        if (json == null) return new HashMap<>();
        return parseMarketplacePendingCoins(json);
    }

    private String readMarketplaceFile() {
        try {
            FileInputStream fis = context.openFileInput(MARKETPLACE_FILE);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = fis.read(buf)) != -1) baos.write(buf, 0, n);
            fis.close();
            return baos.toString("UTF-8");
        } catch (FileNotFoundException e) {
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Failed to load marketplace: " + e.getMessage());
            return null;
        }
    }

    /**
     * Save marketplace listings to the local file.
     */
    public void saveMarketplaceListings(List<SaveData.PlayerListing> listings) {
        Map<String, Integer> pendingCoins = loadMarketplacePendingCoins();
        saveMarketplaceData(listings, pendingCoins);
    }

    /**
     * Save both listings and pending coins to the marketplace file, then upload to cloud.
     */
    private void saveMarketplaceData(List<SaveData.PlayerListing> listings, Map<String, Integer> pendingCoins) {
        String json = serializeMarketplace(listings, pendingCoins);
        try {
            FileOutputStream fos = context.openFileOutput(MARKETPLACE_FILE, android.content.Context.MODE_PRIVATE);
            fos.write(json.getBytes("UTF-8"));
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "Failed to save marketplace locally: " + e.getMessage());
        }
        if (com.ambermoon.lootgame.core.GamePreferences.isCloudSyncEnabled()) {
            GoogleDriveSyncManager.getInstance().uploadMarketplaceToCloud(json, null);
        }
    }

    /**
     * Sync marketplace from cloud, auto-collect pending coins for the current user.
     */
    public void syncMarketplaceFromCloud(MarketplaceSyncCallback callback) {
        if (!com.ambermoon.lootgame.core.GamePreferences.isCloudSyncEnabled()) {
            int collected = collectPendingTradeCoins();
            if (callback != null) callback.onMarketplaceLoaded(loadMarketplaceListings(), collected);
            return;
        }
        GoogleDriveSyncManager.getInstance().downloadMarketplaceFromCloud((json, error) -> {
            if (json != null) {
                try {
                    FileOutputStream fos = context.openFileOutput(MARKETPLACE_FILE, android.content.Context.MODE_PRIVATE);
                    fos.write(json.getBytes("UTF-8"));
                    fos.close();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to cache cloud marketplace locally: " + e.getMessage());
                }
            }
            int collected = collectPendingTradeCoins();
            List<SaveData.PlayerListing> listings = loadMarketplaceListings();
            if (callback != null) callback.onMarketplaceLoaded(listings, collected);
        });
    }

    public interface MarketplaceSyncCallback {
        void onMarketplaceLoaded(List<SaveData.PlayerListing> listings, int coinsCollected);
    }

    private String serializeMarketplace(List<SaveData.PlayerListing> listings, Map<String, Integer> pendingCoins) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"listings\": [\n");
        for (int i = 0; i < listings.size(); i++) {
            SaveData.PlayerListing pl = listings.get(i);
            sb.append("    {\"itemId\": \"").append(pl.itemId).append("\", ");
            sb.append("\"price\": ").append(pl.price).append(", ");
            sb.append("\"sellerUsername\": \"").append(pl.sellerUsername).append("\", ");
            sb.append("\"listTimestamp\": ").append(pl.listTimestamp).append("}");
            if (i < listings.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n  \"pendingCoins\": {\n");
        int idx = 0;
        for (Map.Entry<String, Integer> entry : pendingCoins.entrySet()) {
            sb.append("    \"").append(entry.getKey()).append("\": ").append(entry.getValue());
            if (idx < pendingCoins.size() - 1) sb.append(",");
            sb.append("\n");
            idx++;
        }
        sb.append("  }\n}");
        return sb.toString();
    }

    private List<SaveData.PlayerListing> parseMarketplaceListings(String json) {
        List<SaveData.PlayerListing> listings = new ArrayList<>();
        int listStart = json.indexOf("\"listings\"");
        if (listStart == -1) return listings;
        int arrStart = json.indexOf('[', listStart);
        int arrEnd = json.indexOf(']', arrStart);
        if (arrStart == -1 || arrEnd == -1) return listings;
        String arr = json.substring(arrStart + 1, arrEnd);
        int objStart = 0;
        while ((objStart = arr.indexOf('{', objStart)) != -1) {
            int objEnd = arr.indexOf('}', objStart);
            if (objEnd == -1) break;
            String obj = arr.substring(objStart, objEnd + 1);
            String itemId = extractString(obj, "itemId", "");
            int price = extractInt(obj, "price", 0);
            String seller = extractString(obj, "sellerUsername", "");
            long ts = extractLong(obj, "listTimestamp", 0);
            if (!itemId.isEmpty() && price > 0 && !seller.isEmpty()) {
                listings.add(new SaveData.PlayerListing(itemId, price, seller, ts));
            }
            objStart = objEnd + 1;
        }
        return listings;
    }

    private Map<String, Integer> parseMarketplacePendingCoins(String json) {
        Map<String, Integer> coins = new HashMap<>();
        int pcStart = json.indexOf("\"pendingCoins\"");
        if (pcStart == -1) return coins;
        int braceStart = json.indexOf('{', pcStart + 14);
        if (braceStart == -1) return coins;
        int braceEnd = findMatchingBrace(json, braceStart);
        if (braceEnd == -1) return coins;
        String obj = json.substring(braceStart + 1, braceEnd);
        // Parse key-value pairs: "username": amount
        int q1 = 0;
        while ((q1 = obj.indexOf('"', q1)) != -1) {
            int q2 = obj.indexOf('"', q1 + 1);
            if (q2 == -1) break;
            String key = obj.substring(q1 + 1, q2);
            int colon = obj.indexOf(':', q2);
            if (colon == -1) break;
            StringBuilder num = new StringBuilder();
            for (int i = colon + 1; i < obj.length(); i++) {
                char c = obj.charAt(i);
                if (Character.isDigit(c) || c == '-') num.append(c);
                else if (num.length() > 0) break;
            }
            if (num.length() > 0 && !key.isEmpty()) {
                try { coins.put(key, Integer.parseInt(num.toString())); } catch (Exception ignored) {}
            }
            q1 = q2 + 1;
        }
        return coins;
    }
}
