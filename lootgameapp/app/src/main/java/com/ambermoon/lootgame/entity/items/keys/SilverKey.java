package com.ambermoon.lootgame.entity.items.keys;

import com.ambermoon.lootgame.entity.Item;

/**
 * Silver Key - Opens silver locks.
 * Uncommon key item.
 */
public class SilverKey extends Item {

    public SilverKey() {
        super("Silver Key", ItemCategory.KEY);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Opens silver locks");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new SilverKey();
    }
}
