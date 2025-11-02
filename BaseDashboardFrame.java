// BaseDashboardFrame.java
// Left menu + card layout center
package Main;
import javax.swing.*;
import java.awt.*;

public class BaseDashboardFrame extends BaseFrame {

    protected CardLayout cards = new CardLayout();
    protected JPanel cardPanel = new JPanel(cards);
    protected JPanel sideMenu = new JPanel();
    protected JLabel userLabel = new JLabel();

    public BaseDashboardFrame(String title) {
        super(title);
        build();
    }

    private void build() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(header(getTitle()), BorderLayout.NORTH);

        sideMenu.setBackground(UiTheme.PANEL_BG);
        sideMenu.setPreferredSize(new Dimension(UiTheme.SIDE_WIDTH, 0));
        sideMenu.setLayout(new BoxLayout(sideMenu, BoxLayout.Y_AXIS));
        sideMenu.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        getContentPane().add(sideMenu, BorderLayout.WEST);

        cardPanel.setBackground(UiTheme.BG);
        getContentPane().add(cardPanel, BorderLayout.CENTER);
    }

    protected JButton menuButton(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setFont(UiTheme.NORMAL_FONT);
        b.setBackground(UiTheme.PRIMARY);
        b.setForeground(Color.white);
        b.setFocusPainted(false);
        b.addActionListener(e -> action.run());
        return b;
    }

    protected void addMenuButton(String text, Runnable action) {
        sideMenu.add(menuButton(text, action));
        sideMenu.add(Box.createRigidArea(new Dimension(0, 8)));
    }

    protected void setUserLabel(String text) {
        userLabel.setText("<html><b>User:</b> " + text + "</html>");
        userLabel.setFont(UiTheme.NORMAL_FONT);
        userLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 10, 0));
        sideMenu.add(userLabel, 0);
    }
}
