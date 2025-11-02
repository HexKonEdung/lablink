// Admin_Dashboard.java
// Register for test.saved events so the welcome panel and lists refresh automatically.
package Main;

import javax.swing.*;

public class Admin_Dashboard extends BaseDashboardFrame {

    private Dashboard_Welcome_Panel welcomePanel = new Dashboard_Welcome_Panel();
    private Account_List_Panel accountListPanel = new Account_List_Panel();
    private Patient_List_Panel patientListPanel = new Patient_List_Panel();
    private Test_Record_List_Panel testListPanel = new Test_Record_List_Panel();
    private Activity_Log_Panel activityLogPanel = new Activity_Log_Panel();
    private ProfilePanel profilePanel = new ProfilePanel();

    public Admin_Dashboard() {
        super("Admin Dashboard - LabLink");
        buildMenu();
        buildCards();
        setUserLabel(Session.getCurrentUser().fullName + " (" + Session.getCurrentUser().role + ")");
        // show dashboard by default
        show("dashboard");

        // Listen for test saved events to refresh charts & lists
        EventBus.addListener("test.saved", (evt, payload) -> SwingUtilities.invokeLater(() -> {
            try {
                // recreate welcome panel to refresh charts (or expose refresh method)
                cardPanel.remove(welcomePanel);
                welcomePanel = new Dashboard_Welcome_Panel();
                cardPanel.add(welcomePanel, "dashboard");
                testListPanel.load();
                patientListPanel.load();
                activityLogPanel.load();
                cardPanel.revalidate();
                cardPanel.repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }));
    }

    private void buildMenu() {
        addMenuButton("Dashboard", () -> show("dashboard"));
        addMenuButton("Account Management", () -> show("accounts"));
        addMenuButton("Patient Management", () -> show("patients"));
        addMenuButton("Test Records", () -> show("tests"));
        addMenuButton("Activity Log", () -> show("log"));
        addMenuButton("Profile", () -> show("profile"));
        addMenuButton("Logout", () -> {
            Session.clear();
            dispose();
            SwingUtilities.invokeLater(() -> new Sign_In().centerAndShow());
        });
    }

    private void buildCards() {
        cardPanel.add(welcomePanel, "dashboard");
        cardPanel.add(accountListPanel, "accounts");
        cardPanel.add(patientListPanel, "patients");
        cardPanel.add(testListPanel, "tests");
        cardPanel.add(activityLogPanel, "log");
        cardPanel.add(profilePanel, "profile");
    }

    private void show(String id) {
        cards.show(cardPanel, id);
    }
}
