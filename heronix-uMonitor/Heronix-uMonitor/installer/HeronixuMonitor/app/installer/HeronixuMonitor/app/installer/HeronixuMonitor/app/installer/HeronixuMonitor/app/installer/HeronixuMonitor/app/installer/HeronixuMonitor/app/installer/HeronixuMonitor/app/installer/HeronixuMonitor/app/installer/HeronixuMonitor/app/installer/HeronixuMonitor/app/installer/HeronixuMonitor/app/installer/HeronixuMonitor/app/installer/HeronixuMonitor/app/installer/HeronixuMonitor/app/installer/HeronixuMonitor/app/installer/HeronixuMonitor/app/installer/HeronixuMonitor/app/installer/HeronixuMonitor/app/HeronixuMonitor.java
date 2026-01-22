import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class HeronixuMonitor {
    private JFrame mainFrame;
    private JTabbedPane tabbedPane;
    private Map<String, MonitorPanel> panels;
    private String currentTab = "Overview";

    public HeronixuMonitor() {
        panels = new HashMap<>();
        initializeUI();
    }

    private void initializeUI() {
        mainFrame = new JFrame("Heronix-uMonitor v 1.1 - By Michael Katsaros");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1200, 800);
        mainFrame.setLayout(new BorderLayout());

        // Apply dark theme
        applyDarkTheme();

        // Create tabbed pane for different monitoring views
        tabbedPane = new JTabbedPane();

        // Create panels - they won't start monitoring until activated
        OverviewPanel overviewPanel = new OverviewPanel();
        PerformancePanel performancePanel = new PerformancePanel();
        ProcessPanel processPanel = new ProcessPanel();
        NetworkPanel networkPanel = new NetworkPanel();
        PortPanel portPanel = new PortPanel();
        DiagnosticsPanel diagnosticsPanel = new DiagnosticsPanel();
        CPUIDPanel cpuidPanel = new CPUIDPanel();

        // Store panels
        panels.put("Overview", overviewPanel);
        panels.put("Performance", performancePanel);
        panels.put("Processes", processPanel);
        panels.put("Network", networkPanel);
        panels.put("Ports", portPanel);
        panels.put("CPU-ID", cpuidPanel);
        panels.put("Diagnostics", diagnosticsPanel);

        // Add panels to tabs
        tabbedPane.addTab("Overview", overviewPanel);
        tabbedPane.addTab("Performance", performancePanel);
        tabbedPane.addTab("Processes", processPanel);
        tabbedPane.addTab("Network", networkPanel);
        tabbedPane.addTab("Ports", portPanel);
        tabbedPane.addTab("CPU-ID", cpuidPanel);
        tabbedPane.addTab("Diagnostics", diagnosticsPanel);

        // Only start monitoring for active tab to improve performance
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                handleTabChange();
            }
        });

        mainFrame.add(tabbedPane, BorderLayout.CENTER);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);

        // Start monitoring for the first tab only
        overviewPanel.startMonitoring();
    }

    private void handleTabChange() {
        String newTab = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());

        // Stop monitoring on previous tab to save resources
        MonitorPanel previousPanel = panels.get(currentTab);
        if (previousPanel != null) {
            previousPanel.stopMonitoring();
        }

        // Start monitoring on new tab
        MonitorPanel newPanel = panels.get(newTab);
        if (newPanel != null) {
            newPanel.startMonitoring();
        }

        currentTab = newTab;
    }

    private void applyDarkTheme() {
        // Dark theme colors
        Color darkBg = new Color(45, 45, 48);
        Color darkerBg = new Color(30, 30, 30);
        Color lightText = new Color(220, 220, 220);
        Color accent = new Color(0, 122, 204);

        try {
            UIManager.put("Panel.background", darkBg);
            UIManager.put("TabbedPane.background", darkerBg);
            UIManager.put("TabbedPane.foreground", lightText);
            UIManager.put("TabbedPane.selected", accent);
            UIManager.put("Table.background", darkBg);
            UIManager.put("Table.foreground", lightText);
            UIManager.put("Table.gridColor", new Color(60, 60, 60));
            UIManager.put("TableHeader.background", darkerBg);
            UIManager.put("TableHeader.foreground", lightText);
            UIManager.put("ScrollPane.background", darkBg);
            UIManager.put("Viewport.background", darkBg);
            UIManager.put("Label.foreground", lightText);
            UIManager.put("TextField.background", darkerBg);
            UIManager.put("TextField.foreground", lightText);
            UIManager.put("TextField.caretForeground", lightText);
            UIManager.put("TextArea.background", darkerBg);
            UIManager.put("TextArea.foreground", lightText);
            UIManager.put("Button.background", new Color(60, 60, 60));
            UIManager.put("Button.foreground", lightText);
            UIManager.put("ComboBox.background", darkerBg);
            UIManager.put("ComboBox.foreground", lightText);
            UIManager.put("ProgressBar.background", darkerBg);
            UIManager.put("ProgressBar.foreground", accent);
            UIManager.put("TitledBorder.titleColor", lightText);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mainFrame.getContentPane().setBackground(darkBg);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new HeronixuMonitor();
        });
    }
}
