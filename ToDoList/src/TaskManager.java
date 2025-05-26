import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

class TaskManager {
    private static final String FILE_NAME = "tasks.csv";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public static void saveTasksToFile(ArrayList<Task> tasks) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Task task : tasks) {
                String line = serializeTask(task);
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Task> loadTasksFromFile() {
        ArrayList<Task> tasks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Task task = deserializeTask(line);
                if (task != null) {
                    tasks.add(task);
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    private static String serializeTask(Task task) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String dueDate = task.getDueDate() != null ? dateFormat.format(task.getDueDate()) : "";
        return String.format("%s,%s,%s,%b,%s",
                escapeCsv(task.getTitle()),
                escapeCsv(task.getDescription()),
                dueDate,
                task.isCompleted(),
                dateFormat.format(task.getCreationDate()));
    }

    private static Task deserializeTask(String line) {
        try {
            String[] parts = line.split(",", -1);
            if (parts.length != 5) return null;

            String title = unescapeCsv(parts[0]);
            String description = unescapeCsv(parts[1]);
            Date dueDate = parts[2].isEmpty() ? null : new SimpleDateFormat(DATE_FORMAT).parse(parts[2]);
            boolean status = Boolean.parseBoolean(parts[3]);
            Date creationDate = new SimpleDateFormat(DATE_FORMAT).parse(parts[4]);

            Task task = new Task(title, description, dueDate);
            task.setStatus(status);
            return task;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace(",", "\\,");
    }

    private static String unescapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\\,", ",");
    }
}