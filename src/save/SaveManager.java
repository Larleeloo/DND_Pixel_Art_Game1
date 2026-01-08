package save;

import entity.Item;
import entity.ItemRegistry;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * SaveManager handles persistent game data storage using JSON.
 * Manages player inventory, vault storage, chest cooldowns, and other progression data.
 *
 * Vault System:
 * - The vault stores up to 10,000 items persistently
 * - Items stack up to 16 duplicates before taking another slot
 * - Items are automatically transferred from player inventory to vault when leaving loot game
 */
public class SaveManager {

    private static final String SAVE_DIR = "saves";
    private static final String SAVE_FILE = "player_data.json";

    private static SaveManager instance;

    // Save data
    private List<SavedItem> inventory;
    private List<SavedItem> vaultItems;  // Vault can hold up to 10,000 items
    private long dailyChestLastOpened;
    private long monthlyChestLastOpened;
    private int totalItemsCollected;
    private int legendaryItemsFound;
    private int mythicItemsFound;
    private boolean developerMode; // When true, resets chest cooldowns on each launch

    // Cooldowns in milliseconds
    public static final long DAILY_COOLDOWN = 24 * 60 * 60 * 1000L; // 24 hours
    public static final long MONTHLY_COOLDOWN = 30 * 24 * 60 * 60 * 1000L; // 30 days

    // Vault constants
    public static final int VAULT_MAX_SLOTS = 10000;
    public static final int STACK_SIZE = 16;  // Maximum stack size for duplicate items

    /**
     * Represents a saved item with its properties
     */
    public static class SavedItem {
        public String itemId;
        public int stackCount;

        public SavedItem(String itemId, int stackCount) {
            this.itemId = itemId;
            this.stackCount = stackCount;
        }
    }

    private SaveManager() {
        inventory = new ArrayList<>();
        vaultItems = new ArrayList<>();
        dailyChestLastOpened = 0;
        monthlyChestLastOpened = 0;
        totalItemsCollected = 0;
        legendaryItemsFound = 0;
        mythicItemsFound = 0;
        developerMode = false;
    }

    public static SaveManager getInstance() {
        if (instance == null) {
            instance = new SaveManager();
            instance.load();
        }
        return instance;
    }

    /**
     * Checks if the daily chest can be opened
     */
    public boolean canOpenDailyChest() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - dailyChestLastOpened) >= DAILY_COOLDOWN;
    }

    /**
     * Checks if the monthly chest can be opened
     */
    public boolean canOpenMonthlyChest() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - monthlyChestLastOpened) >= MONTHLY_COOLDOWN;
    }

    /**
     * Gets time remaining until daily chest can be opened (in milliseconds)
     */
    public long getDailyChestTimeRemaining() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - dailyChestLastOpened;
        return Math.max(0, DAILY_COOLDOWN - elapsed);
    }

    /**
     * Gets time remaining until monthly chest can be opened (in milliseconds)
     */
    public long getMonthlyChestTimeRemaining() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - monthlyChestLastOpened;
        return Math.max(0, MONTHLY_COOLDOWN - elapsed);
    }

    /**
     * Marks the daily chest as opened
     */
    public void markDailyChestOpened() {
        dailyChestLastOpened = System.currentTimeMillis();
        save();
    }

    /**
     * Marks the monthly chest as opened
     */
    public void markMonthlyChestOpened() {
        monthlyChestLastOpened = System.currentTimeMillis();
        save();
    }

    /**
     * Adds an item to the saved inventory
     */
    public void addItem(String itemId, int count) {
        // Check if item already exists in inventory
        for (SavedItem saved : inventory) {
            if (saved.itemId.equals(itemId)) {
                saved.stackCount += count;
                save();
                return;
            }
        }
        // Add new item
        inventory.add(new SavedItem(itemId, count));
        totalItemsCollected += count;

        // Track rare items
        Item item = ItemRegistry.getTemplate(itemId);
        if (item != null) {
            if (item.getRarity() == Item.ItemRarity.LEGENDARY) {
                legendaryItemsFound++;
            } else if (item.getRarity() == Item.ItemRarity.MYTHIC) {
                mythicItemsFound++;
            }
        }

        save();
    }

    /**
     * Gets all saved items
     */
    public List<SavedItem> getInventory() {
        return new ArrayList<>(inventory);
    }

    // ==================== Vault Methods ====================

    /**
     * Adds an item to the vault with proper stacking.
     * Items stack up to STACK_SIZE (16) before using a new slot.
     *
     * @param itemId The item registry ID
     * @param count Number of items to add
     * @return Number of items that couldn't be added (overflow)
     */
    public int addItemToVault(String itemId, int count) {
        if (itemId == null || itemId.isEmpty() || count <= 0) return count;

        int remaining = count;

        // First, try to stack with existing items
        for (SavedItem saved : vaultItems) {
            if (saved.itemId.equals(itemId) && saved.stackCount < STACK_SIZE) {
                int spaceAvailable = STACK_SIZE - saved.stackCount;
                int toAdd = Math.min(spaceAvailable, remaining);
                saved.stackCount += toAdd;
                remaining -= toAdd;

                if (remaining == 0) {
                    save();
                    return 0;
                }
            }
        }

        // Add new stacks for remaining items
        while (remaining > 0 && vaultItems.size() < VAULT_MAX_SLOTS) {
            int stackSize = Math.min(STACK_SIZE, remaining);
            vaultItems.add(new SavedItem(itemId, stackSize));
            remaining -= stackSize;
            totalItemsCollected += stackSize;

            // Track rare items
            Item item = ItemRegistry.getTemplate(itemId);
            if (item != null) {
                if (item.getRarity() == Item.ItemRarity.LEGENDARY) {
                    legendaryItemsFound++;
                } else if (item.getRarity() == Item.ItemRarity.MYTHIC) {
                    mythicItemsFound++;
                }
            }
        }

        save();
        return remaining;  // Return any overflow
    }

    /**
     * Removes an item from the vault.
     *
     * @param slotIndex Index of the vault slot
     * @param count Number of items to remove (or -1 for entire stack)
     * @return The removed SavedItem with the actual count removed, or null if invalid
     */
    public SavedItem removeItemFromVault(int slotIndex, int count) {
        if (slotIndex < 0 || slotIndex >= vaultItems.size()) return null;

        SavedItem item = vaultItems.get(slotIndex);
        if (count < 0 || count >= item.stackCount) {
            // Remove entire stack
            vaultItems.remove(slotIndex);
            save();
            return item;
        } else {
            // Remove partial stack
            item.stackCount -= count;
            save();
            return new SavedItem(item.itemId, count);
        }
    }

    /**
     * Gets all vault items.
     */
    public List<SavedItem> getVaultItems() {
        return new ArrayList<>(vaultItems);
    }

    /**
     * Gets the number of used vault slots.
     */
    public int getVaultSlotCount() {
        return vaultItems.size();
    }

    /**
     * Gets the total number of items in the vault (including stacks).
     */
    public int getVaultTotalItems() {
        int total = 0;
        for (SavedItem item : vaultItems) {
            total += item.stackCount;
        }
        return total;
    }

    /**
     * Checks if the vault has room for more items.
     */
    public boolean isVaultFull() {
        return vaultItems.size() >= VAULT_MAX_SLOTS;
    }

    /**
     * Clears all vault items (for testing/reset).
     */
    public void clearVault() {
        vaultItems.clear();
        save();
        System.out.println("SaveManager: Vault cleared");
    }

    /**
     * Transfers all items from a list to the vault.
     * Used when leaving loot game to save collected items.
     *
     * @param items List of SavedItem to transfer
     * @return Number of items that couldn't be stored (overflow)
     */
    public int transferToVault(List<SavedItem> items) {
        int overflow = 0;
        for (SavedItem item : items) {
            overflow += addItemToVault(item.itemId, item.stackCount);
        }
        return overflow;
    }

    /**
     * Gets total items collected
     */
    public int getTotalItemsCollected() {
        return totalItemsCollected;
    }

    /**
     * Gets legendary items found count
     */
    public int getLegendaryItemsFound() {
        return legendaryItemsFound;
    }

    /**
     * Gets mythic items found count
     */
    public int getMythicItemsFound() {
        return mythicItemsFound;
    }

    /**
     * Checks if developer mode is enabled.
     * When enabled, chest cooldowns are reset on each game launch.
     */
    public boolean isDeveloperMode() {
        return developerMode;
    }

    /**
     * Sets developer mode.
     * When enabled, chest cooldowns are reset on each game launch.
     */
    public void setDeveloperMode(boolean enabled) {
        this.developerMode = enabled;
        save();
        System.out.println("SaveManager: Developer mode " + (enabled ? "ENABLED" : "DISABLED"));
    }

    /**
     * Toggles developer mode on/off.
     */
    public void toggleDeveloperMode() {
        setDeveloperMode(!developerMode);
    }

    /**
     * Formats time remaining as a readable string
     */
    public static String formatTimeRemaining(long millis) {
        if (millis <= 0) return "Ready!";

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }

    /**
     * Saves game data to JSON file
     */
    public void save() {
        try {
            // Ensure save directory exists
            File dir = new File(SAVE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"developerMode\": ").append(developerMode).append(",\n");
            json.append("  \"dailyChestLastOpened\": ").append(dailyChestLastOpened).append(",\n");
            json.append("  \"monthlyChestLastOpened\": ").append(monthlyChestLastOpened).append(",\n");
            json.append("  \"totalItemsCollected\": ").append(totalItemsCollected).append(",\n");
            json.append("  \"legendaryItemsFound\": ").append(legendaryItemsFound).append(",\n");
            json.append("  \"mythicItemsFound\": ").append(mythicItemsFound).append(",\n");
            json.append("  \"inventory\": [\n");

            for (int i = 0; i < inventory.size(); i++) {
                SavedItem item = inventory.get(i);
                json.append("    {\"itemId\": \"").append(escapeJson(item.itemId))
                    .append("\", \"stackCount\": ").append(item.stackCount).append("}");
                if (i < inventory.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }

            json.append("  ],\n");

            // Save vault items
            json.append("  \"vaultItems\": [\n");

            for (int i = 0; i < vaultItems.size(); i++) {
                SavedItem item = vaultItems.get(i);
                json.append("    {\"itemId\": \"").append(escapeJson(item.itemId))
                    .append("\", \"stackCount\": ").append(item.stackCount).append("}");
                if (i < vaultItems.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }

            json.append("  ]\n");
            json.append("}\n");

            Files.write(Paths.get(SAVE_DIR, SAVE_FILE), json.toString().getBytes());
            System.out.println("SaveManager: Game data saved successfully (vault: " + vaultItems.size() + " slots)");

        } catch (IOException e) {
            System.err.println("SaveManager: Error saving game data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads game data from JSON file
     */
    public void load() {
        try {
            Path path = Paths.get(SAVE_DIR, SAVE_FILE);
            if (!Files.exists(path)) {
                System.out.println("SaveManager: No save file found, starting fresh");
                return;
            }

            String content = new String(Files.readAllBytes(path));
            parseJson(content);
            System.out.println("SaveManager: Game data loaded successfully");

            // If developer mode is enabled, reset chest cooldowns on launch
            if (developerMode) {
                System.out.println("SaveManager: Developer mode active - resetting chest cooldowns");
                dailyChestLastOpened = 0;
                monthlyChestLastOpened = 0;
                // Don't call save() here to preserve the developer mode flag
                // Cooldowns will be saved naturally when chests are opened
            }

        } catch (IOException e) {
            System.err.println("SaveManager: Error loading game data: " + e.getMessage());
        }
    }

    /**
     * Simple JSON parser for save data
     */
    private void parseJson(String json) {
        // Remove whitespace and newlines for easier parsing
        json = json.trim();

        // Parse developerMode
        developerMode = parseBoolean(json, "developerMode");

        // Parse numeric values
        dailyChestLastOpened = parseLong(json, "dailyChestLastOpened");
        monthlyChestLastOpened = parseLong(json, "monthlyChestLastOpened");
        totalItemsCollected = (int) parseLong(json, "totalItemsCollected");
        legendaryItemsFound = (int) parseLong(json, "legendaryItemsFound");
        mythicItemsFound = (int) parseLong(json, "mythicItemsFound");

        // Parse inventory array
        inventory.clear();
        int invStart = json.indexOf("\"inventory\"");
        if (invStart >= 0) {
            int arrayStart = json.indexOf('[', invStart);
            int arrayEnd = findMatchingBracket(json, arrayStart);
            if (arrayStart >= 0 && arrayEnd > arrayStart) {
                String arrayContent = json.substring(arrayStart + 1, arrayEnd);
                parseInventoryItems(arrayContent, inventory);
            }
        }

        // Parse vault items array
        vaultItems.clear();
        int vaultStart = json.indexOf("\"vaultItems\"");
        if (vaultStart >= 0) {
            int arrayStart = json.indexOf('[', vaultStart);
            int arrayEnd = findMatchingBracket(json, arrayStart);
            if (arrayStart >= 0 && arrayEnd > arrayStart) {
                String arrayContent = json.substring(arrayStart + 1, arrayEnd);
                parseInventoryItems(arrayContent, vaultItems);
            }
        }

        System.out.println("SaveManager: Loaded " + inventory.size() + " inventory items, " + vaultItems.size() + " vault items");
    }

    /**
     * Finds the matching closing bracket for an opening bracket.
     */
    private int findMatchingBracket(String json, int openPos) {
        if (openPos < 0 || openPos >= json.length()) return -1;

        char openChar = json.charAt(openPos);
        char closeChar = openChar == '[' ? ']' : '}';
        int depth = 1;

        for (int i = openPos + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == openChar) depth++;
            else if (c == closeChar) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private long parseLong(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start < 0) {
            search = "\"" + key + "\" :";
            start = json.indexOf(search);
        }
        if (start >= 0) {
            start += search.length();
            // Skip whitespace
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                start++;
            }
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
                end++;
            }
            if (end > start) {
                try {
                    return Long.parseLong(json.substring(start, end));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private boolean parseBoolean(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start < 0) {
            search = "\"" + key + "\" :";
            start = json.indexOf(search);
        }
        if (start >= 0) {
            start += search.length();
            // Skip whitespace
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                start++;
            }
            // Check for "true" or "false"
            if (start + 4 <= json.length() && json.substring(start, start + 4).equalsIgnoreCase("true")) {
                return true;
            }
        }
        return false;
    }

    private void parseInventoryItems(String arrayContent, List<SavedItem> targetList) {
        int pos = 0;
        while (pos < arrayContent.length()) {
            int objStart = arrayContent.indexOf('{', pos);
            if (objStart < 0) break;

            int objEnd = arrayContent.indexOf('}', objStart);
            if (objEnd < 0) break;

            String objContent = arrayContent.substring(objStart + 1, objEnd);

            // Parse itemId
            String itemId = parseString(objContent, "itemId");
            int stackCount = (int) parseLong("{" + objContent + "}", "stackCount");

            if (itemId != null && !itemId.isEmpty()) {
                targetList.add(new SavedItem(itemId, Math.max(1, stackCount)));
            }

            pos = objEnd + 1;
        }
    }

    private String parseString(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start < 0) {
            search = "\"" + key + "\" :";
            start = json.indexOf(search);
        }
        if (start >= 0) {
            start += search.length();
            // Skip whitespace
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                start++;
            }
            if (start < json.length() && json.charAt(start) == '"') {
                start++;
                int end = json.indexOf('"', start);
                if (end > start) {
                    return json.substring(start, end);
                }
            }
        }
        return null;
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * Resets all save data (for testing)
     */
    public void resetAllData() {
        inventory.clear();
        dailyChestLastOpened = 0;
        monthlyChestLastOpened = 0;
        totalItemsCollected = 0;
        legendaryItemsFound = 0;
        mythicItemsFound = 0;
        // Note: Does NOT reset developerMode - that's intentional
        save();
    }

    /**
     * Resets chest cooldowns only (not inventory or stats)
     */
    public void resetChestCooldowns() {
        dailyChestLastOpened = 0;
        monthlyChestLastOpened = 0;
        save();
        System.out.println("SaveManager: Chest cooldowns reset");
    }
}
