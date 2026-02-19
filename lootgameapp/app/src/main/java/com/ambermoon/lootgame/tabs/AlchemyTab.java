package com.ambermoon.lootgame.tabs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import com.ambermoon.lootgame.audio.HapticManager;
import com.ambermoon.lootgame.core.TabActivity;
import com.ambermoon.lootgame.entity.*;
import com.ambermoon.lootgame.save.SaveManager;
import com.ambermoon.lootgame.save.SaveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        setBackgroundColor(Color.TRANSPARENT);

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

        // Learned Recipes button
        Button recipesButton = new Button(context);
        recipesButton.setText("LEARNED RECIPES");
        recipesButton.setTextColor(Color.WHITE);
        recipesButton.setTextSize(13);
        recipesButton.setBackgroundColor(Color.parseColor("#28233A"));
        recipesButton.setPadding(32, 12, 32, 12);
        LinearLayout.LayoutParams recBtnP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        recBtnP.topMargin = 12;
        recipesButton.setLayoutParams(recBtnP);
        recipesButton.setOnClickListener(v -> showLearnedRecipes());
        content.addView(recipesButton);

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
                HapticManager.getInstance().tap();
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
        sm.addLearnedRecipe(currentRecipe.id, currentRecipe.name,
                currentRecipe.ingredients, currentRecipe.result, currentRecipe.resultCount);
        sm.save();

        // Haptic: satisfying rising double-tap for successful craft
        HapticManager.getInstance().craftSuccess();

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
            Item template = ItemRegistry.getTemplate(vi.itemId);
            if (template == null) continue;

            if (col % 4 == 0) {
                currentRow = new LinearLayout(getContext());
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setPadding(0, 4, 0, 4);
                vaultGrid.addView(currentRow);
                col = 0;
            }

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
            View iconView = new ItemIconView(getContext(), template);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(72, 72);
            iconParams.gravity = Gravity.CENTER;
            iconView.setLayoutParams(iconParams);
            cell.addView(iconView);

            // Stack count
            TextView countView = new TextView(getContext());
            countView.setText("x" + vi.stackCount);
            countView.setTextColor(Item.getRarityColor(template.getRarity().ordinal()));
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

    private void showLearnedRecipes() {
        Context ctx = getContext();
        SaveManager sm = SaveManager.getInstance();
        List<SaveData.LearnedRecipe> recipes = sm.getData().learnedRecipes;

        LinearLayout dialogContent = new LinearLayout(ctx);
        dialogContent.setOrientation(LinearLayout.VERTICAL);
        dialogContent.setBackgroundColor(Color.parseColor("#1E1830"));
        dialogContent.setPadding(32, 24, 32, 24);

        // Title
        TextView title = new TextView(ctx);
        title.setText("LEARNED RECIPES");
        title.setTextColor(Color.parseColor("#64DC96"));
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleP.bottomMargin = 4;
        title.setLayoutParams(titleP);
        dialogContent.addView(title);

        // Subtitle hint
        TextView hint = new TextView(ctx);
        hint.setText("Tap a recipe to craft it");
        hint.setTextColor(Color.parseColor("#888899"));
        hint.setTextSize(11);
        hint.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hintP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hintP.bottomMargin = 4;
        hint.setLayoutParams(hintP);
        dialogContent.addView(hint);

        // Recipe count
        TextView countLabel = new TextView(ctx);
        countLabel.setText(recipes.size() + " recipe" + (recipes.size() != 1 ? "s" : "") + " discovered");
        countLabel.setTextColor(Color.parseColor("#AAAACC"));
        countLabel.setTextSize(12);
        countLabel.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams countP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        countP.bottomMargin = 16;
        countLabel.setLayoutParams(countP);
        dialogContent.addView(countLabel);

        if (recipes.isEmpty()) {
            TextView empty = new TextView(ctx);
            empty.setText("No recipes learned yet.\nCraft or deconstruct items to discover recipes!");
            empty.setTextColor(Color.parseColor("#888888"));
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 32, 0, 32);
            dialogContent.addView(empty);
        } else {
            for (SaveData.LearnedRecipe lr : recipes) {
                // Compute what the player needs vs. has for this recipe
                HashMap<String, Integer> needed = new HashMap<>();
                for (String ing : lr.ingredients) {
                    Integer prev = needed.get(ing);
                    needed.put(ing, (prev != null ? prev : 0) + 1);
                }
                boolean canCraft = true;
                List<String> missingLines = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : needed.entrySet()) {
                    int have = sm.getVaultItemCount(entry.getKey());
                    int need = entry.getValue();
                    if (have < need) {
                        canCraft = false;
                        Item ingItem = ItemRegistry.getTemplate(entry.getKey());
                        String ingName = ingItem != null ? ingItem.getName() : entry.getKey();
                        missingLines.add(ingName + " (" + have + "/" + need + ")");
                    }
                }

                // Recipe card
                LinearLayout card = new LinearLayout(ctx);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackgroundColor(canCraft ? Color.parseColor("#1E3328") : Color.parseColor("#28233A"));
                card.setPadding(12, 8, 12, 8);
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                cardParams.bottomMargin = 6;
                card.setLayoutParams(cardParams);

                // Recipe name
                Item resultTemplate = ItemRegistry.getTemplate(lr.result);
                TextView nameLabel = new TextView(ctx);
                String displayName = lr.name != null && !lr.name.isEmpty() ? lr.name :
                        (resultTemplate != null ? resultTemplate.getName() : lr.result);
                if (lr.resultCount > 1) displayName = lr.resultCount + "x " + displayName;
                nameLabel.setText(displayName);
                nameLabel.setTextColor(resultTemplate != null
                        ? Item.getRarityColor(resultTemplate.getRarity().ordinal()) : Color.WHITE);
                nameLabel.setTextSize(12);
                nameLabel.setTypeface(Typeface.DEFAULT_BOLD);
                nameLabel.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams nameP = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                nameP.bottomMargin = 4;
                nameLabel.setLayoutParams(nameP);
                card.addView(nameLabel);

                // Compact icon row
                LinearLayout iconRow = new LinearLayout(ctx);
                iconRow.setOrientation(LinearLayout.HORIZONTAL);
                iconRow.setGravity(Gravity.CENTER);

                for (int i = 0; i < lr.ingredients.size(); i++) {
                    String ingId = lr.ingredients.get(i);
                    Item ingTemplate = ItemRegistry.getTemplate(ingId);

                    if (ingTemplate != null) {
                        View ingIcon = new ItemIconView(ctx, ingTemplate);
                        LinearLayout.LayoutParams ingIconP = new LinearLayout.LayoutParams(36, 36);
                        ingIconP.setMargins(2, 0, 2, 0);
                        ingIcon.setLayoutParams(ingIconP);
                        iconRow.addView(ingIcon);
                    }

                    if (i < lr.ingredients.size() - 1) {
                        TextView plus = new TextView(ctx);
                        plus.setText("+");
                        plus.setTextColor(Color.parseColor("#AAAACC"));
                        plus.setTextSize(12);
                        plus.setPadding(2, 0, 2, 0);
                        plus.setGravity(Gravity.CENTER);
                        iconRow.addView(plus);
                    }
                }

                // Arrow
                TextView arrowText = new TextView(ctx);
                arrowText.setText("\u2192");
                arrowText.setTextColor(Color.parseColor("#64DC96"));
                arrowText.setTextSize(12);
                arrowText.setPadding(6, 0, 6, 0);
                arrowText.setGravity(Gravity.CENTER);
                iconRow.addView(arrowText);

                // Result icon
                if (resultTemplate != null) {
                    View resultIcon = new ItemIconView(ctx, resultTemplate);
                    LinearLayout.LayoutParams resIconP = new LinearLayout.LayoutParams(36, 36);
                    resIconP.setMargins(2, 0, 2, 0);
                    resultIcon.setLayoutParams(resIconP);
                    iconRow.addView(resultIcon);
                }

                card.addView(iconRow);

                // Status line: craftable indicator or missing ingredients in plain text
                if (canCraft) {
                    TextView craftLabel = new TextView(ctx);
                    craftLabel.setText("Tap to craft");
                    craftLabel.setTextColor(Color.parseColor("#64DC96"));
                    craftLabel.setTextSize(10);
                    craftLabel.setGravity(Gravity.CENTER);
                    craftLabel.setPadding(0, 4, 0, 0);
                    card.addView(craftLabel);
                } else {
                    TextView missingLabel = new TextView(ctx);
                    StringBuilder sb = new StringBuilder("Need: ");
                    for (int i = 0; i < missingLines.size(); i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(missingLines.get(i));
                    }
                    missingLabel.setText(sb.toString());
                    missingLabel.setTextColor(Color.parseColor("#AA6666"));
                    missingLabel.setTextSize(10);
                    missingLabel.setGravity(Gravity.CENTER);
                    missingLabel.setPadding(0, 4, 0, 0);
                    card.addView(missingLabel);
                }

                // Tap handler
                final SaveData.LearnedRecipe recipe = lr;
                final boolean craftable = canCraft;
                card.setOnClickListener(v -> {
                    if (craftable) {
                        craftFromRecipe(recipe);
                    } else {
                        Toast.makeText(ctx, "Missing ingredients!", Toast.LENGTH_SHORT).show();
                    }
                });

                dialogContent.addView(card);
            }
        }

        ScrollView dialogScroll = new ScrollView(ctx);
        dialogScroll.addView(dialogContent);

        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setView(dialogScroll)
                .setPositiveButton("Close", null)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1E1830")));
        }
        dialog.show();
    }

    /**
     * Auto-craft a learned recipe: consume ingredients, add result, save, and reopen the dialog.
     */
    private void craftFromRecipe(SaveData.LearnedRecipe lr) {
        SaveManager sm = SaveManager.getInstance();

        // Verify ingredients are still available
        HashMap<String, Integer> needed = new HashMap<>();
        for (String ing : lr.ingredients) {
            Integer prev = needed.get(ing);
            needed.put(ing, (prev != null ? prev : 0) + 1);
        }
        for (Map.Entry<String, Integer> entry : needed.entrySet()) {
            if (sm.getVaultItemCount(entry.getKey()) < entry.getValue()) {
                Toast.makeText(getContext(), "Missing: " + entry.getKey(), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Consume ingredients
        for (String ing : lr.ingredients) {
            sm.removeVaultItem(ing, 1);
        }

        // Add result
        sm.addVaultItem(lr.result, lr.resultCount);
        sm.getData().totalItemsCollected += lr.resultCount;
        sm.save();

        HapticManager.getInstance().craftSuccess();

        Item resultTemplate = ItemRegistry.getTemplate(lr.result);
        String resultName = resultTemplate != null ? resultTemplate.getName() : lr.result;
        Toast.makeText(getContext(), "Crafted: " + resultName, Toast.LENGTH_SHORT).show();

        // Refresh the alchemy vault grid behind the dialog
        refreshVault();

        // Reopen the recipes dialog with updated availability
        showLearnedRecipes();
    }
}
