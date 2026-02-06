package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Magic Lantern - Never runs out of light.
 * Uncommon collectible item.
 */
public class MagicLantern extends Item {

    public MagicLantern() {
        super("Magic Lantern", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Never runs out of light");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new MagicLantern();
    }
}
