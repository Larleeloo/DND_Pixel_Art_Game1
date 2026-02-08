package com.ambermoon.lootgame.tabs;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import com.ambermoon.lootgame.core.TabActivity;
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
        setBackgroundColor(Color.parseColor("#1A1525"));

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

        // Reels (static item images)
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
        pullButton.setText("PULL  (\u25C8 " + COST_PER_PULL + ")");
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
            payLine.setText("3x " + SYMBOLS[i] + " = \u25C8 " + TRIPLE_PAYOUTS[i]);
            payLine.setTextColor(SYMBOL_COLORS[i]);
            payLine.setTextSize(13);
            payLine.setGravity(Gravity.CENTER);
            content.addView(payLine);
        }
        TextView doublePay = new TextView(context);
        doublePay.setText("Any 2 match = \u25C8 " + DOUBLE_PAYOUT);
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
        balanceText.setText("Your Coins: \u25C8 " + sm.getData().coins);
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
        if (sm.getData().coins < COST_PER_PULL || spinning) return;

        sm.spendCoins(COST_PER_PULL);
        spinning = true;
        pullButton.setEnabled(false);
        resultText.setText("");
        updateBalance();

        // Determine results upfront
        final int[] results = {rollSymbol(), rollSymbol(), rollSymbol()};

        // Animate: show spinning state then reveal one by one with static images
        for (int i = 0; i < 3; i++) {
            reelViews[i].setSpinning();
        }

        // Reveal reel 1 after 500ms (show static item image)
        handler.postDelayed(() -> {
            reelViews[0].setSymbol(results[0]);
        }, 500);

        // Reveal reel 2 after 1000ms
        handler.postDelayed(() -> {
            reelViews[1].setSymbol(results[1]);
        }, 1000);

        // Reveal reel 3 after 1500ms, then evaluate
        handler.postDelayed(() -> {
            reelViews[2].setSymbol(results[2]);

            // Evaluate
            int payout = calculatePayout(results);
            if (payout > 0) sm.addCoins(payout);

            String histEntry = SYMBOLS[results[0]] + " | " + SYMBOLS[results[1]] + " | " + SYMBOLS[results[2]];
            if (payout > 0) {
                resultText.setText("WIN! \u25C8 +" + payout);
                resultText.setTextColor(Color.parseColor("#44FF44"));
                histEntry += " = \u25C8 " + payout;
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
        }, 1500);
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
            tv.setText(entry);
            tv.setTextColor(entry.contains("= 0") ? Color.parseColor("#666666") : Color.parseColor("#44FF44"));
            tv.setTextSize(12);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, 2, 0, 2);
            historyLayout.addView(tv);
        }
    }
}
