package Main;

import javax.swing.*;

/**
 * Technician Dashboard frame: registers EventBus listener to react when the
 * welcome panel posts "open.tests.status" (it will show the tests card and set
 * filter).
 */
public class Technician_Dashboard extends BaseDashboardFrame {

    private Technician_Dashboard_Welcome_Panel welcomePanel;
    private Patient_List_Panel patientListPanel = new Patient_List_Panel();
    private Test_Record_List_Panel testListPanel = new Test_Record_List_Panel();
    private ProfilePanel profilePanel = new ProfilePanel();

    public Technician_Dashboard() {
        super("Technician Dashboard - LabLink");
        buildMenu();
        buildCards();
        setUserLabel(Session.getCurrentUser().fullName + " (" + Session.getCurrentUser().role + ")");
        show("dashboard");

        // Listen for "open.tests.status" to open tests and filter by status
        EventBus.addListener("open.tests.status", (evt, payload) -> SwingUtilities.invokeLater(() -> {
            try {
                show("tests");
                if (payload instanceof String) {
                    testListPanel.setStatusAndLoad((String) payload);
                } else {
                    testListPanel.load();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }));

        // Also refresh when tests saved
        EventBus.addListener("test.saved", (evt, payload) -> SwingUtilities.invokeLater(() -> {
            try {
                // refresh welcome panel & lists
                cardPanel.remove(welcomePanel);
                welcomePanel = new Technician_Dashboard_Welcome_Panel();
                cardPanel.add(welcomePanel, "dashboard");
                testListPanel.load();
                patientListPanel.load();
                cardPanel.revalidate();
                cardPanel.repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }));
    }

    private void buildMenu() {
        addMenuButton("Dashboard", () -> show("dashboard"));
        addMenuButton("Patient Management", () -> show("patients"));
        addMenuButton("Test Records", () -> show("tests"));
        addMenuButton("Profile", () -> show("profile"));
        addMenuButton("Logout", () -> {
            Session.clear();
            dispose();
            SwingUtilities.invokeLater(() -> new Sign_In().centerAndShow());
        });
    }

    private void buildCards() {
        welcomePanel = new Technician_Dashboard_Welcome_Panel();
        cardPanel.add(welcomePanel, "dashboard");
        cardPanel.add(patientListPanel, "patients");
        cardPanel.add(testListPanel, "tests");
        cardPanel.add(profilePanel, "profile");
    }

    private void show(String id) {
        cards.show(cardPanel, id);
    }
}
