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
javac --release 11 ^
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

REM Find jar.exe - try multiple methods
set JAR_CMD=
REM Method 1: Check JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\jar.exe" set JAR_CMD=%JAVA_HOME%\bin\jar.exe
)
REM Method 2: Search in Program Files for JDK
if "%JAR_CMD%"=="" (
    for /d %%d in ("C:\Program Files\Java\jdk*") do (
        if exist "%%d\bin\jar.exe" set JAR_CMD=%%d\bin\jar.exe
    )
)
if "%JAR_CMD%"=="" (
    for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk*") do (
        if exist "%%d\bin\jar.exe" set JAR_CMD=%%d\bin\jar.exe
    )
)
if "%JAR_CMD%"=="" (
    for /d %%d in ("C:\Program Files\Microsoft\jdk*") do (
        if exist "%%d\bin\jar.exe" set JAR_CMD=%%d\bin\jar.exe
    )
)
if "%JAR_CMD%"=="" (
    for /d %%d in ("C:\Program Files\Amazon Corretto\jdk*") do (
        if exist "%%d\bin\jar.exe" set JAR_CMD=%%d\bin\jar.exe
    )
)

if "%JAR_CMD%"=="" (
    echo ERROR: Could not find jar.exe. Please set JAVA_HOME or install a JDK.
    echo Looked in: JAVA_HOME, Program Files\Java, Eclipse Adoptium, Microsoft, Amazon Corretto
    goto :error
)

echo Found jar at: %JAR_CMD%
echo Creating intermediate JAR...
pushd "%BUILD_DIR%\classes"
"%JAR_CMD%" -cf "..\classes.jar" .
popd
if errorlevel 1 goto :error

REM Now run D8 on the JAR file
"%BUILD_TOOLS%\d8.bat" ^
    --output "%BUILD_DIR%\dex" ^
    --lib "%PLATFORM%\android.jar" ^
    "%BUILD_DIR%\classes.jar"
if errorlevel 1 goto :error

REM Add DEX to APK using PowerShell
echo Adding DEX to APK...
copy "%BUILD_DIR%\app.unsigned.apk" "%BUILD_DIR%\app.temp.apk" >nul
powershell -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $zip = [System.IO.Compression.ZipFile]::Open('%BUILD_DIR%\app.temp.apk', 'Update'); $entry = $zip.CreateEntry('classes.dex'); $stream = $entry.Open(); $bytes = [System.IO.File]::ReadAllBytes('%BUILD_DIR%\dex\classes.dex'); $stream.Write($bytes, 0, $bytes.Length); $stream.Close(); $zip.Dispose()"
if errorlevel 1 goto :error

REM Add assets if they exist
if exist "%ASSETS_DIR%" (
    echo Adding assets...
    powershell -Command "$assetsDir = '%ASSETS_DIR%'; $apkPath = '%BUILD_DIR%\app.temp.apk'; if (Test-Path $assetsDir) { Add-Type -AssemblyName System.IO.Compression.FileSystem; $zip = [System.IO.Compression.ZipFile]::Open($apkPath, 'Update'); Get-ChildItem -Path $assetsDir -Recurse -File | ForEach-Object { $relativePath = 'assets/' + $_.FullName.Substring($assetsDir.Length + 1).Replace('\', '/'); $entry = $zip.CreateEntry($relativePath); $stream = $entry.Open(); $bytes = [System.IO.File]::ReadAllBytes($_.FullName); $stream.Write($bytes, 0, $bytes.Length); $stream.Close() }; $zip.Dispose() }"
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
    mkdir "%~dp0..\keystore" 2>nul
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
del "%BUILD_DIR%\classes.jar" 2>nul

echo ============================================================
echo BUILD SUCCESSFUL!
echo APK: %BUILD_DIR%\amber-moon.apk
echo ============================================================
echo.
echo To install on emulator or device:
echo   %SDK_ROOT%\platform-tools\adb.exe install -r "%BUILD_DIR%\amber-moon.apk"
echo.
echo To launch the app:
echo   %SDK_ROOT%\platform-tools\adb.exe shell am start -n com.ambermoongame/.core.MainActivity
echo ============================================================
goto :end

:error
echo ============================================================
echo BUILD FAILED!
echo ============================================================
exit /b 1

:end
endlocal
