# Important: Project Directory Structure

## 📂 Directory Layout

Your project has nested directories:

```
H:\Heronix\heronix-uMonitor\           ← Parent directory
  └── Heronix-uMonitor\                ← PROJECT ROOT (you need to be here!)
      ├── pom.xml                      ← Maven configuration
      ├── *.java                       ← Source files
      ├── target\                      ← Built files
      ├── dist\                        ← Distribution packages
      └── *.md                         ← Documentation
```

## ⚠️ Common Mistake

**Wrong directory:**
```bash
H:\Heronix\heronix-uMonitor> mvn compile exec:java
ERROR: no POM in this directory
```

**Correct directory:**
```bash
H:\Heronix\heronix-uMonitor\Heronix-uMonitor> mvn compile exec:java
✓ Works!
```

## ✅ How to Navigate

### **From parent directory:**
```bash
cd Heronix-uMonitor
mvn compile exec:java
```

### **From anywhere:**
```bash
cd h:\Heronix\heronix-uMonitor\Heronix-uMonitor
mvn compile exec:java
```

### **Check you're in the right place:**
```bash
# Look for pom.xml
dir pom.xml     # Windows
ls pom.xml      # Linux/Mac

# Or check current directory
cd              # Windows
pwd             # Linux/Mac
```

Should show: `H:\Heronix\heronix-uMonitor\Heronix-uMonitor`

## 🎯 Quick Reference

| Task | Command | Directory |
|------|---------|-----------|
| **Run with Maven** | `mvn compile exec:java` | `Heronix-uMonitor\` |
| **Build JAR** | `mvn package` | `Heronix-uMonitor\` |
| **Package** | `package-portable.bat` | `Heronix-uMonitor\` |
| **Run JAR** | `java -jar target\*.jar` | `Heronix-uMonitor\` |

## 💡 Pro Tip

Create a batch file in the parent directory:

**`H:\Heronix\heronix-uMonitor\run.bat`:**
```batch
@echo off
cd Heronix-uMonitor
mvn compile exec:java
```

Then you can run from parent:
```bash
H:\Heronix\heronix-uMonitor> run.bat
```

---

**Always work from `Heronix-uMonitor\` (the one with `pom.xml`)!**
