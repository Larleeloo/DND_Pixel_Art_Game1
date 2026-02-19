package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class ScrollOfIceRune extends Item {

    public ScrollOfIceRune() {
        super("Scroll of Ice Rune", ItemCategory.OTHER);
        setRarity(ItemRarity.RARE);
        setDescription("A scroll inscribed with an ice rune");
    }

    @Override
    public Item copy() {
        return new ScrollOfIceRune();
    }
}
