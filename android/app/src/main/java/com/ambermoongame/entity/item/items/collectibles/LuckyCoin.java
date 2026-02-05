package com.ambermoongame.entity.item.items.collectibles;

import com.ambermoongame.entity.item.Item;

/**
 * Lucky Coin - Fortune favors the brave.
 * Rare collectible with drop rate bonus.
 */
public class LuckyCoin extends Item {

    public LuckyCoin() {
        super("Lucky Coin", ItemCategory.OTHER);
        setRarity(ItemRarity.RARE);
        setDescription("Fortune favors the brave");
        setSpecialEffect("+15% drop rate");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new LuckyCoin();
    }
}
