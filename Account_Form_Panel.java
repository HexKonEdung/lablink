// Account_Form_Panel.java
// Add/Edit account form with basic validation (username required, unique check, etc.)
package Main;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

public class Account_Form_Panel extends JPanel {

    private JTextField txtFullName, txtUsername, txtEmail, txtContact;
    private JPasswordField pwdPass, pwdConfirm;
    private JComboBox<String> cmbSex, cmbRole;
    private Account account;
    private Runnable onSaved;

    public Account_Form_Panel(Account a, Runnable onSaved) {
        this.account = a != null ? a : new Account();
        this.onSaved = onSaved;
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UiTheme.PANEL_BG);
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Full Name"), gbc);
        gbc.gridx = 1;
        txtFullName = new JTextField(account.fullName != null ? account.fullName : "", 20);
        form.add(txtFullName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Username"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(account.username != null ? account.username : "", 20);
        form.add(txtUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(new JLabel("Sex"), gbc);
        gbc.gridx = 1;
        cmbSex = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        if (account.sex != null) {
            cmbSex.setSelectedItem(account.sex);
        }
        form.add(cmbSex, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        form.add(new JLabel("Role"), gbc);
        gbc.gridx = 1;
        cmbRole = new JComboBox<>(new String[]{"Admin", "Technician"});
        if (account.role != null) {
            cmbRole.setSelectedItem(account.role);
        }
        form.add(cmbRole, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        form.add(new JLabel("Email"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(account.email != null ? account.email : "", 20);
        form.add(txtEmail, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        form.add(new JLabel("Contact"), gbc);
        gbc.gridx = 1;
        txtContact = new JTextField(account.contactNumber != null ? account.contactNumber : "", 20);
        form.add(txtContact, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        form.add(new JLabel("Password"), gbc);
        gbc.gridx = 1;
        pwdPass = new JPasswordField(20);
        form.add(pwdPass, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        form.add(new JLabel("Confirm Password"), gbc);
        gbc.gridx = 1;
        pwdConfirm = new JPasswordField(20);
        form.add(pwdConfirm, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        JButton btnSave = new JButton("Save");
        btnSave.setBackground(UiTheme.PRIMARY);
        btnSave.setForeground(Color.white);
        btnSave.addActionListener(e -> {
            if (save()) {
                JOptionPane.showMessageDialog(this, "Saved");
                if (onSaved != null) {
                    onSaved.run();
                }
                SwingUtilities.getWindowAncestor(this).dispose();
            }
        });
        form.add(btnSave, gbc);

        add(form, BorderLayout.CENTER);
    }

    private boolean save() {
        String pass = new String(pwdPass.getPassword()).trim();
        String confirm = new String(pwdConfirm.getPassword()).trim();

        // Basic validation
        String username = txtUsername.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username is required.");
            return false;
        }

        // If creating new account, require password
        if (account.accountId == 0 && pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password is required for new account.");
            return false;
        }

        if (!pass.isEmpty() && !pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match");
            return false;
        }

        // Simple email validation (if provided)
        String email = txtEmail.getText().trim();
        if (!email.isEmpty()) {
            if (!isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Invalid email address.");
                return false;
            }
        }

        // If username changed or it's a new account, ensure uniqueness
        if ((account.username == null || !account.username.equalsIgnoreCase(username)) && AccountDAO.isUsernameTaken(username)) {
            JOptionPane.showMessageDialog(this, "Username already taken.");
            return false;
        }

        account.fullName = txtFullName.getText().trim();
        account.username = username;
        account.sex = (String) cmbSex.getSelectedItem();
        account.role = (String) cmbRole.getSelectedItem();
        account.email = email;
        account.contactNumber = txtContact.getText().trim();

        String toSetPass = pass.isEmpty() ? null : pass;
        boolean ok = AccountDAO.save(account, toSetPass);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Save failed. See logs for details.");
        }
        return ok;
    }

    private boolean isValidEmail(String email) {
        // Very small/relaxed regex
        Pattern p = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
        return p.matcher(email).matches();
    }
}
