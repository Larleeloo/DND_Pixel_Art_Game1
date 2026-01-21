package ui;

import entity.item.Item;
import entity.item.ItemEntity;
import entity.item.ItemRegistry;
import entity.item.RecipeManager;
import entity.item.RecipeManager.Recipe;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * AlchemyTableUI provides a drag-and-drop interface for crafting items.
 *
 * Features:
 * - 3 input slots for ingredients
 * - 1 output slot showing the result
 * - Real-time recipe matching as items are placed
 * - Taking output consumes ONE item from each input slot (supports stacks)
 * - Removing any input clears the output
 *
 * Usage:
 *   AlchemyTableUI ui = new AlchemyTableUI();
 *   ui.open(screenWidth, screenHeight);  // Positions to right of inventory
 *   // In update loop:
 *   ui.update(mouseX, mouseY);
 *   // In draw loop:
 *   ui.draw(g);
 *   // Draw dragged items on top of all UI:
 *   ui.drawDraggedItemOverlay(g);
 */
public class AlchemyTableUI {

    // Layout constants
    private static final int SLOT_SIZE = 56;
    private static final int SLOT_PADDING = 8;
    private static final int PANEL_PADDING = 20;

    // UI dimensions (calculated)
    private int x, y;
    private int width, height;

    // State
    private boolean isOpen = false;

    // Slots (3 input + 1 output)
    private CraftingSlot[] inputSlots = new CraftingSlot[3];
    private CraftingSlot outputSlot;

    // Current matched recipe
    private Recipe currentRecipe = null;

    // Drag and drop state
    private CraftingSlot draggedSlot = null;
    private int dragSourceIndex = -1;  // -1 = from inventory, 0-2 = from input slots
    private int dragOffsetX, dragOffsetY;
    private int mouseX, mouseY;
    private boolean isDragging = false;

    // Hover state
    private int hoveredSlotIndex = -1;  // -1 = none, 0-2 = input, 3 = output

    // Colors
    private Color panelBackground = new Color(40, 35, 50, 240);
    private Color slotBackground = new Color(60, 55, 70);
    private Color slotHover = new Color(80, 75, 95);
    private Color slotBorder = new Color(100, 95, 120);
    private Color accentColor = new Color(100, 220, 150);  // Green for alchemy
    private Color titleColor = new Color(150, 255, 180);

    // Callbacks
    private ItemConsumedCallback itemConsumedCallback;
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
     * Callback when items are consumed from input slots.
     */
    public interface ItemConsumedCallback {
        void onItemConsumed(String itemId, int count);
    }

    /**
     * Callback when items are produced and taken from output.
     */
    public interface ItemProducedCallback {
        void onItemProduced(String itemId, int count);
    }

    /**
     * Creates an alchemy table UI for crafting items.
     */
    public AlchemyTableUI() {
        // Initialize slots
        for (int i = 0; i < 3; i++) {
            inputSlots[i] = new CraftingSlot();
        }
        outputSlot = new CraftingSlot();

        calculateDimensions();
    }

    /**
     * Creates an alchemy table UI (compatibility constructor).
     * @param ignored This parameter is ignored; use ReverseCraftingUI for deconstruction
     */
    public AlchemyTableUI(boolean ignored) {
        this();
    }

    private void calculateDimensions() {
        // Layout: [Input1] [Input2] [Input3] -> [Output]
        width = 4 * SLOT_SIZE + 5 * SLOT_PADDING + 40 + PANEL_PADDING * 2;  // 40 for arrow
        height = SLOT_SIZE + PANEL_PADDING * 2 + 50;  // 50 for title
    }

    /**
     * Opens the UI positioned to the right of the inventory.
     * @param screenWidth Screen width for positioning
     * @param screenHeight Screen height for positioning
     */
    public void open(int screenWidth, int screenHeight) {
        // Position to the right of the inventory (inventory is typically on the left side)
        // Inventory is around x=50-400, so we position this starting around x=420
        this.x = 420;
        this.y = screenHeight / 2 - height / 2 - 50;  // Slightly above center

        // Position slots
        int slotY = y + PANEL_PADDING + 40;  // Below title
        int slotX = x + PANEL_PADDING;

        for (int i = 0; i < 3; i++) {
            inputSlots[i].x = slotX + i * (SLOT_SIZE + SLOT_PADDING);
            inputSlots[i].y = slotY;
        }

        // Arrow gap, then output
        outputSlot.x = slotX + 3 * (SLOT_SIZE + SLOT_PADDING) + 40;
        outputSlot.y = slotY;

        isOpen = true;
        System.out.println("AlchemyTableUI: Opened");
    }

    /**
     * Closes the UI and returns any items in input slots.
     */
    public void close() {
        if (!isOpen) return;

        // Return input items to player
        for (CraftingSlot slot : inputSlots) {
            if (!slot.isEmpty() && itemProducedCallback != null) {
                itemProducedCallback.onItemProduced(slot.itemId, slot.stackCount);
            }
            slot.clear();
        }

        // Clear output
        outputSlot.clear();
        currentRecipe = null;

        isOpen = false;
        isDragging = false;

        if (onCloseCallback != null) {
            onCloseCallback.run();
        }

        System.out.println("AlchemyTableUI: Closed");
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
     * Handles mouse press - starts dragging from a slot.
     * Returns true if handled.
     */
    public boolean handleMousePressed(int mouseX, int mouseY) {
        if (!isOpen) return false;

        int slotIndex = getSlotAtPosition(mouseX, mouseY);
        if (slotIndex == -1) return false;

        CraftingSlot slot = getSlotByIndex(slotIndex);
        if (slot != null && !slot.isEmpty()) {
            // Special handling for output slot
            if (slotIndex == 3) {
                // Taking from output - consume inputs and give output
                if (currentRecipe != null) {
                    takeOutput();
                    return true;
                }
            } else {
                // Start dragging from input slot
                draggedSlot = slot;
                dragSourceIndex = slotIndex;
                isDragging = true;
                this.mouseX = mouseX;
                this.mouseY = mouseY;
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
            dragSourceIndex = -1;
            return null;
        }

        ItemEntity droppedItem = null;
        int targetSlot = getSlotAtPosition(mouseX, mouseY);

        if (!containsPoint(mouseX, mouseY)) {
            // Dropped outside UI - return item to inventory
            droppedItem = new ItemEntity(mouseX, mouseY, draggedSlot.itemId);
            droppedItem.setStackCount(draggedSlot.stackCount);
            Item linked = ItemRegistry.create(draggedSlot.itemId);
            if (linked != null) {
                droppedItem.setLinkedItem(linked);
            }

            // Clear source slot
            inputSlots[dragSourceIndex].clear();
            updateRecipe();
        } else if (targetSlot >= 0 && targetSlot < 3 && targetSlot != dragSourceIndex) {
            // Swap with another input slot
            CraftingSlot target = inputSlots[targetSlot];
            String tempId = target.itemId;
            int tempCount = target.stackCount;

            target.setItem(draggedSlot.itemId, draggedSlot.stackCount);
            if (tempId != null && !tempId.isEmpty()) {
                inputSlots[dragSourceIndex].setItem(tempId, tempCount);
            } else {
                inputSlots[dragSourceIndex].clear();
            }
            updateRecipe();
        }
        // Else dropped on same slot or output - do nothing

        isDragging = false;
        draggedSlot = null;
        dragSourceIndex = -1;

        return droppedItem;
    }

    /**
     * Attempts to add an item to an input slot.
     * Returns true if item was accepted.
     */
    public boolean addItem(String itemId, int count) {
        if (!isOpen || itemId == null || itemId.isEmpty()) return false;

        // Find first empty input slot
        for (int i = 0; i < 3; i++) {
            if (inputSlots[i].isEmpty()) {
                inputSlots[i].setItem(itemId, count);
                updateRecipe();
                return true;
            }
        }

        return false;  // All slots full
    }

    /**
     * Attempts to add an item to a specific slot.
     */
    public boolean addItemToSlot(int slotIndex, String itemId, int count) {
        if (!isOpen || slotIndex < 0 || slotIndex >= 3) return false;

        CraftingSlot slot = inputSlots[slotIndex];

        // If slot has same item, try to stack
        if (!slot.isEmpty() && slot.itemId.equals(itemId)) {
            slot.stackCount += count;
            return true;
        }

        // If slot is empty, add item
        if (slot.isEmpty()) {
            slot.setItem(itemId, count);
            updateRecipe();
            return true;
        }

        return false;  // Slot occupied with different item
    }

    /**
     * Gets the slot at the given position.
     * Returns -1 = none, 0-2 = input slots, 3 = output slot
     */
    private int getSlotAtPosition(int mx, int my) {
        for (int i = 0; i < 3; i++) {
            CraftingSlot slot = inputSlots[i];
            if (mx >= slot.x && mx < slot.x + SLOT_SIZE &&
                my >= slot.y && my < slot.y + SLOT_SIZE) {
                return i;
            }
        }

        if (mx >= outputSlot.x && mx < outputSlot.x + SLOT_SIZE &&
            my >= outputSlot.y && my < outputSlot.y + SLOT_SIZE) {
            return 3;
        }

        return -1;
    }

    private CraftingSlot getSlotByIndex(int index) {
        if (index >= 0 && index < 3) return inputSlots[index];
        if (index == 3) return outputSlot;
        return null;
    }

    /**
     * Updates the output based on current input items (alchemy mode).
     */
    private void updateRecipe() {
        List<String> ingredients = new ArrayList<>();
        for (CraftingSlot slot : inputSlots) {
            if (!slot.isEmpty()) {
                ingredients.add(slot.itemId);
            }
        }

        if (ingredients.isEmpty()) {
            outputSlot.clear();
            currentRecipe = null;
            return;
        }

        Recipe recipe = RecipeManager.findRecipe(ingredients);
        if (recipe != null) {
            currentRecipe = recipe;
            outputSlot.setItem(recipe.result, recipe.resultCount);
            System.out.println("AlchemyTableUI: Recipe found - " + recipe.name);
        } else {
            outputSlot.clear();
            currentRecipe = null;
        }
    }

    /**
     * Takes the output item, consuming ONE item from each input slot.
     * Supports stacked items - only decrements stack count by 1.
     */
    private void takeOutput() {
        if (currentRecipe == null || outputSlot.isEmpty()) return;

        // Give result to player
        if (itemProducedCallback != null) {
            itemProducedCallback.onItemProduced(currentRecipe.result, currentRecipe.resultCount);
        }

        // Consume ONE item from each input slot (supports stacks)
        for (CraftingSlot slot : inputSlots) {
            if (!slot.isEmpty()) {
                // Notify about consumed item
                if (itemConsumedCallback != null) {
                    itemConsumedCallback.onItemConsumed(slot.itemId, 1);
                }

                // Decrement stack count by 1
                slot.stackCount--;
                if (slot.stackCount <= 0) {
                    slot.clear();
                }
            }
        }

        // Update recipe - may still have items for another craft
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

        // Draw input slots
        for (int i = 0; i < 3; i++) {
            drawSlot(g2d, inputSlots[i], i, hoveredSlotIndex == i);
        }

        // Draw arrow
        drawArrow(g2d);

        // Draw output slot
        drawSlot(g2d, outputSlot, 3, hoveredSlotIndex == 3);

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
        String title = "ALCHEMY TABLE";
        g2d.setColor(titleColor);
        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, x + (width - titleWidth) / 2, y + 22);

        // Subtitle (instructions)
        String subtitle = "Combine items to craft";
        g2d.setColor(new Color(180, 180, 180));
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        int subWidth = g2d.getFontMetrics().stringWidth(subtitle);
        g2d.drawString(subtitle, x + (width - subWidth) / 2, y + 36);
    }

    private void drawSlot(Graphics2D g2d, CraftingSlot slot, int index, boolean isHovered) {
        int sx = slot.x;
        int sy = slot.y;

        // Slot background
        g2d.setColor(isHovered ? slotHover : slotBackground);
        g2d.fillRoundRect(sx, sy, SLOT_SIZE, SLOT_SIZE, 8, 8);

        // Slot border
        Color borderColor = (index == 3 && currentRecipe != null) ? accentColor : slotBorder;
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(index == 3 ? 2 : 1));
        g2d.drawRoundRect(sx, sy, SLOT_SIZE, SLOT_SIZE, 8, 8);

        // Draw item if present
        if (!slot.isEmpty() && slot.icon != null) {
            // Don't draw if this slot is being dragged
            if (!(isDragging && index < 3 && index == dragSourceIndex)) {
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
        if (index < 3) {
            g2d.setColor(new Color(120, 120, 140));
            g2d.setFont(new Font("Arial", Font.PLAIN, 9));
            g2d.drawString(String.valueOf(index + 1), sx + 4, sy + SLOT_SIZE - 4);
        }
    }

    private void drawArrow(Graphics2D g2d) {
        int arrowX = inputSlots[2].x + SLOT_SIZE + SLOT_PADDING + 10;
        int arrowY = inputSlots[0].y + SLOT_SIZE / 2;

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

    /**
     * Returns an item from the given slot (for external removal).
     */
    public ItemEntity removeItemFromSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= 3) return null;

        CraftingSlot slot = inputSlots[slotIndex];
        if (slot.isEmpty()) return null;

        ItemEntity item = new ItemEntity(0, 0, slot.itemId);
        item.setStackCount(slot.stackCount);
        Item linked = ItemRegistry.create(slot.itemId);
        if (linked != null) {
            item.setLinkedItem(linked);
        }

        slot.clear();
        updateRecipe();

        return item;
    }

    // Getters and setters

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public boolean isDragging() { return isDragging; }
    public Recipe getCurrentRecipe() { return currentRecipe; }

    public void setItemConsumedCallback(ItemConsumedCallback callback) {
        this.itemConsumedCallback = callback;
    }

    public void setItemProducedCallback(ItemProducedCallback callback) {
        this.itemProducedCallback = callback;
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }
}
