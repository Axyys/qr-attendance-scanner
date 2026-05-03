import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private AttendanceDatabase database;
    private JTable studentsTable;
    private DefaultTableModel tableModel;
    private JTextField studentIdField;
    private JTextField nameField;
    private JTextField emailField;

    public SettingsPanel(AttendanceDatabase database) {
        this.database = database;
        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(240, 240, 240));

        // Add student panel
        JPanel addPanel = new JPanel(new GridBagLayout());
        addPanel.setBackground(new Color(240, 240, 240));
        addPanel.setBorder(BorderFactory.createTitledBorder("Add New Student"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Student ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        addPanel.add(new JLabel("Student ID:"), gbc);
        gbc.gridx = 1;
        studentIdField = new JTextField(15);
        addPanel.add(studentIdField, gbc);

        // Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        addPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        addPanel.add(nameField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        addPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(15);
        addPanel.add(emailField, gbc);

        // Add button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton addButton = new JButton("Add Student");
        addButton.setBackground(new Color(34, 139, 34));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> addStudent());
        addPanel.add(addButton, gbc);

        // Students table
        String[] columnNames = {"Student ID", "Name", "Email", "Enrollment Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentsTable = new JTable(tableModel);
        studentsTable.setFont(new Font("Arial", Font.PLAIN, 11));
        studentsTable.setRowHeight(25);
        studentsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        studentsTable.getTableHeader().setBackground(new Color(0, 102, 204));
        studentsTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(studentsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Registered Students"));

        add(addPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        updateStudentsList();
    }

    private void addStudent() {
        String studentId = studentIdField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();

        if (studentId.isEmpty() || name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (database.isValidStudent(studentId)) {
            JOptionPane.showMessageDialog(this, "Student ID already exists", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Student student = new Student(studentId, name, email);
        database.addStudent(student);

        studentIdField.setText("");
        nameField.setText("");
        emailField.setText("");

        updateStudentsList();
        JOptionPane.showMessageDialog(this, "Student added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStudentsList() {
        tableModel.setRowCount(0);
        for (Student student : database.getAllStudents()) {
            tableModel.addRow(new Object[]{
                    student.getStudentId(),
                    student.getName(),
                    student.getEmail(),
                    student.getEnrollmentDate()
            });
        }
    }
}