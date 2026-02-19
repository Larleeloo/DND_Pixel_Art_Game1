package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class GoldHat extends Item {

    public GoldHat() {
        super("Gold Hat", ItemCategory.CLOTHING);
        setRarity(ItemRarity.RARE);
        setDescription("A hat trimmed with gold");
    }

    @Override
    public Item copy() {
        return new GoldHat();
    }
}
