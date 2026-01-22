import java.lang.management.ManagementFactory;
import java.util.concurrent.*;
import com.sun.management.OperatingSystemMXBean;

/**
 * High-performance system monitor using background threads and native APIs.
 * Provides non-blocking, efficient system metrics collection.
 */
public class FastSystemMonitor {

    private final OperatingSystemMXBean osBean;
    private final ScheduledExecutorService executor;
    private final boolean useNativeAPIs;

    // Cached values (updated by background thread)
    private volatile double cachedCpuUsage = 0.0;
    private volatile long[] cachedMemoryInfo = new long[4];
    private volatile boolean initialized = false;

    // Performance metrics
    private volatile long lastUpdateTime = 0;
    private volatile long updateCount = 0;

    public FastSystemMonitor() {
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // Check if native APIs are available
        this.useNativeAPIs = NativeSystemMonitor.isAvailable() && NativeSystemMonitor.testNativeAccess();

        // Create background thread pool (daemon threads, lower priority)
        this.executor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "FastSystemMonitor-Worker");
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY - 1); // Lower priority than UI
                return t;
            }
        });

        // Initialize cached values
        updateCache();

        System.out.println("FastSystemMonitor initialized. Using native APIs: " + useNativeAPIs);
    }

    /**
     * Start background monitoring with specified update interval
     */
    public void startMonitoring(long intervalMs) {
        if (initialized) {
            return;
        }

        executor.scheduleAtFixedRate(() -> {
            try {
                long startTime = System.nanoTime();
                updateCache();
                long elapsedNs = System.nanoTime() - startTime;
                lastUpdateTime = elapsedNs / 1_000_000; // Convert to ms
                updateCount++;

                // Warn if update takes too long
                if (lastUpdateTime > 50) {
                    System.err.println("WARNING: FastSystemMonitor update took " + lastUpdateTime + "ms");
                }
            } catch (Exception e) {
                System.err.println("Error in background monitoring: " + e.getMessage());
            }
        }, 0, intervalMs, TimeUnit.MILLISECONDS);

        initialized = true;
    }

    /**
     * Stop background monitoring
     */
    public void stopMonitoring() {
        if (!initialized) {
            return;
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        initialized = false;
    }

    /**
     * Update cached values in background thread
     */
    private void updateCache() {
        // Update CPU usage
        if (useNativeAPIs) {
            double cpu = NativeSystemMonitor.getCpuUsage();
            if (cpu >= 0) {
                cachedCpuUsage = cpu;
            } else {
                // Fallback to JMX
                cachedCpuUsage = osBean.getCpuLoad() * 100.0;
            }
        } else {
            cachedCpuUsage = osBean.getCpuLoad() * 100.0;
        }

        // Update memory info
        if (useNativeAPIs) {
            long[] mem = NativeSystemMonitor.getMemoryInfo();
            if (mem != null) {
                cachedMemoryInfo = mem;
            } else {
                // Fallback to JMX
                updateMemoryFallback();
            }
        } else {
            updateMemoryFallback();
        }
    }

    /**
     * Fallback memory info using JMX
     */
    private void updateMemoryFallback() {
        long total = osBean.getTotalMemorySize();
        long free = osBean.getFreeMemorySize();
        long used = total - free;
        long usagePercent = (used * 100) / total;
        cachedMemoryInfo = new long[] { total, used, free, usagePercent };
    }

    /**
     * Get current CPU usage (non-blocking, returns cached value)
     */
    public double getCpuUsage() {
        return cachedCpuUsage;
    }

    /**
     * Get system CPU load (0.0 to 1.0)
     */
    public double getSystemCpuLoad() {
        return cachedCpuUsage / 100.0;
    }

    /**
     * Get memory statistics (non-blocking, returns cached values)
     * Returns: [total, used, free, usage%]
     */
    public MemoryStats getMemoryStats() {
        return new MemoryStats(
            cachedMemoryInfo[0],
            cachedMemoryInfo[1],
            cachedMemoryInfo[2],
            cachedMemoryInfo[3]
        );
    }

    /**
     * Get number of processor cores
     */
    public int getProcessorCount() {
        return osBean.getAvailableProcessors();
    }

    /**
     * Get system load average
     */
    public double getSystemLoadAverage() {
        return osBean.getSystemLoadAverage();
    }

    /**
     * Get performance metrics
     */
    public PerformanceMetrics getPerformanceMetrics() {
        return new PerformanceMetrics(
            lastUpdateTime,
            updateCount,
            useNativeAPIs
        );
    }

    /**
     * Memory statistics container
     */
    public static class MemoryStats {
        public final long total;
        public final long used;
        public final long free;
        public final double usagePercent;

        public MemoryStats(long total, long used, long free, long usagePercent) {
            this.total = total;
            this.used = used;
            this.free = free;
            this.usagePercent = usagePercent;
        }
    }

    /**
     * Performance metrics container
     */
    public static class PerformanceMetrics {
        public final long lastUpdateTimeMs;
        public final long updateCount;
        public final boolean usingNativeAPIs;

        public PerformanceMetrics(long lastUpdateTimeMs, long updateCount, boolean usingNativeAPIs) {
            this.lastUpdateTimeMs = lastUpdateTimeMs;
            this.updateCount = updateCount;
            this.usingNativeAPIs = usingNativeAPIs;
        }

        @Override
        public String toString() {
            return String.format("Updates: %d, Last: %dms, Native: %s",
                updateCount, lastUpdateTimeMs, usingNativeAPIs);
        }
    }
}
