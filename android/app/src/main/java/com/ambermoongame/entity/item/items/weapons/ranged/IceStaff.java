package com.ambermoongame.entity.item.items.weapons.ranged;

import com.ambermoongame.entity.item.Item;
import entity.ProjectileEntity.ProjectileType;

/**
 * Staff of Ice - Freezes enemies.
 * Rare ranged weapon with slow effect and chargeable shots.
 */
public class IceStaff extends Item {

    public IceStaff() {
        super("Staff of Ice", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileType.ICEBALL, 18, 14.0f);
        setRarity(ItemRarity.RARE);
        setDescription("Freezes enemies");
        setSpecialEffect("Slow effect");
        setAmmoItemName("mana");
        setChargeable(true, 2.5f, 25, 2.5f);
        setChargeSizeMultiplier(2.0f);
        setScalesWithIntelligence(true);
    }

    @Override
    public Item copy() {
        return new IceStaff();
    }
}
