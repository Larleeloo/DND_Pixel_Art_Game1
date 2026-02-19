package com.ambermoon.lootgame.entity.items.weapons.ranged;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ProjectileEntity;

/**
 * Magic Fishing Rod - Catches fish and magic alike.
 * Uncommon ranged weapon that pulls items toward you.
 */
public class MagicFishingRod extends Item {

    public MagicFishingRod() {
        super("Magic Fishing Rod", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileEntity.PROJECTILE_MAGIC_BOLT, 8, 12.0f);
        setRarity(ItemRarity.RARE);
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
