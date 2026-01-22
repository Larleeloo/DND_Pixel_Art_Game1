package entity.item.items.tools;

import entity.item.Item;

/**
 * Shears - For cutting wool and plants.
 * Common tool for gathering plant materials.
 */
public class Shears extends Item {

    public Shears() {
        super("Shears", ItemCategory.TOOL);
        setDamage(2);
        setRarity(ItemRarity.COMMON);
        setDescription("For cutting wool and plants");
    }

    @Override
    public Item copy() {
        return new Shears();
    }
}
