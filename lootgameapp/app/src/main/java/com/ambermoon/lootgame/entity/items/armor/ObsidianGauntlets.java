package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

public class ObsidianGauntlets extends Item {

    public ObsidianGauntlets() {
        super("Obsidian Gauntlets", ItemCategory.ARMOR);
        setRarity(ItemRarity.EPIC);
        setDescription("Gauntlets forged from obsidian");
        setDefense(12);
    }

    @Override
    public Item copy() {
        return new ObsidianGauntlets();
    }
}
