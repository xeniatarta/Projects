package businesslogic;

import datamodel.Employee;
import datamodel.Task;

import java.util.*;
import java.io.Serializable;

public class TaskManagement implements Serializable {
    private static final long serialVersionUID = 1L;
    private HashMap<Employee, List<Task>> employeeTaskMap = new HashMap<>();

    public void addEmployee(Employee employee) {

        employeeTaskMap.putIfAbsent(employee, new ArrayList<>());
    }

    public void assignTaskToEmployee(Employee employee, Task task) {
        employeeTaskMap.computeIfAbsent(employee, k -> new ArrayList<>()).add(task);
    }

    public int calculateEmployeeWorkDuration(Employee employee) {
        List<Task> tasks = employeeTaskMap.getOrDefault(employee, Collections.emptyList());
        int totalDuration = 0;

        for (Task task : tasks) {
            if ("Completed".equals(task.getStatusTask())) {
                totalDuration += task.estimateDuration();
            }
        }

        return totalDuration;
    }

    public void modifyTaskStatus(Employee employee, int taskId, String newStatus) {
        List<Task> tasks = employeeTaskMap.getOrDefault(employee, Collections.emptyList());

        for (Task task : tasks) {
            if (task.getIdTask() == taskId) {
                task.setStatusTask(newStatus);
                break;
            }
        }
    }

    public List<Task> getTasksForEmployee(Employee employee) {
        return employeeTaskMap.getOrDefault(employee, Collections.emptyList());
    }

    public Set<Employee> getAllEmployees() {
        return employeeTaskMap.keySet();
    }

//    public HashMap<Employee, List<Task>> getEmployeeTaskMap() {
//
//        return employeeTaskMap;
//    }


}

