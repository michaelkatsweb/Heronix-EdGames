# Maven Build Guide - Heronix-uMonitor

## Prerequisites

- **Java 21** (JDK 21 or later)
- **Maven 3.8+**

Verify installation:
```bash
java -version   # Should show version 21
mvn -version    # Should show Maven 3.8+
```

---

## Quick Start

### **Build & Run (One Command)**
```bash
cd Heronix-uMonitor
mvn clean package
java -jar target/HeronixuMonitor-1.1.4-optimized.jar
```

---

## Maven Commands

### **1. Clean Build**
Removes all compiled files and builds from scratch:
```bash
mvn clean compile
```

### **2. Package JAR with Dependencies**
Creates a fat JAR with all dependencies included:
```bash
mvn clean package
```

Output: `target/HeronixuMonitor-1.1.4-optimized.jar` (self-contained)

### **3. Build Without Tests**
```bash
mvn clean package -DskipTests
```

### **4. Development Build (Fast)**
```bash
mvn clean compile -Pdev
```

### **5. Production Build (Optimized)**
```bash
mvn clean package -Pproduction
```

### **6. Install to Local Maven Repository**
```bash
mvn clean install
```

---

## Running the Application

### **After Build:**
```bash
# Run the fat JAR (includes all dependencies)
java -jar target/HeronixuMonitor-1.1.4-optimized.jar

# Or with custom JVM options for better performance
java -XX:+UseZGC -XX:+UseStringDeduplication \
     -jar target/HeronixuMonitor-1.1.4-optimized.jar
```

### **From Maven:**
```bash
# Run directly via Maven (development mode)
mvn exec:java -Dexec.mainClass="HeronixuMonitor"
```

---

## Project Structure

```
Heronix-uMonitor/
├── pom.xml                          # Maven configuration
├── src/
│   └── main/
│       └── java/                    # Source files location (future)
├── *.java                           # Current source files (root)
├── target/                          # Build output (created by Maven)
│   ├── classes/                     # Compiled .class files
│   ├── HeronixuMonitor-1.1.4-optimized.jar    # Fat JAR
│   └── maven-archiver/              # Build metadata
└── lib/                             # External libraries (if any)
```

**Note:** For proper Maven structure, source files should be in `src/main/java/`. Current structure has them in root for compatibility with existing build scripts.

---

## Maven Profiles

### **Available Profiles:**

1. **`dev`** - Development profile (fast builds, skip tests)
   ```bash
   mvn clean package -Pdev
   ```

2. **`production`** - Production build with optimizations
   ```bash
   mvn clean package -Pproduction
   ```

3. **`windows`** - Auto-activated on Windows
4. **`linux`** - Auto-activated on Linux
5. **`mac`** - Auto-activated on macOS

---

## Dependencies

Maven automatically downloads these dependencies:

| Dependency | Version | Purpose |
|------------|---------|---------|
| **jna** | 5.14.0 | Native API access (Windows/Linux/Mac) |
| **jna-platform** | 5.14.0 | Platform-specific extensions |

### **Manual Dependency Check:**
```bash
mvn dependency:tree
```

### **Update Dependencies:**
```bash
mvn versions:display-dependency-updates
```

---

## Build Outputs

After `mvn clean package`, you'll get:

1. **`target/HeronixuMonitor-1.1.4-optimized.jar`**
   - Fat JAR with all dependencies
   - Self-contained, portable
   - ~2-3 MB size

2. **`target/original-HeronixuMonitor-1.1.4-optimized.jar`**
   - Original JAR without dependencies
   - Requires classpath setup

3. **`target/classes/`**
   - Compiled .class files
   - Useful for development

---

## Common Tasks

### **Clean Everything:**
```bash
mvn clean
```

### **Compile Only (No Package):**
```bash
mvn compile
```

### **View Effective POM:**
```bash
mvn help:effective-pom
```

### **Show Project Info:**
```bash
mvn help:describe -Dplugin=compiler
```

### **Generate Project Report:**
```bash
mvn site
```

---

## Performance Optimization Flags

### **Recommended JVM Flags for Running:**

```bash
java -XX:+UseZGC \
     -XX:+UseStringDeduplication \
     -XX:+UnlockExperimentalVMOptions \
     -XX:+EnableJVMCI \
     -Xms64m -Xmx256m \
     -jar target/HeronixuMonitor-1.1.4-optimized.jar
```

**Flags Explained:**
- `-XX:+UseZGC` - Low-latency garbage collector (Java 21)
- `-XX:+UseStringDeduplication` - Reduce memory for duplicate strings
- `-Xms64m` - Initial heap size (adjust based on system)
- `-Xmx256m` - Max heap size (monitor uses ~100MB)

---

## Troubleshooting

### **Error: "requires a project to execute but there is no POM"**
**Solution:** Make sure you're in the `Heronix-uMonitor` directory:
```bash
cd Heronix-uMonitor
mvn clean package
```

### **Error: "Source option 21 is no longer supported"**
**Solution:** Update to Java 21:
```bash
java -version
# Should show version 21.x.x
```

### **Error: "Failed to execute goal...maven-compiler-plugin"**
**Solutions:**
1. Check Java version: `java -version`
2. Set JAVA_HOME correctly:
   ```bash
   # Windows
   set JAVA_HOME=C:\Program Files\Java\jdk-21

   # Linux/Mac
   export JAVA_HOME=/usr/lib/jvm/java-21
   ```

### **Dependencies Not Downloading:**
**Solutions:**
1. Check internet connection
2. Clear Maven cache:
   ```bash
   mvn dependency:purge-local-repository
   ```
3. Force update:
   ```bash
   mvn clean package -U
   ```

### **Out of Memory During Build:**
**Solution:** Increase Maven memory:
```bash
# Windows
set MAVEN_OPTS=-Xmx1024m

# Linux/Mac
export MAVEN_OPTS="-Xmx1024m"
```

---

## CI/CD Integration

### **GitHub Actions Example:**

```yaml
name: Build

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Build with Maven
        run: |
          cd Heronix-uMonitor
          mvn clean package -Pproduction
      - name: Upload artifact
        uses: actions/upload-artifact@v3
        with:
          name: heronix-umonitor
          path: Heronix-uMonitor/target/*.jar
```

---

## Comparison: Maven vs Batch Scripts

| Feature | Maven | Batch Scripts |
|---------|-------|---------------|
| Dependency Management | ✅ Automatic | ❌ Manual download |
| Cross-platform | ✅ Yes | ⚠️ Separate scripts |
| IDE Integration | ✅ Native | ❌ Manual setup |
| Build Reproducibility | ✅ High | ⚠️ Environment dependent |
| Learning Curve | ⚠️ Steeper | ✅ Simple |
| Build Speed (first) | ⚠️ Slower (downloads) | ✅ Faster |
| Build Speed (incremental) | ✅ Fast | ✅ Fast |

**Recommendation:**
- **Use Maven** for production builds and distribution
- **Use batch scripts** for quick testing and development

---

## IDE Integration

### **IntelliJ IDEA:**
1. Open folder containing `pom.xml`
2. IntelliJ auto-detects Maven project
3. Right-click `pom.xml` → Maven → Reload Project

### **Eclipse:**
1. File → Import → Maven → Existing Maven Projects
2. Select folder with `pom.xml`
3. Right-click project → Maven → Update Project

### **VS Code:**
1. Install "Java Extension Pack"
2. Open folder with `pom.xml`
3. VS Code auto-configures Maven

---

## Next Steps

After building:

1. ✅ **Test the application:**
   ```bash
   java -jar target/HeronixuMonitor-1.1.4-optimized.jar
   ```

2. ✅ **Verify performance improvements:**
   - Check CPU usage (should be <2%)
   - Monitor memory (should be ~100MB)
   - Test UI responsiveness

3. ✅ **Read optimization docs:**
   - [PERFORMANCE_OPTIMIZATIONS.md](PERFORMANCE_OPTIMIZATIONS.md)
   - [OPTIMIZATION_SUMMARY.md](OPTIMIZATION_SUMMARY.md)

4. ✅ **Create distribution:**
   ```bash
   # Fat JAR is ready to distribute!
   cp target/HeronixuMonitor-1.1.4-optimized.jar ../releases/
   ```

---

## Support

For issues with Maven builds:
1. Check this guide first
2. Verify Java 21 installation
3. Clear Maven cache if needed
4. Check Maven logs for detailed errors

**Happy Building! 🚀**
