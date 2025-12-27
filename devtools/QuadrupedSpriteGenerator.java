import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.io.*;
import java.util.*;

/**
 * Generates GIF-based sprite animations for quadruped (4-legged) mobs.
 * Creates idle, walk, and run animations for each animal type.
 *
 * Output structure:
 *   assets/mobs/{animal}/sprites/
 *     - idle.gif
 *     - walk.gif
 *     - run.gif
 *
 * Supported animals: wolf, dog, cat, horse, pig, cow, sheep, deer, bear, fox
 *
 * Usage:
 *   java QuadrupedSpriteGenerator [animal_type]
 *   java QuadrupedSpriteGenerator all
 */
public class QuadrupedSpriteGenerator {

    // Sprite dimensions - 64x64 for larger, more visible mobs
    private static final int FRAME_WIDTH = 64;
    private static final int FRAME_HEIGHT = 64;
    private static final int FRAME_DELAY = 150; // ms per frame

    // Animal configurations
    private static final Map<String, AnimalConfig> ANIMALS = new HashMap<>();

    static {
        // Canines
        ANIMALS.put("wolf", new AnimalConfig(
            new Color(100, 100, 110), // Gray body
            new Color(80, 80, 90),    // Darker accent
            new Color(180, 180, 180), // Belly
            new Color(60, 60, 70),    // Dark features
            true, false               // Pointed ears, no spots
        ));
        ANIMALS.put("dog", new AnimalConfig(
            new Color(180, 140, 100), // Brown body
            new Color(140, 100, 60),  // Darker accent
            new Color(220, 200, 180), // Belly
            new Color(60, 40, 20),    // Dark features
            true, true                // Pointed ears, has spots
        ));
        ANIMALS.put("fox", new AnimalConfig(
            new Color(220, 140, 60),  // Orange body
            new Color(180, 100, 40),  // Darker accent
            new Color(255, 240, 220), // White belly/chest
            new Color(40, 30, 20),    // Dark features
            true, false               // Pointed ears, no spots
        ));

        // Felines
        ANIMALS.put("cat", new AnimalConfig(
            new Color(160, 140, 120), // Gray-brown body
            new Color(120, 100, 80),  // Darker stripes
            new Color(200, 190, 180), // Belly
            new Color(60, 50, 40),    // Dark features
            true, false               // Pointed ears, has stripes
        ));

        // Large animals
        ANIMALS.put("horse", new AnimalConfig(
            new Color(140, 100, 70),  // Brown body
            new Color(100, 70, 50),   // Darker mane
            new Color(160, 120, 90),  // Lighter areas
            new Color(40, 30, 20),    // Dark hooves
            false, false              // Rounded ears
        ));
        ANIMALS.put("deer", new AnimalConfig(
            new Color(180, 140, 100), // Tan body
            new Color(140, 100, 70),  // Darker back
            new Color(240, 230, 210), // White belly
            new Color(60, 40, 20),    // Dark features
            false, true               // Rounded ears, has spots
        ));
        ANIMALS.put("bear", new AnimalConfig(
            new Color(100, 70, 50),   // Brown body
            new Color(70, 50, 35),    // Darker accent
            new Color(140, 110, 80),  // Lighter muzzle
            new Color(30, 20, 15),    // Dark features
            false, false              // Rounded ears
        ));

        // Farm animals
        ANIMALS.put("pig", new AnimalConfig(
            new Color(255, 180, 160), // Pink body
            new Color(230, 150, 130), // Darker pink
            new Color(255, 200, 180), // Light belly
            new Color(180, 100, 80),  // Darker features
            false, false              // Rounded ears
        ));
        ANIMALS.put("cow", new AnimalConfig(
            new Color(240, 240, 240), // White body
            new Color(60, 40, 30),    // Black spots
            new Color(255, 255, 255), // White
            new Color(40, 30, 20),    // Dark features
            false, true               // Rounded ears, has spots
        ));
        ANIMALS.put("sheep", new AnimalConfig(
            new Color(245, 240, 235), // White wool
            new Color(220, 210, 200), // Wool shadows
            new Color(255, 250, 245), // Light wool
            new Color(60, 50, 45),    // Dark face/legs
            false, false              // Hidden ears in wool
        ));
    }

    public static void main(String[] args) {
        String targetAnimal = args.length > 0 ? args[0].toLowerCase() : "all";

        if (targetAnimal.equals("all")) {
            for (String animal : ANIMALS.keySet()) {
                generateAnimalSprites(animal);
            }
        } else if (ANIMALS.containsKey(targetAnimal)) {
            generateAnimalSprites(targetAnimal);
        } else {
            System.out.println("Unknown animal: " + targetAnimal);
            System.out.println("Available: " + String.join(", ", ANIMALS.keySet()));
        }
    }

    private static void generateAnimalSprites(String animalType) {
        AnimalConfig config = ANIMALS.get(animalType);
        String outputDir = "assets/mobs/" + animalType + "/sprites";

        // Create output directory
        new File(outputDir).mkdirs();

        System.out.println("Generating sprites for: " + animalType);

        try {
            // Generate idle animation (4 frames - subtle breathing)
            generateGif(outputDir + "/idle.gif",
                generateIdleFrames(config), FRAME_DELAY + 50);

            // Generate walk animation (6 frames)
            generateGif(outputDir + "/walk.gif",
                generateWalkFrames(config), FRAME_DELAY);

            // Generate run animation (4 frames - faster)
            generateGif(outputDir + "/run.gif",
                generateRunFrames(config), FRAME_DELAY - 50);

            System.out.println("  Created: " + outputDir + "/{idle,walk,run}.gif");

        } catch (Exception e) {
            System.err.println("Error generating " + animalType + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static BufferedImage[] generateIdleFrames(AnimalConfig config) {
        BufferedImage[] frames = new BufferedImage[4];

        for (int i = 0; i < 4; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            // Subtle breathing motion
            int breathOffset = (i == 1 || i == 2) ? 1 : 0;

            drawQuadruped(g, config, 0, breathOffset, new int[]{0, 0, 0, 0});
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateWalkFrames(AnimalConfig config) {
        BufferedImage[] frames = new BufferedImage[6];

        // Leg positions for walk cycle (diagonal gait)
        int[][] legOffsets = {
            {2, -1, -1, 2},   // Frame 0: front-left/back-right forward
            {1, 0, 0, 1},     // Frame 1: transitioning
            {0, 1, 1, 0},     // Frame 2: middle
            {-1, 2, 2, -1},   // Frame 3: front-right/back-left forward
            {0, 1, 1, 0},     // Frame 4: transitioning
            {1, 0, 0, 1},     // Frame 5: middle
        };

        for (int i = 0; i < 6; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            int bobOffset = (i == 1 || i == 4) ? -1 : 0;
            drawQuadruped(g, config, bobOffset, 0, legOffsets[i]);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateRunFrames(AnimalConfig config) {
        BufferedImage[] frames = new BufferedImage[4];

        // Leg positions for gallop (front pair, then back pair)
        int[][] legOffsets = {
            {3, 3, -2, -2},   // Frame 0: front legs forward, back legs back
            {1, 1, 1, 1},     // Frame 1: all legs under body
            {-2, -2, 3, 3},   // Frame 2: front legs back, back legs forward
            {0, 0, 0, 0},     // Frame 3: all legs under body
        };

        for (int i = 0; i < 4; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            int bobOffset = (i == 0 || i == 2) ? -2 : 0;
            drawQuadruped(g, config, bobOffset, 0, legOffsets[i]);
            g.dispose();
        }
        return frames;
    }

    private static void drawQuadruped(Graphics2D g, AnimalConfig config,
            int yOffset, int breathOffset, int[] legOffsets) {

        // Scale factor for 64x64 (was designed for 48x32)
        float scale = 1.5f;

        int centerX = FRAME_WIDTH / 2;
        int groundY = FRAME_HEIGHT - 6;

        // Leg positions: front-left, front-right, back-left, back-right
        int legWidth = (int)(6 * scale);
        int legHeight = (int)(14 * scale);

        // Scale offsets
        int scaledYOffset = (int)(yOffset * scale);
        int[] scaledLegOffsets = new int[4];
        for (int i = 0; i < 4; i++) {
            scaledLegOffsets[i] = (int)(legOffsets[i] * scale);
        }

        // Draw back legs first (behind body)
        g.setColor(darken(config.bodyColor, 0.8f));

        // Back-left leg
        int blLegX = centerX - (int)(14 * scale);
        int blLegY = groundY - legHeight + scaledLegOffsets[2];
        g.fillRect(blLegX, blLegY + scaledYOffset, legWidth, legHeight);

        // Back-right leg
        int brLegX = centerX - (int)(7 * scale);
        int brLegY = groundY - legHeight + scaledLegOffsets[3];
        g.fillRect(brLegX, brLegY + scaledYOffset, legWidth, legHeight);

        // Draw body
        int bodyWidth = (int)(36 * scale);
        int bodyHeight = (int)(18 * scale) + (int)(breathOffset * scale);
        int bodyX = centerX - bodyWidth / 2;
        int bodyY = groundY - legHeight - bodyHeight + scaledYOffset;

        g.setColor(config.bodyColor);
        g.fillRoundRect(bodyX, bodyY, bodyWidth, bodyHeight, 6, 6);

        // Draw spots or patterns if applicable
        if (config.hasSpots) {
            g.setColor(config.accentColor);
            g.fillOval(bodyX + (int)(6 * scale), bodyY + (int)(3 * scale), (int)(6 * scale), (int)(6 * scale));
            g.fillOval(bodyX + (int)(18 * scale), bodyY + (int)(6 * scale), (int)(8 * scale), (int)(6 * scale));
            g.fillOval(bodyX + (int)(12 * scale), bodyY + (int)(9 * scale), (int)(5 * scale), (int)(5 * scale));
        }

        // Draw belly
        g.setColor(config.bellyColor);
        g.fillRect(bodyX + (int)(6 * scale), bodyY + bodyHeight - (int)(6 * scale), bodyWidth - (int)(12 * scale), (int)(6 * scale));

        // Draw front legs (in front of body)
        g.setColor(config.bodyColor);

        // Front-left leg
        int flLegX = centerX + (int)(4 * scale);
        int flLegY = groundY - legHeight + scaledLegOffsets[0];
        g.fillRect(flLegX, flLegY + scaledYOffset, legWidth, legHeight);

        // Front-right leg
        int frLegX = centerX + (int)(11 * scale);
        int frLegY = groundY - legHeight + scaledLegOffsets[1];
        g.fillRect(frLegX, frLegY + scaledYOffset, legWidth, legHeight);

        // Draw hooves/paws
        g.setColor(config.darkColor);
        int hoofHeight = (int)(3 * scale);
        g.fillRect(blLegX, groundY - hoofHeight, legWidth, hoofHeight);
        g.fillRect(brLegX, groundY - hoofHeight, legWidth, hoofHeight);
        g.fillRect(flLegX, groundY - hoofHeight, legWidth, hoofHeight);
        g.fillRect(frLegX, groundY - hoofHeight, legWidth, hoofHeight);

        // Draw neck
        int neckWidth = (int)(10 * scale);
        int neckHeight = (int)(12 * scale);
        int neckX = bodyX + bodyWidth - (int)(10 * scale);
        int neckY = bodyY - (int)(6 * scale);
        g.setColor(config.bodyColor);
        g.fillRect(neckX, neckY, neckWidth, neckHeight);

        // Draw head
        int headWidth = (int)(16 * scale);
        int headHeight = (int)(12 * scale);
        int headX = neckX + (int)(3 * scale);
        int headY = neckY - headHeight + (int)(3 * scale);
        g.setColor(config.bodyColor);
        g.fillRoundRect(headX, headY, headWidth, headHeight, 5, 5);

        // Draw ears
        g.setColor(config.bodyColor);
        if (config.pointedEars) {
            // Pointed ears (wolf, dog, fox, cat)
            int earSize = (int)(6 * scale);
            int[] earX1 = {headX + (int)(2 * scale), headX + (int)(5 * scale), headX + (int)(8 * scale)};
            int[] earY1 = {headY, headY - earSize, headY};
            g.fillPolygon(earX1, earY1, 3);

            int[] earX2 = {headX + (int)(8 * scale), headX + (int)(11 * scale), headX + (int)(14 * scale)};
            int[] earY2 = {headY, headY - earSize, headY};
            g.fillPolygon(earX2, earY2, 3);
        } else {
            // Rounded ears (horse, cow, pig, etc.)
            int earSize = (int)(6 * scale);
            g.fillOval(headX, headY - (int)(3 * scale), earSize, earSize);
            g.fillOval(headX + (int)(10 * scale), headY - (int)(3 * scale), earSize, earSize);
        }

        // Draw eye
        g.setColor(config.darkColor);
        int eyeSize = (int)(4 * scale);
        g.fillOval(headX + headWidth - (int)(6 * scale), headY + (int)(3 * scale), eyeSize, eyeSize);

        // Draw nose/snout
        g.setColor(config.darkColor);
        g.fillOval(headX + headWidth - (int)(3 * scale), headY + (int)(6 * scale), (int)(5 * scale), (int)(4 * scale));

        // Draw tail
        g.setColor(config.accentColor);
        int tailX = bodyX - (int)(4 * scale);
        int tailY = bodyY + (int)(3 * scale);
        g.fillOval(tailX, tailY, (int)(6 * scale), (int)(10 * scale));
    }

    private static Color darken(Color c, float factor) {
        return new Color(
            Math.max(0, (int)(c.getRed() * factor)),
            Math.max(0, (int)(c.getGreen() * factor)),
            Math.max(0, (int)(c.getBlue() * factor)),
            c.getAlpha()
        );
    }

    private static void generateGif(String filename, BufferedImage[] frames, int delay) throws Exception {
        ImageOutputStream output = new FileImageOutputStream(new File(filename));

        ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
        ImageWriteParam params = writer.getDefaultWriteParam();

        writer.setOutput(output);
        writer.prepareWriteSequence(null);

        for (BufferedImage frame : frames) {
            // Convert to indexed color for GIF
            BufferedImage indexed = new BufferedImage(
                frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = indexed.createGraphics();
            g.drawImage(frame, 0, 0, null);
            g.dispose();

            IIOMetadata metadata = writer.getDefaultImageMetadata(
                new ImageTypeSpecifier(indexed), params);

            configureGifMetadata(metadata, delay);

            writer.writeToSequence(new IIOImage(indexed, null, metadata), params);
        }

        writer.endWriteSequence();
        output.close();
    }

    private static void configureGifMetadata(IIOMetadata metadata, int delay) throws Exception {
        String metaFormat = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormat);

        // Find or create GraphicControlExtension
        IIOMetadataNode gce = getNode(root, "GraphicControlExtension");
        gce.setAttribute("disposalMethod", "restoreToBackgroundColor");
        gce.setAttribute("userInputFlag", "FALSE");
        gce.setAttribute("transparentColorFlag", "TRUE");
        gce.setAttribute("delayTime", String.valueOf(delay / 10)); // In 1/100ths of a second
        gce.setAttribute("transparentColorIndex", "0");

        // Application extension for looping
        IIOMetadataNode appExtensions = getNode(root, "ApplicationExtensions");
        IIOMetadataNode appNode = new IIOMetadataNode("ApplicationExtension");
        appNode.setAttribute("applicationID", "NETSCAPE");
        appNode.setAttribute("authenticationCode", "2.0");
        appNode.setUserObject(new byte[]{0x1, 0x0, 0x0}); // Loop forever
        appExtensions.appendChild(appNode);

        metadata.setFromTree(metaFormat, root);
    }

    private static IIOMetadataNode getNode(IIOMetadataNode root, String nodeName) {
        for (int i = 0; i < root.getLength(); i++) {
            if (root.item(i).getNodeName().equals(nodeName)) {
                return (IIOMetadataNode) root.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        root.appendChild(node);
        return node;
    }

    // Animal configuration class
    static class AnimalConfig {
        Color bodyColor;
        Color accentColor;
        Color bellyColor;
        Color darkColor;
        boolean pointedEars;
        boolean hasSpots;

        AnimalConfig(Color body, Color accent, Color belly, Color dark,
                     boolean pointed, boolean spots) {
            this.bodyColor = body;
            this.accentColor = accent;
            this.bellyColor = belly;
            this.darkColor = dark;
            this.pointedEars = pointed;
            this.hasSpots = spots;
        }
    }
}
