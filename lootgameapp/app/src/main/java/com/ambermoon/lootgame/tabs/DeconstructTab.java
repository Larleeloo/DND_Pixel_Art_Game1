package com.ambermoon.lootgame.tabs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import com.ambermoon.lootgame.entity.*;
import com.ambermoon.lootgame.save.SaveData;
import com.ambermoon.lootgame.save.SaveManager;

import java.util.List;

public class DeconstructTab extends ScrollView {
    private String inputItemId = null;
    private RecipeManager.Recipe currentRecipe = null;
    private TextView inputSlotText;
    private TextView[] outputTexts = new TextView[3];
    private Button deconstructButton;
    private LinearLayout vaultGrid;

    public DeconstructTab(Context context) {
        super(context);
        setBackgroundColor(Color.parseColor("#1A1525"));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(32, 24, 32, 24);

        TextView title = new TextView(context);
        title.setText("REVERSE CRAFTING");
        title.setTextColor(Color.parseColor("#B464FF"));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(title);

        TextView desc = new TextView(context);
        desc.setText("Break items into components");
        desc.setTextColor(Color.parseColor("#AAAACC"));
        desc.setTextSize(13);
        desc.setGravity(Gravity.CENTER);
        desc.setPadding(0, 8, 0, 24);
        content.addView(desc);

        // Input/Output layout
        LinearLayout ioRow = new LinearLayout(context);
        ioRow.setOrientation(LinearLayout.HORIZONTAL);
        ioRow.setGravity(Gravity.CENTER);

        // Input
        inputSlotText = new TextView(context);
        inputSlotText.setText("[Empty]");
        inputSlotText.setTextColor(Color.parseColor("#666688"));
        inputSlotText.setTextSize(13);
        inputSlotText.setGravity(Gravity.CENTER);
        inputSlotText.setBackgroundColor(Color.parseColor("#28233A"));
        inputSlotText.setPadding(16, 24, 16, 24);
        inputSlotText.setMinWidth(150);
        inputSlotText.setMaxLines(2);
        inputSlotText.setOnClickListener(v -> clearInput());
        ioRow.addView(inputSlotText);

        TextView arrow = new TextView(context);
        arrow.setText("  \u2192  ");
        arrow.setTextColor(Color.parseColor("#B464FF"));
        arrow.setTextSize(20);
        ioRow.addView(arrow);

        // Output slots
        LinearLayout outputRow = new LinearLayout(context);
        outputRow.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < 3; i++) {
            outputTexts[i] = new TextView(context);
            outputTexts[i].setText("-");
            outputTexts[i].setTextColor(Color.parseColor("#666688"));
            outputTexts[i].setTextSize(11);
            outputTexts[i].setGravity(Gravity.CENTER);
            outputTexts[i].setBackgroundColor(Color.parseColor("#28233A"));
            outputTexts[i].setPadding(12, 24, 12, 24);
            outputTexts[i].setMinWidth(100);
            outputTexts[i].setMaxLines(2);
            LinearLayout.LayoutParams op = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            op.setMargins(2, 0, 2, 0);
            outputTexts[i].setLayoutParams(op);
            outputRow.addView(outputTexts[i]);
        }
        ioRow.addView(outputRow);
        content.addView(ioRow);

        deconstructButton = new Button(context);
        deconstructButton.setText("DECONSTRUCT");
        deconstructButton.setTextColor(Color.WHITE);
        deconstructButton.setBackgroundColor(Color.parseColor("#444444"));
        deconstructButton.setEnabled(false);
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dp.topMargin = 16;
        deconstructButton.setLayoutParams(dp);
        deconstructButton.setOnClickListener(v -> doDeconstruct());
        content.addView(deconstructButton);

        TextView note = new TextView(context);
        note.setText("Only reversible items can be deconstructed");
        note.setTextColor(Color.parseColor("#888888"));
        note.setTextSize(11);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, 8, 0, 0);
        content.addView(note);

        // Vault
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

    private void setInput(String itemId) {
        inputItemId = itemId;
        Item item = ItemRegistry.getTemplate(itemId);
        inputSlotText.setText(item != null ? item.getName() : itemId);
        inputSlotText.setTextColor(item != null ? Item.getRarityColor(item.getRarity().ordinal()) : Color.WHITE);

        // Check reverse recipes
        List<RecipeManager.Recipe> reverseRecipes = RecipeManager.findReverseRecipes(itemId);
        if (!reverseRecipes.isEmpty()) {
            currentRecipe = reverseRecipes.get(0);
            List<String> components = currentRecipe.getReverseIngredients();
            for (int i = 0; i < 3; i++) {
                if (i < components.size()) {
                    Item comp = ItemRegistry.getTemplate(components.get(i));
                    outputTexts[i].setText(comp != null ? comp.getName() : components.get(i));
                    outputTexts[i].setTextColor(comp != null ? Item.getRarityColor(comp.getRarity().ordinal()) : Color.WHITE);
                } else {
                    outputTexts[i].setText("-");
                    outputTexts[i].setTextColor(Color.parseColor("#666688"));
                }
            }
            deconstructButton.setEnabled(true);
            deconstructButton.setBackgroundColor(Color.parseColor("#B464FF"));
        } else {
            currentRecipe = null;
            for (int i = 0; i < 3; i++) {
                outputTexts[i].setText("-");
                outputTexts[i].setTextColor(Color.parseColor("#666688"));
            }
            deconstructButton.setEnabled(false);
            deconstructButton.setBackgroundColor(Color.parseColor("#444444"));
            Toast.makeText(getContext(), "Not deconstructable", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearInput() {
        inputItemId = null;
        inputSlotText.setText("[Empty]");
        inputSlotText.setTextColor(Color.parseColor("#666688"));
        for (int i = 0; i < 3; i++) {
            outputTexts[i].setText("-");
            outputTexts[i].setTextColor(Color.parseColor("#666688"));
        }
        currentRecipe = null;
        deconstructButton.setEnabled(false);
        deconstructButton.setBackgroundColor(Color.parseColor("#444444"));
    }

    private void doDeconstruct() {
        if (currentRecipe == null || inputItemId == null) return;
        SaveManager sm = SaveManager.getInstance();

        if (sm.getVaultItemCount(inputItemId) < 1) {
            Toast.makeText(getContext(), "Item not in vault!", Toast.LENGTH_SHORT).show();
            return;
        }

        sm.removeVaultItem(inputItemId, 1);
        for (String comp : currentRecipe.ingredients) {
            sm.addVaultItem(comp, 1);
        }
        sm.save();

        Toast.makeText(getContext(), "Deconstructed!", Toast.LENGTH_SHORT).show();
        clearInput();
        refreshVault();
    }

    private void refreshVault() {
        vaultGrid.removeAllViews();
        SaveManager sm = SaveManager.getInstance();

        LinearLayout currentRow = null;
        int col = 0;
        for (SaveData.VaultItem vi : sm.getData().vaultItems) {
            if (col % 4 == 0) {
                currentRow = new LinearLayout(getContext());
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setPadding(0, 4, 0, 4);
                vaultGrid.addView(currentRow);
                col = 0;
            }

            Item template = ItemRegistry.getTemplate(vi.itemId);
            TextView itemView = new TextView(getContext());
            itemView.setText((template != null ? template.getName() : vi.itemId) + "\nx" + vi.stackCount);
            itemView.setTextColor(template != null ? Item.getRarityColor(template.getRarity().ordinal()) : Color.WHITE);
            itemView.setTextSize(10);
            itemView.setGravity(Gravity.CENTER);
            itemView.setBackgroundColor(Color.parseColor("#28233A"));
            itemView.setPadding(8, 8, 8, 8);
            itemView.setMaxLines(3);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            p.setMargins(2, 0, 2, 0);
            itemView.setLayoutParams(p);
            final String id = vi.itemId;
            itemView.setOnClickListener(v -> setInput(id));
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
    }
}
