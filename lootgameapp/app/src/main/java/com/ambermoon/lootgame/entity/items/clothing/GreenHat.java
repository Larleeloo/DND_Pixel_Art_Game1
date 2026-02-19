package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class GreenHat extends Item {

    public GreenHat() {
        super("Green Hat", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A fashionable green hat");
    }

    @Override
    public Item copy() {
        return new GreenHat();
    }
}
