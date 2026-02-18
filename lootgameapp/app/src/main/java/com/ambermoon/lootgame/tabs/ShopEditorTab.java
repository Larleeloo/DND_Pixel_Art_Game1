package com.ambermoon.lootgame.tabs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ItemRegistry;
import com.ambermoon.lootgame.graphics.CoinIconHelper;
import com.ambermoon.lootgame.save.SaveData;
import com.ambermoon.lootgame.save.SaveManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopEditorTab extends ScrollView implements TextWatcher {
    private LinearLayout itemGrid;
    private EditText searchBox;
    private Map<String, Integer> shopPrices; // itemId -> price (for items in shop)
    private int currentSort = 0; // 0=rarity, 1=name, 2=in-shop

    public ShopEditorTab(Context context) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);

        // Load current shop prices into map
        loadShopPrices();

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24, 16, 24, 16);

        // Header
        TextView title = new TextView(context);
        title.setText("SHOP EDITOR");
        title.setTextColor(Color.parseColor("#FF6B35"));
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("Tap any item to add/edit its shop price");
        subtitle.setTextColor(Color.parseColor("#AAAACC"));
        subtitle.setTextSize(12);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 8);
        content.addView(subtitle);

        // Item count in shop
        TextView countText = new TextView(context);
        countText.setText(shopPrices.size() + " items in shop");
        countText.setTextColor(Color.parseColor("#FFD700"));
        countText.setTextSize(13);
        countText.setGravity(Gravity.CENTER);
        countText.setPadding(0, 0, 0, 8);
        content.addView(countText);

        // Search bar
        searchBox = new EditText(context);
        searchBox.setHint("Search items...");
        searchBox.setTextColor(Color.WHITE);
        searchBox.setHintTextColor(Color.parseColor("#666688"));
        searchBox.setBackgroundColor(Color.parseColor("#28233A"));
        searchBox.setPadding(24, 16, 24, 16);
        searchBox.setSingleLine(true);
        searchBox.addTextChangedListener(this);
        content.addView(searchBox);

        // Sort buttons
        LinearLayout sortRow = new LinearLayout(context);
        sortRow.setOrientation(LinearLayout.HORIZONTAL);
        sortRow.setPadding(0, 8, 0, 8);
        String[] sorts = {"By Rarity", "By Name", "In Shop"};
        for (int i = 0; i < sorts.length; i++) {
            final int si = i;
            Button btn = new Button(context);
            btn.setText(sorts[i]);
            btn.setTextColor(Color.parseColor("#AAAACC"));
            btn.setTextSize(11);
            btn.setBackgroundColor(Color.TRANSPARENT);
            btn.setOnClickListener(v -> { currentSort = si; refreshGrid(); });
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            btn.setLayoutParams(bp);
            sortRow.addView(btn);
        }
        content.addView(sortRow);

        // Item grid
        itemGrid = new LinearLayout(context);
        itemGrid.setOrientation(LinearLayout.VERTICAL);
        content.addView(itemGrid);

        addView(content);
        refreshGrid();
    }

    private void loadShopPrices() {
        shopPrices = new HashMap<>();
        List<SaveData.ShopItem> items = SaveManager.getInstance().loadShopItems();
        for (SaveData.ShopItem si : items) {
            shopPrices.put(si.itemId, si.price);
        }
    }

    private void saveShopPrices() {
        List<SaveData.ShopItem> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : shopPrices.entrySet()) {
            items.add(new SaveData.ShopItem(entry.getKey(), entry.getValue()));
        }
        SaveManager.getInstance().saveShopItems(items);
    }

    private void refreshGrid() {
        itemGrid.removeAllViews();
        String search = searchBox.getText().toString().toLowerCase().trim();

        // Build list of all items
        List<String> allIds = new ArrayList<>(ItemRegistry.getAllItemIds());

        // Filter by search
        List<String> filtered = new ArrayList<>();
        for (String id : allIds) {
            Item template = ItemRegistry.getTemplate(id);
            if (template == null) continue;
            String name = template.getName().toLowerCase();
            if (!search.isEmpty() && !name.contains(search) && !id.contains(search)) continue;
            filtered.add(id);
        }

        // Sort
        Collections.sort(filtered, (a, b) -> {
            Item ta = ItemRegistry.getTemplate(a);
            Item tb = ItemRegistry.getTemplate(b);
            switch (currentSort) {
                case 0: // rarity (highest first)
                    int ra = ta != null ? ta.getRarity().ordinal() : 0;
                    int rb = tb != null ? tb.getRarity().ordinal() : 0;
                    return rb - ra;
                case 1: // name
                    String na = ta != null ? ta.getName() : a;
                    String nb = tb != null ? tb.getName() : b;
                    return na.compareToIgnoreCase(nb);
                case 2: // in-shop first, then by name
                    boolean inA = shopPrices.containsKey(a);
                    boolean inB = shopPrices.containsKey(b);
                    if (inA != inB) return inA ? -1 : 1;
                    String sna = ta != null ? ta.getName() : a;
                    String snb = tb != null ? tb.getName() : b;
                    return sna.compareToIgnoreCase(snb);
                default: return 0;
            }
        });

        for (String id : filtered) {
            Item template = ItemRegistry.getTemplate(id);
            if (template == null) continue;
            addItemRow(id, template);
        }

        if (filtered.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No items match your search.");
            empty.setTextColor(Color.parseColor("#888888"));
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 32, 0, 0);
            itemGrid.addView(empty);
        }
    }

    private void addItemRow(String itemId, Item template) {
        Context ctx = getContext();
        int rarityColor = Item.getRarityColor(template.getRarity().ordinal());
        boolean inShop = shopPrices.containsKey(itemId);

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundColor(inShop ? Color.parseColor("#1E3328") : Color.parseColor("#28233A"));
        row.setPadding(12, 8, 12, 8);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 2, 0, 2);
        row.setLayoutParams(rowParams);

        // Item icon
        View iconView = new ItemIconView(ctx, template);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(52, 52);
        iconView.setLayoutParams(iconParams);
        row.addView(iconView);

        // Item name + category
        LinearLayout nameCol = new LinearLayout(ctx);
        nameCol.setOrientation(LinearLayout.VERTICAL);
        nameCol.setPadding(12, 0, 4, 0);
        LinearLayout.LayoutParams nameColParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        nameCol.setLayoutParams(nameColParams);

        TextView nameText = new TextView(ctx);
        nameText.setText(template.getName());
        nameText.setTextColor(rarityColor);
        nameText.setTextSize(13);
        nameText.setTypeface(Typeface.DEFAULT_BOLD);
        nameCol.addView(nameText);

        TextView catText = new TextView(ctx);
        catText.setText(template.getRarity().getDisplayName() + " " + template.getCategory().name());
        catText.setTextColor(Color.parseColor("#777788"));
        catText.setTextSize(9);
        nameCol.addView(catText);

        row.addView(nameCol);

        // Price / status badge
        if (inShop) {
            TextView priceTag = new TextView(ctx);
            priceTag.setText(CoinIconHelper.withCoin(ctx,
                    "\u25C8 " + shopPrices.get(itemId), 13));
            priceTag.setTextColor(Color.parseColor("#FFD700"));
            priceTag.setTextSize(13);
            priceTag.setTypeface(Typeface.DEFAULT_BOLD);
            priceTag.setGravity(Gravity.CENTER);
            priceTag.setPadding(8, 0, 8, 0);
            row.addView(priceTag);
        } else {
            TextView noShop = new TextView(ctx);
            noShop.setText("--");
            noShop.setTextColor(Color.parseColor("#555555"));
            noShop.setTextSize(12);
            noShop.setGravity(Gravity.CENTER);
            noShop.setPadding(8, 0, 8, 0);
            row.addView(noShop);
        }

        row.setOnClickListener(v -> showPriceDialog(itemId, template));
        itemGrid.addView(row);
    }

    private void showPriceDialog(String itemId, Item template) {
        Context ctx = getContext();
        int rarityColor = Item.getRarityColor(template.getRarity().ordinal());
        boolean inShop = shopPrices.containsKey(itemId);

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
        dialogContent.addView(nameView);

        // Rarity + category
        TextView rarityView = new TextView(ctx);
        rarityView.setText(template.getRarity().getDisplayName() + " " + template.getCategory().name());
        rarityView.setTextColor(Color.parseColor("#AAAACC"));
        rarityView.setTextSize(12);
        rarityView.setGravity(Gravity.CENTER);
        rarityView.setPadding(0, 4, 0, 16);
        dialogContent.addView(rarityView);

        // Item icon
        View iconView = new ItemIconView(ctx, template);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(100, 100);
        iconParams.gravity = Gravity.CENTER;
        iconParams.bottomMargin = 16;
        iconView.setLayoutParams(iconParams);
        dialogContent.addView(iconView);

        // Current price label
        if (inShop) {
            TextView currentLabel = new TextView(ctx);
            currentLabel.setText(CoinIconHelper.withCoin(ctx,
                    "Current price: \u25C8 " + shopPrices.get(itemId), 14));
            currentLabel.setTextColor(Color.parseColor("#FFD700"));
            currentLabel.setTextSize(14);
            currentLabel.setGravity(Gravity.CENTER);
            currentLabel.setPadding(0, 0, 0, 8);
            dialogContent.addView(currentLabel);
        }

        // Price input
        TextView inputLabel = new TextView(ctx);
        inputLabel.setText(inShop ? "Set new price:" : "Set price to add to shop:");
        inputLabel.setTextColor(Color.parseColor("#CCCCCC"));
        inputLabel.setTextSize(13);
        inputLabel.setGravity(Gravity.CENTER);
        dialogContent.addView(inputLabel);

        EditText priceInput = new EditText(ctx);
        priceInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        priceInput.setTextColor(Color.WHITE);
        priceInput.setBackgroundColor(Color.parseColor("#28233A"));
        priceInput.setPadding(24, 16, 24, 16);
        priceInput.setGravity(Gravity.CENTER);
        priceInput.setHint("Enter price in coins");
        priceInput.setHintTextColor(Color.parseColor("#666688"));
        if (inShop) {
            priceInput.setText(String.valueOf(shopPrices.get(itemId)));
        }
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        inputParams.setMargins(0, 8, 0, 0);
        priceInput.setLayoutParams(inputParams);
        dialogContent.addView(priceInput);

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setView(dialogContent);

        builder.setPositiveButton(inShop ? "Update Price" : "Add to Shop", (dialog, which) -> {
            String priceStr = priceInput.getText().toString().trim();
            if (priceStr.isEmpty()) return;
            try {
                int price = Integer.parseInt(priceStr);
                if (price <= 0) {
                    Toast.makeText(ctx, "Price must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                shopPrices.put(itemId, price);
                saveShopPrices();
                refreshGrid();
                Toast.makeText(ctx, CoinIconHelper.withCoin(ctx,
                        template.getName() + " added to shop for \u25C8 " + price, 14), Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(ctx, "Invalid price", Toast.LENGTH_SHORT).show();
            }
        });

        if (inShop) {
            builder.setNeutralButton("Remove", (dialog, which) -> {
                shopPrices.remove(itemId);
                saveShopPrices();
                refreshGrid();
                Toast.makeText(ctx, template.getName() + " removed from shop", Toast.LENGTH_SHORT).show();
            });
        }

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1E1830")));
        }
        dialog.show();
    }

    // TextWatcher
    @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
    @Override public void onTextChanged(CharSequence s, int st, int b, int c) { refreshGrid(); }
    @Override public void afterTextChanged(Editable s) {}
}
