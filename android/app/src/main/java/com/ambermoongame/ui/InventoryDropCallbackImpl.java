package com.ambermoongame.ui;

import com.ambermoongame.entity.item.ItemEntity;
import com.ambermoongame.entity.item.ItemRegistry;

/**
 * Top-level callback class to avoid D8 compiler crash on inner classes implementing interfaces.
 */
public class InventoryDropCallbackImpl implements VaultInventory.InventoryDropCallback {

    private final Inventory inventory;

    public InventoryDropCallbackImpl(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public boolean onDropToInventory(String itemId, int count, int dropX, int dropY) {
        ItemEntity item = new ItemEntity(0, 0, itemId);
        item.setLinkedItem(ItemRegistry.create(itemId));
        item.setStackCount(count);

        int targetSlot = inventory.getSlotAtPosition(dropX, dropY);
        if (targetSlot < 0) {
            targetSlot = inventory.getNearestSlotToPosition(dropX, dropY);
        }

        if (targetSlot >= 0) {
            return inventory.addItemToSlot(item, targetSlot);
        }
        return inventory.addItem(item);
    }

    @Override
    public boolean isPointInInventory(int px, int py) {
        return inventory.containsPoint(px, py);
    }
}
