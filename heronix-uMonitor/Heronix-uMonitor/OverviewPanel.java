import javax.swing.*;
import java.awt.*;

/**
 * Overview panel - System summary dashboard
 */
public class OverviewPanel extends JPanel implements MonitorPanel {
    private JTextArea overviewArea;
    private SystemMonitor systemMonitor;
    private Timer updateTimer;

    public OverviewPanel() {
        initializeUI();
        systemMonitor = new SystemMonitor();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 48));

        overviewArea = new JTextArea();
        overviewArea.setEditable(false);
        overviewArea.setBackground(new Color(30, 30, 30));
        overviewArea.setForeground(new Color(220, 220, 220));
        overviewArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        overviewArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(overviewArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void startMonitoring() {
        updateOverview();
        if (updateTimer == null) {
            updateTimer = new Timer(2000, e -> updateOverview());
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

    private void updateOverview() {
        StringBuilder sb = new StringBuilder();

        sb.append("═══════════════════════════════════════════════════════════════════\n");
        sb.append("                        SYSTEM OVERVIEW                            \n");
        sb.append("═══════════════════════════════════════════════════════════════════\n\n");

        // System Info
        SystemMonitor.SystemInfo info = systemMonitor.getSystemInfo();
        sb.append("Operating System: ").append(info.osName).append(" ").append(info.osVersion).append("\n");
        sb.append("Architecture:     ").append(info.architecture).append("\n");
        sb.append("Processors:       ").append(info.processors).append(" cores\n\n");

        // CPU Usage
        double cpuUsage = systemMonitor.getSystemCpuUsage();
        sb.append("CPU Usage:        ").append(String.format("%.1f%%", cpuUsage)).append("\n");
        sb.append("  ").append(getProgressBar(cpuUsage, 50)).append("\n\n");

        // Memory
        SystemMonitor.MemoryStats memStats = systemMonitor.getMemoryStats();
        sb.append("Memory Usage:\n");
        sb.append("  Total:          ").append(formatBytes(memStats.total)).append("\n");
        sb.append("  Used:           ").append(formatBytes(memStats.used)).append("\n");
        sb.append("  Free:           ").append(formatBytes(memStats.free)).append("\n");
        sb.append("  Usage:          ").append(String.format("%.1f%%", memStats.usagePercent)).append("\n");
        sb.append("  ").append(getProgressBar(memStats.usagePercent, 50)).append("\n\n");

        // Disk Info
        sb.append("Disk Information:\n");
        SystemMonitor.DiskStats[] disks = systemMonitor.getDiskStats();
        for (SystemMonitor.DiskStats disk : disks) {
            if (disk.total > 0) {
                sb.append("  Drive ").append(disk.path).append("\n");
                sb.append("    Total: ").append(formatBytes(disk.total));
                sb.append("  |  Used: ").append(formatBytes(disk.used));
                sb.append("  |  Free: ").append(formatBytes(disk.free)).append("\n");
                sb.append("    ").append(getProgressBar(disk.usagePercent, 45)).append("\n");
            }
        }

        sb.append("\n");
        sb.append("═══════════════════════════════════════════════════════════════════\n");
        sb.append("Last Updated: ").append(new java.util.Date()).append("\n");

        overviewArea.setText(sb.toString());
    }

    private String getProgressBar(double percent, int width) {
        int filled = (int) ((percent / 100.0) * width);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            if (i < filled) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        bar.append("] ").append(String.format("%.1f%%", percent));
        return bar.toString();
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
