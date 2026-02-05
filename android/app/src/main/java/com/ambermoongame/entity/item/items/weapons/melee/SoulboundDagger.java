package com.ambermoongame.entity.item.items.weapons.melee;

import com.ambermoongame.entity.item.Item;

/**
 * Soulbound Dagger - Bound to your soul, returns when thrown.
 * Rare melee weapon that returns to the owner.
 */
public class SoulboundDagger extends Item {

    public SoulboundDagger() {
        super("Soulbound Dagger", ItemCategory.WEAPON);
        setDamage(15);
        setAttackSpeed(2.5f);
        setRange(45);
        setRarity(ItemRarity.RARE);
        setDescription("Bound to your soul, returns when thrown");
        setSpecialEffect("Returns to owner");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new SoulboundDagger();
    }
}
