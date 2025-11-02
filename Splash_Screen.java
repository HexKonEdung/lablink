package Main;

// Splash_Screen.java
// Shows centered logo (data/logo.png) and percentage progress while DatabaseInit runs.

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Splash_Screen extends JWindow {

    private JProgressBar progressBar;
    private JLabel lblPercent;
    private Timer timer;
    private int progress = 0;
    private ImageIcon logoIcon;

    public Splash_Screen() {
        loadLogo();
        buildUI();
    }

    private void loadLogo() {
        try {
            File f = new File("data/logo.png");
            if (f.exists()) {
                logoIcon = new ImageIcon(f.getAbsolutePath());
                Image img = logoIcon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                logoIcon = new ImageIcon(img);
            } else {
                logoIcon = null;
            }
        } catch (Exception e) {
            logoIcon = null;
        }
    }

    private void buildUI() {
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                Color c1 = UiTheme.PRIMARY;
                Color c2 = UiTheme.ACCENT;
                GradientPaint gp = new GradientPaint(0, 0, c1, w, 0, c2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        content.setLayout(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(Box.createVerticalGlue());

        if (logoIcon != null) {
            JLabel logo = new JLabel(logoIcon);
            logo.setAlignmentX(Component.CENTER_ALIGNMENT);
            center.add(logo);
            center.add(Box.createRigidArea(new Dimension(0, 12)));
        }

        JLabel title = new JLabel("LabLink Diagnostics", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(UiTheme.HERO_FONT.deriveFont(26f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(title);

        center.add(Box.createRigidArea(new Dimension(0, 6)));

        JLabel subtitle = new JLabel("Laboratory Information System", SwingConstants.CENTER);
        subtitle.setForeground(new Color(255, 255, 255, 220));
        subtitle.setFont(UiTheme.NORMAL_FONT.deriveFont(14f));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(subtitle);

        center.add(Box.createVerticalGlue());

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(420, 14));
        progressBar.setForeground(UiTheme.ACCENT.darker());
        progressBar.setOpaque(false);
        progressBar.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 80)));

        lblPercent = new JLabel("0%", SwingConstants.CENTER);
        lblPercent.setForeground(Color.WHITE);
        lblPercent.setFont(UiTheme.NORMAL_FONT.deriveFont(12f));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(progressBar, BorderLayout.CENTER);
        bottom.add(lblPercent, BorderLayout.EAST);
        bottom.setBorder(BorderFactory.createEmptyBorder(12, 40, 8, 40));

        content.add(center, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(600, 380);
        setLocationRelativeTo(null);
    }

    public void showThenOpen(Runnable onComplete) {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            // Progress animation while DB init runs in background
            Thread dbThread = new Thread(() -> {
                DatabaseInit.init();
            }, "DB-Init-Thread");
            dbThread.start();

            timer = new Timer(25, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // If DB init thread finished, fast finish progress
                    if (!dbThread.isAlive()) {
                        progress += 6;
                    } else {
                        progress += 2;
                    }
                    if (progress > 100) {
                        progress = 100;
                    }
                    progressBar.setValue(progress);
                    lblPercent.setText(progress + "%");
                    if (progress >= 100) {
                        timer.stop();
                        dispose();
                        if (onComplete != null) {
                            SwingUtilities.invokeLater(onComplete);
                        }
                    }
                }
            });
            timer.setInitialDelay(200);
            timer.start();
        });
    }
}
