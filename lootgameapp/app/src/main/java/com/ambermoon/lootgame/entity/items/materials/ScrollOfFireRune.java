package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class ScrollOfFireRune extends Item {

    public ScrollOfFireRune() {
        super("Scroll of Fire Rune", ItemCategory.OTHER);
        setRarity(ItemRarity.RARE);
        setDescription("A scroll inscribed with a fire rune");
    }

    @Override
    public Item copy() {
        return new ScrollOfFireRune();
    }
}
