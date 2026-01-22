@echo off
REM ============================================================================
REM Heronix-uMonitor v1.1 - Portable Installer Creator
REM Creates a lightweight installer without bundled JRE
REM By Michael Katsaros
REM ============================================================================

echo.
echo ============================================================================
echo   HERONIX-uMONITOR v1.1 - PORTABLE INSTALLER CREATOR
echo   Creating lightweight Windows installer...
echo ============================================================================
echo.

REM Check if JAR exists
if not exist HeronixuMonitor.jar (
    echo ERROR: HeronixuMonitor.jar not found!
    echo Please compile the project first using compile-java21.bat
    pause
    exit /b 1
)

echo [1/3] Preparing installer package...
echo.

REM Create installer directory
if exist "portable-installer" rd /s /q "portable-installer"
mkdir "portable-installer"
mkdir "portable-installer\Heronix-uMonitor"

REM Copy files
echo Copying application files...
copy HeronixuMonitor.jar "portable-installer\Heronix-uMonitor\" >nul
copy RUN_HERE.bat "portable-installer\Heronix-uMonitor\" >nul
copy README_FOR_USERS.txt "portable-installer\Heronix-uMonitor\" >nul

echo [2/3] Creating installer script...
echo.

REM Create the installer script
(
echo @echo off
echo REM ============================================================================
echo REM Heronix-uMonitor v1.1 - Installation Wizard
echo REM By Michael Katsaros
echo REM ============================================================================
echo.
echo cls
echo echo.
echo echo ============================================================================
echo echo   HERONIX-uMONITOR v1.1 - INSTALLATION WIZARD
echo echo   By Michael Katsaros
echo echo ============================================================================
echo echo.
echo echo Welcome to Heronix-uMonitor installer!
echo echo.
echo echo This installer will:
echo echo  - Install Heronix-uMonitor to your chosen location
echo echo  - Create a desktop shortcut
echo echo  - Create a start menu entry
echo echo.
echo echo Press any key to continue or close this window to cancel...
echo pause ^>nul
echo.
echo REM Check for Java
echo echo.
echo echo [1/4] Checking for Java installation...
echo java -version ^>nul 2^>^&1
echo if errorlevel 1 ^(
echo     echo.
echo     echo ============================================================================
echo     echo WARNING: Java is not installed!
echo     echo ============================================================================
echo     echo.
echo     echo Heronix-uMonitor requires Java 21 or newer to run.
echo     echo.
echo     echo Please download and install Java from:
echo     echo   https://www.oracle.com/java/technologies/downloads/
echo     echo.
echo     echo After installing Java, run this installer again.
echo     echo.
echo     echo Press any key to exit...
echo     pause ^>nul
echo     exit /b 1
echo ^)
echo.
echo echo Java found! Continuing installation...
echo echo.
echo.
echo REM Ask for installation directory
echo set "INSTALL_DIR=%%ProgramFiles%%\Heronix-uMonitor"
echo echo [2/4] Choose installation directory
echo echo.
echo echo Default: %%INSTALL_DIR%%
echo echo.
echo set /p "CUSTOM_DIR=Enter custom path or press ENTER for default: "
echo if not "%%CUSTOM_DIR%%"=="" set "INSTALL_DIR=%%CUSTOM_DIR%%"
echo.
echo echo.
echo echo Installing to: %%INSTALL_DIR%%
echo echo.
echo.
echo REM Create installation directory
echo if not exist "%%INSTALL_DIR%%" mkdir "%%INSTALL_DIR%%"
echo.
echo echo [3/4] Copying files...
echo xcopy /E /I /Y "Heronix-uMonitor" "%%INSTALL_DIR%%" ^>nul
echo.
echo if errorlevel 1 ^(
echo     echo.
echo     echo ERROR: Failed to copy files!
echo     echo Try running as Administrator.
echo     echo.
echo     pause
echo     exit /b 1
echo ^)
echo.
echo echo [4/4] Creating shortcuts...
echo echo.
echo.
echo REM Create desktop shortcut
echo set "DESKTOP=%%USERPROFILE%%\Desktop"
echo set "SHORTCUT=%%DESKTOP%%\Heronix-uMonitor.lnk"
echo.
echo ^(
echo echo Set oWS = WScript.CreateObject^("WScript.Shell"^)
echo echo sLinkFile = "%%SHORTCUT%%"
echo echo Set oLink = oWS.CreateShortcut^(sLinkFile^)
echo echo oLink.TargetPath = "%%INSTALL_DIR%%\RUN_HERE.bat"
echo echo oLink.WorkingDirectory = "%%INSTALL_DIR%%"
echo echo oLink.Description = "Heronix-uMonitor - System Monitoring Tool"
echo echo oLink.Save
echo ^) ^> "%%TEMP%%\create-shortcut.vbs"
echo.
echo cscript //nologo "%%TEMP%%\create-shortcut.vbs"
echo del "%%TEMP%%\create-shortcut.vbs"
echo.
echo REM Create start menu shortcut
echo set "STARTMENU=%%APPDATA%%\Microsoft\Windows\Start Menu\Programs"
echo if not exist "%%STARTMENU%%\Heronix-uMonitor" mkdir "%%STARTMENU%%\Heronix-uMonitor"
echo set "SHORTCUT=%%STARTMENU%%\Heronix-uMonitor\Heronix-uMonitor.lnk"
echo.
echo ^(
echo echo Set oWS = WScript.CreateObject^("WScript.Shell"^)
echo echo sLinkFile = "%%SHORTCUT%%"
echo echo Set oLink = oWS.CreateShortcut^(sLinkFile^)
echo echo oLink.TargetPath = "%%INSTALL_DIR%%\RUN_HERE.bat"
echo echo oLink.WorkingDirectory = "%%INSTALL_DIR%%"
echo echo oLink.Description = "Heronix-uMonitor - System Monitoring Tool"
echo echo oLink.Save
echo ^) ^> "%%TEMP%%\create-startmenu.vbs"
echo.
echo cscript //nologo "%%TEMP%%\create-startmenu.vbs"
echo del "%%TEMP%%\create-startmenu.vbs"
echo.
echo echo.
echo echo ============================================================================
echo echo   INSTALLATION COMPLETE!
echo echo ============================================================================
echo echo.
echo echo Heronix-uMonitor v1.1 has been installed successfully!
echo echo.
echo echo Installation location: %%INSTALL_DIR%%
echo echo.
echo echo Shortcuts created:
echo echo  - Desktop: Heronix-uMonitor
echo echo  - Start Menu: Programs ^> Heronix-uMonitor
echo echo.
echo echo You can now run Heronix-uMonitor from:
echo echo  - Desktop shortcut
echo echo  - Start Menu
echo echo  - Or directly from: %%INSTALL_DIR%%\RUN_HERE.bat
echo echo.
echo echo Would you like to run Heronix-uMonitor now? ^(Y/N^)
echo set /p "RUN_NOW="
echo.
echo if /i "%%RUN_NOW%%"=="Y" ^(
echo     echo Starting Heronix-uMonitor...
echo     start "" "%%INSTALL_DIR%%\RUN_HERE.bat"
echo ^)
echo.
echo echo Thank you for installing Heronix-uMonitor!
echo echo.
echo pause
) > "portable-installer\INSTALL.bat"

echo [3/3] Creating uninstaller...
echo.

REM Create uninstaller
(
echo @echo off
echo REM ============================================================================
echo REM Heronix-uMonitor v1.1 - Uninstaller
echo REM By Michael Katsaros
echo REM ============================================================================
echo.
echo cls
echo echo.
echo echo ============================================================================
echo echo   HERONIX-uMONITOR v1.1 - UNINSTALLER
echo echo ============================================================================
echo echo.
echo echo This will remove Heronix-uMonitor from your computer.
echo echo.
echo echo Are you sure you want to uninstall? ^(Y/N^)
echo set /p "CONFIRM="
echo.
echo if /i not "%%CONFIRM%%"=="Y" ^(
echo     echo Uninstall cancelled.
echo     pause
echo     exit /b 0
echo ^)
echo.
echo echo.
echo echo Removing Heronix-uMonitor...
echo echo.
echo.
echo REM Remove shortcuts
echo echo Removing shortcuts...
echo del "%%USERPROFILE%%\Desktop\Heronix-uMonitor.lnk" 2^>nul
echo rd /s /q "%%APPDATA%%\Microsoft\Windows\Start Menu\Programs\Heronix-uMonitor" 2^>nul
echo.
echo REM Remove installation directory
echo echo Removing program files...
echo set "INSTALL_DIR=%%~dp0"
echo cd /d "%%TEMP%%"
echo rd /s /q "%%INSTALL_DIR%%" 2^>nul
echo.
echo echo.
echo echo ============================================================================
echo echo   UNINSTALL COMPLETE
echo echo ============================================================================
echo echo.
echo echo Heronix-uMonitor has been removed from your computer.
echo echo.
echo echo Thank you for using Heronix-uMonitor!
echo echo.
echo pause
) > "portable-installer\Heronix-uMonitor\UNINSTALL.bat"

echo.
echo ============================================================================
echo   SUCCESS! Portable Installer Package Created
echo ============================================================================
echo.
echo Location: portable-installer\
echo.
echo Contents:
echo  - INSTALL.bat (Run this to install)
echo  - Heronix-uMonitor\ (Application folder)
echo    - HeronixuMonitor.jar
echo    - RUN_HERE.bat
echo    - README_FOR_USERS.txt
echo    - UNINSTALL.bat
echo.
echo DISTRIBUTION:
echo  1. Zip the 'portable-installer' folder
echo  2. Rename to: Heronix-uMonitor-v1.1-Installer.zip
echo  3. Users extract and run INSTALL.bat
echo.
echo FEATURES:
echo  - Checks for Java installation
echo  - Allows custom install location
echo  - Creates desktop shortcut
echo  - Creates start menu entry
echo  - Includes uninstaller
echo  - Lightweight (only 61KB + scripts)
echo.
echo ============================================================================
echo.

pause
