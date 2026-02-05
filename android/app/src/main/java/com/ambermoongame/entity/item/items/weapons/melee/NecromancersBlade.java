package com.ambermoongame.entity.item.items.weapons.melee;

import com.ambermoongame.entity.item.Item;

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
        setRarity(ItemRarity.EPIC);
        setDescription("A blade infused with dark energy");
        setSpecialEffect("Lifesteal on hit");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new NecromancersBlade();
    }
}
