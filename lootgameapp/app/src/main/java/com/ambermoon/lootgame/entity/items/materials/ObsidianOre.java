package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class ObsidianOre extends Item {

    public ObsidianOre() {
        super("Obsidian Ore", ItemCategory.MATERIAL);
        setRarity(ItemRarity.RARE);
        setDescription("A chunk of volcanic obsidian");
    }

    @Override
    public Item copy() {
        return new ObsidianOre();
    }
}
