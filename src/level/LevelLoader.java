package level;
import core.*;
import entity.*;
import entity.player.*;
import entity.mob.*;
import block.*;
import graphics.*;
import animation.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Loads level data from JSON files.
 * Uses a simple custom JSON parser to avoid external dependencies.
 */
public class LevelLoader {

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
     * Load only metadata (name, description) from a level file.
     * This is much faster than full loading since it doesn't parse
     * platforms, items, triggers, or blocks.
     * @param path Path to the JSON file
     * @return LevelMetadata object with name and description, or null if loading fails
     */
    public static LevelMetadata loadMetadataOnly(String path) {
        try {
            String json = new String(Files.readAllBytes(Paths.get(path)));
            return parseMetadataOnly(json);
        } catch (IOException e) {
            System.err.println("LevelLoader: Failed to load metadata from " + path);
            return null;
        }
    }

    /**
     * Parse only metadata fields from JSON (name, description).
     * This is a lightweight parse that stops early.
     */
    private static LevelMetadata parseMetadataOnly(String json) {
        LevelMetadata metadata = new LevelMetadata();

        try {
            // Simple extraction - just find the name and description fields
            metadata.name = extractStringField(json, "name");
            metadata.description = extractStringField(json, "description");
        } catch (Exception e) {
            System.err.println("LevelLoader: Failed to parse metadata");
            return null;
        }

        return metadata;
    }

    /**
     * Extract a string field value from JSON without full parsing.
     */
    private static String extractStringField(String json, String fieldName) {
        String searchKey = "\"" + fieldName + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;

        // Find the colon after the key
        int colonIndex = json.indexOf(':', keyIndex + searchKey.length());
        if (colonIndex == -1) return null;

        // Find the opening quote of the value
        int openQuote = json.indexOf('"', colonIndex + 1);
        if (openQuote == -1) return null;

        // Find the closing quote (handling escapes)
        int closeQuote = findStringEnd(json, openQuote + 1);
        if (closeQuote == -1) return null;

        return json.substring(openQuote + 1, closeQuote);
    }

    /**
     * Simple metadata container for level preview.
     */
    public static class LevelMetadata {
        public String name;
        public String description;

        public String getName() {
            return name != null ? name : "Untitled Level";
        }

        public String getDescription() {
            return description != null ? description : "";
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

            // Parse bone animation settings
            if (root.containsKey("useBoneAnimation")) data.useBoneAnimation = toBool(root.get("useBoneAnimation"));
            if (root.containsKey("boneTextureDir")) data.boneTextureDir = (String) root.get("boneTextureDir");

            // Parse sprite animation settings
            if (root.containsKey("useSpriteAnimation")) data.useSpriteAnimation = toBool(root.get("useSpriteAnimation"));
            if (root.containsKey("spriteAnimationDir")) data.spriteAnimationDir = (String) root.get("spriteAnimationDir");

            // Parse dimensions
            if (root.containsKey("levelWidth")) data.levelWidth = toInt(root.get("levelWidth"));
            if (root.containsKey("levelHeight")) data.levelHeight = toInt(root.get("levelHeight"));
            if (root.containsKey("groundY")) data.groundY = toInt(root.get("groundY"));

            // Parse scrolling/camera settings
            if (root.containsKey("scrollingEnabled")) data.scrollingEnabled = toBool(root.get("scrollingEnabled"));
            if (root.containsKey("tileBackgroundHorizontal")) data.tileBackgroundHorizontal = toBool(root.get("tileBackgroundHorizontal"));
            if (root.containsKey("tileBackgroundVertical")) data.tileBackgroundVertical = toBool(root.get("tileBackgroundVertical"));
            if (root.containsKey("verticalScrollEnabled")) data.verticalScrollEnabled = toBool(root.get("verticalScrollEnabled"));
            if (root.containsKey("verticalMargin")) data.verticalMargin = toInt(root.get("verticalMargin"));

            // Parse lighting settings
            if (root.containsKey("nightMode")) data.nightMode = toBool(root.get("nightMode"));
            if (root.containsKey("nightDarkness")) data.nightDarkness = toDouble(root.get("nightDarkness"));
            if (root.containsKey("ambientLight")) data.ambientLight = toDouble(root.get("ambientLight"));
            if (root.containsKey("playerLightEnabled")) data.playerLightEnabled = toBool(root.get("playerLightEnabled"));
            if (root.containsKey("playerLightRadius")) data.playerLightRadius = toDouble(root.get("playerLightRadius"));
            if (root.containsKey("playerLightFalloff")) data.playerLightFalloff = toDouble(root.get("playerLightFalloff"));

            // Parse parallax settings
            if (root.containsKey("parallaxEnabled")) data.parallaxEnabled = toBool(root.get("parallaxEnabled"));

            // Parse parallax layers
            if (root.containsKey("parallaxLayers")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> layerList = (List<Map<String, Object>>) root.get("parallaxLayers");
                for (Map<String, Object> pl : layerList) {
                    LevelData.ParallaxLayerData layer = new LevelData.ParallaxLayerData();
                    if (pl.containsKey("name")) layer.name = (String) pl.get("name");
                    if (pl.containsKey("imagePath")) layer.imagePath = (String) pl.get("imagePath");
                    if (pl.containsKey("depthLevel")) {
                        layer.depthLevel = (String) pl.get("depthLevel");
                        layer.applyDepthDefaults();
                    }
                    // Override defaults if explicitly specified
                    if (pl.containsKey("scrollSpeedX")) layer.scrollSpeedX = toDouble(pl.get("scrollSpeedX"));
                    if (pl.containsKey("scrollSpeedY")) layer.scrollSpeedY = toDouble(pl.get("scrollSpeedY"));
                    if (pl.containsKey("zOrder")) layer.zOrder = toInt(pl.get("zOrder"));
                    if (pl.containsKey("scale")) layer.scale = toDouble(pl.get("scale"));
                    if (pl.containsKey("opacity")) layer.opacity = toDouble(pl.get("opacity"));
                    if (pl.containsKey("tileHorizontal")) layer.tileHorizontal = toBool(pl.get("tileHorizontal"));
                    if (pl.containsKey("tileVertical")) layer.tileVertical = toBool(pl.get("tileVertical"));
                    if (pl.containsKey("offsetX")) layer.offsetX = toInt(pl.get("offsetX"));
                    if (pl.containsKey("offsetY")) layer.offsetY = toInt(pl.get("offsetY"));
                    data.parallaxLayers.add(layer);
                }
            }

            // Parse light sources
            if (root.containsKey("lightSources")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> lightList = (List<Map<String, Object>>) root.get("lightSources");
                for (Map<String, Object> l : lightList) {
                    LevelData.LightSourceData light = new LevelData.LightSourceData();
                    light.x = toInt(l.get("x"));
                    light.y = toInt(l.get("y"));
                    if (l.containsKey("lightType")) {
                        light.lightType = (String) l.get("lightType");
                        light.applyTypeDefaults();
                    }
                    // Override defaults if specified
                    if (l.containsKey("radius")) {
                        light.radius = toDouble(l.get("radius"));
                        // If falloffRadius wasn't explicitly specified, scale it proportionally
                        if (!l.containsKey("falloffRadius")) {
                            light.falloffRadius = light.radius * 2.0;
                        }
                    }
                    if (l.containsKey("falloffRadius")) light.falloffRadius = toDouble(l.get("falloffRadius"));
                    if (l.containsKey("colorRed")) light.colorRed = toInt(l.get("colorRed"));
                    if (l.containsKey("colorGreen")) light.colorGreen = toInt(l.get("colorGreen"));
                    if (l.containsKey("colorBlue")) light.colorBlue = toInt(l.get("colorBlue"));
                    if (l.containsKey("intensity")) light.intensity = toDouble(l.get("intensity"));
                    if (l.containsKey("flicker")) light.flicker = toBool(l.get("flicker"));
                    if (l.containsKey("flickerAmount")) light.flickerAmount = toDouble(l.get("flickerAmount"));
                    if (l.containsKey("flickerSpeed")) light.flickerSpeed = toDouble(l.get("flickerSpeed"));
                    data.lightSources.add(light);
                }
            }

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
                    // Skip comment entries
                    if (i.containsKey("_comment")) continue;

                    LevelData.ItemData item = new LevelData.ItemData();
                    item.x = toInt(i.get("x"));
                    item.y = toInt(i.get("y"));
                    if (i.containsKey("spritePath")) item.spritePath = (String) i.get("spritePath");
                    if (i.containsKey("itemName")) item.itemName = (String) i.get("itemName");
                    if (i.containsKey("itemType")) item.itemType = (String) i.get("itemType");
                    if (i.containsKey("itemId")) item.itemId = (String) i.get("itemId");
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

            // Parse blocks
            if (root.containsKey("blocks")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> blockList = (List<Map<String, Object>>) root.get("blocks");
                for (Map<String, Object> b : blockList) {
                    // Skip comment entries
                    if (b.containsKey("_comment")) continue;

                    LevelData.BlockData block = new LevelData.BlockData();
                    block.x = toInt(b.get("x"));
                    block.y = toInt(b.get("y"));
                    if (b.containsKey("blockType")) block.blockType = (String) b.get("blockType");
                    if (b.containsKey("useGridCoords")) block.useGridCoords = toBool(b.get("useGridCoords"));
                    // Parse optional overlay (GRASS, SNOW, ICE, MOSS, VINES)
                    if (b.containsKey("overlay")) block.overlay = (String) b.get("overlay");
                    // Parse optional color tint
                    if (b.containsKey("tintRed")) block.tintRed = toInt(b.get("tintRed"));
                    if (b.containsKey("tintGreen")) block.tintGreen = toInt(b.get("tintGreen"));
                    if (b.containsKey("tintBlue")) block.tintBlue = toInt(b.get("tintBlue"));
                    data.blocks.add(block);
                }
            }

            // Parse mobs (AI-controlled creatures/enemies)
            if (root.containsKey("mobs")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> mobList = (List<Map<String, Object>>) root.get("mobs");
                for (Map<String, Object> m : mobList) {
                    // Skip comment entries
                    if (m.containsKey("_comment")) continue;

                    LevelData.MobData mob = new LevelData.MobData();
                    mob.x = toInt(m.get("x"));
                    mob.y = toInt(m.get("y"));
                    if (m.containsKey("mobType")) mob.mobType = (String) m.get("mobType");
                    if (m.containsKey("subType")) mob.subType = (String) m.get("subType");
                    if (m.containsKey("behavior")) mob.behavior = (String) m.get("behavior");
                    if (m.containsKey("textureDir")) mob.textureDir = (String) m.get("textureDir");
                    if (m.containsKey("spriteDir")) mob.spriteDir = (String) m.get("spriteDir");
                    if (m.containsKey("wanderMinX")) mob.wanderMinX = toDouble(m.get("wanderMinX"));
                    if (m.containsKey("wanderMaxX")) mob.wanderMaxX = toDouble(m.get("wanderMaxX"));
                    if (m.containsKey("debugDraw")) mob.debugDraw = toBool(m.get("debugDraw"));
                    data.mobs.add(mob);
                }
            }

            // Parse doors (interactive door entities)
            if (root.containsKey("doors")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> doorList = (List<Map<String, Object>>) root.get("doors");
                for (Map<String, Object> d : doorList) {
                    // Skip comment entries
                    if (d.containsKey("_comment")) continue;

                    LevelData.DoorData door = new LevelData.DoorData();
                    door.x = toInt(d.get("x"));
                    door.y = toInt(d.get("y"));
                    if (d.containsKey("width")) door.width = toInt(d.get("width"));
                    if (d.containsKey("height")) door.height = toInt(d.get("height"));
                    if (d.containsKey("texturePath")) door.texturePath = (String) d.get("texturePath");
                    if (d.containsKey("linkId")) door.linkId = (String) d.get("linkId");
                    if (d.containsKey("startsOpen")) door.startsOpen = toBool(d.get("startsOpen"));
                    if (d.containsKey("locked")) door.locked = toBool(d.get("locked"));
                    if (d.containsKey("keyItemId")) door.keyItemId = (String) d.get("keyItemId");
                    if (d.containsKey("actionType")) door.actionType = (String) d.get("actionType");
                    if (d.containsKey("actionTarget")) door.actionTarget = (String) d.get("actionTarget");
                    if (d.containsKey("animationSpeed")) door.animationSpeed = (float) toDouble(d.get("animationSpeed"));
                    data.doors.add(door);
                }
            }

            // Parse buttons (interactive button/switch entities)
            if (root.containsKey("buttons")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> buttonList = (List<Map<String, Object>>) root.get("buttons");
                for (Map<String, Object> b : buttonList) {
                    // Skip comment entries
                    if (b.containsKey("_comment")) continue;

                    LevelData.ButtonData button = new LevelData.ButtonData();
                    button.x = toInt(b.get("x"));
                    button.y = toInt(b.get("y"));
                    if (b.containsKey("width")) button.width = toInt(b.get("width"));
                    if (b.containsKey("height")) button.height = toInt(b.get("height"));
                    if (b.containsKey("texturePath")) button.texturePath = (String) b.get("texturePath");
                    if (b.containsKey("linkId")) button.linkId = (String) b.get("linkId");
                    if (b.containsKey("buttonType")) button.buttonType = (String) b.get("buttonType");
                    if (b.containsKey("activatedByPlayer")) button.activatedByPlayer = toBool(b.get("activatedByPlayer"));
                    if (b.containsKey("activatedByMobs")) button.activatedByMobs = toBool(b.get("activatedByMobs"));
                    if (b.containsKey("requiresInteraction")) button.requiresInteraction = toBool(b.get("requiresInteraction"));
                    if (b.containsKey("timedDuration")) button.timedDuration = toInt(b.get("timedDuration"));
                    if (b.containsKey("actionType")) button.actionType = (String) b.get("actionType");
                    if (b.containsKey("actionTarget")) button.actionTarget = (String) b.get("actionTarget");
                    if (b.containsKey("animationSpeed")) button.animationSpeed = (float) toDouble(b.get("animationSpeed"));

                    // Parse linked door IDs array
                    if (b.containsKey("linkedDoorIds")) {
                        @SuppressWarnings("unchecked")
                        List<Object> doorIdList = (List<Object>) b.get("linkedDoorIds");
                        button.linkedDoorIds = new String[doorIdList.size()];
                        for (int i = 0; i < doorIdList.size(); i++) {
                            button.linkedDoorIds[i] = (String) doorIdList.get(i);
                        }
                    }

                    data.buttons.add(button);
                }
            }

            // Parse vaults (interactive vault/chest entities)
            if (root.containsKey("vaults")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> vaultList = (List<Map<String, Object>>) root.get("vaults");
                for (Map<String, Object> v : vaultList) {
                    // Skip comment entries
                    if (v.containsKey("_comment")) continue;

                    LevelData.VaultData vault = new LevelData.VaultData();
                    vault.x = toInt(v.get("x"));
                    vault.y = toInt(v.get("y"));
                    if (v.containsKey("width")) vault.width = toInt(v.get("width"));
                    if (v.containsKey("height")) vault.height = toInt(v.get("height"));
                    if (v.containsKey("texturePath")) vault.texturePath = (String) v.get("texturePath");
                    if (v.containsKey("linkId")) vault.linkId = (String) v.get("linkId");
                    if (v.containsKey("vaultType")) vault.vaultType = (String) v.get("vaultType");

                    data.vaults.add(vault);
                }
            }

            // Parse moving blocks (animated/moving block entities)
            if (root.containsKey("movingBlocks")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> movingBlockList = (List<Map<String, Object>>) root.get("movingBlocks");
                for (Map<String, Object> mb : movingBlockList) {
                    // Skip comment entries
                    if (mb.containsKey("_comment")) continue;

                    LevelData.MovingBlockData movingBlock = new LevelData.MovingBlockData();
                    movingBlock.x = toInt(mb.get("x"));
                    movingBlock.y = toInt(mb.get("y"));
                    if (mb.containsKey("blockType")) movingBlock.blockType = (String) mb.get("blockType");
                    if (mb.containsKey("useGridCoords")) movingBlock.useGridCoords = toBool(mb.get("useGridCoords"));
                    if (mb.containsKey("movementPattern")) movingBlock.movementPattern = (String) mb.get("movementPattern");
                    if (mb.containsKey("endX")) movingBlock.endX = toInt(mb.get("endX"));
                    if (mb.containsKey("endY")) movingBlock.endY = toInt(mb.get("endY"));
                    if (mb.containsKey("speed")) movingBlock.speed = toDouble(mb.get("speed"));
                    if (mb.containsKey("pauseTime")) movingBlock.pauseTime = toInt(mb.get("pauseTime"));
                    if (mb.containsKey("radius")) movingBlock.radius = toDouble(mb.get("radius"));
                    if (mb.containsKey("waypoints")) movingBlock.waypoints = (String) mb.get("waypoints");
                    // Parse optional tint
                    if (mb.containsKey("tintRed")) movingBlock.tintRed = toInt(mb.get("tintRed"));
                    if (mb.containsKey("tintGreen")) movingBlock.tintGreen = toInt(mb.get("tintGreen"));
                    if (mb.containsKey("tintBlue")) movingBlock.tintBlue = toInt(mb.get("tintBlue"));

                    data.movingBlocks.add(movingBlock);
                }
            }

            // Parse cutscenes (GIF-based cutscene sequences)
            if (root.containsKey("cutscenes")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> cutsceneList = (List<Map<String, Object>>) root.get("cutscenes");
                for (Map<String, Object> cs : cutsceneList) {
                    // Skip comment entries
                    if (cs.containsKey("_comment")) continue;

                    LevelData.CutsceneData cutscene = new LevelData.CutsceneData();
                    if (cs.containsKey("id")) cutscene.id = (String) cs.get("id");
                    if (cs.containsKey("playOnLevelStart")) cutscene.playOnLevelStart = toBool(cs.get("playOnLevelStart"));
                    if (cs.containsKey("playOnce")) cutscene.playOnce = toBool(cs.get("playOnce"));

                    // Parse frames array
                    if (cs.containsKey("frames")) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> frameList = (List<Map<String, Object>>) cs.get("frames");
                        for (Map<String, Object> f : frameList) {
                            LevelData.CutsceneFrameData frame = new LevelData.CutsceneFrameData();
                            if (f.containsKey("gifPath")) frame.gifPath = (String) f.get("gifPath");
                            if (f.containsKey("text")) frame.text = (String) f.get("text");
                            cutscene.frames.add(frame);
                        }
                    }

                    data.cutscenes.add(cutscene);
                }
            }

            System.out.println("LevelLoader: Loaded level '" + data.name + "' with " +
                    data.platforms.size() + " platforms, " +
                    data.items.size() + " items, " +
                    data.triggers.size() + " triggers, " +
                    data.blocks.size() + " blocks, " +
                    data.mobs.size() + " mobs, " +
                    data.doors.size() + " doors, " +
                    data.buttons.size() + " buttons, " +
                    data.vaults.size() + " vaults, " +
                    data.movingBlocks.size() + " moving blocks, " +
                    data.cutscenes.size() + " cutscenes, " +
                    data.parallaxLayers.size() + " parallax layers");

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
     * Convert Number or String to double.
     */
    private static double toDouble(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else if (obj instanceof String) {
            return Double.parseDouble((String) obj);
        }
        return 0.0;
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
        sb.append("  \"useBoneAnimation\": ").append(data.useBoneAnimation).append(",\n");
        sb.append("  \"boneTextureDir\": \"").append(escape(data.boneTextureDir)).append("\",\n");
        sb.append("  \"useSpriteAnimation\": ").append(data.useSpriteAnimation).append(",\n");
        sb.append("  \"spriteAnimationDir\": \"").append(escape(data.spriteAnimationDir)).append("\",\n");
        sb.append("  \"levelWidth\": ").append(data.levelWidth).append(",\n");
        sb.append("  \"levelHeight\": ").append(data.levelHeight).append(",\n");
        sb.append("  \"groundY\": ").append(data.groundY).append(",\n");
        sb.append("  \"scrollingEnabled\": ").append(data.scrollingEnabled).append(",\n");
        sb.append("  \"tileBackgroundHorizontal\": ").append(data.tileBackgroundHorizontal).append(",\n");
        sb.append("  \"tileBackgroundVertical\": ").append(data.tileBackgroundVertical).append(",\n");

        if (data.nextLevel != null) {
            sb.append("  \"nextLevel\": \"").append(escape(data.nextLevel)).append("\",\n");
        }

        // Parallax settings
        sb.append("  \"parallaxEnabled\": ").append(data.parallaxEnabled).append(",\n");

        // Parallax layers
        sb.append("  \"parallaxLayers\": [\n");
        for (int i = 0; i < data.parallaxLayers.size(); i++) {
            LevelData.ParallaxLayerData pl = data.parallaxLayers.get(i);
            sb.append("    { \"name\": \"").append(escape(pl.name)).append("\"");
            sb.append(", \"imagePath\": \"").append(escape(pl.imagePath)).append("\"");
            if (pl.depthLevel != null) {
                sb.append(", \"depthLevel\": \"").append(escape(pl.depthLevel)).append("\"");
            }
            sb.append(", \"scrollSpeedX\": ").append(pl.scrollSpeedX);
            sb.append(", \"scrollSpeedY\": ").append(pl.scrollSpeedY);
            sb.append(", \"zOrder\": ").append(pl.zOrder);
            sb.append(", \"scale\": ").append(pl.scale);
            sb.append(", \"opacity\": ").append(pl.opacity);
            sb.append(", \"tileHorizontal\": ").append(pl.tileHorizontal);
            sb.append(", \"tileVertical\": ").append(pl.tileVertical);
            sb.append(", \"offsetX\": ").append(pl.offsetX);
            sb.append(", \"offsetY\": ").append(pl.offsetY);
            sb.append(" }");
            if (i < data.parallaxLayers.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");

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
        sb.append("  ],\n");

        // Blocks
        sb.append("  \"blocks\": [\n");
        for (int i = 0; i < data.blocks.size(); i++) {
            LevelData.BlockData b = data.blocks.get(i);
            sb.append("    { \"x\": ").append(b.x);
            sb.append(", \"y\": ").append(b.y);
            sb.append(", \"blockType\": \"").append(escape(b.blockType)).append("\"");
            sb.append(", \"useGridCoords\": ").append(b.useGridCoords);
            // Include tint if set
            if (b.hasTint()) {
                sb.append(", \"tintRed\": ").append(b.tintRed);
                sb.append(", \"tintGreen\": ").append(b.tintGreen);
                sb.append(", \"tintBlue\": ").append(b.tintBlue);
            }
            sb.append(" }");
            if (i < data.blocks.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");

        // Moving Blocks
        sb.append("  \"movingBlocks\": [\n");
        for (int i = 0; i < data.movingBlocks.size(); i++) {
            LevelData.MovingBlockData mb = data.movingBlocks.get(i);
            sb.append("    { \"x\": ").append(mb.x);
            sb.append(", \"y\": ").append(mb.y);
            sb.append(", \"blockType\": \"").append(escape(mb.blockType)).append("\"");
            sb.append(", \"useGridCoords\": ").append(mb.useGridCoords);
            sb.append(", \"movementPattern\": \"").append(escape(mb.movementPattern)).append("\"");
            sb.append(", \"endX\": ").append(mb.endX);
            sb.append(", \"endY\": ").append(mb.endY);
            sb.append(", \"speed\": ").append(mb.speed);
            sb.append(", \"pauseTime\": ").append(mb.pauseTime);
            if (mb.movementPattern.equals("CIRCULAR")) {
                sb.append(", \"radius\": ").append(mb.radius);
            }
            if (mb.hasWaypoints()) {
                sb.append(", \"waypoints\": \"").append(escape(mb.waypoints)).append("\"");
            }
            // Include tint if set
            if (mb.hasTint()) {
                sb.append(", \"tintRed\": ").append(mb.tintRed);
                sb.append(", \"tintGreen\": ").append(mb.tintGreen);
                sb.append(", \"tintBlue\": ").append(mb.tintBlue);
            }
            sb.append(" }");
            if (i < data.movingBlocks.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");

        // Cutscenes
        sb.append("  \"cutscenes\": [\n");
        for (int i = 0; i < data.cutscenes.size(); i++) {
            LevelData.CutsceneData cs = data.cutscenes.get(i);
            sb.append("    { \"id\": \"").append(escape(cs.id)).append("\"");
            sb.append(", \"playOnLevelStart\": ").append(cs.playOnLevelStart);
            sb.append(", \"playOnce\": ").append(cs.playOnce);
            sb.append(", \"frames\": [\n");
            for (int j = 0; j < cs.frames.size(); j++) {
                LevelData.CutsceneFrameData frame = cs.frames.get(j);
                sb.append("      { \"gifPath\": \"").append(escape(frame.gifPath)).append("\"");
                if (frame.hasText()) {
                    sb.append(", \"text\": \"").append(escape(frame.text)).append("\"");
                }
                sb.append(" }");
                if (j < cs.frames.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("    ] }");
            if (i < data.cutscenes.size() - 1) sb.append(",");
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
