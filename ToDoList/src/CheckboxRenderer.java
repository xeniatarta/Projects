import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

class CheckboxRenderer extends JCheckBox implements TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setSelected((Boolean) value);
        setHorizontalAlignment(CENTER);
        return this;
    }
}