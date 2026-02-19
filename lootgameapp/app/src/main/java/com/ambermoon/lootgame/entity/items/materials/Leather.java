package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class Leather extends Item {

    public Leather() {
        super("Leather", ItemCategory.MATERIAL);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A piece of tanned leather");
    }

    @Override
    public Item copy() {
        return new Leather();
    }
}
