package com.ambermoongame.entity.item.items.collectibles;

import com.ambermoongame.entity.item.Item;

/**
 * Crystal (Summer Edition) - A crystal infused with summer's warmth.
 * Legendary collectible with fire immunity.
 */
public class CrystalSummer extends Item {

    public CrystalSummer() {
        super("Crystal (Summer Edition)", ItemCategory.OTHER);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A crystal infused with summer's warmth");
        setSpecialEffect("Fire damage immunity");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new CrystalSummer();
    }
}
