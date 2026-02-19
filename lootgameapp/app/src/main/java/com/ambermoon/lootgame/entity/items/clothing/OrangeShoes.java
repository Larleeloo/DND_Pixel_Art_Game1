package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class OrangeShoes extends Item {

    public OrangeShoes() {
        super("Orange Shoes", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A pair of orange shoes");
    }

    @Override
    public Item copy() {
        return new OrangeShoes();
    }
}
