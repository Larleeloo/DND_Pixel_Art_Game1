package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class CollaredShirt extends Item {

    public CollaredShirt() {
        super("Collared Shirt", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A smartly collared shirt");
    }

    @Override
    public Item copy() {
        return new CollaredShirt();
    }
}
