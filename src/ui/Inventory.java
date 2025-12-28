package ui;
import entity.*;
import block.*;
import audio.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Manages collected items and displays inventory UI
 */
public class Inventory {

    private ArrayList<ItemEntity> items;
    private int maxSlots;
    private boolean isOpen;

    // UI positioning
    private int uiX, uiY;
    private int slotSize;
    private int padding;

    // Drag and drop
    private ItemEntity draggedItem;
    private int draggedIndex;
    private int dragX, dragY;
    private boolean isDragging;

    // Held item (hotbar selection)
    private int selectedSlot = 0; // Which slot is currently "held" (0-indexed)
    private static final int HOTBAR_SIZE = 5; // Number of hotbar slots shown

    public Inventory(int maxSlots) {
        this.items = new ArrayList<>();
        this.maxSlots = maxSlots;
        this.isOpen = false;

        // UI settings
        this.slotSize = 60;
        this.padding = 10;
        this.uiX = 50;
        this.uiY = 80;

        // Drag state
        this.draggedItem = null;
        this.draggedIndex = -1;
        this.isDragging = false;
    }

    public boolean addItem(ItemEntity item) {
        if (items.size() < maxSlots) {
            items.add(item);
            return true;
        }
        return false;
    }

    public void toggleOpen() {
        isOpen = !isOpen;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public int getItemCount() {
        return items.size();
    }

    public void handleMousePressed(int mouseX, int mouseY) {
        if (!isOpen) return;

        int cols = 5;
        int panelX = (1920 - (cols * (slotSize + padding) + padding)) / 2;
        int panelY = 150;

        // Check if clicking on an item slot
        for (int i = 0; i < items.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int slotX = panelX + padding + col * (slotSize + padding);
            int slotY = panelY + 50 + row * (slotSize + padding);

            if (mouseX >= slotX && mouseX <= slotX + slotSize &&
                    mouseY >= slotY && mouseY <= slotY + slotSize) {
                // Start dragging this item
                draggedItem = items.get(i);
                draggedIndex = i;
                isDragging = true;
                dragX = mouseX;
                dragY = mouseY;
                break;
            }
        }
    }

    public void handleMouseDragged(int mouseX, int mouseY) {
        if (isDragging) {
            dragX = mouseX;
            dragY = mouseY;
        }
    }

    public ItemEntity handleMouseReleased(int mouseX, int mouseY) {
        if (!isDragging) return null;

        ItemEntity droppedItem = null;

        // Check if dropped outside inventory panel
        int cols = 5;
        int rows = (int)Math.ceil(maxSlots / (double)cols);
        int panelWidth = cols * (slotSize + padding) + padding;
        int panelHeight = rows * (slotSize + padding) + padding + 50;
        int panelX = (1920 - panelWidth) / 2;
        int panelY = 150;

        boolean outsideInventory = mouseX < panelX || mouseX > panelX + panelWidth ||
                mouseY < panelY || mouseY > panelY + panelHeight;

        if (outsideInventory && draggedItem != null) {
            // Drop the item into the world
            droppedItem = draggedItem;
            items.remove(draggedIndex);
        }

        // Reset drag state
        draggedItem = null;
        draggedIndex = -1;
        isDragging = false;

        return droppedItem;
    }

    public boolean removeItem(ItemEntity item) {
        return items.remove(item);
    }

    /**
     * Finds and consumes ammo of the specified type.
     * @param ammoName The name/type of ammo to consume (e.g., "arrow", "bolt")
     * @return The consumed ItemEntity (for damage calculations), or null if no ammo found
     */
    public ItemEntity consumeAmmo(String ammoName) {
        if (ammoName == null || ammoName.isEmpty()) return null;

        String lowerAmmoName = ammoName.toLowerCase();

        // Search through inventory for matching ammo
        for (int i = 0; i < items.size(); i++) {
            ItemEntity item = items.get(i);
            String itemName = item.getItemName().toLowerCase();
            String itemType = item.getItemType().toLowerCase();
            String itemId = item.getItemId();

            // Check if item matches ammo type
            boolean matches = false;

            // Match by item ID (most reliable)
            if (itemId != null && itemId.toLowerCase().contains(lowerAmmoName)) {
                matches = true;
            }
            // Match by name containing the ammo type
            else if (itemName.contains(lowerAmmoName)) {
                matches = true;
            }
            // Match ammo type items
            else if (itemType.equals("ammo") && itemName.contains(lowerAmmoName)) {
                matches = true;
            }

            if (matches) {
                // Remove and return the ammo item
                items.remove(i);
                return item;
            }
        }

        return null;  // No matching ammo found
    }

    /**
     * Checks if the inventory has ammo of the specified type.
     * @param ammoName The name/type of ammo to check for
     * @return true if ammo is available
     */
    public boolean hasAmmo(String ammoName) {
        if (ammoName == null || ammoName.isEmpty()) return false;

        String lowerAmmoName = ammoName.toLowerCase();

        for (ItemEntity item : items) {
            String itemName = item.getItemName().toLowerCase();
            String itemId = item.getItemId();

            if (itemId != null && itemId.toLowerCase().contains(lowerAmmoName)) {
                return true;
            }
            if (itemName.contains(lowerAmmoName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Counts how many of a specific ammo type are in the inventory.
     * @param ammoName The name/type of ammo to count
     * @return The number of matching ammo items
     */
    public int countAmmo(String ammoName) {
        if (ammoName == null || ammoName.isEmpty()) return 0;

        String lowerAmmoName = ammoName.toLowerCase();
        int count = 0;

        for (ItemEntity item : items) {
            String itemName = item.getItemName().toLowerCase();
            String itemId = item.getItemId();

            if (itemId != null && itemId.toLowerCase().contains(lowerAmmoName)) {
                count++;
            } else if (itemName.contains(lowerAmmoName)) {
                count++;
            }
        }

        return count;
    }

    /**
     * Removes an item at a specific slot index (for throwable consumption).
     * @param slotIndex The slot index to remove from
     * @return The removed item, or null if slot was empty
     */
    public ItemEntity removeItemAtSlot(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < items.size()) {
            return items.remove(slotIndex);
        }
        return null;
    }

    /**
     * Gets the item at a specific slot index.
     * @param slotIndex The slot index
     * @return The item at that slot, or null if empty
     */
    public ItemEntity getItemAtSlot(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < items.size()) {
            return items.get(slotIndex);
        }
        return null;
    }

    /**
     * Gets the currently selected hotbar slot index.
     */
    public int getSelectedSlot() {
        return selectedSlot;
    }

    /**
     * Sets the selected hotbar slot (0-indexed).
     * @param slot The slot index (0 to HOTBAR_SIZE-1)
     */
    public void setSelectedSlot(int slot) {
        this.selectedSlot = Math.max(0, Math.min(HOTBAR_SIZE - 1, slot));
    }

    /**
     * Cycles to the next hotbar slot.
     * @param direction 1 for next, -1 for previous
     */
    public void cycleSelectedSlot(int direction) {
        selectedSlot = (selectedSlot + direction + HOTBAR_SIZE) % HOTBAR_SIZE;
    }

    /**
     * Gets the item in the currently selected slot.
     * @return The held item, or null if slot is empty
     */
    public ItemEntity getHeldItem() {
        if (selectedSlot < items.size()) {
            return items.get(selectedSlot);
        }
        return null;
    }

    /**
     * Gets the tool type of the currently held item.
     * @return The tool type, or HAND if no tool is held
     */
    public ToolType getHeldToolType() {
        ItemEntity held = getHeldItem();
        if (held != null) {
            return ToolType.fromItemType(held.getItemType());
        }
        return ToolType.HAND;
    }

    /**
     * Handles number key input to select hotbar slots.
     * @param key The key character ('1' through '5')
     * @return true if the key was handled
     */
    public boolean handleHotbarKey(char key) {
        if (key >= '1' && key <= '5') {
            setSelectedSlot(key - '1');
            return true;
        }
        return false;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isOpen) {
            // Draw full inventory panel
            drawFullInventory(g2d);

            // Draw dragged item on top if dragging
            if (isDragging && draggedItem != null) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                if (draggedItem.getSprite() != null) {
                    g2d.drawImage(draggedItem.getSprite(),
                            dragX - slotSize/2, dragY - slotSize/2,
                            slotSize, slotSize, null);
                }
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        } else {
            // Draw compact inventory preview (always visible)
            drawCompactInventory(g2d);
        }
    }

    private void drawCompactInventory(Graphics2D g2d) {
        // Draw hotbar at bottom center of screen
        int hotbarSlotSize = 50;
        int hotbarPadding = 5;
        int hotbarWidth = HOTBAR_SIZE * (hotbarSlotSize + hotbarPadding) + hotbarPadding;
        int hotbarHeight = hotbarSlotSize + hotbarPadding * 2;
        int hotbarX = (1920 - hotbarWidth) / 2;
        int hotbarY = 1080 - hotbarHeight - 20;

        // Background
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(hotbarX, hotbarY, hotbarWidth, hotbarHeight, 10, 10);

        // Border
        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(hotbarX, hotbarY, hotbarWidth, hotbarHeight, 10, 10);

        // Draw hotbar slots
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            int slotX = hotbarX + hotbarPadding + i * (hotbarSlotSize + hotbarPadding);
            int slotY = hotbarY + hotbarPadding;

            // Slot background - highlight selected slot
            if (i == selectedSlot) {
                g2d.setColor(new Color(255, 255, 255, 100));
            } else {
                g2d.setColor(new Color(60, 60, 60, 200));
            }
            g2d.fillRoundRect(slotX, slotY, hotbarSlotSize, hotbarSlotSize, 6, 6);

            // Slot border - brighter for selected
            if (i == selectedSlot) {
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(3));
            } else {
                g2d.setColor(new Color(120, 120, 120));
                g2d.setStroke(new BasicStroke(2));
            }
            g2d.drawRoundRect(slotX, slotY, hotbarSlotSize, hotbarSlotSize, 6, 6);

            // Draw item if present
            if (i < items.size()) {
                ItemEntity item = items.get(i);
                if (item.getSprite() != null) {
                    g2d.drawImage(item.getSprite(), slotX + 5, slotY + 5,
                            hotbarSlotSize - 10, hotbarSlotSize - 10, null);
                }
            }

            // Draw slot number
            g2d.setColor(new Color(200, 200, 200));
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString(String.valueOf(i + 1), slotX + 3, slotY + 12);
        }

        // Draw held item name and tool info above hotbar
        ItemEntity held = getHeldItem();
        if (held != null) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            String heldName = held.getItemName();
            FontMetrics fm = g2d.getFontMetrics();
            int textX = hotbarX + (hotbarWidth - fm.stringWidth(heldName)) / 2;
            g2d.drawString(heldName, textX, hotbarY - 8);
        }

        // Draw inventory hint in corner
        g2d.setColor(new Color(200, 200, 200, 150));
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("[I] Inventory | [1-5] Select", 10, 1080 - 10);
    }

    private void drawFullInventory(Graphics2D g2d) {
        int cols = 5;
        int rows = (int)Math.ceil(maxSlots / (double)cols);
        int panelWidth = cols * (slotSize + padding) + padding;
        int panelHeight = rows * (slotSize + padding) + padding + 50;

        // Center the panel
        int panelX = (1920 - panelWidth) / 2;
        int panelY = 150;

        // Background panel
        g2d.setColor(new Color(40, 40, 40, 230));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);

        // Border
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);

        // Title
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("INVENTORY", panelX + 20, panelY + 35);

        // Draw slots
        for (int i = 0; i < maxSlots; i++) {
            int col = i % cols;
            int row = i / cols;
            int slotX = panelX + padding + col * (slotSize + padding);
            int slotY = panelY + 50 + row * (slotSize + padding);

            // Slot background
            if (i < items.size()) {
                g2d.setColor(new Color(80, 80, 120, 200));
            } else {
                g2d.setColor(new Color(60, 60, 60, 200));
            }
            g2d.fillRoundRect(slotX, slotY, slotSize, slotSize, 8, 8);

            // Slot border
            g2d.setColor(new Color(150, 150, 150));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(slotX, slotY, slotSize, slotSize, 8, 8);

            // Draw item if present
            if (i < items.size()) {
                ItemEntity item = items.get(i);

                // Skip drawing if this is the item being dragged
                if (isDragging && draggedIndex == i) {
                    // Draw empty slot with dashed border
                    g2d.setColor(new Color(100, 100, 100, 100));
                    g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
                    g2d.drawRoundRect(slotX, slotY, slotSize, slotSize, 8, 8);
                } else {
                    // Draw normal item
                    if (item.getSprite() != null) {
                        g2d.drawImage(item.getSprite(), slotX + 5, slotY + 5, slotSize - 10, slotSize - 10, null);
                    }

                    // Item name
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                    FontMetrics fm = g2d.getFontMetrics();
                    String name = item.getItemName();
                    if (fm.stringWidth(name) > slotSize) {
                        name = name.substring(0, 6) + "...";
                    }
                    int textX = slotX + (slotSize - fm.stringWidth(name)) / 2;
                    g2d.drawString(name, textX, slotY + slotSize + 15);
                }
            }
        }

        // Instructions
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.ITALIC, 14));
        g2d.drawString("Press [I] to close | Drag items outside to drop", panelX + 20, panelY + panelHeight - 15);
    }
}