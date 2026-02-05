package com.ambermoongame.entity.item.items.clothing;

import com.ambermoongame.entity.item.Item;

/**
 * Black Dress - A sophisticated dark dress.
 * Uncommon cosmetic clothing item.
 */
public class BlackDress extends Item {

    public BlackDress() {
        super("Black Dress", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A sophisticated dark dress");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new BlackDress();
    }
}
