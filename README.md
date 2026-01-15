TITLE	The Amber Moon
AUTHOR Larson Sonderman & CLAUDE AI

DESCRIPTION
A Java Swing-based game engine that runs a 2D platformer game. Plays similarly to Terraria, Super Mario, or 2D Minecraft.
Inspired by Dungeons and Dragons games like Baldur's Gate 3. (Non-turn-based)
This game should feel abundant with the quantity and variations in items and tools.

CURRENT FEATURES
================

This section provides comprehensive documentation for developers working on The Amber Moon.
All systems are designed to be modular and extensible.

--------------------------------------------------------------------------------
1. CORE ARCHITECTURE
--------------------------------------------------------------------------------

The game uses a Java Swing-based engine with a 60 FPS game loop. The architecture
follows these key patterns:

DESIGN PATTERNS USED:
  - Singleton: SceneManager, BlockRegistry, SaveManager, InputManager
  - Interface-based polymorphism: Scene, PlayerBase, CombatCapable, ResourceManager
  - State Machine: Mob AI (IDLE → WANDER → CHASE → ATTACK states)
  - Observer: Input events propagate through SceneManager to active scene

GAME LOOP FLOW:
  1. GamePanel (main rendering surface) runs at 60 FPS
  2. SceneManager (singleton) manages scene transitions with fade effects
  3. Scene.update(input) called each frame for game logic
  4. Scene.draw(g) called each frame for rendering
  5. EntityManager updates all entities and handles physics

KEY ENTRY POINTS:
  - Main.java                    → Application entry point
  - GameWindow.java              → Creates borderless 1920x1080 window
  - GamePanel.java               → Game loop, scene initialization
  - SceneManager.getInstance()   → Access current scene and transitions

COORDINATE SYSTEM:
  - Origin (0,0) at top-left of screen
  - X increases rightward, Y increases downward
  - Screen resolution: 1920x1080
  - Block scale factor: 4x (16px base → 64px rendered)
  - Default ground Y: 720 pixels

--------------------------------------------------------------------------------
2. SCENE SYSTEM (scene/)
--------------------------------------------------------------------------------

All game states are managed through the Scene interface. SceneManager handles
transitions between scenes with configurable fade effects.

AVAILABLE SCENES:
  Scene Class                    | Purpose
  -------------------------------|------------------------------------------
  MainMenuScene                  | Title screen with navigation buttons
  LevelSelectionScene            | Browse and launch levels from levels/
  SpriteCharacterCustomization   | Character creation with equipment
  OverworldScene                 | Open world map navigation
  GameScene                      | Main gameplay with JSON-loaded levels
  LootGameScene                  | Mini-game with treasure chests and vault
  CreativeScene                  | Sandbox/creative mode

SCENE INTERFACE METHODS:
  - init()              → Called once when entering scene
  - update(input)       → Game logic, called every frame
  - draw(g)             → Rendering, called every frame
  - dispose()           → Cleanup when leaving scene
  - onMouse*()          → Mouse event handlers

SCENE TRANSITIONS:
  SceneManager.getInstance().loadScene("scene_name");
  // Transition speed configurable (default 0.12 for snappy transitions)

--------------------------------------------------------------------------------
3. ENTITY SYSTEM (entity/)
--------------------------------------------------------------------------------

All game objects inherit from the Entity base class. EntityManager maintains
and updates all entities in the current scene.

ENTITY HIERARCHY:
  Entity (abstract)
  ├── SpriteEntity
  │   ├── SpritePlayerEntity     → Modern GIF-based player (RECOMMENDED)
  │   ├── PlayerEntity           → Legacy static PNG player
  │   ├── GroundEntity           → Ground/platform sprites
  │   └── ItemEntity             → Dropped items in world
  ├── MobEntity
  │   ├── SpriteMobEntity        → Modern GIF-based enemies (RECOMMENDED)
  │   └── old/                   → Deprecated bone-based mobs
  │       ├── HumanoidMobEntity  → [DEPRECATED]
  │       └── QuadrupedMobEntity → [DEPRECATED]
  ├── ProjectileEntity           → Arrows, fireballs, thrown items
  ├── BlockEntity                → Placeable/mineable blocks
  ├── LootChestEntity            → Treasure chests
  ├── DoorEntity                 → Interactive doors
  ├── TriggerEntity              → Invisible trigger zones
  └── VaultEntity                → Persistent storage chests

ENTITY CAPABILITIES (interfaces in entity/capabilities/):
  - CombatCapable        → attack(), takeDamage(), getAttackDamage()
  - ResourceManager      → getHealth(), getMana(), getStamina()
  - BlockInteractionHandler → handleBlockInteraction()

ENTITY MANAGER USAGE:
  EntityManager em = new EntityManager();
  em.addEntity(entity);
  em.update(input);  // Updates all entities
  em.draw(g);        // Renders all entities
  em.removeDeadEntities();  // Cleanup

--------------------------------------------------------------------------------
4. PLAYER SYSTEM (entity/player/)
--------------------------------------------------------------------------------

Two player implementations exist. SpritePlayerEntity is the modern, recommended
approach using GIF animations.

SPRITE PLAYER ENTITY (Recommended):
  - Location: entity/player/SpritePlayerEntity.java
  - Uses GIF sprite animations (idle.gif, walk.gif, jump.gif, etc.)
  - Supports equipment overlay system (armor renders on top of player)
  - Multi-jump support (configurable: double, triple jump)
  - Sprint with stamina drain
  - Full combat: melee attacks, ranged projectiles, charged shots
  - Block interaction: mining and placement

PLAYER BASE INTERFACE:
  All player implementations provide these methods:
  - getHealth(), getMaxHealth(), takeDamage(amount)
  - getMana(), getMaxMana(), useMana(amount)
  - getStamina(), getMaxStamina(), useStamina(amount)
  - getInventory() → Player's inventory object
  - isFacingRight() → Direction player is facing
  - isInvincible() → True during damage immunity frames

RESOURCE SYSTEM:
  Resource  | Default Max | Usage
  ----------|-------------|----------------------------------------
  Health    | 100         | Decreases from damage, restored by potions
  Mana      | 100         | Used for magic attacks, auto-regenerates
  Stamina   | 100         | Used for sprinting, regenerates when idle

MOVEMENT CONTROLS:
  Key       | Action
  ----------|----------------------------------------
  A/D       | Move left/right
  Space     | Jump (press again in air for multi-jump)
  Shift     | Sprint (drains stamina)
  W/S       | Climb ladders (when implemented)

COMBAT CONTROLS:
  Key       | Action
  ----------|----------------------------------------
  Left Click| Attack/Mine/Place blocks
  Hold Left | Charge attack (bows, magic staffs)
  E         | Interact/Mine selected block
  Arrows    | Change mining direction

--------------------------------------------------------------------------------
5. COMBAT SYSTEM
--------------------------------------------------------------------------------

Combat is handled through the CombatCapable interface. Both players and mobs
can implement this interface to participate in combat.

COMBAT CAPABLE INTERFACE:
  - attack()                           → Initiate attack
  - isAttacking()                      → Check if currently attacking
  - getAttackDamage()                  → Base damage value
  - getAttackRange()                   → Melee attack distance in pixels
  - getAttackCooldown()                → Frames between attacks
  - takeDamage(damage, knockbackX, knockbackY) → Receive damage

ATTACK TYPES:
  1. MELEE ATTACKS
     - Close range physical attacks
     - Damage = baseDamage + weaponDamage
     - Range typically 50-100 pixels

  2. RANGED ATTACKS
     - Fire ProjectileEntity toward cursor
     - Projectile handles collision and damage
     - Bows consume arrows, magic consumes mana

  3. CHARGED ATTACKS
     - Hold attack button to charge (up to 5 seconds)
     - Damage multiplier: 1x (no charge) to 3x (full charge)
     - Visual feedback: projectile grows during charge
     - Mana/arrow cost scales with charge level

DAMAGE CALCULATION:
  finalDamage = baseDamage * weaponMultiplier * chargeMultiplier * critMultiplier
  - Crit chance and multiplier configurable per weapon
  - Knockback pushes target away from attacker
  - Invincibility frames prevent damage stacking

--------------------------------------------------------------------------------
6. PROJECTILE SYSTEM (entity/ProjectileEntity.java)
--------------------------------------------------------------------------------

Projectiles are fired by players and mobs for ranged attacks. Each projectile
type has unique properties and behaviors.

PROJECTILE TYPES:
  Type           | Source              | Properties
  ---------------|---------------------|--------------------------------
  ARROW          | Bows                | Gravity, consumes arrows
  BOLT           | Crossbows           | Fast, flat trajectory
  MAGIC_BOLT     | Wands               | No gravity, mana cost
  FIREBALL       | Fire Staff          | Explosion on impact, burn effect
  ICEBALL        | Ice Staff           | Freeze effect on hit
  THROWING_KNIFE | Thrown              | Fast, small hitbox
  THROWING_AXE   | Thrown              | Slower, more damage
  ROCK           | Thrown              | Gravity, blunt damage
  POTION         | Thrown              | Splash effect on impact
  BOMB           | Thrown              | Explosion radius
  FISH           | Mirror to Realms    | Special weapon projectile

PROJECTILE PROPERTIES:
  - velocityX, velocityY    → Movement per frame
  - gravity                 → Vertical acceleration (optional)
  - damage                  → Damage on hit
  - knockbackX, knockbackY  → Push force on hit
  - lifetime                → Frames before despawn
  - piercing                → Can pass through enemies
  - explosionRadius         → Area damage on impact

STATUS EFFECTS (applied on hit):
  Effect   | Duration | Damage/Tick | Special
  ---------|----------|-------------|------------------
  BURNING  | 3 sec    | 5 damage    | Orange tint, fire particles
  FROZEN   | 4 sec    | 3 damage    | Blue tint, 50% slow, ice particles
  POISONED | 5 sec    | 2 damage    | Green tint, bubble particles

CREATING PROJECTILES:
  ProjectileEntity proj = new ProjectileEntity(
      x, y,                    // Start position
      velocityX, velocityY,    // Direction and speed
      ProjectileType.ARROW,    // Type
      damage,                  // Damage amount
      owner                    // Entity that fired it
  );
  entityManager.addEntity(proj);

--------------------------------------------------------------------------------
7. INVENTORY SYSTEM (ui/Inventory.java)
--------------------------------------------------------------------------------

The inventory manages player items with drag-and-drop support, hotbar quick
access, and vault integration for persistent storage.

INVENTORY STRUCTURE:
  - 32 item slots in 8x4 grid
  - 5-slot hotbar for quick access (bottom of inventory)
  - Items stack up to their maxStackSize
  - Scroll wheel cycles hotbar selection

INVENTORY INTERACTIONS:
  Action        | Result
  --------------|----------------------------------------
  Left Click    | Auto-equip item to hotbar
  Drag & Drop   | Move item between slots
  Right Click   | Drop item to world
  Mouse Wheel   | Cycle hotbar / scroll inventory
  E Key         | Equip hovered item
  F Key         | Equip to specific slot (with item selected)

VAULT INVENTORY (ui/VaultInventory.java):
  - Up to 10,000 slots persistent storage
  - Accessed via VaultEntity (treasure chests)
  - Items persist across game sessions (JSON save)
  - Sorting: by rarity or alphabetical
  - Drag/drop between vault and player inventory

INVENTORY METHODS:
  Inventory inv = player.getInventory();
  inv.addItem(item, count);      // Add items, returns leftover
  inv.removeItem(item, count);   // Remove items
  inv.getHotbarItem(slot);       // Get item in hotbar slot 0-4
  inv.getSelectedItem();         // Currently selected hotbar item
  inv.hasItem(item, count);      // Check if has enough

--------------------------------------------------------------------------------
8. ITEM SYSTEM (entity/Item.java, entity/ItemRegistry.java)
--------------------------------------------------------------------------------

Items are defined with categories, rarities, and various properties. The
ItemRegistry contains all predefined items.

ITEM CATEGORIES (ItemCategory enum):
  Category      | Description
  --------------|----------------------------------------
  WEAPON        | Melee weapons (swords, axes, maces)
  RANGED_WEAPON | Bows, crossbows, wands, staffs
  TOOL          | Pickaxes, shovels, axes
  ARMOR         | Helmets, chestplates, leggings, boots
  CLOTHING      | Cosmetic equipment
  BLOCK         | Placeable blocks
  FOOD          | Consumable food items
  POTION        | Healing and effect potions
  MATERIAL      | Crafting materials
  KEY           | Door/chest keys
  ACCESSORY     | Rings, amulets, belts
  THROWABLE     | Grenades, throwing knives

ITEM RARITY (ItemRarity enum):
  Rarity    | Color  | Drop Rate
  ----------|--------|----------
  COMMON    | White  | 50%
  UNCOMMON  | Green  | 25%
  RARE      | Blue   | 15%
  EPIC      | Purple | 7%
  LEGENDARY | Orange | 2.5%
  MYTHIC    | Cyan   | 0.5%

ITEM PROPERTIES:
  - damage, defense           → Combat stats
  - attackSpeed, range        → Combat mechanics
  - critChance, critMultiplier→ Critical hit stats
  - healthRestore, manaRestore→ Consumable effects
  - specialEffect             → Unique item abilities
  - maxStackSize              → Inventory stacking limit

ITEM ANIMATION STATES (for held item overlays):
  - IDLE, USE, BREAK          → General usage
  - ATTACK, BLOCK, CRITICAL   → Combat
  - DRAW, FIRE, RELOAD        → Ranged weapons
  - CHARGE, CAST, GLOW        → Magic
  - MINE, CHOP, IMPACT        → Tools

CREATING NEW ITEMS:
  // In ItemRegistry.initialize():
  Item flameSword = new Item("Flame Sword", ItemCategory.WEAPON, ItemRarity.EPIC);
  flameSword.setDamage(45);
  flameSword.setAttackSpeed(1.2);
  flameSword.setSpecialEffect("burn");
  flameSword.setSpritePath("assets/items/flame_sword.gif");
  register(flameSword);

NOTABLE SPECIAL ITEMS:
  - Mirror to Other Realms: Cycles through 3 realms every 400ms
    - Volcano Realm → Fires fireballs
    - Forest Realm → Fires arrows
    - Ocean Realm → Fires tiny fish
    - Fires 3 projectiles per use, 25 mana cost

--------------------------------------------------------------------------------
9. MOB/ENEMY SYSTEM (entity/mob/)
--------------------------------------------------------------------------------

Enemies use an AI state machine for behavior. SpriteMobEntity is the modern
GIF-based implementation (recommended over deprecated bone-based mobs).

AI STATE MACHINE:
  State  | Behavior
  -------|------------------------------------------------
  IDLE   | Stand still, wait for player detection
  WANDER | Move randomly within area
  CHASE  | Move toward detected player
  ATTACK | Attack when in range
  HURT   | Flinch after taking damage
  FLEE   | Run away when health low
  DEAD   | Death animation, then despawn

AI CONFIGURATION:
  SpriteMobEntity mob = new SpriteMobEntity(x, y, "assets/mobs/zombie");
  mob.setDetectionRange(300);      // Pixels to detect player
  mob.setLoseTargetRange(500);     // Pixels to lose player
  mob.setWanderSpeed(1.0);         // Movement speed when wandering
  mob.setChaseSpeed(2.5);          // Movement speed when chasing
  mob.setAttackDamage(15);         // Damage per attack
  mob.setAttackRange(60);          // Melee attack distance
  mob.setAttackCooldown(60);       // Frames between attacks
  mob.setRangedAttack(true);       // Enable projectile attacks
  mob.setProjectileType(ProjectileType.ARROW);

AUTO-CONFIGURED MOB TYPES:
  Mob type is auto-detected from sprite directory name:

  Type     | Health | Damage | Speed | Special
  ---------|--------|--------|-------|------------------
  zombie   | 50     | 10     | 1.5   | Slow, persistent
  skeleton | 40     | 15     | 2.0   | Ranged attacks
  goblin   | 35     | 12     | 2.5   | Fast, weak
  orc      | 80     | 20     | 1.8   | Tank, strong
  bandit   | 45     | 18     | 2.2   | Balanced
  knight   | 100    | 25     | 1.5   | Heavy armor
  mage     | 30     | 30     | 1.0   | Magic attacks
  wolf     | 40     | 15     | 3.0   | Fast, pack AI
  bear     | 120    | 35     | 1.2   | Boss-tier

REQUIRED MOB SPRITE FILES:
  assets/mobs/[mob_name]/
  ├── idle.gif      → Standing animation
  ├── walk.gif      → Walking animation
  ├── attack.gif    → Attack animation
  ├── hurt.gif      → Damage reaction
  ├── death.gif     → Death animation
  ├── burning.gif   → (Optional) On fire variant
  └── frozen.gif    → (Optional) Frozen variant

--------------------------------------------------------------------------------
10. ANIMATION SYSTEM (animation/)
--------------------------------------------------------------------------------

The game supports both modern GIF-based animations and legacy bone-based
animations. GIF sprites are the recommended approach.

SPRITE ANIMATION (animation/SpriteAnimation.java):
  Manages GIF-based animations for different action states.

ACTION STATES (ActionState enum):
  Movement: IDLE, WALK, RUN, SPRINT
  Jumping:  JUMP, DOUBLE_JUMP, TRIPLE_JUMP, FALL
  Combat:   ATTACK, FIRE, BLOCK, CAST
  Items:    USE_ITEM, EAT
  Reactions: HURT, DEAD
  Effects:  BURNING, FROZEN, POISONED

ANIMATION FEATURES:
  - Automatic frame cycling with configurable timing
  - Direction flipping for left/right facing
  - Smooth state transitions
  - Frame synchronization for overlay alignment

EQUIPMENT OVERLAY (animation/EquipmentOverlay.java):
  Renders equipment on top of the player sprite:

  Slot       | Render Order | Description
  -----------|--------------|---------------------------
  HAIR_BACK  | Behind       | Ponytails, long hair back
  BOOTS      | 1            | Footwear
  LEGS       | 2            | Pants, leggings
  BELT       | 3            | Belts, sashes
  CHEST      | 4            | Shirts, armor
  HELMET     | 5            | Hats, helmets
  HAIR_FRONT | 6            | Bangs, front hair
  HELD_ITEM  | 7            | Currently held item

ANIMATED TEXTURE (animation/AnimatedTexture.java):
  - Extracts frames from GIF files
  - Respects per-frame delays from GIF metadata
  - Provides getCurrentFrame() for rendering
  - update() method advances animation

LEGACY BONE ANIMATION (animation/bone/):
  - Skeleton-based hierarchical animation
  - Keyframe blending between poses
  - Still functional but deprecated
  - Use for reference only

--------------------------------------------------------------------------------
11. BLOCK SYSTEM (block/)
--------------------------------------------------------------------------------

Blocks form the destructible/constructible terrain. Each block type has
unique textures and properties.

BLOCK TYPES (BlockType enum):
  Type        | Solid | Description
  ------------|-------|---------------------------
  GRASS       | Yes   | Dirt with grass overlay
  DIRT        | Yes   | Basic terrain
  STONE       | Yes   | Hard rock
  COBBLESTONE | Yes   | Broken stone
  WOOD        | Yes   | Tree wood
  LEAVES      | Yes   | Tree foliage
  BRICK       | Yes   | Construction
  SAND        | Yes   | Beach/desert
  WATER       | No    | Liquid (non-solid)
  GLASS       | No    | Transparent
  COAL_ORE    | Yes   | Mineable ore
  IRON_ORE    | Yes   | Mineable ore
  GOLD_ORE    | Yes   | Mineable ore
  PLATFORM    | Semi  | One-way platform

BLOCK OVERLAYS (BlockOverlay enum):
  Overlays render on top of base blocks and must be removed before mining:
  - GRASS  → Green grass tufts on top
  - SNOW   → White snow layer
  - ICE    → Semi-transparent ice coating
  - MOSS   → Green moss patches
  - VINES  → Hanging vine decoration

BLOCK INTERACTION:
  1. Click block within 3-block radius to select
  2. Selected block shows yellow highlight
  3. Arrow keys change mining direction indicator
  4. Click again (or press E) to mine from chosen direction
  5. If block has overlay, overlay breaks first

BLOCK PLACEMENT:
  - Player must hold a BLOCK category item
  - Left click empty space within 3-block radius
  - Cannot place where player is standing
  - Cannot place on existing blocks

BLOCK REGISTRY (Singleton):
  - Caches block textures (loaded once, shared)
  - Pre-scales textures to 64x64 rendered size
  - Supports animated blocks (GIF textures)
  - Fallback magenta/black checkerboard for missing textures

--------------------------------------------------------------------------------
12. LEVEL SYSTEM (level/)
--------------------------------------------------------------------------------

Levels are defined in JSON files in the levels/ directory. LevelLoader parses
these files and creates the game world.

LEVEL JSON STRUCTURE:
  {
    "name": "Level Name",
    "description": "Level description",

    // Player spawn
    "playerSpawnX": 100,
    "playerSpawnY": 620,
    "playerSpritePath": "assets/player/sprites",
    "useSpriteAnimation": true,

    // Level dimensions
    "levelWidth": 1920,
    "levelHeight": 1080,
    "groundY": 720,

    // Camera settings
    "scrollingEnabled": true,
    "verticalScrollEnabled": false,
    "verticalMargin": 0,

    // Lighting
    "nightMode": false,
    "nightDarkness": 0.8,
    "ambientLight": 0.12,
    "playerLightEnabled": false,

    // Parallax background
    "parallaxEnabled": true,
    "parallaxLayers": [ /* layer data */ ],

    // Game objects
    "platforms": [ /* platform data */ ],
    "blocks": [ /* block grid */ ],
    "items": [ /* item spawns */ ],
    "mobs": [ /* enemy spawns */ ],
    "doors": [ /* door connections */ ],
    "buttons": [ /* switches */ ],
    "vaults": [ /* chest locations */ ],
    "triggers": [ /* trigger zones */ ]
  }

LEVEL DATA CLASSES:
  - PlatformData    → Sprite-based obstacles
  - BlockData       → Block type and position
  - ItemData        → Item spawn with rarity
  - MobData         → Enemy type, position, AI config
  - DoorData        → Door ID and destination link
  - ButtonData      → Switch with linked door ID
  - VaultData       → Chest type (daily, monthly, regular)
  - TriggerData     → Zone bounds and action
  - LightSourceData → Position, color, radius
  - ParallaxLayerData → Image path, depth, scroll speed

LEVEL LOADER USAGE:
  LevelData level = LevelLoader.loadLevel("levels/forest_1.json");
  // Access level.mobs, level.blocks, level.items, etc.

--------------------------------------------------------------------------------
13. GRAPHICS SYSTEM (graphics/)
--------------------------------------------------------------------------------

Handles rendering, camera, lighting, and parallax backgrounds.

CAMERA (Camera.java):
  - Follows player with smooth interpolation
  - Dead zone prevents jitter on small movements
  - Clamps to level bounds
  - Converts screen ↔ world coordinates

  Camera camera = new Camera(screenWidth, screenHeight);
  camera.setTarget(player);
  camera.update();

  // In rendering:
  g.translate(-camera.getX(), -camera.getY());
  // Draw world objects
  g.translate(camera.getX(), camera.getY());
  // Draw UI (screen space)

LIGHTING SYSTEM (LightingSystem.java):
  - Day/night cycle support
  - Dynamic point light sources
  - Ambient lighting level
  - Darkness overlay with light cutouts

  LightingSystem lighting = new LightingSystem();
  lighting.setDarknessLevel(0.7);  // 70% dark
  lighting.addLightSource(new LightSource(x, y, radius, color));
  lighting.draw(g, camera);

PARALLAX BACKGROUND (ParallaxBackground.java):
  Layers at different depths scroll at different speeds:

  Depth Level      | Z-Index | Scroll Speed
  -----------------|---------|-------------
  Z_BACKGROUND     | -2      | 0.1x (slowest)
  Z_MIDDLEGROUND_3 | -1      | 0.3x
  Z_MIDDLEGROUND_2 | 0       | 0.5x
  Z_MIDDLEGROUND_1 | 1       | 0.7x
  Z_FOREGROUND     | 2       | 1.2x (fastest)

ASSET LOADER (AssetLoader.java):
  Static methods for loading images and GIFs:

  ImageAsset asset = AssetLoader.loadAsset("path/to/image.png");
  BufferedImage img = asset.getImage();

  // For GIFs:
  ImageAsset gifAsset = AssetLoader.loadAsset("path/to/anim.gif");
  AnimatedTexture anim = gifAsset.getAnimatedTexture();
  anim.update();  // Call each frame
  BufferedImage frame = anim.getCurrentFrame();

RENDER PIPELINE ORDER:
  1. Clear screen
  2. Draw parallax background layers (world space)
  3. Draw game entities (world space, camera transformed)
  4. Draw blocks and terrain
  5. Draw lighting overlay
  6. Draw UI elements (screen space)
  7. Draw settings/menu overlays

--------------------------------------------------------------------------------
14. INPUT SYSTEM (input/InputManager.java)
--------------------------------------------------------------------------------

Singleton that captures all keyboard and mouse input. Implements KeyListener,
MouseListener, MouseWheelListener, and MouseMotionListener.

KEYBOARD INPUT:
  InputManager input = InputManager.getInstance();

  // Check if key currently held
  if (input.isKeyPressed('a')) { /* move left */ }
  if (input.isKeyPressed(' ')) { /* space held */ }

  // Check if key just pressed this frame (one-shot)
  if (input.isKeyJustPressed('e')) { /* interact */ }

  // Special keys via KeyEvent codes
  if (input.isKeyPressed(KeyEvent.VK_SHIFT)) { /* sprinting */ }
  if (input.isKeyPressed(KeyEvent.VK_LEFT))  { /* arrow key */ }

MOUSE INPUT:
  int mouseX = input.getMouseX();
  int mouseY = input.getMouseY();

  if (input.isLeftMousePressed()) { /* attack/mine */ }
  if (input.isRightMousePressed()) { /* place/drop */ }
  if (input.isLeftMouseJustPressed()) { /* one-shot click */ }

SCROLL WHEEL:
  int scroll = input.getScrollDirection();
  // Returns: -1 (up), 0 (none), 1 (down)
  // Has threshold (1.5) to prevent accidental scrolling

UI CLICK CONSUMPTION:
  When UI handles a click, it should consume it to prevent game actions:

  // In UI button handler:
  input.consumeLeftClick();

  // In game logic:
  if (!input.isLeftClickConsumed()) {
      // Process game click
  }

--------------------------------------------------------------------------------
15. SAVE SYSTEM (save/SaveManager.java)
--------------------------------------------------------------------------------

Singleton that handles JSON-based persistence of player data, inventory,
and vault contents.

SAVE FILE LOCATION: saves/player_data.json

SAVED DATA:
  - Player inventory items with stack counts
  - Vault inventory (up to 10,000 items)
  - Chest cooldowns (daily/monthly)
  - Statistics (items collected, legendary count)
  - Developer mode flag
  - Equipment loadout
  - Character customization (skin tone, hair color)

SAVE MANAGER USAGE:
  SaveManager save = SaveManager.getInstance();

  // Save current state
  save.saveInventory(player.getInventory());
  save.saveVault(vaultInventory);

  // Load state
  save.loadInventory(player.getInventory());
  VaultInventory vault = save.loadVault();

  // Check chest cooldowns
  if (save.canOpenDailyChest()) {
      // Open chest
      save.recordDailyChestOpened();
  }

CHEST COOLDOWNS:
  - DAILY_COOLDOWN = 24 hours
  - MONTHLY_COOLDOWN = 30 days
  - Cooldowns reset in developer mode for testing

--------------------------------------------------------------------------------
16. AUDIO SYSTEM (audio/AudioManager.java, audio/SoundAction.java)
--------------------------------------------------------------------------------

Manages background music and sound effects with MP3 support via JLayer.
Supports both WAV files (legacy) and MP3 files (recommended for compression).

JLAYER SETUP (Required for MP3 support):
  1. Download jlayer-1.0.1.jar from:
     https://repo1.maven.org/maven2/javazoom/jlayer/1.0.1/jlayer-1.0.1.jar
  2. Place the JAR in the lib/ folder
  3. Add lib/jlayer-1.0.1.jar to your project classpath:
     - IntelliJ: File > Project Structure > Modules > Dependencies > + > JARs
     - Eclipse: Project > Properties > Java Build Path > Libraries > Add JARs
     - VS Code: Add "lib/**/*.jar" to java.project.referencedLibraries
  4. See lib/README.md for detailed IDE setup instructions

SUPPORTED FORMATS:
  - MP3 files (via JLayer) - recommended for compressed audio
  - WAV files (via javax.sound.sampled) - legacy support
  - Automatic fallback: tries MP3 first, then WAV

AUDIO MANAGER USAGE:
  AudioManager audio = new AudioManager();

  // Action-based playback (RECOMMENDED)
  // Uses SoundAction enum for type-safe sound routing
  audio.playAction(SoundAction.JUMP);
  audio.playAction(SoundAction.USE_BATTLE_AXE);
  audio.playAction(SoundAction.OPEN_MONTHLY_CHEST);
  audio.playAction(SoundAction.MUSIC_LEVEL_1);

  // String-based action playback
  audio.playAction("JUMP");
  audio.playAction("USE_SWORD");

  // Direct MP3 playback
  audio.playMP3Sound("sounds/compressed/player/jump.mp3");

  // Legacy WAV playback (still supported)
  audio.playSound("jump");
  audio.playSoundFromPath("sounds/jump.wav");

  // Music control
  audio.loadMusicMP3("sounds/compressed/music/music_level_1.mp3");
  audio.loadMusic("sounds/music.wav");  // WAV fallback
  audio.playMusic();
  audio.stopMusic();
  audio.setMusicVolume(0.7f);  // 0.0 to 1.0

  // SFX volume
  audio.setSFXVolume(0.8f);

  // Mute controls
  audio.setMuteAll(true);
  audio.toggleMusic();
  audio.toggleSFX();

SOUND ACTION CATEGORIES:
  The SoundAction enum provides 300+ predefined game actions organized by category:

  Category     | Examples
  -------------|--------------------------------------------------
  player       | JUMP, DOUBLE_JUMP, WALK, RUN, HURT, DEAD
  combat/melee | USE_SWORD, USE_BATTLE_AXE, USE_MACE, USE_DAGGER
  combat/ranged| FIRE_BOW, FIRE_CROSSBOW, FIRE_WAND, DRAW_BOW
  combat/throw | THROW_KNIFE, THROW_AXE, THROW_BOMB
  combat/impact| IMPACT_ARROW, IMPACT_FIREBALL, EXPLOSION
  effects      | BURNING, FROZEN, POISONED, HEALING
  items        | EAT, DRINK, CAST_SPELL, READ_SCROLL
  tools        | USE_PICKAXE, USE_AXE, USE_SHOVEL
  blocks/break | BREAK_DIRT, BREAK_STONE, BREAK_GLASS
  blocks/place | PLACE_DIRT, PLACE_STONE, PLACE_WOOD
  footsteps    | STEP_GRASS, STEP_STONE, STEP_WOOD
  inventory    | COLLECT, DROP, EQUIP, HOTBAR_SWITCH
  chests       | OPEN_CHEST, OPEN_DAILY_CHEST, OPEN_MONTHLY_CHEST
  doors        | DOOR_OPEN, DOOR_CLOSE, DOOR_LOCKED
  mobs/*       | ZOMBIE_ATTACK, SKELETON_DEATH, WOLF_HOWL, etc.
  ui           | UI_BUTTON_CLICK, MENU_OPEN, NOTIFICATION
  music        | MUSIC_MENU, MUSIC_LEVEL_1, MUSIC_BOSS
  ambient      | AMBIENT_WIND, AMBIENT_RAIN, AMBIENT_CAVE
  events       | LEVEL_START, CHECKPOINT, SECRET_FOUND
  npc          | NPC_GREETING, SHOP_BUY, SHOP_SELL
  crafting     | CRAFT_SUCCESS, FORGE_HAMMER, ENCHANT_SUCCESS
  special      | MIRROR_ACTIVATE, MIRROR_REALM_CHANGE

SOUND FILE LOCATIONS:
  sounds/                  → Legacy WAV files (flat structure)
  sounds/compressed/       → MP3 files organized by category
  ├── player/             → Player movement and state sounds
  ├── combat/             → Combat sounds (melee, ranged, impact)
  ├── effects/            → Status effect sounds
  ├── items/              → Item usage sounds
  ├── tools/              → Tool usage sounds
  ├── blocks/             → Block break/place sounds
  ├── footsteps/          → Footstep sounds by surface
  ├── inventory/          → Inventory management sounds
  ├── chests/             → Chest and vault sounds
  ├── doors/              → Door interaction sounds
  ├── mobs/               → Mob-specific sounds (by mob type)
  ├── ui/                 → UI interaction sounds
  ├── music/              → Background music tracks
  ├── ambient/            → Ambient environment sounds
  ├── water/              → Water interaction sounds
  ├── events/             → Special event sounds
  ├── npc/                → NPC interaction sounds
  ├── crafting/           → Crafting system sounds
  └── special/            → Special item sounds

ADDING NEW SOUNDS:
  1. Create MP3 file with appropriate name (e.g., jump.mp3)
  2. Place in corresponding category folder (e.g., sounds/compressed/player/)
  3. The game automatically picks up the file when playAction() is called
  4. See sounds/compressed/README.md and SOUNDS.txt files for expected filenames

--------------------------------------------------------------------------------
17. UI SYSTEM (ui/)
--------------------------------------------------------------------------------

Custom UI components rendered directly on the game canvas.

UI BUTTON (UIButton.java):
  UIButton btn = new UIButton(x, y, width, height, "Label");
  btn.setOnClick(() -> { /* action */ });
  btn.update(input);  // Check hover/click
  btn.draw(g);

UI SLIDER (UISlider.java):
  UISlider slider = new UISlider(x, y, width, min, max, initial);
  slider.setOnChange((value) -> { /* handle value */ });
  slider.update(input);  // Handle drag
  slider.draw(g);

PLAYER STATUS BAR (PlayerStatusBar.java):
  Displays health, mana, and stamina bars:
  - Health: Red bar (RGB: 220, 50, 50)
  - Mana: Blue bar (RGB: 50, 100, 220)
  - Stamina: Green bar (RGB: 50, 200, 50)

SETTINGS OVERLAY (SettingsOverlay.java):
  Global settings panel accessible via M key:
  - Music volume slider
  - SFX volume slider
  - Mute all toggle
  - Darkened background overlay

INVENTORY UI:
  - 8x4 grid (32 slots) with 5-slot hotbar
  - Rarity color-coded borders
  - Stack count display
  - Drag-and-drop support
  - Tooltip on hover (item stats)

--------------------------------------------------------------------------------
18. SPECIAL GAME FEATURES
--------------------------------------------------------------------------------

LOOT GAME SCENE:
  Mini-game focused on collecting loot from chests:
  - Wide level (6000px) with limited vertical space
  - Daily Chest (center): Opens once per 24 hours, drops 3 items
  - Monthly Chest (right): Opens once per 30 days, drops 10 items
  - Vault for persistent storage
  - Items bounce with physics when dropped
  - Rarity-colored light beams (Borderlands style)
  - Alchemy Table for crafting items
  - Reverse Crafting Table for deconstructing items

ALCHEMY/CRAFTING SYSTEM:
  The game features a comprehensive crafting system with two interactable tables:

  ALCHEMY TABLE (Green Glow):
  - Combines 1-3 items to create new items
  - Approach table and press 'E' to open UI (positioned right of inventory)
  - Drag items from inventory to 3 input slots on the left
  - Recipe matches automatically when correct ingredients are placed
  - Output slot on the right shows the craftable result
  - Click output slot to craft and receive the item
  - Consumes ONE item from each input slot per craft (supports stacks)
  - Removing any input clears the output preview
  - Dragged items appear on top of all UI for clarity

  REVERSE CRAFTING TABLE (Purple Glow):
  - Breaks down items into component parts
  - Only works with items marked as "reversible" in recipes
  - 1 input slot on the left, 3 output slots on the right
  - Place item to deconstruct in the input slot
  - Shows up to 3 component materials in output slots
  - Click any output slot to deconstruct and receive all materials
  - Consumes ONE item from input per deconstruction (supports stacks)
  - Dragged items appear on top of all UI for clarity

  RECIPES (data/alchemy_recipes.json):
  - 100+ predefined recipes in categories:
    * Weapons: Bows, swords, staffs, daggers, axes
    * Armor: Helmets, chestplates, leggings, boots
    * Tools: Pickaxes, axes, shovels, fishing rods
    * Potions: Health, mana, stamina, buff potions
    * Materials: Ingots, planks, yarn, leather
    * Ammo: Arrows, bolts, fire/ice variants
    * Blocks: Stone, brick, glass blocks
    * Collectibles: Keys, lanterns, banners

  RECIPE FORMAT:
  {
    "id": "unique_id",
    "name": "Display Name",
    "ingredients": ["item1", "item2", "item3"],
    "result": "output_item_id",
    "resultCount": 1,
    "category": "weapons",
    "reversible": true
  }

  EXAMPLE RECIPES:
  - Wooden Bow: string + planks + arrow
  - Iron Sword: iron_ingot + iron_ingot + planks
  - Health Potion: apple + mana_leaf
  - Magic Wand: planks + magic_crystal

DOOR AND TRIGGER SYSTEM:
  DoorEntity:
  - Interactive doors between areas
  - Linked to spawn points (door A → door B)
  - Lock/key system (requires KEY item)
  - Opening/closing animations

  TriggerEntity:
  - Invisible activation zones
  - Triggers on player collision
  - Can load levels, show dialogue, spawn entities

  ButtonEntity:
  - Interactive switches/levers
  - Links to doors or other triggers

CHARACTER CUSTOMIZATION:
  Available in SpriteCharacterCustomization scene:
  - 8 skin tone presets
  - 13 hair color presets
  - Equipment slots: Hair, Helmet, Chest, Legs, Boots, Belt
  - Companion selection (alternate player characters)
  - All choices persist across sessions

PROJECT STRUCTURE
src/                    - Game engine source code (organized by package)
  core/                 - Main application classes (Main, GameWindow, GamePanel)
  scene/                - Scene management (Scene, SceneManager, GameScene, menus)
  entity/               - Base entity classes (Entity, EntityManager, SpriteEntity)
    - Item.java             - Item class with categories, rarities, and held item overlay support
    - ItemRegistry.java     - Predefined items (weapons, armor, consumables, etc.)
    - ProjectileEntity.java - Projectile system for ranged attacks and thrown items
    - RecipeManager.java    - Loads and manages crafting recipes from JSON
    - AlchemyTableEntity.java - Interactable alchemy/reverse crafting tables
    player/             - Player-specific classes (PlayerBase, PlayerEntity, PlayerBoneEntity)
      - SpritePlayerEntity.java - Sprite-based player with double/triple jump, sprint, projectiles
    mob/                - Mob AI classes
      - MobEntity.java       - Base mob class with AI state machine
      - SpriteMobEntity.java - GIF-based mob with status effects, auto-configured HP/stats
      old/                - Deprecated bone-based mob classes (legacy)
        - HumanoidMobEntity.java  - [DEPRECATED] Bone-based humanoid mobs
        - HumanoidVariants.java   - [DEPRECATED] Humanoid variant configurations
        - QuadrupedMobEntity.java - [DEPRECATED] Bone-based quadruped mobs
  block/                - Block system (BlockEntity, BlockType, BlockRegistry, BlockAttributes)
  animation/            - Animation system (SpriteAnimation, EquipmentOverlay, AnimatedTexture)
    - SpriteAnimation.java  - Extended with 16 action states (IDLE, WALK, RUN, SPRINT, JUMP, DOUBLE_JUMP, TRIPLE_JUMP, FALL, ATTACK, FIRE, USE_ITEM, EAT, HURT, DEAD, BLOCK, CAST)
    bone/               - Legacy bone animation (Skeleton, Bone, BoneAnimation, QuadrupedSkeleton)
  graphics/             - Rendering (Camera, LightingSystem, TextureManager, Parallax)
  level/                - Level loading (LevelData, LevelLoader)
  audio/                - Sound management (AudioManager)
  input/                - Input handling (InputManager)
  ui/                   - UI components (UIButton, UISlider, Inventory, ToolType)
    - AlchemyTableUI.java   - Drag-and-drop alchemy table interface (3 inputs, 1 output)
    - ReverseCraftingUI.java - Drag-and-drop deconstruction interface (1 input, 3 outputs)
devtools/               - Development tools (texture generators, animation importers)
  - TextureGenerator.java           - Generates player bone textures
  - HumanoidTextureGenerator.java   - Generates humanoid mob textures
  - QuadrupedTextureGenerator.java  - Generates quadruped animal textures
  - BlockTextureGenerator.java      - Generates block textures
  - ParallaxTextureGenerator.java   - Generates parallax backgrounds
  - HumanoidSpriteGenerator.java    - Generates GIF sprites for humanoid mobs
  - QuadrupedSpriteGenerator.java   - Generates GIF sprites for quadruped mobs
  - ExtendedSpriteGenerator.java    - Generates all 15 animation GIFs (sprint, double_jump, fire, eat, etc.)
  blockbench/             - Blockbench import tools (legacy bone animation support)
    - BoneTextureGenerator.java       - Generates simple bone textures
    - BlockbenchAnimationImporter.java - Imports Blockbench animation files
tools/                  - Utility tools for asset generation
  - StatusEffectSpriteGenerator.java - Generates burning.gif and frozen.gif for mobs
  - ParticleGifGenerator.java        - Generates particle overlay GIFs for status effects
assets/
  textures/
    humanoid/           - Humanoid character textures (player, orc, zombie, skeleton)
    quadruped/          - Animal textures (wolf, dog, cat, horse, etc.)
    blocks/             - Block textures (grass, dirt, stone, etc.)
  particles/            - Status effect particle overlays
    - fire_particles.gif   - Burning effect overlay
    - ice_particles.gif    - Frozen effect overlay
    - poison_particles.gif - Poisoned effect overlay
  parallax/             - Parallax background layers
  mobs/                 - GIF-based mob sprites (zombie, skeleton, bandit, etc.)
    [mob_name]/         - Each mob has its own folder with animation GIFs
      - idle.gif, walk.gif, attack.gif, hurt.gif, death.gif
      - burning.gif, frozen.gif (status effect variants)
sounds/                 - All sound files (music, effects, footsteps)
levels/                 - Level JSON files
data/                   - Game data files
  - alchemy_recipes.json  - Crafting recipes (100+ recipes)

TODOs
Work on section 1.01 (Inventory and items) 

KNOWN ISSUES

(None - all known issues have been resolved)

FUTURE FEATURES (ROADMAP)

1.01 Inventory and Items
	-Robust 32 slot inventory system with hot bars
		-Allows stacking of identical items
		-Displays item details and stats when hovering over items
		-Still scrolls with player as they move
		-Has a custom texture for the UI that is located in the assets folder
		-Can filter items by rarity and alphabetical
		-Items dragged out of inventory drop on ground in front of player
	-Robust item system
		-Items have distinct properties and categories, not just names
			-Categories include:
				-Tools
				-Weapons
				-Armor and clothing
				-Blocks
				-Food/Potion
				-Other
			-Properties include:
				-Rarity (white/green/blue/purple/orange/cyan)
				-Range (Weapons and tools)
				-Defense %
				-Special effect
				-Other
		-Items should all be held by the player when equipped in hot bar. Hot bar scrolling should be bound to right click
		-Projectiles should be included with some items (crossbow for example). This projectile system should have its own hitbox system with entities
		-A crafting system should be present. It will function by allowing players to combine 2 to 3 items at certain locations to create new items.
		-Blocks should have variants that are just texture masks of the base block. For example, a frozen block would use an ice mask that is semi-transparent to cover the block or a grass block would use a grass mask over dirt. These block variants must be broken down before being mined. Ice must shatter and grass must be uprooted before the player can mine the block.
		-The player should be able to place blocks immediately in front of them
		-Some items should work as 'keys' to activate certain entities such as other items or doors
		-Some items will have an area of effect range
		-List of item examples:
			-wizards hat
			-exploding potion
			-crossbow
			-sword
			-bread
			-string
			-magic wand
			-scroll
			
1.02 Character Selection
	-A selection of around 10 customizable characters
		-Characters will be unlocked through gameplay
		-Every level will require that you select one unlocked character before playing
		-Each character has their own stats and abilities
		-Wearable clothes, armor and other customization features
		-Each level will be tailored to one or several character abilities
		
1.03 NPCs and dialogue
	-Pixel art dialogue overlays similar to Pokemon
	-NPC characters with custom dialogue that appears in game as a blurb next to their body
	-Place for generic sounds as the NPCs talk
	-Shops with variable prices
	-Companion system where some NPCs will follow the player
	-NPCs should act like complex entities similar to the player but with automated functionality
	
1.04 Boss Fights
	-Complex and unique AI for special 'end-of-area' boss fights
	-Unique movement and attack mechanics for boss fights
	-At least 10 variable boss AI variations, ranging in difficulty
	
1.05 Overworld for level loading and fast travel
	-Each major section of the game will be divided into overworlds with levels similar to those in later Super Mario games
	-Each overworld area will contain levels, shops, boss fights, and player owned houses
		-Player owned houses will be places to customize and display rare loot collected throughout the game
		
1.06 Area 1 level design
	-Level 1-9
	-1 shop unlocked at level 3
	-1 boss fight

1.07 Finish game and level designs (Areas 2-6)

1.08 Bug fixes and testing

RESOLVED ISSUES

[FIXED] File order is confusing. Player textures are in 3 redundant locations
  -> Consolidated to single canonical location: assets/textures/humanoid/player/

[FIXED] Texture generators should not be part of the game engine, as only PNG and GIF files should be used for textures and imported externally into the assets folder
  -> Moved all texture generators to devtools/ directory

[FIXED] Some sound files are located in the base 'assets' folder and should be located in sounds
  -> Moved collect.wav, drop.wav, jump.wav, music.wav to sounds/ directory

[FIXED] 'blocks' assets should be a sub folder within textures
  -> Moved blocks to assets/textures/blocks/

[FIXED] Blockbench support unclear in usage
  -> BlockbenchAnimationImporter moved to devtools/, runtime import deprecated

[FIXED] When mobs attack (specifically quadrupeds) they target the center of the player
  -> Mobs now target the nearest edge of player's hitbox using getDistanceToTargetFace()
  -> Attack distance calculated to front/back of player, not center point

[FIXED] Red behind blocks when breaking does not disappear
  -> Red damage overlay now only appears when block is actively targeted by player
  -> Added targeted state to BlockEntity, resets each frame

[FIXED] Mob hitboxes too small
  -> Increased all mob hitbox sizes by ~40% for better hit detection
  -> Humanoid hitboxes: 60x120 (was 40x100)
  -> Quadruped hitboxes scaled proportionally per animal type

[FIXED] Hostile mobs ignore block collisions and walk through solid blocks
  -> Rewrote MobEntity.applyPhysics() to check collisions BEFORE applying movement
  -> Added horizontal collision detection with solid blocks
  -> Added proper vertical collision detection (both falling and jumping)
  -> Mobs now stop at walls and land on platforms correctly

[FIXED] Java class structure should be organized by relationship to other classes
  -> Reorganized all 48 Java files into logical packages under src/
  -> core/ - Main application entry points
  -> scene/ - Scene management and game states
  -> entity/ - Entity hierarchy with player/ and mob/ subpackages
  -> block/ - Block system classes
  -> animation/ - Skeleton and bone animation system
  -> graphics/ - Rendering, camera, lighting, parallax
  -> level/ - Level data and loading
  -> audio/ - Sound management
  -> input/ - Input handling
  -> ui/ - UI components and inventory

[FIXED] End of levels does not immediately take you back to menu
  -> TriggerEntity now supports menu return for empty/menu targets
  -> Scene transitions sped up (transitionSpeed increased from 0.05 to 0.12)

[FIXED] Vertical scrolling has black bars on top and bottom of screen
  -> Black bars only drawn when parallax background is not enabled
  -> Parallax layers now properly support vertical scrolling with anchorBottom property
  -> verticalMargin can be set to 0 to eliminate letterboxing

[FIXED] Effect for night and darkness is too opaque
  -> Lighting buffer now properly cleared to transparent before drawing darkness overlay
  -> Darkness overlay correctly blends with parallax background layers
  -> Default nightDarkness reduced, ambientLevel increased for better visibility

[FIXED] 'e' to break blocks should be bound to left mouse click
  -> Added MouseListener implementation to InputManager
  -> Left mouse click now works for block mining alongside 'E' key

[FIXED] Moving entities mask more than non-transparent pixels when changing color
  -> Replaced AlphaComposite overlay with per-pixel tinting in Bone.java
  -> Tint now only affects pixels where alpha > 0, preserving transparency
  -> Cached tinted textures for performance optimization

[FIXED] Quadrupeds shape and textures need to be refined
  -> Added species-specific head generation (canine snout, cat round face, pig disc nose, etc.)
  -> Added species-specific body features (cow spots, sheep wool, cat stripes, fox white chest)
  -> Improved walk/run animations with smoother, more natural movement
  -> All 10 animal types now visually distinct and recognizable

[FIXED] All texture files should support GIF file types
  -> Created AnimatedTexture class for managing GIF frame cycling with proper timing
  -> Updated AssetLoader to extract all frames from GIF files with per-frame delays
  -> Added GIF support to Bone class for animated bone textures (e.g., glowing effects)
  -> Added GIF support to ParallaxLayer for animated backgrounds (e.g., moving clouds)
  -> Added GIF support to BlockRegistry for animated blocks (e.g., lava, water)
  -> Added GIF support to SpriteEntity for animated sprites
  -> GIF animations respect original frame delays and loop automatically
  -> All components include update() methods for frame advancement

[FIXED] Character customization UI color sliders overlapped with character screen
  -> Moved slider startY from 120 to 150 to prevent overlap
  -> Removed dead code that incorrectly accessed slider values for positioning

[FIXED] Old bone-based character customization should be removed from main menu
  -> Main menu now opens sprite-based customization (SpriteCharacterCustomization)
  -> Bone-based CharacterCustomizationScene kept for legacy PlayerBoneEntity support

[FIXED] Lighting demo level unnecessary (uses java class not JSON file)
  -> Removed LightingDemoScene.java and its registration in GamePanel
  -> Removed Lighting Demo button from main menu

[FIXED] Old bone-based animation assets need to be moved to their own folder
  -> Moved Bone.java, BoneAnimation.java, Skeleton.java, QuadrupedSkeleton.java, QuadrupedAnimation.java to animation/bone/ package
  -> Updated all imports in 10+ files to use animation.bone.*
  -> Moved BlockbenchAnimationImporter.java and BoneTextureGenerator.java to devtools/blockbench/

[FIXED] Update README.md with current features
  -> Updated CURRENT FEATURES to reflect sprite-based animation system
  -> Updated PROJECT STRUCTURE with new package organization
  -> Moved resolved issues from KNOWN ISSUES to RESOLVED ISSUES

[FIXED] Blocks should work with an overlay system
  -> Created BlockOverlay enum with GRASS, SNOW, ICE, MOSS, VINES overlay types
  -> Added overlay support to BlockEntity with rendering and damage tracking
  -> Overlays render on top of base block textures with semi-transparency
  -> Overlays must be removed before mining the base block
  -> Includes procedural texture generation for overlays when files not available
  -> Added overlay texture caching to BlockRegistry for efficiency

[FIXED] Mobs should be sprite based with hitbox collision detection
  -> Created SpriteMobEntity class for GIF-based mob animations
  -> Supports idle, walk, run, attack, hurt, death animation states
  -> Proper hitbox collision detection with configurable hitbox size
  -> Inherits AI state machine from MobEntity (IDLE, WANDER, CHASE, ATTACK, etc.)
  -> Health bar rendering and invincibility flash effects
  -> Debug mode for visualizing hitboxes and mob state
  -> Placeholder sprite generation for testing without assets

[FIXED] Sprite-based mobs need automatic HP and stat configuration
  -> Added configureMobStats() to SpriteMobEntity that auto-detects mob type from sprite directory
  -> Mobs automatically get appropriate health (40-60), damage, and speed based on type
  -> Supports: zombie, skeleton, goblin, orc, bandit, knight, mage, wolf, bear, and more
  -> Default fallback stats for unknown mob types

[FIXED] Special arrows should apply status effects to mobs
  -> Added StatusEffect enum to SpriteMobEntity (BURNING, FROZEN, POISONED)
  -> Fire arrows burn enemies for 3 seconds with damage-over-time
  -> Ice arrows freeze enemies for 4 seconds with 50% movement slow
  -> Status effects include color tint overlay and particle effects
  -> Configurable duration, damage per tick, and damage multiplier

[FIXED] Status effect particles should be GIF-based overlays
  -> Created assets/particles/ folder with fire_particles.gif, ice_particles.gif, poison_particles.gif
  -> ParticleGifGenerator tool creates animated particle overlays
  -> SpriteMobEntity loads and renders particle GIFs as overlays on affected mobs
  -> Fallback to procedural particle rendering if GIFs not available

[FIXED] Charged shots for bows should consume arrows, not mana
  -> Updated SpritePlayerEntity.fireChargedProjectile() to differentiate weapon types
  -> Bows now consume arrows from inventory, not mana
  -> Arrow projectiles don't scale in size when charged (only magic scales)
  -> Magic staffs continue to consume mana and scale projectiles

[FIXED] Bone-based mob classes should be moved to deprecated package
  -> Created entity/mob/old/ package for deprecated bone-based mobs
  -> Moved HumanoidMobEntity.java, HumanoidVariants.java, QuadrupedMobEntity.java
  -> Added [DEPRECATED] markers to class documentation
  -> Updated GameScene.java imports to use entity.mob.old.*
  -> SpriteMobEntity is now the preferred approach for mob entities

[FIXED] Game window should be a borderless 1920x1080 window, not completely full screen
  -> Changed GameWindow from exclusive fullscreen to borderless 1920x1080 window
  -> Window is now centered on screen using setLocationRelativeTo(null)
  -> Works consistently across different monitor sizes
  -> Game title changed to "The Amber Moon"

[FIXED] Game window draws focus and mouse pointer back to window, preventing other applications
  -> Removed aggressive always-on-top timer that kept stealing focus
  -> Removed windowLostFocus handler that forced window back to front
  -> Window now allows users to switch to other applications normally
  -> Focus is only requested on initial window display

[FIXED] Sensitivity of scroll wheel needs to be turned down
  -> Added scroll accumulation threshold (SCROLL_THRESHOLD = 3.0)
  -> Uses precise wheel rotation for smoother control
  -> Multiple scroll ticks required to trigger hotbar/inventory scroll
  -> Prevents accidental item switching during fast scrolling

[FIXED] UI elements should cancel out in-game clicking actions
  -> Added click consumption system to InputManager
  -> UI buttons now consume clicks when handling them
  -> Player entity checks if click was consumed by UI before processing game actions
  -> Clicking music toggle or other buttons no longer triggers mining/attacks

[FIXED] Player triple jump not working as intended
  -> Fixed double-trigger bug: space char and keyCode were tracked separately
  -> Single space press was triggering two jumps (char on frame 1, keyCode on frame 2)
  -> Now both are consumed in same frame, preventing accidental double-jump
  -> Fixed edge case where falling off ledge caused misaligned jump count
  -> Triple jump strength normalized to -9 for consistent feel

[FIXED] Scroll wheel sensitivity adjusted
  -> Reduced SCROLL_THRESHOLD from 3.0 to 1.5 for faster response
  -> Maintains accumulation to prevent accidental switching while being responsive

[FIXED] Base player customization (skin tone) should be available in customization menu
  -> Added skin tone selection panel with 8 preset options
  -> Skin tones range from Light to Deep, plus a "None" option
  -> Selected skin tone tints the base player sprite
  -> Skin tone selection persists across levels and game sessions

[FIXED] Belts should be included in the player customization menu
  -> Added BELT equipment slot to EquipmentOverlay (renders over legs, under chest)
  -> Added "Belt" category tab to character customization menu
  -> Belt items load from assets/clothing/belt/ directory
  -> Adjusted category tab widths to fit all equipment types

[FIXED] Hair customization should be available in the player customization menu
  -> Added HAIR_FRONT and HAIR_BACK equipment slots to EquipmentOverlay
  -> HAIR_BACK renders behind character (for ponytails, long hair)
  -> HAIR_FRONT renders in front (for bangs, short styles)
  -> Added "Hair" category tab as first equipment category
  -> Added 13 preset hair colors with color selector UI
  -> Hair items load from assets/clothing/hair/ directory with front/back layers

[FIXED] Character customization UI elements bunched up and overlapping
  -> Completely reorganized layout for 1920x1080 screen
  -> Left panel (x=60): Character preview with 4x scale (was 3x)
  -> Center panel (x=420): Category tabs and 6-column item grid
  -> Right panel (x=1140): Skin tone, hair color, and tint controls
  -> All UI elements now properly spaced with no overlapping
  -> Larger item previews (90x90) for better visibility

[FIXED] Companions are listed as items instead of player character alternates
  -> Removed companion registration from ItemRegistry
  -> Created CompanionRegistry class for managing companions as player character alternates
  -> Added companion selection panel to SpriteCharacterCustomization
  -> Companions can now be selected in character customization menu
  -> Companion selection is saved and persisted between sessions

[FIXED] Block breaking system needs revisiting with directional arrow indicator
  -> Blocks now work like UI elements - click to select a block within 3 block radius
  -> Selected block is highlighted with yellow border and fill
  -> Arrow keys change mining direction (up/down/left/right) on selected block
  -> Visual arrow indicator shows which side of the block will be mined
  -> Click selected block again (or press E) to mine from the chosen direction
  -> Clicking elsewhere or moving out of range deselects the block

[FIXED] Blocks are not placeable
  -> Added block placement system via left click
  -> Blocks can be placed within 3 block radius of the player
  -> Player must be holding a block item (ItemCategory.BLOCK)
  -> Placement checks for existing blocks and player collision
  -> Added block items to ItemRegistry (dirt, grass, stone, wood, brick, etc.)
  -> Block type is determined from held item name
