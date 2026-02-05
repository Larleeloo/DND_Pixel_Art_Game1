package com.ambermoongame.entity.item.items.weapons.ranged;

import com.ambermoongame.entity.item.Item;
import entity.ProjectileEntity.ProjectileType;

/**
 * Wooden Bow - A simple hunting bow.
 * Common ranged weapon with chargeable shots.
 */
public class WoodenBow extends Item {

    public WoodenBow() {
        super("Wooden Bow", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileType.ARROW, 8, 12.0f);
        setRarity(ItemRarity.COMMON);
        setDescription("A simple hunting bow");
        setAmmoItemName("arrow");
        setChargeable(true, 2.0f, 0, 2.0f);
        setChargeSpeedMultiplier(1.5f);
        setScalesWithDexterity(true);
    }

    @Override
    public Item copy() {
        return new WoodenBow();
    }
}
