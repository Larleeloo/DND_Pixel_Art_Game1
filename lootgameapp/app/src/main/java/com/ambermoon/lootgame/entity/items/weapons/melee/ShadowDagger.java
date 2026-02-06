package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

/**
 * Shadow Dagger - Strikes from the shadows.
 * Legendary melee weapon with invisibility on backstab. Requires some Wisdom.
 */
public class ShadowDagger extends Item {

    public ShadowDagger() {
        super("Shadow Dagger", ItemCategory.WEAPON);
        setDamage(28);
        setAttackSpeed(3.0f);
        setRange(40);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("Strikes from the shadows");
        setSpecialEffect("Invisible for 2 seconds after backstab");
        setCritChance(0.35f);
        setWisdomRequirement(6);
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new ShadowDagger();
    }
}
