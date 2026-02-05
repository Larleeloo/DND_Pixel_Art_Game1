package com.ambermoongame.entity.item.items.collectibles;

import com.ambermoongame.entity.item.Item;

/**
 * Philosopher's Stone - The legendary alchemical creation.
 * Mythic collectible that transmutes materials. Scales with INT.
 */
public class PhilosophersStone extends Item {

    public PhilosophersStone() {
        super("Philosopher's Stone", ItemCategory.OTHER);
        setRarity(ItemRarity.MYTHIC);
        setDescription("The legendary alchemical creation");
        setSpecialEffect("Transmute any material to gold");
        setScalesWithIntelligence(true);
        setWisdomRequirement(8);
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new PhilosophersStone();
    }
}
