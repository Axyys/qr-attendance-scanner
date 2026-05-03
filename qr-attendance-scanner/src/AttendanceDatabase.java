import java.util.*;

public class AttendanceDatabase {
    private Map<String, Student> students;
    private List<AttendanceRecord> attendanceRecords;

    public AttendanceDatabase() {
        this.students = new HashMap<>();
        this.attendanceRecords = new ArrayList<>();
        loadData();
    }

    private void loadData() {
        // Load students
        List<Student> loadedStudents = DataManager.loadStudents();
        for (Student student : loadedStudents) {
            students.put(student.getStudentId(), student);
        }

        // Load attendance records
        attendanceRecords = DataManager.loadAttendance();
    }

    public void addStudent(Student student) {
        students.put(student.getStudentId(), student);
        saveStudents();
    }

    public void recordAttendance(AttendanceRecord record) {
        attendanceRecords.add(record);
        saveAttendance();
        System.out.println("[DEBUG] Attendance recorded. Total records: " + attendanceRecords.size());
    }

    public boolean isValidStudent(String studentId) {
        return students.containsKey(studentId);
    }

    public Student getStudent(String studentId) {
        return students.get(studentId);
    }

    public List<AttendanceRecord> getAttendanceRecords() {
        return new ArrayList<>(attendanceRecords);
    }

    public Collection<Student> getAllStudents() {
        return students.values();
    }

    public int getTotalRecords() {
        return attendanceRecords.size();
    }

    public void clearAllRecords() {
        attendanceRecords.clear();
        saveAttendance();
    }

    private void saveStudents() {
        DataManager.saveStudents(students.values());
    }

    private void saveAttendance() {
        DataManager.saveAttendance(attendanceRecords);
    }

    public void generateReport() {
        System.out.println("\n=== Attendance Report ===");
        if (attendanceRecords.isEmpty()) {
            System.out.println("No attendance records found.");
            return;
        }

        Map<String, Integer> attendance = new HashMap<>();
        for (AttendanceRecord record : attendanceRecords) {
            attendance.put(record.getStudentId(),
                    attendance.getOrDefault(record.getStudentId(), 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : attendance.entrySet()) {
            Student student = students.get(entry.getKey());
            if (student != null) {
                System.out.println(student.getName() + " (" + entry.getKey() +
                        "): " + entry.getValue() + " attendance(s)");
            }
        }
    }
}