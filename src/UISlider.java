import java.awt.*;

/**
 * A draggable slider UI component for value selection.
 * Supports horizontal sliders with customizable range and colors.
 */
class UISlider {
    private int x, y, width, height;
    private String label;
    private double minValue, maxValue;
    private double currentValue;
    private Color trackColor;
    private Color fillColor;
    private Color handleColor;
    private Color labelColor;
    private boolean isDragging;
    private Runnable onChange;

    // Handle dimensions
    private static final int HANDLE_WIDTH = 12;
    private static final int HANDLE_HEIGHT = 20;

    /**
     * Creates a new slider.
     * @param x X position
     * @param y Y position
     * @param width Slider width
     * @param height Slider height (track height)
     * @param label Label to display
     * @param minValue Minimum value
     * @param maxValue Maximum value
     * @param initialValue Starting value
     */
    public UISlider(int x, int y, int width, int height, String label,
                    double minValue, double maxValue, double initialValue) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.currentValue = Math.max(minValue, Math.min(maxValue, initialValue));
        this.trackColor = new Color(60, 60, 70);
        this.fillColor = new Color(100, 150, 200);
        this.handleColor = new Color(220, 220, 230);
        this.labelColor = new Color(200, 200, 210);
        this.isDragging = false;
    }

    /**
     * Sets the colors for the slider.
     */
    public void setColors(Color track, Color fill, Color handle, Color label) {
        this.trackColor = track;
        this.fillColor = fill;
        this.handleColor = handle;
        this.labelColor = label;
    }

    /**
     * Sets just the fill color (useful for RGB sliders).
     */
    public void setFillColor(Color fill) {
        this.fillColor = fill;
    }

    /**
     * Sets a callback to run when the value changes.
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    /**
     * Gets the current value.
     */
    public double getValue() {
        return currentValue;
    }

    /**
     * Gets the current value as an integer.
     */
    public int getIntValue() {
        return (int) Math.round(currentValue);
    }

    /**
     * Sets the current value.
     */
    public void setValue(double value) {
        this.currentValue = Math.max(minValue, Math.min(maxValue, value));
    }

    /**
     * Gets the normalized value (0.0 to 1.0).
     */
    public double getNormalizedValue() {
        return (currentValue - minValue) / (maxValue - minValue);
    }

    /**
     * Calculates the handle X position based on current value.
     */
    private int getHandleX() {
        double normalized = getNormalizedValue();
        return x + (int)(normalized * (width - HANDLE_WIDTH));
    }

    /**
     * Checks if a point is within the slider's interactive area.
     */
    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width &&
               mouseY >= y - HANDLE_HEIGHT/2 && mouseY <= y + height + HANDLE_HEIGHT/2;
    }

    /**
     * Handles mouse press - starts dragging if on slider.
     */
    public void handleMousePressed(int mouseX, int mouseY) {
        if (contains(mouseX, mouseY)) {
            isDragging = true;
            updateValueFromMouse(mouseX);
        }
    }

    /**
     * Handles mouse release - stops dragging.
     */
    public void handleMouseReleased(int mouseX, int mouseY) {
        isDragging = false;
    }

    /**
     * Handles mouse drag - updates value if dragging.
     */
    public void handleMouseDragged(int mouseX, int mouseY) {
        if (isDragging) {
            updateValueFromMouse(mouseX);
        }
    }

    /**
     * Updates the value based on mouse X position.
     */
    private void updateValueFromMouse(int mouseX) {
        // Clamp mouse X to slider bounds
        int clampedX = Math.max(x, Math.min(x + width, mouseX));

        // Calculate normalized position (0 to 1)
        double normalized = (double)(clampedX - x) / width;

        // Convert to value
        double oldValue = currentValue;
        currentValue = minValue + normalized * (maxValue - minValue);

        // Trigger callback if value changed
        if (currentValue != oldValue && onChange != null) {
            onChange.run();
        }
    }

    /**
     * Draws the slider.
     */
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw label
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.setColor(labelColor);
        g2d.drawString(label, x, y - 5);

        // Draw value on right side
        String valueStr;
        if (maxValue <= 1.0) {
            valueStr = String.format("%.2f", currentValue);
        } else if (maxValue > 255) {
            valueStr = String.format("%.1f", currentValue);
        } else {
            valueStr = String.valueOf(getIntValue());
        }
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(valueStr, x + width - fm.stringWidth(valueStr), y - 5);

        // Draw track background
        g2d.setColor(trackColor);
        g2d.fillRoundRect(x, y, width, height, 4, 4);

        // Draw filled portion
        int fillWidth = (int)(getNormalizedValue() * width);
        g2d.setColor(fillColor);
        g2d.fillRoundRect(x, y, fillWidth, height, 4, 4);

        // Draw track border
        g2d.setColor(new Color(80, 80, 90));
        g2d.drawRoundRect(x, y, width, height, 4, 4);

        // Draw handle
        int handleX = getHandleX();
        int handleY = y - (HANDLE_HEIGHT - height) / 2;

        // Handle shadow
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillRoundRect(handleX + 2, handleY + 2, HANDLE_WIDTH, HANDLE_HEIGHT, 4, 4);

        // Handle body
        g2d.setColor(isDragging ? handleColor.brighter() : handleColor);
        g2d.fillRoundRect(handleX, handleY, HANDLE_WIDTH, HANDLE_HEIGHT, 4, 4);

        // Handle border
        g2d.setColor(new Color(100, 100, 110));
        g2d.drawRoundRect(handleX, handleY, HANDLE_WIDTH, HANDLE_HEIGHT, 4, 4);
    }

    /**
     * Gets the total height including label.
     */
    public int getTotalHeight() {
        return height + 20; // Track height + label space
    }
}
