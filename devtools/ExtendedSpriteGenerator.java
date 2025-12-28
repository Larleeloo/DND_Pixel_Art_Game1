import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.io.*;
import java.util.*;

/**
 * Extended sprite generator that creates all animation states including:
 * - idle, walk, run, sprint (movement)
 * - jump, double_jump, triple_jump, fall (jumping)
 * - attack, fire, cast (combat)
 * - use_item, eat (item usage)
 * - hurt, dead (reactions)
 *
 * Animations use 5-15 frames for smooth, clear motion as specified.
 *
 * Output structure:
 *   assets/player/sprites/ or assets/mobs/{type}/sprites/
 *     - idle.gif (8 frames)
 *     - walk.gif (6 frames)
 *     - run.gif (6 frames)
 *     - sprint.gif (6 frames)
 *     - jump.gif (5 frames)
 *     - double_jump.gif (10 frames)
 *     - triple_jump.gif (12 frames)
 *     - fall.gif (5 frames)
 *     - attack.gif (8 frames)
 *     - fire.gif (10 frames)
 *     - cast.gif (12 frames)
 *     - use_item.gif (8 frames)
 *     - eat.gif (12 frames)
 *     - hurt.gif (6 frames)
 *     - dead.gif (10 frames)
 *
 * Usage:
 *   java ExtendedSpriteGenerator player
 *   java ExtendedSpriteGenerator mob zombie
 *   java ExtendedSpriteGenerator all
 */
public class ExtendedSpriteGenerator {

    // Sprite dimensions
    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 64;
    private static final int FRAME_DELAY = 100; // ms per frame

    // Character configurations
    private static class CharacterConfig {
        Color skinColor;
        Color skinShadow;
        Color clothesColor;
        Color clothesShadow;
        Color hairColor;
        Color eyeColor;
        boolean hasWeapon;
        boolean hasArmor;

        CharacterConfig(Color skin, Color skinShadow, Color clothes, Color clothesShadow,
                        Color hair, Color eyes, boolean weapon, boolean armor) {
            this.skinColor = skin;
            this.skinShadow = skinShadow;
            this.clothesColor = clothes;
            this.clothesShadow = clothesShadow;
            this.hairColor = hair;
            this.eyeColor = eyes;
            this.hasWeapon = weapon;
            this.hasArmor = armor;
        }
    }

    private static final Map<String, CharacterConfig> CONFIGS = new HashMap<>();

    static {
        // Player character
        CONFIGS.put("player", new CharacterConfig(
            new Color(230, 190, 160),  // Skin
            new Color(200, 160, 130),  // Skin shadow
            new Color(100, 120, 180),  // Blue clothes
            new Color(70, 90, 140),    // Clothes shadow
            new Color(80, 50, 30),     // Brown hair
            new Color(60, 120, 180),   // Blue eyes
            true, false
        ));

        // Mobs
        CONFIGS.put("zombie", new CharacterConfig(
            new Color(120, 150, 110), new Color(90, 120, 80),
            new Color(70, 80, 60), new Color(50, 60, 40),
            new Color(40, 50, 30), new Color(200, 50, 50),
            false, false
        ));

        CONFIGS.put("skeleton", new CharacterConfig(
            new Color(240, 230, 210), new Color(200, 190, 170),
            new Color(60, 50, 40), new Color(40, 30, 20),
            null, new Color(255, 100, 100),
            true, false
        ));

        CONFIGS.put("goblin", new CharacterConfig(
            new Color(100, 160, 80), new Color(70, 130, 50),
            new Color(120, 90, 50), new Color(90, 60, 30),
            null, new Color(255, 200, 50),
            true, false
        ));

        CONFIGS.put("orc", new CharacterConfig(
            new Color(90, 130, 70), new Color(60, 100, 40),
            new Color(100, 70, 40), new Color(70, 50, 30),
            new Color(30, 30, 30), new Color(255, 100, 50),
            true, true
        ));

        CONFIGS.put("wizard", new CharacterConfig(
            new Color(220, 180, 150), new Color(190, 150, 120),
            new Color(100, 50, 150), new Color(70, 30, 110),
            new Color(150, 150, 160), new Color(100, 200, 255),
            true, false
        ));

        CONFIGS.put("knight", new CharacterConfig(
            new Color(220, 180, 150), new Color(190, 150, 120),
            new Color(180, 180, 190), new Color(130, 130, 140),
            new Color(80, 60, 40), new Color(80, 120, 180),
            true, true
        ));
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage:");
            System.out.println("  java ExtendedSpriteGenerator player");
            System.out.println("  java ExtendedSpriteGenerator mob <type>");
            System.out.println("  java ExtendedSpriteGenerator all");
            System.out.println("\nAvailable mob types: " + String.join(", ", CONFIGS.keySet()));
            return;
        }

        String command = args[0].toLowerCase();

        if (command.equals("player")) {
            generateSprites("player", "assets/player/sprites");
        } else if (command.equals("mob") && args.length > 1) {
            String mobType = args[1].toLowerCase();
            if (CONFIGS.containsKey(mobType)) {
                generateSprites(mobType, "assets/mobs/" + mobType + "/sprites");
            } else {
                System.out.println("Unknown mob type: " + mobType);
            }
        } else if (command.equals("all")) {
            // Generate player
            generateSprites("player", "assets/player/sprites");

            // Generate all mobs
            for (String type : CONFIGS.keySet()) {
                if (!type.equals("player")) {
                    generateSprites(type, "assets/mobs/" + type + "/sprites");
                }
            }
        }
    }

    private static void generateSprites(String type, String outputDir) {
        CharacterConfig config = CONFIGS.get(type);
        if (config == null) {
            config = CONFIGS.get("player");
        }

        new File(outputDir).mkdirs();
        System.out.println("\nGenerating extended sprites for: " + type);
        System.out.println("Output: " + outputDir);

        try {
            // Movement animations
            generateGif(outputDir + "/idle.gif", generateIdleFrames(config, 8), FRAME_DELAY + 50);
            generateGif(outputDir + "/walk.gif", generateWalkFrames(config, 6), FRAME_DELAY);
            generateGif(outputDir + "/run.gif", generateRunFrames(config, 6), FRAME_DELAY - 20);
            generateGif(outputDir + "/sprint.gif", generateSprintFrames(config, 6), FRAME_DELAY - 40);

            // Jump animations
            generateGif(outputDir + "/jump.gif", generateJumpFrames(config, 5), FRAME_DELAY);
            generateGif(outputDir + "/double_jump.gif", generateDoubleJumpFrames(config, 10), FRAME_DELAY - 20);
            generateGif(outputDir + "/triple_jump.gif", generateTripleJumpFrames(config, 12), FRAME_DELAY - 30);
            generateGif(outputDir + "/fall.gif", generateFallFrames(config, 5), FRAME_DELAY);

            // Combat animations
            generateGif(outputDir + "/attack.gif", generateAttackFrames(config, 8), FRAME_DELAY - 10);
            generateGif(outputDir + "/fire.gif", generateFireFrames(config, 10), FRAME_DELAY);
            generateGif(outputDir + "/cast.gif", generateCastFrames(config, 12), FRAME_DELAY + 20);

            // Item usage animations
            generateGif(outputDir + "/use_item.gif", generateUseItemFrames(config, 8), FRAME_DELAY);
            generateGif(outputDir + "/eat.gif", generateEatFrames(config, 12), FRAME_DELAY + 30);

            // Reaction animations
            generateGif(outputDir + "/hurt.gif", generateHurtFrames(config, 6), FRAME_DELAY);
            generateGif(outputDir + "/dead.gif", generateDeadFrames(config, 10), FRAME_DELAY + 50);

            System.out.println("  Generated 15 animation files");

        } catch (Exception e) {
            System.err.println("Error generating sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== Frame Generation Methods ====================

    private static BufferedImage[] generateIdleFrames(CharacterConfig c, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            // Subtle breathing animation
            double breathOffset = Math.sin(i * Math.PI * 2 / frameCount) * 1;
            int yOffset = (int)breathOffset;

            drawCharacter(g, c, 0, yOffset, 0, false, false, false);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateWalkFrames(CharacterConfig c, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            // Bob up and down while walking
            double bobOffset = Math.sin(i * Math.PI * 2 / frameCount * 2) * 2;
            int yOffset = (int)bobOffset;

            // Arm swing
            double armAngle = Math.sin(i * Math.PI * 2 / frameCount) * 20;

            drawCharacter(g, c, (int)armAngle, yOffset, 0, true, false, false);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateRunFrames(CharacterConfig c, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            double bobOffset = Math.sin(i * Math.PI * 2 / frameCount * 2) * 3;
            int yOffset = (int)bobOffset;
            double armAngle = Math.sin(i * Math.PI * 2 / frameCount) * 30;

            drawCharacter(g, c, (int)armAngle, yOffset, 0, true, false, false);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateSprintFrames(CharacterConfig c, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            double bobOffset = Math.sin(i * Math.PI * 2 / frameCount * 2) * 4;
            int yOffset = (int)bobOffset;
            double armAngle = Math.sin(i * Math.PI * 2 / frameCount) * 40;

            // Lean forward while sprinting
            drawCharacterLeaning(g, c, (int)armAngle, yOffset, 10);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateJumpFrames(CharacterConfig c, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            // Arms raised for jump
            int armRaise = (i < frameCount / 2) ? i * 4 : (frameCount - 1 - i) * 4;

            drawCharacter(g, c, -30 - armRaise, 0, 0, false, true, false);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateDoubleJumpFrames(CharacterConfig c, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            // Spin/flip effect for double jump
            double rotation = (i / (double)frameCount) * 360;
            drawCharacterRotated(g, c, rotation);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateTripleJumpFrames(CharacterConfig c, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            // Faster spin with spread arms
            double rotation = (i / (double)frameCount) * 720; // Two full rotations
            drawCharacterRotatedWithArms(g, c, rotation);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateFallFrames(CharacterConfig c, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            // Arms up, legs down
            drawCharacter(g, c, -20, 0, 0, false, false, true);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateAttackFrames(CharacterConfig c, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            // Swing motion: windup -> swing -> followthrough
            int phase = (i * 3) / frameCount;
            int armAngle;
            if (phase == 0) {
                armAngle = -60 + (i * 20);  // Windup
            } else if (phase == 1) {
                armAngle = 60 - ((i - frameCount/3) * 30);  // Swing
            } else {
                armAngle = -30 + ((i - 2*frameCount/3) * 10);  // Follow through
            }

            drawCharacterAttacking(g, c, armAngle);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateFireFrames(CharacterConfig c, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            // Draw, aim, release sequence
            boolean showProjectile = i > frameCount / 2;
            drawCharacterFiring(g, c, i, frameCount, showProjectile);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateCastFrames(CharacterConfig c, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            // Magic casting with glowing hands
            drawCharacterCasting(g, c, i, frameCount);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateUseItemFrames(CharacterConfig c, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            // Raise item to use
            int armRaise = (i < frameCount / 2) ? i * 8 : (frameCount - i) * 8;
            drawCharacterWithItem(g, c, armRaise);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateEatFrames(CharacterConfig c, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            // Eating motion: bring to mouth repeatedly
            boolean atMouth = (i % 4) < 2;
            drawCharacterEating(g, c, atMouth);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateHurtFrames(CharacterConfig c, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            // Flinch backwards
            int flinch = (i < frameCount / 2) ? i * 2 : (frameCount - i) * 2;
            drawCharacterHurt(g, c, flinch);
            g.dispose();
        }
        return frames;
    }

    private static BufferedImage[] generateDeadFrames(CharacterConfig c, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();

            // Fall down sequence
            double fallProgress = Math.min(1.0, i / (double)(frameCount - 2));
            drawCharacterDying(g, c, fallProgress);
            g.dispose();
        }
        return frames;
    }

    // ==================== Drawing Helpers ====================

    private static void drawCharacter(Graphics2D g, CharacterConfig c, int armAngle, int yOffset,
                                       int lean, boolean moving, boolean jumping, boolean falling) {
        int cx = FRAME_WIDTH / 2;
        int cy = FRAME_HEIGHT - 10 + yOffset;

        // Body
        g.setColor(c.clothesColor);
        g.fillRect(cx - 6, cy - 30, 12, 18);
        g.setColor(c.clothesShadow);
        g.fillRect(cx - 6, cy - 30, 3, 18);

        // Legs
        g.setColor(c.clothesShadow);
        if (moving) {
            g.fillRect(cx - 5, cy - 12, 4, 14);
            g.fillRect(cx + 1, cy - 12, 4, 14);
        } else {
            g.fillRect(cx - 4, cy - 12, 3, 14);
            g.fillRect(cx + 1, cy - 12, 3, 14);
        }

        // Head
        g.setColor(c.skinColor);
        g.fillOval(cx - 5, cy - 44, 10, 12);
        g.setColor(c.skinShadow);
        g.fillOval(cx - 5, cy - 44, 3, 12);

        // Hair
        if (c.hairColor != null) {
            g.setColor(c.hairColor);
            g.fillRect(cx - 5, cy - 46, 10, 4);
        }

        // Eyes
        g.setColor(c.eyeColor);
        g.fillRect(cx + 1, cy - 40, 2, 2);

        // Arms
        g.setColor(c.skinColor);
        if (jumping) {
            // Arms up
            g.fillRect(cx - 8, cy - 35, 3, 10);
            g.fillRect(cx + 5, cy - 35, 3, 10);
        } else if (falling) {
            // Arms spread
            g.fillRect(cx - 10, cy - 28, 5, 3);
            g.fillRect(cx + 5, cy - 28, 5, 3);
        } else {
            g.fillRect(cx - 8, cy - 28, 3, 10);
            g.fillRect(cx + 5, cy - 28, 3, 10);
        }
    }

    private static void drawCharacterLeaning(Graphics2D g, CharacterConfig c, int armAngle, int yOffset, int lean) {
        // Similar to drawCharacter but with a forward lean for sprinting
        drawCharacter(g, c, armAngle, yOffset, lean, true, false, false);
    }

    private static void drawCharacterRotated(Graphics2D g, CharacterConfig c, double rotation) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.rotate(Math.toRadians(rotation), FRAME_WIDTH / 2, FRAME_HEIGHT / 2);
        drawCharacter(g2, c, 0, 0, 0, false, true, false);
        g2.dispose();
    }

    private static void drawCharacterRotatedWithArms(Graphics2D g, CharacterConfig c, double rotation) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.rotate(Math.toRadians(rotation), FRAME_WIDTH / 2, FRAME_HEIGHT / 2);
        drawCharacter(g2, c, 0, 0, 0, false, false, true); // Arms spread
        g2.dispose();
    }

    private static void drawCharacterAttacking(Graphics2D g, CharacterConfig c, int armAngle) {
        int cx = FRAME_WIDTH / 2;
        int cy = FRAME_HEIGHT - 10;

        drawCharacter(g, c, 0, 0, 0, false, false, false);

        // Weapon swing
        if (c.hasWeapon) {
            g.setColor(new Color(150, 150, 160));
            int wx = cx + 8 + (int)(Math.cos(Math.toRadians(armAngle)) * 8);
            int wy = cy - 25 + (int)(Math.sin(Math.toRadians(armAngle)) * 8);
            g.fillRect(wx, wy, 12, 3);
        }
    }

    private static void drawCharacterFiring(Graphics2D g, CharacterConfig c, int frame, int total, boolean showProj) {
        drawCharacter(g, c, 0, 0, 0, false, false, false);

        int cx = FRAME_WIDTH / 2;
        int cy = FRAME_HEIGHT - 10;

        // Bow/weapon aiming
        g.setColor(new Color(139, 90, 43));
        g.fillRect(cx + 6, cy - 32, 3, 14);

        // Projectile
        if (showProj) {
            g.setColor(new Color(100, 100, 100));
            int projX = cx + 12 + (frame - total/2) * 3;
            g.fillRect(projX, cy - 26, 8, 2);
        }
    }

    private static void drawCharacterCasting(Graphics2D g, CharacterConfig c, int frame, int total) {
        drawCharacter(g, c, 0, 0, 0, false, false, false);

        int cx = FRAME_WIDTH / 2;
        int cy = FRAME_HEIGHT - 10;

        // Magic glow effect
        int glowAlpha = (int)(100 + 100 * Math.sin(frame * Math.PI * 2 / total));
        g.setColor(new Color(100, 150, 255, glowAlpha));
        g.fillOval(cx + 4, cy - 32, 8, 8);
        g.fillOval(cx - 12, cy - 32, 8, 8);
    }

    private static void drawCharacterWithItem(Graphics2D g, CharacterConfig c, int raise) {
        drawCharacter(g, c, 0, 0, 0, false, false, false);

        int cx = FRAME_WIDTH / 2;
        int cy = FRAME_HEIGHT - 10;

        // Item in hand
        g.setColor(new Color(200, 180, 100));
        g.fillRect(cx + 6, cy - 28 - raise, 6, 6);
    }

    private static void drawCharacterEating(Graphics2D g, CharacterConfig c, boolean atMouth) {
        drawCharacter(g, c, 0, 0, 0, false, false, false);

        int cx = FRAME_WIDTH / 2;
        int cy = FRAME_HEIGHT - 10;

        // Food item
        g.setColor(new Color(200, 150, 100));
        int foodY = atMouth ? cy - 40 : cy - 32;
        g.fillRect(cx + 4, foodY, 5, 4);
    }

    private static void drawCharacterHurt(Graphics2D g, CharacterConfig c, int flinch) {
        int cx = FRAME_WIDTH / 2;
        int cy = FRAME_HEIGHT - 10;

        // Shift character back
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(-flinch, 0);
        drawCharacter(g2, c, 20, 0, 0, false, false, false);
        g2.dispose();

        // Pain effect
        g.setColor(new Color(255, 0, 0, 100));
        g.fillOval(cx - 8, cy - 35, 16, 20);
    }

    private static void drawCharacterDying(Graphics2D g, CharacterConfig c, double progress) {
        int cx = FRAME_WIDTH / 2;
        int cy = FRAME_HEIGHT - 10;

        // Rotate as falling
        Graphics2D g2 = (Graphics2D) g.create();
        g2.rotate(Math.toRadians(progress * 90), cx, cy);
        g2.translate(0, (int)(progress * 20));
        drawCharacter(g2, c, 0, 0, 0, false, false, true);
        g2.dispose();
    }

    // ==================== GIF Writing ====================

    private static void generateGif(String path, BufferedImage[] frames, int delay) throws Exception {
        ImageOutputStream output = new FileImageOutputStream(new File(path));
        GifSequenceWriter writer = new GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, delay, true);

        for (BufferedImage frame : frames) {
            writer.writeToSequence(frame);
        }

        writer.close();
        output.close();
    }

    // GIF writing helper class
    static class GifSequenceWriter {
        protected ImageWriter gifWriter;
        protected ImageWriteParam imageWriteParam;
        protected IIOMetadata imageMetaData;

        public GifSequenceWriter(ImageOutputStream output, int imageType, int delay, boolean loop) throws Exception {
            gifWriter = ImageIO.getImageWritersBySuffix("gif").next();
            imageWriteParam = gifWriter.getDefaultWriteParam();

            ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);
            imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam);

            String metaFormatName = imageMetaData.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);

            IIOMetadataNode gce = getNode(root, "GraphicControlExtension");
            gce.setAttribute("disposalMethod", "restoreToBackgroundColor");
            gce.setAttribute("userInputFlag", "FALSE");
            gce.setAttribute("transparentColorFlag", "TRUE");
            gce.setAttribute("transparentColorIndex", "0");
            gce.setAttribute("delayTime", Integer.toString(delay / 10));

            IIOMetadataNode appExtensions = getNode(root, "ApplicationExtensions");
            IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
            child.setAttribute("applicationID", "NETSCAPE");
            child.setAttribute("authenticationCode", "2.0");
            child.setUserObject(new byte[]{1, 0, 0});
            appExtensions.appendChild(child);

            imageMetaData.setFromTree(metaFormatName, root);
            gifWriter.setOutput(output);
            gifWriter.prepareWriteSequence(null);
        }

        public void writeToSequence(BufferedImage img) throws Exception {
            gifWriter.writeToSequence(new IIOImage(img, null, imageMetaData), imageWriteParam);
        }

        public void close() throws Exception {
            gifWriter.endWriteSequence();
        }

        private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
            for (int i = 0; i < rootNode.getLength(); i++) {
                if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                    return (IIOMetadataNode) rootNode.item(i);
                }
            }
            IIOMetadataNode node = new IIOMetadataNode(nodeName);
            rootNode.appendChild(node);
            return node;
        }
    }
}
