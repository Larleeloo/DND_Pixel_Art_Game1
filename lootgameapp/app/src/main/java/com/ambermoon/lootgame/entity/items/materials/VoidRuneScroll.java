package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class VoidRuneScroll extends Item {

    public VoidRuneScroll() {
        super("Void Rune Scroll", ItemCategory.OTHER);
        setRarity(ItemRarity.EPIC);
        setDescription("A scroll inscribed with a void rune");
    }

    @Override
    public Item copy() {
        return new VoidRuneScroll();
    }
}
