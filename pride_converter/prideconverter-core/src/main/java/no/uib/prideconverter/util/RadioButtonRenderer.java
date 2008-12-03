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

    /** 
     * Method overridden from TableCellRenderer
     * 
     * @param table
     * @param value
     * @param isSelected
     * @param hasFocus
     * @param row
     * @param column
     * @return Component
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null) {
            return null;
        }
        
        return (Component) value;
    }
}
