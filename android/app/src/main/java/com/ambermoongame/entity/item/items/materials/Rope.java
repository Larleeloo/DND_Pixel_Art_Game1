package com.ambermoongame.entity.item.items.materials;

import com.ambermoongame.entity.item.Item;

/**
 * Rope - Strong fiber cord.
 * Common crafting material.
 */
public class Rope extends Item {

    public Rope() {
        super("Rope", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("Strong fiber cord");
    }

    @Override
    public Item copy() {
        return new Rope();
    }
}
