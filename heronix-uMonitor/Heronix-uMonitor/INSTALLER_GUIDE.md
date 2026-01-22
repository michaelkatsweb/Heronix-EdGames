# Heronix-uMonitor v1.1 - Installer Creation Guide

## Overview

You have **3 options** for creating installers for Heronix-uMonitor:

| Option | File Size | Java Required | Complexity | Recommended |
|--------|-----------|---------------|------------|-------------|
| **Portable Installer** | ~70KB | User must have Java | Easy | ✅ Best for most users |
| **jpackage EXE** | ~50-70MB | No (bundled) | Medium | ✅ Most professional |
| **MSI Installer** | ~50-70MB | No (bundled) | Complex | ⚠️ Requires WiX Toolset |

---

## Option 1: Portable Installer (Recommended)

**Best for:** Quick distribution, small file size, users who already have Java

### How to Create:

```batch
create-portable-installer.bat
```

### What You Get:

- Lightweight installer package (~70KB)
- Professional installation wizard
- Desktop and Start Menu shortcuts
- Includes uninstaller
- Checks for Java before installing

### Distribution:

1. Run `create-portable-installer.bat`
2. Zip the `portable-installer` folder
3. Rename to: `Heronix-uMonitor-v1.1-Installer.zip`
4. Share the ZIP file

### User Experience:

1. User extracts ZIP
2. User runs `INSTALL.bat`
3. Installer checks for Java (prompts to download if missing)
4. User chooses install location
5. Creates shortcuts
6. Done!

### Advantages:
- ✅ Tiny file size (~70KB)
- ✅ Fast download/upload
- ✅ Easy to create
- ✅ No special tools needed
- ✅ Professional install experience

### Disadvantages:
- ⚠️ User must have Java 21+ installed
- ⚠️ Not a single .exe file

---

## Option 2: jpackage EXE Installer (Most Professional)

**Best for:** Professional distribution, users without Java

### How to Create:

```batch
create-installer.bat
```

### What You Get:

- Professional Windows .exe installer (~50-70MB)
- Embedded Java runtime (no Java install needed!)
- Windows native installer experience
- Installs to Program Files
- Creates Start Menu shortcuts
- Creates Desktop shortcut
- Uninstall via Control Panel (Add/Remove Programs)

### Distribution:

1. Run `create-installer.bat`
2. Wait ~5-10 minutes (creates bundled JRE)
3. Find installer in `installer-output` folder
4. Share the .exe file directly

### User Experience:

1. User downloads .exe file
2. Double-clicks installer
3. Follows install wizard
4. Done! (Java included, no separate install needed)

### Advantages:
- ✅ Single .exe file
- ✅ No Java installation required
- ✅ Professional Windows installer
- ✅ Native uninstall support
- ✅ Most user-friendly

### Disadvantages:
- ⚠️ Large file size (50-70MB due to bundled Java)
- ⚠️ Longer creation time (~5-10 minutes)
- ⚠️ Requires JDK 21+ to create

---

## Option 3: MSI Installer (Advanced)

**Best for:** Enterprise environments, Group Policy deployment

### Requirements:

- WiX Toolset must be installed
- Download from: https://wixtoolset.org/

### How to Create:

1. Install WiX Toolset
2. Modify `create-installer.bat` to use `--type msi` instead of `--type exe`
3. Run the modified script

### What You Get:

- Professional .msi installer
- Group Policy deployment support
- Windows Installer technology
- All features of EXE installer

### Advantages:
- ✅ MSI format (enterprise standard)
- ✅ Group Policy support
- ✅ Advanced deployment options

### Disadvantages:
- ⚠️ Requires WiX Toolset installation
- ⚠️ More complex setup
- ⚠️ Large file size (50-70MB)

---

## Comparison: Which Should You Use?

### Use **Portable Installer** if:
- You want fast, easy distribution
- File size matters (uploading to web, email, etc.)
- Your users likely have Java installed
- You want the simplest creation process

### Use **jpackage EXE Installer** if:
- You want the most professional experience
- Your users may not have Java installed
- You don't mind the large file size
- You want a true Windows installer

### Use **MSI Installer** if:
- You're deploying to enterprise environments
- You need Group Policy support
- You're willing to install WiX Toolset

---

## Step-by-Step: Creating the Portable Installer

This is the **recommended option** for most users.

### Step 1: Compile Your Application

```batch
compile-java21.bat
```

Verify `HeronixuMonitor.jar` is created (61KB).

### Step 2: Create Installer Package

```batch
create-portable-installer.bat
```

### Step 3: Test the Installer

1. Navigate to `portable-installer` folder
2. Run `INSTALL.bat`
3. Follow the installation wizard
4. Verify shortcuts were created
5. Test running the application

### Step 4: Prepare for Distribution

```batch
# Zip the portable-installer folder
cd portable-installer
# Use Windows Explorer: Right-click > Send to > Compressed folder
# Or use PowerShell:
Compress-Archive -Path * -DestinationPath ..\Heronix-uMonitor-v1.1-Installer.zip
```

### Step 5: Distribute

Upload `Heronix-uMonitor-v1.1-Installer.zip` to:
- Your website
- GitHub Releases
- Email
- File sharing service
- USB drive

---

## Step-by-Step: Creating the jpackage EXE Installer

This creates a professional Windows installer with bundled Java.

### Step 1: Ensure JDK 21 is Installed

```batch
java -version
```

Should show: `java version "21.0.9"` or newer

### Step 2: Compile Your Application

```batch
compile-java21.bat
```

### Step 3: Create EXE Installer

```batch
create-installer.bat
```

**This will take 5-10 minutes** as it bundles the entire Java runtime.

### Step 4: Find Your Installer

Navigate to `installer-output` folder.

You'll find: `Heronix-uMonitor-1.1.exe` (~50-70MB)

### Step 5: Test the Installer

1. Double-click `Heronix-uMonitor-1.1.exe`
2. Follow installation wizard
3. Verify application installs correctly
4. Check Start Menu and Desktop shortcuts

### Step 6: Distribute

Upload `Heronix-uMonitor-1.1.exe` directly to:
- Your website
- GitHub Releases
- Microsoft Store (requires additional steps)
- File sharing service

---

## Troubleshooting

### Error: "Java not found"

**Solution:** Install JDK 21 from:
- https://www.oracle.com/java/technologies/downloads/
- Ensure `JAVA_HOME` points to JDK 21

### Error: "jpackage not found"

**Solution:** Update `create-installer.bat` to use correct JDK path:
```batch
set JAVA_HOME=C:\Program Files\Java\jdk-21
```

### Error: "WiX Toolset not found"

**Solution:** Either:
1. Install WiX Toolset from https://wixtoolset.org/
2. Or use `--type exe` instead of `--type msi`

### Portable Installer: "Failed to copy files"

**Solution:** Run `INSTALL.bat` as Administrator:
- Right-click `INSTALL.bat`
- Select "Run as Administrator"

---

## Advanced: Customizing the Installer

### Add Custom Icon

1. Create or obtain a `.ico` file (256x256 recommended)
2. Save as `icon.ico` in project folder
3. Update `create-installer.bat`:

```batch
--icon icon.ico ^
```

### Change Install Location

Edit `create-portable-installer.bat` and modify:

```batch
set "INSTALL_DIR=%ProgramFiles%\YourCustomFolder\Heronix-uMonitor"
```

### Add More Files to Installer

Edit `create-portable-installer.bat` and add:

```batch
copy YOUR_FILE.txt "portable-installer\Heronix-uMonitor\" >nul
```

---

## File Size Comparison

| Installer Type | File Size | Contains Java |
|----------------|-----------|---------------|
| JAR only | 61KB | No |
| Portable Installer (ZIP) | ~70KB | No |
| jpackage EXE | ~50-70MB | Yes ✅ |
| jpackage MSI | ~50-70MB | Yes ✅ |

The large size of jpackage installers is due to the **embedded Java runtime**, which ensures users don't need to install Java separately.

---

## Recommended Distribution Strategy

### For General Public:
**Use Portable Installer**
- Small download size
- Most users have Java or can easily install it
- Fast to create and distribute

### For Non-Technical Users:
**Use jpackage EXE Installer**
- No Java installation required
- Professional installer experience
- Users just double-click and install

### For Enterprise:
**Use MSI Installer**
- Group Policy deployment
- Centralized installation
- Corporate compliance

---

## Quick Reference Commands

```batch
# Compile application
compile-java21.bat

# Create portable installer (lightweight)
create-portable-installer.bat

# Create EXE installer (with bundled Java)
create-installer.bat

# Test the application
RUN_HERE.bat
```

---

## Summary

✅ **Best for most users:** Portable Installer (`create-portable-installer.bat`)
- Tiny file size, easy to distribute, professional experience

✅ **Best for non-technical users:** jpackage EXE (`create-installer.bat`)
- No Java needed, most professional, single .exe file

Both options create professional installers with:
- Desktop shortcuts
- Start Menu entries
- Uninstall capability
- Professional user experience

Choose based on your distribution needs and target audience!

---

**Heronix-uMonitor v1.1**
**By Michael Katsaros**
**December 2025**
