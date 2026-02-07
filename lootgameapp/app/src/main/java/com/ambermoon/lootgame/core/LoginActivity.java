package com.ambermoon.lootgame.core;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import com.ambermoon.lootgame.save.GoogleDriveSyncManager;

public class LoginActivity extends Activity {
    private EditText tokenInput;
    // Package-private to avoid synthetic accessors that crash D8 dex compiler
    TextView statusText;
    Button loginButton;
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

        // Google Drive info
        TextView driveLabel = new TextView(this);
        driveLabel.setText("Google Drive Access Token");
        driveLabel.setTextColor(Color.parseColor("#AAAACC"));
        driveLabel.setPadding(0, 0, 0, 8);
        layout.addView(driveLabel);

        tokenInput = new EditText(this);
        tokenInput.setHint("ya29...");
        tokenInput.setTextColor(Color.WHITE);
        tokenInput.setHintTextColor(Color.parseColor("#666688"));
        tokenInput.setBackgroundColor(Color.parseColor("#3C3555"));
        tokenInput.setPadding(24, 20, 24, 20);
        tokenInput.setSingleLine(true);
        layout.addView(tokenInput);

        // Help text
        TextView helpText = new TextView(this);
        helpText.setText("Enter a Google OAuth access token\nwith Drive file access permissions.\nThis allows syncing your vault to Google Drive.");
        helpText.setTextColor(Color.parseColor("#888899"));
        helpText.setTextSize(12);
        helpText.setPadding(0, 8, 0, 0);
        layout.addView(helpText);

        // Login button
        loginButton = new Button(this);
        loginButton.setText("Connect & Sync");
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
        statusText.setText("Sync your vault items across devices via Google Drive");
        statusText.setTextColor(Color.parseColor("#888888"));
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 32, 0, 0);
        layout.addView(statusText);

        scroll.addView(layout);
        setContentView(scroll);
    }

    private void doLogin() {
        String token = tokenInput.getText().toString().trim();

        if (token.isEmpty()) {
            statusText.setTextColor(Color.parseColor("#FF4444"));
            statusText.setText("Please enter a Google access token");
            return;
        }

        loginButton.setEnabled(false);
        statusText.setTextColor(Color.parseColor("#4DA6FF"));
        statusText.setText("Validating token...");

        GamePreferences.setGoogleAccessToken(token);

        GoogleDriveSyncManager.getInstance().validateToken(new ValidateTokenCallback());
    }

    // Package-private to avoid synthetic accessors that crash D8 dex compiler
    void proceedToGame() {
        startActivity(new Intent(LoginActivity.this, TabActivity.class));
        finish();
    }

    // Package-private inner classes to avoid synthetic accessors that crash D8 dex compiler
    class ValidateTokenCallback implements GoogleDriveSyncManager.SyncCallback {
        @Override
        public void onSuccess(String message) {
            statusText.setText("Syncing save data from Google Drive...");
            GoogleDriveSyncManager.getInstance().syncFromCloud(new SyncFromCloudCallback());
        }
        @Override
        public void onError(String error) {
            loginButton.setEnabled(true);
            statusText.setTextColor(Color.parseColor("#FF4444"));
            statusText.setText("Connection failed: " + error);
        }
    }

    class SyncFromCloudCallback implements GoogleDriveSyncManager.SyncCallback {
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
