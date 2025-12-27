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
 * Utility class for generating animated GIF sprites programmatically.
 * Creates test player sprites with 5 frames per action (idle, walk, jump).
 *
 * Run this class to generate the test sprites:
 *   java tools.SpriteGifGenerator
 */
public class SpriteGifGenerator {

    // Sprite dimensions
    private static final int WIDTH = 32;
    private static final int HEIGHT = 64;
    private static final int FRAMES = 5;
    private static final int FRAME_DELAY_MS = 150; // 150ms per frame

    // Colors for the character
    private static final Color SKIN_COLOR = new Color(255, 204, 153);
    private static final Color HAIR_COLOR = new Color(139, 69, 19);
    private static final Color SHIRT_COLOR = new Color(70, 130, 180);
    private static final Color PANTS_COLOR = new Color(60, 60, 80);
    private static final Color SHOES_COLOR = new Color(80, 40, 20);
    private static final Color EYE_COLOR = new Color(30, 30, 30);
    private static final Color OUTLINE_COLOR = new Color(40, 40, 50);

    public static void main(String[] args) {
        String outputDir = "assets/player/sprites/";

        // Create output directory
        new File(outputDir).mkdirs();

        try {
            System.out.println("Generating player sprite GIFs...");
            System.out.println("Dimensions: " + WIDTH + "x" + HEIGHT + ", " + FRAMES + " frames per action");

            // Generate each action sprite
            generateIdleSprite(outputDir + "idle.gif");
            generateWalkSprite(outputDir + "walk.gif");
            generateJumpSprite(outputDir + "jump.gif");

            System.out.println("Done! Sprites saved to " + outputDir);
        } catch (Exception e) {
            System.err.println("Error generating sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generates the idle animation GIF.
     * Character stands still with subtle breathing animation.
     */
    private static void generateIdleSprite(String path) throws Exception {
        BufferedImage[] frames = new BufferedImage[FRAMES];

        for (int i = 0; i < FRAMES; i++) {
            frames[i] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            // Breathing offset (subtle vertical movement)
            double breathPhase = (2 * Math.PI * i) / FRAMES;
            int breathOffset = (int) (Math.sin(breathPhase) * 1);

            drawCharacter(g, 0, breathOffset, 0, 0, false);
            g.dispose();
        }

        writeGif(frames, path, FRAME_DELAY_MS);
        System.out.println("  Generated: " + path);
    }

    /**
     * Generates the walk animation GIF.
     * Character walks with leg and arm movement.
     */
    private static void generateWalkSprite(String path) throws Exception {
        BufferedImage[] frames = new BufferedImage[FRAMES];

        for (int i = 0; i < FRAMES; i++) {
            frames[i] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            // Walking animation cycle
            double walkPhase = (2 * Math.PI * i) / FRAMES;
            int legSwing = (int) (Math.sin(walkPhase) * 4);
            int armSwing = (int) (Math.sin(walkPhase + Math.PI) * 3);
            int bobOffset = (int) (Math.abs(Math.sin(walkPhase * 2)) * 2);

            drawCharacter(g, 0, bobOffset, legSwing, armSwing, false);
            g.dispose();
        }

        writeGif(frames, path, FRAME_DELAY_MS);
        System.out.println("  Generated: " + path);
    }

    /**
     * Generates the jump animation GIF.
     * Character jumps with arms raised and legs tucked.
     */
    private static void generateJumpSprite(String path) throws Exception {
        BufferedImage[] frames = new BufferedImage[FRAMES];

        for (int i = 0; i < FRAMES; i++) {
            frames[i] = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            // Jump animation phases
            // 0: crouch, 1: launch, 2: peak, 3: descend, 4: land
            float jumpProgress = (float) i / (FRAMES - 1);
            boolean armsUp = (i >= 1 && i <= 3);
            int verticalOffset = 0;
            int legTuck = 0;

            if (i == 0) {
                verticalOffset = 2; // Crouch
                legTuck = -2;
            } else if (i == 1) {
                verticalOffset = -2; // Launch
                legTuck = 2;
            } else if (i == 2) {
                verticalOffset = -4; // Peak
                legTuck = 4;
            } else if (i == 3) {
                verticalOffset = -2; // Descend
                legTuck = 2;
            } else {
                verticalOffset = 1; // Land
                legTuck = -1;
            }

            drawCharacter(g, verticalOffset, 0, legTuck, 0, armsUp);
            g.dispose();
        }

        writeGif(frames, path, FRAME_DELAY_MS);
        System.out.println("  Generated: " + path);
    }

    /**
     * Draws the character at the specified position with animation offsets.
     *
     * @param g Graphics context
     * @param yOffset Vertical offset for jumping/crouching
     * @param breathOffset Breathing vertical offset
     * @param legSwing Leg swing offset for walking
     * @param armSwing Arm swing offset for walking
     * @param armsUp Whether arms are raised (jumping)
     */
    private static void drawCharacter(Graphics2D g, int yOffset, int breathOffset,
                                       int legSwing, int armSwing, boolean armsUp) {
        // Clear with transparency
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setComposite(AlphaComposite.SrcOver);

        int baseY = 4 + yOffset + breathOffset;

        // --- LEGS ---
        // Left leg
        int leftLegX = 10;
        int leftLegY = baseY + 38;
        g.setColor(PANTS_COLOR);
        g.fillRect(leftLegX, leftLegY + Math.max(0, legSwing), 6, 14 - Math.abs(legSwing/2));
        g.setColor(OUTLINE_COLOR);
        g.drawRect(leftLegX, leftLegY + Math.max(0, legSwing), 6, 14 - Math.abs(legSwing/2));

        // Right leg
        int rightLegX = 16;
        int rightLegY = baseY + 38;
        g.setColor(PANTS_COLOR);
        g.fillRect(rightLegX, rightLegY + Math.max(0, -legSwing), 6, 14 - Math.abs(legSwing/2));
        g.setColor(OUTLINE_COLOR);
        g.drawRect(rightLegX, rightLegY + Math.max(0, -legSwing), 6, 14 - Math.abs(legSwing/2));

        // --- SHOES ---
        // Left shoe
        g.setColor(SHOES_COLOR);
        g.fillRect(leftLegX - 1, leftLegY + 14 + Math.max(0, legSwing) - Math.abs(legSwing/2), 8, 4);
        g.setColor(OUTLINE_COLOR);
        g.drawRect(leftLegX - 1, leftLegY + 14 + Math.max(0, legSwing) - Math.abs(legSwing/2), 8, 4);

        // Right shoe
        g.setColor(SHOES_COLOR);
        g.fillRect(rightLegX - 1, rightLegY + 14 + Math.max(0, -legSwing) - Math.abs(legSwing/2), 8, 4);
        g.setColor(OUTLINE_COLOR);
        g.drawRect(rightLegX - 1, rightLegY + 14 + Math.max(0, -legSwing) - Math.abs(legSwing/2), 8, 4);

        // --- BODY (TORSO) ---
        int torsoX = 8;
        int torsoY = baseY + 20;
        g.setColor(SHIRT_COLOR);
        g.fillRect(torsoX, torsoY, 16, 18);
        g.setColor(OUTLINE_COLOR);
        g.drawRect(torsoX, torsoY, 16, 18);

        // Shirt detail (darker stripe)
        g.setColor(SHIRT_COLOR.darker());
        g.fillRect(torsoX + 6, torsoY + 2, 4, 14);

        // --- ARMS ---
        if (armsUp) {
            // Arms raised (jumping)
            // Left arm up
            g.setColor(SKIN_COLOR);
            g.fillRect(4, baseY + 10, 5, 12);
            g.setColor(OUTLINE_COLOR);
            g.drawRect(4, baseY + 10, 5, 12);

            // Right arm up
            g.setColor(SKIN_COLOR);
            g.fillRect(23, baseY + 10, 5, 12);
            g.setColor(OUTLINE_COLOR);
            g.drawRect(23, baseY + 10, 5, 12);
        } else {
            // Arms at sides (with swing)
            // Left arm
            g.setColor(SKIN_COLOR);
            g.fillRect(4 + armSwing/2, baseY + 22, 5, 14);
            g.setColor(OUTLINE_COLOR);
            g.drawRect(4 + armSwing/2, baseY + 22, 5, 14);

            // Right arm
            g.setColor(SKIN_COLOR);
            g.fillRect(23 - armSwing/2, baseY + 22, 5, 14);
            g.setColor(OUTLINE_COLOR);
            g.drawRect(23 - armSwing/2, baseY + 22, 5, 14);
        }

        // --- HEAD ---
        int headX = 8;
        int headY = baseY;
        g.setColor(SKIN_COLOR);
        g.fillOval(headX, headY, 16, 18);
        g.setColor(OUTLINE_COLOR);
        g.drawOval(headX, headY, 16, 18);

        // --- HAIR ---
        g.setColor(HAIR_COLOR);
        g.fillArc(headX - 1, headY - 2, 18, 12, 0, 180);
        g.fillRect(headX, headY, 3, 8);
        g.fillRect(headX + 13, headY, 3, 8);

        // --- FACE ---
        // Eyes
        g.setColor(Color.WHITE);
        g.fillOval(headX + 3, headY + 6, 5, 5);
        g.fillOval(headX + 9, headY + 6, 5, 5);

        g.setColor(EYE_COLOR);
        g.fillOval(headX + 4, headY + 7, 3, 3);
        g.fillOval(headX + 10, headY + 7, 3, 3);

        // Mouth (simple line)
        g.setColor(OUTLINE_COLOR);
        g.drawLine(headX + 6, headY + 13, headX + 10, headY + 13);
    }

    /**
     * Writes frames to an animated GIF file.
     *
     * @param frames Array of BufferedImage frames
     * @param path Output file path
     * @param delayMs Delay between frames in milliseconds
     */
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

            // Convert to indexed color for GIF
            BufferedImage indexed = new BufferedImage(frame.getWidth(), frame.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = indexed.createGraphics();
            g.drawImage(frame, 0, 0, null);
            g.dispose();

            // Create metadata with timing info
            IIOMetadata metadata = writer.getDefaultImageMetadata(
                    new javax.imageio.ImageTypeSpecifier(indexed), null);

            String metaFormat = metadata.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormat);

            // Graphics Control Extension for timing and transparency
            IIOMetadataNode gce = getNode(root, "GraphicControlExtension");
            gce.setAttribute("disposalMethod", "restoreToBackgroundColor");
            gce.setAttribute("userInputFlag", "FALSE");
            gce.setAttribute("transparentColorFlag", "TRUE");
            gce.setAttribute("transparentColorIndex", "0");
            gce.setAttribute("delayTime", String.valueOf(delayMs / 10)); // GIF uses centiseconds

            // Application Extension for looping
            IIOMetadataNode appExtensions = getNode(root, "ApplicationExtensions");
            IIOMetadataNode appExt = new IIOMetadataNode("ApplicationExtension");
            appExt.setAttribute("applicationID", "NETSCAPE");
            appExt.setAttribute("authenticationCode", "2.0");
            appExt.setUserObject(new byte[]{1, 0, 0}); // Loop forever
            appExtensions.appendChild(appExt);

            metadata.setFromTree(metaFormat, root);

            writer.writeToSequence(new javax.imageio.IIOImage(indexed, null, metadata), null);
        }

        writer.endWriteSequence();
        ios.close();
    }

    /**
     * Gets or creates a child node with the specified name.
     */
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
