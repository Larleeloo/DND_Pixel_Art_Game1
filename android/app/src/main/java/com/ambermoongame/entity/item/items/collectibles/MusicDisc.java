package com.ambermoongame.entity.item.items.collectibles;

import com.ambermoongame.entity.item.Item;

/**
 * Music Disc - Plays enchanting melodies.
 * Uncommon collectible item.
 */
public class MusicDisc extends Item {

    public MusicDisc() {
        super("Music Disc", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Plays enchanting melodies");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new MusicDisc();
    }
}
