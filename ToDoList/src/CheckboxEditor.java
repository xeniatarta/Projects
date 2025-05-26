import javax.swing.*;
import java.awt.*;

class CheckboxEditor extends DefaultCellEditor {
    public CheckboxEditor() {
        super(new JCheckBox());
        JCheckBox checkBox = (JCheckBox) getComponent();
        checkBox.setHorizontalAlignment(SwingConstants.CENTER);
    }
}
