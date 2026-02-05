package com.ambermoongame.entity.item.items.materials;

import com.ambermoongame.entity.item.Item;

/**
 * String - Used for crafting bows.
 * Common crafting material.
 */
public class StringItem extends Item {

    public StringItem() {
        super("String", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("Used for crafting bows");
    }

    @Override
    public Item copy() {
        return new StringItem();
    }
}
