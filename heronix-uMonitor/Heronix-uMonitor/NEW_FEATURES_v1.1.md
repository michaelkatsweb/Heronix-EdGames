# 🎉 Heronix-uMonitor v1.1 - What's New!

## ✅ Just Added - Three Major Features!

### 1. **Personalized Title Bar** 👤
The application now displays:
```
Heronix-uMonitor v 1.1 - By Michael Katsaros
```

---

### 2. **Dark Grey Theme** 🌙

**Complete Dark Mode Overhaul!**

The entire application now features a professional dark grey theme:

**Color Scheme:**
- **Background:** Dark grey (#2D2D30)
- **Darker Panels:** Very dark grey (#1E1E1E)
- **Text:** Light grey (#DCDCDC)
- **Accents:** Blue (#007ACC)
- **Tables:** Dark with subtle grid lines

**Benefits:**
- ✅ Easier on the eyes during long monitoring sessions
- ✅ Professional, modern look
- ✅ Better focus on important data
- ✅ Reduced eye strain
- ✅ Perfect for dark rooms or night work

**Applied To:**
- All panels and backgrounds
- Tables and table headers
- Text fields and text areas
- Buttons and controls
- Progress bars
- Scroll panes
- Tab panels
- Borders and titles

---

### 3. **CPU-ID Panel** 💻

**NEW TAB: Detailed CPU Information (like CPUID software)**

A comprehensive CPU identification panel showing:

#### **Processor Summary:**
- Processor Name (exact model)
- Number of Cores
- Architecture (x86, x64, ARM, etc.)
- Endianness (Big/Little Endian)

#### **Current CPU Utilization:**
- System CPU Load (real-time %)
- Process CPU Load
- Load Average (1 minute)

#### **System Information:**
- Operating System name and version
- Java Runtime version
- Java Vendor

#### **Physical Memory:**
- Total Physical Memory
- Free Physical Memory
- Committed Virtual Memory

#### **Detailed CPU Features** (Platform-Specific):

**Windows:**
- Number of Cores
- Number of Logical Processors
- Max Clock Speed (MHz)
- Current Clock Speed (MHz)
- L2 Cache Size
- L3 Cache Size

**Linux:**
- Full output from `lscpu` command
- All CPU flags and features
- Cache hierarchy details
- NUMA information

**macOS:**
- CPU Brand String
- Core configuration
- System details

#### **Display Features:**
- ✅ Terminal-style green-on-black display
- ✅ Formatted boxes and borders
- ✅ Auto-refresh every 5 seconds
- ✅ Manual refresh button
- ✅ Scrollable for long output
- ✅ Professional report format

---

## 🎨 Visual Comparison

### Before (Light Theme):
```
┌────────────────────────────┐
│   White background         │
│   Black text               │
│   Bright colors            │
│   Standard theme           │
└────────────────────────────┘
```

### After (Dark Theme):
```
┌────────────────────────────┐
│   Dark grey background     │
│   Light grey text          │
│   Muted, professional      │
│   Easy on the eyes         │
└────────────────────────────┘
```

---

## 📊 CPU-ID Panel Example Output

```
════════════════════════════════════════════════════════════════════════════════
                    CPU IDENTIFICATION - PROCESSOR INFORMATION
════════════════════════════════════════════════════════════════════════════════

┌─ PROCESSOR SUMMARY ──────────────────────────────────────────────────────────
│
│  Processor Name:        Intel(R) Core(TM) i7-9750H CPU @ 2.60GHz
│  Number of Cores:       12
│  Architecture:          amd64
│  Endianness:            little
│
└───────────────────────────────────────────────────────────────────────────────

┌─ CURRENT CPU UTILIZATION ────────────────────────────────────────────────────
│
│  System CPU Load:       45.23%
│  Process CPU Load:      2.15%
│  Load Average (1 min):  3.45
│
└───────────────────────────────────────────────────────────────────────────────

┌─ PHYSICAL MEMORY ─────────────────────────────────────────────────────────────
│
│  Total Physical Memory: 16.00 GB
│  Free Physical Memory:  8.45 GB
│  Committed Virtual Mem: 12.32 GB
│
└───────────────────────────────────────────────────────────────────────────────

┌─ DETAILED CPU FEATURES (Windows) ────────────────────────────────────────────
│
│  NumberOfCores:          6
│  NumberOfLogicalProcessors: 12
│  MaxClockSpeed:          4500 MHz
│  CurrentClockSpeed:      2600 MHz
│  L2CacheSize:            1.50 MB
│  L3CacheSize:            12.00 MB
│
└───────────────────────────────────────────────────────────────────────────────
```

---

## 🚀 How to Access New Features

### Run the Application:
```bash
run-debug.bat
```

### Explore the Features:

1. **See Your Name in Title**
   - Look at the window title bar
   - Shows: "Heronix-uMonitor v 1.1 - By Michael Katsaros"

2. **Enjoy Dark Theme**
   - Immediately visible when app opens
   - All panels use dark theme
   - Professional, modern appearance

3. **View CPU-ID Information**
   - Click the **"CPU-ID"** tab
   - See detailed processor information
   - Click "Refresh CPU Info" to update
   - Auto-refreshes every 5 seconds

---

## 🎯 Tab Layout (Updated)

Your app now has **7 tabs**:

1. **Overview** - System summary dashboard
2. **Performance** - CPU/Memory graphs
3. **Processes** - Running processes list
4. **Network** - Network traffic & connections
5. **Ports** - Port monitoring with security alerts
6. **CPU-ID** - Detailed CPU information ⭐ NEW!
7. **Diagnostics** - System health diagnostics

---

## 💡 Pro Tips

### CPU-ID Panel:
- Scroll down to see all information
- Use for hardware verification
- Check actual CPU specs vs advertised
- Monitor clock speed throttling
- Verify cache sizes
- Identify CPU model for driver updates

### Dark Theme:
- Better for extended use
- Screenshots look professional
- Matches modern IDE themes
- Less distracting in dark environments

---

## 🔧 Technical Details

### Dark Theme Implementation:
- Uses UIManager for global theme
- Custom colors: #2D2D30, #1E1E1E, #DCDCDC
- Applied to all Swing components
- Consistent across all panels

### CPU-ID Implementation:
- Platform-aware (Windows/Linux/Mac)
- Uses system commands (wmic, lscpu, sysctl)
- Parses `/proc/cpuinfo` on Linux
- Background threading for responsiveness
- Auto-refresh with 5-second intervals

---

## 📈 Version History

**v1.1 (Current)**
- ✅ Added personalized title with your name
- ✅ Implemented dark grey theme
- ✅ Added CPU-ID identification panel
- ✅ Performance optimizations (20x faster ports)
- ✅ Color-coded security alerts
- ✅ Process name identification

**v1.0**
- Initial release
- 6 monitoring panels
- Basic functionality

---

## 🎉 What Makes This Special

Your monitoring tool now has:

1. **Personal Touch** - Your name in the title
2. **Professional Look** - Dark theme like commercial software
3. **Hardware Details** - CPU-ID information like dedicated tools
4. **Security Features** - Color-coded port monitoring
5. **Performance** - Fast, responsive, optimized
6. **Comprehensive** - More features than many paid tools

---

## 🚀 Coming Soon (Possible Future Features)

Based on your feedback, we could add:
- [ ] GPU-ID panel (like CPU-ID but for graphics)
- [ ] Motherboard information
- [ ] Temperature monitoring
- [ ] Fan speed monitoring
- [ ] Export CPU report to file
- [ ] Theme toggle (switch between dark/light)
- [ ] Custom color schemes
- [ ] RAM detailed information (SPD, timings)

---

## 🏆 Summary

**What Changed:**
- Title bar now shows your name
- Complete dark grey theme
- New CPU-ID tab with detailed processor info

**Benefits:**
- Professional appearance
- Better usability for long sessions
- Hardware identification capabilities
- More comprehensive monitoring

**How to Use:**
```bash
run-debug.bat
```
Then explore the new CPU-ID tab and enjoy the dark theme!

---

**Heronix-uMonitor v1.1 - Professional System Monitoring by Michael Katsaros** 🚀
