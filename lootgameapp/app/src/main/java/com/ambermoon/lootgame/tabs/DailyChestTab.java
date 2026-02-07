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

public class DailyChestTab extends ScrollView {
    private LinearLayout content;
    private Button openButton;
    private TextView timerText;
    private LinearLayout lootDisplay;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timerTick;
    private List<Item> lastLoot = new ArrayList<>();

    public DailyChestTab(Context context) {
        super(context);
        setBackgroundColor(Color.parseColor("#1A1525"));

        content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(48, 32, 48, 32);

        // Title
        TextView title = new TextView(context);
        title.setText("DAILY TREASURE");
        title.setTextColor(Color.parseColor("#FFB347"));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(title);

        // Chest visual placeholder
        TextView chestIcon = new TextView(context);
        chestIcon.setText("\uD83D\uDCE6");
        chestIcon.setTextSize(80);
        chestIcon.setGravity(Gravity.CENTER);
        chestIcon.setPadding(0, 32, 0, 16);
        content.addView(chestIcon);

        // Info
        TextView info = new TextView(context);
        info.setText("\u25C8 50-250 coins  |  \u2605 3 items");
        info.setTextColor(Color.parseColor("#AAAACC"));
        info.setTextSize(14);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, 0, 0, 24);
        content.addView(info);

        // Open button
        openButton = new Button(context);
        openButton.setText("OPEN CHEST");
        openButton.setTextColor(Color.WHITE);
        openButton.setTextSize(18);
        openButton.setTypeface(Typeface.DEFAULT_BOLD);
        openButton.setBackgroundColor(Color.parseColor("#FFB347"));
        openButton.setPadding(48, 24, 48, 24);
        openButton.setOnClickListener(v -> openChest());
        content.addView(openButton);

        // Timer text
        timerText = new TextView(context);
        timerText.setTextColor(Color.parseColor("#FF6666"));
        timerText.setTextSize(16);
        timerText.setGravity(Gravity.CENTER);
        timerText.setPadding(0, 8, 0, 24);
        content.addView(timerText);

        // Loot display area
        lootDisplay = new LinearLayout(context);
        lootDisplay.setOrientation(LinearLayout.VERTICAL);
        lootDisplay.setGravity(Gravity.CENTER_HORIZONTAL);
        content.addView(lootDisplay);

        // Stats
        addStats();

        addView(content);
        updateUI();
        startTimer();
    }

    private void updateUI() {
        SaveManager sm = SaveManager.getInstance();
        if (sm.canOpenDailyChest()) {
            openButton.setEnabled(true);
            openButton.setBackgroundColor(Color.parseColor("#FFB347"));
            openButton.setText("OPEN CHEST");
            timerText.setText("");
        } else {
            openButton.setEnabled(false);
            openButton.setBackgroundColor(Color.parseColor("#444444"));
            openButton.setText("ON COOLDOWN");
            updateTimer();
        }
    }

    private void startTimer() {
        timerTick = () -> {
            updateTimer();
            handler.postDelayed(timerTick, 1000);
        };
        handler.postDelayed(timerTick, 1000);
    }

    private void updateTimer() {
        SaveManager sm = SaveManager.getInstance();
        if (sm.canOpenDailyChest()) {
            updateUI();
            return;
        }
        long remaining = sm.getDailyTimeRemaining();
        long hours = remaining / (60 * 60 * 1000);
        long mins = (remaining / (60 * 1000)) % 60;
        long secs = (remaining / 1000) % 60;
        timerText.setText(String.format("Available in %02d:%02d:%02d", hours, mins, secs));
    }

    private void openChest() {
        SaveManager sm = SaveManager.getInstance();
        if (!sm.canOpenDailyChest()) return;

        sm.recordDailyChestOpened();

        // Generate loot
        lastLoot = LootTable.generateLoot(3, 1.0f);
        int coins = CoinReward.calculateDaily(sm.getData().consecutiveDays);
        sm.addCoins(coins);

        // Add items to vault
        for (Item item : lastLoot) {
            String id = item.getRegistryId();
            if (id != null) {
                sm.addVaultItem(id, 1);
                sm.getData().totalItemsCollected++;
                if (item.getRarity().ordinal() == Item.RARITY_LEGENDARY) sm.getData().legendaryItemsFound++;
                if (item.getRarity().ordinal() == Item.RARITY_MYTHIC) sm.getData().mythicItemsFound++;
            }
        }

        sm.save();

        // Display loot
        showLoot(coins);
        updateUI();

        // Update coin display in parent
        if (getContext() instanceof TabActivity) {
            ((TabActivity) getContext()).updateCoinDisplay();
        }
    }

    private void showLoot(int coins) {
        lootDisplay.removeAllViews();

        // Coin reward
        TextView coinText = new TextView(getContext());
        coinText.setText("\u25C8 +" + coins + " coins");
        coinText.setTextColor(Color.parseColor("#FFD700"));
        coinText.setTextSize(20);
        coinText.setTypeface(Typeface.DEFAULT_BOLD);
        coinText.setGravity(Gravity.CENTER);
        coinText.setPadding(0, 16, 0, 24);
        lootDisplay.addView(coinText);

        // Section header
        TextView header = new TextView(getContext());
        header.setText("--- Loot Drops ---");
        header.setTextColor(Color.parseColor("#AAAACC"));
        header.setTextSize(14);
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, 0, 0, 16);
        lootDisplay.addView(header);

        // Item display row
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        for (Item item : lastLoot) {
            LinearLayout itemCard = createItemCard(item);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            p.setMargins(8, 0, 8, 0);
            itemCard.setLayoutParams(p);
            row.addView(itemCard);
        }
        lootDisplay.addView(row);
    }

    private LinearLayout createItemCard(Item item) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackgroundColor(Color.parseColor("#28233A"));
        card.setPadding(12, 12, 12, 12);

        // Item icon placeholder (colored square with rarity border)
        View iconView = new ItemIconView(getContext(), item);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(120, 120);
        iconView.setLayoutParams(iconParams);
        card.addView(iconView);

        // Item name
        TextView nameText = new TextView(getContext());
        nameText.setText(item.getName());
        nameText.setTextColor(Item.getRarityColor(item.getRarity().ordinal()));
        nameText.setTextSize(11);
        nameText.setGravity(Gravity.CENTER);
        nameText.setMaxLines(2);
        nameText.setPadding(0, 8, 0, 0);
        card.addView(nameText);

        // Rarity label
        TextView rarityText = new TextView(getContext());
        rarityText.setText(item.getRarity().getDisplayName());
        rarityText.setTextColor(Color.parseColor("#888888"));
        rarityText.setTextSize(10);
        rarityText.setGravity(Gravity.CENTER);
        card.addView(rarityText);

        return card;
    }

    private void addStats() {
        SaveManager sm = SaveManager.getInstance();

        TextView statsHeader = new TextView(getContext());
        statsHeader.setText("\n--- Statistics ---");
        statsHeader.setTextColor(Color.parseColor("#AAAACC"));
        statsHeader.setTextSize(14);
        statsHeader.setGravity(Gravity.CENTER);
        statsHeader.setPadding(0, 32, 0, 8);
        content.addView(statsHeader);

        addStatLine("Total items collected", String.valueOf(sm.getData().totalItemsCollected));
        addStatLine("Legendary found", String.valueOf(sm.getData().legendaryItemsFound));
        addStatLine("Mythic found", String.valueOf(sm.getData().mythicItemsFound));
        addStatLine("Consecutive days", String.valueOf(sm.getData().consecutiveDays));
    }

    private void addStatLine(String label, String value) {
        LinearLayout line = new LinearLayout(getContext());
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setPadding(0, 4, 0, 4);

        TextView labelText = new TextView(getContext());
        labelText.setText(label + ": ");
        labelText.setTextColor(Color.parseColor("#888888"));
        labelText.setTextSize(13);
        line.addView(labelText);

        TextView valueText = new TextView(getContext());
        valueText.setText(value);
        valueText.setTextColor(Color.WHITE);
        valueText.setTextSize(13);
        line.addView(valueText);

        content.addView(line);
    }

}
