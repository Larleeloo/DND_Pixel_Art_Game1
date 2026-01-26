package entity.mob;

import entity.mob.mobs.humanoid.*;
import entity.mob.mobs.quadruped.*;
import entity.mob.mobs.special.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Registry for managing mob creation.
 *
 * MobRegistry provides a centralized way to create mob instances by type name.
 * All mob types are registered here and can be instantiated using create().
 * Mobs are automatically registered to the creative mode palette.
 *
 * Usage:
 *   SpriteMobEntity mob = MobRegistry.create("zombie", x, y);
 *   SpriteMobEntity mob = MobRegistry.create("wolf", x, y, "assets/mobs/wolf/alpha");
 *
 * Mob Categories:
 *   - Humanoid: zombie, skeleton, goblin, orc, bandit, knight, mage
 *   - Quadruped: wolf, bear, dog, cat, cow, pig, sheep, horse, deer, fox
 *   - Special: slime, bat, spider, dragon, ogre, troll, frog
 */
public class MobRegistry {

    /**
     * Mob behavior types for AI configuration.
     */
    public enum MobBehavior {
        HOSTILE,   // Attacks player on sight
        NEUTRAL,   // Attacks only when provoked
        PASSIVE    // Never attacks
    }

    /**
     * Data class containing all information about a registered mob.
     * Used for creative mode palette registration and level saving.
     */
    public static class MobInfo {
        public final String id;
        public final String displayName;
        public final String category;        // humanoid, quadruped, special
        public final MobBehavior behavior;
        public final String spriteDir;

        public MobInfo(String id, String displayName, String category, MobBehavior behavior) {
            this.id = id;
            this.displayName = displayName;
            this.category = category;
            this.behavior = behavior;
            this.spriteDir = "assets/mobs/" + id;
        }

        public MobInfo(String id, String displayName, String category, MobBehavior behavior, String spriteDir) {
            this.id = id;
            this.displayName = displayName;
            this.category = category;
            this.behavior = behavior;
            this.spriteDir = spriteDir;
        }

        /**
         * Gets the mobType string for level data (sprite_humanoid, sprite_quadruped, or mob id for special).
         */
        public String getLevelMobType() {
            if ("humanoid".equals(category)) {
                return "sprite_humanoid";
            } else if ("quadruped".equals(category)) {
                return "sprite_quadruped";
            } else {
                // Special mobs may have unique handling (like frog)
                return id;
            }
        }

        /**
         * Gets the behavior string for level data.
         */
        public String getBehaviorString() {
            switch (behavior) {
                case HOSTILE: return "hostile";
                case NEUTRAL: return "neutral";
                case PASSIVE: return "passive";
                default: return "hostile";
            }
        }
    }

    // Registry of mob factory functions
    // BiFunction<Integer, Integer, SpriteMobEntity> takes (x, y) and returns a mob
    private static final Map<String, BiFunction<Integer, Integer, SpriteMobEntity>> mobFactories = new HashMap<>();

    // Mob category mapping for organization
    private static final Map<String, String> mobCategories = new HashMap<>();

    // Mob info for creative palette registration
    private static final Map<String, MobInfo> mobInfoMap = new HashMap<>();

    // Ordered list for consistent palette display
    private static final List<String> mobOrder = new ArrayList<>();

    // Static initialization block to register all mobs
    static {
        initialize();
    }

    /**
     * Initializes the mob registry with all mob types.
     * All registered mobs are automatically available in the creative mode palette.
     */
    public static void initialize() {
        mobFactories.clear();
        mobCategories.clear();
        mobInfoMap.clear();
        mobOrder.clear();

        // ==================== Humanoid Mobs ====================
        registerMob("zombie", "Zombie", ZombieMob::new, "humanoid", MobBehavior.HOSTILE);
        registerMob("skeleton", "Skeleton", SkeletonMob::new, "humanoid", MobBehavior.HOSTILE);
        registerMob("goblin", "Goblin", GoblinMob::new, "humanoid", MobBehavior.HOSTILE);
        registerMob("orc", "Orc", OrcMob::new, "humanoid", MobBehavior.HOSTILE);
        registerMob("bandit", "Bandit", BanditMob::new, "humanoid", MobBehavior.HOSTILE);
        registerMob("knight", "Knight", KnightMob::new, "humanoid", MobBehavior.HOSTILE);
        registerMob("mage", "Mage", MageMob::new, "humanoid", MobBehavior.HOSTILE);

        // ==================== Quadruped Mobs ====================
        registerMob("wolf", "Wolf", WolfMob::new, "quadruped", MobBehavior.HOSTILE);
        registerMob("bear", "Bear", BearMob::new, "quadruped", MobBehavior.NEUTRAL);
        registerMob("dog", "Dog", DogMob::new, "quadruped", MobBehavior.PASSIVE);
        registerMob("cat", "Cat", CatMob::new, "quadruped", MobBehavior.PASSIVE);
        registerMob("cow", "Cow", CowMob::new, "quadruped", MobBehavior.PASSIVE);
        registerMob("pig", "Pig", PigMob::new, "quadruped", MobBehavior.PASSIVE);
        registerMob("sheep", "Sheep", SheepMob::new, "quadruped", MobBehavior.PASSIVE);
        registerMob("horse", "Horse", HorseMob::new, "quadruped", MobBehavior.PASSIVE);
        registerMob("deer", "Deer", DeerMob::new, "quadruped", MobBehavior.PASSIVE);
        registerMob("fox", "Fox", FoxMob::new, "quadruped", MobBehavior.NEUTRAL);

        // ==================== Special Mobs ====================
        registerMob("slime", "Slime", SlimeMob::new, "special", MobBehavior.HOSTILE);
        registerMob("bat", "Bat", BatMob::new, "special", MobBehavior.HOSTILE);
        registerMob("spider", "Spider", SpiderMob::new, "special", MobBehavior.HOSTILE);
        registerMob("dragon", "Dragon", DragonMob::new, "special", MobBehavior.HOSTILE);
        registerMob("ogre", "Ogre", OgreMob::new, "special", MobBehavior.HOSTILE);
        registerMob("troll", "Troll", TrollMob::new, "special", MobBehavior.HOSTILE);

        // Frog uses existing FrogSprite class with custom sprite path
        registerMob("frog", "Frog",
            (x, y) -> new FrogSprite(x, y, "assets/mobs/frog/purple_frog"),
            "special", MobBehavior.PASSIVE, "assets/mobs/frog/purple_frog");

        // Rabbit uses RabbitSprite class - passive hopping mob with no attack
        registerMob("rabbit", "Rabbit",
            (x, y) -> new RabbitSprite(x, y, "assets/mobs/rabbit"),
            "special", MobBehavior.PASSIVE, "assets/mobs/rabbit");

        System.out.println("MobRegistry: Initialized with " + mobFactories.size() + " mob types");
    }

    /**
     * Registers a mob type with full information for creative palette.
     *
     * @param id          The mob type identifier (lowercase)
     * @param displayName Display name for the palette
     * @param factory     Factory function that creates the mob
     * @param category    The mob category (humanoid, quadruped, special)
     * @param behavior    Default behavior (HOSTILE, NEUTRAL, PASSIVE)
     */
    public static void registerMob(String id, String displayName,
            BiFunction<Integer, Integer, SpriteMobEntity> factory,
            String category, MobBehavior behavior) {
        String normalizedId = id.toLowerCase();
        mobFactories.put(normalizedId, factory);
        mobCategories.put(normalizedId, category);
        mobInfoMap.put(normalizedId, new MobInfo(normalizedId, displayName, category, behavior));
        mobOrder.add(normalizedId);
    }

    /**
     * Registers a mob type with custom sprite directory.
     *
     * @param id          The mob type identifier (lowercase)
     * @param displayName Display name for the palette
     * @param factory     Factory function that creates the mob
     * @param category    The mob category (humanoid, quadruped, special)
     * @param behavior    Default behavior (HOSTILE, NEUTRAL, PASSIVE)
     * @param spriteDir   Custom sprite directory path
     */
    public static void registerMob(String id, String displayName,
            BiFunction<Integer, Integer, SpriteMobEntity> factory,
            String category, MobBehavior behavior, String spriteDir) {
        String normalizedId = id.toLowerCase();
        mobFactories.put(normalizedId, factory);
        mobCategories.put(normalizedId, category);
        mobInfoMap.put(normalizedId, new MobInfo(normalizedId, displayName, category, behavior, spriteDir));
        mobOrder.add(normalizedId);
    }

    /**
     * Legacy registration method for backwards compatibility.
     *
     * @param id       The mob type identifier (lowercase)
     * @param factory  Factory function that creates the mob
     * @param category The mob category (humanoid, quadruped, special)
     */
    public static void registerMob(String id, BiFunction<Integer, Integer, SpriteMobEntity> factory, String category) {
        // Auto-generate display name from ID
        String displayName = id.substring(0, 1).toUpperCase() + id.substring(1);
        registerMob(id, displayName, factory, category, MobBehavior.HOSTILE);
    }

    /**
     * Creates a mob by type name at the specified position.
     *
     * @param mobType The mob type (e.g., "zombie", "wolf", "dragon")
     * @param x       X position
     * @param y       Y position
     * @return A new mob instance, or null if type is unknown
     */
    public static SpriteMobEntity create(String mobType, int x, int y) {
        if (mobType == null) return null;

        String normalizedType = mobType.toLowerCase();
        BiFunction<Integer, Integer, SpriteMobEntity> factory = mobFactories.get(normalizedType);

        if (factory != null) {
            return factory.apply(x, y);
        }

        System.err.println("MobRegistry: Unknown mob type: " + mobType);
        return null;
    }

    /**
     * Creates a mob by type name with custom sprite directory.
     *
     * @param mobType   The mob type (e.g., "zombie", "wolf", "dragon")
     * @param x         X position
     * @param y         Y position
     * @param spriteDir Custom sprite directory path
     * @return A new mob instance, or null if type is unknown
     */
    public static SpriteMobEntity create(String mobType, int x, int y, String spriteDir) {
        if (mobType == null) return null;

        String normalizedType = mobType.toLowerCase();

        // Create mob using the appropriate class with custom sprites
        switch (normalizedType) {
            // Humanoid
            case "zombie": return new ZombieMob(x, y, spriteDir);
            case "skeleton": return new SkeletonMob(x, y, spriteDir);
            case "goblin": return new GoblinMob(x, y, spriteDir);
            case "orc": return new OrcMob(x, y, spriteDir);
            case "bandit": return new BanditMob(x, y, spriteDir);
            case "knight": return new KnightMob(x, y, spriteDir);
            case "mage": return new MageMob(x, y, spriteDir);

            // Quadruped
            case "wolf": return new WolfMob(x, y, spriteDir);
            case "bear": return new BearMob(x, y, spriteDir);
            case "dog": return new DogMob(x, y, spriteDir);
            case "cat": return new CatMob(x, y, spriteDir);
            case "cow": return new CowMob(x, y, spriteDir);
            case "pig": return new PigMob(x, y, spriteDir);
            case "sheep": return new SheepMob(x, y, spriteDir);
            case "horse": return new HorseMob(x, y, spriteDir);
            case "deer": return new DeerMob(x, y, spriteDir);
            case "fox": return new FoxMob(x, y, spriteDir);

            // Special
            case "slime": return new SlimeMob(x, y, spriteDir);
            case "bat": return new BatMob(x, y, spriteDir);
            case "spider": return new SpiderMob(x, y, spriteDir);
            case "dragon": return new DragonMob(x, y, spriteDir);
            case "ogre": return new OgreMob(x, y, spriteDir);
            case "troll": return new TrollMob(x, y, spriteDir);
            case "frog": return new FrogSprite(x, y, spriteDir);
            case "rabbit": return new RabbitSprite(x, y, spriteDir);

            default:
                System.err.println("MobRegistry: Unknown mob type for custom sprites: " + mobType);
                return null;
        }
    }

    /**
     * Checks if a mob type is registered.
     *
     * @param mobType The mob type to check
     * @return true if the type is registered
     */
    public static boolean isRegistered(String mobType) {
        return mobType != null && mobFactories.containsKey(mobType.toLowerCase());
    }

    /**
     * Gets the category of a mob type.
     *
     * @param mobType The mob type
     * @return The category (humanoid, quadruped, special), or null if not found
     */
    public static String getCategory(String mobType) {
        return mobType != null ? mobCategories.get(mobType.toLowerCase()) : null;
    }

    /**
     * Gets all registered mob type IDs.
     *
     * @return Set of all mob type IDs
     */
    public static Set<String> getAllMobTypes() {
        return mobFactories.keySet();
    }

    /**
     * Gets the number of registered mob types.
     *
     * @return Number of mob types
     */
    public static int getMobCount() {
        return mobFactories.size();
    }

    /**
     * Gets the MobInfo for a registered mob type.
     *
     * @param mobType The mob type ID
     * @return MobInfo containing display name, category, behavior, etc.
     */
    public static MobInfo getMobInfo(String mobType) {
        return mobType != null ? mobInfoMap.get(mobType.toLowerCase()) : null;
    }

    /**
     * Gets all mob infos in registration order.
     * Used by creative mode palette for consistent ordering.
     *
     * @return List of all MobInfo objects in registration order
     */
    public static List<MobInfo> getAllMobInfos() {
        List<MobInfo> infos = new ArrayList<>();
        for (String id : mobOrder) {
            MobInfo info = mobInfoMap.get(id);
            if (info != null) {
                infos.add(info);
            }
        }
        return infos;
    }

    /**
     * Gets the display name for a mob type.
     *
     * @param mobType The mob type ID
     * @return Display name, or the ID with first letter capitalized if not found
     */
    public static String getDisplayName(String mobType) {
        MobInfo info = getMobInfo(mobType);
        if (info != null) {
            return info.displayName;
        }
        // Fallback: capitalize first letter
        return mobType != null ? mobType.substring(0, 1).toUpperCase() + mobType.substring(1) : "Unknown";
    }

    /**
     * Gets the default behavior for a mob type.
     *
     * @param mobType The mob type ID
     * @return Default behavior (HOSTILE, NEUTRAL, PASSIVE)
     */
    public static MobBehavior getDefaultBehavior(String mobType) {
        MobInfo info = getMobInfo(mobType);
        return info != null ? info.behavior : MobBehavior.HOSTILE;
    }

    /**
     * Gets the sprite directory for a mob type.
     *
     * @param mobType The mob type ID
     * @return Sprite directory path
     */
    public static String getSpriteDir(String mobType) {
        MobInfo info = getMobInfo(mobType);
        return info != null ? info.spriteDir : "assets/mobs/" + mobType;
    }

    /**
     * Prints all registered mob types to the console.
     */
    public static void printAllMobs() {
        System.out.println("=== MobRegistry: " + mobFactories.size() + " mob types ===");
        System.out.println("\nHumanoid:");
        for (String id : mobOrder) {
            if ("humanoid".equals(mobCategories.get(id))) {
                MobInfo info = mobInfoMap.get(id);
                System.out.println("  - " + info.displayName + " (" + id + ") - " + info.behavior);
            }
        }
        System.out.println("\nQuadruped:");
        for (String id : mobOrder) {
            if ("quadruped".equals(mobCategories.get(id))) {
                MobInfo info = mobInfoMap.get(id);
                System.out.println("  - " + info.displayName + " (" + id + ") - " + info.behavior);
            }
        }
        System.out.println("\nSpecial:");
        for (String id : mobOrder) {
            if ("special".equals(mobCategories.get(id))) {
                MobInfo info = mobInfoMap.get(id);
                System.out.println("  - " + info.displayName + " (" + id + ") - " + info.behavior);
            }
        }
    }
}
