package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

public class FrogEgg extends Item {

    public FrogEgg() {
        super("Frog Egg", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A slimy frog egg");
    }

    @Override
    public Item copy() {
        return new FrogEgg();
    }
}
