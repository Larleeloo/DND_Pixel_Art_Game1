package com.ambermoongame.entity.item.items.materials;

import com.ambermoongame.entity.item.Item;

/**
 * Diamond - A valuable gemstone.
 * Rare crafting material.
 */
public class Diamond extends Item {

    public Diamond() {
        super("Diamond", ItemCategory.MATERIAL);
        setRarity(ItemRarity.RARE);
        setDescription("A valuable gemstone");
    }

    @Override
    public Item copy() {
        return new Diamond();
    }
}
