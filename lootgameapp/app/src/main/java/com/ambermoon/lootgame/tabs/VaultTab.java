package com.ambermoon.lootgame.tabs;

import android.content.Context;
import android.graphics.*;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;

import com.ambermoon.lootgame.core.TabActivity;
import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ItemRegistry;
import com.ambermoon.lootgame.graphics.AssetLoader;
import com.ambermoon.lootgame.save.SaveData;
import com.ambermoon.lootgame.save.SaveManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VaultTab extends ScrollView implements TextWatcher {
    private LinearLayout rootContent;
    private LinearLayout itemGrid;
    private EditText searchBox;
    private String currentFilter = "All";
    private int currentSort = 0; // 0=rarity, 1=name, 2=count

    // Sub-tab state: 0 = Items, 1 = Trading
    private int currentSubTab = 0;
    private TextView itemsTabBtn;
    private TextView tradingTabBtn;

    // Trading sub-tab views
    private LinearLayout tradingContent;
    private LinearLayout itemsContent;

    public VaultTab(Context context) {
        super(context);
        setBackgroundColor(Color.parseColor("#1A1525"));

        rootContent = new LinearLayout(context);
        rootContent.setOrientation(LinearLayout.VERTICAL);
        rootContent.setPadding(24, 16, 24, 16);

        // Header
        TextView title = new TextView(context);
        title.setText("YOUR VAULT");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        rootContent.addView(title);

        SaveManager sm = SaveManager.getInstance();
        int totalStacks = sm.getData().vaultItems.size();
        int totalItems = 0;
        for (SaveData.VaultItem vi : sm.getData().vaultItems) totalItems += vi.stackCount;

        TextView subtitle = new TextView(context);
        subtitle.setText(totalItems + " items in " + totalStacks + " stacks");
        subtitle.setTextColor(Color.parseColor("#AAAACC"));
        subtitle.setTextSize(13);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 12);
        rootContent.addView(subtitle);

        // Sub-tab toggle: Items | Trading
        LinearLayout subTabRow = new LinearLayout(context);
        subTabRow.setOrientation(LinearLayout.HORIZONTAL);
        subTabRow.setGravity(Gravity.CENTER);
        subTabRow.setPadding(0, 0, 0, 12);

        itemsTabBtn = new TextView(context);
        itemsTabBtn.setText("ITEMS");
        itemsTabBtn.setTextSize(14);
        itemsTabBtn.setTypeface(Typeface.DEFAULT_BOLD);
        itemsTabBtn.setGravity(Gravity.CENTER);
        itemsTabBtn.setPadding(32, 12, 32, 12);
        itemsTabBtn.setOnClickListener(v -> switchSubTab(0));
        LinearLayout.LayoutParams tabBtnParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        tabBtnParams.setMargins(4, 0, 4, 0);
        itemsTabBtn.setLayoutParams(tabBtnParams);
        subTabRow.addView(itemsTabBtn);

        tradingTabBtn = new TextView(context);
        tradingTabBtn.setText("TRADING");
        tradingTabBtn.setTextSize(14);
        tradingTabBtn.setTypeface(Typeface.DEFAULT_BOLD);
        tradingTabBtn.setGravity(Gravity.CENTER);
        tradingTabBtn.setPadding(32, 12, 32, 12);
        tradingTabBtn.setOnClickListener(v -> switchSubTab(1));
        tradingTabBtn.setLayoutParams(tabBtnParams);
        subTabRow.addView(tradingTabBtn);

        rootContent.addView(subTabRow);

        // Items sub-tab content container
        itemsContent = new LinearLayout(context);
        itemsContent.setOrientation(LinearLayout.VERTICAL);
        buildItemsContent(context);
        rootContent.addView(itemsContent);

        // Trading sub-tab content container (hidden initially)
        tradingContent = new LinearLayout(context);
        tradingContent.setOrientation(LinearLayout.VERTICAL);
        tradingContent.setVisibility(View.GONE);
        rootContent.addView(tradingContent);

        addView(rootContent);
        updateSubTabStyles();
        refreshGrid();
    }

    private void buildItemsContent(Context context) {
        // Search bar
        searchBox = new EditText(context);
        searchBox.setHint("Search items...");
        searchBox.setTextColor(Color.WHITE);
        searchBox.setHintTextColor(Color.parseColor("#666688"));
        searchBox.setBackgroundColor(Color.parseColor("#28233A"));
        searchBox.setPadding(24, 16, 24, 16);
        searchBox.setSingleLine(true);
        searchBox.addTextChangedListener(this);
        itemsContent.addView(searchBox);

        // Sort buttons
        LinearLayout sortRow = new LinearLayout(context);
        sortRow.setOrientation(LinearLayout.HORIZONTAL);
        sortRow.setPadding(0, 8, 0, 8);
        String[] sorts = {"By Rarity", "By Name", "By Count"};
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
        itemsContent.addView(sortRow);

        // Item grid
        itemGrid = new LinearLayout(context);
        itemGrid.setOrientation(LinearLayout.VERTICAL);
        itemsContent.addView(itemGrid);
    }

    private void switchSubTab(int tab) {
        currentSubTab = tab;
        updateSubTabStyles();
        if (tab == 0) {
            itemsContent.setVisibility(View.VISIBLE);
            tradingContent.setVisibility(View.GONE);
            refreshGrid();
        } else {
            itemsContent.setVisibility(View.GONE);
            tradingContent.setVisibility(View.VISIBLE);
            refreshTrading();
        }
    }

    private void updateSubTabStyles() {
        if (currentSubTab == 0) {
            itemsTabBtn.setTextColor(Color.parseColor("#FFD700"));
            itemsTabBtn.setBackgroundColor(Color.parseColor("#28233A"));
            tradingTabBtn.setTextColor(Color.parseColor("#888888"));
            tradingTabBtn.setBackgroundColor(Color.TRANSPARENT);
        } else {
            itemsTabBtn.setTextColor(Color.parseColor("#888888"));
            itemsTabBtn.setBackgroundColor(Color.TRANSPARENT);
            tradingTabBtn.setTextColor(Color.parseColor("#FFD700"));
            tradingTabBtn.setBackgroundColor(Color.parseColor("#28233A"));
        }
    }

    // ==================== ITEMS SUB-TAB ====================

    private void refreshGrid() {
        itemGrid.removeAllViews();
        SaveManager sm = SaveManager.getInstance();
        String search = searchBox.getText().toString().toLowerCase().trim();

        // Build filtered/sorted list
        List<SaveData.VaultItem> items = new ArrayList<>();
        for (SaveData.VaultItem vi : sm.getData().vaultItems) {
            Item template = ItemRegistry.getTemplate(vi.itemId);
            String name = template != null ? template.getName() : vi.itemId;
            if (!search.isEmpty() && !name.toLowerCase().contains(search)) continue;
            items.add(vi);
        }

        // Sort
        Collections.sort(items, (a, b) -> {
            Item ta = ItemRegistry.getTemplate(a.itemId);
            Item tb = ItemRegistry.getTemplate(b.itemId);
            switch (currentSort) {
                case 0: // rarity (highest first)
                    int ra = ta != null ? ta.getRarity().ordinal() : 0;
                    int rb = tb != null ? tb.getRarity().ordinal() : 0;
                    return rb - ra;
                case 1: // name
                    String na = ta != null ? ta.getName() : a.itemId;
                    String nb = tb != null ? tb.getName() : b.itemId;
                    return na.compareToIgnoreCase(nb);
                case 2: // count (highest first)
                    return b.stackCount - a.stackCount;
                default: return 0;
            }
        });

        LinearLayout currentRow = null;
        int col = 0;
        for (SaveData.VaultItem vi : items) {
            if (col % 4 == 0) {
                currentRow = new LinearLayout(getContext());
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setPadding(0, 4, 0, 4);
                itemGrid.addView(currentRow);
                col = 0;
            }

            Item template = ItemRegistry.getTemplate(vi.itemId);

            // Item cell: icon sprite + name + count
            LinearLayout cell = new LinearLayout(getContext());
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            cell.setBackgroundColor(Color.parseColor("#28233A"));
            cell.setPadding(4, 4, 4, 4);
            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            cellParams.setMargins(2, 0, 2, 0);
            cell.setLayoutParams(cellParams);

            // Item icon sprite (first frame of idle GIF, or rarity circle fallback)
            if (template != null) {
                View iconView = new ItemIconView(getContext(), template);
                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(80, 80);
                iconParams.gravity = Gravity.CENTER;
                iconView.setLayoutParams(iconParams);
                cell.addView(iconView);
            }

            // Stack count below icon
            TextView countView = new TextView(getContext());
            countView.setText("x" + vi.stackCount);
            countView.setTextColor(template != null ? Item.getRarityColor(template.getRarity().ordinal()) : Color.WHITE);
            countView.setTextSize(9);
            countView.setGravity(Gravity.CENTER);
            countView.setPadding(0, 2, 0, 0);
            cell.addView(countView);

            final String id = vi.itemId;
            cell.setOnClickListener(v -> showDetail(id));
            currentRow.addView(cell);
            col++;
        }
        if (currentRow != null) {
            while (col % 4 != 0) {
                View spacer = new View(getContext());
                spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1.0f));
                currentRow.addView(spacer);
                col++;
            }
        }

        if (items.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText(search.isEmpty() ? "Your vault is empty.\nOpen chests to collect items!" : "No items match your search.");
            empty.setTextColor(Color.parseColor("#888888"));
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 32, 0, 0);
            itemGrid.addView(empty);
        }
    }

    // TextWatcher implementation (on VaultTab directly, not an inner class)
    @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
    @Override public void onTextChanged(CharSequence s, int st, int b, int c) { refreshGrid(); }
    @Override public void afterTextChanged(Editable s) {}

    private void showDetail(String itemId) {
        Item template = ItemRegistry.getTemplate(itemId);
        if (template == null) return;

        Context ctx = getContext();
        int rarityColor = Item.getRarityColor(template.getRarity().ordinal());

        // Build dialog content
        LinearLayout dialogContent = new LinearLayout(ctx);
        dialogContent.setOrientation(LinearLayout.VERTICAL);
        dialogContent.setGravity(Gravity.CENTER_HORIZONTAL);
        dialogContent.setBackgroundColor(Color.parseColor("#1E1830"));
        dialogContent.setPadding(48, 32, 48, 32);

        // Item name header
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

        // Animation state selector buttons (only if folder-based animations exist)
        if (folderPath != null) {
            String[] animStates = AssetLoader.list(folderPath);
            if (animStates != null && animStates.length > 0) {
                HorizontalScrollView animScroll = new HorizontalScrollView(ctx);
                animScroll.setHorizontalScrollBarEnabled(false);
                animScroll.setFillViewport(true);

                LinearLayout animButtonRow = new LinearLayout(ctx);
                animButtonRow.setOrientation(LinearLayout.HORIZONTAL);
                animButtonRow.setGravity(Gravity.CENTER);
                animButtonRow.setPadding(0, 4, 0, 8);

                for (String fileName : animStates) {
                    if (!fileName.endsWith(".gif")) continue;
                    String label = fileName.replace(".gif", "");
                    String animPath = folderPath + "/" + fileName;

                    TextView animBtn = new TextView(ctx);
                    animBtn.setText(label);
                    animBtn.setTextColor(Color.parseColor("#AAAACC"));
                    animBtn.setTextSize(10);
                    animBtn.setBackgroundColor(Color.parseColor("#28233A"));
                    animBtn.setPadding(16, 8, 16, 8);
                    animBtn.setGravity(Gravity.CENTER);
                    animBtn.setMinWidth(0);
                    animBtn.setMinimumWidth(0);
                    animBtn.setOnClickListener(v -> animatedView.playAnimation(animPath));
                    LinearLayout.LayoutParams btnP = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    btnP.setMargins(4, 0, 4, 0);
                    animBtn.setLayoutParams(btnP);
                    animButtonRow.addView(animBtn);
                }
                animScroll.addView(animButtonRow);
                dialogContent.addView(animScroll);
            }
        }

        // Tooltip text info
        TextView tooltipText = new TextView(ctx);
        tooltipText.setTextColor(Color.parseColor("#CCCCCC"));
        tooltipText.setTextSize(13);
        tooltipText.setGravity(Gravity.CENTER);
        tooltipText.setText(template.getTooltip() +
                "\nVault Count: " + SaveManager.getInstance().getVaultItemCount(itemId));
        LinearLayout.LayoutParams tipParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tipParams.topMargin = 8;
        tooltipText.setLayoutParams(tipParams);
        dialogContent.addView(tooltipText);

        // Wrap in ScrollView so content is accessible on small screens
        ScrollView dialogScroll = new ScrollView(ctx);
        dialogScroll.addView(dialogContent);

        // Show as popup dialog
        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setView(dialogScroll)
                .setPositiveButton("Close", null)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1E1830")));
        }
        dialog.show();
    }

    // ==================== TRADING SUB-TAB ====================

    private void refreshTrading() {
        tradingContent.removeAllViews();
        Context ctx = getContext();
        SaveManager sm = SaveManager.getInstance();

        // Weekly sell limit indicator
        int sellCount = sm.getWeeklySellCount();
        int maxSells = sm.getMaxSellsPerWeek();

        LinearLayout limitRow = new LinearLayout(ctx);
        limitRow.setOrientation(LinearLayout.HORIZONTAL);
        limitRow.setGravity(Gravity.CENTER);
        limitRow.setBackgroundColor(Color.parseColor("#28233A"));
        limitRow.setPadding(16, 12, 16, 12);
        LinearLayout.LayoutParams limitParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        limitParams.setMargins(0, 0, 0, 12);
        limitRow.setLayoutParams(limitParams);

        TextView limitLabel = new TextView(ctx);
        limitLabel.setText("Weekly Sells: ");
        limitLabel.setTextColor(Color.parseColor("#AAAACC"));
        limitLabel.setTextSize(14);
        limitRow.addView(limitLabel);

        TextView limitValue = new TextView(ctx);
        limitValue.setText(sellCount + " / " + maxSells);
        limitValue.setTextColor(sellCount >= maxSells ? Color.parseColor("#FF4444") : Color.parseColor("#44FF44"));
        limitValue.setTextSize(14);
        limitValue.setTypeface(Typeface.DEFAULT_BOLD);
        limitRow.addView(limitValue);

        tradingContent.addView(limitRow);

        if (!sm.canSellThisWeek()) {
            TextView limitMsg = new TextView(ctx);
            limitMsg.setText("You've reached your weekly sell limit.\nCome back next week!");
            limitMsg.setTextColor(Color.parseColor("#FF8888"));
            limitMsg.setTextSize(12);
            limitMsg.setGravity(Gravity.CENTER);
            limitMsg.setPadding(0, 0, 0, 12);
            tradingContent.addView(limitMsg);
        }

        // --- Section: Your Active Listings ---
        List<SaveData.PlayerListing> myListings = sm.getData().playerListings;
        if (!myListings.isEmpty()) {
            TextView listingsHeader = new TextView(ctx);
            listingsHeader.setText("YOUR ACTIVE LISTINGS");
            listingsHeader.setTextColor(Color.parseColor("#FFD700"));
            listingsHeader.setTextSize(15);
            listingsHeader.setTypeface(Typeface.DEFAULT_BOLD);
            listingsHeader.setGravity(Gravity.CENTER);
            listingsHeader.setPadding(0, 8, 0, 8);
            tradingContent.addView(listingsHeader);

            for (SaveData.PlayerListing pl : myListings) {
                addListingRow(pl);
            }

            // Spacer
            View spacer = new View(ctx);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 24));
            tradingContent.addView(spacer);
        }

        // --- Section: Sell from Vault ---
        TextView sellHeader = new TextView(ctx);
        sellHeader.setText("SELL FROM VAULT");
        sellHeader.setTextColor(Color.WHITE);
        sellHeader.setTextSize(15);
        sellHeader.setTypeface(Typeface.DEFAULT_BOLD);
        sellHeader.setGravity(Gravity.CENTER);
        sellHeader.setPadding(0, 8, 0, 8);
        tradingContent.addView(sellHeader);

        TextView sellSubtitle = new TextView(ctx);
        sellSubtitle.setText("Tap an item to list it for sale in the shop");
        sellSubtitle.setTextColor(Color.parseColor("#AAAACC"));
        sellSubtitle.setTextSize(11);
        sellSubtitle.setGravity(Gravity.CENTER);
        sellSubtitle.setPadding(0, 0, 0, 8);
        tradingContent.addView(sellSubtitle);

        // Show vault items in a grid (similar to items tab but with sell action)
        List<SaveData.VaultItem> vaultItems = sm.getData().vaultItems;
        if (vaultItems.isEmpty()) {
            TextView empty = new TextView(ctx);
            empty.setText("No items in vault to sell.");
            empty.setTextColor(Color.parseColor("#888888"));
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 24, 0, 0);
            tradingContent.addView(empty);
        } else {
            LinearLayout currentRow = null;
            int col = 0;
            for (SaveData.VaultItem vi : vaultItems) {
                if (col % 4 == 0) {
                    currentRow = new LinearLayout(ctx);
                    currentRow.setOrientation(LinearLayout.HORIZONTAL);
                    currentRow.setPadding(0, 4, 0, 4);
                    tradingContent.addView(currentRow);
                    col = 0;
                }

                Item template = ItemRegistry.getTemplate(vi.itemId);

                LinearLayout cell = new LinearLayout(ctx);
                cell.setOrientation(LinearLayout.VERTICAL);
                cell.setGravity(Gravity.CENTER);
                cell.setBackgroundColor(Color.parseColor("#28233A"));
                cell.setPadding(4, 4, 4, 4);
                LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                cellParams.setMargins(2, 0, 2, 0);
                cell.setLayoutParams(cellParams);

                if (template != null) {
                    View iconView = new ItemIconView(ctx, template);
                    LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(80, 80);
                    iconParams.gravity = Gravity.CENTER;
                    iconView.setLayoutParams(iconParams);
                    cell.addView(iconView);
                }

                TextView countView = new TextView(ctx);
                countView.setText("x" + vi.stackCount);
                countView.setTextColor(template != null ? Item.getRarityColor(template.getRarity().ordinal()) : Color.WHITE);
                countView.setTextSize(9);
                countView.setGravity(Gravity.CENTER);
                countView.setPadding(0, 2, 0, 0);
                cell.addView(countView);

                final String id = vi.itemId;
                cell.setOnClickListener(v -> showSellDialog(id));
                currentRow.addView(cell);
                col++;
            }
            if (currentRow != null) {
                while (col % 4 != 0) {
                    View filler = new View(ctx);
                    filler.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1.0f));
                    currentRow.addView(filler);
                    col++;
                }
            }
        }
    }

    /**
     * Build a row for an active player listing, with price edit and cancel buttons.
     */
    private void addListingRow(SaveData.PlayerListing listing) {
        Context ctx = getContext();
        Item template = ItemRegistry.getTemplate(listing.itemId);
        int rarityColor = template != null ? Item.getRarityColor(template.getRarity().ordinal()) : Color.WHITE;

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundColor(Color.parseColor("#28233A"));
        row.setPadding(16, 10, 16, 10);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 3, 0, 3);
        row.setLayoutParams(rowParams);

        // Item icon
        if (template != null) {
            View iconView = new ItemIconView(ctx, template);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(56, 56);
            iconView.setLayoutParams(iconParams);
            row.addView(iconView);
        }

        // Item name + price
        LinearLayout nameCol = new LinearLayout(ctx);
        nameCol.setOrientation(LinearLayout.VERTICAL);
        nameCol.setPadding(12, 0, 8, 0);
        LinearLayout.LayoutParams nameColParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        nameCol.setLayoutParams(nameColParams);

        TextView nameText = new TextView(ctx);
        nameText.setText(template != null ? template.getName() : listing.itemId);
        nameText.setTextColor(rarityColor);
        nameText.setTextSize(13);
        nameText.setTypeface(Typeface.DEFAULT_BOLD);
        nameCol.addView(nameText);

        TextView priceText = new TextView(ctx);
        priceText.setText("\u25C8 " + listing.price + " coins");
        priceText.setTextColor(Color.parseColor("#FFD700"));
        priceText.setTextSize(12);
        nameCol.addView(priceText);

        row.addView(nameCol);

        // Edit price button
        Button editBtn = new Button(ctx);
        editBtn.setText("PRICE");
        editBtn.setTextColor(Color.WHITE);
        editBtn.setTextSize(10);
        editBtn.setBackgroundColor(Color.parseColor("#2E6B8B"));
        editBtn.setPadding(16, 4, 16, 4);
        editBtn.setOnClickListener(v -> showEditPriceDialog(listing));
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        editParams.setMargins(4, 0, 4, 0);
        editBtn.setLayoutParams(editParams);
        row.addView(editBtn);

        // Cancel button
        Button cancelBtn = new Button(ctx);
        cancelBtn.setText("CANCEL");
        cancelBtn.setTextColor(Color.WHITE);
        cancelBtn.setTextSize(10);
        cancelBtn.setBackgroundColor(Color.parseColor("#8B2E2E"));
        cancelBtn.setPadding(16, 4, 16, 4);
        cancelBtn.setOnClickListener(v -> showCancelConfirmation(listing));
        row.addView(cancelBtn);

        tradingContent.addView(row);
    }

    /**
     * Show a dialog to sell an item from the vault.
     */
    private void showSellDialog(String itemId) {
        SaveManager sm = SaveManager.getInstance();
        if (!sm.canSellThisWeek()) {
            Toast.makeText(getContext(), "Weekly sell limit reached! (" + sm.getMaxSellsPerWeek() + "/week)", Toast.LENGTH_SHORT).show();
            return;
        }

        Item template = ItemRegistry.getTemplate(itemId);
        if (template == null) return;

        Context ctx = getContext();
        int rarityColor = Item.getRarityColor(template.getRarity().ordinal());

        LinearLayout dialogContent = new LinearLayout(ctx);
        dialogContent.setOrientation(LinearLayout.VERTICAL);
        dialogContent.setGravity(Gravity.CENTER_HORIZONTAL);
        dialogContent.setBackgroundColor(Color.parseColor("#1E1830"));
        dialogContent.setPadding(48, 32, 48, 32);

        // Item name
        TextView nameView = new TextView(ctx);
        nameView.setText("Sell " + template.getName() + "?");
        nameView.setTextColor(rarityColor);
        nameView.setTextSize(18);
        nameView.setTypeface(Typeface.DEFAULT_BOLD);
        nameView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        nameParams.bottomMargin = 16;
        nameView.setLayoutParams(nameParams);
        dialogContent.addView(nameView);

        // Item icon
        if (template != null) {
            View iconView = new ItemIconView(ctx, template);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(96, 96);
            iconParams.gravity = Gravity.CENTER;
            iconParams.bottomMargin = 12;
            iconView.setLayoutParams(iconParams);
            dialogContent.addView(iconView);
        }

        // Vault count
        int owned = sm.getVaultItemCount(itemId);
        TextView ownedView = new TextView(ctx);
        ownedView.setText("You own: x" + owned);
        ownedView.setTextColor(Color.parseColor("#AAAACC"));
        ownedView.setTextSize(12);
        ownedView.setGravity(Gravity.CENTER);
        ownedView.setPadding(0, 0, 0, 16);
        dialogContent.addView(ownedView);

        // Sells remaining
        int remaining = sm.getMaxSellsPerWeek() - sm.getWeeklySellCount();
        TextView remainView = new TextView(ctx);
        remainView.setText("Sells remaining this week: " + remaining);
        remainView.setTextColor(Color.parseColor("#AAAACC"));
        remainView.setTextSize(11);
        remainView.setGravity(Gravity.CENTER);
        remainView.setPadding(0, 0, 0, 12);
        dialogContent.addView(remainView);

        // Price label
        TextView priceLabel = new TextView(ctx);
        priceLabel.setText("Set your price (coins):");
        priceLabel.setTextColor(Color.parseColor("#FFD700"));
        priceLabel.setTextSize(14);
        priceLabel.setGravity(Gravity.CENTER);
        dialogContent.addView(priceLabel);

        // Price input
        EditText priceInput = new EditText(ctx);
        priceInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        priceInput.setHint("e.g. 100");
        priceInput.setTextColor(Color.WHITE);
        priceInput.setHintTextColor(Color.parseColor("#666688"));
        priceInput.setBackgroundColor(Color.parseColor("#28233A"));
        priceInput.setPadding(24, 12, 24, 12);
        priceInput.setGravity(Gravity.CENTER);
        priceInput.setTextSize(16);
        LinearLayout.LayoutParams priceInputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        priceInputParams.setMargins(0, 8, 0, 16);
        priceInput.setLayoutParams(priceInputParams);
        dialogContent.addView(priceInput);

        // Info text
        TextView infoText = new TextView(ctx);
        infoText.setText("Item will appear in the shop.\nCoins go to you when someone buys it.");
        infoText.setTextColor(Color.parseColor("#888899"));
        infoText.setTextSize(11);
        infoText.setGravity(Gravity.CENTER);
        dialogContent.addView(infoText);

        ScrollView dialogScroll = new ScrollView(ctx);
        dialogScroll.addView(dialogContent);

        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setView(dialogScroll)
                .setPositiveButton("List for Sale", (d, which) -> {
                    String priceStr = priceInput.getText().toString().trim();
                    if (priceStr.isEmpty()) {
                        Toast.makeText(ctx, "Please enter a price", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int price;
                    try {
                        price = Integer.parseInt(priceStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(ctx, "Invalid price", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (price <= 0) {
                        Toast.makeText(ctx, "Price must be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (sm.listItemForSale(itemId, price)) {
                        Toast.makeText(ctx, template.getName() + " listed for \u25C8 " + price + "!", Toast.LENGTH_SHORT).show();
                        refreshTrading();
                        if (ctx instanceof TabActivity) {
                            ((TabActivity) ctx).updateCoinDisplay();
                        }
                    } else {
                        Toast.makeText(ctx, "Failed to list item", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1E1830")));
        }
        dialog.show();
    }

    /**
     * Show dialog to edit the price of an active listing.
     */
    private void showEditPriceDialog(SaveData.PlayerListing listing) {
        Context ctx = getContext();
        Item template = ItemRegistry.getTemplate(listing.itemId);
        String itemName = template != null ? template.getName() : listing.itemId;

        LinearLayout dialogContent = new LinearLayout(ctx);
        dialogContent.setOrientation(LinearLayout.VERTICAL);
        dialogContent.setGravity(Gravity.CENTER_HORIZONTAL);
        dialogContent.setBackgroundColor(Color.parseColor("#1E1830"));
        dialogContent.setPadding(48, 32, 48, 32);

        TextView title = new TextView(ctx);
        title.setText("Change Price: " + itemName);
        title.setTextColor(Color.WHITE);
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 12);
        dialogContent.addView(title);

        TextView currentPriceView = new TextView(ctx);
        currentPriceView.setText("Current price: \u25C8 " + listing.price);
        currentPriceView.setTextColor(Color.parseColor("#FFD700"));
        currentPriceView.setTextSize(14);
        currentPriceView.setGravity(Gravity.CENTER);
        currentPriceView.setPadding(0, 0, 0, 12);
        dialogContent.addView(currentPriceView);

        EditText priceInput = new EditText(ctx);
        priceInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        priceInput.setText(String.valueOf(listing.price));
        priceInput.setTextColor(Color.WHITE);
        priceInput.setBackgroundColor(Color.parseColor("#28233A"));
        priceInput.setPadding(24, 12, 24, 12);
        priceInput.setGravity(Gravity.CENTER);
        priceInput.setTextSize(16);
        priceInput.selectAll();
        LinearLayout.LayoutParams priceInputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        priceInputParams.setMargins(0, 8, 0, 16);
        priceInput.setLayoutParams(priceInputParams);
        dialogContent.addView(priceInput);

        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setView(dialogContent)
                .setPositiveButton("Update Price", (d, which) -> {
                    String priceStr = priceInput.getText().toString().trim();
                    if (priceStr.isEmpty()) return;
                    int newPrice;
                    try {
                        newPrice = Integer.parseInt(priceStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(ctx, "Invalid price", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPrice <= 0) {
                        Toast.makeText(ctx, "Price must be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SaveManager sm = SaveManager.getInstance();
                    if (sm.updateListingPrice(listing.itemId, listing.listTimestamp, newPrice)) {
                        Toast.makeText(ctx, "Price updated to \u25C8 " + newPrice, Toast.LENGTH_SHORT).show();
                        refreshTrading();
                    } else {
                        Toast.makeText(ctx, "Failed to update price", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1E1830")));
        }
        dialog.show();
    }

    /**
     * Show confirmation dialog to cancel a listing and return the item to vault.
     */
    private void showCancelConfirmation(SaveData.PlayerListing listing) {
        Context ctx = getContext();
        Item template = ItemRegistry.getTemplate(listing.itemId);
        String itemName = template != null ? template.getName() : listing.itemId;

        new AlertDialog.Builder(ctx)
                .setTitle("Cancel Listing?")
                .setMessage("Remove " + itemName + " from the shop and return it to your vault?\n\nCurrent price: \u25C8 " + listing.price)
                .setPositiveButton("Cancel Listing", (d, which) -> {
                    SaveManager sm = SaveManager.getInstance();
                    if (sm.cancelListing(listing.itemId, listing.listTimestamp)) {
                        Toast.makeText(ctx, itemName + " returned to vault", Toast.LENGTH_SHORT).show();
                        refreshTrading();
                    } else {
                        Toast.makeText(ctx, "Failed to cancel listing", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Keep Listed", null)
                .show();
    }
}
