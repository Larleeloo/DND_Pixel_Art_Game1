package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class WornShoes extends Item {

    public WornShoes() {
        super("Worn Shoes", ItemCategory.CLOTHING);
        setRarity(ItemRarity.COMMON);
        setDescription("A pair of well-worn shoes");
    }

    @Override
    public Item copy() {
        return new WornShoes();
    }
}
