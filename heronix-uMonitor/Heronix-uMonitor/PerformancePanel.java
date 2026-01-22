import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

/**
 * Performance panel - CPU and Memory graphs with historical data
 */
public class PerformancePanel extends JPanel implements MonitorPanel {
    private SystemMonitor systemMonitor;
    private Timer updateTimer;

    private LinkedList<Double> cpuHistory = new LinkedList<>();
    private LinkedList<Double> memHistory = new LinkedList<>();
    private static final int MAX_HISTORY = 60;

    private GraphPanel cpuGraph;
    private GraphPanel memGraph;
    private JLabel cpuLabel;
    private JLabel memLabel;

    public PerformancePanel() {
        systemMonitor = new SystemMonitor();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridLayout(2, 1, 5, 5));
        setBackground(new Color(45, 45, 48));

        // CPU Panel
        JPanel cpuPanel = new JPanel(new BorderLayout());
        cpuPanel.setBackground(new Color(45, 45, 48));
        cpuLabel = new JLabel("CPU Usage: 0.0%");
        cpuLabel.setForeground(Color.WHITE);
        cpuLabel.setFont(new Font("Arial", Font.BOLD, 14));
        cpuPanel.add(cpuLabel, BorderLayout.NORTH);

        cpuGraph = new GraphPanel(Color.GREEN);
        cpuPanel.add(cpuGraph, BorderLayout.CENTER);
        add(cpuPanel);

        // Memory Panel
        JPanel memPanel = new JPanel(new BorderLayout());
        memPanel.setBackground(new Color(45, 45, 48));
        memLabel = new JLabel("Memory Usage: 0.0%");
        memLabel.setForeground(Color.WHITE);
        memLabel.setFont(new Font("Arial", Font.BOLD, 14));
        memPanel.add(memLabel, BorderLayout.NORTH);

        memGraph = new GraphPanel(Color.CYAN);
        memPanel.add(memGraph, BorderLayout.CENTER);
        add(memPanel);
    }

    @Override
    public void startMonitoring() {
        if (updateTimer == null) {
            updateTimer = new Timer(1000, e -> updateMetrics());
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

    private void updateMetrics() {
        // Get CPU usage
        double cpu = systemMonitor.getSystemCpuUsage();
        cpuHistory.add(cpu);
        if (cpuHistory.size() > MAX_HISTORY) {
            cpuHistory.removeFirst();
        }
        cpuLabel.setText(String.format("CPU Usage: %.1f%%", cpu));
        cpuGraph.setData(cpuHistory);

        // Get Memory usage
        SystemMonitor.MemoryStats memStats = systemMonitor.getMemoryStats();
        memHistory.add(memStats.usagePercent);
        if (memHistory.size() > MAX_HISTORY) {
            memHistory.removeFirst();
        }
        memLabel.setText(String.format("Memory Usage: %.1f%% (%s / %s)",
            memStats.usagePercent,
            formatBytes(memStats.used),
            formatBytes(memStats.total)));
        memGraph.setData(memHistory);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024 * 1024) return String.format("%.0f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * Custom panel for drawing graphs
     */
    class GraphPanel extends JPanel {
        private LinkedList<Double> data;
        private Color lineColor;

        public GraphPanel(Color lineColor) {
            this.lineColor = lineColor;
            this.data = new LinkedList<>();
            setBackground(new Color(30, 30, 30));
            setPreferredSize(new Dimension(600, 150));
        }

        public void setData(LinkedList<Double> data) {
            this.data = new LinkedList<>(data);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (data.isEmpty()) return;

            int width = getWidth();
            int height = getHeight();
            int padding = 20;

            // Draw grid
            g2d.setColor(new Color(60, 60, 60));
            for (int i = 0; i <= 4; i++) {
                int y = padding + (height - 2 * padding) * i / 4;
                g2d.drawLine(padding, y, width - padding, y);
            }

            // Draw data
            g2d.setColor(lineColor);
            g2d.setStroke(new BasicStroke(2.0f));

            int pointSpacing = (width - 2 * padding) / Math.max(1, MAX_HISTORY - 1);
            int prevX = padding;
            int prevY = height - padding - (int) ((data.getFirst() / 100.0) * (height - 2 * padding));

            int index = 0;
            for (Double value : data) {
                int x = padding + index * pointSpacing;
                int y = height - padding - (int) ((value / 100.0) * (height - 2 * padding));

                if (index > 0) {
                    g2d.drawLine(prevX, prevY, x, y);
                }

                prevX = x;
                prevY = y;
                index++;
            }

            // Draw labels
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString("100%", 2, padding + 10);
            g2d.drawString("0%", 2, height - padding);
        }
    }
}
