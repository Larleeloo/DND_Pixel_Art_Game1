package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class Ice extends Item {

    public Ice() {
        super("Ice", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("A block of solid ice");
    }

    @Override
    public Item copy() {
        return new Ice();
    }
}
