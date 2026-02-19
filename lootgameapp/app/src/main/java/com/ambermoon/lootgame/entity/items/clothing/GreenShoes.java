package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class GreenShoes extends Item {

    public GreenShoes() {
        super("Green Shoes", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A pair of green shoes");
    }

    @Override
    public Item copy() {
        return new GreenShoes();
    }
}
