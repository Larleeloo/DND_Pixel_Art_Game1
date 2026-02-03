@echo off
REM ============================================================
REM The Amber Moon - Run Emulator Script (Windows)
REM ============================================================
REM Starts the Pixel 7a emulator and optionally installs the APK
REM ============================================================

setlocal

set SDK_ROOT=%~dp0..\sdk
set EMULATOR=%SDK_ROOT%\emulator\emulator.exe
set ADB=%SDK_ROOT%\platform-tools\adb.exe
set APK=%~dp0..\app\build\amber-moon.apk

echo ============================================================
echo The Amber Moon - Android Emulator
echo ============================================================

REM Check if emulator exists
if not exist "%EMULATOR%" (
    echo ERROR: Emulator not found at %EMULATOR%
    echo Please run setup-emulator.bat first.
    goto :error
)

echo Starting Pixel 7a emulator...
echo (This may take a few minutes on first launch)
echo.

REM Start emulator in background
start "" "%EMULATOR%" -avd pixel_7a -gpu auto

echo Waiting for emulator to boot...
echo.

REM Wait for device to be ready
:wait_loop
"%ADB%" wait-for-device
"%ADB%" shell getprop sys.boot_completed 2>nul | findstr "1" >nul
if errorlevel 1 (
    timeout /t 2 /nobreak >nul
    goto :wait_loop
)

echo Emulator is ready!
echo.

REM Check if APK exists and offer to install
if exist "%APK%" (
    echo Found APK: %APK%
    echo Installing app...
    "%ADB%" install -r "%APK%"
    if errorlevel 1 (
        echo Warning: Installation failed. You may need to rebuild.
    ) else (
        echo.
        echo App installed successfully!
        echo.
        set /p LAUNCH="Launch the app now? (Y/N): "
        if /i "!LAUNCH!"=="Y" (
            echo Launching The Amber Moon...
            "%ADB%" shell am start -n com.ambermoongame/.core.MainActivity
        )
    )
) else (
    echo No APK found. Run build.bat first to create the APK.
)

echo.
echo ============================================================
echo Emulator is running. Useful commands:
echo.
echo   Install APK:  %ADB% install -r "%APK%"
echo   Launch app:   %ADB% shell am start -n com.ambermoongame/.core.MainActivity
echo   View logs:    %ADB% logcat -s AmberMoon
echo   Stop app:     %ADB% shell am force-stop com.ambermoongame
echo   Screenshot:   %ADB% shell screencap -p /sdcard/screen.png
echo ============================================================
goto :end

:error
echo ============================================================
echo FAILED!
echo ============================================================
exit /b 1

:end
endlocal
