package com.ambermoon.lootgame.save;

import java.util.ArrayList;
import java.util.List;

public class SaveData {
    public int version = 1;
    public String platform = "android_loot";
    public long lastModified;

    // PIN (stored in save file — not secure, just a convenience lock)
    public String pin = "";

    // Coins
    public int coins = 500; // first launch bonus
    public long totalCoinsEarned = 500;
    public long totalCoinsSpent = 0;

    // Chest timers
    public long dailyChestLastOpened = 0;
    public long monthlyChestLastOpened = 0;

    // Stats
    public int totalItemsCollected = 0;
    public int legendaryItemsFound = 0;
    public int mythicItemsFound = 0;

    // Streak
    public int consecutiveDays = 0;
    public String lastLoginDate = "";

    // Slot machine
    public int slotMachinePulls = 0;
    public int biggestJackpot = 0;

    // Vault items
    public List<VaultItem> vaultItems = new ArrayList<>();

    public static class VaultItem {
        public String itemId;
        public int stackCount;

        public VaultItem() {}
        public VaultItem(String itemId, int count) {
            this.itemId = itemId;
            this.stackCount = count;
        }
    }
}
