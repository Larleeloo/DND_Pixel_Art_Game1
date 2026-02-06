package com.ambermoongame.entity.item.items.ammo;

import com.ambermoongame.entity.item.Item;
import com.ambermoongame.entity.ProjectileEntity;

/**
 * Fire Arrow - Arrows that burn on impact.
 * Uncommon stackable ammo with burning effect.
 */
public class FireArrow extends Item {

    public FireArrow() {
        super("Fire Arrow", ItemCategory.MATERIAL);
        setDamage(8);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Arrows that burn on impact");
        setStackable(true);
        setMaxStackSize(16);
        setRangedWeapon(false, ProjectileEntity.PROJECTILE_ARROW, 8, 0);
        setStatusEffect(ProjectileEntity.EFFECT_BURNING, 3.0, 5, 1.2f);
        setSpecialEffect("Burns for 3 seconds");
    }

    @Override
    public Item copy() {
        return new FireArrow();
    }
}
