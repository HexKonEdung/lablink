// BaseFrame.java
package Main;
import javax.swing.*;
import java.awt.*;

public class BaseFrame extends JFrame {

    public BaseFrame(String title) {
        super(title);
        init();
    }

    private void init() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
    }

    protected JPanel header(String title) {
        JPanel head = new JPanel(new BorderLayout());
        head.setPreferredSize(new Dimension(0, UiTheme.HEADER_HEIGHT));
        head.setBackground(UiTheme.PRIMARY);
        JLabel lbl = new JLabel("  " + title);
        lbl.setForeground(UiTheme.HEADER_TEXT);
        lbl.setFont(UiTheme.HEADER_FONT);
        head.add(lbl, BorderLayout.WEST);
        return head;
    }

    protected void centerAndShow() {
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
