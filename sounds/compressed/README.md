# Compressed MP3 Sound Effects Directory

This directory contains placeholder folders for all game sound effects in MP3 format.
The AudioManager will automatically look for MP3 files here before falling back to WAV files.

## Usage

To add a sound effect:
1. Create an MP3 file with the appropriate name
2. Place it in the corresponding category folder
3. The game will automatically use it when that action is triggered

Example: To add a jump sound, place `jump.mp3` in `player/`

## API Usage in Code

```java
// Using the SoundAction enum (recommended)
audioManager.playAction(SoundAction.JUMP);
audioManager.playAction(SoundAction.USE_BATTLE_AXE);
audioManager.playAction(SoundAction.OPEN_MONTHLY_CHEST);

// Using string action names
audioManager.playAction("JUMP");
audioManager.playAction("USE_SWORD");
```

## Folder Structure

```
sounds/compressed/
├── player/              # Player movement and state sounds
├── combat/              # Combat-related sounds
│   ├── melee/          # Melee weapon sounds
│   ├── ranged/         # Ranged weapon sounds
│   ├── throw/          # Throwable item sounds
│   └── impact/         # Projectile impact sounds
├── effects/             # Status effect sounds (burn, freeze, poison)
├── items/               # Item usage sounds (eating, drinking, casting)
├── tools/               # Tool usage sounds (pickaxe, axe, shovel)
├── blocks/              # Block interaction sounds
│   ├── break/          # Block breaking sounds
│   ├── place/          # Block placing sounds
│   └── overlay/        # Block overlay removal sounds
├── footsteps/           # Footstep sounds by surface type
├── inventory/           # Inventory management sounds
├── chests/              # Chest and vault sounds
├── doors/               # Door interaction sounds
├── mobs/                # Mob-specific sounds
│   ├── general/        # Generic mob sounds
│   ├── zombie/         # Zombie sounds
│   ├── skeleton/       # Skeleton sounds
│   ├── goblin/         # Goblin sounds
│   ├── orc/            # Orc sounds
│   ├── bandit/         # Bandit sounds
│   ├── knight/         # Knight sounds
│   ├── mage/           # Mage sounds
│   ├── wolf/           # Wolf sounds
│   ├── bear/           # Bear sounds
│   ├── frog/           # Frog sounds
│   ├── spider/         # Spider sounds
│   ├── slime/          # Slime sounds
│   └── boss/           # Boss mob sounds
├── ui/                  # UI interaction sounds
├── music/               # Background music tracks
├── ambient/             # Ambient environment sounds
├── water/               # Water interaction sounds
├── events/              # Special event sounds
├── npc/                 # NPC interaction sounds
├── crafting/            # Crafting system sounds
└── special/             # Special item sounds (Mirror to Other Realms)
```

## Sound File Naming Convention

Each MP3 file should be named to match the SoundAction enum value path:
- `player/jump.mp3` -> SoundAction.JUMP
- `combat/melee/use_sword.mp3` -> SoundAction.USE_SWORD
- `chests/open_monthly_chest.mp3` -> SoundAction.OPEN_MONTHLY_CHEST

## Complete Sound List

See `src/audio/SoundAction.java` for the complete enumeration of all available sound actions.
