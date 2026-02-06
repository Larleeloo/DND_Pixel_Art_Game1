package com.ambermoongame.entity.item;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.ambermoongame.entity.Entity;
import com.ambermoongame.graphics.AndroidAssetLoader;
import com.ambermoongame.input.TouchInputManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * LootChestEntity represents a treasure chest that can be opened to receive random loot.
 * Features opening animation via GIF playback, item drops with physics, and cooldown management.
 *
 * Conversion notes:
 * - AnimatedTexture              -> Manual frame management via ImageAsset.frames
 * - java.awt.Color               -> android.graphics.Color (int)
 * - java.awt.Color.getHSBColor() -> Color.HSVToColor(float[])
 * - Graphics/Graphics2D          -> Canvas + Paint
 * - BufferedImage                 -> Bitmap
 * - Rectangle                    -> Rect
 * - Font/FontMetrics             -> Paint.setTextSize()/measureText()
 * - InputManager                 -> TouchInputManager
 * - System.out/err.println       -> Log.d()/Log.e()
 * - SaveManager                  -> commented out (pending port)
 * - ControllerManager/VibrationPattern -> commented out (pending port)
 *
 * Animation System:
 * - Uses a single GIF per chest type with forward/reverse playback
 * - First frame = closed state, last frame = open state
 * - Play forward to open, animation completion triggers item drop
 *
 * Chest Types:
 * - DAILY: Opens once per day, drops 3 items
 * - MONTHLY: Opens once per month, drops 10 items with higher rarity chances
 */
public class LootChestEntity extends Entity {

    private static final String TAG = "LootChestEntity";

    // Chest type constants (replaces enum to avoid D8 crash)
    public static final int CHEST_TYPE_DAILY = 0;
    public static final int CHEST_TYPE_MONTHLY = 1;
    public static final int CHEST_TYPE_COUNT = 2;

    private static final int[] CHEST_ITEM_COUNTS = {3, 10};
    private static final float[] CHEST_RARITY_BOOSTS = {1.0f, 2.5f};
    private static final String[] CHEST_TEXTURE_PATHS = {
        "assets/chests/daily_chest",
        "assets/chests/monthly_chest"
    };

    public static int getChestItemCount(int type) {
        if (type >= 0 && type < CHEST_TYPE_COUNT) return CHEST_ITEM_COUNTS[type];
        return 3;
    }

    public static float getChestRarityBoost(int type) {
        if (type >= 0 && type < CHEST_TYPE_COUNT) return CHEST_RARITY_BOOSTS[type];
        return 1.0f;
    }

    public static String getChestTexturePath(int type) {
        if (type >= 0 && type < CHEST_TYPE_COUNT) return CHEST_TEXTURE_PATHS[type];
        return CHEST_TEXTURE_PATHS[0];
    }

    public static String getChestTypeName(int type) {
        switch (type) {
            case CHEST_TYPE_DAILY: return "DAILY";
            case CHEST_TYPE_MONTHLY: return "MONTHLY";
            default: return "UNKNOWN";
        }
    }

    // Helper class for backwards compatibility
    public static final class ChestType {
        private final int type;

        private ChestType(int type) { this.type = type; }

        public static ChestType fromInt(int type) { return new ChestType(type); }
        public int intValue() { return type; }
        public int getItemCount() { return getChestItemCount(type); }
        public float getRarityBoost() { return getChestRarityBoost(type); }
        public String getTextureBasePath() { return getChestTexturePath(type); }

        public static final ChestType DAILY = new ChestType(CHEST_TYPE_DAILY);
        public static final ChestType MONTHLY = new ChestType(CHEST_TYPE_MONTHLY);
    }

    private ChestType chestType;
    private int width = 64;
    private int height = 64;

    // Animation states
    private boolean isOpen = false;
    private boolean isOpening = false;
    private boolean isClosing = false;

    // Frame-based animation (replaces AnimatedTexture)
    private List<Bitmap> frames;
    private List<Integer> frameDelays;
    private int currentFrameIndex = 0;
    private boolean animPlaying = false;
    private boolean animForward = true;
    private long animAccumulator = 0;
    private long lastUpdateTime = System.currentTimeMillis();

    // Particle effects
    private List<Particle> particles;
    private long lastParticleTime = 0;

    // Dropped items
    private List<ItemEntity> droppedItems;
    private boolean hasDroppedItems = false;
    private int groundY;

    // Reference to entity list for block collision detection
    private List<Entity> entityList = null;

    // Visual properties
    private float glowIntensity = 0;
    private float glowPhase = 0;
    private boolean canOpen = false;

    // Interaction
    private boolean playerNearby = false;
    private Rect interactionZone;

    private static final Random random = new Random();

    // Reusable drawing objects
    private final Paint drawPaint = new Paint();
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF ovalRect = new RectF();
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();

    /**
     * Creates a new loot chest at the specified position.
     */
    public LootChestEntity(int x, int y, ChestType type, int groundLevel) {
        super(x, y);
        this.chestType = type;
        this.groundY = groundLevel;
        this.particles = new ArrayList<>();
        this.droppedItems = new ArrayList<>();
        this.interactionZone = new Rect(x - 50, y - 50, x + width + 50, y + height + 50);

        // Load textures
        loadTextures();

        // --- Uncomment when SaveManager is ported ---
        // SaveManager save = SaveManager.getInstance();
        // if (type == ChestType.DAILY) {
        //     canOpen = save.canOpenDailyChest();
        // } else {
        //     canOpen = save.canOpenMonthlyChest();
        // }
        canOpen = true; // Default to openable until SaveManager is ported
    }

    /**
     * Loads the GIF texture for opening animation.
     * Extracts frames from ImageAsset for manual playback control.
     */
    private void loadTextures() {
        String texturePath = chestType.textureBasePath + ".gif";

        try {
            AndroidAssetLoader.ImageAsset asset = AndroidAssetLoader.load(texturePath);
            if (asset != null) {
                if (asset.isAnimated && asset.frames != null && !asset.frames.isEmpty()) {
                    frames = new ArrayList<>(asset.frames);
                    frameDelays = new ArrayList<>(asset.delays);
                } else if (asset.bitmap != null) {
                    frames = new ArrayList<>();
                    frames.add(asset.bitmap);
                    frameDelays = new ArrayList<>();
                    frameDelays.add(100);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load texture: " + texturePath);
        }

        // Create placeholder if needed
        if (frames == null || frames.isEmpty()) {
            createPlaceholderFrames();
        }

        // Start at first frame (closed)
        currentFrameIndex = 0;
        animPlaying = false;
    }

    /**
     * Creates placeholder frames when GIF file is not available.
     * Creates multiple frames showing the lid opening animation.
     */
    private void createPlaceholderFrames() {
        frames = new ArrayList<>();
        frameDelays = new ArrayList<>();

        int frameCount = 10;
        for (int f = 0; f < frameCount; f++) {
            float lidAngle = (f / (float)(frameCount - 1)) * 70; // 0 to 70 degrees

            Bitmap frame = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(frame);

            // Chest base color (dark wood) - different for each type
            int baseColor = chestType == ChestType.MONTHLY ?
                Color.rgb(80, 40, 100) : Color.rgb(100, 60, 30);
            int highlightColor = chestType == ChestType.MONTHLY ?
                Color.rgb(120, 60, 140) : Color.rgb(140, 90, 50);
            int metalColor = chestType == ChestType.MONTHLY ?
                Color.rgb(200, 180, 255) : Color.rgb(255, 215, 0);

            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

            // Draw main body
            p.setColor(baseColor);
            RectF bodyRect = new RectF(0, height / 3f, width, height);
            c.drawRoundRect(bodyRect, 8, 8, p);

            // Body highlight
            p.setColor(highlightColor);
            RectF hlRect = new RectF(4, height / 3f + 4, width - 4, height / 3f + 4 + height / 4f);
            c.drawRoundRect(hlRect, 4, 4, p);

            // Metal bands
            p.setColor(metalColor);
            c.drawRect(5, height / 3f, 13, height, p);
            c.drawRect(width - 13, height / 3f, width - 5, height, p);
            c.drawRect(width / 2f - 4, height / 3f, width / 2f + 4, height, p);

            // Lock/keyhole
            p.setColor(darkenColor(metalColor));
            RectF lockOval = new RectF(width / 2f - 8, height / 2f + 5, width / 2f + 8, height / 2f + 25);
            c.drawOval(lockOval, p);
            p.setColor(Color.BLACK);
            RectF keyOval = new RectF(width / 2f - 4, height / 2f + 10, width / 2f + 4, height / 2f + 20);
            c.drawOval(keyOval, p);

            // Draw lid with rotation based on frame
            c.save();
            c.rotate(-lidAngle, width / 2f, height / 3f);

            int lidBaseColor = chestType == ChestType.MONTHLY ?
                Color.rgb(100, 50, 120) : Color.rgb(120, 70, 35);
            int lidHighlightColor = chestType == ChestType.MONTHLY ?
                Color.rgb(140, 80, 160) : Color.rgb(160, 100, 60);

            p.setColor(lidBaseColor);
            RectF lidRect = new RectF(0, 0, width, height / 3f + 5);
            c.drawRoundRect(lidRect, 10, 10, p);

            p.setColor(lidHighlightColor);
            RectF arcRect = new RectF(0, -height / 6f, width, height / 3f - height / 6f);
            c.drawArc(arcRect, 0, -180, true, p);

            // Metal bands on lid
            p.setColor(metalColor);
            c.drawRect(5, 0, 13, height / 3f + 5, p);
            c.drawRect(width - 13, 0, width - 5, height / 3f + 5, p);
            c.drawRect(width / 2f - 4, 0, width / 2f + 4, height / 3f + 5, p);

            c.restore();

            frames.add(frame);
            frameDelays.add(50);
        }
    }

    @Override
    public Rect getBounds() {
        return new Rect(x, y, x + width, y + height);
    }

    @Override
    public void update(TouchInputManager input) {
        long currentTime = System.currentTimeMillis();
        long deltaMs = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // Update glow animation
        glowPhase += 0.05f;
        if (glowPhase > Math.PI * 2) {
            glowPhase -= (float)(Math.PI * 2);
        }
        glowIntensity = canOpen ? (float)(Math.sin(glowPhase) * 0.3 + 0.7) : 0.3f;

        // Update frame animation
        if (animPlaying && frames != null && frames.size() > 1) {
            animAccumulator += deltaMs;
            int delay = frameDelays.get(currentFrameIndex);
            if (delay <= 0) delay = 50;

            while (animAccumulator >= delay) {
                animAccumulator -= delay;

                if (animForward) {
                    currentFrameIndex++;
                    if (currentFrameIndex >= frames.size()) {
                        currentFrameIndex = frames.size() - 1;
                        animPlaying = false;
                    }
                } else {
                    currentFrameIndex--;
                    if (currentFrameIndex < 0) {
                        currentFrameIndex = 0;
                        animPlaying = false;
                    }
                }
            }

            // Check if opening animation completed
            if (isOpening && !animPlaying && currentFrameIndex == frames.size() - 1) {
                isOpening = false;
                isOpen = true;
                if (!hasDroppedItems) {
                    dropItems();
                }
            }

            // Check if closing animation completed
            if (isClosing && !animPlaying && currentFrameIndex == 0) {
                isClosing = false;
                isOpen = false;
            }

            // Spawn particles during opening
            if (isOpening) {
                spawnOpeningParticles();
            }
        }

        // Update particles
        updateParticles();

        // Update dropped items
        for (ItemEntity item : droppedItems) {
            item.update(input);
        }
    }

    /**
     * Attempts to open the chest if conditions are met.
     * Called externally from LootGameScene when player interacts.
     * @return true if chest was opened, false otherwise
     */
    public boolean tryOpen() {
        if (playerNearby && !isOpen && !isOpening && canOpen) {
            open();
            return true;
        }
        return false;
    }

    /**
     * Opens the chest and triggers the loot drop.
     * Plays the frame animation forward.
     */
    public void open() {
        if (isOpen || isOpening || !canOpen) return;

        isOpening = true;
        isClosing = false;

        // Start playing animation forward
        animForward = true;
        animPlaying = true;
        animAccumulator = 0;

        // --- Uncomment when ControllerManager is ported ---
        // ControllerManager controller = ControllerManager.getInstance();
        // if (controller.isVibrationSupported()) {
        //     if (chestType == ChestType.MONTHLY) {
        //         controller.vibrate(VibrationPattern.LOOT_CHEST_MONTHLY);
        //     } else {
        //         controller.vibrate(VibrationPattern.LOOT_CHEST_DAILY);
        //     }
        // }

        // --- Uncomment when SaveManager is ported ---
        // SaveManager save = SaveManager.getInstance();
        // if (chestType == ChestType.DAILY) {
        //     save.markDailyChestOpened();
        // } else {
        //     save.markMonthlyChestOpened();
        // }

        canOpen = false;
        Log.d(TAG, chestType + " chest opened!");
    }

    /**
     * Drops random items from the chest with physics.
     */
    private void dropItems() {
        if (hasDroppedItems) return;
        hasDroppedItems = true;

        // Get all item IDs from registry
        Set<String> allItems = ItemRegistry.getAllItemIds();
        List<String> itemList = new ArrayList<>(allItems);

        // Filter out blocks
        itemList.removeIf(id -> {
            Item template = ItemRegistry.getTemplate(id);
            return template != null && template.getCategory() == Item.ItemCategory.BLOCK;
        });

        for (int i = 0; i < chestType.itemCount; i++) {
            String itemId = selectRandomItem(itemList, chestType.rarityBoost);
            if (itemId == null) continue;

            ItemEntity item = new ItemEntity(x + width / 2, y, itemId);

            double angle = -Math.PI / 2 + (random.nextDouble() - 0.5) * Math.PI / 2;
            double speed = 8 + random.nextDouble() * 6;
            double velX = Math.cos(angle) * speed;
            double velY = Math.sin(angle) * speed - 8;

            item.enablePhysics(velX, velY, groundY);
            item.setShowLightBeam(true);

            if (entityList != null) {
                item.setEntityList(entityList);
            }

            droppedItems.add(item);

            // --- Uncomment when SaveManager is ported ---
            // SaveManager.getInstance().addItem(itemId, 1);

            Log.d(TAG, "Dropped " + itemId + " (" +
                (item.getLinkedItem() != null ? item.getLinkedItem().getRarity().getDisplayName() : "Unknown") + ")");
        }
    }

    /**
     * Selects a random item with rarity weighting.
     */
    private String selectRandomItem(List<String> items, float rarityBoost) {
        if (items.isEmpty()) return null;

        List<String> weightedList = new ArrayList<>();

        for (String itemId : items) {
            Item template = ItemRegistry.getTemplate(itemId);
            if (template == null) continue;

            int weight;
            switch (template.getRarity()) {
                case COMMON:
                    weight = (int)(100 / rarityBoost);
                    break;
                case UNCOMMON:
                    weight = (int)(50 * (rarityBoost > 1 ? rarityBoost * 0.8 : 1));
                    break;
                case RARE:
                    weight = (int)(25 * rarityBoost);
                    break;
                case EPIC:
                    weight = (int)(10 * rarityBoost * 1.5);
                    break;
                case LEGENDARY:
                    weight = (int)(3 * rarityBoost * 2);
                    break;
                case MYTHIC:
                    weight = (int)(1 * rarityBoost * 3);
                    break;
                default:
                    weight = 50;
            }

            for (int i = 0; i < weight; i++) {
                weightedList.add(itemId);
            }
        }

        if (weightedList.isEmpty()) {
            return items.get(random.nextInt(items.size()));
        }

        return weightedList.get(random.nextInt(weightedList.size()));
    }

    /**
     * Spawns particle effects during chest opening.
     */
    private void spawnOpeningParticles() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastParticleTime < 50) return;
        lastParticleTime = currentTime;

        for (int i = 0; i < 3; i++) {
            float angle = (float)(random.nextDouble() * Math.PI * 2);
            float speed = 2 + random.nextFloat() * 3;
            float px = x + width / 2f + (random.nextFloat() - 0.5f) * width;
            float py = y + height / 2f;

            int particleColor;
            if (chestType == ChestType.MONTHLY) {
                // Rainbow particles
                float hue = random.nextFloat() * 360f;
                particleColor = Color.HSVToColor(new float[]{hue, 0.8f, 1.0f});
            } else {
                // Gold particles
                particleColor = Color.rgb(255, 215, 0);
            }

            particles.add(new Particle(px, py,
                (float)Math.cos(angle) * speed,
                (float)Math.sin(angle) * speed - 2,
                particleColor, 30 + random.nextInt(30)));
        }
    }

    /**
     * Updates particle positions and removes dead particles.
     */
    private void updateParticles() {
        particles.removeIf(p -> {
            p.update();
            return p.isDead();
        });
    }

    /** Gets all dropped items from this chest. */
    public List<ItemEntity> getDroppedItems() {
        return droppedItems;
    }

    /**
     * Sets the entity list reference for block collision detection on dropped items.
     */
    public void setEntityList(List<Entity> entities) {
        this.entityList = entities;
        for (ItemEntity item : droppedItems) {
            item.setEntityList(entities);
        }
    }

    /** Sets whether the player is nearby (for interaction prompt). */
    public void setPlayerNearby(boolean nearby) {
        this.playerNearby = nearby;
    }

    /** Checks if a point is within the interaction zone. */
    public boolean isInInteractionZone(Rect playerBounds) {
        return Rect.intersects(interactionZone, playerBounds);
    }

    /** Gets the chest type. */
    public ChestType getChestType() {
        return chestType;
    }

    /** Checks if the player is nearby. */
    public boolean isPlayerNearby() {
        return playerNearby;
    }

    /**
     * Handles a touch at the given screen coordinates.
     * Opens the chest if touched while player is nearby and chest can be opened.
     */
    public boolean handleClick(int clickX, int clickY, int cameraOffsetX, int cameraOffsetY) {
        int worldX = clickX + cameraOffsetX;
        int worldY = clickY + cameraOffsetY;

        Rect clickBounds = new Rect(x - 10, y - 10, x + width + 10, y + height + 10);
        if (clickBounds.contains(worldX, worldY)) {
            return tryOpen();
        }
        return false;
    }

    /** Checks if the chest has been opened. */
    public boolean isOpen() {
        return isOpen;
    }

    /** Checks if the chest can be opened. */
    public boolean canOpen() {
        return canOpen && !isOpen && !isOpening;
    }

    @Override
    public void draw(Canvas canvas) {
        // Draw dropped items first (behind chest)
        for (ItemEntity item : droppedItems) {
            item.draw(canvas);
        }

        // Draw glow effect
        if (canOpen || isOpening) {
            drawGlow(canvas);
        }

        // Draw chest frame
        if (frames != null && !frames.isEmpty()) {
            int idx = Math.max(0, Math.min(currentFrameIndex, frames.size() - 1));
            Bitmap frame = frames.get(idx);
            if (frame != null) {
                srcRect.set(0, 0, frame.getWidth(), frame.getHeight());
                dstRect.set(x, y, x + width, y + height);
                canvas.drawBitmap(frame, srcRect, dstRect, null);
            }
        }

        // Draw particles
        for (Particle p : particles) {
            p.draw(canvas, drawPaint);
        }

        // Draw interaction prompt
        if (playerNearby && !isOpen && !isOpening) {
            drawInteractionPrompt(canvas);
        }

        // Draw cooldown timer if not available
        if (!canOpen && !isOpen) {
            drawCooldownTimer(canvas);
        }
    }

    /**
     * Draws the glowing aura around the chest.
     */
    private void drawGlow(Canvas canvas) {
        int glowColor;
        if (chestType == ChestType.MONTHLY) {
            float hue = (float)((System.currentTimeMillis() % 5000) / 5000.0) * 360f;
            glowColor = Color.HSVToColor(new float[]{hue, 0.8f, 1.0f});
        } else {
            glowColor = Color.rgb(255, 215, 0);
        }

        int glowSize = (int)(20 + glowIntensity * 30);
        int r = Color.red(glowColor);
        int g = Color.green(glowColor);
        int b = Color.blue(glowColor);

        for (int i = 0; i < 5; i++) {
            float alpha = (0.1f - i * 0.015f) * glowIntensity;
            int a = Math.max(0, Math.min(255, (int)(alpha * 255)));
            int size = glowSize + i * 15;

            drawPaint.setColor(Color.argb(a, r, g, b));
            ovalRect.set(x - size / 2f + width / 2f, y - size / 2f + height / 2f,
                         x + size / 2f + width / 2f, y + size / 2f + height / 2f);
            canvas.drawOval(ovalRect, drawPaint);
        }
    }

    /**
     * Draws the interaction prompt.
     */
    private void drawInteractionPrompt(Canvas canvas) {
        if (!canOpen) return;

        String prompt = "Tap to Open";
        textPaint.setTextSize(16);
        textPaint.setFakeBoldText(true);
        float textWidth = textPaint.measureText(prompt);

        int promptX = (int)(x + width / 2f - textWidth / 2f);
        int promptY = y - 30;

        // Background
        drawPaint.setColor(Color.argb(180, 0, 0, 0));
        RectF bgRect = new RectF(promptX - 10, promptY - 18, promptX + textWidth + 10, promptY + 7);
        canvas.drawRoundRect(bgRect, 8, 8, drawPaint);

        // Text
        int textColor = chestType == ChestType.MONTHLY ?
            Color.rgb(200, 180, 255) : Color.rgb(255, 215, 0);
        textPaint.setColor(textColor);
        canvas.drawText(prompt, promptX, promptY, textPaint);
    }

    /**
     * Draws the cooldown timer.
     */
    private void drawCooldownTimer(Canvas canvas) {
        // --- Uncomment when SaveManager is ported ---
        // SaveManager save = SaveManager.getInstance();
        // long remaining;
        // String label;
        // if (chestType == ChestType.DAILY) {
        //     remaining = save.getDailyChestTimeRemaining();
        //     label = "Daily Chest";
        // } else {
        //     remaining = save.getMonthlyChestTimeRemaining();
        //     label = "Monthly Chest";
        // }
        // String timeStr = SaveManager.formatTimeRemaining(remaining);

        String label = chestType == ChestType.DAILY ? "Daily Chest" : "Monthly Chest";
        String displayText = label + ": Cooldown";

        textPaint.setTextSize(14);
        textPaint.setFakeBoldText(true);
        float textWidth = textPaint.measureText(displayText);

        int textX = (int)(x + width / 2f - textWidth / 2f);
        int textY = y - 20;

        // Background
        drawPaint.setColor(Color.argb(180, 0, 0, 0));
        RectF bgRect = new RectF(textX - 10, textY - 15, textX + textWidth + 10, textY + 7);
        canvas.drawRoundRect(bgRect, 6, 6, drawPaint);

        // Text
        textPaint.setColor(Color.rgb(255, 100, 100));
        canvas.drawText(displayText, textX, textY, textPaint);
    }

    /**
     * Darkens a color by multiplying RGB components by 0.7.
     */
    private static int darkenColor(int color) {
        int r = (int)(Color.red(color) * 0.7f);
        int g = (int)(Color.green(color) * 0.7f);
        int b = (int)(Color.blue(color) * 0.7f);
        return Color.argb(Color.alpha(color), r, g, b);
    }

    /**
     * Simple particle class for visual effects.
     */
    private static class Particle {
        float x, y, vx, vy;
        int color;
        int life, maxLife;
        float size;

        Particle(float x, float y, float vx, float vy, int color, int life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.life = life;
            this.maxLife = life;
            this.size = 4 + random.nextFloat() * 4;
        }

        void update() {
            x += vx;
            y += vy;
            vy += 0.1f; // Gravity
            life--;
        }

        boolean isDead() {
            return life <= 0;
        }

        void draw(Canvas canvas, Paint paint) {
            float alpha = (float) life / maxLife;
            int a = (int)(alpha * 255);
            paint.setColor(Color.argb(a, Color.red(color), Color.green(color), Color.blue(color)));
            int s = (int)(size * alpha);
            RectF oval = new RectF(x - s / 2f, y - s / 2f, x + s / 2f, y + s / 2f);
            canvas.drawOval(oval, paint);
        }
    }
}
