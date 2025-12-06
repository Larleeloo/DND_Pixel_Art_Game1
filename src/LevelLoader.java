import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Loads level data from JSON files.
 * Uses a simple custom JSON parser to avoid external dependencies.
 */
class LevelLoader {

    /**
     * Load a level from a JSON file.
     * @param path Path to the JSON file
     * @return LevelData object, or null if loading fails
     */
    public static LevelData load(String path) {
        try {
            String json = new String(Files.readAllBytes(Paths.get(path)));
            return parseJson(json);
        } catch (IOException e) {
            System.err.println("LevelLoader: Failed to load level from " + path);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Load a level from an input stream.
     * @param stream Input stream to read from
     * @return LevelData object, or null if loading fails
     */
    public static LevelData load(InputStream stream) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return parseJson(sb.toString());
        } catch (IOException e) {
            System.err.println("LevelLoader: Failed to load level from stream");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse JSON string into LevelData.
     */
    private static LevelData parseJson(String json) {
        LevelData data = new LevelData();

        try {
            Map<String, Object> root = parseObject(json.trim());

            // Parse metadata
            if (root.containsKey("name")) data.name = (String) root.get("name");
            if (root.containsKey("description")) data.description = (String) root.get("description");
            if (root.containsKey("backgroundPath")) data.backgroundPath = (String) root.get("backgroundPath");
            if (root.containsKey("musicPath")) data.musicPath = (String) root.get("musicPath");
            if (root.containsKey("nextLevel")) data.nextLevel = (String) root.get("nextLevel");

            // Parse player spawn
            if (root.containsKey("playerSpawnX")) data.playerSpawnX = toInt(root.get("playerSpawnX"));
            if (root.containsKey("playerSpawnY")) data.playerSpawnY = toInt(root.get("playerSpawnY"));
            if (root.containsKey("playerSpritePath")) data.playerSpritePath = (String) root.get("playerSpritePath");

            // Parse dimensions
            if (root.containsKey("levelWidth")) data.levelWidth = toInt(root.get("levelWidth"));
            if (root.containsKey("levelHeight")) data.levelHeight = toInt(root.get("levelHeight"));
            if (root.containsKey("groundY")) data.groundY = toInt(root.get("groundY"));

            // Parse scrolling/camera settings
            if (root.containsKey("scrollingEnabled")) data.scrollingEnabled = toBool(root.get("scrollingEnabled"));
            if (root.containsKey("tileBackgroundHorizontal")) data.tileBackgroundHorizontal = toBool(root.get("tileBackgroundHorizontal"));
            if (root.containsKey("tileBackgroundVertical")) data.tileBackgroundVertical = toBool(root.get("tileBackgroundVertical"));

            // Parse platforms
            if (root.containsKey("platforms")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> platformList = (List<Map<String, Object>>) root.get("platforms");
                for (Map<String, Object> p : platformList) {
                    LevelData.PlatformData platform = new LevelData.PlatformData();
                    platform.x = toInt(p.get("x"));
                    platform.y = toInt(p.get("y"));
                    if (p.containsKey("spritePath")) platform.spritePath = (String) p.get("spritePath");
                    if (p.containsKey("solid")) platform.solid = (Boolean) p.get("solid");
                    // Parse optional color mask
                    if (p.containsKey("maskRed")) platform.maskRed = toInt(p.get("maskRed"));
                    if (p.containsKey("maskGreen")) platform.maskGreen = toInt(p.get("maskGreen"));
                    if (p.containsKey("maskBlue")) platform.maskBlue = toInt(p.get("maskBlue"));
                    data.platforms.add(platform);
                }
            }

            // Parse items
            if (root.containsKey("items")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> itemList = (List<Map<String, Object>>) root.get("items");
                for (Map<String, Object> i : itemList) {
                    LevelData.ItemData item = new LevelData.ItemData();
                    item.x = toInt(i.get("x"));
                    item.y = toInt(i.get("y"));
                    if (i.containsKey("spritePath")) item.spritePath = (String) i.get("spritePath");
                    if (i.containsKey("itemName")) item.itemName = (String) i.get("itemName");
                    if (i.containsKey("itemType")) item.itemType = (String) i.get("itemType");
                    data.items.add(item);
                }
            }

            // Parse triggers
            if (root.containsKey("triggers")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> triggerList = (List<Map<String, Object>>) root.get("triggers");
                for (Map<String, Object> t : triggerList) {
                    LevelData.TriggerData trigger = new LevelData.TriggerData();
                    trigger.x = toInt(t.get("x"));
                    trigger.y = toInt(t.get("y"));
                    if (t.containsKey("width")) trigger.width = toInt(t.get("width"));
                    if (t.containsKey("height")) trigger.height = toInt(t.get("height"));
                    if (t.containsKey("type")) trigger.type = (String) t.get("type");
                    if (t.containsKey("target")) trigger.target = (String) t.get("target");
                    data.triggers.add(trigger);
                }
            }

            System.out.println("LevelLoader: Loaded level '" + data.name + "' with " +
                    data.platforms.size() + " platforms, " +
                    data.items.size() + " items, " +
                    data.triggers.size() + " triggers");

        } catch (Exception e) {
            System.err.println("LevelLoader: Failed to parse JSON");
            e.printStackTrace();
            return null;
        }

        return data;
    }

    /**
     * Convert Number or String to int.
     */
    private static int toInt(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj instanceof String) {
            return Integer.parseInt((String) obj);
        }
        return 0;
    }

    /**
     * Convert Object to boolean.
     */
    private static boolean toBool(Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if (obj instanceof String) {
            return Boolean.parseBoolean((String) obj);
        }
        return false;
    }

    /**
     * Simple JSON object parser.
     */
    private static Map<String, Object> parseObject(String json) {
        Map<String, Object> result = new HashMap<>();
        json = json.trim();

        if (!json.startsWith("{") || !json.endsWith("}")) {
            return result;
        }

        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return result;

        int i = 0;
        while (i < json.length()) {
            // Skip whitespace
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= json.length()) break;

            // Parse key
            if (json.charAt(i) != '"') break;
            int keyStart = i + 1;
            int keyEnd = json.indexOf('"', keyStart);
            String key = json.substring(keyStart, keyEnd);
            i = keyEnd + 1;

            // Skip to colon
            while (i < json.length() && json.charAt(i) != ':') i++;
            i++; // skip colon

            // Skip whitespace
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;

            // Parse value
            Object value;
            char c = json.charAt(i);

            if (c == '"') {
                // String value
                int valueStart = i + 1;
                int valueEnd = findStringEnd(json, valueStart);
                value = json.substring(valueStart, valueEnd);
                i = valueEnd + 1;
            } else if (c == '[') {
                // Array value
                int arrayEnd = findMatchingBracket(json, i, '[', ']');
                String arrayStr = json.substring(i, arrayEnd + 1);
                value = parseArray(arrayStr);
                i = arrayEnd + 1;
            } else if (c == '{') {
                // Nested object
                int objEnd = findMatchingBracket(json, i, '{', '}');
                String objStr = json.substring(i, objEnd + 1);
                value = parseObject(objStr);
                i = objEnd + 1;
            } else if (c == 't' || c == 'f') {
                // Boolean
                if (json.substring(i).startsWith("true")) {
                    value = true;
                    i += 4;
                } else {
                    value = false;
                    i += 5;
                }
            } else if (c == 'n') {
                // Null
                value = null;
                i += 4;
            } else {
                // Number
                int numEnd = i;
                while (numEnd < json.length() && (Character.isDigit(json.charAt(numEnd)) ||
                        json.charAt(numEnd) == '.' || json.charAt(numEnd) == '-')) {
                    numEnd++;
                }
                String numStr = json.substring(i, numEnd);
                if (numStr.contains(".")) {
                    value = Double.parseDouble(numStr);
                } else {
                    value = Integer.parseInt(numStr);
                }
                i = numEnd;
            }

            result.put(key, value);

            // Skip to comma or end
            while (i < json.length() && json.charAt(i) != ',' && json.charAt(i) != '}') i++;
            if (i < json.length() && json.charAt(i) == ',') i++;
        }

        return result;
    }

    /**
     * Parse a JSON array.
     */
    private static List<Object> parseArray(String json) {
        List<Object> result = new ArrayList<>();
        json = json.trim();

        if (!json.startsWith("[") || !json.endsWith("]")) {
            return result;
        }

        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return result;

        int i = 0;
        while (i < json.length()) {
            // Skip whitespace
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= json.length()) break;

            char c = json.charAt(i);
            Object value;

            if (c == '{') {
                // Object in array
                int objEnd = findMatchingBracket(json, i, '{', '}');
                String objStr = json.substring(i, objEnd + 1);
                value = parseObject(objStr);
                i = objEnd + 1;
            } else if (c == '[') {
                // Nested array
                int arrEnd = findMatchingBracket(json, i, '[', ']');
                String arrStr = json.substring(i, arrEnd + 1);
                value = parseArray(arrStr);
                i = arrEnd + 1;
            } else if (c == '"') {
                // String
                int valueStart = i + 1;
                int valueEnd = findStringEnd(json, valueStart);
                value = json.substring(valueStart, valueEnd);
                i = valueEnd + 1;
            } else if (c == 't' || c == 'f') {
                // Boolean
                if (json.substring(i).startsWith("true")) {
                    value = true;
                    i += 4;
                } else {
                    value = false;
                    i += 5;
                }
            } else if (c == 'n') {
                // Null
                value = null;
                i += 4;
            } else if (Character.isDigit(c) || c == '-') {
                // Number
                int numEnd = i;
                while (numEnd < json.length() && (Character.isDigit(json.charAt(numEnd)) ||
                        json.charAt(numEnd) == '.' || json.charAt(numEnd) == '-')) {
                    numEnd++;
                }
                String numStr = json.substring(i, numEnd);
                if (numStr.contains(".")) {
                    value = Double.parseDouble(numStr);
                } else {
                    value = Integer.parseInt(numStr);
                }
                i = numEnd;
            } else {
                i++;
                continue;
            }

            result.add(value);

            // Skip to comma or end
            while (i < json.length() && json.charAt(i) != ',' && json.charAt(i) != ']') i++;
            if (i < json.length() && json.charAt(i) == ',') i++;
        }

        return result;
    }

    /**
     * Find the end of a string (handling escape sequences).
     */
    private static int findStringEnd(String json, int start) {
        int i = start;
        while (i < json.length()) {
            if (json.charAt(i) == '\\') {
                i += 2; // Skip escaped character
            } else if (json.charAt(i) == '"') {
                return i;
            } else {
                i++;
            }
        }
        return json.length();
    }

    /**
     * Find matching closing bracket.
     */
    private static int findMatchingBracket(String json, int start, char open, char close) {
        int depth = 0;
        boolean inString = false;

        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == '\\' && inString) {
                i++; // Skip escaped character
                continue;
            }

            if (c == '"') {
                inString = !inString;
                continue;
            }

            if (!inString) {
                if (c == open) depth++;
                if (c == close) {
                    depth--;
                    if (depth == 0) return i;
                }
            }
        }
        return json.length() - 1;
    }

    /**
     * Save a LevelData object to a JSON file.
     * @param data The level data to save
     * @param path The file path to save to
     */
    public static void save(LevelData data, String path) {
        try {
            String json = toJson(data);
            Files.write(Paths.get(path), json.getBytes());
            System.out.println("LevelLoader: Saved level to " + path);
        } catch (IOException e) {
            System.err.println("LevelLoader: Failed to save level to " + path);
            e.printStackTrace();
        }
    }

    /**
     * Convert LevelData to JSON string.
     */
    public static String toJson(LevelData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"name\": \"").append(escape(data.name)).append("\",\n");
        sb.append("  \"description\": \"").append(escape(data.description)).append("\",\n");
        sb.append("  \"backgroundPath\": \"").append(escape(data.backgroundPath)).append("\",\n");
        sb.append("  \"musicPath\": \"").append(escape(data.musicPath)).append("\",\n");
        sb.append("  \"playerSpawnX\": ").append(data.playerSpawnX).append(",\n");
        sb.append("  \"playerSpawnY\": ").append(data.playerSpawnY).append(",\n");
        sb.append("  \"playerSpritePath\": \"").append(escape(data.playerSpritePath)).append("\",\n");
        sb.append("  \"levelWidth\": ").append(data.levelWidth).append(",\n");
        sb.append("  \"levelHeight\": ").append(data.levelHeight).append(",\n");
        sb.append("  \"groundY\": ").append(data.groundY).append(",\n");
        sb.append("  \"scrollingEnabled\": ").append(data.scrollingEnabled).append(",\n");
        sb.append("  \"tileBackgroundHorizontal\": ").append(data.tileBackgroundHorizontal).append(",\n");
        sb.append("  \"tileBackgroundVertical\": ").append(data.tileBackgroundVertical).append(",\n");

        if (data.nextLevel != null) {
            sb.append("  \"nextLevel\": \"").append(escape(data.nextLevel)).append("\",\n");
        }

        // Platforms
        sb.append("  \"platforms\": [\n");
        for (int i = 0; i < data.platforms.size(); i++) {
            LevelData.PlatformData p = data.platforms.get(i);
            sb.append("    { \"x\": ").append(p.x);
            sb.append(", \"y\": ").append(p.y);
            sb.append(", \"spritePath\": \"").append(escape(p.spritePath)).append("\"");
            sb.append(", \"solid\": ").append(p.solid);
            // Include color mask if set
            if (p.hasColorMask()) {
                sb.append(", \"maskRed\": ").append(p.maskRed);
                sb.append(", \"maskGreen\": ").append(p.maskGreen);
                sb.append(", \"maskBlue\": ").append(p.maskBlue);
            }
            sb.append(" }");
            if (i < data.platforms.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");

        // Items
        sb.append("  \"items\": [\n");
        for (int i = 0; i < data.items.size(); i++) {
            LevelData.ItemData item = data.items.get(i);
            sb.append("    { \"x\": ").append(item.x);
            sb.append(", \"y\": ").append(item.y);
            sb.append(", \"spritePath\": \"").append(escape(item.spritePath)).append("\"");
            sb.append(", \"itemName\": \"").append(escape(item.itemName)).append("\"");
            sb.append(", \"itemType\": \"").append(escape(item.itemType)).append("\" }");
            if (i < data.items.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");

        // Triggers
        sb.append("  \"triggers\": [\n");
        for (int i = 0; i < data.triggers.size(); i++) {
            LevelData.TriggerData t = data.triggers.get(i);
            sb.append("    { \"x\": ").append(t.x);
            sb.append(", \"y\": ").append(t.y);
            sb.append(", \"width\": ").append(t.width);
            sb.append(", \"height\": ").append(t.height);
            sb.append(", \"type\": \"").append(escape(t.type)).append("\"");
            sb.append(", \"target\": \"").append(escape(t.target)).append("\" }");
            if (i < data.triggers.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n");

        sb.append("}");
        return sb.toString();
    }

    /**
     * Escape special characters in strings for JSON.
     */
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
