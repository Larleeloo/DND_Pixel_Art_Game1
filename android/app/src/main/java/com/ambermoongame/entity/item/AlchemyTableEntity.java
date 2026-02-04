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

/**
 * AlchemyTableEntity represents an interactable alchemy/crafting table.
 *
 * Conversion notes:
 * - java.awt.Color         -> android.graphics.Color (int)
 * - Graphics/Graphics2D    -> Canvas + Paint
 * - BufferedImage           -> Bitmap
 * - Rectangle              -> Rect
 * - BasicStroke            -> Paint.setStrokeWidth()
 * - Font/FontMetrics       -> Paint.setTextSize()/measureText()
 * - InputManager           -> TouchInputManager
 * - AnimatedTexture        -> AndroidAssetLoader.ImageAsset (simple looping)
 * - System.out/err.println -> Log.d()/Log.e()
 *
 * Features:
 * - Opens with tap when player is nearby
 * - Provides access to alchemy UI for combining items
 * - Visual feedback with glow effect
 * - Supports both alchemy (combining) and reverse crafting (deconstruction) modes
 */
public class AlchemyTableEntity extends Entity {

    private static final String TAG = "AlchemyTableEntity";

    // Dimensions
    private int width = 64;
    private int height = 64;

    // Visual state
    private Bitmap texture;
    private AndroidAssetLoader.ImageAsset animatedAsset;
    private long animStartTime = System.currentTimeMillis();
    private boolean isOpen = false;
    private boolean isInteractable = true;

    // Mode: false = alchemy (combine), true = reverse (deconstruct)
    private boolean reverseMode;

    // Glow effect
    private float glowIntensity = 0.5f;
    private float glowPhase = 0f;
    private int glowColor;

    // Interaction
    private boolean playerNearby = false;
    private static final int INTERACTION_RANGE = 80;

    // Callbacks
    private Runnable onOpenCallback;
    private Runnable onCloseCallback;

    // Time tracking
    private long lastUpdateTime = System.currentTimeMillis();

    // Reusable drawing objects
    private final Paint drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF ovalRect = new RectF();
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();

    /**
     * Creates an alchemy table entity.
     *
     * @param x X position
     * @param y Y position
     * @param reverseMode false for alchemy (combine), true for reverse crafting (deconstruct)
     */
    public AlchemyTableEntity(int x, int y, boolean reverseMode) {
        super(x, y);
        this.reverseMode = reverseMode;

        if (reverseMode) {
            this.glowColor = Color.rgb(200, 100, 255);  // Purple for reverse crafting
        } else {
            this.glowColor = Color.rgb(100, 255, 150);  // Green for alchemy
        }

        loadTexture();
    }

    /** Creates a standard alchemy table. */
    public static AlchemyTableEntity createAlchemyTable(int x, int y) {
        return new AlchemyTableEntity(x, y, false);
    }

    /** Creates a reverse crafting table. */
    public static AlchemyTableEntity createReverseCraftingTable(int x, int y) {
        return new AlchemyTableEntity(x, y, true);
    }

    private void loadTexture() {
        String texturePath = reverseMode
            ? "assets/alchemy/reverse_crafting_table.gif"
            : "assets/alchemy/alchemy_table.gif";

        try {
            AndroidAssetLoader.ImageAsset asset = AndroidAssetLoader.load(texturePath);
            if (asset != null) {
                if (asset.isAnimated) {
                    this.animatedAsset = asset;
                    this.texture = asset.bitmap;
                } else if (asset.bitmap != null) {
                    this.texture = asset.bitmap;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load texture: " + texturePath);
            createPlaceholderTexture();
        }

        if (texture == null) {
            createPlaceholderTexture();
        }
    }

    private void createPlaceholderTexture() {
        texture = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(texture);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        if (reverseMode) {
            // Reverse crafting table - anvil-like shape
            // Base
            p.setColor(Color.rgb(60, 60, 70));
            c.drawRect(8, 48, 56, 60, p);

            // Legs
            p.setColor(Color.rgb(50, 50, 60));
            c.drawRect(12, 40, 20, 52, p);
            c.drawRect(44, 40, 52, 52, p);

            // Body
            p.setColor(Color.rgb(80, 80, 90));
            c.drawRect(4, 24, 60, 44, p);

            // Top surface (anvil horn)
            p.setColor(Color.rgb(100, 100, 110));
            RectF topRect = new RectF(0, 16, 64, 28);
            c.drawRoundRect(topRect, 4, 4, p);

            // Highlight
            p.setColor(Color.rgb(140, 140, 150));
            c.drawRect(4, 18, 60, 21, p);

            // Purple glow accents
            p.setColor(Color.argb(150, 180, 100, 220));
            RectF glowOval = new RectF(26, 28, 38, 36);
            c.drawOval(glowOval, p);

        } else {
            // Alchemy table - cauldron/potion table shape
            // Table legs
            p.setColor(Color.rgb(101, 67, 33));
            c.drawRect(8, 48, 16, 62, p);
            c.drawRect(48, 48, 56, 62, p);

            // Table surface
            p.setColor(Color.rgb(139, 90, 43));
            RectF surfaceRect = new RectF(2, 40, 62, 52);
            c.drawRoundRect(surfaceRect, 4, 4, p);

            // Cauldron/bowl
            p.setColor(Color.rgb(60, 60, 60));
            RectF cauldronOuter = new RectF(16, 20, 48, 44);
            c.drawOval(cauldronOuter, p);

            // Cauldron inner
            p.setColor(Color.rgb(40, 40, 40));
            RectF cauldronInner = new RectF(20, 24, 44, 40);
            c.drawOval(cauldronInner, p);

            // Bubbling potion (green glow)
            p.setColor(Color.argb(200, 80, 200, 120));
            RectF potionOval = new RectF(24, 28, 40, 38);
            c.drawOval(potionOval, p);

            // Bubbles
            p.setColor(Color.argb(200, 150, 255, 180));
            RectF bubble1 = new RectF(28, 26, 34, 32);
            c.drawOval(bubble1, p);
            RectF bubble2 = new RectF(36, 30, 40, 34);
            c.drawOval(bubble2, p);

            // Book/scroll on table
            p.setColor(Color.rgb(200, 180, 140));
            c.drawRect(2, 44, 14, 50, p);
        }
    }

    @Override
    public void update(TouchInputManager input) {
        long currentTime = System.currentTimeMillis();
        long deltaMs = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        float delta = deltaMs / 1000f;

        // Update glow effect
        glowPhase += delta * 2.5f;
        glowIntensity = 0.4f + 0.25f * (float) Math.sin(glowPhase);

        // Update animated texture
        if (animatedAsset != null) {
            long elapsed = currentTime - animStartTime;
            Bitmap frame = animatedAsset.getFrame(elapsed);
            if (frame != null) {
                texture = frame;
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        // Draw glow effect if player is nearby or table is open
        if (playerNearby || isOpen) {
            drawGlowEffect(canvas);
        }

        // Draw interaction prompt if nearby and not open
        if (playerNearby && !isOpen) {
            drawInteractionPrompt(canvas);
        }

        // Draw table texture
        if (texture != null) {
            srcRect.set(0, 0, texture.getWidth(), texture.getHeight());
            dstRect.set(x, y, x + width, y + height);
            canvas.drawBitmap(texture, srcRect, dstRect, null);
        }

        // Draw "IN USE" text when open
        if (isOpen) {
            textPaint.setColor(glowColor);
            textPaint.setTextSize(11);
            textPaint.setFakeBoldText(true);
            String text = "IN USE";
            float textWidth = textPaint.measureText(text);
            canvas.drawText(text, x + (width - textWidth) / 2f, y - 5, textPaint);
        }
    }

    private void drawGlowEffect(Canvas canvas) {
        int glowRadius = (int)(25 * glowIntensity);
        int r = Color.red(glowColor);
        int g = Color.green(glowColor);
        int b = Color.blue(glowColor);

        for (int i = glowRadius; i > 0; i -= 4) {
            float alpha = 0.12f * (1 - (float) i / glowRadius);
            if (isOpen) alpha *= 1.5f;
            int a = Math.max(0, Math.min(255, (int)(alpha * 255)));
            drawPaint.setColor(Color.argb(a, r, g, b));
            ovalRect.set(x + width / 2f - i, y + height / 2f - i,
                         x + width / 2f + i, y + height / 2f + i);
            canvas.drawOval(ovalRect, drawPaint);
        }
    }

    private void drawInteractionPrompt(Canvas canvas) {
        String text = reverseMode ? "[Tap] Deconstruct" : "[Tap] Craft";
        textPaint.setTextSize(13);
        textPaint.setFakeBoldText(true);
        float textWidth = textPaint.measureText(text);

        float bgX = x + (width - textWidth) / 2f - 10;
        float bgY = y - 30;

        // Background
        drawPaint.setColor(Color.argb(180, 0, 0, 0));
        RectF bgRect = new RectF(bgX, bgY, bgX + textWidth + 20, bgY + 24);
        canvas.drawRoundRect(bgRect, 8, 8, drawPaint);

        // Border
        drawPaint.setColor(glowColor);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(1);
        canvas.drawRoundRect(bgRect, 8, 8, drawPaint);
        drawPaint.setStyle(Paint.Style.FILL);

        // Text
        textPaint.setColor(Color.WHITE);
        canvas.drawText(text, x + (width - textWidth) / 2f, y - 12, textPaint);
    }

    /** Attempts to open the alchemy table. */
    public boolean tryOpen() {
        if (!isInteractable || !playerNearby) return false;
        if (!isOpen) {
            open();
            return true;
        }
        return false;
    }

    /** Opens the alchemy table. */
    public void open() {
        isOpen = true;
        if (onOpenCallback != null) {
            onOpenCallback.run();
        }
        Log.d(TAG, (reverseMode ? "Reverse crafting" : "Alchemy") + " table opened");
    }

    /** Closes the alchemy table. */
    public void close() {
        isOpen = false;
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
        Log.d(TAG, (reverseMode ? "Reverse crafting" : "Alchemy") + " table closed");
    }

    /** Toggles the open state. */
    public void toggle() {
        if (isOpen) {
            close();
        } else {
            open();
        }
    }

    /** Checks if player is within interaction range. */
    public void checkPlayerProximity(int playerX, int playerY, int playerWidth, int playerHeight) {
        int playerCenterX = playerX + playerWidth / 2;
        int playerCenterY = playerY + playerHeight / 2;
        int tableCenterX = x + width / 2;
        int tableCenterY = y + height / 2;

        double distance = Math.sqrt(
            Math.pow(playerCenterX - tableCenterX, 2) +
            Math.pow(playerCenterY - tableCenterY, 2)
        );

        playerNearby = distance <= INTERACTION_RANGE;

        // Auto-close if player moves away
        if (isOpen && !playerNearby) {
            close();
        }
    }

    /** Checks if a point is in the interaction zone. */
    public boolean isInInteractionZone(Rect playerBounds) {
        Rect interactionZone = new Rect(
            x - INTERACTION_RANGE / 2,
            y - INTERACTION_RANGE / 2,
            x + width + INTERACTION_RANGE / 2,
            y + height + INTERACTION_RANGE / 2
        );
        return Rect.intersects(interactionZone, playerBounds);
    }

    /** Handles a touch at the given screen coordinates. */
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

    // Getters and setters

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isOpen() { return isOpen; }
    public boolean isPlayerNearby() { return playerNearby; }
    public boolean isReverseMode() { return reverseMode; }

    public void setPlayerNearby(boolean nearby) {
        this.playerNearby = nearby;
        if (isOpen && !nearby) {
            close();
        }
    }

    public void setOnOpenCallback(Runnable callback) { this.onOpenCallback = callback; }
    public void setOnCloseCallback(Runnable callback) { this.onCloseCallback = callback; }
    public void setGlowColor(int color) { this.glowColor = color; }
    public void setInteractable(boolean interactable) { this.isInteractable = interactable; }

    @Override
    public Rect getBounds() {
        return new Rect(x, y, x + width, y + height);
    }
}
