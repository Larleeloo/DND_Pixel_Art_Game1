package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class PurpleShoes extends Item {

    public PurpleShoes() {
        super("Purple Shoes", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A pair of purple shoes");
    }

    @Override
    public Item copy() {
        return new PurpleShoes();
    }
}
