package dataaccess;

import businesslogic.TaskManagement;
import datamodel.Employee;
import datamodel.Task;

import java.io.*;

public class SerealizationOperations {
    public static void serializeTaskManagement(TaskManagement taskManagement, String filename) {
        try (FileOutputStream fileOut = new FileOutputStream(filename);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(taskManagement);
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static TaskManagement deserializeTaskManagement(String filename) {
        TaskManagement taskManagement = null;
        try (FileInputStream fileIn = new FileInputStream(filename);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            taskManagement = (TaskManagement) in.readObject();
        } catch (IOException | ClassNotFoundException i) {
            i.printStackTrace();
        }
        return taskManagement;
    }
}