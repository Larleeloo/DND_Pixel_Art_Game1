package com.ambermoongame.entity.item.items.collectibles;

import com.ambermoongame.entity.item.Item;

/**
 * Crucible - For melting and mixing metals.
 * Uncommon collectible item.
 */
public class Crucible extends Item {

    public Crucible() {
        super("Crucible", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("For melting and mixing metals");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new Crucible();
    }
}
