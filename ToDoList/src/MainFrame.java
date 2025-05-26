import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {
    private ArrayList<Task> taskList = new ArrayList<>();
    private TaskTableModel tableModel;
    private JTable taskTable;
    private JTextField searchField;
    private JComboBox<String> filterComboBox;
    private JComboBox<String> sortComboBox;

    public MainFrame() {
        setTitle("To-Do List");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());


        JLabel titleLabel = new JLabel("To-Do List", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(248, 143, 195));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        add(titleLabel, BorderLayout.NORTH);

        tableModel = new TaskTableModel(taskList);
        taskTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(taskTable);


        taskTable.setBackground(new Color(255, 182, 193));
        taskTable.setForeground(Color.BLACK);
        taskTable.setFont(new Font("Arial", Font.PLAIN, 16));
        taskTable.setRowHeight(30);
        taskTable.getTableHeader().setBackground(new Color(252, 95, 173));
        taskTable.getTableHeader().setForeground(Color.WHITE);
        taskTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));
        taskTable.getColumnModel().getColumn(0).setCellRenderer(new CheckboxRenderer());
        taskTable.getColumnModel().getColumn(0).setCellEditor(new CheckboxEditor());

        add(scrollPane, BorderLayout.CENTER);


        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(255, 182, 193));
        buttonPanel.setLayout(new FlowLayout());

        JButton addButton = createStyledButton("Add Task");
        JButton editButton = createStyledButton("Edit Task");
        JButton deleteButton = createStyledButton("Delete Task");
        JButton saveButton = createStyledButton("Save Tasks");
        JButton loadButton = createStyledButton("Load Tasks");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);


        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(255, 182, 193));


        searchField = new JTextField(15);
        JButton searchButton = createStyledButton("Search");
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);

        filterComboBox = new JComboBox<>(new String[]{"All", "Pending", "Completed"});
        filterComboBox.setBackground(new Color(248, 143, 195)); // Pink
        filterComboBox.setForeground(Color.WHITE);
        topPanel.add(new JLabel("Filter:"));
        topPanel.add(filterComboBox);


        sortComboBox = new JComboBox<>(new String[]{"None", "Due Date", "Status", "Alphabetical"});
        sortComboBox.setBackground(new Color(248, 143, 195));
        sortComboBox.setForeground(Color.WHITE);
        topPanel.add(new JLabel("Sort:"));
        topPanel.add(sortComboBox);

        add(topPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);


        add(bottomPanel, BorderLayout.SOUTH);



        addButton.addActionListener(e -> openTaskDialog(null));
        editButton.addActionListener(e -> editSelectedTask(taskTable));
        deleteButton.addActionListener(e -> deleteSelectedTask(taskTable));
        saveButton.addActionListener(e -> saveTasks());
        loadButton.addActionListener(e -> loadTasks());
        searchButton.addActionListener(e -> refreshTable());
        filterComboBox.addActionListener(e -> refreshTable());
        sortComboBox.addActionListener(e -> refreshTable());

        refreshTable();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(248, 143, 195)); // Pink
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        return button;
    }

    private void openTaskDialog(Task task) {
        TaskDialog dialog = new TaskDialog(this, task);
        dialog.setVisible(true);
        refreshTable();
    }

    private void editSelectedTask(JTable taskTable) {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow >= 0) {
            Task selectedTask = tableModel.getTaskAt(taskTable.convertRowIndexToModel(selectedRow));
            openTaskDialog(selectedTask);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to edit.");
        }
    }

    private void deleteSelectedTask(JTable taskTable) {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow >= 0) {
            taskList.remove(tableModel.getTaskAt(taskTable.convertRowIndexToModel(selectedRow)));
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to delete.");
        }
    }

    private void refreshTable() {
        String searchText = searchField.getText().toLowerCase();
        String filter = (String) filterComboBox.getSelectedItem();
        String sort = (String) sortComboBox.getSelectedItem();

        ArrayList<Task> filteredTasks = (ArrayList<Task>) taskList.stream()
                .filter(task -> {
                    boolean matchesSearch = task.getTitle().toLowerCase().contains(searchText) ||
                            (task.getDescription() != null && task.getDescription().toLowerCase().contains(searchText));
                    boolean matchesFilter = filter.equals("All") ||
                            (filter.equals("Pending") && !task.isCompleted()) ||
                            (filter.equals("Completed") && task.isCompleted());
                    return matchesSearch && matchesFilter;
                })
                .collect(Collectors.toList());

        if (!sort.equals("None")) {
            Comparator<Task> comparator = null;
            switch (sort) {
                case "Due Date":
                    comparator = Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()));
                    break;
                case "Status":
                    comparator = Comparator.comparing(Task::isCompleted);
                    break;
                case "Alphabetical":
                    comparator = Comparator.comparing(Task::getTitle);
                    break;
            }
            filteredTasks.sort(comparator);
        }

        tableModel.setTaskList(filteredTasks);
        tableModel.fireTableDataChanged();
    }

    private void saveTasks() {
        TaskManager.saveTasksToFile(taskList);
        JOptionPane.showMessageDialog(this, "Tasks saved successfully.");
    }

    private void loadTasks() {
        taskList = TaskManager.loadTasksFromFile();
        refreshTable();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    public ArrayList<Task> getTaskList() {
        return taskList;
    }
}