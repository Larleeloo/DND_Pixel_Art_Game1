package com.ambermoon.lootgame.tabs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import com.ambermoon.lootgame.core.TabActivity;
import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ItemRegistry;
import com.ambermoon.lootgame.graphics.AssetLoader;
import com.ambermoon.lootgame.save.SaveData;
import com.ambermoon.lootgame.save.SaveManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShopTab extends ScrollView {
    private LinearLayout itemGrid;
    private TextView balanceText;
    private List<SaveData.ShopItem> shopItems;

    public ShopTab(Context context) {
        super(context);
        setBackgroundColor(Color.parseColor("#1A1525"));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24, 16, 24, 16);

        // Header
        TextView title = new TextView(context);
        title.setText("SHOP");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("Spend your coins on items");
        subtitle.setTextColor(Color.parseColor("#AAAACC"));
        subtitle.setTextSize(13);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 8);
        content.addView(subtitle);

        // Balance display
        balanceText = new TextView(context);
        balanceText.setTextColor(Color.parseColor("#FFD700"));
        balanceText.setTextSize(16);
        balanceText.setGravity(Gravity.CENTER);
        balanceText.setPadding(0, 8, 0, 16);
        content.addView(balanceText);

        // Item grid
        itemGrid = new LinearLayout(context);
        itemGrid.setOrientation(LinearLayout.VERTICAL);
        content.addView(itemGrid);

        addView(content);
        refreshShop();
    }

    private void refreshShop() {
        updateBalance();
        itemGrid.removeAllViews();

        shopItems = SaveManager.getInstance().loadShopItems();

        if (shopItems.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("The shop is empty.\nCheck back later!");
            empty.setTextColor(Color.parseColor("#888888"));
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 48, 0, 0);
            itemGrid.addView(empty);
            return;
        }

        // Sort by price ascending
        List<SaveData.ShopItem> sorted = new ArrayList<>(shopItems);
        Collections.sort(sorted, (a, b) -> a.price - b.price);

        for (SaveData.ShopItem si : sorted) {
            Item template = ItemRegistry.getTemplate(si.itemId);
            if (template == null) continue;

            addShopRow(si, template);
        }
    }

    private void addShopRow(SaveData.ShopItem shopItem, Item template) {
        Context ctx = getContext();
        int rarityColor = Item.getRarityColor(template.getRarity().ordinal());

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundColor(Color.parseColor("#28233A"));
        row.setPadding(16, 12, 16, 12);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 4, 0, 4);
        row.setLayoutParams(rowParams);

        // Item icon
        View iconView = new ItemIconView(ctx, template);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(64, 64);
        iconView.setLayoutParams(iconParams);
        row.addView(iconView);

        // Item name + rarity
        LinearLayout nameCol = new LinearLayout(ctx);
        nameCol.setOrientation(LinearLayout.VERTICAL);
        nameCol.setPadding(16, 0, 8, 0);
        LinearLayout.LayoutParams nameColParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        nameCol.setLayoutParams(nameColParams);

        TextView nameText = new TextView(ctx);
        nameText.setText(template.getName());
        nameText.setTextColor(rarityColor);
        nameText.setTextSize(14);
        nameText.setTypeface(Typeface.DEFAULT_BOLD);
        nameCol.addView(nameText);

        TextView rarityText = new TextView(ctx);
        rarityText.setText(template.getRarity().getDisplayName() + " " + template.getCategory().name());
        rarityText.setTextColor(Color.parseColor("#888899"));
        rarityText.setTextSize(10);
        nameCol.addView(rarityText);

        row.addView(nameCol);

        // Price + buy button
        LinearLayout buyCol = new LinearLayout(ctx);
        buyCol.setOrientation(LinearLayout.VERTICAL);
        buyCol.setGravity(Gravity.CENTER);

        TextView priceText = new TextView(ctx);
        priceText.setText("\u25C8 " + shopItem.price);
        priceText.setTextColor(Color.parseColor("#FFD700"));
        priceText.setTextSize(14);
        priceText.setGravity(Gravity.CENTER);
        buyCol.addView(priceText);

        Button buyBtn = new Button(ctx);
        buyBtn.setText("BUY");
        buyBtn.setTextColor(Color.WHITE);
        buyBtn.setTextSize(11);
        buyBtn.setTypeface(Typeface.DEFAULT_BOLD);
        boolean canAfford = SaveManager.getInstance().getData().coins >= shopItem.price;
        buyBtn.setBackgroundColor(canAfford ? Color.parseColor("#2E8B57") : Color.parseColor("#444444"));
        buyBtn.setPadding(24, 4, 24, 4);
        buyBtn.setOnClickListener(v -> showBuyConfirmation(shopItem, template));
        buyCol.addView(buyBtn);

        row.addView(buyCol);

        // Tap row to see item detail
        row.setOnClickListener(v -> showItemDetail(shopItem, template));

        itemGrid.addView(row);
    }

    private void showItemDetail(SaveData.ShopItem shopItem, Item template) {
        Context ctx = getContext();
        int rarityColor = Item.getRarityColor(template.getRarity().ordinal());

        LinearLayout dialogContent = new LinearLayout(ctx);
        dialogContent.setOrientation(LinearLayout.VERTICAL);
        dialogContent.setGravity(Gravity.CENTER_HORIZONTAL);
        dialogContent.setBackgroundColor(Color.parseColor("#1E1830"));
        dialogContent.setPadding(48, 32, 48, 32);

        // Item name
        TextView nameView = new TextView(ctx);
        nameView.setText(template.getName());
        nameView.setTextColor(rarityColor);
        nameView.setTextSize(18);
        nameView.setTypeface(Typeface.DEFAULT_BOLD);
        nameView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        nameParams.bottomMargin = 16;
        nameView.setLayoutParams(nameParams);
        dialogContent.addView(nameView);

        // Animated item preview
        AnimatedItemView animatedView = new AnimatedItemView(ctx, rarityColor);
        LinearLayout.LayoutParams animParams = new LinearLayout.LayoutParams(160, 160);
        animParams.gravity = Gravity.CENTER;
        animParams.bottomMargin = 12;
        animatedView.setLayoutParams(animParams);
        dialogContent.addView(animatedView);

        // Start with idle animation
        String folderPath = template.getAnimationFolderPath();
        if (folderPath != null) {
            String idlePath = folderPath + "/idle.gif";
            if (AssetLoader.exists(idlePath)) {
                animatedView.playAnimation(idlePath);
            } else if (template.getTexturePath() != null) {
                animatedView.playAnimation(template.getTexturePath());
            }
        } else if (template.getTexturePath() != null) {
            animatedView.playAnimation(template.getTexturePath());
        }

        // Tooltip info
        TextView tooltipText = new TextView(ctx);
        tooltipText.setTextColor(Color.parseColor("#CCCCCC"));
        tooltipText.setTextSize(13);
        tooltipText.setGravity(Gravity.CENTER);
        tooltipText.setText(template.getTooltip());
        LinearLayout.LayoutParams tipParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tipParams.topMargin = 8;
        tooltipText.setLayoutParams(tipParams);
        dialogContent.addView(tooltipText);

        // Price
        TextView priceView = new TextView(ctx);
        priceView.setText("\nPrice: \u25C8 " + shopItem.price + " coins");
        priceView.setTextColor(Color.parseColor("#FFD700"));
        priceView.setTextSize(16);
        priceView.setGravity(Gravity.CENTER);
        priceView.setTypeface(Typeface.DEFAULT_BOLD);
        dialogContent.addView(priceView);

        // Currently owned count
        int owned = SaveManager.getInstance().getVaultItemCount(shopItem.itemId);
        if (owned > 0) {
            TextView ownedView = new TextView(ctx);
            ownedView.setText("You own: x" + owned);
            ownedView.setTextColor(Color.parseColor("#AAAACC"));
            ownedView.setTextSize(12);
            ownedView.setGravity(Gravity.CENTER);
            dialogContent.addView(ownedView);
        }

        ScrollView dialogScroll = new ScrollView(ctx);
        dialogScroll.addView(dialogContent);

        boolean canAfford = SaveManager.getInstance().getData().coins >= shopItem.price;

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setView(dialogScroll);
        if (canAfford) {
            builder.setPositiveButton("Buy (\u25C8 " + shopItem.price + ")", (dialog, which) -> {
                purchaseItem(shopItem, template);
            });
        }
        builder.setNegativeButton("Close", null);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1E1830")));
        }
        dialog.show();
    }

    private void showBuyConfirmation(SaveData.ShopItem shopItem, Item template) {
        Context ctx = getContext();
        int coins = SaveManager.getInstance().getData().coins;

        if (coins < shopItem.price) {
            Toast.makeText(ctx, "Not enough coins! Need \u25C8 " + shopItem.price, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(ctx)
                .setTitle("Buy " + template.getName() + "?")
                .setMessage("Cost: \u25C8 " + shopItem.price + " coins\nYour balance: \u25C8 " + coins + " coins\n\nAfter purchase: \u25C8 " + (coins - shopItem.price) + " coins")
                .setPositiveButton("Buy", (dialog, which) -> purchaseItem(shopItem, template))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void purchaseItem(SaveData.ShopItem shopItem, Item template) {
        SaveManager sm = SaveManager.getInstance();
        if (!sm.spendCoins(shopItem.price)) {
            Toast.makeText(getContext(), "Not enough coins!", Toast.LENGTH_SHORT).show();
            return;
        }

        sm.addVaultItem(shopItem.itemId, 1);
        sm.getData().totalItemsCollected++;
        sm.save();

        Toast.makeText(getContext(), "Purchased " + template.getName() + "!", Toast.LENGTH_SHORT).show();

        if (getContext() instanceof TabActivity) {
            ((TabActivity) getContext()).updateCoinDisplay();
        }
        refreshShop();
    }

    private void updateBalance() {
        SaveManager sm = SaveManager.getInstance();
        balanceText.setText("Your Coins: \u25C8 " + sm.getData().coins);
    }
}
