package com.ambermoon.lootgame.entity.items.tools;

import com.ambermoon.lootgame.entity.Item;

/**
 * Wooden Axe - Chops wood.
 * Common tool for gathering wood.
 */
public class WoodenAxeTool extends Item {

    public WoodenAxeTool() {
        super("Carpenter's Axe", ItemCategory.TOOL);
        setDamage(4);
        setRarity(ItemRarity.COMMON);
        setDescription("A sturdy axe for woodworking");
    }

    @Override
    public Item copy() {
        return new WoodenAxeTool();
    }
}
