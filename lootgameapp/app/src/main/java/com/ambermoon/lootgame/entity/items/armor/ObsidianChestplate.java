package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

public class ObsidianChestplate extends Item {

    public ObsidianChestplate() {
        super("Obsidian Chestplate", ItemCategory.ARMOR);
        setRarity(ItemRarity.EPIC);
        setDescription("A chestplate forged from obsidian");
        setDefense(22);
    }

    @Override
    public Item copy() {
        return new ObsidianChestplate();
    }
}
