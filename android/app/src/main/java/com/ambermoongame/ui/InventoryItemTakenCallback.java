package com.ambermoongame.ui;

import android.util.Log;
import com.ambermoongame.entity.item.ItemEntity;
import com.ambermoongame.entity.item.ItemRegistry;

/**
 * Top-level callback class to avoid D8 compiler crash on inner classes implementing interfaces.
 */
public class InventoryItemTakenCallback implements VaultInventory.ItemTakenCallback {
    private static final String TAG = "InventoryItemTakenCallback";

    private final Inventory inventory;
    private final VaultInventory vaultInventory;

    public InventoryItemTakenCallback(Inventory inventory, VaultInventory vaultInventory) {
        this.inventory = inventory;
        this.vaultInventory = vaultInventory;
    }

    @Override
    public void onItemTaken(String itemId, int count) {
        ItemEntity item = new ItemEntity(0, 0, itemId);
        item.setLinkedItem(ItemRegistry.create(itemId));
        item.setStackCount(count);
        if (!inventory.addItemAtCursorSlot(item)) {
            vaultInventory.addItem(itemId, count);
            Log.d(TAG, "Inventory full, item returned to vault");
        }
    }
}
