package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class GreenDragonEgg extends Item {

    public GreenDragonEgg() {
        super("Green Dragon Egg", ItemCategory.OTHER);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A green dragon egg radiating with power");
        setSpecialEffect("Hatches a green dragon");
    }

    @Override
    public Item copy() {
        return new GreenDragonEgg();
    }
}
