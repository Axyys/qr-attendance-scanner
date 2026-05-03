import java.io.*;
import java.util.*;

public class DataManager {
    private static final String STUDENTS_FILE = "students.dat";
    private static final String ATTENDANCE_FILE = "attendance.dat";

    // Save students to file
    public static void saveStudents(Collection<Student> students) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STUDENTS_FILE))) {
            oos.writeObject(new ArrayList<>(students));
            System.out.println("[INFO] Students saved to file");
        } catch (Exception e) {
            System.err.println("Error saving students: " + e.getMessage());
        }
    }

    // Load students from file
    public static List<Student> loadStudents() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(STUDENTS_FILE))) {
            List<Student> students = (List<Student>) ois.readObject();
            System.out.println("[INFO] Students loaded from file: " + students.size());
            return students;
        } catch (FileNotFoundException e) {
            System.out.println("[INFO] No previous students found");
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error loading students: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Save attendance records to file
    public static void saveAttendance(List<AttendanceRecord> records) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ATTENDANCE_FILE))) {
            oos.writeObject(new ArrayList<>(records));
            System.out.println("[INFO] Attendance records saved to file");
        } catch (Exception e) {
            System.err.println("Error saving attendance: " + e.getMessage());
        }
    }

    // Load attendance records from file
    public static List<AttendanceRecord> loadAttendance() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ATTENDANCE_FILE))) {
            List<AttendanceRecord> records = (List<AttendanceRecord>) ois.readObject();
            System.out.println("[INFO] Attendance records loaded from file: " + records.size());
            return records;
        } catch (FileNotFoundException e) {
            System.out.println("[INFO] No previous attendance records found");
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error loading attendance: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Clear all data files
    public static void clearAllData() {
        try {
            new File(STUDENTS_FILE).delete();
            new File(ATTENDANCE_FILE).delete();
            System.out.println("[INFO] All data cleared");
        } catch (Exception e) {
            System.err.println("Error clearing data: " + e.getMessage());
        }
    }
}