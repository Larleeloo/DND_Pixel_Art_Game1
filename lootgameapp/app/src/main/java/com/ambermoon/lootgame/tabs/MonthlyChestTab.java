package com.ambermoon.lootgame.tabs;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import com.ambermoon.lootgame.core.TabActivity;
import com.ambermoon.lootgame.entity.*;
import com.ambermoon.lootgame.save.SaveManager;

import java.util.ArrayList;
import java.util.List;

public class MonthlyChestTab extends ScrollView {
    private LinearLayout content;
    private Button openButton;
    private TextView timerText;
    private LinearLayout lootDisplay;
    private Handler handler = new Handler(Looper.getMainLooper());

    public MonthlyChestTab(Context context) {
        super(context);
        setBackgroundColor(Color.parseColor("#1A1525"));

        content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(48, 32, 48, 32);

        TextView title = new TextView(context);
        title.setText("MONTHLY TREASURE");
        title.setTextColor(Color.parseColor("#B464FF"));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(title);

        TextView chestIcon = new TextView(context);
        chestIcon.setText("\uD83C\uDF81");
        chestIcon.setTextSize(80);
        chestIcon.setGravity(Gravity.CENTER);
        chestIcon.setPadding(0, 32, 0, 16);
        content.addView(chestIcon);

        TextView info = new TextView(context);
        info.setText("\u25C8 500-2000 coins  |  \u2605 10 items  |  2.5x rarity boost");
        info.setTextColor(Color.parseColor("#AAAACC"));
        info.setTextSize(14);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, 0, 0, 24);
        content.addView(info);

        openButton = new Button(context);
        openButton.setText("OPEN CHEST");
        openButton.setTextColor(Color.WHITE);
        openButton.setTextSize(18);
        openButton.setTypeface(Typeface.DEFAULT_BOLD);
        openButton.setBackgroundColor(Color.parseColor("#B464FF"));
        openButton.setPadding(48, 24, 48, 24);
        openButton.setOnClickListener(v -> openChest());
        content.addView(openButton);

        timerText = new TextView(context);
        timerText.setTextColor(Color.parseColor("#FF6666"));
        timerText.setTextSize(16);
        timerText.setGravity(Gravity.CENTER);
        timerText.setPadding(0, 8, 0, 24);
        content.addView(timerText);

        lootDisplay = new LinearLayout(context);
        lootDisplay.setOrientation(LinearLayout.VERTICAL);
        lootDisplay.setGravity(Gravity.CENTER_HORIZONTAL);
        content.addView(lootDisplay);

        addView(content);
        updateUI();
        startTimer();
    }

    private void updateUI() {
        SaveManager sm = SaveManager.getInstance();
        if (sm.canOpenMonthlyChest()) {
            openButton.setEnabled(true);
            openButton.setBackgroundColor(Color.parseColor("#B464FF"));
            openButton.setText("OPEN CHEST");
            timerText.setText("");
        } else {
            openButton.setEnabled(false);
            openButton.setBackgroundColor(Color.parseColor("#444444"));
            openButton.setText("ON COOLDOWN");
        }
    }

    private void startTimer() {
        handler.postDelayed(new TimerRunnable(), 1000);
    }

    private class TimerRunnable implements Runnable {
        @Override public void run() {
            updateTimer();
            handler.postDelayed(this, 1000);
        }
    }

    private void updateTimer() {
        SaveManager sm = SaveManager.getInstance();
        if (sm.canOpenMonthlyChest()) { updateUI(); return; }
        long remaining = sm.getMonthlyTimeRemaining();
        long days = remaining / (24 * 60 * 60 * 1000);
        long hours = (remaining / (60 * 60 * 1000)) % 24;
        long mins = (remaining / (60 * 1000)) % 60;
        long secs = (remaining / 1000) % 60;
        timerText.setText(String.format("Available in %dd %02d:%02d:%02d", days, hours, mins, secs));
    }

    private void openChest() {
        SaveManager sm = SaveManager.getInstance();
        if (!sm.canOpenMonthlyChest()) return;

        sm.recordMonthlyChestOpened();

        List<Item> loot = LootTable.generateLoot(10, 2.5f);
        int coins = CoinReward.calculateMonthly();
        sm.addCoins(coins);

        for (Item item : loot) {
            String id = item.getRegistryId();
            if (id != null) {
                sm.addVaultItem(id, 1);
                sm.getData().totalItemsCollected++;
                if (item.getRarity().ordinal() == Item.RARITY_LEGENDARY) sm.getData().legendaryItemsFound++;
                if (item.getRarity().ordinal() == Item.RARITY_MYTHIC) sm.getData().mythicItemsFound++;
            }
        }
        sm.save();

        // Show loot
        lootDisplay.removeAllViews();

        TextView coinText = new TextView(getContext());
        coinText.setText("\u25C8 +" + coins + " coins");
        coinText.setTextColor(Color.parseColor("#FFD700"));
        coinText.setTextSize(20);
        coinText.setTypeface(Typeface.DEFAULT_BOLD);
        coinText.setGravity(Gravity.CENTER);
        coinText.setPadding(0, 16, 0, 24);
        lootDisplay.addView(coinText);

        // Display in 2 rows of 5
        for (int row = 0; row < 2; row++) {
            LinearLayout rowLayout = new LinearLayout(getContext());
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER);
            rowLayout.setPadding(0, 8, 0, 8);
            for (int col = 0; col < 5 && row * 5 + col < loot.size(); col++) {
                Item item = loot.get(row * 5 + col);
                TextView itemView = new TextView(getContext());
                itemView.setText(item.getName());
                itemView.setTextColor(Item.getRarityColor(item.getRarity().ordinal()));
                itemView.setTextSize(10);
                itemView.setGravity(Gravity.CENTER);
                itemView.setBackgroundColor(Color.parseColor("#28233A"));
                itemView.setPadding(8, 8, 8, 8);
                itemView.setMaxLines(2);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                p.setMargins(4, 0, 4, 0);
                itemView.setLayoutParams(p);
                rowLayout.addView(itemView);
            }
            lootDisplay.addView(rowLayout);
        }

        updateUI();
        if (getContext() instanceof TabActivity) ((TabActivity) getContext()).updateCoinDisplay();
    }
}
