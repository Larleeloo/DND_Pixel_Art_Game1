package com.ambermoongame.entity.item.items.tools;

import com.ambermoongame.entity.item.Item;

/**
 * Iron Pickaxe - Standard mining tool.
 * Common tool with decent mining speed.
 */
public class IronPickaxe extends Item {

    public IronPickaxe() {
        super("Iron Pickaxe", ItemCategory.TOOL);
        setDamage(5);
        setRarity(ItemRarity.COMMON);
        setDescription("Standard mining tool");
    }

    @Override
    public Item copy() {
        return new IronPickaxe();
    }
}
