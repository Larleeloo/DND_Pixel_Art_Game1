#!/bin/bash
# ============================================================
# Loot Game App - Android Build Script (Linux/macOS)
# ============================================================
# Builds the Loot Game APK without Gradle
# Requires: Android SDK with build-tools and platform-tools
# ============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOOTGAME_DIR="$(dirname "$SCRIPT_DIR")"
ANDROID_DIR="$(dirname "$LOOTGAME_DIR")/android"

# Configuration
SDK_ROOT="$ANDROID_DIR/sdk"
BUILD_TOOLS="$SDK_ROOT/build-tools/34.0.0"
PLATFORM="$SDK_ROOT/platforms/android-34"
APP_DIR="$LOOTGAME_DIR/app"
BUILD_DIR="$APP_DIR/build"
SRC_DIR="$APP_DIR/src/main/java"
RES_DIR="$APP_DIR/src/main/res"
ASSETS_DIR="$APP_DIR/src/main/assets"
MANIFEST="$APP_DIR/src/main/AndroidManifest.xml"
LIBS_DIR="$APP_DIR/libs"
KEYSTORE="$LOOTGAME_DIR/keystore/debug.keystore"

echo "============================================================"
echo "Loot Game App - Android Build"
echo "============================================================"

# Check SDK exists
if [ ! -f "$BUILD_TOOLS/aapt2" ]; then
    echo "ERROR: Android SDK build-tools not found at $BUILD_TOOLS"
    echo "Please ensure the Android SDK is installed at: $SDK_ROOT"
    echo "(Uses the same SDK as the main Android port)"
    exit 1
fi

if [ ! -f "$PLATFORM/android.jar" ]; then
    echo "ERROR: Android platform not found at $PLATFORM"
    exit 1
fi

# Clean build directory
echo "Cleaning build directory..."
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/classes"
mkdir -p "$BUILD_DIR/dex"
mkdir -p "$BUILD_DIR/res"
mkdir -p "$BUILD_DIR/gen"

# Compile resources
echo "Compiling resources..."
"$BUILD_TOOLS/aapt2" compile --dir "$RES_DIR" -o "$BUILD_DIR/res.zip"

# Link resources
echo "Linking resources..."
"$BUILD_TOOLS/aapt2" link \
    -o "$BUILD_DIR/app.unsigned.apk" \
    -I "$PLATFORM/android.jar" \
    --manifest "$MANIFEST" \
    -R "$BUILD_DIR/res.zip" \
    --auto-add-overlay \
    --java "$BUILD_DIR/gen"

# Build classpath
CLASSPATH="$PLATFORM/android.jar"
for jar in "$LIBS_DIR"/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

# Find all Java source files
find "$SRC_DIR" -name "*.java" > "$BUILD_DIR/sources.txt"
JAVA_COUNT=$(wc -l < "$BUILD_DIR/sources.txt")
echo "Compiling $JAVA_COUNT Java sources..."

# Compile Java source files
javac -source 1.8 -target 1.8 \
    -classpath "$CLASSPATH" \
    -d "$BUILD_DIR/classes" \
    @"$BUILD_DIR/sources.txt" 2>&1

# Create JAR from classes
echo "Creating intermediate JAR..."
cd "$BUILD_DIR/classes"
jar -cf "$BUILD_DIR/classes.jar" .
cd "$SCRIPT_DIR"

# Convert to DEX
echo "Converting to DEX format..."
"$BUILD_TOOLS/d8" \
    --output "$BUILD_DIR/dex" \
    --lib "$PLATFORM/android.jar" \
    "$BUILD_DIR/classes.jar"

# Add DEX to APK
echo "Adding DEX to APK..."
cp "$BUILD_DIR/app.unsigned.apk" "$BUILD_DIR/app.temp.apk"
cd "$BUILD_DIR/dex"
zip -u "../app.temp.apk" classes.dex
cd "$SCRIPT_DIR"

# Add assets if they exist
if [ -d "$ASSETS_DIR" ]; then
    echo "Adding assets..."
    cd "$APP_DIR/src/main"
    zip -r "$BUILD_DIR/app.temp.apk" assets
    cd "$SCRIPT_DIR"
fi

# Align APK
echo "Aligning APK..."
"$BUILD_TOOLS/zipalign" -f -p 4 \
    "$BUILD_DIR/app.temp.apk" \
    "$BUILD_DIR/app.aligned.apk"

# Create debug keystore if needed
if [ ! -f "$KEYSTORE" ]; then
    echo "Creating debug keystore..."
    mkdir -p "$(dirname "$KEYSTORE")"
    keytool -genkey -v \
        -keystore "$KEYSTORE" \
        -storepass android \
        -alias androiddebugkey \
        -keypass android \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -dname "CN=Debug, OU=Debug, O=Debug, L=Debug, S=Debug, C=US"
fi

# Sign APK
echo "Signing APK..."
"$BUILD_TOOLS/apksigner" sign \
    --ks "$KEYSTORE" \
    --ks-pass pass:android \
    --out "$BUILD_DIR/loot-game.apk" \
    "$BUILD_DIR/app.aligned.apk"

# Clean up temp files
rm -f "$BUILD_DIR/app.unsigned.apk"
rm -f "$BUILD_DIR/app.temp.apk"
rm -f "$BUILD_DIR/app.aligned.apk"
rm -f "$BUILD_DIR/res.zip"
rm -f "$BUILD_DIR/sources.txt"
rm -f "$BUILD_DIR/classes.jar"

echo "============================================================"
echo "BUILD SUCCESSFUL!"
echo "APK: $BUILD_DIR/loot-game.apk"
echo "============================================================"
echo ""
echo "To install: $SDK_ROOT/platform-tools/adb install -r \"$BUILD_DIR/loot-game.apk\""
echo "To launch:  $SDK_ROOT/platform-tools/adb shell am start -n com.ambermoon.lootgame/.core.MainActivity"
echo "============================================================"
