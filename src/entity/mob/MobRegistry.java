package entity.mob;

import entity.mob.mobs.humanoid.*;
import entity.mob.mobs.quadruped.*;
import entity.mob.mobs.special.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Registry for managing mob creation.
 *
 * MobRegistry provides a centralized way to create mob instances by type name.
 * All mob types are registered here and can be instantiated using create().
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

    // Registry of mob factory functions
    // BiFunction<Integer, Integer, SpriteMobEntity> takes (x, y) and returns a mob
    private static final Map<String, BiFunction<Integer, Integer, SpriteMobEntity>> mobFactories = new HashMap<>();

    // Mob category mapping for organization
    private static final Map<String, String> mobCategories = new HashMap<>();

    // Static initialization block to register all mobs
    static {
        initialize();
    }

    /**
     * Initializes the mob registry with all mob types.
     */
    public static void initialize() {
        mobFactories.clear();
        mobCategories.clear();

        // ==================== Humanoid Mobs ====================
        registerMob("zombie", ZombieMob::new, "humanoid");
        registerMob("skeleton", SkeletonMob::new, "humanoid");
        registerMob("goblin", GoblinMob::new, "humanoid");
        registerMob("orc", OrcMob::new, "humanoid");
        registerMob("bandit", BanditMob::new, "humanoid");
        registerMob("knight", KnightMob::new, "humanoid");
        registerMob("mage", MageMob::new, "humanoid");

        // ==================== Quadruped Mobs ====================
        registerMob("wolf", WolfMob::new, "quadruped");
        registerMob("bear", BearMob::new, "quadruped");
        registerMob("dog", DogMob::new, "quadruped");
        registerMob("cat", CatMob::new, "quadruped");
        registerMob("cow", CowMob::new, "quadruped");
        registerMob("pig", PigMob::new, "quadruped");
        registerMob("sheep", SheepMob::new, "quadruped");
        registerMob("horse", HorseMob::new, "quadruped");
        registerMob("deer", DeerMob::new, "quadruped");
        registerMob("fox", FoxMob::new, "quadruped");

        // ==================== Special Mobs ====================
        registerMob("slime", SlimeMob::new, "special");
        registerMob("bat", BatMob::new, "special");
        registerMob("spider", SpiderMob::new, "special");
        registerMob("dragon", DragonMob::new, "special");
        registerMob("ogre", OgreMob::new, "special");
        registerMob("troll", TrollMob::new, "special");

        // Frog uses existing FrogSprite class
        registerMob("frog", (x, y) -> new FrogSprite(x, y, "assets/mobs/frog/purple_frog"), "special");

        System.out.println("MobRegistry: Initialized with " + mobFactories.size() + " mob types");
    }

    /**
     * Registers a mob type with its factory function.
     *
     * @param id       The mob type identifier (lowercase)
     * @param factory  Factory function that creates the mob
     * @param category The mob category (humanoid, quadruped, special)
     */
    public static void registerMob(String id, BiFunction<Integer, Integer, SpriteMobEntity> factory, String category) {
        String normalizedId = id.toLowerCase();
        mobFactories.put(normalizedId, factory);
        mobCategories.put(normalizedId, category);
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
     * Prints all registered mob types to the console.
     */
    public static void printAllMobs() {
        System.out.println("=== MobRegistry: " + mobFactories.size() + " mob types ===");
        System.out.println("\nHumanoid:");
        for (String id : mobFactories.keySet()) {
            if ("humanoid".equals(mobCategories.get(id))) {
                System.out.println("  - " + id);
            }
        }
        System.out.println("\nQuadruped:");
        for (String id : mobFactories.keySet()) {
            if ("quadruped".equals(mobCategories.get(id))) {
                System.out.println("  - " + id);
            }
        }
        System.out.println("\nSpecial:");
        for (String id : mobFactories.keySet()) {
            if ("special".equals(mobCategories.get(id))) {
                System.out.println("  - " + id);
            }
        }
    }
}
