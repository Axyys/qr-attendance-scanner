import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;

public class ReportPanel extends JPanel {
    private AttendanceDatabase database;
    private JTable reportTable;
    private JLabel summaryLabel;
    private DefaultTableModel tableModel;
    private javax.swing.Timer refreshTimer;  // ← Changed here

    public ReportPanel(AttendanceDatabase database) {
        this.database = database;
        setupUI();
        startAutoRefresh();
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(240, 240, 240));

        // Top panel with refresh button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topPanel.setBackground(new Color(240, 240, 240));

        JButton refreshButton = new JButton("🔄 Refresh Report");
        refreshButton.setFont(new Font("Arial", Font.BOLD, 14));
        refreshButton.setBackground(new Color(0, 102, 204));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> updateReport());

        JButton exportButton = new JButton("📥 Export as CSV");
        exportButton.setFont(new Font("Arial", Font.BOLD, 14));
        exportButton.setBackground(new Color(34, 139, 34));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFocusPainted(false);
        exportButton.addActionListener(e -> exportCSV());

        JButton clearButton = new JButton("🗑️ Clear All");
        clearButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearButton.setBackground(new Color(204, 0, 0));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> clearAllRecords());

        topPanel.add(refreshButton);
        topPanel.add(exportButton);
        topPanel.add(clearButton);

        // Summary label
        summaryLabel = new JLabel();
        summaryLabel.setFont(new Font("Arial", Font.BOLD, 13));
        summaryLabel.setBackground(new Color(200, 220, 255));
        summaryLabel.setOpaque(true);
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table
        String[] columnNames = {"Student ID", "Name", "Email", "Scan Time", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportTable = new JTable(tableModel);
        reportTable.setFont(new Font("Arial", Font.PLAIN, 11));
        reportTable.setRowHeight(25);
        reportTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        reportTable.getTableHeader().setBackground(new Color(0, 102, 204));
        reportTable.getTableHeader().setForeground(Color.WHITE);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

        // Panel for summary
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBackground(new Color(240, 240, 240));
        summaryPanel.add(summaryLabel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(summaryPanel, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);

        updateReport();
    }

    private void startAutoRefresh() {
        // Refresh every 2 seconds
        refreshTimer = new javax.swing.Timer(2000, e -> updateReport());  // ← Changed here
        refreshTimer.start();
    }

    public void updateReport() {
        tableModel.setRowCount(0);

        Map<String, Integer> attendanceCount = new HashMap<>();

        java.util.List<AttendanceRecord> records = database.getAttendanceRecords();

        // Add all records to table (most recent first)
        for (int i = records.size() - 1; i >= 0; i--) {
            AttendanceRecord record = records.get(i);
            String studentId = record.getStudentId();
            Student student = database.getStudent(studentId);

            if (student != null) {
                tableModel.addRow(new Object[]{
                        studentId,
                        student.getName(),
                        student.getEmail(),
                        record.getFormattedDate(),
                        record.getStatus()
                });

                attendanceCount.put(studentId, attendanceCount.getOrDefault(studentId, 0) + 1);
            }
        }

        // Update summary
        if (records.isEmpty()) {
            summaryLabel.setText("📊 No attendance records yet");
        } else {
            summaryLabel.setText("📊 Total Records: " + database.getTotalRecords() +
                    " | Unique Students: " + attendanceCount.size() +
                    " | Last Updated: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
    }

    private void clearAllRecords() {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear all attendance records?",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            database.clearAllRecords();
            updateReport();
            JOptionPane.showMessageDialog(this, "All records cleared!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportCSV() {
        try {
            String filename = "attendance_report_" + System.currentTimeMillis() + ".csv";
            StringBuilder csv = new StringBuilder();
            csv.append("Student ID,Name,Email,Scan Time,Status\n");

            java.util.List<AttendanceRecord> records = database.getAttendanceRecords();
            for (AttendanceRecord record : records) {
                Student student = database.getStudent(record.getStudentId());
                if (student != null) {
                    csv.append(record.getStudentId()).append(",")
                            .append(student.getName()).append(",")
                            .append(student.getEmail()).append(",")
                            .append(record.getFormattedDate()).append(",")
                            .append(record.getStatus()).append("\n");
                }
            }

            java.nio.file.Files.write(java.nio.file.Paths.get(filename), csv.toString().getBytes());
            JOptionPane.showMessageDialog(this, "Report exported to: " + filename, "✓ Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error exporting: " + e.getMessage(), "❌ Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void removeNotify() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        super.removeNotify();
    }
}