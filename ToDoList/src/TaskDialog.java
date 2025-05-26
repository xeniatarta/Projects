import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskDialog extends JDialog {
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JSpinner dueDateSpinner;
    private JButton saveButton;
    private Task task;

    public TaskDialog(JFrame parent, Task task) {
        super(parent, "Task Details", true);
        this.task = task;

        setLayout(new BorderLayout());
        setSize(400, 300);

        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        inputPanel.setBackground(new Color(255, 228, 225));

        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setForeground(new Color(255, 105, 180));
        inputPanel.add(titleLabel);
        titleField = new JTextField();
        titleField.setBackground(Color.WHITE);
        titleField.setForeground(Color.BLACK);
        inputPanel.add(titleField);

        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setForeground(new Color(255, 105, 180));
        inputPanel.add(descriptionLabel);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setBackground(Color.WHITE);
        descriptionArea.setForeground(Color.BLACK);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 105, 180)));
        inputPanel.add(descriptionScrollPane);

        JLabel dueDateLabel = new JLabel("Due Date:");
        dueDateLabel.setForeground(new Color(255, 105, 180));
        inputPanel.add(dueDateLabel);
        dueDateSpinner = new JSpinner(new SpinnerDateModel());
        dueDateSpinner.setEditor(new JSpinner.DateEditor(dueDateSpinner, "yyyy-MM-dd"));
        dueDateSpinner.getComponent(0).setBackground(Color.WHITE);
        dueDateSpinner.getComponent(0).setForeground(Color.BLACK);
        inputPanel.add(dueDateSpinner);

        add(inputPanel, BorderLayout.CENTER);


        saveButton = new JButton("Save");
        saveButton.setBackground(new Color(255, 105, 180)); // Hot Pink
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        saveButton.setFocusPainted(false);
        saveButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        saveButton.addActionListener(e -> saveTask());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(255, 228, 225));
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);

        if (task != null) {
            titleField.setText(task.getTitle());
            descriptionArea.setText(task.getDescription());
            dueDateSpinner.setValue(task.getDueDate() != null ? task.getDueDate() : new Date());
        }
    }

    private void saveTask() {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        Date dueDate = (Date) dueDateSpinner.getValue();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required.");
            return;
        }

        if (task == null) {
            ((MainFrame) getParent()).getTaskList().add(new Task(title, description, dueDate));
        } else {
            task.setTitle(title);
            task.setDescription(description);
            task.setDueDate(dueDate);
        }

        dispose();
    }
}