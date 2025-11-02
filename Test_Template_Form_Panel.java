package Main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Test Template form panel.
 *
 * Fixes applied: - Replaced reference to non-existent UiTheme.CARD_COLOR with
 * UiTheme.PANEL_BG. - Removed uses of non-existent BaseDashboardFrame.showPanel
 * and dashboard.currentUser. Navigation now tries to use the dashboard's
 * protected card layout if available, otherwise closes the surrounding window.
 * - Logging uses ActivityLogDAO.log(...) with Session to retrieve current user.
 */
public class Test_Template_Form_Panel extends JPanel {

    private BaseDashboardFrame dashboard;
    private TestTemplate templateToEdit;
    private boolean isEditMode;
    private TemplateDAO templateDAO;

    private JTextField txtName;
    private JTextField txtCategory;
    private JTextArea txtDescription;
    private JTable paramsTable;
    private javax.swing.table.DefaultTableModel paramsTableModel;

    public Test_Template_Form_Panel(BaseDashboardFrame dashboard, TestTemplate template) {
        super(new BorderLayout(10, 10));
        this.dashboard = dashboard;
        this.templateToEdit = template;
        this.isEditMode = (template != null);
        this.templateDAO = new TemplateDAO();

        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        initComponents();

        if (isEditMode) {
            populateForm();
        }
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel lblTitle = new JLabel(isEditMode ? "Edit Test Template" : "Create New Template");
        UiTheme.stylePageHeading(lblTitle);
        topPanel.add(lblTitle, BorderLayout.WEST);
        JButton btnBack = new JButton("Back to List");
        UiTheme.styleSecondaryButton(btnBack);
        topPanel.add(btnBack, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setOpaque(false);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setResizeWeight(0.4);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(true);
        // Use PANEL_BG which exists in UiTheme
        formPanel.setBackground(UiTheme.PANEL_BG);
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtName = new JTextField(30);
        addFormField(formPanel, gbc, 0, "Template Name:", txtName);
        txtCategory = new JTextField(30);
        addFormField(formPanel, gbc, 1, "Category:", txtCategory);
        txtDescription = new JTextArea(4, 30);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        addFormField(formPanel, gbc, 2, "Description:", new JScrollPane(txtDescription));
        splitPane.setTopComponent(formPanel);

        JPanel paramsPanel = new JPanel(new BorderLayout(5, 5));
        paramsPanel.setOpaque(false);
        JLabel lblParamsTitle = new JLabel("Template Parameters");
        UiTheme.styleFormLabel(lblParamsTitle);
        lblParamsTitle.setBorder(new EmptyBorder(5, 0, 5, 0));
        paramsPanel.add(lblParamsTitle, BorderLayout.NORTH);

        String[] paramCols = {"Name", "Units", "Reference Range", "Critical Values"};
        paramsTableModel = new javax.swing.table.DefaultTableModel(paramCols, 0);
        paramsTable = new JTable(paramsTableModel);
        UiTheme.styleTable(paramsTable, new JScrollPane(paramsTable));
        paramsPanel.add(new JScrollPane(paramsTable), BorderLayout.CENTER);

        JPanel paramsButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        paramsButtonPanel.setOpaque(false);
        JButton btnAddParam = new JButton("Add Param");
        UiTheme.styleSecondaryButton(btnAddParam);
        JButton btnRemoveParam = new JButton("Remove Param");
        UiTheme.styleSecondaryButton(btnRemoveParam);
        paramsButtonPanel.add(btnAddParam);
        paramsButtonPanel.add(btnRemoveParam);
        paramsPanel.add(paramsButtonPanel, BorderLayout.SOUTH);
        splitPane.setBottomComponent(paramsPanel);
        add(splitPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        JButton btnSave = new JButton("Save Template");
        UiTheme.stylePrimaryButton(btnSave);
        bottomPanel.add(btnSave);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        btnBack.addActionListener(e -> {
            // Try to navigate via dashboard's card layout if possible
            if (dashboard != null) {
                try {
                    dashboard.cards.show(dashboard.cardPanel, "templates");
                    return;
                } catch (Exception ex) {
                    // ignore and fallback to closing
                }
            }
            // Fallback: close dialog/window containing this panel
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) {
                w.dispose();
            }
        });
        btnSave.addActionListener(e -> saveTemplate());
        btnAddParam.addActionListener(e -> paramsTableModel.addRow(new Object[]{"", "", "", ""}));
        btnRemoveParam.addActionListener(e -> {
            int viewRow = paramsTable.getSelectedRow();
            if (viewRow >= 0) {
                paramsTableModel.removeRow(viewRow);
            }
        });
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int y, String label, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0.1;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lbl = new JLabel(label);
        UiTheme.styleFormLabel(lbl);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.9;
        gbc.fill = (component instanceof JScrollPane) ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
        UiTheme.styleFormInput(component);
        panel.add(component, gbc);
    }

    private void populateForm() {
        txtName.setText(templateToEdit.getName());
        txtCategory.setText(templateToEdit.getCategory());
        txtDescription.setText(templateToEdit.getDescription());
        if (templateToEdit.getParameters() != null) {
            paramsTableModel.setRowCount(0);
            for (TestParameter p : templateToEdit.getParameters()) {
                paramsTableModel.addRow(new Object[]{
                    p.getParameterName(),
                    p.getUnits(),
                    p.getReferenceRange(),
                    p.getCriticalValues()
                });
            }
        }
    }

    private void saveTemplate() {
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Template Name required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TestTemplate t = isEditMode ? templateToEdit : new TestTemplate();
        t.setName(txtName.getText().trim());
        t.setCategory(txtCategory.getText().trim());
        t.setDescription(txtDescription.getText().trim());

        ArrayList<TestParameter> params = new ArrayList<>();
        for (int i = 0; i < paramsTableModel.getRowCount(); i++) {
            String paramName = (String) paramsTableModel.getValueAt(i, 0);
            if (paramName == null || paramName.trim().isEmpty()) {
                continue;
            }
            TestParameter p = new TestParameter();
            p.setParameterName(paramName.trim());
            p.setUnits((String) paramsTableModel.getValueAt(i, 1));
            p.setReferenceRange((String) paramsTableModel.getValueAt(i, 2));
            p.setCriticalValues((String) paramsTableModel.getValueAt(i, 3));
            params.add(p);
        }
        t.setParameters(params);

        boolean success = false;
        int templateId = -1;
        try {
            if (isEditMode) {
                success = templateDAO.updateTemplate(t);
                templateId = t.getId();
            } else {
                templateId = templateDAO.insertTemplate(t);
                success = (templateId > 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (success) {
            String action = isEditMode ? "UPDATE_TEMPLATE" : "CREATE_TEMPLATE";
            ActivityLogDAO.log(Session.getCurrentUser() != null ? Session.getCurrentUser().username : "system", action, "test_templates", templateId, "Template saved.");
            JOptionPane.showMessageDialog(this, "Template saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
            // Try returning to templates list inside dashboard if available
            if (dashboard != null) {
                try {
                    dashboard.cards.show(dashboard.cardPanel, "templates");
                    return;
                } catch (Exception ex) {
                    // ignore and fallback to closing window
                }
            }
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) {
                w.dispose();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save template.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
