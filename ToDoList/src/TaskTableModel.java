import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

class TaskTableModel extends AbstractTableModel {
    private ArrayList<Task> taskList;
    private final String[] columns = {"Completed", "Title", "Description", "Due Date"};

    public TaskTableModel(ArrayList<Task> taskList) {
        this.taskList = taskList;
    }

    public void setTaskList(ArrayList<Task> taskList) {
        this.taskList = taskList;
    }

    public Task getTaskAt(int rowIndex) {
        return taskList.get(rowIndex);
    }
    @Override
    public int getRowCount() {
        return taskList.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Task task = taskList.get(rowIndex);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        switch (columnIndex) {
            case 0:
                return task.isCompleted();
            case 1:
                return task.getTitle();
            case 2:
                return task.getDescription();
            case 3:
                return task.getDueDate() == null ? "N/A" : dateFormat.format(task.getDueDate());
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            taskList.get(rowIndex).setStatus((Boolean) aValue);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Boolean.class;
        }
        return String.class;
    }
}

