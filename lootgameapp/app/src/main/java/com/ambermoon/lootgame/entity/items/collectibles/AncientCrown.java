package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Ancient Crown - A crown worn by forgotten kings.
 * Epic collectible with stat bonus.
 */
public class AncientCrown extends Item {

    public AncientCrown() {
        super("Ancient Crown", ItemCategory.OTHER);
        setRarity(ItemRarity.EPIC);
        setDescription("A crown worn by forgotten kings");
        setSpecialEffect("+20% to all stats");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new AncientCrown();
    }
}
