package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

/**
 * Ethereal DragonSlayer Blade - Forged to slay ancient dragons.
 * Mythic melee weapon with massive bonus damage against dragons.
 */
public class EtherealDragonslayer extends Item {

    public EtherealDragonslayer() {
        super("Ethereal DragonSlayer Blade", ItemCategory.WEAPON);
        setDamage(50);
        setAttackSpeed(0.8f);
        setRange(90);
        setRarity(ItemRarity.MYTHIC);
        setDescription("Forged to slay ancient dragons");
        setSpecialEffect("+200% damage to dragons");
        setCritChance(0.25f);
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new EtherealDragonslayer();
    }
}
