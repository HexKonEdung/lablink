package Main;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.plot.PiePlot;

/**
 * Dashboard welcome panel showing two pie charts: Tests by Status and Tests by
 * Category. Status slices get fixed, distinct colors. Category slices get
 * deterministic, high-contrast colors generated from their names (HSB-based) so
 * each category is visually distinct even when there are many categories.
 */
public class Dashboard_Welcome_Panel extends JPanel {

    public Dashboard_Welcome_Panel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UiTheme.BG);
        JLabel title = new JLabel("Dashboard", SwingConstants.LEFT);
        title.setFont(UiTheme.HEADER_FONT.deriveFont(22f));
        title.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        top.add(title, BorderLayout.NORTH);

        JPanel charts = new JPanel(new GridLayout(1, 2, 16, 0));
        charts.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        charts.setOpaque(false);

        // Create datasets
        DefaultPieDataset statusDataset = new DefaultPieDataset();
        DefaultPieDataset categoryDataset = new DefaultPieDataset();

        // Populate dataset from DB (counts)
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement()) {
            // Status counts
            try (ResultSet rs = s.executeQuery("SELECT status, COUNT(*) AS c FROM tests GROUP BY status")) {
                int total = 0;
                while (rs.next()) {
                    String status = rs.getString("status");
                    int count = rs.getInt("c");
                    statusDataset.setValue(status == null ? "Unknown" : status, count);
                    total += count;
                }
                if (total == 0) {
                    // empty sample to show graph
                    statusDataset.setValue("Pending", 1);
                    statusDataset.setValue("Completed", 1);
                    statusDataset.setValue("Verified", 1);
                }
            }

            // Category counts
            try (ResultSet rs = s.executeQuery("SELECT COALESCE(category,'Uncategorized') AS cat, COUNT(*) AS c FROM tests GROUP BY COALESCE(category,'Uncategorized')")) {
                int total = 0;
                while (rs.next()) {
                    String cat = rs.getString("cat");
                    int count = rs.getInt("c");
                    categoryDataset.setValue(cat, count);
                    total += count;
                }
                if (total == 0) {
                    categoryDataset.setValue("Hematology", 2);
                    categoryDataset.setValue("Serology", 1);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // Fallback dummy values
            statusDataset.setValue("Pending", 1);
            statusDataset.setValue("Completed", 1);
            statusDataset.setValue("Verified", 1);
            categoryDataset.setValue("Hematology", 2);
            categoryDataset.setValue("Serology", 1);
        }

        // Create charts
        JFreeChart statusChart = ChartFactory.createPieChart("Tests by Status", statusDataset, true, true, false);
        JFreeChart categoryChart = ChartFactory.createPieChart("Tests by Category", categoryDataset, true, true, false);

        // Apply custom colors for status chart (explicit mapping)
        try {
            PiePlot statusPlot = (PiePlot) statusChart.getPlot();
            // Use distinct, contrasting colors
            statusPlot.setSectionPaint("Completed", new Color(46, 204, 113)); // green
            statusPlot.setSectionPaint("Pending", new Color(241, 196, 15));   // amber/orange
            statusPlot.setSectionPaint("Verified", new Color(52, 152, 219));  // blue
            statusPlot.setSectionPaint("Unknown", new Color(149, 165, 166));  // gray fallback

            // Any other statuses: assign colors by hashing key to HSB to ensure uniqueness
            int sidx = 0;
            for (Iterator<?> it = statusDataset.getKeys().iterator(); it.hasNext();) {
                Object key = it.next();
                String k = key == null ? "Unknown" : key.toString();
                if (!"Completed".equals(k) && !"Pending".equals(k) && !"Verified".equals(k) && !"Unknown".equals(k)) {
                    statusPlot.setSectionPaint(k, colorFromString(k));
                    sidx++;
                }
            }
        } catch (Exception ignore) {
        }

        // Apply distinct colors per category using deterministic color generator
        try {
            PiePlot catPlot = (PiePlot) categoryChart.getPlot();
            List<?> keys = categoryDataset.getKeys();
            for (int i = 0; i < keys.size(); i++) {
                Object key = keys.get(i);
                String k = key == null ? "Uncategorized" : key.toString();
                catPlot.setSectionPaint(k, colorFromString(k)); // deterministic HSB color per name
            }
        } catch (Exception ignore) {
        }

        ChartPanel cp1 = new ChartPanel(statusChart);
        cp1.setMouseWheelEnabled(true);
        ChartPanel cp2 = new ChartPanel(categoryChart);
        cp2.setMouseWheelEnabled(true);

        charts.add(cp1);
        charts.add(cp2);

        add(top, BorderLayout.NORTH);
        add(charts, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JLabel hint = new JLabel("Dashboard data loaded.", SwingConstants.LEFT);
        hint.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
        bottom.add(hint, BorderLayout.WEST);
        add(bottom, BorderLayout.SOUTH);
    }

    /**
     * Deterministic color generator from string. Produces high-contrast HSB
     * colors based on the hash of the input. Saturation/brightness chosen for
     * vivid colors.
     */
    private static Color colorFromString(String s) {
        if (s == null) {
            s = "";
        }
        int h = Math.abs(s.hashCode());
        float hue = (h % 360) / 360f;                 // 0..1
        float saturation = 0.62f + ((h / 360) % 3) * 0.06f; // vary a little
        float brightness = 0.88f;
        return Color.getHSBColor(hue, Math.min(1f, saturation), brightness);
    }
}
