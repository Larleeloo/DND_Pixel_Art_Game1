package com.ambermoongame.entity.item.items.weapons.ranged;

import com.ambermoongame.entity.item.Item;
import com.ambermoongame.entity.ProjectileEntity;

/**
 * Magic Wand - Fires magic bolts.
 * Uncommon ranged weapon that uses mana.
 */
public class MagicWand extends Item {

    public MagicWand() {
        super("Magic Wand", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileEntity.PROJECTILE_MAGIC_BOLT, 10, 15.0f);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Fires magic bolts");
        setAmmoItemName("mana");
        setScalesWithIntelligence(true);
    }

    @Override
    public Item copy() {
        return new MagicWand();
    }
}
