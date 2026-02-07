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

public class GoogleDriveSyncManager {
    private static final String TAG = "GoogleDriveSync";

    // Public Google Drive file ID extracted from:
    // https://drive.google.com/file/d/1xINYQBBSiJ2o_12qAWT9tvCtrVoTpWfx/view?usp=drive_link
    private static final String FILE_ID = "1xINYQBBSiJ2o_12qAWT9tvCtrVoTpWfx";

    // Public download URL (no auth required for publicly shared files)
    private static final String PUBLIC_DOWNLOAD_URL =
            "https://drive.google.com/uc?export=download&id=" + FILE_ID;

    // Google Drive API v3 endpoints (auth required for upload)
    private static final String API_UPLOAD_URL =
            "https://www.googleapis.com/upload/drive/v3/files/" + FILE_ID + "?uploadType=media";

    private static GoogleDriveSyncManager instance;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface SyncCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public static GoogleDriveSyncManager getInstance() {
        if (instance == null) instance = new GoogleDriveSyncManager();
        return instance;
    }

    /**
     * Upload local save data to Google Drive (overwrites the shared file).
     * Requires a valid Google access token stored in GamePreferences.
     */
    public void syncToCloud(SyncCallback callback) {
        executor.execute(() -> {
            try {
                String token = GamePreferences.getGoogleAccessToken();
                if (token.isEmpty()) {
                    postError(callback, "No Google access token configured");
                    return;
                }

                String json = SaveManager.getInstance().toJson();

                URL url = new URL(API_UPLOAD_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(json.getBytes("UTF-8"));
                os.close();

                int code = conn.getResponseCode();
                if (code == 200) {
                    postSuccess(callback, "Saved to Google Drive");
                } else {
                    String errorBody = readErrorStream(conn);
                    Log.e(TAG, "Upload failed HTTP " + code + ": " + errorBody);
                    if (code == 401 || code == 403) {
                        postError(callback, "Access token expired or invalid (HTTP " + code + ")");
                    } else {
                        postError(callback, "Upload failed (HTTP " + code + ")");
                    }
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Upload exception", e);
                postError(callback, "Upload error: " + e.getMessage());
            }
        });
    }

    /**
     * Download save data from the public Google Drive file.
     * No authentication required since the file is publicly shared.
     */
    public void syncFromCloud(SyncCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(PUBLIC_DOWNLOAD_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setRequestProperty("User-Agent", "AmberMoon-LootGame/1.0");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int code = conn.getResponseCode();

                // Google Drive may redirect; follow it
                if (code == 302 || code == 303 || code == 307) {
                    String redirect = conn.getHeaderField("Location");
                    conn.disconnect();
                    if (redirect != null) {
                        url = new URL(redirect);
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setInstanceFollowRedirects(true);
                        conn.setRequestProperty("User-Agent", "AmberMoon-LootGame/1.0");
                        conn.setConnectTimeout(15000);
                        conn.setReadTimeout(15000);
                        code = conn.getResponseCode();
                    }
                }

                if (code == 200) {
                    String json = readStream(conn.getInputStream());

                    // Validate that the response is actual JSON save data
                    // (Google Drive may return an HTML page for large files)
                    if (json.trim().startsWith("{") && json.contains("\"version\"")) {
                        SaveManager.getInstance().fromJson(json);
                        SaveManager.getInstance().save();
                        postSuccess(callback, "Loaded from Google Drive");
                    } else if (json.trim().startsWith("{")) {
                        // Valid JSON but might be a new/empty save
                        SaveManager.getInstance().fromJson(json);
                        SaveManager.getInstance().save();
                        postSuccess(callback, "Loaded from Google Drive");
                    } else {
                        // Probably an HTML confirmation page
                        Log.w(TAG, "Response is not JSON, possibly Google Drive HTML page");
                        postError(callback, "Could not read save file from Google Drive");
                    }
                } else if (code == 404) {
                    postSuccess(callback, "No cloud save found, using local");
                } else {
                    postError(callback, "Download failed (HTTP " + code + ")");
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Download exception", e);
                postError(callback, "Download error: " + e.getMessage());
            }
        });
    }

    /**
     * Validate that the Google access token is working by checking Drive API access.
     * This makes a lightweight metadata request to verify the token.
     */
    public void validateToken(SyncCallback callback) {
        executor.execute(() -> {
            try {
                String token = GamePreferences.getGoogleAccessToken();
                if (token.isEmpty()) {
                    postError(callback, "No access token provided");
                    return;
                }

                // Use the Drive API to get file metadata (lightweight check)
                String metadataUrl = "https://www.googleapis.com/drive/v3/files/" + FILE_ID
                        + "?fields=id,name,modifiedTime";
                URL url = new URL(metadataUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int code = conn.getResponseCode();
                conn.disconnect();

                if (code == 200) {
                    postSuccess(callback, "Token valid");
                } else if (code == 401) {
                    postError(callback, "Access token expired or invalid");
                } else if (code == 403) {
                    postError(callback, "Access denied - check token permissions");
                } else {
                    postError(callback, "Validation failed (HTTP " + code + ")");
                }
            } catch (Exception e) {
                Log.e(TAG, "Token validation exception", e);
                postError(callback, "Validation error: " + e.getMessage());
            }
        });
    }

    /**
     * Test connectivity to the public file without any authentication.
     * Useful to verify the file is accessible before attempting full sync.
     */
    public void testConnection(SyncCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(PUBLIC_DOWNLOAD_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("HEAD");
                conn.setInstanceFollowRedirects(true);
                conn.setConnectTimeout(10000);

                int code = conn.getResponseCode();
                conn.disconnect();

                if (code == 200 || code == 302 || code == 303) {
                    postSuccess(callback, "Google Drive file accessible");
                } else {
                    postError(callback, "File not accessible (HTTP " + code + ")");
                }
            } catch (Exception e) {
                postError(callback, "Connection test failed: " + e.getMessage());
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

    private String readErrorStream(HttpURLConnection conn) {
        try {
            InputStream es = conn.getErrorStream();
            if (es != null) return readStream(es);
        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    private void postSuccess(SyncCallback cb, String msg) {
        if (cb != null) mainHandler.post(() -> cb.onSuccess(msg));
    }

    private void postError(SyncCallback cb, String msg) {
        if (cb != null) mainHandler.post(() -> cb.onError(msg));
    }
}
