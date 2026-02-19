package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class WhiteDragonEgg extends Item {

    public WhiteDragonEgg() {
        super("White Dragon Egg", ItemCategory.OTHER);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A white dragon egg radiating with power");
        setSpecialEffect("Hatches a white dragon");
    }

    @Override
    public Item copy() {
        return new WhiteDragonEgg();
    }
}
