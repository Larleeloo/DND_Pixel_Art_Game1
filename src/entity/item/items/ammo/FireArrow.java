package entity.item.items.ammo;

import entity.item.Item;
import entity.ProjectileEntity.ProjectileType;
import entity.ProjectileEntity.StatusEffectType;

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
        setRangedWeapon(false, ProjectileType.ARROW, 8, 0);
        setStatusEffect(StatusEffectType.BURNING, 3.0, 5, 1.2f);
        setSpecialEffect("Burns for 3 seconds");
    }

    @Override
    public Item copy() {
        return new FireArrow();
    }
}
