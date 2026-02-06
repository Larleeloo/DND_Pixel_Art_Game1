package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Eye of the Cosmos - See all that was, is, and will be.
 * Mythic collectible that reveals all secrets. Powerful artifact.
 */
public class EyeOfCosmos extends Item {

    public EyeOfCosmos() {
        super("Eye of the Cosmos", ItemCategory.OTHER);
        setRarity(ItemRarity.MYTHIC);
        setDescription("See all that was, is, and will be");
        setSpecialEffect("Reveal all secrets and treasures");
        setWisdomRequirement(8);
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new EyeOfCosmos();
    }
}
