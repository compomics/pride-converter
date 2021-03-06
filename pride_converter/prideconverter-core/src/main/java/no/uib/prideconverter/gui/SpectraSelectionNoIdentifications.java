package no.uib.prideconverter.gui;

import javax.swing.event.TableModelEvent;
import no.uib.prideconverter.PRIDEConverter;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import no.uib.prideconverter.util.Util;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTableHeader;
import org.jdesktop.swingx.decorator.SortOrder;
import org.systemsbiology.jrap.stax.MSXMLParser;
import org.systemsbiology.jrap.stax.Scan;
import uk.ac.ebi.pride.model.interfaces.mzdata.MzData;
import uk.ac.ebi.pride.xml.MzDataXMLUnmarshaller;

/**
 * This frame handles the spectra selection for all the data formats 
 * that are without identifications (e.g. pkl, dat, mgf). (And is also 
 * used for TPP due to the current lack of support for spectra selection 
 * for TPP data sets.
 * 
 * @author Harald Barsnes
 * 
 * Created March 2008
 */
public class SpectraSelectionNoIdentifications extends javax.swing.JFrame {

    private int numberOfSelectedSpectra;
    private boolean selectAll;
    private ProgressDialog progressDialog;
    private File file;
    private FileReader f;
    private BufferedReader b;
    private String currentLine;
    private StringTokenizer tok;
    private Double precursorMass;
    private Integer precursorCharge;
    private Vector columnToolTips;

    /** 
     * Opens a new SpectraSelection_SimpleDataFormats frame, and inserts stored information.
     * 
     * @param location where to position the frame
     */
    public SpectraSelectionNoIdentifications(Point location) {

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

        spectraJXTable.getColumn("Selected").setMinWidth(75);
        spectraJXTable.getColumn("Selected").setMaxWidth(75);
        spectraJXTable.getColumn("Charge").setMaxWidth(75);
        spectraJXTable.getColumn("Charge").setMinWidth(75);
        spectraJXTable.getColumn("Mass").setMaxWidth(75);
        spectraJXTable.getColumn("Mass").setMinWidth(75);
        spectraJXTable.getColumn("ID").setMaxWidth(75);
        spectraJXTable.getColumn("ID").setMinWidth(75);
        spectraJXTable.getColumn("MS Level").setMaxWidth(75);
        spectraJXTable.getColumn("MS Level").setMinWidth(75);

        columnToolTips = new Vector();
        columnToolTips.add("Filename of Spectrum File");
        columnToolTips.add("Spectrum Identification");
        columnToolTips.add("Precursor Mass");
        columnToolTips.add("Precursor Charge");
        columnToolTips.add("Mass Spectrometry Level");
        columnToolTips.add(null);

        spectraJXTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        spectraJXTable.getTableHeader().setReorderingAllowed(false);
        spectraJXTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        spectraJXTable.setAutoCreateColumnsFromModel(false);

        // makes sure that inserting an MS level lower than 1 is not allowed
        // and displays a warning message for MS levels above 3
        spectraJXTable.getModel().addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent e) {
                if (spectraJXTable.getSelectedColumn() == 4) {

                    int insertedValue = ((Integer) spectraJXTable.getValueAt(
                            spectraJXTable.getSelectedRow(), spectraJXTable.getSelectedColumn())).intValue();

                    if (insertedValue < 2) {

                        JOptionPane.showMessageDialog(null, "Selected MS level is smaller than 2!",
                                "Incorrect MS Level", JOptionPane.ERROR_MESSAGE);
                        spectraJXTable.changeSelection(spectraJXTable.getSelectedRow(), spectraJXTable.getSelectedColumn(),
                                false, false);
                        spectraJXTable.setValueAt(null, spectraJXTable.getSelectedRow(), spectraJXTable.getSelectedColumn());
                        spectraJXTable.editCellAt(spectraJXTable.getSelectedRow(), spectraJXTable.getSelectedColumn());

                    } else if (insertedValue > 3) {

                        int option = JOptionPane.showConfirmDialog(null,
                                "Selected MS level is larger than 3. Are you sure this is the correct MS level?",
                                "Verify MS Level", JOptionPane.YES_NO_OPTION);

                        if (option == JOptionPane.NO_OPTION) {
                            spectraJXTable.changeSelection(spectraJXTable.getSelectedRow(), spectraJXTable.getSelectedColumn(),
                                    false, false);
                            spectraJXTable.setValueAt(null, spectraJXTable.getSelectedRow(), spectraJXTable.getSelectedColumn());
                            spectraJXTable.editCellAt(spectraJXTable.getSelectedRow(), spectraJXTable.getSelectedColumn());
                        }
                    }
                }
            }
        });

        

        if (PRIDEConverter.getProperties().getSpectrumTableModel() != null) {

            selectAllJCheckBox.setSelected(PRIDEConverter.getProperties().selectAllSpectra());

            spectraJXTable.setModel(PRIDEConverter.getProperties().getSpectrumTableModel());
            loadSpectraJLabel.setEnabled(false);
            selectedSpectraJLabel.setEnabled(true);
            numberOfSelectedSpectraJTextField.setEnabled(true);
            spectrumAnnotationJLabel.setEnabled(true);

            selectAllJCheckBox.setEnabled(false);

            for (int i = 0; i < spectraJXTable.getRowCount(); i++) {
                if (((Boolean) spectraJXTable.getValueAt(i, 5)).booleanValue()) {
                    numberOfSelectedSpectra++;
                }
            }

            numberOfSelectedSpectraJTextField.setText("" +
                    numberOfSelectedSpectra + "/" +
                    spectraJXTable.getRowCount());
        } else{
            PRIDEConverter.getProperties().setSelectAllSpectra(true);
            selectAllJCheckBox.setSelected(PRIDEConverter.getProperties().selectAllSpectra());
        }

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

        if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("mzData") ||
                PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("TPP")) {
            loadSpectraJLabel.setEnabled(false);
            loadSpectraJLabel.setToolTipText("Manual spectra selection is currently not supported for mzData and TPP");
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selectAllJPopupMenu = new javax.swing.JPopupMenu();
        selectAllJMenuItem = new javax.swing.JMenuItem();
        invertSelectionJMenuItem = new javax.swing.JMenuItem();
        spectrumDetailsJPopupMenu = new javax.swing.JPopupMenu();
        viewSpectrumAnnotationsJMenuItem = new javax.swing.JMenuItem();
        nextJButton = new javax.swing.JButton();
        backJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        helpJButton = new javax.swing.JButton();
        aboutJButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        selectedSpectraJLabel = new javax.swing.JLabel();
        numberOfSelectedSpectraJTextField = new javax.swing.JTextField();
        spectrumAnnotationJLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        spectraJXTable = new JXTable() {
            protected JXTableHeader createDefaultTableHeader() {
                return new JXTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        String tip = null;
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        tip = (String) columnToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        loadSpectraJLabel = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        selectAllJCheckBox = new javax.swing.JCheckBox();

        selectAllJMenuItem.setMnemonic('A');
        selectAllJMenuItem.setText("Select/Deselect All");
        selectAllJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllJMenuItemActionPerformed(evt);
            }
        });
        selectAllJPopupMenu.add(selectAllJMenuItem);

        invertSelectionJMenuItem.setText("Invert Selection");
        invertSelectionJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invertSelectionJMenuItemActionPerformed(evt);
            }
        });
        selectAllJPopupMenu.add(invertSelectionJMenuItem);

        viewSpectrumAnnotationsJMenuItem.setText("View/Change Spectrum Annotations");
        viewSpectrumAnnotationsJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewSpectrumAnnotationsJMenuItemActionPerformed(evt);
            }
        });
        spectrumDetailsJPopupMenu.add(viewSpectrumAnnotationsJMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Spectra Selection - Step 2 of 8");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        nextJButton.setText("Next  >");
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

        jLabel3.setFont(jLabel3.getFont().deriveFont((jLabel3.getFont().getStyle() | java.awt.Font.ITALIC), jLabel3.getFont().getSize()-2));
        jLabel3.setText("Select the spectra to include in the PRIDE XML file, and click on 'Next' to continue.");

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

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Manual Spectra Selection"));

        selectedSpectraJLabel.setText("Selected Spectra:");
        selectedSpectraJLabel.setToolTipText("Number of Selected Spectra");
        selectedSpectraJLabel.setEnabled(false);

        numberOfSelectedSpectraJTextField.setEditable(false);
        numberOfSelectedSpectraJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        numberOfSelectedSpectraJTextField.setToolTipText("Number of Selected Spectra");
        numberOfSelectedSpectraJTextField.setEnabled(false);

        spectrumAnnotationJLabel.setFont(spectrumAnnotationJLabel.getFont().deriveFont((spectrumAnnotationJLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        spectrumAnnotationJLabel.setText("Right click on a row to add spectrum annotations.");
        spectrumAnnotationJLabel.setEnabled(false);

        spectraJXTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Filename", "ID", "Mass", "Charge", "MS Level", "Selected"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Double.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spectraJXTable.setOpaque(false);
        spectraJXTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                spectraJXTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(spectraJXTable);

        loadSpectraJLabel.setText("<html><a href><i>Load Spectra Details</i></a></html>");
        loadSpectraJLabel.setToolTipText("Click here to load the spectrum details for manual selection");
        loadSpectraJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loadSpectraJLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loadSpectraJLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loadSpectraJLabelMouseExited(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel5Layout.createSequentialGroup()
                        .add(spectrumAnnotationJLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 153, Short.MAX_VALUE)
                        .add(selectedSpectraJLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(numberOfSelectedSpectraJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(loadSpectraJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(504, Short.MAX_VALUE))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(loadSpectraJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(numberOfSelectedSpectraJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(selectedSpectraJLabel)
                    .add(spectrumAnnotationJLabel))
                .addContainerGap())
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Simple Spectra Selection"));

        selectAllJCheckBox.setSelected(true);
        selectAllJCheckBox.setText("Select All Spectra");
        selectAllJCheckBox.setIconTextGap(20);
        selectAllJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllJCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(selectAllJCheckBox)
                .addContainerGap(481, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(selectAllJCheckBox)
                .addContainerGap(10, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 303, Short.MAX_VALUE)
                        .add(backJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nextJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 624, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel3))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
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
     * Closes the frame and opens the next frame (ExperimentProperties).
     * 
     * @param evt
     */
    private void nextJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextJButtonActionPerformed
        saveInsertedInformation();
        new ExperimentProperties(this.getLocation());
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_nextJButtonActionPerformed

    /**
     * @see #cancelJButtonActionPerformed(java.awt.event.ActionEvent)
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelJButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    /**
     * Opens a help window.
     * 
     * @param evt
     */
    private void helpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, false, getClass().getResource(
                "/no/uib/prideconverter/helpfiles/SpectraSelectionNoIdentifications.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_helpJButtonActionPerformed

    /**
     * Opens an About PRIDE Converter dialog.
     * 
     * @param evt
     */
    private void aboutJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, false, getClass().getResource("/no/uib/prideconverter/helpfiles/AboutPRIDE_Converter.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_aboutJButtonActionPerformed

    /**
     * Closes the frame and opens the previous step (DataFileSelection).
     * 
     * @param evt
     */
    private void backJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backJButtonActionPerformed
        saveInsertedInformation();

        if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("TPP")) {
            new DataFileSelectionTPP(this.getLocation());
        } else {
            new DataFileSelection(this.getLocation());
        }

        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_backJButtonActionPerformed

    /**
     * Loads the spectra from the selected files. Done in a separate thread 
     * to keep the program from hanging.
     * 
     * @param evt
     */
    /**
     * Makes sure that the number of selected spectra is updated when the 
     * user selects or deselects a spectra in the list. Right clicking in 
     * the last column open a popup menu with advanced selection option.
     * 
     * @param evt
     */
    /**
     * Selects all the spectra in the table.
     * 
     * @param evt
     */
    private void selectAllJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllJMenuItemActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        boolean columnWasSorted = false;
        int sortedTableColumn = -1;
        SortOrder sortOrder = null;

        if(spectraJXTable.getSortedColumn() != null){
            sortedTableColumn = spectraJXTable.getSortedColumn().getModelIndex();
            sortOrder = spectraJXTable.getSortOrder(sortedTableColumn);
            spectraJXTable.setSortable(false);
            columnWasSorted = true;
        }

        if (selectAll) {
            for (int i = 0; i < spectraJXTable.getRowCount(); i++) {
                if (!(((Boolean) spectraJXTable.getValueAt(i, 5)).booleanValue())) {
                    spectraJXTable.setValueAt(true, i, 5);
                }
            }

            numberOfSelectedSpectraJTextField.setText("" +
                    spectraJXTable.getRowCount() + "/" +
                    spectraJXTable.getRowCount());
            numberOfSelectedSpectra =
                    spectraJXTable.getRowCount();

        } else {
            for (int i = 0; i < spectraJXTable.getRowCount(); i++) {
                if ((((Boolean) spectraJXTable.getValueAt(i, 5)).booleanValue())) {
                    spectraJXTable.setValueAt(false, i, 5);
                }
            }

            numberOfSelectedSpectraJTextField.setText("0" + "/" +
                    spectraJXTable.getRowCount());
            numberOfSelectedSpectra = 0;
        }

        if (numberOfSelectedSpectra == 0) {
            nextJButton.setEnabled(false);
        } else {
            nextJButton.setEnabled(true);
        }

        selectAll = !selectAll;

        if(columnWasSorted){
            spectraJXTable.setSortable(true);
            spectraJXTable.setSortOrder(sortedTableColumn, sortOrder);
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_selectAllJMenuItemActionPerformed

    /**
     * Inverts the selection in the table.
     * 
     * @param evt
     */
    private void invertSelectionJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invertSelectionJMenuItemActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        boolean columnWasSorted = false;
        int sortedTableColumn = -1;
        SortOrder sortOrder = null;

        if(spectraJXTable.getSortedColumn() != null){
            sortedTableColumn = spectraJXTable.getSortedColumn().getModelIndex();
            sortOrder = spectraJXTable.getSortOrder(sortedTableColumn);
            spectraJXTable.setSortable(false);
            columnWasSorted = true;
        }

        numberOfSelectedSpectra = 0;

        for (int i = 0; i < spectraJXTable.getRowCount(); i++) {
            spectraJXTable.setValueAt(
                    (!((Boolean) spectraJXTable.getValueAt(i, 5)).booleanValue()), i, 5);

            if (((Boolean) spectraJXTable.getValueAt(i, 5)).booleanValue()) {
                numberOfSelectedSpectra++;
            }
        }

        numberOfSelectedSpectraJTextField.setText("" + numberOfSelectedSpectra +
                "/" + spectraJXTable.getRowCount());

        if (numberOfSelectedSpectra == 0) {
            nextJButton.setEnabled(false);
        } else {
            nextJButton.setEnabled(true);
        }

        if(columnWasSorted){
            spectraJXTable.setSortable(true);
            spectraJXTable.setSortOrder(sortedTableColumn, sortOrder);
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_invertSelectionJMenuItemActionPerformed

    /**
     * Enables or disables the Next button.
     * 
     * @param evt
     */
    private void selectAllJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllJCheckBoxActionPerformed
        if (!selectAllJCheckBox.isSelected()) {
            if (numberOfSelectedSpectra == 0) {
                nextJButton.setEnabled(false);
            } else {
                nextJButton.setEnabled(true);
            }
        } else {
            nextJButton.setEnabled(true);
        }
    }//GEN-LAST:event_selectAllJCheckBoxActionPerformed

    /**
     * Opens a dialog showning the spectrum annotations for the selected spectrum.
     *
     * @param evt
     */
    private void viewSpectrumAnnotationsJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewSpectrumAnnotationsJMenuItemActionPerformed
        new SpectrumDetails(this, true,
                "" + spectraJXTable.getValueAt(spectraJXTable.getSelectedRow(), 0) + "_" +
                spectraJXTable.getValueAt(spectraJXTable.getSelectedRow(), 1) + "_" +
                spectraJXTable.getValueAt(spectraJXTable.getSelectedRow(), 2) + "_" +
                spectraJXTable.getValueAt(spectraJXTable.getSelectedRow(), 3) + "_" +
                spectraJXTable.getValueAt(spectraJXTable.getSelectedRow(), 4));
}//GEN-LAST:event_viewSpectrumAnnotationsJMenuItemActionPerformed

    /**
     * Makes sure that the number of selected spectra is updated when the 
     * user selects or deselects a spectra in the list. Right clicking in 
     * the last column open a popup menu with advanced selection option.
     * 
     * @param evt
     */
    private void spectraJXTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spectraJXTableMouseClicked

        if (spectraJXTable.columnAtPoint(evt.getPoint()) != -1 && spectraJXTable.rowAtPoint(evt.getPoint()) != -1) {
            spectraJXTable.changeSelection(spectraJXTable.rowAtPoint(evt.getPoint()),
                    spectraJXTable.columnAtPoint(evt.getPoint()), false, false);
        }

        if (spectraJXTable.columnAtPoint(evt.getPoint()) == 5) {

            this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

            numberOfSelectedSpectra = 0;

            for (int i = 0; i < spectraJXTable.getRowCount(); i++) {
                if (((Boolean) spectraJXTable.getValueAt(i, 5)).booleanValue()) {
                    numberOfSelectedSpectra++;
                }
            }

            numberOfSelectedSpectraJTextField.setText("" +
                    numberOfSelectedSpectra + "/" +
                    spectraJXTable.getRowCount());

            if (numberOfSelectedSpectra == 0) {
                nextJButton.setEnabled(false);
            } else {
                nextJButton.setEnabled(true);
            }
        }

        int column = spectraJXTable.columnAtPoint(evt.getPoint());

        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3 && column == 5) {
            selectAllJPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        } else if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
            spectrumDetailsJPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_spectraJXTableMouseClicked

    /**
     * Loads the spectra from the selected files. Done in a separate thread 
     * to keep the program from hanging.
     * 
     * @param evt
     */
    private void loadSpectraJLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loadSpectraJLabelMouseClicked
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        numberOfSelectedSpectra = 0;

        progressDialog = new ProgressDialog(this, true);
        progressDialog.setTitle("Loading Spectra. Please Wait...");
        progressDialog.setIntermidiate(true);

        Thread t = new Thread(new Runnable() {

            public void run() {
                progressDialog.setVisible(true);
            }
        });

        t.start();

        Thread t2 = new Thread(new Runnable() {

            public void run() {

                MSXMLParser msXMLParser;
                int scanCount;
                Scan scan;
                FileReader reader;
                MzDataXMLUnmarshaller unmarshallerMzData;
                MzData mzData;
                Collection<uk.ac.ebi.pride.model.interfaces.mzdata.Spectrum> spectraMzData;
                Iterator<uk.ac.ebi.pride.model.interfaces.mzdata.Spectrum> iterator;
                uk.ac.ebi.pride.model.interfaces.mzdata.Spectrum currentSpectrum;
                String[] values;

                for (int j = 0; j < PRIDEConverter.getProperties().getSelectedSourceFiles().size(); j++) {

                    if (PRIDEConverter.isConversionCanceled()) {

                        while (((DefaultTableModel) spectraJXTable.getModel()).getRowCount() > 0) {
                            ((DefaultTableModel) spectraJXTable.getModel()).removeRow(0);
                        }

                        numberOfSelectedSpectraJTextField.setText("");

                        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                        return;
                    }

                    try {
                        file = new File(PRIDEConverter.getProperties().getSelectedSourceFiles().get(j));

                        progressDialog.setString(file.getName() + " (" + (j + 1) +
                                "/" + PRIDEConverter.getProperties().getSelectedSourceFiles().size() + ")");

                        if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("SEQUEST DTA File")) {

                            f = new FileReader(file);
                            b = new BufferedReader(f);

                            currentLine = b.readLine();

                            tok = new StringTokenizer(currentLine);

                            precursorMass = new Double(tok.nextToken());
                            precursorCharge = new Integer(tok.nextToken());

                            ((DefaultTableModel) spectraJXTable.getModel()).addRow(
                                    new Object[]{
                                        file.getName(),
                                        null,
                                        precursorMass, precursorCharge,
                                        2, true});

                            numberOfSelectedSpectra++;

                            b.close();
                            f.close();

                        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("Micromass PKL File")) {

                            f = new FileReader(file);
                            b = new BufferedReader(f);

                            currentLine = b.readLine();

                            while (currentLine != null) {

                                tok = new StringTokenizer(currentLine);

                                if (tok.countTokens() == 3) {

                                    precursorMass = new Double(tok.nextToken());

                                    //PKL files also includes intensity so ignore the next token
                                    tok.nextToken();

                                    precursorCharge = new Integer(tok.nextToken());

                                    ((DefaultTableModel) spectraJXTable.getModel()).addRow(
                                            new Object[]{
                                                file.getName(),
                                                null,
                                                precursorMass, precursorCharge,
                                                2, true});

                                    numberOfSelectedSpectra++;
                                }

                                currentLine = b.readLine();
                            }

                            b.close();
                            f.close();

                        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("VEMS")) {

                            f = new FileReader(file);
                            b = new BufferedReader(f);

                            currentLine = b.readLine();

                            while (currentLine != null) {

                                tok = new StringTokenizer(currentLine);

                                if (tok.countTokens() == 4) {

                                    precursorMass = new Double(tok.nextToken());

                                    //PKX files also includes intensity so ignore the next token
                                    tok.nextToken();

                                    precursorCharge = new Integer(tok.nextToken());

                                    ((DefaultTableModel) spectraJXTable.getModel()).addRow(
                                            new Object[]{
                                                file.getName(),
                                                null,
                                                precursorMass, precursorCharge,
                                                2, true});

                                    numberOfSelectedSpectra++;
                                }

                                currentLine = b.readLine();
                            }

                            b.close();
                            f.close();

                        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("mzXML")) {

                            msXMLParser = new MSXMLParser(file.getAbsolutePath());

                            scanCount = msXMLParser.getScanCount();

                            for (int i = 1; i <= scanCount; i++) {
                                scan = msXMLParser.rap(i);

                                precursorCharge = scan.getHeader().getPrecursorCharge();
                                precursorMass = new Float(scan.getHeader().getPrecursorMz()).doubleValue();

                                if (precursorMass != -1 &&
                                        precursorCharge != -1) {
                                    ((DefaultTableModel) spectraJXTable.getModel()).addRow(
                                            new Object[]{
                                                file.getName(),
                                                scan.getHeader().getNum(),
                                                precursorMass, precursorCharge,
                                                2, true});
                                } else if (precursorMass == -1 &&
                                        precursorCharge != -1) {
                                    ((DefaultTableModel) spectraJXTable.getModel()).addRow(
                                            new Object[]{
                                                file.getName(),
                                                scan.getHeader().getNum(),
                                                null, precursorCharge,
                                                2, true});
                                } else if (precursorMass != -1 &&
                                        precursorCharge == -1) {
                                    ((DefaultTableModel) spectraJXTable.getModel()).addRow(
                                            new Object[]{
                                                file.getName(),
                                                scan.getHeader().getNum(),
                                                precursorMass, null,
                                                2, true});
                                } else {
                                    ((DefaultTableModel) spectraJXTable.getModel()).addRow(
                                            new Object[]{
                                                file.getName(),
                                                scan.getHeader().getNum(),
                                                null, null,
                                                2, true});
                                }

                                numberOfSelectedSpectra++;

                                numberOfSelectedSpectraJTextField.setText(numberOfSelectedSpectra +
                                        "/" +
                                        numberOfSelectedSpectra);
                            }
                        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("mzData")) {

                            reader = new FileReader(file);

                            unmarshallerMzData = new MzDataXMLUnmarshaller();
                            mzData = unmarshallerMzData.unMarshall(reader);

                            spectraMzData = mzData.getSpectrumCollection();
                            iterator = spectraMzData.iterator();

                            while (iterator.hasNext()) {
                                currentSpectrum = iterator.next();

                                ((DefaultTableModel) spectraJXTable.getModel()).addRow(
                                        new Object[]{
                                            file.getName(),
                                            "" + currentSpectrum.getSpectrumId(),
                                            null, null,
                                            2, true});

                                numberOfSelectedSpectra++;
                                numberOfSelectedSpectraJTextField.setText(numberOfSelectedSpectra +
                                        "/" + numberOfSelectedSpectra);
                            }

                            reader.close();

                        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("Mascot Generic File")) {

                            f = new FileReader(file);
                            b = new BufferedReader(f);

                            String line = null;
                            int lineCount = 0;
                            boolean inSpectrum = false;
                            precursorMass = null;
                            precursorCharge = null;

                            while ((line = b.readLine()) != null && progressDialog.isVisible()) {
                                // Advance line count.
                                lineCount++;
                                // Delete leading/trailing spaces.
                                line = line.trim();
                                // Skip empty lines.
                                if (line.equals("")) {
                                    continue;
                                }
                                // First line can be 'CHARGE'.
                                if (lineCount == 1 && line.startsWith("CHARGE")) {
                                    continue;
                                }

                                // BEGIN IONS marks the start of the real file.
                                if (line.equals("BEGIN IONS")) {
                                    precursorMass = null;
                                    precursorCharge = null;
                                    inSpectrum = true;
                                } // END IONS marks the end.
                                else if (line.equals("END IONS")) {

                                    inSpectrum = false;

                                    ((DefaultTableModel) spectraJXTable.getModel()).addRow(
                                            new Object[]{
                                                file.getName(),
                                                null,
                                                precursorMass, precursorCharge,
                                                2, true});

                                    numberOfSelectedSpectra++;
                                    numberOfSelectedSpectraJTextField.setText(numberOfSelectedSpectra +
                                            "/" + numberOfSelectedSpectra);

                                } else if (inSpectrum && (line.indexOf("=") >= 0)) {
                                    // Find the starting location of the value (which is one beyond the location
                                    // of the '=').
                                    int equalSignIndex = line.indexOf("=");

                                    if (line.startsWith("PEPMASS")) {
                                        // PEPMASS line found.
                                        String value = line.substring(equalSignIndex + 1);

                                        //if (PRIDEConverter.getProperties().isCommaTheDecimalSymbol()) {
                                        value = value.replaceAll(",", ".");
                                        //}

                                        values = value.split("\\s");
                                        precursorMass = Double.parseDouble(values[0]);

                                    } else if (line.startsWith("CHARGE")) {
                                        // CHARGE line found.
                                        // Note the extra parsing to read a Mascot Generic File charge (eg., 1+).
                                        precursorCharge = Util.extractCharge(line.substring(equalSignIndex + 1));
                                    }
                                }
                            }

                            b.close();

                        } else if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("MS2")) {

                            f = new FileReader(file);
                            b = new BufferedReader(f);

                            String line = null;
                            int lineCount = 0;
                            boolean chargeDecided = false;

                            while ((line = b.readLine()) != null) {
                                // Advance line count.
                                lineCount++;

                                // Skip empty lines.
                                if (line.equals("")) {
                                    continue;
                                }

                                // ignore
                                if (line.startsWith("H") ||
                                        line.startsWith("D") ||
                                        line.startsWith("I")) {
                                    continue;
                                }

                                // S marks the start of a new spectrum
                                if (line.startsWith("S")) {

                                    if (PRIDEConverter.getProperties().isCommaTheDecimalSymbol()) {
                                        line = line.replaceAll(",", ".");
                                    }

                                    values = line.split("\\s");

                                    precursorMass = new Double(values[3]);


                                    chargeDecided = false;

                                } else if (line.startsWith("Z") && !chargeDecided) {

                                    String nextLine = b.readLine();

                                    // multiple charges
                                    if (nextLine.startsWith("Z")) {
                                        precursorCharge = null;
                                    } else {
                                        values = line.split("\\s");
                                        precursorCharge = new Integer(values[1]);
                                    }

                                    chargeDecided = true;

                                    ((DefaultTableModel) spectraJXTable.getModel()).addRow(
                                            new Object[]{
                                                file.getName(),
                                                null,
                                                precursorMass, precursorCharge,
                                                2, true});

                                    numberOfSelectedSpectra++;
                                    numberOfSelectedSpectraJTextField.setText(numberOfSelectedSpectra +
                                            "/" + numberOfSelectedSpectra);
                                }
                            }

                            b.close();
                        }

                        numberOfSelectedSpectraJTextField.setText(numberOfSelectedSpectra +
                                "/" + numberOfSelectedSpectra);

                    } catch (FileNotFoundException ex) {
                        JOptionPane.showMessageDialog(null,
                                "The file " + file.getPath() +
                                "\ncould not be found.",
                                "File Not Found", JOptionPane.ERROR_MESSAGE);
                        Util.writeToErrorLog("Error when trying to read file: ");
                        ex.printStackTrace();
                    } catch (Exception e) {

                        Util.writeToErrorLog("Error parsing " + PRIDEConverter.getProperties().getDataSource() + ": ");
                        e.printStackTrace();

                        String fileType = PRIDEConverter.getProperties().getDataSource();

                        JOptionPane.showMessageDialog(null,
                                "The file could not parsed as a " +
                                fileType + ":\n " +
                                file.getPath() +
                                "\n\n" +
                                "See ../Properties/ErrorLog.txt for more details.",
                                "Error Parsing File", JOptionPane.ERROR_MESSAGE);
                    }
                }

                progressDialog.setVisible(false);
                progressDialog.dispose();

                if (spectraJXTable.getRowCount() > 0) {
                    selectedSpectraJLabel.setEnabled(true);
                    numberOfSelectedSpectraJTextField.setEnabled(true);
                    spectrumAnnotationJLabel.setEnabled(true);

                    loadSpectraJLabel.setEnabled(false);
                    selectAllJCheckBox.setEnabled(false);
                }

                if (numberOfSelectedSpectra == 0) {
                    nextJButton.setEnabled(false);
                } else {
                    nextJButton.setEnabled(true);
                }
            }
        });

        t2.start();

        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_loadSpectraJLabelMouseClicked

    private void loadSpectraJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loadSpectraJLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
}//GEN-LAST:event_loadSpectraJLabelMouseEntered

    private void loadSpectraJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loadSpectraJLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_loadSpectraJLabelMouseExited

    /**
     * Saves the inserted information to the Properties object.
     */
    private void saveInsertedInformation() {
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        PRIDEConverter.getProperties().setSelectedSpectraKeys(new ArrayList());
        PRIDEConverter.getProperties().setSelectAllSpectra(selectAllJCheckBox.isSelected());

        if (spectraJXTable.getRowCount() > 0) {

            for (int i = 0; i < spectraJXTable.getRowCount(); i++) {
                if (((Boolean) spectraJXTable.getValueAt(i, 5)).booleanValue()) {

                    PRIDEConverter.getProperties().getSelectedSpectraKeys().add(
                            new Object[]{
                                spectraJXTable.getValueAt(i, 0),
                                spectraJXTable.getValueAt(i, 1),
                                spectraJXTable.getValueAt(i, 2),
                                spectraJXTable.getValueAt(i, 3),
                                spectraJXTable.getValueAt(i, 4)
                            });
                }
            }

            PRIDEConverter.getProperties().setSpectrumTableModel((DefaultTableModel) spectraJXTable.getModel());
        } else {
            PRIDEConverter.getProperties().setSpectrumTableModel(null);
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JButton backJButton;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JButton helpJButton;
    private javax.swing.JMenuItem invertSelectionJMenuItem;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel loadSpectraJLabel;
    private javax.swing.JButton nextJButton;
    private javax.swing.JTextField numberOfSelectedSpectraJTextField;
    private javax.swing.JCheckBox selectAllJCheckBox;
    private javax.swing.JMenuItem selectAllJMenuItem;
    private javax.swing.JPopupMenu selectAllJPopupMenu;
    private javax.swing.JLabel selectedSpectraJLabel;
    private org.jdesktop.swingx.JXTable spectraJXTable;
    private javax.swing.JLabel spectrumAnnotationJLabel;
    private javax.swing.JPopupMenu spectrumDetailsJPopupMenu;
    private javax.swing.JMenuItem viewSpectrumAnnotationsJMenuItem;
    // End of variables declaration//GEN-END:variables
}
