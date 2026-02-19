package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

/**
 * Fancy Boots - Stylish and somewhat protective.
 * Uncommon armor with movement speed bonus.
 */
public class FancyBoots extends Item {

    public FancyBoots() {
        super("Fancy Boots", ItemCategory.ARMOR);
        setDefense(3);
        setRarity(ItemRarity.RARE);
        setDescription("Stylish and somewhat protective");
        setSpecialEffect("+10% movement speed");
    }

    @Override
    public Item copy() {
        return new FancyBoots();
    }
}
