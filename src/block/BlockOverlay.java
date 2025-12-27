package block;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Enum defining overlay types that can be applied on top of base blocks.
 *
 * Overlays are semi-transparent textures rendered on top of base block textures.
 * They can be used for:
 * - GRASS: Grass growing on top of dirt
 * - SNOW: Snow covering any block
 * - ICE: Frozen overlay with transparency
 * - MOSS: Moss growing on stone
 * - VINES: Vines on walls
 *
 * Overlays must be removed/broken before the base block can be mined.
 */
public enum BlockOverlay {
    NONE(null, "None", 0, false),
    GRASS("assets/textures/blocks/overlays/grass_overlay.png", "Grass", 1, true),
    SNOW("assets/textures/blocks/overlays/snow_overlay.png", "Snow", 2, true),
    ICE("assets/textures/blocks/overlays/ice_overlay.png", "Ice", 3, true),
    MOSS("assets/textures/blocks/overlays/moss_overlay.png", "Moss", 1, true),
    VINES("assets/textures/blocks/overlays/vines_overlay.png", "Vines", 1, true);

    private final String texturePath;
    private final String displayName;
    private final int breakSteps;  // Number of hits required to remove overlay
    private final boolean blocksBaseMining;  // Must overlay be removed before mining base?

    BlockOverlay(String texturePath, String displayName, int breakSteps, boolean blocksBaseMining) {
        this.texturePath = texturePath;
        this.displayName = displayName;
        this.breakSteps = breakSteps;
        this.blocksBaseMining = blocksBaseMining;
    }

    /**
     * Gets the path to this overlay's texture file.
     * @return Texture file path or null if NONE
     */
    public String getTexturePath() {
        return texturePath;
    }

    /**
     * Gets the human-readable display name.
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the number of mining hits required to remove this overlay.
     * @return Break steps count
     */
    public int getBreakSteps() {
        return breakSteps;
    }

    /**
     * Whether the overlay blocks mining of the base block.
     * If true, overlay must be removed before base block can be mined.
     * @return true if overlay blocks base mining
     */
    public boolean blocksBaseMining() {
        return blocksBaseMining;
    }

    /**
     * Find an overlay type by its name (case-insensitive).
     * @param name The name to search for
     * @return The matching BlockOverlay, or NONE as default
     */
    public static BlockOverlay fromName(String name) {
        if (name == null || name.isEmpty()) {
            return NONE;
        }

        String upperName = name.toUpperCase().replace(" ", "_");
        for (BlockOverlay overlay : values()) {
            if (overlay.name().equals(upperName)) {
                return overlay;
            }
        }

        // Also try matching display name
        for (BlockOverlay overlay : values()) {
            if (overlay.displayName.equalsIgnoreCase(name)) {
                return overlay;
            }
        }

        return NONE; // Default fallback
    }

    /**
     * Generates a procedural overlay texture if the file doesn't exist.
     * This creates a semi-transparent texture programmatically.
     *
     * @param size The size of the texture to generate
     * @return Generated BufferedImage
     */
    public BufferedImage generateTexture(int size) {
        BufferedImage texture = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = texture.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (this) {
            case GRASS:
                // Green grass tufts on top portion
                g2d.setColor(new Color(0, 0, 0, 0)); // Transparent background
                g2d.fillRect(0, 0, size, size);

                g2d.setColor(new Color(60, 180, 60, 200));
                // Draw grass blades on top
                int bladeWidth = size / 8;
                int bladeHeight = size / 3;
                for (int i = 0; i < 8; i++) {
                    int x = i * bladeWidth;
                    int h = bladeHeight + (int)(Math.random() * bladeHeight / 2);
                    g2d.fillRect(x, 0, bladeWidth, h);
                }
                // Darker green highlights
                g2d.setColor(new Color(40, 140, 40, 180));
                for (int i = 0; i < 4; i++) {
                    int x = (int)(Math.random() * size);
                    int h = (int)(Math.random() * bladeHeight);
                    g2d.fillRect(x, 0, bladeWidth / 2, h);
                }
                break;

            case SNOW:
                // White snow covering top half
                g2d.setColor(new Color(0, 0, 0, 0));
                g2d.fillRect(0, 0, size, size);

                // Snow layer on top
                g2d.setColor(new Color(255, 255, 255, 220));
                g2d.fillRect(0, 0, size, size / 2);

                // Uneven snow edge
                g2d.setColor(new Color(255, 255, 255, 180));
                for (int x = 0; x < size; x += 4) {
                    int h = (int)(Math.random() * size / 4);
                    g2d.fillRect(x, size / 2, 4, h);
                }

                // Snow sparkles
                g2d.setColor(new Color(255, 255, 255, 255));
                for (int i = 0; i < 5; i++) {
                    int x = (int)(Math.random() * size);
                    int y = (int)(Math.random() * size / 2);
                    g2d.fillRect(x, y, 1, 1);
                }
                break;

            case ICE:
                // Semi-transparent ice coating entire block
                g2d.setColor(new Color(180, 220, 255, 100));
                g2d.fillRect(0, 0, size, size);

                // Ice cracks
                g2d.setColor(new Color(200, 240, 255, 150));
                g2d.setStroke(new BasicStroke(1));
                for (int i = 0; i < 3; i++) {
                    int x1 = (int)(Math.random() * size);
                    int y1 = (int)(Math.random() * size);
                    int x2 = x1 + (int)(Math.random() * size / 2) - size / 4;
                    int y2 = y1 + (int)(Math.random() * size / 2) - size / 4;
                    g2d.drawLine(x1, y1, x2, y2);
                }

                // Ice shine
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.fillRect(size / 4, size / 4, size / 4, 2);
                break;

            case MOSS:
                // Green moss patches
                g2d.setColor(new Color(0, 0, 0, 0));
                g2d.fillRect(0, 0, size, size);

                g2d.setColor(new Color(80, 120, 60, 180));
                for (int i = 0; i < 6; i++) {
                    int x = (int)(Math.random() * size);
                    int y = (int)(Math.random() * size);
                    int r = size / 6 + (int)(Math.random() * size / 6);
                    g2d.fillOval(x - r / 2, y - r / 2, r, r);
                }
                break;

            case VINES:
                // Hanging vines
                g2d.setColor(new Color(0, 0, 0, 0));
                g2d.fillRect(0, 0, size, size);

                g2d.setColor(new Color(40, 100, 40, 200));
                int vineWidth = size / 6;
                for (int i = 0; i < 4; i++) {
                    int x = (int)(Math.random() * size);
                    int length = size / 2 + (int)(Math.random() * size / 2);
                    g2d.fillRect(x, 0, vineWidth, length);
                }
                break;

            default:
                // Transparent for NONE
                g2d.setColor(new Color(0, 0, 0, 0));
                g2d.fillRect(0, 0, size, size);
        }

        g2d.dispose();
        return texture;
    }
}
