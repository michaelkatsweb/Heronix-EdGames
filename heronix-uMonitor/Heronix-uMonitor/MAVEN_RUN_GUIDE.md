# Maven Run Guide - Heronix-uMonitor

## 🚀 Run Directly with Maven

### **Quick Start:**

```bash
# Compile and run in one command
mvn compile exec:java
```

That's it! The application will launch.

---

## 📋 All Maven Run Commands

### **1. Run Without Building JAR** (Fastest for Development)

```bash
# Compile and run
mvn compile exec:java
```

### **2. Clean, Build, and Run**

```bash
# Full rebuild then run
mvn clean compile exec:java
```

### **3. Build JAR and Run from JAR**

```bash
# Build the JAR first
mvn clean package

# Then run the JAR
java -jar target/HeronixuMonitor-1.1.4-optimized.jar
```

### **4. Run with Performance Flags**

```bash
# Run with optimizations
mvn compile exec:java -Dexec.args="-XX:+UseZGC -Xmx256m"
```

---

## 🎯 Common Maven Commands

### **Development Workflow:**

```bash
# 1. Make code changes
# 2. Compile and test
mvn compile exec:java

# Or with auto-reload on changes (using spring-boot-devtools if added)
mvn compile exec:java
```

### **Testing:**

```bash
# Run tests (when you add them)
mvn test

# Run specific test
mvn test -Dtest=YourTestClass
```

### **Building:**

```bash
# Build JAR only
mvn package

# Build without tests
mvn package -DskipTests

# Clean and build
mvn clean package
```

---

## ⚡ Quick Reference

| Command | What It Does | When to Use |
|---------|--------------|-------------|
| `mvn compile exec:java` | Compile & run | Development, quick testing |
| `mvn clean package` | Build JAR | Creating distributable |
| `mvn clean compile exec:java` | Clean rebuild & run | After major changes |
| `java -jar target/*.jar` | Run built JAR | After packaging |

---

## 🔧 Advanced Options

### **Run with Custom JVM Arguments:**

```bash
# Set memory limits
mvn exec:java -Dexec.args="-Xms64m -Xmx256m"

# Enable GC logging
mvn exec:java -Dexec.args="-Xlog:gc"

# Use ZGC (low-latency garbage collector)
mvn exec:java -Dexec.args="-XX:+UseZGC"

# Combine multiple arguments
mvn exec:java -Dexec.args="-XX:+UseZGC -XX:+UseStringDeduplication -Xmx256m"
```

### **Run Specific Main Class:**

```bash
# If you have multiple main classes
mvn exec:java -Dexec.mainClass="com.example.OtherMain"
```

### **Debug Mode:**

```bash
# Run with remote debugging enabled
mvn exec:java -Dexec.args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

# Then attach debugger to localhost:5005
```

---

## 📊 Comparison: Maven vs Direct Java

### **Maven Approach:**
```bash
mvn compile exec:java
```
**Pros:**
- ✅ Handles dependencies automatically
- ✅ Compiles if needed
- ✅ IDE integration
- ✅ Consistent across environments

**Cons:**
- ⏱️ Slightly slower startup (Maven overhead)

### **Direct Java Approach:**
```bash
mvn package
java -jar target/HeronixuMonitor-1.1.4-optimized.jar
```
**Pros:**
- ✅ Faster startup
- ✅ No Maven needed for users
- ✅ Simple distribution

**Cons:**
- ❌ Requires manual rebuild after changes

---

## 🎨 IDE Integration

### **IntelliJ IDEA:**

1. Right-click `pom.xml` → Add as Maven Project
2. Right-click `HeronixuMonitor.java` → Run
3. Or use Maven toolbar: `exec:java`

### **Eclipse:**

1. File → Import → Maven → Existing Maven Projects
2. Right-click project → Run As → Maven build
3. Goals: `compile exec:java`

### **VS Code:**

1. Install "Java Extension Pack"
2. Open Command Palette (Ctrl+Shift+P)
3. Type: "Maven: Execute commands"
4. Select: `compile exec:java`

---

## 🔄 Development Workflow

### **Recommended Flow:**

```bash
# 1. Make changes to code
# Edit your .java files

# 2. Test changes quickly
mvn compile exec:java

# 3. If it works, build final JAR
mvn clean package

# 4. Test the JAR
java -jar target/HeronixuMonitor-1.1.4-optimized.jar

# 5. Package for distribution
package-portable.bat
```

---

## 💡 Pro Tips

### **Faster Development:**

Create a script `dev-run.bat`:
```batch
@echo off
echo Quick run...
mvn compile exec:java -q
```

Or `dev-run.sh`:
```bash
#!/bin/bash
echo "Quick run..."
mvn compile exec:java -q
```

Then just run: `dev-run.bat`

### **Skip Recompilation:**

If you only want to run without recompiling:
```bash
mvn exec:java
```

(But `mvn compile exec:java` is safer)

### **Quiet Mode:**

Less Maven output:
```bash
mvn -q compile exec:java
```

### **Offline Mode:**

If dependencies are already downloaded:
```bash
mvn -o compile exec:java
```

---

## 🎯 Quick Command Reference

### **For Development:**
```bash
mvn compile exec:java              # Run application
mvn clean compile exec:java        # Clean rebuild and run
mvn -q compile exec:java           # Quiet mode
```

### **For Building:**
```bash
mvn package                        # Build JAR
mvn clean package                  # Clean build
mvn package -DskipTests            # Fast build (no tests)
```

### **For Distribution:**
```bash
mvn clean package                  # Build
package-portable.bat               # Package
# Share: dist/HeronixuMonitor-1.1.4-Portable.zip
```

---

## 📝 Maven Lifecycle Phases

Understanding what happens:

1. **`mvn compile`** - Compiles source code
2. **`mvn test`** - Runs unit tests
3. **`mvn package`** - Creates JAR file
4. **`mvn install`** - Installs to local Maven repository
5. **`mvn exec:java`** - Runs the main class

You can combine them:
```bash
mvn clean compile test package exec:java
```

---

## 🚀 Your New Workflow

### **Daily Development:**
```bash
# Edit code, then:
mvn compile exec:java
```

### **Ready to Share:**
```bash
# Build and package:
mvn clean package
package-portable.bat

# Share:
dist/HeronixuMonitor-1.1.4-Portable.zip
```

---

## ✅ Summary

**To run with Maven:**
```bash
mvn compile exec:java
```

**To build for distribution:**
```bash
mvn clean package
```

**To run built JAR:**
```bash
java -jar target/HeronixuMonitor-1.1.4-optimized.jar
```

**That's all you need to know!** 🎉
