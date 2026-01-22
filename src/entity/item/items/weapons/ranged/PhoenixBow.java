package entity.item.items.weapons.ranged;

import entity.item.Item;
import entity.ProjectileEntity.ProjectileType;

/**
 * Phoenix Bow - Rises from the ashes.
 * Legendary ranged weapon with fire arrows that resurrect. Requires Wisdom.
 */
public class PhoenixBow extends Item {

    public PhoenixBow() {
        super("Phoenix Bow", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileType.ARROW, 35, 22.0f);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("Rises from the ashes");
        setSpecialEffect("Fire arrows that resurrect");
        setAmmoItemName("arrow");
        setChargeable(true, 2.0f, 0, 3.5f);
        setWisdomRequirement(7);
        setScalesWithDexterity(true);
    }

    @Override
    public Item copy() {
        return new PhoenixBow();
    }
}
