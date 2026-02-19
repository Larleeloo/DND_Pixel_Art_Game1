package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class VoidScroll extends Item {

    public VoidScroll() {
        super("Void Scroll", ItemCategory.OTHER);
        setRarity(ItemRarity.EPIC);
        setDescription("A scroll infused with void energy");
    }

    @Override
    public Item copy() {
        return new VoidScroll();
    }
}
