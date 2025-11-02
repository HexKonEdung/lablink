package Main;

// Patient_Form_Panel.java
// Full, self-contained Patient form with (Patient p, Runnable onSaved) constructor.
// Includes image upload, date chooser, and save() that sets registeredById.
import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.toedter.calendar.JDateChooser;

public class Patient_Form_Panel extends JPanel {

    private JTextField txtName, txtContact, txtEmail, txtAddress, txtAllergies, txtConditions, txtEmergency;
    private JComboBox<String> cmbSex, cmbBlood;
    private Patient patient;
    private Runnable onSaved;
    private JDateChooser dateChooser;
    private JLabel lblImagePreview;
    private String imagePath;

    public Patient_Form_Panel(Patient p, Runnable onSaved) {
        this.patient = p != null ? p : new Patient();
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
        form.add(new JLabel("Name"), gbc);
        gbc.gridx = 1;
        txtName = new JTextField(patient.name != null ? patient.name : "", 20);
        form.add(txtName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Sex"), gbc);
        gbc.gridx = 1;
        cmbSex = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        if (patient.sex != null) {
            cmbSex.setSelectedItem(patient.sex);
        }
        form.add(cmbSex, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(new JLabel("Date of Birth"), gbc);
        gbc.gridx = 1;
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        if (patient.dateOfBirth != null && !patient.dateOfBirth.isEmpty()) {
            try {
                dateChooser.setDate(java.sql.Date.valueOf(patient.dateOfBirth));
            } catch (Exception ignore) {
            }
        }
        form.add(dateChooser, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        form.add(new JLabel("Contact"), gbc);
        gbc.gridx = 1;
        txtContact = new JTextField(patient.contactNumber != null ? patient.contactNumber : "", 20);
        form.add(txtContact, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        form.add(new JLabel("Email"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(patient.email != null ? patient.email : "", 20);
        form.add(txtEmail, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        form.add(new JLabel("Address"), gbc);
        gbc.gridx = 1;
        txtAddress = new JTextField(patient.address != null ? patient.address : "", 20);
        form.add(txtAddress, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        form.add(new JLabel("Blood Type"), gbc);
        gbc.gridx = 1;
        cmbBlood = new JComboBox<>(new String[]{"", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Rh-null", "Bombay (hh)"});
        if (patient.bloodType != null) {
            cmbBlood.setSelectedItem(patient.bloodType);
        }
        form.add(cmbBlood, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        form.add(new JLabel("Allergies"), gbc);
        gbc.gridx = 1;
        txtAllergies = new JTextField(patient.allergies != null ? patient.allergies : "", 20);
        form.add(txtAllergies, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        form.add(new JLabel("Existing Conditions"), gbc);
        gbc.gridx = 1;
        txtConditions = new JTextField(patient.existingConditions != null ? patient.existingConditions : "", 20);
        form.add(txtConditions, gbc);

        gbc.gridx = 0;
        gbc.gridy = 9;
        form.add(new JLabel("Emergency Contact"), gbc);
        gbc.gridx = 1;
        txtEmergency = new JTextField(patient.emergencyContact != null ? patient.emergencyContact : "", 20);
        form.add(txtEmergency, gbc);

        gbc.gridx = 0;
        gbc.gridy = 10;
        form.add(new JLabel("Profile Image"), gbc);
        gbc.gridx = 1;
        JPanel imgRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        imgRow.setOpaque(false);
        lblImagePreview = new JLabel();
        lblImagePreview.setPreferredSize(new Dimension(96, 96));
        lblImagePreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        if (patient.profilePicture != null && !patient.profilePicture.isEmpty()) {
            setImagePreview(patient.profilePicture);
        }
        JButton btnUpload = new JButton("Upload");
        btnUpload.addActionListener(e -> chooseImage());
        imgRow.add(lblImagePreview);
        imgRow.add(btnUpload);
        form.add(imgRow, gbc);

        gbc.gridx = 0;
        gbc.gridy = 11;
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

    private void chooseImage() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Image files", "png", "jpg", "jpeg", "gif"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                imagePath = ImageUtil.storeImage(f);
                setImagePreview(imagePath);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Image save failed: " + ex.getMessage());
            }
        }
    }

    private void setImagePreview(String path) {
        try {
            ImageIcon ic = new ImageIcon(path);
            Image img = ic.getImage().getScaledInstance(96, 96, Image.SCALE_SMOOTH);
            lblImagePreview.setIcon(new ImageIcon(img));
        } catch (Exception ex) {
            lblImagePreview.setIcon(null);
        }
    }

    private boolean save() {
        patient.name = txtName.getText().trim();
        patient.sex = (String) cmbSex.getSelectedItem();
        java.util.Date dt = dateChooser.getDate();
        if (dt != null) {
            patient.dateOfBirth = new java.sql.Date(dt.getTime()).toLocalDate().toString();
        }
        patient.contactNumber = txtContact.getText().trim();
        patient.email = txtEmail.getText().trim();
        patient.address = txtAddress.getText().trim();
        patient.bloodType = (String) cmbBlood.getSelectedItem();
        patient.allergies = txtAllergies.getText().trim();
        patient.existingConditions = txtConditions.getText().trim();
        patient.emergencyContact = txtEmergency.getText().trim();
        patient.profilePicture = imagePath != null ? imagePath : patient.profilePicture;
        if (patient.dateRegistered == null || patient.dateRegistered.isEmpty()) {
            patient.dateRegistered = java.time.LocalDate.now().toString();
        }
        // Save registrant as account_id so DB FK remains valid
        Account cur = Session.getCurrentUser();
        patient.registeredById = (cur != null) ? cur.accountId : 0;
        return PatientDAO.save(patient);
    }
}
