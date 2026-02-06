package com.ambermoon.lootgame.entity.items.tools;

import com.ambermoon.lootgame.entity.Item;

/**
 * Iron Axe - Better for chopping.
 * Common tool with improved chopping speed.
 */
public class IronAxeTool extends Item {

    public IronAxeTool() {
        super("Iron Axe", ItemCategory.TOOL);
        setDamage(8);
        setRarity(ItemRarity.COMMON);
        setDescription("Better for chopping");
    }

    @Override
    public Item copy() {
        return new IronAxeTool();
    }
}
