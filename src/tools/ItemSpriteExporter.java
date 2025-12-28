package tools;

import entity.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;

/**
 * Utility to export procedurally generated item sprites to PNG files.
 * Run this to populate the assets/items/ directory with sprites.
 *
 * Usage: java -cp bin tools.ItemSpriteExporter
 */
public class ItemSpriteExporter {

    private static final int ICON_SIZE = 16;
    private static final String OUTPUT_DIR = "assets/items/";

    public static void main(String[] args) {
        System.out.println("Item Sprite Exporter");
        System.out.println("====================\n");

        // Create output directory
        File outDir = new File(OUTPUT_DIR);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        // Initialize the registry
        ItemRegistry.initialize();

        // Export all registered items
        Set<String> itemIds = ItemRegistry.getAllItemIds();
        int exported = 0;
        int failed = 0;

        for (String itemId : itemIds) {
            try {
                Item item = ItemRegistry.create(itemId);
                if (item != null) {
                    BufferedImage sprite = generateItemSprite(item, itemId);
                    File outputFile = new File(OUTPUT_DIR + itemId + ".png");
                    ImageIO.write(sprite, "PNG", outputFile);
                    System.out.println("Exported: " + itemId + ".png");
                    exported++;
                }
            } catch (Exception e) {
                System.err.println("Failed to export: " + itemId + " - " + e.getMessage());
                failed++;
            }
        }

        System.out.println("\n====================");
        System.out.println("Exported: " + exported + " items");
        if (failed > 0) {
            System.out.println("Failed: " + failed + " items");
        }
        System.out.println("Output directory: " + outDir.getAbsolutePath());
    }

    private static BufferedImage generateItemSprite(Item item, String itemId) {
        String type = item.getCategory().name().toLowerCase();
        String name = item.getName();

        BufferedImage icon = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color primary = getUniqueItemColor(type, name);
        Color secondary = primary.darker();

        switch (type.toLowerCase()) {
            case "weapon":
                drawWeaponIcon(g, primary, secondary, name);
                break;
            case "ranged_weapon":
                drawRangedWeaponIcon(g, primary, secondary, name);
                break;
            case "armor":
                drawArmorIcon(g, primary, secondary, name);
                break;
            case "potion":
                drawPotionIcon(g, primary, name);
                break;
            case "food":
                drawFoodIcon(g, primary, secondary, name);
                break;
            case "tool":
                drawToolIcon(g, primary, secondary, name);
                break;
            case "material":
                drawMaterialIcon(g, primary, secondary, name, itemId);
                break;
            case "key":
                drawKeyIcon(g, primary, name);
                break;
            case "throwable":
                drawThrowableIcon(g, primary, secondary, name);
                break;
            default:
                drawDefaultIcon(g, primary, secondary, name);
        }

        g.dispose();
        return icon;
    }

    private static Color getUniqueItemColor(String type, String name) {
        Color baseColor = getTypeColor(type);
        int hash = name.hashCode();
        float hueShift = ((hash & 0xFF) - 128) / 512.0f;

        float[] hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);
        hsb[0] = (hsb[0] + hueShift + 1.0f) % 1.0f;
        hsb[1] = Math.min(1.0f, hsb[1] + ((hash >> 8) & 0xFF) / 1024.0f);
        hsb[2] = Math.min(1.0f, hsb[2] + ((hash >> 16) & 0xFF) / 1024.0f);

        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }

    private static Color getTypeColor(String type) {
        switch (type.toLowerCase()) {
            case "weapon": return new Color(192, 192, 210);
            case "ranged_weapon": return new Color(139, 90, 43);
            case "armor": return new Color(160, 160, 175);
            case "potion": return new Color(100, 200, 100);
            case "food": return new Color(210, 150, 90);
            case "tool": return new Color(139, 119, 101);
            case "material": return new Color(100, 180, 220);
            case "key": return new Color(218, 165, 32);
            case "throwable": return new Color(100, 100, 120);
            default: return new Color(150, 150, 150);
        }
    }

    private static void drawWeaponIcon(Graphics2D g, Color primary, Color secondary, String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("axe")) {
            g.setColor(new Color(139, 90, 43));
            g.fillRect(7, 4, 2, 11);
            g.setColor(primary);
            g.fillArc(3, 2, 10, 8, 90, 180);
            g.setColor(secondary);
            g.drawArc(3, 2, 10, 8, 90, 180);
        } else if (lowerName.contains("mace") || lowerName.contains("hammer")) {
            g.setColor(new Color(139, 90, 43));
            g.fillRect(7, 6, 2, 9);
            g.setColor(primary);
            g.fillOval(4, 2, 8, 8);
            g.setColor(secondary);
            g.drawOval(4, 2, 8, 8);
        } else if (lowerName.contains("dagger") || lowerName.contains("knife")) {
            g.setColor(secondary);
            g.fillRect(7, 3, 2, 7);
            g.setColor(primary);
            int[] tipX = {8, 6, 10};
            int[] tipY = {1, 4, 4};
            g.fillPolygon(tipX, tipY, 3);
            g.setColor(new Color(139, 90, 43));
            g.fillRect(6, 10, 4, 4);
        } else {
            g.setColor(secondary);
            g.fillRect(7, 2, 2, 10);
            g.setColor(primary);
            g.fillRect(6, 1, 4, 2);
            g.setColor(new Color(139, 90, 43));
            g.fillRect(6, 12, 4, 3);
            g.setColor(new Color(255, 215, 0));
            g.fillRect(4, 11, 8, 2);
        }
    }

    private static void drawRangedWeaponIcon(Graphics2D g, Color primary, Color secondary, String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("crossbow")) {
            g.setColor(primary);
            g.fillRect(3, 7, 10, 2);
            g.setColor(secondary);
            g.fillRect(6, 4, 4, 8);
            g.setColor(new Color(100, 100, 100));
            g.drawLine(2, 7, 13, 7);
        } else if (lowerName.contains("wand") || lowerName.contains("staff")) {
            g.setColor(primary);
            g.fillRect(7, 2, 2, 12);
            g.setColor(new Color(200, 100, 255));
            g.fillOval(5, 0, 6, 6);
            g.setColor(Color.WHITE);
            g.fillOval(7, 2, 2, 2);
        } else {
            g.setColor(primary);
            g.drawArc(3, 2, 10, 12, 60, 180);
            g.setColor(new Color(200, 200, 200));
            g.drawLine(4, 8, 12, 8);
        }
    }

    private static void drawArmorIcon(Graphics2D g, Color primary, Color secondary, String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("helmet") || lowerName.contains("hat")) {
            g.setColor(primary);
            g.fillArc(2, 4, 12, 10, 0, 180);
            g.fillRect(2, 9, 12, 4);
            g.setColor(secondary);
            g.drawArc(2, 4, 12, 10, 0, 180);
        } else if (lowerName.contains("boot")) {
            g.setColor(primary);
            g.fillRect(4, 4, 6, 8);
            g.fillRect(6, 10, 6, 4);
            g.setColor(secondary);
            g.drawRect(4, 4, 6, 8);
        } else {
            g.setColor(primary);
            g.fillRect(4, 2, 8, 12);
            g.setColor(secondary);
            g.fillRect(3, 4, 2, 8);
            g.fillRect(11, 4, 2, 8);
            g.setColor(primary.brighter());
            g.fillOval(6, 5, 4, 4);
        }
    }

    private static void drawPotionIcon(Graphics2D g, Color primary, String name) {
        g.setColor(new Color(200, 200, 220));
        g.fillRect(6, 1, 4, 3);
        g.setColor(new Color(180, 180, 200, 180));
        g.fillOval(3, 4, 10, 11);
        g.setColor(primary);
        g.fillOval(4, 6, 8, 8);
        g.setColor(new Color(255, 255, 255, 100));
        g.fillOval(5, 7, 3, 3);
    }

    private static void drawFoodIcon(Graphics2D g, Color primary, Color secondary, String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("apple")) {
            g.setColor(Color.RED);
            g.fillOval(3, 4, 10, 10);
            g.setColor(new Color(139, 69, 19));
            g.fillRect(7, 1, 2, 4);
            g.setColor(Color.GREEN);
            g.fillOval(9, 2, 4, 3);
        } else if (lowerName.contains("bread")) {
            g.setColor(new Color(210, 170, 100));
            g.fillRoundRect(2, 5, 12, 8, 4, 4);
            g.setColor(new Color(180, 140, 70));
            g.drawLine(4, 9, 12, 9);
        } else if (lowerName.contains("meat") || lowerName.contains("steak")) {
            g.setColor(new Color(180, 80, 60));
            g.fillOval(2, 4, 12, 10);
            g.setColor(new Color(255, 200, 180));
            g.fillOval(5, 6, 6, 5);
        } else {
            g.setColor(primary);
            g.fillOval(3, 4, 10, 10);
            g.setColor(secondary);
            g.drawOval(3, 4, 10, 10);
        }
    }

    private static void drawToolIcon(Graphics2D g, Color primary, Color secondary, String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("pickaxe")) {
            g.setColor(new Color(139, 90, 43));
            g.fillRect(7, 5, 2, 10);
            g.setColor(primary);
            g.fillRect(2, 2, 12, 4);
            int[] tipX = {2, 0, 4};
            int[] tipY = {4, 7, 7};
            g.fillPolygon(tipX, tipY, 3);
            int[] tipX2 = {14, 16, 12};
            int[] tipY2 = {4, 7, 7};
            g.fillPolygon(tipX2, tipY2, 3);
        } else if (lowerName.contains("shovel")) {
            g.setColor(new Color(139, 90, 43));
            g.fillRect(7, 3, 2, 10);
            g.setColor(primary);
            g.fillOval(4, 0, 8, 6);
        } else {
            g.setColor(new Color(139, 90, 43));
            g.fillRect(7, 6, 2, 9);
            g.setColor(primary);
            g.fillRect(4, 1, 8, 6);
        }
    }

    private static void drawMaterialIcon(Graphics2D g, Color primary, Color secondary, String name, String itemId) {
        String lowerName = name.toLowerCase();
        String lowerId = itemId.toLowerCase();

        // Check for ammo types
        if (lowerId.contains("arrow") || lowerName.contains("arrow")) {
            g.setColor(new Color(139, 90, 43));
            g.fillRect(2, 7, 10, 2);
            g.setColor(primary);
            int[] tipX = {12, 15, 12};
            int[] tipY = {6, 8, 10};
            g.fillPolygon(tipX, tipY, 3);
            g.setColor(new Color(200, 50, 50));
            g.fillRect(0, 6, 3, 1);
            g.fillRect(0, 9, 3, 1);
        } else if (lowerId.contains("bolt") || lowerName.contains("bolt")) {
            g.setColor(new Color(100, 100, 100));
            g.fillRect(3, 7, 8, 2);
            g.setColor(primary);
            int[] tipX = {11, 15, 11};
            int[] tipY = {6, 8, 10};
            g.fillPolygon(tipX, tipY, 3);
        } else if (lowerId.contains("mana") || lowerName.contains("mana") || lowerName.contains("crystal")) {
            g.setColor(new Color(100, 100, 255));
            int[] crystalX = {8, 4, 8, 12};
            int[] crystalY = {1, 8, 15, 8};
            g.fillPolygon(crystalX, crystalY, 4);
            g.setColor(new Color(150, 150, 255));
            g.drawPolygon(crystalX, crystalY, 4);
            g.setColor(new Color(200, 200, 255, 150));
            g.fillPolygon(new int[]{8, 6, 8}, new int[]{3, 7, 7}, 3);
        } else if (lowerName.contains("ingot")) {
            g.setColor(primary);
            g.fillRect(2, 6, 12, 6);
            g.setColor(secondary);
            g.fillRect(2, 6, 12, 2);
            g.setColor(primary.brighter());
            g.fillRect(3, 7, 4, 1);
        } else if (lowerName.contains("diamond") || lowerName.contains("gem")) {
            g.setColor(primary);
            int[] gemX = {8, 3, 5, 11, 13};
            int[] gemY = {2, 6, 14, 14, 6};
            g.fillPolygon(gemX, gemY, 5);
            g.setColor(primary.brighter());
            g.drawPolygon(gemX, gemY, 5);
        } else {
            g.setColor(primary);
            g.fillOval(3, 3, 10, 10);
            g.setColor(secondary);
            g.drawOval(3, 3, 10, 10);
        }
    }

    private static void drawKeyIcon(Graphics2D g, Color primary, String name) {
        g.setColor(primary);
        g.fillOval(2, 2, 8, 8);
        g.setColor(primary.darker());
        g.fillOval(4, 4, 4, 4);
        g.setColor(primary);
        g.fillRect(8, 5, 7, 3);
        g.fillRect(12, 7, 2, 3);
        g.fillRect(14, 7, 2, 2);
    }

    private static void drawThrowableIcon(Graphics2D g, Color primary, Color secondary, String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("knife")) {
            g.setColor(new Color(180, 180, 190));
            g.fillRect(6, 2, 4, 8);
            int[] tipX = {8, 5, 11};
            int[] tipY = {0, 3, 3};
            g.fillPolygon(tipX, tipY, 3);
            g.setColor(new Color(100, 80, 60));
            g.fillRect(6, 10, 4, 5);
        } else if (lowerName.contains("axe")) {
            g.setColor(new Color(139, 90, 43));
            g.fillRect(7, 6, 2, 9);
            g.setColor(new Color(160, 160, 170));
            g.fillArc(3, 1, 10, 10, 0, 180);
        } else if (lowerName.contains("rock") || lowerName.contains("stone")) {
            g.setColor(new Color(120, 120, 120));
            g.fillOval(3, 4, 10, 9);
            g.setColor(new Color(100, 100, 100));
            g.fillOval(5, 6, 4, 4);
        } else if (lowerName.contains("bomb")) {
            g.setColor(new Color(40, 40, 40));
            g.fillOval(2, 4, 12, 12);
            g.setColor(new Color(200, 150, 50));
            g.fillRect(7, 0, 2, 5);
            g.setColor(new Color(255, 100, 0));
            g.fillOval(6, 0, 4, 4);
        } else if (lowerName.contains("potion")) {
            g.setColor(new Color(200, 200, 220));
            g.fillRect(6, 1, 4, 3);
            g.setColor(new Color(100, 200, 100, 180));
            g.fillOval(3, 4, 10, 11);
        } else {
            g.setColor(primary);
            g.fillOval(3, 3, 10, 10);
            g.setColor(secondary);
            g.drawOval(3, 3, 10, 10);
        }
    }

    private static void drawDefaultIcon(Graphics2D g, Color primary, Color secondary, String name) {
        g.setColor(primary);
        g.fillRoundRect(2, 2, 12, 12, 4, 4);
        g.setColor(secondary);
        g.drawRoundRect(2, 2, 12, 12, 4, 4);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        String initial = name.length() > 0 ? name.substring(0, 1).toUpperCase() : "?";
        g.drawString(initial, 5, 12);
    }
}
