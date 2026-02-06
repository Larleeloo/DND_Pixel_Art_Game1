package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

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
