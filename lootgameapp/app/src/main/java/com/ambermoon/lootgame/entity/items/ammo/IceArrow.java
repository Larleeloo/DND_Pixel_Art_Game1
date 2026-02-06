package com.ambermoon.lootgame.entity.items.ammo;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ProjectileEntity;

/**
 * Ice Arrow - Arrows that slow enemies.
 * Uncommon stackable ammo with freezing effect.
 */
public class IceArrow extends Item {

    public IceArrow() {
        super("Ice Arrow", ItemCategory.MATERIAL);
        setDamage(7);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Arrows that slow enemies");
        setStackable(true);
        setMaxStackSize(16);
        setRangedWeapon(false, ProjectileEntity.PROJECTILE_ARROW, 7, 0);
        setStatusEffect(ProjectileEntity.EFFECT_FROZEN, 4.0, 3, 1.1f);
        setSpecialEffect("Slows for 4 seconds");
    }

    @Override
    public Item copy() {
        return new IceArrow();
    }
}
