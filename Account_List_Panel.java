// Account_List_Panel.java
// Simplified accounts UI
package Main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class Account_List_Panel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JComboBox<String> cmbRole;

    public Account_List_Panel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(UiTheme.BG);
        txtSearch = new JTextField(18);
        cmbRole = new JComboBox<>(new String[]{"", "Admin", "Technician"});
        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> load());
        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(e -> openForm(null));
        top.add(new JLabel("Search:"));
        top.add(txtSearch);
        top.add(new JLabel("Role:"));
        top.add(cmbRole);
        top.add(btnSearch);
        top.add(btnAdd);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"ID", "Name", "Username", "Role", "Contact", "Email"}, 0) {
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
        bottom.add(btnEdit);
        bottom.add(btnDelete);
        add(bottom, BorderLayout.SOUTH);

        load();
    }

    public void load() {
        model.setRowCount(0);
        List<Account> accounts = AccountDAO.listAll((String) cmbRole.getSelectedItem(), txtSearch.getText().trim());
        for (Account a : accounts) {
            model.addRow(new Object[]{a.accountId, a.fullName, a.username, a.role, a.contactNumber, a.email});
        }
    }

    private void openForm(Account a) {
        Account_Form_Panel f = new Account_Form_Panel(a, this::load);
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Account", true);
        d.getContentPane().add(f);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void editSelected() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select account");
            return;
        }
        int id = (int) model.getValueAt(r, 0);
        Account a = AccountDAO.findById(id);
        openForm(a);
    }

    private void deleteSelected() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select account");
            return;
        }
        int id = (int) model.getValueAt(r, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete selected account?") == JOptionPane.YES_OPTION) {
            AccountDAO.delete(id);
            load();
        }
    }
}
