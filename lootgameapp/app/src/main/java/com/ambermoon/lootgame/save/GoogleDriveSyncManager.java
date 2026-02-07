package com.ambermoon.lootgame.save;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Downloads save data from a public Google Drive file.
 * No authentication required — the file is publicly shared.
 */
public class GoogleDriveSyncManager {
    private static final String TAG = "GoogleDriveSync";

    // Public Google Drive file ID
    private static final String FILE_ID = "1xINYQBBSiJ2o_12qAWT9tvCtrVoTpWfx";

    // Public download URL (no auth required)
    private static final String PUBLIC_DOWNLOAD_URL =
            "https://drive.google.com/uc?export=download&id=" + FILE_ID;

    private static GoogleDriveSyncManager instance;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public static GoogleDriveSyncManager getInstance() {
        if (instance == null) instance = new GoogleDriveSyncManager();
        return instance;
    }

    /**
     * Download save data from the public Google Drive file.
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
                    if (json.trim().startsWith("{")) {
                        SaveManager.getInstance().fromJson(json);
                        SaveManager.getInstance().save();
                        postResult(callback, true, "Loaded from Google Drive");
                    } else {
                        Log.w(TAG, "Response is not JSON, possibly Google Drive HTML page");
                        postResult(callback, false, "Could not read save file from Google Drive");
                    }
                } else if (code == 404) {
                    postResult(callback, true, "No cloud save found, using local");
                } else {
                    postResult(callback, false, "Download failed (HTTP " + code + ")");
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Download exception", e);
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

    private void postResult(SyncCallback cb, boolean success, String msg) {
        if (cb != null) mainHandler.post(() -> cb.onResult(success, msg));
    }
}
