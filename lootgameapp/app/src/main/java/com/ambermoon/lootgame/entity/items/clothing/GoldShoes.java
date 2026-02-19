package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class GoldShoes extends Item {

    public GoldShoes() {
        super("Gold Shoes", ItemCategory.CLOTHING);
        setRarity(ItemRarity.RARE);
        setDescription("Shoes adorned with gold");
    }

    @Override
    public Item copy() {
        return new GoldShoes();
    }
}
