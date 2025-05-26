package graphicaluserinterface;


import businesslogic.TaskManagement;
import businesslogic.Utility;
import dataaccess.SerealizationOperations;
import datamodel.ComplexTask;
import datamodel.Employee;
import datamodel.SimpleTask;
import datamodel.Task;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

public class TaskManagementController {
    private TaskManagement model;
    private TaskManagementView view;
   // private ComplexTask complexTask_model;

    public TaskManagementController(TaskManagement model, TaskManagementView view) {
        this.model = model;
        this.view = view;

        view.modifyStatusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modifyTaskStatus();
            }
        });

        view.filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterEmployeesByWorkDuration();
            }
        });

        view.statusCountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateTaskStatusCount();
            }
        });

        view.addEmployeeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addEmployee();
            }
        });

        view.addTaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTaskToEmployee();
            }

        });

        view.workDurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateWorkDuration();
            }
        });


        view.serializeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                serializeData();
            }
        });

        view.deserializeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deserializeData();
            }
        });

    }

    private void addEmployee() {
        try {
            int id = Integer.parseInt(view.employeeIdField.getText().trim());
            String name = view.employeeNameField.getText().trim();

            if (name.isEmpty()) {
                view.showMessage("Employee name cannot be empty.");
                return;
            }

            Employee employee = new Employee(id, name);
            model.addEmployee(employee);
            updateEmployeeTaskList();
            view.showMessage("Employee added: " + name);
        } catch (NumberFormatException e) {
            view.showMessage("Invalid Employee ID.");
        }
    }

    private void updateEmployeeTaskList() {
        StringBuilder display = new StringBuilder("Employees & Tasks:\n");
        for (Employee employee : model.getAllEmployees()) {
            display.append(employee.getName()).append(" (ID: ").append(employee.getIdEmployee()).append(")\n");
            List<Task> tasks = model.getTasksForEmployee(employee);
            if (tasks.isEmpty()) {
                display.append("  No tasks assigned\n");
            } else {
                for (Task task : tasks) {
                    display.append("  Task ID: ").append(task.getIdTask()).append(", Status: ").append(task.getStatusTask()).append("\n");
                }
            }
        }
        view.updateEmployeeTaskDisplay(display.toString());
    }


    private void addTaskToEmployee() {
        try {
            String employeeName = view.assignEmployeeNameField.getText().trim();
            int taskId = Integer.parseInt(view.taskIdFieldAssign.getText().trim());
            String taskType = (String) view.taskTypeComboBox.getSelectedItem();
            String status = (String) view.taskStatusComboBox.getSelectedItem();

            Employee employee = getEmployeeByName(employeeName);
            if (employee == null) {
                view.showMessage("Employee not found.");
                return;
            }

            Task task;
            if ("SimpleTask".equals(taskType)) {
                int startHour = Integer.parseInt(view.startHourField.getText().trim());
                int endHour = Integer.parseInt(view.endHourField.getText().trim());

                if (startHour >= endHour) {
                    view.showMessage("Start hour must be less than end hour.");
                    return;
                }

                task = new SimpleTask(startHour, endHour, taskId, status);
            } else {
                task = new ComplexTask(taskId, status);
            }

            model.assignTaskToEmployee(employee, task);
            updateEmployeeTaskList();
            view.showMessage("Task assigned to " + employeeName);
        } catch (NumberFormatException e) {
            view.showMessage("Please enter valid numeric values for Task ID / Hours.");
        }
    }

    private void modifyTaskStatus() {
        try {
            String employeeName = view.employeeNameField.getText().trim();
            int taskId = Integer.parseInt(view.taskIdField.getText().trim());
            String newStatus = (String) view.statusComboBox.getSelectedItem();

            Employee employee = getEmployeeByName(employeeName);
            if (employee == null) {
                view.showMessage("Employee not found.");
                return;
            }

            model.modifyTaskStatus(employee, taskId, newStatus);
            updateEmployeeTaskList();
            view.showMessage("Task status updated for employee: " + employeeName);
        } catch (NumberFormatException e) {
            view.showMessage("Invalid Task ID.");
        }
    }

    private void filterEmployeesByWorkDuration() {
        try {
            int threshold = Integer.parseInt(view.durationThresholdField.getText().trim());
            List<String> filteredEmployees = Utility.filterEmployeesByWorkDuration(model, threshold);

            if (filteredEmployees.isEmpty()) {
                view.showMessage("No employees found over duration threshold.");
            } else {
                StringBuilder result = new StringBuilder("Employees over threshold:\n");
                for (String name : filteredEmployees) {
                    result.append(name).append("\n");
                }
                view.showMessage(result.toString());
            }
        } catch (NumberFormatException e) {
            view.showMessage("Invalid threshold value.");
        }
    }

    private void calculateTaskStatusCount() {
        Map<String, Map<String, Integer>> statusCounts = Utility.calculateTaskStatusCount(model);
        StringBuilder result = new StringBuilder("Task Status Counts per Employee:\n");

        for (String name : statusCounts.keySet()) {
            Map<String, Integer> counts = statusCounts.get(name);
            result.append(name)
                    .append(" - Completed: ").append(counts.get("Completed"))
                    .append(", Uncompleted: ").append(counts.get("Uncompleted"))
                    .append("\n");
        }

        view.showMessage(result.toString());
    }

    private Employee getEmployeeByName(String name) {
        for (Employee emp : model.getAllEmployees()) {
            if (emp.getName().equalsIgnoreCase(name)) {
                return emp;
            }
        }
        return null;
    }

    private void calculateWorkDuration() {
        String employeeName = view.workDurationEmployeeField.getText().trim();
        Employee employee = getEmployeeByName(employeeName);

        if (employee == null) {
            view.showMessage("Employee not found.");
            return;
        }

        int totalDuration = model.calculateEmployeeWorkDuration(employee);
        view.showMessage("Total work duration (Completed tasks only) for " + employeeName + ": " + totalDuration + " hours.");
    }

    private void serializeData() {
        String filename = "task_management.ser";
        SerealizationOperations.serializeTaskManagement(model, filename);
        view.showMessage("Data serialized successfully to " + filename);
    }

    private void deserializeData() {
        String filename = "task_management.ser";
        TaskManagement deserializedModel = SerealizationOperations.deserializeTaskManagement(filename);

        if (deserializedModel != null) {
            this.model = deserializedModel;
            view.showMessage("Data deserialized successfully from " + filename);
            updateEmployeeTaskList();
        } else {
            view.showMessage("Failed to deserialize data.");
        }
    }
}



