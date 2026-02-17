package com.ambermoon.lootgame.core;

import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import com.ambermoon.lootgame.audio.HapticManager;
import com.ambermoon.lootgame.graphics.AssetLoader;
import com.ambermoon.lootgame.graphics.BackgroundRegistry;
import com.ambermoon.lootgame.save.SaveManager;
import com.ambermoon.lootgame.tabs.*;

import java.util.ArrayList;
import java.util.List;

public class TabActivity extends Activity {
    private FrameLayout contentFrame;
    private LinearLayout tabBar;
    private TextView coinDisplay;
    private TextView usernameDisplay;
    private View currentTab;
    private int currentTabIndex = 0;
    private BackgroundTileView backgroundView;

    private String[] tabLabels;
    private Button[] tabButtons;
    private boolean isLars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize haptic feedback
        HapticManager.getInstance().init(this);

        // Initialize background registry
        BackgroundRegistry.initialize();

        // Determine if the current user is Lars (case-insensitive)
        String username = GamePreferences.getUsername();
        isLars = "lars".equalsIgnoreCase(username.trim());

        // Build tab list dynamically
        List<String> labels = new ArrayList<>();
        labels.add("Daily");
        labels.add("Monthly");
        labels.add("Alchemy");
        labels.add("Decon");
        labels.add("Slots");
        labels.add("Vault");
        labels.add("Shop");
        if (isLars) {
            labels.add("Edit");
        }
        tabLabels = labels.toArray(new String[0]);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#1A1525"));

        // Header bar
        RelativeLayout header = new RelativeLayout(this);
        header.setBackgroundColor(Color.parseColor("#1E1830"));
        header.setPadding(24, 16, 24, 16);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        header.setLayoutParams(headerParams);

        coinDisplay = new TextView(this);
        coinDisplay.setTextColor(Color.parseColor("#FFD700"));
        coinDisplay.setTextSize(18);
        updateCoinDisplay();
        RelativeLayout.LayoutParams coinParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        coinParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        coinParams.addRule(RelativeLayout.CENTER_VERTICAL);
        coinDisplay.setLayoutParams(coinParams);
        header.addView(coinDisplay);

        // Username display (centered)
        usernameDisplay = new TextView(this);
        usernameDisplay.setTextColor(Color.parseColor("#B8A9D4"));
        usernameDisplay.setTextSize(14);
        usernameDisplay.setText(username.isEmpty() ? "" : username);
        RelativeLayout.LayoutParams userParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        userParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        usernameDisplay.setLayoutParams(userParams);
        header.addView(usernameDisplay);

        // Cosmetics button (top right)
        TextView cosmeticsBtn = new TextView(this);
        cosmeticsBtn.setText("\u2728"); // sparkles unicode
        cosmeticsBtn.setTextSize(20);
        cosmeticsBtn.setPadding(16, 4, 0, 4);
        cosmeticsBtn.setOnClickListener(v -> {
            HapticManager.getInstance().tap();
            CosmeticsPopup.show(this, this::applyBackground);
        });
        RelativeLayout.LayoutParams cosmeticsBtnParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        cosmeticsBtnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        cosmeticsBtnParams.addRule(RelativeLayout.CENTER_VERTICAL);
        cosmeticsBtn.setLayoutParams(cosmeticsBtnParams);
        header.addView(cosmeticsBtn);

        root.addView(header);

        // Content area with background support
        FrameLayout contentWrapper = new FrameLayout(this);
        LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
        contentWrapper.setLayoutParams(wrapperParams);

        // Background tiling view (behind content)
        backgroundView = new BackgroundTileView(this);
        contentWrapper.addView(backgroundView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Content frame (on top of background)
        contentFrame = new FrameLayout(this);
        contentWrapper.addView(contentFrame, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        root.addView(contentWrapper);

        // Tab bar
        tabBar = new LinearLayout(this);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setBackgroundColor(Color.parseColor("#0F0D17"));
        tabBar.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tabBarParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tabBar.setLayoutParams(tabBarParams);

        tabButtons = new Button[tabLabels.length];
        for (int i = 0; i < tabLabels.length; i++) {
            final int idx = i;
            Button btn = new Button(this);
            btn.setText(tabLabels[i]);
            btn.setTextColor(Color.parseColor("#888888"));
            btn.setTextSize(11);
            btn.setBackgroundColor(Color.TRANSPARENT);
            btn.setPadding(8, 16, 8, 16);
            LinearLayout.LayoutParams btnP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            btn.setLayoutParams(btnP);
            btn.setOnClickListener(v -> switchTab(idx));
            tabBar.addView(btn);
            tabButtons[i] = btn;
        }
        root.addView(tabBar);

        setContentView(root);

        // Apply saved background
        String savedBgId = "none";
        if (SaveManager.getInstance() != null && SaveManager.getInstance().getData() != null) {
            savedBgId = SaveManager.getInstance().getData().selectedBackgroundId;
        }
        applyBackground(savedBgId);

        switchTab(0);
    }

    public void switchTab(int index) {
        if (index != currentTabIndex) {
            HapticManager.getInstance().tap();
        }
        currentTabIndex = index;
        contentFrame.removeAllViews();

        // Update tab button colors
        for (int i = 0; i < tabButtons.length; i++) {
            tabButtons[i].setTextColor(i == index ? Color.parseColor("#FFD700") : Color.parseColor("#888888"));
        }

        View tabView;
        switch (index) {
            case 0: tabView = new DailyChestTab(this); break;
            case 1: tabView = new MonthlyChestTab(this); break;
            case 2: tabView = new AlchemyTab(this); break;
            case 3: tabView = new DeconstructTab(this); break;
            case 4: tabView = new SlotMachineTab(this); break;
            case 5: tabView = new VaultTab(this); break;
            case 6: tabView = new ShopTab(this); break;
            case 7:
                if (isLars) {
                    tabView = new ShopEditorTab(this);
                } else {
                    tabView = new DailyChestTab(this);
                }
                break;
            default: tabView = new DailyChestTab(this); break;
        }
        currentTab = tabView;
        contentFrame.addView(tabView);
        updateCoinDisplay();
    }

    private void applyBackground(String backgroundId) {
        BackgroundRegistry.BackgroundEntry entry = BackgroundRegistry.get(backgroundId);
        if (backgroundView != null) {
            backgroundView.setBackgroundEntry(entry);
        }
    }

    public void updateCoinDisplay() {
        if (coinDisplay != null && SaveManager.getInstance() != null) {
            coinDisplay.setText("\u25C8 " + SaveManager.getInstance().getData().coins + " coins");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SaveManager.getInstance() != null) {
            // save() handles both local save and cloud upload
            SaveManager.getInstance().save();
        }
    }

    /**
     * Custom view that draws a background image scaled to fill the entire content area.
     * Uses center-crop scaling with nearest-neighbor filtering for crisp pixel art.
     * Supports animated GIF playback with a frame timer.
     */
    private static class BackgroundTileView extends View {
        private BackgroundRegistry.BackgroundEntry entry;
        private AssetLoader.ImageAsset imageAsset;
        private final Paint paint = new Paint();
        private final Paint bitmapPaint = new Paint();
        private long animStartTime;
        private boolean animating = false;
        private final Runnable frameTickRunnable = () -> {
            if (animating) {
                invalidate();
                scheduleNextFrame();
            }
        };

        public BackgroundTileView(android.content.Context context) {
            super(context);
            paint.setAntiAlias(false);
            bitmapPaint.setAntiAlias(false);
            bitmapPaint.setFilterBitmap(false); // nearest-neighbor for pixel art
        }

        public void setBackgroundEntry(BackgroundRegistry.BackgroundEntry entry) {
            this.entry = entry;
            stopAnimation();
            if (entry != null && !entry.isSolidColor) {
                imageAsset = entry.getImageAsset();
                if (imageAsset != null && imageAsset.isAnimated) {
                    startAnimation();
                }
            } else {
                imageAsset = null;
            }
            invalidate();
        }

        private void startAnimation() {
            animStartTime = System.currentTimeMillis();
            animating = true;
            scheduleNextFrame();
        }

        private void stopAnimation() {
            animating = false;
            removeCallbacks(frameTickRunnable);
        }

        private void scheduleNextFrame() {
            // Redraw at ~30fps for smooth GIF playback
            postDelayed(frameTickRunnable, 33);
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (imageAsset != null && imageAsset.isAnimated && !animating) {
                startAnimation();
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            stopAnimation();
            super.onDetachedFromWindow();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int viewW = getWidth();
            int viewH = getHeight();

            if (entry == null) {
                paint.setColor(Color.parseColor("#1A1525"));
                canvas.drawRect(0, 0, viewW, viewH, paint);
                return;
            }

            if (entry.isSolidColor) {
                paint.setColor(entry.solidColor);
                canvas.drawRect(0, 0, viewW, viewH, paint);
                return;
            }

            // Get the current frame (animated or static)
            Bitmap bmp;
            if (imageAsset != null && imageAsset.isAnimated) {
                long elapsed = System.currentTimeMillis() - animStartTime;
                bmp = imageAsset.getFrame(elapsed);
            } else if (imageAsset != null) {
                bmp = imageAsset.bitmap;
            } else {
                bmp = entry.getBitmap();
            }

            if (bmp == null) {
                paint.setColor(Color.parseColor("#1A1525"));
                canvas.drawRect(0, 0, viewW, viewH, paint);
                return;
            }

            // Scale to cover the entire view (center-crop) with integer scaling
            // for crisp pixel art. The 32x64 image fills the full screen.
            int imgW = bmp.getWidth();
            int imgH = bmp.getHeight();
            float scaleX = (float) viewW / imgW;
            float scaleY = (float) viewH / imgH;
            float scale = Math.max(scaleX, scaleY);

            int scaledW = Math.round(imgW * scale);
            int scaledH = Math.round(imgH * scale);
            int offsetX = (viewW - scaledW) / 2;
            int offsetY = (viewH - scaledH) / 2;

            Rect src = new Rect(0, 0, imgW, imgH);
            Rect dst = new Rect(offsetX, offsetY, offsetX + scaledW, offsetY + scaledH);
            canvas.drawBitmap(bmp, src, dst, bitmapPaint);
        }
    }
}
