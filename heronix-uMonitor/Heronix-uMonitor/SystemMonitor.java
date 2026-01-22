import java.io.File;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

/**
 * Optimized system monitor with caching and efficient data structures.
 * Compatible with existing codebase while providing better performance.
 */
public class SystemMonitor {

    private final OperatingSystemMXBean osBean;
    private final FastSystemMonitor fastMonitor;

    // Cached system info (rarely changes)
    private SystemInfo cachedSystemInfo;

    public SystemMonitor() {
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.fastMonitor = new FastSystemMonitor();

        // Start background monitoring at 500ms interval
        this.fastMonitor.startMonitoring(500);
    }

    /**
     * Get system information (cached, only computed once)
     */
    public SystemInfo getSystemInfo() {
        if (cachedSystemInfo == null) {
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            String architecture = System.getProperty("os.arch");
            int processors = osBean.getAvailableProcessors();
            long totalMemory = osBean.getTotalMemorySize();

            cachedSystemInfo = new SystemInfo(osName, osVersion, architecture, processors, totalMemory);
        }
        return cachedSystemInfo;
    }

    /**
     * Get CPU usage percentage (uses fast cached value from background thread)
     */
    public double getSystemCpuUsage() {
        return fastMonitor.getCpuUsage();
    }

    /**
     * Get memory statistics (uses fast cached values)
     */
    public MemoryStats getMemoryStats() {
        FastSystemMonitor.MemoryStats stats = fastMonitor.getMemoryStats();
        return new MemoryStats(stats.total, stats.used, stats.free, stats.usagePercent);
    }

    /**
     * Get disk statistics for all available drives
     */
    public DiskStats[] getDiskStats() {
        File[] roots = File.listRoots();
        DiskStats[] stats = new DiskStats[roots.length];

        for (int i = 0; i < roots.length; i++) {
            File root = roots[i];
            long total = root.getTotalSpace();
            long free = root.getFreeSpace();
            long used = total - free;
            double usagePercent = total > 0 ? (used * 100.0 / total) : 0.0;

            stats[i] = new DiskStats(root.getPath(), total, used, free, usagePercent);
        }

        return stats;
    }

    /**
     * System information container
     */
    public static class SystemInfo {
        public final String osName;
        public final String osVersion;
        public final String architecture;
        public final int processors;
        public final long totalMemory;

        public SystemInfo(String osName, String osVersion, String architecture,
                         int processors, long totalMemory) {
            this.osName = osName;
            this.osVersion = osVersion;
            this.architecture = architecture;
            this.processors = processors;
            this.totalMemory = totalMemory;
        }
    }

    /**
     * Memory statistics container
     */
    public static class MemoryStats {
        public final long total;
        public final long used;
        public final long free;
        public final double usagePercent;

        public MemoryStats(long total, long used, long free, double usagePercent) {
            this.total = total;
            this.used = used;
            this.free = free;
            this.usagePercent = usagePercent;
        }
    }

    /**
     * Disk statistics container
     */
    public static class DiskStats {
        public final String path;
        public final long total;
        public final long used;
        public final long free;
        public final double usagePercent;

        public DiskStats(String path, long total, long used, long free, double usagePercent) {
            this.path = path;
            this.total = total;
            this.used = used;
            this.free = free;
            this.usagePercent = usagePercent;
        }
    }
}
