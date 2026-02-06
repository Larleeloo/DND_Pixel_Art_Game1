package com.ambermoongame.entity.item.items.weapons.ranged;

import com.ambermoongame.entity.item.Item;
import com.ambermoongame.entity.ProjectileEntity;

/**
 * Blazing Rod - Burns with eternal flame.
 * Rare ranged weapon that sets enemies ablaze.
 */
public class BlazingRod extends Item {

    public BlazingRod() {
        super("Blazing Rod", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileEntity.PROJECTILE_FIREBALL, 25, 14.0f);
        setRarity(ItemRarity.RARE);
        setDescription("Burns with eternal flame");
        setSpecialEffect("Sets enemies ablaze");
        setAmmoItemName("mana");
        setChargeable(true, 2.5f, 25, 2.5f);
        setScalesWithIntelligence(true);
    }

    @Override
    public Item copy() {
        return new BlazingRod();
    }
}
