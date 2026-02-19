package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class GreenShirt extends Item {

    public GreenShirt() {
        super("Green Shirt", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A stylish green shirt");
    }

    @Override
    public Item copy() {
        return new GreenShirt();
    }
}
