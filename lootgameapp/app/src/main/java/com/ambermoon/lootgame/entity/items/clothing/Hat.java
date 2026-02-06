package com.ambermoon.lootgame.entity.items.clothing;

import com.ambermoon.lootgame.entity.Item;

/**
 * Hat - A simple but stylish hat.
 * Common cosmetic clothing item.
 */
public class Hat extends Item {

    public Hat() {
        super("Hat", ItemCategory.CLOTHING);
        setRarity(ItemRarity.COMMON);
        setDescription("A simple but stylish hat");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new Hat();
    }
}
