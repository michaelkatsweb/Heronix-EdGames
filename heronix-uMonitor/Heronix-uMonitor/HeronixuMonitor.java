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

        // Create tabbed pane for different monitoring views
        tabbedPane = new JTabbedPane();

        // Apply dark theme (after tabbedPane is created)
        applyDarkTheme();

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

        // Apply white text color to all tabs
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setForegroundAt(i, Color.WHITE);
        }

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
        // Dark theme colors - Java 21 modern style
        final Color darkBg = new Color(45, 45, 48);
        final Color darkerBg = new Color(30, 30, 30);
        final Color lightText = new Color(220, 220, 220);
        final Color accent = new Color(0, 122, 204);
        final Color tabBg = new Color(37, 37, 38);
        final Color selectedTabBg = new Color(63, 63, 70);
        final Color borderColor = new Color(60, 60, 60);

        try {
            // Panel backgrounds
            UIManager.put("Panel.background", darkBg);
            UIManager.put("OptionPane.background", darkBg);

            // TabbedPane - Fix tab colors
            UIManager.put("TabbedPane.background", tabBg);
            UIManager.put("TabbedPane.foreground", lightText);
            UIManager.put("TabbedPane.selected", selectedTabBg);
            UIManager.put("TabbedPane.contentAreaColor", darkBg);
            UIManager.put("TabbedPane.selectedForeground", new Color(255, 255, 255));
            UIManager.put("TabbedPane.unselectedBackground", tabBg);
            UIManager.put("TabbedPane.selectHighlight", accent);
            UIManager.put("TabbedPane.darkShadow", borderColor);
            UIManager.put("TabbedPane.light", borderColor);
            UIManager.put("TabbedPane.shadow", borderColor);
            UIManager.put("TabbedPane.borderHightlightColor", borderColor);
            UIManager.put("TabbedPane.focus", accent);

            // Tables
            UIManager.put("Table.background", darkBg);
            UIManager.put("Table.foreground", lightText);
            UIManager.put("Table.gridColor", borderColor);
            UIManager.put("Table.selectionBackground", selectedTabBg);
            UIManager.put("Table.selectionForeground", new Color(255, 255, 255));
            UIManager.put("TableHeader.background", darkerBg);
            UIManager.put("TableHeader.foreground", lightText);

            // Scroll panes
            UIManager.put("ScrollPane.background", darkBg);
            UIManager.put("Viewport.background", darkBg);
            UIManager.put("ScrollBar.background", darkerBg);
            UIManager.put("ScrollBar.thumb", borderColor);
            UIManager.put("ScrollBar.track", darkerBg);

            // Text components
            UIManager.put("Label.foreground", lightText);
            UIManager.put("TextField.background", darkerBg);
            UIManager.put("TextField.foreground", lightText);
            UIManager.put("TextField.caretForeground", lightText);
            UIManager.put("TextField.selectionBackground", selectedTabBg);
            UIManager.put("TextArea.background", darkerBg);
            UIManager.put("TextArea.foreground", lightText);
            UIManager.put("TextArea.caretForeground", lightText);
            UIManager.put("TextArea.selectionBackground", selectedTabBg);

            // Buttons and controls
            UIManager.put("Button.background", borderColor);
            UIManager.put("Button.foreground", lightText);
            UIManager.put("Button.select", selectedTabBg);
            UIManager.put("ComboBox.background", darkerBg);
            UIManager.put("ComboBox.foreground", lightText);
            UIManager.put("ComboBox.selectionBackground", selectedTabBg);
            UIManager.put("ComboBox.selectionForeground", new Color(255, 255, 255));

            // Progress bars
            UIManager.put("ProgressBar.background", darkerBg);
            UIManager.put("ProgressBar.foreground", accent);
            UIManager.put("ProgressBar.selectionBackground", lightText);
            UIManager.put("ProgressBar.selectionForeground", darkBg);

            // Borders and separators
            UIManager.put("TitledBorder.titleColor", lightText);
            UIManager.put("Separator.foreground", borderColor);
            UIManager.put("Separator.background", darkBg);

            // CheckBox and Radio buttons
            UIManager.put("CheckBox.background", darkBg);
            UIManager.put("CheckBox.foreground", lightText);
            UIManager.put("RadioButton.background", darkBg);
            UIManager.put("RadioButton.foreground", lightText);

        } catch (Exception e) {
            e.printStackTrace();
        }

        mainFrame.getContentPane().setBackground(darkBg);

        // Set tab colors directly on the component
        tabbedPane.setBackground(tabBg);
        tabbedPane.setForeground(Color.WHITE); // White text for tabs
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Don't use System Look and Feel - it overrides our dark theme
            // Using default Metal L&F which respects our custom colors
            new HeronixuMonitor();
        });
    }
}
