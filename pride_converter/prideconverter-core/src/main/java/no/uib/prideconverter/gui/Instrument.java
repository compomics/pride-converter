package no.uib.prideconverter.gui;

import no.uib.prideconverter.PRIDEConverter;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import no.uib.prideconverter.gui.ComboBoxInputDialog;
import no.uib.prideconverter.util.ComboBoxInputable;
import no.uib.prideconverter.util.MyComboBoxRenderer;
import no.uib.prideconverter.util.OLSInputable;
import org.systemsbiology.jrap.MSXMLParser;
import uk.ac.ebi.pride.model.implementation.mzData.AnalyzerImpl;
import uk.ac.ebi.pride.model.implementation.mzData.CvParamImpl;
import uk.ac.ebi.pride.model.interfaces.mzdata.Analyzer;
import uk.ac.ebi.pride.model.interfaces.mzdata.CvParam;

/**
 * This frame handles the information about the instrument (source, detector 
 * analyzers and processing methods). 
 * 
 * @author  Harald Barsnes
 * 
 * Created March 2008
 */
public class Instrument extends javax.swing.JFrame implements ComboBoxInputable, OLSInputable {

    private PRIDEConverter prideConverter;
    private Vector tempProcessingMethods;
    private String instrumentPath;
    private boolean valuesChanged = false;
    private String lastSelectedInstrumentName;

    /** 
     * Opens a new Instrument frame and inserts the stored information.
     * 
     * @param prideConverter
     * @param location where to position the frame
     */
    public Instrument(PRIDEConverter prideConverter, Point location) {
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

        // sets the icon of the frame
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/no/uib/prideconverter/icons/prideConverter_16.GIF")));

        processingMethodsJTable.getTableHeader().setReorderingAllowed(false);
        processingMethodsJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        analyzerJTable.getTableHeader().setReorderingAllowed(false);
        analyzerJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        setTitle(prideConverter.getWizardName() + " " +
                prideConverter.getPrideConverterVersionNumber() + " - " + getTitle());

        processingMethodsJTable.getColumn(" ").setMaxWidth(40);
        processingMethodsJTable.getColumn(" ").setMinWidth(40);

        analyzerJTable.getColumn(" ").setMaxWidth(40);
        analyzerJTable.getColumn(" ").setMinWidth(40);
        analyzerJTable.getColumn("#Terms").setMinWidth(60);
        analyzerJTable.getColumn("#Terms").setMaxWidth(60);

        ((DefaultTableModel) processingMethodsJTable.getModel()).addTableModelListener(
                new TableModelListener() {

                    public void tableChanged(TableModelEvent e) {
                        valuesChanged = true;
                    }
                });

        mandatoryFieldsCheck();

        setLocation(location);
        setVisible(true);

        //makes it possible to run the program within netbeans
        if (prideConverter.useHardcodedPaths()) {
            instrumentPath =
                    "D:/PRIDE_ms_lims/ms_lims_to_PRIDE/PRIDEConverter/Release/Properties/Instruments/";
        } else {
            instrumentPath = "" +
                    this.getClass().getProtectionDomain().getCodeSource().getLocation();
            instrumentPath = instrumentPath.substring(5, instrumentPath.lastIndexOf("/"));
            instrumentPath = instrumentPath.substring(0, instrumentPath.lastIndexOf("/") + 1) 
                    + "Properties/Instruments/";
            instrumentPath = instrumentPath.replace("%20", " ");
        }

        // for mzXML, mzData and TPP information about the instrument is extraced from the file
        if ((prideConverter.getProperties().getDataSource().equalsIgnoreCase("mzXML") 
                || prideConverter.getProperties().getDataSource().equalsIgnoreCase("mzData") 
                || prideConverter.getProperties().getDataSource().equalsIgnoreCase("TPP")) &&
                !prideConverter.getProperties().areInstrumentDetailsExtracted()) {
            extractInstrumentDetailsFromFile();
        } else {
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            readInstrumentsFromFile();
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Tries to extract instrument details from an mzXML or an mzData file.
     */
    private void extractInstrumentDetailsFromFile() {

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        tempProcessingMethods = new Vector();

        while (processingMethodsJTable.getRowCount() > 0) {
            ((DefaultTableModel) processingMethodsJTable.getModel()).removeRow(0);
        }

        while (analyzerJTable.getRowCount() > 0) {
            ((DefaultTableModel) analyzerJTable.getModel()).removeRow(0);
        }

        MSXMLParser msXMLParser = null;
        String instrumentName;
        String fileName;

        if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("mzXML") ||
                prideConverter.getProperties().getDataSource().equalsIgnoreCase("TPP")) {

            if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("mzXML")) {
                fileName = prideConverter.getProperties().getSelectedSourceFiles().get(0);
            } else {
                File[] mzXmlFiles =
                        new File(prideConverter.getProperties().getTppMzDataFilesFolderName()).listFiles();

                boolean mzXmlFileFound = false;
                fileName = null;

                for (int i = 0; i < mzXmlFiles.length && !mzXmlFileFound; i++) {
                    if (mzXmlFiles[i].getName().endsWith(".mzXML") ||
                            mzXmlFiles[i].getName().endsWith(".MZXML")) {
                        mzXmlFileFound = true;
                        fileName = mzXmlFiles[i].getPath();
                    }
                }
            }

            msXMLParser = new MSXMLParser(fileName);

            instrumentName = msXMLParser.getHeaderInfo().
                    getInstrumentInfo().getManufacturer() + " " +
                    msXMLParser.getHeaderInfo().getInstrumentInfo().getModel();

        } else { //mzData
            instrumentName = prideConverter.getProperties().getMzDataFile().getInstrumentName();
        }

        String newName =
                instrumentPath +
                instrumentName +
                ".int";

        boolean useExistingInstrument = true;

        // check if the instrument already exists
        if (new File(newName).exists()) {

            int option =
                    JOptionPane.showConfirmDialog(this,
                    "The instrument name \'" +
                    instrumentName +
                    "\' is already in use.\nDo you want to use the existing instrument?",
                    "Use Existing Instrument?",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.NO_OPTION) {

                useExistingInstrument = false;

                option =
                        JOptionPane.showConfirmDialog(this,
                        "Do you want to overwrite the existing instrument?",
                        "Overwrite Existing Instrument?",
                        JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.NO_OPTION) {

                    instrumentName = JOptionPane.showInputDialog(this, 
                            "Please provide the name of the new instrument:",
                            instrumentName);

                    while (instrumentName == null) {
                        instrumentName = JOptionPane.showInputDialog(this, "Please provide a valid name:",
                                instrumentName);
                    }

                    newName = instrumentPath + instrumentName + ".int";

                    while (new File(newName).exists()) {
                        instrumentName = JOptionPane.showInputDialog(this, 
                                "This name is also in use. Please choose a different name: ",
                                instrumentName);

                        while (instrumentName == null) {
                            instrumentName = JOptionPane.showInputDialog(this, "Please provide a valid name:",
                                    instrumentName);
                        }

                        newName = instrumentPath + instrumentName + ".int";
                    }

                    saveInstrument(instrumentName);
                }
            }
        } else {
            saveInstrument(instrumentName);
        }

        prideConverter.getUserProperties().setCurrentSelectedInstrument(instrumentName);
        readInstrumentsFromFile();

        if (!useExistingInstrument) {

            tempProcessingMethods = new Vector();

            while (processingMethodsJTable.getRowCount() > 0) {
                ((DefaultTableModel) processingMethodsJTable.getModel()).removeRow(0);
            }

            while (analyzerJTable.getRowCount() > 0) {
                ((DefaultTableModel) analyzerJTable.getModel()).removeRow(0);
            }

            if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("mzXML") ||
                    prideConverter.getProperties().getDataSource().equalsIgnoreCase("TPP")) {

                //add or updated the properties
                if (msXMLParser.getHeaderInfo().getDataProcessing().getCentroided() ==
                        msXMLParser.getHeaderInfo().getDataProcessing().YES) {
                    addProcessingMethod("PeakProcessing", "PSI:1000035", "PSI",
                            "CentroidMassSpectrum", -1);
                } else if (msXMLParser.getHeaderInfo().getDataProcessing().
                        getCentroided() ==
                        msXMLParser.getHeaderInfo().getDataProcessing().NO) {

                } else {
                //unknown
                }

                if (msXMLParser.getHeaderInfo().getDataProcessing().
                        getChargeDeconvoluted() ==
                        msXMLParser.getHeaderInfo().getDataProcessing().YES) {
                    addProcessingMethod("ChargeDeconvolution", "PSI:1000034", "PSI",
                            "true", -1);
                } else if (msXMLParser.getHeaderInfo().getDataProcessing().
                        getChargeDeconvoluted() ==
                        msXMLParser.getHeaderInfo().getDataProcessing().NO) {
                    addProcessingMethod("ChargeDeconvolution", "PSI:1000034", "PSI",
                            "false", -1);
                } else {
                //unknown
                }

                if (msXMLParser.getHeaderInfo().getDataProcessing().getDeisotoped() ==
                        msXMLParser.getHeaderInfo().getDataProcessing().YES) {
                    addProcessingMethod("Deisotoping", "PSI:1000033", "PSI", "true",
                            -1);
                } else if (msXMLParser.getHeaderInfo().getDataProcessing().
                        getDeisotoped() ==
                        msXMLParser.getHeaderInfo().getDataProcessing().NO) {
                    addProcessingMethod("Deisotoping", "PSI:1000033", "PSI", "false",
                            -1);
                } else {
                //unknown
                }

                softwareNameJTextField.setText(msXMLParser.getHeaderInfo().
                        getInstrumentInfo().getSoftwareInfo().name);
                softwareVersionJTextField.setText(msXMLParser.getHeaderInfo().
                        getInstrumentInfo().getSoftwareInfo().version);

                JOptionPane.showMessageDialog(this, "Some of the instrument details have been " +
                        "extracted directly from the data file(s). \n" +
                        "Please verify that the information is correct and provide any missing " +
                        "information.", "Instrument Properties Extracted",
                        JOptionPane.INFORMATION_MESSAGE);

            } else { // mzData

                if (prideConverter.getProperties().getMzDataFile().getProcessingMethodCvParams() != null) {
                    Iterator<CvParam> iterator =
                            prideConverter.getProperties().getMzDataFile().getProcessingMethodCvParams().iterator();

                    while (iterator.hasNext()) {

                        CvParam tempCvTerm = iterator.next();

                        addProcessingMethod(tempCvTerm.getName(),
                                tempCvTerm.getAccession(),
                                tempCvTerm.getCVLookup(),
                                tempCvTerm.getValue(), -1);
                    }
                }

                softwareNameJTextField.setText(
                        prideConverter.getProperties().getMzDataFile().getSoftwareName());
                softwareVersionJTextField.setText(
                        prideConverter.getProperties().getMzDataFile().getSoftwareVersion());
            }

            CvParamImpl tempCVTerm;

            if (prideConverter.getProperties().getDataSource().equalsIgnoreCase("mzXML") ||
                    prideConverter.getProperties().getDataSource().equalsIgnoreCase("TPP")) {
                //instrument source
                if (prideConverter.getUserProperties().getCVTermMappings().
                        containsKey(
                        msXMLParser.getHeaderInfo().getInstrumentInfo().getIonization())) {

                    tempCVTerm = (CvParamImpl) prideConverter.getUserProperties().
                            getCVTermMappings().get(
                            msXMLParser.getHeaderInfo().getInstrumentInfo().getIonization());

                    setInstrumentSource(tempCVTerm.getName(),
                            tempCVTerm.getAccession(), tempCVTerm.getCVLookup());
                } else {
                    JOptionPane.showMessageDialog(this, "Please use OLS to map the instruments source \'" +
                            msXMLParser.getHeaderInfo().getInstrumentInfo().getIonization() +
                            "\' to the correct CV term.", "CV Term Mapping",
                            JOptionPane.INFORMATION_MESSAGE);

                    this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                    new OLSDialog(this, true, "instrumentSource",
                            "Mass Spectroscopy CV (PSI-MS) [PSI]",
                            msXMLParser.getHeaderInfo().getInstrumentInfo().getIonization());
                }

                //instrument detector
                if (prideConverter.getUserProperties().getCVTermMappings().
                        containsKey(
                        msXMLParser.getHeaderInfo().getInstrumentInfo().getDetector())) {

                    tempCVTerm = (CvParamImpl) prideConverter.getUserProperties().
                            getCVTermMappings().get(
                            msXMLParser.getHeaderInfo().getInstrumentInfo().getDetector());

                    setInstrumentDetector(tempCVTerm.getName(),
                            tempCVTerm.getAccession(), tempCVTerm.getCVLookup());
                } else {
                    JOptionPane.showMessageDialog(this, "Please use OLS to map the instruments detector \'" +
                            msXMLParser.getHeaderInfo().getInstrumentInfo().getDetector() +
                            "\' to the correct CV term.", "CV Term Mapping",
                            JOptionPane.INFORMATION_MESSAGE);

                    this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                    new OLSDialog(this, true, "instrumentDetector",
                            "Mass Spectroscopy CV (PSI-MS) [PSI]",
                            msXMLParser.getHeaderInfo().getInstrumentInfo().getDetector());
                }

                //analyzer
                if (prideConverter.getUserProperties().getCVTermMappings().
                        containsKey(
                        msXMLParser.getHeaderInfo().getInstrumentInfo().getMassAnalyzer())) {

                    prideConverter.getProperties().setAnalyzerList(new ArrayList());

                    tempCVTerm = (CvParamImpl) prideConverter.getUserProperties().
                            getCVTermMappings().get(
                            msXMLParser.getHeaderInfo().getInstrumentInfo().getMassAnalyzer());

                    Vector names = new Vector();
                    names.add(tempCVTerm.getName());
                    Vector accessions = new Vector();
                    accessions.add(tempCVTerm.getAccession());
                    Vector ontologies = new Vector();
                    ontologies.add(tempCVTerm.getCVLookup());
                    Vector values = new Vector();
                    values.add(tempCVTerm.getValue());

                    insertAnalyzer(names, accessions, ontologies, values, -1);
                } else {

                    prideConverter.getProperties().setAnalyzerList(new ArrayList());

                    JOptionPane.showMessageDialog(this, "Please use OLS to map the mass analyzer \'" +
                            msXMLParser.getHeaderInfo().getInstrumentInfo().getMassAnalyzer() +
                            "\' to the correct CV term.", "CV Term Mapping",
                            JOptionPane.INFORMATION_MESSAGE);

                    this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                    new OLSDialog(this, true, "analyzer",
                            "Mass Spectroscopy CV (PSI-MS) [PSI]",
                            msXMLParser.getHeaderInfo().getInstrumentInfo().
                            getMassAnalyzer());
                }
            } else { // mzData
                //instrument source
                if (prideConverter.getProperties().getMzDataFile().getInstrumentSourceCvParams() != null) {
                    tempCVTerm =
                            (CvParamImpl) prideConverter.getProperties().getMzDataFile().getInstrumentSourceCvParams().iterator().next();

                    setInstrumentSource(tempCVTerm.getName(),
                            tempCVTerm.getAccession(), tempCVTerm.getCVLookup());
                }

                //instrument detector
                if (prideConverter.getProperties().getMzDataFile().getInstrumentDetectorCvParams() != null) {
                    tempCVTerm =
                            (CvParamImpl) prideConverter.getProperties().getMzDataFile().getInstrumentDetectorCvParams().iterator().next();

                    setInstrumentDetector(tempCVTerm.getName(),
                            tempCVTerm.getAccession(), tempCVTerm.getCVLookup());
                }

                //analyzer
                if (prideConverter.getProperties().getMzDataFile().getAnalyzerListCollection() != null) {
                    Iterator<Analyzer> iteratorAnalyzers =
                            prideConverter.getProperties().getMzDataFile().getAnalyzerListCollection().iterator();

                    Vector terms = new Vector();
                    Vector accessions = new Vector();
                    Vector ontologies = new Vector();
                    Vector values = new Vector();

                    while (iteratorAnalyzers.hasNext()) {

                        terms = new Vector();
                        accessions = new Vector();
                        ontologies = new Vector();
                        values = new Vector();

                        Analyzer tempAnalyzer = iteratorAnalyzers.next();

                        Iterator<CvParam> tempCvParams = tempAnalyzer.getAnalyzerCvParameterList().iterator();

                        while (tempCvParams.hasNext()) {

                            CvParam tempCvTerm = tempCvParams.next();

                            terms.add(tempCvTerm.getName());
                            accessions.add(tempCvTerm.getAccession());
                            ontologies.add(tempCvTerm.getCVLookup());
                            values.add(tempCvTerm.getValue());
                        }

                        insertAnalyzer(terms, accessions, ontologies, values, -1);
                    }
                }

                JOptionPane.showMessageDialog(this, "Some of the instrument details have been " +
                        "extracted directly from the data file(s). \n" +
                        "Please verify that the information is correct and provide any missing " +
                        "information.", "Instrument Properties Extracted",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            saveInstrument(instrumentName);
            prideConverter.getUserProperties().setCurrentSelectedInstrument(instrumentName);
            readInstrumentsFromFile();
        }

        prideConverter.getProperties().setInstrumentDetailsExtracted(true);

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    /**
     * Reads all instruments from file, inserts the names into the combo box 
     * and updatse the instrument details to the selected instrument.
     */
    private void readInstrumentsFromFile() {

        File file = new File(instrumentPath);

        try {
            if (!file.exists()) {
                file.mkdir();
            }

            File[] instrumentFiles = file.listFiles();

            FileReader f = null;
            BufferedReader b = null;
            String tempInstrumentName;
            Vector instrumentNames = new Vector();

            for (int i = 0; i < instrumentFiles.length; i++) {

                if (instrumentFiles[i].getAbsolutePath().endsWith(".int")) {

                    f = new FileReader(instrumentFiles[i]);
                    b = new BufferedReader(f);

                    b.readLine();
                    tempInstrumentName = b.readLine();
                    tempInstrumentName = tempInstrumentName.substring(tempInstrumentName.indexOf(": ") + 2);
                    instrumentNames.add(tempInstrumentName);

                    b.close();
                    f.close();
                }
            }

            java.util.Collections.sort(instrumentNames);

            Vector comboboxTooltips = new Vector();
            comboboxTooltips.add(null);

            for (int i = 0; i < instrumentNames.size(); i++) {
                comboboxTooltips.add(instrumentNames.get(i));
            }

            comboboxTooltips.add(null);

            instrumentNames.insertElementAt("- Please select an instrument -", 0);
            instrumentNames.add("   Create a new instrument...");

            instrumentNameJComboBox.setRenderer(new MyComboBoxRenderer(comboboxTooltips));
            instrumentNameJComboBox.setModel(new DefaultComboBoxModel(instrumentNames));
            instrumentNameJComboBox.setSelectedItem(prideConverter.getUserProperties().
                    getCurrentSelectedInstrument());
            lastSelectedInstrumentName = prideConverter.getUserProperties().
                    getCurrentSelectedInstrument();

            instrumentNameJComboBoxActionPerformed(null);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Checks if all mandatory information is filled in and enables or disables 
     * the Next button.
     */
    private void mandatoryFieldsCheck() {
        if (instrumentDetectorJTextField.getText().length() > 0 &&
                instrumentSourceJTextField.getText().length() > 0 &&
                analyzerJTable.getRowCount() > 0 &&
                processingMethodsJTable.getRowCount() > 0 &&
                softwareNameJTextField.getText().length() > 0 &&
                softwareVersionJTextField.getText().length() > 0) {
            nextJButton.setEnabled(true);
        } else {
            nextJButton.setEnabled(false);
        }

        if (instrumentNameJComboBox.getSelectedIndex() != 0 &&
                instrumentNameJComboBox.getSelectedIndex() !=
                instrumentNameJComboBox.getItemCount() - 1 &&
                instrumentNameJComboBox.getSelectedIndex() != -1) {
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

        processingMethodsPopupJMenu = new javax.swing.JPopupMenu();
        processingMethodsEditJMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        processingMethodsMoveUpJMenuItem = new javax.swing.JMenuItem();
        processingMethodsMoveDownJMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        processingMethodsDeleteSelectedRowJMenuItem = new javax.swing.JMenuItem();
        analyzersPopupJMenu = new javax.swing.JPopupMenu();
        analyzersEditJMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        analyzersMoveUpJMenuItem = new javax.swing.JMenuItem();
        analyzersMoveDownJMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        analyzersDeleteRowJMenuItem = new javax.swing.JMenuItem();
        popupMenu = new javax.swing.JPopupMenu();
        deleteJMenuItem = new javax.swing.JMenuItem();
        nextJButton = new javax.swing.JButton();
        backJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        processingMethodsJTable = new javax.swing.JTable();
        processingMethodsJButton = new javax.swing.JButton();
        jLabel43 = new javax.swing.JLabel();
        softwareNameJTextField = new javax.swing.JTextField();
        jLabel41 = new javax.swing.JLabel();
        softwareVersionJTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        instrumentSourceJTextField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        instrumentDetectorJButton = new javax.swing.JButton();
        instrumentSourceJButton = new javax.swing.JButton();
        instrumentDetectorJTextField = new javax.swing.JTextField();
        analyzerJButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        analyzerJTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        instrumentNameJComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        deleteJButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        helpJButton = new javax.swing.JButton();
        aboutJButton = new javax.swing.JButton();

        processingMethodsEditJMenuItem.setMnemonic('E');
        processingMethodsEditJMenuItem.setText("Edit");
        processingMethodsEditJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processingMethodsEditJMenuItemActionPerformed(evt);
            }
        });
        processingMethodsPopupJMenu.add(processingMethodsEditJMenuItem);
        processingMethodsPopupJMenu.add(jSeparator3);

        processingMethodsMoveUpJMenuItem.setMnemonic('U');
        processingMethodsMoveUpJMenuItem.setText("Move Up");
        processingMethodsMoveUpJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processingMethodsMoveUpJMenuItemActionPerformed(evt);
            }
        });
        processingMethodsPopupJMenu.add(processingMethodsMoveUpJMenuItem);

        processingMethodsMoveDownJMenuItem.setMnemonic('D');
        processingMethodsMoveDownJMenuItem.setText("Move Down");
        processingMethodsMoveDownJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processingMethodsMoveDownJMenuItemActionPerformed(evt);
            }
        });
        processingMethodsPopupJMenu.add(processingMethodsMoveDownJMenuItem);
        processingMethodsPopupJMenu.add(jSeparator4);

        processingMethodsDeleteSelectedRowJMenuItem.setText("Delete");
        processingMethodsDeleteSelectedRowJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processingMethodsDeleteSelectedRowJMenuItemActionPerformed(evt);
            }
        });
        processingMethodsPopupJMenu.add(processingMethodsDeleteSelectedRowJMenuItem);

        analyzersEditJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        analyzersEditJMenuItem.setMnemonic('E');
        analyzersEditJMenuItem.setText("Edit");
        analyzersEditJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                analyzersEditJMenuItemActionPerformed(evt);
            }
        });
        analyzersPopupJMenu.add(analyzersEditJMenuItem);
        analyzersPopupJMenu.add(jSeparator5);

        analyzersMoveUpJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.CTRL_MASK));
        analyzersMoveUpJMenuItem.setMnemonic('U');
        analyzersMoveUpJMenuItem.setText("Move Up");
        analyzersMoveUpJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                analyzersMoveUpJMenuItemActionPerformed(evt);
            }
        });
        analyzersPopupJMenu.add(analyzersMoveUpJMenuItem);

        analyzersMoveDownJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        analyzersMoveDownJMenuItem.setMnemonic('D');
        analyzersMoveDownJMenuItem.setText("Move Down");
        analyzersMoveDownJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                analyzersMoveDownJMenuItemActionPerformed(evt);
            }
        });
        analyzersPopupJMenu.add(analyzersMoveDownJMenuItem);
        analyzersPopupJMenu.add(jSeparator6);

        analyzersDeleteRowJMenuItem.setMnemonic('L');
        analyzersDeleteRowJMenuItem.setText("Delete");
        analyzersDeleteRowJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                analyzersDeleteRowJMenuItemActionPerformed(evt);
            }
        });
        analyzersPopupJMenu.add(analyzersDeleteRowJMenuItem);

        deleteJMenuItem.setText("Delete Instrument");
        deleteJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteJMenuItemActionPerformed(evt);
            }
        });
        popupMenu.add(deleteJMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Instrument Properties - Step 6 of 8");
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

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Processing", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));

        processingMethodsJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "CV Terms", "Value"
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
        processingMethodsJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                processingMethodsJTableMouseClicked(evt);
            }
        });
        processingMethodsJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                processingMethodsJTableKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(processingMethodsJTable);

        processingMethodsJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/ols_transparent.GIF"))); // NOI18N
        processingMethodsJButton.setText("Add Processing Method");
        processingMethodsJButton.setEnabled(false);
        processingMethodsJButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        processingMethodsJButton.setPreferredSize(new java.awt.Dimension(177, 23));
        processingMethodsJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processingMethodsJButtonActionPerformed(evt);
            }
        });

        jLabel43.setText("Software Name:");

        softwareNameJTextField.setEditable(false);
        softwareNameJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        softwareNameJTextField.setEnabled(false);
        softwareNameJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                softwareNameJTextFieldKeyReleased(evt);
            }
        });

        jLabel41.setText("Software Version:");

        softwareVersionJTextField.setEditable(false);
        softwareVersionJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        softwareVersionJTextField.setEnabled(false);
        softwareVersionJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                softwareVersionJTextFieldKeyReleased(evt);
            }
        });

        jLabel2.setText("Processing Methods:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel43)
                        .addGap(26, 26, 26)
                        .addComponent(softwareNameJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                        .addComponent(jLabel41)
                        .addGap(18, 18, 18)
                        .addComponent(softwareVersionJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(processingMethodsJButton, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                    .addComponent(jLabel2)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel43)
                    .addComponent(jLabel41)
                    .addComponent(softwareNameJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(softwareVersionJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(processingMethodsJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Instrument", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));

        jLabel9.setText("Source:");

        instrumentSourceJTextField.setEditable(false);
        instrumentSourceJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel12.setText("Detector:");

        instrumentDetectorJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/ols_transparent.GIF"))); // NOI18N
        instrumentDetectorJButton.setToolTipText("Ontology Lookup Service");
        instrumentDetectorJButton.setEnabled(false);
        instrumentDetectorJButton.setPreferredSize(new java.awt.Dimension(61, 23));
        instrumentDetectorJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                instrumentDetectorJButtonActionPerformed(evt);
            }
        });

        instrumentSourceJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/ols_transparent.GIF"))); // NOI18N
        instrumentSourceJButton.setToolTipText("Ontology Lookup Service");
        instrumentSourceJButton.setEnabled(false);
        instrumentSourceJButton.setPreferredSize(new java.awt.Dimension(61, 23));
        instrumentSourceJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                instrumentSourceJButtonActionPerformed(evt);
            }
        });

        instrumentDetectorJTextField.setEditable(false);
        instrumentDetectorJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        analyzerJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/prideconverter/icons/ols_transparent.GIF"))); // NOI18N
        analyzerJButton.setText("Add Analyzer");
        analyzerJButton.setEnabled(false);
        analyzerJButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        analyzerJButton.setPreferredSize(new java.awt.Dimension(129, 23));
        analyzerJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                analyzerJButtonActionPerformed(evt);
            }
        });

        analyzerJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "CV Terms", "#Terms"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class
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
        analyzerJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                analyzerJTableMouseClicked(evt);
            }
        });
        analyzerJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                analyzerJTableKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(analyzerJTable);

        jLabel1.setText("Analyzers:");

        instrumentNameJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "- Please select an instrument -", "Bruker Ultraflex", "Electrospray", "   Create a new instrument..." }));
        instrumentNameJComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                instrumentNameJComboBoxMouseClicked(evt);
            }
        });
        instrumentNameJComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                instrumentNameJComboBoxItemStateChanged(evt);
            }
        });
        instrumentNameJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                instrumentNameJComboBoxActionPerformed(evt);
            }
        });

        jLabel3.setText("Name:");

        deleteJButton.setFont(new java.awt.Font("Arial", 1, 11));
        deleteJButton.setText("X");
        deleteJButton.setToolTipText("Delete Selected Instrument");
        deleteJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteJButton.setIconTextGap(0);
        deleteJButton.setMargin(new java.awt.Insets(1, 6, 1, 6));
        deleteJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(analyzerJButton, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                    .addComponent(jLabel1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel12)
                            .addComponent(jLabel3))
                        .addGap(9, 9, 9)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(instrumentNameJComboBox, 0, 440, Short.MAX_VALUE)
                                .addGap(6, 6, 6)
                                .addComponent(deleteJButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(instrumentDetectorJTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(instrumentDetectorJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(instrumentSourceJTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(instrumentSourceJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(deleteJButton)
                    .addComponent(instrumentNameJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(instrumentSourceJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(instrumentSourceJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel12)
                        .addComponent(instrumentDetectorJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(instrumentDetectorJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(analyzerJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11))
        );

        jLabel4.setFont(new java.awt.Font("Tahoma", 2, 11));
        jLabel4.setText("Select an instrument from the list, or create your own, and click on 'Next' to continue.");

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
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
     * If the user double clicks on a row in the processing methods table the  
     * OLS dialog is shown where the processing method can be 
     * altered. If the user right clicks a pop up menu is shown for editing, 
     * moving or delting the selected processing method.
     * 
     * @param evt
     */
    private void processingMethodsJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_processingMethodsJTableMouseClicked
        if (evt.getButton() == 3) {

            int row = processingMethodsJTable.rowAtPoint(evt.getPoint());
            int column = processingMethodsJTable.columnAtPoint(evt.getPoint());

            processingMethodsJTable.changeSelection(row, column, false, false);

            //makes sure that only valid "moving options" are enabled
            this.processingMethodsMoveUpJMenuItem.setEnabled(true);
            this.processingMethodsMoveDownJMenuItem.setEnabled(true);

            if (row == processingMethodsJTable.getRowCount() - 1) {
                this.processingMethodsMoveDownJMenuItem.setEnabled(false);
            }

            if (row == 0) {
                this.processingMethodsMoveUpJMenuItem.setEnabled(false);
            }

            processingMethodsPopupJMenu.show(evt.getComponent(), evt.getX(),
                    evt.getY());
        } else if (evt.getButton() == 1 && evt.getClickCount() == 2) {
            processingMethodsEditJMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_processingMethodsJTableMouseClicked

    /**
     * Deletes the selected row in the processing methods table.
     * 
     * @param evt
     */
    private void processingMethodsDeleteSelectedRowJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processingMethodsDeleteSelectedRowJMenuItemActionPerformed

        int selectedRow = processingMethodsJTable.getSelectedRow();

        if (selectedRow != -1) {
            ((DefaultTableModel) processingMethodsJTable.getModel()).removeRow(selectedRow);

            //remove from datastructure as well
            Object[] processingMethods = tempProcessingMethods.toArray();

            tempProcessingMethods = new Vector();

            CvParamImpl tempCvParam;

            int counter = 0;

            for (int i = 0; i <
                    processingMethods.length; i++) {

                if (i != selectedRow) {
                    tempCvParam = (CvParamImpl) processingMethods[i];
                    tempCvParam =
                            new CvParamImpl(tempCvParam.getAccession(),
                            tempCvParam.getCVLookup(),
                            tempCvParam.getName(),
                            new Long(counter++),
                            tempCvParam.getValue());

                    tempProcessingMethods.add(tempCvParam);
                }
            }

            fixProcessingMethodsIndices();
            mandatoryFieldsCheck();
            valuesChanged = true;
        }
}//GEN-LAST:event_processingMethodsDeleteSelectedRowJMenuItemActionPerformed

    /**
     * If the information as been altered, the option to store the data will 
     * be given. Then the frame of the next step (UserParameters) is opened.
     * 
     * @param evt
     */
    private void nextJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextJButtonActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        prideConverter.getUserProperties().setCurrentSelectedInstrument(
                (String) instrumentNameJComboBox.getSelectedItem());

        boolean cancel = false;

        if (valuesChanged) {
            int value = JOptionPane.showConfirmDialog(this,
                    "The instrument has been changed. Do you want to save this for later use?",
                    "Instrument Changed", JOptionPane.YES_NO_CANCEL_OPTION);

            if (value == JOptionPane.YES_OPTION) {

                value = JOptionPane.showConfirmDialog(this,
                        "Overwrite existing instrument?",
                        "Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);

                if (value == JOptionPane.YES_OPTION) {
                    saveInstrument(prideConverter.getUserProperties().
                            getCurrentSelectedInstrument());
                } else if (value == JOptionPane.CANCEL_OPTION) {
                    cancel = true;
                } else { //value == NO

                    String instrumentName = JOptionPane.showInputDialog(this,
                            "Please provide the name of the new instrument: ",
                            "Instrument Name", JOptionPane.PLAIN_MESSAGE);

                    if (instrumentName != null) {

                        String newName;

                        newName = instrumentPath + instrumentName + ".int";

                        while (new File(newName).exists()) {
                            instrumentName =
                                    JOptionPane.showInputDialog(this,
                                    "Please provide the name of the new instrument: ",
                                    "Instrument Name", JOptionPane.PLAIN_MESSAGE);

                            newName = instrumentPath + instrumentName + ".int";
                        }

                        if (instrumentName != null) {
                            saveInstrument(instrumentName);
                            prideConverter.getUserProperties().
                                    setCurrentSelectedInstrument(instrumentName);
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
        if (!cancel) {
            prideConverter.getProperties().setSoftwareName(softwareNameJTextField.getText());
            prideConverter.getProperties().setSoftwareVersion(softwareVersionJTextField.getText());
            prideConverter.getProperties().setProcessingMethod(
                    new ArrayList(processingMethodsJTable.getRowCount()));

            CvParamImpl temp;

            for (int i = 0; i < processingMethodsJTable.getRowCount(); i++) {

                String value = null;

                if (processingMethodsJTable.getValueAt(i, 2) != null) {
                    value = (String) processingMethodsJTable.getValueAt(i, 2);
                }

                temp = (CvParamImpl) tempProcessingMethods.get(i);

                prideConverter.getProperties().getProcessingMethod().add(new CvParamImpl(
                        temp.getAccession(),
                        temp.getCVLookup(),
                        temp.getName(),
                        new Long(i),
                        value));
            }

            new UserParameters(prideConverter, this.getLocation());

            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

            this.setVisible(false);
            this.dispose();
        } else {
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }
    }//GEN-LAST:event_nextJButtonActionPerformed

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
     * If the information as been altered, the option to store the data will 
     * be given. Then the frame of the previous step (ProtocolDetails) is opened.
     * 
     * @param evt
     */
    private void backJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backJButtonActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        prideConverter.getUserProperties().setCurrentSelectedInstrument((String) instrumentNameJComboBox.getSelectedItem());

        boolean cancel = false;

        if (valuesChanged) {
            int value = JOptionPane.showConfirmDialog(this,
                    "The instrument has been changed. Do you want to save this for later use?",
                    "Instrument Changed", JOptionPane.YES_NO_CANCEL_OPTION);

            if (value == JOptionPane.YES_OPTION) {

                value = JOptionPane.showConfirmDialog(this,
                        "Overwrite existing instrument?",
                        "Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);

                if (value == JOptionPane.YES_OPTION) {
                    saveInstrument(prideConverter.getUserProperties().
                            getCurrentSelectedInstrument());
                } else if (value == JOptionPane.CANCEL_OPTION) {
                    cancel = true;
                } else { //value == NO

                    String instrumentName = JOptionPane.showInputDialog(this,
                            "Please provide the name of the new instrument: ",
                            "Instrument Name", JOptionPane.PLAIN_MESSAGE);

                    if (instrumentName != null) {

                        String newName;

                        newName = instrumentPath + instrumentName + ".int";

                        while (new File(newName).exists()) {
                            instrumentName =
                                    JOptionPane.showInputDialog(this,
                                    "Please provide the name of the new instrument: ",
                                    "Instrument Name", JOptionPane.PLAIN_MESSAGE);

                            newName = instrumentPath + instrumentName + ".int";
                        }

                        if (instrumentName != null) {
                            saveInstrument(instrumentName);
                            prideConverter.getUserProperties().
                                    setCurrentSelectedInstrument(instrumentName);
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
        if (!cancel) {

            prideConverter.getProperties().setSoftwareName(softwareNameJTextField.getText());
            prideConverter.getProperties().setSoftwareVersion(softwareVersionJTextField.getText());
            prideConverter.getProperties().setProcessingMethod(new ArrayList(processingMethodsJTable.getRowCount()));

            CvParamImpl temp;

            for (int i = 0; i <
                    processingMethodsJTable.getRowCount(); i++) {

                String value = null;

                if (processingMethodsJTable.getValueAt(i, 2) != null) {
                    value = (String) processingMethodsJTable.getValueAt(i, 2);
                }

                temp = (CvParamImpl) tempProcessingMethods.get(i);

                prideConverter.getProperties().getProcessingMethod().add(new CvParamImpl(
                        temp.getAccession(),
                        temp.getCVLookup(),
                        temp.getName(),
                        new Long(i),
                        value));
            }

            new ProtocolDetails(prideConverter, this.getLocation());

            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

            this.setVisible(false);
            this.dispose();
        } else {
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }
    }//GEN-LAST:event_backJButtonActionPerformed

    /**
     * Opens the OLS dialog.
     * 
     * @param evt
     */
    private void instrumentSourceJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_instrumentSourceJButtonActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new OLSDialog(this, true, "instrumentSource", prideConverter.getUserProperties().
                getLastSelectedOntology(), null);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_instrumentSourceJButtonActionPerformed

    /**
     * Opens the OLS dialog.
     * 
     * @param evt
     */
    private void instrumentDetectorJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_instrumentDetectorJButtonActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new OLSDialog(this, true, "instrumentDetector", prideConverter.getUserProperties().
                getLastSelectedOntology(), null);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_instrumentDetectorJButtonActionPerformed

    /**
     * Opens a NewAnlyzer dialog.
     * 
     * @param evt
     */
    private void analyzerJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_analyzerJButtonActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new NewAnalyzer(this, true, prideConverter);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_analyzerJButtonActionPerformed

    /**
     * Opens the OLS dialog.
     * 
     * @param evt
     */
    private void processingMethodsJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processingMethodsJButtonActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new OLSDialog(this, true, "processingMethods", prideConverter.getUserProperties().
                getLastSelectedOntology(), null);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_processingMethodsJButtonActionPerformed

    /**
     * Opens the OLS dialog.
     * 
     * @param evt
     */
    private void processingMethodsEditJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processingMethodsEditJMenuItemActionPerformed
        int selectedRow = processingMethodsJTable.getSelectedRow();

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new OLSDialog(this, true, "processingMethods", prideConverter.getUserProperties().
                getLastSelectedOntology(), selectedRow, null);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_processingMethodsEditJMenuItemActionPerformed

    /**
     * Moves the selected row in the processing methods table up one position.
     * 
     * @param evt
     */
    private void processingMethodsMoveUpJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processingMethodsMoveUpJMenuItemActionPerformed
        int selectedRow = processingMethodsJTable.getSelectedRow();
        int selectedColumn = processingMethodsJTable.getSelectedColumn();

        Object[] tempRow = new Object[]{
            processingMethodsJTable.getValueAt(selectedRow - 1, 0),
            processingMethodsJTable.getValueAt(selectedRow - 1, 1),
            processingMethodsJTable.getValueAt(selectedRow - 1, 2)
        };

        ((DefaultTableModel) processingMethodsJTable.getModel()).removeRow(selectedRow - 1);
        ((DefaultTableModel) processingMethodsJTable.getModel()).insertRow(selectedRow, tempRow);
        processingMethodsJTable.changeSelection(selectedRow - 1, selectedColumn, false, false);


        CvParamImpl temp = (CvParamImpl) tempProcessingMethods.get(selectedRow - 1);
        tempProcessingMethods.setElementAt(tempProcessingMethods.get(selectedRow), selectedRow - 1);
        tempProcessingMethods.setElementAt(temp, selectedRow);

        valuesChanged = true;
        fixProcessingMethodsIndices();
}//GEN-LAST:event_processingMethodsMoveUpJMenuItemActionPerformed

    /**
     * Moves the selected row in the processing methods table down one position.
     * 
     * @param evt
     */
    private void processingMethodsMoveDownJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processingMethodsMoveDownJMenuItemActionPerformed
        int selectedRow = processingMethodsJTable.getSelectedRow();
        int selectedColumn = processingMethodsJTable.getSelectedColumn();

        Object[] tempRow = new Object[]{
            processingMethodsJTable.getValueAt(selectedRow + 1, 0),
            processingMethodsJTable.getValueAt(selectedRow + 1, 1),
            processingMethodsJTable.getValueAt(selectedRow + 1, 2)
        };

        ((DefaultTableModel) processingMethodsJTable.getModel()).removeRow(selectedRow + 1);
        ((DefaultTableModel) processingMethodsJTable.getModel()).insertRow(selectedRow, tempRow);
        processingMethodsJTable.changeSelection(selectedRow + 1, selectedColumn, false, false);

        CvParamImpl temp = (CvParamImpl) tempProcessingMethods.get(selectedRow + 1);
        tempProcessingMethods.setElementAt(tempProcessingMethods.get(selectedRow), selectedRow + 1);
        tempProcessingMethods.setElementAt(temp, selectedRow);

        valuesChanged = true;
        fixProcessingMethodsIndices();
}//GEN-LAST:event_processingMethodsMoveDownJMenuItemActionPerformed

    /**
     * Opens a NewAnalyzer dialog.
     * 
     * @param evt
     */
    private void analyzersEditJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_analyzersEditJMenuItemActionPerformed
        Object[] analyzers = prideConverter.getProperties().getAnalyzerList().toArray();

        new NewAnalyzer(this, true, prideConverter,
                analyzerJTable.getSelectedRow(),
                (AnalyzerImpl) analyzers[analyzerJTable.getSelectedRow()]);
}//GEN-LAST:event_analyzersEditJMenuItemActionPerformed

    /**
     * Moves the selected row in the analyzers table up one position.
     * 
     * @param evt
     */
    private void analyzersMoveUpJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_analyzersMoveUpJMenuItemActionPerformed
        int selectedRow = analyzerJTable.getSelectedRow();
        int selectedColumn = analyzerJTable.getSelectedColumn();

        Object[] tempRow = new Object[]{
            analyzerJTable.getValueAt(selectedRow - 1, 0),
            analyzerJTable.getValueAt(selectedRow - 1, 1),
            analyzerJTable.getValueAt(selectedRow - 1, 2)
        };

        ((DefaultTableModel) analyzerJTable.getModel()).removeRow(selectedRow - 1);
        ((DefaultTableModel) analyzerJTable.getModel()).insertRow(selectedRow, tempRow);

        analyzerJTable.changeSelection(selectedRow - 1, selectedColumn, false, false);

        valuesChanged = true;
        fixAnalyzersIndices();

        //move in data structure as well
        Object[] analyzers = prideConverter.getProperties().getAnalyzerList().toArray();

        Object tempAnalyzer = analyzers[selectedRow - 1];
        analyzers[selectedRow - 1] = analyzers[selectedRow];
        analyzers[selectedRow] = tempAnalyzer;

        prideConverter.getProperties().setAnalyzerList(new ArrayList());

        AnalyzerImpl tempAnalyzerImpl;

        for (int i = 0; i < analyzers.length; i++) {
            tempAnalyzerImpl = (AnalyzerImpl) analyzers[i];
            tempAnalyzerImpl =
                    new AnalyzerImpl(tempAnalyzerImpl.getAnalyzerCvParameterList(),
                    null, new Long(i));

            prideConverter.getProperties().getAnalyzerList().add(tempAnalyzerImpl);
        }
    }
//GEN-LAST:event_analyzersMoveUpJMenuItemActionPerformed


    /**
     * Moves the selected row in the analyzers table down one position.
     * 
     * @param evt
     */
    private void analyzersMoveDownJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_analyzersMoveDownJMenuItemActionPerformed
        int selectedRow = analyzerJTable.getSelectedRow();
        int selectedColumn = analyzerJTable.getSelectedColumn();

        Object[] tempRow = new Object[]{
            analyzerJTable.getValueAt(selectedRow + 1, 0),
            analyzerJTable.getValueAt(selectedRow + 1, 1),
            analyzerJTable.getValueAt(selectedRow + 1, 2)
        };

        ((DefaultTableModel) analyzerJTable.getModel()).removeRow(selectedRow + 1);
        ((DefaultTableModel) analyzerJTable.getModel()).insertRow(selectedRow, tempRow);

        analyzerJTable.changeSelection(selectedRow + 1, selectedColumn, false, false);

        valuesChanged = true;
        fixAnalyzersIndices();

        //move in data structure as well
        Object[] analyzers = prideConverter.getProperties().getAnalyzerList().toArray();

        Object tempAnalyzer = analyzers[selectedRow + 1];
        analyzers[selectedRow + 1] = analyzers[selectedRow];
        analyzers[selectedRow] = tempAnalyzer;

        prideConverter.getProperties().setAnalyzerList(new ArrayList());

        AnalyzerImpl tempAnalyzerImpl;

        for (int i = 0; i < analyzers.length; i++) {
            tempAnalyzerImpl = (AnalyzerImpl) analyzers[i];
            tempAnalyzerImpl =
                    new AnalyzerImpl(tempAnalyzerImpl.getAnalyzerCvParameterList(),
                    null, new Long(i));

            prideConverter.getProperties().getAnalyzerList().add(tempAnalyzerImpl);
        }
//GEN-LAST:event_analyzersMoveDownJMenuItemActionPerformed
    }

    /**
     * Deletes the selected row in the analyzers table.
     * 
     * @param evt
     */
    private void analyzersDeleteRowJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_analyzersDeleteRowJMenuItemActionPerformed
        if (analyzerJTable.getSelectedRow() != -1) {

            int selectedRow = analyzerJTable.getSelectedRow();
            int selectedColumn = analyzerJTable.getSelectedColumn();

            int[] selectedRows = analyzerJTable.getSelectedRows();

            for (int i = analyzerJTable.getSelectedRows().length - 1; i >= 0; i--) {
                ((DefaultTableModel) analyzerJTable.getModel()).removeRow(selectedRows[i]);
            }

            analyzerJTable.changeSelection(selectedRow, selectedColumn, false, false);
            analyzerJTable.editingCanceled(null);

            //remove from datastructure as well
            Object[] analyzers = prideConverter.getProperties().getAnalyzerList().toArray();

            prideConverter.getProperties().setAnalyzerList(new ArrayList());

            AnalyzerImpl tempAnalyzerImpl;

            int counter = 0;

            for (int i = 0; i < analyzers.length; i++) {

                if (i != selectedRow) {
                    tempAnalyzerImpl = (AnalyzerImpl) analyzers[i];
                    tempAnalyzerImpl =
                            new AnalyzerImpl(tempAnalyzerImpl.getAnalyzerCvParameterList(),
                            null, new Long(counter++));

                    prideConverter.getProperties().getAnalyzerList().add(tempAnalyzerImpl);
                }
            }

            valuesChanged = true;
            fixAnalyzersIndices();
            mandatoryFieldsCheck();
        }
    }
//GEN-LAST:event_analyzersDeleteRowJMenuItemActionPerformed
    /**
     * If the user double clicks on a row in the analyzers table the NewAnalyzer 
     * dialog is shown where the information about the given analyzer can be 
     * altered. If the user right clicks a pop up menu is shown for editing, 
     * moving or delting the selected analyzer.
     * 
     * @param evt
     */
    private void analyzerJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_analyzerJTableMouseClicked
        if (evt.getButton() == 3) {

            int row = analyzerJTable.rowAtPoint(evt.getPoint());
            int column = analyzerJTable.columnAtPoint(evt.getPoint());

            analyzerJTable.changeSelection(row, column, false, false);

            this.analyzersMoveUpJMenuItem.setEnabled(true);
            this.analyzersMoveDownJMenuItem.setEnabled(true);

            if (row == analyzerJTable.getRowCount() - 1) {
                this.analyzersMoveDownJMenuItem.setEnabled(false);
            }

            if (row == 0) {
                this.analyzersMoveUpJMenuItem.setEnabled(false);
            }

            analyzersPopupJMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        } else if (evt.getButton() == 1 && evt.getClickCount() == 2) {
            analyzersEditJMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_analyzerJTableMouseClicked

    /**
     * See mandatoryFieldsCheck
     * 
     * @param evt
     */
    private void softwareNameJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_softwareNameJTextFieldKeyReleased
        valuesChanged = true;
        mandatoryFieldsCheck();
    }//GEN-LAST:event_softwareNameJTextFieldKeyReleased

    /**
     * See mandatoryFieldsCheck
     * 
     * @param evt
     */
    private void softwareVersionJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_softwareVersionJTextFieldKeyReleased
        valuesChanged = true;
        mandatoryFieldsCheck();
    }//GEN-LAST:event_softwareVersionJTextFieldKeyReleased

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
        new HelpWindow(this,
                getClass().getResource("/no/uib/prideconverter/helpfiles/InstrumentDetails.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    /**
     * If the information as been altered, the option to store the data will 
     * be given. Then the information about the selected instrument will be 
     * retrieved.
     * 
     * @param evt
     */
    private void instrumentNameJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_instrumentNameJComboBoxActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        boolean cancel = false;

        if (valuesChanged) {

            int value = JOptionPane.showConfirmDialog(this,
                    "The instrument has been changed. Do you want to save this for later use?",
                    "Instrument Changed", JOptionPane.YES_NO_CANCEL_OPTION);

            if (value == JOptionPane.YES_OPTION) {

                value = JOptionPane.showConfirmDialog(this,
                        "Overwrite existing instrument?",
                        "Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);

                if (value == JOptionPane.YES_OPTION) {

                    try {
                        String newName;
                        newName =
                                instrumentPath + lastSelectedInstrumentName +
                                ".int";

                        FileWriter r = new FileWriter(newName);
                        BufferedWriter bw = new BufferedWriter(r);

                        bw.write("#instrument name\n");
                        bw.write("Name: " + lastSelectedInstrumentName + "\n\n");

                        bw.write("#instrument source\n");
                        bw.write("Accession: " +
                                prideConverter.getProperties().
                                getAccessionInstrumentSourceParameter() +
                                "\n");
                        bw.write("cvLookup: " +
                                prideConverter.getProperties().
                                getCVLookupInstrumentSourceParameter() +
                                "\n");
                        bw.write("Name: " +
                                prideConverter.getProperties().
                                getNameInstrumentSourceParameter() +
                                "\n\n");

                        bw.write("#instrument detector\n");
                        bw.write("Accession: " +
                                prideConverter.getProperties().
                                getAccessionInstrumentDetectorParamater() +
                                "\n");
                        bw.write("cvLookup: " +
                                prideConverter.getProperties().
                                getCVLookupInstrumentDetectorParamater() +
                                "\n");
                        bw.write("Name: " +
                                prideConverter.getProperties().
                                getNameInstrumentDetectorParamater() +
                                "\n\n");

                        bw.write("#analyzers\n");
                        bw.write(prideConverter.getProperties().getAnalyzerList().
                                size() +
                                "\n");

                        if (prideConverter.getProperties().getAnalyzerList().
                                size() ==
                                0) {
                            bw.write("\n");
                        }

                        Iterator analyzers = prideConverter.getProperties().
                                getAnalyzerList().iterator();
                        Iterator cvTerms;

                        AnalyzerImpl tempAnalyzer;
                        CvParamImpl cvParams;
                        String tempA;

                        int numberOfTerms;

                        while (analyzers.hasNext()) {

                            tempAnalyzer = (AnalyzerImpl) analyzers.next();

                            cvTerms =
                                    tempAnalyzer.getAnalyzerCvParameterList().
                                    iterator();

                            bw.write(tempAnalyzer.getAnalyzerCvParameterList().
                                    size() +
                                    "\n");

                            tempA = "";
                            numberOfTerms = 0;

                            while (cvTerms.hasNext()) {
                                cvParams = (CvParamImpl) cvTerms.next();

                                bw.write("Accession: " + cvParams.getAccession() +
                                        "\n");
                                bw.write("CVLookup: " + cvParams.getCVLookup() +
                                        "\n");
                                bw.write("Name: " + cvParams.getName() + "\n");
                                bw.write("Value: " + cvParams.getValue() +
                                        "\n\n");
                            }
                        }

                        bw.write("#processing methods\n");
                        bw.write(tempProcessingMethods.size() +
                                "\n");

                        if (tempProcessingMethods.size() == 0) {
                            bw.write("\n");
                        }

                        CvParamImpl temp;

                        for (int i = 0; i <
                                processingMethodsJTable.getRowCount(); i++) {

                            temp = (CvParamImpl) tempProcessingMethods.get(i);

                            bw.write("Accession: " + temp.getAccession() + "\n");
                            bw.write("CVLookup: " + temp.getCVLookup() + "\n");
                            bw.write("Name: " + temp.getName() + "\n");

                            bw.write("Value: " +
                                    (String) processingMethodsJTable.getValueAt(i,
                                    2) +
                                    "\n\n");
                        }

                        bw.write("#software\n");
                        bw.write("Name: " + softwareNameJTextField.getText() + "\n");
                        bw.write("Version: " + softwareVersionJTextField.getText());

                        valuesChanged = false;

                        bw.close();
                        r.close();
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (value == JOptionPane.CANCEL_OPTION) {
                    cancel = true;
                    instrumentNameJComboBox.setSelectedItem(lastSelectedInstrumentName);
                } else { //value == NO

                    String instrumentName = JOptionPane.showInputDialog(this,
                            "Please provide the name of the new instrument: ",
                            "Instrument Name", JOptionPane.PLAIN_MESSAGE);

                    if (instrumentName != null) {

                        String newName;

                        newName = instrumentPath +
                                instrumentName +
                                ".int";

                        while (new File(newName).exists()) {
                            instrumentName =
                                    JOptionPane.showInputDialog(this,
                                    "This name is already in use. Please provide a new name: ",
                                    "Instrument Name", JOptionPane.PLAIN_MESSAGE);

                            newName = instrumentPath +
                                    instrumentName +
                                    ".int";
                        }

                        if (instrumentName != null) {

                            try {

                                FileWriter r = new FileWriter(newName);
                                BufferedWriter bw = new BufferedWriter(r);

                                bw.write("#instrument name\n");
                                bw.write("Name: " + instrumentName +
                                        "\n\n");

                                bw.write("#instrument source\n");
                                bw.write("Accession: " +
                                        prideConverter.getProperties().
                                        getAccessionInstrumentSourceParameter() +
                                        "\n");
                                bw.write("cvLookup: " +
                                        prideConverter.getProperties().
                                        getCVLookupInstrumentSourceParameter() +
                                        "\n");
                                bw.write("Name: " +
                                        prideConverter.getProperties().
                                        getNameInstrumentSourceParameter() +
                                        "\n\n");

                                bw.write("#instrument detector\n");
                                bw.write("Accession: " +
                                        prideConverter.getProperties().
                                        getAccessionInstrumentDetectorParamater() +
                                        "\n");
                                bw.write("cvLookup: " +
                                        prideConverter.getProperties().
                                        getCVLookupInstrumentDetectorParamater() +
                                        "\n");
                                bw.write("Name: " +
                                        prideConverter.getProperties().
                                        getNameInstrumentDetectorParamater() +
                                        "\n\n");

                                bw.write("#analyzers\n");
                                bw.write(prideConverter.getProperties().
                                        getAnalyzerList().size() +
                                        "\n");

                                if (prideConverter.getProperties().getAnalyzerList().size() == 0) {
                                    bw.write("\n");
                                }

                                Iterator analyzers = prideConverter.getProperties().
                                        getAnalyzerList().iterator();
                                Iterator cvTerms;

                                AnalyzerImpl tempAnalyzer;
                                CvParamImpl cvParams;
                                String tempA;

                                int numberOfTerms;

                                while (analyzers.hasNext()) {

                                    tempAnalyzer = (AnalyzerImpl) analyzers.next();

                                    cvTerms =
                                            tempAnalyzer.getAnalyzerCvParameterList().
                                            iterator();

                                    bw.write(tempAnalyzer.getAnalyzerCvParameterList().
                                            size() +
                                            "\n");

                                    tempA = "";
                                    numberOfTerms = 0;

                                    while (cvTerms.hasNext()) {
                                        cvParams = (CvParamImpl) cvTerms.next();

                                        bw.write("Accession: " +
                                                cvParams.getAccession() +
                                                "\n");
                                        bw.write("CVLookup: " +
                                                cvParams.getCVLookup() +
                                                "\n");
                                        bw.write("Name: " + cvParams.getName() +
                                                "\n");
                                        bw.write("Value: " + cvParams.getValue() +
                                                "\n\n");
                                    }
                                }

                                bw.write("#processing methods\n");
                                bw.write(tempProcessingMethods.size() +
                                        "\n");

                                if (tempProcessingMethods.size() == 0) {
                                    bw.write("\n");
                                }

                                CvParamImpl temp;

                                for (int i = 0; i <
                                        processingMethodsJTable.getRowCount(); i++) {

                                    temp = (CvParamImpl) tempProcessingMethods.get(i);

                                    bw.write("Accession: " + temp.getAccession() + "\n");
                                    bw.write("CVLookup: " + temp.getCVLookup() + "\n");
                                    bw.write("Name: " + temp.getName() + "\n");

                                    bw.write("Value: " +
                                            (String) processingMethodsJTable.getValueAt(i,
                                            2) + "\n\n");
                                }

                                bw.write("#software\n");
                                bw.write("Name: " +
                                        softwareNameJTextField.getText() +
                                        "\n");
                                bw.write("Version: " +
                                        softwareVersionJTextField.getText());

                                valuesChanged = false;

                                bw.close();
                                r.close();
                            } catch (FileNotFoundException ex) {
                                ex.printStackTrace();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }

                            instrumentNameJComboBox.insertItemAt(
                                    instrumentName, instrumentNameJComboBox.getItemCount() - 2);
                        } else {
                            cancel = true;
                            instrumentNameJComboBox.setSelectedItem(lastSelectedInstrumentName);
                            prideConverter.getUserProperties().
                                    setCurrentSelectedInstrument(lastSelectedInstrumentName);
                        }
                    } else {
                        cancel = true;
                        instrumentNameJComboBox.setSelectedItem(lastSelectedInstrumentName);
                        prideConverter.getUserProperties().
                                setCurrentSelectedInstrument(lastSelectedInstrumentName);
                    }
                }
            } else if (value == JOptionPane.CANCEL_OPTION) {
                cancel = true;
                instrumentNameJComboBox.setSelectedItem(lastSelectedInstrumentName);
                prideConverter.getUserProperties().setCurrentSelectedInstrument(lastSelectedInstrumentName);
            }
        }

        if (!cancel) {
            lastSelectedInstrumentName =
                    (String) instrumentNameJComboBox.getSelectedItem();
            String selectedInstrumentName =
                    (String) instrumentNameJComboBox.getSelectedItem();

            prideConverter.getUserProperties().setCurrentSelectedInstrument(selectedInstrumentName);

            //empty the two tables
            while (analyzerJTable.getRowCount() > 0) {
                ((DefaultTableModel) analyzerJTable.getModel()).removeRow(0);
            }

            while (processingMethodsJTable.getRowCount() > 0) {
                ((DefaultTableModel) processingMethodsJTable.getModel()).removeRow(0);
            }

            instrumentSourceJTextField.setText("");
            instrumentDetectorJTextField.setText("");

            softwareNameJTextField.setText("");
            softwareVersionJTextField.setText("");

            tempProcessingMethods = new Vector();

            prideConverter.getProperties().setAnalyzerList(new ArrayList());

            if (instrumentNameJComboBox.getSelectedIndex() == 0) {
                valuesChanged = false;

                instrumentSourceJButton.setEnabled(false);
                instrumentDetectorJButton.setEnabled(false);
                analyzerJButton.setEnabled(false);
                softwareNameJTextField.setEnabled(false);
                softwareVersionJTextField.setEnabled(false);
                softwareNameJTextField.setEditable(false);
                softwareVersionJTextField.setEditable(false);
                processingMethodsJButton.setEnabled(false);

            } else if (instrumentNameJComboBox.getSelectedIndex() ==
                    instrumentNameJComboBox.getItemCount() - 1) {

                valuesChanged = false;

                instrumentSourceJButton.setEnabled(false);
                instrumentDetectorJButton.setEnabled(false);
                analyzerJButton.setEnabled(false);
                softwareNameJTextField.setEnabled(false);
                softwareVersionJTextField.setEnabled(false);
                softwareNameJTextField.setEditable(false);
                softwareVersionJTextField.setEditable(false);
                processingMethodsJButton.setEnabled(false);

                ComboBoxInputDialog input = new ComboBoxInputDialog(this, this, true);
                input.setTitle("Create New Instrument");
                input.setBorderTitle("New Instrument");
                input.setVisible(true);
            } else {
                selectedInstrumentName = instrumentPath + selectedInstrumentName + ".int";

                instrumentSourceJButton.setEnabled(true);
                instrumentDetectorJButton.setEnabled(true);
                analyzerJButton.setEnabled(true);
                softwareNameJTextField.setEnabled(true);
                softwareVersionJTextField.setEnabled(true);
                softwareNameJTextField.setEditable(true);
                softwareVersionJTextField.setEditable(true);
                processingMethodsJButton.setEnabled(true);

                try {
                    String temp, temp2;
                    FileReader f = new FileReader(selectedInstrumentName);
                    BufferedReader b = new BufferedReader(f);
                    b.readLine();//read #instrument name
                    temp = b.readLine();
                    prideConverter.getProperties().setInstrumentName(temp.substring(temp.indexOf(": ") + 2));
                    b.readLine();
                    b.readLine();//read #instrument source
                    temp = b.readLine();
                    prideConverter.getProperties().setAccessionInstrumentSourceParameter(
                            temp.substring(temp.indexOf(": ") + 2));
                    temp = b.readLine();
                    prideConverter.getProperties().setCVLookupInstrumentSourceParameter(
                            temp.substring(temp.indexOf(": ") + 2));
                    temp = b.readLine();
                    prideConverter.getProperties().setNameInstrumentSourceParameter(
                            temp.substring(temp.indexOf(": ") + 2));
                    b.readLine();
                    b.readLine();//read #instrument detector
                    temp = b.readLine();
                    prideConverter.getProperties().setAccessionInstrumentDetectorParamater(
                            temp.substring(temp.indexOf(": ") + 2));
                    temp = b.readLine();
                    prideConverter.getProperties().setCVLookupInstrumentDetectorParamater(
                            temp.substring(temp.indexOf(": ") + 2));
                    temp = b.readLine();
                    prideConverter.getProperties().setNameInstrumentDetectorParamater(
                            temp.substring(temp.indexOf(": ") + 2));
                    b.readLine();
                    b.readLine();//read #analyzers
                    temp = b.readLine();

                    int numberOfAnalyzers = new Integer(temp).intValue();

                    Vector terms = new Vector();
                    Vector accessions = new Vector();
                    Vector cv = new Vector();
                    Vector values = new Vector();
                    for (int i = 0; i < numberOfAnalyzers; i++) {

                        terms = new Vector();
                        accessions = new Vector();
                        cv = new Vector();
                        values = new Vector();

                        temp = b.readLine();
                        int numberOfTerms = new Integer(temp).intValue();
                        for (int j = 0; j < numberOfTerms; j++) {

                            temp = b.readLine();

                            accessions.add(temp.substring(temp.indexOf(": ") + 2));
                            temp = b.readLine();
                            cv.add(temp.substring(temp.indexOf(": ") + 2));
                            temp = b.readLine();
                            terms.add(temp.substring(temp.indexOf(": ") + 2));
                            temp = b.readLine();
                            temp = temp.substring(temp.indexOf(": ") + 2);

                            if (temp.equalsIgnoreCase("null")) {
                                values.add(null);
                            } else {
                                values.add(temp);
                            }

                            b.readLine();
                        }

                        insertAnalyzer(terms, accessions, cv, values, -1);
                    }

                    b.readLine();

                    if (numberOfAnalyzers == 0) {
                        b.readLine();
                    }

                    temp = b.readLine();
                    int numberOfProcessinganalyzers = new Integer(temp).intValue();
                    String name, accession, cvLookup, value;
                    prideConverter.getProperties().setProcessingMethod(new ArrayList());
                    for (int i = 0; i < numberOfProcessinganalyzers; i++) {
                        temp = b.readLine();
                        accession = temp.substring(temp.indexOf(": ") + 2);
                        temp = b.readLine();
                        cvLookup = temp.substring(temp.indexOf(": ") + 2);
                        temp = b.readLine();
                        name = temp.substring(temp.indexOf(": ") + 2);
                        temp = b.readLine();
                        value = temp.substring(temp.indexOf(": ") + 2);

                        if (value.equalsIgnoreCase("null")) {
                            value = null;
                        }

                        addProcessingMethod(name, accession, cvLookup, value, -1);
                        b.readLine();
                    }

                    String tempLine = b.readLine();//read #software

                    //if the list is empty
                    if (!tempLine.equalsIgnoreCase("#software")) {
                        b.readLine();
                    }

                    temp = b.readLine();
                    prideConverter.getProperties().setSoftwareName(temp.substring(temp.indexOf(": ") + 2));
                    temp = b.readLine();
                    prideConverter.getProperties().setSoftwareVersion(temp.substring(temp.indexOf(": ") + 2));

                    softwareNameJTextField.setText(prideConverter.getProperties().getSoftwareName());
                    softwareVersionJTextField.setText(prideConverter.getProperties().getSoftwareVersion());
                    if (prideConverter.getProperties().getNameInstrumentSourceParameter().length() >
                            0) {
                        this.instrumentSourceJTextField.setText(
                                prideConverter.getProperties().getNameInstrumentSourceParameter() +
                                " [" +
                                prideConverter.getProperties().getAccessionInstrumentSourceParameter() +
                                "]");
                        instrumentSourceJTextField.setCaretPosition(0);
                    }

                    if (prideConverter.getProperties().
                            getNameInstrumentDetectorParamater().length() >
                            0) {
                        this.instrumentDetectorJTextField.setText(prideConverter.getProperties().
                                getNameInstrumentDetectorParamater() +
                                " [" +
                                prideConverter.getProperties().
                                getAccessionInstrumentDetectorParamater() +
                                "]");
                        instrumentDetectorJTextField.setCaretPosition(0);
                    }

                    b.close();
                    f.close();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                valuesChanged = false;
            }
        }

        mandatoryFieldsCheck();

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_instrumentNameJComboBoxActionPerformed

    /**
     * Sets the current selected instrument to the one selected in the combo 
     * box.
     * 
     * @param evt
     */
    private void instrumentNameJComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_instrumentNameJComboBoxItemStateChanged
        prideConverter.getUserProperties().setCurrentSelectedInstrument((String) instrumentNameJComboBox.getSelectedItem());
        mandatoryFieldsCheck();
    }//GEN-LAST:event_instrumentNameJComboBoxItemStateChanged

    /**
     * Delete the selected instrument.
     * 
     * @param evt
     */
    private void deleteJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteJMenuItemActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        int option = JOptionPane.showConfirmDialog(this,
                "This will delete the selected instrument. Continue?");

        if (option == JOptionPane.YES_OPTION) {

            String newName = instrumentPath +
                    (String) instrumentNameJComboBox.getSelectedItem() + ".int";

            boolean deleted = new File(newName).delete();

            if (!deleted) {
                JOptionPane.showMessageDialog(this,
                        "The file could not be deleted!");
            } else {
                lastSelectedInstrumentName = null;
                valuesChanged =
                        false;
                readInstrumentsFromFile();
            }
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_deleteJMenuItemActionPerformed

    /**
     * Right clicking in the instrument combo box opens a delete instrument 
     * popup menu.
     * 
     * @param evt
     */
    private void aboutJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpWindow(this, getClass().getResource("/no/uib/prideconverter/helpfiles/AboutPRIDE_Converter.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_aboutJButtonActionPerformed

    /**
     * Opens a popup menu if the user right clicks in the instrument combo box.
     * 
     * @param evt
     */
    private void instrumentNameJComboBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_instrumentNameJComboBoxMouseClicked

        if (evt.getButton() == 3) {

            if (instrumentNameJComboBox.getSelectedIndex() != 0 &&
                    instrumentNameJComboBox.getSelectedIndex() !=
                    instrumentNameJComboBox.getItemCount() - 1) {

                popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }//GEN-LAST:event_instrumentNameJComboBoxMouseClicked

    /**
     * Deletes the selected instrument.
     * 
     * @param evt
     */
    private void deleteJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteJButtonActionPerformed
        if (instrumentNameJComboBox.getSelectedIndex() != 0 &&
                instrumentNameJComboBox.getSelectedIndex() !=
                instrumentNameJComboBox.getItemCount() - 1 &&
                instrumentNameJComboBox.getSelectedIndex() != -1) {

            deleteJMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_deleteJButtonActionPerformed

    /**
     * If the delete key is pressed the selected rows in the analyzer table 
     * is removed.
     * 
     * @param evt
     */
    private void analyzerJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_analyzerJTableKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            analyzersDeleteRowJMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_analyzerJTableKeyReleased

    /**
     * If the delete key is pressed the selected rows in the processing methods 
     * table is removed.
     * 
     * @param evt
     */
    private void processingMethodsJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_processingMethodsJTableKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            processingMethodsDeleteSelectedRowJMenuItemActionPerformed(null);
        }
    }//GEN-LAST:event_processingMethodsJTableKeyReleased

    /**
     * Insert a new instrument source.
     * 
     * @param name
     * @param accession
     * @param ontology
     */
    public void setInstrumentSource(String name, String accession, String ontology) {
        valuesChanged = true;

        instrumentSourceJTextField.setText(name + " [" + accession + "]");
        instrumentSourceJTextField.setCaretPosition(0);
        prideConverter.getProperties().setNameInstrumentSourceParameter(name);
        prideConverter.getProperties().setAccessionInstrumentSourceParameter(accession);
        prideConverter.getProperties().setCVLookupInstrumentSourceParameter(ontology);

        mandatoryFieldsCheck();
    }

    /**
     * Insert a new instrument detector
     * 
     * @param name
     * @param accession
     * @param ontology
     */
    public void setInstrumentDetector(String name, String accession, String ontology) {
        valuesChanged = true;

        instrumentDetectorJTextField.setText(name + " [" + accession + "]");
        instrumentDetectorJTextField.setCaretPosition(0);
        prideConverter.getProperties().setNameInstrumentDetectorParamater(name);
        prideConverter.getProperties().setAccessionInstrumentDetectorParamater(accession);
        prideConverter.getProperties().setCVLookupInstrumentDetectorParamater(ontology);

        mandatoryFieldsCheck();
    }

    /**
     * Instert a new processing method. 
     * 
     * @param name
     * @param accession
     * @param ontology
     * @param modifiedRow The row to modify, use -1 if adding a new processing method
     */
    public void addProcessingMethod(String name, String accession,
            String ontology, int modifiedRow) {
        addProcessingMethod(name, accession, ontology, null, modifiedRow);
    }

    /**
     * Instert a new processing method. 
     * 
     * @param name
     * @param accession
     * @param ontology
     * @param value
     * @param modifiedRow The row to modify, use -1 if adding a new processing method
     */
    public void addProcessingMethod(String name, String accession,
            String ontology, String value, int modifiedRow) {

        valuesChanged = true;

        if (modifiedRow == -1) {

            ((DefaultTableModel) this.processingMethodsJTable.getModel()).addRow(
                    new Object[]{
                new Integer(processingMethodsJTable.getRowCount() + 1),
                name + " [" + accession + "]", value
            });

            tempProcessingMethods.add(new CvParamImpl(
                    accession,
                    ontology,
                    name,
                    new Long(processingMethodsJTable.getRowCount() - 1),
                    value));
        } else {
            processingMethodsJTable.setValueAt(name + " [" + accession + "]",
                    modifiedRow, 1);
            processingMethodsJTable.setValueAt(null, modifiedRow, 2);

            CvParamImpl cvParam = new CvParamImpl(
                    accession,
                    ontology,
                    name,
                    new Long(modifiedRow),
                    value);

            tempProcessingMethods.setElementAt(cvParam, modifiedRow);
        }

        mandatoryFieldsCheck();
    }

    /**
     * Insert a new analyzer.
     * 
     * @param terms
     * @param accessions
     * @param cv
     * @param values
     * @param modifiedRow The row to modify, use -1 if adding a new analyzer 
     */
    public void insertAnalyzer(Vector terms, Vector accessions, Vector cv,
            Vector values, int modifiedRow) {

        valuesChanged = true;

        String temp = "";
        int numberOfTerms = 0;
        Collection analyzerCVParams = new ArrayList(terms.size());

        for (int i = 0; i <
                terms.size(); i++) {
            if (values.get(i) != null) {
                temp += "[" + terms.get(i) + ": " + values.get(i) + "] ";

                analyzerCVParams.add(new CvParamImpl(
                        (String) accessions.get(i),
                        (String) cv.get(i),
                        (String) terms.get(i),
                        new Long(0).longValue(),
                        (String) values.get(i)));
            } else {
                temp += "[" + terms.get(i) + "] ";

                analyzerCVParams.add(new CvParamImpl(
                        (String) accessions.get(i),
                        (String) cv.get(i),
                        (String) terms.get(i),
                        new Long(0).longValue(),
                        null));
            }

            numberOfTerms++;
        }

        if (modifiedRow == -1) {
            prideConverter.getProperties().getAnalyzerList().add(
                    new AnalyzerImpl(
                    analyzerCVParams,
                    null,
                    new Long(analyzerJTable.getRowCount())));

            ((DefaultTableModel) analyzerJTable.getModel()).addRow(
                    new Object[]{
                new Integer(analyzerJTable.getRowCount() + 1),
                temp,
                new Integer(numberOfTerms)
            });
        } else {
            analyzerJTable.setValueAt(temp, modifiedRow, 1);
            analyzerJTable.setValueAt(new Integer(numberOfTerms), modifiedRow, 2);

            Object[] analyzers = prideConverter.getProperties().getAnalyzerList().
                    toArray();
            analyzers[modifiedRow] = new AnalyzerImpl(
                    analyzerCVParams, null, modifiedRow);

            prideConverter.getProperties().setAnalyzerList(new ArrayList());

            for (int i = 0; i <
                    analyzers.length; i++) {
                prideConverter.getProperties().getAnalyzerList().add(analyzers[i]);
            }

        }

        mandatoryFieldsCheck();
    }

    /**
     * Fixes the indices in the processing method table so that they are in 
     * accending order starting from one
     */
    private void fixProcessingMethodsIndices() {
        for (int row = 0; row <
                ((DefaultTableModel) processingMethodsJTable.getModel()).getRowCount(); row++) {
            ((DefaultTableModel) processingMethodsJTable.getModel()).setValueAt(new Integer(row +
                    1), row, 0);
        }
    }

    /**
     * Fixes the indices int the analyzers table so that they are in 
     * accending order starting from one
     */
    private void fixAnalyzersIndices() {
        for (int row = 0; row <
                ((DefaultTableModel) analyzerJTable.getModel()).getRowCount(); row++) {
            ((DefaultTableModel) analyzerJTable.getModel()).setValueAt(new Integer(row +
                    1), row, 0);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JButton analyzerJButton;
    private javax.swing.JTable analyzerJTable;
    private javax.swing.JMenuItem analyzersDeleteRowJMenuItem;
    private javax.swing.JMenuItem analyzersEditJMenuItem;
    private javax.swing.JMenuItem analyzersMoveDownJMenuItem;
    private javax.swing.JMenuItem analyzersMoveUpJMenuItem;
    private javax.swing.JPopupMenu analyzersPopupJMenu;
    private javax.swing.JButton backJButton;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JButton deleteJButton;
    private javax.swing.JMenuItem deleteJMenuItem;
    private javax.swing.JButton helpJButton;
    private javax.swing.JButton instrumentDetectorJButton;
    private javax.swing.JTextField instrumentDetectorJTextField;
    private javax.swing.JComboBox instrumentNameJComboBox;
    private javax.swing.JButton instrumentSourceJButton;
    private javax.swing.JTextField instrumentSourceJTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JButton nextJButton;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JMenuItem processingMethodsDeleteSelectedRowJMenuItem;
    private javax.swing.JMenuItem processingMethodsEditJMenuItem;
    private javax.swing.JButton processingMethodsJButton;
    private javax.swing.JTable processingMethodsJTable;
    private javax.swing.JMenuItem processingMethodsMoveDownJMenuItem;
    private javax.swing.JMenuItem processingMethodsMoveUpJMenuItem;
    private javax.swing.JPopupMenu processingMethodsPopupJMenu;
    private javax.swing.JTextField softwareNameJTextField;
    private javax.swing.JTextField softwareVersionJTextField;
    // End of variables declaration//GEN-END:variables
    /**
     * See ComboBoxInputable
     */
    public void insertIntoComboBox(String text) {

        prideConverter.getUserProperties().setCurrentSelectedInstrument(text);

        String newName;
        newName = instrumentPath + text + ".int";

        try {
            FileWriter r = new FileWriter(newName);
            BufferedWriter bw = new BufferedWriter(r);

            bw.write("#instrument name\n");
            bw.write("Name: " + text + "\n\n");

            bw.write("#instrument source\n");
            bw.write("Accession: \n");
            bw.write("cvLookup: \n");
            bw.write("Name: \n\n");

            bw.write("#instrument detector\n");
            bw.write("Accession: \n");
            bw.write("cvLookup: \n");
            bw.write("Name: \n\n");

            bw.write("#analyzers\n");
            bw.write("0\n\n");

            bw.write("#processing methods\n");
            bw.write("0\n\n");

            bw.write("#software\n");
            bw.write("Name: " + softwareNameJTextField.getText() + "\n");
            bw.write("Version: " + softwareVersionJTextField.getText());

            bw.close();
            r.close();

            readInstrumentsFromFile();

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
        lastSelectedInstrumentName = null;
        instrumentNameJComboBox.setSelectedIndex(0);
        readInstrumentsFromFile();
    }

    /**
     * See ComboBoxInputable
     */
    public boolean alreadyInserted(String currentInstrumentName) {

        String newName;
        newName = instrumentPath + currentInstrumentName + ".int";
        File newFile = new File(newName);

        return newFile.exists();
    }

    /**
     * Save the current instrument.
     * 
     * @param instrumentName
     */
    private void saveInstrument(String instrumentName) {

        try {
            String newName = instrumentName;
            newName =
                    instrumentPath +
                    newName +
                    ".int";

            FileWriter r = new FileWriter(newName);
            BufferedWriter bw = new BufferedWriter(r);

            bw.write("#instrument name\n");
            bw.write("Name: " + instrumentName + "\n\n");

            bw.write("#instrument source\n");
            bw.write("Accession: " +
                    prideConverter.getProperties().
                    getAccessionInstrumentSourceParameter() +
                    "\n");
            bw.write("cvLookup: " +
                    prideConverter.getProperties().
                    getCVLookupInstrumentSourceParameter() +
                    "\n");
            bw.write("Name: " +
                    prideConverter.getProperties().
                    getNameInstrumentSourceParameter() + "\n\n");

            bw.write("#instrument detector\n");
            bw.write("Accession: " +
                    prideConverter.getProperties().
                    getAccessionInstrumentDetectorParamater() +
                    "\n");
            bw.write("cvLookup: " +
                    prideConverter.getProperties().
                    getCVLookupInstrumentDetectorParamater() +
                    "\n");
            bw.write("Name: " +
                    prideConverter.getProperties().
                    getNameInstrumentDetectorParamater() +
                    "\n\n");

            bw.write("#analyzers\n");
            bw.write(prideConverter.getProperties().getAnalyzerList().size() +
                    "\n");

            if (prideConverter.getProperties().getAnalyzerList().size() == 0) {
                bw.write("\n");
            }

            Iterator analyzers = prideConverter.getProperties().getAnalyzerList().
                    iterator();
            Iterator cvTerms;

            AnalyzerImpl tempAnalyzer;

            CvParamImpl cvParams;

            String tempA;

            int numberOfTerms;

            while (analyzers.hasNext()) {

                tempAnalyzer = (AnalyzerImpl) analyzers.next();

                cvTerms = tempAnalyzer.getAnalyzerCvParameterList().iterator();

                bw.write(tempAnalyzer.getAnalyzerCvParameterList().size() + "\n");

                tempA = "";
                numberOfTerms = 0;

                while (cvTerms.hasNext()) {
                    cvParams = (CvParamImpl) cvTerms.next();

                    bw.write("Accession: " + cvParams.getAccession() + "\n");
                    bw.write("CVLookup: " + cvParams.getCVLookup() + "\n");
                    bw.write("Name: " + cvParams.getName() + "\n");
                    bw.write("Value: " + cvParams.getValue() + "\n\n");
                }
            }

            bw.write("#processing methods\n");
            bw.write(tempProcessingMethods.size() + "\n");

            if (tempProcessingMethods.size() == 0) {
                bw.write("\n");
            }

            CvParamImpl temp;

            for (int i = 0; i <
                    processingMethodsJTable.getRowCount(); i++) {

                temp = (CvParamImpl) tempProcessingMethods.get(i);

                bw.write("Accession: " + temp.getAccession() + "\n");
                bw.write("CVLookup: " + temp.getCVLookup() + "\n");
                bw.write("Name: " + temp.getName() + "\n");

                bw.write("Value: " +
                        (String) processingMethodsJTable.getValueAt(i, 2) +
                        "\n\n");
            }

            bw.write("#software\n");
            bw.write("Name: " + softwareNameJTextField.getText() + "\n");
            bw.write("Version: " + softwareVersionJTextField.getText());

            valuesChanged =
                    false;

            bw.close();
            r.close();

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * See OLSInputable
     */
    public void insertOLSResult(String field, String selectedValue,
            String accession,
            String ontologyShort, String ontologyLong, int modifiedRow,
            String mappedTerm) {

        prideConverter.getUserProperties().setLastSelectedOntology(ontologyLong);

        if (mappedTerm != null) {
            prideConverter.getUserProperties().getCVTermMappings().put(
                    mappedTerm, new CvParamImpl(accession, ontologyShort,
                    selectedValue, 0, null));
        }

        if (field.equalsIgnoreCase("instrumentSource")) {
            setInstrumentSource(selectedValue, accession, ontologyShort);
        } else if (field.equalsIgnoreCase("instrumentDetector")) {
            setInstrumentDetector(selectedValue, accession, ontologyShort);
        } else if (field.equalsIgnoreCase("processingMethods")) {
            addProcessingMethod(selectedValue, accession, ontologyShort,
                    modifiedRow);
        } else if (field.equalsIgnoreCase("analyzer")) {

            Vector names = new Vector();
            names.add(selectedValue);
            Vector accessions = new Vector();
            accessions.add(accession);
            Vector ontologies = new Vector();
            ontologies.add(ontologyShort);
            Vector values = new Vector();
            values.add(null);

            insertAnalyzer(names, accessions, ontologies, values, -1);
        }
    }

    /**
     * See OLSInputable
     */
    public Window getWindow() {
        return (Window) this;
    }
}