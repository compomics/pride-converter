package no.uib.prideconverter.gui;

import no.uib.prideconverter.PRIDEConverter;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import no.uib.olsdialog.OLSDialog;
import no.uib.olsdialog.OLSInputable;
import no.uib.prideconverter.util.CvMappingProperties;
import no.uib.prideconverter.util.CvMappingReader;
import uk.ac.ebi.pride.model.implementation.mzData.CvParamImpl;

/**
 * A dialog where information about a sample can be inserted.
 *
 * @author  Harald Barsnes
 * 
 * Created March 2008
 */
public class NewSample extends javax.swing.JDialog implements OLSInputable {

    private SampleDetails sampleDetails;
    private int modifiedRow = -1;
    private Map<String,List<String>> preselectedOntologyTerms;

    /**
     * Opens a new NewSample dialog.
     * 
     * @param sampleDetails a reference to the SampleDetails frame
     * @param modal
     */
    public NewSample(SampleDetails sampleDetails, boolean modal) {
        super(sampleDetails, modal);
        CvMappingProperties prop = new CvMappingProperties();
        CvMappingReader cvMappingReader = new CvMappingReader(prop.getDefaultMappingFile());
        preselectedOntologyTerms = cvMappingReader.getPreselectedTerms(prop.getPropertyValue("sampleKeyword"));


        this.sampleDetails = sampleDetails;

        initComponents();

        // only works for Java 1.6 and newer
//        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
//                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        cvTermsJTable.getTableHeader().setReorderingAllowed(false);
        cvTermsJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        cvTermsJTable.getColumn(" ").setMinWidth(40);
        cvTermsJTable.getColumn(" ").setMaxWidth(40);
        cvTermsJTable.setRowHeight(cvTermsJTable.getFont().getSize() + PRIDEConverter.getProperties().tableRowHeightPaddingSize);

        setLocationRelativeTo(sampleDetails);
        setVisible(true);
    }

    /**
     * Opens a new NewSample dialog
     * 
     * @param sampleDetails a referenve to the SampleDetails frame
     * @param modal
     * @param modifiedRow the row to edit, -1 if adding new row
     */
    public NewSample(SampleDetails sampleDetails, boolean modal, int modifiedRow) {
        super(sampleDetails, modal);

        this.sampleDetails = sampleDetails;
        this.modifiedRow = modifiedRow;

        initComponents();

        // only works for Java 1.6 and newer
//        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
//                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        this.okJButton.setEnabled(true);

        if (modifiedRow != -1) {

            Object[] cvParams = PRIDEConverter.getProperties().getSampleDescriptionCVParams().toArray();
            CvParamImpl cvParam;

            int rowCounter = 1;

            for (int i = 0; i < cvParams.length; i++) {

                cvParam = (CvParamImpl) cvParams[i];

                if (cvParam.getValue().equalsIgnoreCase("SUBSAMPLE_" +
                        (modifiedRow + 1))) {
                    ((DefaultTableModel) cvTermsJTable.getModel()).addRow(new Object[]{
                        new Integer(rowCounter++),
                        cvParam.getName(),
                        cvParam.getAccession(),
                        cvParam.getCVLookup(),
                    });
                }
            }

            Object[] tempUserParams = 
                    PRIDEConverter.getProperties().getSampleDescriptionUserSubSampleNames().toArray();

            sampleNameJTextField.setText("" + tempUserParams[modifiedRow]);
        }

        setTitle("Sample " + (modifiedRow + 1));

        cvTermsJTable.getTableHeader().setReorderingAllowed(false);

        cvTermsJTable.getColumn(" ").setMinWidth(40);
        cvTermsJTable.getColumn(" ").setMaxWidth(40);

        setLocationRelativeTo(sampleDetails);
        setVisible(true);
    }

    /**
     * Adds a sample
     * 
     * @param name
     * @param accession
     * @param ontology
     * @param modifiedRow the row to edit, -1 if adding new row
     */
    public void addSample(String name, String accession, String ontology, int modifiedRow) {

        if (modifiedRow == -1) {

            ((DefaultTableModel) this.cvTermsJTable.getModel()).addRow(
                    new Object[]{
                new Integer(cvTermsJTable.getRowCount() + 1),
                name, accession, ontology
            });
        } else {
            cvTermsJTable.setValueAt(name, modifiedRow, 1);
            cvTermsJTable.setValueAt(accession, modifiedRow, 2);
            cvTermsJTable.setValueAt(ontology, modifiedRow, 3);
        }


        okJButton.setEnabled(sampleNameJTextField.getText().length() > 0);
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
        jLabel1 = new javax.swing.JLabel();
        sampleNameJTextField = new javax.swing.JTextField();
        okJButton = new javax.swing.JButton();
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
        setTitle("New Sample");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        protocolStepJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Sample"));

        cvTermsJTable.setFont(cvTermsJTable.getFont());
        cvTermsJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "CV Term", "Accession", "Ontology"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
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

        jLabel1.setText("Name:");

        sampleNameJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                sampleNameJTextFieldKeyReleased(evt);
            }
        });

        org.jdesktop.layout.GroupLayout protocolStepJPanelLayout = new org.jdesktop.layout.GroupLayout(protocolStepJPanel);
        protocolStepJPanel.setLayout(protocolStepJPanelLayout);
        protocolStepJPanelLayout.setHorizontalGroup(
            protocolStepJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, protocolStepJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(protocolStepJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, protocolStepJPanelLayout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sampleNameJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, olsSearchJButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE))
                .addContainerGap())
        );
        protocolStepJPanelLayout.setVerticalGroup(
            protocolStepJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(protocolStepJPanelLayout.createSequentialGroup()
                .add(protocolStepJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(sampleNameJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(olsSearchJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        okJButton.setText("OK");
        okJButton.setEnabled(false);
        okJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okJButtonActionPerformed(evt);
            }
        });

        cancelJButton.setText("Cancel");
        cancelJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelJButtonActionPerformed(evt);
            }
        });

        helpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/help.GIF"))); // NOI18N
        helpJButton.setToolTipText("Help");
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
                    .add(protocolStepJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 231, Short.MAX_VALUE)
                        .add(okJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 117, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 116, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(protocolStepJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(cancelJButton)
                        .add(okJButton))
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
        String ontologyTitle = PRIDEConverter.getUserProperties().getLastSelectedSampleOntology();
        String newtRoot = PRIDEConverter.getUserProperties().getNewtRoot();
        if(newtRoot.indexOf(ontologyTitle) != -1){
            ontologyTitle = newtRoot;
        }
        new OLSDialog(this, this, true, "sample", ontologyTitle, null, preselectedOntologyTerms);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_olsSearchJButtonActionPerformed

    /**
     * Inserts the sample into the SampleDetails frame.
     * 
     * @param evt
     */
    private void okJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okJButtonActionPerformed

        if (cvTermsJTable.isEditing()) {
            JOptionPane.showMessageDialog(this, 
                    "Table is still being edited. Complete the editing before inserting.",
                    "Table Is Being Edited", JOptionPane.INFORMATION_MESSAGE);
        } else {

            Vector terms = new Vector();
            Vector accessions = new Vector();
            Vector ontologies = new Vector();

            for (int i = 0; i < cvTermsJTable.getRowCount(); i++) {
                terms.add(cvTermsJTable.getValueAt(i, 1));
                accessions.add(cvTermsJTable.getValueAt(i, 2));
                ontologies.add(cvTermsJTable.getValueAt(i, 3));
            }

            // ToDo: check that sample name is unique!!

            sampleDetails.addSampleDetails(
                    sampleNameJTextField.getText(), terms, accessions, ontologies, modifiedRow);

            this.setVisible(false);
            this.dispose();
        }
}//GEN-LAST:event_okJButtonActionPerformed

    /**
     * @see #cancelJButtonActionPerformed(java.awt.event.ActionEvent)
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

        String msNewtRoot = PRIDEConverter.getUserProperties().getNewtRoot();
        if(msNewtRoot.indexOf(ontology) != -1){
            ontology = msNewtRoot;
        }
        new OLSDialog(this, this, true, "sample", ontology, selectedRow, searchTerm, preselectedOntologyTerms);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_editJMenuItemActionPerformed

    /**
     * Moved the selected row one step up in the table.
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
     * Moved the selected row one step down in the table.
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
                this.okJButton.setEnabled(false);
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
     * Opens a help frame.
     * 
     * @param evt
     */
    private void helpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, true, getClass().getResource("/no/uib/prideconverter/helpfiles/NewSample.html"));
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
     * Enables or disables the OK button.
     * 
     * @param evt
     */
    private void sampleNameJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sampleNameJTextFieldKeyReleased
        okJButton.setEnabled(sampleNameJTextField.getText().length() > 0 &&
                cvTermsJTable.getRowCount() > 0);
    }//GEN-LAST:event_sampleNameJTextFieldKeyReleased

    /**
     * If the delete key is clicked the selected rows in the table are removed.
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JMenuItem moveDownJMenuItem;
    private javax.swing.JMenuItem moveUpJMenuItem;
    private javax.swing.JButton okJButton;
    private javax.swing.JButton olsSearchJButton;
    private javax.swing.JPopupMenu popupJMenu;
    private javax.swing.JPanel protocolStepJPanel;
    private javax.swing.JTextField sampleNameJTextField;
    // End of variables declaration//GEN-END:variables
    /**
     * @see OLSInputable
     */
    public void insertOLSResult(String field, String selectedValue, String accession,
            String ontologyShort, String ontologyLong, int modifiedRow, String mappedTerm, Map<String, String> metadata) {

        PRIDEConverter.getUserProperties().setLastSelectedSampleOntology(ontologyLong);
        addSample(selectedValue, accession, ontologyShort, modifiedRow);
    }

    /**
     * @see OLSInputable
     */
    public Window getWindow() {
        return (Window) this;
    }
}
