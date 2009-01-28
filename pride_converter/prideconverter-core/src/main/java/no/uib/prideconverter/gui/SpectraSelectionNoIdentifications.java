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
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import no.uib.prideconverter.util.Util;
import org.systemsbiology.jrap.MSXMLParser;
import org.systemsbiology.jrap.Scan;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;
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

    private PRIDEConverter prideConverter;
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
     * @param prideConverter
     * @param location where to position the frame
     */
    public SpectraSelectionNoIdentifications(PRIDEConverter prideConverter, Point location) {

        this.prideConverter = prideConverter;

        // sets the default wizard frame size
        this.setPreferredSize(new Dimension(prideConverter.getProperties().FRAME_WIDTH,
                prideConverter.getProperties().FRAME_HEIGHT));
        this.setSize(prideConverter.getProperties().FRAME_WIDTH,
                prideConverter.getProperties().FRAME_HEIGHT);
        this.setMaximumSize(new Dimension(prideConverter.getProperties().FRAME_WIDTH,
                prideConverter.getProperties().FRAME_HEIGHT));
        this.setMinimumSize(new Dimension(prideConverter.getProperties().FRAME_WIDTH,
                prideConverter.getProperties().FRAME_HEIGHT));

        initComponents();

        spectraJTable.getColumn("Selected").setMinWidth(75);
        spectraJTable.getColumn("Selected").setMaxWidth(75);
        spectraJTable.getColumn("Charge").setMaxWidth(75);
        spectraJTable.getColumn("Charge").setMinWidth(75);
        spectraJTable.getColumn("Mass").setMaxWidth(75);
        spectraJTable.getColumn("Mass").setMinWidth(75);
        spectraJTable.getColumn("ID").setMaxWidth(75);
        spectraJTable.getColumn("ID").setMinWidth(75);
        spectraJTable.getColumn("Level").setMaxWidth(75);
        spectraJTable.getColumn("Level").setMinWidth(75);

        columnToolTips = new Vector();
        columnToolTips.add("Filename of Spectrum File");
        columnToolTips.add("Spectrum Identification");
        columnToolTips.add("Precursor Mass");
        columnToolTips.add("Precursor Charge");
        columnToolTips.add("Mass Spectrometry Level");
        columnToolTips.add(null);

        spectraJTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        spectraJTable.getTableHeader().setReorderingAllowed(false);
        spectraJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        spectraJTable.setAutoCreateColumnsFromModel(false);
        spectraJTable.setAutoCreateRowSorter(true);

        // makes sure that inserting an MS level lower than 1 is not allowed
        // and displays a warning message for MS levels above 3
        spectraJTable.getModel().addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                if (spectraJTable.getSelectedColumn() == 4) {

                    int insertedValue = ((Integer) spectraJTable.getValueAt(
                            spectraJTable.getSelectedRow(), spectraJTable.getSelectedColumn())).intValue();

                    if (insertedValue < 2) {

                        JOptionPane.showMessageDialog(null, "The inserted MS level is smaller than 2!",
                                "Incorrect MS Level", JOptionPane.ERROR_MESSAGE);
                        spectraJTable.changeSelection(spectraJTable.getSelectedRow(), spectraJTable.getSelectedColumn(),
                                false, false);
                        spectraJTable.setValueAt(null, spectraJTable.getSelectedRow(), spectraJTable.getSelectedColumn());
                        spectraJTable.editCellAt(spectraJTable.getSelectedRow(), spectraJTable.getSelectedColumn());

                    } else if (insertedValue > 3) {

                        int option = JOptionPane.showConfirmDialog(null,
                                "The inserted MS level is larger than 3. Are you sure this is the correct MS level?",
                                "Verify MS Level", JOptionPane.YES_NO_OPTION);

                        if (option == JOptionPane.NO_OPTION) {
                            spectraJTable.changeSelection(spectraJTable.getSelectedRow(), spectraJTable.getSelectedColumn(),
                                    false, false);
                            spectraJTable.setValueAt(null, spectraJTable.getSelectedRow(), spectraJTable.getSelectedColumn());
                            spectraJTable.editCellAt(spectraJTable.getSelectedRow(), spectraJTable.getSelectedColumn());
                        }
                    }
                }
            }
        });

        selectAllJCheckBox.setSelected(prideConverter.getProperties().selectAllSpectra());

        if (prideConverter.getProperties().getSpectrumTableModel() != null) {
            spectraJTable.setModel(prideConverter.getProperties().getSpectrumTableModel());
            loadSpectraJButton.setEnabled(false);
            selectedSpectraJLabel.setEnabled(true);
            numberOfSelectedSpectraJTextField.setEnabled(true);

            selectAllJCheckBox.setEnabled(false);

            for (int i = 0; i < spectraJTable.getRowCount(); i++) {
                if (((Boolean) spectraJTable.getValueAt(i, 5)).booleanValue()) {
                    numberOfSelectedSpectra++;
                }
            }

            numberOfSelectedSpectraJTextField.setText("" +
                    numberOfSelectedSpectra + "/" +
                    spectraJTable.getRowCount());
        }

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        setTitle(prideConverter.getWizardName() + " " + prideConverter.getPrideConverterVersionNumber() +
                " - " + getTitle());

        if (location != null) {
            setLocation(location);
        } else {
            setLocationRelativeTo(null);
        }

        setVisible(true);

        if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("mzData") ||
                prideConverter.getProperties().getDataSource().equalsIgnoreCase("TPP")) {
            loadSpectraJButton.setEnabled(false);
            loadSpectraJButton.setToolTipText("Manual spectra selection is currently not supported for mzData and TPP");
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
        viewSpectrumParametersJMenuItem = new javax.swing.JMenuItem();
        nextJButton = new javax.swing.JButton();
        backJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        helpJButton = new javax.swing.JButton();
        aboutJButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        loadSpectraJButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        spectraJTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
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
        selectedSpectraJLabel = new javax.swing.JLabel();
        numberOfSelectedSpectraJTextField = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        selectAllJCheckBox = new javax.swing.JCheckBox();

        selectAllJMenuItem.setMnemonic('A');
        selectAllJMenuItem.setText("Select All");
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

        viewSpectrumParametersJMenuItem.setText("View/Change Spectrum Parameters");
        viewSpectrumParametersJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewSpectrumParametersJMenuItemActionPerformed(evt);
            }
        });
        spectrumDetailsJPopupMenu.add(viewSpectrumParametersJMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Spectra Selection - Step 2 of 8");
        setMinimumSize(new java.awt.Dimension(580, 560));
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

        jLabel3.setFont(new java.awt.Font("Tahoma", 2, 11));
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

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Manual Spectra Selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N

        loadSpectraJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/load2.GIF"))); // NOI18N
        loadSpectraJButton.setText("Load Spectra");
        loadSpectraJButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        loadSpectraJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSpectraJButtonActionPerformed(evt);
            }
        });

        spectraJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Filename", "ID", "Mass", "Charge", "Level", "Selected"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Double.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spectraJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                spectraJTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(spectraJTable);

        selectedSpectraJLabel.setText("Selected Spectra:");
        selectedSpectraJLabel.setToolTipText("Number of Selected Spectra");
        selectedSpectraJLabel.setEnabled(false);

        numberOfSelectedSpectraJTextField.setEditable(false);
        numberOfSelectedSpectraJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        numberOfSelectedSpectraJTextField.setToolTipText("Number of Selected Spectra");
        numberOfSelectedSpectraJTextField.setEnabled(false);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                    .addComponent(loadSpectraJButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(selectedSpectraJLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(numberOfSelectedSpectraJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numberOfSelectedSpectraJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectedSpectraJLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loadSpectraJButton)
                .addContainerGap())
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Simple Spectra Selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N

        selectAllJCheckBox.setSelected(true);
        selectAllJCheckBox.setText("Select All Spectra");
        selectAllJCheckBox.setIconTextGap(20);
        selectAllJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllJCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectAllJCheckBox)
                .addContainerGap(413, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectAllJCheckBox)
                .addContainerGap(10, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(helpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(aboutJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 239, Short.MAX_VALUE)
                        .addComponent(backJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nextJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cancelJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cancelJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nextJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(backJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(helpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(aboutJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))))
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
        prideConverter.cancelConvertion();
    }//GEN-LAST:event_cancelJButtonActionPerformed

    /**
     * Closes the frame and opens the next frame (ExperimentProperties).
     * 
     * @param evt
     */
    private void nextJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextJButtonActionPerformed
        saveInsertedInformation();
        new ExperimentProperties(prideConverter, this.getLocation());
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_nextJButtonActionPerformed

    /**
     * See cancelJButtonActionPerformed
     * 
     * @param evt
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
        new HelpWindow(this, getClass().getResource(
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
        new HelpWindow(this, getClass().getResource("/no/uib/prideconverter/helpfiles/AboutPRIDE_Converter.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_aboutJButtonActionPerformed

    /**
     * Closes the frame and opens the previous step (DataFileSelection).
     * 
     * @param evt
     */
    private void backJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backJButtonActionPerformed
        saveInsertedInformation();

        if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("TPP")) {
            new DataFileSelectionTPP(prideConverter, this.getLocation());
        } else {
            new DataFileSelection(prideConverter, this.getLocation());
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
    private void loadSpectraJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSpectraJButtonActionPerformed

        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        numberOfSelectedSpectra = 0;

        progressDialog = new ProgressDialog(this, true);
        progressDialog.setTitle("Loading Spectra. Please Wait.");
        progressDialog.setIntermidiate(true);

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                progressDialog.setVisible(true);
            }
        });

        t.start();

        Thread t2 = new Thread(new Runnable() {

            @Override
            public void run() {

                ArrayList spectraList;
                MSXMLParser msXMLParser;
                int scanCount;
                Scan scan;
                MzMLUnmarshaller unmarshallerMzMl;
                List<Spectrum> spectraMzMl;
                FileReader reader;
                MzDataXMLUnmarshaller unmarshallerMzData;
                MzData mzData;
                Collection<uk.ac.ebi.pride.model.interfaces.mzdata.Spectrum> spectraMzData;
                Iterator<uk.ac.ebi.pride.model.interfaces.mzdata.Spectrum> iterator;
                uk.ac.ebi.pride.model.interfaces.mzdata.Spectrum currentSpectrum;
                String[] values;

                for (int j = 0; j < prideConverter.getProperties().getSelectedSourceFiles().size(); j++) {

                    if (prideConverter.isConversionCanceled()) {

                        while (((DefaultTableModel) spectraJTable.getModel()).getRowCount() >
                                0) {
                            ((DefaultTableModel) spectraJTable.getModel()).removeRow(0);
                        }

                        numberOfSelectedSpectraJTextField.setText("");

                        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                        return;
                    }

                    try {
                        file = new File(prideConverter.getProperties().getSelectedSourceFiles().get(j));

                        progressDialog.setString(file.getName() + " (" + (j + 1) +
                                "/" + prideConverter.getProperties().getSelectedSourceFiles().size() +
                                ")");

                        if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("Sequest DTA File")) {

                            f = new FileReader(file);
                            b = new BufferedReader(f);

                            currentLine = b.readLine();

                            tok = new StringTokenizer(currentLine);

                            precursorMass = new Double(tok.nextToken());
                            precursorCharge = new Integer(tok.nextToken());

                            ((DefaultTableModel) spectraJTable.getModel()).addRow(
                                    new Object[]{
                                        file.getName(),
                                        null,
                                        precursorMass, precursorCharge,
                                        2,
                                        new Boolean(true)
                                    });

                            numberOfSelectedSpectra++;

                            b.close();
                            f.close();

                        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("Micromass PKL File")) {

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

                                    ((DefaultTableModel) spectraJTable.getModel()).addRow(
                                            new Object[]{
                                                file.getName(),
                                                null,
                                                precursorMass, precursorCharge,
                                                2,
                                                new Boolean(true)
                                            });

                                    numberOfSelectedSpectra++;
                                }

                                currentLine = b.readLine();
                            }

                            b.close();
                            f.close();

                        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("VEMS")) {

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

                                    ((DefaultTableModel) spectraJTable.getModel()).addRow(
                                            new Object[]{
                                                file.getName(),
                                                null,
                                                precursorMass, precursorCharge,
                                                2,
                                                new Boolean(true)
                                            });

                                    numberOfSelectedSpectra++;
                                }

                                currentLine = b.readLine();
                            }

                            b.close();
                            f.close();

                        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("mzXML")) {

                            spectraList = new ArrayList();

                            msXMLParser = new MSXMLParser(file.getAbsolutePath());

                            scanCount = msXMLParser.getScanCount();

                            for (int i = 1; i <= scanCount; i++) {
                                scan = msXMLParser.rap(i);

                                precursorCharge = scan.getPrecursorCharge();
                                precursorMass = new Float(scan.getPrecursorMz()).doubleValue();

                                if (precursorMass != -1 &&
                                        precursorCharge != -1) {
                                    ((DefaultTableModel) spectraJTable.getModel()).addRow(
                                            new Object[]{
                                                file.getName(),
                                                scan.getNum(),
                                                precursorMass, precursorCharge,
                                                2,
                                                new Boolean(true)
                                            });
                                } else if (precursorMass == -1 &&
                                        precursorCharge != -1) {
                                    ((DefaultTableModel) spectraJTable.getModel()).addRow(
                                            new Object[]{
                                                file.getName(),
                                                scan.getNum(),
                                                null, precursorCharge,
                                                2,
                                                new Boolean(true)
                                            });
                                } else if (precursorMass != -1 &&
                                        precursorCharge == -1) {
                                    ((DefaultTableModel) spectraJTable.getModel()).addRow(
                                            new Object[]{
                                                file.getName(),
                                                scan.getNum(),
                                                precursorMass, null,
                                                2,
                                                new Boolean(true)
                                            });
                                } else {
                                    ((DefaultTableModel) spectraJTable.getModel()).addRow(
                                            new Object[]{
                                                file.getName(),
                                                scan.getNum(),
                                                null, null,
                                                2,
                                                new Boolean(true)
                                            });
                                }

                                numberOfSelectedSpectra++;

                                numberOfSelectedSpectraJTextField.setText(numberOfSelectedSpectra +
                                        "/" +
                                        numberOfSelectedSpectra);
                            }
                        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("mzML")) {

                            unmarshallerMzMl = new MzMLUnmarshaller(file);
                            prideConverter.getProperties().setMzMlFile(unmarshallerMzMl.unmarshall());
                            spectraMzMl = prideConverter.getProperties().getMzMlFile().getRun().getSpectrumList().getSpectrum();

                            for (int i = 0; i < spectraMzMl.size(); i++) {

                                ((DefaultTableModel) spectraJTable.getModel()).addRow(
                                        new Object[]{
                                            file.getName(),
                                            spectraMzMl.get(i).getId(),//.getIndex().toString(),
                                            //spectraMzMl.get(i).getSpectrumDescription().getPrecursorList().getPrecursor().get(0).getSelectedIonList().getSelectedIon().get(0).getCvParam().get(0).getValue(),
                                            null,
                                            null,
                                            2,
                                            new Boolean(true)
                                        });

                                numberOfSelectedSpectra++;

                                numberOfSelectedSpectraJTextField.setText(numberOfSelectedSpectra +
                                        "/" + numberOfSelectedSpectra);
                            }

                            prideConverter.getProperties().setDataFileHasBeenLoaded(true);
                        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("mzData")) {

                            reader = new FileReader(file);

                            unmarshallerMzData = new MzDataXMLUnmarshaller();
                            mzData = unmarshallerMzData.unMarshall(reader);

                            spectraMzData = mzData.getSpectrumCollection();
                            iterator = spectraMzData.iterator();

                            while (iterator.hasNext()) {
                                currentSpectrum = iterator.next();

                                ((DefaultTableModel) spectraJTable.getModel()).addRow(
                                        new Object[]{
                                            file.getName(),
                                            "" + currentSpectrum.getSpectrumId(),
                                            null, null,
                                            2,
                                            new Boolean(true)
                                        });

                                numberOfSelectedSpectra++;
                                numberOfSelectedSpectraJTextField.setText(numberOfSelectedSpectra +
                                        "/" + numberOfSelectedSpectra);
                            }

                            reader.close();

                        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("Mascot Generic File")) {

                            f = new FileReader(file);
                            b = new BufferedReader(f);

                            String line = null;
                            int lineCount = 0;
                            boolean inSpectrum = false;
                            precursorMass = null;
                            precursorCharge = null;

                            while ((line = b.readLine()) != null &&
                                    progressDialog.isVisible()) {
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

                                    ((DefaultTableModel) spectraJTable.getModel()).addRow(
                                            new Object[]{
                                                file.getName(),
                                                null,
                                                precursorMass, precursorCharge,
                                                2,
                                                new Boolean(true)
                                            });

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

                                        //if (prideConverter.getProperties().isCommaTheDecimalSymbol()) {
                                        value = value.replaceAll(",", ".");
                                        //}

                                        values = value.split("\t");
                                        precursorMass = Double.parseDouble(values[0]);

                                    } else if (line.startsWith("CHARGE")) {
                                        // CHARGE line found.
                                        // Note the extra parsing to read a Mascot Generic File charge (eg., 1+).
                                        precursorCharge = extractCharge(line.substring(equalSignIndex + 1));
                                    }
                                }
                            }

                            b.close();

                        } else if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("MS2")) {

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

                                    if (prideConverter.getProperties().isCommaTheDecimalSymbol()) {
                                        line = line.replaceAll(",", ".");
                                    }

                                    values = line.split("\t");

                                    precursorMass = new Double(values[3]);


                                    chargeDecided = false;

                                } else if (line.startsWith("Z") && !chargeDecided) {

                                    String nextLine = b.readLine();

                                    // multiple charges
                                    if (nextLine.startsWith("Z")) {
                                        precursorCharge = null;
                                    } else {
                                        values = line.split("\t");
                                        precursorCharge = new Integer(values[1]);
                                    }

                                    chargeDecided = true;

                                    ((DefaultTableModel) spectraJTable.getModel()).addRow(
                                            new Object[]{
                                                file.getName(),
                                                null,
                                                precursorMass, precursorCharge,
                                                2,
                                                new Boolean(true)
                                            });

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
                        JOptionPane.showMessageDialog(null, "The file named " +
                                file.getPath() +
                                "\ncould not be found.",
                                "File Not Found", JOptionPane.ERROR_MESSAGE);
                        Util.writeToErrorLog("Error when trying to read file: ");
                        ex.printStackTrace();
                    } catch (Exception e) {

                        Util.writeToErrorLog("Error parsing " + prideConverter.getProperties().getDataSource() + ": ");
                        e.printStackTrace();

                        String fileType = prideConverter.getProperties().getDataSource();

                        JOptionPane.showMessageDialog(null,
                                "The following file could not parsed as a " +
                                fileType + ":\n " +
                                file.getPath() +
                                "\n\n" +
                                "See ../Properties/ErrorLog.txt for more details.",
                                "Error Parsing File", JOptionPane.ERROR_MESSAGE);
                    }
                }

                progressDialog.setVisible(false);
                progressDialog.dispose();

                if (spectraJTable.getRowCount() > 0) {
                    selectedSpectraJLabel.setEnabled(true);
                    numberOfSelectedSpectraJTextField.setEnabled(true);

                    loadSpectraJButton.setEnabled(false);
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
    }//GEN-LAST:event_loadSpectraJButtonActionPerformed

    /**
     * Makes sure that the number of selected spectra is updated when the 
     * user selects or deselects a spectra in the list. Right clicking in 
     * the last column open a popup menu with advanced selection option.
     * 
     * @param evt
     */
    private void spectraJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spectraJTableMouseClicked

        if(spectraJTable.columnAtPoint(evt.getPoint()) != -1
                && spectraJTable.rowAtPoint(evt.getPoint()) != -1){
            spectraJTable.changeSelection(spectraJTable.rowAtPoint(evt.getPoint()),
                    spectraJTable.columnAtPoint(evt.getPoint()), false, false);
        }
        
        if (spectraJTable.columnAtPoint(evt.getPoint()) == 5) {

            this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

            numberOfSelectedSpectra = 0;

            for (int i = 0; i < spectraJTable.getRowCount(); i++) {
                if (((Boolean) spectraJTable.getValueAt(i, 5)).booleanValue()) {
                    numberOfSelectedSpectra++;
                }
            }

            numberOfSelectedSpectraJTextField.setText("" +
                    numberOfSelectedSpectra + "/" +
                    spectraJTable.getRowCount());

            if (numberOfSelectedSpectra == 0) {
                nextJButton.setEnabled(false);
            } else {
                nextJButton.setEnabled(true);
            }
        }

        int column = spectraJTable.columnAtPoint(evt.getPoint());

        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3 && column == 5) {
            selectAllJPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        } else {
            if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
                spectrumDetailsJPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_spectraJTableMouseClicked

    /**
     * Selects all the spectra in the table.
     * 
     * @param evt
     */
    private void selectAllJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllJMenuItemActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        if (selectAll) {
            for (int i = 0; i < spectraJTable.getRowCount(); i++) {
                if (!(((Boolean) spectraJTable.getValueAt(i, 5)).booleanValue())) {
                    spectraJTable.setValueAt(new Boolean(true), i, 5);
                }
            }

            numberOfSelectedSpectraJTextField.setText("" +
                    spectraJTable.getRowCount() + "/" +
                    spectraJTable.getRowCount());
            numberOfSelectedSpectra =
                    spectraJTable.getRowCount();

        } else {
            for (int i = 0; i < spectraJTable.getRowCount(); i++) {
                if ((((Boolean) spectraJTable.getValueAt(i, 5)).booleanValue())) {
                    spectraJTable.setValueAt(new Boolean(false), i, 5);
                }
            }

            numberOfSelectedSpectraJTextField.setText("0" + "/" +
                    spectraJTable.getRowCount());
            numberOfSelectedSpectra = 0;
        }

        if (numberOfSelectedSpectra == 0) {
            nextJButton.setEnabled(false);
        } else {
            nextJButton.setEnabled(true);
        }

        selectAll = !selectAll;

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_selectAllJMenuItemActionPerformed

    /**
     * Inverts the selection in the table.
     * 
     * @param evt
     */
    private void invertSelectionJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invertSelectionJMenuItemActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        numberOfSelectedSpectra = 0;

        for (int i = 0; i < spectraJTable.getRowCount(); i++) {
            spectraJTable.setValueAt(
                    new Boolean(!((Boolean) spectraJTable.getValueAt(i, 5)).booleanValue()), i, 5);

            if (((Boolean) spectraJTable.getValueAt(i, 5)).booleanValue()) {
                numberOfSelectedSpectra++;
            }
        }

        numberOfSelectedSpectraJTextField.setText("" + numberOfSelectedSpectra +
                "/" + spectraJTable.getRowCount());

        if (numberOfSelectedSpectra == 0) {
            nextJButton.setEnabled(false);
        } else {
            nextJButton.setEnabled(true);
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

    private void viewSpectrumParametersJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewSpectrumParametersJMenuItemActionPerformed
        new SpectrumDetails(this, true, prideConverter);
    }//GEN-LAST:event_viewSpectrumParametersJMenuItemActionPerformed

    /**
     * Saves the inserted information to the Properties object.
     */
    private void saveInsertedInformation() {
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        prideConverter.getProperties().setSelectedSpectraNames(new ArrayList());
        prideConverter.getProperties().setSelectAllSpectra(selectAllJCheckBox.isSelected());

        if (spectraJTable.getRowCount() > 0) {

            for (int i = 0; i < spectraJTable.getRowCount(); i++) {
                if (((Boolean) spectraJTable.getValueAt(i, 5)).booleanValue()) {

                    prideConverter.getProperties().getSelectedSpectraNames().add(
                            new Object[]{
                                spectraJTable.getValueAt(i, 0),
                                spectraJTable.getValueAt(i, 1),
                                spectraJTable.getValueAt(i, 2),
                                spectraJTable.getValueAt(i, 3),
                                spectraJTable.getValueAt(i, 4)
                            });
                }
            }

            prideConverter.getProperties().setSpectrumTableModel((DefaultTableModel) spectraJTable.getModel());
        } else {
            prideConverter.getProperties().setSpectrumTableModel(null);
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    /**
     * This method extracts an integer from Mascot Generic File charge notation, eg.,
     * 1+.
     * Remark that the charge can also be annotated as "+2,+3", in those rather cases the charge is also "not known." So we save a zero value.
     *
     * @param aCharge   String with the Mascot Generic File charge notation (eg., 1+).
     * @return  int with the corresponding integer.
     */
    private static int extractCharge(String aCharge) {
        int charge = 0;

        // Trim the charge String.
        String trimmedCharge = aCharge.trim();

        boolean negate = false;
        boolean multiCharge = false;

        // See if there is a '-' in the charge String.
        if (trimmedCharge.indexOf("-") >= 0) {
            negate = true;
        }

        // See if there are multiple charges assigned to this spectrum.
        if (trimmedCharge.indexOf(",") >= 0) {
            multiCharge = true;
        }

        if (!multiCharge) {
            // Charge is now: trimmedCharge without the sign character,
            // negated if necessary.

            if (trimmedCharge.endsWith("+")) {
                charge = Integer.parseInt(trimmedCharge.substring(0, trimmedCharge.length() -
                        1));
            } else {
                charge = Integer.parseInt(trimmedCharge);
            }

            if (negate) {
                charge = -charge;
            }
        }

        return charge;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton loadSpectraJButton;
    private javax.swing.JButton nextJButton;
    private javax.swing.JTextField numberOfSelectedSpectraJTextField;
    private javax.swing.JCheckBox selectAllJCheckBox;
    private javax.swing.JMenuItem selectAllJMenuItem;
    private javax.swing.JPopupMenu selectAllJPopupMenu;
    private javax.swing.JLabel selectedSpectraJLabel;
    private javax.swing.JTable spectraJTable;
    private javax.swing.JPopupMenu spectrumDetailsJPopupMenu;
    private javax.swing.JMenuItem viewSpectrumParametersJMenuItem;
    // End of variables declaration//GEN-END:variables
}
