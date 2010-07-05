package no.uib.prideconverter.gui;

import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.table.DefaultTableModel;
import no.uib.olsdialog.OLSDialog;
import no.uib.olsdialog.OLSInputable;
import no.uib.prideconverter.PRIDEConverter;
import uk.ac.ebi.pride.model.implementation.mzData.CvParamImpl;
import uk.ac.ebi.pride.model.implementation.mzData.UserParamImpl;
import uk.ac.ebi.pride.model.interfaces.mzdata.CvParam;
import uk.ac.ebi.pride.model.interfaces.mzdata.UserParam;

/**
 * A dialog for inserting CV and user parameters for a given spectrum.
 *
 * @author  Harald Barsnes
 *
 * Created Januar 2009
 */
public class SpectrumDetails extends javax.swing.JDialog implements OLSInputable {

    private String spectrumKey;

    /**
     * Opens a new SpectrumDetails dialog.
     *
     * @param parent the parent frame of the dialog
     * @param modal
     */
    public SpectrumDetails(java.awt.Frame parent, boolean modal, String spectrumKey) {
        super(parent, modal);

        this.spectrumKey = spectrumKey;

        initComponents();

        // insert the stored CV params for this spectrum
        ArrayList<CvParam> cvParams = PRIDEConverter.getProperties().getSpectrumCvParams().get(spectrumKey);

        if (cvParams != null) {

            Iterator<CvParam> iterator = cvParams.iterator();

            while (iterator.hasNext()) {

                CvParam currentCvTerm = iterator.next();

                ((DefaultTableModel) cvTermsJTable.getModel()).insertRow(
                        cvTermsJTable.getRowCount(), new Object[]{
                            cvTermsJTable.getRowCount()+1,
                            currentCvTerm.getName(),
                            currentCvTerm.getAccession(),
                            currentCvTerm.getCVLookup(),
                            currentCvTerm.getValue()
                        });
            }
        }

        // insert the stored user params for this spectrum
        ArrayList<UserParam> userParams = PRIDEConverter.getProperties().getSpectrumUserParams().get(spectrumKey);

        if (userParams != null) {

            Iterator<UserParam> iterator = userParams.iterator();

            while (iterator.hasNext()) {

                UserParam currentUserParam = iterator.next();

                ((DefaultTableModel) userParametersJTable.getModel()).insertRow(
                        userParametersJTable.getRowCount(), new Object[]{
                            userParametersJTable.getRowCount()+1,
                            currentUserParam.getName(),
                            currentUserParam.getValue()
                        });
            }
        }

        cvTermsJTable.getColumn(" ").setMinWidth(40);
        cvTermsJTable.getColumn(" ").setMaxWidth(40);
        cvTermsJTable.setRowHeight(cvTermsJTable.getFont().getSize() + 6);

        userParametersJTable.getColumn(" ").setMinWidth(40);
        userParametersJTable.getColumn(" ").setMaxWidth(40);
        userParametersJTable.setRowHeight(userParametersJTable.getFont().getSize() + 6);

        cvTermsJTable.getTableHeader().setReorderingAllowed(false);
        cvTermsJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        userParametersJTable.getTableHeader().setReorderingAllowed(false);
        userParametersJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupCvParamsJMenu = new javax.swing.JPopupMenu();
        editCvParamJMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        moveUpCvParamJMenuItem = new javax.swing.JMenuItem();
        moveDownCvParamJMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        deleteSelectedRowCvParamJMenuItem = new javax.swing.JMenuItem();
        popupUserParamsJMenu = new javax.swing.JPopupMenu();
        editUserParamJMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        moveUpUserParamJMenuItem = new javax.swing.JMenuItem();
        moveDownUserParamJMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        deleteSelectedRowUserParamJMenuItem = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        cvTermsJTable = new javax.swing.JTable();
        olsSearchJButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        addUserParamterJButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        userParametersJTable = new javax.swing.JTable();
        okJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        aboutJButton = new javax.swing.JButton();
        helpJButton = new javax.swing.JButton();

        editCvParamJMenuItem.setMnemonic('E');
        editCvParamJMenuItem.setText("Edit");
        editCvParamJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editCvParamJMenuItemActionPerformed(evt);
            }
        });
        popupCvParamsJMenu.add(editCvParamJMenuItem);
        popupCvParamsJMenu.add(jSeparator3);

        moveUpCvParamJMenuItem.setMnemonic('U');
        moveUpCvParamJMenuItem.setText("Move Up");
        moveUpCvParamJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpCvParamJMenuItemActionPerformed(evt);
            }
        });
        popupCvParamsJMenu.add(moveUpCvParamJMenuItem);

        moveDownCvParamJMenuItem.setMnemonic('D');
        moveDownCvParamJMenuItem.setText("Move Down");
        moveDownCvParamJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownCvParamJMenuItemActionPerformed(evt);
            }
        });
        popupCvParamsJMenu.add(moveDownCvParamJMenuItem);
        popupCvParamsJMenu.add(jSeparator4);

        deleteSelectedRowCvParamJMenuItem.setText("Delete");
        deleteSelectedRowCvParamJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSelectedRowCvParamJMenuItemActionPerformed(evt);
            }
        });
        popupCvParamsJMenu.add(deleteSelectedRowCvParamJMenuItem);

        editUserParamJMenuItem.setMnemonic('E');
        editUserParamJMenuItem.setText("Edit");
        editUserParamJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editUserParamJMenuItemActionPerformed(evt);
            }
        });
        popupUserParamsJMenu.add(editUserParamJMenuItem);
        popupUserParamsJMenu.add(jSeparator5);

        moveUpUserParamJMenuItem.setMnemonic('U');
        moveUpUserParamJMenuItem.setText("Move Up");
        moveUpUserParamJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpUserParamJMenuItemActionPerformed(evt);
            }
        });
        popupUserParamsJMenu.add(moveUpUserParamJMenuItem);

        moveDownUserParamJMenuItem.setMnemonic('D');
        moveDownUserParamJMenuItem.setText("Move Down");
        moveDownUserParamJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownUserParamJMenuItemActionPerformed(evt);
            }
        });
        popupUserParamsJMenu.add(moveDownUserParamJMenuItem);
        popupUserParamsJMenu.add(jSeparator6);

        deleteSelectedRowUserParamJMenuItem.setText("Delete");
        deleteSelectedRowUserParamJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSelectedRowUserParamJMenuItemActionPerformed(evt);
            }
        });
        popupUserParamsJMenu.add(deleteSelectedRowUserParamJMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Spectrum Annotations");
        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("CV Parameters"));

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

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
                    .add(olsSearchJButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(olsSearchJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("User Parameters"));

        addUserParamterJButton.setText("Add User Parameter");
        addUserParamterJButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        addUserParamterJButton.setPreferredSize(new java.awt.Dimension(85, 23));
        addUserParamterJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addUserParamterJButtonActionPerformed(evt);
            }
        });

        userParametersJTable.setFont(userParametersJTable.getFont());
        userParametersJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Name", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        userParametersJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                userParametersJTableMouseClicked(evt);
            }
        });
        userParametersJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                userParametersJTableKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(userParametersJTable);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
                    .add(addUserParamterJButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(addUserParamterJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        okJButton.setText("OK");
        okJButton.setMaximumSize(new java.awt.Dimension(65, 23));
        okJButton.setMinimumSize(new java.awt.Dimension(65, 23));
        okJButton.setPreferredSize(new java.awt.Dimension(65, 23));
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

        aboutJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF"))); // NOI18N
        aboutJButton.setToolTipText("About");
        aboutJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutJButtonActionPerformed(evt);
            }
        });

        helpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/help.GIF"))); // NOI18N
        helpJButton.setToolTipText("Find Folder");
        helpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpJButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 386, Short.MAX_VALUE)
                        .add(okJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelJButton)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {cancelJButton, okJButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(cancelJButton)
                            .add(okJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {cancelJButton, okJButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Right clicking in the table will open a popup menu, while double
     * clicking opens an OLS dialog to change the selected row.
     *
     * @param evt
     */
    private void cvTermsJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cvTermsJTableMouseClicked
        if (evt.getButton() == 3) {

            int row = cvTermsJTable.rowAtPoint(evt.getPoint());
            int column = cvTermsJTable.columnAtPoint(evt.getPoint());

            cvTermsJTable.changeSelection(row, column, false, false);

            //makes sure only valid "moving options" are enabled
            this.moveUpCvParamJMenuItem.setEnabled(true);
            this.moveDownCvParamJMenuItem.setEnabled(true);

            if (row == cvTermsJTable.getRowCount() - 1) {
                this.moveDownCvParamJMenuItem.setEnabled(false);
            }

            if (row == 0) {
                this.moveUpCvParamJMenuItem.setEnabled(false);
            }

            popupCvParamsJMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        } else if (evt.getButton() == 1 && evt.getClickCount() == 2) {
            editCvParamJMenuItemActionPerformed(null);
        }
}//GEN-LAST:event_cvTermsJTableMouseClicked

    /**
     * If the delete button is clicked the selected rows in the table are
     * removed.
     *
     * @param evt
     */
    private void cvTermsJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cvTermsJTableKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            deleteSelectedRowCvParamJMenuItemActionPerformed(null);
        }
}//GEN-LAST:event_cvTermsJTableKeyReleased

    /**
     * Opens an OLS dialog.
     *
     * @param evt
     */
    private void olsSearchJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_olsSearchJButtonActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new OLSDialog(this, this, true, "cvterms", PRIDEConverter.getUserProperties().getLastSelectedOntology(), null);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_olsSearchJButtonActionPerformed

    /**
     * Opens a NewUserParameter to add a new user parameter.
     *
     * @param evt
     */
    private void addUserParamterJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addUserParamterJButtonActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new NewUserParameter(this, this, true);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_addUserParamterJButtonActionPerformed

    /**
     * If the user double clicks on a row in the table the NewUserParameter
     * dialog is shown where the information about the given parameter can be
     * altered. If the user right clicks a pop up menu is shown for editing,
     * moving or delting the selected parameter.
     *
     * @param evt
     */
    private void userParametersJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_userParametersJTableMouseClicked
        if (evt.getButton() == 3) {

            int row = userParametersJTable.rowAtPoint(evt.getPoint());
            int column = userParametersJTable.columnAtPoint(evt.getPoint());

            userParametersJTable.changeSelection(row, column, false, false);

            moveUpUserParamJMenuItem.setEnabled(true);
            moveDownUserParamJMenuItem.setEnabled(true);

            if (row == userParametersJTable.getRowCount() - 1) {
                moveDownUserParamJMenuItem.setEnabled(false);
            }

            if (row == 0) {
                moveUpUserParamJMenuItem.setEnabled(false);
            }

            popupUserParamsJMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        } else if (evt.getButton() == 1 && evt.getClickCount() == 2) {
            editUserParamJMenuItemActionPerformed(null);
        }
}//GEN-LAST:event_userParametersJTableMouseClicked

    /**
     * If the delete key is clicked the selected rows in the table are removed.
     *
     * @param evt
     */
    private void userParametersJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_userParametersJTableKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            deleteSelectedRowUserParamJMenuItemActionPerformed(null);
        }
}//GEN-LAST:event_userParametersJTableKeyReleased

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
     * Opens an OLS dialog.
     *
     * @param evt
     */
    private void editCvParamJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editCvParamJMenuItemActionPerformed

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

        new OLSDialog(this, this, true, "cvterms", ontology, selectedRow, searchTerm);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_editCvParamJMenuItemActionPerformed

    /**
     * Moves the selected row one step up in the table.
     *
     * @param evt
     */
    private void moveUpCvParamJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpCvParamJMenuItemActionPerformed
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

        fixCvParamsIndices();
}//GEN-LAST:event_moveUpCvParamJMenuItemActionPerformed

    /**
     * Moves the selected row one step down in the table.
     *
     * @param evt
     */
    private void moveDownCvParamJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownCvParamJMenuItemActionPerformed
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

        fixCvParamsIndices();
}//GEN-LAST:event_moveDownCvParamJMenuItemActionPerformed

    /**
     * Delete the selected row
     *
     * @param evt
     */
    private void deleteSelectedRowCvParamJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSelectedRowCvParamJMenuItemActionPerformed
        if (cvTermsJTable.getSelectedRow() != -1) {
            ((DefaultTableModel) cvTermsJTable.getModel()).removeRow(cvTermsJTable.getSelectedRow());

            if (cvTermsJTable.getRowCount() == 0) {
                //this.insertJButton.setEnabled(false);
            }
        }
}//GEN-LAST:event_deleteSelectedRowCvParamJMenuItemActionPerformed

    /**
     * Opens a NewUserParameter dialog where the selected parameter can
     * be changed.
     *
     * @param evt
     */
    private void editUserParamJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editUserParamJMenuItemActionPerformed
        int selectedRow = userParametersJTable.getSelectedRow();

        new NewUserParameter(this, this, true,
                (String) userParametersJTable.getValueAt(selectedRow, 1),
                (String) userParametersJTable.getValueAt(selectedRow, 2),
                selectedRow);
}//GEN-LAST:event_editUserParamJMenuItemActionPerformed

    /**
     * Moves the selected parameter one step up in the table.
     *
     * @param evt
     */
    private void moveUpUserParamJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpUserParamJMenuItemActionPerformed
        int selectedRow = userParametersJTable.getSelectedRow();
        int selectedColumn = userParametersJTable.getSelectedColumn();

        Object[] tempRow = new Object[]{
            userParametersJTable.getValueAt(selectedRow - 1, 0),
            userParametersJTable.getValueAt(selectedRow - 1, 1),
            userParametersJTable.getValueAt(selectedRow - 1, 2)
        };

        ((DefaultTableModel) userParametersJTable.getModel()).removeRow(selectedRow - 1);
        ((DefaultTableModel) userParametersJTable.getModel()).insertRow(selectedRow, tempRow);

        userParametersJTable.changeSelection(selectedRow - 1, selectedColumn, false, false);

        fixUserParamsIndices();
}//GEN-LAST:event_moveUpUserParamJMenuItemActionPerformed

    /**
     * Moves the selected parameter one step down in the table.
     *
     * @param evt
     */
    private void moveDownUserParamJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownUserParamJMenuItemActionPerformed
        int selectedRow = userParametersJTable.getSelectedRow();
        int selectedColumn = userParametersJTable.getSelectedColumn();

        Object[] tempRow = new Object[]{
            userParametersJTable.getValueAt(selectedRow + 1, 0),
            userParametersJTable.getValueAt(selectedRow + 1, 1),
            userParametersJTable.getValueAt(selectedRow + 1, 2)
        };

        ((DefaultTableModel) userParametersJTable.getModel()).removeRow(selectedRow + 1);
        ((DefaultTableModel) userParametersJTable.getModel()).insertRow(selectedRow, tempRow);

        userParametersJTable.changeSelection(selectedRow + 1, selectedColumn, false, false);

        fixUserParamsIndices();
}//GEN-LAST:event_moveDownUserParamJMenuItemActionPerformed

    /**
     * Delete the selected parameter.
     *
     * @param evt
     */
    private void deleteSelectedRowUserParamJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSelectedRowUserParamJMenuItemActionPerformed

        if (userParametersJTable.getSelectedRow() != -1) {
            ((DefaultTableModel) userParametersJTable.getModel()).removeRow(userParametersJTable.getSelectedRow());

            fixUserParamsIndices();
        }
}//GEN-LAST:event_deleteSelectedRowUserParamJMenuItemActionPerformed

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
        new HelpDialog(this, true, getClass().getResource("/no/uib/prideconverter/helpfiles/SpectrumDetails.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_helpJButtonActionPerformed

    /**
     * Stores the inserted CV and user parameters and closes the dialog.
     *
     * @param evt
     */
    private void okJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okJButtonActionPerformed

        ArrayList<CvParam> cvParams = new ArrayList<CvParam>();

        for (int i = 0; i < cvTermsJTable.getRowCount(); i++) {
            cvParams.add(new CvParamImpl(
                    (String) cvTermsJTable.getValueAt(i, 2),
                    (String) cvTermsJTable.getValueAt(i, 3),
                    (String) cvTermsJTable.getValueAt(i, 1),
                    i,
                    (String) cvTermsJTable.getValueAt(i, 4)));
        }

        PRIDEConverter.getProperties().getSpectrumCvParams().put(spectrumKey, cvParams);

        ArrayList<UserParam> userParams = new ArrayList<UserParam>();

        for (int i = 0; i < userParametersJTable.getRowCount(); i++) {
            userParams.add(new UserParamImpl(
                    (String) userParametersJTable.getValueAt(i, 1),
                    i,
                    (String) userParametersJTable.getValueAt(i, 2)));
        }

        PRIDEConverter.getProperties().getSpectrumUserParams().put(spectrumKey, userParams);

        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_okJButtonActionPerformed

    /**
     * Fixes the indices so that they are in accending order starting from one
     */
    private void fixCvParamsIndices() {
        for (int row = 0; row < ((DefaultTableModel) cvTermsJTable.getModel()).getRowCount(); row++) {
            ((DefaultTableModel) cvTermsJTable.getModel()).setValueAt(new Integer(row + 1), row, 0);
        }
    }

    /**
     * Fixes the indices so that they are in accending order starting from one
     */
    private void fixUserParamsIndices() {
        for (int row = 0; row < ((DefaultTableModel) userParametersJTable.getModel()).getRowCount(); row++) {
            ((DefaultTableModel) userParametersJTable.getModel()).setValueAt(new Integer(row + 1), row, 0);
        }
    }

    /**
     * Add a user parameter to the user paramater table.
     *
     * @param name
     * @param value
     * @param modifiedRow the row to modify, use -1 if adding new row
     */
    public void addUserParameter(String name, String value, int modifiedRow) {

        if (modifiedRow == -1) {
            ((DefaultTableModel) userParametersJTable.getModel()).addRow(new Object[]{
                        new Integer(userParametersJTable.getRowCount() + 1),
                        name,
                        value
                    });
        } else {
            userParametersJTable.setValueAt(name, modifiedRow, 1);
            userParametersJTable.setValueAt(value, modifiedRow, 2);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JButton addUserParamterJButton;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JTable cvTermsJTable;
    private javax.swing.JMenuItem deleteSelectedRowCvParamJMenuItem;
    private javax.swing.JMenuItem deleteSelectedRowUserParamJMenuItem;
    private javax.swing.JMenuItem editCvParamJMenuItem;
    private javax.swing.JMenuItem editUserParamJMenuItem;
    private javax.swing.JButton helpJButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JMenuItem moveDownCvParamJMenuItem;
    private javax.swing.JMenuItem moveDownUserParamJMenuItem;
    private javax.swing.JMenuItem moveUpCvParamJMenuItem;
    private javax.swing.JMenuItem moveUpUserParamJMenuItem;
    private javax.swing.JButton okJButton;
    private javax.swing.JButton olsSearchJButton;
    private javax.swing.JPopupMenu popupCvParamsJMenu;
    private javax.swing.JPopupMenu popupUserParamsJMenu;
    private javax.swing.JTable userParametersJTable;
    // End of variables declaration//GEN-END:variables

    /**
     * Inserts the selected CV parameter into the CV parameters table.
     */
    public void insertOLSResult(String field, String selectedValue, String accession,
            String ontologyShort, String ontologyLong, int modifiedRow, String mappedTerm) {

        PRIDEConverter.getUserProperties().setLastSelectedOntology(ontologyLong);

        if (modifiedRow == -1) {
            ((DefaultTableModel) this.cvTermsJTable.getModel()).addRow(
                    new Object[]{
                        new Integer(cvTermsJTable.getRowCount() + 1),
                        selectedValue, accession, ontologyShort, null
                    });
        } else {
            cvTermsJTable.setValueAt(selectedValue, modifiedRow, 1);
            cvTermsJTable.setValueAt(accession, modifiedRow, 2);
            cvTermsJTable.setValueAt(ontologyShort, modifiedRow, 3);
            cvTermsJTable.setValueAt(null, modifiedRow, 4);
        }
    }

    /**
     * @see OLSInputable
     */
    public Window getWindow() {
        return (Window) this;
    }
}