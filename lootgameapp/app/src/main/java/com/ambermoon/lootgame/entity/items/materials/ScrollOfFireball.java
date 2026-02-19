package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class ScrollOfFireball extends Item {

    public ScrollOfFireball() {
        super("Scroll of Fireball", ItemCategory.OTHER);
        setRarity(ItemRarity.RARE);
        setDescription("A scroll inscribed with a fireball spell");
    }

    @Override
    public Item copy() {
        return new ScrollOfFireball();
    }
}
