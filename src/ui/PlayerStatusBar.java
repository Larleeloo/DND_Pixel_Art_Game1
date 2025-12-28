package ui;

import entity.player.PlayerBase;
import java.awt.*;

/**
 * UI component that displays player health, mana, and stamina bars.
 * Laid out horizontally with health in center, mana left, stamina right.
 */
public class PlayerStatusBar {

    // Bar dimensions
    private static final int HEALTH_BAR_WIDTH = 180;
    private static final int SIDE_BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 16;
    private static final int BAR_SPACING = 8;
    private static final int CORNER_RADIUS = 4;

    // Colors for each bar type
    private static final Color HEALTH_COLOR = new Color(220, 50, 50);
    private static final Color HEALTH_BG = new Color(80, 20, 20);
    private static final Color MANA_COLOR = new Color(50, 100, 220);
    private static final Color MANA_BG = new Color(20, 40, 80);
    private static final Color STAMINA_COLOR = new Color(50, 200, 50);
    private static final Color STAMINA_BG = new Color(20, 60, 20);

    // Border and background
    private static final Color BORDER_COLOR = new Color(60, 60, 60);
    private static final Color PANEL_BG = new Color(0, 0, 0, 160);

    /**
     * Draws the player status bars above the hotbar in a horizontal layout.
     * Mana | Health | Stamina
     *
     * @param g2d    Graphics context
     * @param player The player to display stats for
     * @param screenWidth  Screen width for centering
     * @param screenHeight Screen height for positioning
     */
    public static void draw(Graphics2D g2d, PlayerBase player, int screenWidth, int screenHeight) {
        if (player == null) return;

        // Calculate total panel width: Mana + spacing + Health + spacing + Stamina
        int panelWidth = SIDE_BAR_WIDTH + BAR_SPACING + HEALTH_BAR_WIDTH + BAR_SPACING + SIDE_BAR_WIDTH + 20;
        int panelHeight = BAR_HEIGHT + 10;
        int panelX = (screenWidth - panelWidth) / 2;
        // Position above hotbar (hotbar is at ~1080-80, so place this at ~1080-140)
        int panelY = screenHeight - 140;

        // Draw panel background
        g2d.setColor(PANEL_BG);
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 8, 8);
        g2d.setColor(BORDER_COLOR);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 8, 8);

        int barY = panelY + 5;
        int currentX = panelX + 10;

        // Draw Mana bar (left)
        drawBar(g2d, currentX, barY, SIDE_BAR_WIDTH, player.getMana(), player.getMaxMana(),
                MANA_COLOR, MANA_BG, "MP");
        currentX += SIDE_BAR_WIDTH + BAR_SPACING;

        // Draw Health bar (center - larger)
        drawBar(g2d, currentX, barY, HEALTH_BAR_WIDTH, player.getHealth(), player.getMaxHealth(),
                HEALTH_COLOR, HEALTH_BG, "HP");
        currentX += HEALTH_BAR_WIDTH + BAR_SPACING;

        // Draw Stamina bar (right)
        drawBar(g2d, currentX, barY, SIDE_BAR_WIDTH, player.getStamina(), player.getMaxStamina(),
                STAMINA_COLOR, STAMINA_BG, "SP");
    }

    /**
     * Draws a single status bar with label and value.
     */
    private static void drawBar(Graphics2D g2d, int x, int y, int barWidth, int current, int max,
                                 Color fillColor, Color bgColor, String label) {
        // Draw background
        g2d.setColor(bgColor);
        g2d.fillRoundRect(x, y, barWidth, BAR_HEIGHT, CORNER_RADIUS, CORNER_RADIUS);

        // Draw filled portion
        if (max > 0 && current > 0) {
            int fillWidth = (int) ((double) current / max * barWidth);
            fillWidth = Math.min(fillWidth, barWidth);

            g2d.setColor(fillColor);
            g2d.fillRoundRect(x, y, fillWidth, BAR_HEIGHT, CORNER_RADIUS, CORNER_RADIUS);

            // Add gradient highlight
            GradientPaint highlight = new GradientPaint(
                    x, y, new Color(255, 255, 255, 60),
                    x, y + BAR_HEIGHT / 2, new Color(255, 255, 255, 0)
            );
            g2d.setPaint(highlight);
            g2d.fillRoundRect(x, y, fillWidth, BAR_HEIGHT / 2, CORNER_RADIUS, CORNER_RADIUS);
        }

        // Draw border
        g2d.setColor(new Color(40, 40, 40));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(x, y, barWidth, BAR_HEIGHT, CORNER_RADIUS, CORNER_RADIUS);

        // Draw label
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.drawString(label, x + 4, y + BAR_HEIGHT - 4);

        // Draw value
        String valueText = current + "/" + max;
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + barWidth - fm.stringWidth(valueText) - 4;
        g2d.drawString(valueText, textX, y + BAR_HEIGHT - 4);
    }
}
