package com.ambermoon.lootgame.save;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ambermoon.lootgame.core.GamePreferences;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages cloud save sync via a Google Apps Script Web App that reads/writes
 * save files in the shared Google Drive folder.
 *
 * Each user's save is stored as: save_<username>.json
 * in the Drive folder: https://drive.google.com/drive/folders/1Ah9GEgg6wo39Xi7U6P-2wZ4M0C3EO-g0
 *
 * No tokens or authentication required from the app side.
 * The Web App URL must be set via GamePreferences.setWebAppUrl().
 *
 * See google_apps_script.gs in the project root for the script to deploy.
 */
public class GoogleDriveSyncManager {
    private static final String TAG = "GoogleDriveSync";

    private static GoogleDriveSyncManager instance;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean syncing = false;

    public static GoogleDriveSyncManager getInstance() {
        if (instance == null) instance = new GoogleDriveSyncManager();
        return instance;
    }

    /**
     * Upload current save data to Google Drive for the active user.
     */
    public void syncToCloud(SyncCallback callback) {
        if (syncing) {
            postResult(callback, false, "Sync already in progress");
            return;
        }

        String webAppUrl = GamePreferences.getWebAppUrl();
        String username = GamePreferences.getUsername();

        if (webAppUrl.isEmpty()) {
            postResult(callback, false, "Google Drive sync not configured");
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
                String uploadUrl = webAppUrl + "?username=" +
                        java.net.URLEncoder.encode(username, "UTF-8");

                // POST save data to the web app
                HttpURLConnection conn = (HttpURLConnection) new URL(uploadUrl).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("User-Agent", "AmberMoon-LootGame/1.0");
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(false);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                OutputStream os = conn.getOutputStream();
                os.write(json.getBytes("UTF-8"));
                os.close();

                // Google Apps Script returns a 302 redirect after processing the POST.
                // Follow the redirect with GET to retrieve the response.
                int code = conn.getResponseCode();
                String response;

                if (code == 302 || code == 301 || code == 303 || code == 307) {
                    String redirect = conn.getHeaderField("Location");
                    conn.disconnect();
                    if (redirect != null) {
                        HttpURLConnection rConn = (HttpURLConnection) new URL(redirect).openConnection();
                        rConn.setInstanceFollowRedirects(true);
                        rConn.setConnectTimeout(15000);
                        rConn.setReadTimeout(15000);
                        code = rConn.getResponseCode();
                        response = readStream(rConn.getInputStream());
                        rConn.disconnect();
                    } else {
                        response = "";
                    }
                } else if (code == 200) {
                    response = readStream(conn.getInputStream());
                    conn.disconnect();
                } else {
                    conn.disconnect();
                    syncing = false;
                    postResult(callback, false, "Upload failed (HTTP " + code + ")");
                    return;
                }

                syncing = false;

                if (response.contains("\"success\"") || response.contains("\"ok\"")) {
                    GamePreferences.setLastSyncTime(System.currentTimeMillis());
                    postResult(callback, true, "Saved to Google Drive");
                } else {
                    postResult(callback, false, "Upload may have failed: " + response);
                }
            } catch (Exception e) {
                Log.e(TAG, "Upload exception", e);
                syncing = false;
                postResult(callback, false, "Upload error: " + e.getMessage());
            }
        });
    }

    /**
     * Download save data from Google Drive for the active user.
     */
    public void syncFromCloud(SyncCallback callback) {
        if (syncing) {
            postResult(callback, false, "Sync already in progress");
            return;
        }

        String webAppUrl = GamePreferences.getWebAppUrl();
        String username = GamePreferences.getUsername();

        if (webAppUrl.isEmpty()) {
            postResult(callback, false, "Google Drive sync not configured");
            return;
        }
        if (username.isEmpty()) {
            postResult(callback, false, "No username set");
            return;
        }

        syncing = true;

        executor.execute(() -> {
            try {
                String downloadUrl = webAppUrl + "?username=" +
                        java.net.URLEncoder.encode(username, "UTF-8");

                HttpURLConnection conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
                conn.setRequestMethod("GET");
                conn.setInstanceFollowRedirects(true);
                conn.setRequestProperty("User-Agent", "AmberMoon-LootGame/1.0");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int code = conn.getResponseCode();

                if (code == 200) {
                    String json = readStream(conn.getInputStream());
                    conn.disconnect();

                    // Validate response is actual save JSON (not empty or error)
                    String trimmed = json.trim();
                    if (trimmed.startsWith("{") && trimmed.contains("\"version\"")) {
                        SaveManager.getInstance().fromJson(json);
                        SaveManager.getInstance().save();
                        syncing = false;
                        GamePreferences.setLastSyncTime(System.currentTimeMillis());
                        postResult(callback, true, "Loaded from Google Drive");
                    } else if (trimmed.equals("{}") || trimmed.contains("\"not_found\"")) {
                        syncing = false;
                        postResult(callback, true, "No cloud save found, using local");
                    } else {
                        syncing = false;
                        postResult(callback, false, "Invalid response from Google Drive");
                    }
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

    private String readStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
        return baos.toString("UTF-8");
    }

    public boolean isSyncing() { return syncing; }

    private void postResult(SyncCallback cb, boolean success, String msg) {
        if (cb != null) mainHandler.post(() -> cb.onResult(success, msg));
    }
}
