package no.uib.prideconverter.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import no.uib.prideconverter.PRIDEConverter;
import uk.ac.ebi.pride.model.implementation.mzData.CvParamImpl;

/**
 * Takes care of saving and retrieving the user properites.
 *
 * @author  Harald Barsnes
 * 
 * Created March 2008
 */
public class UserProperties {

    // defaults
    private String outputPath = "user.home";
    private String sourceFileLocation = "";
    private String userName = ""; //database user name
    private String serverHost = "localhost"; //database serverhost
    private String schema = "ms_lims_6"; //database schema
    private String lastSelectedOntology = "Mass Spectroscopy CV (PSI-MS) [PSI]";
    private String lastSelectedSampleOntology = "NEWT UniProt Taxonomy Database [NEWT]";
    private String currentSelectedInstrument = "";
    private String currentSampleSet = "";
    private String currentProtocol = "";
    private String currentContact = "";
    private String fileNameSelectionCriteriaSeparator = ",";
    private double peakIntegrationRangeLower = -0.05;
    private double peakIntegrationRangeUpper = 0.05;
    private double reporterIonIntensityThreshold = 0.0;
    private double[] purityCorrections = {0, 1, 5.90, 2, 0, 2, 5.6, 0.1, 0, 3, 4.5, 0.1, 0.1, 4, 3.5, 0.1};
    private PRIDEConverter prideConverter;
    private HashMap cvTermMappings;
    private String omssaInstallDir = null;

    /**
     * Creates a new UserProperties object
     * 
     * @param prideConverter
     */
    public UserProperties(PRIDEConverter prideConverter) {
        this.prideConverter = prideConverter;
        cvTermMappings = new HashMap();
    }

    /**
     * Tries to read the user properties from file.
     */
    public void readUserPropertiesFromFile(File settingsFile) {

        boolean importOldProperties = false;

        try {
            String path;
            File file;

            //makes it possible to run the program within netbeans
            if (prideConverter.useHardcodedPaths()) {
                path = "D:/PRIDE_ms_lims/ms_lims_to_PRIDE/PRIDEConverter/Release/Properties/UserProperties.prop";
            } else {
                path = "" + this.getClass().getProtectionDomain().getCodeSource().getLocation();
                path = path.substring(5, path.lastIndexOf("/"));
                path = path.substring(0, path.lastIndexOf("/") + 1) + "Properties/UserProperties.prop";
                path = path.replace("%20", " ");
            }

            // use the default settings file
            if (settingsFile == null) {
                file = new File(path);
            } else {
                file = settingsFile;
            }

            FileReader f = new FileReader(file);
            BufferedReader b = new BufferedReader(f);
            String s = b.readLine(); // header
            String version = b.readLine(); // version

            // see if the userproperties file is the (empty) default one. if it is 
            // then ask the user if he/she wants to import the old user settings.
            if (version.endsWith("*")) {

                int option = JOptionPane.showConfirmDialog(null,
                        "Are you upgrading from an older version of PRIDE Converter?",
                        "Upgrading PRIDE Converter?", JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION) {

                    option = JOptionPane.showConfirmDialog(null,
                            "Do you want to import the settings from the previous version?",
                            "Import Settings?", JOptionPane.YES_NO_OPTION);

                    if (option == JOptionPane.YES_OPTION) {
                        importOldProperties = true;
                    }
                }

                // removes the '*' at the end of the version number
                version = version.substring(0, version.length() - 1);
            }

            s = b.readLine();
            outputPath = s.substring(s.indexOf(": ") + 2);
            s = b.readLine();
            userName = s.substring(s.indexOf(": ") + 2);
            s = b.readLine();
            serverHost = s.substring(s.indexOf(": ") + 2);
            s = b.readLine();
            schema = s.substring(s.indexOf(": ") + 2);
            s = b.readLine();
            lastSelectedOntology = s.substring(s.indexOf(": ") + 2);
            s = b.readLine();
            lastSelectedSampleOntology = s.substring(s.indexOf(": ") + 2);
            s = b.readLine();
            currentSelectedInstrument = s.substring(s.indexOf(": ") + 2);
            s = b.readLine();
            currentSampleSet = s.substring(s.indexOf(": ") + 2);
            s = b.readLine();
            currentProtocol = s.substring(s.indexOf(": ") + 2);
            s = b.readLine();
            currentContact = s.substring(s.indexOf(": ") + 2);

            if (version.equalsIgnoreCase("v1.0") ||
                    version.equalsIgnoreCase("v1.1") ||
                    version.equalsIgnoreCase("v1.2") ||
                    version.equalsIgnoreCase("v1.3") ||
                    version.equalsIgnoreCase("v1.4") ||
                    version.equalsIgnoreCase("v1.5") ||
                    version.equalsIgnoreCase("v1.6") ||
                    version.equalsIgnoreCase("v1.7") ||
                    version.equalsIgnoreCase("v1.8") ||
                    version.equalsIgnoreCase("v1.8_beta") ||
                    version.equalsIgnoreCase("v1.9") ||
                    version.equalsIgnoreCase("v1.9_beta") ||
                    version.equalsIgnoreCase("v1.9.1") ||
                    version.equalsIgnoreCase("v1.10") ||
                    version.equalsIgnoreCase("v1.10.1") ||
                    version.equalsIgnoreCase("v1.11") ||
                    version.equalsIgnoreCase("v1.11.1") ||
                    version.equalsIgnoreCase("v1.11.2") ||
                    version.equalsIgnoreCase("v1.11.3") ||
                    version.equalsIgnoreCase("v1.11.4") ||
                    version.equalsIgnoreCase("v1.12") ||
                    version.equalsIgnoreCase("v1.13") ||
                    version.equalsIgnoreCase("v1.13.1") ||
                    version.equalsIgnoreCase("v1.13.2") ||
                    version.equalsIgnoreCase("v1.13.3") ||
                    version.equalsIgnoreCase("v1.14") ||
                    version.equalsIgnoreCase("v1.14.1") ||
                    version.equalsIgnoreCase("v1.14.2")) {
                s = b.readLine();
                fileNameSelectionCriteriaSeparator = s.substring(s.indexOf(": ") +2);
                s = b.readLine();
                sourceFileLocation = s.substring(s.indexOf(": ") + 2);
            } else {
                fileNameSelectionCriteriaSeparator = ",";
                sourceFileLocation = "";
            }

            if (version.equalsIgnoreCase("v1.6") ||
                    version.equalsIgnoreCase("v1.7") ||
                    version.equalsIgnoreCase("v1.8") ||
                    version.equalsIgnoreCase("v1.8_beta") ||
                    version.equalsIgnoreCase("v1.9") ||
                    version.equalsIgnoreCase("v1.9_beta") ||
                    version.equalsIgnoreCase("v1.9.1") ||
                    version.equalsIgnoreCase("v1.10") ||
                    version.equalsIgnoreCase("v1.10.1") ||
                    version.equalsIgnoreCase("v1.11") ||
                    version.equalsIgnoreCase("v1.11.1") ||
                    version.equalsIgnoreCase("v1.11.2") ||
                    version.equalsIgnoreCase("v1.11.3") ||
                    version.equalsIgnoreCase("v1.11.4") ||
                    version.equalsIgnoreCase("v1.12") ||
                    version.equalsIgnoreCase("v1.13") ||
                    version.equalsIgnoreCase("v1.13.1") ||
                    version.equalsIgnoreCase("v1.13.2") ||
                    version.equalsIgnoreCase("v1.13.3") ||
                    version.equalsIgnoreCase("v1.14") ||
                    version.equalsIgnoreCase("v1.14.1") ||
                    version.equalsIgnoreCase("v1.14.2")) {
                
                // read the iTRAQ settings values
                s = b.readLine();
                peakIntegrationRangeLower = new Double(s.substring(s.indexOf(": ") + 2)).doubleValue();
                s = b.readLine();
                peakIntegrationRangeUpper = new Double(s.substring(s.indexOf(": ") + 2)).doubleValue();
                s = b.readLine();
                reporterIonIntensityThreshold = new Double(s.substring(s.indexOf(": ") + 2)).doubleValue();

                s = b.readLine();
                StringTokenizer tok = new StringTokenizer(s.substring(s.indexOf(": ") + 2), ",");

                int purityCorrectionsCounter = 0;

                while (tok.hasMoreTokens()) {
                    purityCorrections[purityCorrectionsCounter] = new Double(tok.nextToken()).doubleValue();
                    purityCorrectionsCounter++;
                }
            }

            if (version.equalsIgnoreCase("v1.10") ||
                    version.equalsIgnoreCase("v1.10.1") ||
                    version.equalsIgnoreCase("v1.11") ||
                    version.equalsIgnoreCase("v1.11.1") ||
                    version.equalsIgnoreCase("v1.11.2") ||
                    version.equalsIgnoreCase("v1.11.3") ||
                    version.equalsIgnoreCase("v1.11.4") ||
                    version.equalsIgnoreCase("v1.12") ||
                    version.equalsIgnoreCase("v1.13") ||
                    version.equalsIgnoreCase("v1.13.1") ||
                    version.equalsIgnoreCase("v1.13.2") ||
                    version.equalsIgnoreCase("v1.13.3") ||
                    version.equalsIgnoreCase("v1.14") ||
                    version.equalsIgnoreCase("v1.14.1") ||
                    version.equalsIgnoreCase("v1.14.2")) {
                
                s = b.readLine();
                omssaInstallDir = s.substring(s.indexOf(": ") + 2);

                if (omssaInstallDir.equalsIgnoreCase("null")) {
                    omssaInstallDir = null;
                }
            } else {
                omssaInstallDir = null;
            }

            if (version.equalsIgnoreCase("v1.8") ||
                    version.equalsIgnoreCase("v1.8_beta") ||
                    version.equalsIgnoreCase("v1.9") ||
                    version.equalsIgnoreCase("v1.9_beta") ||
                    version.equalsIgnoreCase("v1.9.1") ||
                    version.equalsIgnoreCase("v1.10") ||
                    version.equalsIgnoreCase("v1.10.1") ||
                    version.equalsIgnoreCase("v1.11") ||
                    version.equalsIgnoreCase("v1.11.1") ||
                    version.equalsIgnoreCase("v1.11.2") ||
                    version.equalsIgnoreCase("v1.11.3") ||
                    version.equalsIgnoreCase("v1.11.4") ||
                    version.equalsIgnoreCase("v1.12") ||
                    version.equalsIgnoreCase("v1.13") ||
                    version.equalsIgnoreCase("v1.13.1") ||
                    version.equalsIgnoreCase("v1.13.2") ||
                    version.equalsIgnoreCase("v1.13.3") ||
                    version.equalsIgnoreCase("v1.14") ||
                    version.equalsIgnoreCase("v1.14.1") ||
                    version.equalsIgnoreCase("v1.14.2")) {

                s = b.readLine();
                s = b.readLine();

                // read the CV term mappings
                while (s != null) {
                    StringTokenizer tok = new StringTokenizer(s, "|");

                    String key = tok.nextToken();
                    String accession = tok.nextToken();
                    String ontology = tok.nextToken();
                    String name = tok.nextToken();
                    String value = null;

                    if (tok.hasMoreTokens()) {
                        value = tok.nextToken();

                        if (value.equalsIgnoreCase("null")) {
                            value = null;
                        }
                    }

                    cvTermMappings.put(key, new CvParamImpl(accession, ontology, name, 0, value));

                    s = b.readLine();
                }
            }

            b.close();
            f.close();

            // import old user settings
            if (importOldProperties) {

                path = "" + this.getClass().getProtectionDomain().getCodeSource().getLocation();
                path = path.substring(5, path.lastIndexOf("/"));
                path = path.substring(0, path.lastIndexOf("/"));
                path = path.replace("%20", " ");

                String oldUserPropertiesLocation =
                        "" + this.getClass().getProtectionDomain().getCodeSource().getLocation();
                oldUserPropertiesLocation =
                        oldUserPropertiesLocation.substring(5, oldUserPropertiesLocation.lastIndexOf("/"));
                oldUserPropertiesLocation =
                        oldUserPropertiesLocation.substring(0, oldUserPropertiesLocation.lastIndexOf("/") +
                        1) + "Properties/UserProperties.prop";
                oldUserPropertiesLocation = oldUserPropertiesLocation.replace("%20", " ");

                JFileChooser chooser = new JFileChooser();

                int option = JOptionPane.showConfirmDialog(null,
                        "Please locate the old settings file 'UserProperties.prop'.\n" +
                        "It is in the Properties folder of the previous installation.",
                        "Locate Old Settings File", JOptionPane.OK_CANCEL_OPTION);

                if (option == JOptionPane.OK_OPTION) {

                    chooser.setDialogTitle("Select Old Settings File 'UserProperties.prop'");

                    int returnVal = chooser.showOpenDialog(null);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {

                        File selectedFile = chooser.getSelectedFile();
                        FileReader fr = new FileReader(selectedFile);
                        BufferedReader br = new BufferedReader(fr);
                        String firstLine = br.readLine();
                        br.close();
                        fr.close();

                        boolean cancel = false;

                        while ((!selectedFile.getName().equalsIgnoreCase("UserProperties.prop") ||
                                (new File(oldUserPropertiesLocation).equals(selectedFile)) ||
                                !firstLine.equalsIgnoreCase("PRIDEConverter")) &&
                                !cancel) {

                            if (!selectedFile.getName().equalsIgnoreCase("UserProperties.prop")) {

                                option = JOptionPane.showConfirmDialog(null,
                                        "The selected file is not 'UserProperties.prop'.\n" +
                                        "Please select the file named 'UserProperties.prop' in the Properties folder.",
                                        "Locate Old Settings File", JOptionPane.OK_CANCEL_OPTION);
                            } else if (new File(oldUserPropertiesLocation).equals(selectedFile)) {
                                //trying to upgrade from downloaded UserProperties file
                                option = JOptionPane.showConfirmDialog(null,
                                        "It seems like you are trying to upgrade from the wrong UserProperties file.\n" +
                                        "Please select the file named 'UserProperties.prop' in the Properties folder \n" +
                                        "of the previous installation of PRIDE Converter.",
                                        "Wrong UserProperties File", JOptionPane.OK_CANCEL_OPTION);
                            } else {
                                option = JOptionPane.showConfirmDialog(null,
                                        "The selected file is not a PRIDE Converter 'UserProperties.prop' file.\n" +
                                        "Please select the file named 'UserProperties.prop' in the PRIDE Converter's\n" +
                                        "Properties folder.",
                                        "Locate Old Settings File", JOptionPane.OK_CANCEL_OPTION);
                            }

                            if (option == JOptionPane.CANCEL_OPTION) {
                                cancel = true;
                            } else {

                                returnVal = chooser.showOpenDialog(null);

                                if (returnVal == JFileChooser.APPROVE_OPTION) {
                                    selectedFile = chooser.getSelectedFile();
                                    fr = new FileReader(selectedFile);
                                    br = new BufferedReader(fr);
                                    firstLine = br.readLine();
                                    br.close();
                                    fr.close();
                                } else {
                                    cancel = true;
                                }
                            }
                        }

                        if (!cancel) {

                            //copy the instrumens, contacts, protocols and samples
                            File propertiesFolder = selectedFile.getParentFile();

                            File contactsFolder = new File(propertiesFolder + "/Contacts/");
                            File instrumentsFolder = new File(propertiesFolder + "/Instruments/");
                            File protocolsFolder = new File(propertiesFolder + "/Protocols/");
                            File samplesFolder = new File(propertiesFolder + "/Samples/");

                            // the contacts folder is not included in the downloaded zip file
                            // so we have to create it
                            if(!new File(path + "/Properties/Contacts/").exists()){
                                new File(path + "/Properties/Contacts/").mkdir();
                            }

                            File[] files = contactsFolder.listFiles();

                            for (int i = 0; i < files.length; i++) {
                                if (files[i].getName().endsWith(".con")) {
                                    copyFile(files[i], new File(path + "/Properties/Contacts/" + files[i].getName()));
                                }
                            }

                            files = instrumentsFolder.listFiles();

                            for (int i = 0; i < files.length; i++) {
                                if (files[i].getName().endsWith(".int")) {
                                    copyFile(files[i], new File(path + "/Properties/Instruments/" + files[i].getName()));
                                }
                            }

                            files = protocolsFolder.listFiles();

                            for (int i = 0; i < files.length; i++) {
                                if (files[i].getName().endsWith(".pro")) {
                                    copyFile(files[i], new File(path + "/Properties/Protocols/" + files[i].getName()));
                                }
                            }

                            files = samplesFolder.listFiles();

                            for (int i = 0; i < files.length; i++) {
                                if (files[i].getName().endsWith(".sam")) {
                                    copyFile(files[i], new File(path + "/Properties/Samples/" + files[i].getName()));
                                }
                            }

                            //copy the old UserProperties.prop file
                            readUserPropertiesFromFile(selectedFile);
                        }
                    }
                }
            }

            if (settingsFile != null) {
                JOptionPane.showMessageDialog(null,
                        "The settings from the previous version has been successfully imported.",
                        "Settings Imported", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Error when reading the user properties. " +
                    "See ../Properties/ErrorLog.txt for more details.", "File Not Found", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("UserProperties: ");
            ex.printStackTrace();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error when reading the user properties. " +
                    "See ../Properties/ErrorLog.txt for more details.", "File Error", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("UserProperties: ");
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(null, "Error when reading the user properties. " +
                    "See ../Properties/ErrorLog.txt for more details.", "File Error", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("UserProperties: ");
            ex.printStackTrace();
        }
    }

    /**
     * Copies the selected file to a new location.
     * 
     * @param fromFile the file to copy
     * @param toFile the location of the new file
     */
    private void copyFile(File fromFile, File toFile) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(fromFile));
            BufferedWriter bw = new BufferedWriter(new FileWriter(toFile));

            String s;

            while (br.ready()) {
                s = br.readLine();
                bw.write(s);
                bw.newLine();
            }

            br.close();
            bw.close();

        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Error when importing old user settings. " +
                    "See ../Properties/ErrorLog.txt for more details.", "File Not Found", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("UserProperties: ");
            ex.printStackTrace();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error when importing old user settings. " +
                    "See ../Properties/ErrorLog.txt for more details.", "File Error", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("UserProperties: ");
            ex.printStackTrace();
        }
    }

    /**
     * Tries to save the user properties to file.
     */
    public void saveUserPropertiesToFile() {
        try {

            String path;

            if (prideConverter.useHardcodedPaths()) {
                path = "D:/PRIDE_ms_lims/ms_lims_to_PRIDE/PRIDEConverter/Release/Properties/UserProperties.prop";
            } else {

                path = "" + this.getClass().getProtectionDomain().getCodeSource().getLocation();
                path = path.substring(5, path.lastIndexOf("/"));
                path = path.substring(0, path.lastIndexOf("/") + 1) +"Properties/UserProperties.prop";
                path = path.replace("%20", " ");
            }

            File file = new File(path);
            file.getAbsolutePath();
            FileWriter f = new FileWriter(file);

            f.write("PRIDEConverter\n");
            f.write(prideConverter.getPrideConverterVersionNumber() + "\n");
            f.write("OutputPath: " + outputPath + "\n");
            f.write("UserName: " + userName + "\n");
            f.write("ServerHost: " + serverHost + "\n");
            f.write("Schema: " + schema + "\n");
            f.write("LastSelectedOntology: " + lastSelectedOntology + "\n");
            f.write("LastSelectedSampleOntology: " + lastSelectedSampleOntology +"\n");
            f.write("CurrentSelectedInstrument: " + currentSelectedInstrument +"\n");
            f.write("CurrentSampleSet: " + currentSampleSet + "\n");
            f.write("CurrentProtocol: " + currentProtocol + "\n");
            f.write("CurrentContact: " + currentContact + "\n");
            f.write("FileNameSelectionCriteriaSeparator: " +fileNameSelectionCriteriaSeparator + "\n");
            f.write("SourceFileLocation: " + sourceFileLocation + "\n");
            f.write("PeakIntegrationRangeLower: " + peakIntegrationRangeLower +"\n");
            f.write("PeakIntegrationRangeUpper: " + peakIntegrationRangeUpper +"\n");
            f.write("ReporterIonIntensityThreshold: " +reporterIonIntensityThreshold + "\n");

            String temp = "";

            for (int i = 0; i <purityCorrections.length - 1; i++) {
                temp += purityCorrections[i] + ",";
            }

            temp += purityCorrections[purityCorrections.length - 1];

            f.write("PurityCorrections: " + temp + "\n");
            f.write("OmssaInstallDir: " + omssaInstallDir + "\n");
            f.write("CVTermMappings:");

            String key;

            Iterator iter = cvTermMappings.keySet().iterator();

            while (iter.hasNext()) {

                key = (String) iter.next();

                f.write("\n" + key + "|");
                f.write(((CvParamImpl) cvTermMappings.get(key)).getAccession() +"|");
                f.write(((CvParamImpl) cvTermMappings.get(key)).getCVLookup() +"|");
                f.write(((CvParamImpl) cvTermMappings.get(key)).getName() + "|");
                f.write(((CvParamImpl) cvTermMappings.get(key)).getValue() + "|");
            }

            f.close();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error when saving the user properties. " +
                    "See the ErrorLog for more details.", "File Error", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("UserProperties: ");
            ex.printStackTrace();
        }
    }

    /**
     * Set the OMSSA install folder
     * 
     * @param omssaInstallDir
     */
    public void setOmssaInstallDir(String omssaInstallDir) {
        this.omssaInstallDir = omssaInstallDir;
    }

    /**
     * Returns the OMSSA install folder 
     * 
     * @return the omssaInstallDir
     */
    public String getOmssaInstallDir() {
        return omssaInstallDir;
    }

    /**
     * Set the cv term mappings
     * 
     * @param cvTermMappings
     */
    public void setCVTermMappings(HashMap cvTermMappings) {
        this.cvTermMappings = cvTermMappings;
    }

    /**
     * Returns the cv term mappings
     * 
     * @return the cv term mappings
     */
    public HashMap getCVTermMappings() {
        return cvTermMappings;
    }

    /**
     * Set the output path
     * 
     * @param path
     */
    public void setOutputPath(String path) {
        outputPath = path;
    }

    /**
     * Returns the output path
     * 
     * @return the output path
     */
    public String getOutputPath() {
        return outputPath;
    }

    /**
     * Set the user name
     * 
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns the user name 
     * 
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the server host
     * 
     * @param serverHost
     */
    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    /**
     * Returne the name of server host
     * 
     * @return the name of server host
     */
    public String getServerHost() {
        return serverHost;
    }

    /**
     * Set the schema
     * 
     * @param schema
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Returns the name of the schema
     * 
     * @return the name of the schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Set the last selected ontology
     * 
     * @param lastSelectedOntology
     */
    public void setLastSelectedOntology(String lastSelectedOntology) {
        this.lastSelectedOntology = lastSelectedOntology;
    }

    /**
     * Returns the last selected ontology
     * 
     * @return the last selected ontology
     */
    public String getLastSelectedOntology() {
        return lastSelectedOntology;
    }

    /**
     * Set the last selected sample ontology
     * 
     * @param lastSelectedSampleOntology
     */
    public void setLastSelectedSampleOntology(String lastSelectedSampleOntology) {
        this.lastSelectedSampleOntology = lastSelectedSampleOntology;
    }

    /**
     * Return the last selectee sample ontology
     * 
     * @return the last selectee sample ontology
     */
    public String getLastSelectedSampleOntology() {
        return lastSelectedSampleOntology;
    }

    /**
     * Set the current selected intrument
     * 
     * @param currentSelectedInstrument
     */
    public void setCurrentSelectedInstrument(String currentSelectedInstrument) {
        this.currentSelectedInstrument = currentSelectedInstrument;
    }

    /**
     * Return the current selected instrument
     * 
     * @return the current selected instrument
     */
    public String getCurrentSelectedInstrument() {
        return currentSelectedInstrument;
    }

    /**
     * Set the current sample set
     * 
     * @param currentSampleSet
     */
    public void setCurrentSampleSet(String currentSampleSet) {
        this.currentSampleSet = currentSampleSet;
    }

    /**
     * Return the current sample set
     * 
     * @return the current sample set
     */
    public String getCurrentSampleSet() {
        return currentSampleSet;
    }

    /**
     * Set the current protocol
     * 
     * @param currentProtocol
     */
    public void setCurrentProtocol(String currentProtocol) {
        this.currentProtocol = currentProtocol;
    }

    /**
     * Return the current protocol
     * 
     * @return the current protocol
     */
    public String getCurrentProtocol() {
        return currentProtocol;
    }

    /**
     * Set the current contact
     * 
     * @param currentContact
     */
    public void setCurrentContact(String currentContact) {
        this.currentContact = currentContact;
    }

    /**
     * Return the current contact
     * 
     * @return the current contact
     */
    public String getCurrentContact() {
        return currentContact;
    }

    /**
     * Set the current fileNameSelectionCriteriaSeparator
     * 
     * @param fileNameSelectionCriteriaSeparator
     */
    public void setCurrentFileNameSelectionCriteriaSeparator(String fileNameSelectionCriteriaSeparator) {
        this.fileNameSelectionCriteriaSeparator = fileNameSelectionCriteriaSeparator;
    }

    /**
     * Return the current fileNameSelectionCriteriaSeparator
     * 
     * @return the current fileNameSelectionCriteriaSeparator
     */
    public String getCurrentFileNameSelectionCriteriaSeparator() {
        return fileNameSelectionCriteriaSeparator;
    }

    /**
     * Set the current SourceFileLocation
     * 
     * @param sourceFileLocation
     */
    public void setSourceFileLocation(String sourceFileLocation) {
        this.sourceFileLocation = sourceFileLocation;
    }

    /**
     * Return the current SourceFileLocation
     * 
     * @return the current sourceFileLocation
     */
    public String getCurrentSourceFileLocation() {
        return sourceFileLocation;
    }

    /**
     * Returns the lower peak integration range
     * 
     * @return the lower peak integration range
     */
    public double getPeakIntegrationRangeLower() {
        return peakIntegrationRangeLower;
    }

    /**
     * Sets the lower peak integration range
     * 
     * @param peakIntegrationRangeLower
     */
    public void setPeakIntegrationRangeLower(double peakIntegrationRangeLower) {
        this.peakIntegrationRangeLower = peakIntegrationRangeLower;
    }

    /**
     * Returns the upper peak integration range
     * 
     * @return the upper peak integration range
     */
    public double getPeakIntegrationRangeUpper() {
        return peakIntegrationRangeUpper;
    }

    /**
     * Sets the upper peak integration range
     * 
     * @param peakIntegrationRangeUpper
     */
    public void setPeakIntegrationRangeUpper(double peakIntegrationRangeUpper) {
        this.peakIntegrationRangeUpper = peakIntegrationRangeUpper;
    }

    /**
     * Returns the reporter ion intensity threshold
     * 
     * @return the reporter ion intensity threshold
     */
    public double getReporterIonIntensityThreshold() {
        return reporterIonIntensityThreshold;
    }

    /**
     * Sets the reporter ion intensity threshold
     * 
     * @param reporterIonIntensityThreshold
     */
    public void setReporterIonIntensityThreshold(double reporterIonIntensityThreshold) {
        this.reporterIonIntensityThreshold = reporterIonIntensityThreshold;
    }

    /**
     * Returns the purity corrections
     * 
     * @return the purity corrections
     */
    public double[] getPurityCorrections() {
        return purityCorrections;
    }

    /**
     * Sets the purity corrections
     * 
     * @param purityCorrections
     */
    public void setPurityCorrections(double[] purityCorrections) {
        this.purityCorrections = purityCorrections;
    }
}
