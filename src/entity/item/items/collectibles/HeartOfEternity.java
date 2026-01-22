package entity.item.items.collectibles;

import entity.item.Item;

/**
 * Heart of Eternity - The crystallized essence of time itself.
 * Mythic collectible with immortality. Most powerful artifact.
 */
public class HeartOfEternity extends Item {

    public HeartOfEternity() {
        super("Heart of Eternity", ItemCategory.OTHER);
        setRarity(ItemRarity.MYTHIC);
        setDescription("The crystallized essence of time itself");
        setSpecialEffect("Immortality for 10 seconds per day");
        setWisdomRequirement(9);
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new HeartOfEternity();
    }
}
