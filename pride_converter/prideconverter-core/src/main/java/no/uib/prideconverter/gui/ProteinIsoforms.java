package no.uib.prideconverter.gui;

import no.uib.prideconverter.PRIDEConverter;
import java.util.HashMap;
import javax.swing.table.DefaultTableModel;
import no.uib.prideconverter.util.ExcelAdapter;

/**
 * This dialog provides the user with three different alternatives for handeling 
 * the protein isoforms: (i) always select the first one, (ii) manual selection, 
 * or (iii) by providing a list of peptide to protein matches.
 *
 * @author  Harald Barsnes
 * 
 * Created June 2008
 */
public class ProteinIsoforms extends javax.swing.JDialog {

    private ProgressDialog progressDialog;
//    private PRIDEConverter prideConverter;
    private OutputDetails outputDetails;
    private ExcelAdapter excelAdapter;

    /**
     * Opens a new ProteinIsoforms dialog.
     * 
     * @param parent
     * @param modal
     * @param progressDialog
     */
    public ProteinIsoforms(java.awt.Frame parent, boolean modal, ProgressDialog progressDialog) {
        super(parent, modal);
        initComponents();

        outputDetails = (OutputDetails) parent;
        this.progressDialog = progressDialog;
//        this.prideConverter = outputDetails.getPRIDEConverterReference();

        excelAdapter = new ExcelAdapter(peptideToProteinJXTable);

        peptideToProteinJXTable.getTableHeader().setReorderingAllowed(false);

        // only works for Java 1.6 and newer
//        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
//                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        setLocationRelativeTo(outputDetails);
        setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        tableJPopupMenu = new javax.swing.JPopupMenu();
        pasteJMenuItem = new javax.swing.JMenuItem();
        deleteJMenuItem = new javax.swing.JMenuItem();
        addRowJMenuItem = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        manualSelectionJRadioButton = new javax.swing.JRadioButton();
        selectFirstJRadioButton = new javax.swing.JRadioButton();
        provideListJRadioButton = new javax.swing.JRadioButton();
        peptideToProteinJLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        peptideToProteinJScrollPane2 = new javax.swing.JScrollPane();
        peptideToProteinJXTable = new org.jdesktop.swingx.JXTable();
        cancelJButton = new javax.swing.JButton();
        okJButton = new javax.swing.JButton();
        aboutJButton = new javax.swing.JButton();
        helpJButton = new javax.swing.JButton();

        pasteJMenuItem.setText("Paste (Replaces the Current Content)");
        pasteJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pasteJMenuItemActionPerformed(evt);
            }
        });
        tableJPopupMenu.add(pasteJMenuItem);

        deleteJMenuItem.setText("Delete Selected Rows");
        deleteJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteJMenuItemActionPerformed(evt);
            }
        });
        tableJPopupMenu.add(deleteJMenuItem);

        addRowJMenuItem.setText("Add Empty Row");
        addRowJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRowJMenuItemActionPerformed(evt);
            }
        });
        tableJPopupMenu.add(addRowJMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Protein Isoforms Detected");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Protein Isoforms"));

        jLabel1.setFont(jLabel1.getFont().deriveFont((jLabel1.getFont().getStyle() | java.awt.Font.ITALIC), jLabel1.getFont().getSize()-2));
        jLabel1.setText("How do you want to handle protein isoforms?");

        buttonGroup.add(manualSelectionJRadioButton);
        manualSelectionJRadioButton.setText("Manual Selection");
        manualSelectionJRadioButton.setIconTextGap(20);
        manualSelectionJRadioButton.setPreferredSize(new java.awt.Dimension(13, 13));
        manualSelectionJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualSelectionJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup.add(selectFirstJRadioButton);
        selectFirstJRadioButton.setText("Always Select The First Isoform");
        selectFirstJRadioButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        selectFirstJRadioButton.setIconTextGap(20);
        selectFirstJRadioButton.setPreferredSize(new java.awt.Dimension(13, 13));
        selectFirstJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectFirstJRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup.add(provideListJRadioButton);
        provideListJRadioButton.setText("Provide a List of Peptide to Protein Matches");
        provideListJRadioButton.setIconTextGap(20);
        provideListJRadioButton.setPreferredSize(new java.awt.Dimension(13, 13));
        provideListJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                provideListJRadioButtonActionPerformed(evt);
            }
        });

        peptideToProteinJLabel.setText("Peptide to Protein Matches:");

        jLabel5.setFont(jLabel5.getFont().deriveFont((jLabel5.getFont().getStyle() | java.awt.Font.ITALIC), jLabel5.getFont().getSize()-2));
        jLabel5.setText("(Right click in the table to insert data.)");

        peptideToProteinJScrollPane2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                peptideToProteinJScrollPane2MouseClicked(evt);
            }
        });

        peptideToProteinJXTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Peptide Sequence", "Protein Accession"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        peptideToProteinJXTable.setOpaque(false);
        peptideToProteinJXTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                peptideToProteinJXTableKeyReleased(evt);
            }
        });
        peptideToProteinJXTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                peptideToProteinJXTableMouseClicked(evt);
            }
        });
        peptideToProteinJScrollPane2.setViewportView(peptideToProteinJXTable);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(14, 14, 14)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(manualSelectionJRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 183, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(selectFirstJRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(provideListJRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 303, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(38, 38, 38))
                    .add(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(peptideToProteinJScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(peptideToProteinJLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 62, Short.MAX_VALUE)
                                .add(jLabel5))))
                    .add(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {manualSelectionJRadioButton, provideListJRadioButton, selectFirstJRadioButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .add(18, 18, 18)
                .add(selectFirstJRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(manualSelectionJRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(provideListJRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(peptideToProteinJLabel)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(peptideToProteinJScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {manualSelectionJRadioButton, provideListJRadioButton, selectFirstJRadioButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        cancelJButton.setText("Cancel");
        cancelJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelJButtonActionPerformed(evt);
            }
        });

        okJButton.setText("OK");
        okJButton.setEnabled(false);
        okJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okJButtonActionPerformed(evt);
            }
        });

        aboutJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF"))); // NOI18N
        aboutJButton.setToolTipText("About");
        aboutJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutJButtonActionPerformed(evt);
            }
        });

        helpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/help.GIF"))); // NOI18N
        helpJButton.setToolTipText("Help");
        helpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpJButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 222, Short.MAX_VALUE)
                        .add(okJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 78, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
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
     * @see #cancelJButtonActionPerformed(java.awt.event.ActionEvent)
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelJButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    /**
     * Closes the dialog and cancels the conversion.
     * 
     * @param evt
     */
    private void cancelJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
        PRIDEConverter.setCancelConversion(true);
        progressDialog.setVisible(false);
        progressDialog.dispose();
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_cancelJButtonActionPerformed

    /**
     * Sends the selected choice to the main class (PRIDEConverter) and closes 
     * the dialog.
     * 
     * @param evt
     */
    private void okJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okJButtonActionPerformed

        if (selectFirstJRadioButton.isSelected()) {
            PRIDEConverter.getProperties().setProteinIsoformSelectionType(
                    PRIDEConverter.getProperties().PROTEIN_ISOFORMS_ALWAYS_SELECT_FIRST);
        } else if (manualSelectionJRadioButton.isSelected()) {
            PRIDEConverter.getProperties().setProteinIsoformSelectionType(
                    PRIDEConverter.getProperties().PROTEIN_ISOFORMS_MANUAL_SELECTION);
        } else {
            PRIDEConverter.getProperties().setProteinIsoformSelectionType(
                    PRIDEConverter.getProperties().PROTEIN_ISOFORMS_PROVIDE_LIST);

            PRIDEConverter.getProperties().setSelectedIsoforms(new HashMap<String, String>());

            for (int i = 0; i < peptideToProteinJXTable.getRowCount(); i++) {
                PRIDEConverter.getProperties().getSelectedProteinIsoforms().put(
                        (String) peptideToProteinJXTable.getValueAt(i, 0),
                        (String) peptideToProteinJXTable.getValueAt(i, 1));
            }
        }

        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_okJButtonActionPerformed

    /**
     * Enables or disables the OK button when the "provide list" option is 
     * used, based on if a list has been provided or not.
     * 
     * @param evt
     */
    private void provideListJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_provideListJRadioButtonActionPerformed
        if (provideListJRadioButton.isSelected()) {
            if (peptideToProteinJXTable.getRowCount() > 0) {
                okJButton.setEnabled(true);
            } else {
                okJButton.setEnabled(false);
            }
        } else {
            if (selectFirstJRadioButton.isSelected() ||
                    manualSelectionJRadioButton.isSelected()) {
                okJButton.setEnabled(true);
            } else {
                okJButton.setEnabled(false);
            }
        }
    }//GEN-LAST:event_provideListJRadioButtonActionPerformed

    /**
     * @see #provideListJRadioButtonActionPerformed(java.awt.event.ActionEvent)
     */
    private void selectFirstJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectFirstJRadioButtonActionPerformed
        provideListJRadioButtonActionPerformed(null);
    }//GEN-LAST:event_selectFirstJRadioButtonActionPerformed

    /**
     * @see #provideListJRadioButtonActionPerformed(java.awt.event.ActionEvent)
     */
    private void manualSelectionJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualSelectionJRadioButtonActionPerformed
        provideListJRadioButtonActionPerformed(null);
    }//GEN-LAST:event_manualSelectionJRadioButtonActionPerformed

    /**
     * Opens a popup menu that lets the user paste peptide to protein matches 
     * into the table.
     * 
     * @param evt
     */
    /**
     * Pastes the list of peptide to protein matches into the table.
     * 
     * @param evt
     */
    private void pasteJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pasteJMenuItemActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        excelAdapter.actionPerformed(new java.awt.event.ActionEvent(this, 0, "Paste"));
        provideListJRadioButtonActionPerformed(null);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_pasteJMenuItemActionPerformed

    /**
     * Opens a popup menu that lets the user paste peptide to protein matches 
     * into the table.
     * 
     * @param evt
     */
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
     * Opens a Help dialog.
     * 
     * @param evt
     */
    private void helpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, true, getClass().getResource("/no/uib/prideconverter/helpfiles/ProteinIsoforms.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    /**
     * Deletes the selected rows from the peptide to protein table.
     *
     * @param evt
     */
    private void deleteJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteJMenuItemActionPerformed
        int[] selectedRows = peptideToProteinJXTable.getSelectedRows();

        for (int i = selectedRows.length - 1; i >= 0; i--) {
            ((DefaultTableModel) peptideToProteinJXTable.getModel()).removeRow(selectedRows[i]);
        }

        if (provideListJRadioButton.isSelected()) {
            okJButton.setEnabled(peptideToProteinJXTable.getRowCount() > 0);
        }
    }//GEN-LAST:event_deleteJMenuItemActionPerformed

    /**
     * Adds an empty row to the peptide to protein table.
     *
     * @param evt
     */
    private void addRowJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRowJMenuItemActionPerformed

        if (peptideToProteinJXTable.getSelectedRow() != -1) {
            ((DefaultTableModel) peptideToProteinJXTable.getModel()).insertRow(
                    peptideToProteinJXTable.getSelectedRow() + 1, new Object[]{"", ""});

            peptideToProteinJXTable.scrollRectToVisible(peptideToProteinJXTable.getCellRect(
                peptideToProteinJXTable.getSelectedRow() + 1, 0, false));

        } else {
            ((DefaultTableModel) peptideToProteinJXTable.getModel()).addRow(new Object[]{"", ""});

            peptideToProteinJXTable.scrollRectToVisible(peptideToProteinJXTable.getCellRect(
                peptideToProteinJXTable.getRowCount() - 1, 0, false));
        }
    }//GEN-LAST:event_addRowJMenuItemActionPerformed

    /**
     * Opens a popup menu that lets the user paste peptide to protein matches 
     * into the table.
     * 
     * @param evt
     */
    private void peptideToProteinJScrollPane2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_peptideToProteinJScrollPane2MouseClicked
        if (evt.getButton() == 3) {
            tableJPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_peptideToProteinJScrollPane2MouseClicked

    /**
     * Opens a popup menu that lets the user paste peptide to protein matches 
     * into the table.
     * 
     * @param evt
     */
    private void peptideToProteinJXTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_peptideToProteinJXTableMouseClicked
        if (evt.getButton() == 3) {
            tableJPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_peptideToProteinJXTableMouseClicked

    /**
     * Enables or disables the OK button.
     * 
     * @param evt
     */
    private void peptideToProteinJXTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_peptideToProteinJXTableKeyReleased
        if (provideListJRadioButton.isSelected()) {
            okJButton.setEnabled(peptideToProteinJXTable.getRowCount() > 0);
        }
    }//GEN-LAST:event_peptideToProteinJXTableKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JMenuItem addRowJMenuItem;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JMenuItem deleteJMenuItem;
    private javax.swing.JButton helpJButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton manualSelectionJRadioButton;
    private javax.swing.JButton okJButton;
    private javax.swing.JMenuItem pasteJMenuItem;
    private javax.swing.JLabel peptideToProteinJLabel;
    private javax.swing.JScrollPane peptideToProteinJScrollPane2;
    private org.jdesktop.swingx.JXTable peptideToProteinJXTable;
    private javax.swing.JRadioButton provideListJRadioButton;
    private javax.swing.JRadioButton selectFirstJRadioButton;
    private javax.swing.JPopupMenu tableJPopupMenu;
    // End of variables declaration//GEN-END:variables
}
