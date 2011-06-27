package no.uib.prideconverter.gui;

import no.uib.olsdialog.OLSDialog;
import no.uib.olsdialog.OLSInputable;
import no.uib.prideconverter.PRIDEConverter;
import no.uib.prideconverter.util.*;
import uk.ac.ebi.pride.model.implementation.mzData.CvParamImpl;
import uk.ac.ebi.pride.model.interfaces.mzdata.CvParam;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * A frame where information about the samples can be inserted.
 *
 * @author  Harald Barsnes
 * 
 * Created March 2008
 */
public class SampleDetails extends javax.swing.JFrame implements ComboBoxInputable, OLSInputable {

    private String lastSelectedSampleName;
    private String samplePath;
    private boolean valuesChanged = false;
    private Vector columnToolTips;
    private boolean keepQuantificationSelection = false;
    private Vector tempSampleCvParameters;
    private Map<String, List<String>> preselectedOntologyTerms;

    private boolean speciesCheckDone = false;

    /**
     * Opens a new SampleDetails frame.
     * 
     * @param location where to locate the frame on the screen
     */
    public SampleDetails(Point location) {
        CvMappingProperties prop = new CvMappingProperties();
        CvMappingReader cvMappingReader = new CvMappingReader(prop.getDefaultMappingFile());
        preselectedOntologyTerms = cvMappingReader.getPreselectedTerms(prop.getPropertyValue("sampleKeyword"));


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

        TableColumn quantificationColumn = multipleSamplesDetailsJTable.getColumnModel().getColumn(2);

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

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText("Click to choose quantification method");
        quantificationColumn.setCellRenderer(renderer);

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        singleSampleDetailsJTable.getTableHeader().setReorderingAllowed(false);
        singleSampleDetailsJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        multipleSamplesDetailsJTable.getTableHeader().setReorderingAllowed(false);
        multipleSamplesDetailsJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        ((DefaultTableModel) singleSampleDetailsJTable.getModel()).addTableModelListener(
                new TableModelListener() {

                    public void tableChanged(TableModelEvent e) {
                        valuesChanged = true;
                    }
                });

        setTitle(PRIDEConverter.getWizardName() + " " +
                PRIDEConverter.getPrideConverterVersionNumber() + " - " + getTitle());

        singleSampleDetailsJTable.getColumn(" ").setMaxWidth(40);
        singleSampleDetailsJTable.getColumn(" ").setMinWidth(40);
        singleSampleDetailsJTable.setRowHeight(singleSampleDetailsJTable.getFont().getSize() + PRIDEConverter.getProperties().tableRowHeightPaddingSize);

        multipleSamplesDetailsJTable.getColumn(" ").setMaxWidth(40);
        multipleSamplesDetailsJTable.getColumn(" ").setMinWidth(40);
        multipleSamplesDetailsJTable.getColumn("Quantification").setMaxWidth(110);
        multipleSamplesDetailsJTable.getColumn("Quantification").setMinWidth(110);
        multipleSamplesDetailsJTable.setRowHeight(multipleSamplesDetailsJTable.getFont().getSize() + PRIDEConverter.getProperties().tableRowHeightPaddingSize);

        columnToolTips = new Vector();
        columnToolTips.add(null);
        columnToolTips.add(null);
        columnToolTips.add("Quantification method used");

        if (PRIDEConverter.getProperties().getCurrentQuantificationSelection() == null) {
            PRIDEConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());
        }

        lowerRangeJTextField.setText("" +
                PRIDEConverter.getUserProperties().getPeakIntegrationRangeLower());
        upperRangeJTextField.setText("" +
                PRIDEConverter.getUserProperties().getPeakIntegrationRangeUpper());
        intensityThresholdJTextField.setText("" +
                PRIDEConverter.getUserProperties().getReporterIonIntensityThreshold());

        String temp = "";

        for (int i = 0; i < PRIDEConverter.getUserProperties().getPurityCorrections().length - 1; i++) {
            temp += PRIDEConverter.getUserProperties().getPurityCorrections()[i] + ",";
        }

        temp += PRIDEConverter.getUserProperties().getPurityCorrections()[PRIDEConverter.getUserProperties().getPurityCorrections().length - 1];

        purityCorrectionsJTextField.setText(temp);

        samplePath = "" + this.getClass().getProtectionDomain().getCodeSource().getLocation();
        samplePath = samplePath.substring(5, samplePath.lastIndexOf("/"));
        samplePath = samplePath.substring(0, samplePath.lastIndexOf("/") + 1) +
                "Properties/Samples/";
        samplePath = samplePath.replace("%20", " ");

        setLocation(location);
        setVisible(true);

        // if the file is an mzData file the sample details are extracted
        if (PRIDEConverter.getProperties().getDataSource().equalsIgnoreCase("mzData") &&
                !PRIDEConverter.getProperties().areSampleDetailsExtracted()) {
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
        Iterator<CvParam> iterator;

        Collection<CvParam> cvParams = PRIDEConverter.getProperties().getMzDataFile().getSampleDescriptionCvParams();

        PRIDEConverter.getProperties().setSampleDescriptionCVParams(new ArrayList());
        tempSampleCvParameters = new Vector();

        if (cvParams != null) {

            iterator = cvParams.iterator();

            long orderIndex = 0;

            while (iterator.hasNext()) {

                CvParam tempCvParam = iterator.next();

                tempSampleCvParameters.add(
                        new CvParamImpl(tempCvParam.getAccession(),
                        tempCvParam.getCVLookup(),
                        tempCvParam.getName(),
                        new Long(orderIndex++),
                        tempCvParam.getValue()));
            }
        }

        sampleName = PRIDEConverter.getProperties().getMzDataFile().getSampleName();
        PRIDEConverter.getProperties().setSampleDescriptionUserSubSampleNames(new ArrayList());
        PRIDEConverter.getUserProperties().setCurrentSampleSet(sampleName);

        String newName = samplePath + sampleName + ".sam";

        // check if the sample already exists
        if (new File(newName).exists()) {

            int option = JOptionPane.showConfirmDialog(this,
                    "The sample name \'" +
                    sampleName +
                    "\' is already in use.\nUse existing sample?",
                    "Use Existing Sample?",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.NO_OPTION) {

                option = JOptionPane.showConfirmDialog(this,
                        "Overwrite existing sample?",
                        "Overwrite Existing Sample?",
                        JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.NO_OPTION) {

                    sampleName = JOptionPane.showInputDialog(this, "Provide the name of the new sample:",
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

                    PRIDEConverter.getUserProperties().setCurrentSampleSet(sampleName);

                    try {
                        FileWriter r = new FileWriter(newName);
                        BufferedWriter bw = new BufferedWriter(r);

                        bw.write("Name: " + sampleName + "\n");

                        if (tempSampleCvParameters.size() > 0) {

                            bw.write(tempSampleCvParameters.size() + "\n");

                            bw.write(sampleName + "\n#\n");

                            for (int i = 0; i < tempSampleCvParameters.size(); i++) {
                                CvParamImpl tempCv = (CvParamImpl) tempSampleCvParameters.get(i);

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

                        PRIDEConverter.getProperties().setSampleDescriptionCVParamsQuantification(new ArrayList());
                        PRIDEConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());

                    } catch (FileNotFoundException ex) {
                        JOptionPane.showMessageDialog(
                                this, "The file " + newName + " could not be found.",
                                "File Not Found", JOptionPane.ERROR_MESSAGE);
                        Util.writeToErrorLog("Error when trying to save file: ");
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(
                                this, "An error occured when trying to save the file " + newName + ".",
                                "File Error", JOptionPane.ERROR_MESSAGE);
                        Util.writeToErrorLog("Error when trying to save file: ");
                        ex.printStackTrace();
                    }
                }
            }
        } else {
            try {
                FileWriter r = new FileWriter(newName);
                BufferedWriter bw = new BufferedWriter(r);

                bw.write("Name: " + sampleName + "\n");

                if (tempSampleCvParameters.size() > 0) {

                    bw.write(tempSampleCvParameters.size() + "\n");

                    bw.write(sampleName + "\n#\n");

                    for (int i = 0; i < tempSampleCvParameters.size(); i++) {
                        CvParamImpl tempCv = (CvParamImpl) tempSampleCvParameters.get(i);

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

                PRIDEConverter.getProperties().setSampleDescriptionCVParamsQuantification(new ArrayList());
                PRIDEConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());

            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(
                        this, "The file " + newName + " could not be found.",
                        "File Not Found", JOptionPane.ERROR_MESSAGE);
                Util.writeToErrorLog("Error when trying to save file: ");
                ex.printStackTrace();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        this, "An error occured when trying to save the file " + newName + ".",
                        "File Error", JOptionPane.ERROR_MESSAGE);
                Util.writeToErrorLog("Error when trying to save file: ");
                ex.printStackTrace();
            }
        }

        PRIDEConverter.getProperties().setSampleDetailsExtracted(true);
    }

    /**
     * Makes the quantification parameters enabled or disabled based on if a 
     * quantification lable is selected or not.
     * 
     * @param evt
     */
    private void quantificationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {

        boolean quantificationSelected = false;

        for (int i = 0; i < multipleSamplesDetailsJTable.getRowCount(); i++) {

            if (multipleSamplesDetailsJTable.getValueAt(i, 2) != null) {
                if (!((String) multipleSamplesDetailsJTable.getValueAt(i, 2)).equalsIgnoreCase("None")) {
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
        String tempSampleName = null;

        try {
            if (!file.exists()) {
                file.mkdir();
            }

            File[] sampleFiles = file.listFiles();
            FileReader f = null;
            BufferedReader b = null;
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

            namesJComboBox.setRenderer(new MyComboBoxRenderer(comboboxTooltips, SwingConstants.CENTER));

            sampleNames.insertElementAt("- Please Select a Sample Set -", 0);
            sampleNames.add("   Create a New Sample Set...");

            namesJComboBox.setModel(new DefaultComboBoxModel(sampleNames));
            namesJComboBox.setSelectedItem(PRIDEConverter.getUserProperties().getCurrentSampleSet());

            lastSelectedSampleName = PRIDEConverter.getUserProperties().getCurrentSampleSet();

            namesJComboBoxActionPerformed(null);

        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(
                    this, "The file " + tempSampleName + " could not be found.",
                    "File Not Found", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to read file: ");
            ex.printStackTrace();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this, "An error occured when trying to read the file " + tempSampleName + ".",
                    "File Error", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to read file: ");
            ex.printStackTrace();
        }
    }

    /**
     * Enables the Next button if a valid sample set is selected.
     */
    private void mandatoryFieldsCheck() {
        if (singleSampleDetailsJTable.getRowCount() > 0 || multipleSamplesDetailsJTable.getRowCount() > 0) {
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
        popupMultipleSamplesJMenu = new javax.swing.JPopupMenu();
        editMultipleSamplesJMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        moveUpMultipleSamplesJMenuItem = new javax.swing.JMenuItem();
        moveDownMultipleSamplesJMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        deleteSelectedRowMultipleSamplesJMenuItem = new javax.swing.JMenuItem();
        popupDeleteMultipleSamplesJMenu = new javax.swing.JPopupMenu();
        deleteMultipleSamplesJMenuItem = new javax.swing.JMenuItem();
        nextJButton = new javax.swing.JButton();
        backJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        namesJComboBox = new javax.swing.JComboBox();
        deleteJButton = new javax.swing.JButton();
        sampleJTabbedPane = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        singleSampleDetailsJTable = new JTable() {
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
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        multipleSamplesDetailsJTable = new JTable() {
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
        addSampleJButton = new javax.swing.JButton();
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
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
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

        deleteJMenuItem.setText("Delete Sample Set");
        deleteJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteJMenuItemActionPerformed(evt);
            }
        });
        popupMenu.add(deleteJMenuItem);

        editMultipleSamplesJMenuItem.setMnemonic('E');
        editMultipleSamplesJMenuItem.setText("Edit");
        editMultipleSamplesJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMultipleSamplesJMenuItemActionPerformed(evt);
            }
        });
        popupMultipleSamplesJMenu.add(editMultipleSamplesJMenuItem);
        popupMultipleSamplesJMenu.add(jSeparator5);

        moveUpMultipleSamplesJMenuItem.setMnemonic('U');
        moveUpMultipleSamplesJMenuItem.setText("Move Up");
        moveUpMultipleSamplesJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpMultipleSamplesJMenuItemActionPerformed(evt);
            }
        });
        popupMultipleSamplesJMenu.add(moveUpMultipleSamplesJMenuItem);

        moveDownMultipleSamplesJMenuItem.setMnemonic('D');
        moveDownMultipleSamplesJMenuItem.setText("Move Down");
        moveDownMultipleSamplesJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownMultipleSamplesJMenuItemActionPerformed(evt);
            }
        });
        popupMultipleSamplesJMenu.add(moveDownMultipleSamplesJMenuItem);
        popupMultipleSamplesJMenu.add(jSeparator6);

        deleteSelectedRowMultipleSamplesJMenuItem.setText("Delete");
        deleteSelectedRowMultipleSamplesJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSelectedRowMultipleSamplesJMenuItemActionPerformed(evt);
            }
        });
        popupMultipleSamplesJMenu.add(deleteSelectedRowMultipleSamplesJMenuItem);

        deleteMultipleSamplesJMenuItem.setText("Delete Sample Set");
        deleteMultipleSamplesJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMultipleSamplesJMenuItemActionPerformed(evt);
            }
        });
        popupDeleteMultipleSamplesJMenu.add(deleteMultipleSamplesJMenuItem);

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

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Samples Set"));

        jLabel32.setText("Name:");

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

        jLabel2.setFont(jLabel2.getFont().deriveFont((jLabel2.getFont().getStyle() | java.awt.Font.ITALIC), jLabel2.getFont().getSize()-2));
        jLabel2.setText("Preferred Ontologies: NEWT (species), BTO (tissue), CTO (cell type), GO and DO (disease state)");

        singleSampleDetailsJTable.setFont(singleSampleDetailsJTable.getFont());
        singleSampleDetailsJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "CV Terms", "Values"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class
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
        singleSampleDetailsJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                singleSampleDetailsJTableMouseClicked(evt);
            }
        });
        singleSampleDetailsJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                singleSampleDetailsJTableKeyReleased(evt);
            }
        });
        jScrollPane3.setViewportView(singleSampleDetailsJTable);

        sampleDetailsJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/ols_transparent.GIF"))); // NOI18N
        sampleDetailsJButton.setText("Add Sample CV Term");
        sampleDetailsJButton.setEnabled(false);
        sampleDetailsJButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        sampleDetailsJButton.setPreferredSize(new java.awt.Dimension(159, 23));
        sampleDetailsJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sampleDetailsJButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel2)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, sampleDetailsJButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sampleDetailsJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        sampleJTabbedPane.addTab("Single Sample", jPanel1);

        jLabel4.setFont(jLabel4.getFont().deriveFont((jLabel4.getFont().getStyle() | java.awt.Font.ITALIC), jLabel4.getFont().getSize()-2));
        jLabel4.setText("Preferred Ontologies: NEWT (species), BTO (tissue), CTO (cell type), GO and DO (disease state)");

        multipleSamplesDetailsJTable.setFont(multipleSamplesDetailsJTable.getFont());
        multipleSamplesDetailsJTable.setModel(new javax.swing.table.DefaultTableModel(
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
        multipleSamplesDetailsJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                multipleSamplesDetailsJTableMouseClicked(evt);
            }
        });
        multipleSamplesDetailsJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                multipleSamplesDetailsJTableKeyReleased(evt);
            }
        });
        jScrollPane4.setViewportView(multipleSamplesDetailsJTable);

        addSampleJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/ols_transparent.GIF"))); // NOI18N
        addSampleJButton.setText("Add Sample");
        addSampleJButton.setEnabled(false);
        addSampleJButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        addSampleJButton.setPreferredSize(new java.awt.Dimension(159, 23));
        addSampleJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSampleJButtonActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Quantification Parameters"));

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

        peakRangeJLabel.setFont(peakRangeJLabel.getFont().deriveFont((peakRangeJLabel.getFont().getStyle() | java.awt.Font.ITALIC), peakRangeJLabel.getFont().getSize()-2));
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

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(upperRangeJLabel)
                    .add(intensityThresholdJLabel)
                    .add(lowerRangeJLabel)
                    .add(purityCorrectionsJLabel))
                .add(14, 14, 14)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(intensityThresholdJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                            .add(upperRangeJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                            .add(lowerRangeJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE))
                        .add(18, 18, 18)
                        .add(peakRangeJLabel)
                        .add(90, 90, 90))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(purityCorrectionsJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lowerRangeJLabel)
                    .add(peakRangeJLabel)
                    .add(lowerRangeJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(upperRangeJLabel)
                    .add(upperRangeJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(intensityThresholdJLabel)
                    .add(intensityThresholdJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(purityCorrectionsJLabel)
                    .add(purityCorrectionsJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, addSampleJButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addSampleJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        sampleJTabbedPane.addTab("Multiple Samples", jPanel3);

        org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, sampleJTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 645, Short.MAX_VALUE)
                    .add(jPanel8Layout.createSequentialGroup()
                        .add(jLabel32)
                        .add(18, 18, 18)
                        .add(namesJComboBox, 0, 559, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deleteJButton)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(namesJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(deleteJButton))
                .add(18, 18, 18)
                .add(sampleJTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                .addContainerGap())
            .add(jPanel8Layout.createSequentialGroup()
                .add(3, 3, 3)
                .add(jLabel32)
                .add(421, 421, 421))
        );

        jPanel8Layout.linkSize(new java.awt.Component[] {deleteJButton, namesJComboBox}, org.jdesktop.layout.GroupLayout.VERTICAL);

        jLabel3.setFont(jLabel3.getFont().deriveFont((jLabel3.getFont().getStyle() | java.awt.Font.ITALIC), jLabel3.getFont().getSize()-2));
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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 358, Short.MAX_VALUE)
                        .add(backJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nextJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 681, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel3))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(7, 7, 7)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(backJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(nextJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
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
     * If the user double clicks on a row in the table the OLS 
     * dialog is shown where the information about the given sample can be 
     * altered. If the user right clicks a pop up menu is shown for editing, 
     * moving or delting the selected sample.
     * 
     * @param evt
     */
    private void singleSampleDetailsJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_singleSampleDetailsJTableMouseClicked
        if (evt.getButton() == 3) {

            int row = singleSampleDetailsJTable.rowAtPoint(evt.getPoint());
            int column = singleSampleDetailsJTable.columnAtPoint(evt.getPoint());

            singleSampleDetailsJTable.changeSelection(row, column, false, false);

            this.moveUpJMenuItem.setEnabled(true);
            this.moveDownJMenuItem.setEnabled(true);

            if (row == singleSampleDetailsJTable.getRowCount() - 1) {
                this.moveDownJMenuItem.setEnabled(false);
            }

            if (row == 0) {
                this.moveUpJMenuItem.setEnabled(false);
            }

            popupJMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        } else if (evt.getButton() == 1 && evt.getClickCount() == 2) {
            editJMenuItemActionPerformed(null);
        }
}//GEN-LAST:event_singleSampleDetailsJTableMouseClicked

    /**
     * Delete the selected sample.
     * 
     * @param evt
     */
    private void deleteSelectedRowJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSelectedRowJMenuItemActionPerformed

        int selectedRow = singleSampleDetailsJTable.getSelectedRow();

        if (selectedRow != -1) {

            //PRIDEConverter.getProperties().getSampleDescriptionUserSubSampleNames().remove(selectedRow);

            ((DefaultTableModel) singleSampleDetailsJTable.getModel()).removeRow(selectedRow);

            //remove from datastructure as well
            Object[] sampleCvParameters = tempSampleCvParameters.toArray();

            tempSampleCvParameters = new Vector();

            CvParamImpl tempCvParam;

            int counter = 0;

            for (int i = 0; i <
                    sampleCvParameters.length; i++) {

                if (i != selectedRow) {
                    tempCvParam = (CvParamImpl) sampleCvParameters[i];
                    tempCvParam =
                            new CvParamImpl(tempCvParam.getAccession(),
                            tempCvParam.getCVLookup(),
                            tempCvParam.getName(),
                            new Long(counter++),
                            tempCvParam.getValue());

                    tempSampleCvParameters.add(tempCvParam);
                }
            }

            valuesChanged = true;

            fixIndicesSingleSampleTable();
            mandatoryFieldsCheck();
        }
    }//GEN-LAST:event_deleteSelectedRowJMenuItemActionPerformed

    /**
     * Stores the sample set selected and opens the next frame (ProtocolDetails).
     * 
     * @param evt
     */
    private void nextJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextJButtonActionPerformed

        // we do a little hack:
        // this action will forward to the next page of PRIDE converter, however we want check on the species param
        // we can only perform the action (which means a forward to the next page) or don't perform the action (which
        // means the same check is done the next time the action is performed) Hence if no species param was given,
        // it will not forward to the next page. It will effectively act as a enforcement
        // So in order to take care of cases where the user gets a warning, but does not want to provide a species, we
        // need a flag to tell us if the species check has already been done (and is deliberately ignored).

        // first criterion is a MUST condition
        if (singleSampleDetailsJTable.getRowCount() > 0 &&
                multipleSamplesDetailsJTable.getRowCount() > 0) {
            JOptionPane.showMessageDialog(this,
                    "You can only use one sample type at the time. Delete\n" +
                    "all details about the sample type you do not want to use.",
                    "Verify Inserted Sample Details",
                    JOptionPane.WARNING_MESSAGE);
        } else if ( speciesCheckDone || speciesParamGiven() ) { // this is only a SHOULD condition and we will proceed if the user is determined to go on
            // if the species is given or a check has been done, but was ignored, we continue
            // we reset the speciesCheckDone flag, since we are going on in any case
            if (saveInsertedInformation()) {
                // before we go on, we reset the species check, just in case...
                new ProtocolDetails(this.getLocation());
                this.setVisible(false);
                this.dispose();
            }
        } else {
            // the species was not given, so we provide a warning
            JOptionPane.showMessageDialog(this,
                    "No species annotation found! We strongly recommend to " +
                    "annotate the dataset with a NEWT ontology term.",
                    "Check Inserted Sample Details",
                    JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_nextJButtonActionPerformed

    private boolean speciesParamGiven() {
        boolean found = false;
        // iterate over all sample associated CV terms and see if we have a NEWT taxonomy term
        for (Object cvObject : tempSampleCvParameters) {
            CvParamImpl cvParam = (CvParamImpl) cvObject;
            if (cvParam.getCVLookup().equalsIgnoreCase("NEWT")) {
                found = true; // taxonomy term specified - we are happy
                break; // no need to look further
            }
        }
        // before we return the result, we register that the search has run
        speciesCheckDone = true;
        return found;
    }

    /**
     * Stores the sample set selected and opens the previous frame (ExperimentProperties).
     * 
     * @param evt
     */
    private void backJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backJButtonActionPerformed
        if (saveInsertedInformation()) {
            new ExperimentProperties(this.getLocation());
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
        PRIDEConverter.cancelConvertion();
    }//GEN-LAST:event_cancelJButtonActionPerformed

    /**
     * Opens an OLS dialog for editing the current sample.
     * 
     * @param evt
     */
    private void sampleDetailsJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sampleDetailsJButtonActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        String ontologyTitle = PRIDEConverter.getUserProperties().getLastSelectedSampleOntology();
        String newtRoot = PRIDEConverter.getUserProperties().getNewtRoot();
        if(newtRoot.indexOf(ontologyTitle) != -1){
            ontologyTitle = newtRoot;
        }
        new OLSDialog(this, this, true, "singleSample", ontologyTitle, null, preselectedOntologyTerms);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_sampleDetailsJButtonActionPerformed

    /**
     * Opens an OLS dialog for editing the current sample.
     * 
     * @param evt
     */
    private void editJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editJMenuItemActionPerformed
        int selectedRow = singleSampleDetailsJTable.getSelectedRow();

        String searchTerm = (String) singleSampleDetailsJTable.getValueAt(selectedRow, 1);

        String ontology = searchTerm.substring(searchTerm.lastIndexOf("[") + 1, searchTerm.lastIndexOf("]") - 1);
        ontology = PRIDEConverter.getOntologyFromCvTerm(ontology);

        searchTerm = searchTerm.substring(0, searchTerm.lastIndexOf("[") - 1);
        searchTerm = searchTerm.replaceAll("-", " ");
        searchTerm = searchTerm.replaceAll(":", " ");
        searchTerm = searchTerm.replaceAll("\\(", " ");
        searchTerm = searchTerm.replaceAll("\\)", " ");
        searchTerm = searchTerm.replaceAll("&", " ");
        searchTerm = searchTerm.replaceAll("\\+", " ");
        searchTerm = searchTerm.replaceAll("\\[", " ");
        searchTerm = searchTerm.replaceAll("\\]", " ");

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                String msNewtRoot = PRIDEConverter.getUserProperties().getNewtRoot();
        if(msNewtRoot.indexOf(ontology) != -1){
            ontology = msNewtRoot;
        }
        new OLSDialog(this, this, true, "singleSample", ontology, selectedRow, searchTerm, preselectedOntologyTerms);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_editJMenuItemActionPerformed

    /**
     * Moves the selected sample up one position in the table.
     * 
     * @param evt
     */
    private void moveUpJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpJMenuItemActionPerformed
        int selectedRow = singleSampleDetailsJTable.getSelectedRow();
        int selectedColumn = singleSampleDetailsJTable.getSelectedColumn();

        Object[] tempRow = new Object[]{
            singleSampleDetailsJTable.getValueAt(selectedRow - 1, 0),
            singleSampleDetailsJTable.getValueAt(selectedRow - 1, 1),
            singleSampleDetailsJTable.getValueAt(selectedRow - 1, 2)
        };

        ((DefaultTableModel) singleSampleDetailsJTable.getModel()).removeRow(selectedRow - 1);
        ((DefaultTableModel) singleSampleDetailsJTable.getModel()).insertRow(selectedRow, tempRow);

        singleSampleDetailsJTable.changeSelection(selectedRow - 1, selectedColumn, false, false);

        fixIndicesSingleSampleTable();

        valuesChanged = true;

        //move in data structure as well
        CvParamImpl temp = (CvParamImpl) tempSampleCvParameters.get(selectedRow - 1);
        tempSampleCvParameters.setElementAt(tempSampleCvParameters.get(selectedRow), selectedRow - 1);
        tempSampleCvParameters.setElementAt(temp, selectedRow);
}//GEN-LAST:event_moveUpJMenuItemActionPerformed

    /**
     * Moves the selected sample down one position in the table.
     * 
     * @param evt
     */
    private void moveDownJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownJMenuItemActionPerformed
        int selectedRow = singleSampleDetailsJTable.getSelectedRow();
        int selectedColumn = singleSampleDetailsJTable.getSelectedColumn();

        Object[] tempRow = new Object[]{
            singleSampleDetailsJTable.getValueAt(selectedRow + 1, 0),
            singleSampleDetailsJTable.getValueAt(selectedRow + 1, 1),
            singleSampleDetailsJTable.getValueAt(selectedRow + 1, 2)
        };

        ((DefaultTableModel) singleSampleDetailsJTable.getModel()).removeRow(selectedRow + 1);
        ((DefaultTableModel) singleSampleDetailsJTable.getModel()).insertRow(selectedRow, tempRow);

        singleSampleDetailsJTable.changeSelection(selectedRow + 1, selectedColumn, false, false);

        fixIndicesSingleSampleTable();

        valuesChanged = true;

        //move in data structure as well
        CvParamImpl temp = (CvParamImpl) tempSampleCvParameters.get(selectedRow + 1);
        tempSampleCvParameters.setElementAt(tempSampleCvParameters.get(selectedRow), selectedRow + 1);
        tempSampleCvParameters.setElementAt(temp, selectedRow);
}//GEN-LAST:event_moveDownJMenuItemActionPerformed

    /**
     * @see #cancelJButtonActionPerformed(java.awt.event.ActionEvent)
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
        new HelpDialog(this, false, getClass().getResource("/no/uib/prideconverter/helpfiles/SampleDetails.html"));
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
        PRIDEConverter.getUserProperties().setCurrentSampleSet((String) namesJComboBox.getSelectedItem());
        mandatoryFieldsCheck();
    }//GEN-LAST:event_namesJComboBoxItemStateChanged

    /**
     * Saves the given sample (set) to file.
     *
     * @param filePath the path to save to
     * @param sampleName the name of the sample (set)
     */
    private void saveSample(String filePath, String sampleName) {

        try {

            FileWriter r = new FileWriter(filePath);
            BufferedWriter bw = new BufferedWriter(r);

            bw.write("Name: " + sampleName + "\n");

            if (multipleSamplesDetailsJTable.getRowCount() > 0) { // multiple samples

                Iterator iterator = PRIDEConverter.getProperties().getSampleDescriptionCVParams().iterator();

                CvParamImpl temp;

                bw.write(PRIDEConverter.getProperties().getSampleDescriptionCVParams().size() + "\n");

                for (int i = 0; i < PRIDEConverter.getProperties().getSampleDescriptionUserSubSampleNames().size(); i++) {
                    bw.write(PRIDEConverter.getProperties().getSampleDescriptionUserSubSampleNames().get(i) + "\n");
                }

                bw.write("#\n");

                while (iterator.hasNext()) {
                    temp = ((CvParamImpl) iterator.next());

                    bw.write("Accession: " + temp.getAccession() + "\n");
                    bw.write("CVLookup: " + temp.getCVLookup() + "\n");
                    bw.write("Name: " + temp.getName() + "\n");
                    bw.write("Value: " + temp.getValue() + "\n\n");
                }
            } else { // single sample

                PRIDEConverter.getProperties().setSampleDescriptionCVParams(new ArrayList());
                PRIDEConverter.getProperties().setSampleDescriptionUserSubSampleNames(new ArrayList());

                bw.write(tempSampleCvParameters.size() + "\n");
                bw.write("Name: " + sampleName + "\n");
                bw.write("#\n");

                for (int i = 0; i < tempSampleCvParameters.size(); i++) {

                    CvParamImpl temp = ((CvParamImpl) tempSampleCvParameters.get(i));

                    String value = null;

                    if (singleSampleDetailsJTable.getValueAt(i, 2) != null) {
                        value = (String) singleSampleDetailsJTable.getValueAt(i, 2);
                    }

                    bw.write("Accession: " + temp.getAccession() + "\n");
                    bw.write("CVLookup: " + temp.getCVLookup() + "\n");
                    bw.write("Name: " + temp.getName() + "\n");
                    bw.write("Value: " + value + "\n\n");

                    PRIDEConverter.getProperties().getSampleDescriptionCVParams().add(
                            new CvParamImpl(temp.getAccession(),
                            temp.getCVLookup(),
                            temp.getName(),
                            i,
                            value));
                }
            }

            bw.close();
            r.close();

        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(
                    this, "The file " + filePath + " could not be found.",
                    "File Not Found", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to save file: ");
            ex.printStackTrace();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this, "An error occured when trying to save the file " + filePath + ".",
                    "File Error", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to save file: ");
            ex.printStackTrace();
        }
    }

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
                    "The sample set has been changed. Save changes?",
                    "Sample Set Changed", JOptionPane.YES_NO_CANCEL_OPTION);

            if (value == JOptionPane.YES_OPTION) {

                value = JOptionPane.showConfirmDialog(this,
                        "Overwrite existing sample set?",
                        "Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);

                if (value == JOptionPane.YES_OPTION) {

                    String newName;
                    newName = samplePath + lastSelectedSampleName + ".sam";

                    saveSample(newName, lastSelectedSampleName);

                } else if (value == JOptionPane.CANCEL_OPTION) {
                    cancel = true;

                    namesJComboBox.setSelectedItem(lastSelectedSampleName);

                } else { //value == NO

                    String newSampleName = JOptionPane.showInputDialog(this,
                            "Provide the name of the new sample set: ",
                            "Sample Set Name", JOptionPane.PLAIN_MESSAGE);

                    if (newSampleName != null) {

                        String newName;
                        newName = samplePath + newSampleName + ".sam";

                        while (new File(newName).exists()) {
                            newSampleName = JOptionPane.showInputDialog(this,
                                    "This name is already in use. Please provide a different name: ",
                                    "Sample Set Name", JOptionPane.PLAIN_MESSAGE);

                            newName = samplePath + newSampleName + ".sam";
                        }

                        if (newSampleName != null) {

                            PRIDEConverter.getProperties().setSampleDescriptionCVParamsQuantification(new ArrayList());
                            PRIDEConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());

                            saveSample(newName, newSampleName);

                            valuesChanged = false;

                            namesJComboBox.insertItemAt(newSampleName, namesJComboBox.getItemCount() - 2);
                        } else {
                            cancel = true;
                            namesJComboBox.setSelectedItem(lastSelectedSampleName);
                            PRIDEConverter.getUserProperties().setCurrentSampleSet(lastSelectedSampleName);
                        }
                    } else {
                        cancel = true;
                        namesJComboBox.setSelectedItem(lastSelectedSampleName);
                        PRIDEConverter.getUserProperties().setCurrentSampleSet(lastSelectedSampleName);
                    }
                }
            } else if (value == JOptionPane.CANCEL_OPTION) {
                cancel = true;
                namesJComboBox.setSelectedItem(lastSelectedSampleName);
                PRIDEConverter.getUserProperties().setCurrentSampleSet(lastSelectedSampleName);
            }
        }

        if (!cancel) {

            lastSelectedSampleName = (String) namesJComboBox.getSelectedItem();
            String selectedSampleName = (String) namesJComboBox.getSelectedItem();

            PRIDEConverter.getUserProperties().setCurrentSampleSet(selectedSampleName);

            //empty the tables
            while (singleSampleDetailsJTable.getRowCount() > 0) {
                ((DefaultTableModel) singleSampleDetailsJTable.getModel()).removeRow(0);
            }

            while (multipleSamplesDetailsJTable.getRowCount() > 0) {
                ((DefaultTableModel) multipleSamplesDetailsJTable.getModel()).removeRow(0);
            }

            PRIDEConverter.getProperties().setSampleDescriptionCVParams(new ArrayList());
            PRIDEConverter.getProperties().setSampleDescriptionUserSubSampleNames(new ArrayList());

            if (namesJComboBox.getSelectedIndex() == 0) {
                sampleDetailsJButton.setEnabled(false);
                addSampleJButton.setEnabled(false);
                valuesChanged = false;
                PRIDEConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());
            } else if (namesJComboBox.getSelectedIndex() == namesJComboBox.getItemCount() - 1) {

                sampleDetailsJButton.setEnabled(false);
                addSampleJButton.setEnabled(false);
                valuesChanged = false;
                PRIDEConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());

                ComboBoxInputDialog input = new ComboBoxInputDialog(this, this, true);
                input.setTitle("Create New Sample Set");
                input.setBorderTitle("New Sample Set");
                input.setVisible(true);
            } else {

                if (!keepQuantificationSelection) {
                    PRIDEConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());
                }

                sampleDetailsJButton.setEnabled(true);
                addSampleJButton.setEnabled(true);

                selectedSampleName = samplePath + selectedSampleName + ".sam";

                try {
                    String temp;

                    FileReader f = new FileReader(selectedSampleName);
                    BufferedReader b = new BufferedReader(f);

                    tempSampleCvParameters = new Vector();

                    String line = b.readLine();

                    boolean multipleSamples = false;

                    while (line != null && !multipleSamples) {

                        if (line.lastIndexOf("SUBSAMPLE_2") != -1) {
                            multipleSamples = true;
                        }

                        line = b.readLine();
                    }

                    if (multipleSamples) {
                        sampleJTabbedPane.setSelectedIndex(1);
                    } else {
                        sampleJTabbedPane.setSelectedIndex(0);
                    }


                    f = new FileReader(selectedSampleName);
                    b = new BufferedReader(f);

                    b.readLine();

                    int numberOfCVTerms;

                    Vector names, accessions, ontologies, values;
                    String subSampleName;

                    numberOfCVTerms = new Integer(b.readLine()).intValue();

                    subSampleName = b.readLine();

                    if (subSampleName != null) {
                        while (!subSampleName.equalsIgnoreCase("#")) {

                            if (multipleSamples) {
                                PRIDEConverter.getProperties().getSampleDescriptionUserSubSampleNames().add(subSampleName);
                            }

                            subSampleName = b.readLine();
                        }
                    }

                    names = new Vector();
                    accessions = new Vector();
                    ontologies = new Vector();
                    values = new Vector();
                    String name, accession, ontology, value;

                    String subSampleNumber = "SUBSAMPLE_1";
                    //String currentSubSampleNumber = "";

                    for (int i = 0; i < numberOfCVTerms; i++) {

                        temp = b.readLine();
                        accession = temp.substring(temp.indexOf(": ") + 2);
                        temp = b.readLine();
                        ontology = temp.substring(temp.indexOf(": ") + 2);
                        temp = b.readLine();
                        name = temp.substring(temp.indexOf(": ") + 2);
                        temp = b.readLine();
                        value = temp.substring(temp.indexOf(": ") + 2);
                        b.readLine();

                        if (multipleSamples) {
                            if (!value.equalsIgnoreCase(subSampleNumber)) {
                                addSampleDetails(null, names, accessions, ontologies, -1);

                                names = new Vector();
                                accessions = new Vector();
                                ontologies = new Vector();
                                values = new Vector();

                                names.add(name);
                                accessions.add(accession);
                                ontologies.add(ontology);
                                values.add(value);

                                subSampleNumber = value;
                            } else {
                                names.add(name);
                                accessions.add(accession);
                                ontologies.add(ontology);
                                values.add(value);
                            }
                        } else {

                            if (value.lastIndexOf("SUBSAMPLE_") != -1) {
                                value = null;
                            } else if (value.equalsIgnoreCase("null")) {
                                value = null;
                            }

                            addSampleDetails(name, accession, ontology, value, -1);
                        }
                    }

                    if (names.size() > 0) {
                        addSampleDetails(null, names, accessions, ontologies, -1);
                    }

                    if (PRIDEConverter.getProperties().getCurrentQuantificationSelection().size() > 0) {
                        for (int i = 0; i < PRIDEConverter.getProperties().getCurrentQuantificationSelection().size(); i++) {
                            multipleSamplesDetailsJTable.setValueAt(PRIDEConverter.getProperties().getCurrentQuantificationSelection().get(i), i, 2);
                        }
                    }

                    quantificationComboBoxActionPerformed(null);

                    b.close();
                    f.close();

                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(
                            this, "The file " + selectedSampleName + " could not be found.",
                            "File Not Found", JOptionPane.ERROR_MESSAGE);
                    Util.writeToErrorLog("Error when trying to read file: ");
                    ex.printStackTrace();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                            this, "An error occured when trying to read the file " + selectedSampleName + ".",
                            "File Error", JOptionPane.ERROR_MESSAGE);
                    Util.writeToErrorLog("Error when trying to read file: ");
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

        int option = JOptionPane.showConfirmDialog(this, "This will delete the selected sample set. Continue?");

        if (option == JOptionPane.YES_OPTION) {

            String newName = samplePath +
                    (String) namesJComboBox.getSelectedItem() + ".sam";

            boolean deleted = new File(newName).delete();

            PRIDEConverter.getProperties().setSampleDescriptionCVParamsQuantification(new ArrayList());

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
        new HelpDialog(this, false, getClass().getResource("/no/uib/prideconverter/helpfiles/AboutPRIDE_Converter.html"));
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
    private void singleSampleDetailsJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_singleSampleDetailsJTableKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            deleteSelectedRowJMenuItemActionPerformed(null);
        }
}//GEN-LAST:event_singleSampleDetailsJTableKeyReleased

    /**
     * If the user double clicks on a row in the table the OLS
     * dialog is shown where the information about the given sample can be
     * altered. If the user right clicks a pop up menu is shown for editing,
     * moving or delting the selected sample.
     *
     * @param evt
     */
    private void multipleSamplesDetailsJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_multipleSamplesDetailsJTableMouseClicked
        if (evt.getButton() == 3) {

            int row = multipleSamplesDetailsJTable.rowAtPoint(evt.getPoint());
            int column = multipleSamplesDetailsJTable.columnAtPoint(evt.getPoint());

            multipleSamplesDetailsJTable.changeSelection(row, column, false, false);

            this.moveUpMultipleSamplesJMenuItem.setEnabled(true);
            this.moveDownMultipleSamplesJMenuItem.setEnabled(true);

            if (row == multipleSamplesDetailsJTable.getRowCount() - 1) {
                this.moveDownMultipleSamplesJMenuItem.setEnabled(false);
            }

            if (row == 0) {
                this.moveUpMultipleSamplesJMenuItem.setEnabled(false);
            }

            popupMultipleSamplesJMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        } else if (evt.getButton() == 1 && evt.getClickCount() == 2) {
            editMultipleSamplesJMenuItemActionPerformed(null);
        }
}//GEN-LAST:event_multipleSamplesDetailsJTableMouseClicked

    /**
     * If the delete key is pressed the selected row in the table are deleted.
     *
     * @param evt
     */
    private void multipleSamplesDetailsJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_multipleSamplesDetailsJTableKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            deleteSelectedRowMultipleSamplesJMenuItemActionPerformed(null);
        }
}//GEN-LAST:event_multipleSamplesDetailsJTableKeyReleased

    /**
     * Opens a dialog where informaton about a sample can be inserted.
     *
     * @param evt
     */
    private void addSampleJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSampleJButtonActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new NewSample(this, true);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_addSampleJButtonActionPerformed

    /**
     * Opens a dialog where informaton about the selected sample can be edited.
     *
     * @param evt
     */
    private void editMultipleSamplesJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMultipleSamplesJMenuItemActionPerformed
        int selectedRow = multipleSamplesDetailsJTable.getSelectedRow();

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new NewSample(this, true, selectedRow);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_editMultipleSamplesJMenuItemActionPerformed

    /**
     * Moves the selected sample up one position in the table.
     *
     * @param evt
     */
    private void moveUpMultipleSamplesJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpMultipleSamplesJMenuItemActionPerformed
        int selectedRow = multipleSamplesDetailsJTable.getSelectedRow();
        int selectedColumn = multipleSamplesDetailsJTable.getSelectedColumn();

        Object[] tempRow = new Object[]{
            multipleSamplesDetailsJTable.getValueAt(selectedRow - 1, 0),
            multipleSamplesDetailsJTable.getValueAt(selectedRow - 1, 1),
            multipleSamplesDetailsJTable.getValueAt(selectedRow - 1, 2)
        };

        ((DefaultTableModel) multipleSamplesDetailsJTable.getModel()).removeRow(selectedRow - 1);
        ((DefaultTableModel) multipleSamplesDetailsJTable.getModel()).insertRow(selectedRow, tempRow);

        multipleSamplesDetailsJTable.changeSelection(selectedRow - 1, selectedColumn, false, false);

        fixIndicesMultipleSamplesTable();

        valuesChanged = true;

        //move in data structure as well
        Object tempSubSampleName = PRIDEConverter.getProperties().getSampleDescriptionUserSubSampleNames().get(
                selectedRow - 1);
        PRIDEConverter.getProperties().getSampleDescriptionUserSubSampleNames().set(
                selectedRow - 1,
                PRIDEConverter.getProperties().getSampleDescriptionUserSubSampleNames().get(selectedRow));
        PRIDEConverter.getProperties().getSampleDescriptionUserSubSampleNames().set(selectedRow,
                tempSubSampleName);

        Object[] sampleDetails = PRIDEConverter.getProperties().getSampleDescriptionCVParams().toArray();

        PRIDEConverter.getProperties().setSampleDescriptionCVParams(new ArrayList());

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

        for (int i = 0; i < multipleSamplesDetailsJTable.getRowCount(); i++) {
            tempTableRow = (String) multipleSamplesDetailsJTable.getValueAt(i, 1);

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

            PRIDEConverter.getProperties().getSampleDescriptionCVParams().add(tempCvParamImpl);
        }
}//GEN-LAST:event_moveUpMultipleSamplesJMenuItemActionPerformed

    /**
     * Moves the selected sample down one position in the table.
     *
     * @param evt
     */
    private void moveDownMultipleSamplesJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownMultipleSamplesJMenuItemActionPerformed
        int selectedRow = multipleSamplesDetailsJTable.getSelectedRow();
        int selectedColumn = multipleSamplesDetailsJTable.getSelectedColumn();

        Object[] tempRow = new Object[]{
            multipleSamplesDetailsJTable.getValueAt(selectedRow + 1, 0),
            multipleSamplesDetailsJTable.getValueAt(selectedRow + 1, 1),
            multipleSamplesDetailsJTable.getValueAt(selectedRow + 1, 2)
        };

        ((DefaultTableModel) multipleSamplesDetailsJTable.getModel()).removeRow(selectedRow + 1);
        ((DefaultTableModel) multipleSamplesDetailsJTable.getModel()).insertRow(selectedRow, tempRow);

        multipleSamplesDetailsJTable.changeSelection(selectedRow + 1, selectedColumn, false, false);

        fixIndicesMultipleSamplesTable();

        valuesChanged = true;

        //move in data structure as well
        Object tempSubSampleName = PRIDEConverter.getProperties().getSampleDescriptionUserSubSampleNames().get(
                selectedRow + 1);
        PRIDEConverter.getProperties().getSampleDescriptionUserSubSampleNames().set(
                selectedRow + 1,
                PRIDEConverter.getProperties().getSampleDescriptionUserSubSampleNames().get(selectedRow));
        PRIDEConverter.getProperties().getSampleDescriptionUserSubSampleNames().set(selectedRow,
                tempSubSampleName);

        Object[] sampleDetails = PRIDEConverter.getProperties().getSampleDescriptionCVParams().toArray();

        PRIDEConverter.getProperties().setSampleDescriptionCVParams(new ArrayList());

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

        for (int i = 0; i < multipleSamplesDetailsJTable.getRowCount(); i++) {
            tempTableRow = (String) multipleSamplesDetailsJTable.getValueAt(i, 1);

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

            PRIDEConverter.getProperties().getSampleDescriptionCVParams().add(tempCvParamImpl);
        }
}//GEN-LAST:event_moveDownMultipleSamplesJMenuItemActionPerformed

    /**
     * Deletes the selected sample from the table.
     *
     * @param evt
     */
    private void deleteSelectedRowMultipleSamplesJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSelectedRowMultipleSamplesJMenuItemActionPerformed

        int selectedRow = multipleSamplesDetailsJTable.getSelectedRow();

        if (selectedRow != -1) {

            PRIDEConverter.getProperties().getSampleDescriptionUserSubSampleNames().remove(selectedRow);

            ((DefaultTableModel) multipleSamplesDetailsJTable.getModel()).removeRow(selectedRow);

            //remove from datastructure as well
            Object[] sampleDetails = PRIDEConverter.getProperties().getSampleDescriptionCVParams().toArray();

            PRIDEConverter.getProperties().setSampleDescriptionCVParams(new ArrayList());

            String tempRow;
            Vector subSampleNumbers = new Vector();

            for (int i = 0; i < multipleSamplesDetailsJTable.getRowCount(); i++) {
                tempRow = (String) multipleSamplesDetailsJTable.getValueAt(i, 1);

                for (int j = 0; j < tempRow.length(); j++) {
                    if (tempRow.charAt(j) == ']') {
                        subSampleNumbers.add("SUBSAMPLE_" + (i + 1));
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

                    PRIDEConverter.getProperties().getSampleDescriptionCVParams().add(tempCvParamImpl);
                }
            }

            valuesChanged = true;

            fixIndicesMultipleSamplesTable();
            mandatoryFieldsCheck();
        }
}//GEN-LAST:event_deleteSelectedRowMultipleSamplesJMenuItemActionPerformed

    /**
     * Deletes the selected sample set.
     *
     * @param evt
     */
    private void deleteMultipleSamplesJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMultipleSamplesJMenuItemActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        int option = JOptionPane.showConfirmDialog(this, "This will delete the selected sample set up. Continue?");

        if (option == JOptionPane.YES_OPTION) {

            String newName = samplePath +
                    (String) namesJComboBox.getSelectedItem() + ".sam";

            boolean deleted = new File(newName).delete();

            PRIDEConverter.getProperties().setSampleDescriptionCVParamsQuantification(new ArrayList());

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
}//GEN-LAST:event_deleteMultipleSamplesJMenuItemActionPerformed

    /**
     * Add a sample cv term to the table.
     *
     * @param name
     * @param accession
     * @param ontology
     * @param modifiedRow the row to modify, use -1 if adding a new row
     */
    public void addSampleDetails(String name, String accession, String ontology, int modifiedRow) {
        addSampleDetails(name, accession, ontology, null, modifiedRow);
    }

    /**
     * Add a sample cv term to the table.
     *
     * @param name
     * @param accession
     * @param ontology
     * @param value
     * @param modifiedRow the row to modify, use -1 if adding a new row
     */
    public void addSampleDetails(String name, String accession, String ontology, String value, int modifiedRow) {

        valuesChanged = true;

        if (modifiedRow == -1) {

            ((DefaultTableModel) this.singleSampleDetailsJTable.getModel()).addRow(
                    new Object[]{
                        new Integer(singleSampleDetailsJTable.getRowCount() + 1),
                        name + " [" + accession + "]", value
                    });

            tempSampleCvParameters.add(new CvParamImpl(
                    accession,
                    ontology,
                    name,
                    new Long(singleSampleDetailsJTable.getRowCount() - 1),
                    value));
        } else {
            singleSampleDetailsJTable.setValueAt(name + " [" + accession + "]", modifiedRow, 1);
            singleSampleDetailsJTable.setValueAt(null, modifiedRow, 2);

            CvParamImpl cvParam = new CvParamImpl(
                    accession,
                    ontology,
                    name,
                    new Long(modifiedRow),
                    value);

            tempSampleCvParameters.setElementAt(cvParam, modifiedRow);
        }

        mandatoryFieldsCheck();
    }

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

            for (int i = 0; i < names.size(); i++) {

                PRIDEConverter.getProperties().getSampleDescriptionCVParams().add(new CvParamImpl(
                        (String) accessions.get(i),
                        (String) ontologies.get(i),
                        (String) names.get(i),
                        new Long(multipleSamplesDetailsJTable.getRowCount() - 1),
                        ("SUBSAMPLE_" + (multipleSamplesDetailsJTable.getRowCount() + 1))));

                temp += "[" + (String) names.get(i) + "]";
            }

            ((DefaultTableModel) multipleSamplesDetailsJTable.getModel()).addRow(
                    new Object[]{new Integer(multipleSamplesDetailsJTable.getRowCount() + 1), temp});

            if (sampleName != null) {
                PRIDEConverter.getProperties().getSampleDescriptionUserSubSampleNames().add(sampleName);
            }
        } else {

            String temp = "";

            Object[] cvParams = PRIDEConverter.getProperties().getSampleDescriptionCVParams().toArray();
            CvParamImpl cvParam;

            PRIDEConverter.getProperties().setSampleDescriptionCVParams(new ArrayList());

            boolean valuesAdded = false;

            for (int i = 0; i < cvParams.length; i++) {

                cvParam = (CvParamImpl) cvParams[i];


                if (cvParam.getValue().equalsIgnoreCase("SUBSAMPLE_" + (modifiedRow + 1))) {
                    if (!valuesAdded) {
                        for (int j = 0; j < names.size(); j++) {
                            PRIDEConverter.getProperties().getSampleDescriptionCVParams().add(new CvParamImpl(
                                    (String) accessions.get(j),
                                    (String) ontologies.get(j),
                                    (String) names.get(j),
                                    new Long(multipleSamplesDetailsJTable.getRowCount() - 1),
                                    ("SUBSAMPLE_" + (modifiedRow + 1))));

                            temp += "[" + (String) names.get(i) + "]";
                        }

                        valuesAdded = true;
                    }
                } else {
                    PRIDEConverter.getProperties().getSampleDescriptionCVParams().add(cvParam);
                }
            }

            multipleSamplesDetailsJTable.setValueAt(temp, modifiedRow, 1);

            PRIDEConverter.getProperties().getSampleDescriptionUserSubSampleNames().set(modifiedRow, sampleName);
        }

        mandatoryFieldsCheck();
    }

    /**
     * Fixes the indices so that they are in accending order starting from one
     */
    private void fixIndicesSingleSampleTable() {
        for (int row = 0; row < ((DefaultTableModel) singleSampleDetailsJTable.getModel()).getRowCount(); row++) {
            ((DefaultTableModel) singleSampleDetailsJTable.getModel()).setValueAt(new Integer(row + 1), row, 0);
        }
    }

    /**
     * Fixes the indices so that they are in accending order starting from one
     */
    private void fixIndicesMultipleSamplesTable() {
        for (int row = 0; row < ((DefaultTableModel) multipleSamplesDetailsJTable.getModel()).getRowCount(); row++) {
            ((DefaultTableModel) multipleSamplesDetailsJTable.getModel()).setValueAt(new Integer(row + 1), row, 0);
        }
    }

    /**
     * Saves the inserted information to the Properties object.
     * 
     * @return true if save was succesfull, false otherwise.
     */
    private boolean saveInsertedInformation() {
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        PRIDEConverter.getUserProperties().setCurrentSampleSet((String) namesJComboBox.getSelectedItem());
        PRIDEConverter.getProperties().setSampleDescriptionCVParamsQuantification(new ArrayList());
        PRIDEConverter.getProperties().setCurrentQuantificationSelection(new ArrayList());

        int counter = PRIDEConverter.getProperties().getSampleDescriptionCVParams().size();

        for (int i = 0; i < multipleSamplesDetailsJTable.getRowCount(); i++) {

            if (multipleSamplesDetailsJTable.getValueAt(i, 2) != null) {

                if (((String) multipleSamplesDetailsJTable.getValueAt(i, 2)).equalsIgnoreCase("iTRAQ reagent 114")) {
                    PRIDEConverter.getProperties().getSampleDescriptionCVParamsQuantification().add(
                            new CvParamImpl("PRIDE:0000114", "PRIDE", "iTRAQ reagent 114", counter++, "SUBSAMPLE_" +
                            (i + 1)));
                } else if (((String) multipleSamplesDetailsJTable.getValueAt(i, 2)).equalsIgnoreCase("iTRAQ reagent 115")) {
                    PRIDEConverter.getProperties().getSampleDescriptionCVParamsQuantification().add(
                            new CvParamImpl("PRIDE:0000115", "PRIDE", "iTRAQ reagent 115", counter++, "SUBSAMPLE_" +
                            (i + 1)));
                } else if (((String) multipleSamplesDetailsJTable.getValueAt(i, 2)).equalsIgnoreCase("iTRAQ reagent 116")) {
                    PRIDEConverter.getProperties().getSampleDescriptionCVParamsQuantification().add(
                            new CvParamImpl("PRIDE:0000116", "PRIDE", "iTRAQ reagent 116", counter++, "SUBSAMPLE_" +
                            (i + 1)));
                } else if (((String) multipleSamplesDetailsJTable.getValueAt(i, 2)).equalsIgnoreCase("iTRAQ reagent 117")) {
                    PRIDEConverter.getProperties().getSampleDescriptionCVParamsQuantification().add(
                            new CvParamImpl("PRIDE:0000117", "PRIDE", "iTRAQ reagent 117", counter++, "SUBSAMPLE_" +
                            (i + 1)));
                }
            }

            PRIDEConverter.getProperties().getCurrentQuantificationSelection().add((String) multipleSamplesDetailsJTable.getValueAt(i, 2));
        }

        boolean cancel = false;

        if (valuesChanged) {
            int value = JOptionPane.showConfirmDialog(this,
                    "The sample set has been changed. Save changes?",
                    "Sample Set Changed", JOptionPane.YES_NO_CANCEL_OPTION);

            if (value == JOptionPane.YES_OPTION) {

                value = JOptionPane.showConfirmDialog(this,
                        "Overwrite existing sample set?",
                        "Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);

                if (value == JOptionPane.YES_OPTION) {

                    String newName;
                    newName = samplePath + PRIDEConverter.getUserProperties().getCurrentSampleSet() + ".sam";

                    saveSample(newName, PRIDEConverter.getUserProperties().getCurrentSampleSet());

                } else if (value == JOptionPane.CANCEL_OPTION) {
                    cancel = true;
                } else { //value == NO

                    String newSampleName = JOptionPane.showInputDialog(this,
                            "Provide the name of the new sample set: ",
                            "Sample Set Name", JOptionPane.PLAIN_MESSAGE);

                    if (newSampleName != null) {

                        String newName;

                        newName = samplePath + newSampleName + ".sam";

                        while (new File(newName).exists()) {
                            newSampleName = JOptionPane.showInputDialog(this,
                                    "This name is already in use. Please provide a different name: ",
                                    "Sample Set Name", JOptionPane.PLAIN_MESSAGE);

                            newName = samplePath + newSampleName + ".sam";
                        }

                        if (newSampleName != null) {
                            PRIDEConverter.getUserProperties().setCurrentSampleSet(newSampleName);
                            saveSample(newName, newSampleName);
                        } else {
                            cancel = true;
                        }
                    } else {
                        cancel = true;
                    }
                }
            } else if (value == JOptionPane.CANCEL_OPTION) {
                cancel = true;
            }
        }

        if (singleSampleDetailsJTable.getRowCount() > 0) {

            PRIDEConverter.getProperties().setSampleDescriptionCVParams(new ArrayList());

            for (int i = 0; i < tempSampleCvParameters.size(); i++) {

                CvParamImpl temp = ((CvParamImpl) tempSampleCvParameters.get(i));

                String value = null;

                if (singleSampleDetailsJTable.getValueAt(i, 2) != null) {
                    value = (String) singleSampleDetailsJTable.getValueAt(i, 2);
                }

                PRIDEConverter.getProperties().getSampleDescriptionCVParams().add(
                        new CvParamImpl(temp.getAccession(),
                        temp.getCVLookup(),
                        temp.getName(),
                        i,
                        value));
            }
        }


        if (lowerRangeJTextField.isEnabled()) {
            try {
                PRIDEConverter.getUserProperties().setPeakIntegrationRangeLower(
                        new Double(lowerRangeJTextField.getText()).doubleValue());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Peak Integreation Lower Range is not a number.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                cancel = true;
                lowerRangeJTextField.requestFocus();
            }

            try {
                PRIDEConverter.getUserProperties().setPeakIntegrationRangeUpper(
                        new Double(upperRangeJTextField.getText()).doubleValue());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Peak Integreation Upper Range is not a number.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                cancel = true;
                upperRangeJTextField.requestFocus();
            }

            if (Math.abs(PRIDEConverter.getUserProperties().getPeakIntegrationRangeLower()) +
                    PRIDEConverter.getUserProperties().getPeakIntegrationRangeUpper() > 1) {
                JOptionPane.showMessageDialog(this,
                        "The input peak integration range cross over between peaks (sum is > 1).",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                cancel = true;
                lowerRangeJTextField.requestFocus();
            }

            try {
                PRIDEConverter.getUserProperties().setReporterIonIntensityThreshold(
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

                PRIDEConverter.getUserProperties().setPurityCorrections(purrityCorrections);
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
    private javax.swing.JButton addSampleJButton;
    private javax.swing.JButton backJButton;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JButton deleteJButton;
    private javax.swing.JMenuItem deleteJMenuItem;
    private javax.swing.JMenuItem deleteMultipleSamplesJMenuItem;
    private javax.swing.JMenuItem deleteSelectedRowJMenuItem;
    private javax.swing.JMenuItem deleteSelectedRowMultipleSamplesJMenuItem;
    private javax.swing.JMenuItem editJMenuItem;
    private javax.swing.JMenuItem editMultipleSamplesJMenuItem;
    private javax.swing.JButton helpJButton;
    private javax.swing.JLabel intensityThresholdJLabel;
    private javax.swing.JTextField intensityThresholdJTextField;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JLabel lowerRangeJLabel;
    private javax.swing.JTextField lowerRangeJTextField;
    private javax.swing.JMenuItem moveDownJMenuItem;
    private javax.swing.JMenuItem moveDownMultipleSamplesJMenuItem;
    private javax.swing.JMenuItem moveUpJMenuItem;
    private javax.swing.JMenuItem moveUpMultipleSamplesJMenuItem;
    private javax.swing.JTable multipleSamplesDetailsJTable;
    private javax.swing.JComboBox namesJComboBox;
    private javax.swing.JButton nextJButton;
    private javax.swing.JLabel peakRangeJLabel;
    private javax.swing.JPopupMenu popupDeleteMultipleSamplesJMenu;
    private javax.swing.JPopupMenu popupJMenu;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JPopupMenu popupMultipleSamplesJMenu;
    private javax.swing.JLabel purityCorrectionsJLabel;
    private javax.swing.JTextField purityCorrectionsJTextField;
    private javax.swing.JButton sampleDetailsJButton;
    private javax.swing.JTabbedPane sampleJTabbedPane;
    private javax.swing.JTable singleSampleDetailsJTable;
    private javax.swing.JLabel upperRangeJLabel;
    private javax.swing.JTextField upperRangeJTextField;
    // End of variables declaration//GEN-END:variables

    /**
     * @see ComboBoxInputable
     */
    public void insertIntoComboBox(String text) {

        PRIDEConverter.getUserProperties().setCurrentSampleSet(text);

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
            JOptionPane.showMessageDialog(
                    this, "The file " + newName + " could not be found.",
                    "File Not Found", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to save file: ");
            ex.printStackTrace();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this, "An error occured when trying to save the file " + newName + ".",
                    "File Error", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to save file: ");
            ex.printStackTrace();
        }
    }

    /**
     * @see ComboBoxInputable
     */
    public void resetComboBox() {
        lastSelectedSampleName = null;
        namesJComboBox.setSelectedIndex(0);
        readSamplesFromFile();
    }

    /**
     * @see ComboBoxInputable
     */
    public boolean alreadyInserted(String currentSampleName) {

        String newName;
        newName = samplePath + currentSampleName + ".sam";
        File newFile = new File(newName);

        return newFile.exists();
    }

    public void insertOLSResult(String field, String selectedValue, String accession, String ontologyShort,
            String ontologyLong, int modifiedRow, String mappedTerm, Map<String, String> metadata) {

        if (mappedTerm != null) {
            PRIDEConverter.getUserProperties().getCVTermMappings().put(
                    mappedTerm, new CvParamImpl(accession, ontologyShort,
                    selectedValue, 0, null));
        }

        PRIDEConverter.getUserProperties().setLastSelectedSampleOntology(ontologyLong);

        addSampleDetails(selectedValue, accession, ontologyShort, modifiedRow);
    }

    public Window getWindow() {
        return (Window) this;
    }
}
