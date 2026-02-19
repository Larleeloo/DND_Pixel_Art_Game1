package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

public class ObsidianHelmet extends Item {

    public ObsidianHelmet() {
        super("Obsidian Helmet", ItemCategory.ARMOR);
        setRarity(ItemRarity.EPIC);
        setDescription("A helmet forged from obsidian");
        setDefense(16);
    }

    @Override
    public Item copy() {
        return new ObsidianHelmet();
    }
}
