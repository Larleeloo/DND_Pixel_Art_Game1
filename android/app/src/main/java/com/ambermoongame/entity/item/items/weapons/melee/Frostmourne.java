package com.ambermoongame.entity.item.items.weapons.melee;

import com.ambermoongame.entity.item.Item;

/**
 * Frostmourne - Hungers for souls.
 * Legendary melee weapon that freezes enemies solid. Requires Wisdom.
 */
public class Frostmourne extends Item {

    public Frostmourne() {
        super("Frostmourne", ItemCategory.WEAPON);
        setDamage(42);
        setAttackSpeed(0.9f);
        setRange(80);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("Hungers for souls");
        setSpecialEffect("Freezes enemies solid");
        setCritChance(0.18f);
        setWisdomRequirement(7);
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new Frostmourne();
    }
}
