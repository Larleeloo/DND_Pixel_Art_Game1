package ui;
import entity.*;
import block.*;
import audio.*;

import java.awt.*;
import java.awt.event.MouseEvent;

public class UIButton {
    private int x, y, width, height;
    private String text;
    private Color normalColor;
    private Color hoverColor;
    private Color textColor;
    private boolean isHovered;
    private Runnable onClick;

    public UIButton(int x, int y, int width, int height, String text, Runnable onClick) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.onClick = onClick;
        this.normalColor = new Color(200, 50, 50, 200);
        this.hoverColor = new Color(255, 80, 80, 230);
        this.textColor = Color.WHITE;
        this.isHovered = false;
    }

    public void setColors(Color normal, Color hover, Color text) {
        this.normalColor = normal;
        this.hoverColor = hover;
        this.textColor = text;
    }

    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;
    }

    public void handleMouseMove(int mouseX, int mouseY) {
        isHovered = contains(mouseX, mouseY);
    }

    public void handleClick(int mouseX, int mouseY) {
        if (contains(mouseX, mouseY) && onClick != null) {
            onClick.run();
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw button background
        g2d.setColor(isHovered ? hoverColor : normalColor);
        g2d.fillRoundRect(x, y, width, height, 10, 10);

        // Draw button border
        g2d.setColor(textColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, width, height, 10, 10);

        // Draw text centered
        g2d.setColor(textColor);
        Font font = new Font("Arial", Font.BOLD, 18);
        g2d.setFont(font);

        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();

        g2d.drawString(text, textX, textY);
    }
}