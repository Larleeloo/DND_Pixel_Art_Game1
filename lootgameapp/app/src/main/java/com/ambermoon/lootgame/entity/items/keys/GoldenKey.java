package com.ambermoon.lootgame.entity.items.keys;

import com.ambermoon.lootgame.entity.Item;

/**
 * Golden Key - Opens golden locks.
 * Rare key item.
 */
public class GoldenKey extends Item {

    public GoldenKey() {
        super("Golden Key", ItemCategory.KEY);
        setRarity(ItemRarity.RARE);
        setDescription("Opens golden locks");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new GoldenKey();
    }
}
