TITLE	The Amber Moon
AUTHOR Larson Sonderman & CLAUDE AI

DESCRIPTION
A Java Swing-based game engine that runs a 2D platformer game. Plays similarly to Terraria, Super Mario, or 2D Minecraft.
Inspired by Dungeons and Dragons games like Baldur's Gate 3. (Non-turn-based)
This game should feel abundant with the quantity and variations in items and tools.

CURRENT FEATURES
-JSON file-based level loading
-Block-based level construction
-Sprite-based player animations with GIF support
-Sprite-based mob animations with hitbox collision detection
-Equipment overlay system for character customization (helmets, chest, legs, boots, etc.)
-Block overlay system (grass, snow, ice overlays on base blocks)
-Bone-based animations (legacy, moved to animation.bone package)
-Entity management with custom AI for Humanoid and Quadruped entities
-Basic lighting support (simple dark mask and light sources)
-Simple parallax with animated background support
-Scrolling camera that follows the player

MOVEMENT AND ANIMATION SYSTEM (NEW)
-Extended animation states supporting 5-15 frames for smooth, clear motion
  -Movement: IDLE, WALK, RUN, SPRINT (with Shift key)
  -Jumping: JUMP, DOUBLE_JUMP, TRIPLE_JUMP, FALL
  -Combat: ATTACK, FIRE, BLOCK, CAST
  -Item Usage: USE_ITEM, EAT
  -Reactions: HURT, DEAD
-Double and triple jumping for all entities
  -Press Space while in air to perform additional jumps
  -Each jump has unique animation (flips and spins)
  -Configurable max jumps per entity (setMaxJumps)
-Sprinting system for all entities
  -Hold Shift to sprint (increased speed, drains stamina)
  -Stamina regenerates when not sprinting
  -Sprint animations with forward lean

PROJECTILE SYSTEM (NEW)
-ProjectileEntity class for fired/thrown projectiles
  -GIF-based animated projectile sprites
  -Velocity-based movement with optional gravity
  -Collision detection with entities and blocks
  -Configurable damage and knockback
  -Trail effects for visual polish
  -Mouse-aimed projectile firing (projectiles fly toward cursor)
-Multiple projectile types:
  -ARROW, BOLT (bows, crossbows)
  -MAGIC_BOLT, FIREBALL, ICEBALL (magic weapons)
  -THROWING_KNIFE, THROWING_AXE, ROCK (throwables)
  -POTION, BOMB (explosives)
-Special arrows with status effects:
  -Fire Arrow: Burns enemies for 3 seconds (5 damage/tick, 1.2x multiplier)
  -Ice Arrow: Freezes/slows enemies for 4 seconds (3 damage/tick, 1.1x multiplier)
-Charged shot system:
  -Hold attack to charge bows and magic staffs
  -Bows consume arrows (not mana), arrows don't scale in size
  -Magic staffs consume mana, projectiles scale with charge level
  -Visual feedback with growing projectile and particle effects
-Mobs can fire projectiles with ranged attacks
  -Configurable projectile type, damage, speed, cooldown
  -AI switches between ranged and melee based on distance

STATUS EFFECT SYSTEM (NEW)
-Mobs can receive burning, frozen, and poisoned status effects
  -BURNING: Fire damage over time, orange tint, fire particle overlay
  -FROZEN: Ice damage, blue tint, slowed movement (50%), ice particle overlay
  -POISONED: Poison damage over time, green tint, bubble particle overlay
-GIF-based particle overlays in assets/particles/:
  -fire_particles.gif: Rising flames and sparks
  -ice_particles.gif: Falling snowflakes and crystals
  -poison_particles.gif: Rising bubbles
-Configurable effect properties:
  -Duration, damage per tick, tick interval
  -Movement slow multiplier
  -Color tint with pulsing alpha
-Fallback to procedural particle rendering if GIFs not available

ITEM AND WEAPON SYSTEM (NEW)
-Comprehensive Item class with categories and properties
  -Categories: WEAPON, RANGED_WEAPON, TOOL, ARMOR, CLOTHING, BLOCK, FOOD, POTION, MATERIAL, KEY, ACCESSORY, THROWABLE
  -Rarity tiers: COMMON (white), UNCOMMON (green), RARE (blue), EPIC (purple), LEGENDARY (orange), MYTHIC (cyan)
  -Properties: damage, defense, attack speed, range, crit chance, special effects
-Held item overlay system
  -Items render as GIF overlays on the character when held
  -Animations sync with base character animation states
  -Support for all action states (idle, walk, attack, fire, etc.)
-Ranged weapons with projectile firing
  -Crossbow, longbow, magic wand, fire staff, ice staff
  -Configurable projectile type, damage, and speed
  -Firing animation triggers on attack
-Consumable items (food and potions)
  -Health, mana, and stamina restoration
  -Eating animation with progress bar
  -Configurable consume time
-ItemRegistry with predefined items
  -Melee weapons: swords, daggers, axes, maces
  -Ranged weapons: bows, crossbows, magic staves
  -Throwing weapons: knives, axes, rocks
  -Tools: pickaxes, shovels, axes
  -Armor: helmets, chestplates, leggings, boots
  -Consumables: food, potions
  -Materials: crafting ingredients
  -Keys: for doors and chests

NEW PLAYER ANIMATIONS
-Firing animation for projectile weapons (8-12 frames)
-Using item animation for general item usage (6-10 frames)
-Taking damage animation with flinch effect (5-8 frames)
-Eating animation with food approaching mouth (10-15 frames)
-Sprint animation with forward lean (6-10 frames)
-Double jump with spin/flip effect (8-12 frames)
-Triple jump with dramatic spin (10-15 frames)

EXTENDED MOB CAPABILITIES
-Mobs support all new animation states
-Ranged attack support for mobs (setRangedAttack)
-Sprint mode when chasing player
-Projectile management for mob attacks

PROJECT STRUCTURE
src/                    - Game engine source code (organized by package)
  core/                 - Main application classes (Main, GameWindow, GamePanel)
  scene/                - Scene management (Scene, SceneManager, GameScene, menus)
  entity/               - Base entity classes (Entity, EntityManager, SpriteEntity)
    - Item.java             - Item class with categories, rarities, and held item overlay support
    - ItemRegistry.java     - Predefined items (weapons, armor, consumables, etc.)
    - ProjectileEntity.java - Projectile system for ranged attacks and thrown items
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

TODOs
Work on section 1.01 (Inventory and items) 

KNOWN ISSUES

-Player triple jump not working as intended. Physics appear to do a double jump but animation indicates a triple when no third jump has been completed. 

Player trip

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
