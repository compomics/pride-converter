package no.uib.prideconverter.gui;

import no.uib.prideconverter.PRIDEConverter;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import no.uib.olsdialog.OLSDialog;
import no.uib.olsdialog.OLSInputable;
import no.uib.prideconverter.util.CvMappingProperties;
import no.uib.prideconverter.util.CvMappingReader;
import uk.ac.ebi.pride.model.implementation.mzData.AnalyzerImpl;
import uk.ac.ebi.pride.model.implementation.mzData.CvParamImpl;

/**
 * A dialog for inserting information about an analyzer.
 *
 * @author  Harald Barsnes
 * 
 * Created March 2008
 */
public class NewAnalyzer extends javax.swing.JDialog implements OLSInputable {

    private Instrument instrument;
    private int modifiedRow = -1;
    private AnalyzerImpl analyzer;
    private Map<String, List<String>> preselectedOntologyTerms;

    /**
     * Opens a NewAnalyzer dialog.
     * 
     * @param instrument a reference to the Instrument frame
     * @param modal
     */
    public NewAnalyzer(Instrument instrument, boolean modal) {
        super(instrument, modal);
        CvMappingProperties prop = new CvMappingProperties();
        CvMappingReader cvMappingReader = new CvMappingReader(prop.getDefaultMappingFile());
        preselectedOntologyTerms = cvMappingReader.getPreselectedTerms(prop.getPropertyValue("instrumentAnalyzerKeyword"));

        this.instrument = instrument;

        initComponents();

        // only works for Java 1.6 and newer
//        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
//                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        cvTermsJTable.getTableHeader().setReorderingAllowed(false);
        cvTermsJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        cvTermsJTable.getColumn(" ").setMinWidth(40);
        cvTermsJTable.getColumn(" ").setMaxWidth(40);
        cvTermsJTable.setRowHeight(cvTermsJTable.getFont().getSize() + PRIDEConverter.getProperties().tableRowHeightPaddingSize);

        setLocationRelativeTo(instrument);
        setVisible(true);
    }

    /**
     * Opens a NewAnalyzer dialog.
     * 
     * @param instrument a reference to the Instrument frame
     * @param modal
     * @param modifiedRow the row to edit, -1 if adding new row
     * @param analyzer
     */
    public NewAnalyzer(Instrument instrument, boolean modal, int modifiedRow, AnalyzerImpl analyzer) {
        super(instrument, modal);

        this.instrument = instrument;
        this.modifiedRow = modifiedRow;
        this.analyzer = analyzer;

        initComponents();

        cvTermsJTable.getTableHeader().setReorderingAllowed(false);

        // only works for Java 1.6 and newer
//        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
//                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        this.insertJButton.setEnabled(true);

        Iterator cvTerms = analyzer.getAnalyzerCvParameterList().iterator();
        CvParamImpl cvParams;
        int rowCounter = 0;

        while (cvTerms.hasNext()) {
            cvParams = (CvParamImpl) cvTerms.next();

            ((DefaultTableModel) cvTermsJTable.getModel()).addRow(new Object[]{
                        new Integer(++rowCounter),
                        cvParams.getName(),
                        cvParams.getAccession(),
                        cvParams.getCVLookup(),
                        cvParams.getValue()
                    });
        }


        setTitle("Analyzer " + (modifiedRow + 1));

        cvTermsJTable.getColumn(" ").setMinWidth(40);
        cvTermsJTable.getColumn(" ").setMaxWidth(40);

        setLocationRelativeTo(instrument);
        setVisible(true);
    }

    /**
     * Add an analyzer to the table.
     * 
     * @param name
     * @param accession
     * @param ontology
     * @param modifiedRow the row to edit, -1 if adding new row
     */
    public void addAnalyzer(String name, String accession, String ontology, int modifiedRow) {

        if (modifiedRow == -1) {
            ((DefaultTableModel) this.cvTermsJTable.getModel()).addRow(
                    new Object[]{
                        new Integer(cvTermsJTable.getRowCount() + 1),
                        name, accession, ontology, null
                    });
        } else {
            cvTermsJTable.setValueAt(name, modifiedRow, 1);
            cvTermsJTable.setValueAt(accession, modifiedRow, 2);
            cvTermsJTable.setValueAt(ontology, modifiedRow, 3);
            cvTermsJTable.setValueAt(null, modifiedRow, 4);
        }

        insertJButton.setEnabled(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupJMenu = new javax.swing.JPopupMenu();
        editJMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        moveUpJMenuItem = new javax.swing.JMenuItem();
        moveDownJMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        deleteSelectedRowJMenuItem = new javax.swing.JMenuItem();
        protocolStepJPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        cvTermsJTable = new javax.swing.JTable();
        olsSearchJButton = new javax.swing.JButton();
        insertJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        helpJButton = new javax.swing.JButton();
        aboutJButton = new javax.swing.JButton();

        editJMenuItem.setMnemonic('E');
        editJMenuItem.setText("Edit");
        editJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editJMenuItemActionPerformed(evt);
            }
        });
        popupJMenu.add(editJMenuItem);
        popupJMenu.add(jSeparator3);

        moveUpJMenuItem.setMnemonic('U');
        moveUpJMenuItem.setText("Move Up");
        moveUpJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpJMenuItemActionPerformed(evt);
            }
        });
        popupJMenu.add(moveUpJMenuItem);

        moveDownJMenuItem.setMnemonic('D');
        moveDownJMenuItem.setText("Move Down");
        moveDownJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownJMenuItemActionPerformed(evt);
            }
        });
        popupJMenu.add(moveDownJMenuItem);
        popupJMenu.add(jSeparator4);

        deleteSelectedRowJMenuItem.setText("Delete");
        deleteSelectedRowJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSelectedRowJMenuItemActionPerformed(evt);
            }
        });
        popupJMenu.add(deleteSelectedRowJMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("New Analyzer");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        protocolStepJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Analyzer"));

        cvTermsJTable.setFont(cvTermsJTable.getFont());
        cvTermsJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "CV Term", "Accession", "Ontology", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        cvTermsJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cvTermsJTableMouseClicked(evt);
            }
        });
        cvTermsJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                cvTermsJTableKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(cvTermsJTable);

        olsSearchJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/ols_transparent.GIF"))); // NOI18N
        olsSearchJButton.setText("Ontology Lookup Service");
        olsSearchJButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        olsSearchJButton.setPreferredSize(new java.awt.Dimension(85, 23));
        olsSearchJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                olsSearchJButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout protocolStepJPanelLayout = new org.jdesktop.layout.GroupLayout(protocolStepJPanel);
        protocolStepJPanel.setLayout(protocolStepJPanelLayout);
        protocolStepJPanelLayout.setHorizontalGroup(
            protocolStepJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(protocolStepJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(protocolStepJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, olsSearchJButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE))
                .addContainerGap())
        );
        protocolStepJPanelLayout.setVerticalGroup(
            protocolStepJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, protocolStepJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(olsSearchJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        insertJButton.setText("OK");
        insertJButton.setEnabled(false);
        insertJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertJButtonActionPerformed(evt);
            }
        });

        cancelJButton.setText("Cancel");
        cancelJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelJButtonActionPerformed(evt);
            }
        });

        helpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/help.GIF"))); // NOI18N
        helpJButton.setToolTipText("Find Folder");
        helpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpJButtonActionPerformed(evt);
            }
        });

        aboutJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF"))); // NOI18N
        aboutJButton.setToolTipText("About");
        aboutJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutJButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 245, Short.MAX_VALUE)
                        .add(insertJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(protocolStepJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(protocolStepJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(cancelJButton)
                        .add(insertJButton))
                    .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Opens an OLS dialog.
     * 
     * @param evt
     */
    private void olsSearchJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_olsSearchJButtonActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        String ontologyTitle = PRIDEConverter.getUserProperties().getLastSelectedOntology();
        String msAnalyzer = PRIDEConverter.getUserProperties().getMsAnalyzer();
        if(msAnalyzer.indexOf(ontologyTitle) != -1){
            ontologyTitle = msAnalyzer;
        }
        new OLSDialog(this, this, true, "analyzers", ontologyTitle, null, preselectedOntologyTerms);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_olsSearchJButtonActionPerformed

    /**
     * Inserts the analyzer into the analyzer table in the Instrument frame.
     * 
     * @param evt
     */
    private void insertJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertJButtonActionPerformed

        if (cvTermsJTable.isEditing()) {
            JOptionPane.showMessageDialog(this,
                    "Table is still being edited. Complete the editing before inserting.",
                    "Table Is Being Edited", JOptionPane.INFORMATION_MESSAGE);
        } else {

            Vector terms, accessions, values, ontologies;

            terms = new Vector();
            accessions = new Vector();
            ontologies = new Vector();
            values = new Vector();

            for (int i = 0; i < cvTermsJTable.getRowCount(); i++) {
                terms.add(cvTermsJTable.getValueAt(i, 1));
                accessions.add(cvTermsJTable.getValueAt(i, 2));
                ontologies.add(cvTermsJTable.getValueAt(i, 3));
                values.add(cvTermsJTable.getValueAt(i, 4));
            }

            instrument.insertAnalyzer(
                    terms, accessions, ontologies, values, modifiedRow);

            this.setVisible(false);
            this.dispose();
        }
    }//GEN-LAST:event_insertJButtonActionPerformed

    /**
     * Closes the dialog.
     * 
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_formWindowClosing

    /**
     * Opens an OLS dialog.
     * 
     * @param evt
     */
    private void editJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editJMenuItemActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        int selectedRow = cvTermsJTable.getSelectedRow();

        String searchTerm = (String) cvTermsJTable.getValueAt(selectedRow, 1);
        String ontology = (String) cvTermsJTable.getValueAt(selectedRow, 3);

        searchTerm = searchTerm.replaceAll("-", " ");
        searchTerm = searchTerm.replaceAll(":", " ");
        searchTerm = searchTerm.replaceAll("\\(", " ");
        searchTerm = searchTerm.replaceAll("\\)", " ");
        searchTerm = searchTerm.replaceAll("&", " ");
        searchTerm = searchTerm.replaceAll("\\+", " ");
        searchTerm = searchTerm.replaceAll("\\[", " ");
        searchTerm = searchTerm.replaceAll("\\]", " ");

        String msAnalyzer = PRIDEConverter.getUserProperties().getMsAnalyzer();
        if(msAnalyzer.indexOf(ontology) != -1){
            ontology = msAnalyzer;
        }
        new OLSDialog(this, this, true, "analyzers", ontology, selectedRow, searchTerm, preselectedOntologyTerms);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_editJMenuItemActionPerformed

    /**
     * Moves the selected row one step up in the table.
     * 
     * @param evt
     */
    private void moveUpJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpJMenuItemActionPerformed
        int selectedRow = cvTermsJTable.getSelectedRow();
        int selectedColumn = cvTermsJTable.getSelectedColumn();

        Object[] tempRow = new Object[]{
            cvTermsJTable.getValueAt(selectedRow - 1, 0),
            cvTermsJTable.getValueAt(selectedRow - 1, 1),
            cvTermsJTable.getValueAt(selectedRow - 1, 2),
            cvTermsJTable.getValueAt(selectedRow - 1, 3),
            cvTermsJTable.getValueAt(selectedRow - 1, 4)
        };

        ((DefaultTableModel) cvTermsJTable.getModel()).removeRow(selectedRow - 1);
        ((DefaultTableModel) cvTermsJTable.getModel()).insertRow(selectedRow, tempRow);

        cvTermsJTable.changeSelection(selectedRow - 1, selectedColumn, false, false);

        fixIndices();
    }//GEN-LAST:event_moveUpJMenuItemActionPerformed

    /**
     * Moves the selected row one step down in the table.
     * 
     * @param evt
     */
    private void moveDownJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownJMenuItemActionPerformed
        int selectedRow = cvTermsJTable.getSelectedRow();
        int selectedColumn = cvTermsJTable.getSelectedColumn();

        Object[] tempRow = new Object[]{
            cvTermsJTable.getValueAt(selectedRow + 1, 0),
            cvTermsJTable.getValueAt(selectedRow + 1, 1),
            cvTermsJTable.getValueAt(selectedRow + 1, 2),
            cvTermsJTable.getValueAt(selectedRow + 1, 3),
            cvTermsJTable.getValueAt(selectedRow + 1, 4)
        };

        ((DefaultTableModel) cvTermsJTable.getModel()).removeRow(selectedRow + 1);
        ((DefaultTableModel) cvTermsJTable.getModel()).insertRow(selectedRow, tempRow);

        cvTermsJTable.changeSelection(selectedRow + 1, selectedColumn, false, false);

        fixIndices();
    }//GEN-LAST:event_moveDownJMenuItemActionPerformed

    /**
     * Delete the selected row
     * 
     * @param evt
     */
    private void deleteSelectedRowJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSelectedRowJMenuItemActionPerformed
        if (cvTermsJTable.getSelectedRow() != -1) {
            ((DefaultTableModel) cvTermsJTable.getModel()).removeRow(cvTermsJTable.getSelectedRow());

            if (cvTermsJTable.getRowCount() == 0) {
                this.insertJButton.setEnabled(false);
            }
        }
    }//GEN-LAST:event_deleteSelectedRowJMenuItemActionPerformed

    /**
     * Right clicking in the table will open a popup menu, while double 
     * clicking will open an OLS dialog to change the selected row.
     * 
     * @param evt
     */
    private void cvTermsJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cvTermsJTableMouseClicked
        if (evt.getButton() == 3) {

            int row = cvTermsJTable.rowAtPoint(evt.getPoint());
            int column = cvTermsJTable.columnAtPoint(evt.getPoint());

            cvTermsJTable.changeSelection(row, column, false, false);

            //makes sure only valid "moving options" are enabled
            this.moveUpJMenuItem.setEnabled(true);
            this.moveDownJMenuItem.setEnabled(true);

            if (row == cvTermsJTable.getRowCount() - 1) {
                this.moveDownJMenuItem.setEnabled(false);
            }

            if (row == 0) {
                this.moveUpJMenuItem.setEnabled(false);
            }

            popupJMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        } else if (evt.getButton() == 1 && evt.getClickCount() == 2) {
            editJMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_cvTermsJTableMouseClicked

    /**
     * Closes the dialog.
     * 
     * @param evt
     */
    private void cancelJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_cancelJButtonActionPerformed

    /**
     * Opens a Help dialog.
     * 
     * @param evt
     */
    private void helpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, true, getClass().getResource("/no/uib/prideconverter/helpfiles/NewAnalyzer.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    /**
     * Opens an About PRIDE Converter dialog.
     * 
     * @param evt
     */
    private void aboutJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, true, getClass().getResource("/no/uib/prideconverter/helpfiles/AboutPRIDE_Converter.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_aboutJButtonActionPerformed

    /**
     * If the delete button is clicked the selected rows in the table are 
     * removed.
     * 
     * @param evt
     */
    private void cvTermsJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cvTermsJTableKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            deleteSelectedRowJMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_cvTermsJTableKeyReleased

    /**
     * Fixes the indices so that they are in accending order starting from one
     */
    private void fixIndices() {
        for (int row = 0; row < ((DefaultTableModel) cvTermsJTable.getModel()).getRowCount(); row++) {
            ((DefaultTableModel) cvTermsJTable.getModel()).setValueAt(new Integer(row + 1), row, 0);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JTable cvTermsJTable;
    private javax.swing.JMenuItem deleteSelectedRowJMenuItem;
    private javax.swing.JMenuItem editJMenuItem;
    private javax.swing.JButton helpJButton;
    private javax.swing.JButton insertJButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JMenuItem moveDownJMenuItem;
    private javax.swing.JMenuItem moveUpJMenuItem;
    private javax.swing.JButton olsSearchJButton;
    private javax.swing.JPopupMenu popupJMenu;
    private javax.swing.JPanel protocolStepJPanel;
    // End of variables declaration//GEN-END:variables

    /**
     * @see OLSInputable
     */
    public void insertOLSResult(String field, String selectedValue, String accession,
            String ontologyShort, String ontologyLong, int modifiedRow, String mappedTerm, Map<String, String> metadata) {

        PRIDEConverter.getUserProperties().setLastSelectedOntology(ontologyLong);
        addAnalyzer(selectedValue, accession, ontologyShort, modifiedRow);
    }

    /**
     * @see OLSInputable
     */
    public Window getWindow() {
        return (Window) this;
    }
}
