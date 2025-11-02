// ProfilePanel.java
// Simple profile editor + JFreeChart summary; image upload stored via ImageUtil.
package Main;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class ProfilePanel extends JPanel {

    private JTextField txtFullName, txtUsername, txtEmail, txtContact;
    private JPasswordField pwdPass, pwdConfirm;
    private JComboBox<String> cmbSex;
    private JLabel lblProfileImage;
    private String imagePath;
    private JPanel chartHolder;

    public ProfilePanel() {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);

        Account me = Session.getCurrentUser();

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
        txtFullName = new JTextField(me != null ? me.fullName : "", 20);
        form.add(txtFullName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Username"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(me != null ? me.username : "", 20);
        form.add(txtUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(new JLabel("Sex"), gbc);
        gbc.gridx = 1;
        cmbSex = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        if (me != null) {
            cmbSex.setSelectedItem(me.sex);
        }
        form.add(cmbSex, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        form.add(new JLabel("Email"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(me != null ? me.email : "", 20);
        form.add(txtEmail, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        form.add(new JLabel("Contact"), gbc);
        gbc.gridx = 1;
        txtContact = new JTextField(me != null ? me.contactNumber : "", 20);
        form.add(txtContact, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        form.add(new JLabel("New Password"), gbc);
        gbc.gridx = 1;
        pwdPass = new JPasswordField(20);
        form.add(pwdPass, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        form.add(new JLabel("Confirm Password"), gbc);
        gbc.gridx = 1;
        pwdConfirm = new JPasswordField(20);
        form.add(pwdConfirm, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        form.add(new JLabel("Profile Image"), gbc);
        gbc.gridx = 1;
        JPanel imgRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        imgRow.setOpaque(false);
        lblProfileImage = new JLabel();
        lblProfileImage.setPreferredSize(new Dimension(96, 96));
        lblProfileImage.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        if (me != null && me.profilePicture != null) {
            setProfilePreview(me.profilePicture);
        }
        JButton btnUpload = new JButton("Upload");
        btnUpload.addActionListener(e -> chooseProfileImage());
        imgRow.add(lblProfileImage);
        imgRow.add(btnUpload);
        form.add(imgRow, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        JButton btnSave = new JButton("Save Changes");
        btnSave.setBackground(UiTheme.PRIMARY);
        btnSave.setForeground(Color.white);
        btnSave.addActionListener(e -> saveProfile());
        form.add(btnSave, gbc);

        add(form, BorderLayout.WEST);

        chartHolder = new JPanel(new BorderLayout());
        chartHolder.setPreferredSize(new Dimension(420, 280));
        chartHolder.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(chartHolder, BorderLayout.CENTER);

        refreshChart();
    }

    private void setProfilePreview(String path) {
        try {
            ImageIcon ic = new ImageIcon(path);
            Image img = ic.getImage().getScaledInstance(96, 96, Image.SCALE_SMOOTH);
            lblProfileImage.setIcon(new ImageIcon(img));
            imagePath = path;
        } catch (Exception ex) {
            lblProfileImage.setIcon(null);
        }
    }

    private void chooseProfileImage() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "png", "jpg", "jpeg", "gif"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                imagePath = ImageUtil.storeImage(f);
                setProfilePreview(imagePath);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Image save failed: " + ex.getMessage());
            }
        }
    }

    private void saveProfile() {
        Account me = Session.getCurrentUser();
        if (me == null) {
            return;
        }
        String pass = new String(pwdPass.getPassword());
        String confirm = new String(pwdConfirm.getPassword());
        if (!pass.isEmpty() && !pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match");
            return;
        }
        me.fullName = txtFullName.getText().trim();
        me.username = txtUsername.getText().trim();
        me.sex = (String) cmbSex.getSelectedItem();
        me.email = txtEmail.getText().trim();
        me.contactNumber = txtContact.getText().trim();
        me.profilePicture = imagePath != null ? imagePath : me.profilePicture;
        AccountDAO.save(me, pass.isEmpty() ? null : pass);
        JOptionPane.showMessageDialog(this, "Profile updated.");
        refreshChart();
    }

    private void refreshChart() {
        try (Connection c = DBConnection.getConnection()) {
            int patients = 0, pending = 0, completed = 0;
            try (Statement s = c.createStatement()) {
                try (ResultSet rs = s.executeQuery("SELECT COUNT(*) AS c FROM patients")) {
                    if (rs.next()) {
                        patients = rs.getInt("c");
                    }
                }
                try (ResultSet rs = s.executeQuery("SELECT COUNT(*) AS c FROM tests WHERE status = 'Pending'")) {
                    if (rs.next()) {
                        pending = rs.getInt("c");
                    }
                }
                try (ResultSet rs = s.executeQuery("SELECT COUNT(*) AS c FROM tests WHERE status = 'Completed'")) {
                    if (rs.next()) {
                        completed = rs.getInt("c");
                    }
                }
            }
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(patients, "Count", "Patients");
            dataset.addValue(pending, "Count", "Pending Tests");
            dataset.addValue(completed, "Count", "Completed Tests");
            JFreeChart chart = ChartFactory.createBarChart("System Summary", "Category", "Count", dataset);
            ChartPanel cp = new ChartPanel(chart);
            chartHolder.removeAll();
            chartHolder.add(cp, BorderLayout.CENTER);
            chartHolder.revalidate();
            chartHolder.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
