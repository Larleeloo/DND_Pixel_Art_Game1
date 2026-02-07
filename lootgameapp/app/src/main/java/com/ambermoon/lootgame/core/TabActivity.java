package com.ambermoon.lootgame.core;

import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import com.ambermoon.lootgame.save.SaveManager;
import com.ambermoon.lootgame.save.GoogleDriveSyncManager;
import com.ambermoon.lootgame.tabs.*;

public class TabActivity extends Activity {
    private FrameLayout contentFrame;
    private LinearLayout tabBar;
    private TextView coinDisplay;
    private View currentTab;
    private int currentTabIndex = 0;

    private static final String[] TAB_LABELS = {"Daily", "Monthly", "Alchemy", "Decon", "Slots", "Vault"};
    private static final String[] TAB_ICONS = {"\u2623", "\u2623", "\u2697", "\uD83D\uDD28", "\uD83C\uDFB0", "\uD83C\uDFDB"};
    private Button[] tabButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#1A1525"));

        // Header bar
        RelativeLayout header = new RelativeLayout(this);
        header.setBackgroundColor(Color.parseColor("#1E1830"));
        header.setPadding(24, 16, 24, 16);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        header.setLayoutParams(headerParams);

        coinDisplay = new TextView(this);
        coinDisplay.setTextColor(Color.parseColor("#FFD700"));
        coinDisplay.setTextSize(18);
        updateCoinDisplay();
        RelativeLayout.LayoutParams coinParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        coinParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        coinParams.addRule(RelativeLayout.CENTER_VERTICAL);
        coinDisplay.setLayoutParams(coinParams);
        header.addView(coinDisplay);

        // Sync button
        Button syncBtn = new Button(this);
        syncBtn.setText("\u21BB");
        syncBtn.setTextColor(Color.parseColor("#4DA6FF"));
        syncBtn.setTextSize(18);
        syncBtn.setBackgroundColor(Color.TRANSPARENT);
        syncBtn.setOnClickListener(v -> doSync());
        RelativeLayout.LayoutParams syncParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        syncParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        syncParams.addRule(RelativeLayout.CENTER_VERTICAL);
        syncBtn.setLayoutParams(syncParams);
        header.addView(syncBtn);

        root.addView(header);

        // Content area
        contentFrame = new FrameLayout(this);
        contentFrame.setBackgroundColor(Color.parseColor("#1A1525"));
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
        contentFrame.setLayoutParams(contentParams);
        root.addView(contentFrame);

        // Tab bar
        tabBar = new LinearLayout(this);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setBackgroundColor(Color.parseColor("#0F0D17"));
        tabBar.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tabBarParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tabBar.setLayoutParams(tabBarParams);

        tabButtons = new Button[TAB_LABELS.length];
        for (int i = 0; i < TAB_LABELS.length; i++) {
            final int idx = i;
            Button btn = new Button(this);
            btn.setText(TAB_LABELS[i]);
            btn.setTextColor(Color.parseColor("#888888"));
            btn.setTextSize(11);
            btn.setBackgroundColor(Color.TRANSPARENT);
            btn.setPadding(8, 16, 8, 16);
            LinearLayout.LayoutParams btnP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            btn.setLayoutParams(btnP);
            btn.setOnClickListener(v -> switchTab(idx));
            tabBar.addView(btn);
            tabButtons[i] = btn;
        }
        root.addView(tabBar);

        setContentView(root);
        switchTab(0);
    }

    public void switchTab(int index) {
        currentTabIndex = index;
        contentFrame.removeAllViews();

        // Update tab button colors
        for (int i = 0; i < tabButtons.length; i++) {
            tabButtons[i].setTextColor(i == index ? Color.parseColor("#FFD700") : Color.parseColor("#888888"));
        }

        View tabView;
        switch (index) {
            case 0: tabView = new DailyChestTab(this); break;
            case 1: tabView = new MonthlyChestTab(this); break;
            case 2: tabView = new AlchemyTab(this); break;
            case 3: tabView = new DeconstructTab(this); break;
            case 4: tabView = new SlotMachineTab(this); break;
            case 5: tabView = new VaultTab(this); break;
            default: tabView = new DailyChestTab(this); break;
        }
        currentTab = tabView;
        contentFrame.addView(tabView);
        updateCoinDisplay();
    }

    public void updateCoinDisplay() {
        if (coinDisplay != null && SaveManager.getInstance() != null) {
            coinDisplay.setText("\u25C8 " + SaveManager.getInstance().getData().coins + " coins");
        }
    }

    private void doSync() {
        SaveManager.getInstance().save();
        if (GamePreferences.isLoggedIn()) {
            // If logged in with access token, upload to Google Drive
            GoogleDriveSyncManager.getInstance().syncToCloud(new CloudSyncCallback());
        } else {
            // Even without login, can still download from public file
            GoogleDriveSyncManager.getInstance().syncFromCloud(new GoogleDriveSyncManager.SyncCallback() {
                @Override
                public void onSuccess(String msg) {
                    runOnUiThread(() -> {
                        switchTab(currentTabIndex); // Refresh current tab
                        Toast.makeText(TabActivity.this, "Loaded from Google Drive", Toast.LENGTH_SHORT).show();
                    });
                }
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(TabActivity.this, "Sync failed: " + error, Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    private class CloudSyncCallback implements GoogleDriveSyncManager.SyncCallback {
        @Override public void onSuccess(String msg) {
            runOnUiThread(() -> Toast.makeText(TabActivity.this, "Synced to Google Drive!", Toast.LENGTH_SHORT).show());
        }
        @Override public void onError(String error) {
            runOnUiThread(() -> Toast.makeText(TabActivity.this, "Sync failed: " + error, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SaveManager.getInstance() != null) SaveManager.getInstance().save();
    }
}
