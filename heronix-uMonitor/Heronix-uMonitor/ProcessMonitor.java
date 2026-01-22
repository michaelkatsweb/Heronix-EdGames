import java.io.*;
import java.util.*;

/**
 * Process monitoring class - collects information about running processes
 */
public class ProcessMonitor {

    public static class ProcessInfo {
        public final int pid;
        public final String name;
        public final double cpuUsage;
        public final long memoryUsage;

        public ProcessInfo(int pid, String name, double cpuUsage, long memoryUsage) {
            this.pid = pid;
            this.name = name;
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
        }
    }

    /**
     * Get list of running processes
     */
    public List<ProcessInfo> getProcessList() {
        List<ProcessInfo> processes = new ArrayList<>();

        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process p;

            if (os.contains("win")) {
                // Windows: use tasklist
                p = Runtime.getRuntime().exec("tasklist /FO CSV /NH");
            } else if (os.contains("mac")) {
                // macOS: use ps
                p = Runtime.getRuntime().exec("ps -e -o pid,comm,%cpu,rss");
            } else {
                // Linux: use ps
                p = Runtime.getRuntime().exec("ps -eo pid,comm,%cpu,rss");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null && processes.size() < 100) {
                try {
                    if (os.contains("win")) {
                        // Parse Windows tasklist CSV format
                        String[] parts = line.split("\",\"");
                        if (parts.length >= 2) {
                            String name = parts[0].replace("\"", "");
                            String pidStr = parts[1].replace("\"", "");
                            int pid = Integer.parseInt(pidStr);
                            processes.add(new ProcessInfo(pid, name, 0.0, 0));
                        }
                    } else {
                        // Parse Unix ps format
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length >= 4) {
                            int pid = Integer.parseInt(parts[0]);
                            String name = parts[1];
                            double cpu = Double.parseDouble(parts[2]);
                            long mem = Long.parseLong(parts[3]) * 1024; // Convert KB to bytes
                            processes.add(new ProcessInfo(pid, name, cpu, mem));
                        }
                    }
                } catch (Exception e) {
                    // Skip malformed lines
                }
            }
            reader.close();
            p.destroy();

        } catch (Exception e) {
            System.err.println("Error getting process list: " + e.getMessage());
        }

        return processes;
    }

    /**
     * Get total number of running processes
     */
    public int getProcessCount() {
        return getProcessList().size();
    }

    /**
     * Alias for getProcessCount() - for compatibility
     */
    public int getRunningProcesses() {
        return getProcessCount();
    }
}
