package ui;
import entity.item.Item;
import entity.item.ItemEntity;
import entity.item.ItemRegistry;
import entity.item.VaultEntity;
import save.SaveManager;
import save.SaveManager.SavedItem;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages collected items and displays inventory UI.
 *
 * Features:
 * - 32 slot maximum inventory capacity (fixed slots)
 * - 5-slot hotbar for quick access
 * - Scroll wheel support for inventory navigation
 * - Left-click to auto-equip items to hotbar
 * - Drag and drop for item management
 * - Stack display for stackable items
 * - Vault integration for persistent storage
 * - Minecraft-style cursor navigation with D-pad/arrow keys
 */
public class Inventory {

    // Fixed-size slot array for Minecraft-style inventory where items stay in specific slots
    private ItemEntity[] slots;
    private static final int MAX_SLOTS = 32;  // Fixed 32 slot limit
    private boolean isOpen;

    // Vault integration
    private VaultInventory vaultInventory;
    private boolean vaultOpen = false;

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

    // Scroll support for full inventory
    private int scrollOffset = 0;  // Number of rows scrolled
    private static final int VISIBLE_ROWS = 4;  // Rows visible at once in full inventory
    private static final int COLS = 8;  // Columns in full inventory

    // Hover tracking for E key equip
    private int hoveredSlotIndex = -1;
    private int lastMouseX = 0;
    private int lastMouseY = 0;

    // Minecraft-style cursor navigation
    private int cursorSlot = 0;  // Currently highlighted slot in the grid (0 to MAX_SLOTS-1)
    private ItemEntity cursorHeldItem = null;  // Item currently "held" by cursor (picked up with Enter/A)
    private int cursorHeldItemOriginalSlot = -1;  // Original slot index of the held item
    private boolean navigationMode = false;  // True when using keyboard/controller navigation

    public Inventory() {
        this.slots = new ItemEntity[MAX_SLOTS];  // Fixed-size array, null = empty slot
        this.isOpen = false;

        // UI settings
        this.slotSize = 60;
        this.padding = 8;
        this.uiX = 50;
        this.uiY = 80;

        // Drag state
        this.draggedItem = null;
        this.draggedIndex = -1;
        this.isDragging = false;
    }

    // Legacy constructor for compatibility
    public Inventory(int maxSlots) {
        this();  // Use fixed 32 slots regardless of passed value
    }

    /**
     * Gets the maximum number of slots in the inventory.
     */
    public int getMaxSlots() {
        return MAX_SLOTS;
    }

    public boolean addItem(ItemEntity item) {
        // Try to stack with existing items first
        if (item.isStackable()) {
            for (int i = 0; i < MAX_SLOTS; i++) {
                if (slots[i] != null && slots[i].canStackWith(item)) {
                    int remaining = slots[i].addToStack(item);
                    if (remaining == 0) {
                        return true; // Fully stacked
                    }
                    // Continue looking for more stacks if there's overflow
                }
            }
        }

        // If item still has count remaining, find first empty slot
        if (item.getStackCount() > 0) {
            for (int i = 0; i < MAX_SLOTS; i++) {
                if (slots[i] == null) {
                    slots[i] = item;
                    return true;
                }
            }
        }

        // Check if fully consumed during stacking
        if (item.getStackCount() == 0) {
            return true;
        }

        return false; // No room
    }

    /**
     * Adds an item to the inventory, preferring the cursor slot when in navigation mode.
     * If in navigation mode and cursor slot is empty, places item there.
     * Otherwise falls back to normal addItem behavior (stacking or first empty slot).
     *
     * @param item The item to add
     * @return true if successfully added, false if inventory is full
     */
    public boolean addItemAtCursorSlot(ItemEntity item) {
        // Try to stack with existing items first (regardless of mode)
        if (item.isStackable()) {
            for (int i = 0; i < MAX_SLOTS; i++) {
                if (slots[i] != null && slots[i].canStackWith(item)) {
                    int remaining = slots[i].addToStack(item);
                    if (remaining == 0) {
                        return true; // Fully stacked
                    }
                    // Continue looking for more stacks if there's overflow
                }
            }
        }

        // If item still has count remaining, try cursor slot first (when in navigation mode)
        if (item.getStackCount() > 0) {
            // When in navigation mode and inventory is open, prefer cursor slot
            if (navigationMode && isOpen && cursorSlot >= 0 && cursorSlot < MAX_SLOTS && slots[cursorSlot] == null) {
                slots[cursorSlot] = item;
                System.out.println("Inventory: Added " + item.getItemName() + " to cursor slot " + cursorSlot);
                return true;
            }

            // Otherwise find first empty slot
            for (int i = 0; i < MAX_SLOTS; i++) {
                if (slots[i] == null) {
                    slots[i] = item;
                    return true;
                }
            }
        }

        // Check if fully consumed during stacking
        if (item.getStackCount() == 0) {
            return true;
        }

        return false; // No room
    }

    /**
     * Adds an item to a specific slot index.
     * If the slot is occupied, attempts to stack. If stacking fails, returns false.
     *
     * @param item The item to add
     * @param slotIndex The target slot index
     * @return true if successfully added, false if slot is occupied and can't stack
     */
    public boolean addItemToSlot(ItemEntity item, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= MAX_SLOTS) {
            return addItem(item); // Invalid slot, fall back to normal add
        }

        // If slot is empty, place directly
        if (slots[slotIndex] == null) {
            slots[slotIndex] = item;
            return true;
        }

        // If slot has item, try to stack
        if (item.isStackable() && slots[slotIndex].canStackWith(item)) {
            int remaining = slots[slotIndex].addToStack(item);
            if (remaining == 0) {
                return true; // Fully stacked
            }
            // Partial stack, try to add remainder elsewhere
            return addItem(item);
        }

        // Slot occupied and can't stack, fall back to normal add
        return addItem(item);
    }

    public void toggleOpen() {
        isOpen = !isOpen;
        if (isOpen) {
            scrollOffset = 0;  // Reset scroll when opening
            // Reset cursor to first slot when opening
            cursorSlot = 0;
            ensureCursorVisible();
        } else {
            // If closing with held item, drop it back to original slot
            if (cursorHeldItem != null) {
                cancelCursorHeldItem();
            }
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    public int getItemCount() {
        int count = 0;
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (slots[i] != null) count++;
        }
        return count;
    }

    /**
     * Handles scroll wheel input.
     * When vault is open: scrolls through vault inventory
     * When inventory is open: scrolls through inventory rows
     * When both are closed: cycles hotbar selection
     * @param scrollDirection Positive for scroll up, negative for scroll down
     */
    public void handleScroll(int scrollDirection) {
        if (vaultOpen && vaultInventory != null) {
            // Forward scroll to vault inventory when vault is open
            vaultInventory.handleScroll(scrollDirection);
        } else if (isOpen) {
            // Scroll through inventory (fixed 32 slots = 4 rows)
            int totalRows = (int) Math.ceil(MAX_SLOTS / (double) COLS);
            int maxScroll = Math.max(0, totalRows - VISIBLE_ROWS);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - scrollDirection));
        } else {
            // Cycle hotbar selection
            cycleSelectedSlot(-scrollDirection);
        }
    }

    /**
     * Handles left-click for selection (no longer auto-equips, use E key instead).
     * @return true if click was handled
     */
    public boolean handleLeftClick(int mouseX, int mouseY) {
        if (!isOpen) {
            // Check hotbar clicks when closed
            return handleHotbarClick(mouseX, mouseY);
        }

        // Full inventory is open - clicking selects the slot (for drag start)
        // Auto-equip is now done with E key
        int panelWidth = COLS * (slotSize + padding) + padding;
        int panelX = (1920 - panelWidth) / 2;
        int panelY = 150;

        // Check if clicking on any slot (empty or with item)
        int startIndex = scrollOffset * COLS;
        int endIndex = Math.min(startIndex + VISIBLE_ROWS * COLS, MAX_SLOTS);

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;
            int col = displayIndex % COLS;
            int row = displayIndex / COLS;
            int slotX = panelX + padding + col * (slotSize + padding);
            int slotY = panelY + 60 + row * (slotSize + padding);

            if (mouseX >= slotX && mouseX <= slotX + slotSize &&
                    mouseY >= slotY && mouseY <= slotY + slotSize) {
                // Click registers the slot but doesn't auto-equip
                // Drag handling is separate in handleMousePressed
                return true;
            }
        }

        return false;
    }

    /**
     * Handles F key press for equipping items from inventory to hotbar.
     * Equips the currently hovered item.
     * @return true if an item was equipped
     */
    public boolean handleEquipKey() {
        System.out.println("Inventory.handleEquipKey: isOpen=" + isOpen + ", hoveredSlotIndex=" + hoveredSlotIndex);
        if (!isOpen || hoveredSlotIndex < 0 || hoveredSlotIndex >= MAX_SLOTS || slots[hoveredSlotIndex] == null) {
            return false;
        }

        // Equip the hovered item to the selected hotbar slot
        autoEquipItem(hoveredSlotIndex);
        System.out.println("Inventory.handleEquipKey: Equipped item at slot " + hoveredSlotIndex);
        return true;
    }

    /**
     * Updates the mouse position for hover tracking.
     * Call this during mouse move events.
     */
    public void updateMousePosition(int mouseX, int mouseY) {
        // Switch off navigation mode when mouse moves significantly
        if (isOpen && (Math.abs(mouseX - lastMouseX) > 3 || Math.abs(mouseY - lastMouseY) > 3)) {
            navigationMode = false;
        }

        lastMouseX = mouseX;
        lastMouseY = mouseY;

        if (!isOpen) {
            hoveredSlotIndex = -1;
            return;
        }

        // Calculate which slot is being hovered
        int panelWidth = COLS * (slotSize + padding) + padding;
        int panelX = (1920 - panelWidth) / 2;
        int panelY = 150;

        hoveredSlotIndex = -1;
        int startIndex = scrollOffset * COLS;
        int endIndex = Math.min(startIndex + VISIBLE_ROWS * COLS, MAX_SLOTS);

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;
            int col = displayIndex % COLS;
            int row = displayIndex / COLS;
            int slotX = panelX + padding + col * (slotSize + padding);
            int slotY = panelY + 60 + row * (slotSize + padding);

            if (mouseX >= slotX && mouseX <= slotX + slotSize &&
                    mouseY >= slotY && mouseY <= slotY + slotSize) {
                hoveredSlotIndex = i;
                break;
            }
        }
    }

    /**
     * Gets the currently hovered slot index.
     * @return The slot index being hovered, or -1 if none
     */
    public int getHoveredSlotIndex() {
        return hoveredSlotIndex;
    }

    // ==================== Minecraft-Style Navigation ====================

    /**
     * Handles keyboard/controller navigation input.
     * Call this each frame when inventory is open.
     * @param input The InputManager for checking navigation keys
     * @return true if navigation input was handled
     */
    public boolean handleNavigationInput(input.InputManager input) {
        if (!isOpen) return false;

        boolean handled = false;

        // Check navigation directions
        if (input.isNavigateUpJustPressed()) {
            moveCursor(0, -1);
            navigationMode = true;
            handled = true;
        }
        if (input.isNavigateDownJustPressed()) {
            moveCursor(0, 1);
            navigationMode = true;
            handled = true;
        }
        if (input.isNavigateLeftJustPressed()) {
            moveCursor(-1, 0);
            navigationMode = true;
            handled = true;
        }
        if (input.isNavigateRightJustPressed()) {
            moveCursor(1, 0);
            navigationMode = true;
            handled = true;
        }

        // Check select button (Enter/A)
        if (input.isSelectJustPressed()) {
            handleCursorSelect();
            handled = true;
        }

        return handled;
    }

    /**
     * Moves the cursor in the specified direction.
     * @param dx Horizontal movement (-1 left, +1 right)
     * @param dy Vertical movement (-1 up, +1 down)
     */
    private void moveCursor(int dx, int dy) {
        int col = cursorSlot % COLS;
        int row = cursorSlot / COLS;

        // Calculate new position with wrapping
        col = (col + dx + COLS) % COLS;
        row = row + dy;

        // Clamp rows to valid range
        int totalRows = (int) Math.ceil(MAX_SLOTS / (double) COLS);
        if (row < 0) row = 0;
        if (row >= totalRows) row = totalRows - 1;

        cursorSlot = row * COLS + col;

        // Clamp to max slots
        if (cursorSlot >= MAX_SLOTS) {
            cursorSlot = MAX_SLOTS - 1;
        }

        // Ensure cursor is visible (auto-scroll)
        ensureCursorVisible();
    }

    /**
     * Ensures the cursor slot is visible by adjusting scroll offset.
     */
    private void ensureCursorVisible() {
        int cursorRow = cursorSlot / COLS;
        int firstVisibleRow = scrollOffset;
        int lastVisibleRow = scrollOffset + VISIBLE_ROWS - 1;

        if (cursorRow < firstVisibleRow) {
            scrollOffset = cursorRow;
        } else if (cursorRow > lastVisibleRow) {
            scrollOffset = cursorRow - VISIBLE_ROWS + 1;
        }

        // Clamp scroll offset
        int totalRows = (int) Math.ceil(MAX_SLOTS / (double) COLS);
        int maxScroll = Math.max(0, totalRows - VISIBLE_ROWS);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
    }

    /**
     * Handles cursor select action (Enter/A button).
     * If no item is held, picks up item at cursor position.
     * If item is held, places/swaps item at cursor position.
     */
    private void handleCursorSelect() {
        if (cursorHeldItem == null) {
            // Pick up item at cursor position
            if (cursorSlot >= 0 && cursorSlot < MAX_SLOTS && slots[cursorSlot] != null) {
                cursorHeldItem = slots[cursorSlot];
                cursorHeldItemOriginalSlot = cursorSlot;
                slots[cursorSlot] = null;  // Remove from original slot
                System.out.println("Inventory: Picked up " + cursorHeldItem.getItemName() + " from slot " + cursorSlot);
            }
        } else {
            // Place/swap item at cursor position
            if (cursorSlot >= 0 && cursorSlot < MAX_SLOTS) {
                if (slots[cursorSlot] != null) {
                    // Swap with existing item
                    ItemEntity targetItem = slots[cursorSlot];
                    slots[cursorSlot] = cursorHeldItem;
                    slots[cursorHeldItemOriginalSlot] = targetItem;
                    System.out.println("Inventory: Swapped items between slots " + cursorHeldItemOriginalSlot + " and " + cursorSlot);
                } else {
                    // Place in empty slot
                    slots[cursorSlot] = cursorHeldItem;
                    // Original slot is already null from when we picked it up
                    System.out.println("Inventory: Placed " + cursorHeldItem.getItemName() + " in slot " + cursorSlot);
                }

                cursorHeldItem = null;
                cursorHeldItemOriginalSlot = -1;
            }
        }
    }

    /**
     * Cancels cursor held item by returning it to original slot.
     */
    private void cancelCursorHeldItem() {
        if (cursorHeldItem != null && cursorHeldItemOriginalSlot >= 0) {
            // Return item to original slot
            slots[cursorHeldItemOriginalSlot] = cursorHeldItem;
            System.out.println("Inventory: Cancelled pickup, returning " + cursorHeldItem.getItemName() + " to slot " + cursorHeldItemOriginalSlot);
        }
        cursorHeldItem = null;
        cursorHeldItemOriginalSlot = -1;
    }

    /**
     * Gets the current cursor slot index.
     */
    public int getCursorSlot() {
        return cursorSlot;
    }

    /**
     * Sets the cursor to a specific slot.
     */
    public void setCursorSlot(int slot) {
        cursorSlot = Math.max(0, Math.min(MAX_SLOTS - 1, slot));
        ensureCursorVisible();
    }

    /**
     * Checks if an item is currently held by the cursor.
     */
    public boolean isCursorHoldingItem() {
        return cursorHeldItem != null;
    }

    /**
     * Gets the item currently held by the cursor.
     */
    public ItemEntity getCursorHeldItem() {
        return cursorHeldItem;
    }

    /**
     * Checks if navigation mode is active (keyboard/controller).
     */
    public boolean isNavigationMode() {
        return navigationMode;
    }

    /**
     * Sets navigation mode. Call with false when mouse is used.
     */
    public void setNavigationMode(boolean mode) {
        this.navigationMode = mode;
    }

    /**
     * Handles clicking on the hotbar to select slots.
     */
    private boolean handleHotbarClick(int mouseX, int mouseY) {
        int hotbarSlotSize = 50;
        int hotbarPadding = 5;
        int hotbarWidth = HOTBAR_SIZE * (hotbarSlotSize + hotbarPadding) + hotbarPadding;
        int hotbarHeight = hotbarSlotSize + hotbarPadding * 2;
        int hotbarX = (1920 - hotbarWidth) / 2;
        int hotbarY = 1080 - hotbarHeight - 20;

        for (int i = 0; i < HOTBAR_SIZE; i++) {
            int slotX = hotbarX + hotbarPadding + i * (hotbarSlotSize + hotbarPadding);
            int slotY = hotbarY + hotbarPadding;

            if (mouseX >= slotX && mouseX <= slotX + hotbarSlotSize &&
                    mouseY >= slotY && mouseY <= slotY + hotbarSlotSize) {
                setSelectedSlot(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Auto-equips an item by moving it to the selected hotbar slot.
     * If the hotbar slot has an item, they swap positions.
     */
    private void autoEquipItem(int sourceIndex) {
        if (sourceIndex < 0 || sourceIndex >= MAX_SLOTS || slots[sourceIndex] == null) return;
        if (sourceIndex == selectedSlot) return;  // Already in selected slot

        // Swap items (works even if selectedSlot is empty - just moves the item)
        ItemEntity temp = slots[selectedSlot];
        slots[selectedSlot] = slots[sourceIndex];
        slots[sourceIndex] = temp;
    }

    public void handleMousePressed(int mouseX, int mouseY) {
        if (!isOpen) return;

        int panelWidth = COLS * (slotSize + padding) + padding;
        int panelX = (1920 - panelWidth) / 2;
        int panelY = 150;

        // Check if clicking on an item slot for dragging
        int startIndex = scrollOffset * COLS;
        int endIndex = Math.min(startIndex + VISIBLE_ROWS * COLS, MAX_SLOTS);

        for (int i = startIndex; i < endIndex; i++) {
            if (slots[i] == null) continue;  // Skip empty slots

            int displayIndex = i - startIndex;
            int col = displayIndex % COLS;
            int row = displayIndex / COLS;
            int slotX = panelX + padding + col * (slotSize + padding);
            int slotY = panelY + 60 + row * (slotSize + padding);

            if (mouseX >= slotX && mouseX <= slotX + slotSize &&
                    mouseY >= slotY && mouseY <= slotY + slotSize) {
                // Start dragging this item
                draggedItem = slots[i];
                draggedIndex = i;
                isDragging = true;
                dragX = mouseX;
                dragY = mouseY;
                break;
            }
        }
    }

    public void handleMouseDragged(int mouseX, int mouseY) {
        // Update mouse position for hover tracking even while dragging
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        if (isDragging) {
            dragX = mouseX;
            dragY = mouseY;
        }
    }

    /**
     * Gets the slot index at the given screen position.
     * @return slot index (0 to MAX_SLOTS-1), or -1 if not over a slot
     */
    private int getSlotAtPosition(int mouseX, int mouseY) {
        if (!isOpen) return -1;

        int panelWidth = COLS * (slotSize + padding) + padding;
        int panelX = (1920 - panelWidth) / 2;
        int panelY = 150;

        int startIndex = scrollOffset * COLS;
        int endIndex = Math.min(startIndex + VISIBLE_ROWS * COLS, MAX_SLOTS);

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;
            int col = displayIndex % COLS;
            int row = displayIndex / COLS;
            int slotX = panelX + padding + col * (slotSize + padding);
            int slotY = panelY + 60 + row * (slotSize + padding);

            if (mouseX >= slotX && mouseX <= slotX + slotSize &&
                    mouseY >= slotY && mouseY <= slotY + slotSize) {
                return i;
            }
        }
        return -1;
    }

    public ItemEntity handleMouseReleased(int mouseX, int mouseY) {
        if (!isDragging) return null;

        ItemEntity droppedItem = null;

        // Check if dropped outside inventory panel
        int panelWidth = COLS * (slotSize + padding) + padding;
        int panelHeight = VISIBLE_ROWS * (slotSize + padding) + padding + 80;
        int panelX = (1920 - panelWidth) / 2;
        int panelY = 150;

        boolean outsideInventory = mouseX < panelX || mouseX > panelX + panelWidth ||
                mouseY < panelY || mouseY > panelY + panelHeight;

        if (outsideInventory && draggedItem != null) {
            // Check if dropped on the vault UI
            if (vaultOpen && vaultInventory != null && vaultInventory.containsPoint(mouseX, mouseY)) {
                // Transfer item to vault
                String itemId = draggedItem.getItemId();
                if (itemId == null || itemId.isEmpty()) {
                    // Try to find by item name
                    if (draggedItem.getLinkedItem() != null) {
                        itemId = ItemRegistry.findIdByName(draggedItem.getLinkedItem().getName());
                    }
                    if (itemId == null || itemId.isEmpty()) {
                        itemId = draggedItem.getItemName();
                    }
                }

                if (itemId != null && !itemId.isEmpty()) {
                    int overflow = vaultInventory.addItem(itemId, draggedItem.getStackCount());
                    if (overflow == 0) {
                        // Successfully transferred entire stack to vault
                        slots[draggedIndex] = null;
                        System.out.println("Inventory: Transferred " + itemId + " to vault");
                    } else if (overflow < draggedItem.getStackCount()) {
                        // Partially transferred
                        draggedItem.setStackCount(overflow);
                        System.out.println("Inventory: Partially transferred " + itemId + " to vault, " + overflow + " remaining");
                    } else {
                        // Vault full, couldn't transfer
                        System.out.println("Inventory: Vault full, could not transfer " + itemId);
                    }
                }
            } else {
                // Drop the item into the world
                droppedItem = draggedItem;
                slots[draggedIndex] = null;
            }
        } else if (!outsideInventory && draggedItem != null) {
            // Dropped inside inventory - check if dropped on a specific slot
            int targetSlot = getSlotAtPosition(mouseX, mouseY);

            if (targetSlot >= 0 && targetSlot != draggedIndex && targetSlot < MAX_SLOTS) {
                if (slots[targetSlot] != null) {
                    // Target slot has an item - SWAP the items
                    ItemEntity targetItem = slots[targetSlot];
                    slots[targetSlot] = draggedItem;
                    slots[draggedIndex] = targetItem;
                } else {
                    // Target slot is empty - move item there
                    slots[targetSlot] = draggedItem;
                    slots[draggedIndex] = null;
                }
            }
            // If dropped on same slot or invalid slot, item stays in place
        }

        // Reset drag state
        draggedItem = null;
        draggedIndex = -1;
        isDragging = false;

        return droppedItem;
    }

    public boolean removeItem(ItemEntity item) {
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (slots[i] == item) {
                slots[i] = null;
                return true;
            }
        }
        return false;
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
        for (int i = 0; i < MAX_SLOTS; i++) {
            ItemEntity item = slots[i];
            if (item == null) continue;

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
                // Decrement stack count or remove if last item
                if (item.getStackCount() > 1) {
                    item.decrementStack();
                    return item; // Return reference for damage calculations
                } else {
                    // Remove the item from inventory when stack is exhausted
                    slots[i] = null;
                    return item;
                }
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

        for (int i = 0; i < MAX_SLOTS; i++) {
            ItemEntity item = slots[i];
            if (item == null) continue;

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
     * @return The total number of matching ammo items (including stacks)
     */
    public int countAmmo(String ammoName) {
        if (ammoName == null || ammoName.isEmpty()) return 0;

        String lowerAmmoName = ammoName.toLowerCase();
        int count = 0;

        for (int i = 0; i < MAX_SLOTS; i++) {
            ItemEntity item = slots[i];
            if (item == null) continue;

            String itemName = item.getItemName().toLowerCase();
            String itemId = item.getItemId();

            if (itemId != null && itemId.toLowerCase().contains(lowerAmmoName)) {
                count += item.getStackCount();
            } else if (itemName.contains(lowerAmmoName)) {
                count += item.getStackCount();
            }
        }

        return count;
    }

    /**
     * Removes an item at a specific slot index (for throwable consumption).
     * If the item is stacked, decrements the stack count instead of removing.
     * @param slotIndex The slot index to remove from
     * @return The item (for reference), or null if slot was empty
     */
    public ItemEntity removeItemAtSlot(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < MAX_SLOTS && slots[slotIndex] != null) {
            ItemEntity item = slots[slotIndex];
            if (item.getStackCount() > 1) {
                item.decrementStack();
                return item; // Return reference, item still in inventory
            } else {
                slots[slotIndex] = null; // Remove last item in stack
                return item;
            }
        }
        return null;
    }

    /**
     * Gets the item at a specific slot index.
     * @param slotIndex The slot index
     * @return The item at that slot, or null if empty
     */
    public ItemEntity getItemAtSlot(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < MAX_SLOTS) {
            return slots[slotIndex];
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
        if (selectedSlot >= 0 && selectedSlot < MAX_SLOTS) {
            return slots[selectedSlot];
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
            // Note: Dragged item is now drawn separately via drawDraggedItemOverlay()
        } else {
            // Draw compact inventory preview (always visible)
            drawCompactInventory(g2d);
        }
    }

    /**
     * Draws the dragged item overlay on top of all other UI.
     * Call this AFTER drawing all inventory/vault UI to ensure proper z-order.
     */
    public void drawDraggedItemOverlay(Graphics g) {
        if (!isOpen) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw mouse-dragged item
        if (isDragging && draggedItem != null) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
            if (draggedItem.getSprite() != null) {
                g2d.drawImage(draggedItem.getSprite(),
                        dragX - slotSize/2, dragY - slotSize/2,
                        slotSize, slotSize, null);
            }
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        // Draw cursor-held item (Minecraft style - follows cursor slot)
        if (cursorHeldItem != null && navigationMode) {
            // Calculate cursor slot position
            int panelWidth = COLS * (slotSize + padding) + padding;
            int panelX = (1920 - panelWidth) / 2;
            int panelY = 150;

            int displayIndex = cursorSlot - (scrollOffset * COLS);
            if (displayIndex >= 0 && displayIndex < VISIBLE_ROWS * COLS) {
                int col = displayIndex % COLS;
                int row = displayIndex / COLS;
                int slotX = panelX + padding + col * (slotSize + padding);
                int slotY = panelY + 60 + row * (slotSize + padding);

                // Draw the held item slightly offset and with glow effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
                if (cursorHeldItem.getSprite() != null) {
                    // Draw glow behind item
                    g2d.setColor(new Color(0, 255, 255, 80));
                    g2d.fillRoundRect(slotX - 2, slotY - 2, slotSize + 4, slotSize + 4, 10, 10);

                    // Draw the item
                    g2d.drawImage(cursorHeldItem.getSprite(),
                            slotX + 5, slotY + 5,
                            slotSize - 10, slotSize - 10, null);

                    // Draw stack count if more than 1
                    if (cursorHeldItem.getStackCount() > 1) {
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                        g2d.setFont(new Font("Arial", Font.BOLD, 14));
                        String countStr = String.valueOf(cursorHeldItem.getStackCount());
                        FontMetrics cfm = g2d.getFontMetrics();
                        int countX = slotX + slotSize - cfm.stringWidth(countStr) - 5;
                        int countY = slotY + slotSize - 5;
                        g2d.setColor(Color.BLACK);
                        g2d.drawString(countStr, countX + 1, countY + 1);
                        g2d.setColor(Color.WHITE);
                        g2d.drawString(countStr, countX, countY);
                    }
                }
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }
    }

    /**
     * Checks if the player inventory is currently dragging an item.
     */
    public boolean isDragging() {
        return isDragging;
    }

    /**
     * Gets the currently dragged item.
     */
    public ItemEntity getDraggedItem() {
        return draggedItem;
    }

    /**
     * Consumes the currently dragged item (removes it from inventory).
     * Used when the item is transferred to another UI (like alchemy table).
     */
    public void consumeDraggedItem() {
        if (isDragging && draggedIndex >= 0 && draggedIndex < MAX_SLOTS) {
            slots[draggedIndex] = null;
        }
        draggedItem = null;
        draggedIndex = -1;
        isDragging = false;
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
            if (slots[i] != null) {
                ItemEntity item = slots[i];
                if (item.getSprite() != null) {
                    g2d.drawImage(item.getSprite(), slotX + 5, slotY + 5,
                            hotbarSlotSize - 10, hotbarSlotSize - 10, null);
                }

                // Draw stack count if more than 1
                if (item.getStackCount() > 1) {
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 12));
                    String countStr = String.valueOf(item.getStackCount());
                    FontMetrics fm = g2d.getFontMetrics();
                    int textX = slotX + hotbarSlotSize - fm.stringWidth(countStr) - 4;
                    int textY = slotY + hotbarSlotSize - 4;
                    // Draw shadow for visibility
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(countStr, textX + 1, textY + 1);
                    g2d.setColor(Color.WHITE);
                    g2d.drawString(countStr, textX, textY);
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

        // Draw inventory count and hint
        g2d.setColor(new Color(200, 200, 200, 150));
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String hint = "[I] Inventory (" + getItemCount() + "/" + MAX_SLOTS + ") | [1-5] Select | Scroll to cycle";
        g2d.drawString(hint, 10, 1080 - 10);
    }

    private void drawFullInventory(Graphics2D g2d) {
        int panelWidth = COLS * (slotSize + padding) + padding;
        int panelHeight = VISIBLE_ROWS * (slotSize + padding) + padding + 100;

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

        // Item count
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(new Color(180, 180, 180));
        String countText = getItemCount() + " / " + MAX_SLOTS + " items";
        g2d.drawString(countText, panelX + panelWidth - 120, panelY + 35);

        // Draw scroll indicator if needed (always show all 4 rows since slots are fixed)
        int totalRows = (int) Math.ceil(MAX_SLOTS / (double) COLS);
        if (totalRows > VISIBLE_ROWS) {
            int scrollBarX = panelX + panelWidth - 15;
            int scrollBarY = panelY + 60;
            int scrollBarHeight = VISIBLE_ROWS * (slotSize + padding);

            // Background track
            g2d.setColor(new Color(60, 60, 60));
            g2d.fillRoundRect(scrollBarX, scrollBarY, 10, scrollBarHeight, 5, 5);

            // Scroll thumb
            int thumbHeight = Math.max(30, scrollBarHeight * VISIBLE_ROWS / totalRows);
            int maxScroll = totalRows - VISIBLE_ROWS;
            int thumbY = scrollBarY + (scrollBarHeight - thumbHeight) * scrollOffset / maxScroll;
            g2d.setColor(new Color(150, 150, 150));
            g2d.fillRoundRect(scrollBarX, thumbY, 10, thumbHeight, 5, 5);
        }

        // Draw slots
        int startIndex = scrollOffset * COLS;
        int endIndex = Math.min(startIndex + VISIBLE_ROWS * COLS, MAX_SLOTS);

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;
            int col = displayIndex % COLS;
            int row = displayIndex / COLS;
            int slotX = panelX + padding + col * (slotSize + padding);
            int slotY = panelY + 60 + row * (slotSize + padding);

            // Slot background - different colors for hotbar slots
            if (i < HOTBAR_SIZE) {
                g2d.setColor(new Color(100, 80, 60, 200));  // Hotbar slots are orange-ish
            } else if (slots[i] != null) {
                g2d.setColor(new Color(80, 80, 120, 200));
            } else {
                g2d.setColor(new Color(60, 60, 60, 200));
            }
            g2d.fillRoundRect(slotX, slotY, slotSize, slotSize, 8, 8);

            // Slot border - highlight cursor slot (navigation mode) or selected slot
            if (navigationMode && i == cursorSlot) {
                // Minecraft-style cursor highlight (cyan/bright)
                g2d.setColor(new Color(0, 255, 255));  // Cyan for cursor
                g2d.setStroke(new BasicStroke(4));
            } else if (i == selectedSlot) {
                g2d.setColor(Color.YELLOW);
                g2d.setStroke(new BasicStroke(3));
            } else if (i < HOTBAR_SIZE) {
                g2d.setColor(new Color(200, 150, 100));
                g2d.setStroke(new BasicStroke(2));
            } else {
                g2d.setColor(new Color(150, 150, 150));
                g2d.setStroke(new BasicStroke(2));
            }
            g2d.drawRoundRect(slotX, slotY, slotSize, slotSize, 8, 8);

            // Draw inner highlight for cursor slot
            if (navigationMode && i == cursorSlot) {
                g2d.setColor(new Color(0, 255, 255, 60));  // Semi-transparent cyan fill
                g2d.fillRoundRect(slotX + 2, slotY + 2, slotSize - 4, slotSize - 4, 6, 6);
            }

            // Draw hotbar number for first 5 slots
            if (i < HOTBAR_SIZE) {
                g2d.setColor(new Color(255, 200, 100));
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                g2d.drawString(String.valueOf(i + 1), slotX + 3, slotY + 12);
            }

            // Draw item if present
            if (slots[i] != null) {
                ItemEntity item = slots[i];

                // Skip drawing if this is the item being dragged or held by cursor
                boolean isBeingHeld = (isDragging && draggedIndex == i) ||
                                     (cursorHeldItem != null && cursorHeldItemOriginalSlot == i);
                if (isBeingHeld) {
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

                    // Draw stack count if more than 1
                    if (item.getStackCount() > 1) {
                        g2d.setFont(new Font("Arial", Font.BOLD, 14));
                        String countStr = String.valueOf(item.getStackCount());
                        FontMetrics cfm = g2d.getFontMetrics();
                        int countX = slotX + slotSize - cfm.stringWidth(countStr) - 5;
                        int countY = slotY + slotSize - 5;
                        // Draw shadow for visibility
                        g2d.setColor(Color.BLACK);
                        g2d.drawString(countStr, countX + 1, countY + 1);
                        g2d.setColor(Color.WHITE);
                        g2d.drawString(countStr, countX, countY);
                    }
                }
            }
        }

        // Instructions
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.ITALIC, 14));
        String instructions;
        if (navigationMode) {
            instructions = "[I] Close | [Arrows/D-Pad] Navigate | [Enter/A] Pick Up/Place | [F] Equip";
        } else {
            instructions = "[I] Close | [F] Equip | Drag to move | [Arrows/D-Pad] Navigate";
        }
        FontMetrics fm = g2d.getFontMetrics();
        int textX = panelX + (panelWidth - fm.stringWidth(instructions)) / 2;
        g2d.drawString(instructions, textX, panelY + panelHeight - 15);

        // Show "HOLDING" indicator if cursor is holding an item
        if (cursorHeldItem != null) {
            g2d.setColor(new Color(0, 255, 255));
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            String holdingText = "Holding: " + cursorHeldItem.getItemName();
            g2d.drawString(holdingText, panelX + 20, panelY + panelHeight - 35);
        }

        // Draw tooltip for hovered item
        if (hoveredSlotIndex >= 0 && hoveredSlotIndex < MAX_SLOTS && slots[hoveredSlotIndex] != null) {
            drawTooltip(g2d, slots[hoveredSlotIndex]);
        }
    }

    /**
     * Draws a tooltip for the given item at the current mouse position.
     */
    private void drawTooltip(Graphics2D g2d, ItemEntity itemEntity) {
        if (itemEntity == null) return;

        Item item = itemEntity.getLinkedItem();
        String name = itemEntity.getItemName();
        String rarity = "Common";
        String category = itemEntity.getItemType();
        Color rarityColor = Color.WHITE;

        if (item != null) {
            rarity = item.getRarity().getDisplayName();
            rarityColor = item.getRarity().getColor();
            category = item.getCategory().name();
        }

        // Build description lines
        java.util.List<String> lines = new java.util.ArrayList<>();
        lines.add(name);
        lines.add(rarity + " " + category);
        if (itemEntity.getStackCount() > 1) {
            lines.add("Stack: x" + itemEntity.getStackCount());
        }

        // Add item stats if available
        if (item != null) {
            if (item.getDamage() > 0) {
                lines.add("Damage: " + item.getDamage());
            }
            if (item.getDefense() > 0) {
                lines.add("Defense: " + item.getDefense());
            }
            if (item.getHealthRestore() > 0) {
                lines.add("Heals: " + item.getHealthRestore() + " HP");
            }
            if (item.getManaRestore() > 0) {
                lines.add("Restores: " + item.getManaRestore() + " Mana");
            }
            // Add special effect if present
            if (item.getSpecialEffect() != null && !item.getSpecialEffect().isEmpty()) {
                lines.add("Effect: " + item.getSpecialEffect());
            }
            // Add ability scaling tags
            if (item.hasAbilityScaling()) {
                lines.add("Scales: " + item.getAbilityTags());
            }
            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                lines.add(item.getDescription());
            }
        }

        // Calculate tooltip size
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        int maxWidth = 0;
        for (String line : lines) {
            int w = g2d.getFontMetrics().stringWidth(line);
            if (w > maxWidth) maxWidth = w;
        }

        int tooltipWidth = maxWidth + 20;
        int lineHeight = 14;
        int tooltipHeight = lines.size() * lineHeight + 16;

        int tooltipX = lastMouseX + 15;
        int tooltipY = lastMouseY - tooltipHeight / 2;

        // Keep tooltip on screen
        if (tooltipX + tooltipWidth > 1920) {
            tooltipX = lastMouseX - tooltipWidth - 10;
        }
        if (tooltipY < 0) {
            tooltipY = 5;
        }
        if (tooltipY + tooltipHeight > 1080) {
            tooltipY = 1080 - tooltipHeight - 5;
        }

        // Background
        g2d.setColor(new Color(20, 20, 30, 240));
        g2d.fillRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 8, 8);

        // Border with rarity color
        g2d.setColor(rarityColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 8, 8);

        // Draw text lines
        int textY = tooltipY + 16;
        for (int i = 0; i < lines.size(); i++) {
            if (i == 0) {
                // Name in rarity color and bold
                g2d.setColor(rarityColor);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
            } else {
                // Other lines in light gray
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            }
            g2d.drawString(lines.get(i), tooltipX + 10, textY);
            textY += lineHeight;
        }
    }

    // ==================== Vault Integration ====================

    /**
     * Gets or creates the VaultInventory instance.
     */
    public VaultInventory getVaultInventory() {
        if (vaultInventory == null) {
            vaultInventory = new VaultInventory();

            // Set callback for when items are taken from vault (via click or navigation)
            vaultInventory.setItemTakenCallback((itemId, count) -> {
                // Try to add item to player inventory (uses cursor slot in navigation mode)
                ItemEntity item = new ItemEntity(0, 0, itemId);
                item.setLinkedItem(ItemRegistry.create(itemId));
                item.setStackCount(count);
                if (!addItemAtCursorSlot(item)) {
                    // If inventory full, put back in vault
                    vaultInventory.addItem(itemId, count);
                    System.out.println("Inventory: Inventory full, item returned to vault");
                }
            });

            // Set callback for drag-drop from vault to inventory
            vaultInventory.setInventoryDropCallback(new VaultInventory.InventoryDropCallback() {
                @Override
                public boolean onDropToInventory(String itemId, int count, int dropX, int dropY) {
                    // Try to add item to player inventory at the drop position
                    ItemEntity item = new ItemEntity(0, 0, itemId);
                    item.setLinkedItem(ItemRegistry.create(itemId));
                    item.setStackCount(count);

                    // Try to place at the specific slot where item was dropped
                    int targetSlot = getSlotAtPosition(dropX, dropY);
                    if (targetSlot >= 0) {
                        return addItemToSlot(item, targetSlot);
                    }
                    return addItem(item);
                }

                @Override
                public boolean isPointInInventory(int px, int py) {
                    return containsPoint(px, py);
                }
            });
        }
        return vaultInventory;
    }

    /**
     * Checks if a screen point is within the inventory UI bounds.
     */
    public boolean containsPoint(int px, int py) {
        if (!isOpen) {
            // Check hotbar bounds when closed
            int hotbarSlotSize = 50;
            int hotbarPadding = 5;
            int hotbarWidth = HOTBAR_SIZE * (hotbarSlotSize + hotbarPadding) + hotbarPadding;
            int hotbarHeight = hotbarSlotSize + hotbarPadding * 2;
            int hotbarX = (1920 - hotbarWidth) / 2;
            int hotbarY = 1080 - hotbarHeight - 20;
            return px >= hotbarX && px < hotbarX + hotbarWidth &&
                   py >= hotbarY && py < hotbarY + hotbarHeight;
        }

        // Check full inventory bounds when open
        int panelWidth = COLS * (slotSize + padding) + padding;
        int panelHeight = VISIBLE_ROWS * (slotSize + padding) + padding + 100;
        int panelX = (1920 - panelWidth) / 2;
        int panelY = 150;

        return px >= panelX && px < panelX + panelWidth &&
               py >= panelY && py < panelY + panelHeight;
    }

    /**
     * Opens the vault UI alongside the inventory (persistent mode).
     */
    public void openVault() {
        openVault(null);
    }

    /**
     * Opens the vault UI alongside the inventory.
     * If vaultEntity is provided and not persistent, opens in local mode.
     *
     * @param vaultEntity The vault entity to link, or null for persistent mode
     */
    public void openVault(VaultEntity vaultEntity) {
        if (!vaultOpen) {
            vaultOpen = true;
            isOpen = true;  // Also open regular inventory

            // Calculate inventory panel position
            int panelWidth = COLS * (slotSize + padding) + padding;
            int panelX = (1920 - panelWidth) / 2;
            int panelY = 150;

            VaultInventory vault = getVaultInventory();

            // Open in appropriate mode based on vault type
            if (vaultEntity != null && !vaultEntity.getVaultType().isPersistent()) {
                vault.openLocal(panelX, panelY, vaultEntity);
                System.out.println("Inventory: Storage chest opened (local mode)");
            } else {
                vault.open(panelX, panelY);
                System.out.println("Inventory: Vault opened (persistent mode)");
            }
        }
    }

    /**
     * Closes the vault UI.
     */
    public void closeVault() {
        if (vaultOpen) {
            vaultOpen = false;
            if (vaultInventory != null) {
                vaultInventory.close();
            }
            System.out.println("Inventory: Vault closed");
        }
    }

    /**
     * Toggles the vault open/closed state (persistent mode).
     */
    public void toggleVault() {
        toggleVault(null);
    }

    /**
     * Toggles the vault open/closed state.
     * If vaultEntity is provided and not persistent, opens in local mode.
     *
     * @param vaultEntity The vault entity to link, or null for persistent mode
     */
    public void toggleVault(VaultEntity vaultEntity) {
        if (vaultOpen) {
            closeVault();
        } else {
            openVault(vaultEntity);
        }
    }

    /**
     * Checks if the vault is currently open.
     */
    public boolean isVaultOpen() {
        return vaultOpen;
    }

    /**
     * Handles vault scroll events.
     */
    public void handleVaultScroll(int direction) {
        if (vaultOpen && vaultInventory != null) {
            vaultInventory.handleScroll(direction);
        }
    }

    /**
     * Handles vault click events.
     * @return true if the click was handled by the vault
     */
    public boolean handleVaultClick(int mouseX, int mouseY, boolean isRightClick) {
        if (vaultOpen && vaultInventory != null && vaultInventory.containsPoint(mouseX, mouseY)) {
            vaultInventory.handleClick(mouseX, mouseY, isRightClick);
            return true;
        }
        return false;
    }

    /**
     * Updates vault mouse position for hover effects.
     */
    public void updateVaultMousePosition(int mouseX, int mouseY) {
        if (vaultOpen && vaultInventory != null) {
            vaultInventory.updateMousePosition(mouseX, mouseY);
        }
    }

    /**
     * Handles E key for equipping items from vault to inventory.
     * @return true if an item was equipped from vault
     */
    public boolean handleVaultEquipKey() {
        if (vaultOpen && vaultInventory != null) {
            return vaultInventory.handleEquipKey();
        }
        return false;
    }

    /**
     * Handles F key for equipping items (checks both vault and inventory).
     * Vault takes priority if open and mouse is hovering over vault.
     * @return true if an item was equipped
     */
    public boolean handleEquipKeyGlobal(int mouseX, int mouseY) {
        System.out.println("Inventory.handleEquipKeyGlobal: mouseX=" + mouseX + ", mouseY=" + mouseY + ", vaultOpen=" + vaultOpen + ", isOpen=" + isOpen);

        // If vault is open and mouse is over vault, try vault equip first
        if (vaultOpen && vaultInventory != null && vaultInventory.containsPoint(mouseX, mouseY)) {
            System.out.println("Inventory.handleEquipKeyGlobal: Mouse over vault, calling vault equip");
            if (vaultInventory.handleEquipKey()) {
                return true;
            }
        }

        // Otherwise try inventory equip
        if (isOpen) {
            System.out.println("Inventory.handleEquipKeyGlobal: Inventory open, calling inventory equip");
            return handleEquipKey();
        }

        return false;
    }

    /**
     * Draws the vault UI if open.
     */
    public void drawVault(Graphics g) {
        if (vaultOpen && vaultInventory != null) {
            vaultInventory.draw(g);
        }
    }

    /**
     * Draws all dragged item overlays on top of all other UI.
     * Call this AFTER drawVault() and draw() to ensure proper z-order.
     * This ensures dragged items are always visible above both inventory and vault.
     */
    public void drawAllDraggedItemOverlays(Graphics g) {
        // Draw inventory dragged item overlay
        drawDraggedItemOverlay(g);

        // Draw vault dragged item overlay
        if (vaultOpen && vaultInventory != null) {
            vaultInventory.drawDraggedItemOverlay(g);
        }
    }

    /**
     * Transfers all inventory items to the vault.
     * Used when leaving the loot game to preserve collected items.
     *
     * @return Number of items that couldn't be stored (overflow)
     */
    public int transferAllToVault() {
        List<SavedItem> savedItems = new ArrayList<>();

        for (int i = 0; i < MAX_SLOTS; i++) {
            ItemEntity item = slots[i];
            if (item == null) continue;

            String itemId = resolveItemId(item);

            if (itemId != null && !itemId.isEmpty()) {
                savedItems.add(new SavedItem(itemId, item.getStackCount()));
            }
        }

        int overflow = SaveManager.getInstance().transferToVault(savedItems);
        if (overflow == 0) {
            clear();
            System.out.println("Inventory: All items transferred to vault");
        } else {
            System.out.println("Inventory: Vault overflow - " + overflow + " items couldn't be stored");
        }

        return overflow;
    }

    /**
     * Resolves the item registry ID for an ItemEntity.
     * Tries multiple sources in order of reliability:
     * 1. Direct itemId field
     * 2. Linked item name lookup
     * 3. Item name lookup
     * 4. Fallback to item name as ID
     */
    private String resolveItemId(ItemEntity item) {
        // Try direct item ID first
        String itemId = item.getItemId();
        if (itemId != null && !itemId.isEmpty() && ItemRegistry.getTemplate(itemId) != null) {
            return itemId;
        }

        // Try to find by linked item name
        if (item.getLinkedItem() != null) {
            String foundId = ItemRegistry.findIdByName(item.getLinkedItem().getName());
            if (foundId != null) {
                return foundId;
            }
        }

        // Try to find by item name
        String foundId = ItemRegistry.findIdByName(item.getItemName());
        if (foundId != null) {
            return foundId;
        }

        // Last resort: use item name as ID (may not work but better than nothing)
        return item.getItemName();
    }

    /**
     * Adds an item to the vault directly (bypasses inventory).
     */
    public void addToVault(ItemEntity item) {
        String itemId = resolveItemId(item);

        if (itemId != null && !itemId.isEmpty()) {
            SaveManager.getInstance().addItemToVault(itemId, item.getStackCount());
            if (vaultInventory != null && vaultInventory.isOpen()) {
                vaultInventory.loadFromSaveManager();  // Refresh display
            }
        }
    }

    /**
     * Gets all items as a list of SavedItem for vault transfer.
     */
    public List<SavedItem> getItemsAsSavedItems() {
        List<SavedItem> savedItems = new ArrayList<>();

        for (int i = 0; i < MAX_SLOTS; i++) {
            ItemEntity item = slots[i];
            if (item == null) continue;

            String itemId = resolveItemId(item);

            if (itemId != null && !itemId.isEmpty()) {
                savedItems.add(new SavedItem(itemId, item.getStackCount()));
            }
        }

        return savedItems;
    }

    /**
     * Clears all items from the inventory.
     */
    public void clear() {
        for (int i = 0; i < MAX_SLOTS; i++) {
            slots[i] = null;
        }
    }
}
