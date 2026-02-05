package com.ambermoongame.entity.item.items.tools;

import com.ambermoongame.entity.item.Item;

/**
 * Wooden Pickaxe - Mines stone slowly.
 * Common tool for basic mining.
 */
public class WoodenPickaxe extends Item {

    public WoodenPickaxe() {
        super("Wooden Pickaxe", ItemCategory.TOOL);
        setDamage(3);
        setRarity(ItemRarity.COMMON);
        setDescription("Mines stone slowly");
    }

    @Override
    public Item copy() {
        return new WoodenPickaxe();
    }
}
