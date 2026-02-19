package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class WhiteShoes extends Item {

    public WhiteShoes() {
        super("White Shoes", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A pair of white shoes");
    }

    @Override
    public Item copy() {
        return new WhiteShoes();
    }
}
