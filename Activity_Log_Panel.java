// Activity_Log_Panel.java
package Main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class Activity_Log_Panel extends JPanel {

    private DefaultTableModel model;
    private JTable table;
    private JTextField txtSearch;

    public Activity_Log_Panel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(UiTheme.BG);
        txtSearch = new JTextField(18);
        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> load());
        top.add(new JLabel("Search:"));
        top.add(txtSearch);
        top.add(btnSearch);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"Time", "User", "Action", "Target", "Target ID"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
        load();
    }

    public void load() {
        model.setRowCount(0);
        List<ActivityLogEntry> list = ActivityLogDAO.listAll(txtSearch.getText().trim());
        for (ActivityLogEntry e : list) {
            model.addRow(new Object[]{e.timestamp, e.user, e.action, e.targetTable, e.targetId, e.description});
        }
    }
}
