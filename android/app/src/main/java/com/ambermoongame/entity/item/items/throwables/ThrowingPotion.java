package com.ambermoongame.entity.item.items.throwables;

import com.ambermoongame.entity.item.Item;
import com.ambermoongame.entity.ProjectileEntity;

/**
 * Throwing Potion - Explodes in a splash.
 * Common consumable throwable with splash effect.
 */
public class ThrowingPotion extends Item {

    public ThrowingPotion() {
        super("Throwing Potion", ItemCategory.THROWABLE);
        setRangedWeapon(true, ProjectileEntity.PROJECTILE_POTION, 15, 14.0f);
        setRarity(ItemRarity.COMMON);
        setDescription("Explodes in a splash");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new ThrowingPotion();
    }
}
