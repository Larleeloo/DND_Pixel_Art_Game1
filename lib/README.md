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

## File Structure After Setup

```
lib/
├── README.md           (this file)
└── jlayer-1.0.1.jar    (download and place here)
```
