package com.ambermoon.lootgame.tabs;

import android.content.Context;
import android.graphics.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ItemRegistry;
import com.ambermoon.lootgame.entity.RecipeManager;
import com.ambermoon.lootgame.graphics.AssetLoader;
import com.ambermoon.lootgame.save.SaveData;
import com.ambermoon.lootgame.save.SaveManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VaultTab extends ScrollView implements TextWatcher {
    private LinearLayout itemGrid;
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
        searchBox.addTextChangedListener(this);
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

        // Discovered recipes section
        SaveManager sm2 = SaveManager.getInstance();
        List<RecipeManager.Recipe> recipesForItem = RecipeManager.findRecipesForResult(itemId);
        List<RecipeManager.Recipe> discoveredForItem = new ArrayList<>();
        for (RecipeManager.Recipe recipe : recipesForItem) {
            if (sm2.isRecipeDiscovered(recipe.id)) {
                discoveredForItem.add(recipe);
            }
        }
        if (!discoveredForItem.isEmpty()) {
            TextView recipesHeader = new TextView(ctx);
            recipesHeader.setText("LEARNED RECIPES");
            recipesHeader.setTextColor(Color.parseColor("#64DC96"));
            recipesHeader.setTextSize(14);
            recipesHeader.setTypeface(Typeface.DEFAULT_BOLD);
            recipesHeader.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            headerParams.topMargin = 24;
            headerParams.bottomMargin = 8;
            recipesHeader.setLayoutParams(headerParams);
            dialogContent.addView(recipesHeader);

            for (RecipeManager.Recipe recipe : discoveredForItem) {
                // Recipe row container
                LinearLayout recipeRow = new LinearLayout(ctx);
                recipeRow.setOrientation(LinearLayout.HORIZONTAL);
                recipeRow.setGravity(Gravity.CENTER);
                recipeRow.setBackgroundColor(Color.parseColor("#28233A"));
                recipeRow.setPadding(12, 8, 12, 8);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rowParams.bottomMargin = 6;
                recipeRow.setLayoutParams(rowParams);

                // Ingredient icons + names
                for (int i = 0; i < recipe.ingredients.size(); i++) {
                    String ingId = recipe.ingredients.get(i);
                    Item ingTemplate = ItemRegistry.getTemplate(ingId);

                    LinearLayout ingCell = new LinearLayout(ctx);
                    ingCell.setOrientation(LinearLayout.VERTICAL);
                    ingCell.setGravity(Gravity.CENTER);
                    ingCell.setPadding(4, 2, 4, 2);

                    if (ingTemplate != null) {
                        View ingIcon = new ItemIconView(ctx, ingTemplate);
                        LinearLayout.LayoutParams ingIconParams = new LinearLayout.LayoutParams(48, 48);
                        ingIconParams.gravity = Gravity.CENTER;
                        ingIcon.setLayoutParams(ingIconParams);
                        ingCell.addView(ingIcon);
                    }

                    TextView ingName = new TextView(ctx);
                    ingName.setText(ingTemplate != null ? ingTemplate.getName() : ingId);
                    ingName.setTextColor(ingTemplate != null
                            ? Item.getRarityColor(ingTemplate.getRarity().ordinal()) : Color.WHITE);
                    ingName.setTextSize(8);
                    ingName.setGravity(Gravity.CENTER);
                    ingName.setMaxLines(2);
                    ingCell.addView(ingName);

                    recipeRow.addView(ingCell);

                    // Add "+" separator between ingredients
                    if (i < recipe.ingredients.size() - 1) {
                        TextView plus = new TextView(ctx);
                        plus.setText("+");
                        plus.setTextColor(Color.parseColor("#AAAACC"));
                        plus.setTextSize(14);
                        plus.setPadding(4, 0, 4, 0);
                        plus.setGravity(Gravity.CENTER);
                        recipeRow.addView(plus);
                    }
                }

                // Arrow to result
                TextView arrowText = new TextView(ctx);
                arrowText.setText(" \u2192 ");
                arrowText.setTextColor(Color.parseColor("#64DC96"));
                arrowText.setTextSize(14);
                arrowText.setGravity(Gravity.CENTER);
                recipeRow.addView(arrowText);

                // Result icon + name
                Item resultTemplate = ItemRegistry.getTemplate(recipe.result);
                LinearLayout resultCell = new LinearLayout(ctx);
                resultCell.setOrientation(LinearLayout.VERTICAL);
                resultCell.setGravity(Gravity.CENTER);
                resultCell.setPadding(4, 2, 4, 2);

                if (resultTemplate != null) {
                    View resultIcon = new ItemIconView(ctx, resultTemplate);
                    LinearLayout.LayoutParams resIconParams = new LinearLayout.LayoutParams(48, 48);
                    resIconParams.gravity = Gravity.CENTER;
                    resultIcon.setLayoutParams(resIconParams);
                    resultCell.addView(resultIcon);
                }

                TextView resultName = new TextView(ctx);
                String resultLabel = resultTemplate != null ? resultTemplate.getName() : recipe.result;
                if (recipe.resultCount > 1) resultLabel = recipe.resultCount + "x " + resultLabel;
                resultName.setText(resultLabel);
                resultName.setTextColor(resultTemplate != null
                        ? Item.getRarityColor(resultTemplate.getRarity().ordinal()) : Color.WHITE);
                resultName.setTextSize(8);
                resultName.setGravity(Gravity.CENTER);
                resultName.setMaxLines(2);
                resultCell.addView(resultName);

                recipeRow.addView(resultCell);
                dialogContent.addView(recipeRow);
            }
        }

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
}
