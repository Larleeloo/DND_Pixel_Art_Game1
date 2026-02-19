package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class GoldDress extends Item {

    public GoldDress() {
        super("Gold Dress", ItemCategory.CLOTHING);
        setRarity(ItemRarity.RARE);
        setDescription("An elegant golden dress");
    }

    @Override
    public Item copy() {
        return new GoldDress();
    }
}
