package com.ambermoongame.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

import com.ambermoongame.entity.item.Item;
import com.ambermoongame.entity.item.ItemEntity;
import com.ambermoongame.entity.item.ItemRegistry;
import com.ambermoongame.entity.item.VaultEntity;

import java.util.ArrayList;
import java.util.List;

// --- Uncomment when SaveManager is ported ---
// import com.ambermoongame.save.SaveManager;
// import com.ambermoongame.save.SaveManager.SavedItem;

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
 * Android conversion notes:
 * - Graphics2D → Canvas + Paint
 * - BufferedImage → Bitmap
 * - Font/FontMetrics → Paint.setTextSize()/measureText()
 * - AlphaComposite → Paint.setAlpha()
 * - BasicStroke → Paint.setStrokeWidth()
 * - Hardcoded 1080 screen height → dynamic screenHeight
 */
public class VaultInventory {

    private static final String TAG = "VaultInventory";

    // Layout constants
    private static final int SLOT_SIZE = 48;
    private static final int SLOT_PADDING = 4;
    private static final int SCROLLBAR_WIDTH = 16;

    // Dynamic layout based on chest type
    private int columns = 6;
    private int visibleRows = 8;
    private int visibleSlots = 48;
    private int maxSlots = 10000;

    // Screen dimensions
    private int screenWidth = 1920;
    private int screenHeight = 1080;

    // Position
    private int x, y;
    private int width, height;

    // State
    private boolean isOpen = false;
    private int scrollOffset = 0;
    private int maxScrollRows = 0;

    // Local storage mode (for STORAGE_CHEST)
    private boolean localMode = false;
    private VaultEntity linkedVault = null;

    // Items loaded from vault
    private List<VaultSlot> slots = new ArrayList<>();

    // Drag and drop state
    private VaultSlot draggedSlot = null;
    private int draggedSlotIndex = -1;
    private int dragOffsetX, dragOffsetY;
    private int mouseX, mouseY;
    private boolean isDragging = false;

    // Hover state
    private int hoveredSlotIndex = -1;

    // Sort mode constants (replaces enum to avoid D8 crash)
    public static final int SORT_MODE_NONE = 0;
    public static final int SORT_MODE_RARITY_DESC = 1;
    public static final int SORT_MODE_RARITY_ASC = 2;
    public static final int SORT_MODE_ALPHABETICAL = 3;

    private int currentSortMode = SORT_MODE_NONE;

    // Sort button bounds (calculated in draw)
    private int sortRarityBtnX, sortRarityBtnY, sortRarityBtnW, sortRarityBtnH;
    private int sortAlphaBtnX, sortAlphaBtnY, sortAlphaBtnBtnW, sortAlphaBtnH;

    // Callbacks
    private InventoryDropCallback inventoryDropCallback;
    private ItemTakenCallback itemTakenCallback;

    // Reusable drawing objects
    private final Paint fillPaint;
    private final Paint strokePaint;
    private final Paint textPaint;
    private final RectF rectF;

    /**
     * Represents a slot in the vault.
     */
    public static class VaultSlot {
        public String itemId;
        public int stackCount;
        public Item itemTemplate;
        public Bitmap icon;

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

    public interface ItemTakenCallback {
        void onItemTaken(String itemId, int count);
    }

    public interface InventoryDropCallback {
        boolean onDropToInventory(String itemId, int count, int dropX, int dropY);
        boolean isPointInInventory(int x, int y);
    }

    public VaultInventory() {
        calculateDimensions();

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

    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    private void calculateDimensions() {
        width = columns * (SLOT_SIZE + SLOT_PADDING) + SLOT_PADDING + SCROLLBAR_WIDTH;
        height = visibleRows * (SLOT_SIZE + SLOT_PADDING) + SLOT_PADDING + 40;
    }

    private void configureLayoutForChestSize(int totalSlots) {
        maxSlots = totalSlots;

        if (totalSlots <= 5) {
            columns = 5;
            visibleRows = 1;
        } else if (totalSlots <= 16) {
            columns = 4;
            visibleRows = 4;
        } else if (totalSlots <= 32) {
            columns = 8;
            visibleRows = 4;
        } else if (totalSlots <= 48) {
            columns = 6;
            visibleRows = 8;
        } else {
            columns = 6;
            visibleRows = 8;
        }

        visibleSlots = columns * visibleRows;
        calculateDimensions();
    }

    public void open(int playerInventoryX, int playerInventoryY) {
        isOpen = true;
        localMode = false;
        linkedVault = null;

        configureLayoutForChestSize(10000);
        loadFromSaveManager();

        this.x = playerInventoryX - width - 20;
        this.y = playerInventoryY;

        if (this.x < 10) this.x = 10;

        scrollOffset = 0;
        updateMaxScroll();

        Log.d(TAG, "Opened with " + slots.size() + " items");
    }

    public void openLocal(int playerInventoryX, int playerInventoryY, VaultEntity vault) {
        isOpen = true;
        localMode = true;
        linkedVault = vault;

        int chestMaxSlots = vault != null ? vault.getMaxLocalSlots() : 48;
        configureLayoutForChestSize(chestMaxSlots);

        loadFromLocalVault();

        this.x = playerInventoryX - width - 20;
        this.y = playerInventoryY;

        if (this.x < 10) this.x = 10;

        scrollOffset = 0;
        updateMaxScroll();

        String chestType = vault != null ? vault.getVaultType().getDisplayName() : "Storage Chest";
        Log.d(TAG, "Opened " + chestType + " (" + chestMaxSlots + " slots) with " + slots.size() + " items");
    }

    private void loadFromLocalVault() {
        slots.clear();
        // --- Uncomment when SaveManager.SavedItem is available ---
        // if (linkedVault != null) {
        //     for (SavedItem saved : linkedVault.getLocalItems()) {
        //         slots.add(new VaultSlot(saved.itemId, saved.stackCount));
        //     }
        // }
        updateMaxScroll();
    }

    public void close() {
        isOpen = false;
        draggedSlot = null;
        Log.d(TAG, "Closed");
    }

    public void loadFromSaveManager() {
        slots.clear();
        // --- Uncomment when SaveManager is ported ---
        // List<SavedItem> savedItems = SaveManager.getInstance().getVaultItems();
        // for (SavedItem saved : savedItems) {
        //     slots.add(new VaultSlot(saved.itemId, saved.stackCount));
        // }
        updateMaxScroll();
    }

    public int addItem(String itemId, int count) {
        int overflow = 0;
        // --- Uncomment when SaveManager is ported ---
        // if (localMode && linkedVault != null) {
        //     overflow = linkedVault.addLocalItem(itemId, count);
        //     loadFromLocalVault();
        // } else {
        //     overflow = SaveManager.getInstance().addItemToVault(itemId, count);
        //     loadFromSaveManager();
        // }

        // Temporary: add directly to in-memory slots
        // Try stacking first
        for (VaultSlot slot : slots) {
            if (slot.itemId != null && slot.itemId.equals(itemId) && slot.stackCount < 16) {
                int canAdd = 16 - slot.stackCount;
                int toAdd = Math.min(canAdd, count);
                slot.stackCount += toAdd;
                count -= toAdd;
                if (count <= 0) return 0;
            }
        }
        // Add new slot
        if (slots.size() < maxSlots && count > 0) {
            slots.add(new VaultSlot(itemId, count));
            updateMaxScroll();
            return 0;
        }

        return count; // overflow
    }

    public boolean addItemEntity(ItemEntity item) {
        String itemId = resolveItemId(item);
        if (itemId == null || itemId.isEmpty()) {
            Log.e(TAG, "Cannot add item - no valid ID");
            return false;
        }
        int overflow = addItem(itemId, item.getStackCount());
        return overflow == 0;
    }

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

    public ItemEntity removeItem(int slotIndex, int count) {
        if (slotIndex < 0 || slotIndex >= slots.size()) return null;

        VaultSlot slot = slots.get(slotIndex);
        if (slot.isEmpty()) return null;

        String removedId = slot.itemId;
        int removedCount;

        if (count < 0 || count >= slot.stackCount) {
            removedCount = slot.stackCount;
            slots.remove(slotIndex);
        } else {
            removedCount = count;
            slot.stackCount -= count;
            if (slot.stackCount <= 0) {
                slots.remove(slotIndex);
            }
        }

        updateMaxScroll();

        ItemEntity item = new ItemEntity(0, 0, removedId);
        Item linkedItem = ItemRegistry.create(removedId);
        if (linkedItem != null) {
            item.setLinkedItem(linkedItem);
        }
        item.setStackCount(removedCount);
        return item;
    }

    private void updateMaxScroll() {
        int totalRows = (int) Math.ceil((double) Math.max(slots.size(), maxSlots) / columns);
        maxScrollRows = Math.max(0, totalRows - visibleRows);
    }

    public void handleScroll(int direction) {
        if (!isOpen) return;
        scrollOffset += direction;
        scrollOffset = Math.max(0, Math.min(maxScrollRows, scrollOffset));
    }

    public int handleClick(int mouseX, int mouseY, boolean isRightClick) {
        if (!isOpen) return -1;
        if (isDragging) return -1;

        if (handleSortButtonClick(mouseX, mouseY)) return -1;

        int slotIndex = getSlotAtPosition(mouseX, mouseY);
        if (slotIndex >= 0 && slotIndex < slots.size()) {
            VaultSlot slot = slots.get(slotIndex);
            if (!slot.isEmpty()) {
                if (isRightClick) {
                    ItemEntity taken = removeItem(slotIndex, 1);
                    if (taken != null && itemTakenCallback != null) {
                        String itemId = taken.getItemId();
                        if (itemId == null || itemId.isEmpty()) {
                            itemId = ItemRegistry.findIdByName(taken.getItemName());
                        }
                        if (itemId != null) {
                            itemTakenCallback.onItemTaken(itemId, 1);
                        }
                    }
                }
                return slotIndex;
            }
        }

        if (isScrollbarClick(mouseX, mouseY)) {
            handleScrollbarClick(mouseY);
            return -1;
        }

        return -1;
    }

    public boolean handleMousePressed(int mouseX, int mouseY) {
        if (!isOpen) return false;

        int slotIndex = getSlotAtPosition(mouseX, mouseY);
        if (slotIndex >= 0 && slotIndex < slots.size()) {
            VaultSlot slot = slots.get(slotIndex);
            if (!slot.isEmpty()) {
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

    public void handleMouseDragged(int mouseX, int mouseY) {
        if (isDragging) {
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }
    }

    public boolean handleMouseReleased(int mouseX, int mouseY) {
        if (!isDragging || draggedSlot == null) {
            isDragging = false;
            draggedSlot = null;
            draggedSlotIndex = -1;
            return false;
        }

        boolean transferred = false;

        if (!containsPoint(mouseX, mouseY)) {
            if (inventoryDropCallback != null && inventoryDropCallback.isPointInInventory(mouseX, mouseY)) {
                if (inventoryDropCallback.onDropToInventory(draggedSlot.itemId, draggedSlot.stackCount, mouseX, mouseY)) {
                    removeFromVaultStorage(draggedSlotIndex);
                    transferred = true;
                    Log.d(TAG, "Transferred " + draggedSlot.itemId + " to inventory");
                }
            } else {
                if (itemTakenCallback != null) {
                    itemTakenCallback.onItemTaken(draggedSlot.itemId, draggedSlot.stackCount);
                    removeFromVaultStorage(draggedSlotIndex);
                    transferred = true;
                }
            }
        }

        isDragging = false;
        draggedSlot = null;
        draggedSlotIndex = -1;

        return transferred;
    }

    private void removeFromVaultStorage(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < slots.size()) {
            slots.remove(slotIndex);
            updateMaxScroll();
        }
    }

    public void setInventoryDropCallback(InventoryDropCallback callback) {
        this.inventoryDropCallback = callback;
    }

    public boolean isDragging() { return isDragging; }

    private int getSlotAtPosition(int mouseX, int mouseY) {
        int gridX = x + SLOT_PADDING;
        int gridY = y + 40;
        int gridWidth = columns * (SLOT_SIZE + SLOT_PADDING);
        int gridHeight = visibleRows * (SLOT_SIZE + SLOT_PADDING);

        if (mouseX < gridX || mouseX >= gridX + gridWidth) return -1;
        if (mouseY < gridY || mouseY >= gridY + gridHeight) return -1;

        int col = (mouseX - gridX) / (SLOT_SIZE + SLOT_PADDING);
        int row = (mouseY - gridY) / (SLOT_SIZE + SLOT_PADDING);

        if (col < 0 || col >= columns) return -1;
        if (row < 0 || row >= visibleRows) return -1;

        return (scrollOffset + row) * columns + col;
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

    public void updateMousePosition(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        hoveredSlotIndex = getSlotAtPosition(mouseX, mouseY);
    }

    public int getHoveredSlotIndex() { return hoveredSlotIndex; }

    public boolean handleEquipKey() {
        if (!isOpen || hoveredSlotIndex < 0 || hoveredSlotIndex >= slots.size()) return false;

        VaultSlot slot = slots.get(hoveredSlotIndex);
        if (slot.isEmpty()) return false;

        ItemEntity taken = removeItem(hoveredSlotIndex, -1);
        if (taken != null && itemTakenCallback != null) {
            String itemId = taken.getItemId();
            if (itemId == null || itemId.isEmpty()) {
                itemId = ItemRegistry.findIdByName(taken.getItemName());
            }
            if (itemId != null) {
                itemTakenCallback.onItemTaken(itemId, taken.getStackCount());
                return true;
            }
        }
        return false;
    }

    // ==================== Sorting ====================

    public void sortByRarity() {
        if (slots.isEmpty()) return;

        if (currentSortMode == SORT_MODE_RARITY_DESC) {
            currentSortMode = SORT_MODE_RARITY_ASC;
            slots.sort((a, b) -> {
                if (a.isEmpty() && b.isEmpty()) return 0;
                if (a.isEmpty()) return 1;
                if (b.isEmpty()) return -1;
                int rarityA = a.itemTemplate != null ? a.itemTemplate.getRarity().ordinal() : 0;
                int rarityB = b.itemTemplate != null ? b.itemTemplate.getRarity().ordinal() : 0;
                return Integer.compare(rarityA, rarityB);
            });
        } else {
            currentSortMode = SORT_MODE_RARITY_DESC;
            slots.sort((a, b) -> {
                if (a.isEmpty() && b.isEmpty()) return 0;
                if (a.isEmpty()) return 1;
                if (b.isEmpty()) return -1;
                int rarityA = a.itemTemplate != null ? a.itemTemplate.getRarity().ordinal() : 0;
                int rarityB = b.itemTemplate != null ? b.itemTemplate.getRarity().ordinal() : 0;
                return Integer.compare(rarityB, rarityA);
            });
        }

        scrollOffset = 0;
    }

    public void sortAlphabetically() {
        if (slots.isEmpty()) return;

        currentSortMode = SORT_MODE_ALPHABETICAL;
        slots.sort((a, b) -> {
            if (a.isEmpty() && b.isEmpty()) return 0;
            if (a.isEmpty()) return 1;
            if (b.isEmpty()) return -1;
            String nameA = a.itemTemplate != null ? a.itemTemplate.getName() : "";
            String nameB = b.itemTemplate != null ? b.itemTemplate.getName() : "";
            return nameA.compareToIgnoreCase(nameB);
        });

        scrollOffset = 0;
    }

    public int getCurrentSortMode() { return currentSortMode; }

    public boolean handleSortButtonClick(int mouseX, int mouseY) {
        if (!isOpen) return false;

        if (mouseX >= sortRarityBtnX && mouseX <= sortRarityBtnX + sortRarityBtnW &&
            mouseY >= sortRarityBtnY && mouseY <= sortRarityBtnY + sortRarityBtnH) {
            sortByRarity();
            return true;
        }

        if (mouseX >= sortAlphaBtnX && mouseX <= sortAlphaBtnX + sortAlphaBtnBtnW &&
            mouseY >= sortAlphaBtnY && mouseY <= sortAlphaBtnY + sortAlphaBtnH) {
            sortAlphabetically();
            return true;
        }

        return false;
    }

    // ==================== Drawing ====================

    public void draw(Canvas canvas) {
        if (!isOpen) return;

        drawBackground(canvas);
        drawTitle(canvas);
        drawSlots(canvas);
        drawScrollbar(canvas);

        if (hoveredSlotIndex >= 0 && hoveredSlotIndex < slots.size()) {
            drawTooltip(canvas, slots.get(hoveredSlotIndex));
        }
    }

    public void drawDraggedItemOverlay(Canvas canvas) {
        if (!isOpen || draggedSlot == null) return;
        drawDraggedItem(canvas);
    }

    private void drawBackground(Canvas canvas) {
        fillPaint.setColor(Color.argb(230, 30, 30, 50));
        rectF.set(x, y, x + width, y + height);
        canvas.drawRoundRect(rectF, 12, 12, fillPaint);

        strokePaint.setColor(Color.rgb(255, 215, 0));
        strokePaint.setStrokeWidth(2);
        canvas.drawRoundRect(rectF, 12, 12, strokePaint);
    }

    private void drawTitle(Canvas canvas) {
        // Title background
        fillPaint.setColor(Color.rgb(60, 50, 30));
        canvas.drawRect(x + 2, y + 2, x + width - 2, y + 38, fillPaint);

        // Title text
        String title;
        if (localMode && linkedVault != null) {
            title = linkedVault.getVaultType().getDisplayName().toUpperCase();
        } else {
            title = "VAULT";
        }
        textPaint.setColor(Color.rgb(255, 215, 0));
        textPaint.setTextSize(16);
        textPaint.setFakeBoldText(true);
        float titleWidth = textPaint.measureText(title);
        canvas.drawText(title, x + (width - titleWidth) / 2f, y + 24, textPaint);
        textPaint.setFakeBoldText(false);

        // Slot count
        textPaint.setColor(Color.rgb(180, 180, 180));
        textPaint.setTextSize(11);
        String count = slots.size() + " / " + maxSlots;
        canvas.drawText(count, x + 8, y + 24, textPaint);

        // Sort buttons
        drawSortButtons(canvas);
    }

    private void drawSortButtons(Canvas canvas) {
        int btnHeight = 18;
        int btnPadding = 4;
        int btnY = y + 6;

        // Sort by rarity button
        String rarityLabel = currentSortMode == SORT_MODE_RARITY_ASC ? "Rarity ^" :
                             currentSortMode == SORT_MODE_RARITY_DESC ? "Rarity v" : "Rarity";
        textPaint.setTextSize(10);
        int rarityWidth = (int) textPaint.measureText(rarityLabel) + 8;
        sortRarityBtnW = rarityWidth;
        sortRarityBtnH = btnHeight;
        sortRarityBtnX = x + width - rarityWidth - btnPadding - SCROLLBAR_WIDTH;
        sortRarityBtnY = btnY;

        boolean rarityActive = currentSortMode == SORT_MODE_RARITY_ASC || currentSortMode == SORT_MODE_RARITY_DESC;
        fillPaint.setColor(rarityActive ? Color.rgb(100, 80, 40) : Color.rgb(70, 60, 50));
        rectF.set(sortRarityBtnX, sortRarityBtnY, sortRarityBtnX + sortRarityBtnW, sortRarityBtnY + sortRarityBtnH);
        canvas.drawRoundRect(rectF, 4, 4, fillPaint);
        strokePaint.setColor(Color.rgb(200, 180, 100));
        strokePaint.setStrokeWidth(1);
        canvas.drawRoundRect(rectF, 4, 4, strokePaint);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(rarityLabel, sortRarityBtnX + 4, sortRarityBtnY + 13, textPaint);

        // Sort alphabetically button
        String alphaLabel = currentSortMode == SORT_MODE_ALPHABETICAL ? "A-Z *" : "A-Z";
        int alphaWidth = (int) textPaint.measureText(alphaLabel) + 8;
        sortAlphaBtnBtnW = alphaWidth;
        sortAlphaBtnH = btnHeight;
        sortAlphaBtnX = sortRarityBtnX - alphaWidth - btnPadding;
        sortAlphaBtnY = btnY;

        boolean alphaActive = currentSortMode == SORT_MODE_ALPHABETICAL;
        fillPaint.setColor(alphaActive ? Color.rgb(40, 80, 100) : Color.rgb(50, 60, 70));
        rectF.set(sortAlphaBtnX, sortAlphaBtnY, sortAlphaBtnX + sortAlphaBtnBtnW, sortAlphaBtnY + sortAlphaBtnH);
        canvas.drawRoundRect(rectF, 4, 4, fillPaint);
        strokePaint.setColor(Color.rgb(100, 180, 200));
        strokePaint.setStrokeWidth(1);
        canvas.drawRoundRect(rectF, 4, 4, strokePaint);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(alphaLabel, sortAlphaBtnX + 4, sortAlphaBtnY + 13, textPaint);
    }

    private void drawSlots(Canvas canvas) {
        int gridY = y + 40;

        int startIndex = scrollOffset * columns;
        int endIndex = Math.min(startIndex + visibleSlots, Math.max(slots.size(), maxSlots));

        for (int i = 0; i < visibleSlots; i++) {
            int slotIndex = startIndex + i;
            if (slotIndex >= maxSlots) break;
            int col = i % columns;
            int row = i / columns;

            int slotX = x + SLOT_PADDING + col * (SLOT_SIZE + SLOT_PADDING);
            int slotY = gridY + row * (SLOT_SIZE + SLOT_PADDING);

            // Slot background
            boolean isHovered = (slotIndex == hoveredSlotIndex);
            fillPaint.setColor(isHovered ? Color.rgb(80, 80, 100) : Color.rgb(50, 50, 70));
            rectF.set(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE);
            canvas.drawRoundRect(rectF, 6, 6, fillPaint);

            // Slot border
            strokePaint.setColor(Color.rgb(100, 100, 120));
            strokePaint.setStrokeWidth(1);
            canvas.drawRoundRect(rectF, 6, 6, strokePaint);

            // Draw item
            if (slotIndex < slots.size()) {
                VaultSlot slot = slots.get(slotIndex);
                if (!slot.isEmpty()) {
                    drawSlotItem(canvas, slot, slotX, slotY);
                }
            }
        }
    }

    private void drawSlotItem(Canvas canvas, VaultSlot slot, int slotX, int slotY) {
        // Rarity background glow
        if (slot.itemTemplate != null) {
            int rarityColor = slot.itemTemplate.getRarity().getColor();
            fillPaint.setColor(Color.argb(60, Color.red(rarityColor), Color.green(rarityColor), Color.blue(rarityColor)));
            rectF.set(slotX + 2, slotY + 2, slotX + SLOT_SIZE - 2, slotY + SLOT_SIZE - 2);
            canvas.drawRoundRect(rectF, 4, 4, fillPaint);
        }

        // Item icon
        if (slot.icon != null) {
            int iconSize = SLOT_SIZE - 8;
            fillPaint.setColor(Color.WHITE);
            fillPaint.setAlpha(255);
            canvas.drawBitmap(slot.icon, null,
                    new RectF(slotX + 4, slotY + 4, slotX + 4 + iconSize, slotY + 4 + iconSize), fillPaint);
        } else {
            fillPaint.setColor(Color.GRAY);
            canvas.drawRect(slotX + 8, slotY + 8, slotX + SLOT_SIZE - 8, slotY + SLOT_SIZE - 8, fillPaint);
        }

        // Stack count
        if (slot.stackCount > 1) {
            String countStr = String.valueOf(slot.stackCount);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(11);
            textPaint.setFakeBoldText(true);
            float countWidth = textPaint.measureText(countStr);
            canvas.drawText(countStr, slotX + SLOT_SIZE - countWidth - 4, slotY + SLOT_SIZE - 4, textPaint);
            textPaint.setFakeBoldText(false);
        }
    }

    private void drawScrollbar(Canvas canvas) {
        if (maxScrollRows <= 0) return;

        int scrollbarX = x + width - SCROLLBAR_WIDTH + 2;
        int scrollbarY = y + 42;
        int scrollbarHeight = height - 46;

        // Background
        fillPaint.setColor(Color.rgb(40, 40, 60));
        rectF.set(scrollbarX, scrollbarY, scrollbarX + SCROLLBAR_WIDTH - 4, scrollbarY + scrollbarHeight);
        canvas.drawRoundRect(rectF, 4, 4, fillPaint);

        // Handle
        float handleHeight = Math.max(20, scrollbarHeight * ((float) visibleRows / (maxScrollRows + visibleRows)));
        float handleY = scrollbarY + (scrollbarHeight - handleHeight) * ((float) scrollOffset / maxScrollRows);

        fillPaint.setColor(Color.argb(180, 255, 215, 0));
        rectF.set(scrollbarX + 2, handleY, scrollbarX + SCROLLBAR_WIDTH - 6, handleY + handleHeight);
        canvas.drawRoundRect(rectF, 4, 4, fillPaint);
    }

    private void drawTooltip(Canvas canvas, VaultSlot slot) {
        if (slot.isEmpty() || slot.itemTemplate == null) return;

        Item item = slot.itemTemplate;
        String name = item.getName();
        String rarity = item.getRarity().getDisplayName();
        String category = item.getCategory().name();

        List<String> lines = new ArrayList<>();
        lines.add(name);
        lines.add(rarity + " " + category);
        lines.add("x" + slot.stackCount);

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

        int tooltipX = mouseX + 15;
        int tooltipY = mouseY - tooltipHeight / 2;

        if (tooltipX + tooltipWidth > x + width) tooltipX = mouseX - tooltipWidth - 10;
        if (tooltipY < 0) tooltipY = 5;
        if (tooltipY + tooltipHeight > screenHeight) tooltipY = screenHeight - tooltipHeight - 5;

        // Background
        fillPaint.setColor(Color.argb(240, 20, 20, 30));
        rectF.set(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight);
        canvas.drawRoundRect(rectF, 8, 8, fillPaint);

        // Border with rarity color
        int rarityColor = item.getRarity().getColor();
        strokePaint.setColor(rarityColor);
        strokePaint.setStrokeWidth(2);
        canvas.drawRoundRect(rectF, 8, 8, strokePaint);

        // Text
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

    private void drawDraggedItem(Canvas canvas) {
        if (draggedSlot == null || draggedSlot.icon == null) return;

        int size = SLOT_SIZE;
        canvas.drawBitmap(draggedSlot.icon, null,
                new RectF(mouseX - size / 2f + dragOffsetX, mouseY - size / 2f + dragOffsetY,
                           mouseX + size / 2f + dragOffsetX, mouseY + size / 2f + dragOffsetY),
                fillPaint);
    }

    // Getters and setters

    public boolean isOpen() { return isOpen; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getSlotCount() { return slots.size(); }
    public boolean isLocalMode() { return localMode; }
    public VaultEntity getLinkedVault() { return linkedVault; }

    public void setItemTakenCallback(ItemTakenCallback callback) {
        this.itemTakenCallback = callback;
    }

    public boolean containsPoint(int px, int py) {
        return isOpen && px >= x && px < x + width && py >= y && py < y + height;
    }
}
