package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class BlueRobe extends Item {

    public BlueRobe() {
        super("Blue Robe", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Flowing blue robes");
    }

    @Override
    public Item copy() {
        return new BlueRobe();
    }
}
