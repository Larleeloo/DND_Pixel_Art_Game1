package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class BlackHat extends Item {

    public BlackHat() {
        super("Black Hat", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A fashionable black hat");
    }

    @Override
    public Item copy() {
        return new BlackHat();
    }
}
