package entity.item.items.weapons.ranged;

import entity.item.Item;
import entity.ProjectileEntity.ProjectileType;

/**
 * Lightning Rod - Channels the power of storms.
 * Rare ranged weapon with chain lightning effect.
 */
public class LightningRod extends Item {

    public LightningRod() {
        super("Lightning Rod", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileType.MAGIC_BOLT, 30, 25.0f);
        setRarity(ItemRarity.RARE);
        setDescription("Channels the power of storms");
        setSpecialEffect("Chain lightning effect");
        setAmmoItemName("mana");
        setChargeable(true, 2.0f, 20, 3.0f);
        setScalesWithIntelligence(true);
    }

    @Override
    public Item copy() {
        return new LightningRod();
    }
}
