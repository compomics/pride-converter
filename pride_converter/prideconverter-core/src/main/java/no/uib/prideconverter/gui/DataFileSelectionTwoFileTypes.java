package no.uib.prideconverter.gui;

import no.uib.prideconverter.PRIDEConverter;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import no.uib.prideconverter.filefilters.DtaFileFilter;
import no.uib.prideconverter.filefilters.MgfFileFilter;
import no.uib.prideconverter.filefilters.MzDataFileFilter;
import no.uib.prideconverter.filefilters.MzXmlFileFilter;
import no.uib.prideconverter.filefilters.OutFileFilter;
import no.uib.prideconverter.filefilters.PklFileFilter;
import no.uib.prideconverter.filefilters.PklSpoFileFilter;
import no.uib.prideconverter.filefilters.XmlFileFilter;
import uk.ac.ebi.pride.model.interfaces.mzdata.CvParam;
import uk.ac.ebi.pride.model.interfaces.mzdata.UserParam;

/**
 * This frame handles the selection of the data files to be converted for 
 * dataformats with two files, e.g., Spectrum Mill or SEQUEST.
 * 
 * @author Harald Barsnes
 * 
 * Created March 2008
 */
public class DataFileSelectionTwoFileTypes extends javax.swing.JFrame {


    /** 
     * Opens a new DataFileSelectionTwoFileTypes frame, and inserts stored information.
     * 
     * @param location where to position the frame
     */
    public DataFileSelectionTwoFileTypes(Point location) {

        // sets the default wizard frame size
        this.setPreferredSize(new Dimension(PRIDEConverter.getProperties().FRAME_WIDTH,
                PRIDEConverter.getProperties().FRAME_HEIGHT));
        this.setSize(PRIDEConverter.getProperties().FRAME_WIDTH,
                PRIDEConverter.getProperties().FRAME_HEIGHT);
        this.setMaximumSize(new Dimension(PRIDEConverter.getProperties().FRAME_WIDTH,
                PRIDEConverter.getProperties().FRAME_HEIGHT));
        this.setMinimumSize(new Dimension(PRIDEConverter.getProperties().FRAME_WIDTH,
                PRIDEConverter.getProperties().FRAME_HEIGHT));

        initComponents();

        selectedSpectraFilesJTable.getTableHeader().setReorderingAllowed(false);
        selectedIdentificationFilesJTable.getTableHeader().setReorderingAllowed(false);

        selectedSpectraFilesJTable.getColumn(" ").setMinWidth(40);
        selectedSpectraFilesJTable.getColumn(" ").setMaxWidth(40);
        selectedSpectraFilesJTable.setRowHeight(selectedSpectraFilesJTable.getFont().getSize() + PRIDEConverter.getProperties().tableRowHeightPaddingSize);

        selectedIdentificationFilesJTable.getColumn(" ").setMinWidth(40);
        selectedIdentificationFilesJTable.getColumn(" ").setMaxWidth(40);
        selectedIdentificationFilesJTable.setRowHeight(selectedIdentificationFilesJTable.getFont().getSize() + PRIDEConverter.getProperties().tableRowHeightPaddingSize);

        // insert stored information
        if (PRIDEConverter.getProperties().getSelectedSourceFiles() != null) {

            for (int i = 0; i < PRIDEConverter.getProperties().getSelectedSourceFiles().size(); i++) {

                ((DefaultTableModel) selectedSpectraFilesJTable.getModel()).addRow(
                        new Object[]{
                            new Integer(selectedSpectraFilesJTable.getRowCount() + 1),
                            new File(PRIDEConverter.getProperties().getSelectedSourceFiles().get(i)).getName()
                        });
            }
        } else {
            PRIDEConverter.getProperties().setSelectedSourceFiles(new ArrayList());
        }

        if (PRIDEConverter.getProperties().getSelectedIdentificationFiles() != null) {

            for (int i = 0; i < PRIDEConverter.getProperties().getSelectedIdentificationFiles().size(); i++) {
                ((DefaultTableModel) selectedIdentificationFilesJTable.getModel()).addRow(
                        new Object[]{
                            new Integer(selectedIdentificationFilesJTable.getRowCount() +
                            1),
                            new File(PRIDEConverter.getProperties().getSelectedIdentificationFiles().get(i)).getName()
                        });
            }

            if (selectedIdentificationFilesJTable.getRowCount() > 0 &&
                    selectedSpectraFilesJTable.getRowCount() > 0) {
                nextJButton.setEnabled(true);
            }
        } else {
            PRIDEConverter.getProperties().setSelectedIdentificationFiles(new ArrayList());
        }

        // sets the icon of the frame
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        setTitle(PRIDEConverter.getWizardName() + " " + PRIDEConverter.getPrideConverterVersionNumber() +
                " - " + getTitle());

        if (location != null) {
            setLocation(location);
        } else {
            setLocationRelativeTo(null);
        }

        setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        deleteSelectedSpectraFilesJPopupMenu = new javax.swing.JPopupMenu();
        deleteSpectraJMenuItem = new javax.swing.JMenuItem();
        deleteSelectedIdentificationFilesJPopupMenu = new javax.swing.JPopupMenu();
        deleteIdentificationsJMenuItem = new javax.swing.JMenuItem();
        nextJButton = new javax.swing.JButton();
        backJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        selectedSpectraFilesJTable = new javax.swing.JTable();
        selectSpectrumFileOrFolderJButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        helpJButton = new javax.swing.JButton();
        aboutJButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        selectedIdentificationFilesJTable = new javax.swing.JTable();
        selectIdentificationsFileOrFolderJButton = new javax.swing.JButton();

        deleteSpectraJMenuItem.setText("Delete Selected Row(s)");
        deleteSpectraJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSpectraJMenuItemActionPerformed(evt);
            }
        });
        deleteSelectedSpectraFilesJPopupMenu.add(deleteSpectraJMenuItem);

        deleteIdentificationsJMenuItem.setText("Delete Selected Row(s)");
        deleteIdentificationsJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteIdentificationsJMenuItemActionPerformed(evt);
            }
        });
        deleteSelectedIdentificationFilesJPopupMenu.add(deleteIdentificationsJMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("File Selection - Step 1 of 8");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        nextJButton.setText("Next  >");
        nextJButton.setEnabled(false);
        nextJButton.setPreferredSize(new java.awt.Dimension(81, 23));
        nextJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextJButtonActionPerformed(evt);
            }
        });

        backJButton.setText("< Back");
        backJButton.setPreferredSize(new java.awt.Dimension(81, 23));
        backJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backJButtonActionPerformed(evt);
            }
        });

        cancelJButton.setText("Cancel");
        cancelJButton.setPreferredSize(new java.awt.Dimension(81, 23));
        cancelJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelJButtonActionPerformed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Spectrum Files"));

        jLabel4.setText("Selected Files:");

        selectedSpectraFilesJTable.setFont(selectedSpectraFilesJTable.getFont());
        selectedSpectraFilesJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "File"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        selectedSpectraFilesJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                selectedSpectraFilesJTableMouseClicked(evt);
            }
        });
        selectedSpectraFilesJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                selectedSpectraFilesJTableKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(selectedSpectraFilesJTable);

        selectSpectrumFileOrFolderJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/Directory.gif"))); // NOI18N
        selectSpectrumFileOrFolderJButton.setText("Select a File or a Folder");
        selectSpectrumFileOrFolderJButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        selectSpectrumFileOrFolderJButton.setMaximumSize(new java.awt.Dimension(167, 23));
        selectSpectrumFileOrFolderJButton.setMinimumSize(new java.awt.Dimension(167, 23));
        selectSpectrumFileOrFolderJButton.setPreferredSize(new java.awt.Dimension(167, 23));
        selectSpectrumFileOrFolderJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectSpectrumFileOrFolderJButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, selectSpectrumFileOrFolderJButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel4))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(selectSpectrumFileOrFolderJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel3.setFont(jLabel3.getFont().deriveFont((jLabel3.getFont().getStyle() | java.awt.Font.ITALIC), jLabel3.getFont().getSize()-2));
        jLabel3.setText("Select the files you want to convert data from, and click on 'Next'  to continue.");

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

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Identification Files"));

        jLabel5.setText("Selected Files:");

        selectedIdentificationFilesJTable.setFont(selectedIdentificationFilesJTable.getFont());
        selectedIdentificationFilesJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "File"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        selectedIdentificationFilesJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                selectedIdentificationFilesJTableMouseClicked(evt);
            }
        });
        selectedIdentificationFilesJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                selectedIdentificationFilesJTableKeyReleased(evt);
            }
        });
        jScrollPane3.setViewportView(selectedIdentificationFilesJTable);

        selectIdentificationsFileOrFolderJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/Directory.gif"))); // NOI18N
        selectIdentificationsFileOrFolderJButton.setText("Select a File or a Folder");
        selectIdentificationsFileOrFolderJButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        selectIdentificationsFileOrFolderJButton.setMaximumSize(new java.awt.Dimension(167, 23));
        selectIdentificationsFileOrFolderJButton.setMinimumSize(new java.awt.Dimension(167, 23));
        selectIdentificationsFileOrFolderJButton.setPreferredSize(new java.awt.Dimension(167, 23));
        selectIdentificationsFileOrFolderJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectIdentificationsFileOrFolderJButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, selectIdentificationsFileOrFolderJButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel5))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(selectIdentificationsFileOrFolderJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 239, Short.MAX_VALUE)
                        .add(backJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nextJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)
                    .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jLabel3))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(7, 7, 7)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(nextJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(backJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Closes the frame and the wizard.
     * 
     * @param evt
     */
    private void cancelJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
        this.setVisible(false);
        this.dispose();
        PRIDEConverter.cancelConvertion();
    }//GEN-LAST:event_cancelJButtonActionPerformed

    /**
     * Tries to connect to the database and go to the next step (ProjectSelection).
     * 
     * @param evt
     */
    private void nextJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextJButtonActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new SpectraSelectionWithIdentifications(this.getLocation());
        this.setVisible(false);
        this.dispose();
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

    }//GEN-LAST:event_nextJButtonActionPerformed

    /**
     * @see #cancelJButtonActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelJButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    /**
     * Opens a Help window.
     * 
     * @param evt
     */
    private void helpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, false, getClass().getResource("/no/uib/prideconverter/helpfiles/DataFileSelection_TwoFileTypes.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_helpJButtonActionPerformed

    /**
     * Opens an About PRIDE Converter window.
     * 
     * @param evt
     */
    private void aboutJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, false, getClass().getResource("/no/uib/prideconverter/helpfiles/AboutPRIDE_Converter.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_aboutJButtonActionPerformed

    /**
     * Closes the frame and opens the previous frame (DataSourceSelection).
     * 
     * @param evt
     */
    private void backJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backJButtonActionPerformed
        new DataSourceSelection(this.getLocation());
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_backJButtonActionPerformed

    /**
     * Deletes the selected spectra files in the table.
     * 
     * @param evt
     */
    private void deleteSpectraJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSpectraJMenuItemActionPerformed
        if (selectedSpectraFilesJTable.getSelectedRow() != -1) {

            PRIDEConverter.getProperties().setSpectrumTableModel(null);

            int[] selectedRows = selectedSpectraFilesJTable.getSelectedRows();

            for (int i = selectedRows.length - 1; i >= 0; i--) {
                PRIDEConverter.getProperties().getSelectedSourceFiles().remove(selectedRows[i]);
                ((DefaultTableModel) selectedSpectraFilesJTable.getModel()).removeRow(selectedRows[i]);
            }

            fixSpectraTableIndices();

            nextJButton.setEnabled(selectedSpectraFilesJTable.getRowCount() > 0);
        }
}//GEN-LAST:event_deleteSpectraJMenuItemActionPerformed

    /**
     * If the user right clicks in the spectra files table a popup menu is 
     * shown.
     * 
     * @param evt
     */
    private void selectedSpectraFilesJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectedSpectraFilesJTableMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON3) {

            int row = selectedSpectraFilesJTable.rowAtPoint(evt.getPoint());
            int column = selectedSpectraFilesJTable.columnAtPoint(evt.getPoint());

            int[] selectedRows = selectedSpectraFilesJTable.getSelectedRows();

            if (selectedRows.length > 0) {
                if (row < selectedRows[0] || row >
                        selectedRows[selectedRows.length - 1]) {
                    selectedSpectraFilesJTable.changeSelection(row, column, false, false);
                }
            } else {
                selectedSpectraFilesJTable.changeSelection(row, column, false, false);
            }

            deleteSelectedSpectraFilesJPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
}//GEN-LAST:event_selectedSpectraFilesJTableMouseClicked

    /**
     * Opens a file chooser where the user can select the files add to the 
     * table.
     * 
     * @param evt
     */
    private void selectSpectrumFileOrFolderJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectSpectrumFileOrFolderJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        JFileChooser chooser = new JFileChooser(PRIDEConverter.getUserProperties().getCurrentSourceFileLocation());
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);
        if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("Spectrum Mill")) {
            chooser.setFileFilter(new PklFileFilter());
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("SEQUEST Result File")) {
            chooser.setFileFilter(new DtaFileFilter());
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("X!Tandem")) {

            // make sure that the last used file filter is selected
            // the next time the file user is opened
            if (PRIDEConverter.getProperties().getLastUsedFileFilter() != null) {
                if (PRIDEConverter.getProperties().getLastUsedFileFilter().getDescription().equalsIgnoreCase(
                        new MgfFileFilter().getDescription())) {
                    chooser.addChoosableFileFilter(new DtaFileFilter());
                    chooser.addChoosableFileFilter(new PklFileFilter());
                    chooser.addChoosableFileFilter(new MzDataFileFilter());
                    chooser.addChoosableFileFilter(new MzXmlFileFilter());
                    chooser.addChoosableFileFilter(new MgfFileFilter());
                } else if (PRIDEConverter.getProperties().getLastUsedFileFilter().getDescription().equalsIgnoreCase(
                        new MzDataFileFilter().getDescription())) {
                    chooser.addChoosableFileFilter(new DtaFileFilter());
                    chooser.addChoosableFileFilter(new PklFileFilter());
                    chooser.addChoosableFileFilter(new MgfFileFilter());
                    chooser.addChoosableFileFilter(new MzXmlFileFilter());
                    chooser.addChoosableFileFilter(new MzDataFileFilter());
                } else if (PRIDEConverter.getProperties().getLastUsedFileFilter().getDescription().equalsIgnoreCase(
                        new MzXmlFileFilter().getDescription())) {
                    chooser.addChoosableFileFilter(new DtaFileFilter());
                    chooser.addChoosableFileFilter(new PklFileFilter());
                    chooser.addChoosableFileFilter(new MgfFileFilter());
                    chooser.addChoosableFileFilter(new MzDataFileFilter());
                    chooser.addChoosableFileFilter(new MzXmlFileFilter());
                } else if (PRIDEConverter.getProperties().getLastUsedFileFilter().getDescription().equalsIgnoreCase(
                        new DtaFileFilter().getDescription())) {
                    chooser.addChoosableFileFilter(new MgfFileFilter());
                    chooser.addChoosableFileFilter(new MzDataFileFilter());
                    chooser.addChoosableFileFilter(new MzXmlFileFilter());
                    chooser.addChoosableFileFilter(new PklFileFilter());
                    chooser.addChoosableFileFilter(new DtaFileFilter());
                } else if (PRIDEConverter.getProperties().getLastUsedFileFilter().getDescription().equalsIgnoreCase(
                        new PklFileFilter().getDescription())) {
                    chooser.addChoosableFileFilter(new DtaFileFilter());
                    chooser.addChoosableFileFilter(new MgfFileFilter());
                    chooser.addChoosableFileFilter(new MzDataFileFilter());
                    chooser.addChoosableFileFilter(new MzXmlFileFilter());
                    chooser.addChoosableFileFilter(new PklFileFilter());
                }
            } else {
                chooser.addChoosableFileFilter(new DtaFileFilter());
                chooser.addChoosableFileFilter(new MgfFileFilter());
                chooser.addChoosableFileFilter(new MzDataFileFilter());
                chooser.addChoosableFileFilter(new MzXmlFileFilter());
                chooser.addChoosableFileFilter(new PklFileFilter());
            }
        }

        int returnVal = chooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String path;

            PRIDEConverter.getProperties().setSpectrumTableModel(null);
            PRIDEConverter.getProperties().setInstrumentDetailsExtracted(false);
            PRIDEConverter.getProperties().setSelectedSourceFiles(new ArrayList());
            PRIDEConverter.getProperties().setSpectraSelectionCriteria(null);
            PRIDEConverter.getProperties().setDataFileHasBeenLoaded(false);
            PRIDEConverter.getProperties().setSampleDetailsExtracted(false);
            PRIDEConverter.getProperties().setContactInfoExtracted(false);
            PRIDEConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());
            PRIDEConverter.getProperties().setSpectrumCvParams(new HashMap<String, ArrayList<CvParam>>());
            PRIDEConverter.getProperties().setSpectrumUserParams(new HashMap<String, ArrayList<UserParam>>());

            while (selectedSpectraFilesJTable.getRowCount() > 0) {
                ((DefaultTableModel) selectedSpectraFilesJTable.getModel()).removeRow(0);
            }

            PRIDEConverter.getUserProperties().setSourceFileLocation(chooser.getSelectedFile().getPath());
            PRIDEConverter.getProperties().setLastUsedFileFilter(chooser.getFileFilter());

            File[] allFiles = chooser.getSelectedFiles();

            for (int j = 0; j < allFiles.length; j++) {

                File currentFile = allFiles[j];

                if (currentFile.isDirectory()) {

                    File[] files = currentFile.listFiles();
                    File tempFile;

                    for (int i = 0; i < files.length; i++) {
                        tempFile = files[i];

                        if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("Spectrum Mill")) {
                            if (tempFile.getAbsolutePath().endsWith(".pkl") ||
                                    tempFile.getAbsolutePath().endsWith(".PKL")) {
                                if (!PRIDEConverter.getProperties().getSelectedSourceFiles().contains(tempFile.getPath())) {
                                    ((DefaultTableModel) selectedSpectraFilesJTable.getModel()).addRow(
                                            new Object[]{
                                                new Integer(selectedSpectraFilesJTable.getRowCount() + 1),
                                                tempFile.getName(),
                                                new Boolean(true)
                                            });

                                    PRIDEConverter.getProperties().getSelectedSourceFiles().add(tempFile.getPath());
                                } else {
                                    JOptionPane.showMessageDialog(this, 
                                            "A file called " + tempFile.getPath() +
                                            " is already selected.",
                                            "File Already Selected", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("SEQUEST Result File")) {
                            if (tempFile.getAbsolutePath().endsWith(".dta") ||
                                    tempFile.getAbsolutePath().endsWith(".DTA")) {
                                if (!PRIDEConverter.getProperties().getSelectedSourceFiles().contains(tempFile.getPath())) {
                                    ((DefaultTableModel) selectedSpectraFilesJTable.getModel()).addRow(
                                            new Object[]{
                                                new Integer(selectedSpectraFilesJTable.getRowCount() + 1),
                                                tempFile.getName(),
                                                new Boolean(true)
                                            });

                                    PRIDEConverter.getProperties().getSelectedSourceFiles().add(tempFile.getPath());
                                } else {
                                    JOptionPane.showMessageDialog(this, "A file called " + tempFile.getPath() +
                                            " is already selected.",
                                            "File Already Selected", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("X!Tandem")) {
                            if (chooser.getFileFilter().accept(tempFile)) {
                                if (!PRIDEConverter.getProperties().getSelectedSourceFiles().contains(tempFile.getPath())) {
                                    ((DefaultTableModel) selectedSpectraFilesJTable.getModel()).addRow(
                                            new Object[]{
                                                new Integer(selectedSpectraFilesJTable.getRowCount() + 1),
                                                tempFile.getName(),
                                                new Boolean(true)
                                            });

                                    PRIDEConverter.getProperties().getSelectedSourceFiles().add(tempFile.getPath());
                                } else {
                                    JOptionPane.showMessageDialog(this, "A file called " + tempFile.getPath() +
                                            " is already selected.",
                                            "File Already Selected", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    }
                } else {
                    if (!PRIDEConverter.getProperties().getSelectedSourceFiles().contains(currentFile.getPath())) {
                        ((DefaultTableModel) selectedSpectraFilesJTable.getModel()).addRow(
                                new Object[]{
                                    new Integer(selectedSpectraFilesJTable.getRowCount() +
                                    1),
                                    currentFile.getName(),
                                    new Boolean(true)
                                });

                        PRIDEConverter.getProperties().getSelectedSourceFiles().add(currentFile.getPath());
                    } else {
                        JOptionPane.showMessageDialog(this, "A file called " +
                                currentFile.getPath() +
                                " is already selected.", "File Already Selected",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        if (selectedIdentificationFilesJTable.getRowCount() > 0 &&
                selectedSpectraFilesJTable.getRowCount() > 0) {
            nextJButton.setEnabled(true);
        }

        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_selectSpectrumFileOrFolderJButtonActionPerformed

    /**
     * If the user right clicks in the identification files table a popup menu 
     * is shown.
     * 
     * @param evt
     */
    private void selectedIdentificationFilesJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectedIdentificationFilesJTableMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON3) {

            int row = selectedIdentificationFilesJTable.rowAtPoint(evt.getPoint());
            int column = selectedIdentificationFilesJTable.columnAtPoint(evt.getPoint());

            int[] selectedRows = selectedIdentificationFilesJTable.getSelectedRows();

            if (selectedRows.length > 0) {
                if (row < selectedRows[0] || row >
                        selectedRows[selectedRows.length - 1]) {
                    selectedIdentificationFilesJTable.changeSelection(row, column, false, false);
                }
            } else {
                selectedIdentificationFilesJTable.changeSelection(row, column, false, false);
            }

            deleteSelectedIdentificationFilesJPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
}//GEN-LAST:event_selectedIdentificationFilesJTableMouseClicked

    /**
     * Opens a file chooser where the files to be added to the identification 
     * files folder can be selected.
     * 
     * @param evt
     */
    private void selectIdentificationsFileOrFolderJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectIdentificationsFileOrFolderJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        JFileChooser chooser = new JFileChooser(PRIDEConverter.getUserProperties().getCurrentSourceFileLocation());
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);
        if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("Spectrum Mill")) {
            chooser.setFileFilter(new PklSpoFileFilter());
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("SEQUEST Result File")) {
            chooser.setFileFilter(new OutFileFilter());
        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("X!Tandem")) {
            chooser.setFileFilter(new XmlFileFilter());
        }

        int returnVal = chooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String path;

            PRIDEConverter.getProperties().setSpectrumTableModel(null);
            PRIDEConverter.getProperties().setInstrumentDetailsExtracted(false);
            PRIDEConverter.getProperties().setSelectedIdentificationFiles(new ArrayList());
            PRIDEConverter.getProperties().setSpectraSelectionCriteria(null);
            PRIDEConverter.getProperties().setDataFileHasBeenLoaded(false);
            PRIDEConverter.getProperties().setSampleDetailsExtracted(false);
            PRIDEConverter.getProperties().setContactInfoExtracted(false);
            PRIDEConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());
            PRIDEConverter.getProperties().setSpectrumCvParams(new HashMap<String, ArrayList<CvParam>>());
            PRIDEConverter.getProperties().setSpectrumUserParams(new HashMap<String, ArrayList<UserParam>>());

            while (selectedIdentificationFilesJTable.getRowCount() > 0) {
                ((DefaultTableModel) selectedIdentificationFilesJTable.getModel()).removeRow(0);
            }

            PRIDEConverter.getUserProperties().setSourceFileLocation(chooser.getSelectedFile().getPath());

            File[] allFiles = chooser.getSelectedFiles();

            for (int j = 0; j < allFiles.length; j++) {

                File currentFile = allFiles[j];

                if (currentFile.isDirectory()) {

                    File[] files = currentFile.listFiles();
                    File tempFile;

                    for (int i = 0; i < files.length; i++) {
                        tempFile = files[i];

                        if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("Spectrum Mill")) {
                            if (tempFile.getAbsolutePath().endsWith(".spo") ||
                                    tempFile.getAbsolutePath().endsWith(".SPO")) {
                                if (!PRIDEConverter.getProperties().getSelectedIdentificationFiles().contains(tempFile.getPath())) {
                                    ((DefaultTableModel) selectedIdentificationFilesJTable.getModel()).addRow(
                                            new Object[]{
                                                new Integer(selectedIdentificationFilesJTable.getRowCount() + 1),
                                                tempFile.getName(),
                                                new Boolean(true)
                                            });

                                    PRIDEConverter.getProperties().getSelectedIdentificationFiles().add(tempFile.getPath());
                                } else {
                                    JOptionPane.showMessageDialog(this,
                                            "A file called " + tempFile.getPath() +
                                            " is already selected.",
                                            "File Already Selected", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("SEQUEST Result File")) {
                            if (tempFile.getAbsolutePath().endsWith(".out") ||
                                    tempFile.getAbsolutePath().endsWith(".OUT")) {
                                if (!PRIDEConverter.getProperties().getSelectedIdentificationFiles().contains(tempFile.getPath())) {
                                    ((DefaultTableModel) selectedIdentificationFilesJTable.getModel()).addRow(
                                            new Object[]{
                                                new Integer(selectedIdentificationFilesJTable.getRowCount() + 1),
                                                tempFile.getName(),
                                                new Boolean(true)
                                            });

                                    PRIDEConverter.getProperties().getSelectedIdentificationFiles().add(tempFile.getPath());
                                } else {
                                    JOptionPane.showMessageDialog(this,
                                            "A file called " + tempFile.getPath() +
                                            " is already selected.",
                                            "File Already Selected", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("X!Tandem")) {
                            if (chooser.getFileFilter().accept(tempFile)) {
                                if (!PRIDEConverter.getProperties().getSelectedIdentificationFiles().contains(tempFile.getPath())) {
                                    ((DefaultTableModel) selectedIdentificationFilesJTable.getModel()).addRow(
                                            new Object[]{
                                                new Integer(selectedIdentificationFilesJTable.getRowCount() + 1),
                                                tempFile.getName(),
                                                new Boolean(true)
                                            });

                                    PRIDEConverter.getProperties().getSelectedIdentificationFiles().add(tempFile.getPath());
                                } else {
                                    JOptionPane.showMessageDialog(this,
                                            "A file called " + tempFile.getPath() +
                                            " is already selected.",
                                            "File Already Selected", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    }
                } else {

                    if (!PRIDEConverter.getProperties().getSelectedIdentificationFiles().contains(currentFile.getPath())) {
                        ((DefaultTableModel) selectedIdentificationFilesJTable.getModel()).addRow(
                                new Object[]{
                                    new Integer(selectedIdentificationFilesJTable.getRowCount() + 1),
                                    currentFile.getName(),
                                    new Boolean(true)
                                });

                        PRIDEConverter.getProperties().getSelectedIdentificationFiles().add(currentFile.getPath());
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "A file called " + currentFile.getPath() +
                                " is already selected.",
                                "File Already Selected", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        if (selectedIdentificationFilesJTable.getRowCount() > 0 &&
                selectedSpectraFilesJTable.getRowCount() > 0) {
            nextJButton.setEnabled(true);
        }

        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_selectIdentificationsFileOrFolderJButtonActionPerformed

    /**
     * Removes the selected identification files from the table.
     * 
     * @param evt
     */
    private void deleteIdentificationsJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteIdentificationsJMenuItemActionPerformed
        if (selectedIdentificationFilesJTable.getSelectedRow() != -1) {

            PRIDEConverter.getProperties().setSpectrumTableModel(null);

            int[] selectedRows = selectedIdentificationFilesJTable.getSelectedRows();

            for (int i = selectedRows.length - 1; i >= 0; i--) {
                PRIDEConverter.getProperties().getSelectedIdentificationFiles().remove(selectedRows[i]);
                ((DefaultTableModel) selectedIdentificationFilesJTable.getModel()).removeRow(selectedRows[i]);
            }

            fixIdentificationTableIndices();
            nextJButton.setEnabled(selectedIdentificationFilesJTable.getRowCount() > 0);
        }
}//GEN-LAST:event_deleteIdentificationsJMenuItemActionPerformed

    /**
     * @see #deleteSpectraJMenuItemActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void selectedSpectraFilesJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_selectedSpectraFilesJTableKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            deleteSpectraJMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_selectedSpectraFilesJTableKeyReleased

    /**
     * @see #deleteIdentificationsJMenuItemActionPerformed(java.awt.event.ActionEvent)
     * 
     * @param evt
     */
    private void selectedIdentificationFilesJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_selectedIdentificationFilesJTableKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            deleteIdentificationsJMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_selectedIdentificationFilesJTableKeyReleased

    /**
     * Fixes the indices in the spectra file table so that they are in 
     * accending order starting from one.
     */
    private void fixSpectraTableIndices() {
        for (int row = 0; row < ((DefaultTableModel) selectedSpectraFilesJTable.getModel()).getRowCount(); row++) {
            ((DefaultTableModel) selectedSpectraFilesJTable.getModel()).setValueAt(new Integer(row + 1), row, 0);
        }
    }

    /**
     * Fixes the indices in the identification file table so that they are in 
     * accending order starting from one.
     */
    private void fixIdentificationTableIndices() {
        for (int row = 0; row < ((DefaultTableModel) selectedIdentificationFilesJTable.getModel()).getRowCount(); row++) {
            ((DefaultTableModel) selectedIdentificationFilesJTable.getModel()).setValueAt(new Integer(row + 1), row, 0);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JButton backJButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JMenuItem deleteIdentificationsJMenuItem;
    private javax.swing.JPopupMenu deleteSelectedIdentificationFilesJPopupMenu;
    private javax.swing.JPopupMenu deleteSelectedSpectraFilesJPopupMenu;
    private javax.swing.JMenuItem deleteSpectraJMenuItem;
    private javax.swing.JButton helpJButton;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton nextJButton;
    private javax.swing.JButton selectIdentificationsFileOrFolderJButton;
    private javax.swing.JButton selectSpectrumFileOrFolderJButton;
    private javax.swing.JTable selectedIdentificationFilesJTable;
    private javax.swing.JTable selectedSpectraFilesJTable;
    // End of variables declaration//GEN-END:variables
}
