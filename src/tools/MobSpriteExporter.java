package tools;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Generates animated GIF sprites for mob entities.
 * Creates idle, walk, run, attack, hurt, and death animations.
 *
 * Humanoid mobs: 32x64 pixels
 * Quadruped mobs: 64x64 pixels
 */
public class MobSpriteExporter {

    private static final int HUMANOID_WIDTH = 32;
    private static final int HUMANOID_HEIGHT = 64;
    private static final int QUADRUPED_WIDTH = 64;
    private static final int QUADRUPED_HEIGHT = 64;
    private static final int FRAME_DELAY = 120; // ms per frame

    public static void main(String[] args) throws Exception {
        // Create output directories
        String baseDir = "assets/mobs";
        new File(baseDir).mkdirs();

        // Generate humanoid mobs
        String[] humanoidMobs = {"zombie", "skeleton", "goblin", "orc", "bandit", "knight", "mage"};
        for (String mob : humanoidMobs) {
            generateHumanoidMob(baseDir, mob);
        }

        // Generate quadruped mobs
        String[] quadrupedMobs = {"wolf", "bear"};
        for (String mob : quadrupedMobs) {
            generateQuadrupedMob(baseDir, mob);
        }

        System.out.println("\n=== Mob Sprite Generation Complete ===");
        System.out.println("Generated sprites in: " + baseDir);
    }

    private static void generateHumanoidMob(String baseDir, String mobName) throws Exception {
        String mobDir = baseDir + "/" + mobName;
        new File(mobDir).mkdirs();

        Color[] colors = getHumanoidColors(mobName);
        Color bodyColor = colors[0];
        Color secondaryColor = colors[1];
        Color accentColor = colors[2];

        // Generate all animation states
        generateHumanoidAnimation(mobDir, "idle", mobName, bodyColor, secondaryColor, accentColor, 6);
        generateHumanoidAnimation(mobDir, "walk", mobName, bodyColor, secondaryColor, accentColor, 8);
        generateHumanoidAnimation(mobDir, "run", mobName, bodyColor, secondaryColor, accentColor, 6);
        generateHumanoidAnimation(mobDir, "attack", mobName, bodyColor, secondaryColor, accentColor, 8);
        generateHumanoidAnimation(mobDir, "hurt", mobName, bodyColor, secondaryColor, accentColor, 4);
        generateHumanoidAnimation(mobDir, "death", mobName, bodyColor, secondaryColor, accentColor, 8);

        System.out.println("Generated humanoid sprites for: " + mobName);
    }

    private static void generateQuadrupedMob(String baseDir, String mobName) throws Exception {
        String mobDir = baseDir + "/" + mobName;
        new File(mobDir).mkdirs();

        Color[] colors = getQuadrupedColors(mobName);
        Color bodyColor = colors[0];
        Color secondaryColor = colors[1];

        // Generate all animation states
        generateQuadrupedAnimation(mobDir, "idle", mobName, bodyColor, secondaryColor, 6);
        generateQuadrupedAnimation(mobDir, "walk", mobName, bodyColor, secondaryColor, 8);
        generateQuadrupedAnimation(mobDir, "run", mobName, bodyColor, secondaryColor, 6);
        generateQuadrupedAnimation(mobDir, "attack", mobName, bodyColor, secondaryColor, 8);
        generateQuadrupedAnimation(mobDir, "hurt", mobName, bodyColor, secondaryColor, 4);
        generateQuadrupedAnimation(mobDir, "death", mobName, bodyColor, secondaryColor, 8);

        System.out.println("Generated quadruped sprites for: " + mobName);
    }

    private static Color[] getHumanoidColors(String mobName) {
        switch (mobName.toLowerCase()) {
            case "zombie":
                return new Color[]{
                    new Color(100, 140, 90),   // Greenish body
                    new Color(70, 100, 60),    // Darker green
                    new Color(150, 80, 80)     // Reddish accents (wounds)
                };
            case "skeleton":
                return new Color[]{
                    new Color(230, 220, 200),  // Bone white
                    new Color(180, 170, 150),  // Darker bone
                    new Color(50, 50, 50)      // Dark eye sockets
                };
            case "goblin":
                return new Color[]{
                    new Color(80, 150, 80),    // Green skin
                    new Color(50, 100, 50),    // Dark green
                    new Color(200, 150, 50)    // Gold/yellow accents
                };
            case "orc":
                return new Color[]{
                    new Color(100, 130, 80),   // Olive green
                    new Color(70, 90, 50),     // Dark olive
                    new Color(80, 50, 30)      // Brown armor
                };
            case "bandit":
                return new Color[]{
                    new Color(180, 140, 100),  // Skin tone
                    new Color(80, 60, 40),     // Dark clothes
                    new Color(150, 50, 50)     // Red bandana
                };
            case "knight":
                return new Color[]{
                    new Color(160, 160, 170),  // Steel armor
                    new Color(100, 100, 110),  // Dark steel
                    new Color(200, 180, 60)    // Gold trim
                };
            case "mage":
                return new Color[]{
                    new Color(100, 60, 140),   // Purple robes
                    new Color(60, 40, 100),    // Dark purple
                    new Color(100, 200, 255)   // Magic glow
                };
            default:
                return new Color[]{
                    new Color(150, 100, 100),
                    new Color(100, 70, 70),
                    new Color(200, 150, 150)
                };
        }
    }

    private static Color[] getQuadrupedColors(String mobName) {
        switch (mobName.toLowerCase()) {
            case "wolf":
                return new Color[]{
                    new Color(120, 110, 100),  // Gray fur
                    new Color(80, 70, 60)      // Dark gray
                };
            case "bear":
                return new Color[]{
                    new Color(100, 70, 50),    // Brown fur
                    new Color(60, 40, 30)      // Dark brown
                };
            default:
                return new Color[]{
                    new Color(130, 100, 80),
                    new Color(90, 60, 50)
                };
        }
    }

    private static void generateHumanoidAnimation(String dir, String animName, String mobName,
            Color body, Color secondary, Color accent, int frameCount) throws Exception {

        BufferedImage[] frames = new BufferedImage[frameCount];

        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(HUMANOID_WIDTH, HUMANOID_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawHumanoidFrame(g, animName, mobName, body, secondary, accent, i, frameCount);

            g.dispose();
        }

        saveAnimation(dir + "/" + animName + ".gif", frames, FRAME_DELAY);
    }

    private static void generateQuadrupedAnimation(String dir, String animName, String mobName,
            Color body, Color secondary, int frameCount) throws Exception {

        BufferedImage[] frames = new BufferedImage[frameCount];

        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(QUADRUPED_WIDTH, QUADRUPED_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawQuadrupedFrame(g, animName, mobName, body, secondary, i, frameCount);

            g.dispose();
        }

        saveAnimation(dir + "/" + animName + ".gif", frames, FRAME_DELAY);
    }

    private static void drawHumanoidFrame(Graphics2D g, String animName, String mobName,
            Color body, Color secondary, Color accent, int frame, int total) {

        int w = HUMANOID_WIDTH;
        int h = HUMANOID_HEIGHT;

        // Animation phase (0.0 to 1.0)
        double phase = (double) frame / total;
        double cycle = Math.sin(phase * Math.PI * 2);

        // Calculate offsets based on animation type
        int bodyOffsetY = 0;
        int legPhase = 0;
        int armSwing = 0;
        double rotation = 0;
        double scale = 1.0;

        switch (animName) {
            case "idle":
                // Gentle breathing
                bodyOffsetY = (int)(cycle * 1);
                break;
            case "walk":
                // Walking motion
                bodyOffsetY = (int)(Math.abs(cycle) * 2);
                legPhase = (int)(cycle * 3);
                armSwing = (int)(cycle * 4);
                break;
            case "run":
                // Running motion
                bodyOffsetY = (int)(Math.abs(cycle) * 3);
                legPhase = (int)(cycle * 5);
                armSwing = (int)(cycle * 6);
                break;
            case "attack":
                // Attack swing
                armSwing = (int)(Math.sin(phase * Math.PI) * 10);
                bodyOffsetY = (int)(Math.sin(phase * Math.PI) * 2);
                break;
            case "hurt":
                // Flinch backward
                bodyOffsetY = (int)(Math.sin(phase * Math.PI) * 3);
                rotation = Math.sin(phase * Math.PI) * 0.1;
                break;
            case "death":
                // Fall over
                rotation = phase * Math.PI / 2; // Rotate 90 degrees
                bodyOffsetY = (int)(phase * 10);
                scale = 1.0 - phase * 0.1;
                break;
        }

        // Apply transforms
        g.translate(w / 2, h / 2);
        g.rotate(rotation);
        g.scale(scale, scale);
        g.translate(-w / 2, -h / 2);

        // Draw body parts (from back to front)
        int centerX = w / 2;
        int groundY = h - 4;

        // Legs
        g.setColor(secondary);
        int legWidth = 5;
        int legHeight = 18;
        int legY = groundY - legHeight - bodyOffsetY;

        // Left leg
        int leftLegX = centerX - 6 - legPhase;
        g.fillRoundRect(leftLegX, legY, legWidth, legHeight, 2, 2);

        // Right leg
        int rightLegX = centerX + 1 + legPhase;
        g.fillRoundRect(rightLegX, legY, legWidth, legHeight, 2, 2);

        // Body/torso
        int torsoWidth = 14;
        int torsoHeight = 20;
        int torsoY = legY - torsoHeight + 4;

        g.setColor(body);
        g.fillRoundRect(centerX - torsoWidth/2, torsoY + bodyOffsetY, torsoWidth, torsoHeight, 4, 4);

        // Arms
        g.setColor(secondary);
        int armWidth = 4;
        int armHeight = 14;
        int armY = torsoY + 2 + bodyOffsetY;

        // Left arm
        g.fillRoundRect(centerX - torsoWidth/2 - armWidth, armY + armSwing/2, armWidth, armHeight, 2, 2);

        // Right arm (attack arm)
        if (animName.equals("attack")) {
            int attackArmX = centerX + torsoWidth/2;
            int attackArmY = armY - armSwing;
            g.fillRoundRect(attackArmX, attackArmY, armWidth + 2, armHeight, 2, 2);

            // Draw weapon for attack
            g.setColor(Color.GRAY);
            g.fillRect(attackArmX + 2, attackArmY - 8, 3, 12);
        } else {
            g.fillRoundRect(centerX + torsoWidth/2, armY - armSwing/2, armWidth, armHeight, 2, 2);
        }

        // Head
        int headSize = 12;
        int headY = torsoY - headSize + 2 + bodyOffsetY;

        g.setColor(body);
        g.fillOval(centerX - headSize/2, headY, headSize, headSize);

        // Eyes based on mob type
        g.setColor(accent);
        if (mobName.equals("skeleton")) {
            // Dark hollow eyes
            g.setColor(Color.BLACK);
            g.fillOval(centerX - 4, headY + 3, 3, 4);
            g.fillOval(centerX + 1, headY + 3, 3, 4);
        } else if (mobName.equals("mage")) {
            // Glowing eyes
            g.setColor(accent);
            g.fillOval(centerX - 3, headY + 4, 2, 2);
            g.fillOval(centerX + 1, headY + 4, 2, 2);
        } else {
            // Normal eyes
            g.setColor(Color.WHITE);
            g.fillOval(centerX - 4, headY + 3, 3, 3);
            g.fillOval(centerX + 1, headY + 3, 3, 3);
            g.setColor(Color.BLACK);
            g.fillOval(centerX - 3, headY + 4, 2, 2);
            g.fillOval(centerX + 2, headY + 4, 2, 2);
        }

        // Mob-specific details
        if (mobName.equals("knight")) {
            // Helmet
            g.setColor(secondary);
            g.fillRect(centerX - 7, headY - 2, 14, 4);
        } else if (mobName.equals("mage")) {
            // Hat
            g.setColor(body);
            int[] hatX = {centerX - 8, centerX, centerX + 8};
            int[] hatY = {headY, headY - 10, headY};
            g.fillPolygon(hatX, hatY, 3);
        } else if (mobName.equals("bandit")) {
            // Bandana
            g.setColor(accent);
            g.fillRect(centerX - 6, headY, 12, 3);
        }
    }

    private static void drawQuadrupedFrame(Graphics2D g, String animName, String mobName,
            Color body, Color secondary, int frame, int total) {

        int w = QUADRUPED_WIDTH;
        int h = QUADRUPED_HEIGHT;

        // Animation phase
        double phase = (double) frame / total;
        double cycle = Math.sin(phase * Math.PI * 2);

        // Calculate animation offsets
        int bodyOffsetY = 0;
        int legPhase = 0;
        double rotation = 0;
        double scale = 1.0;

        switch (animName) {
            case "idle":
                bodyOffsetY = (int)(cycle * 1);
                break;
            case "walk":
                bodyOffsetY = (int)(Math.abs(cycle) * 2);
                legPhase = (int)(cycle * 4);
                break;
            case "run":
                bodyOffsetY = (int)(Math.abs(cycle) * 4);
                legPhase = (int)(cycle * 6);
                break;
            case "attack":
                // Lunge forward
                bodyOffsetY = (int)(Math.sin(phase * Math.PI) * -3);
                break;
            case "hurt":
                bodyOffsetY = (int)(Math.sin(phase * Math.PI) * 3);
                rotation = Math.sin(phase * Math.PI) * 0.1;
                break;
            case "death":
                rotation = phase * Math.PI / 4;
                bodyOffsetY = (int)(phase * 8);
                scale = 1.0 - phase * 0.1;
                break;
        }

        // Apply transforms
        g.translate(w / 2, h / 2);
        g.rotate(rotation);
        g.scale(scale, scale);
        g.translate(-w / 2, -h / 2);

        int centerX = w / 2;
        int groundY = h - 4;

        // Draw tail
        g.setColor(body);
        int tailY = groundY - 28 + bodyOffsetY;
        g.fillOval(6, tailY + 4, 10, 6);

        // Draw body (horizontal oval)
        int bodyWidth = 36;
        int bodyHeight = 22;
        int bodyY = groundY - bodyHeight - 12 + bodyOffsetY;

        g.setColor(body);
        g.fillOval(centerX - bodyWidth/2 + 4, bodyY, bodyWidth, bodyHeight);

        // Legs (4 legs for quadruped)
        g.setColor(secondary);
        int legWidth = 6;
        int legHeight = 14;
        int legY = bodyY + bodyHeight - 4;

        // Front legs
        g.fillRoundRect(centerX + 8 - legPhase, legY, legWidth, legHeight, 2, 2);
        g.fillRoundRect(centerX + 16 + legPhase, legY, legWidth, legHeight, 2, 2);

        // Back legs
        g.fillRoundRect(centerX - 16 + legPhase, legY, legWidth, legHeight, 2, 2);
        g.fillRoundRect(centerX - 8 - legPhase, legY, legWidth, legHeight, 2, 2);

        // Head
        int headWidth = 14;
        int headHeight = 12;
        int headX = centerX + bodyWidth/2 - 4;
        int headY = bodyY - 2 + bodyOffsetY;

        g.setColor(body);
        g.fillOval(headX, headY, headWidth, headHeight);

        // Snout
        g.setColor(secondary);
        g.fillOval(headX + 8, headY + 4, 8, 6);

        // Eyes
        g.setColor(Color.WHITE);
        g.fillOval(headX + 4, headY + 2, 4, 4);
        g.setColor(Color.BLACK);
        g.fillOval(headX + 5, headY + 3, 2, 2);

        // Ears
        g.setColor(body.darker());
        if (mobName.equals("wolf")) {
            // Pointed ears
            int[] earX1 = {headX + 2, headX + 5, headX + 8};
            int[] earY1 = {headY, headY - 6, headY};
            g.fillPolygon(earX1, earY1, 3);
            int[] earX2 = {headX + 6, headX + 9, headX + 12};
            int[] earY2 = {headY, headY - 6, headY};
            g.fillPolygon(earX2, earY2, 3);
        } else if (mobName.equals("bear")) {
            // Rounded ears
            g.fillOval(headX + 1, headY - 3, 5, 5);
            g.fillOval(headX + 8, headY - 3, 5, 5);
        }
    }

    private static void saveAnimation(String path, BufferedImage[] frames, int delay) throws Exception {
        FileOutputStream fos = new FileOutputStream(path);
        GifEncoder encoder = new GifEncoder(fos);
        encoder.setDelay(delay);
        encoder.setRepeat(0); // Loop forever

        for (BufferedImage frame : frames) {
            encoder.addFrame(frame);
        }

        encoder.finish();
        fos.close();
    }
}
