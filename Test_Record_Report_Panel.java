package Main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.*;
import java.io.FileOutputStream;
import java.util.List;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

/**
 * Test_Record_Report_Panel.java Shows a read-only report for a TestRecord and
 * allows printing or exporting to PDF.
 *
 * Requires iText (com.itextpdf.text.*) on the classpath for PDF export.
 */
public class Test_Record_Report_Panel extends JPanel {

    private TestRecord record;
    private Patient patient;
    private DefaultTableModel model;
    private JPanel contentPanel;

    public Test_Record_Report_Panel(TestRecord record) {
        this.record = record;
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);

        // Load patient for display (may be null)
        this.patient = PatientDAO.findById(record.patientId);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        contentPanel.setBackground(UiTheme.PANEL_BG);

        // Header info grid
        JPanel info = new JPanel(new GridLayout(0, 2, 8, 8));
        info.setBackground(UiTheme.PANEL_BG);

        info.add(new JLabel("Test ID:"));
        info.add(new JLabel(String.valueOf(record.testId)));

        info.add(new JLabel("Patient ID:"));
        info.add(new JLabel(String.valueOf(record.patientId)));

        info.add(new JLabel("Patient Name:"));
        info.add(new JLabel(patient != null ? patient.name : "Unknown"));

        info.add(new JLabel("Test Name:"));
        info.add(new JLabel(record.testName != null ? record.testName : ""));

        info.add(new JLabel("Category:"));
        info.add(new JLabel(record.category != null ? record.category : ""));

        info.add(new JLabel("Sample Type:"));
        info.add(new JLabel(record.sampleType != null ? record.sampleType : ""));

        info.add(new JLabel("Technician:"));
        info.add(new JLabel(record.technician != null ? record.technician : ""));

        info.add(new JLabel("Date Conducted:"));
        info.add(new JLabel(record.dateConducted != null ? record.dateConducted : ""));

        info.add(new JLabel("Status:"));
        info.add(new JLabel(record.status != null ? record.status : ""));

        contentPanel.add(info, BorderLayout.NORTH);

        // Parameters table
        String[] cols = new String[]{"Parameter", "Result", "Normal Range", "Units", "Interpretation"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        UiTheme.styleTable(table, new JScrollPane(table));

        // Load parameters via DAO
        List<TestParameter> params = TestRecordDAO.getParameters(record.testId);
        if (params != null) {
            for (TestParameter p : params) {
                model.addRow(new Object[]{
                    p.parameterName, p.resultValue, p.normalRange, p.units, p.interpretation
                });
            }
        }

        contentPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Remarks / footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setBackground(UiTheme.PANEL_BG);
        ta.setText("Remarks: " + (record.remarks != null ? record.remarks : ""));
        footer.add(ta, BorderLayout.CENTER);
        contentPanel.add(footer, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        // Buttons (Print / Export PDF / Close)
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBackground(UiTheme.BG);

        JButton btnPrint = new JButton("Print");
        btnPrint.setBackground(UiTheme.PRIMARY);
        btnPrint.setForeground(Color.WHITE);
        btnPrint.addActionListener(e -> doPrint());
        bottom.add(btnPrint);

        JButton btnPdf = new JButton("Export PDF");
        btnPdf.setBackground(UiTheme.ACCENT);
        btnPdf.setForeground(Color.WHITE);
        btnPdf.addActionListener(e -> doExportPdf());
        bottom.add(btnPdf);

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) {
                w.dispose();
            }
        });
        bottom.add(btnClose);

        add(bottom, BorderLayout.SOUTH);
    }

    private void doPrint() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Print Test Record - " + record.testId);
        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics g, PageFormat pf, int pageIndex) {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }
                Graphics2D g2 = (Graphics2D) g;
                // translate to printable area
                g2.translate(pf.getImageableX(), pf.getImageableY());
                // scale down if needed
                double sx = pf.getImageableWidth() / contentPanel.getWidth();
                double sy = pf.getImageableHeight() / contentPanel.getHeight();
                double scale = Math.min(1.0, Math.min(sx, sy));
                g2.scale(scale, scale);
                contentPanel.printAll(g2);
                return Printable.PAGE_EXISTS;
            }
        });
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doExportPdf() {
        // Use JFileChooser to pick destination
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Test Record PDF");
        fc.setSelectedFile(new java.io.File("test-" + record.testId + ".pdf"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File out = fc.getSelectedFile();
        try {
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(out));
            doc.open();

            // Use fully-qualified iText Font to avoid ambiguity with java.awt.Font
            com.itextpdf.text.Font h1 = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font normal = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.NORMAL);

            Paragraph title = new Paragraph("Test Record", h1);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(Chunk.NEWLINE);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.addCell(new PdfPCell(new Phrase("Test ID:", normal)));
            infoTable.addCell(new PdfPCell(new Phrase(String.valueOf(record.testId), normal)));
            infoTable.addCell(new PdfPCell(new Phrase("Patient ID:", normal)));
            infoTable.addCell(new PdfPCell(new Phrase(String.valueOf(record.patientId), normal)));
            infoTable.addCell(new PdfPCell(new Phrase("Patient Name:", normal)));
            infoTable.addCell(new PdfPCell(new Phrase(patient != null ? patient.name : "Unknown", normal)));
            infoTable.addCell(new PdfPCell(new Phrase("Test Name:", normal)));
            infoTable.addCell(new PdfPCell(new Phrase(record.testName != null ? record.testName : "", normal)));
            infoTable.addCell(new PdfPCell(new Phrase("Category:", normal)));
            infoTable.addCell(new PdfPCell(new Phrase(record.category != null ? record.category : "", normal)));
            infoTable.addCell(new PdfPCell(new Phrase("Sample Type:", normal)));
            infoTable.addCell(new PdfPCell(new Phrase(record.sampleType != null ? record.sampleType : "", normal)));
            infoTable.addCell(new PdfPCell(new Phrase("Technician:", normal)));
            infoTable.addCell(new PdfPCell(new Phrase(record.technician != null ? record.technician : "", normal)));
            infoTable.addCell(new PdfPCell(new Phrase("Date Conducted:", normal)));
            infoTable.addCell(new PdfPCell(new Phrase(record.dateConducted != null ? record.dateConducted : "", normal)));
            infoTable.addCell(new PdfPCell(new Phrase("Status:", normal)));
            infoTable.addCell(new PdfPCell(new Phrase(record.status != null ? record.status : "", normal)));
            doc.add(infoTable);

            doc.add(Chunk.NEWLINE);

            // Parameters table
            PdfPTable ptab = new PdfPTable(5);
            ptab.setWidthPercentage(100);
            ptab.addCell(new PdfPCell(new Phrase("Parameter", normal)));
            ptab.addCell(new PdfPCell(new Phrase("Result", normal)));
            ptab.addCell(new PdfPCell(new Phrase("Normal Range", normal)));
            ptab.addCell(new PdfPCell(new Phrase("Units", normal)));
            ptab.addCell(new PdfPCell(new Phrase("Interpretation", normal)));

            List<TestParameter> params = TestRecordDAO.getParameters(record.testId);
            if (params != null) {
                for (TestParameter p : params) {
                    ptab.addCell(new PdfPCell(new Phrase(p.parameterName != null ? p.parameterName : "", normal)));
                    ptab.addCell(new PdfPCell(new Phrase(p.resultValue != null ? p.resultValue : "", normal)));
                    ptab.addCell(new PdfPCell(new Phrase(p.normalRange != null ? p.normalRange : "", normal)));
                    ptab.addCell(new PdfPCell(new Phrase(p.units != null ? p.units : "", normal)));
                    ptab.addCell(new PdfPCell(new Phrase(p.interpretation != null ? p.interpretation : "", normal)));
                }
            }

            doc.add(ptab);
            doc.add(Chunk.NEWLINE);

            Paragraph remarks = new Paragraph("Remarks: " + (record.remarks != null ? record.remarks : ""), normal);
            doc.add(remarks);

            doc.close();
            JOptionPane.showMessageDialog(this, "PDF exported to " + out.getAbsolutePath());
        } catch (NoClassDefFoundError | Exception ex) {
            // If iText is not present, show helpful message
            JOptionPane.showMessageDialog(this,
                    "PDF export failed: iText library not found or error occurred.\nAdd iText (com.itextpdf) to the classpath for PDF export.\nError: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
