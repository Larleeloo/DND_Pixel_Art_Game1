package com.ambermoon.lootgame.save;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.ambermoon.lootgame.core.GamePreferences;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages cloud save sync via GitHub repository.
 * Stores per-user saves at: cloud-saves/{username}/loot_save.json
 *
 * Replaces the old GoogleDriveSyncManager which was download-only.
 * This version supports both upload (syncToCloud) and download (syncFromCloud).
 *
 * Requires a GitHub Personal Access Token with repo permissions.
 */
public class CloudSyncManager {
    private static final String TAG = "CloudSync";

    private static final String GITHUB_API = "https://api.github.com";
    private static final String REPO_OWNER = "Larleeloo";
    private static final String REPO_NAME = "DND_Pixel_Art_Game1";
    private static final String SAVE_DIR = "cloud-saves";
    private static final String SAVE_FILENAME = "loot_save.json";

    private static CloudSyncManager instance;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean syncing = false;

    public static CloudSyncManager getInstance() {
        if (instance == null) instance = new CloudSyncManager();
        return instance;
    }

    /**
     * Upload current save data to GitHub for the active user.
     */
    public void syncToCloud(SyncCallback callback) {
        if (syncing) {
            postResult(callback, false, "Sync already in progress");
            return;
        }

        String token = GamePreferences.getGitHubToken();
        String username = GamePreferences.getUsername();

        if (token.isEmpty()) {
            postResult(callback, false, "GitHub token not configured. Set it in preferences to enable cloud sync.");
            return;
        }
        if (username.isEmpty()) {
            postResult(callback, false, "No username set");
            return;
        }

        syncing = true;

        executor.execute(() -> {
            try {
                String json = SaveManager.getInstance().toJson();
                String base64Content = Base64.encodeToString(
                        json.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

                String path = SAVE_DIR + "/" + username + "/" + SAVE_FILENAME;
                String apiUrl = GITHUB_API + "/repos/" + REPO_OWNER + "/" + REPO_NAME + "/contents/" + path;

                // Get current file SHA if it exists (required for updates)
                String sha = getFileSha(apiUrl, token);

                // Build PUT request body
                StringBuilder body = new StringBuilder();
                body.append("{");
                body.append("\"message\":\"Cloud save: ").append(username).append(" - ").append(System.currentTimeMillis()).append("\",");
                body.append("\"content\":\"").append(base64Content).append("\"");
                if (sha != null) {
                    body.append(",\"sha\":\"").append(sha).append("\"");
                }
                body.append("}");

                HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "token " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                int code = conn.getResponseCode();
                conn.disconnect();

                syncing = false;
                GamePreferences.setLastSyncTime(System.currentTimeMillis());

                if (code == 200 || code == 201) {
                    postResult(callback, true, "Saved to cloud");
                } else {
                    postResult(callback, false, "Upload failed (HTTP " + code + ")");
                }
            } catch (Exception e) {
                Log.e(TAG, "Upload exception", e);
                syncing = false;
                postResult(callback, false, "Upload error: " + e.getMessage());
            }
        });
    }

    /**
     * Download save data from GitHub for the active user.
     */
    public void syncFromCloud(SyncCallback callback) {
        if (syncing) {
            postResult(callback, false, "Sync already in progress");
            return;
        }

        String token = GamePreferences.getGitHubToken();
        String username = GamePreferences.getUsername();

        if (token.isEmpty()) {
            postResult(callback, false, "GitHub token not configured. Set it in preferences to enable cloud sync.");
            return;
        }
        if (username.isEmpty()) {
            postResult(callback, false, "No username set");
            return;
        }

        syncing = true;

        executor.execute(() -> {
            try {
                String path = SAVE_DIR + "/" + username + "/" + SAVE_FILENAME;
                String apiUrl = GITHUB_API + "/repos/" + REPO_OWNER + "/" + REPO_NAME + "/contents/" + path;

                HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "token " + token);
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int code = conn.getResponseCode();

                if (code == 200) {
                    String response = readStream(conn.getInputStream());
                    conn.disconnect();

                    // GitHub API returns JSON with base64-encoded "content" field
                    String contentField = extractJsonString(response, "content");
                    if (contentField == null || contentField.isEmpty()) {
                        syncing = false;
                        postResult(callback, false, "Could not read cloud save content");
                        return;
                    }

                    // Decode base64 content (remove newlines GitHub adds)
                    byte[] decoded = Base64.decode(contentField.replace("\\n", "").replace("\n", ""), Base64.DEFAULT);
                    String json = new String(decoded, StandardCharsets.UTF_8);

                    if (json.trim().startsWith("{")) {
                        SaveManager.getInstance().fromJson(json);
                        SaveManager.getInstance().save();
                        syncing = false;
                        GamePreferences.setLastSyncTime(System.currentTimeMillis());
                        postResult(callback, true, "Loaded from cloud");
                    } else {
                        syncing = false;
                        postResult(callback, false, "Invalid save data from cloud");
                    }
                } else if (code == 404) {
                    conn.disconnect();
                    syncing = false;
                    postResult(callback, true, "No cloud save found for " + username + ", using local");
                } else {
                    conn.disconnect();
                    syncing = false;
                    postResult(callback, false, "Download failed (HTTP " + code + ")");
                }
            } catch (Exception e) {
                Log.e(TAG, "Download exception", e);
                syncing = false;
                postResult(callback, false, "Download error: " + e.getMessage());
            }
        });
    }

    /**
     * Get the SHA of an existing file (needed to update it via GitHub API).
     */
    private String getFileSha(String apiUrl, String token) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "token " + token);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() == 200) {
                String response = readStream(conn.getInputStream());
                conn.disconnect();
                return extractJsonString(response, "sha");
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.w(TAG, "Could not get file SHA: " + e.getMessage());
        }
        return null;
    }

    private String readStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
        return baos.toString("UTF-8");
    }

    /**
     * Simple JSON string field extractor (no external JSON library dependency).
     */
    private String extractJsonString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return null;
        int colon = json.indexOf(':', idx + search.length());
        if (colon == -1) return null;
        int q1 = json.indexOf('"', colon + 1);
        if (q1 == -1) return null;
        // Find the closing quote, handling escaped quotes
        int q2 = q1 + 1;
        while (q2 < json.length()) {
            if (json.charAt(q2) == '"' && json.charAt(q2 - 1) != '\\') break;
            q2++;
        }
        if (q2 >= json.length()) return null;
        return json.substring(q1 + 1, q2);
    }

    public boolean isSyncing() { return syncing; }

    private void postResult(SyncCallback cb, boolean success, String msg) {
        if (cb != null) mainHandler.post(() -> cb.onResult(success, msg));
    }
}
