package ui;

import entity.item.Item;
import entity.item.ItemEntity;
import entity.item.ItemRegistry;
import entity.item.RecipeManager;
import entity.item.RecipeManager.Recipe;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * ReverseCraftingUI provides a drag-and-drop interface for deconstructing items.
 *
 * Features:
 * - 1 input slot for the item to deconstruct
 * - 3 output slots showing the resulting components
 * - Real-time recipe matching as item is placed
 * - Taking outputs consumes ONE item from input (supports stacks)
 * - Only works with items marked as "reversible" in recipes
 *
 * Usage:
 *   ReverseCraftingUI ui = new ReverseCraftingUI();
 *   ui.open(screenWidth, screenHeight);  // Positions to right of inventory
 *   // In update loop:
 *   ui.update(mouseX, mouseY);
 *   // In draw loop:
 *   ui.draw(g);
 *   // Draw dragged items on top of all UI:
 *   ui.drawDraggedItemOverlay(g);
 */
public class ReverseCraftingUI {

    // Layout constants
    private static final int SLOT_SIZE = 56;
    private static final int SLOT_PADDING = 8;
    private static final int PANEL_PADDING = 20;

    // UI dimensions (calculated)
    private int x, y;
    private int width, height;

    // State
    private boolean isOpen = false;

    // Slots (1 input + 3 output)
    private CraftingSlot inputSlot;
    private CraftingSlot[] outputSlots = new CraftingSlot[3];

    // Current matched recipe
    private Recipe currentRecipe = null;

    // Drag and drop state
    private CraftingSlot draggedSlot = null;
    private boolean dragFromInput = false;
    private int dragOffsetX, dragOffsetY;
    private int mouseX, mouseY;
    private boolean isDragging = false;

    // Hover state
    private int hoveredSlotIndex = -1;  // -1 = none, 0 = input, 1-3 = output

    // Colors
    private Color panelBackground = new Color(40, 35, 50, 240);
    private Color slotBackground = new Color(60, 55, 70);
    private Color slotHover = new Color(80, 75, 95);
    private Color slotBorder = new Color(100, 95, 120);
    private Color accentColor = new Color(180, 100, 255);  // Purple for deconstruction
    private Color titleColor = new Color(220, 150, 255);

    // Callbacks
    private ItemProducedCallback itemProducedCallback;
    private Runnable onCloseCallback;

    /**
     * Represents a single crafting slot.
     */
    public static class CraftingSlot {
        public String itemId;
        public int stackCount;
        public Item itemTemplate;
        public BufferedImage icon;
        public int x, y;

        public CraftingSlot() {
            this.stackCount = 0;
        }

        public boolean isEmpty() {
            return itemId == null || itemId.isEmpty() || stackCount <= 0;
        }

        public void setItem(String itemId, int count) {
            this.itemId = itemId;
            this.stackCount = count;
            this.itemTemplate = ItemRegistry.getTemplate(itemId);
            if (itemTemplate != null) {
                this.icon = itemTemplate.getIcon();
            } else {
                this.icon = null;
            }
        }

        public void clear() {
            this.itemId = null;
            this.stackCount = 0;
            this.itemTemplate = null;
            this.icon = null;
        }
    }

    /**
     * Callback when items are produced (deconstructed components or returned input).
     */
    public interface ItemProducedCallback {
        void onItemProduced(String itemId, int count);
    }

    /**
     * Creates a reverse crafting UI for deconstructing items.
     */
    public ReverseCraftingUI() {
        inputSlot = new CraftingSlot();
        for (int i = 0; i < 3; i++) {
            outputSlots[i] = new CraftingSlot();
        }
        calculateDimensions();
    }

    /**
     * Compatibility constructor.
     */
    public ReverseCraftingUI(boolean ignored) {
        this();
    }

    private void calculateDimensions() {
        // Layout: [Input] -> [Output1] [Output2] [Output3]
        width = 4 * SLOT_SIZE + 5 * SLOT_PADDING + 40 + PANEL_PADDING * 2;  // 40 for arrow
        height = SLOT_SIZE + PANEL_PADDING * 2 + 50;  // 50 for title
    }

    /**
     * Opens the UI positioned to the right of the inventory.
     * @param screenWidth Screen width for positioning
     * @param screenHeight Screen height for positioning
     */
    public void open(int screenWidth, int screenHeight) {
        // Position to the right of the inventory
        this.x = 420;
        this.y = screenHeight / 2 - height / 2 - 50;

        // Position slots
        int slotY = y + PANEL_PADDING + 40;  // Below title
        int slotX = x + PANEL_PADDING;

        // Input slot on left
        inputSlot.x = slotX;
        inputSlot.y = slotY;

        // Arrow gap, then 3 output slots
        int outputStartX = slotX + SLOT_SIZE + SLOT_PADDING + 40;
        for (int i = 0; i < 3; i++) {
            outputSlots[i].x = outputStartX + i * (SLOT_SIZE + SLOT_PADDING);
            outputSlots[i].y = slotY;
        }

        isOpen = true;
        System.out.println("ReverseCraftingUI: Opened");
    }

    /**
     * Closes the UI and returns any items in input slot.
     */
    public void close() {
        if (!isOpen) return;

        // Return input items to player
        if (!inputSlot.isEmpty() && itemProducedCallback != null) {
            itemProducedCallback.onItemProduced(inputSlot.itemId, inputSlot.stackCount);
        }
        inputSlot.clear();

        // Clear outputs
        for (CraftingSlot slot : outputSlots) {
            slot.clear();
        }
        currentRecipe = null;

        isOpen = false;
        isDragging = false;

        if (onCloseCallback != null) {
            onCloseCallback.run();
        }

        System.out.println("ReverseCraftingUI: Closed");
    }

    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Updates the UI with current mouse position.
     */
    public void update(int mouseX, int mouseY) {
        if (!isOpen) return;

        this.mouseX = mouseX;
        this.mouseY = mouseY;

        // Update hover state
        hoveredSlotIndex = getSlotAtPosition(mouseX, mouseY);
    }

    /**
     * Handles mouse press - starts dragging from input slot or takes from output.
     * Returns true if handled.
     */
    public boolean handleMousePressed(int mouseX, int mouseY) {
        if (!isOpen) return false;

        int slotIndex = getSlotAtPosition(mouseX, mouseY);
        if (slotIndex == -1) return false;

        if (slotIndex == 0) {
            // Input slot - start dragging if has item
            if (!inputSlot.isEmpty()) {
                draggedSlot = inputSlot;
                dragFromInput = true;
                isDragging = true;
                this.mouseX = mouseX;
                this.mouseY = mouseY;
                return true;
            }
        } else if (slotIndex >= 1 && slotIndex <= 3) {
            // Output slot - take deconstructed items
            if (currentRecipe != null && !outputSlots[0].isEmpty()) {
                takeOutput();
                return true;
            }
        }

        return false;
    }

    /**
     * Handles mouse drag - updates drag position.
     */
    public void handleMouseDragged(int mouseX, int mouseY) {
        if (isDragging) {
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }
    }

    /**
     * Handles mouse release - drops item or cancels drag.
     * Returns the item if dropped outside the UI.
     */
    public ItemEntity handleMouseReleased(int mouseX, int mouseY) {
        if (!isDragging || draggedSlot == null) {
            isDragging = false;
            draggedSlot = null;
            dragFromInput = false;
            return null;
        }

        ItemEntity droppedItem = null;

        if (!containsPoint(mouseX, mouseY)) {
            // Dropped outside UI - return item to inventory
            droppedItem = new ItemEntity(mouseX, mouseY, draggedSlot.itemId);
            droppedItem.setStackCount(draggedSlot.stackCount);
            Item linked = ItemRegistry.create(draggedSlot.itemId);
            if (linked != null) {
                droppedItem.setLinkedItem(linked);
            }

            // Clear input slot
            inputSlot.clear();
            updateRecipe();
        }
        // Dropped back on input slot - do nothing

        isDragging = false;
        draggedSlot = null;
        dragFromInput = false;

        return droppedItem;
    }

    /**
     * Attempts to add an item to the input slot.
     * Returns true if item was accepted.
     */
    public boolean addItem(String itemId, int count) {
        if (!isOpen || itemId == null || itemId.isEmpty()) return false;

        // Only accept if input slot is empty
        if (inputSlot.isEmpty()) {
            inputSlot.setItem(itemId, count);
            updateRecipe();
            return true;
        }

        return false;
    }

    /**
     * Gets the slot at the given position.
     * Returns -1 = none, 0 = input, 1-3 = output slots
     */
    private int getSlotAtPosition(int mx, int my) {
        // Check input slot
        if (mx >= inputSlot.x && mx < inputSlot.x + SLOT_SIZE &&
            my >= inputSlot.y && my < inputSlot.y + SLOT_SIZE) {
            return 0;
        }

        // Check output slots
        for (int i = 0; i < 3; i++) {
            CraftingSlot slot = outputSlots[i];
            if (mx >= slot.x && mx < slot.x + SLOT_SIZE &&
                my >= slot.y && my < slot.y + SLOT_SIZE) {
                return i + 1;
            }
        }

        return -1;
    }

    /**
     * Updates the output slots based on input item.
     */
    private void updateRecipe() {
        // Clear outputs first
        for (CraftingSlot slot : outputSlots) {
            slot.clear();
        }

        if (inputSlot.isEmpty()) {
            currentRecipe = null;
            return;
        }

        String itemId = inputSlot.itemId;
        List<Recipe> reverseRecipes = RecipeManager.findReverseRecipes(itemId);

        if (!reverseRecipes.isEmpty()) {
            // Use first available recipe
            currentRecipe = reverseRecipes.get(0);

            // Set output slots with ingredients (up to 3)
            for (int i = 0; i < Math.min(3, currentRecipe.ingredients.size()); i++) {
                outputSlots[i].setItem(currentRecipe.ingredients.get(i), 1);
            }

            System.out.println("ReverseCraftingUI: Reverse recipe found - " + currentRecipe.name +
                " with " + currentRecipe.ingredients.size() + " components");
        } else {
            currentRecipe = null;
            System.out.println("ReverseCraftingUI: Item cannot be deconstructed: " + itemId);
        }
    }

    /**
     * Takes the output items, consuming ONE item from input.
     */
    private void takeOutput() {
        if (currentRecipe == null) return;

        // Give all ingredients to player
        if (itemProducedCallback != null) {
            for (String ingredientId : currentRecipe.ingredients) {
                itemProducedCallback.onItemProduced(ingredientId, 1);
            }
        }

        // Consume ONE item from input (supports stacks)
        inputSlot.stackCount--;
        if (inputSlot.stackCount <= 0) {
            inputSlot.clear();
        }

        // Update recipe - may still have items for another deconstruction
        updateRecipe();
    }

    /**
     * Draws the UI (excluding dragged items which should be drawn on top of all UI).
     */
    public void draw(Graphics g) {
        if (!isOpen) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background panel
        drawBackground(g2d);

        // Draw title
        drawTitle(g2d);

        // Draw input slot
        drawSlot(g2d, inputSlot, 0, hoveredSlotIndex == 0, true);

        // Draw arrow
        drawArrow(g2d);

        // Draw output slots
        for (int i = 0; i < 3; i++) {
            drawSlot(g2d, outputSlots[i], i + 1, hoveredSlotIndex == i + 1, false);
        }

        // Draw tooltip
        if (hoveredSlotIndex >= 0) {
            CraftingSlot slot = getSlotByIndex(hoveredSlotIndex);
            if (slot != null && !slot.isEmpty()) {
                drawTooltip(g2d, slot);
            }
        }
    }

    /**
     * Draws the dragged item overlay. Call this AFTER all other UI is drawn
     * to ensure dragged items appear on top.
     */
    public void drawDraggedItemOverlay(Graphics g) {
        if (!isOpen || !isDragging || draggedSlot == null) return;

        Graphics2D g2d = (Graphics2D) g;
        drawDraggedItem(g2d);
    }

    private CraftingSlot getSlotByIndex(int index) {
        if (index == 0) return inputSlot;
        if (index >= 1 && index <= 3) return outputSlots[index - 1];
        return null;
    }

    private void drawBackground(Graphics2D g2d) {
        // Main panel
        g2d.setColor(panelBackground);
        g2d.fillRoundRect(x, y, width, height, 16, 16);

        // Border
        g2d.setColor(accentColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, width, height, 16, 16);
    }

    private void drawTitle(Graphics2D g2d) {
        // Title bar
        g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 60));
        g2d.fillRect(x + 2, y + 2, width - 4, 32);

        // Title text
        String title = "DECONSTRUCTION TABLE";
        g2d.setColor(titleColor);
        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, x + (width - titleWidth) / 2, y + 22);

        // Subtitle (instructions)
        String subtitle = "Place item to break down";
        g2d.setColor(new Color(180, 180, 180));
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        int subWidth = g2d.getFontMetrics().stringWidth(subtitle);
        g2d.drawString(subtitle, x + (width - subWidth) / 2, y + 36);
    }

    private void drawSlot(Graphics2D g2d, CraftingSlot slot, int index, boolean isHovered, boolean isInput) {
        int sx = slot.x;
        int sy = slot.y;

        // Slot background
        g2d.setColor(isHovered ? slotHover : slotBackground);
        g2d.fillRoundRect(sx, sy, SLOT_SIZE, SLOT_SIZE, 8, 8);

        // Slot border - highlight output if recipe found
        Color borderColor;
        if (!isInput && currentRecipe != null) {
            borderColor = accentColor;
        } else {
            borderColor = slotBorder;
        }
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(!isInput && currentRecipe != null ? 2 : 1));
        g2d.drawRoundRect(sx, sy, SLOT_SIZE, SLOT_SIZE, 8, 8);

        // Draw item if present
        if (!slot.isEmpty() && slot.icon != null) {
            // Don't draw if this slot is being dragged
            if (!(isDragging && isInput && dragFromInput)) {
                // Rarity glow
                if (slot.itemTemplate != null) {
                    Color rarityColor = slot.itemTemplate.getRarity().getColor();
                    g2d.setColor(new Color(rarityColor.getRed(), rarityColor.getGreen(),
                                           rarityColor.getBlue(), 40));
                    g2d.fillRoundRect(sx + 2, sy + 2, SLOT_SIZE - 4, SLOT_SIZE - 4, 6, 6);
                }

                // Item icon
                int iconSize = SLOT_SIZE - 12;
                g2d.drawImage(slot.icon, sx + 6, sy + 6, iconSize, iconSize, null);

                // Stack count
                if (slot.stackCount > 1) {
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 11));
                    String count = String.valueOf(slot.stackCount);
                    int countWidth = g2d.getFontMetrics().stringWidth(count);
                    g2d.drawString(count, sx + SLOT_SIZE - countWidth - 4, sy + SLOT_SIZE - 4);
                }
            }
        }

        // Slot label
        if (isInput) {
            g2d.setColor(new Color(120, 120, 140));
            g2d.setFont(new Font("Arial", Font.PLAIN, 9));
            g2d.drawString("IN", sx + 4, sy + SLOT_SIZE - 4);
        } else {
            g2d.setColor(new Color(120, 120, 140));
            g2d.setFont(new Font("Arial", Font.PLAIN, 9));
            g2d.drawString(String.valueOf(index), sx + 4, sy + SLOT_SIZE - 4);
        }
    }

    private void drawArrow(Graphics2D g2d) {
        int arrowX = inputSlot.x + SLOT_SIZE + SLOT_PADDING + 10;
        int arrowY = inputSlot.y + SLOT_SIZE / 2;

        // Arrow shape
        g2d.setColor(currentRecipe != null ? accentColor : new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Arrow line
        g2d.drawLine(arrowX, arrowY, arrowX + 20, arrowY);

        // Arrow head
        int[] xPoints = {arrowX + 20, arrowX + 14, arrowX + 14};
        int[] yPoints = {arrowY, arrowY - 6, arrowY + 6};
        g2d.fillPolygon(xPoints, yPoints, 3);
    }

    private void drawTooltip(Graphics2D g2d, CraftingSlot slot) {
        if (slot.itemTemplate == null) return;

        Item item = slot.itemTemplate;
        String name = item.getName();
        String rarity = item.getRarity().getDisplayName();

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        int nameWidth = g2d.getFontMetrics().stringWidth(name);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        int infoWidth = g2d.getFontMetrics().stringWidth(rarity);

        int tooltipWidth = Math.max(nameWidth, infoWidth) + 20;
        int tooltipHeight = 45;

        int tx = mouseX + 15;
        int ty = mouseY - tooltipHeight / 2;

        // Keep on screen
        if (tx + tooltipWidth > x + width) {
            tx = mouseX - tooltipWidth - 10;
        }

        // Background
        g2d.setColor(new Color(20, 20, 30, 230));
        g2d.fillRoundRect(tx, ty, tooltipWidth, tooltipHeight, 6, 6);

        // Border
        g2d.setColor(item.getRarity().getColor());
        g2d.drawRoundRect(tx, ty, tooltipWidth, tooltipHeight, 6, 6);

        // Name
        g2d.setColor(item.getRarity().getColor());
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(name, tx + 10, ty + 18);

        // Rarity
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString(rarity + " x" + slot.stackCount, tx + 10, ty + 34);
    }

    private void drawDraggedItem(Graphics2D g2d) {
        if (draggedSlot == null || draggedSlot.icon == null) return;

        int size = SLOT_SIZE - 8;
        g2d.drawImage(draggedSlot.icon,
                      mouseX - size / 2,
                      mouseY - size / 2,
                      size, size, null);
    }

    /**
     * Checks if a point is within the UI bounds.
     */
    public boolean containsPoint(int px, int py) {
        return isOpen && px >= x && px < x + width && py >= y && py < y + height;
    }

    // Getters and setters

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public boolean isDragging() { return isDragging; }
    public Recipe getCurrentRecipe() { return currentRecipe; }

    public void setItemProducedCallback(ItemProducedCallback callback) {
        this.itemProducedCallback = callback;
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }
}
