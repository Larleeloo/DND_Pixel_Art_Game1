@echo off
REM ============================================================
REM The Amber Moon - Android Build Script (Windows)
REM ============================================================
REM This script builds the Android APK without Gradle
REM Requires: Android SDK with build-tools and platform-tools
REM ============================================================

setlocal enabledelayedexpansion

REM Configuration - use short paths to avoid space issues
for %%i in ("%~dp0..") do set ANDROID_DIR=%%~fi
set SDK_ROOT=%ANDROID_DIR%\sdk
set BUILD_TOOLS=%SDK_ROOT%\build-tools\34.0.0
set PLATFORM=%SDK_ROOT%\platforms\android-34
set APP_DIR=%ANDROID_DIR%\app
set BUILD_DIR=%APP_DIR%\build
set SRC_DIR=%APP_DIR%\src\main\java
set RES_DIR=%APP_DIR%\src\main\res
set ASSETS_DIR=%APP_DIR%\src\main\assets
set MANIFEST=%APP_DIR%\src\main\AndroidManifest.xml
set LIBS_DIR=%APP_DIR%\libs
set KEYSTORE=%ANDROID_DIR%\keystore\debug.keystore

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
"%BUILD_TOOLS%\aapt2.exe" link -o "%BUILD_DIR%\app.unsigned.apk" -I "%PLATFORM%\android.jar" --manifest "%MANIFEST%" -R "%BUILD_DIR%\res.zip" --auto-add-overlay --java "%BUILD_DIR%\gen"
if errorlevel 1 goto :error

REM Compile Java source files
echo Compiling Java sources...
set CLASSPATH=%PLATFORM%\android.jar
for %%f in ("%LIBS_DIR%\*.jar") do set CLASSPATH=!CLASSPATH!;%%f

dir /s /b "%SRC_DIR%\*.java" > "%BUILD_DIR%\sources.txt"
javac --release 11 -classpath "%CLASSPATH%" -d "%BUILD_DIR%\classes" @"%BUILD_DIR%\sources.txt" 2> "%BUILD_DIR%\compile_errors.txt"
if errorlevel 1 (
    type "%BUILD_DIR%\compile_errors.txt"
    goto :error
)

REM Convert to DEX
echo Converting to DEX format...

REM Find jar.exe - try multiple methods
set JAR_CMD=
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\jar.exe" set "JAR_CMD=%JAVA_HOME%\bin\jar.exe"
)
if "%JAR_CMD%"=="" (
    for /d %%d in ("C:\Program Files\Java\jdk*") do (
        if exist "%%d\bin\jar.exe" set "JAR_CMD=%%d\bin\jar.exe"
    )
)
if "%JAR_CMD%"=="" (
    for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk*") do (
        if exist "%%d\bin\jar.exe" set "JAR_CMD=%%d\bin\jar.exe"
    )
)
if "%JAR_CMD%"=="" (
    for /d %%d in ("C:\Program Files\Microsoft\jdk*") do (
        if exist "%%d\bin\jar.exe" set "JAR_CMD=%%d\bin\jar.exe"
    )
)
if "%JAR_CMD%"=="" (
    for /d %%d in ("C:\Program Files\Amazon Corretto\jdk*") do (
        if exist "%%d\bin\jar.exe" set "JAR_CMD=%%d\bin\jar.exe"
    )
)

if "%JAR_CMD%"=="" (
    echo ERROR: Could not find jar.exe. Please set JAVA_HOME or install a JDK.
    goto :error
)

echo Found jar at: %JAR_CMD%
echo Creating intermediate JAR...
cd /d "%BUILD_DIR%\classes"
"%JAR_CMD%" -cf "%BUILD_DIR%\classes.jar" .
if errorlevel 1 (
    echo ERROR: JAR creation failed
    goto :error
)
cd /d "%~dp0"

echo Running D8 dex compiler...
call "%BUILD_TOOLS%\d8.bat" --output "%BUILD_DIR%\dex" --lib "%PLATFORM%\android.jar" "%BUILD_DIR%\classes.jar"
if errorlevel 1 (
    echo ERROR: D8 compilation failed
    goto :error
)

REM Add DEX to APK
echo Adding DEX to APK...
copy "%BUILD_DIR%\app.unsigned.apk" "%BUILD_DIR%\app.temp.apk" >nul

REM Use PowerShell script file to avoid quoting issues
echo $apkPath = '%BUILD_DIR%\app.temp.apk' > "%BUILD_DIR%\add_dex.ps1"
echo $dexPath = '%BUILD_DIR%\dex\classes.dex' >> "%BUILD_DIR%\add_dex.ps1"
echo Add-Type -AssemblyName System.IO.Compression.FileSystem >> "%BUILD_DIR%\add_dex.ps1"
echo $zip = [System.IO.Compression.ZipFile]::Open($apkPath, 'Update') >> "%BUILD_DIR%\add_dex.ps1"
echo $entry = $zip.CreateEntry('classes.dex') >> "%BUILD_DIR%\add_dex.ps1"
echo $stream = $entry.Open() >> "%BUILD_DIR%\add_dex.ps1"
echo $bytes = [System.IO.File]::ReadAllBytes($dexPath) >> "%BUILD_DIR%\add_dex.ps1"
echo $stream.Write($bytes, 0, $bytes.Length) >> "%BUILD_DIR%\add_dex.ps1"
echo $stream.Close() >> "%BUILD_DIR%\add_dex.ps1"
echo $zip.Dispose() >> "%BUILD_DIR%\add_dex.ps1"

powershell -ExecutionPolicy Bypass -File "%BUILD_DIR%\add_dex.ps1"
if errorlevel 1 (
    echo ERROR: Failed to add DEX to APK
    goto :error
)

REM Add assets if they exist
if exist "%ASSETS_DIR%" (
    echo Adding assets...
    echo $apkPath = '%BUILD_DIR%\app.temp.apk' > "%BUILD_DIR%\add_assets.ps1"
    echo $assetsDir = '%ASSETS_DIR%' >> "%BUILD_DIR%\add_assets.ps1"
    echo Add-Type -AssemblyName System.IO.Compression.FileSystem >> "%BUILD_DIR%\add_assets.ps1"
    echo $zip = [System.IO.Compression.ZipFile]::Open($apkPath, 'Update'^) >> "%BUILD_DIR%\add_assets.ps1"
    echo Get-ChildItem -Path $assetsDir -Recurse -File ^| ForEach-Object { >> "%BUILD_DIR%\add_assets.ps1"
    echo     $relativePath = 'assets/' + $_.FullName.Substring($assetsDir.Length + 1^).Replace('\', '/'^) >> "%BUILD_DIR%\add_assets.ps1"
    echo     $entry = $zip.CreateEntry($relativePath^) >> "%BUILD_DIR%\add_assets.ps1"
    echo     $stream = $entry.Open(^) >> "%BUILD_DIR%\add_assets.ps1"
    echo     $bytes = [System.IO.File]::ReadAllBytes($_.FullName^) >> "%BUILD_DIR%\add_assets.ps1"
    echo     $stream.Write($bytes, 0, $bytes.Length^) >> "%BUILD_DIR%\add_assets.ps1"
    echo     $stream.Close(^) >> "%BUILD_DIR%\add_assets.ps1"
    echo } >> "%BUILD_DIR%\add_assets.ps1"
    echo $zip.Dispose(^) >> "%BUILD_DIR%\add_assets.ps1"
    powershell -ExecutionPolicy Bypass -File "%BUILD_DIR%\add_assets.ps1"
)

REM Align APK
echo Aligning APK...
"%BUILD_TOOLS%\zipalign.exe" -f -p 4 "%BUILD_DIR%\app.temp.apk" "%BUILD_DIR%\app.aligned.apk"
if errorlevel 1 (
    echo ERROR: APK alignment failed
    goto :error
)

REM Create debug keystore if needed
if not exist "%KEYSTORE%" (
    echo Creating debug keystore...
    if not exist "%ANDROID_DIR%\keystore" mkdir "%ANDROID_DIR%\keystore"
    REM Get keytool from same directory as jar
    for %%i in ("%JAR_CMD%") do set "JAVA_BIN_DIR=%%~dpi"
    "%JAVA_BIN_DIR%keytool.exe" -genkey -v -keystore "%KEYSTORE%" -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Debug, OU=Debug, O=Debug, L=Debug, S=Debug, C=US"
    if errorlevel 1 (
        echo ERROR: Keystore creation failed
        goto :error
    )
)

REM Sign APK
echo Signing APK...
call "%BUILD_TOOLS%\apksigner.bat" sign --ks "%KEYSTORE%" --ks-pass pass:android --out "%BUILD_DIR%\amber-moon.apk" "%BUILD_DIR%\app.aligned.apk"
if errorlevel 1 (
    echo ERROR: APK signing failed
    goto :error
)

REM Clean up temp files
del "%BUILD_DIR%\app.unsigned.apk" 2>nul
del "%BUILD_DIR%\app.temp.apk" 2>nul
del "%BUILD_DIR%\app.aligned.apk" 2>nul
del "%BUILD_DIR%\res.zip" 2>nul
del "%BUILD_DIR%\sources.txt" 2>nul
del "%BUILD_DIR%\compile_errors.txt" 2>nul
del "%BUILD_DIR%\classes.jar" 2>nul
del "%BUILD_DIR%\add_dex.ps1" 2>nul
del "%BUILD_DIR%\add_assets.ps1" 2>nul

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
