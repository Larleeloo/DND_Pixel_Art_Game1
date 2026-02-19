package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

/**
 * Void Blade - Forged in the heart of a black hole.
 * Mythic melee weapon that absorbs enemy souls. Requires high Wisdom.
 */
public class VoidBlade extends Item {

    public VoidBlade() {
        super("Void Blade", ItemCategory.WEAPON);
        setDamage(65);
        setAttackSpeed(1.3f);
        setRange(85);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("Forged in the heart of a black hole");
        setSpecialEffect("Absorbs enemy souls");
        setCritChance(0.30f);
        setWisdomRequirement(8);
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new VoidBlade();
    }
}
