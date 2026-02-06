package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

/**
 * Chrono Blade - Bends time around its wielder.
 * Mythic melee weapon that slows time on hit. Requires very high Wisdom.
 */
public class TimeWarpBlade extends Item {

    public TimeWarpBlade() {
        super("Chrono Blade", ItemCategory.WEAPON);
        setDamage(40);
        setAttackSpeed(2.0f);
        setRange(70);
        setRarity(ItemRarity.MYTHIC);
        setDescription("Bends time around its wielder");
        setSpecialEffect("Slows time on hit");
        setWisdomRequirement(9);
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new TimeWarpBlade();
    }
}
