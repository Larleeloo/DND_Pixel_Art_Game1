package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class Gunpowder extends Item {

    public Gunpowder() {
        super("Gunpowder", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("Explosive black powder");
    }

    @Override
    public Item copy() {
        return new Gunpowder();
    }
}
