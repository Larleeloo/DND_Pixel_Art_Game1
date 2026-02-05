package com.ambermoongame.entity.item.items.collectibles;

import com.ambermoongame.entity.item.Item;

/**
 * Painting of a Dog - A loyal companion portrait.
 * Uncommon collectible item.
 */
public class PaintingDog extends Item {

    public PaintingDog() {
        super("Painting of a Dog", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A loyal companion portrait");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new PaintingDog();
    }
}
