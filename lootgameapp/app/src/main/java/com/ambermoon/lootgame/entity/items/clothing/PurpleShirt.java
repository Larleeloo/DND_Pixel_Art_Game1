package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class PurpleShirt extends Item {

    public PurpleShirt() {
        super("Purple Shirt", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A stylish purple shirt");
    }

    @Override
    public Item copy() {
        return new PurpleShirt();
    }
}
