package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class BlankScroll extends Item {

    public BlankScroll() {
        super("Blank Scroll", ItemCategory.MATERIAL);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("An unused scroll ready for inscription");
    }

    @Override
    public Item copy() {
        return new BlankScroll();
    }
}
