package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class RedDragonEgg extends Item {

    public RedDragonEgg() {
        super("Red Dragon Egg", ItemCategory.OTHER);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A red dragon egg radiating with power");
        setSpecialEffect("Hatches a red dragon");
    }

    @Override
    public Item copy() {
        return new RedDragonEgg();
    }
}
