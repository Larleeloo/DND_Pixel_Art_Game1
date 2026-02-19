package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class BlackShirt extends Item {

    public BlackShirt() {
        super("Black Shirt", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A stylish black shirt");
    }

    @Override
    public Item copy() {
        return new BlackShirt();
    }
}
