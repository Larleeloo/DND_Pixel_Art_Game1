import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Utility class for generating simple bone textures programmatically.
 * Useful for testing and prototyping before creating actual pixel art.
 *
 * ============================================================================
 * 15-BONE SKELETON TEXTURE GENERATION
 * ============================================================================
 *
 * This class generates placeholder textures for the 15-bone humanoid skeleton:
 *
 *   BONE               SIZE      COLOR
 *   ----               ----      -----
 *   torso              6x8 px    Blue shirt (#6496C8)
 *   neck               3x2 px    Skin tone (#FFC896)
 *   head               5x5 px    Skin tone (#FFC896)
 *   arm_upper_left     2x3 px    Skin tone (#FFC896)
 *   arm_lower_left     2x3 px    Skin tone (#FFC896)
 *   hand_left          2x2 px    Darker skin (#FFB482)
 *   arm_upper_right    2x3 px    Skin tone (#FFC896)
 *   arm_lower_right    2x3 px    Skin tone (#FFC896)
 *   hand_right         2x2 px    Darker skin (#FFB482)
 *   leg_upper_left     3x5 px    Dark pants (#505078)
 *   leg_lower_left     3x5 px    Dark pants (#505078)
 *   foot_left          4x2 px    Brown shoes (#3C2814)
 *   leg_upper_right    3x5 px    Dark pants (#505078)
 *   leg_lower_right    3x5 px    Dark pants (#505078)
 *   foot_right         4x2 px    Brown shoes (#3C2814)
 *
 * These textures are scaled by RENDER_SCALE (4x) when displayed.
 *
 * ============================================================================
 */
public class BoneTextureGenerator {

    /**
     * Generates a simple rectangular bone texture with rounded corners.
     * @param width Width in pixels
     * @param height Height in pixels
     * @param color Fill color
     * @param outlineColor Outline color
     * @return Generated BufferedImage
     */
    public static BufferedImage generateRectBone(int width, int height, Color color, Color outlineColor) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Enable anti-aliasing for smoother edges
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw rounded rectangle
        int arc = Math.min(width, height) / 3;
        g.setColor(color);
        g.fillRoundRect(1, 1, width - 2, height - 2, arc, arc);

        // Draw outline
        g.setColor(outlineColor);
        g.setStroke(new BasicStroke(1));
        g.drawRoundRect(1, 1, width - 3, height - 3, arc, arc);

        g.dispose();
        return img;
    }

    /**
     * Generates an oval/ellipse bone texture.
     * @param width Width in pixels
     * @param height Height in pixels
     * @param color Fill color
     * @param outlineColor Outline color
     * @return Generated BufferedImage
     */
    public static BufferedImage generateOvalBone(int width, int height, Color color, Color outlineColor) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(color);
        g.fillOval(1, 1, width - 2, height - 2);

        g.setColor(outlineColor);
        g.setStroke(new BasicStroke(1));
        g.drawOval(1, 1, width - 3, height - 3);

        g.dispose();
        return img;
    }

    /**
     * Generates a limb bone (arm or leg) with a tapered shape.
     * @param width Width in pixels
     * @param height Height in pixels
     * @param color Fill color
     * @param outlineColor Outline color
     * @return Generated BufferedImage
     */
    public static BufferedImage generateLimbBone(int width, int height, Color color, Color outlineColor) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Create a tapered polygon
        int[] xPoints = {width/4, 3*width/4, 2*width/3, width/3};
        int[] yPoints = {0, 0, height, height};

        g.setColor(color);
        g.fillPolygon(xPoints, yPoints, 4);

        // Add rounded ends
        g.fillOval(width/4 - 2, -2, width/2 + 4, 6);
        g.fillOval(width/3 - 2, height - 4, width/3 + 4, 6);

        g.setColor(outlineColor);
        g.setStroke(new BasicStroke(1));
        g.drawPolygon(xPoints, yPoints, 4);

        g.dispose();
        return img;
    }

    /**
     * Generates a complete set of humanoid bone textures and saves them to a directory.
     * @param outputDir Directory to save textures
     * @param skinColor Base skin/body color
     */
    public static void generateHumanoidSet(String outputDir, Color skinColor) {
        Color outline = skinColor.darker().darker();

        try {
            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Torso - larger rectangle
            BufferedImage torso = generateRectBone(12, 16, skinColor, outline);
            ImageIO.write(torso, "PNG", new File(outputDir + "/torso.png"));

            // Head - oval
            BufferedImage head = generateOvalBone(10, 12, skinColor, outline);
            ImageIO.write(head, "PNG", new File(outputDir + "/head.png"));

            // Arms - small rectangles
            BufferedImage armLeft = generateLimbBone(6, 14, skinColor, outline);
            ImageIO.write(armLeft, "PNG", new File(outputDir + "/arm_left.png"));

            BufferedImage armRight = generateLimbBone(6, 14, skinColor, outline);
            ImageIO.write(armRight, "PNG", new File(outputDir + "/arm_right.png"));

            // Legs - longer tapered
            BufferedImage legLeft = generateLimbBone(8, 18, skinColor, outline);
            ImageIO.write(legLeft, "PNG", new File(outputDir + "/leg_left.png"));

            BufferedImage legRight = generateLimbBone(8, 18, skinColor, outline);
            ImageIO.write(legRight, "PNG", new File(outputDir + "/leg_right.png"));

            System.out.println("Generated humanoid bone textures in: " + outputDir);
        } catch (IOException e) {
            System.err.println("Failed to generate bone textures: " + e.getMessage());
        }
    }

    /**
     * Generates placeholder bone textures with a pixel art style.
     * Creates textures for all 15 bones in the humanoid skeleton.
     *
     * @param outputDir Directory to save textures
     * @param primaryColor Main body color (shirt)
     * @param secondaryColor Detail/shading color
     */
    public static void generatePixelArtSet(String outputDir, Color primaryColor, Color secondaryColor) {
        try {
            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Color palette
            Color skinColor = new Color(255, 200, 150);    // #FFC896 - main skin
            Color skinShade = new Color(220, 170, 120);    // #DCAA78 - skin shadow
            Color handColor = new Color(255, 180, 130);    // #FFB482 - hands (darker skin)
            Color handShade = new Color(200, 150, 100);    // #C89664 - hand shadow
            Color pantsColor = new Color(80, 80, 120);     // #505078 - dark pants
            Color pantsShade = new Color(60, 60, 100);     // #3C3C64 - pants shadow
            Color shoeColor = new Color(60, 40, 20);       // #3C2814 - brown shoes
            Color shoeShade = new Color(40, 25, 10);       // #28190A - shoe shadow

            // ====== CORE BONES ======

            // Torso - 6x8 pixel art (main body)
            BufferedImage torso = createPixelArtTorso(6, 8, primaryColor, secondaryColor);
            ImageIO.write(torso, "PNG", new File(outputDir + "/torso.png"));

            // Neck - 3x2 pixel art (skin tone connector)
            BufferedImage neck = createPixelArtNeck(3, 2, skinColor, skinShade);
            ImageIO.write(neck, "PNG", new File(outputDir + "/neck.png"));

            // Head - 5x5 pixel art
            BufferedImage head = createPixelArtHead(5, 5, skinColor, skinShade);
            ImageIO.write(head, "PNG", new File(outputDir + "/head.png"));

            // ====== ARM BONES (6 total) ======

            // Upper arms - 2x3 pixel art (skin color)
            BufferedImage armUpper = createPixelArtLimb(2, 3, skinColor, skinShade);
            ImageIO.write(armUpper, "PNG", new File(outputDir + "/arm_upper_left.png"));
            ImageIO.write(armUpper, "PNG", new File(outputDir + "/arm_upper_right.png"));

            // Lower arms/forearms - 2x3 pixel art (skin color)
            BufferedImage armLower = createPixelArtLimb(2, 3, skinColor, skinShade);
            ImageIO.write(armLower, "PNG", new File(outputDir + "/arm_lower_left.png"));
            ImageIO.write(armLower, "PNG", new File(outputDir + "/arm_lower_right.png"));

            // Hands - 2x2 pixel art (slightly darker skin)
            BufferedImage hand = createPixelArtHand(2, 2, handColor, handShade);
            ImageIO.write(hand, "PNG", new File(outputDir + "/hand_left.png"));
            ImageIO.write(hand, "PNG", new File(outputDir + "/hand_right.png"));

            // ====== LEG BONES (6 total) ======

            // Upper legs/thighs - 3x5 pixel art (pants color)
            BufferedImage legUpper = createPixelArtLimb(3, 5, pantsColor, pantsShade);
            ImageIO.write(legUpper, "PNG", new File(outputDir + "/leg_upper_left.png"));
            ImageIO.write(legUpper, "PNG", new File(outputDir + "/leg_upper_right.png"));

            // Lower legs/calves - 3x5 pixel art (pants color)
            BufferedImage legLower = createPixelArtLimb(3, 5, pantsColor, pantsShade);
            ImageIO.write(legLower, "PNG", new File(outputDir + "/leg_lower_left.png"));
            ImageIO.write(legLower, "PNG", new File(outputDir + "/leg_lower_right.png"));

            // Feet - 4x2 pixel art (brown shoes, wider than tall)
            BufferedImage foot = createPixelArtFoot(4, 2, shoeColor, shoeShade);
            ImageIO.write(foot, "PNG", new File(outputDir + "/foot_left.png"));
            ImageIO.write(foot, "PNG", new File(outputDir + "/foot_right.png"));

            // ====== LEGACY SINGLE-PART LIMBS (for backwards compatibility) ======
            BufferedImage armLeft = createPixelArtLimb(2, 5, skinColor, skinShade);
            ImageIO.write(armLeft, "PNG", new File(outputDir + "/arm_left.png"));
            ImageIO.write(armLeft, "PNG", new File(outputDir + "/arm_right.png"));
            BufferedImage legLeft = createPixelArtLimb(3, 8, pantsColor, pantsShade);
            ImageIO.write(legLeft, "PNG", new File(outputDir + "/leg_left.png"));
            ImageIO.write(legLeft, "PNG", new File(outputDir + "/leg_right.png"));

            System.out.println("Generated 15-bone pixel art textures in: " + outputDir);
            System.out.println("  Core: torso, neck, head");
            System.out.println("  Arms: arm_upper_*, arm_lower_*, hand_*");
            System.out.println("  Legs: leg_upper_*, leg_lower_*, foot_*");
        } catch (IOException e) {
            System.err.println("Failed to generate bone textures: " + e.getMessage());
        }
    }

    /**
     * Creates a pixel art neck texture.
     * Small connector bone between torso and head.
     */
    private static BufferedImage createPixelArtNeck(int w, int h, Color primary, Color secondary) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Main neck shape
        g.setColor(primary);
        g.fillRect(0, 0, w, h);

        // Side shading
        g.setColor(secondary);
        g.fillRect(0, 0, 1, h);

        // Highlight
        g.setColor(primary.brighter());
        g.fillRect(w - 1, 0, 1, h);

        g.dispose();
        return img;
    }

    private static BufferedImage createPixelArtHand(int w, int h, Color primary, Color secondary) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Main hand shape
        g.setColor(primary);
        g.fillRect(0, 0, w, h);

        // Side shading
        g.setColor(secondary);
        g.fillRect(0, 0, 1, h);

        // Highlight
        g.setColor(primary.brighter());
        g.fillRect(w - 1, 0, 1, h);

        g.dispose();
        return img;
    }

    private static BufferedImage createPixelArtFoot(int w, int h, Color primary, Color secondary) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Main foot/shoe shape (wider at front)
        g.setColor(primary);
        g.fillRect(0, 0, w, h);

        // Top shading
        g.setColor(secondary);
        g.fillRect(0, 0, w, 1);

        // Toe highlight
        g.setColor(primary.brighter());
        g.fillRect(w - 2, 1, 2, h - 1);

        g.dispose();
        return img;
    }

    private static BufferedImage createPixelArtTorso(int w, int h, Color primary, Color secondary) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Fill main body
        g.setColor(primary);
        g.fillRect(1, 0, w - 2, h);

        // Add shading on sides
        g.setColor(secondary);
        g.fillRect(0, 1, 1, h - 2);
        g.fillRect(w - 1, 1, 1, h - 2);

        // Add highlight
        g.setColor(primary.brighter());
        g.fillRect(2, 1, 1, h - 2);

        g.dispose();
        return img;
    }

    private static BufferedImage createPixelArtHead(int w, int h, Color primary, Color secondary) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Main head shape
        g.setColor(primary);
        g.fillRect(1, 1, w - 2, h - 2);
        g.fillRect(0, 2, w, h - 4);

        // Shading
        g.setColor(secondary);
        g.fillRect(0, 2, 1, h - 4);

        // Eyes (simple dots)
        g.setColor(Color.BLACK);
        g.fillRect(1, 2, 1, 1);
        g.fillRect(w - 2, 2, 1, 1);

        g.dispose();
        return img;
    }

    private static BufferedImage createPixelArtLimb(int w, int h, Color primary, Color secondary) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Main limb
        g.setColor(primary);
        g.fillRect(0, 0, w, h);

        // Side shading
        g.setColor(secondary);
        g.fillRect(0, 0, 1, h);

        // Highlight
        g.setColor(primary.brighter());
        g.fillRect(w - 1, 0, 1, h);

        g.dispose();
        return img;
    }

    /**
     * Command-line utility to generate bone textures.
     * Usage: java BoneTextureGenerator [outputDir] [style]
     * style: "smooth" or "pixel"
     */
    public static void main(String[] args) {
        String outputDir = "assets/bones";
        String style = "pixel";

        if (args.length > 0) {
            outputDir = args[0];
        }
        if (args.length > 1) {
            style = args[1];
        }

        // Default colors - a pleasant skin tone
        Color skinColor = new Color(255, 200, 150);

        if (style.equals("smooth")) {
            generateHumanoidSet(outputDir, skinColor);
        } else {
            // Pixel art style with shirt and pants colors
            Color shirtColor = new Color(100, 150, 200);  // Blue shirt
            Color pantsColor = new Color(80, 80, 120);    // Dark pants

            generatePixelArtSet(outputDir, shirtColor, pantsColor);
        }

        System.out.println("Bone textures generated successfully!");
        System.out.println("You can now replace these with your own PNG or GIF files.");
    }
}
