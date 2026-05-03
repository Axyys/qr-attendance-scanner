import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AttendanceRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String studentId;
    private LocalDateTime scanTime;
    private String status;

    public AttendanceRecord(String studentId, LocalDateTime scanTime, String status) {
        this.studentId = studentId;
        this.scanTime = scanTime;
        this.status = status;
    }

    public String getStudentId() {
        return studentId;
    }

    public LocalDateTime getScanTime() {
        return scanTime;
    }

    public String getStatus() {
        return status;
    }

    public String getFormattedDate() {
        return scanTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}