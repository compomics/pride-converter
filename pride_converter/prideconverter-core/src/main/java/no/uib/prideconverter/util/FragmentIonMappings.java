package no.uib.prideconverter.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;

/**
 * Takes care of saving and retrieving the fragmentat ion ion CV mappings.
 *
 * @author  Harald Barsnes
 * 
 * Created July 2009
 */
public class FragmentIonMappings {

    /**
     * Contains the fragment ion CV term mappings.
     * Key: name used in data format.
     * Value: CV term name and charge (separated by |). Note: charge could be empty.
     */
    private HashMap<String, FragmentIonMappedDetails> cvTermMappings;

    /**
     * Creates a new UserProperties object
     * 
     */
    public FragmentIonMappings() {
        cvTermMappings = new HashMap<String, FragmentIonMappedDetails>();
    }

    /**
     * Tries to read the user properties from file.
     */
    public void readFragmentIonMappingsFromFile() {

        cvTermMappings = new HashMap<String, FragmentIonMappedDetails>();

        try {
            String path = "" + this.getClass().getProtectionDomain().getCodeSource().getLocation();
            path = path.substring(5, path.lastIndexOf("/"));
            path = path.substring(0, path.lastIndexOf("/") + 1) + "Properties/FragmentIonMappings.prop";
            path = path.replace("%20", " ");

            File file = new File(path);

            FileReader f = new FileReader(file);
            BufferedReader b = new BufferedReader(f);
            String s = b.readLine(); // header

            StringTokenizer tok;
            s = b.readLine();

            while(s != null){

                if(!s.startsWith("#")){
                    tok = new StringTokenizer(s,"|");

                    String key = tok.nextToken();
                    String name = tok.nextToken();
                    String cvParamAccession = tok.nextToken();
                    String chargeAsString = tok.nextToken();
                    Integer charge = null;

                    if(!chargeAsString.equalsIgnoreCase("null")){
                        charge = new Integer(chargeAsString.trim());
                    }

                    cvTermMappings.put(key, new FragmentIonMappedDetails(name, cvParamAccession, charge));
                }

                s = b.readLine();
            }


            b.close();
            f.close();


        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error reading the FragmentIonMappings. " +
                    "See ../Properties/ErrorLog.txt for more details.", "File Not Found",
                    JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("FragmentIonMappings: ");
            ex.printStackTrace();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "Errorreading the FragmentIonMappings. " +
                    "See ../Properties/ErrorLog.txt for more details.", "File Error",
                    JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("FragmentIonMappings: ");
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error reading the FragmentIonMappings. " +
                    "See ../Properties/ErrorLog.txt for more details.", "File Error",
                    JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("FragmentIonMappings: ");
            ex.printStackTrace();
        }
    }

    /**
     * Tries to save the FragmentIonMappings to file.
     */
    public void saveFragmentIonMappingsToFile() {

        try {
            String path = "" + this.getClass().getProtectionDomain().getCodeSource().getLocation();
            path = path.substring(5, path.lastIndexOf("/"));
            path = path.substring(0, path.lastIndexOf("/") + 1) + "Properties/FragmentIonMappings.prop";
            path = path.replace("%20", " ");

            File file = new File(path);
            file.getAbsolutePath();
            FileWriter f = new FileWriter(file);

            f.write("#dataformat_name|cv_term_name|charge\n");

            Iterator<String> iterator = cvTermMappings.keySet().iterator();

            while(iterator.hasNext()){
                String currentKey = iterator.next();
                f.write(currentKey + "|" + 
                        cvTermMappings.get(currentKey).getName() + "|" +
                        cvTermMappings.get(currentKey).getCvParamAccession() + "|" +
                        cvTermMappings.get(currentKey).getCharge() + "\n");
            }

            f.close();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error when saving FragmentIonMappings. " +
                    "See ../Properties/ErrorLog.txt for more details.", "File Error",
                    JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("FragmentIonMappings: ");
            ex.printStackTrace();
        }
    }

    /**
     * Set the cv term mappings
     * 
     * @param cvTermMappings
     */
    public void setCVTermMappings(HashMap<String, FragmentIonMappedDetails> cvTermMappings) {
        this.cvTermMappings = cvTermMappings;
    }

    /**
     * Returns the cv term mappings
     * 
     * @return the cv term mappings
     */
    public HashMap<String, FragmentIonMappedDetails> getCVTermMappings() {
        return cvTermMappings;
    }
}
