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
import com.ambermoongame.entity.item.RecipeManager;
import com.ambermoongame.entity.item.RecipeManager.Recipe;

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
 * Android conversion notes:
 * - Graphics2D → Canvas + Paint
 * - BufferedImage → Bitmap
 * - Font/FontMetrics → Paint.setTextSize()/measureText()
 * - java.awt.Color → Android int color
 * - BasicStroke → Paint.setStrokeWidth()
 * - fillPolygon → Canvas.drawPath()
 * - Hardcoded screen dimensions → dynamic screenWidth/screenHeight
 */
public class AlchemyTableUI {

    private static final String TAG = "AlchemyTableUI";

    // Layout constants
    private static final int SLOT_SIZE = 56;
    private static final int SLOT_PADDING = 8;
    private static final int PANEL_PADDING = 20;

    // UI dimensions
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
    private int dragSourceIndex = -1;
    private int mouseX, mouseY;
    private boolean isDragging = false;

    // Hover state
    private int hoveredSlotIndex = -1;

    // Colors (Android int colors)
    private final int panelBackground = Color.argb(240, 40, 35, 50);
    private final int slotBackground = Color.rgb(60, 55, 70);
    private final int slotHover = Color.rgb(80, 75, 95);
    private final int slotBorder = Color.rgb(100, 95, 120);
    private final int accentColor = Color.rgb(100, 220, 150);  // Green for alchemy
    private final int titleColor = Color.rgb(150, 255, 180);

    // Callbacks
    private ItemConsumedCallback itemConsumedCallback;
    private ItemProducedCallback itemProducedCallback;
    private Runnable onCloseCallback;

    // Reusable drawing objects
    private final Paint fillPaint;
    private final Paint strokePaint;
    private final Paint textPaint;
    private final RectF rectF;
    private final Path arrowPath;

    /**
     * Represents a single crafting slot.
     */
    public static class CraftingSlot {
        public String itemId;
        public int stackCount;
        public Item itemTemplate;
        public Bitmap icon;
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

    public interface ItemConsumedCallback {
        void onItemConsumed(String itemId, int count);
    }

    public interface ItemProducedCallback {
        void onItemProduced(String itemId, int count);
    }

    public AlchemyTableUI() {
        for (int i = 0; i < 3; i++) {
            inputSlots[i] = new CraftingSlot();
        }
        outputSlot = new CraftingSlot();

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
        this.arrowPath = new Path();
    }

    public AlchemyTableUI(boolean ignored) {
        this();
    }

    private void calculateDimensions() {
        width = 4 * SLOT_SIZE + 5 * SLOT_PADDING + 40 + PANEL_PADDING * 2;
        height = SLOT_SIZE + PANEL_PADDING * 2 + 50;
    }

    /**
     * Opens the UI positioned to the right of the inventory.
     */
    public void open(int screenWidth, int screenHeight) {
        int inventoryWidth = 8 * (60 + 8) + 8;
        int inventoryX = (screenWidth - inventoryWidth) / 2;
        int inventoryY = 150;

        this.x = inventoryX + inventoryWidth + 16;
        this.y = inventoryY + 20;

        // Position slots
        int slotY = y + PANEL_PADDING + 40;
        int slotX = x + PANEL_PADDING;

        for (int i = 0; i < 3; i++) {
            inputSlots[i].x = slotX + i * (SLOT_SIZE + SLOT_PADDING);
            inputSlots[i].y = slotY;
        }

        outputSlot.x = slotX + 3 * (SLOT_SIZE + SLOT_PADDING) + 40;
        outputSlot.y = slotY;

        isOpen = true;
        Log.d(TAG, "Opened");
    }

    public void close() {
        if (!isOpen) return;

        for (CraftingSlot slot : inputSlots) {
            if (!slot.isEmpty() && itemProducedCallback != null) {
                itemProducedCallback.onItemProduced(slot.itemId, slot.stackCount);
            }
            slot.clear();
        }

        outputSlot.clear();
        currentRecipe = null;

        isOpen = false;
        isDragging = false;

        if (onCloseCallback != null) {
            onCloseCallback.run();
        }

        Log.d(TAG, "Closed");
    }

    public boolean isOpen() { return isOpen; }

    public void update(int mouseX, int mouseY) {
        if (!isOpen) return;

        this.mouseX = mouseX;
        this.mouseY = mouseY;

        hoveredSlotIndex = getSlotAtPosition(mouseX, mouseY);
    }

    public boolean handleMousePressed(int mouseX, int mouseY) {
        if (!isOpen) return false;

        int slotIndex = getSlotAtPosition(mouseX, mouseY);
        if (slotIndex == -1) return false;

        CraftingSlot slot = getSlotByIndex(slotIndex);
        if (slot != null && !slot.isEmpty()) {
            if (slotIndex == 3) {
                if (currentRecipe != null) {
                    takeOutput();
                    return true;
                }
            } else {
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

    public void handleMouseDragged(int mouseX, int mouseY) {
        if (isDragging) {
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }
    }

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
            droppedItem = new ItemEntity(mouseX, mouseY, draggedSlot.itemId);
            droppedItem.setStackCount(draggedSlot.stackCount);
            Item linked = ItemRegistry.create(draggedSlot.itemId);
            if (linked != null) {
                droppedItem.setLinkedItem(linked);
            }

            inputSlots[dragSourceIndex].clear();
            updateRecipe();
        } else if (targetSlot >= 0 && targetSlot < 3 && targetSlot != dragSourceIndex) {
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

        isDragging = false;
        draggedSlot = null;
        dragSourceIndex = -1;

        return droppedItem;
    }

    public boolean addItem(String itemId, int count) {
        if (!isOpen || itemId == null || itemId.isEmpty()) return false;

        for (int i = 0; i < 3; i++) {
            if (inputSlots[i].isEmpty()) {
                inputSlots[i].setItem(itemId, count);
                updateRecipe();
                return true;
            }
        }

        return false;
    }

    public boolean addItemToSlot(int slotIndex, String itemId, int count) {
        if (!isOpen || slotIndex < 0 || slotIndex >= 3) return false;

        CraftingSlot slot = inputSlots[slotIndex];

        if (!slot.isEmpty() && slot.itemId.equals(itemId)) {
            slot.stackCount += count;
            return true;
        }

        if (slot.isEmpty()) {
            slot.setItem(itemId, count);
            updateRecipe();
            return true;
        }

        return false;
    }

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
            Log.d(TAG, "Recipe found - " + recipe.name);
        } else {
            outputSlot.clear();
            currentRecipe = null;
        }
    }

    private void takeOutput() {
        if (currentRecipe == null || outputSlot.isEmpty()) return;

        if (itemProducedCallback != null) {
            itemProducedCallback.onItemProduced(currentRecipe.result, currentRecipe.resultCount);
        }

        for (CraftingSlot slot : inputSlots) {
            if (!slot.isEmpty()) {
                if (itemConsumedCallback != null) {
                    itemConsumedCallback.onItemConsumed(slot.itemId, 1);
                }
                slot.stackCount--;
                if (slot.stackCount <= 0) {
                    slot.clear();
                }
            }
        }

        updateRecipe();
    }

    // ==================== Drawing ====================

    public void draw(Canvas canvas) {
        if (!isOpen) return;

        drawBackground(canvas);
        drawTitle(canvas);

        for (int i = 0; i < 3; i++) {
            drawSlot(canvas, inputSlots[i], i, hoveredSlotIndex == i);
        }

        drawArrow(canvas);
        drawSlot(canvas, outputSlot, 3, hoveredSlotIndex == 3);

        if (hoveredSlotIndex >= 0) {
            CraftingSlot slot = getSlotByIndex(hoveredSlotIndex);
            if (slot != null && !slot.isEmpty()) {
                drawTooltip(canvas, slot);
            }
        }
    }

    public void drawDraggedItemOverlay(Canvas canvas) {
        if (!isOpen || !isDragging || draggedSlot == null) return;
        drawDraggedItem(canvas);
    }

    private void drawBackground(Canvas canvas) {
        fillPaint.setColor(panelBackground);
        rectF.set(x, y, x + width, y + height);
        canvas.drawRoundRect(rectF, 16, 16, fillPaint);

        strokePaint.setColor(accentColor);
        strokePaint.setStrokeWidth(2);
        canvas.drawRoundRect(rectF, 16, 16, strokePaint);
    }

    private void drawTitle(Canvas canvas) {
        // Title bar
        fillPaint.setColor(Color.argb(60, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor)));
        canvas.drawRect(x + 2, y + 2, x + width - 2, y + 34, fillPaint);

        // Title text
        String title = "ALCHEMY TABLE";
        textPaint.setColor(titleColor);
        textPaint.setTextSize(15);
        textPaint.setFakeBoldText(true);
        float titleWidth = textPaint.measureText(title);
        canvas.drawText(title, x + (width - titleWidth) / 2f, y + 22, textPaint);
        textPaint.setFakeBoldText(false);

        // Subtitle
        String subtitle = "Combine items to craft";
        textPaint.setColor(Color.rgb(180, 180, 180));
        textPaint.setTextSize(10);
        float subWidth = textPaint.measureText(subtitle);
        canvas.drawText(subtitle, x + (width - subWidth) / 2f, y + 36, textPaint);
    }

    private void drawSlot(Canvas canvas, CraftingSlot slot, int index, boolean isHovered) {
        int sx = slot.x;
        int sy = slot.y;

        // Slot background
        fillPaint.setColor(isHovered ? slotHover : slotBackground);
        rectF.set(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE);
        canvas.drawRoundRect(rectF, 8, 8, fillPaint);

        // Slot border
        int borderColor = (index == 3 && currentRecipe != null) ? accentColor : slotBorder;
        strokePaint.setColor(borderColor);
        strokePaint.setStrokeWidth(index == 3 ? 2 : 1);
        canvas.drawRoundRect(rectF, 8, 8, strokePaint);

        // Draw item
        if (!slot.isEmpty() && slot.icon != null) {
            if (!(isDragging && index < 3 && index == dragSourceIndex)) {
                // Rarity glow
                if (slot.itemTemplate != null) {
                    int rarityColor = slot.itemTemplate.getRarity().getColor();
                    fillPaint.setColor(Color.argb(40, Color.red(rarityColor), Color.green(rarityColor), Color.blue(rarityColor)));
                    rectF.set(sx + 2, sy + 2, sx + SLOT_SIZE - 2, sy + SLOT_SIZE - 2);
                    canvas.drawRoundRect(rectF, 6, 6, fillPaint);
                }

                // Item icon
                int iconSize = SLOT_SIZE - 12;
                fillPaint.setColor(Color.WHITE);
                fillPaint.setAlpha(255);
                canvas.drawBitmap(slot.icon, null,
                        new RectF(sx + 6, sy + 6, sx + 6 + iconSize, sy + 6 + iconSize), fillPaint);

                // Stack count
                if (slot.stackCount > 1) {
                    String count = String.valueOf(slot.stackCount);
                    textPaint.setColor(Color.WHITE);
                    textPaint.setTextSize(11);
                    textPaint.setFakeBoldText(true);
                    float countWidth = textPaint.measureText(count);
                    canvas.drawText(count, sx + SLOT_SIZE - countWidth - 4, sy + SLOT_SIZE - 4, textPaint);
                    textPaint.setFakeBoldText(false);
                }
            }
        }

        // Slot label for input slots
        if (index < 3) {
            textPaint.setColor(Color.rgb(120, 120, 140));
            textPaint.setTextSize(9);
            canvas.drawText(String.valueOf(index + 1), sx + 4, sy + SLOT_SIZE - 4, textPaint);
        }
    }

    private void drawArrow(Canvas canvas) {
        int arrowX = inputSlots[2].x + SLOT_SIZE + SLOT_PADDING + 10;
        int arrowY = inputSlots[0].y + SLOT_SIZE / 2;

        int arrowColor = currentRecipe != null ? accentColor : Color.rgb(100, 100, 100);

        // Arrow line
        strokePaint.setColor(arrowColor);
        strokePaint.setStrokeWidth(3);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        canvas.drawLine(arrowX, arrowY, arrowX + 20, arrowY, strokePaint);

        // Arrow head
        fillPaint.setColor(arrowColor);
        arrowPath.reset();
        arrowPath.moveTo(arrowX + 20, arrowY);
        arrowPath.lineTo(arrowX + 14, arrowY - 6);
        arrowPath.lineTo(arrowX + 14, arrowY + 6);
        arrowPath.close();
        canvas.drawPath(arrowPath, fillPaint);
    }

    private void drawTooltip(Canvas canvas, CraftingSlot slot) {
        if (slot.itemTemplate == null) return;

        Item item = slot.itemTemplate;
        String name = item.getName();
        String rarity = item.getRarity().getDisplayName();
        String category = item.getCategory().name();

        List<String> lines = new ArrayList<>();
        lines.add(name);
        lines.add(rarity + " " + category + " x" + slot.stackCount);

        if (item.getDamage() > 0) lines.add("Damage: " + item.getDamage());
        if (item.getDefense() > 0) lines.add("Defense: " + item.getDefense());
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

        int tx = mouseX + 15;
        int ty = mouseY - tooltipHeight / 2;

        if (tx + tooltipWidth > x + width) tx = mouseX - tooltipWidth - 10;
        if (ty < 0) ty = 5;
        if (ty + tooltipHeight > 1080) ty = 1080 - tooltipHeight - 5;

        // Background
        fillPaint.setColor(Color.argb(230, 20, 20, 30));
        rectF.set(tx, ty, tx + tooltipWidth, ty + tooltipHeight);
        canvas.drawRoundRect(rectF, 6, 6, fillPaint);

        // Border with rarity color
        int rarityColor = item.getRarity().getColor();
        strokePaint.setColor(rarityColor);
        strokePaint.setStrokeWidth(2);
        canvas.drawRoundRect(rectF, 6, 6, strokePaint);

        // Text
        int textY = ty + 16;
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
            canvas.drawText(lines.get(i), tx + 10, textY, textPaint);
            textY += lineHeight;
        }
    }

    private void drawDraggedItem(Canvas canvas) {
        if (draggedSlot == null || draggedSlot.icon == null) return;

        int size = SLOT_SIZE - 8;
        canvas.drawBitmap(draggedSlot.icon, null,
                new RectF(mouseX - size / 2f, mouseY - size / 2f,
                           mouseX + size / 2f, mouseY + size / 2f), fillPaint);
    }

    public boolean containsPoint(int px, int py) {
        return isOpen && px >= x && px < x + width && py >= y && py < y + height;
    }

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
