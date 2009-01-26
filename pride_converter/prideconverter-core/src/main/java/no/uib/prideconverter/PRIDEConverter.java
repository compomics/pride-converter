package no.uib.prideconverter;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.sql.Driver;
import java.sql.SQLException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Desktop;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import be.proteomics.lims.db.accessors.Spectrumfile;
import be.proteomics.lims.db.accessors.Identification;
import be.proteomics.lims.util.fileio.MascotGenericFile;
import be.proteomics.mascotdatfile.util.interfaces.MascotDatfileInf;
import be.proteomics.mascotdatfile.util.interfaces.Modification;
import be.proteomics.mascotdatfile.util.interfaces.QueryToPeptideMapInf;
import be.proteomics.mascotdatfile.util.mascot.FixedModification;
import be.proteomics.mascotdatfile.util.mascot.Peak;
import be.proteomics.mascotdatfile.util.mascot.PeptideHit;
import be.proteomics.mascotdatfile.util.mascot.ProteinHit;
import be.proteomics.mascotdatfile.util.mascot.Query;
import be.proteomics.mascotdatfile.util.mascot.VariableModification;
import be.proteomics.mascotdatfile.util.mascot.enumeration.MascotDatfileType;
import be.proteomics.mascotdatfile.util.mascot.factory.MascotDatfileFactory;
import be.proteomics.mascotdatfile.util.mascot.iterator.QueryEnumerator;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyKrupp;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

import de.proteinms.omxparser.OmssaOmxFile;
import de.proteinms.omxparser.util.MSHitSet;
import de.proteinms.omxparser.util.MSHits;
import de.proteinms.omxparser.util.MSModHit;
import de.proteinms.omxparser.util.MSPepHit;
import de.proteinms.omxparser.util.MSSpectrum;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import no.uib.prideconverter.gui.*;
import no.uib.prideconverter.util.IdentificationGeneral;
import no.uib.prideconverter.util.OmssaModification;
import no.uib.prideconverter.util.Properties;
import no.uib.prideconverter.util.UserProperties;
import no.uib.prideconverter.util.Util;
import no.uib.prideconverter.util.iTRAQ;

import org.systemsbiology.jrap.MSXMLParser;
import org.systemsbiology.jrap.Scan;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import uk.ac.ebi.pride.model.implementation.mzData.*;
import uk.ac.ebi.pride.model.implementation.core.ExperimentImpl;
import uk.ac.ebi.pride.model.implementation.core.PeptideImpl;
import uk.ac.ebi.pride.model.implementation.core.GelFreeIdentificationImpl;
import uk.ac.ebi.pride.model.implementation.core.ModificationImpl;
import uk.ac.ebi.pride.model.implementation.core.MonoMassDeltaImpl;
import uk.ac.ebi.pride.model.implementation.core.TwoDimensionalIdentificationImpl;
import uk.ac.ebi.pride.model.interfaces.mzdata.Spectrum;
import uk.ac.ebi.pride.model.interfaces.core.Experiment;
import uk.ac.ebi.pride.model.interfaces.mzdata.MzData;
import uk.ac.ebi.pride.xml.MzDataXMLUnmarshaller;
import uk.ac.ebi.pride.xml.XMLMarshaller;
import uk.ac.ebi.pride.xml.validation.PrideXmlValidator;
import uk.ac.ebi.pride.xml.validation.XMLValidationErrorHandler;
import uk.ac.ebi.tpp_to_pride.parsers.PeptideProphetXMLParser;
import uk.ac.ebi.tpp_to_pride.parsers.ProteinProphetXMLParser;
import uk.ac.ebi.tpp_to_pride.parsers.XmlPullParserPlus;
import uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet.ProteinProphetIsoform;
import uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet.ProteinProphetProteinID;
import uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet.ProteinProphetSummary;
import uk.ac.ebi.tpp_to_pride.wrappers.peptideprophet.*;

/**
 * This class contains the code for the actual conversion. Is works as 
 * the supervisor of all the wizard frames.
 *
 * @author  Harald Barsnes
 * 
 * Created March 2008
 */
public class PRIDEConverter {

    private static String wizardName = "PRIDE Converter";
    private static String prideConverterVersionNumber = "v1.14.2";
    private static ArrayList<IdentificationGeneral> ids;
    private static Collection identifications;
    private static int totalNumberOfSpectra = 0;
    private static int emptySpectraCounter = 0;
    private static int peptideIdCount = 0;
    private static String currentFileName = "";
    private static OutputDetails outputFrame;
    private static UserProperties userProperties;
    private static Properties properties;
    private static ProgressDialog progressDialog;
    private static Connection conn = null;
    private static boolean cancelConversion = false;
    private static boolean useErrorLog = true;
    private static boolean debug = false;
    private boolean useHardcodedPaths = false;

    /** 
     * Creates a new instance of PRIDEConverter.
     * 
     * Opens the first frame of the wizard (Data Source Selection).
     */
    public PRIDEConverter() {
        userProperties = new UserProperties(this);
        userProperties.readUserPropertiesFromFile(null);
        properties = new Properties();
        new DataSourceSelection(this, null);
    }

    /**
     * Tries to connect to the ms_lims database.
     * 
     * @param dataBaseDetails a reference to the database details frame
     * @return true if connected, false otherwise
     */
    public boolean connectToDataBase(DataBaseDetails dataBaseDetails) {

        boolean connectionSuccessfull = false;

        try {
            // DB driver loading.
            Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
            // DB user and password setting.
            java.util.Properties props = new java.util.Properties();
            props.put("user", userProperties.getUserName());
            props.put("password", properties.getPassWord());
            conn = driver.connect("jdbc:mysql://" +
                    userProperties.getServerHost() + "/" +
                    userProperties.getSchema(), props);
            connectionSuccessfull = true;

            //test to check if the latest version if ms_lims is used
            try {
                Identification.getIdentification(conn, "");
            } catch (MySQLSyntaxErrorException e) {
                JOptionPane.showMessageDialog(dataBaseDetails,
                        "Database connection not established:" +
                        "\n Please check that you are using the latest version of MS_LIMS.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                connectionSuccessfull = false;
                closeDataBaseConnection();
            }
        } catch (CommunicationsException ce) {
            JOptionPane.showMessageDialog(dataBaseDetails, "Database connection not established:" +
                    "\n" + "Unknown host.", "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dataBaseDetails, "Database connection not established:" +
                    "\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        return connectionSuccessfull;
    }

    /**
     * Closes the database connection
     */
    public void closeDataBaseConnection() {
        // Close DB connection.
        if (conn != null) {
            try {
                conn.close();
                if (debug) {
                    System.out.println("DB connection closed.");
                }
            } catch (SQLException sqle) {
                // Nothing to be done.
                JOptionPane.showMessageDialog(
                        null, "An error occured when attempting to close the DB connection." +
                        "See ../Properties/ErrorLog.txt for more details.",
                        "DB Connection Error", JOptionPane.ERROR_MESSAGE);
                Util.writeToErrorLog("DB Connection: Error while attempting to close DB connection!");
                sqle.printStackTrace();
            }
        }
    }

    /**
     * Tries to set the look and feel to PlasticLookAndFeel. Then checks if 
     * an ErrorLog is created, if not, it is created. And finally starts the 
     * PRIDEConverter wizard. Also checks if a newer version of the PRIDE 
     * Converter is available.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {

                try {
                    PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());
                    UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
                } catch (UnsupportedLookAndFeelException e) {
                    Util.writeToErrorLog("Setting Look And Feel: Error while attempting to set the Look And Feel");
                    e.printStackTrace();
                }

                // check if a newer version of the PRIDE Converter is available
                try {
                    URL downloadPage = new URL(
                            "http://code.google.com/p/pride-converter/downloads/detail?name=PRIDE_Converter_" +
                            prideConverterVersionNumber + ".zip");
                    int respons =
                            ((java.net.HttpURLConnection) downloadPage.openConnection()).getResponseCode();

                    // 404 means that the file no longer exists, which means that 
                    // the running version is no longer available for download, 
                    // which again means that a never version is available.
                    if (respons == 404) {
                        int option = JOptionPane.showConfirmDialog(null,
                                "A newer version of the PRIDE Converter is available. \n" +
                                "Do you want to upgrade?\n\n" +
                                "Selecting \'Yes\' will open the PRIDE Converter web page\n" +
                                "where you can download the latest version.",
                                "PRIDE Converter - Upgrade Available",
                                JOptionPane.YES_NO_CANCEL_OPTION);
                        if (option == JOptionPane.YES_OPTION) {
                            Desktop.getDesktop().browse(new URI("http://code.google.com/p/pride-converter/"));
                            System.exit(0);
                        } else if (option == JOptionPane.CANCEL_OPTION) {
                            System.exit(0);
                        }
                    }
                } catch (MalformedURLException e) {
                    Util.writeToErrorLog("PRIDEConvertert: Error when trying to look for update. " +
                            e.toString());
                //e.printStackTrace();
                } catch (IOException e) {
                    Util.writeToErrorLog("PRIDEConvertert: Error when trying to look for update. " +
                            e.toString());
                //e.printStackTrace();
                } catch (URISyntaxException e) {
                    Util.writeToErrorLog("PRIDEConvertert: Error when trying to look for update. " +
                            e.toString());
                //e.printStackTrace();
                }

                if (useErrorLog) {
                    try {
                        String path = "" + this.getClass().getProtectionDomain().getCodeSource().getLocation();
                        path = path.substring(5, path.lastIndexOf("/"));
                        path = path.substring(0, path.lastIndexOf("/") + 1) + "Properties/ErrorLog.txt";
                        path = path.replace("%20", " ");

                        File file = new File(path);
                        System.setOut(new java.io.PrintStream(new FileOutputStream(file, true)));
                        System.setErr(new java.io.PrintStream(new FileOutputStream(file, true)));

                        // creates a new error log file if it does not exist
                        if (!file.exists()) {
                            file.createNewFile();

                            FileWriter w = new FileWriter(file);
                            BufferedWriter bw = new BufferedWriter(w);

                            bw.close();
                            w.close();
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(
                                null, "An error occured when trying to create the ErrorLog." +
                                "See ../Properties/ErrorLog.txt for more details.",
                                "Error Creating ErrorLog", JOptionPane.ERROR_MESSAGE);
                        Util.writeToErrorLog("Error when creating ErrorLog: ");
                        e.printStackTrace();
                    }
                }

                new PRIDEConverter();
            }
        });
    }

    /**
     * Cancels the convertion process by System.exit(0). 
     * First saves the user properties.
     */
    public void cancelConvertion() {

        userProperties.saveUserPropertiesToFile();

        if (debug) {
            System.out.println("Converter closed.");
        }

        System.exit(0);
    }

    /**
     * Tries to convert the selected file(s) into PRIDE XML
     * 
     * @param outputDetails a reference to the OutputDetails frame.
     */
    public void convert(OutputDetails outputDetails) {

        userProperties.saveUserPropertiesToFile();
        setCancelConversion(false);

        outputFrame = outputDetails;
        progressDialog = new ProgressDialog(outputDetails, true);

        final Thread t = new Thread(new Runnable() {

            @Override
            public void run() {

                if (properties.getDataSource().equalsIgnoreCase("ms_lims")) {
                    progressDialog.setIntermidiate(false);
                    progressDialog.setTitle("Retrieving Spectra. Please Wait.");
                } else {
                    progressDialog.setIntermidiate(true);
                    progressDialog.setTitle("Please Wait.");
                }

                progressDialog.setVisible(true);
            }
        }, "ProgressDialog");

        t.start();

        new Thread("ConverterThread") {

            @Override
            public void run() {

//                long start = System.currentTimeMillis();
                boolean xmlValidated = false;

                try {

                    properties.setAlreadyChoosenModifications(new ArrayList());

                    if (cancelConversion) {
                        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                        outputFrame.setConvertButtonEnabled(true);
                        return;
                    }

                    try {
                        progressDialog.setString(null);
                        progressDialog.setTitle("Transforming Spectra. Please Wait.");
                        progressDialog.setIntermidiate(true);
                        progressDialog.setString(null);
                    } catch (NullPointerException e) {
                        Util.writeToErrorLog("Progress bar: NullPointerException!!!\n" + e.toString());
                    }

                    // Transform all selected spectra into mzData spectra and retrieve the identifications.
                    ArrayList mzDataSpectra;

                    mzDataSpectra = new ArrayList();

                    HashMap filenameToSpectrumID = new HashMap();

                    if (properties.getDataSource().equalsIgnoreCase("ms_lims")) {
                        filenameToSpectrumID = transformSpectraFrom_ms_lims(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("Mascot Dat File")) {
                        filenameToSpectrumID =
                                transformSpectraFromMascotDatFile(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("Mascot Generic File")) {
                        filenameToSpectrumID =
                                transformSpectraFromMascotGenericFile(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("Sequest DTA File") ||
                            properties.getDataSource().equalsIgnoreCase("Spectrum Mill") ||
                            properties.getDataSource().equalsIgnoreCase("Sequest Result File")) {
                        filenameToSpectrumID =
                                transformSpectraFromSequestAndSpectrumMill(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("Micromass PKL File") ||
                            properties.getDataSource().equalsIgnoreCase("VEMS")) {
                        filenameToSpectrumID =
                                transformSpectraFromPKLAndPKXFiles(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("X!Tandem")) {
                        filenameToSpectrumID =
                                transformSpectraFromXTandemDataFile(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("mzXML")) {
                        filenameToSpectrumID =
                                transformSpectraFromMzXML(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("OMSSA")) {
                        filenameToSpectrumID =
                                transformSpectraFromOMSSA(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("mzML")) {
                        filenameToSpectrumID =
                                transformSpectraFromMzML(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("MS2")) {
                        filenameToSpectrumID =
                                transformSpectraFromMS2Files(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("TPP")) {
                        filenameToSpectrumID =
                                transformSpectraFromTPPFiles(mzDataSpectra);
                    }
//                    else if (properties.getDataSource().equalsIgnoreCase("mzData")) {
//                        transformSpectraFromMzDataFiles(mzDataSpectra);
//                    }


                    if (debug) {
                        System.out.println("\nProcessed " + mzDataSpectra.size() + " spectra to mzData format.");
                    }

                    if (cancelConversion) {
                        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                        outputFrame.setConvertButtonEnabled(true);
                        return;
                    }

                    // removes the current file name string
                    progressDialog.setString(null);

                    try {
                        progressDialog.setTitle("Creating mzData. Please Wait.");
                        progressDialog.setIntermidiate(true);
                    } catch (NullPointerException e) {
                        Util.writeToErrorLog("Progress bar: NullPointerException!!!\n" + e.toString());
                    }

                    // create the mzData object
                    MzData mzData;

                    if (properties.getDataSource().equalsIgnoreCase("TPP")) {

                        // for TPP data the mzDataFile has already been created, we
                        // just need to add/update the additional details
                        mzData = updateMzData(properties.getMzDataFile());

                    } else if (properties.getDataSource().equalsIgnoreCase("mzData")) {

                        // for mzData the, we just have to add/update the additional details
                        // and combine the files into one mzData file
                        mzData = transformSpectraFromMzDataFiles();

                    } else {
                        mzData = createMzData(mzDataSpectra);
                    }

                    if (cancelConversion) {
                        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                        outputFrame.setConvertButtonEnabled(true);
                        return;
                    }

                    // Make sure each spectrum is identified only once.
                    HashMap groupedIds = new HashMap();

                    if (properties.getDataSource().equalsIgnoreCase("Mascot Generic File") ||
                            properties.getDataSource().equalsIgnoreCase("Sequest DTA File") ||
                            properties.getDataSource().equalsIgnoreCase("Micromass PKL File") ||
                            properties.getDataSource().equalsIgnoreCase("mzXML") ||
                            properties.getDataSource().equalsIgnoreCase("VEMS") ||
                            properties.getDataSource().equalsIgnoreCase("MS2") ||
                            properties.getDataSource().equalsIgnoreCase("mzData") ||
                            properties.getDataSource().equalsIgnoreCase("TPP")) {
                        // no identifications in mgf, dta, pkl, pkx and mzXML files
                        // for TPP the identifications are already handled
                        ids = new ArrayList<IdentificationGeneral>();
                    }

                    HashMap omitDuplicates = new HashMap(ids.size());

                    int unidentifedSpectraCounter = 0;

                    try {
                        progressDialog.setTitle("Removing Duplicated Spectra. Please Wait.");
                        progressDialog.setIntermidiate(false);
                        progressDialog.setMax(ids.size());
                    } catch (NullPointerException e) {
                        Util.writeToErrorLog("Progress bar: NullPointerException!!!\n" +
                                e.toString());
                    }

                    for (int i = 0; i < ids.size(); i++) {

                        progressDialog.setValue(i);

                        if (cancelConversion) {
                            outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                            outputFrame.setConvertButtonEnabled(true);
                            return;
                        }

                        if (ids.get(i) != null) {
                            IdentificationGeneral peptideIdentification = ids.get(i);
                            if (omitDuplicates.containsKey(peptideIdentification.getSpectrumFileId())) {
                                IdentificationGeneral oldID =
                                        (IdentificationGeneral) omitDuplicates.get(
                                        peptideIdentification.getSpectrumFileId());
                                if (peptideIdentification.getScore() > oldID.getScore()) {
                                    omitDuplicates.put(peptideIdentification.getSpectrumFileId(),
                                            peptideIdentification);
                                }
                            } else {
                                omitDuplicates.put(peptideIdentification.getSpectrumFileId(),
                                        peptideIdentification);
                            }
                        } else {
                            unidentifedSpectraCounter++;
                        }
                    }


                    if (debug) {
                        System.out.println("\nTransformed " + (ids.size() -
                                unidentifedSpectraCounter) +
                                " original identifications into " +
                                omitDuplicates.size() +
                                " unique identifications.\n");
                    }

                    try {
                        progressDialog.setTitle("Grouping Identifications. Please Wait.");
                        progressDialog.setIntermidiate(false);
                        progressDialog.setMax(omitDuplicates.size());
                    } catch (NullPointerException e) {
                        Util.writeToErrorLog("Progress bar: NullPointerException!!!\n" + e.toString());
                    }

                    // Cycle all unique identifications, grouping them by 
                    // accession and retaining only relevant data.
                    Iterator iter = omitDuplicates.values().iterator();

                    int counter = 0;
                    while (iter.hasNext()) {

                        if (cancelConversion) {
                            outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                            outputFrame.setConvertButtonEnabled(true);
                            return;
                        }

                        progressDialog.setValue(counter++);

                        IdentificationGeneral peptideIdentification = (IdentificationGeneral) iter.next();

                        String accession = peptideIdentification.getPeptideAccession();
                        String accessionVersion = null;

                        // special case for DBToolkit ([0-9]*-[0-9]*)
                        String dbToolKitPattern = "[\\d]\\+?-[\\d]\\+?";

                        Integer tempStartIndex = peptideIdentification.getPeptideStart();

                        Pattern pattern = Pattern.compile(dbToolKitPattern);
                        Matcher matcher = pattern.matcher(accession);

                        if (matcher.find()) {
                            tempStartIndex =
                                    new Integer(accession.substring(accession.lastIndexOf("(") +
                                    1, accession.lastIndexOf("-"))).intValue();
                            accession = accession.substring(0, accession.lastIndexOf(" "));

                        }

                        if (accession.lastIndexOf("|") != -1) {
                            if (properties.getDataSource().equalsIgnoreCase("Sequest Result File")) {
                                if (accession.indexOf("|", accession.indexOf("|")) != -1) {
                                    accession = accession.substring(0, accession.indexOf("|",
                                            accession.indexOf("|") + 1));
                                }
                            } else {
                                accession = accession.substring(0, accession.indexOf("|"));
                            }
                        }

                        int dotLocation = accession.indexOf(".");

                        if (dotLocation > 0) {
                            accessionVersion = accession.substring(dotLocation + 1).trim();
                            accession = accession.substring(0, dotLocation).trim();
                        }

                        PRIDEConverter.InnerID innerId = null;

                        if (!groupedIds.containsKey(accession)) {
                            // See which database the identification came from.
                            // Currently the database version is not extracted.
                            String database = "unknown";
                            String db_version = null;

                            if (peptideIdentification.getDatabase() != null) {
                                database = peptideIdentification.getDatabase();
                            }

                            innerId = new PRIDEConverter.InnerID(accession, accessionVersion,
                                    peptideIdentification.getSearchEngine(), database, db_version);
                        } else {
                            // Add it to existing ID.
                            innerId = (PRIDEConverter.InnerID) groupedIds.get(accession);
                        }

                        // Find the mzData spectrum reference from our HashMap.
                        Long spectrumRef =
                                (Long) filenameToSpectrumID.get(peptideIdentification.getSpectrumFileId());

                        if (spectrumRef == null) {
                            System.err.println("Could not find a spectrum reference for spectrum '" +
                                    peptideIdentification.getSpectrumFileId() + "'!\n");
                        }

                        innerId.addPeptide(spectrumRef,
                                peptideIdentification.getSequence(),
                                tempStartIndex,
                                new Double(peptideIdentification.getScore()),
                                peptideIdentification.getThreshold(),
                                peptideIdentification.getModifications(),
                                peptideIdentification.getCvParams(),
                                peptideIdentification.getUserParams());

                        // Add the new or re-store the modified protein ID to the hash.
                        groupedIds.put(accession, innerId);
                    }

                    if (debug) {
                        System.out.println("\nProcessed " + counter + " identifications.\n");
                    }

                    if (!properties.getDataSource().equalsIgnoreCase("TPP")) {
                        identifications = new ArrayList(groupedIds.size());
                    }

                    iter = groupedIds.keySet().iterator();

                    if (cancelConversion) {
                        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                        outputFrame.setConvertButtonEnabled(true);
                        return;
                    }

                    try {
                        progressDialog.setTitle("Converting Identifications. Please Wait.");
                        progressDialog.setIntermidiate(false);
                        progressDialog.setMax(groupedIds.size());
                    } catch (NullPointerException e) {
                        Util.writeToErrorLog("Progress bar: NullPointerException!!!\n" +
                                e.toString());
                    //e.printStackTrace();
                    }

                    counter = 0;

                    // Adding the identifications.
                    while (iter.hasNext()) {

                        if (cancelConversion) {
                            outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                            outputFrame.setConvertButtonEnabled(true);
                            return;
                        }

                        progressDialog.setValue(counter++);
                        String key = (String) iter.next();
                        PRIDEConverter.InnerID lInnerID = (PRIDEConverter.InnerID) groupedIds.get(key);
                        identifications.add(lInnerID.getGelFreeIdentification());
                    }

                    ArrayList experimentAdditionalCvParams = new ArrayList(2);

                    // add a CV term for the PRIDE Converter
                    experimentAdditionalCvParams.add(new CvParamImpl(
                            "PRIDE:0000175", "PRIDE", "XML generation software",
                            new Long(0), (wizardName + " " + prideConverterVersionNumber)));

                    // add the Project CV term
                    if (!properties.getExperimentProject().equalsIgnoreCase("")) {
                        experimentAdditionalCvParams.add(new CvParamImpl(
                                "PRIDE:0000097", "PRIDE", "Project", new Long(1),
                                properties.getExperimentProject()));
                    }

                    // the experiment properties
                    Experiment experiment = new ExperimentImpl(
                            properties.getPrideAccessionNumber(),
                            properties.getExperimentTitle(),
                            properties.getReferences(),
                            properties.getExperimentLabel(),
                            properties.getExperimentProtocolSteps(),
                            identifications,
                            userProperties.getCurrentProtocol(),
                            mzData,
                            experimentAdditionalCvParams,
                            properties.getExperimentUserParameters());

                    // add it to a collection
                    Collection experiments = new ArrayList(1);
                    experiments.add(experiment);

                    String tempLabel = properties.getExperimentLabel().replaceAll(" ", "_");

                    String completeFileName;

                    // write the output
                    if (properties.getDataSource().equalsIgnoreCase("ms_lims")) {

                        String projectIdsAsString = "";

                        if (properties.getProjectIds().size() > 1) {
                            projectIdsAsString = "ms_lims_projects_";
                        } else {
                            projectIdsAsString = "ms_lims_project_" +
                                    zerofill(properties.getProjectIds().get(0)) + "_";
                        }

                        completeFileName = userProperties.getOutputPath() +
                                projectIdsAsString + tempLabel + "_to_PRIDE_" +
                                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".xml";
                    } else {

                        String dataSourceShort = "";

                        if (properties.getDataSource().equalsIgnoreCase("Mascot Dat File")) {
                            dataSourceShort = "mascotDatFile";
                        } else if (properties.getDataSource().equalsIgnoreCase("Mascot Generic File")) {
                            dataSourceShort = "mascotGenericFile";
                        } else if (properties.getDataSource().equalsIgnoreCase("Sequest DTA File")) {
                            dataSourceShort = "dta_files";
                        } else if (properties.getDataSource().equalsIgnoreCase("X!Tandem")) {
                            dataSourceShort = "x!Tandem";
                        } else if (properties.getDataSource().equalsIgnoreCase("Micromass PKL File")) {
                            dataSourceShort = "pkl_files";
                        } else if (properties.getDataSource().equalsIgnoreCase("Spectrum Mill")) {
                            dataSourceShort = "spectrum_mill";
                        } else if (properties.getDataSource().equalsIgnoreCase("Sequest Result File")) {
                            dataSourceShort = "sequest";
                        } else if (properties.getDataSource().equalsIgnoreCase("mzXML")) {
                            dataSourceShort = "mzXML";
                        } else if (properties.getDataSource().equalsIgnoreCase("OMSSA")) {
                            dataSourceShort = "OMSSA";
                        } else if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                            dataSourceShort = "pkx_files";
                        } else if (properties.getDataSource().equalsIgnoreCase("MS2")) {
                            dataSourceShort = "ms2_files";
                        } else if (properties.getDataSource().equalsIgnoreCase("mzData")) {
                            dataSourceShort = "mzData";
                        } else if (properties.getDataSource().equalsIgnoreCase("TPP")) {
                            dataSourceShort = "tpp";
                        }

                        completeFileName = userProperties.getOutputPath() +
                                dataSourceShort + "_" + tempLabel + "_to_PRIDE_" +
                                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".xml";
                    }

                    BufferedWriter bw = new BufferedWriter(new FileWriter(completeFileName));

                    if (debug) {
                        System.out.println("XMLMarshaller begins.");
                    }

                    if (cancelConversion) {
                        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                        outputFrame.setConvertButtonEnabled(true);
                        return;
                    }

                    try {
                        progressDialog.setTitle("Creating XML File. Please Wait.");
                        progressDialog.setIntermidiate(true);
                    } catch (NullPointerException e) {
                        Util.writeToErrorLog("Progress bar: NullPointerException!!!\n" + e.toString());
                    }

                    // needs a lot of memory!
                    XMLMarshaller xmlOut = new XMLMarshaller(true);
                    xmlOut.marshallExperiments(experiments, bw);

                    bw.flush();
                    bw.close();

                    if (debug) {
                        System.out.println("XMLMarshaller done.");
                    }

                    try {
                        progressDialog.setTitle("Validating XML File. Please Wait.");
                        progressDialog.setIntermidiate(true);
                    } catch (NullPointerException e) {
                        Util.writeToErrorLog("Progress bar: NullPointerException!!!\n" + e.toString());
                    }

                    // validate the xml file
                    FileReader reader = new FileReader(completeFileName);
                    XMLValidationErrorHandler xmlErrors = PrideXmlValidator.validate(reader);

                    if (cancelConversion) {
                        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                        outputFrame.setConvertButtonEnabled(true);
                        return;
                    }

                    progressDialog.setVisible(false);
                    progressDialog.dispose();
                    outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                    xmlValidated = xmlErrors.noErrors();
                    reader.close();

                    if (!xmlValidated) {
                        Util.writeToErrorLog("PRIDE xml validation failed!");
                        Util.writeToErrorLog(xmlErrors.getErrorsFormattedAsPlainText());
                        JOptionPane.showMessageDialog(
                                outputFrame,
                                "PRIDE XML validation failed!\n\n" +
                                "See ../Properties/ErrorLog.txt for more details.",
                                "XML Validation Failed",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        if (debug) {
                            System.out.println("PRIDE xml validated.");
                        }

                        int spectraCount = -1;
                        int peptideIdentificationsCount = -1;

                        if (properties.getDataSource().equalsIgnoreCase("Mascot Generic File") ||
                                properties.getDataSource().equalsIgnoreCase("Mascot Dat File") ||
                                properties.getDataSource().equalsIgnoreCase("Sequest DTA File") ||
                                properties.getDataSource().equalsIgnoreCase("X!Tandem") ||
                                properties.getDataSource().equalsIgnoreCase("Micromass PKL File") ||
                                properties.getDataSource().equalsIgnoreCase("Spectrum Mill") ||
                                properties.getDataSource().equalsIgnoreCase("Sequest Result File") ||
                                properties.getDataSource().equalsIgnoreCase("mzXML") ||
                                properties.getDataSource().equalsIgnoreCase("OMSSA") ||
                                properties.getDataSource().equalsIgnoreCase("VEMS") ||
                                properties.getDataSource().equalsIgnoreCase("MS2") ||
                                properties.getDataSource().equalsIgnoreCase("mzData")) {
                            spectraCount = totalNumberOfSpectra -
                                    emptySpectraCounter;
                            peptideIdentificationsCount = omitDuplicates.size();
                        } else if (properties.getDataSource().equalsIgnoreCase("ms_lims")) {
                            spectraCount = unidentifedSpectraCounter +
                                    omitDuplicates.size();
                            peptideIdentificationsCount = omitDuplicates.size();
                        } else if (properties.getDataSource().equalsIgnoreCase("TPP")) {
                            spectraCount = totalNumberOfSpectra -
                                    emptySpectraCounter;
                            peptideIdentificationsCount = peptideIdCount;
                        }

                        // present a dialog with information about the created file
                        JOptionPane.showMessageDialog(outputFrame, "PRIDE XML File Created Successfully:\n" +
                                completeFileName +
                                "\n\nThe File Includes:\n" +
                                "Spectra: " + spectraCount + "\n" +
                                "Peptide Identifications: " + peptideIdentificationsCount + "\n" +
                                "Protein Identifications: " + identifications.size() + "\n\n" +
                                "PRIDE XML File Size: " +
                                roundDouble(((double) new File(completeFileName).length() /
                                properties.NUMBER_OF_BYTES_PER_MEGABYTE), 2) +
                                " MB",
                                "PRIDE XML File Created",
                                JOptionPane.INFORMATION_MESSAGE);

                        // insert the information into the OutputDetails frame
                        outputFrame.insertConvertedFileDetails(spectraCount,
                                peptideIdentificationsCount, identifications.size(),
                                roundDouble(((double) new File(completeFileName).length() /
                                properties.NUMBER_OF_BYTES_PER_MEGABYTE), 2),
                                completeFileName);
                    }
                } catch (OutOfMemoryError error) {
                    outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                    progressDialog.setVisible(false);
                    progressDialog.dispose();
                    Runtime.getRuntime().gc();
                    JOptionPane.showMessageDialog(null,
                            "The task used up all the available memory and had to be stopped.\n" +
                            "Memory boundaries are set in ../Properties/JavaOptions.txt.\n" +
                            "PRIDE XML file not created.\n\n" +
                            "If the data sets are too big for your computer, e-mail a support\n" +
                            "request to the PRIDE team at the EBI: pride-support@ebi.ac.uk",
                            "Out of Memory Error",
                            JOptionPane.ERROR_MESSAGE);

                    Util.writeToErrorLog("XML Parser: Ran out of memory!");
                    error.printStackTrace();
                    outputFrame.setConvertButtonEnabled(true);
                } catch (Exception ex) {

                    Util.writeToErrorLog("An error occured while trying to parse: " + currentFileName);
                    ex.printStackTrace();

                    JOptionPane.showMessageDialog(outputFrame,
                            "An error occured while trying to parse: " +
                            currentFileName + "\n\n" +
                            "See ../Properties/ErrorLog.txt for more details.\n" +
                            "PRIDE XML file not created.",
                            "Parsing Error",
                            JOptionPane.ERROR_MESSAGE);

                    outputFrame.setConvertButtonEnabled(true);
                    progressDialog.setVisible(false);
                    progressDialog.dispose();
                    cancelConversion = true;
                }

                outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

//                long end = System.currentTimeMillis();
//                System.out.println("Done: " + (end - start) + "\n");
            }
        }.start();

        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    /**
     * Rounds of a double value to the wanted number of decimalplaces
     *
     * @param d the double to round of
     * @param places number of decimal places wanted
     * @return double - the new double
     */
    public static double roundDouble(double d, int places) {
        return Math.round(d * Math.pow(10, (double) places)) / Math.pow(10, (double) places);
    }

    /**
     * Returns a UsertProperties object with all the user settings.
     * 
     * @return the user settings
     */
    public static UserProperties getUserProperties() {
        return userProperties;
    }

    /**
     * Returns a Properties object with all the settings.
     * 
     * @return the user settings
     */
    public static Properties getProperties() {
        return properties;
    }

    /**
     * Returns the ms_lims database connection.
     * 
     * @return the ms_lims database connection
     */
    public Connection getConn() {
        return conn;
    }

    /**
     * Adds a modification code (a.k.a. name) cvParam mapping.
     * 
     * @param modificationCode
     * @param cvParam
     */
    public void addModification(String modificationCode, CvParamImpl cvParam, Double modificationMonoMass) {

        CvParamImpl tempCvParam = cvParam;

        // if the modification mass is provided, use it
        if (modificationMonoMass != null) {
            tempCvParam = new CvParamImpl(
                    cvParam.getAccession(),
                    cvParam.getCVLookup(),
                    cvParam.getName(),
                    cvParam.getOrderIndex(),
                    modificationMonoMass.toString());
        }

        userProperties.getCVTermMappings().put(modificationCode, tempCvParam);
    }

    /**
     * Returns the version number of the PRIDE Converter.
     * 
     * @return the version number of the PRIDE Converter
     */
    public static String getPrideConverterVersionNumber() {
        return prideConverterVersionNumber;
    }

    /**
     * Returns the wizard name (e.g. PRIDE Converter)
     * 
     * @return the wizard name
     */
    public static String getWizardName() {
        return wizardName;
    }

    /**
     * Returns the true if the conversion is to be canceled.
     * 
     * @return true if the conversion is to be canceled
     */
    public static boolean isConversionCanceled() {
        return cancelConversion;
    }

    /**
     * Set the cancel conversion variable. Doing this will in the end cancel 
     * the conversion.
     * 
     * @param cancel
     */
    public static void setCancelConversion(boolean cancel) {
        cancelConversion = cancel;
    }

    /**
     * Adds zeros to the begining of the ms_lims project id so that all project 
     * ids will have equal length, making sortig on file name easier.
     * 
     * @param id the id to zero fill
     * @return the zero filled id
     */
    private String zerofill(long id) {
        String result = "" + id;

        while (result.length() < properties.ZERO_FILLED_NUMBER_LENGTH) {
            result = "0" + result;
        }

        return result;
    }

    /**
     * This method transforms spectra from X!Tandem files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aSpectra   Collection with the Spectrumfile instances to to transform.
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     */
    private static HashMap transformSpectraFromXTandemDataFile(ArrayList aTransformedSpectra) throws IOException {

        // note: the xml parsing in this class is far from perfect and should be reimplemented

        HashMap mapping = new HashMap();

        double[][] arrays;
        Collection precursors;
        Collection ionSelection;

        int charge;
        double intensity;

        File file;
        FileReader f;
        BufferedReader b;

        StringTokenizer tokXValues, tokYValues;
        Double precursorMass;
        Integer precursorCharge;

        NodeList idNodes,
                proteinNodes, peptideNodes, traceNodes, spectrumNodes,
                xDataNodes, yDataNodes;
        DocumentBuilderFactory dbf;
        DocumentBuilder db;
        Document dom;
        Element docEle;
        String tempXValue,
                tempYValue;

        int spectraCounter = 1;
        String fileName;
        boolean matchFound = false;
        NodeList nodes;
        int spectrumID = -1;
        precursorMass = 0.0;
        precursorCharge = 0;
        double hyperscore = 0.0;

        String upstreamFlankingSequence = null;
        String downstreamFlankingSequence = null;

        String label = "";
        int start = -1;
        String peptideSequence = "";
        String xValues = "", yValues = "";
        int numberOfValues = 0;
        int counter;
        ids = new ArrayList<IdentificationGeneral>();

        String modificationName;
        String modificationLocation;
        double modificationMass;
        NamedNodeMap modificationMap;
        ArrayList modificationCVParams, monoMasses, peptideModifications;

        ArrayList cVParams, userParams;
        Collection spectrumDescriptionComments;
        String identified;
        Spectrum fragmentation;

        Double mzRangeStart;
        Double mzRangeStop;

        progressDialog.setIntermidiate(false);
        progressDialog.setValue(0);

        for (int j = 0; j < properties.getSelectedSourceFiles().size() && !cancelConversion; j++) {

            file = new File(properties.getSelectedSourceFiles().get(j));
            fileName = file.getName();
            currentFileName = fileName;

            progressDialog.setValue(0);
            progressDialog.setString(currentFileName + " (" + (j + 1) + "/" +
                    properties.getSelectedSourceFiles().size() + ")");

            try {
                //get the factory
                dbf = DocumentBuilderFactory.newInstance();

                //Using factory get an instance of document builder
                db = dbf.newDocumentBuilder();

                //parse using builder to get DOM representation of the XML file
                dom = db.parse(file);

                //get the root elememt
                docEle = dom.getDocumentElement();

                nodes = docEle.getChildNodes();

                spectrumID = -1;
                precursorMass = 0.0;
                precursorCharge = 0;
                hyperscore = 0.0;

                upstreamFlankingSequence = null;
                downstreamFlankingSequence = null;

                progressDialog.setMax(nodes.getLength());

                for (int i = 0; i < nodes.getLength(); i++) {

                    progressDialog.setValue(i);

                    if (nodes.item(i).getAttributes() != null) {

                        if (nodes.item(i).getAttributes().getNamedItem("type") != null) {

                            peptideModifications = null;

                            if (nodes.item(i).getAttributes().getNamedItem("type").
                                    getNodeValue().equalsIgnoreCase("model")) {

                                if (nodes.item(i).getAttributes().getNamedItem("id") != null) {
                                    spectrumID = new Integer(
                                            nodes.item(i).getAttributes().getNamedItem("id").getNodeValue()).intValue();
                                }

                                if (nodes.item(i).getAttributes().getNamedItem("mh") != null) {
                                    precursorMass = new Double(
                                            nodes.item(i).getAttributes().getNamedItem("mh").getNodeValue()).doubleValue();
                                }

                                if (nodes.item(i).getAttributes().getNamedItem("z") != null) {
                                    precursorCharge = new Integer(
                                            nodes.item(i).getAttributes().getNamedItem("z").getNodeValue()).intValue();
                                }

                                if (nodes.item(i).getAttributes().getNamedItem("label") != null) {
                                    label = nodes.item(i).getAttributes().getNamedItem("label").getNodeValue();
                                }

                                matchFound = false;

                                if (properties.getSelectedSpectraNames().size() > 0) {
                                    for (int k = 0; k < properties.getSelectedSpectraNames().size() &&
                                            !matchFound; k++) {
                                        if (((String) ((Object[]) properties.getSelectedSpectraNames().
                                                get(k))[0]).equalsIgnoreCase(fileName)) {
                                            if (((Integer) ((Object[]) properties.getSelectedSpectraNames().
                                                    get(k))[1]).intValue() == spectrumID) {
                                                matchFound = true;
                                            }
                                        }
                                    }
                                } else {
                                    matchFound = true;
                                }

                                if (matchFound) {

                                    idNodes = nodes.item(i).getChildNodes();

                                    for (int k = 0; k < idNodes.getLength(); k++) {

                                        if (idNodes.item(k).getNodeName().equalsIgnoreCase("protein")) {

                                            proteinNodes = idNodes.item(k).getChildNodes();

                                            for (int m = 0; m < proteinNodes.getLength(); m++) {

                                                if (proteinNodes.item(m).getNodeName().equalsIgnoreCase("peptide")) {

                                                    peptideNodes = proteinNodes.item(m).getChildNodes();

                                                    for (int n = 0; n < peptideNodes.getLength(); n++) {

                                                        if (peptideNodes.item(n).getNodeName().equalsIgnoreCase("domain")) {

                                                            start = new Integer(
                                                                    peptideNodes.item(n).getAttributes().getNamedItem("start").getNodeValue()).intValue();
                                                            peptideSequence =
                                                                    peptideNodes.item(n).getAttributes().getNamedItem("seq").getNodeValue();

                                                            hyperscore = new Double(
                                                                    peptideNodes.item(n).getAttributes().getNamedItem("hyperscore").getNodeValue()).doubleValue();

                                                            upstreamFlankingSequence =
                                                                    peptideNodes.item(n).getAttributes().getNamedItem("pre").getNodeValue();

                                                            downstreamFlankingSequence =
                                                                    peptideNodes.item(n).getAttributes().getNamedItem("post").getNodeValue();

                                                            peptideModifications = new ArrayList();
                                                            modificationCVParams = new ArrayList();

                                                            for (int c = 0; c < peptideNodes.item(n).getChildNodes().getLength(); c++) {

                                                                if (peptideNodes.item(n).getChildNodes().item(c).getNodeName().equalsIgnoreCase("aa")) {

                                                                    modificationMap = peptideNodes.item(n).getChildNodes().item(c).getAttributes();

                                                                    modificationName =
                                                                            modificationMap.getNamedItem("type").getNodeValue();
                                                                    modificationLocation = "" +
                                                                            (new Integer(modificationMap.getNamedItem("at").getNodeValue()).intValue() -
                                                                            start + 1);
                                                                    modificationMass =
                                                                            new Double(
                                                                            modificationMap.getNamedItem("modified").getNodeValue()).doubleValue();

                                                                    modificationName += "=" + modificationMass;

                                                                    if (!properties.getAlreadyChoosenModifications().contains(modificationName)) {
                                                                        new ModificationMapping(outputFrame,
                                                                                true, progressDialog,
                                                                                modificationName,
                                                                                "-",
                                                                                modificationMass,
                                                                                (CvParamImpl) userProperties.getCVTermMappings().
                                                                                get(modificationName));

                                                                        properties.getAlreadyChoosenModifications().add(modificationName);
                                                                    } else {
                                                                        //do nothing, mapping already choosen
                                                                    }

                                                                    CvParamImpl tempCvParam =
                                                                            (CvParamImpl) userProperties.getCVTermMappings().get(modificationName);


                                                                    modificationCVParams.add(tempCvParam);

                                                                    monoMasses = new ArrayList();
                                                                    // get the modification mass (DiffMono) retrieved from PSI-MOD
                                                                    if (tempCvParam.getValue() != null) {
                                                                        monoMasses.add(new MonoMassDeltaImpl(
                                                                                new Double(tempCvParam.getValue()).doubleValue()));
                                                                    } else {
                                                                        // if the DiffMono is not found for the PSI-MOD the mass 
                                                                        // from the file is used
                                                                        monoMasses.add(new MonoMassDeltaImpl(modificationMass));
                                                                    //monoMasses = null;
                                                                    }

                                                                    peptideModifications.add(new ModificationImpl(
                                                                            tempCvParam.getAccession(),
                                                                            new Integer(modificationLocation),
                                                                            tempCvParam.getCVLookup(),
                                                                            null,
                                                                            monoMasses,
                                                                            null,
                                                                            modificationCVParams,
                                                                            null));
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else if (idNodes.item(k).getNodeName().
                                                equalsIgnoreCase(
                                                "group")) {
                                            if (idNodes.item(k).getAttributes().
                                                    getNamedItem("label").
                                                    getNodeValue().
                                                    equalsIgnoreCase(
                                                    "fragment ion mass spectrum")) {

                                                traceNodes = idNodes.item(k).getChildNodes();

                                                for (int m = 0; m < traceNodes.getLength(); m++) {

                                                    if (traceNodes.item(m).getNodeName().equalsIgnoreCase(
                                                            "GAML:trace")) {

                                                        spectrumNodes = traceNodes.item(m).getChildNodes();

                                                        for (int n = 0; n < spectrumNodes.getLength(); n++) {

                                                            if (spectrumNodes.item(n).getNodeName().equalsIgnoreCase(
                                                                    "GAML:Xdata")) {

                                                                xDataNodes = spectrumNodes.item(n).getChildNodes();

                                                                for (int p = 0; p < xDataNodes.getLength(); p++) {

                                                                    if (xDataNodes.item(p).getNodeName().equalsIgnoreCase(
                                                                            "GAML:values")) {

                                                                        numberOfValues = new Integer(
                                                                                xDataNodes.item(p).getAttributes().getNamedItem(
                                                                                "numvalues").getNodeValue());

                                                                        xValues = xDataNodes.item(p).getTextContent();
                                                                    }
                                                                }
                                                            } else if (spectrumNodes.item(n).getNodeName().equalsIgnoreCase("GAML:Ydata")) {

                                                                yDataNodes = spectrumNodes.item(n).getChildNodes();

                                                                for (int p = 0; p < yDataNodes.getLength(); p++) {

                                                                    if (yDataNodes.item(p).getNodeName().equalsIgnoreCase(
                                                                            "GAML:values")) {

                                                                        yValues = yDataNodes.item(p).getTextContent();
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Transform peaklist into double arrays.
                                    arrays = new double[2][numberOfValues];
                                    tokXValues = new StringTokenizer(xValues);
                                    tokYValues = new StringTokenizer(yValues);

                                    counter = 0;

                                    while (tokXValues.hasMoreTokens()) {

                                        tempXValue = tokXValues.nextToken();
                                        tempYValue = tokYValues.nextToken();

                                        arrays[0][counter] = new Double(tempXValue).doubleValue();
                                        arrays[1][counter++] = new Double(tempYValue).doubleValue();
                                    }

                                    //calculate itraq values
                                    iTRAQ iTRAQValues = null;

                                    if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {

                                        iTRAQValues = new iTRAQ(arrays, precursorMass, precursorCharge,
                                                userProperties.getPeakIntegrationRangeLower(),
                                                userProperties.getPeakIntegrationRangeUpper(),
                                                userProperties.getReporterIonIntensityThreshold(),
                                                userProperties.getPurityCorrections());
                                        iTRAQValues.calculateiTRAQValues();
                                    }

                                    String[] iTraqNorm = null;
                                    String[] iTraqUT = null;
                                    String[][] iTraqRatio = null;

                                    if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
                                        //iTraq-stuff
                                        iTraqNorm = (String[]) iTRAQValues.getAllNorms().get(0);
                                        iTraqUT = (String[]) iTRAQValues.getAllUTs().get(0);
                                        iTraqRatio = (String[][]) iTRAQValues.getAllRatios().get(0);
                                    }

                                    cVParams = new ArrayList();
                                    cVParams.add(new CvParamImpl("PRIDE:0000176",
                                            "PRIDE", "X!Tandem Hyperscore",
                                            cVParams.size(), "" + hyperscore));

                                    if (upstreamFlankingSequence != null) {
                                        cVParams.add(new CvParamImpl("PRIDE:0000065", "PRIDE",
                                                "Upstream flanking sequence", cVParams.size(),
                                                "" + upstreamFlankingSequence.toUpperCase()));
                                    }
                                    if (downstreamFlankingSequence != null) {
                                        cVParams.add(new CvParamImpl("PRIDE:0000066", "PRIDE",
                                                "Downstream flanking sequence", cVParams.size(),
                                                "" + downstreamFlankingSequence.toUpperCase()));
                                    }

                                    if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
                                        cVParams = addItraqCVTerms(cVParams, iTraqNorm);
                                        userParams = addItraqUserTerms(iTraqRatio);
                                    } else {
                                        userParams = null;
                                    }

                                    if (hyperscore >= properties.getPeptideScoreThreshold()) {

                                        ids.add(new IdentificationGeneral(
                                                "" + spectraCounter,//spectrumFileID
                                                label, //accession
                                                "X!Tandem", //search engine
                                                null, //database
                                                null, //database version
                                                peptideSequence.toUpperCase(), //sequence
                                                start, //start
                                                hyperscore, //score
                                                null, //threshold
                                                iTraqNorm, iTraqUT, iTraqRatio,
                                                cVParams, userParams, peptideModifications));
                                    }

                                    // Precursor collection.
                                    precursors = new ArrayList(1);

                                    // Ion selection parameters.
                                    ionSelection = new ArrayList(3);

                                    // See if we know the precursor charge, and if so, include it.
                                    charge = precursorCharge;
                                    if (charge > 0) {
                                        ionSelection.add(
                                                new CvParamImpl("PSI:1000041",
                                                "PSI", "ChargeState", 0,
                                                Integer.toString(charge)));
                                    }

                                    intensity = 0;//no intensity found for X!Tandem data (?)
                                    if (intensity > 1) {
                                        ionSelection.add(new CvParamImpl("PSI:1000042",
                                                "PSI", "Intensity", 1,
                                                Double.toString(intensity)));
                                    }

                                    ionSelection.add(new CvParamImpl("PSI:1000040",
                                            "PSI", "MassToChargeRatio", 2, Double.toString(
                                            precursorMass)));

                                    precursors.add(new PrecursorImpl(/*activation*/null,
                                            null, ionSelection, null, 1,
                                            0, 0));

                                    // Spectrum description comments.
                                    spectrumDescriptionComments = new ArrayList(1);

                                    identified = "Identified";
                                    spectrumDescriptionComments.add(new SpectrumDescCommentImpl(identified));

                                    if (arrays[properties.MZ_ARRAY].length > 0) {
                                        mzRangeStart = new Double(arrays[properties.MZ_ARRAY][0]);
                                        mzRangeStop = new Double(
                                                arrays[properties.MZ_ARRAY][arrays[properties.MZ_ARRAY].length - 1]);


                                        // Create new mzData spectrum for the fragmentation spectrum.
                                        fragmentation = new SpectrumImpl(
                                                new BinaryArrayImpl(arrays[properties.INTENSITIES_ARRAY],
                                                BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                                mzRangeStart,
                                                new BinaryArrayImpl(arrays[properties.MZ_ARRAY],
                                                BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                                2l, null,
                                                mzRangeStop,
                                                null,
                                                spectraCounter, precursors,
                                                spectrumDescriptionComments,
                                                null, null, null, null);

                                        // Store (spectrumfileid, spectrumid) mapping.
                                        mapping.put("" + spectraCounter,
                                                new Long(spectraCounter));

                                        spectraCounter++;

                                        // Store the transformed spectrum.
                                        aTransformedSpectra.add(fragmentation);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(null, "The file named " +
                        file.getPath() +
                        "\ncould not be found.",
                        "File Not Found", JOptionPane.ERROR_MESSAGE);
                Util.writeToErrorLog("File not found: ");
                ex.printStackTrace();
            } catch (Exception e) {
                if (!cancelConversion) {
                    Util.writeToErrorLog("Error parsing X!Tandem file: " + e.toString());
                    e.printStackTrace();

                    JOptionPane.showMessageDialog(null,
                            "The following file could not parsed as an X!Tandem file:\n " +
                            file.getPath() + "\n\n" +
                            "See ../Properties/ErrorLog.txt for more details.",
                            "Error Parsing File", JOptionPane.ERROR_MESSAGE);
                }
            }

            totalNumberOfSpectra = spectraCounter - 1;
        }

        return mapping;
    }

    /**
     * This method transforms spectra from Spectrum Mill and Sequest files, 
     * returning a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aSpectra   Collection with the Spectrumfile instances to to transform.
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     */
    private static HashMap transformSpectraFromSequestAndSpectrumMill(
            ArrayList aTransformedSpectra) throws IOException {

        HashMap mapping = new HashMap();

        double[][] arrays = null;
        Collection precursors;
        Collection ionSelection;

        int charge;
        String fileName;

        ids = new ArrayList<IdentificationGeneral>();
        StringTokenizer tok;
        String accession = null, database = null, sequence = null, prospectorVersion = null;
        String coverage_map = null;
        Integer start = null;
        Double score = null;
        Double deltaCn = null;
        Double sp = null;
        String rankSp = null;
        Double mH = null;
        String ions = null;
        String upstreamFlankingSequence = null;
        String downstreamFlankingSequence = null;
        int spectraCounter = 1;
        String currentLine;
        Vector columnHeaders;

        String modificationLine;
        Vector allLines;
        boolean hitFound;

        Pattern pattern;
        Matcher matcher;
        String modificationName, currentModification;
        HashMap modifications = new HashMap();

        Integer modificationLocation;
        String modificationNameShort;
        double modificationMass;
        ArrayList modificationCVParams, monoMasses, peptideModifications;

        boolean errorDetected;
        FileReader f;
        BufferedReader b;
        boolean emptyLineFound;
        double precursorMass;
        double precursorIntensty;
        int precursorCharge;
        Vector masses;
        Vector intensities;
        File identificationFile;
        Collection spectrumDescriptionComments;
        String identified;
        boolean matchFound;
        int numberOfHits;
        int index;
        HashMap<String, Integer> spectrumMillColumnHeaders;
        Vector<String> spectrumMillValues;
        StringTokenizer startTok;
        StringTokenizer modTok;
        String currentMod;
        int currentIndex;
        ArrayList cVParams, userParams;
        String tempFileName;
        String variableModificationPattern = "[(]\\w.[ ][+-]\\d*[.]\\d{4}[)]";
        String fixedModificationPattern = "\\w[=]\\d*[.]\\d{4}";
        boolean modificationsDetected;
        String[] sequenceArray;
        int location;
        Spectrum fragmentation;
        String[] values;
        Double mzRangeStart;
        Double mzRangeStop;

        progressDialog.setIntermidiate(false);
        progressDialog.setValue(0);
        progressDialog.setMax(properties.getSelectedSourceFiles().size());

        for (int j = 0; j < properties.getSelectedSourceFiles().size() && !cancelConversion; j++) {

            fileName = properties.getSelectedSourceFiles().get(j).substring(
                    properties.getSelectedSourceFiles().get(j).lastIndexOf(File.separator) + 1);
            currentFileName = fileName;

            progressDialog.setValue(j);
            progressDialog.setString(currentFileName + " (" + (j + 1) + "/" +
                    properties.getSelectedSourceFiles().size() + ")");

            errorDetected = false;

            boolean isSelected = false;

            for (int k = 0; k < properties.getSelectedSpectraNames().size() &&
                    !isSelected && !cancelConversion; k++) {

                Object[] temp = (Object[]) properties.getSelectedSpectraNames().get(k);

                if (((String) temp[0]).equalsIgnoreCase(currentFileName)) {
                    isSelected = true;
                }
            }

            if (properties.getSelectedSpectraNames().size() == 0) {
                isSelected = true;
            }

            if (isSelected) {

                precursorMass = -1.0;
                precursorIntensty = -1.0;
                precursorCharge = -1;

                try {
                    f = new FileReader(new File(properties.getSelectedSourceFiles().get(j)));
                    b = new BufferedReader(f);

                    currentLine = b.readLine();

                    if (properties.isCommaTheDecimalSymbol()) {
                        currentLine = currentLine.replaceAll(",", ".");
                    }

                    tok = new StringTokenizer(currentLine);

                    precursorMass = new Double(tok.nextToken()).doubleValue();
                    precursorIntensty = -1;

                    //intensity not included in pkl files
                    if (properties.getDataSource().equalsIgnoreCase("Spectrum Mill")) {
                        precursorIntensty = new Double(tok.nextToken()).doubleValue();
                    }

                    precursorCharge = new Integer(tok.nextToken()).intValue();

                    currentLine = b.readLine();

                    masses = new Vector();
                    intensities = new Vector();

                    emptyLineFound = false;

                    while (currentLine != null && !emptyLineFound) {

                        if (currentLine.equalsIgnoreCase("")) {
                            emptyLineFound = true;
                        //System.out.println("Empty line found!!");
                        } else {

                            if (properties.isCommaTheDecimalSymbol()) {
                                currentLine = currentLine.replaceAll(",", ".");
                            }

                            tok = new StringTokenizer(currentLine);
                            masses.add(new Double(tok.nextToken()));
                            intensities.add(new Double(tok.nextToken()));

                            currentLine = b.readLine();
                        }
                    }

                    arrays = new double[2][masses.size()];

                    for (int i = 0; i < masses.size(); i++) {
                        arrays[0][i] = ((Double) masses.get(i)).doubleValue();
                        arrays[1][i] = ((Double) intensities.get(i)).doubleValue();
                    }

                    b.close();
                    f.close();
                } catch (Exception e) {

                    errorDetected = true;
                    String fileType = "";

                    if (properties.getDataSource().equalsIgnoreCase("Sequest Result File") ||
                            properties.getDataSource().equalsIgnoreCase("Sequest DTA File")) {
                        fileType = "Sequest DTA file";
                    } else if (properties.getDataSource().equalsIgnoreCase("Spectrum Mill")) {
                        fileType = "Micromass PKL file";
                    }

                    if (!cancelConversion) {

                        Util.writeToErrorLog("Error parsing " + fileType + ": ");
                        e.printStackTrace();

                        JOptionPane.showMessageDialog(null,
                                "The following file could not parsed as a " +
                                fileType + ":\n " +
                                new File(properties.getSelectedSourceFiles().get(j)).getName() +
                                "\n\n" +
                                "See ../Properties/ErrorLog.txt for more details.",
                                "Error Parsing File", JOptionPane.ERROR_MESSAGE);
                    }
                }

                if (!errorDetected) {

                    identificationFile = null;

                    try {
                        // Spectrum description comments.
                        spectrumDescriptionComments = new ArrayList(1);

                        identified = "Not identified";

                        matchFound = false;

                        //have to check if it's identified or not
                        if (properties.getDataSource().equalsIgnoreCase("Spectrum Mill")) {

                            identificationFile = new File("");

                            for (int i = 0; i < properties.getSelectedIdentificationFiles().size() &&
                                    !matchFound && !cancelConversion; i++) {

                                tempFileName = properties.getSelectedIdentificationFiles().get(i);

                                tempFileName = tempFileName.substring(
                                        tempFileName.lastIndexOf(File.separator) + 1);

                                matchFound = tempFileName.equalsIgnoreCase(
                                        currentFileName + ".spo");

                                if (matchFound) {
                                    identificationFile = new File(properties.getSelectedIdentificationFiles().get(i));
                                }
                            }

                            if (matchFound) {
                                identified = "Identified";

                                accession = null;
                                database = null;
                                sequence = null;
                                start = null;
                                score = null;
                                prospectorVersion = null;
                                deltaCn = null;
                                sp = null;
                                ions = null;
                                upstreamFlankingSequence = null;
                                downstreamFlankingSequence = null;
                                rankSp = null;
                                mH = null;

                                peptideModifications = new ArrayList();

                                f = new FileReader(identificationFile);
                                b = new BufferedReader(f);

                                currentLine = b.readLine();

                                while (currentLine != null) {

                                    if (currentLine.startsWith("database") &&
                                            !currentLine.startsWith("database_date")) {
                                        tok = new StringTokenizer(currentLine);
                                        tok.nextToken();
                                        database = tok.nextToken();
                                    } else if (currentLine.startsWith("prospector_version")) {
                                        tok = new StringTokenizer(currentLine);
                                        tok.nextToken();
                                        prospectorVersion = tok.nextToken();
                                    } else if (currentLine.startsWith("num_hits_to_report")) {
                                        tok = new StringTokenizer(currentLine);

                                        tok.nextToken();
                                        numberOfHits = new Integer(tok.nextToken()).intValue();

                                        if (numberOfHits > 0) {

                                            currentLine = b.readLine();
                                            tok = new StringTokenizer(currentLine, "\t");

                                            index = 0;

                                            spectrumMillColumnHeaders = new HashMap<String, Integer>();

                                            while (tok.hasMoreTokens()) {
                                                spectrumMillColumnHeaders.put(tok.nextToken(), new Integer(index++));
                                            }

                                            modificationName = null;
                                            modificationLocation = null;
                                            modificationNameShort = null;

                                            modificationCVParams = new ArrayList();

                                            currentLine = b.readLine();
                                            tok = new StringTokenizer(currentLine, "\t");

                                            spectrumMillValues = new Vector();

                                            while (tok.hasMoreTokens()) {
                                                spectrumMillValues.add(tok.nextToken());
                                            }

                                            score = new Double(spectrumMillValues.get(
                                                    spectrumMillColumnHeaders.get("score").intValue()));

                                            upstreamFlankingSequence = spectrumMillValues.get(
                                                    spectrumMillColumnHeaders.get("previous_aa").intValue());

                                            sequence = spectrumMillValues.get(
                                                    spectrumMillColumnHeaders.get("sequence").intValue());

                                            downstreamFlankingSequence = spectrumMillValues.get(
                                                    spectrumMillColumnHeaders.get("next_aa").intValue());

                                            coverage_map = spectrumMillValues.get(
                                                    spectrumMillColumnHeaders.get("coverage_map"));

                                            startTok = new StringTokenizer(coverage_map, "+");
                                            startTok.nextToken();

                                            start = new Integer(startTok.nextToken()) + 1;

                                            if (spectrumMillColumnHeaders.containsKey("varMods")) {

                                                modificationName = spectrumMillValues.get(
                                                        spectrumMillColumnHeaders.get("varMods").intValue());

                                                if (!modificationName.equalsIgnoreCase(" ")) {

                                                    modTok = new StringTokenizer(modificationName);

                                                    while (modTok.hasMoreTokens() && !cancelConversion) {

                                                        currentMod = modTok.nextToken();

                                                        modificationNameShort =
                                                                currentMod.substring(0,
                                                                currentMod.indexOf(":"));
                                                        modificationName = currentMod.substring(
                                                                currentMod.indexOf(":") + 1);

                                                        currentIndex = 0;

                                                        while (sequence.indexOf(modificationNameShort, currentIndex) !=
                                                                -1 && !cancelConversion) {

                                                            modificationLocation = new Integer(
                                                                    sequence.indexOf(
                                                                    modificationNameShort, currentIndex) + 1);

                                                            currentIndex = sequence.indexOf(
                                                                    modificationNameShort, currentIndex) + 1;

                                                            if (!properties.getAlreadyChoosenModifications().
                                                                    contains(modificationName)) {
                                                                new ModificationMapping(outputFrame,
                                                                        true, progressDialog,
                                                                        modificationName,
                                                                        modificationNameShort.toUpperCase(),
                                                                        null,
                                                                        (CvParamImpl) userProperties.getCVTermMappings().
                                                                        get(modificationName));

                                                                properties.getAlreadyChoosenModifications().
                                                                        add(modificationName);
                                                            } else {
                                                                //do nothing, mapping already choosen
                                                            }

                                                            CvParamImpl tempCvParam =
                                                                    (CvParamImpl) userProperties.getCVTermMappings().get(modificationName);

                                                            modificationCVParams = new ArrayList();
                                                            modificationCVParams.add(tempCvParam);

                                                            monoMasses = new ArrayList();

                                                            // get the modification mass (DiffMono) retrieved from PSI-MOD
                                                            if (tempCvParam.getValue() != null) {
                                                                monoMasses.add(new MonoMassDeltaImpl(
                                                                        new Double(tempCvParam.getValue()).doubleValue()));
                                                            } else {
                                                                monoMasses = null;
                                                            }


                                                            peptideModifications.add(new ModificationImpl(
                                                                    tempCvParam.getAccession(),
                                                                    modificationLocation,
                                                                    tempCvParam.getCVLookup(),
                                                                    null,
                                                                    monoMasses,
                                                                    null,
                                                                    modificationCVParams,
                                                                    null));
                                                        }
                                                    }
                                                }
                                            }

                                            modificationName = null;
                                            modificationLocation = null;

                                            if (spectrumMillColumnHeaders.containsKey("fixedMods") &&
                                                    !cancelConversion) {
                                                modificationName = spectrumMillValues.get(
                                                        spectrumMillColumnHeaders.get("fixedMods").
                                                        intValue());

                                                if (!modificationName.equalsIgnoreCase(" ")) {

                                                    modTok = new StringTokenizer(modificationName);

                                                    while (modTok.hasMoreTokens() && !cancelConversion) {

                                                        currentMod = modTok.nextToken();

                                                        modificationNameShort =
                                                                currentMod.substring(0,
                                                                currentMod.indexOf(":"));
                                                        modificationName = currentMod.substring(
                                                                currentMod.indexOf(":") + 1);

                                                        currentIndex = 0;

                                                        while (sequence.indexOf(modificationNameShort,
                                                                currentIndex) !=
                                                                -1 && !cancelConversion) {

                                                            modificationLocation = new Integer(
                                                                    sequence.indexOf(
                                                                    modificationNameShort, currentIndex) + 1);
                                                            currentIndex = sequence.indexOf(
                                                                    modificationNameShort, currentIndex) + 1;

                                                            if (!properties.getAlreadyChoosenModifications().
                                                                    contains(modificationName)) {
                                                                new ModificationMapping(outputFrame,
                                                                        true, progressDialog,
                                                                        modificationName,
                                                                        modificationNameShort.toUpperCase(),
                                                                        null,
                                                                        (CvParamImpl) userProperties.getCVTermMappings().
                                                                        get(modificationName));

                                                                properties.getAlreadyChoosenModifications().
                                                                        add(modificationName);
                                                            } else {
                                                                //do nothing, mapping already choosen
                                                            }

                                                            CvParamImpl tempCvParam =
                                                                    (CvParamImpl) userProperties.getCVTermMappings().get(modificationName);

                                                            modificationCVParams = new ArrayList();
                                                            modificationCVParams.add(tempCvParam);

                                                            monoMasses = new ArrayList();

                                                            // get the modification mass (DiffMono) retrieved from PSI-MOD
                                                            if (tempCvParam.getValue() != null) {
                                                                monoMasses.add(new MonoMassDeltaImpl(
                                                                        new Double(tempCvParam.getValue()).doubleValue()));
                                                            } else {
                                                                monoMasses = null;
                                                            }

                                                            peptideModifications.add(new ModificationImpl(
                                                                    tempCvParam.getAccession(),
                                                                    modificationLocation,
                                                                    tempCvParam.getCVLookup(),
                                                                    null,
                                                                    monoMasses,
                                                                    null,
                                                                    modificationCVParams,
                                                                    null));
                                                        }
                                                    }
                                                }
                                            }

                                            precursorCharge = new Integer(spectrumMillValues.get(
                                                    spectrumMillColumnHeaders.get("parent_charge").intValue()));

                                            accession = spectrumMillValues.get(
                                                    spectrumMillColumnHeaders.get("accession_number").intValue());
                                        }
                                    }

                                    currentLine = b.readLine();
                                }

                                b.close();
                                f.close();

                                //calculate itraq values
                                iTRAQ iTRAQValues = null;

                                if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
                                    iTRAQValues = new iTRAQ(arrays, precursorMass,
                                            precursorCharge,
                                            userProperties.getPeakIntegrationRangeLower(),
                                            userProperties.getPeakIntegrationRangeUpper(),
                                            userProperties.getReporterIonIntensityThreshold(),
                                            userProperties.getPurityCorrections());
                                    iTRAQValues.calculateiTRAQValues();
                                }

                                String[] iTraqNorm = null;
                                String[] iTraqUT = null;
                                String[][] iTraqRatio = null;

                                if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
                                    //iTraq-stuff
                                    iTraqNorm = (String[]) iTRAQValues.getAllNorms().get(0);
                                    iTraqUT = (String[]) iTRAQValues.getAllUTs().get(0);
                                    iTraqRatio = (String[][]) iTRAQValues.getAllRatios().get(0);
                                }

                                cVParams = new ArrayList();

                                cVParams.add(new CvParamImpl("PRIDE:0000177",
                                        "PRIDE", "Spectrum Mill Peptide Score",
                                        cVParams.size(), "" +
                                        score));

                                if (upstreamFlankingSequence != null) {

                                    upstreamFlankingSequence = upstreamFlankingSequence.trim().
                                            substring(1, 2);

                                    if (!upstreamFlankingSequence.equalsIgnoreCase("-")) {
                                        cVParams.add(new CvParamImpl("PRIDE:0000065",
                                                "PRIDE",
                                                "Upstream flanking sequence",
                                                cVParams.size(),
                                                "" +
                                                upstreamFlankingSequence.toUpperCase()));
                                    }
                                }

                                if (downstreamFlankingSequence != null) {

                                    downstreamFlankingSequence = downstreamFlankingSequence.trim().substring(1, 2);

                                    if (!downstreamFlankingSequence.equalsIgnoreCase("-")) {
                                        cVParams.add(new CvParamImpl("PRIDE:0000066",
                                                "PRIDE",
                                                "Downstream flanking sequence",
                                                cVParams.size(),
                                                "" +
                                                downstreamFlankingSequence.toUpperCase()));
                                    }
                                }

                                if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
                                    cVParams = addItraqCVTerms(cVParams, iTraqNorm);
                                    userParams = addItraqUserTerms(iTraqRatio);
                                } else {
                                    userParams = null;
                                }

                                if (score.doubleValue() >= properties.getPeptideScoreThreshold()) {

                                    ids.add(new IdentificationGeneral(
                                            new File(properties.getSelectedSourceFiles().
                                            get(j)).getName(),//spectrumFileID
                                            accession, //accession
                                            "Spectrum Mill",//search engine
                                            database, //database
                                            null, //databaseversion
                                            sequence.toUpperCase(), //sequence
                                            start, //start
                                            score, //score
                                            null, //threshold
                                            iTraqNorm, iTraqUT, iTraqRatio,
                                            cVParams, userParams, peptideModifications));
                                }
                            }
                        } else if (properties.getDataSource().equalsIgnoreCase("Sequest Result File")) {

                            identificationFile = new File("");

                            for (int i = 0; i <
                                    properties.getSelectedIdentificationFiles().size() &&
                                    !matchFound && !cancelConversion; i++) {

                                tempFileName = properties.getSelectedIdentificationFiles().get(i);

                                tempFileName = tempFileName.substring(
                                        tempFileName.lastIndexOf(File.separator) + 1,
                                        tempFileName.length() - 4) + ".dta";

                                matchFound = tempFileName.equalsIgnoreCase(
                                        currentFileName);

                                if (matchFound) {
                                    identificationFile = new File(properties.getSelectedIdentificationFiles().get(i));
                                }
                            }

                            if (matchFound) {
                                identified = "Identified";

                                accession = null;
                                database = null;
                                sequence = null;
                                start = null;
                                score = null;
                                prospectorVersion = null;
                                sp = null;
                                ions = null;
                                upstreamFlankingSequence = null;
                                downstreamFlankingSequence = null;
                                rankSp = null;
                                mH = null;
                                peptideModifications = null;

                                f = new FileReader(identificationFile);
                                b = new BufferedReader(f);

                                currentLine = b.readLine();

                                allLines = new Vector();
                                allLines.add(currentLine);

                                String tempString;
                                columnHeaders = new Vector();

                                hitFound = false;

                                while (currentLine != null && !hitFound) {

                                    allLines.add(currentLine);

                                    // extract the name of the database
                                    if (currentLine.lastIndexOf("# amino acids") != -1) {

                                        if (currentLine.lastIndexOf("\\") != -1) {
                                            database = currentLine.substring(currentLine.lastIndexOf("\\") + 1);
                                        } else if (currentLine.lastIndexOf("/") != -1) {
                                            database = currentLine.substring(currentLine.lastIndexOf("/") + 1);
                                        } else if (currentLine.lastIndexOf(" ") != -1) {
                                            database = currentLine.substring(currentLine.lastIndexOf(" ") + 1);
                                        } else {
                                            database = null;
                                        }
                                    }

                                    tempString = currentLine.trim();
                                    tempString = tempString.replaceAll(" ", "");

                                    if (tempString.startsWith("#Rank/Sp")) {

                                        String columnHeader = tempString;

                                        modificationLine = (String) allLines.get(allLines.size() - 3);

                                        //variable modifications   
                                        pattern = Pattern.compile(variableModificationPattern);
                                        matcher = pattern.matcher(modificationLine);

                                        while (matcher.find() && !cancelConversion) {

                                            currentModification = matcher.group().
                                                    substring(1, matcher.group().length() - 1);

                                            modificationName =
                                                    currentModification.substring(0,
                                                    currentModification.indexOf(" "));
                                            modificationNameShort =
                                                    modificationName.substring(0, 1);
                                            modificationMass = new Double(currentModification.substring(
                                                    currentModification.indexOf(" ") + 1)).doubleValue();

                                            if (!properties.getAlreadyChoosenModifications().
                                                    contains(modificationName)) {
                                                new ModificationMapping(outputFrame,
                                                        true, progressDialog, modificationName,
                                                        modificationNameShort,
                                                        modificationMass,
                                                        (CvParamImpl) userProperties.getCVTermMappings().
                                                        get(modificationName),
                                                        false);

                                                modifications.put(modificationName,
                                                        modificationMass);
                                                properties.getAlreadyChoosenModifications().
                                                        add(modificationName);
                                            } else {
                                                //do nothing, mapping already choosen
                                            }
                                        }

                                        //fixed modifications
                                        pattern = Pattern.compile(fixedModificationPattern);
                                        matcher = pattern.matcher(modificationLine);

                                        while (matcher.find() && !cancelConversion) {

                                            currentModification = matcher.group();

                                            modificationName =
                                                    currentModification.substring(
                                                    0,
                                                    currentModification.indexOf("="));
                                            modificationNameShort =
                                                    modificationName.substring(0,
                                                    1);
                                            modificationMass = new Double(currentModification.substring(
                                                    currentModification.indexOf("=") + 1)).doubleValue();

                                            if (!properties.getAlreadyChoosenModifications().
                                                    contains(modificationName)) {
                                                new ModificationMapping(outputFrame,
                                                        true, progressDialog, modificationName,
                                                        modificationNameShort,
                                                        modificationMass,
                                                        (CvParamImpl) userProperties.getCVTermMappings().get(
                                                        modificationName),
                                                        true);

                                                modifications.put(modificationName,
                                                        modificationMass);

                                                properties.getAlreadyChoosenModifications().
                                                        add(modificationName);
                                            } else {
                                                //do nothing, mapping already choosen
                                            }
                                        }


                                        currentLine = b.readLine();
                                        currentLine = b.readLine();

                                        // handle protein isoforms
                                        // not currently used and not finished. 
                                        // see Mascot Dat file for example.
//                                        String nextLine = b.readLine();
//
//                                        // check for protein isoforms
//                                        while(!nextLine.startsWith("  2.")){
//                                         
//                                            tok = new StringTokenizer(nextLine);
//                                            tok.nextToken();
//                                            String isoformAccession = tok.nextToken();
//                                            
//                                            nextLine = b.readLine();
//                                        }

                                        tok = new StringTokenizer(currentLine);

                                        tok.nextToken(); // # column

                                        rankSp = tok.nextToken() +
                                                tok.nextToken();

                                        if (rankSp.endsWith("/")) {
                                            rankSp += tok.nextToken();
                                        }

                                        // the old version of sequest files does 
                                        // not contain the Id# column
                                        if (columnHeader.lastIndexOf("Id#") !=
                                                -1) {
                                            tok.nextToken(); //Id#
                                        }

                                        mH = new Double(tok.nextToken()).doubleValue(); //(M+H)+

                                        deltaCn = new Double(tok.nextToken()).doubleValue(); //deltCn
                                        score = new Double(tok.nextToken()).doubleValue(); //XCorr
                                        sp = new Double(tok.nextToken()).doubleValue(); //Sp
                                        ions = tok.nextToken(); //Ions

                                        if (ions.endsWith("/")) {
                                            ions += tok.nextToken();
                                        }

                                        accession = tok.nextToken(); //Accession number
                                        sequence = tok.nextToken(); //Peptide

                                        //skip the protein isoform counter
                                        String isoformsPattern = "\\+[\\d]";

                                        pattern = Pattern.compile(isoformsPattern);
                                        matcher = pattern.matcher(sequence);

                                        if (matcher.find()) {
                                            sequence = tok.nextToken(); //Peptide
                                        }

                                        if (sequence.indexOf(".") == 1) {
                                            upstreamFlankingSequence = sequence.substring(0, 1).
                                                    toUpperCase();
                                        }
                                        if (sequence.lastIndexOf(".") ==
                                                sequence.length() - 2) {
                                            downstreamFlankingSequence = sequence.substring(sequence.length() -
                                                    1, sequence.length()).
                                                    toUpperCase();
                                        }

                                        sequence = sequence.substring(2, sequence.length() - 2);
                                        sequenceArray = new String[sequence.length()];

                                        modificationsDetected = false;
                                        index = 0;

                                        for (int i = 0; i < sequence.length() && !cancelConversion; i++) {

                                            if (i == sequence.length() - 1) {
                                                sequenceArray[index++] = sequence.substring(sequence.length() -
                                                        1, sequence.length());
                                            } else {
                                                if (properties.getAlreadyChoosenModifications().
                                                        contains(sequence.substring(i, i + 2))) {
                                                    sequenceArray[index++] = sequence.substring(i, i + 2);
                                                    i++;

                                                } else {
                                                    sequenceArray[index++] = sequence.substring(i, i + 1);
                                                }
                                            }
                                        }

                                        sequence = "";
                                        peptideModifications = null;

                                        for (int i = 0; i < sequenceArray.length && !cancelConversion; i++) {

                                            if (sequenceArray[i] != null) {

                                                if (sequenceArray[i].length() == 2) {

                                                    if (properties.getAlreadyChoosenModifications().contains(sequenceArray[i])) {

                                                        if (peptideModifications == null) {
                                                            peptideModifications = new ArrayList();
                                                        }

                                                        CvParamImpl tempCvParam =
                                                                (CvParamImpl) userProperties.getCVTermMappings().get(sequenceArray[i]);

                                                        modificationCVParams = new ArrayList();
                                                        modificationCVParams.add(tempCvParam);

                                                        monoMasses = new ArrayList();

                                                        // get the modification mass (DiffMono) retrieved from PSI-MOD
                                                        if (tempCvParam.getValue() != null) {
                                                            monoMasses.add(new MonoMassDeltaImpl(
                                                                    new Double(tempCvParam.getValue()).doubleValue()));
                                                        } else {
                                                            // it would be possible to use the mass from the data file
                                                            // but this is currently not used, as this could be incorrect
                                                            // relative to properties of the PSI-MOD modification
//                                                    monoMasses.add(new MonoMassDeltaImpl(((Double) modifications.get(
//                                                            sequenceArray[i])).doubleValue()));
                                                            monoMasses = null;
                                                        }

                                                        peptideModifications.add(new ModificationImpl(
                                                                tempCvParam.getAccession(),
                                                                new Integer(i + 1),
                                                                tempCvParam.getCVLookup(),
                                                                null,
                                                                monoMasses,
                                                                null,
                                                                modificationCVParams,
                                                                null));
                                                    }
                                                }
                                            }

                                            // check if the current amino acid contains a fixed modification
                                            if (sequenceArray[i] != null) {
                                                if (properties.getAlreadyChoosenModifications().contains(sequenceArray[i].substring(0, 1))) {

                                                    if (peptideModifications == null) {
                                                        peptideModifications = new ArrayList();
                                                    }

                                                    CvParamImpl tempCvParam =
                                                            (CvParamImpl) userProperties.getCVTermMappings().get(sequenceArray[i].substring(0, 1));

                                                    modificationCVParams = new ArrayList();
                                                    modificationCVParams.add(tempCvParam);

                                                    monoMasses = new ArrayList();

                                                    // get the modification mass (DiffMono) retrieved from PSI-MOD
                                                    if (tempCvParam.getValue() != null) {
                                                        monoMasses.add(new MonoMassDeltaImpl(
                                                                new Double(tempCvParam.getValue()).doubleValue()));
                                                    } else {
                                                        // it would be possible to use the mass from the data file
                                                        // but this is currently not used, as this could be incorrect 
                                                        // relative to properties of the PSI-MOD modification
//                                                    monoMasses.add(new MonoMassDeltaImpl(((Double) modifications.get(
//                                                            sequenceArray[i])).doubleValue()));
                                                        monoMasses = null;
                                                    }

                                                    peptideModifications.add(new ModificationImpl(
                                                            tempCvParam.getAccession(),
                                                            new Integer(i + 1),
                                                            tempCvParam.getCVLookup(),
                                                            null,
                                                            monoMasses,
                                                            null,
                                                            modificationCVParams,
                                                            null));
                                                }
                                            }

                                            if (sequenceArray[i] != null) {
                                                sequence += sequenceArray[i].substring(0, 1);
                                            }
                                        }

                                        sequence = sequence.toUpperCase();
                                        hitFound = true;
                                    }

                                    currentLine = b.readLine();
                                }

                                b.close();
                                f.close();

                                //calculate itraq values
                                iTRAQ iTRAQValues = null;

                                if (properties.getSampleDescriptionCVParamsQuantification().
                                        size() > 0 && !cancelConversion) {
                                    iTRAQValues =
                                            new iTRAQ(arrays, precursorMass,
                                            precursorCharge,
                                            userProperties.getPeakIntegrationRangeLower(),
                                            userProperties.getPeakIntegrationRangeUpper(),
                                            userProperties.getReporterIonIntensityThreshold(),
                                            userProperties.getPurityCorrections());
                                    iTRAQValues.calculateiTRAQValues();
                                }

                                String[] iTraqNorm = null;
                                String[] iTraqUT = null;
                                String[][] iTraqRatio = null;

                                if (properties.getSampleDescriptionCVParamsQuantification().
                                        size() > 0 && !cancelConversion) {
                                    iTraqNorm = (String[]) iTRAQValues.getAllNorms().get(0);
                                    iTraqUT = (String[]) iTRAQValues.getAllUTs().get(0);
                                    iTraqRatio = (String[][]) iTRAQValues.getAllRatios().get(0);
                                }

                                cVParams = new ArrayList();
                                cVParams.add(new CvParamImpl("PRIDE:0000053",
                                        "PRIDE", "Sequest Score",
                                        cVParams.size(), "" + score));

                                if (deltaCn != null) {
                                    cVParams.add(new CvParamImpl("PRIDE:0000012",
                                            "PRIDE", "Delta Cn", cVParams.size(), "" + deltaCn));
                                }

                                if (sp != null) {
                                    cVParams.add(new CvParamImpl("PRIDE:0000054",
                                            "PRIDE", "Sp", cVParams.size(), "" + sp));
                                }

                                if (ions != null) {
                                    cVParams.add(new CvParamImpl("PRIDE:0000055",
                                            "PRIDE", "Ions", cVParams.size(), "" + ions));
                                }

                                if (upstreamFlankingSequence != null) {
                                    if (!upstreamFlankingSequence.equalsIgnoreCase("-")) {
                                        cVParams.add(new CvParamImpl("PRIDE:0000065",
                                                "PRIDE",
                                                "Upstream flanking sequence",
                                                cVParams.size(),
                                                "" +
                                                upstreamFlankingSequence.toUpperCase()));
                                    }
                                }

                                if (downstreamFlankingSequence != null) {
                                    if (!downstreamFlankingSequence.equalsIgnoreCase("-")) {
                                        cVParams.add(new CvParamImpl("PRIDE:0000066",
                                                "PRIDE",
                                                "Downstream flanking sequence",
                                                cVParams.size(),
                                                "" +
                                                downstreamFlankingSequence.toUpperCase()));
                                    }
                                }

                                if (rankSp != null) {
                                    cVParams.add(new CvParamImpl("PRIDE:0000050",
                                            "PRIDE", "Rank/Sp", cVParams.size(), "" + rankSp));
                                }

                                if (mH != null) {
                                    cVParams.add(new CvParamImpl("PRIDE:0000051",
                                            "PRIDE", "(M+H)+", cVParams.size(), "" + mH));
                                }

                                if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
                                    cVParams = addItraqCVTerms(cVParams, iTraqNorm);
                                    userParams = addItraqUserTerms(iTraqRatio);
                                } else {
                                    userParams = null;
                                }

                                if (score.doubleValue() >= properties.getPeptideScoreThreshold()) {

                                    ids.add(new IdentificationGeneral(
                                            new File(properties.getSelectedSourceFiles().get(j)).getName(),//spectrumFileID
                                            accession, //accession
                                            "Sequest",//search engine
                                            database, //database
                                            null, //databaseversion
                                            sequence.toUpperCase(), //sequence
                                            null, //start
                                            score, //score
                                            null, //threshold
                                            iTraqNorm, iTraqUT, iTraqRatio,
                                            cVParams, userParams, peptideModifications));
                                }
                            }
                        }

                        // Precursor collection.
                        precursors = new ArrayList(1);

                        // Ion selection parameters.
                        ionSelection = new ArrayList(4);

                        // See if we know the precursor charge, and if so, include it.
                        charge = precursorCharge;

                        if (charge > 0) {
                            ionSelection.add(new CvParamImpl("PSI:1000041",
                                    "PSI", "ChargeState", 0,
                                    Integer.toString(charge)));
                        }

                        //intensity not given in Sequest DTA File
                        if (precursorIntensty != -1) {
                            ionSelection.add(new CvParamImpl("PSI:1000042",
                                    "PSI", "Intensity", 1,
                                    Double.toString(precursorIntensty)));
                        }

                        ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                                "MassToChargeRatio", 2, Double.toString(
                                precursorMass)));

                        precursors.add(new PrecursorImpl(null, null,
                                ionSelection, null, 1, 0, 0));

                        spectrumDescriptionComments.add(new SpectrumDescCommentImpl(identified));

                        if (arrays[properties.MZ_ARRAY].length > 0) {
                            mzRangeStart =
                                    new Double(arrays[properties.MZ_ARRAY][0]);
                            mzRangeStop = new Double(arrays[properties.MZ_ARRAY][arrays[properties.MZ_ARRAY].length -
                                    1]);

                            // Create new mzData spectrum for the fragmentation spectrum.
                            fragmentation = new SpectrumImpl(
                                    new BinaryArrayImpl(arrays[properties.INTENSITIES_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    mzRangeStart,
                                    new BinaryArrayImpl(arrays[properties.MZ_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    2l, null,
                                    mzRangeStop,
                                    null,
                                    spectraCounter, precursors,
                                    spectrumDescriptionComments,
                                    null, null, null, null);

                            if (properties.getDataSource().equalsIgnoreCase("Spectrum Mill") ||
                                    properties.getDataSource().equalsIgnoreCase("Sequest Result File")) {

                                if (properties.selectAllSpectra()) {
                                    // Store (spectrumfileid, spectrumid) mapping.
                                    mapping.put(new File(properties.getSelectedSourceFiles().get(j)).getName(),
                                            new Long(spectraCounter));
                                    spectraCounter++;

                                    // Store the transformed spectrum.
                                    aTransformedSpectra.add(fragmentation);
                                } else if (properties.selectAllIdentifiedSpectra() &&
                                        identified.equalsIgnoreCase("Identified")) {
                                    // Store (spectrumfileid, spectrumid) mapping.
                                    mapping.put(new File(properties.getSelectedSourceFiles().
                                            get(j)).getName(),
                                            new Long(spectraCounter));
                                    spectraCounter++;

                                    // Store the transformed spectrum.
                                    aTransformedSpectra.add(fragmentation);
                                } else {

                                    boolean spectraSelected = false;

                                    for (int k = 0; k < properties.getSelectedSpectraNames().size() &&
                                            !spectraSelected && !cancelConversion; k++) {

                                        Object[] temp = (Object[]) properties.getSelectedSpectraNames().get(k);

                                        if (((String) temp[0]).equalsIgnoreCase(currentFileName)) {
                                            spectraSelected = true;
                                        }
                                    }

                                    if (spectraSelected) {

                                        // Store (spectrumfileid, spectrumid) mapping.
                                        mapping.put(new File(properties.getSelectedSourceFiles().get(j)).getName(),
                                                new Long(spectraCounter));
                                        spectraCounter++;

                                        // Store the transformed spectrum.
                                        aTransformedSpectra.add(fragmentation);
                                    }
                                }
                            } else {
                                // Store (spectrumfileid, spectrumid) mapping.
                                mapping.put(new File(properties.getSelectedSourceFiles().get(j)).getName(),
                                        new Long(spectraCounter));
                                spectraCounter++;

                                // Store the transformed spectrum.
                                aTransformedSpectra.add(fragmentation);
                            }
                        }
                    } catch (FileNotFoundException ex) {
                        JOptionPane.showMessageDialog(null, "The file named " +
                                properties.getSelectedSourceFiles().get(j) +
                                "\ncould not be found.",
                                "File Not Found", JOptionPane.ERROR_MESSAGE);
                        Util.writeToErrorLog("File not found: ");
                        ex.printStackTrace();
                    } catch (Exception e) {
                        String fileType = "";

                        if (properties.getDataSource().equalsIgnoreCase("Sequest Result File")) {
                            fileType = "Sequest *.out file";
                        } else if (properties.getDataSource().equalsIgnoreCase("Spectrum Mill")) {
                            fileType = "Spectrum Mill *.pkl.spo file";
                        }

                        if (!cancelConversion) {

                            Util.writeToErrorLog("Error parsing " + fileType + ": ");
                            e.printStackTrace();

                            JOptionPane.showMessageDialog(null,
                                    "The following file could not parsed as a " +
                                    fileType + ":\n " +
                                    identificationFile.getPath() +
                                    "\n\n" +
                                    "See ../Properties/ErrorLog.txt for more details.",
                                    "Error Parsing File",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }

//            long end = System.currentTimeMillis();
//  
//            System.out.println((j + 1) + " Parse: " + (end - startTimer));// + "\n");
        }

        totalNumberOfSpectra = spectraCounter - 1;

        return mapping;
    }

    /**
     * This method transforms spectra from PKL and PKX files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aSpectra   Collection with the Spectrumfile instances to to transform.
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     */
    private static HashMap transformSpectraFromPKLAndPKXFiles(
            ArrayList aTransformedSpectra) throws IOException {

        HashMap mapping = new HashMap();

        double[][] arrays = null;
        Collection precursors;
        Collection ionSelection;
        int charge;
        String fileName, currentLine;
        StringTokenizer tok;
        Double mzRangeStart;
        Double mzRangeStop;

        ArrayList spectrumDescriptionComments, supDataArrays, cVParams, supDescArrays;

        boolean errorDetected;
        FileReader f;
        BufferedReader b;
        double precursorMass;
        double precursorIntensty;
        double precursorRetentionTime;
        int precursorCharge;
        Vector masses;
        Vector intensities;
        Vector fragmentIonChargeStates;

        Spectrum fragmentation;
        int spectraCounter = 1;
        boolean matchFound;

        progressDialog.setIntermidiate(false);
        progressDialog.setValue(0);
        progressDialog.setMax(properties.getSelectedSourceFiles().size());

        for (int j = 0; j < properties.getSelectedSourceFiles().size() && !cancelConversion; j++) {

            fileName = new File(properties.getSelectedSourceFiles().get(j)).getName();
            currentFileName = fileName;

            progressDialog.setValue(j);
            progressDialog.setString(currentFileName + " (" + (j + 1) + "/" +
                    properties.getSelectedSourceFiles().size() + ")");

            errorDetected = false;
            matchFound = false;

            precursorMass = -1.0;
            precursorIntensty = -1.0;
            precursorCharge = -1;
            precursorRetentionTime = -1;

            try {
                f = new FileReader(new File(properties.getSelectedSourceFiles().get(j)));
                b = new BufferedReader(f);

                currentLine = b.readLine();
                masses = new Vector();
                intensities = new Vector();
                fragmentIonChargeStates = new Vector();

                boolean firstSpectraFound = false;

                // find the first spectra parent ion
                while (currentLine != null && !firstSpectraFound && !cancelConversion) {

                    if (properties.isCommaTheDecimalSymbol()) {
                        currentLine = currentLine.replaceAll(",", ".");
                    }

                    tok = new StringTokenizer(currentLine);

                    if ((properties.getDataSource().equalsIgnoreCase("VEMS") &&
                            tok.countTokens() == 4) ||
                            (properties.getDataSource().equalsIgnoreCase("Micromass PKL File") &&
                            tok.countTokens() == 3)) {

                        precursorMass = new Double(tok.nextToken()).doubleValue();
                        precursorIntensty = new Double(tok.nextToken()).doubleValue();
                        precursorCharge = new Integer(tok.nextToken()).intValue();
                        precursorRetentionTime = -1;

                        if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                            precursorRetentionTime = new Double(tok.nextToken()).doubleValue();
                        }

                        firstSpectraFound = true;
                    }

                    currentLine = b.readLine();
                }

                // find the rest of the spectra
                while (currentLine != null && !cancelConversion) {

                    if (properties.isCommaTheDecimalSymbol()) {
                        currentLine = currentLine.replaceAll(",", ".");
                    }

                    tok = new StringTokenizer(currentLine);

                    if ((properties.getDataSource().equalsIgnoreCase("VEMS") &&
                            tok.countTokens() == 4) ||
                            (properties.getDataSource().equalsIgnoreCase("Micromass PKL File") &&
                            tok.countTokens() == 3)) {

                        // check if the spectra is selected or not
                        matchFound = false;

                        if (properties.getSelectedSpectraNames().size() > 0) {
                            for (int k = 0; k < properties.getSelectedSpectraNames().size() && !matchFound; k++) {

                                if (((String) ((Object[]) properties.getSelectedSpectraNames().
                                        get(k))[0]).equalsIgnoreCase(fileName)) {
                                    if (((Double) ((Object[]) properties.getSelectedSpectraNames().
                                            get(k))[2]).doubleValue() ==
                                            precursorMass) {
                                        matchFound = true;
                                    }
                                }
                            }
                        } else {
                            matchFound = true;
                        }

                        if (matchFound) {

                            arrays = new double[3][masses.size()];

                            for (int i = 0; i < masses.size() && !cancelConversion; i++) {
                                arrays[0][i] = ((Double) masses.get(i)).doubleValue();
                                arrays[1][i] = ((Double) intensities.get(i)).doubleValue();

                                if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                                    arrays[2][i] = ((Double) fragmentIonChargeStates.get(i)).doubleValue();
                                }
                            }

                            // Precursor collection.
                            precursors = new ArrayList(1);

                            // Ion selection parameters.
                            ionSelection = new ArrayList(4);

                            // See if we know the precursor charge, and if so, include it.
                            charge = precursorCharge;
                            if (charge > 0) {
                                ionSelection.add(new CvParamImpl("PSI:1000041",
                                        "PSI", "ChargeState", 0,
                                        Integer.toString(charge)));
                            }

                            // precursor intensity
                            ionSelection.add(new CvParamImpl("PSI:1000042",
                                    "PSI", "Intensity", 1,
                                    Double.toString(precursorIntensty)));

                            // precursor mass
                            ionSelection.add(new CvParamImpl("PSI:1000040",
                                    "PSI", "MassToChargeRatio", 2, Double.toString(
                                    precursorMass)));

                            // precursor retention time
                            if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                                ionSelection.add(new CvParamImpl("PRIDE:0000203",
                                        "PRIDE", "Parent ion retention time", 3, Double.toString(
                                        precursorRetentionTime)));
                            }

                            precursors.add(new PrecursorImpl(null, null,
                                    ionSelection, null, 1, 0, 0));

                            spectrumDescriptionComments = new ArrayList(1);
                            spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Not identified"));

                            if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                                supDataArrays = new ArrayList(1);
                                supDataArrays.add(
                                        new SupDataArrayBinaryImpl("Fragment ion charge states",
                                        0,
                                        new BinaryArrayImpl(
                                        arrays[properties.FRAGMENT_ION_CHARGE_STATES_ARRAY],
                                        BinaryArrayImpl.LITTLE_ENDIAN_LABEL)));

                                cVParams = new ArrayList(1);
                                cVParams.add(new CvParamImpl("PRIDE:0000204",
                                        "PRIDE", "fragment ion charge", 0, null));

                                supDescArrays = new ArrayList(1);
                                supDescArrays.add(
                                        new SupDescImpl(null, 0, cVParams, null,
                                        null));
                            } else {
                                supDataArrays = null;
                                supDescArrays = null;
                            }

                            if (arrays[properties.MZ_ARRAY].length > 0) {
                                mzRangeStart = new Double(arrays[properties.MZ_ARRAY][0]);
                                mzRangeStop = new Double(
                                        arrays[properties.MZ_ARRAY][arrays[properties.MZ_ARRAY].length - 1]);

                                // Create new mzData spectrum for the fragmentation spectrum.
                                fragmentation = new SpectrumImpl(
                                        new BinaryArrayImpl(arrays[properties.INTENSITIES_ARRAY],
                                        BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                        mzRangeStart,
                                        new BinaryArrayImpl(arrays[properties.MZ_ARRAY],
                                        BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                        2l,
                                        supDataArrays,
                                        mzRangeStop,
                                        null,
                                        spectraCounter, precursors,
                                        spectrumDescriptionComments,
                                        null, null,
                                        null,
                                        supDescArrays);

                                mapping.put((new File(properties.getSelectedSourceFiles().
                                        get(j)).getName() +
                                        "_" + precursorMass),
                                        new Long(spectraCounter));
                                spectraCounter++;

                                // Store the transformed spectrum.
                                aTransformedSpectra.add(fragmentation);
                            }
                        }

                        masses = new Vector();
                        intensities = new Vector();
                        fragmentIonChargeStates = new Vector();

                        precursorMass = new Double(tok.nextToken()).doubleValue();
                        precursorIntensty = new Double(tok.nextToken()).doubleValue();
                        precursorCharge = new Integer(tok.nextToken()).intValue();
                        precursorRetentionTime = -1;

                        if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                            precursorRetentionTime =
                                    new Double(tok.nextToken()).doubleValue();
                        }

                    } else if (!currentLine.equalsIgnoreCase("")) {
                        tok = new StringTokenizer(currentLine);
                        masses.add(new Double(tok.nextToken()));
                        intensities.add(new Double(tok.nextToken()));

                        if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                            fragmentIonChargeStates.add(new Double(tok.nextToken()));
                        }
                    }

                    currentLine = b.readLine();
                }

                // add the last spectra
                // check if the spectra is selected or not
                matchFound = false;

                if (properties.getSelectedSpectraNames().size() > 0) {
                    for (int k = 0; k < properties.getSelectedSpectraNames().size() && !matchFound; k++) {

                        if (((String) ((Object[]) properties.getSelectedSpectraNames().
                                get(k))[0]).equalsIgnoreCase(fileName)) {
                            if (((Double) ((Object[]) properties.getSelectedSpectraNames().
                                    get(k))[2]).doubleValue() ==
                                    precursorMass) {
                                matchFound = true;
                            }
                        }
                    }
                } else {
                    matchFound = true;
                }

                if (matchFound) {

                    arrays = new double[3][masses.size()];

                    for (int i = 0; i < masses.size() && !cancelConversion; i++) {
                        arrays[0][i] = ((Double) masses.get(i)).doubleValue();
                        arrays[1][i] = ((Double) intensities.get(i)).doubleValue();

                        if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                            arrays[2][i] = ((Double) fragmentIonChargeStates.get(i)).doubleValue();
                        }
                    }

                    // Precursor collection.
                    precursors = new ArrayList(1);

                    // Ion selection parameters.
                    ionSelection = new ArrayList(4);

                    // See if we know the precursor charge, and if so, include it.
                    charge = precursorCharge;
                    if (charge > 0) {
                        ionSelection.add(new CvParamImpl("PSI:1000041", "PSI",
                                "ChargeState", 0, Integer.toString(charge)));
                    }

                    // precursor intensity
                    ionSelection.add(new CvParamImpl("PSI:1000042", "PSI",
                            "Intensity", 1, Double.toString(precursorIntensty)));

                    // precursor mass
                    ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                            "MassToChargeRatio", 2, Double.toString(
                            precursorMass)));

                    // precursor retention time
                    if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                        ionSelection.add(new CvParamImpl("PRIDE:0000203",
                                "PRIDE", "Parent ion retention time", 3, Double.toString(
                                precursorRetentionTime)));
                    }

                    precursors.add(new PrecursorImpl(null, null, ionSelection,
                            null, 1, 0, 0));

                    spectrumDescriptionComments = new ArrayList(1);
                    spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Not identified"));

                    if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                        supDataArrays = new ArrayList(1);
                        supDataArrays.add(
                                new SupDataArrayBinaryImpl("Fragment ion charge states",
                                0,
                                new BinaryArrayImpl(
                                arrays[properties.FRAGMENT_ION_CHARGE_STATES_ARRAY],
                                BinaryArrayImpl.LITTLE_ENDIAN_LABEL)));

                        cVParams = new ArrayList(1);
                        cVParams.add(new CvParamImpl("PRIDE:0000204", "PRIDE",
                                "fragment ion charge", 0, null));

                        supDescArrays = new ArrayList(1);
                        supDescArrays.add(
                                new SupDescImpl(null, 0, cVParams, null, null));
                    } else {
                        supDataArrays = null;
                        supDescArrays = null;
                    }

                    if (arrays[properties.MZ_ARRAY].length > 0) {
                        mzRangeStart = new Double(arrays[properties.MZ_ARRAY][0]);
                        mzRangeStop = new Double(
                                arrays[properties.MZ_ARRAY][arrays[properties.MZ_ARRAY].length - 1]);

                        // Create new mzData spectrum for the fragmentation spectrum.
                        fragmentation = new SpectrumImpl(
                                new BinaryArrayImpl(arrays[properties.INTENSITIES_ARRAY],
                                BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                mzRangeStart,
                                new BinaryArrayImpl(arrays[properties.MZ_ARRAY],
                                BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                2l,
                                supDataArrays,
                                mzRangeStop,
                                null,
                                spectraCounter, precursors,
                                spectrumDescriptionComments,
                                null, null, null, supDescArrays);

                        mapping.put((new File(properties.getSelectedSourceFiles().
                                get(j)).getName() +
                                "_" + precursorMass),
                                new Long(spectraCounter));
                        spectraCounter++;

                        // Store the transformed spectrum.
                        aTransformedSpectra.add(fragmentation);
                    }
                }

                b.close();
                f.close();

            } catch (Exception e) {

                errorDetected = true;
                String fileType = "";

                if (properties.getDataSource().equalsIgnoreCase("Micromass PKL File")) {
                    fileType = "Micromass PKL file";
                } else if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                    fileType = "VEMS PKX file";
                }

                if (!cancelConversion) {

                    Util.writeToErrorLog("Error parsing " + fileType + ": ");
                    e.printStackTrace();

                    JOptionPane.showMessageDialog(null,
                            "The following file could not parsed as a " +
                            fileType + ":\n " +
                            new File(properties.getSelectedSourceFiles().get(j)).getName() +
                            "\n\n" +
                            "See ../Properties/ErrorLog.txt for more details.",
                            "Error Parsing File", JOptionPane.ERROR_MESSAGE);
                }
            }

            totalNumberOfSpectra = spectraCounter - 1;
        }

        return mapping;
    }

    /**
     * This method transforms spectra from mzData files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aSpectra   Collection with the Spectrumfile instances to to transform.
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     */
    private static HashMap transformSpectraFromMzData(ArrayList aTransformedSpectra) throws IOException {

        // note: this method is currently not in use. for mzData files the original mzData file
        //       is used directly 

        HashMap mapping = new HashMap();

        return mapping;
    }

    /**
     * This method transforms spectra from mxML files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aSpectra   Collection with the Spectrumfile instances to to transform.
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     */
    private static HashMap transformSpectraFromMzML(ArrayList aTransformedSpectra)
            throws IOException {

        //CURRENTLY NOT WORKING!!!
        JOptionPane.showMessageDialog(null, "Currently not supported!");

        HashMap mapping = new HashMap();

        return mapping;

//        //double[][] arrays;
//        float[][] arraysFloat;
//        Collection precursors;
//
//        Collection ionSelection;
//
//        int precursorCharge;
////        double precursorIntensity;
//        float precursorMz;
//        //Collection activation;
//        int spectraCounter = 1;
//        String fileName;
//
//        boolean matchFound = false;
//        //MSXMLParser msXMLParser;
//        MzMLUnmarshaller mzMLParser;
//        MzML mzMLFile;
//
//        int scanCount;
//        //Scan scan;
//
//        int msLevel;
//        //Spectrum spectrum;
//        uk.ac.ebi.jmzml.model.mzml.Spectrum spectrum;
//        BinaryDataArray mzValues, intensities;
//
//        List<uk.ac.ebi.jmzml.model.mzml.Spectrum> spectra;
//        Collection spectrumDescriptionComments;
//        String identified;
//
//        for (int j = 0; j <
//                selectedSourceFiles.size() && !cancelConversion; j++) {
//
//            fileName =
//                    new File(selectedSourceFiles.get(j)).getName();
//            currentFileName = fileName;
//
//            progressDialog.setCurrentFileName(currentFileName);
//
//            mzMLParser =
//                    new MzMLUnmarshaller(new File(selectedSourceFiles.get(j)));
//            mzMLFile = mzMLParser.unmarshall();
//
//            spectra = mzMLFile.getRun().getSpectrumList().getSpectrum();
//            scanCount = spectra.size();
//
//            for (int i = 1; i <=
//                    scanCount; i++) {
//
//                spectrum = spectra.get(i);
//
//                matchFound = false;
//
//                if (selectedSpectraNames.size() > 0) {
//                    for (int k = 0; k <
//                            selectedSpectraNames.size() &&
//                            !matchFound; k++) {
//
//                        if (((String) ((Object[]) selectedSpectraNames.get(k))[0]).equalsIgnoreCase(fileName)) {
//                            if (((Integer) ((Object[]) selectedSpectraNames.get(k))[1]).intValue() ==
//                                    spectrum.getIndex().longValue()) {
//                                matchFound = true;
//                            }
//                        }
//                    }
//                } else {
//                    matchFound = true;
//                }
//
//                if (matchFound) {
//
//                    // find the mz and intensity values
//                    for (int k = 0; k <
//                            spectrum.getBinaryDataArrayList().getBinaryDataArray().size(); k++) {
//                        for (int m = 0; m <
//                                spectrum.getBinaryDataArrayList().getBinaryDataArray().get(i).getCvParam().size(); m++) {
//
//                            if (spectrum.getBinaryDataArrayList().getBinaryDataArray().get(i).getCvParam().get(m).
//        getName().equalsIgnoreCase("m/z array")) {
//                                mzValues = spectrum.getBinaryDataArrayList().getBinaryDataArray().get(i);
//                            } else if (spectrum.getBinaryDataArrayList().getBinaryDataArray().get(i).getCvParam().get(m).
//        getName().equalsIgnoreCase("intensity array")) {
//                                intensities = spectrum.getBinaryDataArrayList().getBinaryDataArray().get(i);
//                            }
//                        }
//                    }
//
//
////                    // Precursor collection.
////                    precursors =
////                            new ArrayList(1);
////
////                    // Ion selection parameters.
////                    ionSelection =
////                            new ArrayList(4);
//
////                    // See if we know the precursor charge, and if so, include it.
////                    if (precursorCharge > 0) {
////                        ionSelection.add(new CvParamImpl("PSI:1000041", "PSI", "ChargeState", 0, Integer.toString(precursorCharge)));
////                    }
//
//// See if we know the precursor intensity 
////                    if (precursorIntensity > 1) {
////                        ionSelection.add(new CvParamImpl("PSI:1000042", "PSI", "Intensity", 1, Double.toString(precursorIntensity)));
////                    }
////                    ionSelection.add(new CvParamImpl("PSI:1000040", "PSI", "MassToChargeRatio", 2, Float.toString(
////                            precursorMz)));
//
////                    precursors.add(new PrecursorImpl(null, null, ionSelection, null, 1, spectraCounter, 0));
//
//                    // Spectrum description comments.
//                    spectrumDescriptionComments = new ArrayList(1);
//
//                    identified = "Not identified";
//                    spectrumDescriptionComments.add(new SpectrumDescCommentImpl(identified));
//
////                    new BinaryArrayImpl(mzValues.get, orderIndexInstrumentSourceParameter, dataSource, softwareVersion)
////                    
////                    if (msLevel == 1) {
////                        spectrum = new SpectrumImpl(
////                                new BinaryArrayImpl(arraysFloat[INTENSITIES_ARRAY], BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
////                                new Double(arraysFloat[MZ_ARRAY][0]), //null
////                                new BinaryArrayImpl(arraysFloat[MZ_ARRAY], BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
////                                1, null,
////                                new Double(arraysFloat[MZ_ARRAY][arraysFloat[MZ_ARRAY].length -
////                                1]), //null, 
////                                null,
////                                spectraCounter, null, null, null, null, null, null);
////                    } else {//msLevel == 2){
////                        spectrum = new SpectrumImpl(
////                                new BinaryArrayImpl(arraysFloat[INTENSITIES_ARRAY], BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
////                                new Double(arraysFloat[MZ_ARRAY][0]), //null, 
////                                new BinaryArrayImpl(arraysFloat[MZ_ARRAY], BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
////                                2l, null,
////                                new Double(arraysFloat[MZ_ARRAY][arraysFloat[MZ_ARRAY].length -
////                                1]), //null, 
////                                null,
////                                spectraCounter, precursors, spectrumDescriptionComments, null, null, null, null);
////                    }
//
////                    double min = Double.MAX_VALUE;
////                    double max = 0.0;
////                    
////                    for(int k=0; k< arraysFloat[0].length; k++){
////                        
////                        if(arraysFloat[0][k] > max){
////                            max = arraysFloat[0][k];
////                        }
////                        if(arraysFloat[0][k] < min){
////                            min = arraysFloat[0][k];
////                        }
////                    }
//
//
////                    System.out.println(scan.getLowMz() +  " " + new Double(arraysFloat[MZ_ARRAY][0]) + " " + min + 
////                    " " + scan.getHighMz() + " " + new Double(arraysFloat[MZ_ARRAY][arraysFloat[MZ_ARRAY].length - 1]) + " " + max);
//
//
//// Create new mzData spectrum for the fragmentation spectrum.
//// Store (spectrumfileid, spectrumid) mapping.
//                    mapping.put(new Long(spectraCounter), new Long(spectraCounter));
//                    spectraCounter++;
//
//// Store the transformed spectrum.
//                    aTransformedSpectra.add(spectrum);
//                }
//            }
//        }
//
//        totalNumberOfSpectra = spectraCounter - 1;
//
//        return mapping;
    }

    /**
     * This method transforms spectra from mzXML files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aSpectra   Collection with the Spectrumfile instances to to transform.
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     */
    private static HashMap transformSpectraFromMzXML(ArrayList aTransformedSpectra)
            throws IOException {

        HashMap mapping = new HashMap();

        float[][] arraysFloat;
        Collection precursors;

        Collection ionSelection;

        int precursorCharge;
        float precursorMz;
        int spectraCounter = 1;
        String fileName;

        boolean matchFound = false;
        MSXMLParser msXMLParser;

        int scanCount;
        Scan scan;

        int msLevel;
        Spectrum spectrum;
        Double mzRangeStart;
        Double mzRangeStop;

        Collection spectrumDescriptionComments;

        String identified;

        progressDialog.setIntermidiate(false);
        progressDialog.setValue(0);

        for (int j = 0; j < properties.getSelectedSourceFiles().size() && !cancelConversion; j++) {

            fileName = new File(properties.getSelectedSourceFiles().get(j)).getName();

            currentFileName = fileName;

            progressDialog.setValue(0);
            progressDialog.setString(currentFileName + " (" + (j + 1) + "/" +
                    properties.getSelectedSourceFiles().size() + ")");

            msXMLParser = new MSXMLParser(
                    new File(properties.getSelectedSourceFiles().get(j)).getAbsolutePath());

            scanCount = msXMLParser.getScanCount();

            progressDialog.setMax(scanCount);

            for (int i = 1; i <= scanCount; i++) {

                progressDialog.setValue(i);

                scan = msXMLParser.rap(i);
                precursorCharge = scan.getPrecursorCharge();
                precursorMz = scan.getPrecursorMz();
                msLevel = scan.getMsLevel();

                matchFound = false;

                if (properties.getSelectedSpectraNames().size() > 0) {
                    for (int k = 0; k < properties.getSelectedSpectraNames().size() && !matchFound; k++) {

                        if (((String) ((Object[]) properties.getSelectedSpectraNames().
                                get(k))[0]).equalsIgnoreCase(fileName)) {
                            if (((Integer) ((Object[]) properties.getSelectedSpectraNames().
                                    get(k))[1]).intValue() == scan.getNum()) {
                                matchFound = true;
                            }
                        }
                    }
                } else {
                    matchFound = true;
                }

                if (matchFound) {

                    // Transform peaklist into float arrays.
                    arraysFloat = scan.getMassIntensityList();

                    // Precursor collection.
                    precursors = new ArrayList(1);

                    // Ion selection parameters.
                    ionSelection = new ArrayList(4);

                    // See if we know the precursor charge, and if so, include it.
                    if (precursorCharge > 0) {
                        ionSelection.add(new CvParamImpl("PSI:1000041", "PSI",
                                "ChargeState", 0,
                                Integer.toString(precursorCharge)));
                    }

                    ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                            "MassToChargeRatio", 2, Float.toString(
                            precursorMz)));

                    precursors.add(new PrecursorImpl(null, null, ionSelection,
                            null, 1, 0, 0));

                    // Spectrum description comments.
                    spectrumDescriptionComments =
                            new ArrayList(1);

                    identified = "Not identified";
                    spectrumDescriptionComments.add(new SpectrumDescCommentImpl(identified));

                    if (arraysFloat[properties.MZ_ARRAY].length > 0) {
                        mzRangeStart = new Double(arraysFloat[properties.MZ_ARRAY][0]);
                        mzRangeStop = new Double(
                                arraysFloat[properties.MZ_ARRAY][arraysFloat[properties.MZ_ARRAY].length - 1]);

                        if (msLevel == 1) {
                            spectrum = new SpectrumImpl(
                                    new BinaryArrayImpl(arraysFloat[properties.INTENSITIES_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    mzRangeStart,
                                    new BinaryArrayImpl(arraysFloat[properties.MZ_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    1, null,
                                    new Double(arraysFloat[properties.MZ_ARRAY][arraysFloat[properties.MZ_ARRAY].length -
                                    1]), //null, 
                                    null,
                                    spectraCounter, null, null, null, null, null,
                                    null);
                        } else {//msLevel == 2){
                            spectrum = new SpectrumImpl(
                                    new BinaryArrayImpl(arraysFloat[properties.INTENSITIES_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    mzRangeStart,
                                    new BinaryArrayImpl(arraysFloat[properties.MZ_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    2l, null,
                                    mzRangeStop,
                                    null,
                                    spectraCounter, precursors,
                                    spectrumDescriptionComments, null, null, null,
                                    null);
                        }

                        // Create new mzData spectrum for the fragmentation spectrum.
                        // Store (spectrumfileid, spectrumid) mapping.
                        mapping.put(new Long(spectraCounter), new Long(spectraCounter));
                        spectraCounter++;

                        // Store the transformed spectrum.
                        aTransformedSpectra.add(spectrum);
                    }
                }
            }
        }

        totalNumberOfSpectra = spectraCounter - 1;

        return mapping;
    }

    /**
     * This method extracts an integer from Mascot Generic File charge 
     * notation, eg., 1+.
     * 
     * Note that the charge can also be annotated as "+2,+3", in those rather 
     * unusual cases the charge is also "not known." So we save a zero value.
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
                charge = Integer.parseInt(trimmedCharge.substring(0, trimmedCharge.length() - 1));
            } else {
                charge = Integer.parseInt(trimmedCharge);
            }

            if (negate) {
                charge = -charge;
            }
        }

        return charge;
    }

    /**
     * This method transforms spectra from Mascot Generic files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aSpectra   Collection with the Spectrumfile instances to to transform.
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     */
    private static HashMap transformSpectraFromMascotGenericFile(ArrayList aTransformedSpectra)
            throws IOException {

        HashMap mapping = new HashMap();

        double[][] arrays;
        Collection precursors;

        Collection ionSelection;

        int charge;
        double intensity;
        int spectraCounter = 1;
        String filePath;

        Collection spectrumDescriptionComments;
        String identified;
        Spectrum fragmentation;
        File tempFile;

        boolean matchFound = false;

        Vector masses;
        Vector intensities;
        Vector charges;
        String[] values, values2;
        double precursorMass = 0.0;
        double precursorIntensity = 0.0;
        int precursorCharge = 0;
        Double mzRangeStart;
        Double mzRangeStop;

        progressDialog.setString(null);
        progressDialog.setIntermidiate(true);

        for (int j = 0; j < properties.getSelectedSourceFiles().size() && !cancelConversion; j++) {

            //long start = System.currentTimeMillis();

            filePath = properties.getSelectedSourceFiles().get(j);

            currentFileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);

            progressDialog.setIntermidiate(true);
            progressDialog.setString(currentFileName + " (" + (j + 1) + "/" +
                    properties.getSelectedSourceFiles().size() + ")");

            BufferedReader br = new BufferedReader(new FileReader(filePath));

            String line = null;
            int lineCount = 0;
            masses = new Vector();
            intensities = new Vector();
            charges = new Vector();
            boolean inSpectrum = false;
            Integer msLevel = 2; // defaults to MS/MS

            while ((line = br.readLine()) != null && !cancelConversion) {
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
                    inSpectrum = true;
                } // END IONS marks the end.
                else if (line.equals("END IONS")) {
                    inSpectrum = false;

                    matchFound = false;

                    if (properties.getSelectedSpectraNames().size() > 0) {
                        for (int k = 0; k < properties.getSelectedSpectraNames().size() &&
                                !matchFound && !cancelConversion; k++) {

                            Object[] temp = (Object[]) properties.getSelectedSpectraNames().get(k);

                            if (((String) temp[0]).equalsIgnoreCase(currentFileName)) {
                                if (((Double) temp[2]).doubleValue() == precursorMass) {
                                    if (temp[3] != null) {
                                        if (((Integer) temp[3]).intValue() == precursorCharge) {
                                            matchFound = true;
                                        }
                                    } else {
                                        matchFound = true;
                                    }
                                }
                            }

                        // Not yet finished
                        // ms level
//                            if (temp[4] != null) {
//                                msLevel = (Integer) temp[4];
//                            } else {
//                                // defaults to MS2
//                                msLevel = 2;
//                            }
                        }
                    } else {
                        matchFound = true;
                    }

                    if (matchFound && !cancelConversion) {

                        arrays = new double[2][masses.size()];

                        for (int i = 0; i < masses.size() && !cancelConversion; i++) {
                            arrays[0][i] = ((Double) masses.get(i)).doubleValue();
                            arrays[1][i] = ((Double) intensities.get(i)).doubleValue();
                        }

                        // Precursor collection.
                        precursors = new ArrayList(1);

                        // Ion selection parameters.
                        ionSelection = new ArrayList(3);

                        if (precursorCharge > 0) {
                            ionSelection.add(new CvParamImpl("PSI:1000041",
                                    "PSI", "ChargeState", 0,
                                    Integer.toString(precursorCharge)));
                        }

                        if (precursorIntensity > 1) {
                            ionSelection.add(new CvParamImpl("PSI:1000042",
                                    "PSI", "Intensity", 1,
                                    Double.toString(precursorIntensity)));
                        }

                        ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                                "MassToChargeRatio", 2, Double.toString(
                                precursorMass)));

                        precursors.add(new PrecursorImpl(null, null,
                                ionSelection, null, msLevel - 1, 0, 0));

                        // Spectrum description comments.
                        spectrumDescriptionComments = null;
//                        spectrumDescriptionComments = new ArrayList(1);
//                        spectrumDescriptionComments.add(new SpectrumDescCommentImpl(comments));

                        if (arrays[properties.MZ_ARRAY].length > 0) {
                            mzRangeStart = new Double(arrays[properties.MZ_ARRAY][0]);
                            mzRangeStop = new Double(
                                    arrays[properties.MZ_ARRAY][arrays[properties.MZ_ARRAY].length - 1]);

                            fragmentation =
                                    new SpectrumImpl(
                                    new BinaryArrayImpl(arrays[properties.INTENSITIES_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    mzRangeStart,
                                    new BinaryArrayImpl(arrays[properties.MZ_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    msLevel, null,
                                    mzRangeStop,
                                    null,
                                    spectraCounter, precursors,
                                    spectrumDescriptionComments,
                                    null, null, null, null);

                            // Store (spectrumfileid, spectrumid) mapping.
                            mapping.put(new Long(spectraCounter),
                                    new Long(spectraCounter));
                            spectraCounter++;

                            // Store the transformed spectrum.
                            aTransformedSpectra.add(fragmentation);
                        }
                    }

                    masses = new Vector();
                    intensities = new Vector();
                    charges = new Vector();
                } // Read embedded parameters.
                else if (inSpectrum && (line.indexOf("=") >= 0)) {
                    // Find the starting location of the value (which is one beyond the location
                    // of the '=').
                    int equalSignIndex = line.indexOf("=");

                    if (line.startsWith("PEPMASS")) {
                        // PEPMASS line found.
                        String value = line.substring(equalSignIndex + 1);//.trim();
                        //StringTokenizer st = new StringTokenizer(value, " \t");

                        if (properties.isCommaTheDecimalSymbol()) {
                            value = value.replaceAll(",", ".");
                        }

                        values = value.split("\t");
                        precursorMass = Double.parseDouble(values[0]);

                        if (values.length > 1) {
                            precursorIntensity = Double.parseDouble(values[1]);
                        } else {
                            precursorIntensity = 0.0;
                        }
                    } else if (line.startsWith("CHARGE")) {
                        // CHARGE line found.
                        // Note the extra parsing to read a Mascot Generic File charge (eg., 1+).
                        precursorCharge = extractCharge(line.substring(equalSignIndex + 1));
                    }
                } // Read peaks, minding the possibility of charge present!
                else if (inSpectrum) {
                    // We're inside the spectrum, with no '=' in the line, so it should be
                    // a peak line.
                    // A peak line should be either of the following two:
                    // 234.56 789
                    // 234.56 789   1+

                    if (properties.isCommaTheDecimalSymbol()) {
                        line = line.replaceAll(",", ".");
                    }

                    values = line.split("\\s+");

                    int count = values.length;

                    if (count == 2 || count == 3) {

                        masses.add(Double.parseDouble(values[0]));
                        intensities.add(new Double(values[1]));

                        if (count == 3) {
                            charges.add(new Integer(extractCharge(values[2])));
                        }
                    } else {
                        System.err.println("\n\nUnrecognized line at line number " +
                                lineCount + ": '" + line + "'!\n");
                    }
                }
            }

            br.close();

//            long end = System.currentTimeMillis();
//            System.out.println((j + 1) + " Parse: " + (end - start));// + "\n");
        }

        totalNumberOfSpectra = spectraCounter - 1;

        return mapping;
    }

    /**
     * This method transforms spectra from TPP files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aSpectra   Collection with the Spectrumfile instances to to transform.
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     */
    private static HashMap transformSpectraFromTPPFiles(ArrayList aTransformedSpectra) {

//        long timerStart = System.currentTimeMillis();

        progressDialog.setString(null);
        progressDialog.setTitle("Reading PeptideProphet File. Please Wait.");
        progressDialog.setIntermidiate(true);
        progressDialog.setString(null);

        HashMap mapping = new HashMap(); // is not used

        // Start reading the files!
        XmlPullParserFactory factory = null;
        XmlPullParser xpp = null;
        String currentFile = null;

        peptideIdCount = 0;

        try {
            factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();

            // First build a HashMap of all the peptides identified by PeptideProphet.
            BufferedReader br = new BufferedReader(new FileReader(getProperties().getPeptideProphetFileName()));
            xpp.setInput(br);
            XmlPullParserPlus xppp = new XmlPullParserPlus(xpp);
            currentFile = getProperties().getPeptideProphetFileName();
            PeptideProphetXMLParser ppxp = new PeptideProphetXMLParser(
                    new File(getProperties().getTppSpectrumFilesFolderName()), true);

            HashMap runs = ppxp.readPeptideProphet(xppp, properties.getPeptideProphetThreshold());
            br.close();

            progressDialog.setString(null);
            progressDialog.setTitle("Reading ProteinProphet File. Please Wait.");
            progressDialog.setIntermidiate(true);
            progressDialog.setString(null);

            // Now build all proteins from ProteinProphet, associating them to the peptides.
            br = new BufferedReader(new FileReader(getProperties().getProteinProphetFileName()));
            xpp.setInput(br);
            currentFile = getProperties().getProteinProphetFileName();
            ProteinProphetXMLParser ppp = new ProteinProphetXMLParser();

            Collection proteins = ppp.readProteinProphet(xppp, properties.getProteinProphetThreshold());
            ProteinProphetSummary ppSummary = ppp.getProteinProphetSummary();
            br.close();

            progressDialog.setString(null);
            progressDialog.setTitle("Creating Protein Map. Please Wait.");
            progressDialog.setIntermidiate(true);
            progressDialog.setString(null);

            // Create a HashMap of proteins for quick retrieval.
            HashMap forProteinSequences = new HashMap();

            for (Iterator lIterator = proteins.iterator(); lIterator.hasNext() && !cancelConversion;) {
                ProteinProphetProteinID protein = (ProteinProphetProteinID) lIterator.next();
                forProteinSequences.put(protein.getAccession(), protein);
            }

            // Key all queries by the peptide found in them as well
            // (again, charge + ' ' + modified sequence)
            // Also make a list of the identified scans per run (per file).
            HashMap peptidesToQueries = new HashMap();

            Iterator iter = runs.values().iterator();

            progressDialog.setString(null);
            progressDialog.setTitle("Creating Lookup Tables. Please Wait.");
            progressDialog.setIntermidiate(true);
            progressDialog.setString(null);

            while (iter.hasNext() && !cancelConversion) {
                PeptideProphetMSMSRun lRun = (PeptideProphetMSMSRun) iter.next();

                Iterator innerIt = lRun.getQueries().iterator();

                while (innerIt.hasNext() && !cancelConversion) {
                    PeptideProphetQuery lQuery = (PeptideProphetQuery) innerIt.next();
                    PeptideProphetSearchHit ppsh = lQuery.getSearchHit();

                    String key = ppsh.getSequence();

                    // Create the peptide (modseq + charge) to query map.
                    Collection queries = null;

                    if (peptidesToQueries.containsKey(key)) {
                        queries = (Collection) peptidesToQueries.get(key);
                    } else {
                        queries = new ArrayList();
                        peptidesToQueries.put(key, queries);
                    }

                    queries.add(lQuery);
                }
            }

            // Now we have the proteins and the peptides in their LC runs.
            // Create PRIDE protein objects.
            identifications = new ArrayList();
            HashMap runAndScanToSpectrumID = new HashMap();
            List mzDataSpectra = new ArrayList();

            progressDialog.setString(null);
            progressDialog.setTitle("Transforming Spectra. Please Wait.");
            progressDialog.setIntermidiate(true);
            progressDialog.setString(null);

            // Load all spectra.
            ppxp.addAllSpectra(mzDataSpectra, runAndScanToSpectrumID, progressDialog);

            progressDialog.setString(null);
            progressDialog.setTitle("Extracting Identifications. Please Wait.");
            progressDialog.setIntermidiate(false);
            progressDialog.setValue(0);
            progressDialog.setMax(proteins.size());

            int counter = 0;

            for (Iterator lIterator = proteins.iterator(); lIterator.hasNext() && !cancelConversion;) {

                progressDialog.setValue(counter++);

                ProteinProphetProteinID protein = (ProteinProphetProteinID) lIterator.next();

                // Report proteins without a sequence.
                String protSequence = protein.getSequence();
                if (protSequence == null) {
                    if (debug) {
                        System.out.println("Protein '" + protein.getAccession() + "' did not have an associated sequence.");
                    }
                }

                Collection PRIDE_peptides = new ArrayList();
                HashMap peptides = protein.getPeptides();

                Iterator innerIt = peptides.keySet().iterator();

                while (innerIt.hasNext() && !cancelConversion) {
                    String chargeModSeq = (String) innerIt.next();

                    // Find the corresponding queries.
                    Collection queries = (Collection) peptidesToQueries.get(chargeModSeq);

                    // Sanity check.
                    if (queries == null) {
                        if (debug) {
                            System.out.println("Could not find any queries for peptide '" + chargeModSeq +
                                    "', assigned to protein '" + protein.getAccession() + "'!");
                        }
                    } else {
                        // Collect all peptide info for each query.
                        for (Iterator lIterator1 = queries.iterator(); lIterator1.hasNext() && !cancelConversion;) {
                            PeptideProphetQuery lQuery = (PeptideProphetQuery) lIterator1.next();
                            PeptideProphetSearchHit hit = lQuery.getSearchHit();

                            // Create the peptide (modseq + charge) to query map.
                            // Alright, so since this query is identified and linked to an identified protein,
                            // the corresponding spectrum (and its parent(s)) needs to be retrieved and
                            // the spectrumidentifier should be entered in the lookup HashMap if it is novel.
                            String runName = lQuery.getRun();

                            int id = -1;

                            // First check if we have already encountered this spectrum.
                            String spectrumKey;

                            // have to be handled differently for mzXML and mgf files
                            if (lQuery.getSpectrumTitle() != null) { // MGF
                                spectrumKey = runName + " " + lQuery.getSpectrumTitle();
                            } else { // mzXML
                                spectrumKey = runName + " " + lQuery.getStartScan();
                            }

                            if (runAndScanToSpectrumID.containsKey(spectrumKey)) {
                                id = ((Integer) runAndScanToSpectrumID.get(spectrumKey)).intValue();
                            } else {
                                if (debug) {
                                    System.out.println("Unable to find pre-parsed spectrum for key '" + spectrumKey + "'!");
                                }
                                id = ppxp.addMzSpectrumForSpecifiedScan(runName, Integer.parseInt(lQuery.getStartScan()), mzDataSpectra);
                                // It is a new ID. Add it to the runAndScanToSpectrumID HashMap.
                                runAndScanToSpectrumID.put(spectrumKey, new Integer(id));
                            }

                            // Sanity check on the number of scans for this query.
                            if (!lQuery.getStartScan().equals(lQuery.getEndScan())) {
                                if (debug) {
                                    System.out.println("Query '" + lQuery.getRun() + " (" + lQuery.getStartScan() +
                                            " - " + lQuery.getEndScan() + ")" + "' consists of more than one scan!");
                                }
                            }

                            Long specRef = new Long(id);

                            // Get the hit sequence.
                            String pepSequence = hit.getSequence();

                            // Determine start and stop location if possible.
                            Integer pepStart = null;

                            if (protSequence != null) {

                                int start = protSequence.indexOf(pepSequence);

                                if (start >= 0) {
                                    start += 1;
                                    pepStart = new Integer(start);
                                } else {
                                    if (debug) {
                                        System.out.println("Could not find start position of '" + pepSequence + "' in '" + protein.getAccession() + "' (protein sequence '" + protSequence + "')!");
                                    }
                                }
                            } else {
                                if (debug) {
                                    System.out.println("Unable to determine start and stop position of '" + pepSequence + "' in '" + protein.getAccession() + "' because no protein sequence was found.");
                                }
                            }

                            Collection PRIDE_modifications = null;

                            if (hit.getModifiedAminoAcids() != null && hit.getModifiedAminoAcids().size() > 0) {

                                // Do modification stuff.
                                Collection foundMods = hit.getModifiedAminoAcids();
                                PRIDE_modifications = new ArrayList(foundMods.size());

                                // Get the ms/ms run - the modifications are listed there.
                                PeptideProphetMSMSRun run = (PeptideProphetMSMSRun) runs.get(lQuery.getRun());
                                PeptideProphetSearch search_info = run.getSearch_info();

                                // The modifications are keyed by their symbol
                                // (which is not used anywhere else in the XML files).
                                HashMap modificationMap = search_info.getModifications();

                                // Cycle across all modifications to create a lookup table based on the mass
                                // and the amino acid affected.
                                Iterator modIter = modificationMap.values().iterator();
                                HashMap modificationByMass = new HashMap(modificationMap.size());

                                while (modIter.hasNext()) {
                                    PeptideProphetModification mod = (PeptideProphetModification) modIter.next();

                                    // Now add the mass as key and the modification as value to the lookup table.
                                    // Please note that we check for uniqueness of the key. If the masses are not unique,
                                    // issue an error.
                                    Object duplicate = modificationByMass.put(mod.getMass() + "_" + mod.getAminoacid(), mod);

                                    if (duplicate != null) {
                                        if (debug) {
                                            System.out.println("Modifications with non-unique combination of mass and amino acid (" + mod.getMass() + " " + mod.getAminoacid() + ") found!");
                                        }
                                    }
                                }

                                // Now cycle through the found modifications and annotate them in PRIDE style using OLS
                                for (Iterator lIterator2 = foundMods.iterator(); lIterator2.hasNext();) {
                                    PeptideProphetModifiedAminoAcid ppma = (PeptideProphetModifiedAminoAcid) lIterator2.next();
                                    double mass = ppma.getMass();
                                    char residue = pepSequence.charAt(ppma.getPosition() - 1);
                                    String key = mass + "_" + residue;

                                    // Now look this up; precision differs between the definitions however :(,
                                    // so we have to find the correct result.
                                    while (modificationByMass.get(key) == null) {
                                        BigDecimal bd = new BigDecimal(Double.toString(mass));
                                        // If we round ALL decimals, something is horribly wrong!
                                        bd = bd.setScale(bd.scale() - 1, BigDecimal.ROUND_HALF_UP);

                                        if (bd.scale() == 0) {
                                            if (debug) {
                                                System.out.println("Rounded down modification mass for '" + ppma.getMass() + "_" + residue + "' down to '" + bd.doubleValue() +
                                                        "' without finding a match!");
                                            }
                                            break;
                                        }

                                        mass = bd.doubleValue();
                                        key = mass + "_" + residue;
                                    }

                                    // Okay, we can now try to retrieve the modification.
                                    PeptideProphetModification modInfo = (PeptideProphetModification) modificationByMass.get(key);

                                    // map the modification to the correct PSI-MOD modification using OLS if needed
                                    if (!properties.getAlreadyChoosenModifications().contains(key)) {
                                        new ModificationMapping(outputFrame, true, progressDialog,
                                                key,
                                                modInfo.getAminoacid(),
                                                modInfo.getMassDiff(),
                                                (CvParamImpl) userProperties.getCVTermMappings().get(key));
                                        properties.getAlreadyChoosenModifications().add(key);
                                    } else {
                                        //do nothing, mapping already choosen
                                    }

                                    if (userProperties.getCVTermMappings().get(
                                            (key)) != null) {

                                        CvParamImpl tempCvParam =
                                                (CvParamImpl) userProperties.getCVTermMappings().get(key);

                                        ArrayList modificationCVParams = new ArrayList();
                                        modificationCVParams.add(tempCvParam);

                                        ArrayList monoMasses = new ArrayList();
                                        // get the modification mass (DiffMono) retrieved from PSI-MOD
                                        if (tempCvParam.getValue() != null) {
                                            monoMasses.add(new MonoMassDeltaImpl(
                                                    new Double(tempCvParam.getValue()).doubleValue()));
                                        } else {
                                            // if the DiffMono is not found for the PSI-MOD the mass 
                                            // from the file is used
                                            monoMasses.add(new MonoMassDeltaImpl(modInfo.getMassDiff()));
                                        //monoMasses = null;
                                        }

                                        PRIDE_modifications.add(new ModificationImpl(
                                                tempCvParam.getAccession(),
                                                new Integer(ppma.getPosition()),
                                                tempCvParam.getCVLookup(),
                                                null,
                                                monoMasses,
                                                null,
                                                modificationCVParams,
                                                null));
                                    } else {
                                        Util.writeToErrorLog("Unknown modifications! - Should not happen!!! Modification: " + key);
                                    }
                                }
                            }

                            Collection additionalParams = new ArrayList();

                            // Add the PeptideProphet probability.
                            additionalParams.add(new CvParamImpl(
                                    "PRIDE:0000099", "PRIDE", "PeptideProphet probability score",
                                    0, hit.getProbability() + ""));

                            // Retrieval of the constituent scores.
                            String dotproduct = (String) hit.getSearchScores().get("dotproduct");
                            String delta = (String) hit.getSearchScores().get("delta");
                            String deltastar = (String) hit.getSearchScores().get("deltastar");
                            String zscore = (String) hit.getSearchScores().get("zscore");
                            String expect = (String) hit.getSearchScores().get("expect");

                            // Only add the parameter if it can be found.
                            // Since these should be found, signal all missing
                            // params as a logger warning!
                            if (dotproduct != null) {
                                additionalParams.add(new CvParamImpl("PRIDE:0000179", "PRIDE", "dotproduct", 1, dotproduct));
                            } else {
                                if (debug) {
                                    System.out.println("No dotproduct score found for peptide " + hit.getModifiedSequence());
                                }
                            }

                            if (delta != null) {
                                additionalParams.add(new CvParamImpl("PRIDE:0000180", "PRIDE", "delta", 2, delta));
                            } else {
                                if (debug) {
                                    System.out.println("No delta found for peptide " + hit.getModifiedSequence());
                                }
                            }

                            if (deltastar != null) {
                                additionalParams.add(new CvParamImpl("PRIDE:0000181", "PRIDE", "deltastar", 3, deltastar));
                            } else {
                                if (debug) {
                                    System.out.println("No deltastar found for peptide " + hit.getModifiedSequence());
                                }
                            }

                            if (zscore != null) {
                                additionalParams.add(new CvParamImpl("PRIDE:0000182", "PRIDE", "zscore", 4, zscore));
                            } else {
                                if (debug) {
                                    System.out.println("No zscore score found for peptide " + hit.getModifiedSequence());
                                }
                            }

                            if (expect != null) {
                                additionalParams.add(new CvParamImpl("PRIDE:0000183", "PRIDE", "expect", 5, expect));
                            } else {
                                if (debug) {
                                    System.out.println("No expect found for peptide " + hit.getModifiedSequence());
                                }
                            }

                            peptideIdCount++;

                            // Create the peptide.
                            PRIDE_peptides.add(new PeptideImpl(specRef, pepSequence, pepStart, PRIDE_modifications, additionalParams, null));

                            // Data integrity check.
                            if (hit.getRoundedProbability() < properties.getPeptideProphetThreshold()) {
                                if (debug) {
                                    System.out.println("Output peptide '" + chargeModSeq +
                                            "' with a PeptideProphet probability lower than " +
                                            "properties.getPeptideProphetThreshold(): " + hit.getProbability() + ".");
                                }
                            }
                        }
                    }
                }

                // Adding protein annotations.
                Collection additionalCVParams = new ArrayList();
                if (protSequence != null) {
                    additionalCVParams.add(new CvParamImpl("PRIDE:0000041", "PRIDE",
                            "Search database protein sequence", -1, protein.getSequence()));
                }

                if (protein.getDescription() != null && !protein.getDescription().trim().equals("")) {
                    additionalCVParams.add(new CvParamImpl("PRIDE:0000063", "PRIDE",
                            "Protein description line", -1, protein.getDescription()));
                }

                additionalCVParams.add(new CvParamImpl("PRIDE:0000100", "PRIDE",
                        "ProteinProphet probability score", -1, protein.getProbability() + ""));
                Collection isoforms = protein.getIsoforms();

                if (isoforms != null && !isoforms.isEmpty()) {
                    for (Iterator lIterator1 = isoforms.iterator(); lIterator1.hasNext();) {
                        ProteinProphetIsoform isoform = (ProteinProphetIsoform) lIterator1.next();
                        additionalCVParams.add(new CvParamImpl("PRIDE:0000098", "PRIDE",
                                "Indistinguishable alternative protein accession", -1, isoform.getAccession()));
                    }
                }

                // iTraq calculations
                // not yet finished. some links between the identification and the spectra are missing.
//                ArrayList userParams = new ArrayList();
//                
//                if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
//                    iTRAQ iTRAQValues = new iTRAQ(
//                            ((BinaryArrayImpl) ((Spectrum) aTransformedSpectra.get(
//                            i)).getMzArrayBinary()).getDoubleArray(),
//                            ((BinaryArrayImpl) ((Spectrum) aTransformedSpectra.get(
//                            i)).getIntenArrayBinary()).getDoubleArray(),
//                            tempIdentification.getExp_mass().doubleValue(), 
//                            tempIdentification.getCharge(),
//                            userProperties.getPeakIntegrationRangeLower(),
//                            userProperties.getPeakIntegrationRangeUpper(),
//                            userProperties.getReporterIonIntensityThreshold(),
//                            userProperties.getPurityCorrections());
//                    iTRAQValues.calculateiTRAQValues();
//
//                    additionalCVParams = addItraqCVTerms(additionalCVParams, iTRAQValues);
//                    userParams = addItraqUserTerms(iTRAQValues);
//                } else {
//                    userParams = null;
//                }

                uk.ac.ebi.pride.model.interfaces.core.Identification PRIDE_protein;

                if (properties.isGelFree()) {

                    //GelFreeIdentificationImpl PRIDE_protein = new GelFreeIdentificationImpl(
                    PRIDE_protein =
                            new GelFreeIdentificationImpl(
                            protein.getAccession(), //protein accession
                            protein.getVersion(), // accession version
                            null, // spliceforms
                            ppSummary.getSourceDatabase(), // database
                            PRIDE_peptides, // the peptides
                            additionalCVParams, // cv params
                            null, // user params
                            ppSummary.getSoftware(), // search engine
                            ppSummary.getDbVersion(), // database version
                            new Double(protein.getPercent_coverage() / 100), // sequence coverage
                            new Double(protein.getProbability()), // score
                            new Double(properties.getProteinProphetThreshold()), // threshold
                            null); // spectrum reference
                } else {
                    PRIDE_protein = new TwoDimensionalIdentificationImpl(
                            protein.getAccession(), //protein accession
                            protein.getVersion(), // accession version
                            null, // spliceforms
                            ppSummary.getSourceDatabase(), // database
                            ppSummary.getDbVersion(), // database version
                            PRIDE_peptides, // the peptides
                            additionalCVParams, // cv params
                            null, // user params
                            null, // PI
                            null, // MW
                            null, // sequence coverage
                            null, // the gel
                            null, // x coordinate
                            null, // y Coordinate
                            null, // spectrum reference
                            ppSummary.getSoftware(), // search engine
                            new Double(protein.getProbability()), // score
                            new Double(properties.getProteinProphetThreshold())); // threshold
                }

                identifications.add(PRIDE_protein);
            }

            if (!cancelConversion) {

                progressDialog.setString(null);
                progressDialog.setTitle("Creating mzData File. Please Wait.");
                progressDialog.setIntermidiate(true);
                progressDialog.setString(null);

                // The CV lookup stuff. (NB: currently hardcoded to PSI only)
                Collection cvLookups = new ArrayList(1);
                cvLookups.add(new CVLookupImpl(properties.getCvVersion(),
                        properties.getCvFullName(),
                        properties.getCvLabel(),
                        properties.getCvAddress()));

                // Instrument source CV parameters.
                Collection instrumentSourceCVParameters = new ArrayList(1);
                instrumentSourceCVParameters.add(new CvParamImpl(
                        properties.getAccessionInstrumentSourceParameter(),
                        properties.getCVLookupInstrumentSourceParameter(),
                        properties.getNameInstrumentSourceParameter(),
                        0,
                        properties.getValueInstrumentSourceParameter()));

                // Instrument detector parameters.
                Collection instrumentDetectorParamaters = new ArrayList(1);
                instrumentDetectorParamaters.add(new CvParamImpl(
                        properties.getAccessionInstrumentDetectorParamater(),
                        properties.getCVLookupInstrumentDetectorParamater(),
                        properties.getNameInstrumentDetectorParamater(),
                        0,
                        properties.getValueInstrumentDetectorParamater()));

                // sample details
                ArrayList sampleDescriptionUserParams = new ArrayList(
                        properties.getSampleDescriptionUserSubSampleNames().size());

                for (int i = 0; i < properties.getSampleDescriptionUserSubSampleNames().size(); i++) {
                    sampleDescriptionUserParams.add(new UserParamImpl(
                            "SUBSAMPLE_" + (i + 1),
                            i, (String) properties.getSampleDescriptionUserSubSampleNames().
                            get(i)));
                }

                for (int i = 0; i < properties.getSampleDescriptionCVParamsQuantification().size(); i++) {
                    properties.getSampleDescriptionCVParams().add(
                            properties.getSampleDescriptionCVParamsQuantification().get(i));
                }

                // Create mzData instance.
                getProperties().setMzDataFile(
                        new MzDataImpl(
                        mzDataSpectra,
                        null, // software compeletion time
                        properties.getContacts(),
                        properties.getInstrumentName(),
                        properties.getProcessingMethod(),
                        properties.getProcessingMethodUserParams(),
                        properties.getSourceFile(),
                        userProperties.getCurrentSampleSet(),
                        properties.getInstrumentAdditionalCvParams(),
                        properties.getInstrumentAdditionalUserParams(),
                        cvLookups,
                        properties.getMzDataVersion(),
                        instrumentDetectorParamaters,
                        properties.getInstrumentDetectorUserParams(),
                        properties.getSampleDescriptionComment(),
                        properties.getMzDataAccessionNumber(),
                        properties.getAnalyzerList(),
                        properties.getSoftwareComments(),
                        instrumentSourceCVParameters,
                        properties.getInstrumentSourceUserParams(),
                        properties.getSoftwareVersion(),
                        properties.getSampleDescriptionCVParams(),
                        sampleDescriptionUserParams,
                        properties.getSoftwareName()));
            }
        } catch (Exception e) {
            // Not good. Print line and dump stacktrace.
            if (factory == null) {
                //logger.error(" *** Unable to create XmlPullParserFactory!");
            } else if (xpp == null) {
                //logger.error(" *** Unable to create XmlPullParser!");
            } else {
                //logger.error(" *** Error parsing line " + xpp.getLineNumber() + " in file '" + currentFile + "'!");
            }
            //logger.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(
                    null, "An error occured when parsing a Peptide-/ProteinProphet project.\n" +
                    "See ../Properties/ErrorLog.txt for more details.",
                    "Error Parsing Peptide-/ProteinProphet", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error Parsing Peptide-/ProteinProphet: ");
            e.printStackTrace();
        }

//        long end = System.currentTimeMillis();
//        System.out.println("Transformation Done: " + (end - timerStart) + "\n");

        return mapping;
    }

    /**
     * This method transforms spectra from MS2 files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aSpectra   Collection with the Spectrumfile instances to to transform.
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     */
    private static HashMap transformSpectraFromMS2Files(ArrayList aTransformedSpectra)
            throws IOException {

        HashMap mapping = new HashMap();

        double[][] arrays;
        Collection precursors;

        Collection ionSelection;

        int charge;
        double intensity;
        int spectraCounter = 1;
        String filePath;

        Collection spectrumDescriptionComments;
        String identified;
        Spectrum fragmentation;
        File tempFile;

        boolean matchFound = false;

        Vector masses;
        Vector intensities;
        Vector charges;
        String[] values, values2;
        double precursorMass = 0.0;
        double precursorIntensity = 0.0;
        double precursorRetentionTime = -1;
        int precursorCharge = -1;
        Double mzRangeStart;
        Double mzRangeStop;

        progressDialog.setString(null);
        progressDialog.setIntermidiate(true);

        for (int j = 0; j < properties.getSelectedSourceFiles().size() && !cancelConversion; j++) {

            //long start = System.currentTimeMillis();

            filePath = properties.getSelectedSourceFiles().get(j);

            currentFileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);

            progressDialog.setIntermidiate(true);
            progressDialog.setString(currentFileName + " (" + (j + 1) + "/" +
                    properties.getSelectedSourceFiles().size() + ")");

            BufferedReader br = new BufferedReader(new FileReader(filePath));

            String line = null;
            int lineCount = 0;
            masses = new Vector();
            intensities = new Vector();
            boolean multipleCharges = false;

            while ((line = br.readLine()) != null && !cancelConversion) {
                // Advance line count.
                lineCount++;

                // Skip empty lines.
                if (line.equals("")) {
                    continue;
                }

                // ignore header
                if (line.startsWith("H")) {
                    continue;
                }

                // charge dependent analysis
                if (line.startsWith("D")) {
                    continue;
                }

                // 'S' marks the start of a new spectrum
                if (line.startsWith("S")) {

                    // store the previous spectrum
                    if (masses.size() > 0) {

                        matchFound = false;

                        if (properties.getSelectedSpectraNames().size() > 0) {
                            for (int k = 0; k < properties.getSelectedSpectraNames().size() &&
                                    !matchFound && !cancelConversion; k++) {

                                Object[] temp = (Object[]) properties.getSelectedSpectraNames().get(k);

                                if (((String) temp[0]).equalsIgnoreCase(currentFileName)) {

                                    if (((Double) temp[2]).doubleValue() == precursorMass) {

                                        if (temp[3] != null) {
                                            if (((Integer) temp[3]).intValue() == precursorCharge) {
                                                matchFound = true;
                                            }
                                        } else {
                                            matchFound = true;
                                        }
                                    }
                                }
                            }
                        } else {
                            matchFound = true;
                        }

                        if (matchFound && !cancelConversion) {

                            arrays = new double[2][masses.size()];

                            for (int i = 0; i < masses.size() && !cancelConversion; i++) {
                                arrays[0][i] = ((Double) masses.get(i)).doubleValue();
                                arrays[1][i] = ((Double) intensities.get(i)).doubleValue();
                            }

                            // Precursor collection.
                            precursors = new ArrayList(1);

                            // Ion selection parameters.
                            ionSelection = new ArrayList(3);

                            if (precursorCharge > 0) {
                                ionSelection.add(new CvParamImpl("PSI:1000041",
                                        "PSI", "ChargeState", 0,
                                        Integer.toString(precursorCharge)));
                            }

//                            if (precursorIntensity > 1) {
//                                ionSelection.add(new CvParamImpl("PSI:1000042",
//                                        "PSI", "Intensity", 1,
//                                        Double.toString(precursorIntensity)));
//                            }

                            // precursor retention time
                            if (precursorRetentionTime != -1) {
                                ionSelection.add(new CvParamImpl("PRIDE:0000203",
                                        "PRIDE", "Parent ion retention time", 3, Double.toString(
                                        precursorRetentionTime)));
                            }

                            ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                                    "MassToChargeRatio", 2, Double.toString(
                                    precursorMass)));

                            precursors.add(new PrecursorImpl(null, null,
                                    ionSelection, null, 1, 0, 0));

                            // Spectrum description comments.
                            spectrumDescriptionComments = new ArrayList(1);

                            identified = "Not identified";
                            spectrumDescriptionComments.add(new SpectrumDescCommentImpl(identified));

                            if (arrays[properties.MZ_ARRAY].length > 0) {
                                mzRangeStart = new Double(arrays[properties.MZ_ARRAY][0]);
                                mzRangeStop = new Double(
                                        arrays[properties.MZ_ARRAY][arrays[properties.MZ_ARRAY].length - 1]);

                                fragmentation =
                                        new SpectrumImpl(
                                        new BinaryArrayImpl(arrays[properties.INTENSITIES_ARRAY],
                                        BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                        mzRangeStart,
                                        new BinaryArrayImpl(arrays[properties.MZ_ARRAY],
                                        BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                        2l, null,
                                        mzRangeStop,
                                        null,
                                        spectraCounter, precursors,
                                        spectrumDescriptionComments,
                                        null, null, null, null);

                                // Store (spectrumfileid, spectrumid) mapping.
                                mapping.put(new Long(spectraCounter),
                                        new Long(spectraCounter));
                                spectraCounter++;

                                // Store the transformed spectrum.
                                aTransformedSpectra.add(fragmentation);
                            }
                        }

                        multipleCharges = false;
                        precursorRetentionTime = -1;
                        precursorCharge = -1;
                        masses = new Vector();
                        intensities = new Vector();
                    }

                    if (properties.isCommaTheDecimalSymbol()) {
                        line = line.replaceAll(",", ".");
                    }

                    values = line.split("\t");

                    precursorMass = new Double(values[3]);

                } else if (line.startsWith("I")) {
                    if (line.lastIndexOf("RetTime") != -1) {

                        if (properties.isCommaTheDecimalSymbol()) {
                            line = line.replaceAll(",", ".");
                        }

                        values = line.split("\t");

                        precursorRetentionTime = new Double(values[2]);
                    }
                } else if (line.startsWith("Z")) {

                    if (precursorCharge != -1) {
                        multipleCharges = true;
                        precursorCharge = 0;
                    } else {
                        values = line.split("\t");
                        precursorCharge = new Integer(values[1]).intValue();
                    }
                } else { // read the peaks

                    if (properties.isCommaTheDecimalSymbol()) {
                        line = line.replaceAll(",", ".");
                    }

                    values = line.split(" ");

                    int count = values.length;

                    if (count == 2) {

                        masses.add(Double.parseDouble(values[0]));
                        intensities.add(new Double(values[1]));

                    } else {
                        System.err.println("\n\nUnrecognized line at line number " +
                                lineCount + ": '" + line + "'!\n");
                    }
                }
            }

            br.close();

            // store the last spectrum
            if (!cancelConversion) {

                if (masses.size() > 0) {

                    matchFound = false;

                    if (properties.getSelectedSpectraNames().size() > 0) {
                        for (int k = 0; k < properties.getSelectedSpectraNames().size() &&
                                !matchFound && !cancelConversion; k++) {

                            Object[] temp = (Object[]) properties.getSelectedSpectraNames().get(k);

                            if (((String) temp[0]).equalsIgnoreCase(currentFileName)) {

                                if (((Double) temp[2]).doubleValue() == precursorMass) {

                                    if (temp[3] != null) {
                                        if (((Integer) temp[3]).intValue() == precursorCharge) {
                                            matchFound = true;
                                        }
                                    } else {
                                        matchFound = true;
                                    }
                                }
                            }
                        }
                    } else {
                        matchFound = true;
                    }

                    if (matchFound && !cancelConversion) {

                        arrays = new double[2][masses.size()];

                        for (int i = 0; i < masses.size() && !cancelConversion; i++) {
                            arrays[0][i] = ((Double) masses.get(i)).doubleValue();
                            arrays[1][i] = ((Double) intensities.get(i)).doubleValue();
                        }

                        // Precursor collection.
                        precursors = new ArrayList(1);

                        // Ion selection parameters.
                        ionSelection = new ArrayList(3);

                        if (precursorCharge > 0) {
                            ionSelection.add(new CvParamImpl("PSI:1000041",
                                    "PSI", "ChargeState", 0,
                                    Integer.toString(precursorCharge)));
                        }

//                            if (precursorIntensity > 1) {
//                                ionSelection.add(new CvParamImpl("PSI:1000042",
//                                        "PSI", "Intensity", 1,
//                                        Double.toString(precursorIntensity)));
//                            }

                        // precursor retention time
                        if (precursorRetentionTime != -1) {
                            ionSelection.add(new CvParamImpl("PRIDE:0000203",
                                    "PRIDE", "Parent ion retention time", 3, Double.toString(
                                    precursorRetentionTime)));
                        }

                        ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                                "MassToChargeRatio", 2, Double.toString(
                                precursorMass)));

                        precursors.add(new PrecursorImpl(null, null,
                                ionSelection, null, 1, 0, 0));

                        // Spectrum description comments.
                        spectrumDescriptionComments = new ArrayList(1);

                        identified = "Not identified";
                        spectrumDescriptionComments.add(new SpectrumDescCommentImpl(identified));

                        if (arrays[properties.MZ_ARRAY].length > 0) {
                            mzRangeStart = new Double(arrays[properties.MZ_ARRAY][0]);
                            mzRangeStop = new Double(
                                    arrays[properties.MZ_ARRAY][arrays[properties.MZ_ARRAY].length - 1]);

                            fragmentation =
                                    new SpectrumImpl(
                                    new BinaryArrayImpl(arrays[properties.INTENSITIES_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    mzRangeStart,
                                    new BinaryArrayImpl(arrays[properties.MZ_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    2l, null,
                                    mzRangeStop,
                                    null,
                                    spectraCounter, precursors,
                                    spectrumDescriptionComments,
                                    null, null, null, null);

                            // Store (spectrumfileid, spectrumid) mapping.
                            mapping.put(new Long(spectraCounter),
                                    new Long(spectraCounter));
                            spectraCounter++;

                            // Store the transformed spectrum.
                            aTransformedSpectra.add(fragmentation);
                        }
                    }
                }
            }
        }

        totalNumberOfSpectra = spectraCounter - 1;

        return mapping;
    }

    /**
     * This method transforms spectra from Mascot Dat files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aSpectra   Collection with the Spectrumfile instances to to transform.
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     */
    private static HashMap transformSpectraFromMascotDatFile(ArrayList aTransformedSpectra)
            throws IOException {
        HashMap mapping = new HashMap();

        emptySpectraCounter = 0;
        int idCounter = 1;
        int progressCounter = 0;

        QueryToPeptideMapInf queryToPeptideMap;
        QueryEnumerator queries;
        Query currentQuery;
        PeptideHit tempPeptideHit;

        Peak[] peakList;
        double[][] arrays;
        Collection precursors, ionSelection;
        String chargeString;
        String fileName;

        boolean matchFound = false;
        int iconScore, threshold;
        StringTokenizer tok;

        Double mzStartRange = null;
        Double mzStopRange = null;
        File tempFile;

        double intensity;
        Collection spectrumDescriptionComments;
        Spectrum fragmentation;

        ids = new ArrayList<IdentificationGeneral>();
        properties.setSelectedProteinHits(new ArrayList());
        properties.setSelectedIsoformAccessions(new ArrayList());
        properties.setSelectedIsoformPeptideSequences(new ArrayList());
        setCancelConversion(false);
        properties.setIsoFormsSelectionTypeSelected(false);
        boolean alreadySelected;
        boolean peptideIsIdentified;

        Vector fixedModifications, variableModifications;
        String modName;
        ArrayList cVParams, peptideModifications, modificationCVParams, monoMasses, userParams;

        progressDialog.setIntermidiate(false);
        MascotDatfileInf tempMascotDatfile;

        boolean calculateITraq = false;

        if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
            calculateITraq = true;
        }

        for (int k = 0; k < properties.getSelectedSourceFiles().size() && !cancelConversion; k++) {

            tempFile = new File(properties.getSelectedSourceFiles().get(k));

            currentFileName = tempFile.getName();

            progressDialog.setIntermidiate(true);
            progressDialog.setValue(0);
            progressDialog.setString(currentFileName + " (" + (k + 1) + "/" +
                    properties.getSelectedSourceFiles().size() + ")");

            double size = (double) tempFile.length() /
                    properties.NUMBER_OF_BYTES_PER_MEGABYTE;

            if (size > properties.MAX_MASCOT_DAT_FILESIZE_BEFORE_INDEXING) {
                //if file is large
                tempMascotDatfile = MascotDatfileFactory.create(tempFile.getPath(),
                        MascotDatfileType.INDEX);
            } else {
                tempMascotDatfile = MascotDatfileFactory.create(tempFile.getPath(),
                        MascotDatfileType.MEMORY);
            }

            // handle the modifications
            fixedModifications = tempMascotDatfile.getModificationList().getFixedModifications();
            variableModifications = tempMascotDatfile.getModificationList().getVariableModifications();

            for (int i = 0; i < fixedModifications.size() && !cancelConversion; i++) {
                modName = ((FixedModification) fixedModifications.get(i)).getType();

                if (!properties.getAlreadyChoosenModifications().contains(modName)) {
                    new ModificationMapping(outputFrame, true, progressDialog,
                            (Modification) fixedModifications.get(i),
                            (CvParamImpl) userProperties.getCVTermMappings().get(modName));

                    properties.getAlreadyChoosenModifications().add(modName);
                } else {
                    //do nothing, mapping already choosen
                }
            }

            for (int i = 0; i < variableModifications.size() && !cancelConversion; i++) {

                modName = ((VariableModification) variableModifications.get(i)).getType();

                if (!properties.getAlreadyChoosenModifications().contains(modName)) {
                    new ModificationMapping(outputFrame, true, progressDialog,
                            (Modification) variableModifications.get(i),
                            (CvParamImpl) userProperties.getCVTermMappings().get(modName));
                    properties.getAlreadyChoosenModifications().add(modName);
                } else {
                    //do nothing, mapping already choosen
                }
            }

            //calculate iTraq values
            iTRAQ iTRAQValues = null;

            if (calculateITraq) {
                iTRAQValues =
                        new iTRAQ(tempFile.getPath(),
                        userProperties.getPeakIntegrationRangeLower(),
                        userProperties.getPeakIntegrationRangeUpper(),
                        userProperties.getReporterIonIntensityThreshold(),
                        userProperties.getPurityCorrections());
                iTRAQValues.calculateiTRAQValues();
            }

            queryToPeptideMap = tempMascotDatfile.getQueryToPeptideMap();
            queries = tempMascotDatfile.getQueryEnumerator();

            progressCounter = 0;
            progressDialog.setIntermidiate(false);
            progressDialog.setMax(tempMascotDatfile.getNumberOfQueries());
            progressDialog.setValue(0);

            while (queries.hasMoreElements() && !cancelConversion) {

                progressDialog.setValue(progressCounter++);

                currentQuery = queries.nextElement();
                matchFound = false;
                fileName = currentQuery.getFilename();

                if (properties.getSelectedSpectraNames().size() > 0) {
                    for (int i = 0; i < properties.getSelectedSpectraNames().size() && !matchFound; i++) {
                        if (((String) ((Object[]) properties.getSelectedSpectraNames().get(i))[0]).equalsIgnoreCase(
                                fileName)) {
                            matchFound = true;
                        } else {
                            matchFound = false;
                        }
                    }
                } else {
                    if (properties.getSpectraSelectionCriteria() != null) {
                        if (userProperties.getCurrentFileNameSelectionCriteriaSeparator().
                                length() == 0) {
                            tok = new StringTokenizer(properties.getSpectraSelectionCriteria());
                        } else {
                            tok = new StringTokenizer(properties.getSpectraSelectionCriteria(),
                                    userProperties.getCurrentFileNameSelectionCriteriaSeparator());
                        }
                    } else {
                        matchFound = true;
                    }
                }

                if (matchFound) {

                    peptideIsIdentified = false;
                    tempPeptideHit = null;

                    try {
                        peakList = currentQuery.getPeakList();
                        arrays = new double[2][peakList.length];

                        for (int j = 0; j < peakList.length; j++) {
                            arrays[0][j] = peakList[j].getMZ();
                            arrays[1][j] = peakList[j].getIntensity();
                        }

                        if (peakList.length > 0) {
                            mzStartRange = currentQuery.getMinMZ();
                            mzStopRange = currentQuery.getMaxMZ();

                            // Precursor collection.
                            precursors = new ArrayList(1);

                            // Ion selection parameters.
                            ionSelection = new ArrayList(3);

                            // See if we know the precursor charge, and if so, include it.
                            chargeString = currentQuery.getChargeString();

                            chargeString = chargeString.replaceFirst("\\+", "");

                            if (!chargeString.equalsIgnoreCase("0")) {
                                ionSelection.add(new CvParamImpl("PSI:1000041",
                                        "PSI", "ChargeState", 0, chargeString));
                            }

                            // See if we know the precursor intensity
                            intensity = currentQuery.getPrecursorIntensity();

                            if (intensity > 1) {
                                ionSelection.add(new CvParamImpl("PSI:1000042",
                                        "PSI", "Intensity", 1,
                                        Double.toString(intensity)));
                            }

                            ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                                    "MassToChargeRatio", 2,
                                    Double.toString(currentQuery.getPrecursorMZ())));

                            precursors.add(new PrecursorImpl(null, null,
                                    ionSelection, null, 1, 0, 0));

                            // Spectrum description comments.
                            spectrumDescriptionComments = new ArrayList(1);

                            tempPeptideHit = queryToPeptideMap.getPeptideHitOfOneQuery(currentQuery.getQueryNumber());

                            if (tempPeptideHit != null) {

                                //Added to make up for the "bug" in old version of the Mascot Perl script.
                                //This script rounds down (!) to the lower integer both the threshold and 
                                //the score, and if the result is >= threshold it is labeled identified.
                                if (properties.roundMascotScoreAndThresholdDownToNearestInteger()) {

                                    iconScore = new Double(tempPeptideHit.getIonsScore()).intValue();
                                    threshold = new Double(tempPeptideHit.calculateIdentityThreshold((100 -
                                            (double) properties.getMascotConfidenceLevel()) / 100)).intValue();

                                    if (iconScore >= threshold) {
                                        spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Identified"));
                                        peptideIsIdentified = true;
                                    } else {
                                        spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Not identified"));
                                        peptideIsIdentified = false;
                                    }
                                } else {
                                    if (queryToPeptideMap.getPeptideHitsAboveIdentityThreshold(currentQuery.getQueryNumber(),
                                            (100 - (double) properties.getMascotConfidenceLevel()) / 100).size() > 0) {
                                        spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Identified"));
                                        peptideIsIdentified = true;
                                    } else {
                                        spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Not identified"));
                                        peptideIsIdentified = false;
                                    }
                                }
                            } else {
                                spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Not identified"));
                                peptideIsIdentified = false;
                            }

                            // Create new mzData spectrum for the fragmentation spectrum.
                            fragmentation = new SpectrumImpl(
                                    new BinaryArrayImpl(arrays[properties.INTENSITIES_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    mzStartRange,
                                    new BinaryArrayImpl(arrays[properties.MZ_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    2l,
                                    null,
                                    mzStopRange,
                                    null,
                                    idCounter, precursors,
                                    spectrumDescriptionComments,
                                    null, null, null, null);

                            // Store (spectrumfileid, spectrumid) mapping.
                            //mapping.put(new Long(idCounter), new Long(idCounter++));
                            mapping.put(currentQuery.getFilename(), new Long(idCounter));
                            idCounter++;

                            // Store the transformed spectrum.
                            aTransformedSpectra.add(fragmentation);

                            // extract the peptide identifications
                            if (peptideIsIdentified) {
                                properties.setTempProteinHit(
                                        (ProteinHit) tempPeptideHit.getProteinHits().get(0));

                                if (tempPeptideHit.getProteinHits().size() > 1) {

                                    if (!properties.isIsoFormsSelectionTypeSelected()) {
                                        new ProteinIsoforms(outputFrame, true, progressDialog);
                                        properties.setIsoFormsSelectionTypeSelected(true);
                                    }

                                    if (properties.getProteinIsoformSelectionType() ==
                                            properties.PROTEIN_ISOFORMS_ALWAYS_SELECT_FIRST) {

                                        properties.setTempProteinHit(
                                                (ProteinHit) tempPeptideHit.getProteinHits().get(0));

                                    } else if (properties.getProteinIsoformSelectionType() ==
                                            properties.PROTEIN_ISOFORMS_MAUNAL_SELECTION) {

                                        alreadySelected = false;

                                        for (int w = 0; w < tempPeptideHit.getProteinHits().size() &&
                                                !alreadySelected; w++) {
                                            if (properties.getSelectedProteinHits().contains(
                                                    ((ProteinHit) tempPeptideHit.getProteinHits().get(w)).getAccession())) {
                                                alreadySelected = true;
                                            }
                                        }

                                        if (!alreadySelected) {

                                            if (cancelConversion) {
                                                outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                                                outputFrame.setConvertButtonEnabled(true);
                                                return null;
                                            }

                                            new ProteinIsoFormSelection(outputFrame, true,
                                                    tempPeptideHit, progressDialog);
                                        }
                                    } else {//list provided

                                        if (properties.getSelectedIsoformPeptideSequences().
                                                contains(tempPeptideHit.getSequence())) {

                                            int index =
                                                    properties.getSelectedIsoformPeptideSequences().indexOf(
                                                    tempPeptideHit.getSequence());

                                            for (int h = 0; h < tempPeptideHit.getProteinHits().size(); h++) {

                                                if (((ProteinHit) tempPeptideHit.getProteinHits().
                                                        get(h)).getAccession().equalsIgnoreCase(
                                                        (String) properties.getSelectedIsoformAccessions().get(index))) {
                                                    properties.setTempProteinHit((ProteinHit) tempPeptideHit.getProteinHits().get(h));
                                                }
                                            }
                                        } else {//not found. do manual selection
                                            alreadySelected = false;

                                            for (int w = 0; w < tempPeptideHit.getProteinHits().size() &&
                                                    !alreadySelected; w++) {
                                                if (properties.getSelectedProteinHits().contains(
                                                        ((ProteinHit) tempPeptideHit.getProteinHits().
                                                        get(w)).getAccession())) {
                                                    alreadySelected = true;
                                                }
                                            }

                                            if (!alreadySelected) {

                                                if (/*!progressDialog.isVisible() ||*/cancelConversion) {
                                                    outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                                                    outputFrame.setConvertButtonEnabled(true);
                                                    return null;
                                                }

                                                new ProteinIsoFormSelection(outputFrame, true,
                                                        tempPeptideHit, progressDialog);
                                            }
                                        }
                                    }
                                }

                                cVParams = new ArrayList();
                                cVParams.add(new CvParamImpl("PRIDE:0000069", "PRIDE",
                                        "Mascot Score", 0, "" + tempPeptideHit.getIonsScore()));

                                String[] iTraqNorm = null;
                                String[] iTraqUT = null;
                                String[][] iTraqRatio = null;
                                userParams = null;

                                if (calculateITraq) {
                                    iTraqNorm = (String[]) iTRAQValues.getAllNorms().get(
                                            currentQuery.getQueryNumber());
                                    iTraqUT = (String[]) iTRAQValues.getAllUTs().get(
                                            currentQuery.getQueryNumber());
                                    iTraqRatio = (String[][]) iTRAQValues.getAllRatios().get(
                                            currentQuery.getQueryNumber());

                                    cVParams = addItraqCVTerms(cVParams, iTraqNorm);
                                    userParams = addItraqUserTerms(iTraqRatio);
                                }

                                peptideModifications = null;
                                int location;

                                if (tempPeptideHit.getModifications() != null) {

                                    peptideModifications = new ArrayList();

                                    for (int m = 0; m <
                                            tempPeptideHit.getModifications().length; m++) {

                                        if (tempPeptideHit.getModifications()[m] != null) {

                                            if (userProperties.getCVTermMappings().get(
                                                    (tempPeptideHit.getModifications()[m]).getType()) !=
                                                    null) {

                                                CvParamImpl tempCvParam =
                                                        (CvParamImpl) userProperties.getCVTermMappings().get(
                                                        (tempPeptideHit.getModifications()[m]).getType());

                                                modificationCVParams = new ArrayList();
                                                modificationCVParams.add(tempCvParam);

                                                monoMasses = new ArrayList();
                                                // get the modification mass (DiffMono) retrieved from PSI-MOD
                                                if (tempCvParam.getValue() != null) {
                                                    monoMasses.add(new MonoMassDeltaImpl(
                                                            new Double(tempCvParam.getValue()).doubleValue()));
                                                } else {
                                                    // if the DiffMono is not found for the PSI-MOD the mass 
                                                    // from the file is used
                                                    monoMasses.add(new MonoMassDeltaImpl(
                                                            tempPeptideHit.getModifications()[m].getMass()));
                                                //monoMasses = null;
                                                }

                                                location = m;

//                                            //n-terminal modification
//                                            if (location ==
//                                                    0) {
//                                                location = 1;
//                                            }
//
//                                            //c-terminal modification
//                                            if (location ==
//                                                    tempPeptideHit.getModifications().length) {
//                                                location -= 1;
//                                            }

                                                peptideModifications.add(new ModificationImpl(
                                                        tempCvParam.getAccession(),
                                                        location,
                                                        tempCvParam.getCVLookup(),
                                                        null,
                                                        monoMasses,
                                                        null,
                                                        modificationCVParams,
                                                        null));
                                            } else {
                                                Util.writeToErrorLog("Unknown modifications! - Should not happen!!! Modification: " +
                                                        (tempPeptideHit.getModifications()[m]).getType());
                                            }
                                        }
                                    }
                                }

                                ids.add(new IdentificationGeneral(
                                        currentQuery.getFilename(), //spectrumFileName
                                        properties.getTempProteinHit().getAccession(), //accession
                                        "Mascot", // search engine
                                        tempMascotDatfile.getParametersSection().getDatabase(), // database
                                        tempMascotDatfile.getHeaderSection().getVersion(),//database version
                                        tempPeptideHit.getSequence(), //sequence
                                        properties.getTempProteinHit().getStart(), //Start
                                        tempPeptideHit.getIonsScore(), //score
                                        tempPeptideHit.calculateIdentityThreshold((100 -
                                        (double) properties.getMascotConfidenceLevel()) /
                                        100), //threshold
                                        iTraqNorm,
                                        iTraqUT,
                                        iTraqRatio,
                                        cVParams, userParams,
                                        peptideModifications));
                            }
                        }

                    } catch (NullPointerException e) {
//                        Util.writeToErrorLog("Query without peak list: Exception caught..." +
//                                e.toString());
                        emptySpectraCounter++;
                    }
                }
            }
        }

        totalNumberOfSpectra = idCounter - 1;
        return mapping;
    }

    /**
     * This method transforms spectra from OMSSA files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aSpectra   Collection with the Spectrumfile instances to to transform.
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     */
    private static HashMap transformSpectraFromOMSSA(ArrayList aTransformedSpectra)
            throws IOException {
        HashMap mapping = new HashMap();

        emptySpectraCounter = 0;
        int idCounter = 1;

        double[][] arrays;
        Collection precursors, ionSelection;
        String chargeString;

        String fileName;

        boolean matchFound = false;
        StringTokenizer tok;
        ids = new ArrayList<IdentificationGeneral>();

        int start;
        String accession,
                peptideSequence, upstreamFlankingSequence, downstreamFlankingSequence;
        String database;

        MSPepHit currentMSPepHit;
        MSHits currentMSHit;
        ArrayList cVParams, userParams;
        List<Integer> mzValues;
        List<Integer> intensityValues;
        Collection spectrumDescriptionComments;
        MSModHit currentMSModHit;

        int modType, modSite;

        Iterator<MSModHit> modsIterator;
        ArrayList modificationCVParams;
        ArrayList monoMasses;
        ArrayList peptideModifications;
        MSHits tempMSHit;
        Iterator<MSHits> msHitIterator;
        double lowestEValue;
        List<MSHits> allMSHits;

        DocumentBuilderFactory dbf;
        DocumentBuilder db;
        Document dom;
        Element docEle;
        NodeList nodes,
                modNodes, residueNodes, tempNodes;
        String modName = "";
        Vector modResidues;

        Integer modNumber = -1;
        Double modMonoMass = 0.0;

        HashMap<Integer, OmssaModification> omssaModificationDetails =
                new HashMap();

        OmssaOmxFile omxFile;
        List<Integer> fixedModifications;
        List<Integer> variableModifications;
        HashMap<MSSpectrum, MSHitSet> results;
        Iterator<MSSpectrum> iterator;
        MSSpectrum tempSpectrum;
        Vector<String> modifiedResidues;
        Spectrum fragmentation;

        Double mzRangeStart;
        Double mzRangeStop;

        //read the mods.xml file
        if (!cancelConversion) {
            try {
                if (userProperties.getOmssaInstallDir() != null) {
                    File mods = new File(userProperties.getOmssaInstallDir() + "mods.xml");

                    //get the factory
                    dbf = DocumentBuilderFactory.newInstance();

                    //Using factory get an instance of document builder
                    db = dbf.newDocumentBuilder();

                    //parse using builder to get DOM representation of the XML file
                    dom = db.parse(mods);

                    //get the root elememt
                    docEle = dom.getDocumentElement();

                    nodes = docEle.getChildNodes();

                    for (int i = 0; i < nodes.getLength() && !cancelConversion; i++) {

                        if (nodes.item(i).getNodeName().equalsIgnoreCase("MSModSpec")) {

                            modNodes = nodes.item(i).getChildNodes();
                            modNumber = -1;
                            modName = "";
                            modMonoMass = 0.0;
                            modResidues = new Vector();

                            for (int j = 0; j < modNodes.getLength() && !cancelConversion; j++) {

                                if (modNodes.item(j).getNodeName().equalsIgnoreCase("MSModSpec_mod")) {

                                    tempNodes = modNodes.item(j).getChildNodes();

                                    for (int m = 0; m < tempNodes.getLength(); m++) {
                                        if (tempNodes.item(m).getNodeName().equalsIgnoreCase("MSMod")) {
                                            modNumber = new Integer(tempNodes.item(m).getTextContent());
                                        }
                                    }
                                } else if (modNodes.item(j).getNodeName().equalsIgnoreCase("MSModSpec_name")) {
                                    modName = modNodes.item(j).getTextContent();
                                } else if (modNodes.item(j).getNodeName().equalsIgnoreCase("MSModSpec_monomass")) {
                                    modMonoMass = new Double(modNodes.item(j).getTextContent());
                                } else if (modNodes.item(j).getNodeName().equalsIgnoreCase("MSModSpec_residues")) {
                                    residueNodes = modNodes.item(j).getChildNodes();

                                    modResidues = new Vector();

                                    for (int m = 0; m <
                                            residueNodes.getLength(); m++) {

                                        if (residueNodes.item(m).getNodeName().equalsIgnoreCase(
                                                "MSModSpec_residues_E")) {

                                            modResidues.add(residueNodes.item(m).getTextContent());
                                        }
                                    }
                                }
                            }

                            if (modMonoMass == 0.0) {
                                modMonoMass = null;
                            }

                            omssaModificationDetails.put(modNumber,
                                    new OmssaModification(modNumber, modName,
                                    modMonoMass, modResidues));
                        }
                    }
                }
            } catch (Exception e) {
                Util.writeToErrorLog("Error parsing the mods.xml: ");
                e.printStackTrace();

                JOptionPane.showMessageDialog(null,
                        "The mods.xml file could not be parsed.\n" +
                        "See ../Properties/ErrorLog.txt for more details.",
                        "Error Parsing File", JOptionPane.ERROR_MESSAGE);
            }
        }

        //read the usermods.xml file
        if (!cancelConversion) {
            try {
                if (userProperties.getOmssaInstallDir() != null) {
                    File mods = new File(userProperties.getOmssaInstallDir() + "usermods.xml");

                    //get the factory
                    dbf = DocumentBuilderFactory.newInstance();

                    //Using factory get an instance of document builder
                    db = dbf.newDocumentBuilder();

                    //parse using builder to get DOM representation of the XML file
                    dom = db.parse(mods);

                    //get the root elememt
                    docEle = dom.getDocumentElement();

                    nodes = docEle.getChildNodes();

                    for (int i = 0; i < nodes.getLength() && !cancelConversion; i++) {

                        if (nodes.item(i).getNodeName().equalsIgnoreCase("MSModSpec")) {

                            modNodes = nodes.item(i).getChildNodes();
                            modNumber = -1;
                            modName = "";
                            modMonoMass = 0.0;
                            modResidues = new Vector();

                            for (int j = 0; j < modNodes.getLength() && !cancelConversion; j++) {

                                if (modNodes.item(j).getNodeName().equalsIgnoreCase("MSModSpec_mod")) {

                                    tempNodes = modNodes.item(j).getChildNodes();

                                    for (int m = 0; m < tempNodes.getLength(); m++) {
                                        if (tempNodes.item(m).getNodeName().equalsIgnoreCase("MSMod")) {
                                            modNumber = new Integer(tempNodes.item(m).getTextContent());
                                        }
                                    }
                                } else if (modNodes.item(j).getNodeName().equalsIgnoreCase("MSModSpec_name")) {
                                    modName = modNodes.item(j).getTextContent();
                                } else if (modNodes.item(j).getNodeName().equalsIgnoreCase("MSModSpec_monomass")) {
                                    modMonoMass = new Double(modNodes.item(j).getTextContent());
                                } else if (modNodes.item(j).getNodeName().equalsIgnoreCase("MSModSpec_residues")) {
                                    residueNodes = modNodes.item(j).getChildNodes();

                                    modResidues = new Vector();

                                    for (int m = 0; m < residueNodes.getLength(); m++) {

                                        if (residueNodes.item(m).getNodeName().equalsIgnoreCase("MSModSpec_residues_E")) {

                                            modResidues.add(residueNodes.item(m).getTextContent());
                                        }
                                    }
                                }
                            }

                            if (modMonoMass == 0.0) {
                                modMonoMass = null;
                            }

                            omssaModificationDetails.put(modNumber,
                                    new OmssaModification(modNumber, modName,
                                    modMonoMass, modResidues));
                        }
                    }
                }
            } catch (Exception e) {
                Util.writeToErrorLog("Error parsing the usermods.xml: ");
                e.printStackTrace();

                JOptionPane.showMessageDialog(null,
                        "The usermods.xml file could not be parsed.\n" +
                        "See ../Properties/ErrorLog.txt for more details.",
                        "Error Parsing File", JOptionPane.ERROR_MESSAGE);
            }
        }

        progressDialog.setIntermidiate(false);
        progressDialog.setValue(0);
        int progressCounter = 0;

        // read the omssa files
        for (int k = 0; k < properties.getSelectedSourceFiles().size() && !cancelConversion; k++) {

            currentFileName = new File(properties.getSelectedSourceFiles().get(k)).getName();

            progressDialog.setIntermidiate(true);
            progressCounter = 0;
            progressDialog.setValue(0);
            progressDialog.setString(currentFileName + " (" + (k + 1) + "/" +
                    properties.getSelectedSourceFiles().size() + ")");

            // @TODO move the parsing of the mods.xml and usermodsxml into the OmssaOmxFile
            omxFile = new OmssaOmxFile(properties.getSelectedSourceFiles().get(k), null, null);

            fixedModifications =
                    omxFile.getParserResult().MSSearch_request.MSRequest.get(0).MSRequest_settings.MSSearchSettings.MSSearchSettings_fixed.MSMod;
            variableModifications =
                    omxFile.getParserResult().MSSearch_request.MSRequest.get(0).MSRequest_settings.MSSearchSettings.MSSearchSettings_variable.MSMod;

            int omssaResponseScale =
                    omxFile.getParserResult().MSSearch_response.MSResponse.get(0).MSResponse_scale;

//            int omssaSearchSettingsScale =
//                    omxFile.getParserResult().MSSearch_request.MSRequest.get(0).MSRequest_settings.MSSearchSettings.MSSearchSettings_scale;


            // fixed modifications
            for (int i = 0; i < fixedModifications.size(); i++) {

                new ModificationMapping(outputFrame, true, progressDialog, "" +
                        omssaModificationDetails.get(fixedModifications.get(i)).getModName(),
                        omssaModificationDetails.get(fixedModifications.get(i)).getModResiduesAsString(),
                        omssaModificationDetails.get(fixedModifications.get(i)).getModMonoMass(),
                        (CvParamImpl) userProperties.getCVTermMappings().get(
                        omssaModificationDetails.get(fixedModifications.get(i)).getModName()));
            }

            // variable modifications
            for (int i = 0; i < variableModifications.size(); i++) {

                new ModificationMapping(outputFrame, true, progressDialog, "" +
                        omssaModificationDetails.get(variableModifications.get(i)).getModName(),
                        omssaModificationDetails.get(variableModifications.get(i)).getModResiduesAsString(),
                        omssaModificationDetails.get(variableModifications.get(i)).getModMonoMass(),
                        (CvParamImpl) userProperties.getCVTermMappings().get(
                        omssaModificationDetails.get(variableModifications.get(i)).getModName()));
            }

            results = omxFile.getSpectrumToHitSetMap();
            iterator = results.keySet().iterator();

            progressDialog.setIntermidiate(false);
            progressDialog.setMax(results.keySet().size());
            progressDialog.setValue(0);

            while (iterator.hasNext()) {

                progressDialog.setValue(progressCounter++);

                tempSpectrum = iterator.next();

                // OMSSA question: possible with more than one file name per spectrum??
                fileName = tempSpectrum.MSSpectrum_ids.MSSpectrum_ids_E.get(0);

                matchFound = false;

                if (properties.getSelectedSpectraNames().size() > 0) {
                    for (int i = 0; i < properties.getSelectedSpectraNames().size() && !matchFound; i++) {
                        if (((String) ((Object[]) properties.getSelectedSpectraNames().get(i))[0]).equalsIgnoreCase(
                                fileName)) {
                            matchFound = true;
                        } else {
                            matchFound = false;
                        }
                    }
                } else {
                    if (properties.getSpectraSelectionCriteria() != null) {
                        if (userProperties.getCurrentFileNameSelectionCriteriaSeparator().length() == 0) {
                            tok = new StringTokenizer(properties.getSpectraSelectionCriteria());
                        } else {
                            tok = new StringTokenizer(properties.getSpectraSelectionCriteria(),
                                    userProperties.getCurrentFileNameSelectionCriteriaSeparator());
                        }
                    } else {
                        matchFound = true;
                    }
                }

                if (matchFound) {

                    start = -1;
                    accession = null;
                    database = null;
                    peptideSequence = null;
                    upstreamFlankingSequence = null;
                    downstreamFlankingSequence = null;
                    peptideModifications = null;

                    mzValues = tempSpectrum.MSSpectrum_mz.MSSpectrum_mz_E;
                    intensityValues = tempSpectrum.MSSpectrum_abundance.MSSpectrum_abundance_E;

                    int omssaAbundanceScale = tempSpectrum.MSSpectrum_iscale;

                    arrays = new double[2][mzValues.size()];

                    for (int j = 0; j <
                            mzValues.size(); j++) {
                        arrays[0][j] = mzValues.get(j) / omssaResponseScale;
                        arrays[1][j] = intensityValues.get(j) / omssaAbundanceScale;
                    }

                    // Precursor collection.
                    precursors = new ArrayList(1);

                    // Ion selection parameters.
                    ionSelection = new ArrayList(4);

                    // OMSSA question: possible with more than one charge per spectrum??
                    chargeString = "" + tempSpectrum.MSSpectrum_charge.MSSpectrum_charge_E.get(0);
                    chargeString =
                            chargeString.replaceFirst("\\+", "");

                    if (!chargeString.equalsIgnoreCase("0")) {
                        ionSelection.add(new CvParamImpl("PSI:1000041", "PSI",
                                "ChargeState", 0, chargeString));
                    }

                    ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                            "MassToChargeRatio", 2, "" + tempSpectrum.MSSpectrum_precursormz / omssaResponseScale));

                    precursors.add(new PrecursorImpl(null, null, ionSelection,
                            null, 1, 0, 0));

                    // Spectrum description comments.
                    spectrumDescriptionComments = new ArrayList(1);

                    if (results.get(tempSpectrum).MSHitSet_hits.MSHits.size() > 0) {
                        spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Identified"));

                        cVParams = new ArrayList();

                        // find (and select) the MSHit with the lowest e-value
                        allMSHits = results.get(tempSpectrum).MSHitSet_hits.MSHits;
                        msHitIterator = allMSHits.iterator();
                        lowestEValue = Double.MAX_VALUE;
                        currentMSHit = null;

                        while (msHitIterator.hasNext()) {

                            tempMSHit = msHitIterator.next();

                            if (tempMSHit.MSHits_evalue < lowestEValue) {
                                lowestEValue = tempMSHit.MSHits_evalue;
                                currentMSHit = tempMSHit;
                            }
                        }

                        cVParams.add(new CvParamImpl("PRIDE:0000185", "PRIDE",
                                "OMSSA E-value", cVParams.size(), "" +
                                currentMSHit.MSHits_evalue));

                        cVParams.add(new CvParamImpl("PRIDE:0000186", "PRIDE",
                                "OMSSA P-value", cVParams.size(), "" +
                                currentMSHit.MSHits_pvalue));

                        downstreamFlankingSequence = currentMSHit.MSHits_pepstart;
                        upstreamFlankingSequence = currentMSHit.MSHits_pepstop;

                        if (upstreamFlankingSequence != null) {
                            cVParams.add(new CvParamImpl("PRIDE:0000065",
                                    "PRIDE", "Upstream flanking sequence",
                                    cVParams.size(), "" +
                                    upstreamFlankingSequence.toUpperCase()));
                        }

                        if (downstreamFlankingSequence != null) {
                            cVParams.add(new CvParamImpl("PRIDE:0000066",
                                    "PRIDE", "Downstream flanking sequence",
                                    cVParams.size(), "" +
                                    downstreamFlankingSequence.toUpperCase()));
                        }

                        peptideSequence = currentMSHit.MSHits_pepstring;

                        // OMSSA question: how to handle protein isoforms?
                        // Currently handled by simply selection the first peptide hit (in the xml file)
                        currentMSPepHit = currentMSHit.MSHits_pephits.MSPepHit.get(0);

                        //String accession = currentMSHit.MSHits_libaccession;
                        if (currentMSPepHit.MSPepHit_accession != null) {
                            accession = currentMSPepHit.MSPepHit_accession;
                        } else {
                            accession = "gi|" + currentMSPepHit.MSPepHit_gi;
                        }

                        if (omxFile.getParserResult().MSSearch_request.MSRequest.get(0).MSRequest_settings.MSSearchSettings.MSSearchSettings_db !=
                                null) {
                            database =
                                    omxFile.getParserResult().MSSearch_request.MSRequest.get(0).MSRequest_settings.MSSearchSettings.MSSearchSettings_db;
                        }

                        start = currentMSPepHit.MSPepHit_start + 1;

                        // handle the modifications
                        // OMSSA question: more than one MSRequest??

                        // fixed modifications
                        if (fixedModifications.size() > 0) {

                            peptideModifications = new ArrayList();

                            for (int i = 0; i < fixedModifications.size(); i++) {
                                modifiedResidues =
                                        omssaModificationDetails.get(fixedModifications.get(i)).
                                        getModResidues();

                                for (int j = 0; j < modifiedResidues.size(); j++) {

                                    int index = peptideSequence.indexOf(modifiedResidues.get(j));

                                    while (index != -1) {

                                        CvParamImpl tempCvParam =
                                                (CvParamImpl) userProperties.getCVTermMappings().get(
                                                omssaModificationDetails.get(fixedModifications.get(i)).getModName());

                                        modificationCVParams = new ArrayList();
                                        modificationCVParams.add(tempCvParam);

                                        monoMasses = new ArrayList();
                                        // get the modification mass (DiffMono) retrieved from PSI-MOD
                                        if (tempCvParam.getValue() != null) {
                                            monoMasses.add(new MonoMassDeltaImpl(
                                                    new Double(tempCvParam.getValue()).doubleValue()));
                                        } else {
                                            // if the DiffMono is not found for the PSI-MOD the mass 
                                            // from the file is used
                                            monoMasses.add(
                                                    new MonoMassDeltaImpl(omssaModificationDetails.get(
                                                    fixedModifications.get(i)).getModMonoMass()));
                                        //monoMasses = null;
                                        }

                                        peptideModifications.add(new ModificationImpl(
                                                tempCvParam.getAccession(),
                                                index + 1,
                                                tempCvParam.getCVLookup(),
                                                null,
                                                monoMasses,
                                                null,
                                                modificationCVParams,
                                                null));

                                        index = peptideSequence.indexOf(modifiedResidues.get(j), index + 1);
                                    }
                                }
                            }

                            if (peptideModifications.size() == 0) {
                                peptideModifications = null;
                            }
                        }

                        // variable modifications
                        modsIterator = currentMSHit.MSHits_mods.MSModHit.iterator();

                        if (peptideModifications == null) {
                            if (currentMSHit.MSHits_mods.MSModHit.size() > 0) {
                                peptideModifications = new ArrayList();
                            }
                        }

                        while (modsIterator.hasNext()) {

                            currentMSModHit = modsIterator.next();

                            modType = currentMSModHit.MSModHit_modtype.MSMod;
                            modSite = currentMSModHit.MSModHit_site;

                            modificationCVParams = new ArrayList();
                            modificationCVParams.add(userProperties.getCVTermMappings().get(
                                    omssaModificationDetails.get(modType).getModName()));

                            monoMasses = new ArrayList();
                            monoMasses.add(
                                    new MonoMassDeltaImpl(omssaModificationDetails.get(modType).getModMonoMass()));

                            peptideModifications.add(new ModificationImpl(
                                    ((CvParamImpl) userProperties.getCVTermMappings().get(
                                    omssaModificationDetails.get(modType).getModName())).getAccession(),
                                    modSite + 1,
                                    ((CvParamImpl) userProperties.getCVTermMappings().get(
                                    omssaModificationDetails.get(modType).getModName())).getCVLookup(),
                                    null,
                                    monoMasses,
                                    null,
                                    modificationCVParams,
                                    null));
                        }

                        //calculate itraq values
                        iTRAQ iTRAQValues = null;

                        if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
                            iTRAQValues = new iTRAQ(arrays,
                                    tempSpectrum.MSSpectrum_precursormz / omssaResponseScale,
                                    tempSpectrum.MSSpectrum_charge.MSSpectrum_charge_E.get(0),
                                    userProperties.getPeakIntegrationRangeLower(),
                                    userProperties.getPeakIntegrationRangeUpper(),
                                    userProperties.getReporterIonIntensityThreshold(),
                                    userProperties.getPurityCorrections());
                            iTRAQValues.calculateiTRAQValues();
                        }

                        String[] iTraqNorm = null;
                        String[] iTraqUT = null;
                        String[][] iTraqRatio = null;

                        if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
                            iTraqNorm = (String[]) iTRAQValues.getAllNorms().get(0);
                            iTraqUT = (String[]) iTRAQValues.getAllUTs().get(0);
                            iTraqRatio = (String[][]) iTRAQValues.getAllRatios().get(0);
                        }

                        if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
                            cVParams = addItraqCVTerms(cVParams, iTraqNorm);
                            userParams = addItraqUserTerms(iTraqRatio);
                        } else {
                            userParams = null;
                        }

                        if (currentMSHit.MSHits_pvalue >= properties.getPeptideScoreThreshold()) {

                            ids.add(new IdentificationGeneral(
                                    fileName, //spectrum file name
                                    accession, //spectrum accession
                                    "OMSSA", //search engine
                                    database, //database
                                    null, //database version
                                    peptideSequence.toUpperCase(), //sequence
                                    start, //start
                                    currentMSHit.MSHits_pvalue, //score
                                    null, //threshold
                                    iTraqNorm, iTraqUT, iTraqRatio,
                                    cVParams, userParams, peptideModifications));
                        }
                    } else {
                        spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Not identified"));
                    }

                    if (arrays[properties.MZ_ARRAY].length > 0) {
                        mzRangeStart = new Double(arrays[properties.MZ_ARRAY][0]);
                        mzRangeStop = new Double(
                                arrays[properties.MZ_ARRAY][arrays[properties.MZ_ARRAY].length - 1]);


                        // Create new mzData spectrum for the fragmentation spectrum.
                        fragmentation = new SpectrumImpl(
                                new BinaryArrayImpl(arrays[properties.INTENSITIES_ARRAY],
                                BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                mzRangeStart,
                                new BinaryArrayImpl(arrays[properties.MZ_ARRAY],
                                BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                2l,
                                null,
                                mzRangeStop,
                                null,
                                idCounter, precursors, spectrumDescriptionComments,
                                null, null, null, null);

                        // Store (spectrumfileid, spectrumid) mapping.
                        //mapping.put(new Long(idCounter), new Long(idCounter++));

                        mapping.put(fileName, new Long(idCounter));
                        idCounter++;

                        // Store the transformed spectrum.
                        aTransformedSpectra.add(fragmentation);
                    }
                }
            }
        }

        totalNumberOfSpectra = idCounter - 1;
        return mapping;
    }

    /**
     * This method transforms spectra from ms_lims, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aSpectra   Collection with the Spectrumfile instances to to transform.
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     */
    private static HashMap transformSpectraFrom_ms_lims(
            ArrayList aTransformedSpectra) throws IOException {

        HashMap mapping = new HashMap();

        //Get the selected spectra
        ArrayList selectedSpectra = new ArrayList();
        selectedSpectra = getSelectedSpectraFromDatabase();

        int idCounter = 0;
        Iterator iter = selectedSpectra.iterator();
        Spectrumfile dbSpectrum;

        String filename;
        String file;

        MascotGenericFile lSpectrumFile;

        double[][] arrays;
        Collection precursors;
        Collection ionSelection;
        int charge;
        double intensity;
        Collection spectrumDescriptionComments;

        long identificationCount;
        String identified;

        Spectrum fragmentation;
        Double mzRangeStart;
        Double mzRangeStop;

        TreeSet treeSet = new TreeSet();

        progressDialog.setIntermidiate(false);
        progressDialog.setValue(0);
        progressDialog.setMax(selectedSpectra.size());
        int progressCounter = 0;

        while (iter.hasNext() && !cancelConversion) {
            dbSpectrum = (Spectrumfile) iter.next();
            filename = dbSpectrum.getFilename();

            progressDialog.setValue(progressCounter++);
            progressDialog.setString(filename + " (" + (progressCounter) + "/" + selectedSpectra.size() + ")");

            file = new String(dbSpectrum.getUnzippedFile());
            lSpectrumFile = new MascotGenericFile(filename, file);

            // Transform peaklist into double arrays.
            arrays = transformPeakListToArrays_ms_lims(lSpectrumFile.getPeaks(), treeSet);

            // Precursor collection.
            precursors = new ArrayList(1);

            // Ion selection parameters.
            ionSelection = new ArrayList(4);

            // See if we know the precursor charge, and if so, include it.
            charge = lSpectrumFile.getCharge();

            if (charge > 0) {
                ionSelection.add(new CvParamImpl("PSI:1000041", "PSI",
                        "ChargeState", 0, Integer.toString(charge)));
            }

            // See if we know the precursor intensity
            intensity = lSpectrumFile.getIntensity();
            if (intensity > 1) {
                ionSelection.add(new CvParamImpl("PSI:1000042", "PSI",
                        "Intensity", 1, Double.toString(intensity)));
            }

            ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                    "MassToChargeRatio", 2,
                    Double.toString(lSpectrumFile.getPrecursorMZ())));

            precursors.add(new PrecursorImpl(null, null, ionSelection, null, 1, 0, 0));
            idCounter++;

            // Spectrum description comments.
            spectrumDescriptionComments = new ArrayList(1);
            identificationCount = dbSpectrum.getIdentified();
            identified = (identificationCount > 0 ? "Identified" : "Not identified");
            spectrumDescriptionComments.add(new SpectrumDescCommentImpl(identified));

            if (arrays[properties.MZ_ARRAY].length > 0) {
                mzRangeStart = new Double(arrays[properties.MZ_ARRAY][0]);
                mzRangeStop = new Double(
                        arrays[properties.MZ_ARRAY][arrays[properties.MZ_ARRAY].length - 1]);

                fragmentation =
                        new SpectrumImpl(
                        new BinaryArrayImpl(arrays[properties.INTENSITIES_ARRAY],
                        BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                        mzRangeStart,
                        new BinaryArrayImpl(arrays[properties.MZ_ARRAY],
                        BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                        2l,
                        null,
                        mzRangeStop,
                        null,
                        idCounter, precursors, spectrumDescriptionComments,
                        null, null, null, null);

                // Store (spectrumfileid, spectrumid) mapping.
                mapping.put("" + dbSpectrum.getSpectrumfileid(),
                        new Long(idCounter));

                // Store the transformed spectrum.
                aTransformedSpectra.add(fragmentation);
                idCounter++;
            }
        }

        // peptide identifications
        ids = new ArrayList<IdentificationGeneral>();
        Identification tempIdentification;

        boolean calculateITraq = false;
        iTRAQ iTRAQValues = null;

        if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
            calculateITraq = true;
        }

        ArrayList cVParams, userParams, peptideModifications, modificationCVParams;
        progressDialog.setString(null);
        progressDialog.setIntermidiate(false);
        progressDialog.setValue(0);
        progressDialog.setMax(selectedSpectra.size());
        progressDialog.setTitle("Getting Spectra IDs. Please wait.");

        for (int i = 0; i < selectedSpectra.size() && !cancelConversion; i++) {

            try {
                progressDialog.setValue(i);
                tempIdentification = Identification.getIdentification(conn,
                        ((Spectrumfile) selectedSpectra.get(i)).getFilename());

                if (tempIdentification != null) {

                    if (tempIdentification.getScore() >= properties.getPeptideScoreThreshold()) {

                        cVParams = new ArrayList(1);
                        cVParams.add(new CvParamImpl("PRIDE:0000069", "PRIDE", "Mascot Score", 0, "" +
                                tempIdentification.getScore()));


                        // iTraq calculations
                        if (calculateITraq) {
                            iTRAQValues = new iTRAQ(
                                    ((BinaryArrayImpl) ((Spectrum) aTransformedSpectra.get(
                                    i)).getMzArrayBinary()).getDoubleArray(),
                                    ((BinaryArrayImpl) ((Spectrum) aTransformedSpectra.get(
                                    i)).getIntenArrayBinary()).getDoubleArray(),
                                    tempIdentification.getExp_mass().doubleValue(),
                                    tempIdentification.getCharge(),
                                    userProperties.getPeakIntegrationRangeLower(),
                                    userProperties.getPeakIntegrationRangeUpper(),
                                    userProperties.getReporterIonIntensityThreshold(),
                                    userProperties.getPurityCorrections());
                            iTRAQValues.calculateiTRAQValues();

                            cVParams = addItraqCVTerms(cVParams, iTRAQValues);
                            userParams = addItraqUserTerms(iTRAQValues);
                        } else {
                            userParams = null;
                        }

                        // modifications
                        String modifiedSequence = tempIdentification.getModified_sequence();

                        String nTerm = modifiedSequence.substring(0, modifiedSequence.indexOf("-"));
                        String cTerm = modifiedSequence.substring(modifiedSequence.lastIndexOf("-") + 1);

                        String peptideSequence = modifiedSequence.substring(modifiedSequence.indexOf("-") +
                                1, modifiedSequence.lastIndexOf("-"));

                        String modificationName;
                        peptideModifications = null;

                        if (!nTerm.equalsIgnoreCase("NH2")) {

                            StringTokenizer tok = new StringTokenizer(nTerm, ",");

                            while (tok.hasMoreTokens() && !cancelConversion) {

                                modificationName = tok.nextToken() + " (N-terminal)";

                                if (!properties.getAlreadyChoosenModifications().
                                        contains(modificationName)) {
                                    new ModificationMapping(outputFrame,
                                            true, progressDialog, modificationName,
                                            "N-terminal", null,
                                            (CvParamImpl) userProperties.getCVTermMappings().get(modificationName));
                                    properties.getAlreadyChoosenModifications().add(modificationName);
                                } else {
                                    //do nothing, mapping already choosen
                                }

                                if (peptideModifications == null) {
                                    peptideModifications = new ArrayList();
                                }

                                modificationCVParams = new ArrayList();
                                modificationCVParams.add(userProperties.getCVTermMappings().
                                        get(modificationName));

                                peptideModifications.add(new ModificationImpl(
                                        ((CvParamImpl) userProperties.getCVTermMappings().
                                        get(modificationName)).getAccession(),
                                        0,
                                        ((CvParamImpl) userProperties.getCVTermMappings().
                                        get(modificationName)).getCVLookup(),
                                        null,
                                        null,
                                        null,
                                        modificationCVParams,
                                        null));
                            }
                        }

                        if (!cTerm.equalsIgnoreCase("COOH")) {
                            StringTokenizer tok = new StringTokenizer(cTerm, ",");

                            while (tok.hasMoreTokens() && !cancelConversion) {

                                modificationName = tok.nextToken() + " (C-terminal)";

                                if (!properties.getAlreadyChoosenModifications().
                                        contains(modificationName)) {
                                    new ModificationMapping(outputFrame,
                                            true, progressDialog, modificationName,
                                            "C-terminal", null,
                                            (CvParamImpl) userProperties.getCVTermMappings().
                                            get(modificationName));
                                    properties.getAlreadyChoosenModifications().
                                            add(modificationName);
                                } else {
                                    //do nothing, mapping already choosen
                                }

                                if (peptideModifications == null) {
                                    peptideModifications = new ArrayList();
                                }

                                modificationCVParams = new ArrayList();
                                modificationCVParams.add(userProperties.getCVTermMappings().
                                        get(modificationName));

                                peptideModifications.add(new ModificationImpl(
                                        ((CvParamImpl) userProperties.getCVTermMappings().
                                        get(modificationName)).getAccession(),
                                        peptideSequence.length() + 1,
                                        ((CvParamImpl) userProperties.getCVTermMappings().
                                        get(modificationName)).getCVLookup(),
                                        null,
                                        null,
                                        null,
                                        modificationCVParams,
                                        null));
                            }
                        }

                        String modificationPattern = "[<][^<]*[>]";

                        Pattern pattern = Pattern.compile(modificationPattern);
                        Matcher matcher = pattern.matcher(peptideSequence);

                        int index = 0;
                        int modificationTagLengthSum = 0;

                        while (matcher.find() && !cancelConversion) {

                            String internalModification = matcher.group();

                            //remove '<' and '>'
                            internalModification = internalModification.substring(1,
                                    internalModification.length() - 1);

                            index = matcher.start() - modificationTagLengthSum;

                            StringTokenizer tok = new StringTokenizer(internalModification, ",");

                            while (tok.hasMoreTokens() && !cancelConversion) {

                                modificationName = tok.nextToken();

                                if (!properties.getAlreadyChoosenModifications().
                                        contains(modificationName)) {
                                    new ModificationMapping(outputFrame,
                                            true, progressDialog, modificationName, "-", null,
                                            (CvParamImpl) userProperties.getCVTermMappings().
                                            get(modificationName));
                                    properties.getAlreadyChoosenModifications().add(modificationName);
                                } else {
                                    //do nothing, mapping already choosen
                                }

                                if (peptideModifications == null) {
                                    peptideModifications = new ArrayList();
                                }

                                modificationCVParams = new ArrayList();
                                modificationCVParams.add(userProperties.getCVTermMappings().
                                        get(modificationName));

                                peptideModifications.add(new ModificationImpl(
                                        ((CvParamImpl) userProperties.getCVTermMappings().
                                        get(modificationName)).getAccession(),
                                        index,
                                        ((CvParamImpl) userProperties.getCVTermMappings().
                                        get(modificationName)).getCVLookup(),
                                        null,
                                        null,
                                        null,
                                        modificationCVParams,
                                        null));
                            }

                            modificationTagLengthSum += internalModification.length() + 2;
                        }

                        ids.add(new IdentificationGeneral(
                                "" + tempIdentification.getL_spectrumfileid(),
                                tempIdentification.getAccession(),
                                "Mascot " + tempIdentification.getMascot_version(),
                                tempIdentification.getDb(),
                                null,
                                tempIdentification.getSequence(),
                                new Integer(new Long(tempIdentification.getStart()).intValue()),
                                new Double(tempIdentification.getScore()),
                                new Double((1 - tempIdentification.getConfidence().doubleValue()) * 100),
                                null,
                                null,
                                null,
                                cVParams, userParams,
                                peptideModifications));

                        //assumed to be the same for all identifications in the selected project
                        properties.setMascotConfidenceLevel((int) ((1 -
                                tempIdentification.getConfidence().doubleValue()) * 100));
                    }
                }

            } catch (java.sql.SQLException e) {
                JOptionPane.showMessageDialog(null, "An error occured when accessing the database.\n" +
                        "See ../Properties/ErrorLog.txt for more details.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                Util.writeToErrorLog("An error occured when accessing the database: ");
                e.printStackTrace();
            }
        }

        return mapping;
    }

    /**
     * This method takes a HashMap comprising a peaklist (mapping: (m/z, intensity))
     * and transforms it into two double arrays - one with the m/z values, the other with
     * the corresponding intensities (the two double arrays are indexed in the first
     * dimension of the result by MZ_ARRAY and INTENSITIES_ARRAY). Please note that the
     * arrays will be sorted by m/z.
     *
     * @param aPeakList HashMap with the (m/z, intensity) mapping.
     * @return  double[][] with two double arrays, one (indexed in the first dimension
     *                     by the MZ_DATA constant defined on this class) comprising the
     *                     m/z values, the other (indexed in the first dimension by the
     *                     INTENSITIES_DATA constant defined on this class) holding the
     *                     corresponding intensities. Note that the arrays will be sorted
     *                     by m/z.
     */
    private static double[][] transformPeakListToArrays_ms_lims(HashMap aPeakList,
            TreeSet treeSet) {
        double[][] result = new double[2][aPeakList.size()];

        // Use a TreeSet to sort the keys (m/z values).
        treeSet.clear();
        treeSet.addAll(aPeakList.keySet());

        Iterator treeSetIterator = treeSet.iterator();

        int counter = 0;

        Double mz, intensity;

        while (treeSetIterator.hasNext()) {
            // Extract m/z and corresponding intensity.
            mz = (Double) treeSetIterator.next();
            intensity = (Double) aPeakList.get(mz);

            // Store the m/z and intensity.
            result[properties.MZ_ARRAY][counter] = mz.doubleValue();
            result[properties.INTENSITIES_ARRAY][counter] = intensity.doubleValue();
            // Increment counter.
            counter++;
        }

        return result;
    }

    /**
     * Debug variable. Used to run the PRIDE Converter within Netbeans.
     * 
     * @return true if hardcoded paths should be used.
     */
    public boolean useHardcodedPaths() {
        return useHardcodedPaths;
    }

    /**
     * Class that helps in the conversion of identifications to PRIDE GelFreeIdentification.
     */
    private static class InnerID {

        private String iAccession = null;
        private String iAccessionVersion = null;
        private ArrayList iPeptides = new ArrayList();
        private String iSearchEngine = null;
        private String iDatabase = null;
        private String iDBVersion = null;
        private ArrayList iScores = new ArrayList();
        private ArrayList iThresholds = new ArrayList();

        /**
         * Constructor that the takes the accessionnumber for the future GelFreeIdentification and
         * the accession version (if any) as well as the search engine that performed the identification and
         * the database name and version for the search.
         *
         * @param aAccession    String with the accession number for the identification.
         * @param aAccessionVersion String with the version for the accession number (can be 'null').
         * @param aSearchEngine String with the search engine used for the identification.
         * @param aDatabase String with the database used for the identification.
         * @param aDBVersion    String with the version (or release date) of the database used for the identification.
         */
        public InnerID(String aAccession, String aAccessionVersion,
                String aSearchEngine, String aDatabase, String aDBVersion) {
            this.iAccession = aAccession;
            this.iAccessionVersion = aAccessionVersion;
            this.iSearchEngine = aSearchEngine;
            this.iDatabase = aDatabase;
            this.iDBVersion = aDBVersion;
        }

        /**
         * This method adds a peptide identification to the protein identification.
         *
         * @param aSpectrumRef  Long with the reference to the relevant mzData spectrum.
         * @param aSequence String with the sequence.
         * @param aStart    Integer with the start position of the peptide.
         * @param aScore    Double with the score for the peptide.
         * @param aThreshold    Double with the threshold for the peptide.
         */
        public void addPeptide(Long aSpectrumRef, String aSequence,
                Integer aStart, Double aScore, Double aThreshold,
                Collection modifications, Collection cVParams,
                Collection aUserParams) {
            //iPeptides.add(new PeptideImpl(aSpectrumRef, aSequence, aStart, cVParams, aUserParams));
            iPeptides.add(new PeptideImpl(aSpectrumRef, aSequence, aStart,
                    modifications, cVParams, aUserParams));
            iScores.add(aScore);
            iThresholds.add(aThreshold);
        }

        /**
         * This method calculates the average score.
         *
         * @return  Double with the average score.
         */
        public Double getScore() {
            Iterator iter = iScores.iterator();
            double temp = 0.0;
            int counter = 0;
            while (iter.hasNext()) {
                temp += ((Double) iter.next()).doubleValue();
                counter++;
            }

            if (!properties.getDataSource().equalsIgnoreCase("Spectrum Mill")) {
                temp /= counter;
            }

            BigDecimal bd = new BigDecimal(temp).setScale(2,
                    BigDecimal.ROUND_HALF_UP);

            return new Double(bd.doubleValue());
        }

        /**
         * This method calculates the average threshold.
         *
         * @return  Double with the average threshold.
         */
        public Double getThreshold() {

            Iterator iter = iThresholds.iterator();
            double temp = 0.0;
            int counter = 0;
            while (iter.hasNext()) {
                temp += ((Double) iter.next()).doubleValue();
                counter++;
            }

            temp /= counter;
            BigDecimal bd = new BigDecimal(temp).setScale(2,
                    BigDecimal.ROUND_HALF_UP);

            return new Double(bd.doubleValue());
        }

        /**
         * This method returns a PRIDE GelFreeIdentification based upon this instance.
         *
         * @return  GelFreeIdentification   with the PRIDE GelFreeIdentification object.
         */
        public uk.ac.ebi.pride.model.interfaces.core.Identification getGelFreeIdentification() {
            Collection userParams;

            if (properties.getDataSource().equalsIgnoreCase("Mascot Dat File")) {

                userParams = new ArrayList(1);
//                userParams.add(new UserParamImpl("ScoreCalculation", 0, "MeanOfPeptideScores"));
//                userParams.add(new UserParamImpl("ThresholdCalculation", 1, "MeanOfPeptideIdentityThresholds"));
//                userParams.add(new UserParamImpl("PeptideIdentification", 2, "AboveOrEqualToIdentityThreshold"));
                userParams.add(new UserParamImpl("MascotConfidenceLevel", 0, "" +
                        properties.getMascotConfidenceLevel()));
            } else {
                userParams = null;
            }

            Double threshold;

            if (properties.getDataSource().equalsIgnoreCase("X!Tandem") ||
                    properties.getDataSource().equalsIgnoreCase("Spectrum Mill") ||
                    properties.getDataSource().equalsIgnoreCase("Sequest Result File") ||
                    properties.getDataSource().equalsIgnoreCase("OMSSA")) {
                threshold = null;
            } else {
                threshold = this.getThreshold();
            }

            Double score;

            if (properties.getDataSource().equalsIgnoreCase("Sequest Result File") ||
                    properties.getDataSource().equalsIgnoreCase("OMSSA")) {
                score = null;
            } else {
                score = this.getScore();
            }

            uk.ac.ebi.pride.model.interfaces.core.Identification PRIDE_protein;

            if (properties.isGelFree()) {

                PRIDE_protein = new GelFreeIdentificationImpl(
                        iAccession, //protein accession
                        iAccessionVersion, // accession version
                        null, // spliceforms
                        iDatabase, // database
                        iPeptides, // the peptides
                        null, // cv params
                        userParams, // user params
                        iSearchEngine, // search engine
                        iDBVersion, // database version
                        null, // sequence coverage
                        score, // score
                        threshold, // threshold
                        null); // spectrum reference

            } else {
                PRIDE_protein = new TwoDimensionalIdentificationImpl(
                        iAccession, //protein accession
                        iAccessionVersion, // accession version
                        null, // spliceforms
                        iDatabase, // database
                        iDBVersion, // database version
                        iPeptides, // the peptides
                        null, // cv params
                        userParams, // user params
                        null, // PI
                        null, // MW
                        null, // sequence coverage
                        null, // the gel
                        null, // x coordinate
                        null, // y Coordinate
                        null, // spectrum reference
                        iSearchEngine, // search engine
                        score, // score
                        threshold); // threshold
            }

            return PRIDE_protein;
        }
    }

    /**
     * Returns an ArrayList of the selected spectra in the ms_lims database.
     * 
     * @return the selected spectra
     */
    private static ArrayList getSelectedSpectraFromDatabase() {

        ArrayList selectedSpectra = new ArrayList();
        ArrayList selectedFileNames = new ArrayList();

        try {
            if (properties.getSelectedSpectraNames().size() > 0) {

                progressDialog.setMax(properties.getSelectedSpectraNames().size());

                for (int i = 0; i < properties.getSelectedSpectraNames().size(); i++) {

                    if (cancelConversion) {
                        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                        outputFrame.setConvertButtonEnabled(true);
                        return null;
                    }

                    progressDialog.setValue(i);
                    selectedSpectra.add(Spectrumfile.getFromName(
                            (String) ((Object[]) properties.getSelectedSpectraNames().get(i))[0], conn));
                }
            } else {

                Spectrumfile[] dbSpectra;

                if (properties.getSpectraSelectionCriteria() != null) {

                    StringTokenizer tok;

                    if (userProperties.getCurrentFileNameSelectionCriteriaSeparator().length() == 0) {
                        tok = new StringTokenizer(properties.getSpectraSelectionCriteria());
                    } else {
                        tok = new StringTokenizer(properties.getSpectraSelectionCriteria(),
                                userProperties.getCurrentFileNameSelectionCriteriaSeparator());
                    }

                    String tempToken;

                    while (tok.hasMoreTokens()) {

                        tempToken = tok.nextToken();
                        tempToken = tempToken.trim();

                        for (int i = 0; i < properties.getProjectIds().size(); i++) {

                            if (properties.isSelectionCriteriaFileName()) {

                                dbSpectra = Spectrumfile.getAllSpectraForProject(properties.getProjectIds().
                                        get(i), conn,
                                        "filename like '%" +
                                        tempToken + "%'");
                            } else {
                                Identification[] tempIdentifications = Identification.getAllIdentificationsforProject(
                                        conn, properties.getProjectIds().
                                        get(i),
                                        "identificationid = '" +
                                        tempToken + "'");

                                if (tempIdentifications.length > 0) {
                                    dbSpectra =
                                            Spectrumfile.getAllSpectraForProject(properties.getProjectIds().
                                            get(i), conn,
                                            "spectrumfileid = " +
                                            tempIdentifications[0].getL_spectrumfileid());
                                } else {
                                    dbSpectra = null;
                                }
                            }

                            if (dbSpectra != null) {

                                progressDialog.setMax(dbSpectra.length);

                                for (int j = 0; j < dbSpectra.length; j++) {
                                    if (cancelConversion) {
                                        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                                        outputFrame.setConvertButtonEnabled(true);
                                        return null;
                                    }

                                    progressDialog.setValue(j);

                                    if (!selectedFileNames.contains(dbSpectra[j].getFilename())) {
                                        selectedSpectra.add(dbSpectra[j]);
                                        selectedFileNames.add(dbSpectra[j].getFilename());
                                    } else {
                                        //System.out.println("Discarded: " + dbSpectra[i].getFilename());
                                    }
                                }
                            }
                        }
                    }
                } else {

                    for (int i = 0; i < properties.getProjectIds().size(); i++) {

                        if (properties.selectAllSpectra()) {
                            dbSpectra = Spectrumfile.getAllSpectraForProject(properties.getProjectIds().
                                    get(i), conn);
                        } else { //selectAllIdentifiedSpectra
                            dbSpectra = Spectrumfile.getAllSpectraForProject(properties.getProjectIds().
                                    get(i), conn,
                                    "identified > 0");
                        }

                        progressDialog.setMax(dbSpectra.length);

                        for (int j = 0; j < dbSpectra.length; j++) {

                            if (cancelConversion) {
                                outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                                outputFrame.setConvertButtonEnabled(true);
                                return null;
                            }

                            progressDialog.setValue(j);
                            selectedSpectra.add(dbSpectra[j]);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            setCancelConversion(true);
            JOptionPane.showMessageDialog(outputFrame,
                    "An error occured when extracting data from the ms_lims database.\n" +
                    "The conversion has been canceled.\n" +
                    "See ../Properties/ErrorLog.txt for more details.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error accessing database: " + e.toString());
            selectedSpectra = null;
        }

        return selectedSpectra;
    }

    /**
     * Updates the annotation (contacts, instrument details etc) with the
     * values chosen by the user, combines all the spectra from the selected
     * mzData files into one mzData object and returns this object.
     *
     * @return the mzData object
     */
    private MzData transformSpectraFromMzDataFiles() {

        // The CV lookup stuff. (NB: currently hardcoded to PSI only)
        Collection cvLookups = new ArrayList(1);
        cvLookups.add(new CVLookupImpl(properties.getCvVersion(),
                properties.getCvFullName(),
                properties.getCvLabel(),
                properties.getCvAddress()));

        // Instrument source CV parameters.
        Collection instrumentSourceCVParameters = new ArrayList(3);
        instrumentSourceCVParameters.add(new CvParamImpl(
                properties.getAccessionInstrumentSourceParameter(),
                properties.getCVLookupInstrumentSourceParameter(),
                properties.getNameInstrumentSourceParameter(),
                0,
                properties.getValueInstrumentSourceParameter()));

        // Instrument detector parameters.
        Collection instrumentDetectorParamaters = new ArrayList(3);
        instrumentDetectorParamaters.add(new CvParamImpl(
                properties.getAccessionInstrumentDetectorParamater(),
                properties.getCVLookupInstrumentDetectorParamater(),
                properties.getNameInstrumentDetectorParamater(),
                0,
                properties.getValueInstrumentDetectorParamater()));

        ArrayList sampleDescriptionUserParams = new ArrayList(
                properties.getSampleDescriptionUserSubSampleNames().size());

        for (int i = 0; i < properties.getSampleDescriptionUserSubSampleNames().size(); i++) {
            sampleDescriptionUserParams.add(new UserParamImpl(
                    "SUBSAMPLE_" + (i + 1),
                    i, (String) properties.getSampleDescriptionUserSubSampleNames().
                    get(i)));
        }

        for (int i = 0; i < properties.getSampleDescriptionCVParamsQuantification().size(); i++) {
            properties.getSampleDescriptionCVParams().add(
                    properties.getSampleDescriptionCVParamsQuantification().get(i));
        }


        progressDialog.setString(null);
        progressDialog.setIntermidiate(true);

        ArrayList aTransformedSpectra = new ArrayList();

        long spectrumId = 1;

        for (int j = 0; j < properties.getSelectedSourceFiles().size() && !cancelConversion; j++) {

            String filePath = properties.getSelectedSourceFiles().get(j);

            currentFileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);

            progressDialog.setIntermidiate(true);
            progressDialog.setString(currentFileName + " (" + (j + 1) + "/" +
                    properties.getSelectedSourceFiles().size() + ")");

            try {
                MzDataXMLUnmarshaller unmarshallerMzData = new MzDataXMLUnmarshaller();
                MzData currentMzDataFile = unmarshallerMzData.unMarshall(new FileReader(filePath));

                Iterator<Spectrum> spectra = currentMzDataFile.getSpectrumCollection().iterator();

                while (spectra.hasNext()) {

                    Spectrum currentSpectrum = spectra.next();

                    Spectrum updatedSpectrum =
                            new SpectrumImpl(
                            currentSpectrum.getIntenArrayBinary(),
                            currentSpectrum.getMzRangeStart(),
                            currentSpectrum.getMzArrayBinary(),
                            currentSpectrum.getMsLevel(),
                            currentSpectrum.getSupDataArrayBinaryCollection(),
                            currentSpectrum.getMzRangeStop(),
                            currentSpectrum.getAcqSpecification(),
                            spectrumId++,
                            currentSpectrum.getPrecursorCollection(),
                            currentSpectrum.getSpectrumDescCommentCollection(),
                            currentSpectrum.getSpectrumInstrumentCvParameters(),
                            currentSpectrum.getSpectrumInstrumentUserParameters(),
                            currentSpectrum.getSupDataArrayCollection(),
                            currentSpectrum.getSupDescCollection());

                    aTransformedSpectra.add(updatedSpectrum);
                }
            } catch (FileNotFoundException e) {

                Util.writeToErrorLog("An error occured while trying to parse: " + filePath);
                e.printStackTrace();

                JOptionPane.showMessageDialog(null,
                        "An error occured while trying to parse: " + filePath + "\n\n" +
                        "See ../Properties/ErrorLog.txt for more details.\n" +
                        "The file can most likely not be converted to PRIDE XML.",
                        "Parsing Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {

                Util.writeToErrorLog("An error occured while trying to parse: " +
                        filePath);
                e.printStackTrace();

                JOptionPane.showMessageDialog(null,
                        "An error occured while trying to parse: " + filePath + "\n\n" +
                        "See ../Properties/ErrorLog.txt for more details.\n" +
                        "The file can most likely not be converted to PRIDE XML.",
                        "Parsing Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        // Create the combinded mzData object.
        MzData mzData = new MzDataImpl(
                aTransformedSpectra,
                properties.getSoftwareCompletionTime(),
                properties.getContacts(),
                properties.getInstrumentName(),
                properties.getProcessingMethod(),
                properties.getProcessingMethodUserParams(),
                properties.getSourceFile(),
                userProperties.getCurrentSampleSet(),
                properties.getInstrumentAdditionalCvParams(),
                properties.getInstrumentAdditionalUserParams(),
                cvLookups,
                properties.getMzDataVersion(),
                instrumentDetectorParamaters,
                properties.getInstrumentDetectorUserParams(),
                properties.getSampleDescriptionComment(),
                properties.getMzDataAccessionNumber(),
                properties.getAnalyzerList(),
                properties.getSoftwareComments(),
                instrumentSourceCVParameters,
                properties.getInstrumentSourceUserParams(),
                properties.getSoftwareVersion(),
                properties.getSampleDescriptionCVParams(),
                sampleDescriptionUserParams,
                properties.getSoftwareName());

        totalNumberOfSpectra = aTransformedSpectra.size();

        return mzData;
    }

    /**
     * Builds and returns the mzData object.
     * 
     * @param mzDataSpectra
     * @return the mzData object
     */
    private MzData createMzData(ArrayList mzDataSpectra) {

        // The CV lookup stuff. (NB: currently hardcoded to PSI only)
        Collection cvLookups = new ArrayList(1);
        cvLookups.add(new CVLookupImpl(properties.getCvVersion(),
                properties.getCvFullName(),
                properties.getCvLabel(),
                properties.getCvAddress()));

        // Instrument source CV parameters.
        Collection instrumentSourceCVParameters = new ArrayList(3);
        instrumentSourceCVParameters.add(new CvParamImpl(
                properties.getAccessionInstrumentSourceParameter(),
                properties.getCVLookupInstrumentSourceParameter(),
                properties.getNameInstrumentSourceParameter(),
                0,
                properties.getValueInstrumentSourceParameter()));

        // Instrument detector parameters.
        Collection instrumentDetectorParamaters = new ArrayList(3);
        instrumentDetectorParamaters.add(new CvParamImpl(
                properties.getAccessionInstrumentDetectorParamater(),
                properties.getCVLookupInstrumentDetectorParamater(),
                properties.getNameInstrumentDetectorParamater(),
                0,
                properties.getValueInstrumentDetectorParamater()));

        ArrayList sampleDescriptionUserParams = new ArrayList(
                properties.getSampleDescriptionUserSubSampleNames().size());

        for (int i = 0; i < properties.getSampleDescriptionUserSubSampleNames().size(); i++) {
            sampleDescriptionUserParams.add(new UserParamImpl(
                    "SUBSAMPLE_" + (i + 1),
                    i, (String) properties.getSampleDescriptionUserSubSampleNames().
                    get(i)));
        }

        for (int i = 0; i < properties.getSampleDescriptionCVParamsQuantification().size(); i++) {
            properties.getSampleDescriptionCVParams().add(
                    properties.getSampleDescriptionCVParamsQuantification().get(i));
        }

        // Create the mzData object.
        MzData mzData = new MzDataImpl(
                mzDataSpectra,
                properties.getSoftwareCompletionTime(),
                properties.getContacts(),
                properties.getInstrumentName(),
                properties.getProcessingMethod(),
                properties.getProcessingMethodUserParams(),
                properties.getSourceFile(),
                userProperties.getCurrentSampleSet(),
                properties.getInstrumentAdditionalCvParams(),
                properties.getInstrumentAdditionalUserParams(),
                cvLookups,
                properties.getMzDataVersion(),
                instrumentDetectorParamaters,
                properties.getInstrumentDetectorUserParams(),
                properties.getSampleDescriptionComment(),
                properties.getMzDataAccessionNumber(),
                properties.getAnalyzerList(),
                properties.getSoftwareComments(),
                instrumentSourceCVParameters,
                properties.getInstrumentSourceUserParams(),
                properties.getSoftwareVersion(),
                properties.getSampleDescriptionCVParams(),
                sampleDescriptionUserParams,
                properties.getSoftwareName());

        return mzData;
    }

    /**
     * Updates the annotation (contacts, instrument details etc) with the
     * values chosen by the user and returns the updated mzData object.
     * 
     * @param mzData the mzData object to be updated
     * @return the updated mzData object
     */
    private MzData updateMzData(MzData mzData) {

        // The CV lookup stuff. (NB: currently hardcoded to PSI only)
        Collection cvLookups = new ArrayList(1);
        cvLookups.add(new CVLookupImpl(properties.getCvVersion(),
                properties.getCvFullName(),
                properties.getCvLabel(),
                properties.getCvAddress()));

        // Instrument source CV parameters.
        Collection instrumentSourceCVParameters = new ArrayList(3);
        instrumentSourceCVParameters.add(new CvParamImpl(
                properties.getAccessionInstrumentSourceParameter(),
                properties.getCVLookupInstrumentSourceParameter(),
                properties.getNameInstrumentSourceParameter(),
                0,
                properties.getValueInstrumentSourceParameter()));

        // Instrument detector parameters.
        Collection instrumentDetectorParamaters = new ArrayList(3);
        instrumentDetectorParamaters.add(new CvParamImpl(
                properties.getAccessionInstrumentDetectorParamater(),
                properties.getCVLookupInstrumentDetectorParamater(),
                properties.getNameInstrumentDetectorParamater(),
                0,
                properties.getValueInstrumentDetectorParamater()));

        ArrayList sampleDescriptionUserParams = new ArrayList(
                properties.getSampleDescriptionUserSubSampleNames().size());

        for (int i = 0; i < properties.getSampleDescriptionUserSubSampleNames().size(); i++) {
            sampleDescriptionUserParams.add(new UserParamImpl(
                    "SUBSAMPLE_" + (i + 1),
                    i, (String) properties.getSampleDescriptionUserSubSampleNames().
                    get(i)));
        }

        for (int i = 0; i < properties.getSampleDescriptionCVParamsQuantification().size(); i++) {
            properties.getSampleDescriptionCVParams().add(
                    properties.getSampleDescriptionCVParamsQuantification().get(i));
        }

        // Create the mzData object.
        mzData = new MzDataImpl(
                mzData.getSpectrumCollection(),
                properties.getSoftwareCompletionTime(),
                properties.getContacts(),
                properties.getInstrumentName(),
                properties.getProcessingMethod(),
                properties.getProcessingMethodUserParams(),
                properties.getSourceFile(),
                userProperties.getCurrentSampleSet(),
                properties.getInstrumentAdditionalCvParams(),
                properties.getInstrumentAdditionalUserParams(),
                cvLookups,
                properties.getMzDataVersion(),
                instrumentDetectorParamaters,
                properties.getInstrumentDetectorUserParams(),
                properties.getSampleDescriptionComment(),
                properties.getMzDataAccessionNumber(),
                properties.getAnalyzerList(),
                properties.getSoftwareComments(),
                instrumentSourceCVParameters,
                properties.getInstrumentSourceUserParams(),
                properties.getSoftwareVersion(),
                properties.getSampleDescriptionCVParams(),
                sampleDescriptionUserParams,
                properties.getSoftwareName());

        totalNumberOfSpectra = mzData.getSpectrumCollection().size();

        return mzData;
    }

    /**
     * Adds the correct iTRAQ CV terms to a CV param list.
     * 
     * @param cvParams the CV param list to be extended
     * @param iTraqNorms the values to add
     * @return the updated CV param list
     */
    private static ArrayList addItraqCVTerms(ArrayList cvParams, String[] iTraqNorms) {
        cvParams.add(new CvParamImpl("PRIDE:000018", "PRIDE", "iTRAQ intensity 114", cvParams.size(), iTraqNorms[0]));
        cvParams.add(new CvParamImpl("PRIDE:000019", "PRIDE", "iTRAQ intensity 115", cvParams.size(), iTraqNorms[1]));
        cvParams.add(new CvParamImpl("PRIDE:000020", "PRIDE", "iTRAQ intensity 116", cvParams.size(), iTraqNorms[2]));
        cvParams.add(new CvParamImpl("PRIDE:000021", "PRIDE", "iTRAQ intensity 117", cvParams.size(), iTraqNorms[3]));

        return cvParams;
    }

    /**
     * Adds the correct iTRAQ CV terms to a CV param list.
     * 
     * @param cvParams the CV param list to be extended
     * @param iTRAQValues the values to add
     * @return the updated CV param list
     */
    private static ArrayList addItraqCVTerms(ArrayList cvParams, iTRAQ iTRAQValues) {
        cvParams.add(
                new CvParamImpl(
                "PRIDE:000018", "PRIDE", "iTRAQ intensity 114", cvParams.size(),
                ((String[]) iTRAQValues.getAllNorms().get(0))[0]));
        cvParams.add(
                new CvParamImpl(
                "PRIDE:000019", "PRIDE", "iTRAQ intensity 115", cvParams.size(),
                ((String[]) iTRAQValues.getAllNorms().get(0))[1]));
        cvParams.add(
                new CvParamImpl(
                "PRIDE:000020", "PRIDE", "iTRAQ intensity 116", cvParams.size(),
                ((String[]) iTRAQValues.getAllNorms().get(0))[2]));
        cvParams.add(
                new CvParamImpl(
                "PRIDE:000021", "PRIDE", "iTRAQ intensity 117", cvParams.size(),
                ((String[]) iTRAQValues.getAllNorms().get(0))[3]));

        return cvParams;
    }

    /**
     * Adds the coeect iTraq terms to an arraylist and returns this list.
     * 
     * @param iTRAQValues the values to add
     * @return the updated array list
     */
    private static ArrayList addItraqUserTerms(iTRAQ iTRAQValues) {

        ArrayList userParams = new ArrayList(16);
        userParams.add(new UserParamImpl("114_114", 0, ((String[][]) iTRAQValues.getAllRatios().get(0))[0][0]));
        userParams.add(new UserParamImpl("114_115", 1, ((String[][]) iTRAQValues.getAllRatios().get(0))[0][1]));
        userParams.add(new UserParamImpl("114_116", 2, ((String[][]) iTRAQValues.getAllRatios().get(0))[0][2]));
        userParams.add(new UserParamImpl("114_117", 3, ((String[][]) iTRAQValues.getAllRatios().get(0))[0][3]));
        userParams.add(new UserParamImpl("115_114", 4, ((String[][]) iTRAQValues.getAllRatios().get(0))[1][0]));
        userParams.add(new UserParamImpl("115_115", 5, ((String[][]) iTRAQValues.getAllRatios().get(0))[1][1]));
        userParams.add(new UserParamImpl("115_116", 6, ((String[][]) iTRAQValues.getAllRatios().get(0))[1][2]));
        userParams.add(new UserParamImpl("115_117", 7, ((String[][]) iTRAQValues.getAllRatios().get(0))[1][3]));
        userParams.add(new UserParamImpl("116_114", 8, ((String[][]) iTRAQValues.getAllRatios().get(0))[2][0]));
        userParams.add(new UserParamImpl("116_115", 9, ((String[][]) iTRAQValues.getAllRatios().get(0))[2][1]));
        userParams.add(new UserParamImpl("116_116", 10, ((String[][]) iTRAQValues.getAllRatios().get(0))[2][2]));
        userParams.add(new UserParamImpl("116_117", 11, ((String[][]) iTRAQValues.getAllRatios().get(0))[2][3]));
        userParams.add(new UserParamImpl("117_114", 12, ((String[][]) iTRAQValues.getAllRatios().get(0))[3][0]));
        userParams.add(new UserParamImpl("117_115", 13, ((String[][]) iTRAQValues.getAllRatios().get(0))[3][1]));
        userParams.add(new UserParamImpl("117_116", 14, ((String[][]) iTRAQValues.getAllRatios().get(0))[3][2]));
        userParams.add(new UserParamImpl("117_117", 15, ((String[][]) iTRAQValues.getAllRatios().get(0))[3][3]));

        return userParams;
    }

    /**
     * Adds the coeect iTraq terms to an arraylist and returns this list.
     * 
     * @param iTraqRatios the values to add
     * @return the updated array list
     */
    private static ArrayList addItraqUserTerms(String[][] iTraqRatios) {
        ArrayList userParams = new ArrayList(16);
        userParams.add(new UserParamImpl("114_114", 0, iTraqRatios[0][0]));
        userParams.add(new UserParamImpl("114_115", 1, iTraqRatios[0][1]));
        userParams.add(new UserParamImpl("114_116", 2, iTraqRatios[0][2]));
        userParams.add(new UserParamImpl("114_117", 3, iTraqRatios[0][3]));
        userParams.add(new UserParamImpl("115_114", 4, iTraqRatios[1][0]));
        userParams.add(new UserParamImpl("115_115", 5, iTraqRatios[1][1]));
        userParams.add(new UserParamImpl("115_116", 6, iTraqRatios[1][2]));
        userParams.add(new UserParamImpl("115_117", 7, iTraqRatios[1][3]));
        userParams.add(new UserParamImpl("116_114", 8, iTraqRatios[2][0]));
        userParams.add(new UserParamImpl("116_115", 9, iTraqRatios[2][1]));
        userParams.add(new UserParamImpl("116_116", 10, iTraqRatios[2][2]));
        userParams.add(new UserParamImpl("116_117", 11, iTraqRatios[2][3]));
        userParams.add(new UserParamImpl("117_114", 12, iTraqRatios[3][0]));
        userParams.add(new UserParamImpl("117_115", 13, iTraqRatios[3][1]));
        userParams.add(new UserParamImpl("117_116", 14, iTraqRatios[3][2]));
        userParams.add(new UserParamImpl("117_117", 15, iTraqRatios[3][3]));

        return userParams;
    }
}
