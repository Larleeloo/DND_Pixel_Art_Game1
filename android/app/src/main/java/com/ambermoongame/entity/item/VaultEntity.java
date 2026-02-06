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

/**
 * VaultEntity represents an interactable vault/chest that provides access to
 * the player's persistent storage.
 *
 * Conversion notes:
 * - AnimatedTexture              -> Manual frame management via ImageAsset.frames
 * - java.awt.Color               -> android.graphics.Color (int)
 * - Graphics/Graphics2D          -> Canvas + Paint
 * - BufferedImage                 -> Bitmap
 * - Rectangle                    -> Rect
 * - Font/FontMetrics             -> Paint.setTextSize()/measureText()
 * - InputManager                 -> TouchInputManager
 * - System.out/err.println       -> Log.d()/Log.e()
 * - SaveManager.SavedItem        -> simple SavedItem inner class (pending SaveManager port)
 *
 * Animation System:
 * - Uses a single GIF per vault type with forward/reverse playback
 * - First frame = closed state, last frame = open state
 * - Play forward to open, play reverse to close
 *
 * Types:
 * - PLAYER_VAULT: Main player vault with persistent storage (10,000 slots)
 * - STORAGE_CHEST: Local storage chest (per-level, 48 slots)
 * - LARGE_CHEST: 32 slots
 * - MEDIUM_CHEST: 16 slots
 * - ANCIENT_POTTERY: 5 slots
 */
public class VaultEntity extends Entity {

    private static final String TAG = "VaultEntity";

    // Vault type constants (replaces enum to avoid D8 crash)
    public static final int VAULT_PLAYER = 0;
    public static final int VAULT_STORAGE_CHEST = 1;
    public static final int VAULT_LARGE_CHEST = 2;
    public static final int VAULT_MEDIUM_CHEST = 3;
    public static final int VAULT_ANCIENT_POTTERY = 4;
    public static final int VAULT_TYPE_COUNT = 5;

    private static final String[] VAULT_DISPLAY_NAMES = {
        "Player Vault", "Storage Chest", "Large Chest", "Medium Chest", "Ancient Pottery"
    };
    private static final String[] VAULT_TEXTURE_PATHS = {
        "assets/vault/player_vault", "assets/vault/storage_chest",
        "assets/vault/large_chest", "assets/vault/medium_chest",
        "assets/items/ancient_pottery"
    };
    private static final boolean[] VAULT_PERSISTENT = {
        true, false, false, false, false
    };
    private static final int[] VAULT_MAX_SLOTS = {
        10000, 48, 32, 16, 5
    };

    // Helper class to provide enum-like API for backwards compatibility
    public static final class VaultType {
        private final int type;

        private VaultType(int type) {
            this.type = type;
        }

        public static VaultType fromInt(int type) {
            return new VaultType(type);
        }

        public int intValue() { return type; }
        public String getDisplayName() { return getVaultDisplayName(type); }
        public String getTextureBasePath() { return getVaultTexturePath(type); }
        public boolean isPersistent() { return isVaultPersistent(type); }
        public int getMaxSlots() { return getVaultMaxSlots(type); }

        // Convenience static instances
        public static final VaultType PLAYER_VAULT = new VaultType(VAULT_PLAYER);
        public static final VaultType STORAGE_CHEST = new VaultType(VAULT_STORAGE_CHEST);
        public static final VaultType LARGE_CHEST = new VaultType(VAULT_LARGE_CHEST);
        public static final VaultType MEDIUM_CHEST = new VaultType(VAULT_MEDIUM_CHEST);
        public static final VaultType ANCIENT_POTTERY = new VaultType(VAULT_ANCIENT_POTTERY);
    }

    public static String getVaultDisplayName(int type) {
        if (type >= 0 && type < VAULT_TYPE_COUNT) return VAULT_DISPLAY_NAMES[type];
        return "Unknown";
    }

    public static String getVaultTexturePath(int type) {
        if (type >= 0 && type < VAULT_TYPE_COUNT) return VAULT_TEXTURE_PATHS[type];
        return VAULT_TEXTURE_PATHS[0];
    }

    public static boolean isVaultPersistent(int type) {
        if (type >= 0 && type < VAULT_TYPE_COUNT) return VAULT_PERSISTENT[type];
        return false;
    }

    public static int getVaultMaxSlots(int type) {
        if (type >= 0 && type < VAULT_TYPE_COUNT) return VAULT_MAX_SLOTS[type];
        return 48;
    }

    /**
     * Simple item data class replacing SaveManager.SavedItem until SaveManager is ported.
     */
    public static class SavedItem {
        public String itemId;
        public int stackCount;

        public SavedItem(String itemId, int stackCount) {
            this.itemId = itemId;
            this.stackCount = stackCount;
        }
    }

    // Dimensions
    private int width = 64;
    private int height = 64;

    // Vault type
    private VaultType vaultType;

    // Frame-based animation (replaces AnimatedTexture)
    private List<Bitmap> frames;
    private List<Integer> frameDelays;
    private int currentFrameIndex = 0;
    private boolean animPlaying = false;
    private boolean animForward = true;
    private long animAccumulator = 0;

    // Visual state
    private boolean isOpen = false;
    private boolean isOpening = false;
    private boolean isClosing = false;
    private boolean isInteractable = true;

    // Glow effect
    private float glowIntensity = 0.5f;
    private float glowPhase = 0f;
    private int glowColor = Color.rgb(255, 215, 0);  // Gold glow

    // Interaction
    private boolean playerNearby = false;
    private static final int INTERACTION_RANGE = 80;

    // Callbacks
    private Runnable onOpenCallback;
    private Runnable onCloseCallback;

    // Time tracking
    private long lastUpdateTime = System.currentTimeMillis();

    // Local storage for non-persistent vaults
    private List<SavedItem> localItems = new ArrayList<>();

    // Reusable drawing objects
    private final Paint drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF ovalRect = new RectF();
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();

    /**
     * Creates a new VaultEntity.
     */
    public VaultEntity(int x, int y, VaultType vaultType) {
        super(x, y);
        this.vaultType = vaultType;
        loadTexture();
    }

    /** Creates a player vault at the specified position. */
    public static VaultEntity createPlayerVault(int x, int y) {
        return new VaultEntity(x, y, VaultType.PLAYER_VAULT);
    }

    /** Creates a storage chest at the specified position (48 slots). */
    public static VaultEntity createStorageChest(int x, int y) {
        return new VaultEntity(x, y, VaultType.STORAGE_CHEST);
    }

    /** Creates a large chest at the specified position (32 slots). */
    public static VaultEntity createLargeChest(int x, int y) {
        return new VaultEntity(x, y, VaultType.LARGE_CHEST);
    }

    /** Creates a medium chest at the specified position (16 slots). */
    public static VaultEntity createMediumChest(int x, int y) {
        return new VaultEntity(x, y, VaultType.MEDIUM_CHEST);
    }

    /** Creates an ancient pottery container at the specified position (5 slots). */
    public static VaultEntity createAncientPottery(int x, int y) {
        return new VaultEntity(x, y, VaultType.ANCIENT_POTTERY);
    }

    /**
     * Loads the GIF texture for opening animation.
     * Extracts frames from ImageAsset for manual playback control.
     */
    private void loadTexture() {
        String texturePath = vaultType.getTextureBasePath() + ".gif";

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

        if (frames == null || frames.isEmpty()) {
            createPlaceholderFrames();
        }

        // Start at first frame (closed)
        currentFrameIndex = 0;
        animPlaying = false;
    }

    /**
     * Creates placeholder frames when GIF file is not available.
     */
    private void createPlaceholderFrames() {
        frames = new ArrayList<>();
        frameDelays = new ArrayList<>();

        int frameCount = 8;
        for (int f = 0; f < frameCount; f++) {
            float lidAngle = (f / (float)(frameCount - 1)) * 45;

            Bitmap frame = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(frame);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

            // Draw a simple vault/chest shape
            p.setColor(Color.rgb(139, 90, 43));  // Brown for wood
            RectF bodyRect = new RectF(4, 16, 60, 60);
            c.drawRoundRect(bodyRect, 8, 8, p);

            // Draw lid with rotation based on frame
            c.save();
            c.rotate(-lidAngle, 32, 16);
            p.setColor(Color.rgb(101, 67, 33));  // Darker brown
            RectF lidRect = new RectF(2, 8, 62, 28);
            c.drawRoundRect(lidRect, 6, 6, p);
            c.restore();

            // Draw metal bands
            p.setColor(Color.rgb(169, 169, 169));  // Silver
            c.drawRect(8, 20, 12, 60, p);
            c.drawRect(52, 20, 56, 60, p);
            c.drawRect(28, 20, 36, 60, p);

            // Draw lock
            p.setColor(Color.rgb(255, 215, 0));  // Gold
            RectF lockOval = new RectF(28, 35, 36, 45);
            c.drawOval(lockOval, p);

            frames.add(frame);
            frameDelays.add(50);
        }
    }

    @Override
    public void update(TouchInputManager input) {
        long currentTime = System.currentTimeMillis();
        long deltaMs = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        float delta = deltaMs / 1000f;

        // Update glow effect
        glowPhase += delta * 2;
        glowIntensity = 0.4f + 0.2f * (float) Math.sin(glowPhase);

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
            }

            // Check if closing animation completed
            if (isClosing && !animPlaying && currentFrameIndex == 0) {
                isClosing = false;
                isOpen = false;
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        // Draw glow effect if player is nearby
        if (playerNearby || isOpen) {
            drawGlowEffect(canvas);
        }

        // Draw interaction prompt if nearby and not open
        if (playerNearby && !isOpen) {
            drawInteractionPrompt(canvas);
        }

        // Draw vault frame
        if (frames != null && !frames.isEmpty()) {
            int idx = Math.max(0, Math.min(currentFrameIndex, frames.size() - 1));
            Bitmap frame = frames.get(idx);
            if (frame != null) {
                srcRect.set(0, 0, frame.getWidth(), frame.getHeight());
                dstRect.set(x, y, x + width, y + height);
                canvas.drawBitmap(frame, srcRect, dstRect, null);
            }
        }

        // Draw "OPEN" text when vault is open
        if (isOpen) {
            textPaint.setColor(Color.argb(200, 0, 255, 0));
            textPaint.setTextSize(12);
            textPaint.setFakeBoldText(true);
            String text = "VAULT OPEN";
            float textWidth = textPaint.measureText(text);
            canvas.drawText(text, x + (width - textWidth) / 2f, y - 5, textPaint);
        }
    }

    private void drawGlowEffect(Canvas canvas) {
        int glowRadius = (int) (20 * glowIntensity);
        int r = Color.red(glowColor);
        int g = Color.green(glowColor);
        int b = Color.blue(glowColor);

        for (int i = glowRadius; i > 0; i -= 4) {
            float alpha = 0.1f * (1 - (float) i / glowRadius);
            if (isOpen) alpha *= 1.5f;
            int a = Math.max(0, Math.min(255, (int)(alpha * 255)));
            drawPaint.setColor(Color.argb(a, r, g, b));
            ovalRect.set(x + width / 2f - i, y + height / 2f - i,
                         x + width / 2f + i, y + height / 2f + i);
            canvas.drawOval(ovalRect, drawPaint);
        }
    }

    private void drawInteractionPrompt(Canvas canvas) {
        String text = "[Tap] Open Vault";
        textPaint.setTextSize(14);
        textPaint.setFakeBoldText(true);
        float textWidth = textPaint.measureText(text);

        float bgX = x + (width - textWidth) / 2f - 8;
        float bgY = y - 28;

        // Background
        drawPaint.setColor(Color.argb(150, 0, 0, 0));
        RectF bgRect = new RectF(bgX, bgY, bgX + textWidth + 16, bgY + 22);
        canvas.drawRoundRect(bgRect, 8, 8, drawPaint);

        // Text
        textPaint.setColor(Color.WHITE);
        canvas.drawText(text, x + (width - textWidth) / 2f, y - 12, textPaint);
    }

    /** Attempts to open the vault. Returns true if opened. */
    public boolean tryOpen() {
        if (!isInteractable || !playerNearby) return false;
        if (!isOpen) {
            open();
            return true;
        }
        return false;
    }

    /**
     * Handles a touch at the given screen coordinates.
     */
    public boolean handleClick(int clickX, int clickY, int cameraOffsetX, int cameraOffsetY) {
        int worldX = clickX + cameraOffsetX;
        int worldY = clickY + cameraOffsetY;

        Rect bounds = new Rect(x, y, x + width, y + height);
        if (bounds.contains(worldX, worldY)) {
            if (playerNearby && isInteractable) {
                toggle();
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a screen position is within the vault bounds.
     */
    public boolean containsPoint(int screenX, int screenY, int cameraOffsetX, int cameraOffsetY) {
        int worldX = screenX + cameraOffsetX;
        int worldY = screenY + cameraOffsetY;
        return new Rect(x, y, x + width, y + height).contains(worldX, worldY);
    }

    /** Opens the vault. Plays the frame animation forward. */
    public void open() {
        if (isOpen || isOpening) return;

        isOpening = true;
        isClosing = false;
        animForward = true;
        animPlaying = true;
        animAccumulator = 0;

        if (onOpenCallback != null) {
            onOpenCallback.run();
        }
        Log.d(TAG, "Vault opening");
    }

    /** Closes the vault. Plays the frame animation in reverse. */
    public void close() {
        if (!isOpen || isClosing) return;

        isClosing = true;
        isOpening = false;
        animForward = false;
        animPlaying = true;
        animAccumulator = 0;

        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
        Log.d(TAG, "Vault closing");
    }

    /** Toggles the vault open/closed state. */
    public void toggle() {
        if (isOpening || isClosing) return;
        if (isOpen) {
            close();
        } else {
            open();
        }
    }

    /** Checks if the player is within interaction range. */
    public void checkPlayerProximity(int playerX, int playerY, int playerWidth, int playerHeight) {
        int playerCenterX = playerX + playerWidth / 2;
        int playerCenterY = playerY + playerHeight / 2;
        int vaultCenterX = x + width / 2;
        int vaultCenterY = y + height / 2;

        double distance = Math.sqrt(
            Math.pow(playerCenterX - vaultCenterX, 2) +
            Math.pow(playerCenterY - vaultCenterY, 2)
        );

        boolean wasNearby = playerNearby;
        playerNearby = distance <= INTERACTION_RANGE;

        // Auto-close if player moves away while open
        if ((isOpen || isOpening) && wasNearby && !playerNearby && !isClosing) {
            isOpening = false;
            isOpen = true;
            close();
        }
    }

    // Getters and setters

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public void setPosition(int newX, int newY) { this.x = newX; this.y = newY; }
    public VaultType getVaultType() { return vaultType; }
    public boolean isOpen() { return isOpen; }
    public boolean isPlayerNearby() { return playerNearby; }
    public boolean isPersistent() { return vaultType.isPersistent(); }

    public void setOnOpenCallback(Runnable callback) { this.onOpenCallback = callback; }
    public void setOnCloseCallback(Runnable callback) { this.onCloseCallback = callback; }
    public void setGlowColor(int color) { this.glowColor = color; }
    public void setInteractable(boolean interactable) { this.isInteractable = interactable; }

    @Override
    public Rect getBounds() {
        return new Rect(x, y, x + width, y + height);
    }

    // ==================== Local Storage Methods ====================

    /** Gets the local items list (for non-persistent vault types). */
    public List<SavedItem> getLocalItems() {
        return new ArrayList<>(localItems);
    }

    /**
     * Adds an item to local storage.
     * @return Number of items that couldn't be added (overflow)
     */
    public int addLocalItem(String itemId, int count) {
        if (!vaultType.isPersistent() && itemId != null && !itemId.isEmpty() && count > 0) {
            int remaining = count;
            int stackSize = 16;

            // Try to stack with existing items
            for (SavedItem item : localItems) {
                if (item.itemId.equals(itemId) && item.stackCount < stackSize) {
                    int space = stackSize - item.stackCount;
                    int toAdd = Math.min(space, remaining);
                    item.stackCount += toAdd;
                    remaining -= toAdd;
                    if (remaining == 0) return 0;
                }
            }

            // Add new stacks
            while (remaining > 0 && localItems.size() < vaultType.getMaxSlots()) {
                int toAdd = Math.min(stackSize, remaining);
                localItems.add(new SavedItem(itemId, toAdd));
                remaining -= toAdd;
            }

            return remaining;
        }
        return count;
    }

    /**
     * Removes an item from local storage.
     * @return The removed item info, or null if invalid
     */
    public SavedItem removeLocalItem(int slotIndex, int count) {
        if (!vaultType.isPersistent() && slotIndex >= 0 && slotIndex < localItems.size()) {
            SavedItem item = localItems.get(slotIndex);
            if (count < 0 || count >= item.stackCount) {
                localItems.remove(slotIndex);
                return item;
            } else {
                item.stackCount -= count;
                return new SavedItem(item.itemId, count);
            }
        }
        return null;
    }

    /** Clears all local items. */
    public void clearLocalItems() { localItems.clear(); }

    /** Gets the local storage slot count. */
    public int getLocalSlotCount() { return localItems.size(); }

    /** Gets the maximum local storage slots. */
    public int getMaxLocalSlots() { return vaultType.getMaxSlots(); }
}
