package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class GoldRobe extends Item {

    public GoldRobe() {
        super("Gold Robe", ItemCategory.CLOTHING);
        setRarity(ItemRarity.RARE);
        setDescription("Robes woven with golden thread");
    }

    @Override
    public Item copy() {
        return new GoldRobe();
    }
}
