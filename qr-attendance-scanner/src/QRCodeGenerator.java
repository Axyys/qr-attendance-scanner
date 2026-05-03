import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;

public class QRCodeGenerator {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;

    public static void generateQRCode(String data, String filePath) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    data,
                    BarcodeFormat.QR_CODE,
                    WIDTH,
                    HEIGHT
            );

            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            System.out.println("✓ QR Code generated: " + filePath);

        } catch (Exception e) {
            System.out.println("Error generating QR code: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== QR Code Generator ===");
        System.out.println("Loading students from database...\n");

        // Create database to load saved students
        AttendanceDatabase database = new AttendanceDatabase();
        Collection<Student> students = database.getAllStudents();

        if (students.isEmpty()) {
            System.out.println("No students found in database.");
            System.out.println("Adding default students...\n");

            // Add default students if none exist
            database.addStudent(new Student("STU001", "John Doe", "john@school.edu"));
            database.addStudent(new Student("STU002", "Jane Smith", "jane@school.edu"));
            database.addStudent(new Student("STU003", "Mike Johnson", "mike@school.edu"));
            database.addStudent(new Student("STU004", "Sarah Williams", "sarah@school.edu"));
            database.addStudent(new Student("STU005", "Tom Brown", "tom@school.edu"));

            students = database.getAllStudents();
        }

        // Generate QR codes for all students
        System.out.println("Generating QR codes for " + students.size() + " students:\n");

        for (Student student : students) {
            String filename = "qr_" + student.getStudentId().toLowerCase() + ".png";
            generateQRCode(student.getStudentId(), filename);
            System.out.println("  - " + student.getName() + " (" + student.getStudentId() + ")");
        }

        System.out.println("\n✓ All QR codes generated successfully!");
        System.out.println("Students saved: " + students.size());
    }
}