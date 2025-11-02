// Sign_In.java
// Loads FlatLaf if available, initializes DB once, shows splash, then Sign In form.
package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Sign_In extends BaseFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblMsg;

    public Sign_In() {
        super("LabLink Diagnostics - Sign In");
        initUI();
    }

    private void initUI() {
        // Apply small LAF hints
        UiTheme.applyLookAndFeelHints();

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UiTheme.BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("LabLink Diagnostics");
        title.setForeground(UiTheme.PRIMARY.darker());
        title.setFont(UiTheme.HERO_FONT.deriveFont(26f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        root.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 1;
        root.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(22);
        txtUsername.setFont(UiTheme.NORMAL_FONT);
        root.add(txtUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        root.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(22);
        txtPassword.setFont(UiTheme.NORMAL_FONT);
        root.add(txtPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        JButton btnSignIn = new JButton("Sign In");
        btnSignIn.setBackground(UiTheme.PRIMARY);
        btnSignIn.setForeground(Color.WHITE);
        btnSignIn.setFocusPainted(false);
        btnSignIn.setFont(UiTheme.NORMAL_FONT.deriveFont(Font.BOLD));
        btnSignIn.addActionListener(e -> doSignIn());
        btnRow.add(btnSignIn);

        JButton btnQuit = new JButton("Exit");
        btnQuit.setBackground(Color.LIGHT_GRAY);
        btnQuit.setForeground(Color.DARK_GRAY);
        btnQuit.setFocusPainted(false);
        btnQuit.addActionListener(e -> System.exit(0));
        btnRow.add(btnQuit);

        root.add(btnRow, gbc);

        gbc.gridy = 4;
        lblMsg = new JLabel(" ");
        lblMsg.setForeground(new Color(200, 40, 40));
        lblMsg.setFont(UiTheme.NORMAL_FONT);
        lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(lblMsg, gbc);

        KeyAdapter resetMsg = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                lblMsg.setText(" ");
            }
        };
        txtUsername.addKeyListener(resetMsg);
        txtPassword.addKeyListener(resetMsg);

        // Enter key triggers sign in
        txtPassword.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doSignIn();
                }
            }
        });

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(root, BorderLayout.CENTER);
        setSize(560, 360);
        setLocationRelativeTo(null);
    }

    private void doSignIn() {
        String u = txtUsername.getText().trim();
        String p = new String(txtPassword.getPassword());
        if (u.isEmpty() || p.isEmpty()) {
            lblMsg.setText("Username and password are required.");
            return;
        }
        Account a = AccountDAO.authenticate(u, p);
        if (a != null) {
            Session.setCurrentUser(a);
            lblMsg.setText("Welcome, " + a.fullName);
            SwingUtilities.invokeLater(() -> {
                dispose();
                if ("Admin".equalsIgnoreCase(a.role)) {
                    Admin_Dashboard ad = new Admin_Dashboard();
                    ad.centerAndShow();
                } else {
                    Technician_Dashboard td = new Technician_Dashboard();
                    td.centerAndShow();
                }
            });
        } else {
            lblMsg.setText("Invalid credentials.");
        }
    }

    public static void main(String[] args) {
        // Try FlatLaf if available
        try {
            Class.forName("com.formdev.flatlaf.FlatLightLaf");
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception ignore) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
            }
        }

        // Load driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "MySQL JDBC driver not found. Add connector to classpath.");
            return;
        }

        // Show splash -> DB init -> SignIn
        SwingUtilities.invokeLater(() -> {
            Splash_Screen splash = new Splash_Screen();
            splash.showThenOpen(() -> {
                Sign_In s = new Sign_In();
                s.centerAndShow();
            });
        });
    }
}
