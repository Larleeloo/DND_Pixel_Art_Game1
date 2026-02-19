package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Undead Scroll - Contains necromantic knowledge.
 * Rare collectible item.
 */
public class UndeadScroll extends Item {

    public UndeadScroll() {
        super("Undead Scroll", ItemCategory.OTHER);
        setRarity(ItemRarity.EPIC);
        setDescription("Contains necromantic knowledge");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new UndeadScroll();
    }
}
