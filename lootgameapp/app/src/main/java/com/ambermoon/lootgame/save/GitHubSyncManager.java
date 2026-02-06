package com.ambermoon.lootgame.save;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.ambermoon.lootgame.core.GamePreferences;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GitHubSyncManager {
    private static final String TAG = "GitHubSync";
    private static final String REPO_OWNER = "Larleeloo";
    private static final String REPO_NAME = "DND_Pixel_Art_Game1";
    private static final String BASE_PATH = "cloud-saves/";

    private static GitHubSyncManager instance;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private String currentSha; // SHA of the existing file (needed for updates)

    public static GitHubSyncManager getInstance() {
        if (instance == null) instance = new GitHubSyncManager();
        return instance;
    }

    private String getSavePath() {
        return BASE_PATH + GamePreferences.getGitHubUserId() + "/loot_game_save.json";
    }

    public void syncToCloud(SyncCallback callback) {
        executor.execute(() -> {
            try {
                String token = GamePreferences.getGitHubToken();
                if (token.isEmpty()) { postError(callback, "No token"); return; }

                // First, get current file SHA if it exists
                fetchCurrentSha(token);

                String json = SaveManager.getInstance().toJson();
                String encoded = Base64.encodeToString(json.getBytes("UTF-8"), Base64.NO_WRAP);

                String apiUrl = "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/contents/" + getSavePath();
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setDoOutput(true);

                String body = "{\"message\":\"Loot Game save sync\",\"content\":\"" + encoded + "\"";
                if (currentSha != null) body += ",\"sha\":\"" + currentSha + "\"";
                body += "}";

                OutputStream os = conn.getOutputStream();
                os.write(body.getBytes("UTF-8"));
                os.close();

                int code = conn.getResponseCode();
                if (code == 200 || code == 201) {
                    // Read response to get new SHA
                    String resp = readStream(conn.getInputStream());
                    currentSha = extractSha(resp);
                    postSuccess(callback, "Synced to cloud");
                } else {
                    postError(callback, "HTTP " + code);
                }
                conn.disconnect();
            } catch (Exception e) {
                postError(callback, e.getMessage());
            }
        });
    }

    public void syncFromCloud(SyncCallback callback) {
        executor.execute(() -> {
            try {
                String token = GamePreferences.getGitHubToken();
                if (token.isEmpty()) { postError(callback, "No token"); return; }

                String apiUrl = "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/contents/" + getSavePath();
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

                int code = conn.getResponseCode();
                if (code == 200) {
                    String resp = readStream(conn.getInputStream());
                    currentSha = extractSha(resp);
                    String content = extractContent(resp);
                    if (content != null) {
                        String json = new String(Base64.decode(content, Base64.DEFAULT), "UTF-8");
                        SaveManager.getInstance().fromJson(json);
                        SaveManager.getInstance().save();
                        postSuccess(callback, "Loaded from cloud");
                    }
                } else if (code == 404) {
                    postSuccess(callback, "No cloud save found, using local");
                } else {
                    postError(callback, "HTTP " + code);
                }
                conn.disconnect();
            } catch (Exception e) {
                postError(callback, e.getMessage());
            }
        });
    }

    public void validateToken(SyncCallback callback) {
        executor.execute(() -> {
            try {
                String token = GamePreferences.getGitHubToken();
                URL url = new URL("https://api.github.com/user");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + token);
                int code = conn.getResponseCode();
                conn.disconnect();
                if (code == 200) postSuccess(callback, "Token valid");
                else postError(callback, "Invalid token (HTTP " + code + ")");
            } catch (Exception e) {
                postError(callback, e.getMessage());
            }
        });
    }

    private void fetchCurrentSha(String token) {
        try {
            String apiUrl = "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/contents/" + getSavePath();
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            if (conn.getResponseCode() == 200) {
                String resp = readStream(conn.getInputStream());
                currentSha = extractSha(resp);
            }
            conn.disconnect();
        } catch (Exception e) {
            currentSha = null;
        }
    }

    private String readStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
        return baos.toString("UTF-8");
    }

    private String extractSha(String json) {
        int idx = json.indexOf("\"sha\"");
        if (idx == -1) return null;
        int q1 = json.indexOf('"', json.indexOf(':', idx) + 1);
        int q2 = json.indexOf('"', q1 + 1);
        return (q1 != -1 && q2 != -1) ? json.substring(q1 + 1, q2) : null;
    }

    private String extractContent(String json) {
        int idx = json.indexOf("\"content\"");
        if (idx == -1) return null;
        int q1 = json.indexOf('"', json.indexOf(':', idx) + 1);
        int q2 = json.indexOf('"', q1 + 1);
        if (q1 == -1 || q2 == -1) return null;
        return json.substring(q1 + 1, q2).replace("\\n", "");
    }

    private void postSuccess(SyncCallback cb, String msg) {
        if (cb != null) mainHandler.post(() -> cb.onSuccess(msg));
    }

    private void postError(SyncCallback cb, String msg) {
        if (cb != null) mainHandler.post(() -> cb.onError(msg));
    }
}
