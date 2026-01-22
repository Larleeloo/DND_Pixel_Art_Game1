package entity.item.items.weapons.melee;

import entity.item.Item;

/**
 * Vampiric Blade - Thirsts for blood.
 * Rare melee weapon with 10% lifesteal.
 */
public class VampiricBlade extends Item {

    public VampiricBlade() {
        super("Vampiric Blade", ItemCategory.WEAPON);
        setDamage(20);
        setAttackSpeed(1.0f);
        setRange(60);
        setRarity(ItemRarity.RARE);
        setDescription("Thirsts for blood");
        setSpecialEffect("10% lifesteal");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new VampiricBlade();
    }
}
