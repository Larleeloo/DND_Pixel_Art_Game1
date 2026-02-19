package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class BlueShirt extends Item {

    public BlueShirt() {
        super("Blue Shirt", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A stylish blue shirt");
    }

    @Override
    public Item copy() {
        return new BlueShirt();
    }
}
