package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class GoldShirt extends Item {

    public GoldShirt() {
        super("Gold Shirt", ItemCategory.CLOTHING);
        setRarity(ItemRarity.RARE);
        setDescription("A shirt woven with golden thread");
    }

    @Override
    public Item copy() {
        return new GoldShirt();
    }
}
