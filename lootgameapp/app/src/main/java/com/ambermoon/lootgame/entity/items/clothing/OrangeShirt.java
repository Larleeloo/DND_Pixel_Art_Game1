package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class OrangeShirt extends Item {

    public OrangeShirt() {
        super("Orange Shirt", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A stylish orange shirt");
    }

    @Override
    public Item copy() {
        return new OrangeShirt();
    }
}
