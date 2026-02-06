package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

/**
 * Celestial Robes - Garments of the gods.
 * Mythic armor with mana and magic damage boost. Ancient artifact.
 */
public class CelestialRobes extends Item {

    public CelestialRobes() {
        super("Celestial Robes", ItemCategory.ARMOR);
        setDefense(20);
        setRarity(ItemRarity.MYTHIC);
        setDescription("Garments of the gods");
        setSpecialEffect("+50% mana, +30% magic damage");
        setScalesWithIntelligence(true);
        setWisdomRequirement(8);
    }

    @Override
    public Item copy() {
        return new CelestialRobes();
    }
}
