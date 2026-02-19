package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class Emerald extends Item {

    public Emerald() {
        super("Emerald", ItemCategory.MATERIAL);
        setRarity(ItemRarity.RARE);
        setDescription("A vivid green gemstone");
    }

    @Override
    public Item copy() {
        return new Emerald();
    }
}
