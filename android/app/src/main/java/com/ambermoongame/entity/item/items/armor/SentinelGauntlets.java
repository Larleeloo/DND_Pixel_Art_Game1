package com.ambermoongame.entity.item.items.armor;

import com.ambermoongame.entity.item.Item;

/**
 * Sentinel Gauntlets - Gauntlets of a watchful guardian.
 * Uncommon armor with attack speed bonus.
 */
public class SentinelGauntlets extends Item {

    public SentinelGauntlets() {
        super("Sentinel Gauntlets", ItemCategory.ARMOR);
        setDefense(8);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Gauntlets of a watchful guardian");
        setSpecialEffect("+5% attack speed");
    }

    @Override
    public Item copy() {
        return new SentinelGauntlets();
    }
}
