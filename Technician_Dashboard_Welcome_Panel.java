package Main;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.plot.PiePlot;

/**
 * Technician dashboard welcome panel: status summary list + pie chart. The
 * status text entries are clickable and will open the Test Records card,
 * filtered by the clicked status via EventBus ("open.tests.status" event).
 */
public class Technician_Dashboard_Welcome_Panel extends JPanel {

    public Technician_Dashboard_Welcome_Panel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);

        JLabel title = new JLabel("Technician Dashboard", SwingConstants.LEFT);
        title.setFont(UiTheme.HEADER_FONT.deriveFont(22f));
        title.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        add(title, BorderLayout.NORTH);

        JPanel main = new JPanel(new GridLayout(1, 2, 12, 0));
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Left: summary area with clickable status lines
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(UiTheme.PANEL_BG);
        left.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel hint = new JLabel("<html><b>Test Status Summary</b><br><br>Click status below to view list:</html>");
        hint.setFont(UiTheme.NORMAL_FONT);
        left.add(hint);
        left.add(Box.createRigidArea(new Dimension(0, 10)));

        // Get counts
        int pending = 0, completed = 0, verified = 0;
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("SELECT status, COUNT(*) AS c FROM tests GROUP BY status")) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    int cnt = rs.getInt("c");
                    if ("Pending".equalsIgnoreCase(status)) {
                        pending = cnt;
                    } else if ("Completed".equalsIgnoreCase(status)) {
                        completed = cnt;
                    } else if ("Verified".equalsIgnoreCase(status)) {
                        verified = cnt;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Create clickable buttons that look like links
        left.add(createStatusButton("Pending Tests: " + pending, "Pending"));
        left.add(Box.createRigidArea(new Dimension(0, 8)));
        left.add(createStatusButton("Completed Tests: " + completed, "Completed"));
        left.add(Box.createRigidArea(new Dimension(0, 8)));
        left.add(createStatusButton("Verified Tests: " + verified, "Verified"));

        // Right: pie chart
        DefaultPieDataset ds = new DefaultPieDataset();
        ds.setValue("Pending", Math.max(1, pending));
        ds.setValue("Completed", Math.max(1, completed));
        ds.setValue("Verified", Math.max(1, verified));
        JFreeChart chart = ChartFactory.createPieChart("Status Distribution", ds, true, true, false);

        // Use clear contrasting colors
        try {
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setSectionPaint("Completed", new Color(46, 204, 113)); // green
            plot.setSectionPaint("Pending", new Color(241, 196, 15));   // amber
            plot.setSectionPaint("Verified", new Color(52, 152, 219));  // blue
            plot.setBackgroundPaint(UiTheme.PANEL_BG);
        } catch (Exception ignore) {
        }

        ChartPanel cp = new ChartPanel(chart);
        cp.setMouseWheelEnabled(true);

        main.add(left);
        main.add(cp);

        add(main, BorderLayout.CENTER);

        JLabel footer = new JLabel("Loaded.", SwingConstants.LEFT);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
        add(footer, BorderLayout.SOUTH);
    }

    private JComponent createStatusButton(String text, String status) {
        JButton b = new JButton(text);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setForeground(UiTheme.PRIMARY.darker());
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(UiTheme.NORMAL_FONT);
        b.addActionListener(e -> {
            // Post event to open tests filtered by status, and dashboard will show tests card
            EventBus.post("open.tests.status", status);
        });
        return b;
    }
}
