package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class BlueShoes extends Item {

    public BlueShoes() {
        super("Blue Shoes", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A pair of blue shoes");
    }

    @Override
    public Item copy() {
        return new BlueShoes();
    }
}
