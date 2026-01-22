# Packaging Guide - Heronix-uMonitor

## 📦 Packaging Options

You have **5 different ways** to package and distribute Heronix-uMonitor:

1. **Fat JAR** (Self-contained, cross-platform) ✅ **EASIEST**
2. **Native Executable** (Windows .exe)
3. **Windows Installer** (MSI)
4. **Portable ZIP** (No installation needed)
5. **Platform-Specific Packages** (DEB, RPM, DMG)

---

## 1️⃣ Fat JAR (Recommended - Already Built!)

### **What You Have Now:**

✅ **Already created:** `target/HeronixuMonitor-1.1.4-optimized.jar` (3.2 MB)

This JAR includes **everything**:
- Your application code
- JNA libraries (5.14.0)
- All dependencies

### **Distribute:**

Just share the JAR file! Users run:
```bash
java -jar HeronixuMonitor-1.1.4-optimized.jar
```

### **Pros:**
✅ Cross-platform (Windows, Linux, macOS)
✅ Single file distribution
✅ No installation needed
✅ Works anywhere Java 21+ is installed

### **Cons:**
❌ Requires Java 21 to be installed
❌ Not a "native" executable

### **Best For:**
- Quick distribution
- Technical users
- Cross-platform deployment
- Internal tools

---

## 2️⃣ Native Windows Executable (.exe)

### **Using jpackage (Built into Java 21):**

```bash
# Create Windows .exe with bundled JRE
jpackage \
  --input target \
  --name HeronixuMonitor \
  --main-jar HeronixuMonitor-1.1.4-optimized.jar \
  --main-class HeronixuMonitor \
  --type exe \
  --icon icon.ico \
  --app-version 1.1.4 \
  --vendor "Heronix" \
  --win-console \
  --win-dir-chooser \
  --win-menu \
  --win-shortcut
```

**Output:** `HeronixuMonitor-1.1.4.exe` (~50-80 MB with bundled JRE)

### **Using Launch4j (Lightweight wrapper):**

Create `launch4j-config.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<launch4jConfig>
  <headerType>gui</headerType>
  <jar>HeronixuMonitor-1.1.4-optimized.jar</jar>
  <outfile>HeronixuMonitor.exe</outfile>
  <icon>icon.ico</icon>
  <jre>
    <minVersion>21.0.0</minVersion>
  </jre>
</launch4jConfig>
```

Then run:
```bash
launch4j launch4j-config.xml
```

**Output:** `HeronixuMonitor.exe` (~3.5 MB, requires Java 21)

### **Pros:**
✅ Native Windows executable
✅ Double-click to run
✅ Can bundle JRE (jpackage)
✅ Professional appearance

### **Cons:**
❌ Windows-only
❌ Larger size (if bundled JRE)

---

## 3️⃣ Windows Installer (MSI)

### **Using jpackage:**

```bash
# Create MSI installer
jpackage \
  --input target \
  --name HeronixuMonitor \
  --main-jar HeronixuMonitor-1.1.4-optimized.jar \
  --main-class HeronixuMonitor \
  --type msi \
  --icon icon.ico \
  --app-version 1.1.4 \
  --vendor "Heronix" \
  --win-dir-chooser \
  --win-menu \
  --win-menu-group "Heronix Tools" \
  --win-shortcut \
  --win-per-user-install
```

**Output:** `HeronixuMonitor-1.1.4.msi` (~50-80 MB)

### **Features:**
- Start menu integration
- Desktop shortcut
- Add/Remove Programs entry
- Automatic updates support
- Professional installation experience

### **Pros:**
✅ Professional installer
✅ Start menu integration
✅ Uninstaller included
✅ Bundles JRE

### **Cons:**
❌ Larger download
❌ Windows-only

---

## 4️⃣ Portable ZIP Package

### **Create Portable Distribution:**

```bash
# Create portable package directory
mkdir HeronixuMonitor-Portable
cd HeronixuMonitor-Portable

# Copy JAR
cp target/HeronixuMonitor-1.1.4-optimized.jar .

# Create run scripts
cat > run.bat << 'EOF'
@echo off
echo Starting Heronix-uMonitor...
java -XX:+UseZGC -Xmx256m -jar HeronixuMonitor-1.1.4-optimized.jar
pause
EOF

cat > run.sh << 'EOF'
#!/bin/bash
echo "Starting Heronix-uMonitor..."
java -XX:+UseZGC -Xmx256m -jar HeronixuMonitor-1.1.4-optimized.jar
EOF

chmod +x run.sh

# Create README
cat > README.txt << 'EOF'
Heronix-uMonitor v1.1.4-optimized
==================================

Requirements:
- Java 21 or newer

To Run:
- Windows: Double-click run.bat
- Linux/Mac: ./run.sh

Or manually: java -jar HeronixuMonitor-1.1.4-optimized.jar
EOF

# Package everything
cd ..
zip -r HeronixuMonitor-1.1.4-Portable.zip HeronixuMonitor-Portable
```

**Output:** `HeronixuMonitor-1.1.4-Portable.zip` (~3.3 MB)

### **Pros:**
✅ No installation
✅ Portable (USB stick friendly)
✅ Easy distribution
✅ Includes documentation

---

## 5️⃣ Platform-Specific Packages

### **Linux DEB Package:**

```bash
jpackage \
  --input target \
  --name heronix-umonitor \
  --main-jar HeronixuMonitor-1.1.4-optimized.jar \
  --main-class HeronixuMonitor \
  --type deb \
  --app-version 1.1.4 \
  --vendor "Heronix" \
  --linux-shortcut \
  --linux-menu-group "System;Monitor"
```

**Output:** `heronix-umonitor_1.1.4_amd64.deb`

### **Linux RPM Package:**

```bash
jpackage \
  --input target \
  --name heronix-umonitor \
  --main-jar HeronixuMonitor-1.1.4-optimized.jar \
  --main-class HeronixuMonitor \
  --type rpm \
  --app-version 1.1.4 \
  --vendor "Heronix" \
  --linux-shortcut \
  --linux-menu-group "System;Monitor"
```

**Output:** `heronix-umonitor-1.1.4.rpm`

### **macOS DMG:**

```bash
jpackage \
  --input target \
  --name HeronixuMonitor \
  --main-jar HeronixuMonitor-1.1.4-optimized.jar \
  --main-class HeronixuMonitor \
  --type dmg \
  --app-version 1.1.4 \
  --vendor "Heronix" \
  --icon icon.icns \
  --mac-package-name "HeronixuMonitor"
```

**Output:** `HeronixuMonitor-1.1.4.dmg`

---

## 🚀 Quick Package Scripts

### **All-in-One Windows Packaging Script:**

Create `package-windows.bat`:

```batch
@echo off
echo ========================================
echo Packaging Heronix-uMonitor for Windows
echo ========================================

REM Build with Maven
echo [1/4] Building with Maven...
call mvn clean package -DskipTests
if errorlevel 1 goto :error

REM Create output directory
echo [2/4] Creating output directory...
if not exist "dist" mkdir dist

REM Copy JAR
echo [3/4] Copying JAR...
copy target\HeronixuMonitor-1.1.4-optimized.jar dist\

REM Create Windows executable with jpackage
echo [4/4] Creating Windows installer...
jpackage ^
  --input dist ^
  --name HeronixuMonitor ^
  --main-jar HeronixuMonitor-1.1.4-optimized.jar ^
  --main-class HeronixuMonitor ^
  --type msi ^
  --app-version 1.1.4 ^
  --vendor "Heronix" ^
  --win-dir-chooser ^
  --win-menu ^
  --win-shortcut ^
  --win-per-user-install

echo.
echo ========================================
echo Packaging Complete!
echo ========================================
echo.
echo Output files:
dir /b dist
echo.
goto :end

:error
echo.
echo ERROR: Build failed!
pause
exit /b 1

:end
pause
```

### **Portable Package Script:**

Create `create-portable.bat`:

```batch
@echo off
echo Creating Portable Package...

REM Build
call mvn clean package -DskipTests

REM Create portable directory
mkdir dist\HeronixuMonitor-Portable
copy target\HeronixuMonitor-1.1.4-optimized.jar dist\HeronixuMonitor-Portable\

REM Create run script
echo @echo off > dist\HeronixuMonitor-Portable\run.bat
echo java -XX:+UseZGC -Xmx256m -jar HeronixuMonitor-1.1.4-optimized.jar >> dist\HeronixuMonitor-Portable\run.bat

REM Create README
echo Heronix-uMonitor v1.1.4 > dist\HeronixuMonitor-Portable\README.txt
echo Requires Java 21+ >> dist\HeronixuMonitor-Portable\README.txt
echo Run: run.bat >> dist\HeronixuMonitor-Portable\README.txt

REM Create ZIP
cd dist
powershell -Command "Compress-Archive -Path HeronixuMonitor-Portable -DestinationPath HeronixuMonitor-1.1.4-Portable.zip -Force"
cd ..

echo.
echo Portable package created: dist\HeronixuMonitor-1.1.4-Portable.zip
pause
```

---

## 📊 Comparison Table

| Method | Size | Requires Java | Platform | Ease of Use | Professional |
|--------|------|---------------|----------|-------------|--------------|
| **Fat JAR** | 3.2 MB | ✅ Yes | All | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **EXE (Launch4j)** | 3.5 MB | ✅ Yes | Windows | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **EXE (jpackage)** | 50-80 MB | ❌ No | Windows | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **MSI Installer** | 50-80 MB | ❌ No | Windows | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Portable ZIP** | 3.3 MB | ✅ Yes | All | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **DEB/RPM** | 50-80 MB | ❌ No | Linux | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **DMG** | 50-80 MB | ❌ No | macOS | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

---

## 🎯 Recommended Distribution Strategy

### **For General Users:**
1. **Windows:** MSI Installer (jpackage with bundled JRE)
2. **Linux:** DEB/RPM packages
3. **macOS:** DMG package

### **For Technical Users:**
1. **Cross-platform:** Fat JAR
2. **Alternative:** Portable ZIP

### **For Quick Testing:**
1. Fat JAR (what you have now!)

---

## 🛠️ Step-by-Step: Create MSI Installer

### **Prerequisites:**
1. WiX Toolset (for MSI creation)
   - Download: https://wixtoolset.org/
   - Or jpackage handles it automatically

2. Icon file (optional but recommended)
   - Create `icon.ico` (256x256 recommended)

### **Steps:**

```bash
# 1. Build the JAR
mvn clean package

# 2. Create installer
jpackage \
  --input target \
  --name HeronixuMonitor \
  --main-jar HeronixuMonitor-1.1.4-optimized.jar \
  --main-class HeronixuMonitor \
  --type msi \
  --app-version 1.1.4 \
  --vendor "Heronix" \
  --description "Professional system monitoring application" \
  --win-dir-chooser \
  --win-menu \
  --win-menu-group "Heronix Tools" \
  --win-shortcut \
  --win-per-user-install

# 3. Test the installer
# Output will be: HeronixuMonitor-1.1.4.msi
```

---

## 📝 Distribution Checklist

Before distributing:

- [ ] Update version number in `pom.xml`
- [ ] Test the packaged application
- [ ] Verify all 7 panels work
- [ ] Check performance (CPU, memory)
- [ ] Test on clean system (if possible)
- [ ] Include README/documentation
- [ ] Add license file
- [ ] Create release notes
- [ ] Test installation/uninstallation

---

## 🎁 Quick Start for Distribution

### **Option 1: Just Share the JAR** (Easiest!)

```bash
# You already have this!
cp target/HeronixuMonitor-1.1.4-optimized.jar ~/Downloads/
```

Share `HeronixuMonitor-1.1.4-optimized.jar` with users!

### **Option 2: Create Portable ZIP** (Better!)

```bash
# Run the script above or manually:
mkdir release
cp target/HeronixuMonitor-1.1.4-optimized.jar release/
cd release
# Add run scripts and README
# Zip it up
```

### **Option 3: Create Professional Installer** (Best!)

```bash
# Use jpackage (one command!)
jpackage --input target \
  --name HeronixuMonitor \
  --main-jar HeronixuMonitor-1.1.4-optimized.jar \
  --main-class HeronixuMonitor \
  --type msi \
  --app-version 1.1.4
```

---

## 🚀 Next Steps

1. **Choose your packaging method** (I recommend starting with Fat JAR or Portable ZIP)
2. **Run the packaging command**
3. **Test the package**
4. **Distribute!**

**Need help with a specific packaging method? Let me know!**
