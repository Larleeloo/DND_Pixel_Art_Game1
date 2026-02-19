package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

/**
 * Necromancer's Blade - A blade infused with dark energy.
 * Epic melee weapon with lifesteal effect.
 */
public class NecromancersBlade extends Item {

    public NecromancersBlade() {
        super("Necromancer's Blade", ItemCategory.WEAPON);
        setDamage(28);
        setAttackSpeed(1.1f);
        setRange(70);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A blade infused with dark energy");
        setSpecialEffect("Lifesteal on hit");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new NecromancersBlade();
    }
}
