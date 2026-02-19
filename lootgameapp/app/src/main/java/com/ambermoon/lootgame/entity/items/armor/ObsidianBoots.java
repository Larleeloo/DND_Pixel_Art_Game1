package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

public class ObsidianBoots extends Item {

    public ObsidianBoots() {
        super("Obsidian Boots", ItemCategory.ARMOR);
        setRarity(ItemRarity.EPIC);
        setDescription("Boots forged from obsidian");
        setDefense(14);
    }

    @Override
    public Item copy() {
        return new ObsidianBoots();
    }
}
