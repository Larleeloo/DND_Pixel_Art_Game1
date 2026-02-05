package com.ambermoongame.entity.item.items.weapons.ranged;

import com.ambermoongame.entity.item.Item;
import entity.ProjectileEntity.ProjectileType;

/**
 * Magic Fishing Rod - Catches fish and magic alike.
 * Uncommon ranged weapon that pulls items toward you.
 */
public class MagicFishingRod extends Item {

    public MagicFishingRod() {
        super("Magic Fishing Rod", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileType.MAGIC_BOLT, 8, 12.0f);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Catches fish and magic alike");
        setSpecialEffect("Pulls items toward you");
        setAmmoItemName("mana");
        setScalesWithIntelligence(true);
    }

    @Override
    public Item copy() {
        return new MagicFishingRod();
    }
}
