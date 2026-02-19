package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class GoldPants extends Item {

    public GoldPants() {
        super("Gold Pants", ItemCategory.CLOTHING);
        setRarity(ItemRarity.RARE);
        setDescription("Pants woven with golden thread");
    }

    @Override
    public Item copy() {
        return new GoldPants();
    }
}
