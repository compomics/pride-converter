package no.uib.prideconverter.gui;

import no.uib.prideconverter.PRIDEConverter;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import uk.ac.ebi.pride.model.implementation.mzData.CvParamImpl;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.StringTokenizer;
import no.uib.prideconverter.gui.ComboBoxInputDialog;
import no.uib.prideconverter.util.ComboBoxInputable;
import no.uib.prideconverter.util.MyComboBoxRenderer;
import uk.ac.ebi.pride.model.interfaces.mzdata.CvParam;

/**
 * A frame where information about the samples can be inserted.
 *
 * @author  Harald Barsnes
 * 
 * Created March 2008
 */
public class SampleDetails extends javax.swing.JFrame implements ComboBoxInputable {

    private PRIDEConverter prideConverter;
    private String lastSelectedSampleName;
    private String samplePath;
    private boolean valuesChanged = false;
    private Vector columnToolTips;
    private boolean keepQuantificationSelection = false;

    /**
     * Opens a new SampleDetails frame.
     * 
     * @param prideConverter
     * @param location where to locate the frame on the screen
     */
    public SampleDetails(PRIDEConverter prideConverter, Point location) {
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

        TableColumn quantificationColumn = sampleDetailsJTable.getColumnModel().getColumn(2);

        JComboBox comboBox = new JComboBox();
        comboBox.addItem("iTRAQ reagent 114");
        comboBox.addItem("iTRAQ reagent 115");
        comboBox.addItem("iTRAQ reagent 116");
        comboBox.addItem("iTRAQ reagent 117");
        comboBox.addItem("None");

        comboBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quantificationComboBoxActionPerformed(evt);
            }
        });

        quantificationColumn.setCellEditor(new DefaultCellEditor(comboBox));

        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();
        renderer.setToolTipText("Click to choose quantification method");
        quantificationColumn.setCellRenderer(renderer);

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        sampleDetailsJTable.getTableHeader().setReorderingAllowed(false);
        sampleDetailsJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        setTitle(prideConverter.getWizardName() + " " +
                prideConverter.getPrideConverterVersionNumber() + " - " + getTitle());

        sampleDetailsJTable.getColumn(" ").setMaxWidth(40);
        sampleDetailsJTable.getColumn(" ").setMinWidth(40);
        sampleDetailsJTable.getColumn("Quantification").setMaxWidth(110);
        sampleDetailsJTable.getColumn("Quantification").setMinWidth(110);

        columnToolTips = new Vector();
        columnToolTips.add(null);
        columnToolTips.add(null);
        columnToolTips.add("Quantification method used");

        if (prideConverter.getProperties().getCurrentQuantificationSelection() == null) {
            prideConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());
        }

        lowerRangeJTextField.setText("" +
                prideConverter.getUserProperties().getPeakIntegrationRangeLower());
        upperRangeJTextField.setText("" +
                prideConverter.getUserProperties().getPeakIntegrationRangeUpper());
        intensityThresholdJTextField.setText("" +
                prideConverter.getUserProperties().getReporterIonIntensityThreshold());

        String temp = "";

        for (int i = 0; i < prideConverter.getUserProperties().getPurityCorrections().length - 1; i++) {
            temp += prideConverter.getUserProperties().getPurityCorrections()[i] + ",";
        }

        temp += prideConverter.getUserProperties().getPurityCorrections()[prideConverter.getUserProperties().getPurityCorrections().length - 1];

        purityCorrectionsJTextField.setText(temp);

        if (prideConverter.useHardcodedPaths()) {
            samplePath = "D:/PRIDE_ms_lims/ms_lims_to_PRIDE/PRIDEConverter/Release/Properties/Samples/";
        } else {
            samplePath = "" +
                    this.getClass().getProtectionDomain().getCodeSource().getLocation();
            samplePath = samplePath.substring(5, samplePath.lastIndexOf("/"));
            samplePath = samplePath.substring(0, samplePath.lastIndexOf("/") + 1) +
                    "Properties/Samples/";
            samplePath = samplePath.replace("%20", " ");
        }

        setLocation(location);
        setVisible(true);

        // if the file is an mzData file the sample details are extracted
        if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("mzData") &&
                !prideConverter.getProperties().areSampleDetailsExtracted()) {
            extractSampleDetailsFromFile();
        }

        keepQuantificationSelection = true;
        readSamplesFromFile();
        mandatoryFieldsCheck();
        keepQuantificationSelection = false;
    }

    /**
     * Extracts sample details from an mzData or mzXML file and inserts them into 
     * the sample table.
     */
    private void extractSampleDetailsFromFile() {

        String sampleName;

        Collection<CvParam> cvParams = prideConverter.getProperties().getMzDataFile().getSampleDescriptionCvParams();

        prideConverter.getProperties().setSampleDescriptionCVParams(new ArrayList());

        Iterator<CvParam> iterator = cvParams.iterator();

        long orderIndex = 0;

        while (iterator.hasNext()) {

            CvParam tempCvParam = iterator.next();

            prideConverter.getProperties().getSampleDescriptionCVParams().add(
                    new CvParamImpl(tempCvParam.getAccession(),
                    tempCvParam.getCVLookup(),
                    tempCvParam.getName(),
                    new Long(orderIndex++),
                    "SUBSAMPLE_1"));
        }

        sampleName = prideConverter.getProperties().getMzDataFile().getSampleName();

        ArrayList sampleNames = new ArrayList();
        sampleNames.add(sampleName);
        prideConverter.getProperties().setSampleDescriptionUserSubSampleNames(sampleNames);

        prideConverter.getUserProperties().setCurrentSampleSet(sampleName);

        String newName = samplePath + sampleName + ".sam";

        // check if the sample already exists
        if (new File(newName).exists()) {

            int option =
                    JOptionPane.showConfirmDialog(this,
                    "The sample name \'" +
                    sampleName +
                    "\' is already in use.\nDo you want to use the existing sample?",
                    "Use Existing Sample?",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.NO_OPTION) {

                option =
                        JOptionPane.showConfirmDialog(this,
                        "Do you want to overwrite this sample?",
                        "Overwrite Existing Sample?",
                        JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.NO_OPTION) {

                    sampleName = JOptionPane.showInputDialog(this, "Please provide the name of the new sample:",
                            sampleName);

                    while (sampleName == null) {
                        sampleName = JOptionPane.showInputDialog(this, "Please provide a valid name:",
                                sampleName);
                    }

                    newName = samplePath + sampleName + ".sam";

                    while (new File(newName).exists()) {
                        sampleName = JOptionPane.showInputDialog(this,
                                "This name is also in use. Please choose a different name: ",
                                sampleName);

                        while (sampleName == null) {
                            sampleName = JOptionPane.showInputDialog(this, "Please provide a valid name:",
                                    sampleName);
                        }

                        newName = samplePath + sampleName + ".sam";
                    }

                    prideConverter.getUserProperties().setCurrentSampleSet(sampleName);

                    try {
                        FileWriter r = new FileWriter(newName);
                        BufferedWriter bw = new BufferedWriter(r);

                        bw.write("Name: " + sampleName + "\n");

                        if (prideConverter.getProperties().getSampleDescriptionCVParams() != null) {

                            iterator = prideConverter.getProperties().getSampleDescriptionCVParams().iterator();

                            CvParamImpl tempCv;

                            bw.write(prideConverter.getProperties().getSampleDescriptionCVParams().size() +
                                    "\n");

                            for (int i = 0; i <
                                    prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().size(); i++) {
                                bw.write(prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().get(i) +
                                        "\n");
                            }

                            bw.write("#\n");

                            while (iterator.hasNext()) {
                                tempCv = ((CvParamImpl) iterator.next());

                                bw.write("Accession: " + tempCv.getAccession() + "\n");
                                bw.write("CVLookup: " + tempCv.getCVLookup() + "\n");
                                bw.write("Name: " + tempCv.getName() + "\n");
                                bw.write("Value: " + tempCv.getValue() + "\n\n");
                            }
                        } else {
                            bw.write("0");
                        }

                        valuesChanged = false;

                        bw.close();
                        r.close();

                        prideConverter.getProperties().setSampleDescriptionCVParamsQuantification(new ArrayList());
                        prideConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());

                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } else {
            try {
                FileWriter r = new FileWriter(newName);
                BufferedWriter bw = new BufferedWriter(r);

                bw.write("Name: " + sampleName + "\n");

                if (prideConverter.getProperties().getSampleDescriptionCVParams() != null) {

                    iterator = prideConverter.getProperties().getSampleDescriptionCVParams().iterator();

                    CvParamImpl tempCv;

                    bw.write(prideConverter.getProperties().getSampleDescriptionCVParams().size() +
                            "\n");

                    for (int i = 0; i <
                            prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().size(); i++) {
                        bw.write(prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().get(i) +
                                "\n");
                    }

                    bw.write("#\n");


                    while (iterator.hasNext()) {
                        tempCv = ((CvParamImpl) iterator.next());

                        bw.write("Accession: " + tempCv.getAccession() + "\n");
                        bw.write("CVLookup: " + tempCv.getCVLookup() + "\n");
                        bw.write("Name: " + tempCv.getName() + "\n");
                        bw.write("Value: " + tempCv.getValue() + "\n\n");
                    }
                } else {
                    bw.write("0");
                }

                valuesChanged = false;

                bw.close();
                r.close();

                prideConverter.getProperties().setSampleDescriptionCVParamsQuantification(new ArrayList());
                prideConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());

            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        prideConverter.getProperties().setSampleDetailsExtracted(true);
    }

    /**
     * Makes the quantification parameters enabled or disabled based on if a 
     * quantification lable is selected or not.
     * 
     * @param evt
     */
    private void quantificationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {

        boolean quantificationSelected = false;

        for (int i = 0; i < sampleDetailsJTable.getRowCount(); i++) {

            if (sampleDetailsJTable.getValueAt(i, 2) != null) {
                if (!((String) sampleDetailsJTable.getValueAt(i, 2)).equalsIgnoreCase("None")) {
                    quantificationSelected = true;
                }
            }
        }

        lowerRangeJTextField.setEnabled(quantificationSelected);
        lowerRangeJLabel.setEnabled(quantificationSelected);
        purityCorrectionsJLabel.setEnabled(quantificationSelected);
        purityCorrectionsJTextField.setEnabled(quantificationSelected);
        upperRangeJLabel.setEnabled(quantificationSelected);
        upperRangeJTextField.setEnabled(quantificationSelected);
        intensityThresholdJLabel.setEnabled(quantificationSelected);
        intensityThresholdJTextField.setEnabled(quantificationSelected);
        peakRangeJLabel.setEnabled(quantificationSelected);
    }

    /**
     * Reads the samples from file and inserts the names into the names combo 
     * box.
     */
    private void readSamplesFromFile() {

        File file = new File(samplePath);

        try {
            if (!file.exists()) {
                file.mkdir();
            }

            File[] sampleFiles = file.listFiles();
            FileReader f = null;
            BufferedReader b = null;
            String tempSampleName;
            Vector sampleNames = new Vector();

            for (int i = 0; i < sampleFiles.length; i++) {

                if (sampleFiles[i].getAbsolutePath().endsWith(".sam")) {
                    f = new FileReader(sampleFiles[i]);
                    b = new BufferedReader(f);

                    tempSampleName = b.readLine();
                    tempSampleName = tempSampleName.substring(tempSampleName.indexOf(": ") + 2);
                    sampleNames.add(tempSampleName);

                    b.close();
                    f.close();
                }
            }

            java.util.Collections.sort(sampleNames);

            Vector comboboxTooltips = new Vector();
            comboboxTooltips.add(null);

            for (int i = 0; i < sampleNames.size(); i++) {
                comboboxTooltips.add(sampleNames.get(i));
            }

            comboboxTooltips.add(null);

            namesJComboBox.setRenderer(new MyComboBoxRenderer(comboboxTooltips));

            sampleNames.insertElementAt("- Please select a sample set -", 0);
            sampleNames.add("   Create a new sample set...");

            namesJComboBox.setModel(new DefaultComboBoxModel(sampleNames));
            namesJComboBox.setSelectedItem(prideConverter.getUserProperties().getCurrentSampleSet());

            lastSelectedSampleName = prideConverter.getUserProperties().getCurrentSampleSet();

            namesJComboBoxActionPerformed(null);

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Enables the Next button if a valid sample set is selected.
     */
    private void mandatoryFieldsCheck() {
        if (sampleDetailsJTable.getRowCount() > 0) {
            nextJButton.setEnabled(true);
        } else {
            nextJButton.setEnabled(false);
        }

        if (namesJComboBox.getSelectedIndex() != 0 &&
                namesJComboBox.getSelectedIndex() !=
                namesJComboBox.getItemCount() - 1 &&
                namesJComboBox.getSelectedIndex() != -1) {
            deleteJButton.setEnabled(true);
        } else {
            deleteJButton.setEnabled(false);
        }
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
        popupMenu = new javax.swing.JPopupMenu();
        deleteJMenuItem = new javax.swing.JMenuItem();
        nextJButton = new javax.swing.JButton();
        backJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        sampleDetailsJTable = new JTable() {
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
        sampleDetailsJButton = new javax.swing.JButton();
        jLabel32 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        namesJComboBox = new javax.swing.JComboBox();
        deleteJButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        helpJButton = new javax.swing.JButton();
        aboutJButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        lowerRangeJLabel = new javax.swing.JLabel();
        lowerRangeJTextField = new javax.swing.JTextField();
        upperRangeJLabel = new javax.swing.JLabel();
        upperRangeJTextField = new javax.swing.JTextField();
        peakRangeJLabel = new javax.swing.JLabel();
        intensityThresholdJLabel = new javax.swing.JLabel();
        intensityThresholdJTextField = new javax.swing.JTextField();
        purityCorrectionsJLabel = new javax.swing.JLabel();
        purityCorrectionsJTextField = new javax.swing.JTextField();

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

        deleteJMenuItem.setText("Delete Sample Set");
        deleteJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteJMenuItemActionPerformed(evt);
            }
        });
        popupMenu.add(deleteJMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Sample Properties - Step 4 of 8");
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

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Samples Set", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));

        sampleDetailsJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "CV Terms", "Quantification"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        sampleDetailsJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sampleDetailsJTableMouseClicked(evt);
            }
        });
        sampleDetailsJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                sampleDetailsJTableKeyReleased(evt);
            }
        });
        jScrollPane3.setViewportView(sampleDetailsJTable);

        sampleDetailsJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/ols_transparent.GIF"))); // NOI18N
        sampleDetailsJButton.setText("Add Sample");
        sampleDetailsJButton.setEnabled(false);
        sampleDetailsJButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        sampleDetailsJButton.setPreferredSize(new java.awt.Dimension(159, 23));
        sampleDetailsJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sampleDetailsJButtonActionPerformed(evt);
            }
        });

        jLabel32.setText("Name:");

        jLabel2.setFont(new java.awt.Font("Tahoma", 2, 11));
        jLabel2.setText("Preferred Ontologies are: NEWT for species, BTO for tissue, CTO for cell type, GO and DO for disease state");

        namesJComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                namesJComboBoxMouseClicked(evt);
            }
        });
        namesJComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                namesJComboBoxItemStateChanged(evt);
            }
        });
        namesJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                namesJComboBoxActionPerformed(evt);
            }
        });

        deleteJButton.setFont(new java.awt.Font("Arial", 1, 11));
        deleteJButton.setText("X");
        deleteJButton.setToolTipText("Delete Selected Sample Set");
        deleteJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteJButton.setIconTextGap(0);
        deleteJButton.setMargin(new java.awt.Insets(1, 6, 1, 6));
        deleteJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                    .addComponent(sampleDetailsJButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel32)
                        .addGap(18, 18, 18)
                        .addComponent(namesJComboBox, 0, 446, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteJButton))
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel32)
                    .addComponent(deleteJButton)
                    .addComponent(namesJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sampleDetailsJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel3.setFont(new java.awt.Font("Tahoma", 2, 11));
        jLabel3.setText("Select a sample set from the list, or create your own, and click on 'Next' to continue.");

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

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Quantification Parameters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));

        lowerRangeJLabel.setText("Peak Integration Range Lower:");
        lowerRangeJLabel.setEnabled(false);

        lowerRangeJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        lowerRangeJTextField.setText("-0.05");
        lowerRangeJTextField.setEnabled(false);

        upperRangeJLabel.setText("Peak Integration Range Upper:");
        upperRangeJLabel.setEnabled(false);

        upperRangeJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        upperRangeJTextField.setText("0.05");
        upperRangeJTextField.setEnabled(false);

        peakRangeJLabel.setFont(new java.awt.Font("Tahoma", 2, 11));
        peakRangeJLabel.setText("(|lower range| + upper range < 1)");
        peakRangeJLabel.setEnabled(false);

        intensityThresholdJLabel.setText("Reporter Ion Intensity Threshold:");
        intensityThresholdJLabel.setEnabled(false);

        intensityThresholdJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        intensityThresholdJTextField.setText("0");
        intensityThresholdJTextField.setEnabled(false);

        purityCorrectionsJLabel.setText("Purity Corrections:");
        purityCorrectionsJLabel.setToolTipText("16 comma-separated purity-correction percentages in the order: -2, -1, +1, +2 for each consecutive tag");
        purityCorrectionsJLabel.setEnabled(false);

        purityCorrectionsJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        purityCorrectionsJTextField.setText("0,1,5.90,2,0,2,5.6,0.1,0,3,4.5,0.1,0.1,4,3.5,0.1");
        purityCorrectionsJTextField.setToolTipText("16 comma-separated purity-correction percentages in the order: -2, -1, +1, +2 for each consecutive tag");
        purityCorrectionsJTextField.setEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(upperRangeJLabel)
                    .addComponent(intensityThresholdJLabel)
                    .addComponent(lowerRangeJLabel)
                    .addComponent(purityCorrectionsJLabel))
                .addGap(14, 14, 14)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(intensityThresholdJTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                            .addComponent(upperRangeJTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                            .addComponent(lowerRangeJTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(peakRangeJLabel)
                        .addGap(90, 90, 90))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(purityCorrectionsJTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lowerRangeJLabel)
                    .addComponent(peakRangeJLabel)
                    .addComponent(lowerRangeJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(upperRangeJLabel)
                    .addComponent(upperRangeJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(intensityThresholdJLabel)
                    .addComponent(intensityThresholdJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(purityCorrectionsJLabel)
                    .addComponent(purityCorrectionsJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                    .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cancelJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(backJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nextJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
     * If the user double clicks on a row in the table the OLS 
     * dialog is shown where the information about the given sample can be 
     * altered. If the user right clicks a pop up menu is shown for editing, 
     * moving or delting the selected sample.
     * 
     * @param evt
     */
    private void sampleDetailsJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sampleDetailsJTableMouseClicked
        if (evt.getButton() == 3) {

            int row = sampleDetailsJTable.rowAtPoint(evt.getPoint());
            int column = sampleDetailsJTable.columnAtPoint(evt.getPoint());

            sampleDetailsJTable.changeSelection(row, column, false, false);

            this.moveUpJMenuItem.setEnabled(true);
            this.moveDownJMenuItem.setEnabled(true);

            if (row == sampleDetailsJTable.getRowCount() - 1) {
                this.moveDownJMenuItem.setEnabled(false);
            }

            if (row == 0) {
                this.moveUpJMenuItem.setEnabled(false);
            }

            popupJMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        } else if (evt.getButton() == 1 && evt.getClickCount() == 2) {
            editJMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_sampleDetailsJTableMouseClicked

    /**
     * Delete the selected sample.
     * 
     * @param evt
     */
    private void deleteSelectedRowJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSelectedRowJMenuItemActionPerformed

        int selectedRow = sampleDetailsJTable.getSelectedRow();

        if (selectedRow != -1) {

            prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().remove(selectedRow);

            ((DefaultTableModel) sampleDetailsJTable.getModel()).removeRow(selectedRow);

            //remove from datastructure as well
            Object[] sampleDetails = prideConverter.getProperties().getSampleDescriptionCVParams().toArray();

            prideConverter.getProperties().setSampleDescriptionCVParams(new ArrayList());

            String tempRow;
            Vector subSampleNumbers = new Vector();

            for (int i = 0; i < sampleDetailsJTable.getRowCount(); i++) {
                tempRow = (String) sampleDetailsJTable.getValueAt(i, 1);

                for (int j = 0; j < tempRow.length(); j++) {
                    if (tempRow.charAt(j) == ']') {
                        subSampleNumbers.add("SUBSAMPLE_" + (i + 1));
                    //System.out.println("SUBSAMPLE_" + (i+1));
                    }
                }
            }

            CvParamImpl tempCvParamImpl;
            int counter = 0;

            for (int i = 0; i < sampleDetails.length; i++) {

                tempCvParamImpl = (CvParamImpl) sampleDetails[i];

                if (!tempCvParamImpl.getValue().endsWith("_" +
                        (selectedRow + 1))) {

                    tempCvParamImpl = new CvParamImpl(tempCvParamImpl.getAccession(),
                            tempCvParamImpl.getCVLookup(),
                            tempCvParamImpl.getName(),
                            new Long(counter),
                            (String) subSampleNumbers.get(counter++));

                    prideConverter.getProperties().getSampleDescriptionCVParams().add(tempCvParamImpl);
                }
            }

            valuesChanged = true;

            fixIndices();
            mandatoryFieldsCheck();
        }
    }//GEN-LAST:event_deleteSelectedRowJMenuItemActionPerformed

    /**
     * Stores the sample set selected and opens the next frame (ProtocolDetails).
     * 
     * @param evt
     */
    private void nextJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextJButtonActionPerformed
        if (saveInsertedInformation()) {
            new ProtocolDetails(prideConverter, this.getLocation());
            this.setVisible(false);
            this.dispose();
        }
    }//GEN-LAST:event_nextJButtonActionPerformed

    /**
     * Stores the sample set selected and opens the previous frame (ExperimentProperties).
     * 
     * @param evt
     */
    private void backJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backJButtonActionPerformed
        if (saveInsertedInformation()) {
            new ExperimentProperties(prideConverter, this.getLocation());
            this.setVisible(false);
            this.dispose();
        }
    }//GEN-LAST:event_backJButtonActionPerformed

    /**
     * Closes the frame, and ends the wizard.
     * 
     * @param evt
     */
    private void cancelJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
        this.setVisible(false);
        this.dispose();
        prideConverter.cancelConvertion();
    }//GEN-LAST:event_cancelJButtonActionPerformed

    /**
     * Opens an OLS dialog for editing the current sample.
     * 
     * @param evt
     */
    private void sampleDetailsJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sampleDetailsJButtonActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new NewSample(this, true, prideConverter);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_sampleDetailsJButtonActionPerformed

    /**
     * Opens an OLS dialog for editing the current sample.
     * 
     * @param evt
     */
    private void editJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editJMenuItemActionPerformed
        int selectedRow = sampleDetailsJTable.getSelectedRow();

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new NewSample(this, true, prideConverter, selectedRow);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_editJMenuItemActionPerformed

    /**
     * Moves the selected sample up one position in the table.
     * 
     * @param evt
     */
    private void moveUpJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpJMenuItemActionPerformed
        int selectedRow = sampleDetailsJTable.getSelectedRow();
        int selectedColumn = sampleDetailsJTable.getSelectedColumn();

        Object[] tempRow = new Object[]{
            sampleDetailsJTable.getValueAt(selectedRow - 1, 0),
            sampleDetailsJTable.getValueAt(selectedRow - 1, 1),
            sampleDetailsJTable.getValueAt(selectedRow - 1, 2)
        };

        ((DefaultTableModel) sampleDetailsJTable.getModel()).removeRow(selectedRow - 1);
        ((DefaultTableModel) sampleDetailsJTable.getModel()).insertRow(selectedRow, tempRow);

        sampleDetailsJTable.changeSelection(selectedRow - 1, selectedColumn, false, false);

        fixIndices();

        valuesChanged = true;

        //move in data structure as well
        Object tempSubSampleName = prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().get(
                selectedRow - 1);
        prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().set(
                selectedRow - 1,
                prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().get(selectedRow));
        prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().set(selectedRow,
                tempSubSampleName);

        Object[] sampleDetails = prideConverter.getProperties().getSampleDescriptionCVParams().toArray();

        prideConverter.getProperties().setSampleDescriptionCVParams(new ArrayList());

        String temp = ((CvParamImpl) sampleDetails[sampleDetails.length - 1]).getValue();

        int highestSubSampleNumber = new Integer(temp.substring(
                temp.lastIndexOf("_") + 1)).intValue();

        CvParamImpl tempCvParamImpl;

        for (int i = 0; i < sampleDetails.length; i++) {
            if (((CvParamImpl) sampleDetails[i]).getValue().endsWith("_" +
                    (selectedRow + 1))) {

                tempCvParamImpl = (CvParamImpl) sampleDetails[i];

                tempCvParamImpl = new CvParamImpl(tempCvParamImpl.getAccession(),
                        tempCvParamImpl.getCVLookup(),
                        tempCvParamImpl.getName(),
                        new Long(i),
                        "SUBSAMPLE_" + (selectedRow));

                sampleDetails[i] = tempCvParamImpl;

            } else if (((CvParamImpl) sampleDetails[i]).getValue().endsWith("_" +
                    (selectedRow))) {

                tempCvParamImpl = (CvParamImpl) sampleDetails[i];

                tempCvParamImpl = new CvParamImpl(tempCvParamImpl.getAccession(),
                        tempCvParamImpl.getCVLookup(),
                        tempCvParamImpl.getName(),
                        new Long(i),
                        "SUBSAMPLE_" + (selectedRow + 1));

                sampleDetails[i] = tempCvParamImpl;
            }
        }

        Vector tempSampleCVParams = new Vector();

        for (int i = 1; i <= highestSubSampleNumber; i++) {

            for (int j = 0; j < sampleDetails.length; j++) {

                if (((CvParamImpl) sampleDetails[j]).getValue().endsWith("_" + i)) {
                    tempSampleCVParams.add(sampleDetails[j]);
                }
            }
        }

        String tempTableRow;
        Vector subSampleNumbers = new Vector();

        for (int i = 0; i < sampleDetailsJTable.getRowCount(); i++) {
            tempTableRow = (String) sampleDetailsJTable.getValueAt(i, 1);

            for (int j = 0; j < tempTableRow.length(); j++) {
                if (tempTableRow.charAt(j) == ']') {
                    subSampleNumbers.add("SUBSAMPLE_" + (i + 1));
                }
            }
        }

        for (int i = 0; i < tempSampleCVParams.size(); i++) {

            tempCvParamImpl = (CvParamImpl) tempSampleCVParams.get(i);

            tempCvParamImpl = new CvParamImpl(tempCvParamImpl.getAccession(),
                    tempCvParamImpl.getCVLookup(),
                    tempCvParamImpl.getName(),
                    new Long(i),
                    (String) subSampleNumbers.get(i));

            prideConverter.getProperties().getSampleDescriptionCVParams().add(tempCvParamImpl);
        }
}//GEN-LAST:event_moveUpJMenuItemActionPerformed

    /**
     * Moves the selected sample down one position in the table.
     * 
     * @param evt
     */
    private void moveDownJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownJMenuItemActionPerformed
        int selectedRow = sampleDetailsJTable.getSelectedRow();
        int selectedColumn = sampleDetailsJTable.getSelectedColumn();

        Object[] tempRow = new Object[]{
            sampleDetailsJTable.getValueAt(selectedRow + 1, 0),
            sampleDetailsJTable.getValueAt(selectedRow + 1, 1),
            sampleDetailsJTable.getValueAt(selectedRow + 1, 2)
        };

        ((DefaultTableModel) sampleDetailsJTable.getModel()).removeRow(selectedRow + 1);
        ((DefaultTableModel) sampleDetailsJTable.getModel()).insertRow(selectedRow, tempRow);

        sampleDetailsJTable.changeSelection(selectedRow + 1, selectedColumn, false, false);

        fixIndices();

        valuesChanged = true;

        //move in data structure as well
        Object tempSubSampleName = prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().get(
                selectedRow + 1);
        prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().set(
                selectedRow + 1,
                prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().get(selectedRow));
        prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().set(selectedRow,
                tempSubSampleName);

        Object[] sampleDetails = prideConverter.getProperties().getSampleDescriptionCVParams().toArray();

        prideConverter.getProperties().setSampleDescriptionCVParams(new ArrayList());

        String temp = ((CvParamImpl) sampleDetails[sampleDetails.length - 1]).getValue();

        int highestSubSampleNumber = new Integer(temp.substring(
                temp.lastIndexOf("_") + 1)).intValue();

        CvParamImpl tempCvParamImpl;

        for (int i = 0; i < sampleDetails.length; i++) {
            if (((CvParamImpl) sampleDetails[i]).getValue().endsWith("_" +
                    (selectedRow + 1))) {

                tempCvParamImpl = (CvParamImpl) sampleDetails[i];

                tempCvParamImpl = new CvParamImpl(tempCvParamImpl.getAccession(),
                        tempCvParamImpl.getCVLookup(),
                        tempCvParamImpl.getName(),
                        new Long(i),
                        "SUBSAMPLE_" + (selectedRow + 2));

                sampleDetails[i] = tempCvParamImpl;

            } else if (((CvParamImpl) sampleDetails[i]).getValue().endsWith("_" +
                    (selectedRow + 2))) {

                tempCvParamImpl = (CvParamImpl) sampleDetails[i];

                tempCvParamImpl = new CvParamImpl(tempCvParamImpl.getAccession(),
                        tempCvParamImpl.getCVLookup(),
                        tempCvParamImpl.getName(),
                        new Long(i),
                        "SUBSAMPLE_" + (selectedRow + 1));

                sampleDetails[i] = tempCvParamImpl;
            }
        }

        Vector tempSampleCVParams = new Vector();

        for (int i = 1; i <= highestSubSampleNumber; i++) {

            for (int j = 0; j < sampleDetails.length; j++) {

                if (((CvParamImpl) sampleDetails[j]).getValue().endsWith("_" + i)) {
                    tempSampleCVParams.add(sampleDetails[j]);
                }
            }
        }

        String tempTableRow;
        Vector subSampleNumbers = new Vector();

        for (int i = 0; i < sampleDetailsJTable.getRowCount(); i++) {
            tempTableRow = (String) sampleDetailsJTable.getValueAt(i, 1);

            for (int j = 0; j < tempTableRow.length(); j++) {
                if (tempTableRow.charAt(j) == ']') {
                    subSampleNumbers.add("SUBSAMPLE_" + (i + 1));
                }
            }
        }

        for (int i = 0; i < tempSampleCVParams.size(); i++) {

            tempCvParamImpl = (CvParamImpl) tempSampleCVParams.get(i);

            tempCvParamImpl = new CvParamImpl(tempCvParamImpl.getAccession(),
                    tempCvParamImpl.getCVLookup(),
                    tempCvParamImpl.getName(),
                    new Long(i),
                    (String) subSampleNumbers.get(i));

            prideConverter.getProperties().getSampleDescriptionCVParams().add(tempCvParamImpl);
        }
}//GEN-LAST:event_moveDownJMenuItemActionPerformed

    /**
     * See cancelJButtonActionPerformed
     * 
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelJButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    /**
     * Opens a help frame.
     * 
     * @param evt
     */
    private void helpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpWindow(this, getClass().getResource("/no/uib/prideconverter/helpfiles/SampleDetails.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    /**
     * Right clicking in the combo box opens a popup menu where the selected 
     * sample set can be deleted.
     * 
     * @param evt
     */
    private void namesJComboBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_namesJComboBoxMouseClicked
        if (evt.getButton() == 3) {

            if (namesJComboBox.getSelectedIndex() != 0 &&
                    namesJComboBox.getSelectedIndex() !=
                    namesJComboBox.getItemCount() - 1) {

                popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }//GEN-LAST:event_namesJComboBoxMouseClicked

    /**
     * Sets the current sample set to the set selected in the combo box.
     * 
     * @param evt
     */
    private void namesJComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_namesJComboBoxItemStateChanged
        prideConverter.getUserProperties().setCurrentSampleSet((String) namesJComboBox.getSelectedItem());
        mandatoryFieldsCheck();
    }//GEN-LAST:event_namesJComboBoxItemStateChanged

    /**
     * If the information as been altered, the option to store the data will 
     * be given. Then the information about the selected sample set will be 
     * retrieved.
     * 
     * @param evt
     */
    private void namesJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_namesJComboBoxActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        boolean cancel = false;

        if (valuesChanged) {

            int value = JOptionPane.showConfirmDialog(this,
                    "The sample set has been changed. Do you want to save this for later use?",
                    "Sample Set Changed", JOptionPane.YES_NO_CANCEL_OPTION);

            if (value == JOptionPane.YES_OPTION) {

                value = JOptionPane.showConfirmDialog(this,
                        "Overwrite existing sample set?",
                        "Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);

                if (value == JOptionPane.YES_OPTION) {

                    try {

                        String newName;
                        newName = samplePath + lastSelectedSampleName + ".sam";

                        FileWriter r = new FileWriter(newName);
                        BufferedWriter bw = new BufferedWriter(r);

                        bw.write("Name: " + lastSelectedSampleName + "\n");
                        Iterator iterator = prideConverter.getProperties().getSampleDescriptionCVParams().iterator();

                        CvParamImpl temp;

                        bw.write(prideConverter.getProperties().getSampleDescriptionCVParams().size() +
                                "\n");

                        for (int i = 0; i <
                                prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().size(); i++) {
                            bw.write(prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().get(i) +
                                    "\n");
                        }

                        bw.write("#\n");

                        while (iterator.hasNext()) {
                            temp = ((CvParamImpl) iterator.next());

                            bw.write("Accession: " + temp.getAccession() + "\n");
                            bw.write("CVLookup: " + temp.getCVLookup() + "\n");
                            bw.write("Name: " + temp.getName() + "\n");
                            bw.write("Value: " + temp.getValue() + "\n\n");
                        }

                        valuesChanged = false;

                        bw.close();
                        r.close();

                        prideConverter.getProperties().setSampleDescriptionCVParamsQuantification(new ArrayList());
                        prideConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());

                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (value == JOptionPane.CANCEL_OPTION) {
                    cancel = true;

                    namesJComboBox.setSelectedItem(lastSelectedSampleName);

                } else { //value == NO
                    try {
                        String newSampleName = JOptionPane.showInputDialog(this,
                                "Please provide the name of the new sample set: ",
                                "Sample Set Name", JOptionPane.PLAIN_MESSAGE);

                        if (newSampleName != null) {

                            String newName;
                            newName = samplePath +
                                    newSampleName +
                                    ".sam";

                            while (new File(newName).exists()) {
                                newSampleName = JOptionPane.showInputDialog(this,
                                        "This name is already in use. Please provide a new name: ",
                                        "Sample Set Name", JOptionPane.PLAIN_MESSAGE);

                                newName = samplePath +
                                        newSampleName +
                                        ".sam";
                            }

                            if (newSampleName != null) {

                                prideConverter.getProperties().setSampleDescriptionCVParamsQuantification(new ArrayList());
                                prideConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());

                                FileWriter r = new FileWriter(newName);
                                BufferedWriter bw = new BufferedWriter(r);

                                bw.write("Name: " + newSampleName +
                                        "\n");

                                Iterator iterator = prideConverter.getProperties().getSampleDescriptionCVParams().iterator();

                                CvParamImpl temp;

                                bw.write(prideConverter.getProperties().getSampleDescriptionCVParams().size() + "\n");

                                for (int i = 0; i <
                                        prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().size(); i++) {
                                    bw.write(prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().get(i) +
                                            "\n");
                                }

                                bw.write("#\n");

                                while (iterator.hasNext()) {
                                    temp = ((CvParamImpl) iterator.next());

                                    bw.write("Accession: " + temp.getAccession() +
                                            "\n");
                                    bw.write("CVLookup: " + temp.getCVLookup() +
                                            "\n");
                                    bw.write("Name: " + temp.getName() + "\n");
                                    bw.write("Value: " + temp.getValue() +
                                            "\n\n");
                                }

                                bw.close();
                                r.close();

                                valuesChanged = false;

                                namesJComboBox.insertItemAt(newSampleName, namesJComboBox.getItemCount() -
                                        2);
                            } else {
                                cancel = true;
                                namesJComboBox.setSelectedItem(lastSelectedSampleName);
                                prideConverter.getUserProperties().setCurrentSampleSet(lastSelectedSampleName);
                            }
                        } else {
                            cancel = true;
                            namesJComboBox.setSelectedItem(lastSelectedSampleName);
                            prideConverter.getUserProperties().setCurrentSampleSet(lastSelectedSampleName);
                        }
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } else if (value == JOptionPane.CANCEL_OPTION) {
                cancel = true;
                namesJComboBox.setSelectedItem(lastSelectedSampleName);
                prideConverter.getUserProperties().setCurrentSampleSet(lastSelectedSampleName);
            }
        }
        if (!cancel) {

            lastSelectedSampleName = (String) namesJComboBox.getSelectedItem();
            String selectedSampleName = (String) namesJComboBox.getSelectedItem();

            prideConverter.getUserProperties().setCurrentSampleSet(selectedSampleName);

            //empty the table
            while (sampleDetailsJTable.getRowCount() > 0) {
                ((DefaultTableModel) sampleDetailsJTable.getModel()).removeRow(0);
            }

            prideConverter.getProperties().setSampleDescriptionCVParams(new ArrayList());
            prideConverter.getProperties().setSampleDescriptionUserSubSampleNames(new ArrayList());

            if (namesJComboBox.getSelectedIndex() == 0) {
                sampleDetailsJButton.setEnabled(false);
                valuesChanged = false;
                prideConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());
            } else if (namesJComboBox.getSelectedIndex() ==
                    namesJComboBox.getItemCount() - 1) {

                sampleDetailsJButton.setEnabled(false);
                valuesChanged = false;
                prideConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());

                ComboBoxInputDialog input = new ComboBoxInputDialog(this, this, true);
                input.setTitle("Create New Sample Set");
                input.setBorderTitle("New Sample Set");
                input.setVisible(true);
            } else {

                if (!keepQuantificationSelection) {
                    prideConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());
                }

                sampleDetailsJButton.setEnabled(true);

                selectedSampleName = samplePath + selectedSampleName + ".sam";

                try {
                    String temp;

                    FileReader f = new FileReader(selectedSampleName);
                    BufferedReader b = new BufferedReader(f);

                    b.readLine();

                    int numberOfCVTerms;

                    Vector names, accessions, ontologies;
                    String subSampleName;

                    numberOfCVTerms = new Integer(b.readLine()).intValue();

                    subSampleName = b.readLine();

                    if (subSampleName != null) {
                        while (!subSampleName.equalsIgnoreCase("#")) {
                            prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().add(subSampleName);
                            subSampleName = b.readLine();
                        }
                    }

                    names = new Vector();
                    accessions = new Vector();
                    ontologies = new Vector();
                    String name, accession, ontology;

                    String subSampleNumber = "SUBSAMPLE_1";
                    String currentSubSampleNumber = "";

                    for (int i = 0; i < numberOfCVTerms; i++) {

                        temp = b.readLine();
                        accession = temp.substring(temp.indexOf(": ") + 2);
                        temp = b.readLine();
                        ontology = temp.substring(temp.indexOf(": ") + 2);
                        temp = b.readLine();
                        name = temp.substring(temp.indexOf(": ") + 2);
                        temp = b.readLine();
                        currentSubSampleNumber = temp.substring(temp.indexOf(": ") + 2);
                        b.readLine();

                        if (!currentSubSampleNumber.equalsIgnoreCase(subSampleNumber)) {
                            addSampleDetails(null, names, accessions, ontologies, -1);

                            names = new Vector();
                            accessions = new Vector();
                            ontologies = new Vector();

                            names.add(name);
                            accessions.add(accession);
                            ontologies.add(ontology);

                            subSampleNumber = currentSubSampleNumber;
                        } else {
                            names.add(name);
                            accessions.add(accession);
                            ontologies.add(ontology);
                        }
                    }

                    if (names.size() > 0) {
                        addSampleDetails(null, names, accessions, ontologies, -1);
                    }

                    if (prideConverter.getProperties().getCurrentQuantificationSelection().size() > 0) {
                        for (int i = 0; i <
                                prideConverter.getProperties().getCurrentQuantificationSelection().size(); i++) {
                            sampleDetailsJTable.setValueAt(prideConverter.getProperties().getCurrentQuantificationSelection().get(i), i, 2);
                        }
                    }

                    quantificationComboBoxActionPerformed(null);

                    b.close();
                    f.close();

                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                valuesChanged = false;
            }

            mandatoryFieldsCheck();
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_namesJComboBoxActionPerformed

    /**
     * Delete the selected sample set.
     * 
     * @param evt
     */
    private void deleteJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteJMenuItemActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        int option = JOptionPane.showConfirmDialog(this, "This will delete the selected sample set up. Continue?");

        if (option == JOptionPane.YES_OPTION) {

            String newName = samplePath +
                    (String) namesJComboBox.getSelectedItem() + ".sam";

            boolean deleted = new File(newName).delete();

            prideConverter.getProperties().setSampleDescriptionCVParamsQuantification(new ArrayList());

            if (!deleted) {
                JOptionPane.showMessageDialog(this, "The file could not be deleted!");
                System.out.println("newName: " + newName);
            } else {
                lastSelectedSampleName = null;
                valuesChanged = false;
                readSamplesFromFile();
            }
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_deleteJMenuItemActionPerformed

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
     * Deletes the selected sample set.
     * 
     * @param evt
     */
    private void deleteJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteJButtonActionPerformed
        if (namesJComboBox.getSelectedIndex() != 0 &&
                namesJComboBox.getSelectedIndex() !=
                namesJComboBox.getItemCount() - 1 &&
                namesJComboBox.getSelectedIndex() != -1) {

            deleteJMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_deleteJButtonActionPerformed

    /**
     * If the delete key is pressed the selected row in the table are deleted.
     * 
     * @param evt
     */
    private void sampleDetailsJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sampleDetailsJTableKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            deleteSelectedRowJMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_sampleDetailsJTableKeyReleased

    /**
     * Add a sample set to the table.
     * 
     * @param sampleName 
     * @param names
     * @param accessions
     * @param ontologies
     * @param modifiedRow the row to modify, use -1 if adding a new row
     */
    public void addSampleDetails(String sampleName, Vector names, Vector accessions, Vector ontologies, int modifiedRow) {

        valuesChanged = true;

        if (modifiedRow == -1) {

            String temp = "";

            for (int i = 0; i <
                    names.size(); i++) {
                prideConverter.getProperties().getSampleDescriptionCVParams().add(new CvParamImpl(
                        (String) accessions.get(i),
                        (String) ontologies.get(i),
                        (String) names.get(i),
                        new Long(sampleDetailsJTable.getRowCount() - 1),
                        ("SUBSAMPLE_" + (sampleDetailsJTable.getRowCount() + 1))));

                temp += "[" + (String) names.get(i) + "]";
            }

            ((DefaultTableModel) sampleDetailsJTable.getModel()).addRow(
                    new Object[]{new Integer(sampleDetailsJTable.getRowCount() + 1), temp
            });

            if (sampleName != null) {
                prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().add(sampleName);
            }
        } else {

            String temp = "";

            Object[] cvParams = prideConverter.getProperties().getSampleDescriptionCVParams().toArray();
            CvParamImpl cvParam;

            prideConverter.getProperties().setSampleDescriptionCVParams(new ArrayList());

            boolean valuesAdded = false;

            for (int i = 0; i < cvParams.length; i++) {

                cvParam = (CvParamImpl) cvParams[i];

                if (cvParam.getValue().equalsIgnoreCase("SUBSAMPLE_" +
                        (modifiedRow + 1))) {
                    if (!valuesAdded) {
                        for (int j = 0; j < names.size(); j++) {
                            prideConverter.getProperties().getSampleDescriptionCVParams().add(new CvParamImpl(
                                    (String) accessions.get(j),
                                    (String) ontologies.get(j),
                                    (String) names.get(j),
                                    new Long(sampleDetailsJTable.getRowCount() -
                                    1),
                                    ("SUBSAMPLE_" +
                                    (modifiedRow + 1))));

                            temp += "[" + (String) names.get(j) + "]";
                        }

                        valuesAdded = true;
                    }

                } else {
                    prideConverter.getProperties().getSampleDescriptionCVParams().add(cvParam);
                }
            }

            sampleDetailsJTable.setValueAt(temp, modifiedRow, 1);

            prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().set(modifiedRow, sampleName);
        }

        mandatoryFieldsCheck();
    }

    /**
     * Fixes the indices so that they are in accending order starting from one
     */
    private void fixIndices() {
        for (int row = 0; row <
                ((DefaultTableModel) sampleDetailsJTable.getModel()).getRowCount(); row++) {
            ((DefaultTableModel) sampleDetailsJTable.getModel()).setValueAt(new Integer(row +
                    1), row, 0);
        }
    }

    /**
     * Saves the inserted information to the Properties object.
     * 
     * @return true if save was succesfull, false otherwise.
     */
    private boolean saveInsertedInformation() {
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        prideConverter.getUserProperties().setCurrentSampleSet((String) namesJComboBox.getSelectedItem());
        prideConverter.getProperties().setSampleDescriptionCVParamsQuantification(new ArrayList());
        prideConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());

        int counter = prideConverter.getProperties().getSampleDescriptionCVParams().size();

        for (int i = 0; i < sampleDetailsJTable.getRowCount(); i++) {

            if (sampleDetailsJTable.getValueAt(i, 2) != null) {

                if (((String) sampleDetailsJTable.getValueAt(i, 2)).equalsIgnoreCase("iTRAQ reagent 114")) {
                    prideConverter.getProperties().getSampleDescriptionCVParamsQuantification().add(
                            new CvParamImpl("PRIDE:0000114", "PRIDE", "iTRAQ reagent 114", counter++, "SUBSAMPLE_" +
                            (i + 1)));
                } else if (((String) sampleDetailsJTable.getValueAt(i, 2)).equalsIgnoreCase("iTRAQ reagent 115")) {
                    prideConverter.getProperties().getSampleDescriptionCVParamsQuantification().add(
                            new CvParamImpl("PRIDE:0000115", "PRIDE", "iTRAQ reagent 115", counter++, "SUBSAMPLE_" +
                            (i + 1)));
                } else if (((String) sampleDetailsJTable.getValueAt(i, 2)).equalsIgnoreCase("iTRAQ reagent 116")) {
                    prideConverter.getProperties().getSampleDescriptionCVParamsQuantification().add(
                            new CvParamImpl("PRIDE:0000116", "PRIDE", "iTRAQ reagent 116", counter++, "SUBSAMPLE_" +
                            (i + 1)));
                } else if (((String) sampleDetailsJTable.getValueAt(i, 2)).equalsIgnoreCase("iTRAQ reagent 117")) {
                    prideConverter.getProperties().getSampleDescriptionCVParamsQuantification().add(
                            new CvParamImpl("PRIDE:0000117", "PRIDE", "iTRAQ reagent 117", counter++, "SUBSAMPLE_" +
                            (i + 1)));
                }
            }

            prideConverter.getProperties().getCurrentQuantificationSelection().add((String) sampleDetailsJTable.getValueAt(i, 2));
        }

        boolean cancel = false;

        if (valuesChanged) {
            int value = JOptionPane.showConfirmDialog(this,
                    "The sample set has been changed. Do you want to save this for later use?",
                    "Sample Set Changed", JOptionPane.YES_NO_CANCEL_OPTION);

            if (value == JOptionPane.YES_OPTION) {

                value = JOptionPane.showConfirmDialog(this,
                        "Overwrite existing sample set?",
                        "Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);

                if (value == JOptionPane.YES_OPTION) {

                    try {
                        String newName;
                        newName = samplePath +
                                prideConverter.getUserProperties().getCurrentSampleSet() +
                                ".sam";

                        FileWriter r = new FileWriter(newName);
                        BufferedWriter bw = new BufferedWriter(r);

                        bw.write("Name: " + prideConverter.getUserProperties().getCurrentSampleSet() + "\n");

                        Iterator iterator = prideConverter.getProperties().getSampleDescriptionCVParams().iterator();

                        CvParamImpl temp;

                        bw.write(prideConverter.getProperties().getSampleDescriptionCVParams().size() + "\n");

                        for (int i = 0; i <
                                prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().size(); i++) {
                            bw.write(prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().get(i) +
                                    "\n");
                        }

                        bw.write("#\n");

                        while (iterator.hasNext()) {
                            temp = ((CvParamImpl) iterator.next());

                            bw.write("Accession: " + temp.getAccession() + "\n");
                            bw.write("CVLookup: " + temp.getCVLookup() + "\n");
                            bw.write("Name: " + temp.getName() + "\n");
                            bw.write("Value: " + temp.getValue() + "\n\n");
                        }

                        bw.close();
                        r.close();

                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (value == JOptionPane.CANCEL_OPTION) {
                    cancel = true;
                } else { //value == NO
                    try {
                        String newSampleName = JOptionPane.showInputDialog(this,
                                "Please provide the name of the new sample set: ",
                                "Sample Set Name", JOptionPane.PLAIN_MESSAGE);

                        if (newSampleName != null) {

                            String newName;

                            newName = samplePath +
                                    newSampleName +
                                    ".sam";

                            while (new File(newName).exists()) {
                                newSampleName = JOptionPane.showInputDialog(this,
                                        "This name is already in use. Please provide a new name: ",
                                        "Sample Set Name", JOptionPane.PLAIN_MESSAGE);

                                newName = samplePath +
                                        newSampleName +
                                        ".sam";
                            }

                            if (newSampleName != null) {

                                prideConverter.getUserProperties().setCurrentSampleSet(newSampleName);

                                FileWriter r = new FileWriter(newName);
                                BufferedWriter bw = new BufferedWriter(r);

                                bw.write("Name: " + newSampleName + "\n");

                                Iterator iterator = prideConverter.getProperties().getSampleDescriptionCVParams().iterator();

                                CvParamImpl temp;

                                bw.write(prideConverter.getProperties().getSampleDescriptionCVParams().size() + "\n");

                                for (int i = 0; i <
                                        prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().size(); i++) {
                                    bw.write(prideConverter.getProperties().getSampleDescriptionUserSubSampleNames().get(i) +
                                            "\n");
                                }

                                bw.write("#\n");

                                while (iterator.hasNext()) {
                                    temp = ((CvParamImpl) iterator.next());

                                    bw.write("Accession: " + temp.getAccession() + "\n");
                                    bw.write("CVLookup: " + temp.getCVLookup() + "\n");
                                    bw.write("Name: " + temp.getName() + "\n");
                                    bw.write("Value: " + temp.getValue() + "\n\n");
                                }

                                bw.close();
                                r.close();
                            } else {
                                cancel = true;
                            }
                        } else {
                            cancel = true;
                        }
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } else if (value == JOptionPane.CANCEL_OPTION) {
                cancel = true;
            }
        }

        if (lowerRangeJTextField.isEnabled()) {
            try {
                prideConverter.getUserProperties().setPeakIntegrationRangeLower(
                        new Double(lowerRangeJTextField.getText()).doubleValue());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Peak Integreation Lower Range is not a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                cancel = true;
                lowerRangeJTextField.requestFocus();
            }

            try {
                prideConverter.getUserProperties().setPeakIntegrationRangeUpper(
                        new Double(upperRangeJTextField.getText()).doubleValue());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                        "Peak Integreation Upper Range is not a number.", 
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                cancel = true;
                upperRangeJTextField.requestFocus();
            }

            if (Math.abs(prideConverter.getUserProperties().getPeakIntegrationRangeLower()) +
                    prideConverter.getUserProperties().getPeakIntegrationRangeUpper() > 1) {
                JOptionPane.showMessageDialog(this, 
                        "The input peak integration range cross over between peaks (sum is > 1).", 
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                cancel = true;
                lowerRangeJTextField.requestFocus();
            }

            try {
                prideConverter.getUserProperties().setReporterIonIntensityThreshold(
                        new Double(intensityThresholdJTextField.getText()).doubleValue());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                        "The Reporter Ion Intensity Threshold is not a number.", 
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                cancel = true;
                intensityThresholdJTextField.requestFocus();
            }

            double[] purrityCorrections = new double[16];
            int purrityCorrectionsCounter = 0;

            try {
                StringTokenizer tok = new StringTokenizer(purityCorrectionsJTextField.getText(), ",");

                while (tok.hasMoreTokens()) {
                    purrityCorrections[purrityCorrectionsCounter] = new Double(tok.nextToken()).doubleValue();
                    purrityCorrectionsCounter++;
                }

                if (purrityCorrectionsCounter != 16) {
                    JOptionPane.showMessageDialog(this, 
                            "The number of Purity Correction values has to be 16.", 
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                    cancel = true;
                    purityCorrectionsJTextField.requestFocus();
                }

                prideConverter.getUserProperties().setPurityCorrections(purrityCorrections);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                        "One of the Purity Correction values is not a number.", 
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                cancel = true;
                purityCorrectionsJTextField.requestFocus();
            } catch (ArrayIndexOutOfBoundsException e) {
                JOptionPane.showMessageDialog(this, 
                        "The number of Purity Correction values has to be 16.", 
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                cancel = true;
                purityCorrectionsJTextField.requestFocus();
            }
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        return !cancel;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JButton backJButton;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JButton deleteJButton;
    private javax.swing.JMenuItem deleteJMenuItem;
    private javax.swing.JMenuItem deleteSelectedRowJMenuItem;
    private javax.swing.JMenuItem editJMenuItem;
    private javax.swing.JButton helpJButton;
    private javax.swing.JLabel intensityThresholdJLabel;
    private javax.swing.JTextField intensityThresholdJTextField;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel lowerRangeJLabel;
    private javax.swing.JTextField lowerRangeJTextField;
    private javax.swing.JMenuItem moveDownJMenuItem;
    private javax.swing.JMenuItem moveUpJMenuItem;
    private javax.swing.JComboBox namesJComboBox;
    private javax.swing.JButton nextJButton;
    private javax.swing.JLabel peakRangeJLabel;
    private javax.swing.JPopupMenu popupJMenu;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JLabel purityCorrectionsJLabel;
    private javax.swing.JTextField purityCorrectionsJTextField;
    private javax.swing.JButton sampleDetailsJButton;
    private javax.swing.JTable sampleDetailsJTable;
    private javax.swing.JLabel upperRangeJLabel;
    private javax.swing.JTextField upperRangeJTextField;
    // End of variables declaration//GEN-END:variables
    /**
     * See ComboBoxInputable
     */
    public void insertIntoComboBox(String text) {

        prideConverter.getUserProperties().setCurrentSampleSet(text);

        String newName;
        newName = samplePath + text + ".sam";

        try {
            FileWriter r = new FileWriter(newName);
            BufferedWriter bw = new BufferedWriter(r);

            bw.write("Name: " + text + "\n");
            bw.write("0");
            bw.close();
            r.close();

            readSamplesFromFile();

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * See ComboBoxInputable
     */
    public void resetComboBox() {
        lastSelectedSampleName = null;
        namesJComboBox.setSelectedIndex(0);
        readSamplesFromFile();
    }

    /**
     * See ComboBoxInputable
     */
    public boolean alreadyInserted(String currentSampleName) {

        String newName;
        newName = samplePath + currentSampleName + ".sam";
        File newFile = new File(newName);

        return newFile.exists();
    }
}
