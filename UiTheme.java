package Main;

// UiTheme.java
// Centralized UI theme, helper factory methods and look-and-feel hints.
// Extended with helper styling methods used by many UI panels.
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Centralized UI theme and small helper factory methods used across the app.
 */
public class UiTheme {

    // Fonts
    public static final String FONT_FAMILY = "Segoe UI";
    public static final Font HERO_FONT = new Font(FONT_FAMILY, Font.BOLD, 30);
    public static final Font HEADER_FONT = new Font(FONT_FAMILY, Font.BOLD, 20);
    public static final Font NORMAL_FONT = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font SMALL_FONT = new Font(FONT_FAMILY, Font.PLAIN, 11);
    public static final Font FONT_BODY = NORMAL_FONT;

    // Colors
    public static final Color PRIMARY = new Color(0x36, 0xF9, 0x9F);
    public static final Color ACCENT = new Color(0x27, 0xDE, 0x8A);
    public static final Color HEADER_TEXT = Color.WHITE;
    public static final Color BG = new Color(250, 250, 250);
    public static final Color PANEL_BG = Color.WHITE;
    public static final Color TEXT_COLOR = new Color(48, 48, 48);
    public static final Color MUTED = new Color(140, 140, 140);
    public static final Color BORDER_COLOR = new Color(220, 220, 220);

    // Graph palette - exposed so dashboard/chart code can use it
    public static final Color[] GRAPH_COLORS = new Color[]{
        new Color(0x36, 0xF9, 0x9F), // teal/green
        new Color(0x27, 0xDE, 0x8A), // accent green
        new Color(156, 39, 176), // purple
        new Color(33, 150, 243), // blue
        new Color(255, 193, 7), // amber
        new Color(244, 67, 54), // red
        new Color(76, 175, 80), // green
        new Color(121, 85, 72) // brown
    };

    // Sizes
    public static final int HEADER_HEIGHT = 62;
    public static final int SIDE_WIDTH = 260;
    public static final int CONTROL_HEIGHT = 34;
    public static final int ICON_SIZE = 20;

    // Logo path
    public static final String LOGO_PATH = "data/logo.png";

    public static final EmptyBorder PANEL_PADDING = new EmptyBorder(12, 12, 12, 12);

    public static void applyLookAndFeelHints() {
        try {
            UIManager.put("Button.font", NORMAL_FONT);
            UIManager.put("Label.font", NORMAL_FONT);
            UIManager.put("TextField.font", NORMAL_FONT);
            UIManager.put("TextArea.font", NORMAL_FONT);
            UIManager.put("ComboBox.font", NORMAL_FONT);
            UIManager.put("Table.font", NORMAL_FONT);
            UIManager.put("Table.rowHeight", 26);
            UIManager.put("Table.selectionBackground", ACCENT.darker());
            UIManager.put("Table.selectionForeground", HEADER_TEXT);
            UIManager.put("Panel.background", BG);
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.border", new BorderUIResource.LineBorderUIResource(BORDER_COLOR));
            UIManager.put("TextArea.border", new BorderUIResource.LineBorderUIResource(BORDER_COLOR));
            UIManager.put("ScrollPane.border", new BorderUIResource.LineBorderUIResource(BORDER_COLOR));
        } catch (Exception ignore) {
        }
    }

    // Primary pill button
    public static void stylePrimaryButton(JButton b) {
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFont(NORMAL_FONT.deriveFont(Font.BOLD));
        b.setFocusPainted(false);
        b.setBorder(createPillBorder(PRIMARY.darker()));
        b.setPreferredSize(new Dimension(140, CONTROL_HEIGHT));
        b.setOpaque(true);
    }

    // Secondary (outline) button
    public static void styleSecondaryButton(JButton b) {
        b.setBackground(PANEL_BG);
        b.setForeground(PRIMARY.darker());
        b.setFont(NORMAL_FONT);
        b.setFocusPainted(false);
        b.setBorder(createPillBorder(BORDER_COLOR));
        b.setPreferredSize(new Dimension(120, CONTROL_HEIGHT));
        b.setOpaque(true);
    }

    public static void styleDangerButton(JButton b) {
        b.setBackground(new Color(220, 60, 60));
        b.setForeground(Color.WHITE);
        b.setFont(NORMAL_FONT.deriveFont(Font.BOLD));
        b.setFocusPainted(false);
        b.setBorder(createPillBorder(new Color(170, 40, 40)));
        b.setPreferredSize(new Dimension(140, CONTROL_HEIGHT));
        b.setOpaque(true);
    }

    public static Border createPillBorder(Color c) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(c, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        );
    }

    // Style for page headings
    public static void stylePageHeading(JLabel l) {
        l.setFont(HEADER_FONT);
        l.setForeground(TEXT_COLOR);
    }

    // Form label styling
    public static void styleFormLabel(JComponent c) {
        c.setFont(NORMAL_FONT);
        c.setForeground(TEXT_COLOR);
    }

    // Form input styling (TextField/JScrollPane etc.)
    public static void styleFormInput(JComponent c) {
        c.setFont(NORMAL_FONT);
        if (c instanceof JTextField) {
            ((JTextField) c).setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(6, 6, 6, 6)));
        } else if (c instanceof JScrollPane) {
            ((JScrollPane) c).setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        }
    }

    // Small helper to style JTable + scroll pane
    public static void styleTable(JTable table, JScrollPane scrollPane) {
        table.setFont(NORMAL_FONT);
        table.setRowHeight(26);
        table.setGridColor(BORDER_COLOR);
        JTableHeader th = table.getTableHeader();
        if (th != null) {
            th.setFont(NORMAL_FONT.deriveFont(Font.BOLD));
        }
        if (scrollPane != null) {
            scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        }
    }

    // Create a header panel with gradient
    public static JPanel createHeaderPanel(String titleText, JComponent rightComponent) {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY, w, 0, ACCENT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, HEADER_HEIGHT));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        left.setOpaque(false);
        JLabel lblTitle = new JLabel(titleText);
        lblTitle.setFont(HEADER_FONT);
        lblTitle.setForeground(HEADER_TEXT);

        try {
            java.io.File logo = new java.io.File(LOGO_PATH);
            if (logo.exists()) {
                ImageIcon ic = new ImageIcon(logo.getAbsolutePath());
                Image scaled = ic.getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH);
                JLabel logoLbl = new JLabel(new ImageIcon(scaled));
                left.add(logoLbl);
            }
        } catch (Exception ignore) {
        }

        left.add(lblTitle);
        header.add(left, BorderLayout.WEST);

        if (rightComponent != null) {
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
            right.setOpaque(false);
            right.add(rightComponent);
            header.add(right, BorderLayout.EAST);
        }

        return header;
    }

    public static JTextField createSearchField(int columns) {
        JTextField f = new JTextField(columns);
        f.setFont(NORMAL_FONT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(6, 8, 6, 8)
        ));
        return f;
    }

    public static JLabel createAvatarPlaceholder(int size) {
        JLabel lbl = new JLabel();
        lbl.setPreferredSize(new Dimension(size, size));
        lbl.setOpaque(true);
        lbl.setBackground(Color.WHITE);
        lbl.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 120)));
        return lbl;
    }
}
