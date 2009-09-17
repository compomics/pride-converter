package no.uib.prideconverter.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * A table cell renderer for radio buttons.
 * 
 * @author  Harald Barsnes
 * 
 * Created June 2004
 */
public class RadioButtonRenderer implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null) {
            return null;
        }

        JRadioButton temp = (JRadioButton) value;

        if (isSelected) {
            temp.setBackground(table.getSelectionBackground());
        } else {
            temp.setBackground(Color.WHITE);
        }

        temp.setHorizontalAlignment(SwingConstants.CENTER);

        return temp;
    }
}
