package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class Cape extends Item {

    public Cape() {
        super("Cape", ItemCategory.CLOTHING);
        setRarity(ItemRarity.RARE);
        setDescription("A flowing cape");
    }

    @Override
    public Item copy() {
        return new Cape();
    }
}
