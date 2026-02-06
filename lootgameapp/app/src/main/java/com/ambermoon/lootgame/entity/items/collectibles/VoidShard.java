package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Void Shard - A fragment of the void itself.
 * Legendary collectible with phase ability.
 */
public class VoidShard extends Item {

    public VoidShard() {
        super("Void Shard", ItemCategory.OTHER);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A fragment of the void itself");
        setSpecialEffect("Phase through walls briefly");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new VoidShard();
    }
}
