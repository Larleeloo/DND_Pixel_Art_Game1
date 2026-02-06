package com.ambermoongame.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.ambermoongame.entity.item.Item;
import com.ambermoongame.entity.item.ItemEntity;
import com.ambermoongame.entity.item.ItemRegistry;
import com.ambermoongame.entity.item.VaultEntity;
import com.ambermoongame.input.TouchInputManager;

import java.util.ArrayList;
import java.util.List;

// --- Uncomment when SaveManager is ported ---
// import com.ambermoongame.save.SaveManager;
// import com.ambermoongame.save.SaveManager.SavedItem;

/**
 * Manages collected items and displays inventory UI.
 *
 * Features:
 * - 32 slot maximum inventory capacity (fixed slots)
 * - 5-slot hotbar for quick access
 * - Touch scroll support for inventory navigation
 * - Tap to select, long-press to drag for item management
 * - Stack display for stackable items
 * - Vault integration for persistent storage
 * - Touch-based cursor navigation
 *
 * Android conversion notes:
 * - Graphics2D → Canvas + Paint
 * - Font/FontMetrics → Paint.setTextSize() + Paint.measureText()
 * - AlphaComposite → Paint.setAlpha()
 * - BasicStroke → Paint.setStrokeWidth() + Paint.Style.STROKE
 * - Hardcoded 1920x1080 → dynamic screenWidth/screenHeight
 * - InputManager keyboard → TouchInputManager
 */
public class Inventory {

    private static final String TAG = "Inventory";

    // Fixed-size slot array for Minecraft-style inventory
    private ItemEntity[] slots;
    private static final int MAX_SLOTS = 32;
    private boolean isOpen;

    // Screen dimensions (set dynamically)
    private int screenWidth = 1920;
    private int screenHeight = 1080;

    // Vault integration
    private VaultInventory vaultInventory;
    private boolean vaultOpen = false;

    // UI positioning
    private int uiX, uiY;
    private int slotSize;
    private int padding;

    // Drag and drop (touch-based)
    private ItemEntity draggedItem;
    private int draggedIndex;
    private int dragX, dragY;
    private boolean isDragging;

    // Held item (hotbar selection)
    private int selectedSlot = 0;
    private static final int HOTBAR_SIZE = 5;

    // Scroll support for full inventory
    private int scrollOffset = 0;
    private static final int VISIBLE_ROWS = 4;
    private static final int COLS = 8;

    // Hover/touch tracking
    private int hoveredSlotIndex = -1;
    private int lastTouchX = 0;
    private int lastTouchY = 0;

    // Cursor navigation (for controller/d-pad on Android)
    private int cursorSlot = 0;
    private ItemEntity cursorHeldItem = null;
    private int cursorHeldItemOriginalSlot = -1;
    private boolean navigationMode = false;

    // Reusable drawing objects
    private final Paint fillPaint;
    private final Paint strokePaint;
    private final Paint textPaint;
    private final RectF rectF;

    public Inventory() {
        this.slots = new ItemEntity[MAX_SLOTS];
        this.isOpen = false;

        this.slotSize = 60;
        this.padding = 8;
        this.uiX = 50;
        this.uiY = 80;

        this.draggedItem = null;
        this.draggedIndex = -1;
        this.isDragging = false;

        // Pre-allocate reusable drawing objects
        this.fillPaint = new Paint();
        this.fillPaint.setAntiAlias(true);
        this.fillPaint.setStyle(Paint.Style.FILL);

        this.strokePaint = new Paint();
        this.strokePaint.setAntiAlias(true);
        this.strokePaint.setStyle(Paint.Style.STROKE);

        this.textPaint = new Paint();
        this.textPaint.setAntiAlias(true);

        this.rectF = new RectF();
    }

    public Inventory(int maxSlots) {
        this();
    }

    /**
     * Sets the screen dimensions for dynamic layout.
     */
    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    public int getMaxSlots() {
        return MAX_SLOTS;
    }

    public boolean addItem(ItemEntity item) {
        if (item.isStackable()) {
            for (int i = 0; i < MAX_SLOTS; i++) {
                if (slots[i] != null && slots[i].canStackWith(item)) {
                    int remaining = slots[i].addToStack(item);
                    if (remaining == 0) {
                        return true;
                    }
                }
            }
        }

        if (item.getStackCount() > 0) {
            for (int i = 0; i < MAX_SLOTS; i++) {
                if (slots[i] == null) {
                    slots[i] = item;
                    return true;
                }
            }
        }

        if (item.getStackCount() == 0) {
            return true;
        }

        return false;
    }

    public boolean addItemAtCursorSlot(ItemEntity item) {
        if (item.isStackable()) {
            for (int i = 0; i < MAX_SLOTS; i++) {
                if (slots[i] != null && slots[i].canStackWith(item)) {
                    int remaining = slots[i].addToStack(item);
                    if (remaining == 0) {
                        return true;
                    }
                }
            }
        }

        if (item.getStackCount() > 0) {
            if (navigationMode && isOpen && cursorSlot >= 0 && cursorSlot < MAX_SLOTS && slots[cursorSlot] == null) {
                slots[cursorSlot] = item;
                Log.d(TAG, "Added " + item.getItemName() + " to cursor slot " + cursorSlot);
                return true;
            }

            for (int i = 0; i < MAX_SLOTS; i++) {
                if (slots[i] == null) {
                    slots[i] = item;
                    return true;
                }
            }
        }

        if (item.getStackCount() == 0) {
            return true;
        }

        return false;
    }

    public boolean addItemToSlot(ItemEntity item, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= MAX_SLOTS) {
            return addItem(item);
        }

        if (slots[slotIndex] == null) {
            slots[slotIndex] = item;
            return true;
        }

        if (item.isStackable() && slots[slotIndex].canStackWith(item)) {
            int remaining = slots[slotIndex].addToStack(item);
            if (remaining == 0) {
                return true;
            }
            return addItem(item);
        }

        return addItem(item);
    }

    public void toggleOpen() {
        isOpen = !isOpen;
        if (isOpen) {
            scrollOffset = 0;
            cursorSlot = 0;
            ensureCursorVisible();
        } else {
            if (cursorHeldItem != null) {
                cancelCursorHeldItem();
            }
        }
    }

    public boolean isOpen() { return isOpen; }

    public int getItemCount() {
        int count = 0;
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (slots[i] != null) count++;
        }
        return count;
    }

    public void handleScroll(int scrollDirection) {
        if (vaultOpen && vaultInventory != null) {
            vaultInventory.handleScroll(scrollDirection);
        } else if (isOpen) {
            int totalRows = (int) Math.ceil(MAX_SLOTS / (double) COLS);
            int maxScroll = Math.max(0, totalRows - VISIBLE_ROWS);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - scrollDirection));
        } else {
            cycleSelectedSlot(-scrollDirection);
        }
    }

    /**
     * Handles tap/click for selection.
     * @return true if tap was handled
     */
    public boolean handleLeftClick(int touchX, int touchY) {
        if (!isOpen) {
            return handleHotbarClick(touchX, touchY);
        }

        int panelWidth = COLS * (slotSize + padding) + padding;
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = 150;

        int startIndex = scrollOffset * COLS;
        int endIndex = Math.min(startIndex + VISIBLE_ROWS * COLS, MAX_SLOTS);

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;
            int col = displayIndex % COLS;
            int row = displayIndex / COLS;
            int slotX = panelX + padding + col * (slotSize + padding);
            int slotY = panelY + 60 + row * (slotSize + padding);

            if (touchX >= slotX && touchX <= slotX + slotSize &&
                    touchY >= slotY && touchY <= slotY + slotSize) {
                return true;
            }
        }

        return false;
    }

    /**
     * Handles equip action for the currently hovered item.
     * @return true if an item was equipped
     */
    public boolean handleEquipKey() {
        if (!isOpen || hoveredSlotIndex < 0 || hoveredSlotIndex >= MAX_SLOTS || slots[hoveredSlotIndex] == null) {
            return false;
        }
        autoEquipItem(hoveredSlotIndex);
        return true;
    }

    /**
     * Updates the touch position for hover tracking.
     */
    public void updateMousePosition(int touchX, int touchY) {
        if (isOpen && (Math.abs(touchX - lastTouchX) > 3 || Math.abs(touchY - lastTouchY) > 3)) {
            navigationMode = false;
        }

        lastTouchX = touchX;
        lastTouchY = touchY;

        if (!isOpen) {
            hoveredSlotIndex = -1;
            return;
        }

        int panelWidth = COLS * (slotSize + padding) + padding;
        int panelX = (screenWidth - panelWidth) / 2;
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

            if (touchX >= slotX && touchX <= slotX + slotSize &&
                    touchY >= slotY && touchY <= slotY + slotSize) {
                hoveredSlotIndex = i;
                break;
            }
        }
    }

    public int getHoveredSlotIndex() {
        return hoveredSlotIndex;
    }

    // ==================== Navigation ====================

    /**
     * Handles navigation input (for external controller/d-pad support).
     * @return true if navigation input was handled
     */
    public boolean handleNavigationInput(TouchInputManager input) {
        if (!isOpen) return false;

        boolean handled = false;

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

        if (input.isSelectJustPressed()) {
            handleCursorSelect();
            handled = true;
        }

        return handled;
    }

    private void moveCursor(int dx, int dy) {
        int col = cursorSlot % COLS;
        int row = cursorSlot / COLS;

        col = (col + dx + COLS) % COLS;
        row = row + dy;

        int totalRows = (int) Math.ceil(MAX_SLOTS / (double) COLS);
        if (row < 0) row = 0;
        if (row >= totalRows) row = totalRows - 1;

        cursorSlot = row * COLS + col;

        if (cursorSlot >= MAX_SLOTS) {
            cursorSlot = MAX_SLOTS - 1;
        }

        ensureCursorVisible();
    }

    private void ensureCursorVisible() {
        int cursorRow = cursorSlot / COLS;
        int firstVisibleRow = scrollOffset;
        int lastVisibleRow = scrollOffset + VISIBLE_ROWS - 1;

        if (cursorRow < firstVisibleRow) {
            scrollOffset = cursorRow;
        } else if (cursorRow > lastVisibleRow) {
            scrollOffset = cursorRow - VISIBLE_ROWS + 1;
        }

        int totalRows = (int) Math.ceil(MAX_SLOTS / (double) COLS);
        int maxScroll = Math.max(0, totalRows - VISIBLE_ROWS);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
    }

    private void handleCursorSelect() {
        if (cursorHeldItem == null) {
            if (cursorSlot >= 0 && cursorSlot < MAX_SLOTS && slots[cursorSlot] != null) {
                cursorHeldItem = slots[cursorSlot];
                cursorHeldItemOriginalSlot = cursorSlot;
                slots[cursorSlot] = null;
            }
        } else {
            if (cursorSlot >= 0 && cursorSlot < MAX_SLOTS) {
                if (slots[cursorSlot] != null) {
                    ItemEntity targetItem = slots[cursorSlot];
                    slots[cursorSlot] = cursorHeldItem;
                    slots[cursorHeldItemOriginalSlot] = targetItem;
                } else {
                    slots[cursorSlot] = cursorHeldItem;
                }

                cursorHeldItem = null;
                cursorHeldItemOriginalSlot = -1;
            }
        }
    }

    private void cancelCursorHeldItem() {
        if (cursorHeldItem != null && cursorHeldItemOriginalSlot >= 0) {
            slots[cursorHeldItemOriginalSlot] = cursorHeldItem;
        }
        cursorHeldItem = null;
        cursorHeldItemOriginalSlot = -1;
    }

    public int getCursorSlot() { return cursorSlot; }

    public void setCursorSlot(int slot) {
        cursorSlot = Math.max(0, Math.min(MAX_SLOTS - 1, slot));
        ensureCursorVisible();
    }

    public boolean isCursorHoldingItem() { return cursorHeldItem != null; }
    public ItemEntity getCursorHeldItem() { return cursorHeldItem; }
    public boolean isNavigationMode() { return navigationMode; }
    public void setNavigationMode(boolean mode) { this.navigationMode = mode; }

    private boolean handleHotbarClick(int touchX, int touchY) {
        int hotbarSlotSize = 50;
        int hotbarPadding = 5;
        int hotbarWidth = HOTBAR_SIZE * (hotbarSlotSize + hotbarPadding) + hotbarPadding;
        int hotbarHeight = hotbarSlotSize + hotbarPadding * 2;
        int hotbarX = (screenWidth - hotbarWidth) / 2;
        int hotbarY = screenHeight - hotbarHeight - 20;

        for (int i = 0; i < HOTBAR_SIZE; i++) {
            int slotX = hotbarX + hotbarPadding + i * (hotbarSlotSize + hotbarPadding);
            int slotY = hotbarY + hotbarPadding;

            if (touchX >= slotX && touchX <= slotX + hotbarSlotSize &&
                    touchY >= slotY && touchY <= slotY + hotbarSlotSize) {
                setSelectedSlot(i);
                return true;
            }
        }
        return false;
    }

    private void autoEquipItem(int sourceIndex) {
        if (sourceIndex < 0 || sourceIndex >= MAX_SLOTS || slots[sourceIndex] == null) return;
        if (sourceIndex == selectedSlot) return;

        ItemEntity temp = slots[selectedSlot];
        slots[selectedSlot] = slots[sourceIndex];
        slots[sourceIndex] = temp;
    }

    public void handleMousePressed(int touchX, int touchY) {
        if (!isOpen) return;

        int panelWidth = COLS * (slotSize + padding) + padding;
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = 150;

        int startIndex = scrollOffset * COLS;
        int endIndex = Math.min(startIndex + VISIBLE_ROWS * COLS, MAX_SLOTS);

        for (int i = startIndex; i < endIndex; i++) {
            if (slots[i] == null) continue;

            int displayIndex = i - startIndex;
            int col = displayIndex % COLS;
            int row = displayIndex / COLS;
            int slotX = panelX + padding + col * (slotSize + padding);
            int slotY = panelY + 60 + row * (slotSize + padding);

            if (touchX >= slotX && touchX <= slotX + slotSize &&
                    touchY >= slotY && touchY <= slotY + slotSize) {
                draggedItem = slots[i];
                draggedIndex = i;
                isDragging = true;
                dragX = touchX;
                dragY = touchY;
                break;
            }
        }
    }

    public void handleMouseDragged(int touchX, int touchY) {
        lastTouchX = touchX;
        lastTouchY = touchY;

        if (isDragging) {
            dragX = touchX;
            dragY = touchY;
        }
    }

    private int getSlotAtPosition(int touchX, int touchY) {
        if (!isOpen) return -1;

        int panelWidth = COLS * (slotSize + padding) + padding;
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = 150;

        int gridStartX = panelX + padding;
        int gridStartY = panelY + 60;
        int gridWidth = COLS * (slotSize + padding);
        int gridHeight = VISIBLE_ROWS * (slotSize + padding);

        int tolerance = padding;
        if (touchX < gridStartX - tolerance || touchX > gridStartX + gridWidth + tolerance ||
            touchY < gridStartY - tolerance || touchY > gridStartY + gridHeight + tolerance) {
            return -1;
        }

        int cellWidth = slotSize + padding;
        int cellHeight = slotSize + padding;

        int relativeX = touchX - gridStartX;
        int relativeY = touchY - gridStartY;

        int col = Math.max(0, Math.min(COLS - 1, relativeX / cellWidth));
        int row = Math.max(0, Math.min(VISIBLE_ROWS - 1, relativeY / cellHeight));

        if (relativeX >= COLS * cellWidth) col = COLS - 1;
        if (relativeY >= VISIBLE_ROWS * cellHeight) row = VISIBLE_ROWS - 1;

        int slotIndex = (scrollOffset + row) * COLS + col;

        if (slotIndex >= 0 && slotIndex < MAX_SLOTS) {
            return slotIndex;
        }

        return -1;
    }

    private int getNearestSlotToPosition(int touchX, int touchY) {
        int slot = getSlotAtPosition(touchX, touchY);
        if (slot >= 0) return slot;

        int panelWidth = COLS * (slotSize + padding) + padding;
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = 150;
        int gridStartX = panelX + padding;
        int gridStartY = panelY + 60;

        int nearestSlot = cursorSlot;
        double minDistance = Double.MAX_VALUE;

        int startIndex = scrollOffset * COLS;
        int endIndex = Math.min(startIndex + VISIBLE_ROWS * COLS, MAX_SLOTS);

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;
            int col = displayIndex % COLS;
            int row = displayIndex / COLS;
            int slotCenterX = gridStartX + col * (slotSize + padding) + slotSize / 2;
            int slotCenterY = gridStartY + row * (slotSize + padding) + slotSize / 2;

            double distance = Math.sqrt(Math.pow(touchX - slotCenterX, 2) + Math.pow(touchY - slotCenterY, 2));
            if (distance < minDistance) {
                minDistance = distance;
                nearestSlot = i;
            }
        }

        return nearestSlot;
    }

    public ItemEntity handleMouseReleased(int touchX, int touchY) {
        if (!isDragging) return null;

        ItemEntity droppedItem = null;

        int panelWidth = COLS * (slotSize + padding) + padding;
        int panelHeight = VISIBLE_ROWS * (slotSize + padding) + padding + 80;
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = 150;

        boolean outsideInventory = touchX < panelX || touchX > panelX + panelWidth ||
                touchY < panelY || touchY > panelY + panelHeight;

        if (outsideInventory && draggedItem != null) {
            if (vaultOpen && vaultInventory != null && vaultInventory.containsPoint(touchX, touchY)) {
                String itemId = draggedItem.getItemId();
                if (itemId == null || itemId.isEmpty()) {
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
                        slots[draggedIndex] = null;
                        Log.d(TAG, "Transferred " + itemId + " to vault");
                    } else if (overflow < draggedItem.getStackCount()) {
                        draggedItem.setStackCount(overflow);
                    }
                }
            } else {
                droppedItem = draggedItem;
                slots[draggedIndex] = null;
            }
        } else if (!outsideInventory && draggedItem != null) {
            int targetSlot = getSlotAtPosition(touchX, touchY);

            if (targetSlot < 0) {
                targetSlot = getNearestSlotToPosition(touchX, touchY);
            }

            if (targetSlot >= 0 && targetSlot != draggedIndex && targetSlot < MAX_SLOTS) {
                if (slots[targetSlot] != null) {
                    ItemEntity targetItem = slots[targetSlot];
                    slots[targetSlot] = draggedItem;
                    slots[draggedIndex] = targetItem;
                } else {
                    slots[targetSlot] = draggedItem;
                    slots[draggedIndex] = null;
                }
            }
        }

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

    public ItemEntity consumeAmmo(String ammoName) {
        if (ammoName == null || ammoName.isEmpty()) return null;

        String lowerAmmoName = ammoName.toLowerCase();

        for (int i = 0; i < MAX_SLOTS; i++) {
            ItemEntity item = slots[i];
            if (item == null) continue;

            String itemName = item.getItemName().toLowerCase();
            String itemType = item.getItemType().toLowerCase();
            String itemId = item.getItemId();

            boolean matches = false;
            if (itemId != null && itemId.toLowerCase().contains(lowerAmmoName)) {
                matches = true;
            } else if (itemName.contains(lowerAmmoName)) {
                matches = true;
            } else if (itemType.equals("ammo") && itemName.contains(lowerAmmoName)) {
                matches = true;
            }

            if (matches) {
                if (item.getStackCount() > 1) {
                    item.decrementStack();
                    return item;
                } else {
                    slots[i] = null;
                    return item;
                }
            }
        }

        return null;
    }

    public boolean hasAmmo(String ammoName) {
        if (ammoName == null || ammoName.isEmpty()) return false;

        String lowerAmmoName = ammoName.toLowerCase();

        for (int i = 0; i < MAX_SLOTS; i++) {
            ItemEntity item = slots[i];
            if (item == null) continue;

            String itemName = item.getItemName().toLowerCase();
            String itemId = item.getItemId();

            if (itemId != null && itemId.toLowerCase().contains(lowerAmmoName)) return true;
            if (itemName.contains(lowerAmmoName)) return true;
        }

        return false;
    }

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

    public ItemEntity removeItemAtSlot(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < MAX_SLOTS && slots[slotIndex] != null) {
            ItemEntity item = slots[slotIndex];
            if (item.getStackCount() > 1) {
                item.decrementStack();
                return item;
            } else {
                slots[slotIndex] = null;
                return item;
            }
        }
        return null;
    }

    public ItemEntity getItemAtSlot(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < MAX_SLOTS) {
            return slots[slotIndex];
        }
        return null;
    }

    public int getSelectedSlot() { return selectedSlot; }

    public void setSelectedSlot(int slot) {
        this.selectedSlot = Math.max(0, Math.min(HOTBAR_SIZE - 1, slot));
    }

    public void cycleSelectedSlot(int direction) {
        selectedSlot = (selectedSlot + direction + HOTBAR_SIZE) % HOTBAR_SIZE;
    }

    public ItemEntity getHeldItem() {
        if (selectedSlot >= 0 && selectedSlot < MAX_SLOTS) {
            return slots[selectedSlot];
        }
        return null;
    }

    public int getHeldToolType() {
        ItemEntity held = getHeldItem();
        if (held != null) {
            return ToolType.fromItemType(held.getItemType());
        }
        return ToolType.HAND;
    }

    public boolean handleHotbarKey(char key) {
        if (key >= '1' && key <= '5') {
            setSelectedSlot(key - '1');
            return true;
        }
        return false;
    }

    public boolean isDragging() { return isDragging; }
    public ItemEntity getDraggedItem() { return draggedItem; }

    public void consumeDraggedItem() {
        if (isDragging && draggedIndex >= 0 && draggedIndex < MAX_SLOTS) {
            slots[draggedIndex] = null;
        }
        draggedItem = null;
        draggedIndex = -1;
        isDragging = false;
    }

    // ==================== Drawing ====================

    public void draw(Canvas canvas) {
        if (isOpen) {
            drawFullInventory(canvas);
        } else {
            drawCompactInventory(canvas);
        }
    }

    public void drawDraggedItemOverlay(Canvas canvas) {
        if (!isOpen) return;

        // Draw mouse-dragged item
        if (isDragging && draggedItem != null) {
            Bitmap sprite = draggedItem.getSprite();
            if (sprite != null) {
                fillPaint.setAlpha(217); // ~85%
                canvas.drawBitmap(sprite, null,
                        new RectF(dragX - slotSize / 2f, dragY - slotSize / 2f,
                                  dragX + slotSize / 2f, dragY + slotSize / 2f), fillPaint);
                fillPaint.setAlpha(255);
            }
        }

        // Draw cursor-held item (navigation mode)
        if (cursorHeldItem != null && navigationMode) {
            int panelWidth = COLS * (slotSize + padding) + padding;
            int panelX = (screenWidth - panelWidth) / 2;
            int panelY = 150;

            int displayIndex = cursorSlot - (scrollOffset * COLS);
            if (displayIndex >= 0 && displayIndex < VISIBLE_ROWS * COLS) {
                int col = displayIndex % COLS;
                int row = displayIndex / COLS;
                int slotX = panelX + padding + col * (slotSize + padding);
                int slotY = panelY + 60 + row * (slotSize + padding);

                Bitmap sprite = cursorHeldItem.getSprite();
                if (sprite != null) {
                    // Glow behind item
                    fillPaint.setColor(Color.argb(80, 0, 255, 255));
                    rectF.set(slotX - 2, slotY - 2, slotX + slotSize + 4, slotY + slotSize + 4);
                    canvas.drawRoundRect(rectF, 10, 10, fillPaint);

                    // Draw item
                    fillPaint.setAlpha(230);
                    canvas.drawBitmap(sprite, null,
                            new RectF(slotX + 5, slotY + 5, slotX + slotSize - 5, slotY + slotSize - 5), fillPaint);
                    fillPaint.setAlpha(255);

                    // Stack count
                    if (cursorHeldItem.getStackCount() > 1) {
                        String countStr = String.valueOf(cursorHeldItem.getStackCount());
                        textPaint.setTextSize(14);
                        textPaint.setFakeBoldText(true);
                        float countWidth = textPaint.measureText(countStr);
                        int countX = (int) (slotX + slotSize - countWidth - 5);
                        int countY = slotY + slotSize - 5;
                        textPaint.setColor(Color.BLACK);
                        canvas.drawText(countStr, countX + 1, countY + 1, textPaint);
                        textPaint.setColor(Color.WHITE);
                        canvas.drawText(countStr, countX, countY, textPaint);
                        textPaint.setFakeBoldText(false);
                    }
                }
            }
        }
    }

    private void drawCompactInventory(Canvas canvas) {
        int hotbarSlotSize = 50;
        int hotbarPadding = 5;
        int hotbarWidth = HOTBAR_SIZE * (hotbarSlotSize + hotbarPadding) + hotbarPadding;
        int hotbarHeight = hotbarSlotSize + hotbarPadding * 2;
        int hotbarX = (screenWidth - hotbarWidth) / 2;
        int hotbarY = screenHeight - hotbarHeight - 20;

        // Background
        fillPaint.setColor(Color.argb(180, 0, 0, 0));
        rectF.set(hotbarX, hotbarY, hotbarX + hotbarWidth, hotbarY + hotbarHeight);
        canvas.drawRoundRect(rectF, 10, 10, fillPaint);

        // Border
        strokePaint.setColor(Color.rgb(100, 100, 100));
        strokePaint.setStrokeWidth(2);
        canvas.drawRoundRect(rectF, 10, 10, strokePaint);

        // Draw hotbar slots
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            int slotX = hotbarX + hotbarPadding + i * (hotbarSlotSize + hotbarPadding);
            int slotY = hotbarY + hotbarPadding;

            // Slot background
            if (i == selectedSlot) {
                fillPaint.setColor(Color.argb(100, 255, 255, 255));
            } else {
                fillPaint.setColor(Color.argb(200, 60, 60, 60));
            }
            rectF.set(slotX, slotY, slotX + hotbarSlotSize, slotY + hotbarSlotSize);
            canvas.drawRoundRect(rectF, 6, 6, fillPaint);

            // Slot border
            if (i == selectedSlot) {
                strokePaint.setColor(Color.WHITE);
                strokePaint.setStrokeWidth(3);
            } else {
                strokePaint.setColor(Color.rgb(120, 120, 120));
                strokePaint.setStrokeWidth(2);
            }
            canvas.drawRoundRect(rectF, 6, 6, strokePaint);

            // Draw item
            if (slots[i] != null) {
                ItemEntity item = slots[i];
                Bitmap sprite = item.getSprite();
                if (sprite != null) {
                    canvas.drawBitmap(sprite, null,
                            new RectF(slotX + 5, slotY + 5,
                                      slotX + hotbarSlotSize - 5, slotY + hotbarSlotSize - 5), fillPaint);
                }

                // Stack count
                if (item.getStackCount() > 1) {
                    String countStr = String.valueOf(item.getStackCount());
                    textPaint.setTextSize(12);
                    textPaint.setFakeBoldText(true);
                    float countWidth = textPaint.measureText(countStr);
                    int textX = (int) (slotX + hotbarSlotSize - countWidth - 4);
                    int textY = slotY + hotbarSlotSize - 4;
                    textPaint.setColor(Color.BLACK);
                    canvas.drawText(countStr, textX + 1, textY + 1, textPaint);
                    textPaint.setColor(Color.WHITE);
                    canvas.drawText(countStr, textX, textY, textPaint);
                    textPaint.setFakeBoldText(false);
                }
            }

            // Slot number
            textPaint.setColor(Color.rgb(200, 200, 200));
            textPaint.setTextSize(10);
            canvas.drawText(String.valueOf(i + 1), slotX + 3, slotY + 12, textPaint);
        }

        // Held item name above hotbar
        ItemEntity held = getHeldItem();
        if (held != null) {
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(14);
            textPaint.setFakeBoldText(true);
            String heldName = held.getItemName();
            float nameWidth = textPaint.measureText(heldName);
            float textX = hotbarX + (hotbarWidth - nameWidth) / 2f;
            canvas.drawText(heldName, textX, hotbarY - 8, textPaint);
            textPaint.setFakeBoldText(false);
        }

        // Item count hint
        textPaint.setColor(Color.argb(150, 200, 200, 200));
        textPaint.setTextSize(12);
        String hint = "Inventory (" + getItemCount() + "/" + MAX_SLOTS + ")";
        canvas.drawText(hint, 10, screenHeight - 10, textPaint);
    }

    private void drawFullInventory(Canvas canvas) {
        int panelWidth = COLS * (slotSize + padding) + padding;
        int panelHeight = VISIBLE_ROWS * (slotSize + padding) + padding + 100;

        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = 150;

        // Background panel
        fillPaint.setColor(Color.argb(230, 40, 40, 40));
        rectF.set(panelX, panelY, panelX + panelWidth, panelY + panelHeight);
        canvas.drawRoundRect(rectF, 20, 20, fillPaint);

        // Border
        strokePaint.setColor(Color.rgb(200, 200, 200));
        strokePaint.setStrokeWidth(3);
        canvas.drawRoundRect(rectF, 20, 20, strokePaint);

        // Title
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24);
        textPaint.setFakeBoldText(true);
        canvas.drawText("INVENTORY", panelX + 20, panelY + 35, textPaint);
        textPaint.setFakeBoldText(false);

        // Item count
        textPaint.setTextSize(14);
        textPaint.setColor(Color.rgb(180, 180, 180));
        String countText = getItemCount() + " / " + MAX_SLOTS + " items";
        canvas.drawText(countText, panelX + panelWidth - 120, panelY + 35, textPaint);

        // Scroll indicator
        int totalRows = (int) Math.ceil(MAX_SLOTS / (double) COLS);
        if (totalRows > VISIBLE_ROWS) {
            int scrollBarX = panelX + panelWidth - 15;
            int scrollBarY = panelY + 60;
            int scrollBarHeight = VISIBLE_ROWS * (slotSize + padding);

            fillPaint.setColor(Color.rgb(60, 60, 60));
            rectF.set(scrollBarX, scrollBarY, scrollBarX + 10, scrollBarY + scrollBarHeight);
            canvas.drawRoundRect(rectF, 5, 5, fillPaint);

            int thumbHeight = Math.max(30, scrollBarHeight * VISIBLE_ROWS / totalRows);
            int maxScroll = totalRows - VISIBLE_ROWS;
            int thumbY = scrollBarY + (scrollBarHeight - thumbHeight) * scrollOffset / maxScroll;
            fillPaint.setColor(Color.rgb(150, 150, 150));
            rectF.set(scrollBarX, thumbY, scrollBarX + 10, thumbY + thumbHeight);
            canvas.drawRoundRect(rectF, 5, 5, fillPaint);
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

            // Slot background
            if (i < HOTBAR_SIZE) {
                fillPaint.setColor(Color.argb(200, 100, 80, 60));
            } else if (slots[i] != null) {
                fillPaint.setColor(Color.argb(200, 80, 80, 120));
            } else {
                fillPaint.setColor(Color.argb(200, 60, 60, 60));
            }
            rectF.set(slotX, slotY, slotX + slotSize, slotY + slotSize);
            canvas.drawRoundRect(rectF, 8, 8, fillPaint);

            // Slot border
            if (navigationMode && i == cursorSlot) {
                strokePaint.setColor(Color.CYAN);
                strokePaint.setStrokeWidth(4);
            } else if (i == selectedSlot) {
                strokePaint.setColor(Color.YELLOW);
                strokePaint.setStrokeWidth(3);
            } else if (i < HOTBAR_SIZE) {
                strokePaint.setColor(Color.rgb(200, 150, 100));
                strokePaint.setStrokeWidth(2);
            } else {
                strokePaint.setColor(Color.rgb(150, 150, 150));
                strokePaint.setStrokeWidth(2);
            }
            canvas.drawRoundRect(rectF, 8, 8, strokePaint);

            // Inner highlight for cursor slot
            if (navigationMode && i == cursorSlot) {
                fillPaint.setColor(Color.argb(60, 0, 255, 255));
                rectF.set(slotX + 2, slotY + 2, slotX + slotSize - 2, slotY + slotSize - 2);
                canvas.drawRoundRect(rectF, 6, 6, fillPaint);
            }

            // Hotbar number
            if (i < HOTBAR_SIZE) {
                textPaint.setColor(Color.rgb(255, 200, 100));
                textPaint.setTextSize(10);
                textPaint.setFakeBoldText(true);
                canvas.drawText(String.valueOf(i + 1), slotX + 3, slotY + 12, textPaint);
                textPaint.setFakeBoldText(false);
            }

            // Draw item
            if (slots[i] != null) {
                ItemEntity item = slots[i];

                boolean isBeingHeld = (isDragging && draggedIndex == i) ||
                                     (cursorHeldItem != null && cursorHeldItemOriginalSlot == i);
                if (isBeingHeld) {
                    // Dashed border for dragged slot
                    strokePaint.setColor(Color.argb(100, 100, 100, 100));
                    strokePaint.setStrokeWidth(2);
                    strokePaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));
                    rectF.set(slotX, slotY, slotX + slotSize, slotY + slotSize);
                    canvas.drawRoundRect(rectF, 8, 8, strokePaint);
                    strokePaint.setPathEffect(null);
                } else {
                    Bitmap sprite = item.getSprite();
                    if (sprite != null) {
                        canvas.drawBitmap(sprite, null,
                                new RectF(slotX + 5, slotY + 5, slotX + slotSize - 5, slotY + slotSize - 5),
                                fillPaint);
                    }

                    if (item.getStackCount() > 1) {
                        String countStr = String.valueOf(item.getStackCount());
                        textPaint.setTextSize(14);
                        textPaint.setFakeBoldText(true);
                        float countWidth = textPaint.measureText(countStr);
                        int countX = (int) (slotX + slotSize - countWidth - 5);
                        int countY = slotY + slotSize - 5;
                        textPaint.setColor(Color.BLACK);
                        canvas.drawText(countStr, countX + 1, countY + 1, textPaint);
                        textPaint.setColor(Color.WHITE);
                        canvas.drawText(countStr, countX, countY, textPaint);
                        textPaint.setFakeBoldText(false);
                    }
                }
            }
        }

        // Instructions
        textPaint.setColor(Color.LTGRAY);
        textPaint.setTextSize(14);
        String instructions = navigationMode ?
                "Tap to close | Navigate | Select to pick/place" :
                "Tap to close | Drag to move | Tap equip button";
        float instrWidth = textPaint.measureText(instructions);
        float textX = panelX + (panelWidth - instrWidth) / 2f;
        canvas.drawText(instructions, textX, panelY + panelHeight - 15, textPaint);

        // Holding indicator
        if (cursorHeldItem != null) {
            textPaint.setColor(Color.CYAN);
            textPaint.setTextSize(12);
            textPaint.setFakeBoldText(true);
            String holdingText = "Holding: " + cursorHeldItem.getItemName();
            canvas.drawText(holdingText, panelX + 20, panelY + panelHeight - 35, textPaint);
            textPaint.setFakeBoldText(false);
        }

        // Tooltip for hovered item
        if (hoveredSlotIndex >= 0 && hoveredSlotIndex < MAX_SLOTS && slots[hoveredSlotIndex] != null) {
            drawTooltip(canvas, slots[hoveredSlotIndex]);
        }
    }

    private void drawTooltip(Canvas canvas, ItemEntity itemEntity) {
        if (itemEntity == null) return;

        Item item = itemEntity.getLinkedItem();
        String name = itemEntity.getItemName();
        String rarity = "Common";
        String category = itemEntity.getItemType();
        int rarityColor = Color.WHITE;

        if (item != null) {
            rarity = item.getRarity().getDisplayName();
            rarityColor = item.getRarity().getColor();
            category = item.getCategory().name();
        }

        List<String> lines = new ArrayList<>();
        lines.add(name);
        lines.add(rarity + " " + category);
        if (itemEntity.getStackCount() > 1) {
            lines.add("Stack: x" + itemEntity.getStackCount());
        }

        if (item != null) {
            if (item.getDamage() > 0) lines.add("Damage: " + item.getDamage());
            if (item.getDefense() > 0) lines.add("Defense: " + item.getDefense());
            if (item.getHealthRestore() > 0) lines.add("Heals: " + item.getHealthRestore() + " HP");
            if (item.getManaRestore() > 0) lines.add("Restores: " + item.getManaRestore() + " Mana");
            if (item.getSpecialEffect() != null && !item.getSpecialEffect().isEmpty()) {
                lines.add("Effect: " + item.getSpecialEffect());
            }
            if (item.hasAbilityScaling()) {
                lines.add("Scales: " + item.getAbilityTags());
            }
            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                lines.add(item.getDescription());
            }
        }

        textPaint.setTextSize(12);
        textPaint.setFakeBoldText(true);
        int maxWidth = 0;
        for (String line : lines) {
            int w = (int) textPaint.measureText(line);
            if (w > maxWidth) maxWidth = w;
        }

        int tooltipWidth = maxWidth + 20;
        int lineHeight = 14;
        int tooltipHeight = lines.size() * lineHeight + 16;

        int tooltipX = lastTouchX + 15;
        int tooltipY = lastTouchY - tooltipHeight / 2;

        if (tooltipX + tooltipWidth > screenWidth) tooltipX = lastTouchX - tooltipWidth - 10;
        if (tooltipY < 0) tooltipY = 5;
        if (tooltipY + tooltipHeight > screenHeight) tooltipY = screenHeight - tooltipHeight - 5;

        // Background
        fillPaint.setColor(Color.argb(240, 20, 20, 30));
        rectF.set(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight);
        canvas.drawRoundRect(rectF, 8, 8, fillPaint);

        // Border with rarity color
        strokePaint.setColor(rarityColor);
        strokePaint.setStrokeWidth(2);
        canvas.drawRoundRect(rectF, 8, 8, strokePaint);

        // Text lines
        int textY = tooltipY + 16;
        for (int i = 0; i < lines.size(); i++) {
            if (i == 0) {
                textPaint.setColor(rarityColor);
                textPaint.setTextSize(12);
                textPaint.setFakeBoldText(true);
            } else {
                textPaint.setColor(Color.LTGRAY);
                textPaint.setTextSize(10);
                textPaint.setFakeBoldText(false);
            }
            canvas.drawText(lines.get(i), tooltipX + 10, textY, textPaint);
            textY += lineHeight;
        }
    }

    // ==================== Vault Integration ====================

    public VaultInventory getVaultInventory() {
        if (vaultInventory == null) {
            vaultInventory = new VaultInventory();
            vaultInventory.setScreenSize(screenWidth, screenHeight);

            vaultInventory.setItemTakenCallback((itemId, count) -> {
                ItemEntity item = new ItemEntity(0, 0, itemId);
                item.setLinkedItem(ItemRegistry.create(itemId));
                item.setStackCount(count);
                if (!addItemAtCursorSlot(item)) {
                    vaultInventory.addItem(itemId, count);
                    Log.d(TAG, "Inventory full, item returned to vault");
                }
            });

            vaultInventory.setInventoryDropCallback(new VaultInventory.InventoryDropCallback() {
                @Override
                public boolean onDropToInventory(String itemId, int count, int dropX, int dropY) {
                    ItemEntity item = new ItemEntity(0, 0, itemId);
                    item.setLinkedItem(ItemRegistry.create(itemId));
                    item.setStackCount(count);

                    int targetSlot = getSlotAtPosition(dropX, dropY);
                    if (targetSlot < 0) {
                        targetSlot = getNearestSlotToPosition(dropX, dropY);
                    }

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

    public boolean containsPoint(int px, int py) {
        if (!isOpen) {
            int hotbarSlotSize = 50;
            int hotbarPadding = 5;
            int hotbarWidth = HOTBAR_SIZE * (hotbarSlotSize + hotbarPadding) + hotbarPadding;
            int hotbarHeight = hotbarSlotSize + hotbarPadding * 2;
            int hotbarX = (screenWidth - hotbarWidth) / 2;
            int hotbarY = screenHeight - hotbarHeight - 20;
            return px >= hotbarX && px < hotbarX + hotbarWidth &&
                   py >= hotbarY && py < hotbarY + hotbarHeight;
        }

        int panelWidth = COLS * (slotSize + padding) + padding;
        int panelHeight = VISIBLE_ROWS * (slotSize + padding) + padding + 100;
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = 150;

        return px >= panelX && px < panelX + panelWidth &&
               py >= panelY && py < panelY + panelHeight;
    }

    public void openVault() { openVault(null); }

    public void openVault(VaultEntity vaultEntity) {
        if (!vaultOpen) {
            vaultOpen = true;
            isOpen = true;

            int panelWidth = COLS * (slotSize + padding) + padding;
            int panelX = (screenWidth - panelWidth) / 2;
            int panelY = 150;

            VaultInventory vault = getVaultInventory();

            if (vaultEntity != null && !vaultEntity.getVaultType().isPersistent()) {
                vault.openLocal(panelX, panelY, vaultEntity);
            } else {
                vault.open(panelX, panelY);
            }
        }
    }

    public void closeVault() {
        if (vaultOpen) {
            vaultOpen = false;
            if (vaultInventory != null) {
                vaultInventory.close();
            }
        }
    }

    public void toggleVault() { toggleVault(null); }

    public void toggleVault(VaultEntity vaultEntity) {
        if (vaultOpen) {
            closeVault();
        } else {
            openVault(vaultEntity);
        }
    }

    public boolean isVaultOpen() { return vaultOpen; }

    public void handleVaultScroll(int direction) {
        if (vaultOpen && vaultInventory != null) {
            vaultInventory.handleScroll(direction);
        }
    }

    public boolean handleVaultClick(int touchX, int touchY, boolean isRightClick) {
        if (vaultOpen && vaultInventory != null && vaultInventory.containsPoint(touchX, touchY)) {
            vaultInventory.handleClick(touchX, touchY, isRightClick);
            return true;
        }
        return false;
    }

    public void updateVaultMousePosition(int touchX, int touchY) {
        if (vaultOpen && vaultInventory != null) {
            vaultInventory.updateMousePosition(touchX, touchY);
        }
    }

    public boolean handleVaultEquipKey() {
        if (vaultOpen && vaultInventory != null) {
            return vaultInventory.handleEquipKey();
        }
        return false;
    }

    public boolean handleEquipKeyGlobal(int touchX, int touchY) {
        if (vaultOpen && vaultInventory != null && vaultInventory.containsPoint(touchX, touchY)) {
            if (vaultInventory.handleEquipKey()) return true;
        }
        if (isOpen) return handleEquipKey();
        return false;
    }

    public void drawVault(Canvas canvas) {
        if (vaultOpen && vaultInventory != null) {
            vaultInventory.draw(canvas);
        }
    }

    public void drawAllDraggedItemOverlays(Canvas canvas) {
        drawDraggedItemOverlay(canvas);
        if (vaultOpen && vaultInventory != null) {
            vaultInventory.drawDraggedItemOverlay(canvas);
        }
    }

    // --- Uncomment when SaveManager is ported ---
    // public int transferAllToVault() { ... }
    // public void addToVault(ItemEntity item) { ... }
    // public List<SavedItem> getItemsAsSavedItems() { ... }

    /**
     * Resolves the item registry ID for an ItemEntity.
     */
    private String resolveItemId(ItemEntity item) {
        String itemId = item.getItemId();
        if (itemId != null && !itemId.isEmpty() && ItemRegistry.getTemplate(itemId) != null) {
            return itemId;
        }

        if (item.getLinkedItem() != null) {
            String foundId = ItemRegistry.findIdByName(item.getLinkedItem().getName());
            if (foundId != null) return foundId;
        }

        String foundId = ItemRegistry.findIdByName(item.getItemName());
        if (foundId != null) return foundId;

        return item.getItemName();
    }

    public void clear() {
        for (int i = 0; i < MAX_SLOTS; i++) {
            slots[i] = null;
        }
    }
}
