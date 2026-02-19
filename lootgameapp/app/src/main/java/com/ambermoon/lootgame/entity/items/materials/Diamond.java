package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class Diamond extends Item {

    public Diamond() {
        super("Diamond", ItemCategory.MATERIAL);
        setRarity(ItemRarity.RARE);
        setDescription("A dazzling precious diamond");
    }

    @Override
    public Item copy() {
        return new Diamond();
    }
}
