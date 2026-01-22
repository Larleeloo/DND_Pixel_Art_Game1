package entity.item.items.armor;

import entity.item.Item;

/**
 * Titan Gauntlets - Worn by ancient giants.
 * Legendary armor with melee damage boost and ground slam. Ancient artifact.
 */
public class TitanGauntlets extends Item {

    public TitanGauntlets() {
        super("Titan Gauntlets", ItemCategory.ARMOR);
        setDefense(18);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("Worn by ancient giants");
        setSpecialEffect("+40% melee damage, ground slam ability");
        setScalesWithStrength(true);
        setWisdomRequirement(6);
    }

    @Override
    public Item copy() {
        return new TitanGauntlets();
    }
}
