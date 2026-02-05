package com.ambermoongame.entity.item.items.tools;

import com.ambermoongame.entity.item.Item;

/**
 * Wooden Axe - Chops wood.
 * Common tool for gathering wood.
 */
public class WoodenAxeTool extends Item {

    public WoodenAxeTool() {
        super("Wooden Axe", ItemCategory.TOOL);
        setDamage(4);
        setRarity(ItemRarity.COMMON);
        setDescription("Chops wood");
    }

    @Override
    public Item copy() {
        return new WoodenAxeTool();
    }
}
