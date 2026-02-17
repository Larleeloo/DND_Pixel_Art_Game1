package com.ambermoon.lootgame.core;

import android.app.Dialog;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.*;

import com.ambermoon.lootgame.graphics.BackgroundRegistry;
import com.ambermoon.lootgame.graphics.BackgroundRegistry.BackgroundEntry;
import com.ambermoon.lootgame.save.SaveManager;

import java.util.List;
import java.util.Set;

/**
 * Cosmetics settings popup accessible from the header bar.
 * Allows users to select unlocked backgrounds and view locked ones.
 */
public class CosmeticsPopup {

    private static final int GRID_COLUMNS = 3;
    private static final int THUMBNAIL_SCALE = 3;
    private static final int THUMBNAIL_WIDTH = BackgroundRegistry.BG_PIXEL_WIDTH * THUMBNAIL_SCALE;   // 96
    private static final int THUMBNAIL_HEIGHT = BackgroundRegistry.BG_PIXEL_HEIGHT * THUMBNAIL_SCALE;  // 192

    public interface OnBackgroundChangedListener {
        void onBackgroundChanged(String backgroundId);
    }

    public static void show(Context context, OnBackgroundChangedListener listener) {
        BackgroundRegistry.initialize();

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Root container
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#1E1830"));
        root.setPadding(32, 24, 32, 24);

        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.parseColor("#1E1830"));
        bg.setCornerRadius(24);
        bg.setStroke(2, Color.parseColor("#FFD700"));
        root.setBackground(bg);

        // Title
        TextView title = new TextView(context);
        title.setText("Cosmetics");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 8);
        root.addView(title);

        // Unlock count
        Set<String> unlockedIds = getUnlockedIds();
        List<BackgroundEntry> allBackgrounds = BackgroundRegistry.getAll();
        int totalUnlockable = 0;
        int totalUnlocked = 0;
        for (BackgroundEntry entry : allBackgrounds) {
            if (!entry.alwaysUnlocked) {
                totalUnlockable++;
                if (unlockedIds.contains(entry.id)) totalUnlocked++;
            }
        }
        TextView unlockCount = new TextView(context);
        unlockCount.setText(totalUnlocked + " / " + totalUnlockable + " unlocked");
        unlockCount.setTextColor(Color.parseColor("#888888"));
        unlockCount.setTextSize(11);
        unlockCount.setGravity(Gravity.CENTER);
        unlockCount.setPadding(0, 0, 0, 16);
        root.addView(unlockCount);

        // Divider
        root.addView(createDivider(context, 0, 16));

        // --- Backgrounds Section ---
        TextView bgSectionTitle = new TextView(context);
        bgSectionTitle.setText("Backgrounds");
        bgSectionTitle.setTextColor(Color.parseColor("#B8A9D4"));
        bgSectionTitle.setTextSize(14);
        bgSectionTitle.setTypeface(null, Typeface.BOLD);
        bgSectionTitle.setPadding(0, 0, 0, 4);
        root.addView(bgSectionTitle);

        TextView bgHint = new TextView(context);
        bgHint.setText("Open chests to unlock new backgrounds!");
        bgHint.setTextColor(Color.parseColor("#666666"));
        bgHint.setTextSize(10);
        bgHint.setPadding(0, 0, 0, 12);
        root.addView(bgHint);

        // Scrollable grid
        ScrollView scrollView = new ScrollView(context);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
        scrollView.setLayoutParams(scrollParams);

        LinearLayout gridContainer = new LinearLayout(context);
        gridContainer.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(gridContainer);

        String selectedId = "none";
        if (SaveManager.getInstance() != null && SaveManager.getInstance().getData() != null) {
            selectedId = SaveManager.getInstance().getData().selectedBackgroundId;
        }

        buildBackgroundGrid(context, gridContainer, allBackgrounds, selectedId, unlockedIds, dialog, listener);

        root.addView(scrollView);

        // Divider before clothing section
        root.addView(createDivider(context, 16, 16));

        // --- Clothing Section (placeholder) ---
        TextView clothingSectionTitle = new TextView(context);
        clothingSectionTitle.setText("Clothing Mockups");
        clothingSectionTitle.setTextColor(Color.parseColor("#B8A9D4"));
        clothingSectionTitle.setTextSize(14);
        clothingSectionTitle.setTypeface(null, Typeface.BOLD);
        clothingSectionTitle.setPadding(0, 0, 0, 8);
        root.addView(clothingSectionTitle);

        TextView comingSoon = new TextView(context);
        comingSoon.setText("Coming soon...");
        comingSoon.setTextColor(Color.parseColor("#666666"));
        comingSoon.setTextSize(12);
        comingSoon.setPadding(0, 0, 0, 16);
        root.addView(comingSoon);

        // Close button
        Button closeBtn = new Button(context);
        closeBtn.setText("Close");
        closeBtn.setTextColor(Color.parseColor("#FFD700"));
        closeBtn.setTextSize(14);
        closeBtn.setBackgroundColor(Color.parseColor("#28233A"));
        closeBtn.setPadding(32, 16, 32, 16);
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
            dialog.getWindow().setLayout((int) (size.x * 0.9), (int) (size.y * 0.8));
        }

        dialog.show();
    }

    private static Set<String> getUnlockedIds() {
        // Lars gets all backgrounds unlocked automatically
        String username = GamePreferences.getUsername();
        if ("lars".equalsIgnoreCase(username.trim())) {
            Set<String> allIds = new java.util.HashSet<>();
            for (BackgroundEntry entry : BackgroundRegistry.getAll()) {
                allIds.add(entry.id);
            }
            return allIds;
        }

        if (SaveManager.getInstance() != null) {
            return SaveManager.getInstance().getUnlockedBackgroundIds();
        }
        return new java.util.HashSet<>();
    }

    private static View createDivider(Context context, int topMargin, int bottomMargin) {
        View divider = new View(context);
        divider.setBackgroundColor(Color.parseColor("#3A3050"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2);
        params.topMargin = topMargin;
        params.bottomMargin = bottomMargin;
        divider.setLayoutParams(params);
        return divider;
    }

    private static void buildBackgroundGrid(Context context, LinearLayout gridContainer,
                                             List<BackgroundEntry> backgrounds,
                                             String selectedId, Set<String> unlockedIds,
                                             Dialog dialog,
                                             OnBackgroundChangedListener listener) {
        LinearLayout currentRow = null;
        int colIndex = 0;

        for (int i = 0; i < backgrounds.size(); i++) {
            BackgroundEntry entry = backgrounds.get(i);
            boolean isUnlocked = entry.alwaysUnlocked || unlockedIds.contains(entry.id);
            boolean isSelected = entry.id.equals(selectedId);

            if (colIndex == 0) {
                currentRow = new LinearLayout(context);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setGravity(Gravity.CENTER_HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rowParams.bottomMargin = 12;
                currentRow.setLayoutParams(rowParams);
                gridContainer.addView(currentRow);
            }

            // Cell
            LinearLayout cell = new LinearLayout(context);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER_HORIZONTAL);
            cell.setPadding(8, 8, 8, 8);
            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            cell.setLayoutParams(cellParams);

            // Thumbnail
            View thumbnailView = createThumbnailView(context, entry, isSelected, isUnlocked);
            cell.addView(thumbnailView);

            // Name label (rarity-colored if unlocked, gray if locked)
            TextView label = new TextView(context);
            if (isUnlocked) {
                label.setText(entry.displayName);
                label.setTextColor(isSelected
                        ? Color.parseColor("#FFD700")
                        : BackgroundRegistry.getRarityColor(entry.rarity));
            } else {
                label.setText("\uD83D\uDD12 ???"); // lock emoji + ???
                label.setTextColor(Color.parseColor("#555555"));
            }
            label.setTextSize(10);
            label.setGravity(Gravity.CENTER);
            label.setPadding(0, 4, 0, 0);
            label.setMaxLines(1);
            cell.addView(label);

            // Rarity label below name
            TextView rarityLabel = new TextView(context);
            rarityLabel.setText(BackgroundRegistry.getRarityName(entry.rarity));
            rarityLabel.setTextColor(isUnlocked
                    ? BackgroundRegistry.getRarityColor(entry.rarity)
                    : Color.parseColor("#444444"));
            rarityLabel.setTextSize(8);
            rarityLabel.setGravity(Gravity.CENTER);
            rarityLabel.setPadding(0, 2, 0, 0);
            cell.addView(rarityLabel);

            if (isUnlocked) {
                cell.setOnClickListener(v -> {
                    if (SaveManager.getInstance() != null) {
                        SaveManager.getInstance().getData().selectedBackgroundId = entry.id;
                        SaveManager.getInstance().save();
                    }
                    if (listener != null) {
                        listener.onBackgroundChanged(entry.id);
                    }
                    dialog.dismiss();
                });
            }
            // Locked cells are not clickable

            currentRow.addView(cell);
            colIndex++;

            if (colIndex >= GRID_COLUMNS) {
                colIndex = 0;
            }
        }

        // Fill remaining cells in last row
        if (colIndex > 0 && currentRow != null) {
            while (colIndex < GRID_COLUMNS) {
                View spacer = new View(context);
                LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                        0, 1, 1.0f);
                spacer.setLayoutParams(spacerParams);
                currentRow.addView(spacer);
                colIndex++;
            }
        }
    }

    private static View createThumbnailView(Context context, BackgroundEntry entry,
                                             boolean isSelected, boolean isUnlocked) {
        View view = new View(context) {
            private final Paint paint = new Paint();
            private final Paint borderPaint = new Paint();
            private final Paint lockOverlayPaint = new Paint();

            {
                paint.setFilterBitmap(false);
                paint.setAntiAlias(false);
                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setStrokeWidth(isSelected ? 4 : 2);
                if (isSelected) {
                    borderPaint.setColor(Color.parseColor("#FFD700"));
                } else if (isUnlocked) {
                    borderPaint.setColor(BackgroundRegistry.getRarityColor(entry.rarity));
                } else {
                    borderPaint.setColor(Color.parseColor("#2A2A2A"));
                }
                lockOverlayPaint.setColor(Color.argb(160, 0, 0, 0));
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                int w = getWidth();
                int h = getHeight();

                if (entry.isSolidColor) {
                    paint.setColor(entry.solidColor);
                    canvas.drawRect(0, 0, w, h, paint);
                } else {
                    Bitmap bmp = entry.getBitmap();
                    if (bmp != null) {
                        Rect src = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
                        Rect dst = new Rect(0, 0, w, h);
                        canvas.drawBitmap(bmp, src, dst, paint);
                    } else {
                        paint.setColor(Color.parseColor("#222222"));
                        canvas.drawRect(0, 0, w, h, paint);
                    }
                }

                // Darken overlay for locked backgrounds
                if (!isUnlocked) {
                    canvas.drawRect(0, 0, w, h, lockOverlayPaint);
                }

                // Border
                canvas.drawRect(1, 1, w - 1, h - 1, borderPaint);
            }
        };

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        view.setLayoutParams(params);
        return view;
    }
}
