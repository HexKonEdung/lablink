package Main;

// Test_Record_Form_Panel.java
// Add/Edit test record with parameter table
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.sql.*;

public class Test_Record_Form_Panel extends JPanel {

    private JTextField txtTestName, txtDate;
    private JComboBox<String> cmbCategory, cmbSample, cmbStatus;
    private JComboBox<Integer> cmbPatient;
    private JTable paramTable;
    private DefaultTableModel paramModel;
    private TestRecord record;
    private Runnable onSaved;

    public Test_Record_Form_Panel(TestRecord t, Runnable onSaved) {
        this.record = t != null ? t : new TestRecord();
        this.onSaved = onSaved;
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);

        JPanel top = new JPanel(new GridBagLayout());
        top.setBackground(UiTheme.PANEL_BG);
        top.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        top.add(new JLabel("Patient"), gbc);
        gbc.gridx = 1;
        // show patient names in the combo: build model with id values but display names
        List<Patient> patients = PatientDAO.listAll(null, null);
        DefaultComboBoxModel<Integer> patientModel = new DefaultComboBoxModel<>();
        for (Patient p : patients) {
            patientModel.addElement(p.patientId);
        }
        cmbPatient = new JComboBox<>(patientModel);
        // Render patient name instead of number
        cmbPatient.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Integer) {
                    Patient p = PatientDAO.findById((Integer) value);
                    setText(p != null ? p.name + " (ID:" + p.patientId + ")" : "ID: " + value);
                }
                return this;
            }
        });
        if (record.patientId > 0) {
            cmbPatient.setSelectedItem(record.patientId);
        }
        top.add(cmbPatient, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        top.add(new JLabel("Test Name"), gbc);
        gbc.gridx = 1;
        txtTestName = new JTextField(record.testName != null ? record.testName : "", 20);
        top.add(txtTestName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        top.add(new JLabel("Category"), gbc);
        gbc.gridx = 1;
        cmbCategory = new JComboBox<>(new String[]{"Hematology", "Clinical Chemistry", "Urinalysis", "Serology", "Microbiology", "Molecular Diagnostics", "Toxicology", "Histopathology"});
        if (record.category != null) {
            cmbCategory.setSelectedItem(record.category);
        }
        top.add(cmbCategory, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        top.add(new JLabel("Sample Type"), gbc);
        gbc.gridx = 1;
        cmbSample = new JComboBox<>(new String[]{"Blood", "Urine", "Stool", "Sputum", "Swab", "Other"});
        if (record.sampleType != null) {
            cmbSample.setSelectedItem(record.sampleType);
        }
        top.add(cmbSample, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        top.add(new JLabel("Date Conducted"), gbc);
        gbc.gridx = 1;
        txtDate = new JTextField(record.dateConducted != null ? record.dateConducted : java.time.LocalDate.now().toString(), 20);
        top.add(txtDate, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        top.add(new JLabel("Technician"), gbc);
        gbc.gridx = 1;
        JTextField txtTech = new JTextField(record.technician != null ? record.technician : (Session.getCurrentUser() != null ? Session.getCurrentUser().username : ""), 20);
        txtTech.setEditable(false);
        top.add(txtTech, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        top.add(new JLabel("Status"), gbc);
        gbc.gridx = 1;
        cmbStatus = new JComboBox<>(new String[]{"Pending", "Completed", "Verified"});
        if (record.status != null) {
            cmbStatus.setSelectedItem(record.status);
        }
        top.add(cmbStatus, gbc);

        add(top, BorderLayout.NORTH);

        paramModel = new DefaultTableModel(new Object[]{"Parameter", "Result", "Range", "Units", "Interpretation"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return true;
            }
        };
        paramTable = new JTable(paramModel);
        add(new JScrollPane(paramTable), BorderLayout.CENTER);
        
        if (record.testId > 0) {
        loadParametersFromDB(record.testId);
        }

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddParam = new JButton("Add Parameter");
        btnAddParam.addActionListener(e -> paramModel.addRow(new Object[]{"", "", "", "", ""}));
        JButton btnRemoveParam = new JButton("Remove Parameter");
        btnRemoveParam.addActionListener(e -> {
            int r = paramTable.getSelectedRow();
            if (r >= 0) {
                paramModel.removeRow(r);
            }
        });
        JButton btnSave = new JButton("Save Test");
        btnSave.addActionListener(e -> save(txtTech.getText()));
        bottom.add(btnAddParam);
        bottom.add(btnRemoveParam);
        bottom.add(btnSave);
        add(bottom, BorderLayout.SOUTH);
    }
    
    private void loadParametersFromDB(int testId) {
    if (testId <= 0) return;
    try (Connection c = DBConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(
                 "SELECT parameter_name, result_value, normal_range, units, interpretation FROM parameters WHERE test_id = ?")) {
        ps.setInt(1, testId);
        try (ResultSet rs = ps.executeQuery()) {
            paramModel.setRowCount(0); // clear table first
            while (rs.next()) {
                paramModel.addRow(new Object[]{
                    rs.getString("parameter_name"),
                    rs.getString("result_value"),
                    rs.getString("normal_range"),
                    rs.getString("units"),
                    rs.getString("interpretation")
                });
            }
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}

    private void save(String technician) {
        record.patientId = (Integer) cmbPatient.getSelectedItem();
        record.testName = txtTestName.getText().trim();
        record.category = (String) cmbCategory.getSelectedItem();
        record.sampleType = (String) cmbSample.getSelectedItem();
        record.dateConducted = txtDate.getText().trim();
        record.technician = technician;
        record.status = (String) cmbStatus.getSelectedItem();
        int id = TestRecordDAO.save(record);
        if (id > 0) {
            try (Connection c = DBConnection.getConnection()) {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM parameters WHERE test_id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                String ins = "INSERT INTO parameters (test_id, parameter_name, result_value, normal_range, units, interpretation) VALUES (?,?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(ins)) {
                    for (int i = 0; i < paramModel.getRowCount(); i++) {
                        ps.setInt(1, id);
                        ps.setString(2, String.valueOf(paramModel.getValueAt(i, 0)));
                        ps.setString(3, String.valueOf(paramModel.getValueAt(i, 1)));
                        ps.setString(4, String.valueOf(paramModel.getValueAt(i, 2)));
                        ps.setString(5, String.valueOf(paramModel.getValueAt(i, 3)));
                        ps.setString(6, String.valueOf(paramModel.getValueAt(i, 4)));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ActivityLogDAO.log(Session.getCurrentUser() != null ? Session.getCurrentUser().username : "system", "Saved", "tests", id, "Test saved with parameters.");

            // new: broadcast an event so dashboards/panels can refresh 
            EventBus.post("test.saved", id);

            JOptionPane.showMessageDialog(this, "Saved");
            if (onSaved != null) {
                onSaved.run();
            }
            SwingUtilities.getWindowAncestor(this).dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Save failed.");
        }
    }
}
