import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class NetworkPanel extends JPanel implements MonitorPanel {
    private NetworkMonitor networkMonitor;
    private JTable interfaceTable, connectionTable;
    private DefaultTableModel interfaceTableModel, connectionTableModel;
    private JLabel downloadLabel, uploadLabel, totalDownloadLabel, totalUploadLabel;
    private JPanel graphPanel;
    private Timer updateTimer;
    private DecimalFormat df = new DecimalFormat("#.##");
    private List<Double> downloadHistory = new ArrayList<>();
    private List<Double> uploadHistory = new ArrayList<>();
    private static final int MAX_HISTORY = 60;

    public NetworkPanel() {
        networkMonitor = new NetworkMonitor();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with network stats
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Network Statistics"));

        downloadLabel = new JLabel("Download Speed: 0 KB/s");
        downloadLabel.setFont(new Font("Arial", Font.BOLD, 14));
        uploadLabel = new JLabel("Upload Speed: 0 KB/s");
        uploadLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalDownloadLabel = new JLabel("Total Downloaded: 0 MB");
        totalUploadLabel = new JLabel("Total Uploaded: 0 MB");

        statsPanel.add(downloadLabel);
        statsPanel.add(uploadLabel);
        statsPanel.add(totalDownloadLabel);
        statsPanel.add(totalUploadLabel);

        // Graph panel
        graphPanel = new NetworkGraphPanel(downloadHistory, uploadHistory);
        graphPanel.setPreferredSize(new Dimension(600, 150));
        graphPanel.setBorder(BorderFactory.createTitledBorder("Traffic Graph"));

        // Interface table
        JPanel interfacePanel = new JPanel(new BorderLayout());
        interfacePanel.setBorder(BorderFactory.createTitledBorder("Network Interfaces"));

        String[] interfaceColumns = {"Name", "Display Name", "IP Addresses", "Status"};
        interfaceTableModel = new DefaultTableModel(interfaceColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        interfaceTable = new JTable(interfaceTableModel);
        interfaceTable.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane interfaceScrollPane = new JScrollPane(interfaceTable);
        interfaceScrollPane.setPreferredSize(new Dimension(0, 100));
        interfacePanel.add(interfaceScrollPane, BorderLayout.CENTER);

        // Connection table
        JPanel connectionPanel = new JPanel(new BorderLayout());
        connectionPanel.setBorder(BorderFactory.createTitledBorder("Active Connections"));

        String[] connectionColumns = {"Protocol", "Local Address", "Foreign Address", "State"};
        connectionTableModel = new DefaultTableModel(connectionColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        connectionTable = new JTable(connectionTableModel);
        connectionTable.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane connectionScrollPane = new JScrollPane(connectionTable);
        connectionPanel.add(connectionScrollPane, BorderLayout.CENTER);

        // Layout
        JPanel topSection = new JPanel(new BorderLayout(10, 10));
        topSection.add(statsPanel, BorderLayout.NORTH);
        topSection.add(graphPanel, BorderLayout.CENTER);
        topSection.add(interfacePanel, BorderLayout.SOUTH);

        add(topSection, BorderLayout.NORTH);
        add(connectionPanel, BorderLayout.CENTER);
    }

    @Override
    public void startMonitoring() {
        if (updateTimer == null) {
            updateTimer = new Timer(2000, e -> updateMetrics()); // 2 seconds for network
            loadInterfaces();
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
        NetworkMonitor.NetworkStats stats = networkMonitor.getNetworkStats();

        // Update labels
        downloadLabel.setText("Download Speed: " + formatBytes((long) stats.receiveSpeed) + "/s");
        uploadLabel.setText("Upload Speed: " + formatBytes((long) stats.sendSpeed) + "/s");
        totalDownloadLabel.setText("Total Downloaded: " + formatBytes(stats.totalReceived));
        totalUploadLabel.setText("Total Uploaded: " + formatBytes(stats.totalSent));

        // Update graph data
        downloadHistory.add(stats.receiveSpeed / 1024.0); // Convert to KB
        uploadHistory.add(stats.sendSpeed / 1024.0);
        if (downloadHistory.size() > MAX_HISTORY) {
            downloadHistory.remove(0);
            uploadHistory.remove(0);
        }
        graphPanel.repaint();

        // Update connections
        updateConnections();
    }

    private void loadInterfaces() {
        interfaceTableModel.setRowCount(0);
        List<NetworkMonitor.NetworkInterface> interfaces = networkMonitor.getNetworkInterfaces();
        for (NetworkMonitor.NetworkInterface iface : interfaces) {
            interfaceTableModel.addRow(new Object[]{
                iface.name,
                iface.displayName,
                String.join(", ", iface.ipAddresses),
                iface.isUp ? "Up" : "Down"
            });
        }
    }

    private void updateConnections() {
        List<NetworkMonitor.Connection> connections = networkMonitor.getActiveConnections();

        // Limit to first 100 connections to avoid performance issues
        int maxConnections = Math.min(100, connections.size());

        // Prepare all data in a vector (single operation)
        Object[][] data = new Object[maxConnections][4];
        for (int i = 0; i < maxConnections; i++) {
            NetworkMonitor.Connection conn = connections.get(i);
            data[i][0] = conn.protocol;
            data[i][1] = conn.localAddress;
            data[i][2] = conn.foreignAddress;
            data[i][3] = conn.state;
        }

        // Batch update - suspends rendering during update, single repaint at end
        connectionTable.setAutoCreateRowSorter(false); // Disable sorting during update
        connectionTableModel.setRowCount(0);

        // Add all rows at once
        for (Object[] row : data) {
            connectionTableModel.addRow(row);
        }

        connectionTable.setAutoCreateRowSorter(true); // Re-enable sorting
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return df.format(bytes / 1024.0) + " KB";
        if (bytes < 1024 * 1024 * 1024) return df.format(bytes / (1024.0 * 1024)) + " MB";
        return df.format(bytes / (1024.0 * 1024 * 1024)) + " GB";
    }

    // Network graph panel
    static class NetworkGraphPanel extends JPanel {
        private List<Double> downloadData;
        private List<Double> uploadData;

        public NetworkGraphPanel(List<Double> downloadData, List<Double> uploadData) {
            this.downloadData = downloadData;
            this.uploadData = uploadData;
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Draw grid
            g2d.setColor(Color.DARK_GRAY);
            for (int i = 0; i <= 5; i++) {
                int y = i * height / 5;
                g2d.drawLine(0, y, width, y);
            }

            // Find max value for scaling
            double maxValue = 1.0;
            for (Double val : downloadData) {
                if (val > maxValue) maxValue = val;
            }
            for (Double val : uploadData) {
                if (val > maxValue) maxValue = val;
            }

            int spacing = Math.max(1, width / 60);

            // Draw download graph (green)
            if (downloadData.size() > 1) {
                g2d.setColor(Color.GREEN);
                g2d.setStroke(new BasicStroke(2));
                for (int i = 0; i < downloadData.size() - 1; i++) {
                    int x1 = i * spacing;
                    int x2 = (i + 1) * spacing;
                    int y1 = height - (int) (downloadData.get(i) * height / maxValue);
                    int y2 = height - (int) (downloadData.get(i + 1) * height / maxValue);
                    g2d.drawLine(x1, y1, x2, y2);
                }
            }

            // Draw upload graph (red)
            if (uploadData.size() > 1) {
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(2));
                for (int i = 0; i < uploadData.size() - 1; i++) {
                    int x1 = i * spacing;
                    int x2 = (i + 1) * spacing;
                    int y1 = height - (int) (uploadData.get(i) * height / maxValue);
                    int y2 = height - (int) (uploadData.get(i + 1) * height / maxValue);
                    g2d.drawLine(x1, y1, x2, y2);
                }
            }

            // Draw legend
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.setColor(Color.GREEN);
            g2d.fillRect(10, 10, 15, 15);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Download", 30, 22);

            g2d.setColor(Color.RED);
            g2d.fillRect(110, 10, 15, 15);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Upload", 130, 22);
        }
    }
}
