package tools;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

/**
 * Generates animated GIF clothing overlays for the sprite-based character system.
 * Creates equipment for: helmets, shirts, pants, shoes, gloves, necklaces, wristwear.
 *
 * Each item has idle, walk, and jump animations matching the base player sprite.
 *
 * Run: java tools.ClothingGifGenerator
 */
public class ClothingGifGenerator {

    private static final int WIDTH = 32;
    private static final int HEIGHT = 64;
    private static final int FRAMES = 5;
    private static final int FRAME_DELAY_MS = 150;

    // Base directory for clothing assets
    private static final String BASE_DIR = "assets/clothing/";

    public static void main(String[] args) {
        try {
            System.out.println("Generating clothing overlay GIFs...");
            System.out.println("Dimensions: " + WIDTH + "x" + HEIGHT + ", " + FRAMES + " frames per action\n");

            // Generate helmets/hats
            generateHelmet("leather_cap", new Color(139, 90, 43), new Color(101, 67, 33));
            generateHelmet("iron_helm", new Color(180, 180, 190), new Color(140, 140, 150));
            generateHelmet("wizard_hat", new Color(75, 0, 130), new Color(148, 103, 189));

            // Generate shirts/chest armor
            generateShirt("cloth_shirt", new Color(200, 180, 160), new Color(170, 150, 130));
            generateShirt("leather_vest", new Color(139, 90, 43), new Color(101, 67, 33));
            generateShirt("chainmail", new Color(160, 160, 170), new Color(120, 120, 130));

            // Generate pants
            generatePants("cloth_pants", new Color(80, 80, 100), new Color(60, 60, 80));
            generatePants("leather_pants", new Color(101, 67, 33), new Color(80, 50, 25));
            generatePants("armored_greaves", new Color(150, 150, 160), new Color(110, 110, 120));

            // Generate shoes/boots
            generateBoots("sandals", new Color(180, 140, 100), new Color(140, 100, 60));
            generateBoots("leather_boots", new Color(80, 50, 25), new Color(60, 35, 15));
            generateBoots("iron_boots", new Color(140, 140, 150), new Color(100, 100, 110));

            // Generate gloves
            generateGloves("cloth_gloves", new Color(200, 180, 160), new Color(170, 150, 130));
            generateGloves("leather_gloves", new Color(101, 67, 33), new Color(80, 50, 25));
            generateGloves("iron_gauntlets", new Color(150, 150, 160), new Color(110, 110, 120));

            // Generate necklaces
            generateNecklace("gold_chain", new Color(255, 215, 0), new Color(218, 165, 32));
            generateNecklace("silver_pendant", new Color(192, 192, 192), new Color(169, 169, 169));
            generateNecklace("ruby_amulet", new Color(224, 17, 95), new Color(139, 0, 0));

            // Generate wristwear
            generateWristwear("leather_bracers", new Color(139, 90, 43), new Color(101, 67, 33));
            generateWristwear("gold_bracelet", new Color(255, 215, 0), new Color(218, 165, 32));
            generateWristwear("iron_vambraces", new Color(150, 150, 160), new Color(110, 110, 120));

            System.out.println("\nDone! Clothing overlays saved to " + BASE_DIR);
        } catch (Exception e) {
            System.err.println("Error generating clothing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== HELMET GENERATION ====================

    private static void generateHelmet(String name, Color primary, Color secondary) throws Exception {
        String dir = BASE_DIR + "helmet/" + name + "/";
        new File(dir).mkdirs();

        generateHelmetAction(dir + "idle.gif", name, primary, secondary, "idle");
        generateHelmetAction(dir + "walk.gif", name, primary, secondary, "walk");
        generateHelmetAction(dir + "jump.gif", name, primary, secondary, "jump");

        System.out.println("  Generated helmet: " + name);
    }

    private static void generateHelmetAction(String path, String type, Color primary, Color secondary, String action) throws Exception {
        BufferedImage[] frames = new BufferedImage[FRAMES];

        for (int i = 0; i < FRAMES; i++) {
            frames[i] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            int yOffset = getActionYOffset(action, i);
            drawHelmet(g, type, primary, secondary, yOffset);

            g.dispose();
        }

        writeGif(frames, path, FRAME_DELAY_MS);
    }

    private static void drawHelmet(Graphics2D g, String type, Color primary, Color secondary, int yOffset) {
        int baseY = 4 + yOffset;

        if (type.contains("wizard")) {
            // Tall pointed wizard hat
            g.setColor(primary);
            int[] xPoints = {16, 8, 24};
            int[] yPoints = {baseY - 8, baseY + 10, baseY + 10};
            g.fillPolygon(xPoints, yPoints, 3);
            g.setColor(secondary);
            g.fillRect(6, baseY + 8, 20, 4);
            // Star decoration
            g.setColor(Color.YELLOW);
            g.fillOval(14, baseY - 2, 4, 4);
        } else if (type.contains("iron")) {
            // Iron helmet with visor
            g.setColor(primary);
            g.fillRoundRect(8, baseY, 16, 14, 4, 4);
            g.setColor(secondary);
            g.fillRect(10, baseY + 6, 12, 3);
            // Plume
            g.setColor(new Color(180, 50, 50));
            g.fillRect(14, baseY - 4, 4, 6);
        } else {
            // Leather cap
            g.setColor(primary);
            g.fillArc(8, baseY, 16, 12, 0, 180);
            g.setColor(secondary);
            g.fillRect(8, baseY + 4, 16, 3);
        }
    }

    // ==================== SHIRT GENERATION ====================

    private static void generateShirt(String name, Color primary, Color secondary) throws Exception {
        String dir = BASE_DIR + "chest/" + name + "/";
        new File(dir).mkdirs();

        generateShirtAction(dir + "idle.gif", name, primary, secondary, "idle");
        generateShirtAction(dir + "walk.gif", name, primary, secondary, "walk");
        generateShirtAction(dir + "jump.gif", name, primary, secondary, "jump");

        System.out.println("  Generated shirt: " + name);
    }

    private static void generateShirtAction(String path, String type, Color primary, Color secondary, String action) throws Exception {
        BufferedImage[] frames = new BufferedImage[FRAMES];

        for (int i = 0; i < FRAMES; i++) {
            frames[i] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            int yOffset = getActionYOffset(action, i);
            int armSwing = getArmSwing(action, i);
            drawShirt(g, type, primary, secondary, yOffset, armSwing, action.equals("jump") && i >= 1 && i <= 3);

            g.dispose();
        }

        writeGif(frames, path, FRAME_DELAY_MS);
    }

    private static void drawShirt(Graphics2D g, String type, Color primary, Color secondary, int yOffset, int armSwing, boolean armsUp) {
        int baseY = 4 + yOffset;
        int torsoY = baseY + 20;

        // Main torso
        g.setColor(primary);
        g.fillRect(8, torsoY, 16, 18);

        // Collar/detail
        g.setColor(secondary);
        if (type.contains("chainmail")) {
            // Chainmail pattern
            for (int dy = 0; dy < 18; dy += 3) {
                for (int dx = 0; dx < 16; dx += 3) {
                    g.fillOval(8 + dx, torsoY + dy, 2, 2);
                }
            }
        } else {
            g.fillRect(12, torsoY, 8, 4);
            g.drawLine(16, torsoY + 4, 16, torsoY + 16);
        }

        // Sleeves (arms)
        g.setColor(primary);
        if (armsUp) {
            // Arms raised
            g.fillRect(4, baseY + 14, 5, 8);
            g.fillRect(23, baseY + 14, 5, 8);
        } else {
            // Arms at sides with swing
            g.fillRect(4 + armSwing/2, baseY + 22, 5, 10);
            g.fillRect(23 - armSwing/2, baseY + 22, 5, 10);
        }
    }

    // ==================== PANTS GENERATION ====================

    private static void generatePants(String name, Color primary, Color secondary) throws Exception {
        String dir = BASE_DIR + "legs/" + name + "/";
        new File(dir).mkdirs();

        generatePantsAction(dir + "idle.gif", name, primary, secondary, "idle");
        generatePantsAction(dir + "walk.gif", name, primary, secondary, "walk");
        generatePantsAction(dir + "jump.gif", name, primary, secondary, "jump");

        System.out.println("  Generated pants: " + name);
    }

    private static void generatePantsAction(String path, String type, Color primary, Color secondary, String action) throws Exception {
        BufferedImage[] frames = new BufferedImage[FRAMES];

        for (int i = 0; i < FRAMES; i++) {
            frames[i] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            int yOffset = getActionYOffset(action, i);
            int legSwing = getLegSwing(action, i);
            drawPants(g, type, primary, secondary, yOffset, legSwing);

            g.dispose();
        }

        writeGif(frames, path, FRAME_DELAY_MS);
    }

    private static void drawPants(Graphics2D g, String type, Color primary, Color secondary, int yOffset, int legSwing) {
        int baseY = 4 + yOffset;

        // Left leg
        int leftLegY = baseY + 38;
        g.setColor(primary);
        g.fillRect(10, leftLegY + Math.max(0, legSwing), 6, 14 - Math.abs(legSwing/2));
        if (type.contains("armored")) {
            g.setColor(secondary);
            g.fillRect(10, leftLegY + Math.max(0, legSwing), 6, 3);
        }

        // Right leg
        int rightLegY = baseY + 38;
        g.setColor(primary);
        g.fillRect(16, rightLegY + Math.max(0, -legSwing), 6, 14 - Math.abs(legSwing/2));
        if (type.contains("armored")) {
            g.setColor(secondary);
            g.fillRect(16, rightLegY + Math.max(0, -legSwing), 6, 3);
        }

        // Belt
        g.setColor(secondary);
        g.fillRect(8, baseY + 36, 16, 3);
    }

    // ==================== BOOTS GENERATION ====================

    private static void generateBoots(String name, Color primary, Color secondary) throws Exception {
        String dir = BASE_DIR + "boots/" + name + "/";
        new File(dir).mkdirs();

        generateBootsAction(dir + "idle.gif", name, primary, secondary, "idle");
        generateBootsAction(dir + "walk.gif", name, primary, secondary, "walk");
        generateBootsAction(dir + "jump.gif", name, primary, secondary, "jump");

        System.out.println("  Generated boots: " + name);
    }

    private static void generateBootsAction(String path, String type, Color primary, Color secondary, String action) throws Exception {
        BufferedImage[] frames = new BufferedImage[FRAMES];

        for (int i = 0; i < FRAMES; i++) {
            frames[i] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            int yOffset = getActionYOffset(action, i);
            int legSwing = getLegSwing(action, i);
            drawBoots(g, type, primary, secondary, yOffset, legSwing);

            g.dispose();
        }

        writeGif(frames, path, FRAME_DELAY_MS);
    }

    private static void drawBoots(Graphics2D g, String type, Color primary, Color secondary, int yOffset, int legSwing) {
        int baseY = 4 + yOffset;

        // Left boot
        int leftY = baseY + 52 + Math.max(0, legSwing) - Math.abs(legSwing/2);
        g.setColor(primary);
        g.fillRect(9, leftY, 8, 5);
        g.setColor(secondary);
        g.fillRect(9, leftY, 8, 2);

        // Right boot
        int rightY = baseY + 52 + Math.max(0, -legSwing) - Math.abs(legSwing/2);
        g.setColor(primary);
        g.fillRect(15, rightY, 8, 5);
        g.setColor(secondary);
        g.fillRect(15, rightY, 8, 2);
    }

    // ==================== GLOVES GENERATION ====================

    private static void generateGloves(String name, Color primary, Color secondary) throws Exception {
        String dir = BASE_DIR + "gloves/" + name + "/";
        new File(dir).mkdirs();

        generateGlovesAction(dir + "idle.gif", name, primary, secondary, "idle");
        generateGlovesAction(dir + "walk.gif", name, primary, secondary, "walk");
        generateGlovesAction(dir + "jump.gif", name, primary, secondary, "jump");

        System.out.println("  Generated gloves: " + name);
    }

    private static void generateGlovesAction(String path, String type, Color primary, Color secondary, String action) throws Exception {
        BufferedImage[] frames = new BufferedImage[FRAMES];

        for (int i = 0; i < FRAMES; i++) {
            frames[i] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            int yOffset = getActionYOffset(action, i);
            int armSwing = getArmSwing(action, i);
            boolean armsUp = action.equals("jump") && i >= 1 && i <= 3;
            drawGloves(g, type, primary, secondary, yOffset, armSwing, armsUp);

            g.dispose();
        }

        writeGif(frames, path, FRAME_DELAY_MS);
    }

    private static void drawGloves(Graphics2D g, String type, Color primary, Color secondary, int yOffset, int armSwing, boolean armsUp) {
        int baseY = 4 + yOffset;

        g.setColor(primary);
        if (armsUp) {
            // Hands raised
            g.fillRoundRect(3, baseY + 8, 6, 6, 2, 2);
            g.fillRoundRect(23, baseY + 8, 6, 6, 2, 2);
        } else {
            // Hands at sides
            g.fillRoundRect(3 + armSwing/2, baseY + 34, 6, 6, 2, 2);
            g.fillRoundRect(23 - armSwing/2, baseY + 34, 6, 6, 2, 2);
        }

        // Gauntlet detail
        if (type.contains("iron") || type.contains("gauntlet")) {
            g.setColor(secondary);
            if (armsUp) {
                g.drawRect(3, baseY + 8, 5, 5);
                g.drawRect(23, baseY + 8, 5, 5);
            } else {
                g.drawRect(3 + armSwing/2, baseY + 34, 5, 5);
                g.drawRect(23 - armSwing/2, baseY + 34, 5, 5);
            }
        }
    }

    // ==================== NECKLACE GENERATION ====================

    private static void generateNecklace(String name, Color primary, Color secondary) throws Exception {
        String dir = BASE_DIR + "necklace/" + name + "/";
        new File(dir).mkdirs();

        generateNecklaceAction(dir + "idle.gif", name, primary, secondary, "idle");
        generateNecklaceAction(dir + "walk.gif", name, primary, secondary, "walk");
        generateNecklaceAction(dir + "jump.gif", name, primary, secondary, "jump");

        System.out.println("  Generated necklace: " + name);
    }

    private static void generateNecklaceAction(String path, String type, Color primary, Color secondary, String action) throws Exception {
        BufferedImage[] frames = new BufferedImage[FRAMES];

        for (int i = 0; i < FRAMES; i++) {
            frames[i] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            int yOffset = getActionYOffset(action, i);
            int sway = action.equals("walk") ? (int)(Math.sin(2 * Math.PI * i / FRAMES) * 1) : 0;
            drawNecklace(g, type, primary, secondary, yOffset, sway);

            g.dispose();
        }

        writeGif(frames, path, FRAME_DELAY_MS);
    }

    private static void drawNecklace(Graphics2D g, String type, Color primary, Color secondary, int yOffset, int sway) {
        int baseY = 4 + yOffset;
        int neckY = baseY + 16;

        // Chain around neck
        g.setColor(primary);
        g.drawArc(10, neckY, 12, 8, 180, 180);

        // Pendant
        int pendantX = 15 + sway;
        int pendantY = neckY + 6;

        if (type.contains("ruby") || type.contains("amulet")) {
            // Gem pendant
            g.setColor(secondary);
            g.fillOval(pendantX - 2, pendantY, 6, 6);
            g.setColor(primary);
            g.drawOval(pendantX - 2, pendantY, 6, 6);
        } else if (type.contains("pendant")) {
            // Silver pendant
            g.setColor(primary);
            g.fillOval(pendantX - 1, pendantY, 4, 5);
        } else {
            // Simple chain (no pendant visible)
            g.setColor(primary);
            g.fillOval(pendantX, pendantY, 2, 2);
        }
    }

    // ==================== WRISTWEAR GENERATION ====================

    private static void generateWristwear(String name, Color primary, Color secondary) throws Exception {
        String dir = BASE_DIR + "wristwear/" + name + "/";
        new File(dir).mkdirs();

        generateWristwearAction(dir + "idle.gif", name, primary, secondary, "idle");
        generateWristwearAction(dir + "walk.gif", name, primary, secondary, "walk");
        generateWristwearAction(dir + "jump.gif", name, primary, secondary, "jump");

        System.out.println("  Generated wristwear: " + name);
    }

    private static void generateWristwearAction(String path, String type, Color primary, Color secondary, String action) throws Exception {
        BufferedImage[] frames = new BufferedImage[FRAMES];

        for (int i = 0; i < FRAMES; i++) {
            frames[i] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            int yOffset = getActionYOffset(action, i);
            int armSwing = getArmSwing(action, i);
            boolean armsUp = action.equals("jump") && i >= 1 && i <= 3;
            drawWristwear(g, type, primary, secondary, yOffset, armSwing, armsUp);

            g.dispose();
        }

        writeGif(frames, path, FRAME_DELAY_MS);
    }

    private static void drawWristwear(Graphics2D g, String type, Color primary, Color secondary, int yOffset, int armSwing, boolean armsUp) {
        int baseY = 4 + yOffset;

        g.setColor(primary);
        if (armsUp) {
            // Wrists when arms raised
            g.fillRect(4, baseY + 18, 5, 3);
            g.fillRect(23, baseY + 18, 5, 3);
        } else {
            // Wrists at arm position
            g.fillRect(4 + armSwing/2, baseY + 30, 5, 3);
            g.fillRect(23 - armSwing/2, baseY + 30, 5, 3);
        }

        // Detail for bracers
        if (type.contains("bracer") || type.contains("vambrace")) {
            g.setColor(secondary);
            if (armsUp) {
                g.drawLine(4, baseY + 19, 8, baseY + 19);
                g.drawLine(23, baseY + 19, 27, baseY + 19);
            } else {
                g.drawLine(4 + armSwing/2, baseY + 31, 8 + armSwing/2, baseY + 31);
                g.drawLine(23 - armSwing/2, baseY + 31, 27 - armSwing/2, baseY + 31);
            }
        }
    }

    // ==================== HELPER METHODS ====================

    private static int getActionYOffset(String action, int frame) {
        switch (action) {
            case "idle":
                return (int)(Math.sin(2 * Math.PI * frame / FRAMES) * 1);
            case "walk":
                return (int)(Math.abs(Math.sin(2 * Math.PI * frame / FRAMES * 2)) * 2);
            case "jump":
                int[] jumpOffsets = {2, -2, -4, -2, 1};
                return jumpOffsets[frame];
            default:
                return 0;
        }
    }

    private static int getArmSwing(String action, int frame) {
        if (action.equals("walk")) {
            return (int)(Math.sin(2 * Math.PI * frame / FRAMES + Math.PI) * 3);
        }
        return 0;
    }

    private static int getLegSwing(String action, int frame) {
        if (action.equals("walk")) {
            return (int)(Math.sin(2 * Math.PI * frame / FRAMES) * 4);
        } else if (action.equals("jump")) {
            int[] legTucks = {-2, 2, 4, 2, -1};
            return legTucks[frame];
        }
        return 0;
    }

    private static void writeGif(BufferedImage[] frames, String path, int delayMs) throws Exception {
        ImageWriter writer = null;
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("gif");
        if (writers.hasNext()) {
            writer = writers.next();
        }
        if (writer == null) {
            throw new Exception("No GIF writer found");
        }

        File file = new File(path);
        file.getParentFile().mkdirs();

        ImageOutputStream ios = ImageIO.createImageOutputStream(file);
        writer.setOutput(ios);
        writer.prepareWriteSequence(null);

        for (int i = 0; i < frames.length; i++) {
            BufferedImage frame = frames[i];

            BufferedImage indexed = new BufferedImage(frame.getWidth(), frame.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = indexed.createGraphics();
            g.drawImage(frame, 0, 0, null);
            g.dispose();

            IIOMetadata metadata = writer.getDefaultImageMetadata(
                    new javax.imageio.ImageTypeSpecifier(indexed), null);

            String metaFormat = metadata.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormat);

            IIOMetadataNode gce = getNode(root, "GraphicControlExtension");
            gce.setAttribute("disposalMethod", "restoreToBackgroundColor");
            gce.setAttribute("userInputFlag", "FALSE");
            gce.setAttribute("transparentColorFlag", "TRUE");
            gce.setAttribute("transparentColorIndex", "0");
            gce.setAttribute("delayTime", String.valueOf(delayMs / 10));

            IIOMetadataNode appExtensions = getNode(root, "ApplicationExtensions");
            IIOMetadataNode appExt = new IIOMetadataNode("ApplicationExtension");
            appExt.setAttribute("applicationID", "NETSCAPE");
            appExt.setAttribute("authenticationCode", "2.0");
            appExt.setUserObject(new byte[]{1, 0, 0});
            appExtensions.appendChild(appExt);

            metadata.setFromTree(metaFormat, root);

            writer.writeToSequence(new javax.imageio.IIOImage(indexed, null, metadata), null);
        }

        writer.endWriteSequence();
        ios.close();
    }

    private static IIOMetadataNode getNode(IIOMetadataNode root, String nodeName) {
        for (int i = 0; i < root.getLength(); i++) {
            if (root.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) root.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        root.appendChild(node);
        return node;
    }
}
