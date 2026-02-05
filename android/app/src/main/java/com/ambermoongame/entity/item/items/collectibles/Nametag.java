package com.ambermoongame.entity.item.items.collectibles;

import com.ambermoongame.entity.item.Item;

/**
 * Nametag - Name anything you want.
 * Uncommon collectible item.
 */
public class Nametag extends Item {

    public Nametag() {
        super("Nametag", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Name anything you want");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new Nametag();
    }
}
