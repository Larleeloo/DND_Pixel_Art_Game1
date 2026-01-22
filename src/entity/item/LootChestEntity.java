package entity.item;

import entity.Entity;
import graphics.AnimatedTexture;
import graphics.AssetLoader;
import input.ControllerManager;
import input.InputManager;
import input.VibrationPattern;
import save.SaveManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * LootChestEntity represents a treasure chest that can be opened to receive random loot.
 * Features opening animation via GIF playback, item drops with physics, and cooldown management.
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

    public enum ChestType {
        DAILY(3, 1.0f, "assets/chests/daily_chest"),    // 3 items, normal rarity
        MONTHLY(10, 2.5f, "assets/chests/monthly_chest"); // 10 items, boosted rarity

        public final int itemCount;
        public final float rarityBoost;
        public final String textureBasePath;

        ChestType(int itemCount, float rarityBoost, String textureBasePath) {
            this.itemCount = itemCount;
            this.rarityBoost = rarityBoost;
            this.textureBasePath = textureBasePath;
        }
    }

    private ChestType chestType;
    private int width = 64;
    private int height = 64;

    // Animation states
    private boolean isOpen = false;
    private boolean isOpening = false;
    private boolean isClosing = false;

    // Texture (single GIF with forward/reverse playback)
    private AnimatedTexture animatedTexture;
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
    private Rectangle interactionZone;

    private static final Random random = new Random();

    /**
     * Creates a new loot chest at the specified position.
     */
    public LootChestEntity(int x, int y, ChestType type, int groundLevel) {
        super(x, y);
        this.chestType = type;
        this.groundY = groundLevel;
        this.particles = new ArrayList<>();
        this.droppedItems = new ArrayList<>();
        this.interactionZone = new Rectangle(x - 50, y - 50, width + 100, height + 100);

        // Load textures
        loadTextures();

        // Check if chest can be opened
        SaveManager save = SaveManager.getInstance();
        if (type == ChestType.DAILY) {
            canOpen = save.canOpenDailyChest();
        } else {
            canOpen = save.canOpenMonthlyChest();
        }
    }

    /**
     * Loads the GIF texture for opening animation.
     * A single GIF represents the full open/close animation:
     * - First frame = closed state
     * - Last frame = open state
     * - Play forward to open, play reverse to close
     */
    private void loadTextures() {
        String texturePath = chestType.textureBasePath + ".gif";

        try {
            AssetLoader.ImageAsset asset = AssetLoader.load(texturePath);
            if (asset.animatedTexture != null) {
                animatedTexture = asset.animatedTexture;
            } else if (asset.staticImage != null) {
                // Single static image - create single-frame animation
                List<BufferedImage> frames = new ArrayList<>();
                frames.add(asset.staticImage);
                List<Integer> delays = new ArrayList<>();
                delays.add(100);
                animatedTexture = new AnimatedTexture(frames, delays);
            }
        } catch (Exception e) {
            System.err.println("LootChestEntity: Failed to load texture: " + texturePath);
        }

        // Create placeholder if needed
        if (animatedTexture == null) {
            animatedTexture = createPlaceholderTexture();
        }

        // Configure for non-looping playback and start at first frame (closed)
        animatedTexture.setLooping(false);
        animatedTexture.goToStart();
    }

    /**
     * Creates a placeholder animated texture when GIF file is not available.
     * Creates multiple frames showing the lid opening animation.
     */
    private AnimatedTexture createPlaceholderTexture() {
        List<BufferedImage> frames = new ArrayList<>();
        List<Integer> delays = new ArrayList<>();

        // Create frames for opening animation (10 frames, 0 to 70 degrees)
        int frameCount = 10;
        for (int f = 0; f < frameCount; f++) {
            float lidAngle = (f / (float)(frameCount - 1)) * 70; // 0 to 70 degrees

            BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frame.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Chest base color (dark wood) - different for each type
            Color baseColor = chestType == ChestType.MONTHLY ?
                new Color(80, 40, 100) : new Color(100, 60, 30);
            Color highlightColor = chestType == ChestType.MONTHLY ?
                new Color(120, 60, 140) : new Color(140, 90, 50);
            Color metalColor = chestType == ChestType.MONTHLY ?
                new Color(200, 180, 255) : new Color(255, 215, 0);

            // Draw main body
            g.setColor(baseColor);
            g.fillRoundRect(0, height / 3, width, height * 2 / 3, 8, 8);

            // Body highlight
            g.setColor(highlightColor);
            g.fillRoundRect(4, height / 3 + 4, width - 8, height / 4, 4, 4);

            // Metal bands
            g.setColor(metalColor);
            g.fillRect(5, height / 3, 8, height * 2 / 3);
            g.fillRect(width - 13, height / 3, 8, height * 2 / 3);
            g.fillRect(width / 2 - 4, height / 3, 8, height * 2 / 3);

            // Lock/keyhole
            g.setColor(metalColor.darker());
            g.fillOval(width / 2 - 8, height / 2 + 5, 16, 20);
            g.setColor(Color.BLACK);
            g.fillOval(width / 2 - 4, height / 2 + 10, 8, 10);

            // Draw lid with rotation based on frame
            java.awt.geom.AffineTransform oldTransform = g.getTransform();
            g.rotate(Math.toRadians(-lidAngle), width / 2, height / 3);

            Color lidBaseColor = chestType == ChestType.MONTHLY ?
                new Color(100, 50, 120) : new Color(120, 70, 35);
            Color lidHighlightColor = chestType == ChestType.MONTHLY ?
                new Color(140, 80, 160) : new Color(160, 100, 60);

            g.setColor(lidBaseColor);
            g.fillRoundRect(0, 0, width, height / 3 + 5, 10, 10);

            g.setColor(lidHighlightColor);
            g.fillArc(0, -height / 6, width, height / 3, 0, 180);

            // Metal bands on lid
            g.setColor(metalColor);
            g.fillRect(5, 0, 8, height / 3 + 5);
            g.fillRect(width - 13, 0, 8, height / 3 + 5);
            g.fillRect(width / 2 - 4, 0, 8, height / 3 + 5);

            g.setTransform(oldTransform);
            g.dispose();

            frames.add(frame);
            delays.add(50); // 50ms per frame
        }

        return new AnimatedTexture(frames, delays);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public void update(InputManager input) {
        long currentTime = System.currentTimeMillis();
        long deltaMs = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // Update glow animation
        glowPhase += 0.05f;
        if (glowPhase > Math.PI * 2) {
            glowPhase -= Math.PI * 2;
        }
        glowIntensity = canOpen ? (float)(Math.sin(glowPhase) * 0.3 + 0.7) : 0.3f;

        // Update the animated texture
        if (animatedTexture != null) {
            animatedTexture.update(deltaMs);

            // Check if opening animation completed (reached last frame)
            if (isOpening && animatedTexture.isAtEnd() && animatedTexture.isPaused()) {
                isOpening = false;
                isOpen = true;

                // Drop items when fully open
                if (!hasDroppedItems) {
                    dropItems();
                }
            }

            // Check if closing animation completed (reached first frame)
            if (isClosing && animatedTexture.isAtStart() && animatedTexture.isPaused()) {
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

        // Note: E key handling is done in LootGameScene to ensure proper input priority
    }

    /**
     * Attempts to open the chest if conditions are met.
     * Called externally from LootGameScene when player presses E.
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
     * Plays the GIF animation forward.
     */
    public void open() {
        if (isOpen || isOpening || !canOpen) return;

        isOpening = true;
        isClosing = false;

        // Start playing the animation forward
        if (animatedTexture != null) {
            animatedTexture.playForward();
        }

        // Trigger controller vibration with intricate pattern based on chest type
        ControllerManager controller = ControllerManager.getInstance();
        if (controller.isVibrationSupported()) {
            if (chestType == ChestType.MONTHLY) {
                controller.vibrate(VibrationPattern.LOOT_CHEST_MONTHLY);
            } else {
                controller.vibrate(VibrationPattern.LOOT_CHEST_DAILY);
            }
        }

        // Mark chest as opened in save data
        SaveManager save = SaveManager.getInstance();
        if (chestType == ChestType.DAILY) {
            save.markDailyChestOpened();
        } else {
            save.markMonthlyChestOpened();
        }

        canOpen = false;
        System.out.println("LootChestEntity: " + chestType + " chest opened!");
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

        // Filter out blocks (they're not exciting loot)
        itemList.removeIf(id -> {
            Item template = ItemRegistry.getTemplate(id);
            return template != null && template.getCategory() == Item.ItemCategory.BLOCK;
        });

        for (int i = 0; i < chestType.itemCount; i++) {
            // Select random item with rarity weighting
            String itemId = selectRandomItem(itemList, chestType.rarityBoost);
            if (itemId == null) continue;

            // Create item entity using the registry item constructor
            ItemEntity item = new ItemEntity(x + width / 2, y, itemId);

            // Enable physics with random velocity
            double angle = -Math.PI / 2 + (random.nextDouble() - 0.5) * Math.PI / 2; // -90 to -45 degrees
            double speed = 8 + random.nextDouble() * 6;
            double velX = Math.cos(angle) * speed;
            double velY = Math.sin(angle) * speed - 8; // Additional upward velocity

            item.enablePhysics(velX, velY, groundY);
            item.setShowLightBeam(true);

            // Set entity list for block collision detection
            if (entityList != null) {
                item.setEntityList(entityList);
            }

            droppedItems.add(item);

            // Save the item to player inventory
            SaveManager.getInstance().addItem(itemId, 1);

            System.out.println("LootChestEntity: Dropped " + itemId + " (" +
                (item.getLinkedItem() != null ? item.getLinkedItem().getRarity().getDisplayName() : "Unknown") + ")");
        }
    }

    /**
     * Selects a random item with rarity weighting.
     * Higher rarityBoost values increase chances of rare items.
     */
    private String selectRandomItem(List<String> items, float rarityBoost) {
        if (items.isEmpty()) return null;

        // Build weighted list based on rarity
        List<String> weightedList = new ArrayList<>();

        for (String itemId : items) {
            Item template = ItemRegistry.getTemplate(itemId);
            if (template == null) continue;

            // Weight based on rarity (rarer = less weight, but boosted by rarityBoost)
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

        // Spawn golden sparkles
        for (int i = 0; i < 3; i++) {
            float angle = (float)(random.nextDouble() * Math.PI * 2);
            float speed = 2 + random.nextFloat() * 3;
            float px = x + width / 2 + (random.nextFloat() - 0.5f) * width;
            float py = y + height / 2;

            Color particleColor;
            if (chestType == ChestType.MONTHLY) {
                // Monthly chest has rainbow particles
                float hue = random.nextFloat();
                particleColor = Color.getHSBColor(hue, 0.8f, 1.0f);
            } else {
                // Daily chest has gold particles
                particleColor = new Color(255, 215, 0);
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

    /**
     * Gets all dropped items from this chest.
     */
    public List<ItemEntity> getDroppedItems() {
        return droppedItems;
    }

    /**
     * Sets the entity list reference for block collision detection on dropped items.
     * When set, items will properly collide with solid blocks instead of
     * only using the ground level.
     *
     * @param entities The list of entities in the scene
     */
    public void setEntityList(List<Entity> entities) {
        this.entityList = entities;
        // Also update any already dropped items
        for (ItemEntity item : droppedItems) {
            item.setEntityList(entities);
        }
    }

    /**
     * Sets whether the player is nearby (for interaction prompt).
     */
    public void setPlayerNearby(boolean nearby) {
        this.playerNearby = nearby;
    }

    /**
     * Checks if a point is within the interaction zone.
     */
    public boolean isInInteractionZone(Rectangle playerBounds) {
        return interactionZone.intersects(playerBounds);
    }

    /**
     * Gets the chest type.
     */
    public ChestType getChestType() {
        return chestType;
    }

    /**
     * Checks if the player is nearby.
     */
    public boolean isPlayerNearby() {
        return playerNearby;
    }

    /**
     * Handles a mouse click at the given screen coordinates.
     * Opens the chest if clicked while player is nearby and chest can be opened.
     *
     * @param clickX Click X position (screen coordinates)
     * @param clickY Click Y position (screen coordinates)
     * @param cameraOffsetX Camera X offset for coordinate translation
     * @param cameraOffsetY Camera Y offset for coordinate translation
     * @return true if the click was handled (chest opened)
     */
    public boolean handleClick(int clickX, int clickY, int cameraOffsetX, int cameraOffsetY) {
        // Translate screen coordinates to world coordinates
        int worldX = clickX + cameraOffsetX;
        int worldY = clickY + cameraOffsetY;

        // Check if click is within chest bounds (use a slightly larger hitbox for easier clicking)
        Rectangle clickBounds = new Rectangle(x - 10, y - 10, width + 20, height + 20);
        if (clickBounds.contains(worldX, worldY)) {
            return tryOpen();
        }
        return false;
    }

    /**
     * Checks if the chest has been opened.
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Checks if the chest can be opened.
     */
    public boolean canOpen() {
        return canOpen && !isOpen && !isOpening;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw dropped items first (behind chest)
        for (ItemEntity item : droppedItems) {
            item.draw(g);
        }

        // Draw glow effect
        if (canOpen || isOpening) {
            drawGlow(g2d);
        }

        // Draw chest texture (GIF-based with directional playback)
        if (animatedTexture != null) {
            g2d.drawImage(animatedTexture.getCurrentFrame(), x, y, width, height, null);
        }

        // Draw particles
        for (Particle p : particles) {
            p.draw(g2d);
        }

        // Draw interaction prompt
        if (playerNearby && !isOpen && !isOpening) {
            drawInteractionPrompt(g2d);
        }

        // Draw cooldown timer if not available
        if (!canOpen && !isOpen) {
            drawCooldownTimer(g2d);
        }
    }

    /**
     * Draws the glowing aura around the chest.
     */
    private void drawGlow(Graphics2D g) {
        Color glowColor;
        if (chestType == ChestType.MONTHLY) {
            // Rainbow glow for monthly chest
            float hue = (float)((System.currentTimeMillis() % 5000) / 5000.0);
            glowColor = Color.getHSBColor(hue, 0.8f, 1.0f);
        } else {
            // Gold glow for daily chest
            glowColor = new Color(255, 215, 0);
        }

        int glowSize = (int)(20 + glowIntensity * 30);
        for (int i = 0; i < 5; i++) {
            float alpha = (0.1f - i * 0.015f) * glowIntensity;
            int size = glowSize + i * 15;
            g.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(),
                (int)(alpha * 255)));
            g.fillOval(x - size / 2 + width / 2, y - size / 2 + height / 2, size, size);
        }
    }

    /**
     * Draws the interaction prompt.
     */
    private void drawInteractionPrompt(Graphics2D g) {
        if (!canOpen) return;

        String prompt = "Press [E] to Open";
        g.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(prompt);

        int promptX = x + width / 2 - textWidth / 2;
        int promptY = y - 30;

        // Background
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(promptX - 10, promptY - 18, textWidth + 20, 25, 8, 8);

        // Text
        g.setColor(chestType == ChestType.MONTHLY ? new Color(200, 180, 255) : new Color(255, 215, 0));
        g.drawString(prompt, promptX, promptY);
    }

    /**
     * Draws the cooldown timer.
     */
    private void drawCooldownTimer(Graphics2D g) {
        SaveManager save = SaveManager.getInstance();
        long remaining;
        String label;

        if (chestType == ChestType.DAILY) {
            remaining = save.getDailyChestTimeRemaining();
            label = "Daily Chest";
        } else {
            remaining = save.getMonthlyChestTimeRemaining();
            label = "Monthly Chest";
        }

        String timeStr = SaveManager.formatTimeRemaining(remaining);

        g.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();

        String displayText = label + ": " + timeStr;
        int textWidth = fm.stringWidth(displayText);

        int textX = x + width / 2 - textWidth / 2;
        int textY = y - 20;

        // Background
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(textX - 10, textY - 15, textWidth + 20, 22, 6, 6);

        // Text
        g.setColor(new Color(255, 100, 100));
        g.drawString(displayText, textX, textY);
    }

    /**
     * Simple particle class for visual effects.
     */
    private static class Particle {
        float x, y, vx, vy;
        Color color;
        int life, maxLife;
        float size;

        Particle(float x, float y, float vx, float vy, Color color, int life) {
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

        void draw(Graphics2D g) {
            float alpha = (float) life / maxLife;
            Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(),
                (int)(alpha * 255));
            g.setColor(c);
            int s = (int)(size * alpha);
            g.fillOval((int)x - s/2, (int)y - s/2, s, s);
        }
    }
}
