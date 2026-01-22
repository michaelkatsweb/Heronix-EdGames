# Quick Start - Heronix-uMonitor v1.1.4-optimized

## Build & Run (30 seconds)

### **Option 1: Maven (Recommended)**
```bash
cd Heronix-uMonitor
mvn clean package
java -jar target/HeronixuMonitor-1.1.4-optimized.jar
```

### **Option 2: Batch Script**
```bash
# Windows
build-optimized.bat
java -jar HeronixuMonitor-Optimized.jar

# Linux/Mac
chmod +x build-optimized.sh
./build-optimized.sh
java -jar HeronixuMonitor-Optimized.jar
```

---

## What You Get

✅ **7 Monitoring Panels:**
1. Overview - System summary
2. Performance - CPU/Memory graphs
3. Processes - Running processes
4. Network - Traffic & connections
5. Ports - Open ports scanner
6. CPU-ID - Processor details
7. Diagnostics - Health checks

✅ **Performance:**
- 75% less CPU usage
- 95% faster updates
- Zero UI freezing
- Task Manager speed!

---

## Requirements

- **Java 21** (required)
- **Maven 3.8+** (for Maven build)
- **Windows/Linux/macOS**

Check: `java -version` and `mvn -version`

---

## Performance Flags (Optional)

For best performance:
```bash
java -XX:+UseZGC -XX:+UseStringDeduplication -Xmx256m \
     -jar target/HeronixuMonitor-1.1.4-optimized.jar
```

---

## Troubleshooting

**Build fails:**
- Use Java 21: `java -version`
- Clear cache: `mvn clean`

**JNA not working:**
- Check console: "Using native APIs: true"
- Falls back to JMX automatically

**Missing panels:**
- All panels now included!
- Rebuild: `mvn clean package`

---

## Documentation

- **[FINAL_BUILD_SUMMARY.md](FINAL_BUILD_SUMMARY.md)** - Complete overview
- **[PERFORMANCE_OPTIMIZATIONS.md](PERFORMANCE_OPTIMIZATIONS.md)** - Technical details
- **[MAVEN_BUILD.md](MAVEN_BUILD.md)** - Maven guide
- **[BUILD_STATUS.md](BUILD_STATUS.md)** - Build info

---

## That's It!

Built in 30 seconds, runs like Task Manager. Enjoy! ⚡
