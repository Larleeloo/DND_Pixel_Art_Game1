package ui;

import entity.Item;
import entity.ItemEntity;
import entity.ItemRegistry;
import save.SaveManager;
import save.SaveManager.SavedItem;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * VaultInventory provides a scrollable UI for managing vault storage.
 *
 * Features:
 * - Scrollable grid with up to 10,000 slots
 * - Items stack up to 16 duplicates
 * - Drag and drop between vault and player inventory
 * - Persistent storage via SaveManager
 * - Visual item display with rarity colors and stack counts
 *
 * Layout:
 * - Appears to the right of the player inventory when vault is open
 * - 6 columns x 8 visible rows = 48 visible slots
 * - Scroll bar for navigating through all slots
 */
public class VaultInventory {

    // Layout constants
    private static final int SLOT_SIZE = 48;
    private static final int SLOT_PADDING = 4;
    private static final int COLUMNS = 6;
    private static final int VISIBLE_ROWS = 8;
    private static final int VISIBLE_SLOTS = COLUMNS * VISIBLE_ROWS;
    private static final int SCROLLBAR_WIDTH = 16;

    // Position (set when shown, positioned next to player inventory)
    private int x, y;
    private int width, height;

    // State
    private boolean isOpen = false;
    private int scrollOffset = 0;  // Number of rows scrolled
    private int maxScrollRows = 0;

    // Local storage mode (for STORAGE_CHEST)
    private boolean localMode = false;
    private entity.VaultEntity linkedVault = null;

    // Items loaded from vault
    private List<VaultSlot> slots = new ArrayList<>();

    // Drag and drop state
    private VaultSlot draggedSlot = null;
    private int draggedSlotIndex = -1;
    private int dragOffsetX, dragOffsetY;
    private int mouseX, mouseY;
    private boolean isDragging = false;

    // Hover state for tooltips
    private int hoveredSlotIndex = -1;

    // Reference to player inventory for drag-drop transfers
    private InventoryDropCallback inventoryDropCallback;

    // Callback for when an item is taken from vault
    private ItemTakenCallback itemTakenCallback;

    /**
     * Represents a slot in the vault.
     */
    public static class VaultSlot {
        public String itemId;
        public int stackCount;
        public Item itemTemplate;  // Cached template for display
        public BufferedImage icon;

        public VaultSlot(String itemId, int stackCount) {
            this.itemId = itemId;
            this.stackCount = stackCount;
            this.itemTemplate = ItemRegistry.getTemplate(itemId);
            if (itemTemplate != null) {
                this.icon = itemTemplate.getIcon();
            }
        }

        public boolean isEmpty() {
            return itemId == null || itemId.isEmpty() || stackCount <= 0;
        }
    }

    /**
     * Callback interface for when items are taken from vault.
     */
    public interface ItemTakenCallback {
        void onItemTaken(String itemId, int count);
    }

    /**
     * Callback interface for when items are dropped onto inventory area.
     */
    public interface InventoryDropCallback {
        /**
         * Called when an item from vault is dropped onto inventory.
         * @param itemId The item registry ID
         * @param count Number of items
         * @param dropX Screen X position of drop
         * @param dropY Screen Y position of drop
         * @return true if the item was accepted by inventory
         */
        boolean onDropToInventory(String itemId, int count, int dropX, int dropY);

        /**
         * Checks if a point is within the inventory bounds.
         */
        boolean isPointInInventory(int x, int y);
    }

    public VaultInventory() {
        calculateDimensions();
    }

    private void calculateDimensions() {
        width = COLUMNS * (SLOT_SIZE + SLOT_PADDING) + SLOT_PADDING + SCROLLBAR_WIDTH;
        height = VISIBLE_ROWS * (SLOT_SIZE + SLOT_PADDING) + SLOT_PADDING + 40;  // Extra for title
    }

    /**
     * Opens the vault UI and loads items from SaveManager (persistent mode).
     */
    public void open(int playerInventoryX, int playerInventoryY) {
        isOpen = true;
        localMode = false;
        linkedVault = null;
        loadFromSaveManager();

        // Position to the left of player inventory
        this.x = playerInventoryX - width - 20;
        this.y = playerInventoryY;

        // Ensure on screen
        if (this.x < 10) this.x = 10;

        scrollOffset = 0;
        updateMaxScroll();

        System.out.println("VaultInventory: Opened with " + slots.size() + " items");
    }

    /**
     * Opens the vault UI in local mode (for STORAGE_CHEST).
     * Items are stored in the vault entity, not in SaveManager.
     */
    public void openLocal(int playerInventoryX, int playerInventoryY, entity.VaultEntity vault) {
        isOpen = true;
        localMode = true;
        linkedVault = vault;
        loadFromLocalVault();

        // Position to the left of player inventory
        this.x = playerInventoryX - width - 20;
        this.y = playerInventoryY;

        // Ensure on screen
        if (this.x < 10) this.x = 10;

        scrollOffset = 0;
        updateMaxScroll();

        System.out.println("VaultInventory: Opened in local mode with " + slots.size() + " items");
    }

    /**
     * Loads items from the linked local vault.
     */
    private void loadFromLocalVault() {
        slots.clear();
        if (linkedVault != null) {
            for (SavedItem saved : linkedVault.getLocalItems()) {
                slots.add(new VaultSlot(saved.itemId, saved.stackCount));
            }
        }
        updateMaxScroll();
    }

    /**
     * Closes the vault UI.
     */
    public void close() {
        isOpen = false;
        draggedSlot = null;
        System.out.println("VaultInventory: Closed");
    }

    /**
     * Loads items from SaveManager into vault slots.
     */
    public void loadFromSaveManager() {
        slots.clear();
        List<SavedItem> savedItems = SaveManager.getInstance().getVaultItems();

        for (SavedItem saved : savedItems) {
            slots.add(new VaultSlot(saved.itemId, saved.stackCount));
        }

        updateMaxScroll();
    }

    /**
     * Adds an item to the vault with proper stacking.
     *
     * @param itemId Item registry ID
     * @param count Number of items to add
     * @return Number of items that couldn't be added (overflow)
     */
    public int addItem(String itemId, int count) {
        int overflow;
        if (localMode && linkedVault != null) {
            overflow = linkedVault.addLocalItem(itemId, count);
            loadFromLocalVault();  // Refresh display
        } else {
            overflow = SaveManager.getInstance().addItemToVault(itemId, count);
            loadFromSaveManager();  // Refresh display
        }
        return overflow;
    }

    /**
     * Adds an ItemEntity to the vault.
     *
     * @param item ItemEntity to add
     * @return true if item was fully added
     */
    public boolean addItemEntity(ItemEntity item) {
        String itemId = item.getItemName();
        if (itemId == null || itemId.isEmpty()) {
            // Try to find by linked item
            if (item.getLinkedItem() != null) {
                itemId = ItemRegistry.findIdByName(item.getLinkedItem().getName());
            }
        }

        if (itemId == null || itemId.isEmpty()) {
            System.err.println("VaultInventory: Cannot add item - no valid ID");
            return false;
        }

        int overflow = addItem(itemId, item.getStackCount());
        return overflow == 0;
    }

    /**
     * Removes an item from the vault at the specified slot.
     *
     * @param slotIndex Slot index
     * @param count Number to remove (-1 for entire stack)
     * @return ItemEntity representing removed items, or null if failed
     */
    public ItemEntity removeItem(int slotIndex, int count) {
        if (slotIndex < 0 || slotIndex >= slots.size()) return null;

        VaultSlot slot = slots.get(slotIndex);
        if (slot.isEmpty()) return null;

        SavedItem removed;
        if (localMode && linkedVault != null) {
            removed = linkedVault.removeLocalItem(slotIndex, count);
            if (removed != null) {
                loadFromLocalVault();  // Refresh display
            }
        } else {
            removed = SaveManager.getInstance().removeItemFromVault(slotIndex, count);
            if (removed != null) {
                loadFromSaveManager();  // Refresh display
            }
        }

        if (removed != null) {
            // Create ItemEntity for the removed items
            ItemEntity item = new ItemEntity(0, 0, removed.itemId);
            item.setLinkedItem(ItemRegistry.create(removed.itemId));
            item.setStackCount(removed.stackCount);
            return item;
        }

        return null;
    }

    private void updateMaxScroll() {
        int totalRows = (int) Math.ceil((double) slots.size() / COLUMNS);
        maxScrollRows = Math.max(0, totalRows - VISIBLE_ROWS);
    }

    /**
     * Handles mouse scroll events.
     */
    public void handleScroll(int direction) {
        if (!isOpen) return;

        scrollOffset -= direction;  // Scroll up/down
        scrollOffset = Math.max(0, Math.min(maxScrollRows, scrollOffset));
    }

    /**
     * Handles mouse click events (for taking items on click).
     *
     * @return The slot index clicked, or -1 if none
     */
    public int handleClick(int mouseX, int mouseY, boolean isRightClick) {
        if (!isOpen) return -1;

        // Don't handle clicks while dragging
        if (isDragging) return -1;

        int slotIndex = getSlotAtPosition(mouseX, mouseY);
        if (slotIndex >= 0 && slotIndex < slots.size()) {
            VaultSlot slot = slots.get(slotIndex);
            if (!slot.isEmpty()) {
                if (isRightClick) {
                    // Right-click: take one item
                    ItemEntity taken = removeItem(slotIndex, 1);
                    if (taken != null && itemTakenCallback != null) {
                        itemTakenCallback.onItemTaken(taken.getItemName(), 1);
                    }
                } else {
                    // Left-click: take entire stack
                    ItemEntity taken = removeItem(slotIndex, -1);
                    if (taken != null && itemTakenCallback != null) {
                        itemTakenCallback.onItemTaken(taken.getItemName(), taken.getStackCount());
                    }
                }
                return slotIndex;
            }
        }

        // Check scrollbar click
        if (isScrollbarClick(mouseX, mouseY)) {
            handleScrollbarClick(mouseY);
            return -1;
        }

        return -1;
    }

    /**
     * Handles mouse pressed events (starts dragging an item).
     *
     * @return true if drag started
     */
    public boolean handleMousePressed(int mouseX, int mouseY) {
        if (!isOpen) return false;

        int slotIndex = getSlotAtPosition(mouseX, mouseY);
        if (slotIndex >= 0 && slotIndex < slots.size()) {
            VaultSlot slot = slots.get(slotIndex);
            if (!slot.isEmpty()) {
                // Start dragging this slot
                draggedSlot = slot;
                draggedSlotIndex = slotIndex;
                isDragging = true;
                this.mouseX = mouseX;
                this.mouseY = mouseY;
                return true;
            }
        }
        return false;
    }

    /**
     * Handles mouse dragged events (updates drag position).
     */
    public void handleMouseDragged(int mouseX, int mouseY) {
        if (isDragging) {
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }
    }

    /**
     * Handles mouse released events (completes drag or cancels).
     *
     * @return true if an item was transferred to inventory
     */
    public boolean handleMouseReleased(int mouseX, int mouseY) {
        if (!isDragging || draggedSlot == null) {
            isDragging = false;
            draggedSlot = null;
            draggedSlotIndex = -1;
            return false;
        }

        boolean transferred = false;

        // Check if dropped outside vault bounds
        if (!containsPoint(mouseX, mouseY)) {
            // Check if dropped on inventory
            if (inventoryDropCallback != null && inventoryDropCallback.isPointInInventory(mouseX, mouseY)) {
                // Transfer to inventory
                if (inventoryDropCallback.onDropToInventory(draggedSlot.itemId, draggedSlot.stackCount, mouseX, mouseY)) {
                    // Successfully transferred - remove from vault
                    removeFromVaultStorage(draggedSlotIndex);
                    transferred = true;
                    System.out.println("VaultInventory: Transferred " + draggedSlot.itemId + " to inventory");
                }
            } else {
                // Dropped outside both vault and inventory - use callback to add to inventory anyway
                if (itemTakenCallback != null) {
                    itemTakenCallback.onItemTaken(draggedSlot.itemId, draggedSlot.stackCount);
                    // Remove from vault
                    removeFromVaultStorage(draggedSlotIndex);
                    transferred = true;
                    System.out.println("VaultInventory: Dropped " + draggedSlot.itemId + " to inventory");
                }
            }
        }

        // Reset drag state
        isDragging = false;
        draggedSlot = null;
        draggedSlotIndex = -1;

        return transferred;
    }

    /**
     * Removes item from vault storage (handles both local and persistent modes).
     */
    private void removeFromVaultStorage(int slotIndex) {
        if (localMode && linkedVault != null) {
            linkedVault.removeLocalItem(slotIndex, -1);
            loadFromLocalVault();
        } else {
            SaveManager.getInstance().removeItemFromVault(slotIndex, -1);
            loadFromSaveManager();
        }
    }

    /**
     * Sets the callback for dropping items to inventory.
     */
    public void setInventoryDropCallback(InventoryDropCallback callback) {
        this.inventoryDropCallback = callback;
    }

    /**
     * Checks if currently dragging an item.
     */
    public boolean isDragging() {
        return isDragging;
    }

    private int getSlotAtPosition(int mouseX, int mouseY) {
        // Check if within vault grid area
        int gridX = x + SLOT_PADDING;
        int gridY = y + 40;  // Below title
        int gridWidth = COLUMNS * (SLOT_SIZE + SLOT_PADDING);
        int gridHeight = VISIBLE_ROWS * (SLOT_SIZE + SLOT_PADDING);

        if (mouseX < gridX || mouseX >= gridX + gridWidth) return -1;
        if (mouseY < gridY || mouseY >= gridY + gridHeight) return -1;

        int col = (mouseX - gridX) / (SLOT_SIZE + SLOT_PADDING);
        int row = (mouseY - gridY) / (SLOT_SIZE + SLOT_PADDING);

        if (col < 0 || col >= COLUMNS) return -1;
        if (row < 0 || row >= VISIBLE_ROWS) return -1;

        return (scrollOffset + row) * COLUMNS + col;
    }

    private boolean isScrollbarClick(int mouseX, int mouseY) {
        int scrollbarX = x + width - SCROLLBAR_WIDTH;
        return mouseX >= scrollbarX && mouseX < x + width &&
               mouseY >= y + 40 && mouseY < y + height;
    }

    private void handleScrollbarClick(int mouseY) {
        int scrollbarY = y + 40;
        int scrollbarHeight = height - 40;

        float clickPercent = (float) (mouseY - scrollbarY) / scrollbarHeight;
        scrollOffset = (int) (clickPercent * maxScrollRows);
        scrollOffset = Math.max(0, Math.min(maxScrollRows, scrollOffset));
    }

    /**
     * Updates the mouse position for hover effects.
     */
    public void updateMousePosition(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        hoveredSlotIndex = getSlotAtPosition(mouseX, mouseY);
    }

    /**
     * Draws the vault inventory UI.
     */
    public void draw(Graphics g) {
        if (!isOpen) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background panel
        drawBackground(g2d);

        // Draw title
        drawTitle(g2d);

        // Draw item slots
        drawSlots(g2d);

        // Draw scrollbar
        drawScrollbar(g2d);

        // Draw tooltip for hovered item
        if (hoveredSlotIndex >= 0 && hoveredSlotIndex < slots.size()) {
            drawTooltip(g2d, slots.get(hoveredSlotIndex));
        }

        // Draw dragged item
        if (draggedSlot != null) {
            drawDraggedItem(g2d);
        }
    }

    private void drawBackground(Graphics2D g2d) {
        // Semi-transparent background
        g2d.setColor(new Color(30, 30, 50, 230));
        g2d.fillRoundRect(x, y, width, height, 12, 12);

        // Border
        g2d.setColor(new Color(255, 215, 0));  // Gold border
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, width, height, 12, 12);
    }

    private void drawTitle(Graphics2D g2d) {
        // Title background
        g2d.setColor(new Color(60, 50, 30));
        g2d.fillRect(x + 2, y + 2, width - 4, 36);

        // Title text
        g2d.setColor(new Color(255, 215, 0));
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String title = localMode ? "STORAGE CHEST" : "VAULT";
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, x + (width - titleWidth) / 2, y + 24);

        // Slot count
        g2d.setColor(new Color(180, 180, 180));
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        int maxSlots = localMode ? (linkedVault != null ? linkedVault.getMaxLocalSlots() : 48) : SaveManager.VAULT_MAX_SLOTS;
        String count = slots.size() + " / " + maxSlots;
        g2d.drawString(count, x + 8, y + 24);
    }

    private void drawSlots(Graphics2D g2d) {
        int gridY = y + 40;

        int startIndex = scrollOffset * COLUMNS;
        int endIndex = Math.min(startIndex + VISIBLE_SLOTS, slots.size() + VISIBLE_SLOTS);

        for (int i = 0; i < VISIBLE_SLOTS; i++) {
            int slotIndex = startIndex + i;
            int col = i % COLUMNS;
            int row = i / COLUMNS;

            int slotX = x + SLOT_PADDING + col * (SLOT_SIZE + SLOT_PADDING);
            int slotY = gridY + row * (SLOT_SIZE + SLOT_PADDING);

            // Draw slot background
            boolean isHovered = (slotIndex == hoveredSlotIndex);
            if (isHovered) {
                g2d.setColor(new Color(80, 80, 100));
            } else {
                g2d.setColor(new Color(50, 50, 70));
            }
            g2d.fillRoundRect(slotX, slotY, SLOT_SIZE, SLOT_SIZE, 6, 6);

            // Draw slot border
            g2d.setColor(new Color(100, 100, 120));
            g2d.drawRoundRect(slotX, slotY, SLOT_SIZE, SLOT_SIZE, 6, 6);

            // Draw item if slot has one
            if (slotIndex < slots.size()) {
                VaultSlot slot = slots.get(slotIndex);
                if (!slot.isEmpty()) {
                    drawSlotItem(g2d, slot, slotX, slotY);
                }
            }
        }
    }

    private void drawSlotItem(Graphics2D g2d, VaultSlot slot, int slotX, int slotY) {
        // Draw rarity background glow
        if (slot.itemTemplate != null) {
            Color rarityColor = slot.itemTemplate.getRarity().getColor();
            g2d.setColor(new Color(rarityColor.getRed(), rarityColor.getGreen(),
                                   rarityColor.getBlue(), 60));
            g2d.fillRoundRect(slotX + 2, slotY + 2, SLOT_SIZE - 4, SLOT_SIZE - 4, 4, 4);
        }

        // Draw item icon
        if (slot.icon != null) {
            int iconSize = SLOT_SIZE - 8;
            g2d.drawImage(slot.icon, slotX + 4, slotY + 4, iconSize, iconSize, null);
        } else {
            // Placeholder for missing icon
            g2d.setColor(Color.GRAY);
            g2d.fillRect(slotX + 8, slotY + 8, SLOT_SIZE - 16, SLOT_SIZE - 16);
        }

        // Draw stack count
        if (slot.stackCount > 1) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            String countStr = String.valueOf(slot.stackCount);
            int countWidth = g2d.getFontMetrics().stringWidth(countStr);
            g2d.drawString(countStr, slotX + SLOT_SIZE - countWidth - 4, slotY + SLOT_SIZE - 4);
        }
    }

    private void drawScrollbar(Graphics2D g2d) {
        if (maxScrollRows <= 0) return;

        int scrollbarX = x + width - SCROLLBAR_WIDTH + 2;
        int scrollbarY = y + 42;
        int scrollbarHeight = height - 46;

        // Background
        g2d.setColor(new Color(40, 40, 60));
        g2d.fillRoundRect(scrollbarX, scrollbarY, SCROLLBAR_WIDTH - 4, scrollbarHeight, 4, 4);

        // Handle
        float handleHeight = Math.max(20, scrollbarHeight * ((float) VISIBLE_ROWS / (maxScrollRows + VISIBLE_ROWS)));
        float handleY = scrollbarY + (scrollbarHeight - handleHeight) * ((float) scrollOffset / maxScrollRows);

        g2d.setColor(new Color(255, 215, 0, 180));
        g2d.fillRoundRect(scrollbarX + 2, (int) handleY, SCROLLBAR_WIDTH - 8, (int) handleHeight, 4, 4);
    }

    private void drawTooltip(Graphics2D g2d, VaultSlot slot) {
        if (slot.isEmpty() || slot.itemTemplate == null) return;

        Item item = slot.itemTemplate;
        String name = item.getName();
        String rarity = item.getRarity().getDisplayName();
        String category = item.getCategory().name();

        // Calculate tooltip size
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        int nameWidth = g2d.getFontMetrics().stringWidth(name);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        int infoWidth = g2d.getFontMetrics().stringWidth(rarity + " " + category);

        int tooltipWidth = Math.max(nameWidth, infoWidth) + 20;
        int tooltipHeight = 50;

        int tooltipX = mouseX + 15;
        int tooltipY = mouseY - tooltipHeight / 2;

        // Keep tooltip on screen
        if (tooltipX + tooltipWidth > x + width) {
            tooltipX = mouseX - tooltipWidth - 10;
        }

        // Background
        g2d.setColor(new Color(20, 20, 30, 240));
        g2d.fillRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 8, 8);

        // Border with rarity color
        g2d.setColor(item.getRarity().getColor());
        g2d.drawRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 8, 8);

        // Name
        g2d.setColor(item.getRarity().getColor());
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(name, tooltipX + 10, tooltipY + 18);

        // Info
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString(rarity + " " + category, tooltipX + 10, tooltipY + 32);

        // Stack info
        g2d.drawString("x" + slot.stackCount, tooltipX + 10, tooltipY + 44);
    }

    private void drawDraggedItem(Graphics2D g2d) {
        if (draggedSlot == null || draggedSlot.icon == null) return;

        int size = SLOT_SIZE;
        g2d.drawImage(draggedSlot.icon,
                      mouseX - size / 2 + dragOffsetX,
                      mouseY - size / 2 + dragOffsetY,
                      size, size, null);
    }

    // Getters and setters

    public boolean isOpen() { return isOpen; }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public int getSlotCount() { return slots.size(); }

    public boolean isLocalMode() { return localMode; }

    public entity.VaultEntity getLinkedVault() { return linkedVault; }

    public void setItemTakenCallback(ItemTakenCallback callback) {
        this.itemTakenCallback = callback;
    }

    /**
     * Checks if a point is within the vault UI bounds.
     */
    public boolean containsPoint(int px, int py) {
        return isOpen && px >= x && px < x + width && py >= y && py < y + height;
    }
}
