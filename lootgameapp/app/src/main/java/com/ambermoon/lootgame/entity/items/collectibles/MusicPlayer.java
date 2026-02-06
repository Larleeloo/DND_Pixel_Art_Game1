package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

/**
 * Music Player - A magical music box.
 * Rare collectible item.
 */
public class MusicPlayer extends Item {

    public MusicPlayer() {
        super("Music Player", ItemCategory.OTHER);
        setRarity(ItemRarity.RARE);
        setDescription("A magical music box");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new MusicPlayer();
    }
}
