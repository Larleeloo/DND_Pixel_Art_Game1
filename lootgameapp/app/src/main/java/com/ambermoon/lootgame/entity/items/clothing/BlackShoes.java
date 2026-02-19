package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class BlackShoes extends Item {

    public BlackShoes() {
        super("Black Shoes", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A pair of black shoes");
    }

    @Override
    public Item copy() {
        return new BlackShoes();
    }
}
