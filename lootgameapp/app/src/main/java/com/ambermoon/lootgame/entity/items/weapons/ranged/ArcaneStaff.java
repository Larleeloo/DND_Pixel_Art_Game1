package com.ambermoon.lootgame.entity.items.weapons.ranged;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ProjectileEntity;

/**
 * Arcane Staff - Channels pure arcane energy.
 * Epic ranged weapon with high crit chance and powerful chargeable shots.
 */
public class ArcaneStaff extends Item {

    public ArcaneStaff() {
        super("Arcane Staff", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileEntity.PROJECTILE_MAGIC_BOLT, 35, 16.0f);
        setRarity(ItemRarity.EPIC);
        setDescription("Channels pure arcane energy");
        setCritChance(0.20f);
        setAmmoItemName("mana");
        setChargeable(true, 4.0f, 40, 4.0f);
        setChargeSizeMultiplier(3.0f);
        setChargeSpeedMultiplier(2.0f);
        setScalesWithIntelligence(true);
    }

    @Override
    public Item copy() {
        return new ArcaneStaff();
    }
}
