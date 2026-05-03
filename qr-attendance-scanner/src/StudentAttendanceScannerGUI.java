import javax.swing.*;
import java.awt.*;

public class StudentAttendanceScannerGUI extends JFrame {
    private AttendanceDatabase database;
    private QRScannerPanel scannerPanel;
    private WebcamScannerPanel webcamScannerPanel;
    private ReportPanel reportPanel;
    private JTabbedPane tabbedPane;

    public StudentAttendanceScannerGUI() {
        this.database = new AttendanceDatabase();
        initializeDatabase();
        setupUI();
    }

    private void initializeDatabase() {
        database.addStudent(new Student("STU001", "John Doe", "john@school.edu"));
        database.addStudent(new Student("STU002", "Jane Smith", "jane@school.edu"));
        database.addStudent(new Student("STU003", "Mike Johnson", "mike@school.edu"));
        database.addStudent(new Student("STU004", "Sarah Williams", "sarah@school.edu"));
        database.addStudent(new Student("STU005", "Tom Brown", "tom@school.edu"));
    }

    private void setupUI() {
        setTitle("Student Attendance Scanner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setResizable(true);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();

        // Webcam scanner panel (NEW)
        webcamScannerPanel = new WebcamScannerPanel(database);
        tabbedPane.addTab("Webcam Scanner", new ImageIcon(), webcamScannerPanel, "Scan QR codes with your camera");

        // File scanner panel
        scannerPanel = new QRScannerPanel(database);
        tabbedPane.addTab("Scan from File", new ImageIcon(), scannerPanel, "Scan QR codes from image files");

        // Report panel
        reportPanel = new ReportPanel(database);
        tabbedPane.addTab("Attendance Report", new ImageIcon(), reportPanel, "View attendance records");

        // Settings panel
        SettingsPanel settingsPanel = new SettingsPanel(database);
        tabbedPane.addTab("Manage Students", new ImageIcon(), settingsPanel, "Add or remove students");

        add(tabbedPane);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentAttendanceScannerGUI());
    }
}