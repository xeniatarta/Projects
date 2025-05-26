package businesslogic;

import datamodel.Employee;
import datamodel.Task;

import java.util.*;
import java.util.stream.Collectors;

public class Utility {

    private static class EmployeeWorkDurationComparator implements Comparator<EmployeeWorkDuration> {
        @Override
        public int compare(EmployeeWorkDuration e1, EmployeeWorkDuration e2) {
            return Integer.compare(e1.getWorkDuration(), e2.getWorkDuration());
        }
    }

    public static List<String> filterEmployeesByWorkDuration(TaskManagement taskManagement, int threshold) {
        List<EmployeeWorkDuration> employeeWorkDurations = new ArrayList<>();

        for (Employee employee : taskManagement.getAllEmployees()) {
            int workDuration = taskManagement.calculateEmployeeWorkDuration(employee);

            if (workDuration > threshold) {
                employeeWorkDurations.add(new EmployeeWorkDuration(employee.getName(), workDuration));
            }
        }

        employeeWorkDurations.sort(new EmployeeWorkDurationComparator());

        List<String> filteredEmployees = new ArrayList<>();
        for (EmployeeWorkDuration employeeWorkDuration : employeeWorkDurations) {
            filteredEmployees.add(employeeWorkDuration.getName());
        }

        return filteredEmployees;
    }

    private static class EmployeeWorkDuration {
        private final String name;
        private final int workDuration;

        public EmployeeWorkDuration(String name, int workDuration) {
            this.name = name;
            this.workDuration = workDuration;
        }

        public String getName() {
            return name;
        }

        public int getWorkDuration() {
            return workDuration;
        }
    }


    public static Map<String, Map<String, Integer>> calculateTaskStatusCount(TaskManagement taskManagement) {
        Map<String, Map<String, Integer>> result = new HashMap<>();

        for (Employee employee : taskManagement.getAllEmployees()) {
            List<Task> tasks = taskManagement.getTasksForEmployee(employee);

            int completedCount = 0;
            int uncompletedCount = 0;

            for (Task task : tasks) {
                if ("Completed".equals(task.getStatusTask())) {
                    completedCount++;
                } else {
                    uncompletedCount++;
                }
            }

            Map<String, Integer> statusCount = new HashMap<>();
            statusCount.put("Completed", completedCount);
            statusCount.put("Uncompleted", uncompletedCount);

            result.put(employee.getName(), statusCount);
        }

        return result;
    }
}