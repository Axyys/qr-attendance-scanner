import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;

public class QRScannerPanel extends JPanel {
    private AttendanceDatabase database;
    private MultiFormatReader qrReader;
    private JLabel statusLabel;
    private JLabel imageLabel;
    private JTextArea resultArea;
    private BufferedImage selectedImage;

    public QRScannerPanel(AttendanceDatabase database) {
        this.database = database;
        this.qrReader = new MultiFormatReader();
        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(240, 240, 240));

        // Top panel with buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topPanel.setBackground(new Color(240, 240, 240));

        JButton browseButton = new JButton("Browse QR Code Image");
        browseButton.setFont(new Font("Arial", Font.BOLD, 14));
        browseButton.setBackground(new Color(0, 102, 204));
        browseButton.setForeground(Color.WHITE);
        browseButton.setFocusPainted(false);
        browseButton.addActionListener(e -> browseFile());

        JButton scanButton = new JButton("Scan QR Code");
        scanButton.setFont(new Font("Arial", Font.BOLD, 14));
        scanButton.setBackground(new Color(34, 139, 34));
        scanButton.setForeground(Color.WHITE);
        scanButton.setFocusPainted(false);
        scanButton.addActionListener(e -> scanQRCode());

        JButton clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearButton.setBackground(new Color(204, 0, 0));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> clear());

        topPanel.add(browseButton);
        topPanel.add(scanButton);
        topPanel.add(clearButton);

        // Center panel with image and result
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.setBackground(new Color(240, 240, 240));

        // Image display
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);
        imageLabel.setBackground(Color.WHITE);
        imageLabel.setOpaque(true);
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        centerPanel.add(imageLabel);

        // Result area
        resultArea = new JTextArea();
        resultArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        resultArea.setEditable(false);
        resultArea.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        centerPanel.add(scrollPane);

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        statusPanel.setBackground(new Color(240, 240, 240));
        statusLabel = new JLabel("Status: Ready");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusPanel.add(statusLabel);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif"));
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                selectedImage = ImageIO.read(file);
                displayImage(selectedImage);
                statusLabel.setText("Status: Image loaded - " + file.getName());
            } catch (Exception e) {
                statusLabel.setText("Status: Error loading image");
                JOptionPane.showMessageDialog(this, "Error loading image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void displayImage(BufferedImage image) {
        int maxWidth = 300;
        int maxHeight = 300;
        Image scaledImage = image.getScaledInstance(maxWidth, maxHeight, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaledImage));
    }

    private void scanQRCode() {
        if (selectedImage == null) {
            JOptionPane.showMessageDialog(this, "Please load an image first", "No Image", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LuminanceSource source = new BufferedImageLuminanceSource(selectedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result scanResult = qrReader.decode(bitmap);

            String qrContent = scanResult.getText();
            processAttendance(qrContent);
            statusLabel.setText("Status: QR code scanned successfully!");

        } catch (Exception e) {
            statusLabel.setText("Status: Failed to scan QR code");
            resultArea.setText("Error: Could not decode QR code\n" + e.getMessage());
        }
    }

    private void processAttendance(String studentId) {
        if (database.isValidStudent(studentId)) {
            Student student = database.getStudent(studentId);
            AttendanceRecord record = new AttendanceRecord(studentId, LocalDateTime.now(), "Present");
            database.recordAttendance(record);

            resultArea.setText("✓ ATTENDANCE RECORDED\n\n" +
                    "Student ID: " + studentId + "\n" +
                    "Name: " + student.getName() + "\n" +
                    "Email: " + student.getEmail() + "\n" +
                    "Time: " + record.getFormattedDate() + "\n" +
                    "Status: " + record.getStatus());
        } else {
            resultArea.setText("✗ INVALID STUDENT\n\n" +
                    "Student ID: " + studentId + "\n" +
                    "This student ID is not registered in the system.");
        }
    }

    private void clear() {
        selectedImage = null;
        imageLabel.setIcon(null);
        resultArea.setText("");
        statusLabel.setText("Status: Ready");
    }
}