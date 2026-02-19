package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class BlueHat extends Item {

    public BlueHat() {
        super("Blue Hat", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A fashionable blue hat");
    }

    @Override
    public Item copy() {
        return new BlueHat();
    }
}
