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
import com.ambermoon.lootgame.graphics.CoinIconHelper;
import com.ambermoon.lootgame.save.SaveManager;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class SlotMachineTab extends ScrollView {
    private static final int COST_PER_PULL = 25;
    private static final String[] SYMBOLS = {"Apple", "Sword", "Shield", "Gem", "Star", "Crown"};
    private static final int[] SYMBOL_COLORS = {
        Color.WHITE, Color.rgb(30, 255, 30), Color.rgb(30, 100, 255),
        Color.rgb(180, 30, 255), Color.rgb(255, 165, 0), Color.rgb(0, 255, 255)
    };
    private static final int[] WEIGHTS = {30, 25, 20, 15, 8, 2};
    private static final int[] TRIPLE_PAYOUTS = {50, 100, 200, 500, 1000, 5000};
    private static final int DOUBLE_PAYOUT = 10;

    private SecureRandom rng = new SecureRandom();
    private SlotReelView[] reelViews = new SlotReelView[3];
    private Button pullButton;
    private TextView balanceText;
    private TextView resultText;
    private LinearLayout historyLayout;
    private List<String> history = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean spinning = false;

    public SlotMachineTab(Context context) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(32, 24, 32, 24);

        TextView title = new TextView(context);
        title.setText("LUCKY SLOTS");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(title);

        balanceText = new TextView(context);
        balanceText.setTextColor(Color.parseColor("#FFD700"));
        balanceText.setTextSize(16);
        balanceText.setGravity(Gravity.CENTER);
        balanceText.setPadding(0, 16, 0, 24);
        content.addView(balanceText);

        // Reels (vertically scrolling symbol animation)
        LinearLayout reelRow = new LinearLayout(context);
        reelRow.setOrientation(LinearLayout.HORIZONTAL);
        reelRow.setGravity(Gravity.CENTER);
        reelRow.setBackgroundColor(Color.parseColor("#0F0D17"));
        reelRow.setPadding(16, 16, 16, 16);

        for (int i = 0; i < 3; i++) {
            reelViews[i] = new SlotReelView(context);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0, 200, 1.0f);
            rp.setMargins(8, 0, 8, 0);
            reelViews[i].setLayoutParams(rp);
            reelRow.addView(reelViews[i]);
        }
        content.addView(reelRow);

        // Result
        resultText = new TextView(context);
        resultText.setTextSize(18);
        resultText.setGravity(Gravity.CENTER);
        resultText.setPadding(0, 16, 0, 16);
        content.addView(resultText);

        // Pull button
        pullButton = new Button(context);
        CoinIconHelper.setCoinText(pullButton,
                "PULL  (\u25C8 " + COST_PER_PULL + ")", 18);
        pullButton.setTextColor(Color.WHITE);
        pullButton.setTextSize(18);
        pullButton.setTypeface(Typeface.DEFAULT_BOLD);
        pullButton.setBackgroundColor(Color.parseColor("#FFD700"));
        pullButton.setPadding(48, 24, 48, 24);
        pullButton.setOnClickListener(v -> pull());
        content.addView(pullButton);

        // Payout table
        TextView payoutTitle = new TextView(context);
        payoutTitle.setText("\n--- Payout Table ---");
        payoutTitle.setTextColor(Color.parseColor("#AAAACC"));
        payoutTitle.setTextSize(14);
        payoutTitle.setGravity(Gravity.CENTER);
        content.addView(payoutTitle);

        for (int i = SYMBOLS.length - 1; i >= 0; i--) {
            TextView payLine = new TextView(context);
            CoinIconHelper.setCoinText(payLine,
                    "3x " + SYMBOLS[i] + " = \u25C8 " + TRIPLE_PAYOUTS[i], 13);
            payLine.setTextColor(SYMBOL_COLORS[i]);
            payLine.setTextSize(13);
            payLine.setGravity(Gravity.CENTER);
            content.addView(payLine);
        }
        TextView doublePay = new TextView(context);
        CoinIconHelper.setCoinText(doublePay,
                "Any 2 match = \u25C8 " + DOUBLE_PAYOUT, 13);
        doublePay.setTextColor(Color.parseColor("#888888"));
        doublePay.setTextSize(13);
        doublePay.setGravity(Gravity.CENTER);
        content.addView(doublePay);

        // History
        TextView histTitle = new TextView(context);
        histTitle.setText("\n--- History ---");
        histTitle.setTextColor(Color.parseColor("#AAAACC"));
        histTitle.setTextSize(14);
        histTitle.setGravity(Gravity.CENTER);
        content.addView(histTitle);

        historyLayout = new LinearLayout(context);
        historyLayout.setOrientation(LinearLayout.VERTICAL);
        content.addView(historyLayout);

        addView(content);
        updateBalance();
    }

    private void updateBalance() {
        SaveManager sm = SaveManager.getInstance();
        CoinIconHelper.setCoinText(balanceText,
                "Your Coins: \u25C8 " + sm.getData().coins, 16);
        pullButton.setEnabled(!spinning && sm.getData().coins >= COST_PER_PULL);
        pullButton.setBackgroundColor(
            sm.getData().coins >= COST_PER_PULL && !spinning ? Color.parseColor("#FFD700") : Color.parseColor("#444444"));
    }

    private int rollSymbol() {
        int total = 0;
        for (int w : WEIGHTS) total += w;
        int roll = rng.nextInt(total);
        int cumulative = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            cumulative += WEIGHTS[i];
            if (roll < cumulative) return i;
        }
        return 0;
    }

    private void pull() {
        SaveManager sm = SaveManager.getInstance();
        if (sm.getData().coins < COST_PER_PULL || spinning) {
            if (sm.getData().coins < COST_PER_PULL) {
                HapticManager.getInstance().errorBuzz();
            }
            return;
        }

        sm.spendCoins(COST_PER_PULL);
        spinning = true;
        pullButton.setEnabled(false);
        resultText.setText("");
        updateBalance();

        // Haptic: pull lever click
        HapticManager.getInstance().slotPull();

        // Determine results upfront
        final int[] results = {rollSymbol(), rollSymbol(), rollSymbol()};

        // Spin all 3 reels with staggered durations so they stop sequentially.
        // Reel 1 stops first (shortest spin), reel 3 stops last (longest spin).
        // Each reel scrolls through symbols and decelerates to land on its target.
        reelViews[0].spin(results[0], 1000, () -> HapticManager.getInstance().reelStop());
        reelViews[1].spin(results[1], 1600, () -> HapticManager.getInstance().reelStop());
        reelViews[2].spin(results[2], 2200, () -> {
            // Final reel stop haptic
            HapticManager.getInstance().reelStop();

            // All reels have stopped - evaluate results
            int payout = calculatePayout(results);
            if (payout > 0) sm.addCoins(payout);

            String histEntry = SYMBOLS[results[0]] + " | " + SYMBOLS[results[1]] + " | " + SYMBOLS[results[2]];
            if (payout > 0) {
                CoinIconHelper.setCoinText(resultText,
                        "WIN! \u25C8 +" + payout, 18);
                resultText.setTextColor(Color.parseColor("#44FF44"));
                histEntry += " = \u25C8 " + payout;

                // Win haptics (delayed slightly so reel-stop is felt first)
                boolean isTriple = results[0] == results[1] && results[1] == results[2];
                boolean isJackpot = isTriple && results[0] == 5; // Crown = index 5
                handler.postDelayed(() -> {
                    if (isJackpot) {
                        HapticManager.getInstance().slotJackpot();
                    } else if (isTriple) {
                        HapticManager.getInstance().slotWinTriple();
                    } else {
                        HapticManager.getInstance().slotWinDouble();
                    }
                }, 150);
            } else {
                resultText.setText("No match");
                resultText.setTextColor(Color.parseColor("#FF4444"));
                histEntry += " = 0";
            }

            sm.getData().slotMachinePulls++;
            if (payout > sm.getData().biggestJackpot) sm.getData().biggestJackpot = payout;
            sm.save();

            // Add to history
            history.add(0, histEntry);
            if (history.size() > 10) history.remove(history.size() - 1);
            refreshHistory();

            spinning = false;
            updateBalance();
            if (getContext() instanceof TabActivity) ((TabActivity) getContext()).updateCoinDisplay();
        });
    }

    private int calculatePayout(int[] results) {
        // Triple match
        if (results[0] == results[1] && results[1] == results[2]) {
            return TRIPLE_PAYOUTS[results[0]];
        }
        // Double match
        if (results[0] == results[1] || results[0] == results[2] || results[1] == results[2]) {
            return DOUBLE_PAYOUT;
        }
        return 0;
    }

    private void refreshHistory() {
        historyLayout.removeAllViews();
        for (String entry : history) {
            TextView tv = new TextView(getContext());
            CoinIconHelper.setCoinText(tv, entry, 12);
            tv.setTextColor(entry.contains("= 0") ? Color.parseColor("#666666") : Color.parseColor("#44FF44"));
            tv.setTextSize(12);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, 2, 0, 2);
            historyLayout.addView(tv);
        }
    }
}
