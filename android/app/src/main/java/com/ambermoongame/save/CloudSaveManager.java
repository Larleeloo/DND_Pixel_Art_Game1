package com.ambermoongame.save;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.ambermoongame.core.GamePreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages cross-platform save files via GitHub.
 * Stores saves in a GitHub repository for sync between desktop and Android.
 *
 * Repository structure:
 *   cloud-saves/<user_id>/player_data.json
 *
 * Requires a GitHub Personal Access Token with repo permissions.
 */
public class CloudSaveManager {

    private static final String TAG = "CloudSaveManager";

    // GitHub API configuration
    private static final String GITHUB_API = "https://api.github.com";
    private static final String REPO_OWNER = "Larleeloo";
    private static final String REPO_NAME = "DND_Pixel_Art_Game1";
    private static final String SAVE_PATH = "cloud-saves";
    private static final String SAVE_FILENAME = "player_data.json";

    // Local save
    private static final String LOCAL_SAVE_DIR = "saves";
    private static final String LOCAL_SAVE_FILE = "player_data.json";

    private static CloudSaveManager instance;
    private static Context appContext;

    private ExecutorService executor;
    private Handler mainHandler;

    // Sync state
    private boolean syncing = false;
    private long lastSyncTime = 0;

    // Callbacks
    public interface SyncCallback {
        void onSuccess(String message);
        void onError(String error);
        void onConflict(long localTime, long cloudTime);
    }

    // Save data (mirrors desktop SaveManager)
    private List<SavedItem> inventory;
    private List<SavedItem> vaultItems;
    private long dailyChestLastOpened;
    private long monthlyChestLastOpened;
    private int totalItemsCollected;
    private int legendaryItemsFound;
    private int mythicItemsFound;
    private boolean developerMode;
    private long lastModified;
    private String platform;
    private String syncId;

    public static class SavedItem {
        public String itemId;
        public int stackCount;

        public SavedItem(String itemId, int stackCount) {
            this.itemId = itemId;
            this.stackCount = stackCount;
        }
    }

    private CloudSaveManager() {
        inventory = new ArrayList<>();
        vaultItems = new ArrayList<>();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        platform = "android";
        syncId = java.util.UUID.randomUUID().toString();
    }

    public static void initialize(Context context) {
        if (instance == null) {
            instance = new CloudSaveManager();
        }
        appContext = context.getApplicationContext();
        instance.load();
    }

    public static CloudSaveManager getInstance() {
        if (instance == null) {
            instance = new CloudSaveManager();
        }
        return instance;
    }

    // ==================== Local Save/Load ====================

    /**
     * Save game data to local storage.
     */
    public void save() {
        if (appContext == null) return;

        lastModified = System.currentTimeMillis();

        try {
            File dir = new File(appContext.getFilesDir(), LOCAL_SAVE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, LOCAL_SAVE_FILE);
            String json = toJson();

            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            writer.write(json);
            writer.close();
            fos.close();

            Log.d(TAG, "Local save complete");

            // Trigger cloud sync if enabled
            if (GamePreferences.isCloudSyncEnabled()) {
                syncToCloud(null);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to save locally: " + e.getMessage());
        }
    }

    /**
     * Load game data from local storage.
     */
    public void load() {
        if (appContext == null) return;

        try {
            File file = new File(appContext.getFilesDir(), LOCAL_SAVE_DIR + "/" + LOCAL_SAVE_FILE);
            if (!file.exists()) {
                Log.d(TAG, "No local save found, starting fresh");
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            fis.close();

            fromJson(sb.toString());
            Log.d(TAG, "Local load complete");
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Failed to load locally: " + e.getMessage());
        }
    }

    // ==================== Cloud Sync ====================

    /**
     * Sync local save to cloud (GitHub).
     */
    public void syncToCloud(SyncCallback callback) {
        if (syncing) {
            if (callback != null) {
                callback.onError("Sync already in progress");
            }
            return;
        }

        String token = GamePreferences.getGitHubToken();
        String userId = GamePreferences.getGitHubUserId();

        if (token.isEmpty() || userId.isEmpty()) {
            if (callback != null) {
                callback.onError("GitHub token or user ID not configured");
            }
            return;
        }

        syncing = true;

        executor.execute(() -> {
            try {
                String json = toJson();
                String base64Content = Base64.encodeToString(json.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

                String path = SAVE_PATH + "/" + userId + "/" + SAVE_FILENAME;
                String apiUrl = GITHUB_API + "/repos/" + REPO_OWNER + "/" + REPO_NAME + "/contents/" + path;

                // Get current file SHA (if exists)
                String sha = getFileSha(apiUrl, token);

                // Create/update file
                JSONObject body = new JSONObject();
                body.put("message", "Cloud save from Android - " + System.currentTimeMillis());
                body.put("content", base64Content);
                if (sha != null) {
                    body.put("sha", sha);
                }

                HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "token " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                int responseCode = conn.getResponseCode();
                conn.disconnect();

                syncing = false;
                lastSyncTime = System.currentTimeMillis();
                GamePreferences.setLastSyncTime(lastSyncTime);

                if (responseCode == 200 || responseCode == 201) {
                    notifyCallback(callback, true, "Sync complete", 0, 0);
                } else {
                    notifyCallback(callback, false, "Sync failed: HTTP " + responseCode, 0, 0);
                }
            } catch (Exception e) {
                syncing = false;
                notifyCallback(callback, false, "Sync error: " + e.getMessage(), 0, 0);
            }
        });
    }

    /**
     * Sync from cloud to local.
     */
    public void syncFromCloud(SyncCallback callback) {
        if (syncing) {
            if (callback != null) {
                callback.onError("Sync already in progress");
            }
            return;
        }

        String token = GamePreferences.getGitHubToken();
        String userId = GamePreferences.getGitHubUserId();

        if (token.isEmpty() || userId.isEmpty()) {
            if (callback != null) {
                callback.onError("GitHub token or user ID not configured");
            }
            return;
        }

        syncing = true;

        executor.execute(() -> {
            try {
                String path = SAVE_PATH + "/" + userId + "/" + SAVE_FILENAME;
                String apiUrl = GITHUB_API + "/repos/" + REPO_OWNER + "/" + REPO_NAME + "/contents/" + path;

                HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "token " + token);
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

                int responseCode = conn.getResponseCode();

                if (responseCode == 200) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    JSONObject response = new JSONObject(sb.toString());
                    String content = response.getString("content").replace("\n", "");
                    byte[] decoded = Base64.decode(content, Base64.DEFAULT);
                    String json = new String(decoded, StandardCharsets.UTF_8);

                    // Check for conflict
                    JSONObject cloudData = new JSONObject(json);
                    long cloudTime = cloudData.optLong("lastModified", 0);

                    if (lastModified > 0 && cloudTime > 0 && lastModified != cloudTime) {
                        // Conflict detected
                        syncing = false;
                        notifyCallback(callback, false, null, lastModified, cloudTime);
                        return;
                    }

                    // Apply cloud data
                    fromJson(json);
                    save(); // Save locally

                    syncing = false;
                    lastSyncTime = System.currentTimeMillis();
                    notifyCallback(callback, true, "Download complete", 0, 0);
                } else if (responseCode == 404) {
                    syncing = false;
                    notifyCallback(callback, true, "No cloud save found", 0, 0);
                } else {
                    syncing = false;
                    notifyCallback(callback, false, "Download failed: HTTP " + responseCode, 0, 0);
                }

                conn.disconnect();
            } catch (Exception e) {
                syncing = false;
                notifyCallback(callback, false, "Download error: " + e.getMessage(), 0, 0);
            }
        });
    }

    private String getFileSha(String apiUrl, String token) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "token " + token);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject response = new JSONObject(sb.toString());
                return response.getString("sha");
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.w(TAG, "Could not get file SHA: " + e.getMessage());
        }
        return null;
    }

    private void notifyCallback(SyncCallback callback, boolean success, String message,
                                long localTime, long cloudTime) {
        if (callback == null) return;

        mainHandler.post(() -> {
            if (localTime > 0 && cloudTime > 0) {
                callback.onConflict(localTime, cloudTime);
            } else if (success) {
                callback.onSuccess(message);
            } else {
                callback.onError(message);
            }
        });
    }

    // ==================== JSON Serialization ====================

    private String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("version", 2);
            json.put("platform", platform);
            json.put("lastModified", lastModified);
            json.put("syncId", syncId);
            json.put("developerMode", developerMode);
            json.put("dailyChestLastOpened", dailyChestLastOpened);
            json.put("monthlyChestLastOpened", monthlyChestLastOpened);
            json.put("totalItemsCollected", totalItemsCollected);
            json.put("legendaryItemsFound", legendaryItemsFound);
            json.put("mythicItemsFound", mythicItemsFound);

            JSONArray invArray = new JSONArray();
            for (SavedItem item : inventory) {
                JSONObject itemObj = new JSONObject();
                itemObj.put("itemId", item.itemId);
                itemObj.put("stackCount", item.stackCount);
                invArray.put(itemObj);
            }
            json.put("inventory", invArray);

            JSONArray vaultArray = new JSONArray();
            for (SavedItem item : vaultItems) {
                JSONObject itemObj = new JSONObject();
                itemObj.put("itemId", item.itemId);
                itemObj.put("stackCount", item.stackCount);
                vaultArray.put(itemObj);
            }
            json.put("vaultItems", vaultArray);

            return json.toString(2);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to serialize: " + e.getMessage());
            return "{}";
        }
    }

    private void fromJson(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);

        developerMode = json.optBoolean("developerMode", false);
        dailyChestLastOpened = json.optLong("dailyChestLastOpened", 0);
        monthlyChestLastOpened = json.optLong("monthlyChestLastOpened", 0);
        totalItemsCollected = json.optInt("totalItemsCollected", 0);
        legendaryItemsFound = json.optInt("legendaryItemsFound", 0);
        mythicItemsFound = json.optInt("mythicItemsFound", 0);
        lastModified = json.optLong("lastModified", 0);
        syncId = json.optString("syncId", syncId);

        inventory.clear();
        JSONArray invArray = json.optJSONArray("inventory");
        if (invArray != null) {
            for (int i = 0; i < invArray.length(); i++) {
                JSONObject itemObj = invArray.getJSONObject(i);
                inventory.add(new SavedItem(
                    itemObj.getString("itemId"),
                    itemObj.getInt("stackCount")
                ));
            }
        }

        vaultItems.clear();
        JSONArray vaultArray = json.optJSONArray("vaultItems");
        if (vaultArray != null) {
            for (int i = 0; i < vaultArray.length(); i++) {
                JSONObject itemObj = vaultArray.getJSONObject(i);
                vaultItems.add(new SavedItem(
                    itemObj.getString("itemId"),
                    itemObj.getInt("stackCount")
                ));
            }
        }
    }

    // ==================== Accessors ====================

    public boolean isSyncing() { return syncing; }
    public long getLastSyncTime() { return lastSyncTime; }
    public List<SavedItem> getInventory() { return new ArrayList<>(inventory); }
    public List<SavedItem> getVaultItems() { return new ArrayList<>(vaultItems); }
    public int getTotalItemsCollected() { return totalItemsCollected; }
    public int getLegendaryItemsFound() { return legendaryItemsFound; }
    public int getMythicItemsFound() { return mythicItemsFound; }
    public boolean isDeveloperMode() { return developerMode; }
    public void setDeveloperMode(boolean mode) { this.developerMode = mode; save(); }
}
