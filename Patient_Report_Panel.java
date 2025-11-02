// Patient_Report_Panel.java
package Main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Patient report panel: show "Registered By" using the resolved name if
 * available.
 */
public class Patient_Report_Panel extends JPanel {

    private Patient patient;
    private DefaultTableModel testsModel;
    private JTable testsTable;

    public Patient_Report_Panel(Patient p) {
        this.patient = p;
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);
        JPanel content = new JPanel(new GridLayout(0, 2));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.setBackground(UiTheme.PANEL_BG);

        content.add(new JLabel("Patient ID:"));
        content.add(new JLabel(String.valueOf(p.patientId)));
        content.add(new JLabel("Name:"));
        content.add(new JLabel(p.name));
        content.add(new JLabel("Sex:"));
        content.add(new JLabel(p.sex));
        content.add(new JLabel("DOB:"));
        content.add(new JLabel(p.dateOfBirth));
        content.add(new JLabel("Contact:"));
        content.add(new JLabel(p.contactNumber));
        content.add(new JLabel("Email:"));
        content.add(new JLabel(p.email));
        content.add(new JLabel("Blood Type:"));
        content.add(new JLabel(p.bloodType));
        content.add(new JLabel("Allergies:"));
        content.add(new JLabel(p.allergies));
        content.add(new JLabel("Existing Conditions:"));
        content.add(new JLabel(p.existingConditions));

        // Show Registered By (resolved name)
        content.add(new JLabel("Registered By:"));
        String reg = p.registeredBy;
        if ((reg == null || reg.isEmpty()) && p.registeredById > 0) {
            Account a = AccountDAO.findById(p.registeredById);
            reg = a != null ? (a.fullName != null && !a.fullName.isEmpty() ? a.fullName : a.username) : String.valueOf(p.registeredById);
        }
        content.add(new JLabel(reg != null ? reg : ""));

        add(content, BorderLayout.NORTH);

        // Tests table for this patient
        testsModel = new DefaultTableModel(new Object[]{"Test ID", "Test Name", "Category", "Status", "Date", "Technician"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        testsTable = new JTable(testsModel);
        add(new JScrollPane(testsTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh Tests");
        btnRefresh.addActionListener(e -> loadTests());
        JButton btnView = new JButton("View Selected Test");
        btnView.addActionListener(e -> {
            int r = testsTable.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Select a test.");
                return;
            }
            int id = (int) testsModel.getValueAt(r, 0);
            TestRecord t = TestRecordDAO.findById(id);
            Test_Record_Report_Panel report = new Test_Record_Report_Panel(t);
            JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Test Record", true);
            d.getContentPane().add(report);
            d.pack();
            d.setLocationRelativeTo(this);
            d.setVisible(true);
        });
        bottom.add(btnRefresh);
        bottom.add(btnView);
        add(bottom, BorderLayout.SOUTH);

        loadTests();

        EventBus.addListener("test.saved", (evt, payload) -> SwingUtilities.invokeLater(this::loadTests));
    }

    private void loadTests() {
        testsModel.setRowCount(0);
        List<TestRecord> list = TestRecordDAO.listAll("", String.valueOf(patient.patientId));
        for (TestRecord tr : list) {
            if (tr.patientId == patient.patientId) {
                testsModel.addRow(new Object[]{tr.testId, tr.testName, tr.category, tr.status, tr.dateConducted, tr.technician});
            }
        }
    }
}
