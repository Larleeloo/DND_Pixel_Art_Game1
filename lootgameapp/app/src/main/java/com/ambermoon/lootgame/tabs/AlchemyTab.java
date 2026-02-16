package com.ambermoon.lootgame.tabs;

import android.content.Context;
import android.graphics.*;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import com.ambermoon.lootgame.core.TabActivity;
import com.ambermoon.lootgame.entity.*;
import com.ambermoon.lootgame.save.SaveManager;
import com.ambermoon.lootgame.save.SaveData;

import java.util.ArrayList;
import java.util.List;

public class AlchemyTab extends ScrollView {
    private String[] inputSlots = new String[3]; // item IDs in slots
    private String outputItem = null;
    private RecipeManager.Recipe currentRecipe = null;
    private TextView outputText;
    private Button craftButton;
    private LinearLayout vaultGrid;
    private TextView[] slotTexts = new TextView[3];

    public AlchemyTab(Context context) {
        super(context);
        setBackgroundColor(Color.parseColor("#1A1525"));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(32, 24, 32, 24);

        // Title
        TextView title = new TextView(context);
        title.setText("ALCHEMY TABLE");
        title.setTextColor(Color.parseColor("#64DC96"));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(title);

        // Input slots row
        TextView inputLabel = new TextView(context);
        inputLabel.setText("Input Slots (tap vault item to add):");
        inputLabel.setTextColor(Color.parseColor("#AAAACC"));
        inputLabel.setTextSize(13);
        inputLabel.setPadding(0, 24, 0, 8);
        content.addView(inputLabel);

        LinearLayout slotsRow = new LinearLayout(context);
        slotsRow.setOrientation(LinearLayout.HORIZONTAL);
        slotsRow.setGravity(Gravity.CENTER);
        for (int i = 0; i < 3; i++) {
            final int slot = i;
            TextView slotView = new TextView(context);
            slotView.setText("[Empty]");
            slotView.setTextColor(Color.parseColor("#666688"));
            slotView.setTextSize(12);
            slotView.setGravity(Gravity.CENTER);
            slotView.setBackgroundColor(Color.parseColor("#28233A"));
            slotView.setPadding(16, 24, 16, 24);
            slotView.setMinWidth(180);
            slotView.setMaxLines(2);
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            sp.setMargins(4, 0, 4, 0);
            slotView.setLayoutParams(sp);
            slotView.setOnClickListener(v -> clearSlot(slot));
            slotsRow.addView(slotView);
            slotTexts[i] = slotView;
        }
        content.addView(slotsRow);

        // Arrow
        TextView arrow = new TextView(context);
        arrow.setText("\u2193");
        arrow.setTextColor(Color.parseColor("#64DC96"));
        arrow.setTextSize(24);
        arrow.setGravity(Gravity.CENTER);
        arrow.setPadding(0, 8, 0, 8);
        content.addView(arrow);

        // Output slot
        outputText = new TextView(context);
        outputText.setText("?");
        outputText.setTextColor(Color.parseColor("#666688"));
        outputText.setTextSize(16);
        outputText.setGravity(Gravity.CENTER);
        outputText.setBackgroundColor(Color.parseColor("#28233A"));
        outputText.setPadding(24, 16, 24, 16);
        outputText.setMinWidth(200);
        content.addView(outputText);

        // Craft button
        craftButton = new Button(context);
        craftButton.setText("CRAFT");
        craftButton.setTextColor(Color.WHITE);
        craftButton.setTextSize(16);
        craftButton.setBackgroundColor(Color.parseColor("#444444"));
        craftButton.setEnabled(false);
        craftButton.setPadding(48, 16, 48, 16);
        LinearLayout.LayoutParams craftP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        craftP.topMargin = 16;
        craftButton.setLayoutParams(craftP);
        craftButton.setOnClickListener(v -> doCraft());
        content.addView(craftButton);

        // Vault section
        TextView vaultLabel = new TextView(context);
        vaultLabel.setText("--- Your Vault ---");
        vaultLabel.setTextColor(Color.parseColor("#AAAACC"));
        vaultLabel.setTextSize(14);
        vaultLabel.setGravity(Gravity.CENTER);
        vaultLabel.setPadding(0, 32, 0, 8);
        content.addView(vaultLabel);

        vaultGrid = new LinearLayout(context);
        vaultGrid.setOrientation(LinearLayout.VERTICAL);
        content.addView(vaultGrid);

        addView(content);
        refreshVault();
    }

    private void addItemToSlot(String itemId) {
        for (int i = 0; i < 3; i++) {
            if (inputSlots[i] == null) {
                // Count how many of this item are already in slots
                int alreadyInSlots = 0;
                for (String s : inputSlots) {
                    if (itemId.equals(s)) alreadyInSlots++;
                }
                // Check if vault has more than what's already placed
                int vaultCount = SaveManager.getInstance().getVaultItemCount(itemId);
                if (alreadyInSlots >= vaultCount) {
                    Toast.makeText(getContext(), "Not enough in vault!", Toast.LENGTH_SHORT).show();
                    return;
                }
                inputSlots[i] = itemId;
                Item item = ItemRegistry.getTemplate(itemId);
                slotTexts[i].setText(item != null ? item.getName() : itemId);
                slotTexts[i].setTextColor(item != null ? Item.getRarityColor(item.getRarity().ordinal()) : Color.WHITE);
                checkRecipe();
                return;
            }
        }
    }

    private void clearSlot(int slot) {
        inputSlots[slot] = null;
        slotTexts[slot].setText("[Empty]");
        slotTexts[slot].setTextColor(Color.parseColor("#666688"));
        checkRecipe();
    }

    private void checkRecipe() {
        List<String> ingredients = new ArrayList<>();
        for (String s : inputSlots) {
            if (s != null) ingredients.add(s);
        }

        if (ingredients.isEmpty()) {
            outputText.setText("?");
            outputText.setTextColor(Color.parseColor("#666688"));
            craftButton.setEnabled(false);
            craftButton.setBackgroundColor(Color.parseColor("#444444"));
            currentRecipe = null;
            return;
        }

        currentRecipe = RecipeManager.findRecipe(ingredients);
        if (currentRecipe != null) {
            Item result = ItemRegistry.getTemplate(currentRecipe.result);
            outputText.setText(result != null ? result.getName() : currentRecipe.result);
            outputText.setTextColor(Color.parseColor("#64DC96"));
            craftButton.setEnabled(true);
            craftButton.setBackgroundColor(Color.parseColor("#64DC96"));
        } else {
            outputText.setText("No recipe");
            outputText.setTextColor(Color.parseColor("#FF4444"));
            craftButton.setEnabled(false);
            craftButton.setBackgroundColor(Color.parseColor("#444444"));
        }
    }

    private void doCraft() {
        if (currentRecipe == null) return;
        SaveManager sm = SaveManager.getInstance();

        // Count required quantity of each ingredient
        java.util.HashMap<String, Integer> needed = new java.util.HashMap<>();
        for (String ing : currentRecipe.ingredients) {
            Integer prev = needed.get(ing);
            needed.put(ing, (prev != null ? prev : 0) + 1);
        }
        // Check vault has enough of each ingredient
        for (java.util.Map.Entry<String, Integer> entry : needed.entrySet()) {
            if (sm.getVaultItemCount(entry.getKey()) < entry.getValue()) {
                Toast.makeText(getContext(), "Missing: " + entry.getKey(), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Consume ingredients
        for (String ing : currentRecipe.ingredients) {
            sm.removeVaultItem(ing, 1);
        }

        // Add result
        sm.addVaultItem(currentRecipe.result, currentRecipe.resultCount);
        sm.getData().totalItemsCollected += currentRecipe.resultCount;
        sm.addDiscoveredRecipe(currentRecipe.id);
        sm.save();

        Toast.makeText(getContext(), "Crafted: " + currentRecipe.name, Toast.LENGTH_SHORT).show();

        // Clear slots
        for (int i = 0; i < 3; i++) clearSlot(i);
        refreshVault();
    }

    private void refreshVault() {
        vaultGrid.removeAllViews();
        SaveManager sm = SaveManager.getInstance();
        List<SaveData.VaultItem> items = sm.getData().vaultItems;

        LinearLayout currentRow = null;
        int col = 0;
        for (SaveData.VaultItem vi : items) {
            if (col % 4 == 0) {
                currentRow = new LinearLayout(getContext());
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setPadding(0, 4, 0, 4);
                vaultGrid.addView(currentRow);
                col = 0;
            }

            Item template = ItemRegistry.getTemplate(vi.itemId);

            // Item cell: icon sprite + count
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
                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(72, 72);
                iconParams.gravity = Gravity.CENTER;
                iconView.setLayoutParams(iconParams);
                cell.addView(iconView);
            }

            // Stack count
            TextView countView = new TextView(getContext());
            countView.setText("x" + vi.stackCount);
            countView.setTextColor(template != null ? Item.getRarityColor(template.getRarity().ordinal()) : Color.WHITE);
            countView.setTextSize(9);
            countView.setGravity(Gravity.CENTER);
            countView.setPadding(0, 2, 0, 0);
            cell.addView(countView);

            final String id = vi.itemId;
            cell.setOnClickListener(v -> addItemToSlot(id));
            currentRow.addView(cell);
            col++;
        }

        // Fill remaining columns in last row
        if (currentRow != null) {
            while (col % 4 != 0) {
                View spacer = new View(getContext());
                LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, 1, 1.0f);
                spacer.setLayoutParams(sp);
                currentRow.addView(spacer);
                col++;
            }
        }

        if (items.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("Your vault is empty.\nOpen chests to collect items!");
            empty.setTextColor(Color.parseColor("#888888"));
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 32, 0, 0);
            vaultGrid.addView(empty);
        }
    }
}
