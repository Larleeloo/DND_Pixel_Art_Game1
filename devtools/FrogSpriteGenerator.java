import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates GIF-based sprite animations for frog mobs.
 * Creates hop, tongue, hurt, and sleep animations for each frog variant.
 *
 * Output structure:
 *   assets/mobs/frog/{variant}/
 *     - hop.gif (movement animation)
 *     - tongue.gif (attack animation, 1 second duration)
 *     - hurt.gif (damage reaction)
 *     - sleep.gif (idle animation)
 *
 * Supported variants: purple_frog, green_frog, blue_frog, red_frog, gold_frog, brown_toad
 *
 * Usage:
 *   java FrogSpriteGenerator [variant]
 *   java FrogSpriteGenerator all
 */
public class FrogSpriteGenerator {

    // Sprite dimensions - 32x32 for small frogs
    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 32;
    private static final int FRAME_DELAY = 100; // ms per frame (default)
    private static final int TONGUE_FRAME_DELAY = 100; // 10 frames * 100ms = 1 second

    // Frog configurations
    private static final Map<String, FrogConfig> FROGS = new HashMap<>();

    static {
        FROGS.put("purple_frog", new FrogConfig(
            new Color(128, 60, 180),   // Purple body
            new Color(90, 40, 130),    // Darker spots
            new Color(180, 140, 220),  // Belly
            new Color(255, 100, 150)   // Tongue color (pink)
        ));
        FROGS.put("green_frog", new FrogConfig(
            new Color(50, 150, 50),    // Green body
            new Color(30, 100, 30),    // Darker spots
            new Color(150, 220, 150),  // Belly
            new Color(255, 100, 150)   // Tongue
        ));
        FROGS.put("blue_frog", new FrogConfig(
            new Color(60, 120, 200),   // Blue body
            new Color(40, 80, 150),    // Darker spots
            new Color(150, 200, 255),  // Belly
            new Color(255, 100, 150)   // Tongue
        ));
        FROGS.put("red_frog", new FrogConfig(
            new Color(200, 50, 50),    // Red body (poison dart)
            new Color(150, 30, 30),    // Darker spots
            new Color(255, 150, 150),  // Belly
            new Color(255, 180, 180)   // Tongue
        ));
        FROGS.put("gold_frog", new FrogConfig(
            new Color(220, 180, 50),   // Golden body
            new Color(180, 140, 30),   // Darker spots
            new Color(255, 240, 150),  // Belly
            new Color(255, 100, 150)   // Tongue
        ));
        FROGS.put("brown_toad", new FrogConfig(
            new Color(139, 90, 60),    // Brown body
            new Color(100, 60, 40),    // Darker warts
            new Color(180, 150, 120),  // Belly
            new Color(200, 100, 100)   // Tongue
        ));
    }

    public static void main(String[] args) {
        String variant = args.length > 0 ? args[0].toLowerCase() : "all";

        if (variant.equals("all")) {
            for (String type : FROGS.keySet()) {
                generateFrogSprites(type);
            }
        } else if (FROGS.containsKey(variant)) {
            generateFrogSprites(variant);
        } else {
            System.out.println("Unknown frog variant: " + variant);
            System.out.println("Available variants: " + String.join(", ", FROGS.keySet()));
        }
    }

    private static void generateFrogSprites(String variant) {
        FrogConfig config = FROGS.get(variant);
        String outputDir = "assets/mobs/frog/" + variant;

        // Create output directory
        new File(outputDir).mkdirs();

        System.out.println("Generating sprites for " + variant + "...");

        try {
            // Generate all animation types
            generateSleepAnimation(outputDir, config);
            generateHopAnimation(outputDir, config);
            generateTongueAnimation(outputDir, config);
            generateHurtAnimation(outputDir, config);

            System.out.println("  Generated all animations for " + variant);
        } catch (Exception e) {
            System.err.println("Error generating " + variant + " sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generates the sleep (idle) animation - frog sitting still, blinking occasionally.
     */
    private static void generateSleepAnimation(String outputDir, FrogConfig config) throws Exception {
        List<BufferedImage> frames = new ArrayList<>();
        int[] delays = new int[8];

        // 8 frames - mostly eyes open, blink on frame 5-6
        for (int i = 0; i < 8; i++) {
            BufferedImage frame = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frame.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean eyesClosed = (i == 5 || i == 6);
            drawFrogBody(g, config, 0, 0, eyesClosed);

            g.dispose();
            frames.add(frame);
            delays[i] = (i == 5 || i == 6) ? 80 : 200; // Faster blink frames
        }

        saveGif(outputDir + "/sleep.gif", frames, delays);
    }

    /**
     * Generates the hop animation - frog jumping motion.
     */
    private static void generateHopAnimation(String outputDir, FrogConfig config) throws Exception {
        List<BufferedImage> frames = new ArrayList<>();
        int[] delays = new int[6];

        // 6 frames: crouch, launch, airborne, descend, land, recover
        int[] yOffsets = {2, 0, -4, -6, -4, 0};      // Vertical position offset
        int[] legAngles = {20, -10, -20, -15, 0, 10}; // Leg extension angles

        for (int i = 0; i < 6; i++) {
            BufferedImage frame = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frame.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawFrogHopping(g, config, yOffsets[i], legAngles[i]);

            g.dispose();
            frames.add(frame);
            delays[i] = 100;
        }

        saveGif(outputDir + "/hop.gif", frames, delays);
    }

    /**
     * Generates the tongue attack animation - 10 frames for 1 second duration.
     * Damage occurs at frame 5 (0.5 seconds in).
     */
    private static void generateTongueAnimation(String outputDir, FrogConfig config) throws Exception {
        List<BufferedImage> frames = new ArrayList<>();
        int[] delays = new int[10];

        // 10 frames: anticipation (2), tongue out (3), damage frame (1), retract (3), recover (1)
        // Tongue extends at frames 2-6, damage at frame 5
        double[] tongueExtension = {0, 0, 0.3, 0.6, 0.9, 1.0, 0.8, 0.5, 0.2, 0};

        for (int i = 0; i < 10; i++) {
            BufferedImage frame = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frame.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawFrogWithTongue(g, config, tongueExtension[i]);

            g.dispose();
            frames.add(frame);
            delays[i] = TONGUE_FRAME_DELAY;
        }

        saveGif(outputDir + "/tongue.gif", frames, delays);
    }

    /**
     * Generates the hurt animation - frog recoiling from damage.
     */
    private static void generateHurtAnimation(String outputDir, FrogConfig config) throws Exception {
        List<BufferedImage> frames = new ArrayList<>();
        int[] delays = new int[5];

        // 5 frames: impact, recoil back, flash, recover, normal
        int[] xOffsets = {0, -2, -3, -1, 0};
        boolean[] flashing = {false, true, false, true, false};

        for (int i = 0; i < 5; i++) {
            BufferedImage frame = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frame.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (flashing[i]) {
                // Flash white when hurt
                drawFrogBody(g, new FrogConfig(
                    Color.WHITE, Color.WHITE, Color.WHITE, config.tongueColor
                ), xOffsets[i], 0, false);
            } else {
                drawFrogBody(g, config, xOffsets[i], 0, false);
            }

            g.dispose();
            frames.add(frame);
            delays[i] = 100;
        }

        saveGif(outputDir + "/hurt.gif", frames, delays);
    }

    /**
     * Draws the basic frog body.
     */
    private static void drawFrogBody(Graphics2D g, FrogConfig config, int offsetX, int offsetY, boolean eyesClosed) {
        int centerX = FRAME_WIDTH / 2 + offsetX;
        int baseY = FRAME_HEIGHT - 8 + offsetY;

        // Draw back legs (behind body)
        g.setColor(config.bodyColor.darker());
        g.fillOval(centerX - 12, baseY - 6, 8, 10);  // Left back leg
        g.fillOval(centerX + 4, baseY - 6, 8, 10);   // Right back leg

        // Draw body (oval)
        g.setColor(config.bodyColor);
        g.fillOval(centerX - 10, baseY - 12, 20, 14);

        // Draw belly
        g.setColor(config.bellyColor);
        g.fillOval(centerX - 6, baseY - 8, 12, 8);

        // Draw spots
        g.setColor(config.spotColor);
        g.fillOval(centerX - 6, baseY - 10, 4, 3);
        g.fillOval(centerX + 2, baseY - 11, 3, 2);

        // Draw head (overlapping body)
        g.setColor(config.bodyColor);
        g.fillOval(centerX - 8, baseY - 18, 16, 10);

        // Draw eyes (large, bulging)
        g.setColor(Color.WHITE);
        g.fillOval(centerX - 7, baseY - 20, 6, 6);   // Left eye
        g.fillOval(centerX + 1, baseY - 20, 6, 6);   // Right eye

        // Draw pupils
        if (!eyesClosed) {
            g.setColor(Color.BLACK);
            g.fillOval(centerX - 5, baseY - 18, 3, 3);  // Left pupil
            g.fillOval(centerX + 2, baseY - 18, 3, 3);  // Right pupil
        } else {
            // Closed eyes - horizontal line
            g.setColor(Color.BLACK);
            g.drawLine(centerX - 6, baseY - 17, centerX - 2, baseY - 17);
            g.drawLine(centerX + 2, baseY - 17, centerX + 6, baseY - 17);
        }

        // Draw front legs
        g.setColor(config.bodyColor.darker());
        g.fillOval(centerX - 10, baseY - 4, 6, 5);   // Left front leg
        g.fillOval(centerX + 4, baseY - 4, 6, 5);    // Right front leg
    }

    /**
     * Draws frog in hopping pose.
     */
    private static void drawFrogHopping(Graphics2D g, FrogConfig config, int yOffset, int legAngle) {
        int centerX = FRAME_WIDTH / 2;
        int baseY = FRAME_HEIGHT - 8 + yOffset;

        // Adjust leg positions based on angle
        int backLegExtend = (int)(Math.sin(Math.toRadians(legAngle)) * 4);
        int frontLegExtend = (int)(Math.sin(Math.toRadians(-legAngle)) * 3);

        // Draw back legs (extended behind)
        g.setColor(config.bodyColor.darker());
        g.fillOval(centerX - 14 - backLegExtend, baseY - 4 + Math.abs(backLegExtend), 10, 8);
        g.fillOval(centerX + 4 + backLegExtend, baseY - 4 + Math.abs(backLegExtend), 10, 8);

        // Draw body (oval, slightly squished when jumping)
        g.setColor(config.bodyColor);
        int bodyHeight = legAngle < 0 ? 12 : 14;
        g.fillOval(centerX - 10, baseY - 10 - (14 - bodyHeight), 20, bodyHeight);

        // Draw belly
        g.setColor(config.bellyColor);
        g.fillOval(centerX - 6, baseY - 8, 12, 6);

        // Draw head
        g.setColor(config.bodyColor);
        g.fillOval(centerX - 8, baseY - 16, 16, 10);

        // Draw eyes
        g.setColor(Color.WHITE);
        g.fillOval(centerX - 7, baseY - 18, 6, 6);
        g.fillOval(centerX + 1, baseY - 18, 6, 6);

        // Draw pupils
        g.setColor(Color.BLACK);
        g.fillOval(centerX - 5, baseY - 16, 3, 3);
        g.fillOval(centerX + 2, baseY - 16, 3, 3);

        // Draw front legs (reaching forward)
        g.setColor(config.bodyColor.darker());
        g.fillOval(centerX - 12 + frontLegExtend, baseY - 2, 6, 5);
        g.fillOval(centerX + 6 - frontLegExtend, baseY - 2, 6, 5);
    }

    /**
     * Draws frog with tongue extended.
     */
    private static void drawFrogWithTongue(Graphics2D g, FrogConfig config, double tongueExtent) {
        int centerX = FRAME_WIDTH / 2;
        int baseY = FRAME_HEIGHT - 8;

        // Draw body first
        drawFrogBody(g, config, 0, 0, false);

        // Draw tongue if extended
        if (tongueExtent > 0) {
            int tongueLength = (int)(tongueExtent * 16);
            g.setColor(config.tongueColor);
            g.setStroke(new BasicStroke(2));
            g.drawLine(centerX, baseY - 14, centerX + tongueLength, baseY - 14 - (int)(tongueExtent * 4));

            // Tongue tip (fly catcher)
            if (tongueLength > 4) {
                g.fillOval(centerX + tongueLength - 2, baseY - 16 - (int)(tongueExtent * 4), 4, 4);
            }
        }

        // If mouth is open for tongue attack, redraw mouth area
        if (tongueExtent > 0.2) {
            // Open mouth
            g.setColor(new Color(60, 20, 20));
            g.fillOval(centerX - 3, baseY - 14, 6, 3);
        }
    }

    /**
     * Saves frames as an animated GIF.
     */
    private static void saveGif(String path, List<BufferedImage> frames, int[] delays) throws Exception {
        ImageOutputStream output = new FileImageOutputStream(new File(path));
        ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
        writer.setOutput(output);

        ImageWriteParam params = writer.getDefaultWriteParam();
        ImageTypeSpecifier imageType = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB);
        IIOMetadata metadata = writer.getDefaultImageMetadata(imageType, params);

        writer.prepareWriteSequence(null);

        for (int i = 0; i < frames.size(); i++) {
            // Convert to indexed color for GIF
            BufferedImage indexed = convertToIndexed(frames.get(i));

            IIOMetadata frameMetadata = writer.getDefaultImageMetadata(
                ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_BYTE_INDEXED), params);
            configureGifMetadata(frameMetadata, delays[i], i == 0);

            writer.writeToSequence(new IIOImage(indexed, null, frameMetadata), params);
        }

        writer.endWriteSequence();
        output.close();
        System.out.println("  Saved: " + path);
    }

    /**
     * Converts ARGB image to indexed color for GIF.
     */
    private static BufferedImage convertToIndexed(BufferedImage src) {
        BufferedImage indexed = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
        Graphics2D g = indexed.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return indexed;
    }

    /**
     * Configures GIF metadata for animation.
     */
    private static void configureGifMetadata(IIOMetadata metadata, int delay, boolean first) throws Exception {
        String metaFormat = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormat);

        // Find or create GraphicControlExtension
        IIOMetadataNode gce = getOrCreateNode(root, "GraphicControlExtension");
        gce.setAttribute("disposalMethod", "restoreToBackgroundColor");
        gce.setAttribute("userInputFlag", "FALSE");
        gce.setAttribute("transparentColorFlag", "TRUE");
        gce.setAttribute("transparentColorIndex", "0");
        gce.setAttribute("delayTime", String.valueOf(delay / 10)); // GIF delay is in 1/100ths of second

        // Set application extension for looping (only on first frame)
        if (first) {
            IIOMetadataNode appExtensions = getOrCreateNode(root, "ApplicationExtensions");
            IIOMetadataNode appExtension = new IIOMetadataNode("ApplicationExtension");
            appExtension.setAttribute("applicationID", "NETSCAPE");
            appExtension.setAttribute("authenticationCode", "2.0");
            appExtension.setUserObject(new byte[]{1, 0, 0}); // Loop forever
            appExtensions.appendChild(appExtension);
        }

        metadata.setFromTree(metaFormat, root);
    }

    /**
     * Gets or creates a child node.
     */
    private static IIOMetadataNode getOrCreateNode(IIOMetadataNode root, String nodeName) {
        for (int i = 0; i < root.getLength(); i++) {
            if (root.item(i).getNodeName().equals(nodeName)) {
                return (IIOMetadataNode) root.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        root.appendChild(node);
        return node;
    }

    /**
     * Frog configuration class.
     */
    static class FrogConfig {
        Color bodyColor;
        Color spotColor;
        Color bellyColor;
        Color tongueColor;

        FrogConfig(Color body, Color spots, Color belly, Color tongue) {
            this.bodyColor = body;
            this.spotColor = spots;
            this.bellyColor = belly;
            this.tongueColor = tongue;
        }
    }
}
