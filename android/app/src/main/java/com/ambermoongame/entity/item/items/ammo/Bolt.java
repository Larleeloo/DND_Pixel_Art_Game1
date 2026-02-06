package com.ambermoongame.entity.item.items.ammo;

import com.ambermoongame.entity.item.Item;
import com.ambermoongame.entity.ProjectileEntity;

/**
 * Bolt - Standard crossbow ammunition.
 * Common stackable ammo item.
 */
public class Bolt extends Item {

    public Bolt() {
        super("Bolt", ItemCategory.MATERIAL);
        setDamage(8);
        setRarity(ItemRarity.COMMON);
        setDescription("Standard crossbow ammunition");
        setStackable(true);
        setMaxStackSize(16);
        setRangedWeapon(false, ProjectileEntity.PROJECTILE_BOLT, 8, 0);
    }

    @Override
    public Item copy() {
        return new Bolt();
    }
}
