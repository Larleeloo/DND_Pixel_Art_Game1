package com.ambermoongame.entity.item.items.armor;

import com.ambermoongame.entity.item.Item;

/**
 * Steel Chestplate - Better chest protection.
 * Uncommon armor piece with improved defense.
 */
public class SteelChestplate extends Item {

    public SteelChestplate() {
        super("Steel Chestplate", ItemCategory.ARMOR);
        setDefense(15);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Better chest protection");
    }

    @Override
    public Item copy() {
        return new SteelChestplate();
    }
}
