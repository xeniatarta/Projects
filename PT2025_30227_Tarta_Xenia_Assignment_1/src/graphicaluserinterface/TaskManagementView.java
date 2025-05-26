package graphicaluserinterface;

import javax.swing.*;
import java.awt.*;

public class TaskManagementView extends JFrame {

    public JTextField employeeIdField, employeeNameField;
    public JButton addEmployeeButton;

    public JTextField assignEmployeeNameField, taskIdFieldAssign, startHourField, endHourField;
    public JComboBox<String> taskTypeComboBox, taskStatusComboBox;
    public JButton addTaskButton;

    public JTextField taskIdField;
    public JComboBox<String> statusComboBox;
    public JButton modifyStatusButton;
    public JTextField durationThresholdField;
    public JButton filterButton, statusCountButton;
    public JTextField workDurationEmployeeField;
    public JButton workDurationButton;

    public JButton serializeButton;
    public JButton deserializeButton;

    public JTextArea employeeTaskDisplay;
    private JScrollPane scrollPane;

    public TaskManagementView() {
        setTitle("Task Management System");
        setSize(800, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel employeePanel = new JPanel();
        employeePanel.setLayout(new BoxLayout(employeePanel, BoxLayout.Y_AXIS));
        employeePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add Employee"));

        employeeIdField = new JTextField();
        employeeNameField = new JTextField();
        addEmployeeButton = new JButton("Add Employee");

        inputPanel.add(new JLabel("Employee ID:"));
        inputPanel.add(employeeIdField);
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(employeeNameField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addEmployeeButton);

        JPanel serializationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        serializeButton = new JButton("Serialize Data");
        deserializeButton = new JButton("Deserialize Data");
        serializationPanel.add(serializeButton);
        serializationPanel.add(deserializeButton);

        employeePanel.add(inputPanel);
        employeePanel.add(buttonPanel);
        employeePanel.add(serializationPanel);

        tabbedPane.addTab("Employee Management", employeePanel);

        JPanel taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        taskPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Assign Task"));

        assignEmployeeNameField = new JTextField();
        taskIdFieldAssign = new JTextField();
        taskTypeComboBox = new JComboBox<>(new String[]{"SimpleTask, ComplexTask"});
        startHourField = new JTextField();
        endHourField = new JTextField();
        taskStatusComboBox = new JComboBox<>(new String[]{"Completed", "Uncompleted"});
        addTaskButton = new JButton("Add Task");

        formPanel.add(new JLabel("Employee Name:"));
        formPanel.add(assignEmployeeNameField);
        formPanel.add(new JLabel("Task ID:"));
        formPanel.add(taskIdFieldAssign);
        formPanel.add(new JLabel("Task Type:"));
        formPanel.add(taskTypeComboBox);
        formPanel.add(new JLabel("Start Hour:"));
        formPanel.add(startHourField);
        formPanel.add(new JLabel("End Hour:"));
        formPanel.add(endHourField);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(taskStatusComboBox);

        JPanel addTaskPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addTaskPanel.add(addTaskButton);

        taskPanel.add(formPanel);
        taskPanel.add(addTaskPanel);

        tabbedPane.addTab("Task Assignment", taskPanel);

        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel modifyPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        modifyPanel.setBorder(BorderFactory.createTitledBorder("Modify Task Status"));

        taskIdField = new JTextField();
        statusComboBox = new JComboBox<>(new String[]{"Completed", "Uncompleted"});
        modifyStatusButton = new JButton("Modify Status");

        modifyPanel.add(new JLabel("Task ID:"));
        modifyPanel.add(taskIdField);
        modifyPanel.add(new JLabel("New Status:"));
        modifyPanel.add(statusComboBox);

        JPanel modifyButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modifyButtonPanel.add(modifyStatusButton);

        JPanel filterPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter by Work Duration"));
        durationThresholdField = new JTextField();
        filterButton = new JButton("Filter");
        filterPanel.add(durationThresholdField);
        filterPanel.add(filterButton);

        JPanel statusCountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusCountButton = new JButton("Show Task Status Count");
        statusCountPanel.add(statusCountButton);

        JPanel workDurationPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        workDurationPanel.setBorder(BorderFactory.createTitledBorder("Employee Work Duration"));
        workDurationEmployeeField = new JTextField();
        workDurationButton = new JButton("Calculate");
        workDurationPanel.add(workDurationEmployeeField);
        workDurationPanel.add(workDurationButton);

        actionPanel.add(modifyPanel);
        actionPanel.add(modifyButtonPanel);
        actionPanel.add(filterPanel);
        actionPanel.add(statusCountPanel);
        actionPanel.add(workDurationPanel);

        tabbedPane.addTab("Task Actions", actionPanel);

        employeeTaskDisplay = new JTextArea();
        employeeTaskDisplay.setEditable(false);
        scrollPane = new JScrollPane(employeeTaskDisplay);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Employees & Tasks"));
        tabbedPane.addTab("Task Display", scrollPane);

        add(tabbedPane);
        setVisible(true);
    }

    public void updateEmployeeTaskDisplay(String data) {

        employeeTaskDisplay.setText(data);
    }

    public void showMessage(String message) {

        JOptionPane.showMessageDialog(this, message);
    }
}
