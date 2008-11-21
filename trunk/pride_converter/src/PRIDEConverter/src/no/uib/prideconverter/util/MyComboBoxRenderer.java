package no.uib.prideconverter.util;

import java.awt.Component;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * A combo box renderer that allows tooltip for each element in the combo 
 * box list.
 *
 * @author  Harald Barsnes
 * 
 * Created March 2008
 */
public class MyComboBoxRenderer extends BasicComboBoxRenderer {

    private Vector tooltips;

    /**
     * Creates a new instance of the MyComboBoxRenderer.
     * 
     * @param tooltips vector containg the tooltips
     */
    public MyComboBoxRenderer(Vector tooltips) {
        this.tooltips = tooltips;
    }

    /**
     * Set the tooltips.
     * 
     * @param tooltips vector containg the tooltips
     */
    public void setToolTips(Vector tooltips) {
        this.tooltips = tooltips;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
            if (-1 < index && index < tooltips.size()) {

                if (tooltips.get(index) != null) {
   
                    String toolTip = (String) tooltips.get(index);
                    StringTokenizer tok = new StringTokenizer(toolTip);
                    String temp = "", temp2 = "";

                    while (tok.hasMoreTokens()) {
                        temp += tok.nextToken() + " ";

                        if (temp.length() > 40) {
                            temp2 += temp + "<br>";
                            temp = "";
                        }
                    }
                    
                    if(temp.length() > 0){
                        temp2 += temp;
                    }

                    list.setToolTipText("<html>" + temp2 + "</html>");
                } else {
                    list.setToolTipText(null);
                }
            }
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setFont(list.getFont());
        setText((value == null) ? "" : value.toString());
        return this;
    }
}