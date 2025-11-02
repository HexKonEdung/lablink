package Main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Test_Record_List_Panel now exposes setStatusAndLoad so external callers
 * (dashboard) can request the panel to show tests of a particular status. It
 * also listens to the EventBus "test.saved" to refresh when tests change.
 */
public class Test_Record_List_Panel extends JPanel {

    private DefaultTableModel model;
    private JTable table;
    private JTextField txtSearch;
    private JComboBox<String> cmbStatus;

    public Test_Record_List_Panel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(UiTheme.BG);
        txtSearch = new JTextField(18);
        cmbStatus = new JComboBox<>(new String[]{"", "Pending", "Completed", "Verified"});
        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> load());
        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(e -> openForm(null));
        top.add(new JLabel("Search:"));
        top.add(txtSearch);
        top.add(new JLabel("Status:"));
        top.add(cmbStatus);
        top.add(btnSearch);
        top.add(btnAdd);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"Test ID", "Patient", "Test Name", "Category", "Status", "Date", "Technician"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnEdit = new JButton("Edit");
        btnEdit.addActionListener(e -> editSelected());
        JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(e -> deleteSelected());
        JButton btnView = new JButton("View");
        btnView.addActionListener(e -> viewSelected());
        bottom.add(btnEdit);
        bottom.add(btnDelete);
        bottom.add(btnView);
        add(bottom, BorderLayout.SOUTH);

        // Refresh when tests change elsewhere
        EventBus.addListener("test.saved", (evt, payload) -> SwingUtilities.invokeLater(this::load));

        load();
    }

    public void load() {
        model.setRowCount(0);
        List<TestRecord> list = TestRecordDAO.listAll((String) cmbStatus.getSelectedItem(), txtSearch.getText().trim());
        for (TestRecord tr : list) {
            // lookup patient name
            Patient p = PatientDAO.findById(tr.patientId);
            String patientDisplay = p != null ? p.name + " (ID:" + p.patientId + ")" : String.valueOf(tr.patientId);
            model.addRow(new Object[]{tr.testId, patientDisplay, tr.testName, tr.category, tr.status, tr.dateConducted, tr.technician});
        }
    }

    private void openForm(TestRecord t) {
        Test_Record_Form_Panel f = new Test_Record_Form_Panel(t, this::load);
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), t == null ? "Add Test" : "Edit Test", true);
        d.getContentPane().add(f);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void editSelected() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select a test");
            return;
        }
        int id = (int) model.getValueAt(r, 0);
        TestRecord t = TestRecordDAO.findById(id);
        openForm(t);
    }

    private void deleteSelected() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select test");
            return;
        }
        int id = (int) model.getValueAt(r, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete selected test?") == JOptionPane.YES_OPTION) {
            TestRecordDAO.delete(id);
            EventBus.post("test.saved", id); // notify listeners that data changed
            load();
        }
    }

    private void viewSelected() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select a test.");
            return;
        }
        int id = (int) model.getValueAt(r, 0);
        TestRecord t = TestRecordDAO.findById(id);
        Test_Record_Report_Panel report = new Test_Record_Report_Panel(t);
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Test Record", true);
        d.getContentPane().add(report);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    /**
     * External caller can request this panel to set status filter and reload.
     */
    public void setStatusAndLoad(String status) {
        if (status == null) {
            return;
        }
        cmbStatus.setSelectedItem(status);
        load();
    }
}
