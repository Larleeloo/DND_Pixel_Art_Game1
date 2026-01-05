package save;

import entity.Item;
import entity.ItemRegistry;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * SaveManager handles persistent game data storage using JSON.
 * Manages player inventory, chest cooldowns, and other progression data.
 */
public class SaveManager {

    private static final String SAVE_DIR = "saves";
    private static final String SAVE_FILE = "player_data.json";

    private static SaveManager instance;

    // Save data
    private List<SavedItem> inventory;
    private long dailyChestLastOpened;
    private long monthlyChestLastOpened;
    private int totalItemsCollected;
    private int legendaryItemsFound;
    private int mythicItemsFound;

    // Cooldowns in milliseconds
    public static final long DAILY_COOLDOWN = 24 * 60 * 60 * 1000L; // 24 hours
    public static final long MONTHLY_COOLDOWN = 30 * 24 * 60 * 60 * 1000L; // 30 days

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
        dailyChestLastOpened = 0;
        monthlyChestLastOpened = 0;
        totalItemsCollected = 0;
        legendaryItemsFound = 0;
        mythicItemsFound = 0;
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

            json.append("  ]\n");
            json.append("}\n");

            Files.write(Paths.get(SAVE_DIR, SAVE_FILE), json.toString().getBytes());
            System.out.println("SaveManager: Game data saved successfully");

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

        // Parse dailyChestLastOpened
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
            int arrayEnd = json.indexOf(']', arrayStart);
            if (arrayStart >= 0 && arrayEnd > arrayStart) {
                String arrayContent = json.substring(arrayStart + 1, arrayEnd);
                parseInventoryItems(arrayContent);
            }
        }
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

    private void parseInventoryItems(String arrayContent) {
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
                inventory.add(new SavedItem(itemId, Math.max(1, stackCount)));
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
        save();
    }
}
