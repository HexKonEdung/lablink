package Main;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class Patient_List_Panel extends JPanel {

    private DefaultTableModel model;
    private JTable table;
    private JTextField txtSearch;
    private JComboBox<String> cmbSex;

    public Patient_List_Panel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(UiTheme.BG);
        txtSearch = new JTextField(18);
        cmbSex = new JComboBox<>(new String[]{"", "Male", "Female", "Other"});
        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> load());
        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(e -> openForm(null));
        top.add(new JLabel("Search:"));
        top.add(txtSearch);
        top.add(new JLabel("Sex:"));
        top.add(cmbSex);
        top.add(btnSearch);
        top.add(btnAdd);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"ID", "Name", "Sex", "DOB", "Contact", "Registered"}, 0) {
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

        load();
    }

    public void load() {
        model.setRowCount(0);
        List<Patient> list = PatientDAO.listAll((String) cmbSex.getSelectedItem(), txtSearch.getText().trim());
        for (Patient p : list) {
            model.addRow(new Object[]{p.patientId, p.name, p.sex, p.dateOfBirth, p.contactNumber, p.dateRegistered});
        }
    }

    private void openForm(Patient p) {
        Patient_Form_Panel f = new Patient_Form_Panel(p, this::load);
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), p == null ? "Add Patient" : "Edit Patient", true);
        d.getContentPane().add(f);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void editSelected() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select a patient");
            return;
        }
        int id = (int) model.getValueAt(r, 0);
        Patient p = PatientDAO.findById(id);
        openForm(p);
    }

    private void deleteSelected() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select a patient");
            return;
        }
        int id = (int) model.getValueAt(r, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete selected patient?") == JOptionPane.YES_OPTION) {
            PatientDAO.delete(id);
            load();
        }
    }

    private void viewSelected() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select a patient");
            return;
        }
        int id = (int) model.getValueAt(r, 0);
        Patient p = PatientDAO.findById(id);
        Patient_Report_Panel report = new Patient_Report_Panel(p);
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Patient Details", true);
        d.getContentPane().add(report);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }
}
