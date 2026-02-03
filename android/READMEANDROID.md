# The Amber Moon - Android Port

This document describes the Android port of "The Amber Moon" 2D platformer game, originally built with Java Swing for desktop.

## Overview

The Android port maintains feature parity with the desktop version while adapting the input system for touch controls and optimizing rendering for mobile devices. The game targets a 1920x1080 resolution (landscape) with automatic scaling to fit different screen sizes.

## Project Structure

```
android/
├── app/
│   ├── build/                    # Build output (APK, classes, etc.)
│   ├── src/main/
│   │   ├── java/com/ambermoongame/
│   │   │   ├── audio/            # Audio system (music, sound effects)
│   │   │   ├── core/             # Core game classes (activities, game loop)
│   │   │   ├── graphics/         # Asset loading, rendering utilities
│   │   │   ├── input/            # Touch and controller input handling
│   │   │   ├── save/             # Save system and cloud saves
│   │   │   ├── scene/            # Game scenes (menu, gameplay, etc.)
│   │   │   └── ui/               # Touch control overlays
│   │   ├── res/                  # Android resources
│   │   │   ├── drawable/         # App icons and images
│   │   │   ├── values/           # Strings, styles, themes
│   │   │   └── xml/              # Backup rules
│   │   └── AndroidManifest.xml   # App manifest
│   └── assets/                   # Game assets (copied from desktop)
├── scripts/
│   ├── build.bat                 # Windows build script
│   ├── build.sh                  # Linux/Mac build script (planned)
│   └── run.bat                   # Build + install + launch script
└── sdk/                          # Android SDK (user-provided)
    ├── build-tools/34.0.0/       # aapt2, d8, apksigner, etc.
    └── platform-tools/           # adb
```

## Core Components

### Activities

#### MainActivity (`core/MainActivity.java`)
- Entry point for the application
- Initializes global managers (GamePreferences, CloudSaveManager, AndroidAudioManager)
- Sets up fullscreen immersive mode
- Launches GameActivity immediately

#### GameActivity (`core/GameActivity.java`)
- Hosts the main game surface and touch controls
- Manages game lifecycle (pause, resume, destroy)
- Routes input events to appropriate handlers
- Provides vibration feedback support

### Rendering

#### GameSurfaceView (`core/GameSurfaceView.java`)
- Custom SurfaceView with dedicated render thread
- Targets 60 FPS game loop
- Handles coordinate scaling for different screen sizes
- Letterboxing to maintain 16:9 aspect ratio

**Scaling System:**
- Target resolution: 1920x1080 (landscape)
- Automatic letterboxing for different aspect ratios
- Touch coordinates automatically converted to game coordinates

### Input System

#### TouchInputManager (`input/TouchInputManager.java`)
- Translates touch events to game actions
- Thread-safe with pending event flags (UI thread → game thread)
- Supports multi-touch for simultaneous inputs
- Pinch-to-zoom gesture support

**Touch-to-Keyboard Mapping:**
| Touch Control | Desktop Equivalent |
|--------------|-------------------|
| D-Pad | W/A/S/D keys |
| Jump Button | Space |
| Attack Button | F / Left Click |
| Interact Button | E |
| Sprint Button | Shift |
| Inventory Button | I |
| Menu Button | M |
| Screen Tap | Mouse position + click |

#### TouchControlOverlay (`ui/TouchControlOverlay.java`)
- On-screen virtual controls
- Configurable opacity
- 8-way D-Pad with visual feedback
- Action buttons positioned for comfortable thumb access

#### AndroidControllerManager (`input/AndroidControllerManager.java`)
- Gamepad/controller support
- Virtual mouse cursor for controller navigation
- Button mapping for standard Android gamepads

### Scene System

#### AndroidSceneManager (`scene/AndroidSceneManager.java`)
- Singleton scene manager
- Fade transitions between scenes
- Settings overlay support
- Auto-save on pause

#### Available Scenes
- `MainMenuScene` - Main menu with Play, Creative, Character, etc.
- `LevelSelectionScene` - Level selection screen
- `SpriteCharacterCustomization` - Character customization
- `OverworldScene` - Main gameplay overworld
- `CreativeScene` - Creative/sandbox mode
- `LootGameScene` - Loot game mini-game
- `GameScene` - In-level gameplay

### Audio System

#### AndroidAudioManager (`audio/AndroidAudioManager.java`)
- Background music playback (MediaPlayer)
- Sound effects (SoundPool)
- Volume control synced with GamePreferences
- Pause/resume with activity lifecycle

### Save System

#### GamePreferences (`core/GamePreferences.java`)
- SharedPreferences wrapper for settings
- Music/SFX volume
- Touch control opacity
- Vibration toggle

#### CloudSaveManager (`save/CloudSaveManager.java`)
- Local save file management
- Cloud sync support (Google Play Games - planned)

### Asset Loading

#### AndroidAssetLoader (`graphics/AndroidAssetLoader.java`)
- Loads images from assets folder
- Simple HashMap cache for loaded images
- Converts assets to Android Bitmap format

## Build System

The Android port uses command-line build tools instead of Gradle for simplicity:

### Requirements
- JDK 11 or higher (JDK 21 recommended)
- Android SDK with:
  - Build Tools 34.0.0 (aapt2, d8, apksigner)
  - Platform Tools (adb)
  - Platform android-34 (android.jar)

### Build Process (`scripts/build.bat`)

1. **Compile Java** - javac compiles all .java files against android.jar
2. **Create JAR** - Package classes into intermediate JAR
3. **DEX Conversion** - d8 converts JAR to Dalvik bytecode (classes.dex)
4. **Resource Compilation** - aapt2 compiles and links resources
5. **APK Assembly** - Combine DEX + resources into unsigned APK
6. **Signing** - Sign APK with debug keystore (auto-generated)
7. **Alignment** - zipalign for optimized APK

### Quick Commands

**Build only:**
```batch
cd android\scripts
build.bat
```

**Build + Install + Launch:**
```batch
cd android\scripts
run.bat
```

## Threading Model

The app uses multiple threads:

1. **UI Thread** - Android main thread, handles touch events
2. **Game Thread** - Runs game loop (update + render) at 60 FPS
3. **Audio Thread** - MediaPlayer runs on separate thread

### Thread Safety

Touch events use a pending flag system to safely transfer data between threads:
- UI thread sets `pendingTouchDown`/`pendingTouchUp` flags
- Game thread consumes these in `resetFrame()` at start of each update
- Prevents race conditions where touch events could be lost

## Configuration

### AndroidManifest.xml Settings

- **minSdkVersion**: 24 (Android 7.0)
- **targetSdkVersion**: 34 (Android 14)
- **screenOrientation**: sensorLandscape (both landscape orientations)
- **configChanges**: Handles orientation/size changes without restart
- **hardwareAccelerated**: true
- **largeHeap**: true (for asset loading)

### Fullscreen Mode

Uses immersive sticky mode:
- Hides status bar and navigation bar
- Bars appear temporarily on swipe, then auto-hide
- Works on Android 11+ (WindowInsetsController) and legacy (system UI flags)

## Known Issues & Limitations

1. **No Gradle** - Uses command-line tools only, limiting IDE integration
2. **Debug signing only** - Release signing not yet configured
3. **No ProGuard** - Code is not obfuscated/optimized
4. **Cloud saves** - Google Play Games integration not yet implemented
5. **Asset loading** - No asset compression or texture atlasing

## Future Improvements

- [ ] Gradle build system for IDE integration
- [ ] Release signing configuration
- [ ] Google Play Games Services (achievements, leaderboards, cloud saves)
- [ ] Adaptive icons for Android 8.0+
- [ ] ProGuard/R8 code shrinking
- [ ] Asset compression and texture atlasing
- [ ] Performance profiling and optimization
- [ ] Tablet-optimized UI layout

## Troubleshooting

### App crashes on launch
- Check logcat: `adb logcat -s AndroidRuntime:E`
- Common causes: missing assets, initialization order issues

### Touch not responding
- Ensure GameSurfaceView.onTouchEvent forwards to TouchInputManager
- Check for race conditions in touch state flags

### Black screen
- Verify scene is registered and initialized
- Check if draw() method is being called
- Confirm scaling calculation in GameSurfaceView

### Build failures
- Ensure JDK bin directory is in PATH (for jar, keytool)
- Verify Android SDK paths in build.bat
- Check for spaces in paths (use quotes)

## Version History

- **1.0** - Initial Android port
  - Core gameplay ported from desktop
  - Touch controls with virtual D-pad
  - Controller support
  - Basic audio system
  - Scene management with transitions
