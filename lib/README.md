# External Libraries

This folder contains external JAR dependencies for the game.

## Required: JLayer (MP3 Support)

JLayer is required for MP3 audio playback. Without it, only WAV files will work.

### Download

Download `jlayer-1.0.1.jar` from one of these sources:

1. **Maven Repository (Recommended)**:
   https://mvnrepository.com/artifact/javazoom/jlayer/1.0.1
   Direct download: https://repo1.maven.org/maven2/javazoom/jlayer/1.0.1/jlayer-1.0.1.jar

2. **GitHub**:
   https://github.com/umjammer/jlayer/releases

### Installation

1. Download `jlayer-1.0.1.jar`
2. Place it in this `lib/` folder
3. Add it to your project classpath (see IDE instructions below)

### IntelliJ IDEA Setup

1. Open **File > Project Structure** (Ctrl+Alt+Shift+S)
2. Select **Modules** in the left panel
3. Click the **Dependencies** tab
4. Click the **+** button and select **JARs or directories...**
5. Navigate to `lib/jlayer-1.0.1.jar` and click **OK**
6. Click **Apply** and **OK**

### Eclipse Setup

1. Right-click the project > **Properties**
2. Select **Java Build Path**
3. Click the **Libraries** tab
4. Click **Add JARs...** or **Add External JARs...**
5. Select `lib/jlayer-1.0.1.jar`
6. Click **Apply and Close**

### VS Code Setup

Add to your `.vscode/settings.json`:
```json
{
    "java.project.referencedLibraries": [
        "lib/**/*.jar"
    ]
}
```

### Command Line Compilation

```bash
# Compile
javac -cp "lib/*:src" -d out src/**/*.java

# Run
java -cp "lib/*:out" core.Main
```

### Verification

When the game starts, you should see:
```
JLayer library found - MP3 support enabled
AudioManager initialized with MP3 support
```

If JLayer is missing, you'll see:
```
JLayer library not found - MP3 support disabled
AudioManager initialized (WAV only)
```

## Optional: JInput (Xbox Controller Support)

JInput enables Xbox and other game controller support.

### Download

Download from one of these sources:

1. **Maven Repository (Recommended)**:
   https://mvnrepository.com/artifact/net.java.jinput/jinput/2.0.10
   Direct JAR: https://repo1.maven.org/maven2/net/java/jinput/jinput/2.0.10/jinput-2.0.10.jar

2. **JInput Platform (Native libraries)**:
   https://mvnrepository.com/artifact/net.java.jinput/jinput-platform/2.0.10
   - Download the JAR for your platform (natives-windows, natives-linux, natives-osx)
   - You need the native libraries for your operating system

### Installation

1. Download `jinput-2.0.10.jar`
2. Download the platform-specific native library JAR (e.g., `jinput-platform-2.0.10-natives-windows.jar`)
3. Extract the native DLL/SO files from the platform JAR to the project root or `lib/` folder
4. Place all JARs in this `lib/` folder
5. Add the JARs to your project classpath (see IDE instructions below)

### IntelliJ IDEA Setup

1. Open **File > Project Structure** (Ctrl+Alt+Shift+S)
2. Select **Modules** in the left panel
3. Click the **Dependencies** tab
4. Click the **+** button and select **JARs or directories...**
5. Navigate to `lib/jinput-2.0.10.jar` and click **OK**
6. Repeat for any platform-specific JARs
7. Click **Apply** and **OK**

**Native Library Setup (Important for controllers):**
1. Extract native libraries (.dll, .so, .dylib) from the platform JAR
2. Go to **Run > Edit Configurations**
3. Add VM options: `-Djava.library.path=lib`
   Or place native files in the project root directory

### Eclipse Setup

1. Right-click the project > **Properties**
2. Select **Java Build Path**
3. Click the **Libraries** tab
4. Click **Add JARs...** or **Add External JARs...**
5. Select `lib/jinput-2.0.10.jar`
6. Click **Apply and Close**

**Native Library Setup:**
1. Right-click the project > **Properties**
2. Go to **Java Build Path > Libraries**
3. Expand jinput JAR entry
4. Edit **Native library location**
5. Set to `lib/` folder containing native files

### VS Code Setup

Add to your `.vscode/settings.json`:
```json
{
    "java.project.referencedLibraries": [
        "lib/**/*.jar"
    ]
}
```

For native libraries, create `.vscode/launch.json`:
```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Launch Main",
            "request": "launch",
            "mainClass": "core.Main",
            "vmArgs": "-Djava.library.path=lib"
        }
    ]
}
```

### Native Library Files

JInput requires platform-specific native libraries:

| Platform | Files Required |
|----------|----------------|
| Windows  | jinput-dx8_64.dll, jinput-dx8.dll, jinput-raw_64.dll, jinput-raw.dll |
| Linux    | libjinput-linux64.so, libjinput-linux.so |
| macOS    | libjinput-osx.jnilib |

### Verification

When the game starts with controller support enabled:
```
JInput library found - Controller support enabled
Xbox controller connected: [Controller Name]
```

If JInput is missing or no controller found:
```
JInput library not found - Controller support disabled
No Xbox controller found. Connect a controller and restart the game.
```

### Xbox Controller Button Mappings

| Controller Input | Game Action |
|------------------|-------------|
| Right Stick Up | W (Move up/climb) |
| Right Stick Down | S (Move down) |
| Right Stick Left | A (Move left) |
| Right Stick Right | D (Move right) |
| Left Stick | Mouse cursor movement |
| Left Stick Click (L3) | Left mouse click |
| A Button | Space (Jump) |
| X Button | E (Interact/Mine) |
| Y Button | I (Inventory) |
| Start Button | M (Menu/Settings) |

## File Structure After Setup

```
lib/
├── README.md                    (this file)
├── jlayer-1.0.1.jar             (download and place here)
├── jinput-2.0.10.jar            (optional - for controller support)
├── jinput-platform-*.jar        (optional - platform natives)
└── [native libraries]           (extracted from platform JAR)
```
