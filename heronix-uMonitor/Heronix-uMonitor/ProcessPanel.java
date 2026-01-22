import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Process panel - Shows list of running processes
 */
public class ProcessPanel extends JPanel implements MonitorPanel {
    private JTable processTable;
    private DefaultTableModel tableModel;
    private ProcessMonitor processMonitor;
    private Timer updateTimer;
    private JLabel countLabel;

    public ProcessPanel() {
        processMonitor = new ProcessMonitor();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 48));

        // Top panel with info
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(45, 45, 48));
        countLabel = new JLabel("Processes: 0");
        countLabel.setForeground(Color.WHITE);
        countLabel.setFont(new Font("Arial", Font.BOLD, 12));
        topPanel.add(countLabel);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"PID", "Name", "CPU %", "Memory"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        processTable = new JTable(tableModel);
        processTable.setBackground(new Color(30, 30, 30));
        processTable.setForeground(new Color(220, 220, 220));
        processTable.setGridColor(new Color(60, 60, 60));
        processTable.setSelectionBackground(new Color(75, 110, 175));
        processTable.setFont(new Font("Consolas", Font.PLAIN, 11));
        processTable.getTableHeader().setBackground(new Color(60, 60, 60));
        processTable.getTableHeader().setForeground(Color.WHITE);
        processTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(processTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void startMonitoring() {
        updateProcessList();
        if (updateTimer == null) {
            updateTimer = new Timer(3000, e -> updateProcessList());
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

    private void updateProcessList() {
        SwingUtilities.invokeLater(() -> {
            var processes = processMonitor.getProcessList();
            countLabel.setText("Processes: " + processes.size());

            // Prepare data
            Object[][] data = new Object[Math.min(processes.size(), 100)][4];
            for (int i = 0; i < data.length; i++) {
                ProcessMonitor.ProcessInfo proc = processes.get(i);
                data[i][0] = proc.pid;
                data[i][1] = proc.name;
                data[i][2] = String.format("%.1f", proc.cpuUsage);
                data[i][3] = formatBytes(proc.memoryUsage);
            }

            // Batch update
            processTable.setAutoCreateRowSorter(false);
            tableModel.setRowCount(0);
            for (Object[] row : data) {
                tableModel.addRow(row);
            }
            processTable.setAutoCreateRowSorter(true);
        });
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.0f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
