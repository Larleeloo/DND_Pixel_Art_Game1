package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class WhitePants extends Item {

    public WhitePants() {
        super("White Pants", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A pair of white pants");
    }

    @Override
    public Item copy() {
        return new WhitePants();
    }
}
