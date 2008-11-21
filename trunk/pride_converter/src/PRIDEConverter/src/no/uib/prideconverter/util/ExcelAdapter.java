package no.uib.prideconverter.util;

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.util.StringTokenizer;
import javax.swing.table.DefaultTableModel;

/**
 * ExcelAdapter enables Copy-Paste Clipboard functionality on JTables.
 * The clipboard data format used by the adapter is compatible with
 * the clipboard format used by Excel. This provides for clipboard
 * interoperability between enabled JTables and Excel.
 * 
 * Created June 2004
 */
public class ExcelAdapter implements ActionListener {

    private String rowstring,  value;
    private Clipboard system;
    private StringSelection stsel;
    private JTable jTable1;
    int rowSelection = 0;
    int columnSelection = 1;
    int cellSelection = 2;
    int selectionType = 0;
    boolean debug = false;
    boolean appendColumnNames = false;
    boolean insertRowsWhenPasting = true;

    /**
     * Creates a new ExcelAdapter-object
     */
    public ExcelAdapter() {
        super();
    }

    /**
     * The Excel Adapter is constructed with a
     * JTable on which it enables Copy-Paste and acts
     * as a Clipboard listener.
     *
     * @param myJTable
     */
    public ExcelAdapter(JTable myJTable) {
        jTable1 = myJTable;
        selectionType = 0;
        // Identifying the copy KeyStroke user can modify this
        // to copy on some other Key combination.
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);

        // Identifying the Paste KeyStroke user can modify this
        //to copy on some other Key combination.
        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);

        jTable1.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED);
        jTable1.registerKeyboardAction(this, "Paste", paste, JComponent.WHEN_FOCUSED);
        system = Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    /**
     * Sets the selectiontype
     * 0 = rowselection
     * 1 = columnselection
     * 2 = cellselection
     *
     * @param type
     */
    public void setSelectionType(int type) {
        selectionType = type;
    }

    /**
     * Returns the selectiontype
     * 0 = rowselection
     * 1 = columnselection
     * 2 = cellselection
     *
     * @return type
     */
    public int getSelectionType() {
        return selectionType;
    }

    /**
     * Set if rows should be inserted when pasting. False means overwirte.
     * 
     * @param insertRowsWhenPasting
     */
    public void setInsertRowsWhenPasting(boolean insertRowsWhenPasting) {
        this.insertRowsWhenPasting = insertRowsWhenPasting;
    }

    /**
     * This method is activated on the Keystrokes we are listening to
     * in this implementation. Here it listens for Copy and Paste ActionCommands.
     * Selections comprising non-adjacent cells result in invalid selection and
     * then copy action cannot be performed.
     * Paste is done by aligning the upper left corner of the selection with the
     * 1st element in the current selection of the JTable.
     *
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareTo("Copy") == 0) {
            StringBuffer sbf = new StringBuffer();

            // Check to ensure we have selected only a continguous block of cells
            int numcols = jTable1.getSelectedColumnCount();
            int numrows = jTable1.getSelectedRowCount();
            int[] rowsselected = jTable1.getSelectedRows();
            int[] colsselected = jTable1.getSelectedColumns();

            if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0] &&
                    numrows == rowsselected.length) &&
                    (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0] &&
                    numcols == colsselected.length))) {
                JOptionPane.showMessageDialog(null, "You have to select a continguous block of cells",
                        "Invalid Copy Selection",
                        JOptionPane.ERROR_MESSAGE);//FIX ME???
                return;
            }

            String temp = "";

            for (int l = 0; l < numrows; l++) {
                for (int m = 0; m < numcols; m++) {
                    if (jTable1.getValueAt(rowsselected[l], colsselected[m]) != null) {

                        sbf.append(jTable1.getValueAt(rowsselected[l], colsselected[m]));

                    } else {
                        sbf.append("");
                    }
                    if (m < numcols - 1) {
                        sbf.append("\t");
                    }
                }
                sbf.append("\n");

                stsel = new StringSelection(sbf.toString());
                system = Toolkit.getDefaultToolkit().getSystemClipboard();
                system.setContents(stsel, stsel);

            }
        }

        if (e.getActionCommand().compareTo("Paste") == 0) {


            jTable1.getParent().getParent().getParent().setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));


            String table = "";

            if (debug) {
                System.out.println("Trying to Paste");
            }
            int startRow = 0;//(jTable1.getSelectedRows())[0];
            int startCol = 0;//(jTable1.getSelectedColumns())[0];

            while (jTable1.getRowCount() > 0) {
                ((DefaultTableModel) jTable1.getModel()).removeRow(0);
            }

            try {
                String trstring = (String) (system.getContents(this).getTransferData(DataFlavor.stringFlavor));
                if (debug) {
                    System.out.println("String is: " + trstring);
                }
                StringTokenizer st1 = new StringTokenizer(trstring, "\n");

                if (insertRowsWhenPasting) {

                    for (int i = 0; i < st1.countTokens(); i++) {
                        ((DefaultTableModel) jTable1.getModel()).addRow(
                                new Object[]{null, null});
                    }
                }

                for (int i = 0; st1.hasMoreTokens(); i++) {

                    rowstring = st1.nextToken();
                    StringTokenizer st2 = new StringTokenizer(rowstring, "\t");

                    for (int j = 0; st2.hasMoreTokens(); j++) {
                        value = st2.nextToken();
                        if (j < jTable1.getColumnCount()) {
                            jTable1.setValueAt(value, i, j);
                        }
                        
                        if (debug) {
                            System.out.println("Putting " + value + "at row=" + (startRow + i) + "column=" + (startCol + j));
                        }
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "An error occured during pasting. Perhaps you had an empty selection? \n" + "See ../Properties/ErrorLog.txt for more details.");
                ex.printStackTrace();

                ((DefaultTableModel) jTable1.getModel()).addRow(new Object[]{null, null});
            }

            jTable1.getParent().getParent().getParent().setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Returns the Table on which this adapter acts.
     *
     * @return JTable
     */
    public JTable getJTable() {
        return jTable1;
    }

    /**
     * Set the Table on which this adapter acts.
     *
     * @param jTable1
     */
    public void setJTable(JTable jTable1) {
        this.jTable1 = jTable1;
    }

    /**
     * Copies the selected cells
     */
    public void copySelected() {
        StringBuffer sbf = new StringBuffer();

        // Check to ensure we have selected only a continguous block of cells
        int numcols = jTable1.getSelectedColumnCount();
        int numrows = jTable1.getSelectedRowCount();
        int[] rowsselected = jTable1.getSelectedRows();
        int[] colsselected = jTable1.getSelectedColumns();

        if (numcols != 0 && numrows != 0) {

            if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0] &&
                    numrows == rowsselected.length) &&
                    (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0] &&
                    numcols == colsselected.length))) {
                JOptionPane.showMessageDialog(null, "You have to select a continguous block of cells",
                        "Invalid Copy Selection",
                        JOptionPane.ERROR_MESSAGE);//FIX ME???
                return;
            }

            String temp = "";

            for (int l = 0; l < numrows; l++) {
                for (int m = 0; m < numcols; m++) {
                    if (jTable1.getValueAt(rowsselected[l], colsselected[m]) != null) {

                        sbf.append(jTable1.getValueAt(rowsselected[l], colsselected[m]));

                    } else {
                        sbf.append("");
                    }
                    if (m < numcols - 1) {
                        sbf.append("\t");
                    }
                }
                sbf.append("\n");

                stsel = new StringSelection(sbf.toString());
                system = Toolkit.getDefaultToolkit().getSystemClipboard();
                system.setContents(stsel, stsel);
            }

        }
    }
}
