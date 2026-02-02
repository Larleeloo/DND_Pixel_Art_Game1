@echo off
REM ============================================================
REM The Amber Moon - Android Build Script (Windows)
REM ============================================================
REM This script builds the Android APK without Gradle
REM Requires: Android SDK with build-tools and platform-tools
REM ============================================================

setlocal enabledelayedexpansion

REM Configuration
set SDK_ROOT=%~dp0..\sdk
set BUILD_TOOLS=%SDK_ROOT%\build-tools\34.0.0
set PLATFORM=%SDK_ROOT%\platforms\android-34
set APP_DIR=%~dp0..\app
set BUILD_DIR=%APP_DIR%\build
set SRC_DIR=%APP_DIR%\src\main\java
set RES_DIR=%APP_DIR%\src\main\res
set ASSETS_DIR=%APP_DIR%\src\main\assets
set MANIFEST=%APP_DIR%\src\main\AndroidManifest.xml
set LIBS_DIR=%APP_DIR%\libs
set KEYSTORE=%~dp0..\keystore\debug.keystore

echo ============================================================
echo The Amber Moon - Android Build
echo ============================================================

REM Check SDK exists
if not exist "%BUILD_TOOLS%\aapt2.exe" (
    echo ERROR: Android SDK build-tools not found at %BUILD_TOOLS%
    echo Please download the Android SDK and extract to the sdk folder.
    goto :error
)

REM Clean build directory
echo Cleaning build directory...
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
mkdir "%BUILD_DIR%"
mkdir "%BUILD_DIR%\classes"
mkdir "%BUILD_DIR%\dex"
mkdir "%BUILD_DIR%\res"

REM Compile resources
echo Compiling resources...
"%BUILD_TOOLS%\aapt2.exe" compile --dir "%RES_DIR%" -o "%BUILD_DIR%\res.zip"
if errorlevel 1 goto :error

REM Link resources
echo Linking resources...
"%BUILD_TOOLS%\aapt2.exe" link ^
    -o "%BUILD_DIR%\app.unsigned.apk" ^
    -I "%PLATFORM%\android.jar" ^
    --manifest "%MANIFEST%" ^
    -R "%BUILD_DIR%\res.zip" ^
    --auto-add-overlay ^
    --java "%BUILD_DIR%\gen"
if errorlevel 1 goto :error

REM Compile Java source files
echo Compiling Java sources...
set CLASSPATH=%PLATFORM%\android.jar
for %%f in ("%LIBS_DIR%\*.jar") do set CLASSPATH=!CLASSPATH!;%%f

dir /s /b "%SRC_DIR%\*.java" > "%BUILD_DIR%\sources.txt"
javac -source 1.8 -target 1.8 ^
    -classpath "%CLASSPATH%" ^
    -d "%BUILD_DIR%\classes" ^
    @"%BUILD_DIR%\sources.txt" ^
    2> "%BUILD_DIR%\compile_errors.txt"
if errorlevel 1 (
    type "%BUILD_DIR%\compile_errors.txt"
    goto :error
)

REM Convert to DEX
echo Converting to DEX format...
"%BUILD_TOOLS%\d8.bat" ^
    --output "%BUILD_DIR%\dex" ^
    --lib "%PLATFORM%\android.jar" ^
    "%BUILD_DIR%\classes"
if errorlevel 1 goto :error

REM Add DEX to APK
echo Adding DEX to APK...
copy "%BUILD_DIR%\app.unsigned.apk" "%BUILD_DIR%\app.temp.apk" >nul
cd "%BUILD_DIR%\dex"
"%SDK_ROOT%\build-tools\34.0.0\..\..\platform-tools\adb.exe" >nul 2>&1
jar -uf "..\app.temp.apk" classes.dex
cd "%~dp0"
if errorlevel 1 goto :error

REM Add assets if they exist
if exist "%ASSETS_DIR%" (
    echo Adding assets...
    cd "%APP_DIR%\src\main"
    jar -uf "%BUILD_DIR%\app.temp.apk" assets
    cd "%~dp0"
)

REM Align APK
echo Aligning APK...
"%BUILD_TOOLS%\zipalign.exe" -f -p 4 ^
    "%BUILD_DIR%\app.temp.apk" ^
    "%BUILD_DIR%\app.aligned.apk"
if errorlevel 1 goto :error

REM Create debug keystore if needed
if not exist "%KEYSTORE%" (
    echo Creating debug keystore...
    keytool -genkey -v ^
        -keystore "%KEYSTORE%" ^
        -storepass android ^
        -alias androiddebugkey ^
        -keypass android ^
        -keyalg RSA ^
        -keysize 2048 ^
        -validity 10000 ^
        -dname "CN=Debug, OU=Debug, O=Debug, L=Debug, S=Debug, C=US"
)

REM Sign APK
echo Signing APK...
"%BUILD_TOOLS%\apksigner.bat" sign ^
    --ks "%KEYSTORE%" ^
    --ks-pass pass:android ^
    --out "%BUILD_DIR%\amber-moon.apk" ^
    "%BUILD_DIR%\app.aligned.apk"
if errorlevel 1 goto :error

REM Clean up temp files
del "%BUILD_DIR%\app.unsigned.apk" 2>nul
del "%BUILD_DIR%\app.temp.apk" 2>nul
del "%BUILD_DIR%\app.aligned.apk" 2>nul
del "%BUILD_DIR%\res.zip" 2>nul
del "%BUILD_DIR%\sources.txt" 2>nul
del "%BUILD_DIR%\compile_errors.txt" 2>nul

echo ============================================================
echo BUILD SUCCESSFUL!
echo APK: %BUILD_DIR%\amber-moon.apk
echo ============================================================
goto :end

:error
echo ============================================================
echo BUILD FAILED!
echo ============================================================
exit /b 1

:end
endlocal
