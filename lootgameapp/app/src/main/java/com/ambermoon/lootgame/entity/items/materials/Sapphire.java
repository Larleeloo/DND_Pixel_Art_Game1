package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class Sapphire extends Item {

    public Sapphire() {
        super("Sapphire", ItemCategory.MATERIAL);
        setRarity(ItemRarity.RARE);
        setDescription("A deep blue gemstone");
    }

    @Override
    public Item copy() {
        return new Sapphire();
    }
}
