package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class BlackRobe extends Item {

    public BlackRobe() {
        super("Black Robe", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Flowing black robes");
    }

    @Override
    public Item copy() {
        return new BlackRobe();
    }
}
