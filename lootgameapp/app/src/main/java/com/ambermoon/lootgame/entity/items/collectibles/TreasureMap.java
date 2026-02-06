package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Ancient Treasure Map - Marks the location of hidden riches.
 * Rare collectible that reveals secret areas.
 */
public class TreasureMap extends Item {

    public TreasureMap() {
        super("Ancient Treasure Map", ItemCategory.OTHER);
        setRarity(ItemRarity.RARE);
        setDescription("Marks the location of hidden riches");
        setSpecialEffect("Reveals secret areas");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new TreasureMap();
    }
}
