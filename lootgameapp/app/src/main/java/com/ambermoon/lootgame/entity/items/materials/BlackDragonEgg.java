package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class BlackDragonEgg extends Item {

    public BlackDragonEgg() {
        super("Black Dragon Egg", ItemCategory.OTHER);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A black dragon egg radiating with power");
        setSpecialEffect("Hatches a black dragon");
    }

    @Override
    public Item copy() {
        return new BlackDragonEgg();
    }
}
