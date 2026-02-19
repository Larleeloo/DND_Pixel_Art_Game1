package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class WhiteRobe extends Item {

    public WhiteRobe() {
        super("White Robe", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Flowing white robes");
    }

    @Override
    public Item copy() {
        return new WhiteRobe();
    }
}
