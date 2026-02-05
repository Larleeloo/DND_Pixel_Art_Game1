package com.ambermoongame.entity.item.items.weapons.melee;

import com.ambermoongame.entity.item.Item;

/**
 * Soul Reaver - Devours the souls of the fallen.
 * Mythic melee weapon with 100% lifesteal on kill. Requires high Wisdom.
 */
public class SoulReaver extends Item {

    public SoulReaver() {
        super("Soul Reaver", ItemCategory.WEAPON);
        setDamage(45);
        setAttackSpeed(1.0f);
        setRange(75);
        setRarity(ItemRarity.MYTHIC);
        setDescription("Devours the souls of the fallen");
        setSpecialEffect("100% lifesteal on kill");
        setCritChance(0.20f);
        setWisdomRequirement(8);
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new SoulReaver();
    }
}
