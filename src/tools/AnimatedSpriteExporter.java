package tools;

import entity.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Set;

/**
 * Utility to export animated GIF sprites for all items.
 * Each item type has unique animations:
 * - Weapons: swing/slash effect
 * - Bows/Ranged: draw/pull animation
 * - Potions: bubbling/shimmer
 * - Food: bounce/wiggle
 * - Ammo: spin/rotate
 * - Throwables: tumble/rotate
 * - Materials/Gems: sparkle/shine
 * - Keys: jingle/shake
 * - Tools: bob/ready animation
 *
 * Usage: java -cp bin tools.AnimatedSpriteExporter
 */
public class AnimatedSpriteExporter {

    private static final int ICON_SIZE = 16;
    private static final int FRAME_COUNT = 8;
    private static final int FRAME_DELAY = 120; // milliseconds
    private static final String OUTPUT_DIR = "assets/items/";

    public static void main(String[] args) {
        System.out.println("Animated Item Sprite Exporter");
        System.out.println("=============================\n");

        File outDir = new File(OUTPUT_DIR);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        ItemRegistry.initialize();
        Set<String> itemIds = ItemRegistry.getAllItemIds();
        int exported = 0;
        int failed = 0;

        for (String itemId : itemIds) {
            try {
                Item item = ItemRegistry.create(itemId);
                if (item != null) {
                    exportAnimatedGif(item, itemId);
                    System.out.println("Exported: " + itemId + ".gif");
                    exported++;
                }
            } catch (Exception e) {
                System.err.println("Failed to export: " + itemId + " - " + e.getMessage());
                e.printStackTrace();
                failed++;
            }
        }

        System.out.println("\n=============================");
        System.out.println("Exported: " + exported + " animated items");
        if (failed > 0) {
            System.out.println("Failed: " + failed + " items");
        }
    }

    private static void exportAnimatedGif(Item item, String itemId) throws IOException {
        String type = item.getCategory().name().toLowerCase();
        String name = item.getName();

        BufferedImage[] frames = new BufferedImage[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            frames[i] = generateAnimatedFrame(type, name, itemId, i, FRAME_COUNT);
        }

        // Write GIF
        File outputFile = new File(OUTPUT_DIR + itemId + ".gif");
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            GifEncoder encoder = new GifEncoder(fos);
            encoder.setDelay(FRAME_DELAY);
            encoder.setRepeat(0); // Infinite loop
            encoder.start(ICON_SIZE, ICON_SIZE);

            for (BufferedImage frame : frames) {
                encoder.addFrame(frame);
            }

            encoder.finish();
        }
    }

    private static BufferedImage generateAnimatedFrame(String type, String name, String itemId,
                                                         int frameIndex, int totalFrames) {
        BufferedImage frame = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = frame.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Calculate animation progress (0.0 to 1.0)
        double progress = (double) frameIndex / totalFrames;
        double wave = Math.sin(progress * Math.PI * 2);
        double bounce = Math.abs(Math.sin(progress * Math.PI * 2));

        Color primary = getUniqueItemColor(type, name);
        Color secondary = primary.darker();

        // Apply type-specific animation
        switch (type.toLowerCase()) {
            case "weapon":
                drawAnimatedWeapon(g, primary, secondary, name, progress, wave);
                break;
            case "ranged_weapon":
                drawAnimatedRangedWeapon(g, primary, secondary, name, progress, wave);
                break;
            case "potion":
                drawAnimatedPotion(g, primary, name, progress, wave, frameIndex);
                break;
            case "food":
                drawAnimatedFood(g, primary, secondary, name, bounce);
                break;
            case "material":
                drawAnimatedMaterial(g, primary, secondary, name, itemId, progress, wave, frameIndex);
                break;
            case "throwable":
                drawAnimatedThrowable(g, primary, secondary, name, progress);
                break;
            case "tool":
                drawAnimatedTool(g, primary, secondary, name, wave);
                break;
            case "armor":
                drawAnimatedArmor(g, primary, secondary, name, wave);
                break;
            case "key":
                drawAnimatedKey(g, primary, name, wave, frameIndex);
                break;
            default:
                drawAnimatedDefault(g, primary, secondary, name, bounce);
        }

        g.dispose();
        return frame;
    }

    // ==================== Animation Drawing Methods ====================

    private static void drawAnimatedWeapon(Graphics2D g, Color primary, Color secondary,
                                            String name, double progress, double wave) {
        String lowerName = name.toLowerCase();

        // Weapon swing animation - rotate slightly
        double swingAngle = wave * 0.15; // Subtle swing

        Graphics2D g2 = (Graphics2D) g.create();
        g2.rotate(swingAngle, 8, 12);

        if (lowerName.contains("axe")) {
            // Axe with swing
            g2.setColor(new Color(139, 90, 43));
            g2.fillRect(7, 4, 2, 11);
            g2.setColor(primary);
            g2.fillArc(3, 2, 10, 8, 90, 180);
            g2.setColor(secondary);
            g2.drawArc(3, 2, 10, 8, 90, 180);
            // Shine effect
            if (progress < 0.3) {
                g2.setColor(new Color(255, 255, 255, (int)(150 * (1 - progress/0.3))));
                g2.fillRect(4, 3, 2, 2);
            }
        } else if (lowerName.contains("mace") || lowerName.contains("hammer")) {
            g2.setColor(new Color(139, 90, 43));
            g2.fillRect(7, 6, 2, 9);
            g2.setColor(primary);
            g2.fillOval(4, 2, 8, 8);
            g2.setColor(secondary);
            g2.drawOval(4, 2, 8, 8);
        } else if (lowerName.contains("dagger") || lowerName.contains("knife")) {
            g2.setColor(secondary);
            g2.fillRect(7, 3, 2, 7);
            g2.setColor(primary);
            g2.fillPolygon(new int[]{8, 6, 10}, new int[]{1, 4, 4}, 3);
            g2.setColor(new Color(139, 90, 43));
            g2.fillRect(6, 10, 4, 4);
        } else {
            // Sword with shine
            g2.setColor(secondary);
            g2.fillRect(7, 2, 2, 10);
            g2.setColor(primary);
            g2.fillRect(6, 1, 4, 2);
            g2.setColor(new Color(139, 90, 43));
            g2.fillRect(6, 12, 4, 3);
            g2.setColor(new Color(255, 215, 0));
            g2.fillRect(4, 11, 8, 2);
            // Moving shine
            int shineY = 2 + (int)(progress * 8);
            g2.setColor(new Color(255, 255, 255, 180));
            g2.drawLine(7, shineY, 8, shineY);
        }
        g2.dispose();
    }

    private static void drawAnimatedRangedWeapon(Graphics2D g, Color primary, Color secondary,
                                                   String name, double progress, double wave) {
        String lowerName = name.toLowerCase();

        if (lowerName.contains("crossbow")) {
            // Crossbow with pull animation
            int pull = (int)(wave * 2);
            g.setColor(primary);
            g.fillRect(3, 7, 10, 2);
            g.setColor(secondary);
            g.fillRect(6, 4, 4, 8);
            g.setColor(new Color(100, 100, 100));
            g.drawLine(2 - pull, 7, 13 + pull, 7);
        } else if (lowerName.contains("wand") || lowerName.contains("staff")) {
            // Magic staff with glowing orb
            g.setColor(primary);
            g.fillRect(7, 2, 2, 12);

            // Pulsing orb
            int orbSize = 5 + (int)(wave);
            int orbOffset = (6 - orbSize) / 2;
            Color orbColor = new Color(200, 100, 255, 200 + (int)(wave * 50));
            g.setColor(orbColor);
            g.fillOval(5 + orbOffset, orbOffset, orbSize, orbSize);

            // Sparkle
            g.setColor(Color.WHITE);
            int sparkleX = 6 + (int)(progress * 4);
            int sparkleY = 1 + (int)(Math.sin(progress * Math.PI * 4) * 2);
            g.fillRect(sparkleX, sparkleY, 1, 1);
        } else {
            // Bow with draw animation
            int drawOffset = (int)(wave * 1.5);
            g.setColor(primary);
            g.drawArc(3 - drawOffset, 2, 10 + drawOffset, 12, 60, 180);
            g.setColor(new Color(200, 200, 200));
            g.drawLine(4 - drawOffset, 8, 12 + drawOffset, 8);
        }
    }

    private static void drawAnimatedPotion(Graphics2D g, Color primary, String name,
                                            double progress, double wave, int frameIndex) {
        // Cork
        g.setColor(new Color(139, 90, 43));
        g.fillRect(6, 1, 4, 3);

        // Bottle glass
        g.setColor(new Color(180, 180, 200, 180));
        g.fillOval(3, 4, 10, 11);

        // Liquid with bubble animation
        g.setColor(primary);
        g.fillOval(4, 6, 8, 8);

        // Bubbles
        int bubble1Y = 12 - (int)(progress * 6);
        int bubble2Y = 10 - (int)((progress + 0.5) % 1.0 * 6);
        g.setColor(new Color(255, 255, 255, 150));
        g.fillOval(5, bubble1Y, 2, 2);
        g.fillOval(9, bubble2Y, 2, 2);

        // Shine
        g.setColor(new Color(255, 255, 255, 100));
        g.fillOval(5, 7, 3, 3);
    }

    private static void drawAnimatedFood(Graphics2D g, Color primary, Color secondary,
                                          String name, double bounce) {
        String lowerName = name.toLowerCase();
        int offsetY = (int)(bounce * 2);

        if (lowerName.contains("apple")) {
            g.setColor(Color.RED);
            g.fillOval(3, 4 + offsetY, 10, 10);
            g.setColor(new Color(139, 69, 19));
            g.fillRect(7, 1 + offsetY, 2, 4);
            g.setColor(Color.GREEN);
            g.fillOval(9, 2 + offsetY, 4, 3);
            // Shine
            g.setColor(new Color(255, 255, 255, 100));
            g.fillOval(4, 5 + offsetY, 3, 3);
        } else if (lowerName.contains("bread")) {
            g.setColor(new Color(210, 170, 100));
            g.fillRoundRect(2, 5 + offsetY, 12, 8, 4, 4);
            g.setColor(new Color(180, 140, 70));
            g.drawLine(4, 9 + offsetY, 12, 9 + offsetY);
        } else if (lowerName.contains("meat") || lowerName.contains("steak")) {
            g.setColor(new Color(180, 80, 60));
            g.fillOval(2, 4 + offsetY, 12, 10);
            g.setColor(new Color(255, 200, 180));
            g.fillOval(5, 6 + offsetY, 6, 5);
            // Sizzle effect
            g.setColor(new Color(255, 255, 200, 100));
            g.fillOval(8, 3 + offsetY, 2, 2);
        } else {
            g.setColor(primary);
            g.fillOval(3, 4 + offsetY, 10, 10);
            g.setColor(secondary);
            g.drawOval(3, 4 + offsetY, 10, 10);
        }
    }

    private static void drawAnimatedMaterial(Graphics2D g, Color primary, Color secondary,
                                              String name, String itemId, double progress,
                                              double wave, int frameIndex) {
        String lowerName = name.toLowerCase();
        String lowerId = itemId.toLowerCase();

        if (lowerId.contains("arrow") || lowerName.contains("arrow")) {
            // Spinning arrow
            Graphics2D g2 = (Graphics2D) g.create();
            g2.rotate(progress * Math.PI * 2, 8, 8);
            g2.setColor(new Color(139, 90, 43));
            g2.fillRect(2, 7, 10, 2);
            g2.setColor(primary);
            g2.fillPolygon(new int[]{12, 15, 12}, new int[]{6, 8, 10}, 3);
            g2.setColor(new Color(200, 50, 50));
            g2.fillRect(0, 6, 3, 1);
            g2.fillRect(0, 9, 3, 1);
            g2.dispose();
        } else if (lowerId.contains("bolt") || lowerName.contains("bolt")) {
            // Spinning bolt
            Graphics2D g2 = (Graphics2D) g.create();
            g2.rotate(progress * Math.PI * 2, 8, 8);
            g2.setColor(new Color(100, 100, 100));
            g2.fillRect(3, 7, 8, 2);
            g2.setColor(primary);
            g2.fillPolygon(new int[]{11, 15, 11}, new int[]{6, 8, 10}, 3);
            g2.dispose();
        } else if (lowerId.contains("mana") || lowerName.contains("crystal")) {
            // Pulsing crystal
            int pulse = (int)(wave * 2);
            g.setColor(new Color(100, 100, 255, 200 + (int)(wave * 50)));
            g.fillPolygon(new int[]{8, 4 - pulse/2, 8, 12 + pulse/2},
                         new int[]{1 - pulse/2, 8, 15 + pulse/2, 8}, 4);
            g.setColor(new Color(150, 150, 255));
            g.drawPolygon(new int[]{8, 4, 8, 12}, new int[]{1, 8, 15, 8}, 4);
            // Inner glow
            g.setColor(new Color(200, 200, 255, 150));
            g.fillPolygon(new int[]{8, 6, 8}, new int[]{3, 7, 7}, 3);
            // Sparkle
            if (frameIndex % 3 == 0) {
                g.setColor(Color.WHITE);
                g.fillRect(5 + frameIndex % 5, 4, 1, 1);
            }
        } else if (lowerName.contains("ingot")) {
            // Shimmering ingot
            g.setColor(primary);
            g.fillRect(2, 6, 12, 6);
            g.setColor(secondary);
            g.fillRect(2, 6, 12, 2);
            // Moving shine
            int shineX = 2 + (int)(progress * 10);
            g.setColor(new Color(255, 255, 255, 150));
            g.fillRect(shineX, 7, 2, 4);
        } else if (lowerName.contains("diamond") || lowerName.contains("gem")) {
            // Sparkling gem
            g.setColor(primary);
            g.fillPolygon(new int[]{8, 3, 5, 11, 13}, new int[]{2, 6, 14, 14, 6}, 5);
            g.setColor(primary.brighter());
            g.drawPolygon(new int[]{8, 3, 5, 11, 13}, new int[]{2, 6, 14, 14, 6}, 5);
            // Multiple sparkles
            g.setColor(new Color(255, 255, 255, 200));
            int sparkle = frameIndex % 4;
            if (sparkle == 0) g.fillRect(5, 5, 2, 2);
            else if (sparkle == 1) g.fillRect(9, 7, 2, 2);
            else if (sparkle == 2) g.fillRect(7, 10, 2, 2);
            else g.fillRect(4, 8, 2, 2);
        } else {
            g.setColor(primary);
            g.fillOval(3, 3, 10, 10);
            g.setColor(secondary);
            g.drawOval(3, 3, 10, 10);
        }
    }

    private static void drawAnimatedThrowable(Graphics2D g, Color primary, Color secondary,
                                               String name, double progress) {
        String lowerName = name.toLowerCase();

        // Rotation for throwing animation
        Graphics2D g2 = (Graphics2D) g.create();
        g2.rotate(progress * Math.PI * 2, 8, 8);

        if (lowerName.contains("knife")) {
            g2.setColor(new Color(180, 180, 190));
            g2.fillRect(6, 2, 4, 8);
            g2.fillPolygon(new int[]{8, 5, 11}, new int[]{0, 3, 3}, 3);
            g2.setColor(new Color(100, 80, 60));
            g2.fillRect(6, 10, 4, 5);
        } else if (lowerName.contains("axe")) {
            g2.setColor(new Color(139, 90, 43));
            g2.fillRect(7, 6, 2, 9);
            g2.setColor(new Color(160, 160, 170));
            g2.fillArc(3, 1, 10, 10, 0, 180);
        } else if (lowerName.contains("rock") || lowerName.contains("stone")) {
            g2.setColor(new Color(120, 120, 120));
            g2.fillOval(3, 4, 10, 9);
            g2.setColor(new Color(100, 100, 100));
            g2.fillOval(5, 6, 4, 4);
        } else if (lowerName.contains("bomb")) {
            g2.dispose();
            g2 = (Graphics2D) g.create(); // Don't rotate bomb
            g2.setColor(new Color(40, 40, 40));
            g2.fillOval(2, 4, 12, 12);
            g2.setColor(new Color(200, 150, 50));
            g2.fillRect(7, 0, 2, 5);
            // Flickering fuse
            Color fuseColor = (int)(progress * 8) % 2 == 0 ?
                new Color(255, 100, 0) : new Color(255, 200, 0);
            g2.setColor(fuseColor);
            g2.fillOval(6, 0, 4, 4);
        } else if (lowerName.contains("potion")) {
            g2.dispose();
            g2 = (Graphics2D) g.create();
            g2.setColor(new Color(200, 200, 220));
            g2.fillRect(6, 1, 4, 3);
            g2.setColor(new Color(100, 200, 100, 180));
            g2.fillOval(3, 4, 10, 11);
        } else {
            g2.setColor(primary);
            g2.fillOval(3, 3, 10, 10);
            g2.setColor(secondary);
            g2.drawOval(3, 3, 10, 10);
        }
        g2.dispose();
    }

    private static void drawAnimatedTool(Graphics2D g, Color primary, Color secondary,
                                          String name, double wave) {
        String lowerName = name.toLowerCase();
        int offsetY = (int)(wave * 1.5);

        if (lowerName.contains("pickaxe")) {
            g.setColor(new Color(139, 90, 43));
            g.fillRect(7, 5 + offsetY, 2, 10);
            g.setColor(primary);
            g.fillRect(2, 2 + offsetY, 12, 4);
            g.fillPolygon(new int[]{2, 0, 4}, new int[]{4 + offsetY, 7 + offsetY, 7 + offsetY}, 3);
            g.fillPolygon(new int[]{14, 16, 12}, new int[]{4 + offsetY, 7 + offsetY, 7 + offsetY}, 3);
        } else if (lowerName.contains("shovel")) {
            g.setColor(new Color(139, 90, 43));
            g.fillRect(7, 3 + offsetY, 2, 10);
            g.setColor(primary);
            g.fillOval(4, 0 + offsetY, 8, 6);
        } else {
            g.setColor(new Color(139, 90, 43));
            g.fillRect(7, 6 + offsetY, 2, 9);
            g.setColor(primary);
            g.fillRect(4, 1 + offsetY, 8, 6);
        }
    }

    private static void drawAnimatedArmor(Graphics2D g, Color primary, Color secondary,
                                           String name, double wave) {
        String lowerName = name.toLowerCase();
        int shimmer = (int)(Math.abs(wave) * 50);

        if (lowerName.contains("helmet") || lowerName.contains("hat")) {
            g.setColor(primary);
            g.fillArc(2, 4, 12, 10, 0, 180);
            g.fillRect(2, 9, 12, 4);
            g.setColor(new Color(255, 255, 255, shimmer));
            g.fillArc(3, 5, 4, 4, 0, 180);
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
            g.setColor(new Color(255, 255, 255, shimmer));
            g.fillOval(6, 5, 4, 4);
        }
    }

    private static void drawAnimatedKey(Graphics2D g, Color primary, String name,
                                         double wave, int frameIndex) {
        // Jingling key
        int jingle = (int)(wave * 2);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.rotate(wave * 0.2, 6, 6);

        g2.setColor(primary);
        g2.fillOval(2 + jingle, 2, 8, 8);
        g2.setColor(primary.darker());
        g2.fillOval(4 + jingle, 4, 4, 4);
        g2.setColor(primary);
        g2.fillRect(8 + jingle, 5, 7, 3);
        g2.fillRect(12 + jingle, 7, 2, 3);
        g2.fillRect(14 + jingle, 7, 2, 2);

        // Sparkle
        if (frameIndex % 2 == 0) {
            g2.setColor(new Color(255, 255, 200, 200));
            g2.fillRect(3 + jingle, 3, 2, 2);
        }
        g2.dispose();
    }

    private static void drawAnimatedDefault(Graphics2D g, Color primary, Color secondary,
                                             String name, double bounce) {
        int offsetY = (int)(bounce * 2);
        g.setColor(primary);
        g.fillRoundRect(2, 2 + offsetY, 12, 12, 4, 4);
        g.setColor(secondary);
        g.drawRoundRect(2, 2 + offsetY, 12, 12, 4, 4);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        String initial = name.length() > 0 ? name.substring(0, 1).toUpperCase() : "?";
        g.drawString(initial, 5, 12 + offsetY);
    }

    // ==================== Color Utilities ====================

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
}
