package entity.item.items.clothing;

import entity.item.Item;

/**
 * 3-Piece Suit - Formal attire for special occasions.
 * Uncommon cosmetic clothing item.
 */
public class ThreePieceSuit extends Item {

    public ThreePieceSuit() {
        super("3-Piece Suit", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Formal attire for special occasions");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new ThreePieceSuit();
    }
}
