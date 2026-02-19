package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class RedShirt extends Item {

    public RedShirt() {
        super("Red Shirt", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A stylish red shirt");
    }

    @Override
    public Item copy() {
        return new RedShirt();
    }
}
