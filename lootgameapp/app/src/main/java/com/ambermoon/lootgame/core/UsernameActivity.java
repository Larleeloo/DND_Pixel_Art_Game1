package com.ambermoon.lootgame.core;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ambermoon.lootgame.save.GoogleDriveSyncManager;
import com.ambermoon.lootgame.save.SaveManager;

import java.util.Collections;
import java.util.List;

/**
 * Username + PIN entry screen shown on app launch.
 * Allows friends to pick their username and enter a PIN to load their save data.
 */
public class UsernameActivity extends Activity {

    public static final String APP_VERSION = "v1.0.0";

    private EditText usernameInput;
    private EditText pinInput;
    private Button playBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Root layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#1A1525"));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(64, 0, 64, 0);

        // Spacer to push content toward center
        View topSpacer = new View(this);
        topSpacer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.5f));
        root.addView(topSpacer);

        // Title
        TextView title = new TextView(this);
        title.setText("Amber Moon");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(32);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.bottomMargin = 4;
        title.setLayoutParams(titleParams);
        root.addView(title);

        // Subtitle
        TextView subtitle = new TextView(this);
        subtitle.setText("Loot Game");
        subtitle.setTextColor(Color.parseColor("#B8A9D4"));
        subtitle.setTextSize(18);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subParams.bottomMargin = 4;
        subtitle.setLayoutParams(subParams);
        root.addView(subtitle);

        // Version number
        TextView versionLabel = new TextView(this);
        versionLabel.setText(APP_VERSION);
        versionLabel.setTextColor(Color.parseColor("#555555"));
        versionLabel.setTextSize(12);
        versionLabel.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams verParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        verParams.bottomMargin = 32;
        versionLabel.setLayoutParams(verParams);
        root.addView(versionLabel);

        // "Enter Username" label
        TextView userLabel = new TextView(this);
        userLabel.setText("Enter Username");
        userLabel.setTextColor(Color.parseColor("#CCCCCC"));
        userLabel.setTextSize(16);
        userLabel.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams userLabelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        userLabelParams.bottomMargin = 8;
        userLabel.setLayoutParams(userLabelParams);
        root.addView(userLabel);

        // Username input field
        usernameInput = new EditText(this);
        usernameInput.setHint("username");
        usernameInput.setHintTextColor(Color.parseColor("#555555"));
        usernameInput.setTextColor(Color.WHITE);
        usernameInput.setTextSize(20);
        usernameInput.setGravity(Gravity.CENTER);
        usernameInput.setBackgroundColor(Color.parseColor("#2A2440"));
        usernameInput.setPadding(32, 24, 32, 24);
        usernameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        usernameInput.setSingleLine(true);
        usernameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        inputParams.bottomMargin = 16;
        usernameInput.setLayoutParams(inputParams);

        // Pre-fill with last username
        String lastUsername = GamePreferences.getUsername();
        if (!lastUsername.isEmpty()) {
            usernameInput.setText(lastUsername);
            usernameInput.setSelection(lastUsername.length());
        }
        root.addView(usernameInput);

        // "Enter PIN" label
        TextView pinLabel = new TextView(this);
        pinLabel.setText("Enter PIN");
        pinLabel.setTextColor(Color.parseColor("#CCCCCC"));
        pinLabel.setTextSize(16);
        pinLabel.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams pinLabelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pinLabelParams.bottomMargin = 8;
        pinLabel.setLayoutParams(pinLabelParams);
        root.addView(pinLabel);

        // PIN input field (4-digit numeric)
        pinInput = new EditText(this);
        pinInput.setHint("4-digit PIN");
        pinInput.setHintTextColor(Color.parseColor("#555555"));
        pinInput.setTextColor(Color.WHITE);
        pinInput.setTextSize(24);
        pinInput.setGravity(Gravity.CENTER);
        pinInput.setBackgroundColor(Color.parseColor("#2A2440"));
        pinInput.setPadding(32, 24, 32, 24);
        pinInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        pinInput.setSingleLine(true);
        pinInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        LinearLayout.LayoutParams pinParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pinParams.bottomMargin = 16;
        pinInput.setLayoutParams(pinParams);
        root.addView(pinInput);

        // Disclaimer
        TextView disclaimer = new TextView(this);
        disclaimer.setText("Note: PINs are stored alongside save data and are not encrypted. "
                + "Do not use a PIN you use for other accounts.");
        disclaimer.setTextColor(Color.parseColor("#AA6644"));
        disclaimer.setTextSize(11);
        disclaimer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams discParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        discParams.bottomMargin = 20;
        disclaimer.setLayoutParams(discParams);
        root.addView(disclaimer);

        // Play button
        playBtn = new Button(this);
        playBtn.setText("PLAY");
        playBtn.setTextColor(Color.parseColor("#1A1525"));
        playBtn.setTextSize(18);
        playBtn.setTypeface(Typeface.DEFAULT_BOLD);
        playBtn.setBackgroundColor(Color.parseColor("#FFD700"));
        playBtn.setPadding(32, 20, 32, 20);
        LinearLayout.LayoutParams playParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        playParams.bottomMargin = 32;
        playBtn.setLayoutParams(playParams);
        playBtn.setOnClickListener(v -> onPlayClicked());
        root.addView(playBtn);

        // Recent usernames section
        List<String> recent = GamePreferences.getRecentUsernames();
        if (!recent.isEmpty()) {
            TextView recentLabel = new TextView(this);
            recentLabel.setText("Recent Players");
            recentLabel.setTextColor(Color.parseColor("#888888"));
            recentLabel.setTextSize(13);
            recentLabel.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams rlParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rlParams.bottomMargin = 8;
            recentLabel.setLayoutParams(rlParams);
            root.addView(recentLabel);

            ScrollView scroll = new ScrollView(this);
            scroll.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));

            LinearLayout recentList = new LinearLayout(this);
            recentList.setOrientation(LinearLayout.VERTICAL);
            recentList.setGravity(Gravity.CENTER_HORIZONTAL);
            scroll.addView(recentList);

            Collections.sort(recent, String.CASE_INSENSITIVE_ORDER);
            for (String name : recent) {
                Button nameBtn = new Button(this);
                nameBtn.setText(name);
                nameBtn.setTextColor(Color.parseColor("#B8A9D4"));
                nameBtn.setTextSize(15);
                nameBtn.setBackgroundColor(Color.parseColor("#2A2440"));
                nameBtn.setPadding(24, 16, 24, 16);
                LinearLayout.LayoutParams nbParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                nbParams.bottomMargin = 8;
                nameBtn.setLayoutParams(nbParams);
                nameBtn.setOnClickListener(v -> {
                    usernameInput.setText(name);
                    pinInput.setText("");
                    pinInput.requestFocus();
                });
                recentList.addView(nameBtn);
            }
            root.addView(scroll);
        }

        // Bottom spacer
        View bottomSpacer = new View(this);
        bottomSpacer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));
        root.addView(bottomSpacer);

        setContentView(root);
    }

    private void onPlayClicked() {
        String username = usernameInput.getText().toString().trim();
        String pin = pinInput.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!username.matches("[A-Za-z0-9_\\-]+")) {
            Toast.makeText(this, "Letters, numbers, _ and - only", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pin.length() != 4) {
            Toast.makeText(this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button while checking
        playBtn.setEnabled(false);
        playBtn.setText("Checking...");

        // Step 1: Check local save
        if (SaveManager.localSaveExists(this, username)) {
            String savedPin = SaveManager.readLocalPin(this, username);
            if (savedPin.isEmpty()) {
                // Legacy save with no PIN — set the PIN and proceed
                proceedToGame(username, pin, true);
            } else if (savedPin.equals(pin)) {
                proceedToGame(username, pin, false);
            } else {
                showPinError();
            }
            return;
        }

        // Step 2: No local save — check cloud
        GoogleDriveSyncManager.getInstance().fetchCloudSave(username, (json, error) -> {
            if (json != null) {
                // Cloud save exists — check PIN
                String cloudPin = SaveManager.extractPinFromJson(json);
                if (cloudPin.isEmpty()) {
                    // Cloud save with no PIN — set PIN and proceed
                    proceedToGame(username, pin, true);
                } else if (cloudPin.equals(pin)) {
                    proceedToGame(username, pin, false);
                } else {
                    showPinError();
                }
            } else {
                // No cloud save — this is a brand new user
                proceedToGame(username, pin, true);
            }
        });
    }

    private void proceedToGame(String username, String pin, boolean setPin) {
        GamePreferences.setUsername(username);
        SaveManager.init(this);

        if (setPin) {
            SaveManager.getInstance().getData().pin = pin;
            SaveManager.getInstance().save();
        }

        // Sync from cloud before entering the game so local data is up to date
        playBtn.setText("Syncing...");
        GoogleDriveSyncManager.getInstance().syncFromCloud((success, msg) -> {
            startActivity(new Intent(UsernameActivity.this, TabActivity.class));
            finish();
        });
    }

    private void showPinError() {
        playBtn.setEnabled(true);
        playBtn.setText("PLAY");
        Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
    }
}
