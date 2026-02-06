package com.ambermoon.lootgame.entity.items.weapons.ranged;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ProjectileEntity;

/**
 * Crossbow - Powerful but slow to reload.
 * Uncommon ranged weapon with high damage.
 */
public class Crossbow extends Item {

    public Crossbow() {
        super("Crossbow", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileEntity.PROJECTILE_BOLT, 20, 20.0f);
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
