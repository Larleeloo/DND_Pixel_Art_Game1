package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class BlueDragonEgg extends Item {

    public BlueDragonEgg() {
        super("Blue Dragon Egg", ItemCategory.OTHER);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A blue dragon egg radiating with power");
        setSpecialEffect("Hatches a blue dragon");
    }

    @Override
    public Item copy() {
        return new BlueDragonEgg();
    }
}
