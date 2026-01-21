package scene.creative;

import entity.*;
import block.*;
import graphics.AssetLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Manages the palette system for the Creative Scene level editor.
 * Handles palette initialization, sorting, item creation, and palette rendering.
 * Extracted from CreativeScene for better separation of concerns.
 */
public class CreativePaletteManager {

    // Palette constants
    public static final int PALETTE_WIDTH = 200;
    public static final int PALETTE_ITEM_SIZE = 48;
    public static final int PALETTE_ITEMS_PER_ROW = 3;
    public static final int PALETTE_VISIBLE_ROWS = 8;

    // Palette data
    private List<PaletteItem> blockPalette;
    private List<PaletteItem> movingBlockPalette;
    private List<PaletteItem> itemPalette;
    private List<PaletteItem> mobPalette;
    private List<PaletteItem> lightPalette;
    private List<PaletteItem> interactivePalette;
    private List<PaletteItem> parallaxPalette;
    private List<PaletteItem> sortedItemPalette;

    // State
    private PaletteCategory currentCategory = PaletteCategory.BLOCKS;
    private int selectedPaletteIndex = 0;
    private int paletteScrollOffset = 0;
    private ItemSortMode itemSortMode = ItemSortMode.RARITY;

    // Block textures cache
    private Map<BlockType, BufferedImage> blockTextures;

    /**
     * Palette categories available in the creative editor.
     */
    public enum PaletteCategory {
        BLOCKS("Blocks"),
        MOVING_BLOCKS("Moving"),
        ITEMS("Items"),
        MOBS("Mobs"),
        LIGHTS("Lights"),
        INTERACTIVE("Interactive"),
        PARALLAX("Parallax");

        private final String displayName;

        PaletteCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Sort modes for the item palette.
     */
    public enum ItemSortMode {
        RARITY("Rarity"),
        ALPHABETICAL("A-Z");

        private final String displayName;

        ItemSortMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Represents an item in the palette.
     */
    public static class PaletteItem {
        public String id;
        public String displayName;
        public BufferedImage icon;
        public Object data;

        public PaletteItem(String id, String displayName, BufferedImage icon, Object data) {
            this.id = id;
            this.displayName = displayName;
            this.icon = icon;
            this.data = data;
        }
    }

    public CreativePaletteManager() {
        blockTextures = new HashMap<>();
    }

    /**
     * Initialize all palettes with available content.
     */
    public void initialize() {
        // Preload block textures
        BlockRegistry.getInstance().preloadAllTextures();
        for (BlockType type : BlockType.values()) {
            blockTextures.put(type, BlockRegistry.getInstance().getTexture(type));
        }

        initializeBlockPalette();
        initializeMovingBlockPalette();
        initializeItemPalette();
        initializeMobPalette();
        initializeLightPalette();
        initializeInteractivePalette();
        initializeParallaxPalette();

        sortItemPalette();
    }

    private void initializeBlockPalette() {
        blockPalette = new ArrayList<>();
        for (BlockType type : BlockType.values()) {
            BufferedImage icon = BlockRegistry.getInstance().getTexture(type);
            if (icon != null) {
                BufferedImage scaledIcon = scaleImage(icon, PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE);
                blockPalette.add(new PaletteItem(type.name(), type.getDisplayName(), scaledIcon, type));
            }
        }
    }

    private void initializeMovingBlockPalette() {
        movingBlockPalette = new ArrayList<>();

        BlockType[] movableTypes = {BlockType.STONE, BlockType.COBBLESTONE, BlockType.BRICK,
                                    BlockType.WOOD, BlockType.IRON_ORE, BlockType.GOLD_ORE};

        String[][] movementPatterns = {
            {"HORIZONTAL", "Horizontal", "Moves left and right"},
            {"VERTICAL", "Vertical", "Moves up and down"},
            {"CIRCULAR", "Circular", "Moves in a circle"}
        };

        for (BlockType type : movableTypes) {
            BufferedImage baseIcon = BlockRegistry.getInstance().getTexture(type);
            if (baseIcon != null) {
                for (String[] pattern : movementPatterns) {
                    BufferedImage icon = createMovingBlockIcon(baseIcon, pattern[0]);
                    Map<String, Object> movingData = new HashMap<>();
                    movingData.put("blockType", type);
                    movingData.put("movementPattern", pattern[0]);
                    movingData.put("speed", 2.0);
                    movingData.put("pauseTime", 30);
                    movingData.put("endX", 3);
                    movingData.put("endY", 0);
                    movingData.put("radius", 100.0);
                    String itemName = type.getDisplayName() + " (" + pattern[1] + ")";
                    movingBlockPalette.add(new PaletteItem(type.name() + "_" + pattern[0], itemName, icon, movingData));
                }
            }
        }
    }

    private void initializeItemPalette() {
        itemPalette = new ArrayList<>();
        ItemRegistry.initialize();

        Set<String> allItemIds = ItemRegistry.getAllItemIds();
        for (String itemId : allItemIds) {
            Item template = ItemRegistry.getTemplate(itemId);
            if (template != null) {
                BufferedImage icon = createItemIcon(itemId);
                itemPalette.add(new PaletteItem(itemId, template.getName(), icon, itemId));
            }
        }
    }

    private void initializeMobPalette() {
        mobPalette = new ArrayList<>();
        String[][] mobTypes = {
            {"zombie", "Zombie", "sprite_humanoid"},
            {"skeleton", "Skeleton", "sprite_humanoid"},
            {"goblin", "Goblin", "sprite_humanoid"},
            {"orc", "Orc", "sprite_humanoid"},
            {"bandit", "Bandit", "sprite_humanoid"},
            {"knight", "Knight", "sprite_humanoid"},
            {"mage", "Mage", "sprite_humanoid"},
            {"wolf", "Wolf", "sprite_quadruped"},
            {"bear", "Bear", "sprite_quadruped"},
            {"pig", "Pig", "sprite_quadruped"},
            {"cow", "Cow", "sprite_quadruped"},
            {"sheep", "Sheep", "sprite_quadruped"},
            {"frog", "Frog", "frog"}
        };

        for (String[] mob : mobTypes) {
            BufferedImage icon = createMobIcon(mob[0]);
            Map<String, String> mobData = new HashMap<>();
            mobData.put("subType", mob[0]);
            mobData.put("mobType", mob[2]);
            mobData.put("behavior", mob[0].equals("pig") || mob[0].equals("cow") ||
                        mob[0].equals("sheep") || mob[0].equals("frog") ? "passive" : "hostile");
            mobPalette.add(new PaletteItem(mob[0], mob[1], icon, mobData));
        }
    }

    private void initializeLightPalette() {
        lightPalette = new ArrayList<>();
        String[] lightTypes = {"torch", "campfire", "lantern", "magic", "crystal"};
        for (String lightType : lightTypes) {
            BufferedImage icon = createLightIcon(lightType);
            String displayName = Character.toUpperCase(lightType.charAt(0)) + lightType.substring(1);
            lightPalette.add(new PaletteItem(lightType, displayName, icon, lightType));
        }
    }

    private void initializeInteractivePalette() {
        interactivePalette = new ArrayList<>();

        // Doors
        String[][] doorTypes = {
            {"wooden_door", "Wooden Door", "assets/doors/wooden_door.gif"},
            {"iron_door", "Iron Door", "assets/doors/iron_door.gif"},
            {"stone_door", "Stone Door", "assets/doors/stone_door.gif"}
        };
        for (String[] door : doorTypes) {
            BufferedImage icon = createDoorIcon(door[2]);
            Map<String, Object> doorData = new HashMap<>();
            doorData.put("type", "door");
            doorData.put("texturePath", door[2]);
            doorData.put("linkId", "");
            doorData.put("actionType", "none");
            doorData.put("actionTarget", "");
            interactivePalette.add(new PaletteItem(door[0], door[1], icon, doorData));
        }

        // Buttons
        String[][] buttonTypes = {
            {"stone_button", "Stone Button", "assets/buttons/stone_button.gif"},
            {"wooden_button", "Wooden Button", "assets/buttons/wooden_button.gif"},
            {"pressure_plate", "Pressure Plate", "assets/buttons/pressure_plate.gif"}
        };
        for (String[] button : buttonTypes) {
            BufferedImage icon = createButtonIcon(button[2]);
            Map<String, Object> buttonData = new HashMap<>();
            buttonData.put("type", "button");
            buttonData.put("texturePath", button[2]);
            buttonData.put("linkId", "");
            buttonData.put("linkedDoorIds", new ArrayList<String>());
            buttonData.put("buttonType", button[0].contains("pressure") ? "momentary" : "toggle");
            buttonData.put("actionType", "none");
            buttonData.put("actionTarget", "");
            interactivePalette.add(new PaletteItem(button[0], button[1], icon, buttonData));
        }

        // Vaults
        String[][] vaultTypes = {
            {"player_vault", "Player Vault", "assets/vault/player_vault.gif", "PLAYER_VAULT"},
            {"storage_chest", "Storage Chest (48)", "assets/vault/storage_chest.gif", "STORAGE_CHEST"},
            {"large_chest", "Large Chest (32)", "assets/vault/large_chest.gif", "LARGE_CHEST"},
            {"medium_chest", "Medium Chest (16)", "assets/vault/medium_chest.gif", "MEDIUM_CHEST"},
            {"ancient_pottery", "Ancient Pottery (5)", "assets/items/ancient_pottery/idle.gif", "ANCIENT_POTTERY"}
        };
        for (String[] vault : vaultTypes) {
            BufferedImage icon = createVaultIcon(vault[2]);
            Map<String, Object> vaultData = new HashMap<>();
            vaultData.put("type", "vault");
            vaultData.put("texturePath", vault[2]);
            vaultData.put("vaultType", vault[3]);
            interactivePalette.add(new PaletteItem(vault[0], vault[1], icon, vaultData));
        }
    }

    private void initializeParallaxPalette() {
        parallaxPalette = new ArrayList<>();
        String[][] parallaxOptions = {
            {"sky", "Sky Background", "assets/parallax/sky.png", "-2", "0.1"},
            {"buildings_far", "Distant Buildings", "assets/parallax/buildings_far.png", "-1", "0.3"},
            {"buildings_mid", "Mid Buildings", "assets/parallax/buildings_mid.png", "0", "0.5"},
            {"buildings_near", "Near Buildings", "assets/parallax/buildings_near.png", "1", "0.7"},
            {"foreground", "Foreground", "assets/parallax/foreground.png", "2", "1.2"},
            {"background", "Main Background", "assets/background.png", "-2", "0.2"}
        };

        for (String[] option : parallaxOptions) {
            BufferedImage icon = createParallaxIcon(option[2]);
            Map<String, String> data = new HashMap<>();
            data.put("name", option[0]);
            data.put("path", option[2]);
            data.put("zOrder", option[3]);
            data.put("scrollSpeed", option[4]);
            parallaxPalette.add(new PaletteItem(option[0], option[1], icon, data));
        }
    }

    // ==================== Icon Creation Methods ====================

    public BufferedImage scaleImage(BufferedImage source, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return scaled;
    }

    public BufferedImage createItemIcon(String itemId) {
        String[] extensions = {".gif", ".png"};
        for (String ext : extensions) {
            String path = "assets/items/" + itemId + ext;
            AssetLoader.ImageAsset asset = AssetLoader.load(path);
            if (asset != null && asset.staticImage != null) {
                return scaleImage(asset.staticImage, PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE);
            }
        }

        // Fallback placeholder
        BufferedImage icon = new BufferedImage(PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color color = getItemColor(itemId);
        g.setColor(color);
        g.fillRoundRect(4, 4, PALETTE_ITEM_SIZE - 8, PALETTE_ITEM_SIZE - 8, 8, 8);
        g.setColor(Color.BLACK);
        g.drawRoundRect(4, 4, PALETTE_ITEM_SIZE - 8, PALETTE_ITEM_SIZE - 8, 8, 8);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        String letter = itemId.substring(0, 1).toUpperCase();
        FontMetrics fm = g.getFontMetrics();
        int textX = (PALETTE_ITEM_SIZE - fm.stringWidth(letter)) / 2;
        int textY = (PALETTE_ITEM_SIZE + fm.getAscent() - fm.getDescent()) / 2;
        g.setColor(Color.WHITE);
        g.drawString(letter, textX, textY);

        g.dispose();
        return icon;
    }

    private Color getItemColor(String itemId) {
        if (itemId.contains("sword") || itemId.contains("axe") || itemId.contains("knife")) {
            return new Color(150, 150, 180);
        } else if (itemId.contains("bow") || itemId.contains("crossbow")) {
            return new Color(139, 90, 43);
        } else if (itemId.contains("staff") || itemId.contains("wand") || itemId.contains("magic")) {
            return new Color(138, 43, 226);
        } else if (itemId.contains("potion")) {
            return itemId.contains("health") ? new Color(255, 50, 50) : new Color(50, 50, 255);
        } else if (itemId.contains("gold")) {
            return new Color(255, 215, 0);
        } else if (itemId.contains("fire")) {
            return new Color(255, 100, 0);
        } else if (itemId.contains("ice")) {
            return new Color(100, 200, 255);
        }
        return Color.GRAY;
    }

    public BufferedImage createMovingBlockIcon(BufferedImage baseIcon, String pattern) {
        BufferedImage icon = new BufferedImage(PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(scaleImage(baseIcon, PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE), 0, 0, null);

        g.setColor(new Color(255, 255, 0, 200));
        g.setStroke(new BasicStroke(2));

        int cx = PALETTE_ITEM_SIZE / 2;
        int cy = PALETTE_ITEM_SIZE / 2;

        switch (pattern) {
            case "HORIZONTAL":
                g.drawLine(8, cy, 16, cy);
                g.drawLine(8, cy, 12, cy - 4);
                g.drawLine(8, cy, 12, cy + 4);
                g.drawLine(PALETTE_ITEM_SIZE - 8, cy, PALETTE_ITEM_SIZE - 16, cy);
                g.drawLine(PALETTE_ITEM_SIZE - 8, cy, PALETTE_ITEM_SIZE - 12, cy - 4);
                g.drawLine(PALETTE_ITEM_SIZE - 8, cy, PALETTE_ITEM_SIZE - 12, cy + 4);
                break;
            case "VERTICAL":
                g.drawLine(cx, 8, cx, 16);
                g.drawLine(cx, 8, cx - 4, 12);
                g.drawLine(cx, 8, cx + 4, 12);
                g.drawLine(cx, PALETTE_ITEM_SIZE - 8, cx, PALETTE_ITEM_SIZE - 16);
                g.drawLine(cx, PALETTE_ITEM_SIZE - 8, cx - 4, PALETTE_ITEM_SIZE - 12);
                g.drawLine(cx, PALETTE_ITEM_SIZE - 8, cx + 4, PALETTE_ITEM_SIZE - 12);
                break;
            case "CIRCULAR":
                g.drawArc(12, 12, PALETTE_ITEM_SIZE - 24, PALETTE_ITEM_SIZE - 24, 45, 270);
                g.drawLine(cx + 6, 16, cx + 10, 12);
                g.drawLine(cx + 6, 16, cx + 2, 12);
                break;
        }

        g.dispose();
        return icon;
    }

    public BufferedImage createMobIcon(String mobType) {
        String[] paths = {
            "assets/mobs/" + mobType + "/idle.gif",
            "assets/mobs/" + mobType + "/sprites/idle.gif",
            "assets/mobs/" + mobType + "/idle.png"
        };

        for (String path : paths) {
            AssetLoader.ImageAsset asset = AssetLoader.load(path);
            if (asset != null && asset.staticImage != null) {
                return scaleImage(asset.staticImage, PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE);
            }
        }

        // Fallback placeholder
        BufferedImage icon = new BufferedImage(PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color color = getMobColor(mobType);
        g.setColor(color);

        if (mobType.equals("wolf") || mobType.equals("bear") || mobType.equals("pig") ||
            mobType.equals("cow") || mobType.equals("sheep") || mobType.equals("frog")) {
            g.fillOval(6, 14, PALETTE_ITEM_SIZE - 12, PALETTE_ITEM_SIZE - 22);
            g.fillOval(8, 8, 16, 14);
        } else {
            g.fillOval(16, 4, 16, 16);
            g.fillRect(20, 20, 8, 16);
            g.fillRect(14, 36, 6, 8);
            g.fillRect(28, 36, 6, 8);
        }

        g.setColor(Color.BLACK);
        g.drawOval(16, 4, 16, 16);

        g.dispose();
        return icon;
    }

    private Color getMobColor(String mobType) {
        switch (mobType) {
            case "zombie": return new Color(100, 150, 100);
            case "skeleton": return new Color(220, 220, 200);
            case "goblin": return new Color(50, 150, 50);
            case "orc": return new Color(100, 120, 80);
            case "bandit": return new Color(120, 80, 60);
            case "knight": return new Color(180, 180, 200);
            case "mage": return new Color(100, 50, 150);
            case "wolf": return new Color(100, 100, 100);
            case "bear": return new Color(139, 90, 43);
            case "pig": return new Color(255, 180, 180);
            case "cow": return new Color(100, 80, 60);
            case "sheep": return new Color(240, 240, 240);
            case "frog": return new Color(128, 60, 180);
            default: return Color.GRAY;
        }
    }

    public BufferedImage createLightIcon(String lightType) {
        BufferedImage icon = new BufferedImage(PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color glowColor;
        switch (lightType) {
            case "torch": glowColor = new Color(255, 200, 100); break;
            case "campfire": glowColor = new Color(255, 150, 50); break;
            case "lantern": glowColor = new Color(255, 240, 180); break;
            case "magic": glowColor = new Color(150, 150, 255); break;
            case "crystal": glowColor = new Color(100, 255, 200); break;
            default: glowColor = Color.YELLOW;
        }

        g.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 50));
        g.fillOval(4, 4, PALETTE_ITEM_SIZE - 8, PALETTE_ITEM_SIZE - 8);

        g.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 150));
        g.fillOval(12, 12, PALETTE_ITEM_SIZE - 24, PALETTE_ITEM_SIZE - 24);

        g.setColor(glowColor);
        g.fillOval(18, 18, 12, 12);

        g.dispose();
        return icon;
    }

    public BufferedImage createParallaxIcon(String imagePath) {
        AssetLoader.ImageAsset asset = AssetLoader.load(imagePath);
        if (asset != null && asset.staticImage != null) {
            return scaleImage(asset.staticImage, PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE);
        }

        BufferedImage icon = new BufferedImage(PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(100, 150, 200),
            0, PALETTE_ITEM_SIZE, new Color(50, 80, 120)
        );
        g.setPaint(gradient);
        g.fillRoundRect(2, 2, PALETTE_ITEM_SIZE - 4, PALETTE_ITEM_SIZE - 4, 6, 6);

        g.setColor(new Color(255, 255, 255, 100));
        for (int i = 10; i < PALETTE_ITEM_SIZE - 10; i += 8) {
            g.drawLine(4, i, PALETTE_ITEM_SIZE - 4, i);
        }

        g.setColor(Color.BLACK);
        g.drawRoundRect(2, 2, PALETTE_ITEM_SIZE - 4, PALETTE_ITEM_SIZE - 4, 6, 6);

        g.dispose();
        return icon;
    }

    public BufferedImage createDoorIcon(String texturePath) {
        AssetLoader.ImageAsset asset = AssetLoader.load(texturePath);
        if (asset != null && asset.staticImage != null) {
            return scaleImage(asset.staticImage, PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE);
        }

        BufferedImage icon = new BufferedImage(PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(139, 90, 43));
        g.fillRect(12, 4, 24, 40);
        g.setColor(new Color(101, 67, 33));
        g.setStroke(new BasicStroke(2));
        g.drawRect(12, 4, 24, 40);
        g.setColor(new Color(255, 215, 0));
        g.fillOval(30, 22, 4, 4);

        g.dispose();
        return icon;
    }

    public BufferedImage createButtonIcon(String texturePath) {
        AssetLoader.ImageAsset asset = AssetLoader.load(texturePath);
        if (asset != null && asset.staticImage != null) {
            return scaleImage(asset.staticImage, PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE);
        }

        BufferedImage icon = new BufferedImage(PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(80, 80, 80));
        g.fillRoundRect(8, 20, 32, 12, 4, 4);
        g.setColor(new Color(120, 120, 120));
        g.fillRoundRect(10, 18, 28, 10, 4, 4);
        g.setColor(new Color(160, 160, 160));
        g.fillRoundRect(12, 20, 24, 4, 2, 2);

        g.dispose();
        return icon;
    }

    public BufferedImage createVaultIcon(String texturePath) {
        AssetLoader.ImageAsset asset = AssetLoader.load(texturePath);
        if (asset != null && asset.staticImage != null) {
            return scaleImage(asset.staticImage, PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE);
        }

        BufferedImage icon = new BufferedImage(PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(139, 90, 43));
        g.fillRoundRect(6, 18, 36, 26, 6, 6);
        g.setColor(new Color(101, 67, 33));
        g.fillRoundRect(4, 10, 40, 14, 4, 4);
        g.setColor(new Color(169, 169, 169));
        g.fillRect(10, 18, 3, 26);
        g.fillRect(35, 18, 3, 26);
        g.fillRect(21, 18, 6, 26);
        g.setColor(new Color(255, 215, 0));
        g.fillOval(21, 28, 6, 8);

        g.dispose();
        return icon;
    }

    // ==================== Sorting Methods ====================

    public void sortItemPalette() {
        sortedItemPalette = new ArrayList<>(itemPalette);

        switch (itemSortMode) {
            case RARITY:
                sortedItemPalette.sort((a, b) -> {
                    Item itemA = ItemRegistry.getTemplate(a.id);
                    Item itemB = ItemRegistry.getTemplate(b.id);
                    if (itemA == null || itemB == null) return 0;
                    int rarityCompare = itemB.getRarity().ordinal() - itemA.getRarity().ordinal();
                    if (rarityCompare == 0) {
                        return a.displayName.compareToIgnoreCase(b.displayName);
                    }
                    return rarityCompare;
                });
                break;
            case ALPHABETICAL:
                sortedItemPalette.sort((a, b) -> a.displayName.compareToIgnoreCase(b.displayName));
                break;
        }
    }

    public void toggleItemSortMode() {
        ItemSortMode[] modes = ItemSortMode.values();
        int currentIndex = itemSortMode.ordinal();
        itemSortMode = modes[(currentIndex + 1) % modes.length];
        sortItemPalette();
        selectedPaletteIndex = 0;
        paletteScrollOffset = 0;
    }

    // ==================== Getters and State Management ====================

    public List<PaletteItem> getCurrentPalette() {
        switch (currentCategory) {
            case BLOCKS: return blockPalette;
            case MOVING_BLOCKS: return movingBlockPalette;
            case ITEMS: return sortedItemPalette != null ? sortedItemPalette : itemPalette;
            case MOBS: return mobPalette;
            case LIGHTS: return lightPalette;
            case INTERACTIVE: return interactivePalette;
            case PARALLAX: return parallaxPalette;
            default: return blockPalette;
        }
    }

    public PaletteItem getSelectedPaletteItem() {
        List<PaletteItem> palette = getCurrentPalette();
        if (selectedPaletteIndex >= 0 && selectedPaletteIndex < palette.size()) {
            return palette.get(selectedPaletteIndex);
        }
        return null;
    }

    public PaletteCategory getCurrentCategory() { return currentCategory; }
    public void setCurrentCategory(PaletteCategory category) {
        this.currentCategory = category;
        this.selectedPaletteIndex = 0;
        this.paletteScrollOffset = 0;
    }

    public int getSelectedPaletteIndex() { return selectedPaletteIndex; }
    public void setSelectedPaletteIndex(int index) { this.selectedPaletteIndex = index; }

    public int getPaletteScrollOffset() { return paletteScrollOffset; }
    public void setPaletteScrollOffset(int offset) { this.paletteScrollOffset = offset; }

    public ItemSortMode getItemSortMode() { return itemSortMode; }

    public Map<BlockType, BufferedImage> getBlockTextures() { return blockTextures; }

    public void cycleCategory() {
        PaletteCategory[] categories = PaletteCategory.values();
        int nextIndex = (currentCategory.ordinal() + 1) % categories.length;
        setCurrentCategory(categories[nextIndex]);
    }

    public void handleScroll(int scrollDirection) {
        List<PaletteItem> palette = getCurrentPalette();
        int totalRows = (int) Math.ceil((double) palette.size() / PALETTE_ITEMS_PER_ROW);
        int maxScroll = Math.max(0, totalRows - PALETTE_VISIBLE_ROWS);

        paletteScrollOffset += scrollDirection;
        paletteScrollOffset = Math.max(0, Math.min(paletteScrollOffset, maxScroll));
    }
}
