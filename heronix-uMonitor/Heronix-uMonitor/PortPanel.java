import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * Port panel - Shows open ports with security information
 */
public class PortPanel extends JPanel implements MonitorPanel {
    private JTable portTable;
    private DefaultTableModel tableModel;
    private javax.swing.Timer updateTimer;
    private JLabel statusLabel;

    public PortPanel() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 48));

        // Top panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(45, 45, 48));
        statusLabel = new JLabel("Scanning ports...");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        topPanel.add(statusLabel);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> scanPorts());
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"Port", "Protocol", "State", "Risk Level"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        portTable = new JTable(tableModel);
        portTable.setBackground(new Color(30, 30, 30));
        portTable.setForeground(new Color(220, 220, 220));
        portTable.setGridColor(new Color(60, 60, 60));
        portTable.setSelectionBackground(new Color(75, 110, 175));
        portTable.setFont(new Font("Consolas", Font.PLAIN, 11));
        portTable.getTableHeader().setBackground(new Color(60, 60, 60));
        portTable.getTableHeader().setForeground(Color.WHITE);
        portTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(portTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void startMonitoring() {
        scanPorts();
        if (updateTimer == null) {
            updateTimer = new javax.swing.Timer(10000, e -> scanPorts());
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

    private void scanPorts() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            java.util.List<PortInfo> ports;

            @Override
            protected Void doInBackground() {
                ports = getOpenPorts();
                return null;
            }

            @Override
            protected void done() {
                displayPorts(ports);
            }
        };
        worker.execute();
    }

    private java.util.List<PortInfo> getOpenPorts() {
        java.util.List<PortInfo> ports = new ArrayList<>();

        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process p;

            if (os.contains("win")) {
                p = Runtime.getRuntime().exec("netstat -ano");
            } else {
                p = Runtime.getRuntime().exec("netstat -tuln");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("LISTENING") || line.contains("LISTEN")) {
                    try {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length >= 2) {
                            String protocol = parts[0];
                            String address = parts[1];

                            // Extract port
                            int portNumber = extractPort(address);
                            if (portNumber > 0) {
                                String risk = assessRisk(portNumber);
                                ports.add(new PortInfo(portNumber, protocol, "LISTENING", risk));
                            }
                        }
                    } catch (Exception e) {
                        // Skip malformed lines
                    }
                }
            }
            reader.close();
            p.destroy();

        } catch (Exception e) {
            System.err.println("Error scanning ports: " + e.getMessage());
        }

        return ports;
    }

    private int extractPort(String address) {
        try {
            int colonIndex = address.lastIndexOf(':');
            if (colonIndex > 0) {
                return Integer.parseInt(address.substring(colonIndex + 1));
            }
        } catch (Exception e) {
            // Ignore
        }
        return -1;
    }

    private String assessRisk(int port) {
        // Well-known ports
        if (port == 22 || port == 80 || port == 443) return "Low";
        if (port == 21 || port == 23 || port == 3389) return "Medium";
        if (port == 135 || port == 139 || port == 445) return "High";
        if (port < 1024) return "Medium";
        return "Low";
    }

    private void displayPorts(java.util.List<PortInfo> ports) {
        statusLabel.setText("Open Ports: " + ports.size());

        // Prepare data
        Object[][] data = new Object[ports.size()][4];
        for (int i = 0; i < ports.size(); i++) {
            PortInfo port = ports.get(i);
            data[i][0] = port.port;
            data[i][1] = port.protocol;
            data[i][2] = port.state;
            data[i][3] = port.risk;
        }

        // Batch update
        portTable.setAutoCreateRowSorter(false);
        tableModel.setRowCount(0);
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
        portTable.setAutoCreateRowSorter(true);
    }

    static class PortInfo {
        int port;
        String protocol;
        String state;
        String risk;

        PortInfo(int port, String protocol, String state, String risk) {
            this.port = port;
            this.protocol = protocol;
            this.state = state;
            this.risk = risk;
        }
    }
}
