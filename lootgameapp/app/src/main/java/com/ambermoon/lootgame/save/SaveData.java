package com.ambermoon.lootgame.save;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveData {
    public int version = 1;
    public String appVersion = "";
    public String platform = "android_loot";
    public long lastModified;
    public String lastModifiedDate = "";

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

    // Learned recipes (full recipe data stored in save, similar to alchemy_recipes.json)
    public List<LearnedRecipe> learnedRecipes = new ArrayList<>();

    // Shop items (items available for purchase, configured by Lars)
    public List<ShopItem> shopItems = new ArrayList<>();

    // Player marketplace: items this player currently has listed for sale
    public List<PlayerListing> playerListings = new ArrayList<>();

    // Timestamps of when this player sold items (for 5-per-week limit tracking)
    public List<Long> sellTimestamps = new ArrayList<>();

    // Coins earned from marketplace sales that haven't been collected yet
    public int pendingTradeCoins = 0;

    // Loadout: items reserved for the Amber Moon game (max 25 slots)
    public List<VaultItem> loadoutItems = new ArrayList<>();

    // Cosmetics
    public String selectedBackgroundId = "none";
    public List<String> unlockedBackgrounds = new ArrayList<>();

    // Profile picture (Base64-encoded JPEG, max 64x64)
    public String profilePicBase64 = "";

    public static class VaultItem {
        public String itemId;
        public int stackCount;

        public VaultItem() {}
        public VaultItem(String itemId, int count) {
            this.itemId = itemId;
            this.stackCount = count;
        }
    }

    public static class LearnedRecipe {
        public String id;
        public String name;
        public List<String> ingredients;
        public String result;
        public int resultCount;

        public LearnedRecipe() {
            ingredients = new ArrayList<>();
            resultCount = 1;
        }

        public LearnedRecipe(String id, String name, List<String> ingredients, String result, int resultCount) {
            this.id = id;
            this.name = name;
            this.ingredients = new ArrayList<>(ingredients);
            this.result = result;
            this.resultCount = resultCount;
        }
    }

    public static class ShopItem {
        public String itemId;
        public int price;

        public ShopItem() {}
        public ShopItem(String itemId, int price) {
            this.itemId = itemId;
            this.price = price;
        }
    }

    public static class PlayerListing {
        public String itemId;
        public int price;
        public String sellerUsername;
        public long listTimestamp;

        public PlayerListing() {}
        public PlayerListing(String itemId, int price, String sellerUsername, long listTimestamp) {
            this.itemId = itemId;
            this.price = price;
            this.sellerUsername = sellerUsername;
            this.listTimestamp = listTimestamp;
        }
    }
}
