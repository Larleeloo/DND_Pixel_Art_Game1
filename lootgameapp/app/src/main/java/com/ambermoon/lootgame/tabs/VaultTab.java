package com.ambermoon.lootgame.tabs;

import android.content.Context;
import android.graphics.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ItemRegistry;
import com.ambermoon.lootgame.save.SaveData;
import com.ambermoon.lootgame.save.SaveManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VaultTab extends ScrollView {
    private LinearLayout itemGrid;
    private TextView detailPanel;
    private EditText searchBox;
    private String currentFilter = "All";
    private int currentSort = 0; // 0=rarity, 1=name, 2=count

    public VaultTab(Context context) {
        super(context);
        setBackgroundColor(Color.parseColor("#1A1525"));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24, 16, 24, 16);

        // Header
        TextView title = new TextView(context);
        title.setText("YOUR VAULT");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(title);

        SaveManager sm = SaveManager.getInstance();
        int totalStacks = sm.getData().vaultItems.size();
        int totalItems = 0;
        for (SaveData.VaultItem vi : sm.getData().vaultItems) totalItems += vi.stackCount;

        TextView subtitle = new TextView(context);
        subtitle.setText(totalItems + " items in " + totalStacks + " stacks");
        subtitle.setTextColor(Color.parseColor("#AAAACC"));
        subtitle.setTextSize(13);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 16);
        content.addView(subtitle);

        // Search bar
        searchBox = new EditText(context);
        searchBox.setHint("Search items...");
        searchBox.setTextColor(Color.WHITE);
        searchBox.setHintTextColor(Color.parseColor("#666688"));
        searchBox.setBackgroundColor(Color.parseColor("#28233A"));
        searchBox.setPadding(24, 16, 24, 16);
        searchBox.setSingleLine(true);
        searchBox.addTextChangedListener(new SearchTextWatcher());
        content.addView(searchBox);

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
        content.addView(sortRow);

        // Item grid
        itemGrid = new LinearLayout(context);
        itemGrid.setOrientation(LinearLayout.VERTICAL);
        content.addView(itemGrid);

        // Detail panel
        detailPanel = new TextView(context);
        detailPanel.setTextColor(Color.parseColor("#CCCCCC"));
        detailPanel.setTextSize(13);
        detailPanel.setBackgroundColor(Color.parseColor("#28233A"));
        detailPanel.setPadding(24, 16, 24, 16);
        detailPanel.setVisibility(View.GONE);
        content.addView(detailPanel);

        addView(content);
        refreshGrid();
    }

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
            TextView itemView = new TextView(getContext());
            String name = template != null ? template.getName() : vi.itemId;
            itemView.setText(name + "\nx" + vi.stackCount);
            itemView.setTextColor(template != null ? Item.getRarityColor(template.getRarity().ordinal()) : Color.WHITE);
            itemView.setTextSize(10);
            itemView.setGravity(Gravity.CENTER);
            itemView.setBackgroundColor(Color.parseColor("#28233A"));
            itemView.setPadding(8, 12, 8, 12);
            itemView.setMaxLines(3);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            p.setMargins(2, 0, 2, 0);
            itemView.setLayoutParams(p);

            final String id = vi.itemId;
            itemView.setOnClickListener(v -> showDetail(id));
            currentRow.addView(itemView);
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

    private class SearchTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) { refreshGrid(); }
        @Override public void afterTextChanged(Editable s) {}
    }

    private void showDetail(String itemId) {
        Item template = ItemRegistry.getTemplate(itemId);
        if (template == null) {
            detailPanel.setVisibility(View.GONE);
            return;
        }

        detailPanel.setVisibility(View.VISIBLE);
        detailPanel.setText(template.getTooltip() +
                "\nVault Count: " + SaveManager.getInstance().getVaultItemCount(itemId));
    }
}
