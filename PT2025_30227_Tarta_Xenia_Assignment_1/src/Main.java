import businesslogic.TaskManagement;
import graphicaluserinterface.TaskManagementController;
import graphicaluserinterface.TaskManagementView;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TaskManagement model = new TaskManagement();
            TaskManagementView view = new TaskManagementView();
            new TaskManagementController(model, view);
            view.setVisible(true);
        });
    }
}
