package com.ambermoon.lootgame.core;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import com.ambermoon.lootgame.save.GitHubSyncManager;

public class LoginActivity extends Activity {
    private EditText usernameInput;
    private EditText tokenInput;
    private TextView statusText;
    private Button loginButton;
    private Button offlineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Build UI programmatically (no XML layout needed)
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#1A1525"));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(64, 100, 64, 64);

        // Title
        TextView title = new TextView(this);
        title.setText("The Amber Moon");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Loot Game");
        subtitle.setTextColor(Color.WHITE);
        subtitle.setTextSize(20);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 8, 0, 60);
        layout.addView(subtitle);

        // Username
        TextView userLabel = new TextView(this);
        userLabel.setText("GitHub Username");
        userLabel.setTextColor(Color.parseColor("#AAAACC"));
        userLabel.setPadding(0, 0, 0, 8);
        layout.addView(userLabel);

        usernameInput = new EditText(this);
        usernameInput.setHint("username");
        usernameInput.setTextColor(Color.WHITE);
        usernameInput.setHintTextColor(Color.parseColor("#666688"));
        usernameInput.setBackgroundColor(Color.parseColor("#3C3555"));
        usernameInput.setPadding(24, 20, 24, 20);
        usernameInput.setSingleLine(true);
        layout.addView(usernameInput);

        // Token
        TextView tokenLabel = new TextView(this);
        tokenLabel.setText("Personal Access Token");
        tokenLabel.setTextColor(Color.parseColor("#AAAACC"));
        tokenLabel.setPadding(0, 24, 0, 8);
        layout.addView(tokenLabel);

        tokenInput = new EditText(this);
        tokenInput.setHint("ghp_...");
        tokenInput.setTextColor(Color.WHITE);
        tokenInput.setHintTextColor(Color.parseColor("#666688"));
        tokenInput.setBackgroundColor(Color.parseColor("#3C3555"));
        tokenInput.setPadding(24, 20, 24, 20);
        tokenInput.setSingleLine(true);
        layout.addView(tokenInput);

        // Login button
        loginButton = new Button(this);
        loginButton.setText("Login & Sync");
        loginButton.setTextColor(Color.WHITE);
        loginButton.setBackgroundColor(Color.parseColor("#4DA6FF"));
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.topMargin = 48;
        loginButton.setLayoutParams(btnParams);
        loginButton.setOnClickListener(v -> doLogin());
        layout.addView(loginButton);

        // Offline button
        offlineButton = new Button(this);
        offlineButton.setText("Play Offline");
        offlineButton.setTextColor(Color.WHITE);
        offlineButton.setBackgroundColor(Color.parseColor("#28233A"));
        LinearLayout.LayoutParams offParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        offParams.topMargin = 16;
        offlineButton.setLayoutParams(offParams);
        offlineButton.setOnClickListener(v -> {
            startActivity(new Intent(this, TabActivity.class));
            finish();
        });
        layout.addView(offlineButton);

        // Status text
        statusText = new TextView(this);
        statusText.setText("Sync your vault items across devices via GitHub");
        statusText.setTextColor(Color.parseColor("#888888"));
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 32, 0, 0);
        layout.addView(statusText);

        scroll.addView(layout);
        setContentView(scroll);
    }

    private void doLogin() {
        String username = usernameInput.getText().toString().trim();
        String token = tokenInput.getText().toString().trim();

        if (username.isEmpty() || token.isEmpty()) {
            statusText.setTextColor(Color.parseColor("#FF4444"));
            statusText.setText("Please enter both username and token");
            return;
        }

        loginButton.setEnabled(false);
        statusText.setTextColor(Color.parseColor("#4DA6FF"));
        statusText.setText("Validating token...");

        GamePreferences.setGitHubUserId(username);
        GamePreferences.setGitHubToken(token);

        GitHubSyncManager.getInstance().validateToken(new ValidateTokenCallback());
    }

    private void proceedToGame() {
        startActivity(new Intent(LoginActivity.this, TabActivity.class));
        finish();
    }

    private class ValidateTokenCallback implements GitHubSyncManager.SyncCallback {
        @Override
        public void onSuccess(String message) {
            statusText.setText("Syncing save data...");
            GitHubSyncManager.getInstance().syncFromCloud(new SyncFromCloudCallback());
        }
        @Override
        public void onError(String error) {
            loginButton.setEnabled(true);
            statusText.setTextColor(Color.parseColor("#FF4444"));
            statusText.setText("Login failed: " + error);
        }
    }

    private class SyncFromCloudCallback implements GitHubSyncManager.SyncCallback {
        @Override
        public void onSuccess(String msg) {
            proceedToGame();
        }
        @Override
        public void onError(String error) {
            // Still proceed even if sync fails
            proceedToGame();
        }
    }
}
