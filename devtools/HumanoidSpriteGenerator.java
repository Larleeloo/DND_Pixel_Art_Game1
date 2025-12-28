import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.io.*;
import java.util.*;

/**
 * Generates GIF-based sprite animations for humanoid mobs.
 * Creates idle, walk, run, and attack animations for each mob type.
 *
 * Output structure:
 *   assets/mobs/{type}/sprites/
 *     - idle.gif
 *     - walk.gif
 *     - run.gif
 *     - attack.gif
 *
 * Supported types: zombie, skeleton, goblin, orc, bandit, guard, wizard, knight
 *
 * Usage:
 *   java HumanoidSpriteGenerator [mob_type]
 *   java HumanoidSpriteGenerator all
 */
public class HumanoidSpriteGenerator {

    // Sprite dimensions - 64x64 for humanoid mobs
    private static final int FRAME_WIDTH = 64;
    private static final int FRAME_HEIGHT = 64;
    private static final int FRAME_DELAY = 150; // ms per frame

    // Humanoid configurations
    private static final Map<String, HumanoidConfig> HUMANOIDS = new HashMap<>();

    static {
        // Undead
        HUMANOIDS.put("zombie", new HumanoidConfig(
            new Color(100, 140, 100),  // Green-gray skin
            new Color(80, 100, 80),    // Darker skin
            new Color(60, 80, 60),     // Clothes
            new Color(40, 50, 40),     // Dark details
            false, false, false        // No armor, no weapon, no hat
        ));
        HUMANOIDS.put("skeleton", new HumanoidConfig(
            new Color(230, 220, 200),  // Bone white
            new Color(200, 190, 170),  // Bone shadow
            new Color(60, 50, 40),     // Tattered clothes
            new Color(40, 30, 20),     // Dark details
            false, true, false         // No armor, has weapon, no hat
        ));

        // Monsters
        HUMANOIDS.put("goblin", new HumanoidConfig(
            new Color(80, 140, 80),    // Green skin
            new Color(60, 110, 60),    // Darker green
            new Color(100, 80, 50),    // Brown clothes
            new Color(40, 30, 20),     // Dark details
            false, true, false         // No armor, has weapon, no hat
        ));
        HUMANOIDS.put("orc", new HumanoidConfig(
            new Color(80, 120, 80),    // Dark green skin
            new Color(60, 90, 60),     // Darker green
            new Color(80, 60, 40),     // Brown armor
            new Color(40, 30, 20),     // Dark details
            true, true, false          // Has armor, has weapon, no hat
        ));

        // Humans
        HUMANOIDS.put("bandit", new HumanoidConfig(
            new Color(200, 160, 130),  // Skin tone
            new Color(170, 130, 100),  // Skin shadow
            new Color(80, 60, 50),     // Dark clothes
            new Color(40, 30, 20),     // Dark details
            false, true, true          // No armor, has weapon, has hat
        ));
        HUMANOIDS.put("guard", new HumanoidConfig(
            new Color(200, 160, 130),  // Skin tone
            new Color(170, 130, 100),  // Skin shadow
            new Color(60, 80, 120),    // Blue uniform
            new Color(40, 50, 80),     // Dark blue
            true, true, true           // Has armor, has weapon, has hat
        ));
        HUMANOIDS.put("wizard", new HumanoidConfig(
            new Color(200, 160, 130),  // Skin tone
            new Color(170, 130, 100),  // Skin shadow
            new Color(80, 40, 120),    // Purple robes
            new Color(50, 20, 80),     // Dark purple
            false, true, true          // No armor, has staff, has hat
        ));
        HUMANOIDS.put("knight", new HumanoidConfig(
            new Color(200, 160, 130),  // Skin tone
            new Color(170, 130, 100),  // Skin shadow
            new Color(180, 180, 190),  // Silver armor
            new Color(100, 100, 110),  // Dark metal
            true, true, true           // Has armor, has weapon, has helmet
        ));
    }

    public static void main(String[] args) {
        String targetType = args.length > 0 ? args[0].toLowerCase() : "all";

        if (targetType.equals("all")) {
            for (String type : HUMANOIDS.keySet()) {
                generateHumanoidSprites(type);
            }
        } else if (HUMANOIDS.containsKey(targetType)) {
            generateHumanoidSprites(targetType);
        } else {
            System.out.println("Unknown type: " + targetType);
            System.out.println("Available: " + String.join(", ", HUMANOIDS.keySet()));
        }
    }

    private static void generateHumanoidSprites(String mobType) {
        HumanoidConfig config = HUMANOIDS.get(mobType);
        String outputDir = "assets/mobs/" + mobType + "/sprites";

        // Create output directory
        new File(outputDir).mkdirs();

        System.out.println("Generating sprites for: " + mobType);

        try {
            // Generate idle animation (4 frames - breathing)
            generateGif(outputDir + "/idle.gif",
                generateIdleFrames(config), FRAME_DELAY + 50);

            // Generate walk animation (6 frames)
            generateGif(outputDir + "/walk.gif",
                generateWalkFrames(config), FRAME_DELAY);

            // Generate run animation (4 frames)
            generateGif(outputDir + "/run.gif",
                generateRunFrames(config), FRAME_DELAY - 50);

            // Generate attack animation (4 frames)
            generateGif(outputDir + "/attack.gif",
                generateAttackFrames(config), FRAME_DELAY - 30);

            System.out.println("  Created: " + outputDir + "/{idle,walk,run,attack}.gif");

        } catch (Exception e) {
            System.err.println("Error generating " + mobType + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static BufferedImage[] generateIdleFrames(HumanoidConfig config) {
        BufferedImage[] frames = new BufferedImage[4];

        for (int i = 0; i < 4; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            // Subtle breathing motion
            int breathOffset = (i == 1 || i == 2) ? 1 : 0;

            drawHumanoid(g, config, 0, breathOffset, 0, 0, 0);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateWalkFrames(HumanoidConfig config) {
        BufferedImage[] frames = new BufferedImage[6];

        // Leg positions for walk cycle
        int[] legOffsets = {2, 1, 0, -1, 0, 1};
        int[] armOffsets = {-1, 0, 1, 2, 1, 0};

        for (int i = 0; i < 6; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            int bobOffset = (i == 1 || i == 4) ? -1 : 0;
            drawHumanoid(g, config, bobOffset, 0, legOffsets[i], -legOffsets[i], armOffsets[i]);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateRunFrames(HumanoidConfig config) {
        BufferedImage[] frames = new BufferedImage[4];

        // Leg positions for run
        int[] legOffsets = {3, 1, -2, 0};
        int[] armOffsets = {-2, 0, 3, 1};

        for (int i = 0; i < 4; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            int bobOffset = (i == 0 || i == 2) ? -2 : 0;
            drawHumanoid(g, config, bobOffset, 0, legOffsets[i], -legOffsets[i], armOffsets[i]);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateAttackFrames(HumanoidConfig config) {
        BufferedImage[] frames = new BufferedImage[4];

        // Attack swing positions
        int[] armAngles = {0, 45, 90, 45};

        for (int i = 0; i < 4; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            drawHumanoidAttack(g, config, armAngles[i]);
            g.dispose();
        }
        return frames;
    }

    private static void drawHumanoid(Graphics2D g, HumanoidConfig config,
            int yOffset, int breathOffset, int leftLegOffset, int rightLegOffset, int armOffset) {

        int centerX = FRAME_WIDTH / 2;
        int groundY = FRAME_HEIGHT - 4;

        // Dimensions
        int headSize = 14;
        int bodyWidth = 16;
        int bodyHeight = 18 + breathOffset;
        int limbWidth = 6;
        int armLength = 14;
        int legLength = 16;

        // Calculate positions
        int bodyX = centerX - bodyWidth / 2;
        int bodyY = groundY - legLength - bodyHeight + yOffset;
        int headX = centerX - headSize / 2;
        int headY = bodyY - headSize + 2;

        // Draw back arm (behind body)
        g.setColor(config.skinColor);
        int backArmX = bodyX - 2;
        int backArmY = bodyY + 4 + armOffset;
        g.fillRoundRect(backArmX, backArmY, limbWidth, armLength, 3, 3);

        // Draw back leg
        g.setColor(config.clothesColor);
        int backLegX = centerX - 4;
        int backLegY = bodyY + bodyHeight - 2 + rightLegOffset;
        g.fillRoundRect(backLegX, backLegY, limbWidth, legLength, 3, 3);

        // Draw body
        g.setColor(config.clothesColor);
        g.fillRoundRect(bodyX, bodyY, bodyWidth, bodyHeight, 4, 4);

        // Draw armor if applicable
        if (config.hasArmor) {
            g.setColor(config.darkColor);
            g.fillRect(bodyX + 2, bodyY + 2, bodyWidth - 4, 6);
            g.setColor(new Color(150, 150, 160));
            g.drawRect(bodyX + 2, bodyY + 2, bodyWidth - 4, 6);
        }

        // Draw front leg
        g.setColor(config.clothesColor);
        int frontLegX = centerX - 2;
        int frontLegY = bodyY + bodyHeight - 2 + leftLegOffset;
        g.fillRoundRect(frontLegX, frontLegY, limbWidth, legLength, 3, 3);

        // Draw shoes/feet
        g.setColor(config.darkColor);
        g.fillRoundRect(backLegX - 1, groundY - 4, limbWidth + 2, 4, 2, 2);
        g.fillRoundRect(frontLegX - 1, groundY - 4, limbWidth + 2, 4, 2, 2);

        // Draw front arm
        g.setColor(config.skinColor);
        int frontArmX = bodyX + bodyWidth - 4;
        int frontArmY = bodyY + 4 - armOffset;
        g.fillRoundRect(frontArmX, frontArmY, limbWidth, armLength, 3, 3);

        // Draw weapon in front hand if applicable
        if (config.hasWeapon) {
            g.setColor(new Color(120, 100, 80)); // Wood color
            g.fillRect(frontArmX + limbWidth, frontArmY + armLength - 4, 3, 12);
            g.setColor(new Color(180, 180, 190)); // Metal
            g.fillRect(frontArmX + limbWidth - 1, frontArmY + armLength + 6, 5, 8);
        }

        // Draw head
        g.setColor(config.skinColor);
        g.fillOval(headX, headY, headSize, headSize);

        // Draw hat/helmet if applicable
        if (config.hasHat) {
            g.setColor(config.clothesColor);
            if (config.hasArmor) {
                // Helmet
                g.fillArc(headX - 1, headY - 2, headSize + 2, headSize, 0, 180);
            } else {
                // Hat/hood
                g.fillArc(headX - 2, headY - 4, headSize + 4, headSize / 2 + 4, 0, 180);
            }
        }

        // Draw eyes
        g.setColor(config.darkColor);
        g.fillOval(headX + headSize - 5, headY + 5, 3, 3);
    }

    private static void drawHumanoidAttack(Graphics2D g, HumanoidConfig config, int armAngle) {
        int centerX = FRAME_WIDTH / 2;
        int groundY = FRAME_HEIGHT - 4;

        // Dimensions
        int headSize = 14;
        int bodyWidth = 16;
        int bodyHeight = 18;
        int limbWidth = 6;
        int armLength = 14;
        int legLength = 16;

        // Calculate positions
        int bodyX = centerX - bodyWidth / 2;
        int bodyY = groundY - legLength - bodyHeight;
        int headX = centerX - headSize / 2;
        int headY = bodyY - headSize + 2;

        // Draw back arm
        g.setColor(config.skinColor);
        g.fillRoundRect(bodyX - 2, bodyY + 4, limbWidth, armLength, 3, 3);

        // Draw legs
        g.setColor(config.clothesColor);
        g.fillRoundRect(centerX - 4, bodyY + bodyHeight - 2, limbWidth, legLength, 3, 3);
        g.fillRoundRect(centerX - 2, bodyY + bodyHeight - 2, limbWidth, legLength, 3, 3);

        // Draw body
        g.setColor(config.clothesColor);
        g.fillRoundRect(bodyX, bodyY, bodyWidth, bodyHeight, 4, 4);

        // Draw shoes
        g.setColor(config.darkColor);
        g.fillRoundRect(centerX - 5, groundY - 4, limbWidth + 2, 4, 2, 2);
        g.fillRoundRect(centerX - 1, groundY - 4, limbWidth + 2, 4, 2, 2);

        // Draw attack arm with rotation
        Graphics2D g2d = (Graphics2D) g;
        int armPivotX = bodyX + bodyWidth - 2;
        int armPivotY = bodyY + 6;

        g2d.translate(armPivotX, armPivotY);
        g2d.rotate(Math.toRadians(-armAngle));

        g.setColor(config.skinColor);
        g.fillRoundRect(0, -limbWidth/2, armLength, limbWidth, 3, 3);

        // Draw weapon
        if (config.hasWeapon) {
            g.setColor(new Color(120, 100, 80));
            g.fillRect(armLength - 2, -2, 4, 16);
            g.setColor(new Color(180, 180, 190));
            g.fillRect(armLength - 3, 12, 6, 10);
        }

        g2d.rotate(Math.toRadians(armAngle));
        g2d.translate(-armPivotX, -armPivotY);

        // Draw head
        g.setColor(config.skinColor);
        g.fillOval(headX, headY, headSize, headSize);

        // Draw hat/helmet
        if (config.hasHat) {
            g.setColor(config.clothesColor);
            if (config.hasArmor) {
                g.fillArc(headX - 1, headY - 2, headSize + 2, headSize, 0, 180);
            } else {
                g.fillArc(headX - 2, headY - 4, headSize + 4, headSize / 2 + 4, 0, 180);
            }
        }

        // Draw eyes
        g.setColor(config.darkColor);
        g.fillOval(headX + headSize - 5, headY + 5, 3, 3);
    }

    private static void generateGif(String filename, BufferedImage[] frames, int delay) throws Exception {
        ImageOutputStream output = new FileImageOutputStream(new File(filename));

        ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
        ImageWriteParam params = writer.getDefaultWriteParam();

        writer.setOutput(output);
        writer.prepareWriteSequence(null);

        for (BufferedImage frame : frames) {
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

        IIOMetadataNode gce = getNode(root, "GraphicControlExtension");
        gce.setAttribute("disposalMethod", "restoreToBackgroundColor");
        gce.setAttribute("userInputFlag", "FALSE");
        gce.setAttribute("transparentColorFlag", "TRUE");
        gce.setAttribute("delayTime", String.valueOf(delay / 10));
        gce.setAttribute("transparentColorIndex", "0");

        IIOMetadataNode appExtensions = getNode(root, "ApplicationExtensions");
        IIOMetadataNode appNode = new IIOMetadataNode("ApplicationExtension");
        appNode.setAttribute("applicationID", "NETSCAPE");
        appNode.setAttribute("authenticationCode", "2.0");
        appNode.setUserObject(new byte[]{0x1, 0x0, 0x0});
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

    // Humanoid configuration class
    static class HumanoidConfig {
        Color skinColor;
        Color skinShadow;
        Color clothesColor;
        Color darkColor;
        boolean hasArmor;
        boolean hasWeapon;
        boolean hasHat;

        HumanoidConfig(Color skin, Color shadow, Color clothes, Color dark,
                       boolean armor, boolean weapon, boolean hat) {
            this.skinColor = skin;
            this.skinShadow = shadow;
            this.clothesColor = clothes;
            this.darkColor = dark;
            this.hasArmor = armor;
            this.hasWeapon = weapon;
            this.hasHat = hat;
        }
    }
}
