package com.ambermoongame.entity.item.items.weapons.melee;

import com.ambermoongame.entity.item.Item;

/**
 * Thunder Hammer - Strikes with the fury of storms.
 * Legendary melee weapon with chain lightning effect. Requires some Wisdom.
 */
public class ThunderHammer extends Item {

    public ThunderHammer() {
        super("Thunder Hammer", ItemCategory.WEAPON);
        setDamage(55);
        setAttackSpeed(0.6f);
        setRange(90);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("Strikes with the fury of storms");
        setSpecialEffect("Chain lightning on hit");
        setCritChance(0.12f);
        setWisdomRequirement(6);
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new ThunderHammer();
    }
}
