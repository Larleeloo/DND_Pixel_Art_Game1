package com.ambermoon.lootgame.tabs;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import com.ambermoon.lootgame.audio.HapticManager;
import com.ambermoon.lootgame.core.TabActivity;
import com.ambermoon.lootgame.entity.*;
import com.ambermoon.lootgame.graphics.BackgroundRegistry;
import com.ambermoon.lootgame.graphics.CoinIconHelper;
import com.ambermoon.lootgame.save.SaveManager;

import java.util.ArrayList;
import java.util.List;

public class MonthlyChestTab extends ScrollView {
    private LinearLayout content;
    private Button openButton;
    private TextView timerText;
    private LinearLayout lootDisplay;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timerTick;
    private ChestIconView chestView;
    private TextView infoText;

    public MonthlyChestTab(Context context) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);

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

        // Chest GIF icon (replaces emoji placeholder)
        chestView = new ChestIconView(context, "chests/monthly_chest.gif",
                Color.parseColor("#B464FF"));
        LinearLayout.LayoutParams chestParams = new LinearLayout.LayoutParams(240, 240);
        chestParams.gravity = Gravity.CENTER;
        chestParams.topMargin = 32;
        chestParams.bottomMargin = 16;
        chestView.setLayoutParams(chestParams);
        content.addView(chestView);

        infoText = new TextView(context);
        infoText.setTextColor(Color.parseColor("#AAAACC"));
        infoText.setTextSize(14);
        infoText.setGravity(Gravity.CENTER);
        infoText.setPadding(0, 0, 0, 24);
        content.addView(infoText);

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

    private int getStreakBonusItems() {
        SaveManager sm = SaveManager.getInstance();
        return Math.min(sm.getData().consecutiveDays / 5, 15);
    }

    private void updateUI() {
        SaveManager sm = SaveManager.getInstance();

        // Update info text with streak bonus
        int bonusItems = getStreakBonusItems();
        if (bonusItems > 0) {
            infoText.setText(CoinIconHelper.withCoin(getContext(),
                    "\u25C8 500-2000 coins  |  \u2605 10 + " + bonusItems
                    + " streak items  |  2.5x rarity boost", 14));
        } else {
            infoText.setText(CoinIconHelper.withCoin(getContext(),
                    "\u25C8 500-2000 coins  |  \u2605 10 items  |  2.5x rarity boost", 14));
        }

        if (sm.canOpenMonthlyChest()) {
            openButton.setEnabled(true);
            openButton.setBackgroundColor(Color.parseColor("#B464FF"));
            openButton.setText("OPEN CHEST");
            timerText.setText("");
            chestView.showFirstFrame();
        } else {
            openButton.setEnabled(false);
            openButton.setBackgroundColor(Color.parseColor("#444444"));
            openButton.setText("ON COOLDOWN");
            chestView.showLastFrame();
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

        // Play chest opening animation (forward once, hold last frame)
        chestView.playOnce();

        // Haptic: dramatic monthly chest opening rumble
        HapticManager.getInstance().chestOpenMonthly();

        sm.recordMonthlyChestOpened();

        // Calculate streak bonus items: +1 item per 5 consecutive days, max 15 bonus
        int bonusItems = getStreakBonusItems();
        int totalItems = 10 + bonusItems;

        List<Item> loot = LootTable.generateLoot(totalItems, 2.5f);
        int coins = CoinReward.calculateMonthly();
        sm.addCoins(coins);

        // Staggered item-drop haptics for all items
        for (int idx = 0; idx < loot.size(); idx++) {
            handler.postDelayed(() -> HapticManager.getInstance().itemDrop(), 400 + idx * 120);
        }

        for (Item item : loot) {
            String id = item.getRegistryId();
            if (id != null) {
                sm.addVaultItem(id, 1);
                sm.getData().totalItemsCollected++;
                if (item.getRarity().ordinal() == Item.RARITY_LEGENDARY) sm.getData().legendaryItemsFound++;
                if (item.getRarity().ordinal() == Item.RARITY_MYTHIC) sm.getData().mythicItemsFound++;
            }
        }

        // Roll for background unlock (40% chance, 2.5x rarity boost for monthly)
        BackgroundRegistry.BackgroundEntry bgDrop = BackgroundRegistry.rollChestDrop(
                sm.getUnlockedBackgroundIds(), 2.5f, 0.40f);
        if (bgDrop != null) {
            sm.unlockBackground(bgDrop.id);
        }

        sm.save();

        // Show loot
        lootDisplay.removeAllViews();

        TextView coinText = new TextView(getContext());
        coinText.setText(CoinIconHelper.withCoin(getContext(),
                "\u25C8 +" + coins + " coins", 20));
        coinText.setTextColor(Color.parseColor("#FFD700"));
        coinText.setTextSize(20);
        coinText.setTypeface(Typeface.DEFAULT_BOLD);
        coinText.setGravity(Gravity.CENTER);
        coinText.setPadding(0, 16, 0, 24);
        lootDisplay.addView(coinText);

        // Streak bonus notification
        if (bonusItems > 0) {
            TextView streakText = new TextView(getContext());
            streakText.setText("+" + bonusItems + " bonus item" + (bonusItems > 1 ? "s" : "")
                    + " from " + sm.getData().consecutiveDays + "-day streak!");
            streakText.setTextColor(Color.parseColor("#B464FF"));
            streakText.setTextSize(14);
            streakText.setTypeface(Typeface.DEFAULT_BOLD);
            streakText.setGravity(Gravity.CENTER);
            streakText.setPadding(0, 0, 0, 16);
            lootDisplay.addView(streakText);
        }

        // Background drop notification
        if (bgDrop != null) {
            LinearLayout bgCard = new LinearLayout(getContext());
            bgCard.setOrientation(LinearLayout.HORIZONTAL);
            bgCard.setGravity(Gravity.CENTER);
            bgCard.setBackgroundColor(Color.parseColor("#28233A"));
            bgCard.setPadding(24, 16, 24, 16);
            LinearLayout.LayoutParams bgCardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            bgCardParams.bottomMargin = 16;
            bgCard.setLayoutParams(bgCardParams);

            View swatch = new View(getContext());
            swatch.setBackgroundColor(bgDrop.solidColor);
            LinearLayout.LayoutParams swatchParams = new LinearLayout.LayoutParams(48, 48);
            swatchParams.rightMargin = 16;
            swatch.setLayoutParams(swatchParams);
            bgCard.addView(swatch);

            TextView bgText = new TextView(getContext());
            bgText.setText("\u2728 NEW BACKGROUND: " + bgDrop.displayName);
            bgText.setTextColor(BackgroundRegistry.getRarityColor(bgDrop.rarity));
            bgText.setTextSize(14);
            bgText.setTypeface(Typeface.DEFAULT_BOLD);
            bgCard.addView(bgText);

            lootDisplay.addView(bgCard);
        }

        // Display items in rows of 5 with item icon sprites
        int rows = (int) Math.ceil(loot.size() / 5.0);
        for (int row = 0; row < rows; row++) {
            LinearLayout rowLayout = new LinearLayout(getContext());
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER);
            rowLayout.setPadding(0, 8, 0, 8);
            for (int col = 0; col < 5 && row * 5 + col < loot.size(); col++) {
                Item item = loot.get(row * 5 + col);

                LinearLayout itemCard = new LinearLayout(getContext());
                itemCard.setOrientation(LinearLayout.VERTICAL);
                itemCard.setGravity(Gravity.CENTER);
                itemCard.setBackgroundColor(Color.parseColor("#28233A"));
                itemCard.setPadding(4, 4, 4, 4);

                // Item icon sprite (first frame of idle GIF)
                View iconView = new ItemIconView(getContext(), item);
                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(80, 80);
                iconParams.gravity = Gravity.CENTER;
                iconView.setLayoutParams(iconParams);
                itemCard.addView(iconView);

                // Item name below icon
                TextView nameView = new TextView(getContext());
                nameView.setText(item.getName());
                nameView.setTextColor(Item.getRarityColor(item.getRarity().ordinal()));
                nameView.setTextSize(9);
                nameView.setGravity(Gravity.CENTER);
                nameView.setMaxLines(2);
                nameView.setPadding(0, 4, 0, 0);
                itemCard.addView(nameView);

                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                p.setMargins(2, 0, 2, 0);
                itemCard.setLayoutParams(p);
                rowLayout.addView(itemCard);
            }
            lootDisplay.addView(rowLayout);
        }

        updateUI();
        if (getContext() instanceof TabActivity) ((TabActivity) getContext()).updateCoinDisplay();
    }
}
