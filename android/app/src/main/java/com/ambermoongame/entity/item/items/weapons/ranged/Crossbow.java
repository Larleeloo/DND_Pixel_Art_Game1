package com.ambermoongame.entity.item.items.weapons.ranged;

import com.ambermoongame.entity.item.Item;
import entity.ProjectileEntity.ProjectileType;

/**
 * Crossbow - Powerful but slow to reload.
 * Uncommon ranged weapon with high damage.
 */
public class Crossbow extends Item {

    public Crossbow() {
        super("Crossbow", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileType.BOLT, 20, 20.0f);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Powerful but slow to reload");
        setAmmoItemName("bolt");
        setScalesWithDexterity(true);
    }

    @Override
    public Item copy() {
        return new Crossbow();
    }
}
