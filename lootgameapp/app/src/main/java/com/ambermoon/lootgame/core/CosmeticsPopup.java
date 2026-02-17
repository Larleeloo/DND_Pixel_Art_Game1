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

/**
 * Cosmetics settings popup accessible from the header bar.
 * Allows users to select backgrounds and (future) view clothing mockups.
 */
public class CosmeticsPopup {

    private static final int GRID_COLUMNS = 3;
    // Scale factor for the 32x64 pixel art thumbnails in the grid
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

        // Rounded corners via a GradientDrawable
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
        title.setPadding(0, 0, 0, 16);
        root.addView(title);

        // Divider
        View divider = new View(context);
        divider.setBackgroundColor(Color.parseColor("#3A3050"));
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2);
        divParams.bottomMargin = 16;
        divider.setLayoutParams(divParams);
        root.addView(divider);

        // --- Backgrounds Section ---
        TextView bgSectionTitle = new TextView(context);
        bgSectionTitle.setText("Backgrounds");
        bgSectionTitle.setTextColor(Color.parseColor("#B8A9D4"));
        bgSectionTitle.setTextSize(14);
        bgSectionTitle.setTypeface(null, Typeface.BOLD);
        bgSectionTitle.setPadding(0, 0, 0, 12);
        root.addView(bgSectionTitle);

        // Scrollable grid of backgrounds
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

        List<BackgroundEntry> allBackgrounds = BackgroundRegistry.getAll();
        buildBackgroundGrid(context, gridContainer, allBackgrounds, selectedId, dialog, listener);

        root.addView(scrollView);

        // Divider before clothing section
        View divider2 = new View(context);
        divider2.setBackgroundColor(Color.parseColor("#3A3050"));
        LinearLayout.LayoutParams div2Params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2);
        div2Params.topMargin = 16;
        div2Params.bottomMargin = 16;
        divider2.setLayoutParams(div2Params);
        root.addView(divider2);

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

        // Size the dialog to most of the screen
        if (dialog.getWindow() != null) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            dialog.getWindow().setLayout((int) (size.x * 0.9), (int) (size.y * 0.8));
        }

        dialog.show();
    }

    private static void buildBackgroundGrid(Context context, LinearLayout gridContainer,
                                             List<BackgroundEntry> backgrounds,
                                             String selectedId, Dialog dialog,
                                             OnBackgroundChangedListener listener) {
        LinearLayout currentRow = null;
        int colIndex = 0;

        for (int i = 0; i < backgrounds.size(); i++) {
            BackgroundEntry entry = backgrounds.get(i);

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

            // Cell for each background
            LinearLayout cell = new LinearLayout(context);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER_HORIZONTAL);
            cell.setPadding(8, 8, 8, 8);
            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            cell.setLayoutParams(cellParams);

            // Thumbnail view
            View thumbnailView = createThumbnailView(context, entry, entry.id.equals(selectedId));
            cell.addView(thumbnailView);

            // Label
            TextView label = new TextView(context);
            label.setText(entry.displayName);
            label.setTextColor(entry.id.equals(selectedId)
                    ? Color.parseColor("#FFD700") : Color.parseColor("#B8A9D4"));
            label.setTextSize(10);
            label.setGravity(Gravity.CENTER);
            label.setPadding(0, 6, 0, 0);
            label.setMaxLines(1);
            cell.addView(label);

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

            currentRow.addView(cell);
            colIndex++;

            if (colIndex >= GRID_COLUMNS) {
                colIndex = 0;
            }
        }

        // Fill remaining cells in last row with empty spacers
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

    private static View createThumbnailView(Context context, BackgroundEntry entry, boolean isSelected) {
        // Custom view that draws the background thumbnail
        View view = new View(context) {
            private final Paint paint = new Paint();
            private final Paint borderPaint = new Paint();

            {
                paint.setFilterBitmap(false); // nearest-neighbor for pixel art
                paint.setAntiAlias(false);
                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setStrokeWidth(isSelected ? 4 : 2);
                borderPaint.setColor(isSelected
                        ? Color.parseColor("#FFD700") : Color.parseColor("#3A3050"));
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                int w = getWidth();
                int h = getHeight();

                if (entry.isBuiltIn) {
                    // Draw solid color
                    paint.setColor(entry.solidColor);
                    canvas.drawRect(0, 0, w, h, paint);
                } else {
                    // Draw tiled pixel art background
                    Bitmap bmp = entry.getBitmap();
                    if (bmp != null) {
                        // Scale and tile to fill the thumbnail
                        Rect src = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
                        Rect dst = new Rect(0, 0, w, h);
                        canvas.drawBitmap(bmp, src, dst, paint);
                    } else {
                        // Fallback: dark gray
                        paint.setColor(Color.parseColor("#222222"));
                        canvas.drawRect(0, 0, w, h, paint);
                    }
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
