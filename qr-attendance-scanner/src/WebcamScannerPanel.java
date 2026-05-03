import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

public class WebcamScannerPanel extends JPanel {
    private AttendanceDatabase database;
    private Webcam webcam;
    private JLabel cameraDisplayLabel;
    private JLabel statusLabel;
    private JTextArea resultArea;
    private JButton startButton;
    private JButton stopButton;
    private MultiFormatReader qrReader;
    private Thread captureThread;
    private volatile boolean isRunning = false;

    public WebcamScannerPanel(AttendanceDatabase database) {
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

        startButton = new JButton("▶️ Start Camera");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.setBackground(new Color(34, 139, 34));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.addActionListener(e -> startCamera());

        stopButton = new JButton("⏹️ Stop Camera");
        stopButton.setFont(new Font("Arial", Font.BOLD, 14));
        stopButton.setBackground(new Color(204, 0, 0));
        stopButton.setForeground(Color.WHITE);
        stopButton.setFocusPainted(false);
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopCamera());

        JButton clearButton = new JButton("🗑️ Clear");
        clearButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearButton.setBackground(new Color(102, 102, 102));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> clearResult());

        topPanel.add(startButton);
        topPanel.add(stopButton);
        topPanel.add(clearButton);

        // Center panel with camera feed and result
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.setBackground(new Color(240, 240, 240));

        // Camera display label (manual rendering)
        cameraDisplayLabel = new JLabel();
        cameraDisplayLabel.setHorizontalAlignment(JLabel.CENTER);
        cameraDisplayLabel.setVerticalAlignment(JLabel.CENTER);
        cameraDisplayLabel.setBackground(Color.BLACK);
        cameraDisplayLabel.setOpaque(true);
        cameraDisplayLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        cameraDisplayLabel.setText("Camera feed will appear here");
        cameraDisplayLabel.setForeground(Color.WHITE);
        centerPanel.add(cameraDisplayLabel);

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

        initWebcam();
    }

    private void initWebcam() {
        try {
            Webcam.setAutoOpenMode(false);
            webcam = Webcam.getDefault();
            if (webcam != null) {
                webcam.setViewSize(WebcamResolution.VGA.getSize());
            }
        } catch (Exception e) {
            cameraDisplayLabel.setText("❌ Error initializing webcam");
            startButton.setEnabled(false);
        }
    }

    private void startCamera() {
        if (webcam == null) {
            statusLabel.setText("Status: ❌ No webcam available");
            return;
        }

        try {
            startButton.setEnabled(false);
            statusLabel.setText("Status: Opening camera...");

            // Ensure webcam is closed first
            try {
                if (webcam.isOpen()) {
                    webcam.close();
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                System.err.println("Error closing webcam: " + e.getMessage());
            }

            // Now open it
            webcam.open();
            isRunning = true;
            stopButton.setEnabled(true);
            statusLabel.setText("Status: ✓ Camera running - scanning for QR codes...");

            // Start capture and scan thread
            captureThread = new Thread(this::captureAndScan);
            captureThread.setDaemon(true);
            captureThread.start();

        } catch (Exception e) {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            statusLabel.setText("Status: ❌ Error opening camera");
            System.err.println("Camera error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void captureAndScan() {
        long lastScanTime = 0;
        long cooldown = 2000;

        while (isRunning && webcam.isOpen()) {
            try {
                // Capture frame
                BufferedImage frame = webcam.getImage();

                if (frame != null) {
                    // Display frame
                    SwingUtilities.invokeLater(() -> {
                        Image scaledImage = frame.getScaledInstance(cameraDisplayLabel.getWidth(), cameraDisplayLabel.getHeight(), Image.SCALE_FAST);
                        cameraDisplayLabel.setIcon(new ImageIcon(scaledImage));
                    });

                    // Try to scan QR code
                    try {
                        long now = System.currentTimeMillis();
                        if (now - lastScanTime >= cooldown) {
                            LuminanceSource source = new BufferedImageLuminanceSource(frame);
                            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                            Result result = qrReader.decode(bitmap);

                            lastScanTime = now;
                            String studentId = result.getText();
                            SwingUtilities.invokeLater(() -> recordAttendance(studentId));
                        }
                    } catch (NotFoundException e) {
                        // No QR code, continue scanning
                    }
                }

                Thread.sleep(50); // ~20 FPS

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                if (isRunning) {
                    System.err.println("Capture error: " + e.getMessage());
                }
            }
        }
    }

    private void recordAttendance(String studentId) {
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

            statusLabel.setText("Status: ✓ Scanned! Ready for next...");

        } else {
            resultArea.setText("✗ INVALID STUDENT\n\n" +
                    "Student ID: " + studentId + "\n" +
                    "Not registered in the system.");

            statusLabel.setText("Status: ✗ Invalid student ID");
        }
    }

    private void clearResult() {
        resultArea.setText("");
    }

    private void stopCamera() {
        try {
            isRunning = false;

            if (captureThread != null && captureThread.isAlive()) {
                captureThread.interrupt();
                captureThread.join(1000);
            }

            if (webcam != null && webcam.isOpen()) {
                webcam.close();
            }

            cameraDisplayLabel.setIcon(null);
            cameraDisplayLabel.setText("Camera stopped");
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            statusLabel.setText("Status: Ready");

        } catch (Exception e) {
            System.err.println("Error stopping camera: " + e.getMessage());
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }

    @Override
    public void removeNotify() {
        stopCamera();
        super.removeNotify();
    }
}