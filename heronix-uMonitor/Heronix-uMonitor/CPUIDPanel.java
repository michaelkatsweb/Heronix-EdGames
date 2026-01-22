import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class CPUIDPanel extends JPanel implements MonitorPanel {
    private JTextArea cpuInfoArea;
    private Timer updateTimer;

    // Cached strings for performance
    private static final String SEPARATOR_80 = "═".repeat(80);
    private static final String DASH_79 = "─".repeat(79);

    public CPUIDPanel() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("CPU Identification - Detailed Processor Information");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);

        // CPU info display area
        cpuInfoArea = new JTextArea();
        cpuInfoArea.setEditable(false);
        cpuInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        cpuInfoArea.setBackground(new Color(30, 30, 30));
        cpuInfoArea.setForeground(new Color(0, 255, 0));
        JScrollPane scrollPane = new JScrollPane(cpuInfoArea);

        // Refresh button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh CPU Info");
        refreshButton.addActionListener(e -> loadCPUInfo());
        bottomPanel.add(refreshButton);

        add(titlePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    @Override
    public void startMonitoring() {
        // CPU features don't change - only load once
        if (cpuInfoArea.getText().isEmpty()) {
            loadCPUInfo();
        }
        // No timer needed - CPU ID information is static
        // Users can manually refresh using the button if needed
    }

    @Override
    public void stopMonitoring() {
        if (updateTimer != null && updateTimer.isRunning()) {
            updateTimer.stop();
        }
    }

    private void loadCPUInfo() {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return getCPUInformation();
            }

            @Override
            protected void done() {
                try {
                    cpuInfoArea.setText(get());
                    cpuInfoArea.setCaretPosition(0);
                } catch (Exception e) {
                    cpuInfoArea.setText("Error loading CPU information: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private String getCPUInformation() {
        StringBuilder sb = new StringBuilder();
        String os = System.getProperty("os.name").toLowerCase();

        sb.append(SEPARATOR_80).append("\n");
        sb.append("                    CPU IDENTIFICATION - PROCESSOR INFORMATION\n");
        sb.append(SEPARATOR_80).append("\n\n");

        try {
            OperatingSystemMXBean osBean = (OperatingSystemMXBean)
                ManagementFactory.getOperatingSystemMXBean();

            // Basic CPU Information
            sb.append("┌─ PROCESSOR SUMMARY ").append(DASH_79.substring(0, 58)).append("\n");
            sb.append("│\n");
            sb.append("│  Processor Name:        ").append(getCPUName()).append("\n");
            sb.append("│  Number of Cores:       ").append(osBean.getAvailableProcessors()).append("\n");
            sb.append("│  Architecture:          ").append(System.getProperty("os.arch")).append("\n");
            sb.append("│  Endianness:            ").append(System.getProperty("sun.cpu.endian", "Unknown")).append("\n");
            sb.append("│\n");
            sb.append("└").append(DASH_79).append("\n\n");

            // CPU Load Information
            sb.append("┌─ CURRENT CPU UTILIZATION ").append("─".repeat(52)).append("\n");
            sb.append("│\n");
            sb.append("│  System CPU Load:       ").append(String.format("%.2f%%", osBean.getCpuLoad() * 100)).append("\n");
            sb.append("│  Process CPU Load:      ").append(String.format("%.2f%%", osBean.getProcessCpuLoad() * 100)).append("\n");
            sb.append("│  Load Average (1 min):  ").append(String.format("%.2f", osBean.getSystemLoadAverage())).append("\n");
            sb.append("│\n");
            sb.append("└").append(DASH_79).append("\n\n");

            // System Information
            sb.append("┌─ SYSTEM INFORMATION ").append("─".repeat(58)).append("\n");
            sb.append("│\n");
            sb.append("│  Operating System:      ").append(System.getProperty("os.name")).append("\n");
            sb.append("│  OS Version:            ").append(System.getProperty("os.version")).append("\n");
            sb.append("│  Java Version:          ").append(System.getProperty("java.version")).append("\n");
            sb.append("│  Java Vendor:           ").append(System.getProperty("java.vendor")).append("\n");
            sb.append("│\n");
            sb.append("└").append(DASH_79).append("\n\n");

            // Memory Information
            sb.append("┌─ PHYSICAL MEMORY ").append("─".repeat(61)).append("\n");
            sb.append("│\n");
            sb.append("│  Total Physical Memory: ").append(formatBytes(osBean.getTotalMemorySize())).append("\n");
            sb.append("│  Free Physical Memory:  ").append(formatBytes(osBean.getFreeMemorySize())).append("\n");
            sb.append("│  Committed Virtual Mem: ").append(formatBytes(osBean.getCommittedVirtualMemorySize())).append("\n");
            sb.append("│\n");
            sb.append("└").append(DASH_79).append("\n\n");

            // Detailed CPU Features (if available)
            if (os.contains("win")) {
                sb.append(getWindowsCPUDetails());
            } else if (os.contains("linux")) {
                sb.append(getLinuxCPUDetails());
            } else {
                sb.append("┌─ DETAILED CPU FEATURES ").append("─".repeat(55)).append("\n");
                sb.append("│\n");
                sb.append("│  Platform-specific features not available for this OS\n");
                sb.append("│\n");
                sb.append("└").append("─".repeat(79)).append("\n\n");
            }

            // Cache Information
            sb.append("┌─ CACHE INFORMATION ").append("─".repeat(59)).append("\n");
            sb.append("│\n");
            sb.append("│  Cache information requires platform-specific queries\n");
            sb.append("│  Use 'wmic cpu get L2CacheSize,L3CacheSize' on Windows\n");
            sb.append("│  Use 'lscpu' on Linux for detailed cache information\n");
            sb.append("│\n");
            sb.append("└").append(DASH_79).append("\n\n");

        } catch (Exception e) {
            sb.append("Error retrieving CPU information: ").append(e.getMessage()).append("\n");
        }

        sb.append(SEPARATOR_80).append("\n");
        sb.append("End of CPU Identification Report\n");
        sb.append(SEPARATOR_80).append("\n");

        return sb.toString();
    }

    private String getCPUName() {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                Process process = Runtime.getRuntime().exec("wmic cpu get name");
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
                );
                reader.readLine(); // Skip header
                String name = reader.readLine();
                reader.close();
                return name != null ? name.trim() : "Unknown";
            } else if (os.contains("linux")) {
                Process process = Runtime.getRuntime().exec("cat /proc/cpuinfo");
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
                );
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("model name")) {
                        reader.close();
                        return line.split(":")[1].trim();
                    }
                }
                reader.close();
            } else if (os.contains("mac")) {
                Process process = Runtime.getRuntime().exec("sysctl -n machdep.cpu.brand_string");
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
                );
                String name = reader.readLine();
                reader.close();
                return name != null ? name.trim() : "Unknown";
            }
        } catch (Exception e) {
            // Fall through to unknown
        }
        return "Unknown CPU";
    }

    private String getWindowsCPUDetails() {
        StringBuilder sb = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(
                "wmic cpu get NumberOfCores,NumberOfLogicalProcessors,MaxClockSpeed,CurrentClockSpeed,L2CacheSize,L3CacheSize /format:list"
            );
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            sb.append("┌─ DETAILED CPU FEATURES (Windows) ").append("─".repeat(44)).append("\n");
            sb.append("│\n");

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();

                        if (!value.isEmpty()) {
                            sb.append("│  ").append(String.format("%-25s", key + ":"))
                              .append(formatCPUValue(key, value)).append("\n");
                        }
                    }
                }
            }
            reader.close();

            sb.append("│\n");
            sb.append("└").append(DASH_79).append("\n\n");
        } catch (Exception e) {
            sb.append("│  Error retrieving detailed CPU info\n");
            sb.append("└").append("─".repeat(79)).append("\n\n");
        }
        return sb.toString();
    }

    private String getLinuxCPUDetails() {
        StringBuilder sb = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("lscpu");
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            sb.append("┌─ DETAILED CPU FEATURES (Linux) ").append("─".repeat(46)).append("\n");
            sb.append("│\n");

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        sb.append("│  ").append(String.format("%-25s", parts[0].trim() + ":"))
                          .append(parts[1].trim()).append("\n");
                    }
                }
            }
            reader.close();

            sb.append("│\n");
            sb.append("└").append(DASH_79).append("\n\n");
        } catch (Exception e) {
            sb.append("│  Error retrieving detailed CPU info\n");
            sb.append("└").append("─".repeat(79)).append("\n\n");
        }
        return sb.toString();
    }

    private String formatCPUValue(String key, String value) {
        if (key.contains("CacheSize")) {
            try {
                long size = Long.parseLong(value) * 1024; // KB to bytes
                return formatBytes(size);
            } catch (Exception e) {
                return value;
            }
        } else if (key.contains("ClockSpeed")) {
            return value + " MHz";
        }
        return value;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
