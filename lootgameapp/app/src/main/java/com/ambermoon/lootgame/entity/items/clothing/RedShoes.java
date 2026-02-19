package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class RedShoes extends Item {

    public RedShoes() {
        super("Red Shoes", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A pair of red shoes");
    }

    @Override
    public Item copy() {
        return new RedShoes();
    }
}
