package com.ambermoon.lootgame.core;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import android.graphics.Point;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ItemRegistry;
import com.ambermoon.lootgame.graphics.AvatarRegistry;
import com.ambermoon.lootgame.graphics.EquipmentSlot;
import com.ambermoon.lootgame.graphics.SpriteLayerCompositor;
import com.ambermoon.lootgame.save.SaveData;
import com.ambermoon.lootgame.save.SaveManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Full-screen dialog for clothing preview and equipment selection.
 * Shows a 256x256 animated preview with avatar selection and
 * 10 equipment slot buttons that open item pickers.
 *
 * Uses named static inner classes for all Views to avoid D8 compiler bugs.
 */
public class ClothingPreviewPopup {

    private static final int PREVIEW_SIZE_DP = 200;  // Preview display size
    private static final int SLOT_COLUMNS = 2;

    public interface OnPreviewChangedListener {
        void onPreviewChanged();
    }

    public static void show(Context context, OnPreviewChangedListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Root container
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#1E1830"));
        root.setPadding(24, 16, 24, 16);

        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.parseColor("#1E1830"));
        bg.setCornerRadius(24);
        bg.setStroke(2, Color.parseColor("#FFD700"));
        root.setBackground(bg);

        // Title
        TextView title = new TextView(context);
        title.setText("Character Preview");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 8);
        root.addView(title);

        // --- Animated Preview ---
        ClothingPreviewView previewView = new ClothingPreviewView(context);
        float density = context.getResources().getDisplayMetrics().density;
        int previewPx = (int) (PREVIEW_SIZE_DP * density);
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(previewPx, previewPx);
        previewParams.gravity = Gravity.CENTER_HORIZONTAL;
        previewParams.bottomMargin = 8;
        previewView.setLayoutParams(previewParams);
        root.addView(previewView);

        // --- Walk/Idle toggle ---
        LinearLayout toggleRow = new LinearLayout(context);
        toggleRow.setOrientation(LinearLayout.HORIZONTAL);
        toggleRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams toggleRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        toggleRowParams.bottomMargin = 8;
        toggleRow.setLayoutParams(toggleRowParams);

        Button walkBtn = createStyledButton(context, "Walking", true);
        Button idleBtn = createStyledButton(context, "Idle", false);

        walkBtn.setOnClickListener(v -> {
            previewView.setShowWalking(true);
            styleToggleButton(walkBtn, true);
            styleToggleButton(idleBtn, false);
        });
        idleBtn.setOnClickListener(v -> {
            previewView.setShowWalking(false);
            styleToggleButton(walkBtn, false);
            styleToggleButton(idleBtn, true);
        });

        toggleRow.addView(walkBtn);
        toggleRow.addView(idleBtn);
        root.addView(toggleRow);

        // --- Avatar Selector ---
        TextView avatarLabel = new TextView(context);
        avatarLabel.setText("Select Avatar");
        avatarLabel.setTextColor(Color.parseColor("#B8A9D4"));
        avatarLabel.setTextSize(12);
        avatarLabel.setTypeface(null, Typeface.BOLD);
        avatarLabel.setPadding(0, 0, 0, 4);
        root.addView(avatarLabel);

        int currentAvatar = getSavedAvatarIndex();
        LinearLayout avatarRow = new LinearLayout(context);
        avatarRow.setOrientation(LinearLayout.HORIZONTAL);
        avatarRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams avatarRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        avatarRowParams.bottomMargin = 8;
        avatarRow.setLayoutParams(avatarRowParams);

        Button[] avatarBtns = new Button[AvatarRegistry.AVATAR_COUNT];
        for (int i = 0; i < AvatarRegistry.AVATAR_COUNT; i++) {
            final int idx = i;
            Button avatarBtn = new Button(context);
            avatarBtn.setText(AvatarRegistry.AVATAR_NAMES[i]);
            avatarBtn.setTextSize(11);
            avatarBtn.setPadding(16, 8, 16, 8);
            LinearLayout.LayoutParams btnP = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            btnP.setMargins(4, 0, 4, 0);
            avatarBtn.setLayoutParams(btnP);
            styleAvatarButton(avatarBtn, i == currentAvatar);
            avatarBtn.setOnClickListener(v -> {
                saveAvatarIndex(idx);
                for (int j = 0; j < avatarBtns.length; j++) {
                    styleAvatarButton(avatarBtns[j], j == idx);
                }
                refreshPreview(previewView);
                if (listener != null) listener.onPreviewChanged();
            });
            avatarBtns[i] = avatarBtn;
            avatarRow.addView(avatarBtn);
        }
        root.addView(avatarRow);

        // Divider
        root.addView(createDivider(context));

        // --- Equipment Slots ---
        TextView slotsLabel = new TextView(context);
        slotsLabel.setText("Equipment Slots");
        slotsLabel.setTextColor(Color.parseColor("#B8A9D4"));
        slotsLabel.setTextSize(12);
        slotsLabel.setTypeface(null, Typeface.BOLD);
        slotsLabel.setPadding(0, 8, 0, 4);
        root.addView(slotsLabel);

        TextView slotsHint = new TextView(context);
        slotsHint.setText("Tap a slot to equip items from your vault");
        slotsHint.setTextColor(Color.parseColor("#666666"));
        slotsHint.setTextSize(9);
        slotsHint.setPadding(0, 0, 0, 8);
        root.addView(slotsHint);

        // Scrollable slot grid
        ScrollView slotScroll = new ScrollView(context);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
        slotScroll.setLayoutParams(scrollParams);

        LinearLayout slotGrid = new LinearLayout(context);
        slotGrid.setOrientation(LinearLayout.VERTICAL);
        slotScroll.addView(slotGrid);

        // Build slot buttons in 2-column grid
        Map<String, String> equipped = getSavedEquipment();
        Button[] slotButtons = new Button[EquipmentSlot.SLOT_COUNT];
        LinearLayout currentRow = null;

        for (int i = 0; i < EquipmentSlot.SLOT_COUNT; i++) {
            if (i % SLOT_COLUMNS == 0) {
                currentRow = new LinearLayout(context);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rowParams.bottomMargin = 4;
                currentRow.setLayoutParams(rowParams);
                slotGrid.addView(currentRow);
            }

            final int slotIndex = i;
            Button slotBtn = new Button(context);
            slotBtn.setTextSize(10);
            slotBtn.setPadding(8, 8, 8, 8);
            slotBtn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams slotBtnP = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            slotBtnP.setMargins(2, 0, 2, 0);
            slotBtn.setLayoutParams(slotBtnP);

            updateSlotButtonText(slotBtn, slotIndex, equipped);

            slotBtn.setOnClickListener(v -> {
                showItemPicker(context, slotIndex, dialog, previewView, slotButtons, listener);
            });

            slotButtons[i] = slotBtn;
            currentRow.addView(slotBtn);
        }

        root.addView(slotScroll);

        // --- Clear All button ---
        Button clearBtn = new Button(context);
        clearBtn.setText("Clear All Equipment");
        clearBtn.setTextColor(Color.parseColor("#AA6666"));
        clearBtn.setTextSize(11);
        clearBtn.setBackgroundColor(Color.parseColor("#28233A"));
        clearBtn.setPadding(16, 8, 16, 8);
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        clearParams.topMargin = 4;
        clearBtn.setLayoutParams(clearParams);
        clearBtn.setOnClickListener(v -> {
            clearAllEquipment();
            Map<String, String> clearedEquipped = getSavedEquipment();
            for (int i = 0; i < slotButtons.length; i++) {
                updateSlotButtonText(slotButtons[i], i, clearedEquipped);
            }
            refreshPreview(previewView);
            if (listener != null) listener.onPreviewChanged();
        });
        root.addView(clearBtn);

        // --- Close button ---
        Button closeBtn = new Button(context);
        closeBtn.setText("Close");
        closeBtn.setTextColor(Color.parseColor("#FFD700"));
        closeBtn.setTextSize(14);
        closeBtn.setBackgroundColor(Color.parseColor("#28233A"));
        closeBtn.setPadding(32, 12, 32, 12);
        closeBtn.setOnClickListener(v -> dialog.dismiss());
        LinearLayout.LayoutParams closeBtnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        closeBtnParams.topMargin = 8;
        closeBtn.setLayoutParams(closeBtnParams);
        root.addView(closeBtn);

        dialog.setContentView(root);

        if (dialog.getWindow() != null) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            dialog.getWindow().setLayout((int) (size.x * 0.92), (int) (size.y * 0.88));
        }

        // Initial preview render
        refreshPreview(previewView);

        dialog.show();
    }

    // ==================== Preview Compositing ====================

    /**
     * Refreshes the preview by recompositing all layers.
     */
    private static void refreshPreview(ClothingPreviewView previewView) {
        int avatarIdx = getSavedAvatarIndex();
        Bitmap[] baseWalk = AvatarRegistry.getWalkFrames(avatarIdx);
        Bitmap baseIdle = AvatarRegistry.getIdleFrame(avatarIdx);

        Map<String, String> equipped = getSavedEquipment();

        // Build equipment overlay maps
        Map<Integer, Bitmap[]> slotWalkFrames = new HashMap<>();
        Map<Integer, Bitmap> slotIdleFrames = new HashMap<>();

        for (int slot = 0; slot < EquipmentSlot.SLOT_COUNT; slot++) {
            String slotName = EquipmentSlot.getSlotName(slot);
            String itemId = equipped.get(slotName);
            if (itemId == null || itemId.isEmpty()) continue;

            Item item = ItemRegistry.getTemplate(itemId);
            if (item == null) continue;

            // Generate placeholder overlay colored by rarity
            int color = SpriteLayerCompositor.getColorForRarity(item.getRarity().ordinal());
            Bitmap[] allFrames = SpriteLayerCompositor.generatePlaceholderEquipment(slot, color);

            slotWalkFrames.put(slot, SpriteLayerCompositor.getWalkFrames(allFrames));
            slotIdleFrames.put(slot, SpriteLayerCompositor.getIdleFrame(allFrames));
        }

        // Composite
        Bitmap[] compositedWalk = SpriteLayerCompositor.compositeWalkAnimation(baseWalk, slotWalkFrames);
        Bitmap compositedIdle = SpriteLayerCompositor.compositeIdle(baseIdle, slotIdleFrames);

        previewView.setFrames(compositedWalk, compositedIdle);
    }

    // ==================== Item Picker ====================

    /**
     * Shows a sub-dialog for selecting an item for a specific equipment slot.
     */
    private static void showItemPicker(Context context, int slot, Dialog parentDialog,
                                        ClothingPreviewView previewView,
                                        Button[] slotButtons,
                                        OnPreviewChangedListener listener) {
        // Get items from vault that match this slot
        List<SaveData.VaultItem> vaultItems = null;
        if (SaveManager.getInstance() != null && SaveManager.getInstance().getData() != null) {
            vaultItems = SaveManager.getInstance().getData().vaultItems;
        }
        List<String> matchingIds = EquipmentSlot.getItemIdsForSlot(slot, vaultItems);

        Dialog picker = new Dialog(context);
        picker.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (picker.getWindow() != null) {
            picker.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        LinearLayout pickerRoot = new LinearLayout(context);
        pickerRoot.setOrientation(LinearLayout.VERTICAL);
        pickerRoot.setBackgroundColor(Color.parseColor("#221E30"));
        pickerRoot.setPadding(24, 16, 24, 16);

        android.graphics.drawable.GradientDrawable pickerBg = new android.graphics.drawable.GradientDrawable();
        pickerBg.setColor(Color.parseColor("#221E30"));
        pickerBg.setCornerRadius(16);
        pickerBg.setStroke(2, Color.parseColor("#B8A9D4"));
        pickerRoot.setBackground(pickerBg);

        // Title
        TextView pickerTitle = new TextView(context);
        pickerTitle.setText("Select " + EquipmentSlot.getDisplayName(slot));
        pickerTitle.setTextColor(Color.parseColor("#FFD700"));
        pickerTitle.setTextSize(16);
        pickerTitle.setTypeface(null, Typeface.BOLD);
        pickerTitle.setGravity(Gravity.CENTER);
        pickerTitle.setPadding(0, 0, 0, 12);
        pickerRoot.addView(pickerTitle);

        // Scrollable item list
        ScrollView itemScroll = new ScrollView(context);
        LinearLayout.LayoutParams itemScrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
        itemScroll.setLayoutParams(itemScrollParams);

        LinearLayout itemList = new LinearLayout(context);
        itemList.setOrientation(LinearLayout.VERTICAL);
        itemScroll.addView(itemList);

        // "None" option to unequip
        Button noneBtn = new Button(context);
        noneBtn.setText("-- None (Unequip) --");
        noneBtn.setTextColor(Color.parseColor("#888888"));
        noneBtn.setTextSize(12);
        noneBtn.setBackgroundColor(Color.parseColor("#2A2540"));
        noneBtn.setPadding(16, 12, 16, 12);
        LinearLayout.LayoutParams noneBtnP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        noneBtnP.bottomMargin = 4;
        noneBtn.setLayoutParams(noneBtnP);
        noneBtn.setOnClickListener(v -> {
            equipItem(slot, null);
            Map<String, String> equipped = getSavedEquipment();
            for (int i = 0; i < slotButtons.length; i++) {
                updateSlotButtonText(slotButtons[i], i, equipped);
            }
            refreshPreview(previewView);
            if (listener != null) listener.onPreviewChanged();
            picker.dismiss();
        });
        itemList.addView(noneBtn);

        // Item buttons
        if (matchingIds.isEmpty()) {
            TextView noItems = new TextView(context);
            noItems.setText("No matching items in your vault.\nOpen chests to find equipment!");
            noItems.setTextColor(Color.parseColor("#666666"));
            noItems.setTextSize(11);
            noItems.setGravity(Gravity.CENTER);
            noItems.setPadding(16, 24, 16, 24);
            itemList.addView(noItems);
        } else {
            for (String itemId : matchingIds) {
                Item item = ItemRegistry.getTemplate(itemId);
                if (item == null) continue;

                Button itemBtn = new Button(context);
                String rarityName = item.getRarity().getDisplayName();
                itemBtn.setText(item.getName() + " (" + rarityName + ")");
                itemBtn.setTextColor(Item.getRarityColor(item.getRarity().ordinal()));
                itemBtn.setTextSize(12);
                itemBtn.setBackgroundColor(Color.parseColor("#2A2540"));
                itemBtn.setPadding(16, 12, 16, 12);
                itemBtn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams itemBtnP = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                itemBtnP.bottomMargin = 4;
                itemBtn.setLayoutParams(itemBtnP);

                itemBtn.setOnClickListener(v -> {
                    equipItem(slot, itemId);
                    Map<String, String> equipped = getSavedEquipment();
                    for (int i = 0; i < slotButtons.length; i++) {
                        updateSlotButtonText(slotButtons[i], i, equipped);
                    }
                    refreshPreview(previewView);
                    if (listener != null) listener.onPreviewChanged();
                    picker.dismiss();
                });

                itemList.addView(itemBtn);
            }
        }

        pickerRoot.addView(itemScroll);

        // Cancel button
        Button cancelBtn = new Button(context);
        cancelBtn.setText("Cancel");
        cancelBtn.setTextColor(Color.parseColor("#B8A9D4"));
        cancelBtn.setTextSize(12);
        cancelBtn.setBackgroundColor(Color.parseColor("#28233A"));
        cancelBtn.setPadding(24, 10, 24, 10);
        cancelBtn.setOnClickListener(v -> picker.dismiss());
        LinearLayout.LayoutParams cancelP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cancelP.topMargin = 8;
        cancelBtn.setLayoutParams(cancelP);
        pickerRoot.addView(cancelBtn);

        picker.setContentView(pickerRoot);

        if (picker.getWindow() != null) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            picker.getWindow().setLayout((int) (size.x * 0.8), (int) (size.y * 0.6));
        }

        picker.show();
    }

    // ==================== Save/Load Helpers ====================

    private static int getSavedAvatarIndex() {
        if (SaveManager.getInstance() != null && SaveManager.getInstance().getData() != null) {
            return SaveManager.getInstance().getData().selectedAvatarIndex;
        }
        return 0;
    }

    private static void saveAvatarIndex(int index) {
        if (SaveManager.getInstance() != null && SaveManager.getInstance().getData() != null) {
            SaveManager.getInstance().getData().selectedAvatarIndex = index;
            SaveManager.getInstance().save();
        }
    }

    private static Map<String, String> getSavedEquipment() {
        if (SaveManager.getInstance() != null && SaveManager.getInstance().getData() != null) {
            Map<String, String> eq = SaveManager.getInstance().getData().equippedCosmetics;
            if (eq != null) return eq;
        }
        return new HashMap<>();
    }

    private static void equipItem(int slot, String itemId) {
        if (SaveManager.getInstance() != null && SaveManager.getInstance().getData() != null) {
            Map<String, String> eq = SaveManager.getInstance().getData().equippedCosmetics;
            if (eq == null) {
                eq = new HashMap<>();
                SaveManager.getInstance().getData().equippedCosmetics = eq;
            }
            String slotName = EquipmentSlot.getSlotName(slot);
            if (itemId == null || itemId.isEmpty()) {
                eq.remove(slotName);
            } else {
                eq.put(slotName, itemId);
            }
            SaveManager.getInstance().save();
        }
    }

    private static void clearAllEquipment() {
        if (SaveManager.getInstance() != null && SaveManager.getInstance().getData() != null) {
            SaveManager.getInstance().getData().equippedCosmetics = new HashMap<>();
            SaveManager.getInstance().save();
        }
    }

    // ==================== UI Helpers ====================

    private static void updateSlotButtonText(Button btn, int slot, Map<String, String> equipped) {
        String slotName = EquipmentSlot.getSlotName(slot);
        String itemId = equipped.get(slotName);

        if (itemId != null && !itemId.isEmpty()) {
            Item item = ItemRegistry.getTemplate(itemId);
            String itemName = (item != null) ? item.getName() : itemId;
            btn.setText(EquipmentSlot.getDisplayName(slot) + "\n" + itemName);
            int rarityColor = (item != null)
                    ? Item.getRarityColor(item.getRarity().ordinal())
                    : Color.WHITE;
            btn.setTextColor(rarityColor);
            btn.setBackgroundColor(Color.parseColor("#2A2540"));
        } else {
            btn.setText(EquipmentSlot.getDisplayName(slot) + "\n(Empty)");
            btn.setTextColor(Color.parseColor("#666666"));
            btn.setBackgroundColor(Color.parseColor("#201C2E"));
        }
    }

    private static Button createStyledButton(Context context, String text, boolean active) {
        Button btn = new Button(context);
        btn.setText(text);
        btn.setTextSize(11);
        btn.setPadding(20, 6, 20, 6);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(4, 0, 4, 0);
        btn.setLayoutParams(p);
        styleToggleButton(btn, active);
        return btn;
    }

    private static void styleToggleButton(Button btn, boolean active) {
        if (active) {
            btn.setTextColor(Color.parseColor("#FFD700"));
            btn.setBackgroundColor(Color.parseColor("#3A3050"));
        } else {
            btn.setTextColor(Color.parseColor("#888888"));
            btn.setBackgroundColor(Color.parseColor("#201C2E"));
        }
    }

    private static void styleAvatarButton(Button btn, boolean selected) {
        if (selected) {
            btn.setTextColor(Color.parseColor("#FFD700"));
            btn.setBackgroundColor(Color.parseColor("#3A3050"));
        } else {
            btn.setTextColor(Color.parseColor("#B8A9D4"));
            btn.setBackgroundColor(Color.parseColor("#201C2E"));
        }
    }

    private static View createDivider(Context context) {
        View divider = new View(context);
        divider.setBackgroundColor(Color.parseColor("#3A3050"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2);
        params.topMargin = 8;
        params.bottomMargin = 4;
        divider.setLayoutParams(params);
        return divider;
    }

    // ==================== Static Preview for Cosmetics Popup ====================

    /**
     * Generates a static preview bitmap of the current character configuration.
     * Used as a thumbnail in the CosmeticsPopup.
     *
     * @param displaySize The desired output size in pixels (e.g., 128 for 128x128)
     * @return Upscaled composited idle frame
     */
    public static Bitmap generateStaticPreview(int displaySize) {
        int avatarIdx = getSavedAvatarIndex();
        Bitmap baseIdle = AvatarRegistry.getIdleFrame(avatarIdx);

        Map<String, String> equipped = getSavedEquipment();
        Map<Integer, Bitmap> slotIdleFrames = new HashMap<>();

        for (int slot = 0; slot < EquipmentSlot.SLOT_COUNT; slot++) {
            String slotName = EquipmentSlot.getSlotName(slot);
            String itemId = equipped.get(slotName);
            if (itemId == null || itemId.isEmpty()) continue;

            Item item = ItemRegistry.getTemplate(itemId);
            if (item == null) continue;

            int color = SpriteLayerCompositor.getColorForRarity(item.getRarity().ordinal());
            Bitmap[] allFrames = SpriteLayerCompositor.generatePlaceholderEquipment(slot, color);
            slotIdleFrames.put(slot, SpriteLayerCompositor.getIdleFrame(allFrames));
        }

        Bitmap idle = SpriteLayerCompositor.compositeIdle(baseIdle, slotIdleFrames);

        // Upscale with nearest-neighbor
        Bitmap upscaled = Bitmap.createBitmap(displaySize, displaySize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(upscaled);
        Paint paint = new Paint();
        paint.setFilterBitmap(false); // nearest-neighbor
        paint.setAntiAlias(false);
        Rect src = new Rect(0, 0, idle.getWidth(), idle.getHeight());
        Rect dst = new Rect(0, 0, displaySize, displaySize);
        canvas.drawBitmap(idle, src, dst, paint);

        return upscaled;
    }
}
