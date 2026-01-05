package tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * Utility to generate placeholder GIF textures for items without textures.
 * Creates simple colored pixel art icons based on item names.
 */
public class GeneratePlaceholderGifs {

    // Color schemes for different item types
    private static final Color WEAPON_COLOR = new Color(180, 180, 200);      // Silver
    private static final Color RANGED_COLOR = new Color(139, 90, 43);        // Brown
    private static final Color ARMOR_COLOR = new Color(140, 140, 160);       // Steel
    private static final Color FOOD_COLOR = new Color(200, 150, 100);        // Tan
    private static final Color POTION_COLOR = new Color(100, 200, 100);      // Green
    private static final Color MATERIAL_COLOR = new Color(160, 140, 120);    // Stone
    private static final Color KEY_COLOR = new Color(200, 180, 0);           // Gold
    private static final Color COLLECTIBLE_COLOR = new Color(150, 100, 200); // Purple
    private static final Color CLOTHING_COLOR = new Color(100, 150, 200);    // Blue
    private static final Color BLOCK_COLOR = new Color(139, 90, 43);         // Brown
    private static final Color MYTHIC_GLOW = new Color(0, 255, 255);         // Cyan
    private static final Color LEGENDARY_GLOW = new Color(255, 165, 0);      // Orange

    // Size of generated icons
    private static final int SIZE = 32;

    public static void main(String[] args) {
        String basePath = "assets/items/";

        // List of missing items with their category hints
        Map<String, String> missingItems = new LinkedHashMap<>();

        // Weapons
        missingItems.put("void_blade", "mythic_weapon");
        missingItems.put("soul_reaver", "mythic_weapon");
        missingItems.put("time_warp_blade", "mythic_weapon");
        missingItems.put("frostmourne", "legendary_weapon");
        missingItems.put("thunder_hammer", "legendary_weapon");
        missingItems.put("shadow_dagger", "legendary_weapon");
        missingItems.put("phoenix_bow", "legendary_ranged");
        missingItems.put("celestial_bow", "mythic_ranged");
        missingItems.put("infinity_staff", "mythic_ranged");
        missingItems.put("ethereal_dragonslayer", "mythic_weapon");
        missingItems.put("electrified_katana", "legendary_weapon");
        missingItems.put("necromancers_blade", "epic_weapon");
        missingItems.put("soulbound_dagger", "rare_weapon");
        missingItems.put("epic_bow", "epic_ranged");
        missingItems.put("summoning_rod", "rare_ranged");
        missingItems.put("lightning_rod", "rare_ranged");
        missingItems.put("blazing_rod", "rare_ranged");
        missingItems.put("magic_fishing_rod", "uncommon_ranged");
        missingItems.put("crystal_sword", "rare_weapon");
        missingItems.put("vampiric_blade", "rare_weapon");
        missingItems.put("poison_dagger", "rare_weapon");

        // Armor
        missingItems.put("steel_shield", "uncommon_armor");
        missingItems.put("sentinel_gauntlets", "uncommon_armor");
        missingItems.put("gold_armor_helmet", "uncommon_armor");
        missingItems.put("gold_armor_chest", "uncommon_armor");
        missingItems.put("gold_armor_legs", "uncommon_armor");
        missingItems.put("gold_armor_boots", "uncommon_armor");
        missingItems.put("chainmail_armor", "common_armor");
        missingItems.put("dragon_scale_armor", "legendary_armor");
        missingItems.put("fancy_boots", "uncommon_armor");
        missingItems.put("phoenix_crown", "epic_armor");
        missingItems.put("void_armor", "legendary_armor");
        missingItems.put("celestial_robes", "mythic_armor");
        missingItems.put("titan_gauntlets", "legendary_armor");

        // Clothing
        missingItems.put("green_dress", "common_clothing");
        missingItems.put("orange_dress", "common_clothing");
        missingItems.put("black_dress", "uncommon_clothing");
        missingItems.put("hat", "common_clothing");
        missingItems.put("pants", "common_clothing");
        missingItems.put("shirt", "common_clothing");
        missingItems.put("three_piece_suit", "uncommon_clothing");
        missingItems.put("swimwear", "common_clothing");
        missingItems.put("yellow_cloak", "uncommon_clothing");
        missingItems.put("chameleon_cloak", "rare_clothing");
        missingItems.put("witchs_hat", "uncommon_clothing");
        missingItems.put("gown_forgotten_verses", "epic_clothing");

        // Potions
        missingItems.put("lucky_potion", "rare_potion");
        missingItems.put("honey_potion", "common_potion");
        missingItems.put("brewed_potion", "common_potion");
        missingItems.put("elixir_of_immortality", "mythic_potion");
        missingItems.put("potion_of_ascension", "legendary_potion");
        missingItems.put("essence_of_dragon", "legendary_potion");

        // Food
        missingItems.put("cake", "uncommon_food");
        missingItems.put("cookies", "common_food");
        missingItems.put("melon", "common_food");
        missingItems.put("pumpkin", "common_food");
        missingItems.put("salmon", "common_food");
        missingItems.put("chicken_egg", "common_food");

        // Materials
        missingItems.put("iron_ore", "common_material");
        missingItems.put("gold_coins", "common_material");
        missingItems.put("copper_ingots", "common_material");
        missingItems.put("gold_bars", "uncommon_material");
        missingItems.put("iron_bars", "common_material");
        missingItems.put("leather", "common_material");
        missingItems.put("yarn", "common_material");
        missingItems.put("flour", "common_material");
        missingItems.put("mysterious_gemstone", "rare_material");
        missingItems.put("dragon_egg", "legendary_material");
        missingItems.put("bonemite", "common_material");
        missingItems.put("ink", "common_material");
        missingItems.put("planks", "common_material");
        missingItems.put("rocks", "common_material");
        missingItems.put("rope", "common_material");
        missingItems.put("sapling", "common_material");
        missingItems.put("machine_parts", "uncommon_material");
        missingItems.put("skull", "uncommon_material");
        missingItems.put("mana_leaf", "uncommon_material");

        // Tools
        missingItems.put("shears", "common_tool");
        missingItems.put("fishing_rod", "common_tool");
        missingItems.put("walking_stick", "common_tool");

        // Collectibles
        missingItems.put("orb", "uncommon_collectible");
        missingItems.put("ancient_pottery", "uncommon_collectible");
        missingItems.put("music_disc", "uncommon_collectible");
        missingItems.put("music_player", "rare_collectible");
        missingItems.put("journal", "common_collectible");
        missingItems.put("painting_wolves", "uncommon_collectible");
        missingItems.put("painting_dog", "uncommon_collectible");
        missingItems.put("marbles", "common_collectible");
        missingItems.put("nametag", "uncommon_collectible");
        missingItems.put("candle", "common_collectible");
        missingItems.put("mysterious_candle", "rare_collectible");
        missingItems.put("water_bottle", "common_collectible");
        missingItems.put("saddle", "uncommon_collectible");
        missingItems.put("backpack", "uncommon_collectible");
        missingItems.put("personalized_banner", "uncommon_collectible");
        missingItems.put("frog", "common_collectible");
        missingItems.put("jack_o_lantern", "uncommon_collectible");
        missingItems.put("rocket", "uncommon_collectible");
        missingItems.put("wind_charge", "uncommon_collectible");
        missingItems.put("undead_scroll", "rare_collectible");
        missingItems.put("trip_wire_trap", "uncommon_collectible");
        missingItems.put("magic_lantern", "uncommon_collectible");
        missingItems.put("crucible", "uncommon_collectible");
        missingItems.put("mirror_realms", "epic_collectible");
        missingItems.put("rowboat", "uncommon_collectible");
        missingItems.put("crystal_summer", "legendary_collectible");
        missingItems.put("crystal_winter", "legendary_collectible");
        missingItems.put("heart_of_eternity", "mythic_collectible");
        missingItems.put("eye_of_cosmos", "mythic_collectible");
        missingItems.put("philosophers_stone", "mythic_collectible");
        missingItems.put("ankh_of_rebirth", "legendary_collectible");
        missingItems.put("ancient_crown", "epic_collectible");
        missingItems.put("demon_horn", "epic_collectible");
        missingItems.put("angel_feather", "epic_collectible");
        missingItems.put("void_shard", "legendary_collectible");
        missingItems.put("lucky_coin", "rare_collectible");
        missingItems.put("treasure_map", "rare_collectible");

        // Blocks
        missingItems.put("dirt_block", "common_block");
        missingItems.put("grass_block", "common_block");
        missingItems.put("stone_block", "common_block");
        missingItems.put("cobblestone_block", "common_block");
        missingItems.put("wood_block", "common_block");
        missingItems.put("brick_block", "common_block");
        missingItems.put("sand_block", "common_block");
        missingItems.put("glass_block", "uncommon_block");
        missingItems.put("leaves_block", "common_block");

        int generated = 0;
        for (Map.Entry<String, String> entry : missingItems.entrySet()) {
            String itemId = entry.getKey();
            String type = entry.getValue();
            String path = basePath + itemId + ".gif";

            File file = new File(path);
            if (!file.exists()) {
                try {
                    BufferedImage img = generateIcon(itemId, type);
                    ImageIO.write(img, "gif", file);
                    generated++;
                    System.out.println("Generated: " + path);
                } catch (IOException e) {
                    System.err.println("Failed to generate: " + path + " - " + e.getMessage());
                }
            }
        }

        System.out.println("\nGenerated " + generated + " placeholder GIFs");
    }

    /**
     * Generates a simple pixel art icon for an item.
     */
    private static BufferedImage generateIcon(String itemId, String type) {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Parse type for rarity and category
        String[] parts = type.split("_");
        String rarity = parts[0];
        String category = parts.length > 1 ? parts[1] : "collectible";

        // Get base color for category
        Color baseColor = getBaseColor(category);
        Color glowColor = getGlowColor(rarity);

        // Fill background with transparency
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, SIZE, SIZE);
        g.setComposite(AlphaComposite.SrcOver);

        // Draw glow for rare+ items
        if (glowColor != null) {
            g.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 80));
            g.fillOval(2, 2, SIZE - 4, SIZE - 4);
        }

        // Draw item shape based on category
        drawItemShape(g, category, baseColor, itemId);

        // Add sparkle for legendary/mythic
        if (rarity.equals("legendary") || rarity.equals("mythic")) {
            drawSparkles(g, glowColor);
        }

        g.dispose();
        return img;
    }

    private static Color getBaseColor(String category) {
        switch (category) {
            case "weapon": return WEAPON_COLOR;
            case "ranged": return RANGED_COLOR;
            case "armor": return ARMOR_COLOR;
            case "food": return FOOD_COLOR;
            case "potion": return POTION_COLOR;
            case "material": return MATERIAL_COLOR;
            case "key": return KEY_COLOR;
            case "collectible": return COLLECTIBLE_COLOR;
            case "clothing": return CLOTHING_COLOR;
            case "block": return BLOCK_COLOR;
            case "tool": return MATERIAL_COLOR;
            default: return Color.GRAY;
        }
    }

    private static Color getGlowColor(String rarity) {
        switch (rarity) {
            case "mythic": return MYTHIC_GLOW;
            case "legendary": return LEGENDARY_GLOW;
            case "epic": return new Color(180, 30, 255);
            case "rare": return new Color(30, 100, 255);
            default: return null;
        }
    }

    private static void drawItemShape(Graphics2D g, String category, Color baseColor, String itemId) {
        Color darker = baseColor.darker();
        Color lighter = baseColor.brighter();

        switch (category) {
            case "weapon":
                // Sword shape
                g.setColor(lighter);
                g.fillRect(14, 4, 4, 18);  // Blade
                g.setColor(baseColor);
                g.fillRect(15, 5, 2, 16);
                g.setColor(darker);
                g.fillRect(10, 22, 12, 3);  // Guard
                g.setColor(new Color(139, 90, 43));
                g.fillRect(14, 25, 4, 6);  // Handle
                break;

            case "ranged":
                // Bow shape
                g.setColor(new Color(139, 90, 43));
                g.drawArc(8, 4, 16, 24, 270, 180);  // Bow curve
                g.setColor(baseColor);
                g.drawLine(8, 6, 8, 26);  // String
                break;

            case "armor":
                // Chestplate shape
                g.setColor(baseColor);
                g.fillRoundRect(6, 6, 20, 20, 4, 4);
                g.setColor(darker);
                g.fillRect(12, 8, 8, 4);  // Neck
                g.setColor(lighter);
                g.fillRect(10, 14, 12, 8);  // Chest
                break;

            case "clothing":
                // Shirt shape
                g.setColor(baseColor);
                g.fillRect(8, 8, 16, 16);
                g.setColor(darker);
                g.fillRect(4, 8, 4, 12);   // Left sleeve
                g.fillRect(24, 8, 4, 12);  // Right sleeve
                g.setColor(lighter);
                g.fillRect(12, 10, 8, 4);  // Collar
                break;

            case "food":
                // Round food shape
                g.setColor(baseColor);
                g.fillOval(6, 6, 20, 20);
                g.setColor(lighter);
                g.fillOval(10, 10, 6, 6);  // Highlight
                break;

            case "potion":
                // Potion bottle shape
                g.setColor(new Color(200, 200, 220));
                g.fillRect(12, 4, 8, 6);   // Neck
                g.setColor(baseColor);
                g.fillOval(6, 10, 20, 18); // Body
                g.setColor(lighter);
                g.fillOval(10, 14, 6, 8);  // Liquid highlight
                break;

            case "material":
                // Ingot/material shape
                g.setColor(baseColor);
                int[] xPoints = {8, 16, 24, 16};
                int[] yPoints = {16, 6, 16, 26};
                g.fillPolygon(xPoints, yPoints, 4);
                g.setColor(lighter);
                g.drawPolygon(xPoints, yPoints, 4);
                break;

            case "collectible":
                // Star/gem shape
                g.setColor(baseColor);
                drawStar(g, 16, 16, 10, 5);
                g.setColor(lighter);
                drawStar(g, 16, 16, 5, 5);
                break;

            case "block":
                // Block shape
                Color blockColor = getBlockColor(itemId);
                g.setColor(blockColor);
                g.fillRect(4, 4, 24, 24);
                g.setColor(blockColor.brighter());
                g.drawRect(4, 4, 24, 24);
                g.setColor(blockColor.darker());
                g.fillRect(6, 6, 8, 8);  // Pattern
                g.fillRect(18, 18, 8, 8);
                break;

            case "tool":
                // Pickaxe shape
                g.setColor(baseColor);
                g.fillRect(6, 6, 14, 4);   // Head
                g.setColor(new Color(139, 90, 43));
                g.fillRect(14, 10, 4, 16); // Handle
                break;

            default:
                g.setColor(baseColor);
                g.fillOval(6, 6, 20, 20);
        }
    }

    private static Color getBlockColor(String itemId) {
        if (itemId.contains("dirt")) return new Color(139, 90, 43);
        if (itemId.contains("grass")) return new Color(34, 139, 34);
        if (itemId.contains("stone")) return new Color(128, 128, 128);
        if (itemId.contains("cobble")) return new Color(100, 100, 100);
        if (itemId.contains("wood")) return new Color(160, 82, 45);
        if (itemId.contains("brick")) return new Color(178, 34, 34);
        if (itemId.contains("sand")) return new Color(238, 214, 175);
        if (itemId.contains("glass")) return new Color(200, 220, 255);
        if (itemId.contains("leaves")) return new Color(34, 139, 34);
        return new Color(128, 128, 128);
    }

    private static void drawStar(Graphics2D g, int cx, int cy, int r, int points) {
        int[] xPoints = new int[points * 2];
        int[] yPoints = new int[points * 2];

        for (int i = 0; i < points * 2; i++) {
            double angle = Math.PI / 2 + i * Math.PI / points;
            int radius = (i % 2 == 0) ? r : r / 2;
            xPoints[i] = cx + (int)(Math.cos(angle) * radius);
            yPoints[i] = cy - (int)(Math.sin(angle) * radius);
        }

        g.fillPolygon(xPoints, yPoints, points * 2);
    }

    private static void drawSparkles(Graphics2D g, Color color) {
        if (color == null) return;
        g.setColor(color);

        // Small sparkle points
        g.fillRect(4, 4, 2, 2);
        g.fillRect(26, 8, 2, 2);
        g.fillRect(6, 24, 2, 2);
        g.fillRect(24, 22, 2, 2);
    }
}
