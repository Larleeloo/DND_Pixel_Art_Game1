package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class ScrollOfPoison extends Item {

    public ScrollOfPoison() {
        super("Scroll of Poison", ItemCategory.OTHER);
        setRarity(ItemRarity.RARE);
        setDescription("A scroll inscribed with a poison spell");
    }

    @Override
    public Item copy() {
        return new ScrollOfPoison();
    }
}
