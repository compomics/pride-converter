package no.uib.prideconverter;

import be.proteomics.lims.db.accessors.Identification;
import be.proteomics.lims.db.accessors.Protocol;
import be.proteomics.lims.db.accessors.Spectrumfile;
import no.uib.prideconverter.gui.*;
import no.uib.prideconverter.util.*;
import no.uib.prideconverter.util.Properties;
import uk.ac.ebi.pride.model.implementation.core.*;
import uk.ac.ebi.pride.model.implementation.mzData.*;
import uk.ac.ebi.pride.model.interfaces.core.Experiment;
import uk.ac.ebi.pride.model.interfaces.core.FragmentIon;
import uk.ac.ebi.pride.model.interfaces.core.Peptide;
import uk.ac.ebi.pride.model.interfaces.core.MassDelta;
import uk.ac.ebi.pride.model.interfaces.mzdata.*;
import uk.ac.ebi.pride.xml.MzDataXMLUnmarshaller;
import uk.ac.ebi.pride.xml.XMLMarshaller;
import uk.ac.ebi.pride.xml.validation.PrideXmlValidator;
import uk.ac.ebi.pride.xml.validation.XMLValidationErrorHandler;

import javax.swing.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * This class contains the code for the actual conversion. Is works as 
 * the supervisor of all the wizard frames.
 *
 * @author  Harald Barsnes
 * 
 * Created March 2008
 */
public class PRIDEConverter extends AbstractPrideConverter {

    // Todo: check usage and move to abstract converter 
    private static ArrayList<IdentificationGeneral> ids;
    private static Collection<uk.ac.ebi.pride.model.interfaces.core.Identification> identifications;
    private static int totalNumberOfSpectra = 0;
    private static int totalPeptideCount = 0;
    private static String currentFileName = "";
    private static String spectrumKey = "";
    private static OutputDetails outputFrame;
    private static UserProperties userProperties;
    private static FragmentIonMappings fragmentIonMappings;
    private static Properties properties;
    private static ProgressDialog progressDialog;
    private static Connection conn = null;
    private static MzData mzData;
    private static boolean useErrorLog = true;
    private static boolean debug = false;

    static {
        userProperties = new UserProperties();
        userProperties.readUserPropertiesFromFile(null);
        fragmentIonMappings = new FragmentIonMappings();
        fragmentIonMappings.readFragmentIonMappingsFromFile();
        properties = new Properties();
    }

    /**
     * Creates a new instance of PRIDEConverter.
     * 
     * Opens the first frame of the wizard (Data Source Selection).
     */
    public PRIDEConverter() {
//        new DataSourceSelection(null);
    }

    public static ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public static OutputDetails getOutputFrame() {
        return outputFrame;
    }

    public static FragmentIonMappings getFragmentIonMappings() {
        return fragmentIonMappings;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setMzData(MzData mzData) {
        PRIDEConverter.mzData = mzData;
    }

    public static ArrayList<IdentificationGeneral> getIds() {
        return ids;
    }
    public static void setIds(ArrayList<IdentificationGeneral> ids) {
        PRIDEConverter.ids = ids;
    }

    public static String getCurrentFileName() {
        return currentFileName;
    }
    public static void setCurrentFileName(String currentFileName) {
        PRIDEConverter.currentFileName = currentFileName;
    }

    public static String getSpectrumKey() {
        return spectrumKey;
    }
    public static void setSpectrumKey(String spectrumKey) {
        PRIDEConverter.spectrumKey = spectrumKey;
    }

    public static int getTotalNumberOfSpectra() {
        return totalNumberOfSpectra;
    }
    public static void setTotalNumberOfSpectra(int totalNumberOfSpectra) {
        PRIDEConverter.totalNumberOfSpectra = totalNumberOfSpectra;
    }

    public static int getTotalPeptideCount() {
        return totalPeptideCount;
    }
    public static void setTotalPeptideCount(int totalPeptideCount) {
        PRIDEConverter.totalPeptideCount = totalPeptideCount;
    }

    public static Collection<uk.ac.ebi.pride.model.interfaces.core.Identification> getIdentifications() {
        return identifications;
    }
    public static void setIdentifications(Collection<uk.ac.ebi.pride.model.interfaces.core.Identification> identifications) {
        PRIDEConverter.identifications = identifications;
    }

    /**
     * Tries to connect to the ms_lims database.
     * 
     * @param dataBaseDetails a reference to the database details frame
     * @return true if connected, false otherwise
     */
    public static boolean connectToDataBase(DataBaseDetails dataBaseDetails) {

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

            //test to check if the supported version of ms_lims is used
            try {
                Protocol.getAllProtocols(conn); // test for ms_lims 7
            //Identification.getIdentification(conn, ""); // test for ms_lims 6
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(dataBaseDetails,
                        "Database connection not established:\n" +
                        "Verify that you are using the supported version of ms_lims,\n" +
                        "and upgrade if necessary: http://genesis.ugent.be/ms_lims.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                connectionSuccessfull = false;
                closeDataBaseConnection();
            }
        } catch (Exception e) {

            if (e.getMessage().lastIndexOf("Communications link failure") != -1) {
                // this is the most likely option as far as I can see
                JOptionPane.showMessageDialog(dataBaseDetails, "Database connection not established:" +
                        "\n" + "Verify server host.", "Database Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dataBaseDetails, "Database connection not established:" +
                        "\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        return connectionSuccessfull;
    }

    /**
     * Closes the ms_lims database connection
     */
    public static void closeDataBaseConnection() {

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

        // makes sure that '.' is used as the decimal sign
        Locale.setDefault(Locale.US);

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

//                try {
//                    PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());
//                    UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
//                } catch (UnsupportedLookAndFeelException e) {
//                    Util.writeToErrorLog("Setting Look And Feel: Error while attempting to set the Look And Feel");
//                    e.printStackTrace();
//                }

                // check if a newer version of PRIDE Converter is available
                try {

                    boolean deprecatedOrDeleted = false;

                    URL downloadPage = new URL(
                            "http://code.google.com/p/pride-converter/downloads/detail?name=PRIDE_Converter_" +
                            getPrideConverterVersionNumber() + ".zip");
                    int respons = ((java.net.HttpURLConnection) downloadPage.openConnection()).getResponseCode();

                    // 404 means that the file no longer exists, which means that 
                    // the running version is no longer available for download, 
                    // which again means that a never version is available.
                    if (respons == 404) {
                        deprecatedOrDeleted = true;
                    } else {

                        // also need to check if the available running version has been
                        // deprecated (but not deleted)
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(downloadPage.openStream()));

                        String inputLine;

                        while ((inputLine = in.readLine()) != null && !deprecatedOrDeleted) {
                            if (inputLine.lastIndexOf("Deprecated") != -1 &&
                                    inputLine.lastIndexOf("Deprecated Downloads") == -1 &&
                                    inputLine.lastIndexOf("Deprecated downloads") == -1) {
                                deprecatedOrDeleted = true;
                            }
                        }

                        in.close();
                    }

                    // informs the user about an updated version of the converter, unless the user
                    // is running a beta version
                    if (deprecatedOrDeleted && getPrideConverterVersionNumber().lastIndexOf("beta") == -1) {
                        int option = JOptionPane.showConfirmDialog(null,
                                "A newer version of PRIDE Converter is available.\n" +
                                "Do you want to upgrade?\n\n" +
                                "Selecting \'Yes\' will open the PRIDE Converter web page where you\n" +
                                "can see a list of the changes and download the latest version.",
                                "PRIDE Converter - Upgrade Available",
                                JOptionPane.YES_NO_CANCEL_OPTION);
                        if (option == JOptionPane.YES_OPTION) {
                            BareBonesBrowserLaunch.openURL("http://code.google.com/p/pride-converter/");
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

                // ToDo: move code from main method into DataSourceSelection ?
                // ToDo: move the look and feel into DataSourceSelection ?
                new DataSourceSelection(null);
            }
        });
    }

    /**
     * Cancels the convertion process by System.exit(0).
     * First saves the user properties.
     */
    public static void cancelConvertion() {

        userProperties.saveUserPropertiesToFile();
        fragmentIonMappings.saveFragmentIonMappingsToFile();

        if (debug) {
            System.out.println("Converter closed.");
        }

        System.exit(0);
    }

    /**
     * Tries to convert the selected file(s) into one PRIDE XML file.
     *
     * This is where the actual conversion occurs. All the information
     * inserted in the different frames are used in this method.
     *
     * @param outputDetails a reference to the OutputDetails frame.
     */
    public void convert(OutputDetails outputDetails) {

        userProperties.saveUserPropertiesToFile();
        fragmentIonMappings.saveFragmentIonMappingsToFile();
        setCancelConversion(false);

        outputFrame = outputDetails;
        progressDialog = new ProgressDialog(outputDetails, true);

        // this thread's only job is to make the progress dialog visible so that
        // it can be updated independently of the converter thread
        final Thread t = new Thread(new Runnable() {

            public void run() {

                if (properties.getDataSource().equalsIgnoreCase("ms_lims")) {
                    progressDialog.setIntermidiate(false);
                    progressDialog.setTitle("Retrieving Spectra. Please Wait...");
                } else {
                    progressDialog.setIntermidiate(true);
                    progressDialog.setTitle("Transforming Spectra. Please Wait...");
                }

                progressDialog.setVisible(true);
            }
        }, "ProgressDialog");

        t.start();

        // Wait until progress dialog is visible.
        //
        // The following is not needed in Java 1.6, but seemed to be needed in 1.5.
        //
        // Not including the lines _used to_ result in a crash on Windows, but not anymore.
        // Including the lines results in a crash on Linux and Mac.
        if (System.getProperty("os.name").toLowerCase().lastIndexOf("windows") != -1) {
            while (!progressDialog.isVisible()) {
            }
        }

        // this thread does the actual conversion
        new Thread("ConverterThread") {

            @Override
            public void run() {

                boolean xmlValidated = false;

                try {

                    properties.setAlreadyChoosenModifications(new ArrayList<String>());

                    if (isConversionCanceled()) {
                        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                        outputFrame.setConvertButtonEnabled(true);
                        return;
                    }

                    try {
                        progressDialog.setString(null);
                        progressDialog.setTitle("Transforming Spectra. Please Wait...");
                        progressDialog.setIntermidiate(true);
                        progressDialog.setString(null);
                    } catch (NullPointerException e) {
                        Util.writeToErrorLog("Progress bar: NullPointerException!!!\n" + e.toString());
                    }


                    // ToDo: big files: start writing the PRIDE XML (up to/including the SpectrumList header)


                    // Transform all selected spectra into mzData spectra and retrieve the identifications.
                    ArrayList<Spectrum> mzDataSpectra = new ArrayList<Spectrum>();

                    // a hashmap containing the filename (in some case a file id, i.e. a number) as the key
                    // and the spectrum id (to be used in the PRIDE XML file as the element.
                    HashMap filenameToSpectrumID = new HashMap();

                    // ToDo: big files: here we populate the spectra list!! (mzDataSpectra)
                    // ToDo: big files: check if we have all the information we need to write the PRIDE XML up to
                    // ToDo: big files: the SpectrumList start tag and write it before dealing with the spectra
                    // detect the data source used, and use the corresponding transformSpectra method
                    if (properties.getDataSource().equalsIgnoreCase("ms_lims")) {
                        filenameToSpectrumID = MS_LimsConverter.transformSpectraFrom_ms_lims(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("Mascot DAT File")) {
                        filenameToSpectrumID = MascotDatConverter.transformSpectraFromMascotDatFile(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("Mascot Generic File")) {
                        filenameToSpectrumID = MascotGenericConverter.transformSpectraFromMascotGenericFile(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("SEQUEST DTA File") ||
                            properties.getDataSource().equalsIgnoreCase("Spectrum Mill") ||
                            properties.getDataSource().equalsIgnoreCase("SEQUEST Result File")) {
                        filenameToSpectrumID = SequestSpectrumMillConverter.transformSpectraFromSequestAndSpectrumMill(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("Micromass PKL File") ||
                            properties.getDataSource().equalsIgnoreCase("VEMS")) {
                        filenameToSpectrumID = PKLandPKXConverter.transformSpectraFromPKLAndPKXFiles(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("X!Tandem")) {
                        filenameToSpectrumID = XTandemConverter.transformSpectraFromXTandemDataFile(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("mzXML")) {
                        filenameToSpectrumID = MzXMLConverter.transformSpectraFromMzXML(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("OMSSA")) {
                        filenameToSpectrumID = OmssaConverter.transformSpectraFromOMSSA(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("mzML")) {
                        filenameToSpectrumID = MzMLConverter.transformSpectraFromMzML(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("MS2")) {
                        filenameToSpectrumID = Ms2Converter.transformSpectraFromMS2Files(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("TPP")) {
                        filenameToSpectrumID = TPPConverter.transformSpectraFromTPPProjects(mzDataSpectra);
                    } else if (properties.getDataSource().equalsIgnoreCase("DTASelect")) {
                        filenameToSpectrumID = DTASelectConverter.transformSpectraFromDTASelectProjects(mzDataSpectra);
                    }

                    if (debug) {
                        System.out.println("\nProcessed " + mzDataSpectra.size() + " spectra to mzData format.");
                    }

                    if (isConversionCanceled()) {
                        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                        outputFrame.setConvertButtonEnabled(true);
                        return;
                    }

                    // removes the current file name string
                    progressDialog.setString(null);

                    try {
                        progressDialog.setTitle("Creating mzData. Please Wait...");
                        progressDialog.setIntermidiate(true);
                    } catch (NullPointerException e) {
                        Util.writeToErrorLog("Progress bar: NullPointerException!!!\n" + e.toString());
                    }

                    // create the mzData object. note the special cases for some data formats
                    if (properties.getDataSource().equalsIgnoreCase("TPP") ||
                            properties.getDataSource().equalsIgnoreCase("DTASelect")) {

                        // for TPP and DTASelect the mzDataFile has already been created
                        mzData = properties.getMzDataFile();

                    } else if (properties.getDataSource().equalsIgnoreCase("mzData")) {

                        // for mzData the, we just have to add/update the additional details
                        // and combine the files into one mzData file
                        mzData = combineMzDataFiles(new HashMap<String, Long>(), mzDataSpectra); // Note that the map is not used here

                    } else if (properties.getDataSource().equalsIgnoreCase("X!Tandem")) {
                        if (properties.getSelectedSourceFiles().get(0).toLowerCase().endsWith(".mzdata")) {
                            // the spectra has already been extracted
                        } else {
                            mzData = createMzData(mzDataSpectra);
                        }
                    } else {
                        mzData = createMzData(mzDataSpectra);
                    }

                    if (isConversionCanceled()) {
                        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                        outputFrame.setConvertButtonEnabled(true);
                        return;
                    }

                    // Make sure each spectrum is identified only once.
                    HashMap<String, InnerID> groupedIds = new HashMap<String, InnerID>();

                    if (properties.getDataSource().equalsIgnoreCase("Mascot Generic File") ||
                            properties.getDataSource().equalsIgnoreCase("SEQUEST DTA File") ||
                            properties.getDataSource().equalsIgnoreCase("Micromass PKL File") ||
                            properties.getDataSource().equalsIgnoreCase("mzXML") ||
                            properties.getDataSource().equalsIgnoreCase("VEMS") ||
                            properties.getDataSource().equalsIgnoreCase("MS2") ||
                            properties.getDataSource().equalsIgnoreCase("mzData") ||
                            properties.getDataSource().equalsIgnoreCase("TPP") ||
                            properties.getDataSource().equalsIgnoreCase("DTASelect")) {
                        // no identifications in mgf, dta, pkl, pkx and mzXML files
                        // for TPP and DTASelect the identifications are already handled

                        // so, for these cases we will not have initialised ids, so we need to do it here:
                        ids = new ArrayList<IdentificationGeneral>();
                    }

                    HashMap<String, IdentificationGeneral> omitDuplicates = new HashMap<String, IdentificationGeneral>(ids.size());

                    int unidentifedSpectraCounter = 0;

                    try {
                        progressDialog.setTitle("Removing Duplicated Spectra. Please Wait...");
                        progressDialog.setIntermidiate(false);
                        progressDialog.setMax(ids.size());
                    } catch (NullPointerException e) {
                        Util.writeToErrorLog("Progress bar: NullPointerException!!!\n" + e.toString());
                    }

                    for (int i = 0; i < ids.size(); i++) {

                        progressDialog.setValue(i);

                        if (isConversionCanceled()) {
                            outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                            outputFrame.setConvertButtonEnabled(true);
                            return;
                        }

                        if (ids.get(i) != null) {
                            IdentificationGeneral peptideIdentification = ids.get(i);
                            if (omitDuplicates.containsKey(peptideIdentification.getSpectrumFileId())) {
                                IdentificationGeneral oldID = omitDuplicates.get(
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

                    totalPeptideCount = omitDuplicates.size();

                    if (debug) {
                        System.out.println("\nTransformed " + (ids.size() -
                                unidentifedSpectraCounter) + " original identifications into " +
                                totalPeptideCount + " unique identifications.\n");
                    }

                    try {
                        progressDialog.setTitle("Grouping Identifications. Please Wait...");
                        progressDialog.setIntermidiate(false);
                        progressDialog.setMax(totalPeptideCount);
                    } catch (NullPointerException e) {
                        Util.writeToErrorLog("Progress bar: NullPointerException!!!\n" + e.toString());
                    }


                    // Cycle all unique identifications, group them by
                    // accession and retain only relevant data.
                    Iterator iter = omitDuplicates.values().iterator();

                    int counter = 0;

                    while (iter.hasNext()) {

                        if (isConversionCanceled()) {
                            outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                            outputFrame.setConvertButtonEnabled(true);
                            return;
                        }

                        progressDialog.setValue(counter++);

                        IdentificationGeneral peptideIdentification = (IdentificationGeneral) iter.next();

                        String accession = peptideIdentification.getPeptideAccession();
                        String accessionVersion = null;
                        Integer tempStartIndex = peptideIdentification.getPeptideStart();

                        // special case for DBToolkit ([0-9]*-[0-9]*)
                        String dbToolKitPattern = "\\([\\d]\\+?-[\\d]\\+?\\)";

                        Pattern pattern = Pattern.compile(dbToolKitPattern);
                        Matcher matcher = pattern.matcher(accession);

                        if (matcher.find()) {
                            tempStartIndex =
                                    new Integer(accession.substring(accession.lastIndexOf("(") +
                                    1, accession.lastIndexOf("-")));
                            accession = accession.substring(0, accession.lastIndexOf(" "));
                        }


                        // @TODO: Improve the parsing of accession number

                        // parsing of the accession number if it includs more than one '|'
                        // if it does the part after the second '|' is removed
                        if (accession.lastIndexOf("|") != -1) {
                            //if (properties.getDataSource().equalsIgnoreCase("SEQUEST Result File")) {
                            if (accession.indexOf("|", accession.indexOf("|") + 1) != -1) {
                                accession = accession.substring(0, accession.indexOf("|",
                                        accession.indexOf("|") + 1));
                            }
//                            } else {
//                                accession = accession.substring(0, accession.indexOf("|"));
//                            }
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
                            innerId = groupedIds.get(accession);
                        }

                        // Find the mzData spectrum reference from our HashMap.
                        Long spectrumRef =
                                (Long) filenameToSpectrumID.get(peptideIdentification.getSpectrumFileId());

                        if (spectrumRef == null) {
                            System.err.println("Could not find a spectrum reference for spectrum '" +
                                    peptideIdentification.getSpectrumFileId() + "'!\n");
                        }

                        boolean addIdentification = false;

                        if (properties.getProteinIdentificationFilter().length() > 0) {
                            if (accession.lastIndexOf(properties.getProteinIdentificationFilter()) == -1) {
                                addIdentification = true;
                            }
                        } else {
                            addIdentification = true;
                        }

                        if (addIdentification) {
                            innerId.addPeptide(spectrumRef,
                                    peptideIdentification.getSequence(),
                                    tempStartIndex,
                                    peptideIdentification.getScore(),
                                    peptideIdentification.getThreshold(),
                                    peptideIdentification.getModifications(),
                                    peptideIdentification.getCvParams(),
                                    peptideIdentification.getUserParams(),
                                    peptideIdentification.getFragmentIons());

                            // Add the new or re-store the modified protein ID to the hash.
                            groupedIds.put(accession, innerId);
                        }
                    }

                    if (debug) {
                        System.out.println("\nProcessed " + counter + " identifications.\n");
                    }

                    if (!properties.getDataSource().equalsIgnoreCase("TPP") &&
                            !properties.getDataSource().equalsIgnoreCase("DTASelect")) {
                        identifications = new ArrayList<uk.ac.ebi.pride.model.interfaces.core.Identification>(groupedIds.size());
                    }

                    iter = groupedIds.keySet().iterator();

                    if (isConversionCanceled()) {
                        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                        outputFrame.setConvertButtonEnabled(true);
                        return;
                    }

                    try {
                        progressDialog.setTitle("Converting Identifications. Please Wait...");
                        progressDialog.setIntermidiate(false);
                        progressDialog.setMax(groupedIds.size());
                    } catch (NullPointerException e) {
                        Util.writeToErrorLog("Progress bar: NullPointerException!!!\n" + e.toString());
                    }

                    counter = 0;

                    // Adding the identifications.
                    while (iter.hasNext()) {

                        if (isConversionCanceled()) {
                            outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                            outputFrame.setConvertButtonEnabled(true);
                            return;
                        }

                        progressDialog.setValue(counter++);
                        String key = (String) iter.next();
                        PRIDEConverter.InnerID lInnerID = groupedIds.get(key);
                        identifications.add(lInnerID.getGelFreeIdentification());
                    }

                    ArrayList<CvParam> experimentAdditionalCvParams = new ArrayList<CvParam>();

                    // add a CV term for the PRIDE Converter version
                    experimentAdditionalCvParams.add(new CvParamImpl(
                            "PRIDE:0000175", "PRIDE", "XML generation software",
                            (long) experimentAdditionalCvParams.size(),
                            (getWizardName() + " " + getPrideConverterVersionNumber())));

                    // add a CV term for the original MS data file format
                    experimentAdditionalCvParams.add(new CvParamImpl(
                            "PRIDE:0000218", "PRIDE", "Original MS data file format",
                            (long) experimentAdditionalCvParams.size(),
                            properties.getDataSource()));

                    // add the Project CV term
                    if (!properties.getExperimentProject().equalsIgnoreCase("")) {
                        experimentAdditionalCvParams.add(new CvParamImpl(
                                "PRIDE:0000097", "PRIDE", "Project",
                                (long) experimentAdditionalCvParams.size(),
                                properties.getExperimentProject()));
                    }

                    // add the Experiment Description CV term
                    if (!properties.getExperimentDescription().equalsIgnoreCase("")) {
                        experimentAdditionalCvParams.add(new CvParamImpl(
                                "PRIDE:0000040", "PRIDE", "Experiment description",
                                (long) experimentAdditionalCvParams.size(),
                                properties.getExperimentDescription()));
                    }

                    // create the PRIDE experiment (which holds all the data for the PRIDE XML)
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

                    // add it to a collection (the marshaller expects a collection)
                    Collection<Experiment> experiments = new ArrayList<Experiment>(1);
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
                                    Util.zerofill(properties.getProjectIds().get(0),
                                    properties.ZERO_FILLED_NUMBER_LENGTH) + "_";
                        }

                        completeFileName = userProperties.getOutputPath() +
                                projectIdsAsString + tempLabel + "_to_PRIDE_" +
                                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".xml";
                    } else {

                        String dataSourceShort = "";

                        if (properties.getDataSource().equalsIgnoreCase("Mascot DAT File")) {
                            dataSourceShort = "mascotDatFile";
                        } else if (properties.getDataSource().equalsIgnoreCase("Mascot Generic File")) {
                            dataSourceShort = "mascotGenericFile";
                        } else if (properties.getDataSource().equalsIgnoreCase("SEQUEST DTA File")) {
                            dataSourceShort = "dta_files";
                        } else if (properties.getDataSource().equalsIgnoreCase("X!Tandem")) {
                            dataSourceShort = "x!Tandem";
                        } else if (properties.getDataSource().equalsIgnoreCase("Micromass PKL File")) {
                            dataSourceShort = "pkl_files";
                        } else if (properties.getDataSource().equalsIgnoreCase("Spectrum Mill")) {
                            dataSourceShort = "spectrum_mill";
                        } else if (properties.getDataSource().equalsIgnoreCase("SEQUEST Result File")) {
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
                        } else if (properties.getDataSource().equalsIgnoreCase("DTASelect")) {
                            dataSourceShort = "dta_select";
                        }

                        // the complete name of (and path to) the PRIDE XML file to be generated
                        completeFileName = userProperties.getOutputPath() +
                                dataSourceShort + "_" + tempLabel + "_to_PRIDE_" +
                                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".xml";
                    }

                    BufferedWriter bw = new BufferedWriter(new FileWriter(completeFileName));

                    if (debug) {
                        System.out.println("XMLMarshaller begins.");
                    }

                    if (isConversionCanceled()) {
                        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                        outputFrame.setConvertButtonEnabled(true);
                        return;
                    }

                    try {
                        progressDialog.setTitle("Creating XML File. Please Wait...");
                        progressDialog.setIntermidiate(true);
                    } catch (NullPointerException e) {
                        Util.writeToErrorLog("Progress bar: NullPointerException!!!\n" + e.toString());
                    }

                    // marshall out the whole experiment in one go
                    // needs a lot of memory!
                    // ToDo: replace by multi-step marshalling:
                    // ToDo:    1. marshal XML up to (including) SpectrumList tag using a placeholder for the spectrum counter
                    // ToDo:    2. marshal each spectrum as we transform it from the original input
                    // ToDo:    3. marshal the rest of the XML (including identifications)
                    // ToDo: step 1 has to be done before all the transformation/processing of the input files/spectra info
                    // ToDo: step 2 can be marshalled into the file started in 1 or in a separate file for later merging
                    // ToDo: step 3 includes all identifications (we have to investigate if we could marshal them out
                    // ToDo:    individually as well or if they are needed in memory until all spectra have been processed
                    XMLMarshaller xmlOut = new XMLMarshaller(true);
                    xmlOut.marshallExperiments(experiments, bw);

                    bw.flush();
                    bw.close();

                    if (debug) {
                        System.out.println("XMLMarshaller done.");
                    }

                    try {
                        progressDialog.setTitle("Validating PRIDE XML File. Please Wait...");
                        progressDialog.setIntermidiate(true);
                    } catch (NullPointerException e) {
                        Util.writeToErrorLog("Progress bar: NullPointerException!!!\n" + e.toString());
                    }

                    if (isConversionCanceled()) {
                        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                        outputFrame.setConvertButtonEnabled(true);
                        return;
                    }

                    // validate the xml file
                    //
                    // NB: validation may cause a java stack overflow exception, if so
                    // increase the stack settings in the JavaOptions file.
                    FileReader reader = new FileReader(completeFileName);
                    XMLValidationErrorHandler xmlErrors = PrideXmlValidator.validate(reader);

                    xmlValidated = xmlErrors.noErrors();
                    reader.close();

                    if (!xmlValidated) {

                        progressDialog.setVisible(false);
                        progressDialog.dispose();
                        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

                        Util.writeToErrorLog("PRIDE XML validation failed!");
                        Util.writeToErrorLog(xmlErrors.getErrorsFormattedAsPlainText());
                        JOptionPane.showMessageDialog(
                                outputFrame,
                                "PRIDE XML validation failed!\n\n" +
                                "See ../Properties/ErrorLog.txt for more details.",
                                "PRIDE XML Validation Failed",
                                JOptionPane.ERROR_MESSAGE);
                    } else {

                        if (debug) {
                            System.out.println("PRIDE XML validated.");
                        }


                        // get the size (in MB) of the PRIDE XML file before zipping
                        double sizeOfFileBeforeZipping = Util.roundDouble(((double) new File(completeFileName).length() /
                                properties.NUMBER_OF_BYTES_PER_MEGABYTE), 2);


                        try {
                            progressDialog.setTitle("Zipping PRIDE XML File. Please Wait...");
                            progressDialog.setIntermidiate(true);
                        } catch (NullPointerException e) {
                            Util.writeToErrorLog("Progress bar: NullPointerException!!!\n" + e.toString());
                        }


                        // gzip the PRIDE XML file
                        File file = new File(completeFileName);
                        FileOutputStream fos = new FileOutputStream(completeFileName + ".gz");
                        GZIPOutputStream gzos = new GZIPOutputStream(fos);
                        FileInputStream fin = new FileInputStream(file);
                        BufferedInputStream in = new BufferedInputStream(fin);

                        byte[] buffer = new byte[1024];

                        int i;

                        while ((i = in.read(buffer)) >= 0) {
                            gzos.write(buffer, 0, i);
                        }

                        in.close();
                        gzos.close();


                        // get the size (in MB) of the PRIDE XML file after zipping
                        double sizeOfZippedFile = Util.roundDouble(((double) new File(completeFileName + ".gz").length() /
                                properties.NUMBER_OF_BYTES_PER_MEGABYTE), 2);

                        // makes sure that a file size if 0.0 MB is never shown, use 0.1 instead
                        if(sizeOfZippedFile == 0.0){
                            sizeOfZippedFile = 0.1;
                        }

                        progressDialog.setVisible(false);
                        progressDialog.dispose();
                        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

                        // delete the unzipped version of the PRIDE XML file
                        if (!new File(completeFileName).delete()) {
                            JOptionPane.showMessageDialog(outputFrame,
                                    "Not able to delete the original (unzipped) PRIDE XML file " +
                                    completeFileName,
                                    "Could Not Delete File",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }

                        int spectraCount = mzData.getSpectrumCollection().size();

                        // present a dialog with information about the created file
                        JOptionPane.showMessageDialog(outputFrame,
                                "PRIDE XML File Created Successfully:\n" +
                                completeFileName + ".gz" +
                                "\n\nThe File Includes:\n" +
                                "Spectra: " + spectraCount + "\n" +
                                "Peptide Identifications: " + totalPeptideCount + "\n" +
                                "Protein Identifications: " + identifications.size() + "\n\n" +
                                "PRIDE XML File Size: " + sizeOfZippedFile + " MB",
                                "PRIDE XML File Created",
                                JOptionPane.INFORMATION_MESSAGE);

                        // insert the information into the OutputDetails frame
                        outputFrame.insertConvertedFileDetails(spectraCount,
                                totalPeptideCount, identifications.size(),
                                sizeOfZippedFile, completeFileName + ".gz");


                        // check the size of the unzipped file and compare it to the
                        // maximum size recommended before using the FTP server
                        if (sizeOfFileBeforeZipping > 15) {
                            new RequestFtpAccess(outputFrame, true);
                        }
                    }

                    // memory clean up
                    ids.clear();
                    filenameToSpectrumID.clear();
                    mzDataSpectra.clear();
                    mzData = null;
                    groupedIds.clear();
                    omitDuplicates.clear();
                    experiment = null;
                    bw.close();
                    reader.close();
                    xmlOut = null;

                } catch (OutOfMemoryError error) {
                    outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                    progressDialog.setVisible(false);
                    progressDialog.dispose();
                    Runtime.getRuntime().gc();
                    JOptionPane.showMessageDialog(null,
                            "The task used up all the available memory and had to be stopped.\n" +
                            "Memory boundaries are set in ../Properties/JavaOptions.txt. See\n" +
                            "the Troubleshooting section at the PRIDE Converter home page for\n" +
                            "details. The PRIDE XML file was not created.\n\n" +
                            "If the data sets are too big for your computer, e-mail a support\n" +
                            "request to the PRIDE team at the EBI: pride-support@ebi.ac.uk",
                            "Out Of Memory Error",
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
                    setCancelConversion(true);
                }

                outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }.start();

        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
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
    public static Connection getConn() {
        return conn;
    }

    /**
     * Adds a modification code (a.k.a. name) cvParam mapping.
     *
     * @param modificationCode code
     * @param cvParam CV parameter
     * @param modificationMonoMass mass
     */
    public static void addModification(String modificationCode, CvParamImpl cvParam, Double modificationMonoMass) {

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
     * Adds the extracted modifications to the given peptideModifications list and
     * returns the unmodified peptide sequence
     *
     * @param peptideModifications the (most likely) empty list of peptide modifications
     * @param peptideSequence the (modified) peptide sequence
     * @param variableModifications the hashmap of variable modifications
     * @param fixedModifications the hashmap of fixed modifications
     * @return the unmodified peptide sequence
     */
    protected static String addModificationDetails(ArrayList<ModificationImpl> peptideModifications, String peptideSequence,
            HashMap<String, Double> variableModifications, HashMap<String, Double> fixedModifications) {

        String[] sequenceArray = new String[peptideSequence.length()];

        int index = 0;

        List<String> aminoAcids = new ArrayList<String>();
        Collections.addAll(aminoAcids,
                "G", "A", "S", "P", "V", "T", "C", "L", "I",
                "X", "N", "O", "B", "D", "Q", "K", "Z", "E",
                "M", "H", "F", "R", "Y", "W");

        for (int i = 0; i < peptideSequence.length() && !isConversionCanceled(); i++) {

            if (i == peptideSequence.length() - 1) {
                sequenceArray[index++] =
                        peptideSequence.substring(peptideSequence.length() - 1, peptideSequence.length());
            } else {

                String secondAminoAcid = peptideSequence.substring(i + 1, i + 2);

                if (!aminoAcids.contains(secondAminoAcid)) {
                    sequenceArray[index++] = peptideSequence.substring(i, i + 2);
                    i++;
                } else {
                    sequenceArray[index++] = peptideSequence.substring(i, i + 1);
                }
            }
        }

        peptideSequence = "";

        for (int i = 0; i < sequenceArray.length && !isConversionCanceled(); i++) {

            if (sequenceArray[i] != null) {

                if (sequenceArray[i].length() == 2) {

                    if (properties.getAlreadyChoosenModifications().contains(sequenceArray[i])) {

                        CvParamImpl tempCvParam =
                                (CvParamImpl) userProperties.getCVTermMappings().get(sequenceArray[i]);

                        ArrayList<CvParam> modificationCVParams = new ArrayList<CvParam>();
                        modificationCVParams.add(tempCvParam);

                        ArrayList<MassDelta> monoMasses = new ArrayList<MassDelta>();

                        // get the modification mass (DiffMono) retrieved from PSI-MOD
                        if (tempCvParam.getValue() != null) {
                            monoMasses.add(new MonoMassDeltaImpl(
                                    Double.parseDouble(tempCvParam.getValue())));
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
                                i + 1,
                                tempCvParam.getCVLookup(),
                                null,
                                monoMasses,
                                null,
                                modificationCVParams,
                                null));
                    } else {

                        String modificationName = sequenceArray[i];
                        String modificationNameShort = sequenceArray[i].substring(0, 1);
                        Double modificationMass = variableModifications.get(modificationNameShort);

                        if (!properties.getAlreadyChoosenModifications().contains(modificationName)) {
                            new ModificationMapping(outputFrame,
                                    true, progressDialog, modificationName,
                                    modificationNameShort,
                                    modificationMass,
                                    (CvParamImpl) userProperties.getCVTermMappings().get(modificationName),
                                    false);

                            properties.getAlreadyChoosenModifications().add(modificationName);
                        } else {
                            //do nothing, mapping already choosen
                        }

                        CvParamImpl tempCvParam =
                                (CvParamImpl) userProperties.getCVTermMappings().get(sequenceArray[i]);

                        ArrayList<CvParam> modificationCVParams = new ArrayList<CvParam>();
                        modificationCVParams.add(tempCvParam);

                        ArrayList<MassDelta> monoMasses = new ArrayList<MassDelta>();

                        // get the modification mass (DiffMono) retrieved from PSI-MOD
                        if (tempCvParam.getValue() != null) {
                            monoMasses.add(new MonoMassDeltaImpl(
                                    Double.parseDouble(tempCvParam.getValue())));
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
                                i + 1,
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

                    CvParamImpl tempCvParam =
                            (CvParamImpl) userProperties.getCVTermMappings().get(sequenceArray[i].substring(0, 1));

                    ArrayList<CvParam> modificationCVParams = new ArrayList<CvParam>();
                    modificationCVParams.add(tempCvParam);

                    ArrayList<MassDelta> monoMasses = new ArrayList<MassDelta>();

                    // get the modification mass (DiffMono) retrieved from PSI-MOD
                    if (tempCvParam.getValue() != null) {
                        monoMasses.add(new MonoMassDeltaImpl(
                                Double.parseDouble(tempCvParam.getValue())));
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
                            i + 1,
                            tempCvParam.getCVLookup(),
                            null,
                            monoMasses,
                            null,
                            modificationCVParams,
                            null));
                } else {

                    if (fixedModifications.containsKey(sequenceArray[i].substring(0, 1))) {

                        String modificationName = sequenceArray[i];
                        String modificationNameShort = sequenceArray[i].substring(0, 1);
                        Double modificationMass = fixedModifications.get(modificationNameShort);

                        if (!properties.getAlreadyChoosenModifications().contains(modificationName)) {
                            new ModificationMapping(outputFrame,
                                    true, progressDialog, modificationName,
                                    modificationNameShort,
                                    modificationMass,
                                    (CvParamImpl) userProperties.getCVTermMappings().get(modificationName),
                                    true);

                            properties.getAlreadyChoosenModifications().add(modificationName);
                        } else {
                            //do nothing, mapping already choosen
                        }


                        CvParamImpl tempCvParam =
                                (CvParamImpl) userProperties.getCVTermMappings().get(sequenceArray[i]);

                        ArrayList<CvParam> modificationCVParams = new ArrayList<CvParam>();
                        modificationCVParams.add(tempCvParam);

                        ArrayList<MassDelta> monoMasses = new ArrayList<MassDelta>();

                        // get the modification mass (DiffMono) retrieved from PSI-MOD
                        if (tempCvParam.getValue() != null) {
                            monoMasses.add(new MonoMassDeltaImpl(
                                    Double.parseDouble(tempCvParam.getValue())));
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
                                i + 1,
                                tempCvParam.getCVLookup(),
                                null,
                                monoMasses,
                                null,
                                modificationCVParams,
                                null));
                    }
                }
            }

            if (sequenceArray[i] != null) {
                peptideSequence += sequenceArray[i].substring(0, 1);
            }
        }

        return peptideSequence;
    }

    /**
     * This method reads all the MS2 spectra from the harddrive, converts them to mzData spectra
     * and stores a mapping of filename to ID number in the mzData structure. The latter is used
     * afterwards to connect the peptide identifications to their corresponding spectra.
     * Note that the final Collection of spectra, as well as the mapping table are passed as
     * reference parameters here!
     *
     * @param aFiles    File[] with the spectra files to load.
     * @param aSpectra  Collection in which the transformed mzData spectra will be stored.
     * @param aMappings HashMap with the (filename --> mzData ID number) mapping.
     * @throws java.io.IOException when one of the files could not be read.
     */
    protected static void readMS2Spectra(File[] aFiles, Collection<Spectrum> aSpectra, HashMap<String, Long> aMappings) throws IOException {

        // This counter is used to generate the ID numbers for the mzData spectra.
        int idCount = 0;

        for (int i=0; i< aFiles.length && !PRIDEConverter.isConversionCanceled(); i++) {

            // Setting up the variables we'll collect.
            String filename = aFiles[i].getName();

            // Ignore non .ms2 files from the input folder
            if (filename.toLowerCase().endsWith(".ms2")) {

                String usedFileName = filename.substring(0, filename.indexOf("."));

                Double precursorMZ = null;
                String scanNumber = null;

                int precursorCharge = -1;
                double[] mzArray = null;
                double[] intensityArray = null;

                BufferedReader br = new BufferedReader(new FileReader(aFiles[i]));
                String currentLine = null;
                int lineCount = 0;

                // We don't know in advance how many m/z and intensity values we'll need to store,
                // so use these in the interim.
                ArrayList<String> mz = new ArrayList<String>();
                ArrayList<String> intensities = new ArrayList<String>();

                while ((currentLine = br.readLine()) != null && !PRIDEConverter.isConversionCanceled()) {

                    lineCount++;

                    if (!(currentLine.equals(""))) {

                        // Take the precursor mass
                        if (currentLine.startsWith("S")) {
                            if (precursorMZ != null) {

                                // And process the m/z and intensities.
                                int size = mz.size();
                                mzArray = new double[size];
                                intensityArray = new double[size];

                                for (int j = 0; j < size; j++) {
                                    mzArray[j] = Double.parseDouble(mz.get(j));
                                    intensityArray[j] = Double.parseDouble(intensities.get(j));
                                }

                                // OK, all done. Create a mzData spectrum next!
                                Spectrum mzdataSpectrum = transformSpectrum(idCount, mzArray, intensityArray, precursorCharge, precursorMZ);

                                // Add the spectrum and its mapping to the collection and table.
                                aSpectra.add(mzdataSpectrum);

                                // In one project the scanNumber had an extra "0" that has to be removed here in order
                                // to map correctly the peptide to the spectra (for instance 011717 as scanNumber,
                                //  11717 in the file)
                                if (scanNumber.length() == 6) {
                                    scanNumber = scanNumber.substring(1, scanNumber.length());
                                }

                                // We are going to use the file name and the scanNumber to build the HashMap
                                Long xTmp = aMappings.put(usedFileName + "_" + scanNumber, (long) idCount);

                                if (xTmp != null) {
                                    // we already stored a result for this ID!!!
                                    JOptionPane.showMessageDialog(null, "Ambiguous spectrum mapping. Consult " +
                                            "the error log file for details.", "Mapping Error", JOptionPane.ERROR_MESSAGE);
                                    Util.writeToErrorLog("Ambiguous spectrum mapping for ID '" + scanNumber
                                            + "' and spectrum file '" + usedFileName + "'." );
                                    PRIDEConverter.setCancelConversion(true);
                                }

                                // If you'll look at the above method, you'll see that it consumes two ID's -
                                // one for the spectrum and one for the precursor. So advance it by 2 here.
                                idCount += 1;

                                // That completes the cycle.
                                mz = new ArrayList<String>();
                                intensities = new ArrayList<String>();
                            }

                            String[] scanTokens = currentLine.split("\t");

                            if (!(scanTokens.length == 4)) {

                                if (debug) {
                                    System.out.println("Current split scan line tokens:");

                                    for (String scanToken : scanTokens) {
                                        System.out.println(scanToken);
                                    }

                                    throw new IOException("There were " + scanTokens.length +
                                            " elements (instead of the expected 4) on line " + lineCount + " in your file ('" + filename + "')!");
                                }
                            } else {
                                // we do not need the initial H, start and end scans, just the precursor mass
                                scanNumber = scanTokens[1].trim();
                                precursorMZ = new Double(scanTokens[3].trim());
                            }

                            // Read mz and intensities
                        } else if (!(currentLine.startsWith("H") || currentLine.startsWith("S") ||
                                currentLine.startsWith("I") || currentLine.startsWith("Z"))) {
                            String[] mzAndintensity = currentLine.split(" ");
                            mz.add(mzAndintensity[0].trim());
                            intensities.add(mzAndintensity[1].trim());
                        }
                    }
                }

                // FENCE-POST:
                int size = mz.size();
                mzArray = new double[size];
                intensityArray = new double[size];

                for (int j = 0; j < size; j++) {
                    mzArray[j] = Double.parseDouble(mz.get(j));
                    intensityArray[j] = Double.parseDouble(intensities.get(j));
                }

                // OK, all done. Create a mzData spectrum next!
                Spectrum mzdataSpectrum = transformSpectrum(idCount, mzArray, intensityArray, precursorCharge, precursorMZ.doubleValue());

                // Add the spectrum and its mapping to the collection and table.
                aSpectra.add(mzdataSpectrum);

                // Note that the actual ID for the spectrum is the ID we passed in +1 - see the transforming method for details.
                Long xTmp = aMappings.put(usedFileName + "_" + scanNumber, (long) idCount);

                if (xTmp != null) {
                    // we already stored a result for this ID!!!
                    JOptionPane.showMessageDialog(null, "Ambiguous spectrum mapping. Consult " +
                            "the error log file for details.", "Mapping Error", JOptionPane.ERROR_MESSAGE);
                    Util.writeToErrorLog("Ambiguous spectrum mapping for ID '" + scanNumber
                            + "' and spectrum file '" + usedFileName + "'." );
                    PRIDEConverter.setCancelConversion(true);
                }

                // If you'll look at the above method, you'll see that it consumes two ID's -
                // one for the spectrum and one for the precursor. So advance it by 2 here.
                idCount += 1;

                // Ok, we're through the file. Close it.
                br.close();
            } else if (filename.toLowerCase().endsWith(".dta")) {

                if (debug) {
                    System.out.println("The file " + filename + " was not included among the processed files\n");
                }

                // @TODO: implement for dta files as well?
                // The part below has not been tested as we have no examples of DTASelect projects
                // using dta files as spectrum files. But most of the code below should work.

//                try {
//                    FileReader f = new FileReader(new File(filename));
//                    BufferedReader b = new BufferedReader(f);
//
//                    String currentLine = b.readLine();
//
//                    if (properties.isCommaTheDecimalSymbol()) {
//                        currentLine = currentLine.replaceAll(",", ".");
//                    }
//
//                    StringTokenizer tok = new StringTokenizer(currentLine);
//
//                    double precursorMass = new Double(tok.nextToken()).doubleValue();
//                    int precursorCharge = new Integer(tok.nextToken()).intValue();
//
//                    currentLine = b.readLine();
//
//                    Vector masses = new Vector();
//                    Vector intensities = new Vector();
//
//                    boolean emptyLineFound = false;
//
//                    while (currentLine != null && !emptyLineFound) {
//
//                        if (currentLine.equalsIgnoreCase("")) {
//                            emptyLineFound = true;
//                        } else {
//
//                            if (properties.isCommaTheDecimalSymbol()) {
//                                currentLine = currentLine.replaceAll(",", ".");
//                            }
//
//                            tok = new StringTokenizer(currentLine);
//                            masses.add(new Double(tok.nextToken()));
//                            intensities.add(new Double(tok.nextToken()));
//
//                            currentLine = b.readLine();
//                        }
//                    }
//
//                    double[][] arrays = new double[2][masses.size()];
//
//                    for (int j = 0; j < masses.size(); j++) {
//                        arrays[0][j] = ((Double) masses.get(j)).doubleValue();
//                        arrays[1][j] = ((Double) intensities.get(j)).doubleValue();
//                    }
//
//                    b.close();
//                    f.close();
//
//
//                    // Precursor collection.
//                    ArrayList precursors = new ArrayList(1);
//
//                    // Ion selection parameters.
//                    ArrayList ionSelection = new ArrayList(4);
//
//                    // See if we know the precursor charge, and if so, include it.
//                    if (precursorCharge > 0) {
//                        ionSelection.add(new CvParamImpl("PSI:1000041",
//                                "PSI", "ChargeState", 0,
//                                Integer.toString(precursorCharge)));
//                    }
//
//                    ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
//                            "MassToChargeRatio", 1, Double.toString(
//                            precursorMass)));
//
//                    precursors.add(new PrecursorImpl(null, null,
//                            ionSelection, null, 2, 0, 0));
//
//                    if (arrays[properties.MZ_ARRAY].length > 0) {
//                        Double mzRangeStart = new Double(arrays[properties.MZ_ARRAY][0]);
//                        Double mzRangeStop = new Double(arrays[properties.MZ_ARRAY][arrays[properties.MZ_ARRAY].length - 1]);
//
//                        // Create new mzData spectrum for the fragmentation spectrum.
//                        SpectrumImpl fragmentation = new SpectrumImpl(
//                                new BinaryArrayImpl(arrays[properties.INTENSITIES_ARRAY],
//                                BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
//                                mzRangeStart,
//                                new BinaryArrayImpl(arrays[properties.MZ_ARRAY],
//                                BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
//                                2, null,
//                                mzRangeStop,
//                                null,
//                                idCount++, precursors,
//                                null,
//                                null,
//                                null,
//                                null, null);
//
//                        // Store (spectrumfileid, spectrumid) mapping.
//
//                        String usedFileName = filename.substring(0, filename.indexOf("."));
//
//                        aMappings.put(usedFileName + "_" + scanNumber, new Long(idCount));
//
//                        // Store the transformed spectrum.
//                        aSpectra.add(fragmentation);
//                    }
//                } catch (Exception e) {
//
//                    boolean errorDetected = true;
//
//                    if (!isConversionCanceled()) {
//
//                        Util.writeToErrorLog("Error parsing SEQUEST DTA file: ");
//                        e.printStackTrace();
//
//                        JOptionPane.showMessageDialog(null,
//                                "The following file could not parsed as a " +
//                                "SEQUEST DTA file:\n " +
//                                filename +
//                                "\n\n" +
//                                "See ../Properties/ErrorLog.txt for more details.",
//                                "Error Parsing File", JOptionPane.ERROR_MESSAGE);
//                    }
//                }
            } else {
                if (debug) {
                    System.out.println("The file " + filename + " was not included among the processed files\n");
                }
            }
        }
    }

    /**
     * This method transform the information from a peaklist into an mzData spectrum.
     *
     * @param aId              int with the ID for the precurosr. The ID of the fragmentation spectrum itself will be this ID + 1.
     * @param aMzArray         double[] with the m/z values for this spectrum.
     * @param aIntensityArray  double[] with the intensity values for this spectrum.
     * @param aPrecursorCharge int with the precursor charge.
     * @param aPrecursorMZ     double with the precursor m/z.
     * @return int with the current state of the counter.
     */
    private static Spectrum transformSpectrum(int aId, double[] aMzArray, double[] aIntensityArray,
            int aPrecursorCharge, double aPrecursorMZ) {

        // Sort this array (we'll need the range start and stop later).
        Arrays.sort(aMzArray);

        // Precursor annotation collection.
        Collection<Precursor> precursors = new ArrayList<Precursor>(1);

        // Ion selection annotation parameters.
        Collection<CvParam> ionSelection = new ArrayList<CvParam>(4); // ToDo: never used !?  should be integrated?

        // See if we know the precursor charge, and if so, include it.

        if (aPrecursorCharge > 0) {
            ionSelection.add(new CvParamImpl("PSI:1000041", "PSI", "ChargeState", 0, Integer.toString(aPrecursorCharge)));
        }

        // See if we know the precursor intensity (for the machine used here,
        // intensity is always 'NumberOfCounts').
        ionSelection.add(new CvParamImpl("PSI:1000040", "PSI", "MassToChargeRatio", 1, Double.toString(aPrecursorMZ)));

        aId++; // Note that the counter is used AND incremented here.

        precursors.add(new PrecursorImpl(null, null, ionSelection, null, 2, aId, 0));

        // Create new mzData spectrum for the fragmentation spectrum.
        // Notice that certain collections and annotations are 'null' here.
        Spectrum fragmentation = new SpectrumImpl(
                new BinaryArrayImpl(aIntensityArray, BinaryArrayImpl.BIG_ENDIAN_LABEL),
                aMzArray[0],
                new BinaryArrayImpl(aMzArray, BinaryArrayImpl.BIG_ENDIAN_LABEL),
                2,
                null,
                aMzArray[aMzArray.length - 1],
                null,
                aId,
                precursors,
                null, null, null, null, null);

        return fragmentation;
    }

    /**
     * Class that helps in the conversion of identifications to PRIDE GelFreeIdentification.
     */
    protected static class InnerID {

        private String iAccession = null;
        private String iAccessionVersion = null;
        private ArrayList<Peptide> iPeptides = new ArrayList<Peptide>();
        private String iSearchEngine = null;
        private String iDatabase = null;
        private String iDBVersion = null;
        private ArrayList<Double> iScores = new ArrayList<Double>();
        private ArrayList<Double> iThresholds = new ArrayList<Double>();
        private Integer iSequenceCount = null;
        private Integer iSpectrumCount = null;
        private Double iCoverage = null;
        private Integer iProteinLength = null;
        private Double iMolecularWeight = null;
        private Double iPI = null;
        private String iValidationStatus = null;
        private String iDescription = null;

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
         * Constructor that the takes the accessionnumber for the future GelFreeIdentification and
         * the accession version (if any) as well as the search engine that performed the identification and
         * the database name and version for the search.
         *
         * @param iAccession   String with the accession number for the identification.
         * @param iSequenceCount
         * @param iSpectrumCount
         * @param iCoverage
         * @param iProteinLength
         * @param iMolecularWeight
         * @param iPI
         * @param iValidationStatus
         * @param iDescription
         */
        protected InnerID(String iAccession, Integer iSequenceCount, Integer iSpectrumCount, Double iCoverage,
                Integer iProteinLength, Double iMolecularWeight, Double iPI, String iValidationStatus,
                String iDescription, String aSearchEngine, String aDatabase) {
            this.iAccession = iAccession;
            this.iSequenceCount = iSequenceCount;
            this.iSpectrumCount = iSpectrumCount;
            this.iCoverage = iCoverage;
            this.iProteinLength = iProteinLength;
            this.iMolecularWeight = iMolecularWeight;
            this.iPI = iPI;
            this.iValidationStatus = iValidationStatus;
            this.iDescription = iDescription;
            this.iSearchEngine = aSearchEngine;
            this.iDatabase = aDatabase;
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
                Collection aUserParams, Collection<FragmentIon> aFragmentIons) {

            iPeptides.add(new PeptideImpl(aSpectrumRef, aSequence, aStart, modifications, cVParams, aUserParams, aFragmentIons));
            iScores.add(aScore);
            iThresholds.add(aThreshold);
        }

        /**
         * This method adds a peptide identification to the protein identification.
         *
         * @param aSpectrumRef
         * @param aCharge
         * @param aSequestXcorr
         * @param aSequestDelta
         * @param aPeptideMass
         * @param aCalculatedPeptideMass
         * @param aTotalIntensity
         * @param aSequestRSp
         * @param aSequestSp
         * @param aIonProportion
         * @param aRedundancy
         * @param aSequence
         * @param aPreAA
         * @param aPostAA
         * @param aCvParams
         * @param aUserParams
         */
        public void addPeptide(Long aSpectrumRef, Integer aCharge, Double aSequestXcorr, Double aSequestDelta,
                Double aPeptideMass, Double aCalculatedPeptideMass, Double aTotalIntensity, Integer aSequestRSp,
                Double aSequestSp, Double aIonProportion, Integer aRedundancy, String aSequence, String aPreAA,
                String aPostAA, Collection aModifications, Collection<CvParam> aCvParams, Collection<UserParam> aUserParams) {

            // Add here the information that is specific for each peptide
            if (aCvParams == null) {
                aCvParams = new ArrayList<CvParam>(8);
            }

            aCvParams.add(new CvParamImpl("PRIDE:0000065", "PRIDE", "Upstream flanking sequence", 1, aPreAA));
            aCvParams.add(new CvParamImpl("PRIDE:0000066", "PRIDE", "Downstream flanking sequence", 2, aPostAA));
            aCvParams.add(new CvParamImpl("PRIDE:0000013", "PRIDE", "X correlation", 3, aSequestXcorr.toString()));

            // We have to take into account the existence of missing values in the text file that we are parsing
            if (aSequestDelta != null) {
                aCvParams.add(new CvParamImpl("PRIDE:0000012", "PRIDE", "Delta Cn", 4, aSequestDelta.toString()));
            }

            aCvParams.add(new CvParamImpl("PRIDE:0000054", "PRIDE", "Sp", 5, aSequestSp.toString()));

            if (aSequestRSp != null) {
                aCvParams.add(new CvParamImpl("PRIDE:0000062", "PRIDE", "RSp", 6, aSequestRSp.toString()));
            }

            aCvParams.add(new CvParamImpl("PSI:1000224", "PSI", "Charge State", 7, aCharge.toString()));

            // Add individual additional user parameters.
            if (aUserParams == null) {
                aUserParams = new ArrayList<UserParam>(4);
            }

            aUserParams.add(new UserParamImpl("Peptide Mass", 1, aPeptideMass.toString()));
            aUserParams.add(new UserParamImpl("CalcM+H+ Mass", 2, aCalculatedPeptideMass.toString()));
            aUserParams.add(new UserParamImpl("Total Intensity", 3, aTotalIntensity.toString()));
            aUserParams.add(new UserParamImpl("Ion Proportion", 4, aIonProportion.toString()));
            aUserParams.add(new UserParamImpl("Redundancy", 5, aRedundancy.toString()));

            iPeptides.add(new PeptideImpl(aSpectrumRef, aSequence, null, aModifications, aCvParams, aUserParams));
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
                temp += (Double) iter.next();
                counter++;
            }

            if (!properties.getDataSource().equalsIgnoreCase("Spectrum Mill")) {
                temp /= counter;
            }

            BigDecimal bd = new BigDecimal(temp).setScale(2, BigDecimal.ROUND_HALF_UP);

            return bd.doubleValue();
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
                temp += (Double) iter.next();
                counter++;
            }

            temp /= counter;
            BigDecimal bd = new BigDecimal(temp).setScale(2, BigDecimal.ROUND_HALF_UP);

            return bd.doubleValue();
        }

        /**
         * Returns the accession number
         *
         * @return the accession number
         */
        public String getIAccession() {
            return iAccession;
        }

        /**
         * Returns the sequence count
         *
         * @return the sequence count
         */
        public Integer getISequenceCount() {
            return iSequenceCount;
        }

        /**
         * Returns the spectrum count
         *
         * @return the spectrum count
         */
        public Integer getISpectrumCount() {
            return iSpectrumCount;
        }

        /**
         * Returns the ion coverage
         *
         * @return the ion coverage
         */
        public Double getICoverage() {
            return iCoverage;
        }

        /**
         * Returns the protein length
         *
         * @return the protein length
         */
        public Integer getIProteinLength() {
            return iProteinLength;
        }

        /**
         * Returns the molecular weight
         *
         * @return the molecular weight
         */
        public Double getIMolecularWeight() {
            return iMolecularWeight;
        }

        /**
         * Returns the PI
         *
         * @return the PI
         */
        public Double getIPI() {
            return iPI;
        }

        /**
         * Returns the validation status
         *
         * @return the validation status
         */
        public String getIValidationStatus() {
            return iValidationStatus;
        }

        /**
         * Returns the description
         *
         * @return the description
         */
        public String getIDescription() {
            return iDescription;
        }

        /**
         * Returns the list of peptides
         *
         * @return list of peptides
         */
        public ArrayList<Peptide> getIPeptides() {
            return iPeptides;
        }

        /**
         * Returns the search engine
         *
         * @return the search engine
         */
        public String getISearchEngine() {
            return iSearchEngine;
        }

        /**
         * Returns the database
         *
         * @return the database
         */
        public String getIDatabase() {
            return iDatabase;
        }

        /**
         * Returns the database version
         *
         * @return the database version
         */
        public String getIDBVersion() {
            return iDBVersion;
        }

        /**
         * Sets the list of peptides. (This setter method is necessary.)
         * @param iPeptides the ArrayList<Peptide> of peptides to set.
         */
        public void setIPeptides(ArrayList<Peptide> iPeptides) {
            this.iPeptides = iPeptides;
        }

        /**
         * This method returns a PRIDE GelFreeIdentification based upon this instance.
         *
         * @return  GelFreeIdentification   with the PRIDE GelFreeIdentification object.
         */
        public uk.ac.ebi.pride.model.interfaces.core.Identification getGelFreeIdentification() {

            Collection<UserParam> userParams = null;
            Collection<CvParam> cvParams = null;

            if (properties.getDataSource().equalsIgnoreCase("Mascot DAT File")) {

                userParams = new ArrayList<UserParam>(1);
//                userParams.add(new UserParamImpl("ScoreCalculation", 0, "MeanOfPeptideScores"));
//                userParams.add(new UserParamImpl("ThresholdCalculation", 1, "MeanOfPeptideIdentityThresholds"));
//                userParams.add(new UserParamImpl("PeptideIdentification", 2, "AboveOrEqualToIdentityThreshold"));
                userParams.add(new UserParamImpl("MascotConfidenceLevel", 0, "" + properties.getMascotConfidenceLevel()));
            } else if (properties.getDataSource().equalsIgnoreCase("DTASelect")) {

                cvParams = new ArrayList<CvParam>(3);

                // Add here the information that is common for each protein:
                if (iDescription != null) {
                    cvParams.add(new CvParamImpl("PRIDE:0000063", "PRIDE", "Protein description line", 1, iDescription));
                    cvParams.add(new CvParamImpl("PRIDE:0000172", "PRIDE", "Search database protein sequence length", 2, iProteinLength.toString()));
                    cvParams.add(new CvParamImpl("PRIDE:0000057", "PRIDE", "Molecular Weight", 3, iMolecularWeight.toString()));
                }

                userParams = new ArrayList<UserParam>(4);
                userParams.add(new UserParamImpl("Sequence count", 1, iSequenceCount.toString()));
                userParams.add(new UserParamImpl("Spectrum count", 2, iSpectrumCount.toString()));
                userParams.add(new UserParamImpl("pI", 3, iPI.toString()));
                userParams.add(new UserParamImpl("Validation status", 4, iValidationStatus));
            }

            Double threshold;

            if (properties.getDataSource().equalsIgnoreCase("X!Tandem") ||
                    properties.getDataSource().equalsIgnoreCase("Spectrum Mill") ||
                    properties.getDataSource().equalsIgnoreCase("SEQUEST Result File") ||
                    properties.getDataSource().equalsIgnoreCase("OMSSA") ||
                    properties.getDataSource().equalsIgnoreCase("DTASelect")) {
                threshold = null;
            } else {
                threshold = this.getThreshold();
            }

            Double score;

            if (properties.getDataSource().equalsIgnoreCase("SEQUEST Result File") ||
                    properties.getDataSource().equalsIgnoreCase("OMSSA") ||
                    properties.getDataSource().equalsIgnoreCase("DTASelect")) {
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
                        cvParams, // cv params
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
                        cvParams, // cv params
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
     * New class needed to wrap the peptide information: It has to be identical to PeptideImple.
     * Otherwise we will get a CastException
     */
    public static class InnerPeptide {

        private Integer scanNumber = null;
        private String sequence = null;
        private Integer start = null;
        private Collection cvParams = null;
        private Collection userParams = null;
        private Collection modifications = null;

        /**
         * Construct an InnerPeptide object.
         *
         * @param scanNumber
         * @param sequence
         * @param aStart
         * @param aModifications
         * @param aCvParams
         * @param aUserParams
         */
        public InnerPeptide(Integer scanNumber, String sequence, Integer aStart, Collection aModifications,
                Collection aCvParams, Collection aUserParams) {
            this.scanNumber = scanNumber;
            this.sequence = sequence;
            this.start = aStart;
            this.modifications = aModifications;
            this.cvParams = aCvParams;
            this.userParams = aUserParams;
        }

        /**
         * Returns the list of CV parameters
         *
         * @return the list of CV parameters
         */
        public Collection getCvParams() {
            return cvParams;
        }

        /**
         * Sets the list of CV parameters
         *
         * @param aCvParams the list of CV parameters
         */
        public void setCvParams(Collection aCvParams) {
            this.cvParams = aCvParams;
        }

        /**
         * Returns the list of user parameters
         *
         * @return the list of user parameters
         */
        public Collection getUserParams() {
            return userParams;
        }

        /**
         * Sets the list of user parameters
         *
         * @param aUserParams the list of user parameters
         */
        public void setUserParams(Collection aUserParams) {
            this.userParams = aUserParams;
        }

        /**
         * Returns the scan number
         *
         * @return the scan number
         */
        public Integer getScanNumber() {
            return scanNumber;
        }

        /**
         * Sets the scan number
         *
         * @param scanNumber the scan number
         */
        public void setScanNumber(Integer scanNumber) {
            this.scanNumber = scanNumber;
        }

        /**
         * Returns the sequence
         *
         * @return the sequence
         */
        public String getSequence() {
            return sequence;
        }

        /**
         * Sets the sequence
         *
         * @param sequence the sequence
         */
        public void setSequence(String sequence) {
            this.sequence = sequence;
        }

        /**
         * Returns the peptide start index
         *
         * @return the peptide start index
         */
        public Integer getStartIndex() {
            return start;
        }

        /**
         * Sets the peptide start index
         *
         * @param start peptide start index
         */
        public void setStartIndex(Integer start) {
            this.start = start;
        }

        /**
         * Returns the modifications
         *
         * @return the modifications
         */
        public Collection getModifications() {
            return modifications;
        }

        /**
         * Sets the modifications
         *
         * @param modifications the modifications to set
         */
        public void setModifications(Collection modifications) {
            this.modifications = modifications;
        }
    }

    /**
     * Returns an ArrayList of the selected spectra in the ms_lims database.
     *
     * @return the selected spectra
     */
    protected static ArrayList getSelectedSpectraFromDatabase() {

        ArrayList<Spectrumfile> selectedSpectra = new ArrayList<Spectrumfile>();
        ArrayList<String> selectedFileNames = new ArrayList<String>();

        try {
            if (properties.getSelectedSpectraKeys().size() > 0) {

                progressDialog.setMax(properties.getSelectedSpectraKeys().size());

                for (int i = 0; i < properties.getSelectedSpectraKeys().size(); i++) {

                    if (isConversionCanceled()) {
                        outputFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                        outputFrame.setConvertButtonEnabled(true);
                        return null;
                    }

                    progressDialog.setValue(i);
                    selectedSpectra.add(Spectrumfile.getFromName(
                            (String) ((Object[]) properties.getSelectedSpectraKeys().get(i))[0], conn));
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

                                dbSpectra = Spectrumfile.getAllSpectraForProject(properties.getProjectIds().get(i),
                                        conn, "filename like '%" + tempToken + "%'");
                            } else {
                                Identification[] tempIdentifications = Identification.getAllIdentificationsforProject(
                                        conn, properties.getProjectIds().get(i), "identificationid = '" + tempToken + "'");

                                if (tempIdentifications.length > 0) {
                                    dbSpectra =
                                            Spectrumfile.getAllSpectraForProject(properties.getProjectIds().get(i),
                                            conn, "spectrumfileid = " + tempIdentifications[0].getL_spectrumfileid());
                                } else {
                                    dbSpectra = null;
                                }
                            }

                            if (dbSpectra != null) {

                                progressDialog.setMax(dbSpectra.length);

                                for (int j = 0; j < dbSpectra.length; j++) {
                                    if (isConversionCanceled()) {
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
                            dbSpectra = Spectrumfile.getAllSpectraForProject(properties.getProjectIds().get(i), conn);
                        } else { //selectAllIdentifiedSpectra
                            dbSpectra = Spectrumfile.getAllSpectraForProject(properties.getProjectIds().get(i),
                                    conn, "identified > 0");
                        }

                        progressDialog.setMax(dbSpectra.length);

                        for (int j = 0; j < dbSpectra.length; j++) {

                            if (isConversionCanceled()) {
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
     * @param mapping a hashmap of the spectrum ids mapped to the spectrum number
     *                in the xml file
     * @param aTransformedSpectra List of transformed Spectrum.
     * @return the mzData object
     */
    protected static MzData combineMzDataFiles(HashMap<String, Long> mapping, ArrayList<Spectrum> aTransformedSpectra) {

        // The CV lookup stuff. (NB: currently hardcoded to PSI only)
        Collection<CVLookup> cvLookups = new ArrayList<CVLookup>(1);
        cvLookups.add(new CVLookupImpl(properties.getCvVersion(),
                properties.getCvFullName(),
                properties.getCvLabel(),
                properties.getCvAddress()));

        // Instrument source CV parameters.
        Collection<CvParam> instrumentSourceCVParameters = new ArrayList<CvParam>(1);
        instrumentSourceCVParameters.add(new CvParamImpl(
                properties.getAccessionInstrumentSourceParameter(),
                properties.getCVLookupInstrumentSourceParameter(),
                properties.getNameInstrumentSourceParameter(),
                0,
                properties.getValueInstrumentSourceParameter()));

        // Instrument detector parameters.
        Collection<CvParam> instrumentDetectorParamaters = new ArrayList<CvParam>(1);
        instrumentDetectorParamaters.add(new CvParamImpl(
                properties.getAccessionInstrumentDetectorParamater(),
                properties.getCVLookupInstrumentDetectorParamater(),
                properties.getNameInstrumentDetectorParamater(),
                0,
                properties.getValueInstrumentDetectorParamater()));

        ArrayList<UserParam> sampleDescriptionUserParams = new ArrayList<UserParam>(
                properties.getSampleDescriptionUserSubSampleNames().size());

        for (int i = 0; i < properties.getSampleDescriptionUserSubSampleNames().size(); i++) {
            sampleDescriptionUserParams.add(new UserParamImpl(
                    "SUBSAMPLE_" + (i + 1),
                    i, (String) properties.getSampleDescriptionUserSubSampleNames().get(i)));
        }

        for (int i = 0; i < properties.getSampleDescriptionCVParamsQuantification().size(); i++) {
            properties.getSampleDescriptionCVParams().add(
                    properties.getSampleDescriptionCVParamsQuantification().get(i));
        }

        progressDialog.setString(null);
        progressDialog.setIntermidiate(true);

        totalNumberOfSpectra = 0;

        for (int j = 0; j < properties.getSelectedSourceFiles().size() && !isConversionCanceled(); j++) {

            String filePath = properties.getSelectedSourceFiles().get(j);
            currentFileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);

            progressDialog.setIntermidiate(true);
            progressDialog.setString(currentFileName + " (" + (j + 1) + "/" +
                    properties.getSelectedSourceFiles().size() + ")");

            try {
                MzDataXMLUnmarshaller unmarshallerMzData = new MzDataXMLUnmarshaller();
                MzData currentMzDataFile = unmarshallerMzData.unMarshall(new FileReader(filePath));

                for (Spectrum spectrum : currentMzDataFile.getSpectrumCollection()) {

                    totalNumberOfSpectra++;

                    Spectrum updatedSpectrum =
                            new SpectrumImpl(
                                    spectrum.getIntenArrayBinary(),
                                    spectrum.getMzRangeStart(),
                                    spectrum.getMzArrayBinary(),
                                    spectrum.getMsLevel(),
                                    spectrum.getSupDataArrayBinaryCollection(),
                                    spectrum.getMzRangeStop(),
                                    spectrum.getAcqSpecification(),
                                    totalNumberOfSpectra,
                                    spectrum.getPrecursorCollection(),
                                    spectrum.getSpectrumDescCommentCollection(),
                                    spectrum.getSpectrumInstrumentCvParameters(),
                                    spectrum.getSpectrumInstrumentUserParameters(),
                                    spectrum.getSupDataArrayCollection(),
                                    spectrum.getSupDescCollection());

                    aTransformedSpectra.add(updatedSpectrum);
                    String spectrum_key = new File(filePath).getName() + "_" + spectrum.getSpectrumId();
                    
                    // Store (spectrumKey, spectrumid) mapping.
                    Long xTmp = mapping.put(spectrum_key, (long) totalNumberOfSpectra);

                    if (xTmp != null) {
                        // we already stored a result for this ID!!!
                        JOptionPane.showMessageDialog(null, "Ambiguous spectrum mapping. Consult " +
                                "the error log file for details.", "Mapping Error", JOptionPane.ERROR_MESSAGE);
                        Util.writeToErrorLog("Ambiguous spectrum mapping for ID '" + spectrum.getSpectrumId()
                                + "' and spectrum file '" + new File(filePath).getName() + "'." );
                        PRIDEConverter.setCancelConversion(true);
                    }
                }
            } catch (FileNotFoundException e) {

                Util.writeToErrorLog("An error occured while trying to parse: " + filePath);
                e.printStackTrace();

                JOptionPane.showMessageDialog(null,
                        "An error occured while trying to parse: " + filePath + "\n\n" +
                        "See ../Properties/ErrorLog.txt for more details.\n",
                        "Parsing Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {

                Util.writeToErrorLog("An error occured while trying to parse: " + filePath);
                e.printStackTrace();

                JOptionPane.showMessageDialog(null,
                        "An error occured while trying to parse: " + filePath + "\n\n" +
                        "See ../Properties/ErrorLog.txt for more details.\n",
                        "Parsing Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        // Create the combinded mzData object.
        MzData tempMzData = new MzDataImpl(
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

        return tempMzData;
    }

    /**
     * Builds and returns the mzData object.
     *
     * @param mzDataSpectra List of Spectrum
     * @return the mzData object
     */
    private static MzData createMzData(ArrayList<Spectrum> mzDataSpectra) {

        // The CV lookup stuff. (NB: currently hardcoded to PSI only) // TODO: add more ontologies
        Collection<CVLookup> cvLookups = new ArrayList<CVLookup>(1);
        cvLookups.add(new CVLookupImpl(properties.getCvVersion(),
                properties.getCvFullName(),
                properties.getCvLabel(),
                properties.getCvAddress()));

        // Instrument source CV parameters.
        Collection<CvParam> instrumentSourceCVParameters = new ArrayList<CvParam>(1);
        instrumentSourceCVParameters.add(new CvParamImpl(
                properties.getAccessionInstrumentSourceParameter(),
                properties.getCVLookupInstrumentSourceParameter(),
                properties.getNameInstrumentSourceParameter(),
                0,
                properties.getValueInstrumentSourceParameter()));

        // Instrument detector parameters.
        Collection<CvParam> instrumentDetectorParamaters = new ArrayList<CvParam>(1);
        instrumentDetectorParamaters.add(new CvParamImpl(
                properties.getAccessionInstrumentDetectorParamater(),
                properties.getCVLookupInstrumentDetectorParamater(),
                properties.getNameInstrumentDetectorParamater(),
                0,
                properties.getValueInstrumentDetectorParamater()));

        ArrayList<UserParam> sampleDescriptionUserParams = new ArrayList<UserParam>(
                properties.getSampleDescriptionUserSubSampleNames().size());

        for (int i = 0; i < properties.getSampleDescriptionUserSubSampleNames().size(); i++) {
            sampleDescriptionUserParams.add(new UserParamImpl(
                    "SUBSAMPLE_" + (i + 1), i,
                    (String) properties.getSampleDescriptionUserSubSampleNames().get(i)));
        }

        for (int i = 0; i < properties.getSampleDescriptionCVParamsQuantification().size(); i++) {
            properties.getSampleDescriptionCVParams().add(
                    properties.getSampleDescriptionCVParamsQuantification().get(i));
        }

        // Create the mzData object.
        MzData tempMzData = new MzDataImpl(
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

        return tempMzData;
    }

    /**
     * Adds the correct iTRAQ CV terms to a CV param list.
     *
     * @param cvParams the CV param list to be extended
     * @param iTraqNorms the values to add
     */
    protected static void addItraqCVTerms(ArrayList<CvParam> cvParams, String[] iTraqNorms) {
        // ToDo: assumes input array size = 4  (never checks)
        // ToDo: add check!
        cvParams.add(new CvParamImpl("PRIDE:000018", "PRIDE", "iTRAQ intensity 114", cvParams.size(), iTraqNorms[0]));
        cvParams.add(new CvParamImpl("PRIDE:000019", "PRIDE", "iTRAQ intensity 115", cvParams.size(), iTraqNorms[1]));
        cvParams.add(new CvParamImpl("PRIDE:000020", "PRIDE", "iTRAQ intensity 116", cvParams.size(), iTraqNorms[2]));
        cvParams.add(new CvParamImpl("PRIDE:000021", "PRIDE", "iTRAQ intensity 117", cvParams.size(), iTraqNorms[3]));
    }

    /**
     * Adds the correct iTRAQ CV terms to a CV param list.
     *
     * @param cvParams the CV param list to be extended
     * @param iTRAQValues the values to add
     * @return the updated CV param list
     */
    protected static void addItraqCVTerms(ArrayList<CvParam> cvParams, iTRAQ iTRAQValues) {

        cvParams.add(new CvParamImpl(
                "PRIDE:000018", "PRIDE", "iTRAQ intensity 114", cvParams.size(),
                ((String[]) iTRAQValues.getAllNorms().get(0))[0]));
        cvParams.add(new CvParamImpl(
                "PRIDE:000019", "PRIDE", "iTRAQ intensity 115", cvParams.size(),
                ((String[]) iTRAQValues.getAllNorms().get(0))[1]));
        cvParams.add(new CvParamImpl(
                "PRIDE:000020", "PRIDE", "iTRAQ intensity 116", cvParams.size(),
                ((String[]) iTRAQValues.getAllNorms().get(0))[2]));
        cvParams.add(new CvParamImpl(
                "PRIDE:000021", "PRIDE", "iTRAQ intensity 117", cvParams.size(),
                ((String[]) iTRAQValues.getAllNorms().get(0))[3]));
    }

    /**
     * Adds the coeect iTraq terms to an arraylist and returns this list.
     *
     * @param iTRAQValues the values to add
     * @return the updated array list
     */
    protected static ArrayList<UserParam> addItraqUserTerms(iTRAQ iTRAQValues) {

        ArrayList<UserParam> userParams = new ArrayList<UserParam>(16);
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
    protected static ArrayList<UserParam> addItraqUserTerms(String[][] iTraqRatios) {
        ArrayList<UserParam> userParams = new ArrayList<UserParam>(16);
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

    /**
     * Generates the spectrum key by combining all the elements in the
     * provided array of objects. Each element is separated by '_'.
     *
     * @param subKeys the array of elements to combine
     * @return the generated spectrum key
     */
    protected static String generateSpectrumKey(Object[] subKeys) {

        spectrumKey = "";

        for (Object subKey : subKeys) {
            spectrumKey += subKey + "_";
        }

        spectrumKey = spectrumKey.substring(0, spectrumKey.length() - 1);

        return spectrumKey;
    }

    /**
     * Adds the user defined parameters (CV- and UserParams) to the SpectrumComments.
     *
     * @param spectrumComments the orginal spectrum comments
     * @param cvParams the user defined spectrum cv params
     * @param userParams the user defined spectrum user params
     * @return the updated list of spectrum comments
     */
    protected static Collection<SpectrumDescComment> addUserSpectrumComments(Collection<SpectrumDescComment> spectrumComments,
            ArrayList<CvParam> cvParams, ArrayList<UserParam> userParams) {

        //(NB: spectrum comments are simply text, not real CV- and UserParams

        // add user cv params
        if (cvParams != null) {
            for (CvParam cvParam : cvParams) {

                String comment = cvParam.getAccession() + " " + cvParam.getName();

                if (cvParam.getValue() != null) {
                    comment += ": " + cvParam.getValue();
                }

                spectrumComments.add(new SpectrumDescCommentImpl(comment));
            }
        }

        // add user user params
        if (userParams != null) {
            for (UserParam userParam : userParams) {
                String comment = userParam.getName() + ": " + userParam.getValue();
                spectrumComments.add(new SpectrumDescCommentImpl(comment));
            }
        }

        return spectrumComments;
    }

    /**
     * Returns the fragment ion cv terms. Note: this method contains some hard coding of CV terms.
     *
     * NB: the fragment ion's m/z value has to be reported as the observed m/z value and
     * not the theoretical m/z value in order for the annotation to be detected in PRIDE.
     *
     * @param fragmentIonMappedDetails containing fragment ion type (name and accession)
     * @param mzValue observed fragment ion m/z value (NB: <B>observed m/z</b> not theoretical m/z)
     * @param charge can be null, the charge of the ion.
     * @param fragmentIonNumber the number of the fragment ion.
     * @param intensity the ion intensity.
     * @param massError can be null
     * @param retentionTimeError can be null
     * @return the fragment ion cv terms
     */
    protected static ArrayList<CvParam> createFragmentIonCvParams(FragmentIonMappedDetails fragmentIonMappedDetails,
            Double mzValue, Integer charge, Integer fragmentIonNumber, Double intensity, Double massError,
            Double retentionTimeError) {

        ArrayList<CvParam> currentCvParams = new ArrayList<CvParam>();

        int orderIndex = 0;

        // add fragment ion type and fragment ion number
        currentCvParams.add(new CvParamImpl(
                fragmentIonMappedDetails.getCvParamAccession(),
                "PRIDE",
                fragmentIonMappedDetails.getName(),
                orderIndex++,
                fragmentIonNumber.toString()));

        // add fragment ion mz value
        currentCvParams.add(new CvParamImpl(
                "PRIDE:0000188",
                "PRIDE",
                "product ion m/z",
                orderIndex++,
                mzValue.toString()));

        // add fragment ion intensity
        currentCvParams.add(new CvParamImpl(
                "PRIDE:0000189",
                "PRIDE",
                "product ion intensity",
                orderIndex++,
                intensity.toString()));

        // add fragment ion charge
        if (charge != null) {
            currentCvParams.add(new CvParamImpl(
                    "PRIDE:0000204",
                    "PRIDE",
                    "product ion charge",
                    orderIndex++,
                    charge.toString()));
        }

        // add fragment ion mass error
        if (massError != null) {
            currentCvParams.add(new CvParamImpl(
                    "PRIDE:0000190",
                    "PRIDE",
                    "product ion mass error",
                    orderIndex++,
                    massError.toString()));
        }

        // add fragment ion retention time error
        if (retentionTimeError != null) {
            currentCvParams.add(new CvParamImpl(
                    "PRIDE:0000191",
                    "PRIDE",
                    "product ion retention time error",
                    orderIndex,
                    retentionTimeError.toString()));
        }

        return currentCvParams;
    }
}
