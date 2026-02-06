package com.ambermoon.lootgame.entity.items.tools;

import com.ambermoon.lootgame.entity.Item;

/**
 * Golden Pickaxe - Mines quickly but breaks fast.
 * Uncommon tool with high mining speed but low durability.
 */
public class GoldenPickaxe extends Item {

    public GoldenPickaxe() {
        super("Golden Pickaxe", ItemCategory.TOOL);
        setDamage(8);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Mines quickly but breaks fast");
    }

    @Override
    public Item copy() {
        return new GoldenPickaxe();
    }
}
