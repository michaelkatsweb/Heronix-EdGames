# Heronix-uMonitor - Command Cheatsheet

## 🚀 Running the Application

### **Maven (Development):**
```bash
mvn compile exec:java          # Compile and run
mvn clean compile exec:java    # Clean rebuild and run
mvn -q compile exec:java       # Quiet mode
```

### **JAR (Distribution):**
```bash
java -jar target/HeronixuMonitor-1.1.4-optimized.jar
```

---

## 🔨 Building

### **Build JAR:**
```bash
mvn clean package              # Full build with dependencies
mvn package -DskipTests        # Fast build (skip tests)
mvn clean package -Pproduction # Production optimizations
```

### **Quick Compile:**
```bash
mvn compile                    # Compile only
mvn clean compile              # Clean compile
```

---

## 📦 Packaging

### **Portable ZIP:**
```bash
package-portable.bat           # Windows
./package-portable.sh          # Linux/Mac
```
**Output:** `dist/HeronixuMonitor-1.1.4-Portable.zip`

### **Manual PowerShell:**
```powershell
# One-liner to create portable package
mvn clean package; Compress-Archive -Path target/HeronixuMonitor-1.1.4-optimized.jar -DestinationPath HeronixuMonitor.zip
```

---

## 🧪 Testing & Verification

### **Check Build:**
```bash
mvn verify                     # Run all checks
mvn test                       # Run tests only
```

### **Check JAR Contents:**
```bash
jar tf target/HeronixuMonitor-1.1.4-optimized.jar | head -20
```

### **Check JAR Size:**
```bash
ls -lh target/*.jar
```

---

## 🔧 Maven Profiles

### **Development:**
```bash
mvn compile exec:java -Pdev
```

### **Production:**
```bash
mvn clean package -Pproduction
```

### **Platform-Specific:**
```bash
mvn package -Pwindows          # Auto-activated on Windows
mvn package -Plinux            # Auto-activated on Linux
mvn package -Pmac              # Auto-activated on macOS
```

---

## 🎯 Common Tasks

### **Make Changes & Test:**
```bash
# 1. Edit code
# 2. Run
mvn compile exec:java
```

### **Prepare for Distribution:**
```bash
# 1. Build
mvn clean package

# 2. Package
package-portable.bat

# 3. Test
unzip dist/HeronixuMonitor-1.1.4-Portable.zip
cd HeronixuMonitor-Portable
run.bat
```

### **Clean Everything:**
```bash
mvn clean                      # Remove target/
rm -rf dist/                   # Remove distribution files
```

---

## 📊 File Locations

| File | Location |
|------|----------|
| **Source Files** | `*.java` (root) |
| **Compiled Classes** | `target/classes/` |
| **Built JAR** | `target/HeronixuMonitor-1.1.4-optimized.jar` |
| **Portable ZIP** | `dist/HeronixuMonitor-1.1.4-Portable.zip` |
| **Documentation** | `*.md` files |

---

## ⚡ Performance Flags

### **Run with Optimizations:**
```bash
# With ZGC (low-latency GC)
java -XX:+UseZGC -jar target/HeronixuMonitor-1.1.4-optimized.jar

# With memory limits
java -Xms64m -Xmx256m -jar target/HeronixuMonitor-1.1.4-optimized.jar

# Combined (recommended)
java -XX:+UseZGC -XX:+UseStringDeduplication -Xmx256m -jar target/HeronixuMonitor-1.1.4-optimized.jar
```

---

## 🐛 Troubleshooting

### **Build Fails:**
```bash
mvn clean                      # Clean build artifacts
mvn -X package                 # Debug mode
mvn dependency:tree            # Check dependencies
```

### **Can't Find JAR:**
```bash
mvn package                    # Build it first
ls target/*.jar                # Verify it exists
```

### **Maven Not Found:**
```bash
mvn -version                   # Check Maven installed
where mvn                      # Find Maven location (Windows)
which mvn                      # Find Maven location (Linux/Mac)
```

---

## 📚 Documentation Quick Links

| Guide | Purpose |
|-------|---------|
| [README.md](README.md) | Main documentation |
| [QUICK_START.md](QUICK_START.md) | 30-second guide |
| [MAVEN_RUN_GUIDE.md](MAVEN_RUN_GUIDE.md) | Running with Maven |
| [MAVEN_BUILD.md](MAVEN_BUILD.md) | Maven build details |
| [PACKAGING_GUIDE.md](PACKAGING_GUIDE.md) | All packaging options |
| [HOW_TO_PACKAGE.md](HOW_TO_PACKAGE.md) | Simple packaging |
| [PERFORMANCE_OPTIMIZATIONS.md](PERFORMANCE_OPTIMIZATIONS.md) | Technical details |

---

## ✅ Daily Workflow

```bash
# Morning:
git pull                       # Get latest changes

# Development:
# Edit code...
mvn compile exec:java          # Test changes

# End of Day:
mvn clean package              # Build final JAR
git add .
git commit -m "Your changes"
git push

# Release:
mvn clean package              # Build
package-portable.bat           # Package
# Share: dist/HeronixuMonitor-1.1.4-Portable.zip
```

---

## 🎓 One-Liners

### **Quick Build & Run:**
```bash
mvn clean compile exec:java
```

### **Quick Package:**
```bash
mvn clean package && package-portable.bat
```

### **Check Everything:**
```bash
java -version && mvn -version && ls target/*.jar
```

---

**Keep this cheatsheet handy! 📋**
