package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

public class Cap extends Item {

    public Cap() {
        super("Cap", ItemCategory.CLOTHING);
        setRarity(ItemRarity.COMMON);
        setDescription("A simple cloth cap");
    }

    @Override
    public Item copy() {
        return new Cap();
    }
}
