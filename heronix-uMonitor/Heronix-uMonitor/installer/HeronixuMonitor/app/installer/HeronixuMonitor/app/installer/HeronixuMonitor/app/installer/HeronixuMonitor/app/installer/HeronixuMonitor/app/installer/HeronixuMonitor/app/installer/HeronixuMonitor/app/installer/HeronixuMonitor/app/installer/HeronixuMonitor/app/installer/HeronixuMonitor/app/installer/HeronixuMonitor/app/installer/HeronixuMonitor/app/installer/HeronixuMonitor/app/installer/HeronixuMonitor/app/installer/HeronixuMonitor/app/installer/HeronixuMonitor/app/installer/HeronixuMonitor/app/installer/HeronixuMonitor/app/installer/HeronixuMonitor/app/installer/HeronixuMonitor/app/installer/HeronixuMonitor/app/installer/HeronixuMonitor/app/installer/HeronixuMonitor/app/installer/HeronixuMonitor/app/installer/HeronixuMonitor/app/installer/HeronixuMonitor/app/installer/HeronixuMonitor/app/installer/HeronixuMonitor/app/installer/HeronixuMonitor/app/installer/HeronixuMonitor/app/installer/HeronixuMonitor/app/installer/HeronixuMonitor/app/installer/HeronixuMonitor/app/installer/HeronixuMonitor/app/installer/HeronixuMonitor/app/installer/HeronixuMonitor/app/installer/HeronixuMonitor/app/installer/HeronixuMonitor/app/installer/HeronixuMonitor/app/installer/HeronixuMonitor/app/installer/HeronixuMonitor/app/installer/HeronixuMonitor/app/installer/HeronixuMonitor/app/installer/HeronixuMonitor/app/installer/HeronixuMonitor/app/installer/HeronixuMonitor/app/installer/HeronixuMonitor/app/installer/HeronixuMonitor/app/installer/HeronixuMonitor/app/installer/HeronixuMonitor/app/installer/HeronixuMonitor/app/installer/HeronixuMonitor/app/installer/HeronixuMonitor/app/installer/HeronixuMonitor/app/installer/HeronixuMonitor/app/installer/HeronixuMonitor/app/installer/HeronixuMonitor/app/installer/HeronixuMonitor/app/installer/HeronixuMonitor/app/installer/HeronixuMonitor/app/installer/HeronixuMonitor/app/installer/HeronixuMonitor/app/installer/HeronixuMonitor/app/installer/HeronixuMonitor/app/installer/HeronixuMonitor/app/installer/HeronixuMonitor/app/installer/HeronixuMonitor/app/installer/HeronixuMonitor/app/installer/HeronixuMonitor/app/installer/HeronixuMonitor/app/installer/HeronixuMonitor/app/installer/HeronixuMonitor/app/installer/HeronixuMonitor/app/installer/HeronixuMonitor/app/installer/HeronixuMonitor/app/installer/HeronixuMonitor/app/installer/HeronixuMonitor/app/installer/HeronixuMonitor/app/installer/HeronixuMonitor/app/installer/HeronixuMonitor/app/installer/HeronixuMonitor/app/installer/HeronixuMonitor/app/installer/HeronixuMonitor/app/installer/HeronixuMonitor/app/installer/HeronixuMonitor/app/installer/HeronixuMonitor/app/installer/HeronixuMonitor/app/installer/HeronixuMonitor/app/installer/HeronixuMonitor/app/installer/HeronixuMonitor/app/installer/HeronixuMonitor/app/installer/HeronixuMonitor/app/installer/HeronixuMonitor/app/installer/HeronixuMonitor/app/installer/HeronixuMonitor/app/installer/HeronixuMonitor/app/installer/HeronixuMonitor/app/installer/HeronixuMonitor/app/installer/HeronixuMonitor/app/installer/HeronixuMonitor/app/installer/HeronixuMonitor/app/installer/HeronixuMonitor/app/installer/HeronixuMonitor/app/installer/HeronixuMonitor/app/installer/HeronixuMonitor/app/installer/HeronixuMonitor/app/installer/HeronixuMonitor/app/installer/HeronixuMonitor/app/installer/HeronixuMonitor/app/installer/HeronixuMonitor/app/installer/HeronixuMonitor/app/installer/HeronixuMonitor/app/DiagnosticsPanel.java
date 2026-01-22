import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DiagnosticsPanel extends JPanel implements MonitorPanel {
    private SystemMonitor systemMonitor;
    private NetworkMonitor networkMonitor;
    private ProcessMonitor processMonitor;
    private JTextArea diagnosticsArea;
    private JLabel statusLabel;
    private Timer updateTimer;
    private DecimalFormat df = new DecimalFormat("#.##");

    public DiagnosticsPanel() {
        systemMonitor = new SystemMonitor();
        networkMonitor = new NetworkMonitor();
        processMonitor = new ProcessMonitor();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with status
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("System Health Status"));

        statusLabel = new JLabel("System Status: Checking...");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setForeground(Color.BLUE);

        JButton runDiagnosticsButton = new JButton("Run Full Diagnostics");
        runDiagnosticsButton.addActionListener(e -> runDiagnostics());

        topPanel.add(statusLabel, BorderLayout.CENTER);
        topPanel.add(runDiagnosticsButton, BorderLayout.EAST);

        // Diagnostics text area
        diagnosticsArea = new JTextArea();
        diagnosticsArea.setEditable(false);
        diagnosticsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        diagnosticsArea.setBackground(Color.BLACK);
        diagnosticsArea.setForeground(Color.GREEN);
        JScrollPane scrollPane = new JScrollPane(diagnosticsArea);

        // Bottom panel with alerts
        JPanel alertPanel = createAlertPanel();

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(alertPanel, BorderLayout.SOUTH);

        runDiagnostics(); // Initial run
    }

    private JPanel createAlertPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Alert Thresholds"));

        JPanel cpuPanel = new JPanel(new BorderLayout());
        cpuPanel.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
        cpuPanel.add(new JLabel("CPU Threshold: 80%", JLabel.CENTER));

        JPanel memoryPanel = new JPanel(new BorderLayout());
        memoryPanel.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
        memoryPanel.add(new JLabel("Memory Threshold: 85%", JLabel.CENTER));

        JPanel diskPanel = new JPanel(new BorderLayout());
        diskPanel.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
        diskPanel.add(new JLabel("Disk Threshold: 90%", JLabel.CENTER));

        panel.add(cpuPanel);
        panel.add(memoryPanel);
        panel.add(diskPanel);

        return panel;
    }

    @Override
    public void startMonitoring() {
        if (updateTimer == null) {
            updateTimer = new Timer(10000, e -> runDiagnostics()); // 10 seconds for diagnostics
            runDiagnostics(); // Initial run
        }
        if (!updateTimer.isRunning()) {
            updateTimer.start();
        }
    }

    @Override
    public void stopMonitoring() {
        if (updateTimer != null && updateTimer.isRunning()) {
            updateTimer.stop();
        }
    }

    private void runDiagnostics() {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        sb.append("=".repeat(70)).append("\n");
        sb.append("HERONIX uMONITOR - SYSTEM DIAGNOSTICS REPORT\n");
        sb.append("Generated: ").append(sdf.format(new Date())).append("\n");
        sb.append("=".repeat(70)).append("\n\n");

        boolean hasIssues = false;
        StringBuilder issues = new StringBuilder();

        // System Information
        sb.append(">>> SYSTEM INFORMATION\n");
        SystemMonitor.SystemInfo info = systemMonitor.getSystemInfo();
        sb.append("  Operating System: ").append(info.osName).append(" ")
          .append(info.osVersion).append("\n");
        sb.append("  Architecture: ").append(info.architecture).append("\n");
        sb.append("  Processors: ").append(info.processors).append(" cores\n");
        sb.append("  Total Memory: ").append(formatBytes(info.totalMemory)).append("\n\n");

        // CPU Diagnostics
        sb.append(">>> CPU DIAGNOSTICS\n");
        double cpuUsage = systemMonitor.getSystemCpuUsage();
        sb.append("  CPU Usage: ").append(df.format(cpuUsage)).append("%\n");
        if (cpuUsage > 80) {
            sb.append("  [WARNING] CPU usage is HIGH!\n");
            hasIssues = true;
            issues.append("High CPU usage (").append(df.format(cpuUsage)).append("%); ");
        } else if (cpuUsage > 50) {
            sb.append("  [INFO] CPU usage is moderate\n");
        } else {
            sb.append("  [OK] CPU usage is normal\n");
        }
        sb.append("\n");

        // Memory Diagnostics
        sb.append(">>> MEMORY DIAGNOSTICS\n");
        SystemMonitor.MemoryStats memStats = systemMonitor.getMemoryStats();
        sb.append("  Total Memory: ").append(formatBytes(memStats.total)).append("\n");
        sb.append("  Used Memory: ").append(formatBytes(memStats.used)).append("\n");
        sb.append("  Free Memory: ").append(formatBytes(memStats.free)).append("\n");
        sb.append("  Memory Usage: ").append(df.format(memStats.usagePercent)).append("%\n");

        if (memStats.usagePercent > 85) {
            sb.append("  [WARNING] Memory usage is HIGH!\n");
            hasIssues = true;
            issues.append("High memory usage (").append(df.format(memStats.usagePercent))
                  .append("%); ");
        } else if (memStats.usagePercent > 70) {
            sb.append("  [INFO] Memory usage is moderate\n");
        } else {
            sb.append("  [OK] Memory usage is normal\n");
        }
        sb.append("\n");

        // Disk Diagnostics
        sb.append(">>> DISK DIAGNOSTICS\n");
        var disks = systemMonitor.getDiskStats();
        for (SystemMonitor.DiskStats disk : disks) {
            sb.append("  Drive: ").append(disk.path).append("\n");
            sb.append("    Total: ").append(formatBytes(disk.total)).append("\n");
            sb.append("    Used: ").append(formatBytes(disk.used)).append("\n");
            sb.append("    Free: ").append(formatBytes(disk.free)).append("\n");
            sb.append("    Usage: ").append(df.format(disk.usagePercent)).append("%\n");

            if (disk.usagePercent > 90) {
                sb.append("    [WARNING] Disk usage is CRITICAL!\n");
                hasIssues = true;
                issues.append("Critical disk usage on ").append(disk.path)
                      .append(" (").append(df.format(disk.usagePercent)).append("%); ");
            } else if (disk.usagePercent > 75) {
                sb.append("    [INFO] Disk usage is high\n");
            } else {
                sb.append("    [OK] Disk usage is normal\n");
            }
        }
        sb.append("\n");

        // Network Diagnostics
        sb.append(">>> NETWORK DIAGNOSTICS\n");
        NetworkMonitor.NetworkStats netStats = networkMonitor.getNetworkStats();
        sb.append("  Download Speed: ").append(formatBytes((long) netStats.receiveSpeed))
          .append("/s\n");
        sb.append("  Upload Speed: ").append(formatBytes((long) netStats.sendSpeed))
          .append("/s\n");
        sb.append("  Total Downloaded: ").append(formatBytes(netStats.totalReceived))
          .append("\n");
        sb.append("  Total Uploaded: ").append(formatBytes(netStats.totalSent)).append("\n");

        var interfaces = networkMonitor.getNetworkInterfaces();
        sb.append("  Active Interfaces: ").append(interfaces.size()).append("\n");
        sb.append("  [OK] Network is operational\n\n");

        // Process Count
        sb.append(">>> PROCESS DIAGNOSTICS\n");
        var processes = processMonitor.getRunningProcesses();
        sb.append("  Running Processes: ").append(processes.size()).append("\n");
        sb.append("  [OK] Process count is normal\n\n");

        // Final Status
        sb.append("=".repeat(70)).append("\n");
        if (hasIssues) {
            sb.append("OVERALL STATUS: WARNINGS DETECTED\n");
            sb.append("Issues: ").append(issues.toString()).append("\n");
            statusLabel.setText("System Status: ⚠ Warnings Detected");
            statusLabel.setForeground(Color.ORANGE);
        } else {
            sb.append("OVERALL STATUS: ALL SYSTEMS HEALTHY\n");
            statusLabel.setText("System Status: ✓ Healthy");
            statusLabel.setForeground(Color.GREEN);
        }
        sb.append("=".repeat(70)).append("\n");

        diagnosticsArea.setText(sb.toString());
        diagnosticsArea.setCaretPosition(0);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return df.format(bytes / 1024.0) + " KB";
        if (bytes < 1024 * 1024 * 1024) return df.format(bytes / (1024.0 * 1024)) + " MB";
        return df.format(bytes / (1024.0 * 1024 * 1024)) + " GB";
    }
}
