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

import com.ambermoon.lootgame.save.SaveManager;

import java.util.Collections;
import java.util.List;

/**
 * Username entry screen shown on app launch.
 * Allows friends to pick their username to load their specific save data.
 * No password required.
 */
public class UsernameActivity extends Activity {

    private EditText usernameInput;
    private LinearLayout recentList;

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
        titleParams.bottomMargin = 12;
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
        subParams.bottomMargin = 48;
        subtitle.setLayoutParams(subParams);
        root.addView(subtitle);

        // "Enter Username" label
        TextView label = new TextView(this);
        label.setText("Enter Username");
        label.setTextColor(Color.parseColor("#CCCCCC"));
        label.setTextSize(16);
        label.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        labelParams.bottomMargin = 16;
        label.setLayoutParams(labelParams);
        root.addView(label);

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
        // Limit username length and disallow special characters that break file paths
        usernameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        inputParams.bottomMargin = 24;
        usernameInput.setLayoutParams(inputParams);

        // Pre-fill with last username
        String lastUsername = GamePreferences.getUsername();
        if (!lastUsername.isEmpty()) {
            usernameInput.setText(lastUsername);
            usernameInput.setSelection(lastUsername.length());
        }

        root.addView(usernameInput);

        // Play button
        Button playBtn = new Button(this);
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

            recentList = new LinearLayout(this);
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
                    onPlayClicked();
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

        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sanitize: only allow alphanumeric, underscore, hyphen
        if (!username.matches("[A-Za-z0-9_\\-]+")) {
            Toast.makeText(this, "Letters, numbers, _ and - only", Toast.LENGTH_SHORT).show();
            return;
        }

        // Store username and initialize save for this user
        GamePreferences.setUsername(username);
        SaveManager.init(this);

        // Launch the game
        startActivity(new Intent(this, TabActivity.class));
        finish();
    }
}
