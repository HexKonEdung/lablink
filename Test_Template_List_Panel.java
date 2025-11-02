package Main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class Test_Template_List_Panel extends JPanel {

    private JTable templateTable;
    private DefaultTableModel tableModel;
    private TemplateDAO templateDAO;
    private ActivityLogDAO logDAO;
    private BaseDashboardFrame dashboard;
    private JLabel lblStatus;
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> sorter;

    public Test_Template_List_Panel(BaseDashboardFrame dashboard) {
        super(new BorderLayout(10, 10));
        this.dashboard = dashboard;
        this.templateDAO = new TemplateDAO();
        this.logDAO = new ActivityLogDAO();

        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        initComponents();
        loadTemplates();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Test Template Management");
        UiTheme.stylePageHeading(lblTitle);
        topPanel.add(lblTitle, BorderLayout.WEST);

        JPanel searchAddPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchAddPanel.setOpaque(false);

        searchAddPanel.add(new JLabel("Search:"));
        txtSearch = new JTextField(20);
        UiTheme.styleFormInput(txtSearch);
        searchAddPanel.add(txtSearch);

        JButton btnAdd = new JButton("Add New Template");
        UiTheme.stylePrimaryButton(btnAdd);
        searchAddPanel.add(btnAdd);

        topPanel.add(searchAddPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Template Name", "Category", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        templateTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        templateTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(templateTable);
        UiTheme.styleTable(templateTable, scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        lblStatus = new JLabel("Loading templates...");
        lblStatus.setFont(UiTheme.NORMAL_FONT);
        bottomPanel.add(lblStatus, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton btnEdit = new JButton("Edit Selected");
        UiTheme.stylePrimaryButton(btnEdit);
        buttonPanel.add(btnEdit);

        JButton btnDelete = new JButton("Delete Selected");
        UiTheme.styleDangerButton(btnDelete);
        buttonPanel.add(btnDelete);

        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        btnAdd.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Template form navigation not implemented in this demo.");
        });
        btnEdit.addActionListener(e -> editSelectedTemplate());
        btnDelete.addActionListener(e -> deleteSelectedTemplate());

        templateTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedTemplate();
                }
            }
        });

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTable();
            }
        });
    }

    private void filterTable() {
        String text = txtSearch.getText();
        sorter.setRowFilter(text.trim().length() == 0 ? null : RowFilter.regexFilter("(?i)" + text));
    }

    private void loadTemplates() {
        lblStatus.setText("Loading templates...");

        SwingWorker<List<TestTemplate>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TestTemplate> doInBackground() throws Exception {
                return templateDAO.listAll();
            }

            @Override
            protected void done() {
                try {
                    List<TestTemplate> templates = get();
                    tableModel.setRowCount(0);
                    for (TestTemplate t : templates) {
                        tableModel.addRow(new Object[]{
                            t.getId(), t.getName(), t.getCategory(), t.getDescription()
                        });
                    }
                    lblStatus.setText(templates.size() + " templates loaded.");
                } catch (Exception e) {
                    e.printStackTrace();
                    lblStatus.setText("Error loading templates.");
                }
            }
        };
        worker.execute();
    }

    private TestTemplate getSelectedTemplate() {
        int viewRow = templateTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a template.", "No Template Selected", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int modelRow = templateTable.convertRowIndexToModel(viewRow);
        int templateId = (int) tableModel.getValueAt(modelRow, 0);
        return templateDAO.findById(templateId);
    }

    private void editSelectedTemplate() {
        TestTemplate t = getSelectedTemplate();
        if (t != null) {
            JOptionPane.showMessageDialog(this, "Edit template not wired in demo. Template ID: " + t.getId());
        }
    }

    private void deleteSelectedTemplate() {
        TestTemplate t = getSelectedTemplate();
        if (t == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete template '" + t.getName() + "'?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            if (templateDAO.deleteTemplate(t.getId())) {
                ActivityLogDAO.log(Session.getCurrentUser() != null ? Session.getCurrentUser().username : "system", "DELETE_TEMPLATE", "test_templates", t.getId(), "Deleted template.");
                loadTemplates();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete template.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
