package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class ScrollOfPoisonRune extends Item {

    public ScrollOfPoisonRune() {
        super("Scroll of Poison Rune", ItemCategory.OTHER);
        setRarity(ItemRarity.RARE);
        setDescription("A scroll inscribed with a poison rune");
    }

    @Override
    public Item copy() {
        return new ScrollOfPoisonRune();
    }
}
