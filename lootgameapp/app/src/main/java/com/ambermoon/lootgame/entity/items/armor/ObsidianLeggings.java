package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

public class ObsidianLeggings extends Item {

    public ObsidianLeggings() {
        super("Obsidian Leggings", ItemCategory.ARMOR);
        setRarity(ItemRarity.EPIC);
        setDescription("Leggings forged from obsidian");
        setDefense(18);
    }

    @Override
    public Item copy() {
        return new ObsidianLeggings();
    }
}
