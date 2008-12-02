/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 20-jan-2004
 * Time: 10:07:23
 */
package no.uib.prideconverter.util;

//import be.proteomics.lims.util.mascot.MascotIdentifiedSpectrum;
import java.io.*;
import java.util.*;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/03/08 10:14:19 $
 */
import java.util.ArrayList;

/**
 * This class maps a multiple spectra Mascot Generic File to memory.
 * It allows for search and retrieval (as well as comparing functionality).
 *
 * @author Lennart
 * @version $Id: MascotGenericFile.java,v 1.3 2007/03/08 10:14:19 kenny Exp $
 * Modified by Harald Barsnes (November 2008)
 */
public class MascotGenericFile_MultipleSpectra {

    /**
     * @TODO This class is a modified version of be.proteomics.lims.util.fileio.MascotGenericFile
     * and should somehow be related to this (extend?), but this is not currently done.
     */

    protected ArrayList peaks;
    protected ArrayList precursorMzs;
    protected ArrayList precursorCharge;
    protected ArrayList precursorIntensity;
    protected ArrayList charges;
    protected ArrayList commentsAll;
    protected ArrayList titles;
    
    protected int numberOfSpectra = 0;
    
    /**
     * This variable holds the filename for the spectrum file.
     */
    protected String iFilename = null;
    /**
     * This HashMap holds all the peaks in the spectrum file.
     */
    protected HashMap iPeaks = new HashMap();
    /**
     * This variable holds the precursor M/Z
     */
    protected double iPrecursorMz = -1.0;
    /**
     * This variable holds the charge state.
     */
    protected int iCharge = 0;
    /**
     * The precursor intensity.
     */
    protected double iIntensity = -1.0;
    /**
     * This variable holds the comments for this MascotGenericFile.
     */
    private String iComments = null;
    /**
     * The title of the MascotGenericFile.
     */
    private String iTitle = null;
    /**
     * This HashMap will hold the charges for those ions for which a charge is known.
     */
    private HashMap iCharges = new HashMap();
    /**
     * This constant defines the key in the spectrum header for the title.
     */
    private static final String TITLE = "TITLE";
    /**
     * This constant defines the key in the spectrum header for the precursor M/Z and intensity.
     */
    private static final String PEPMASS = "PEPMASS";
    /**
     * This constant defines the key in the spectrum header for the precursor charge.
     * Note that this field can be omitted from a MascotGenericFile.
     */
    private static final String CHARGE = "CHARGE";
    /**
     * This constant defines the start of a comment line.
     */
    private static final String COMMENT_START = "###";
    /**
     * This constant defines the start tag for the ions.
     */
    private static final String IONS_START = "BEGIN IONS";
    /**
     * This constant defines the ernd tag for the ions.
     */
    private static final String IONS_END = "END IONS";
    /**
     * This Properties instance contains all the Embedded properties that are listed in a Mascot Generic File.
     */
    private Properties iExtraEmbeddedParameters;

    /**
     * This constructor takes the MGF File as a String as read from file or DB.
     * The filename is specified separately here.
     *
     * @param   aFilename   String with the filename for the MGF File.
     * @param   aContents   String with the contents of the MGF File.
     */
    public MascotGenericFile_MultipleSpectra(String aFilename, String aContents) {

        peaks = new ArrayList();
        precursorMzs = new ArrayList();
        precursorCharge = new ArrayList();
        precursorIntensity = new ArrayList();
        commentsAll = new ArrayList();
        charges = new ArrayList();
        titles = new ArrayList();

        this.parseFromString(aContents);
        this.iFilename = aFilename;
    }

    /**
     * This constructor takes the filename of the MGF File as argument and
     * loads it form the hard drive.
     *
     * @param   aFilename   File with the pointer to the MGF File.
     * @exception   IOException when the file could not be read.
     */
    public MascotGenericFile_MultipleSpectra(File aFilename) throws IOException {
        if (!aFilename.exists()) {
            throw new IOException("MGF File '" + aFilename.getCanonicalPath() +
                    "' was not found!");
        } else {
            StringBuffer lsb = new StringBuffer();
            BufferedReader br = new BufferedReader(new FileReader(aFilename));
            String line = null;
            while ((line = br.readLine()) != null) {
                lsb.append(line + "\n");
            }
            br.close();

            peaks = new ArrayList();
            precursorMzs = new ArrayList();
            titles = new ArrayList();
            precursorCharge = new ArrayList();
            precursorIntensity = new ArrayList();
            commentsAll = new ArrayList();
            charges = new ArrayList();

            this.parseFromString(lsb.toString());
            this.iFilename = aFilename.getName();
        }
    }

    /**
     * This method checks whether the MascotIdentifiedSpectrum corresponds to
     * this spectrum. The precise method for comparison is up to the individual
     * implementations.
     *
     * @param   aMIS   MascotIdentifiedSpectrum to compare to.
     * @return  boolean which indicates whether these objects correspond.
     */
//    public boolean corresponds(MascotIdentifiedSpectrum aMIS) {
//        boolean corresponds = false;
//        // The search title can yield the information we need.
//        // Typically, the title is maintained, or, if it is not, we simply need to
//        // check against the filename (in the latter case, we have substituted the
//        // title with the filename ourselves).
//        String searchTitle = aMIS.getSearchTitle();
//        if(this.iTitle.equals(searchTitle) || this.iFilename.equals(searchTitle)) {
//            corresponds = true;
//        }
//
//        // Return the result.
//        return corresponds;
//    }

    /**
     * This methods returns the comments for this MascotGenericFile.
     *
     * @return  String with the comments for this MascotGenericFile.
     */
    public String getComments() {
        return iComments;
    }

    /**
     * This method sets the comments for this MascotGenericFile.
     *
     * @param aComments String with the comments for this MAscotGenericFile.
     */
    public void setComments(String aComments) {
        iComments = aComments;
    }

    /**
     * This method returns the Value of the corresponding embedded parameter Key.
     * @param aKey  String with the Key of the embedded parameter.
     * @return String   Value of the embedded parameter Key.
     */
//    public String getExtraEmbeddedProperty(String aKey) {
//        String lReturn = "NoSuchKey";
//        if (iExtraEmbeddedParameters != null) {
//            if (iExtraEmbeddedParameters.containsKey(aKey)) {
//                lReturn = (String) iExtraEmbeddedParameters.get(aKey);
//            }
//        }
//        return lReturn;
//    }

    /**
     * This private method can be called during the parsing of the aFileContents String to save embedded parameters in the Properties instance.
     * @param aKey Embedded Property Key.
     * @param aValue Embedded Property Value.
     */
//    private void addExtraEmbeddedParameter(String aKey, String aValue) {
//        if (iExtraEmbeddedParameters == null) {
//            iExtraEmbeddedParameters = new Properties();
//        }
//        iExtraEmbeddedParameters.put(aKey, aValue);
//    }

    /**
     * This method checks for equality between this object and the
     * specified object.
     *
     * @param anObject  Object to test equality with.
     * @return  boolean indicating whether the presented objects are equal
     *                  ('true') or not ('false').
     */
    public boolean equals(Object anObject) {
        boolean result = false;

        if (anObject != null &&
                anObject instanceof MascotGenericFile_MultipleSpectra) {
            MascotGenericFile_MultipleSpectra other = (MascotGenericFile_MultipleSpectra) anObject;
            if (this.iFilename.equals(other.iFilename) && this.iCharge ==
                    other.iCharge &&
                    this.iTitle.equals(other.iTitle) &&
                    this.iPeaks.equals(other.iPeaks) &&
                    this.iCharges.equals(other.iCharges)) {
                result = true;
            }
        }

        return result;
    }

    /**
     * This method formats an integer to a charge String as used in a MascotGenericFile
     * (eg., 1 to 1+).
     *
     * @param aCharge   int with the charge to format.
     * @return  String with the formatted charge (eg., 1+).
     */
    private String processCharge(int aCharge) {
        // The charge notation is '1+', or conversely, '1-'.
        // Therefore we do some extra processing.
        String affix = "+";
        if (aCharge < 0) {
            affix = "-";
        }
        return Math.abs(aCharge) + affix;
    }

    /**
     * This method extracts an integer from Mascot Generic File charge notation, eg.,
     * 1+.
     * Remark that the charge can also be annotated as "+2,+3", in those rather cases the charge is also "not known." So we save a zero value.
     *
     * @param aCharge   String with the Mascot Generic File charge notation (eg., 1+).
     * @return  int with the corresponding integer.
     */
    private int extractCharge(String aCharge) {
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

            charge = Integer.parseInt(trimmedCharge.substring(0, trimmedCharge.length() -
                    1));
            if (negate) {
                charge = -charge;
            }
        }

        return charge;
    }

    /**
     * This method will parse the input String and read all the information present into
     * a MascotGenericFile object.
     *
     * @param aFileContent  String with the contents of the file.
     */
    private void parseFromString(String aFileContent) {
        try {
            BufferedReader br = new BufferedReader(new StringReader(aFileContent));
            String line = null;
            // Cycle the file.
            int lineCount = 0;
            boolean inSpectrum = false;
            StringBuffer comments = new StringBuffer();

            iPeaks = new HashMap();
            iComments = "";
            iCharges = new HashMap();

            numberOfSpectra = 0;

            while ((line = br.readLine()) != null) {
                // Advance line count.
                lineCount++;
                // Delete leading/trailing spaces.
                line = line.trim();
                // Skip empty lines.
                if (line.equals("")) {
                    continue;
                }
                // First line can be 'CHARGE'.
                if (lineCount == 1 && line.startsWith(CHARGE)) {
                    continue;
                }
                // Read all starting comments.
                if (line.startsWith("#")) {
                    comments.append(line + "\n");
                } // BEGIN IONS marks the start of the real file.
                else if (line.equals(IONS_START)) {
                    inSpectrum = true;
                } // END IONS marks the end.
                else if (line.equals(IONS_END)) {
                    inSpectrum = false;

                    peaks.add(iPeaks);
                    commentsAll.add(iComments);
                    charges.add(iCharges);
                    precursorMzs.add(iPrecursorMz);
                    precursorCharge.add(iCharge);
                    precursorIntensity.add(iIntensity);
                    //titles.add(iTitle);

                    numberOfSpectra++;
                    iPeaks = new HashMap();
                    iComments = "";
                    iCharges = new HashMap();

                } // Read embedded parameters. The most important parameters (such as TITLE, PEPMASS and optional CHARGE fields )
                // will be saved as instance variables as well as in the iEmbeddedParameter Properties instance.
                else if (inSpectrum && (line.indexOf("=") >= 0)) {
                    // Find the starting location of the value (which is one beyond the location
                    // of the '=').
                    int equalSignIndex = line.indexOf("=");

                    // See which header line is encountered.
                    if (line.startsWith(TITLE)) {
                        // TITLE line found.
                        titles.add(line.substring(equalSignIndex + 1).trim());
                    } else if (line.startsWith(PEPMASS)) {
                        // PEPMASS line found.
                        String value = line.substring(equalSignIndex + 1).trim();
                        StringTokenizer st = new StringTokenizer(value, " \t");
                        this.setPrecursorMZ(Double.parseDouble(st.nextToken().trim()));
                        // It is possible that parent intensity is not mentioned. We then set it to '0'.
                        if (st.hasMoreTokens()) {
                            this.setIntensity(Double.parseDouble(st.nextToken().trim()));
                        } else {
                            this.setIntensity(0.0);
                        }
                    } else if (line.startsWith(CHARGE)) {
                        // CHARGE line found.
                        // Note the extra parsing to read a Mascot Generic File charge (eg., 1+).
                        this.setCharge(this.extractCharge(line.substring(equalSignIndex +
                                1)));
                    } else {
                        // This is an extra embedded parameter!
                        String aKey = line.substring(0, equalSignIndex);
                        String aValue = line.substring(equalSignIndex + 1);
                        // Save the extra embedded parameter in iEmbeddedParameter
//                        addExtraEmbeddedParameter(aKey, aValue);
                    }
                } // Read peaks, minding the possibility of charge present!
                else if (inSpectrum) {
                    // We're inside the spectrum, with no '=' in the line, so it should be
                    // a peak line.
                    // A peak line should be either of the following two:
                    // 234.56 789
                    // 234.56 789   1+
                    StringTokenizer st = new StringTokenizer(line, " \t");
                    int count = st.countTokens();
                    if (count == 2 || count == 3) {
                        String temp = st.nextToken().trim();
                        Double mass = new Double(temp);
                        temp = st.nextToken().trim();
                        Double intensity = new Double(temp);
                        this.iPeaks.put(mass, intensity);
                        if (st.hasMoreTokens()) {
                            int charge = this.extractCharge(st.nextToken());
                            iCharges.put(mass, new Integer(charge));
                        }
                    } else {
                        System.err.println("\n\nUnrecognized line at line number " +
                                lineCount + ": '" + line + "'!\n");
                    }
                }
            }
            // Last but not least: add the comments.
            this.iComments = comments.toString();
            // That's it.
            br.close();
        } catch (IOException ioe) {
            // We do not expect IOException when using a StringReader.
            ioe.printStackTrace();
        }
    }

    /**
     * This method reports on the charge of the precursor ion.
     * Note that when the charge could not be determined, this
     * method will return '0'.
     *
     * @return  int with the charge of the precursor, or '0'
     *              if no charge state is known.
     */
    public int getPrecursorCharge(int spectraNumber) {
        return ((Integer) precursorCharge.get(spectraNumber)).intValue();
    }

    /**
     * This method reports on the filename for the file.
     *
     * @return  String with the filename for the file.
     */
    public String getFilename() {
        return iFilename;
    }

    /**
     * This method reports on the intensity of the precursor ion.
     *
     * @return  double with the intensity of the precursor ion.
     */
    public double getIntensity(int spectraNumber) {
        return ((Double) precursorIntensity.get(spectraNumber)).doubleValue();
    }

    /**
     * This method reports on the peaks in the spectrum, with the
     * Doubles for the masses as keys in the HashMap, and the intensities
     * for each peak as Double value for that mass key.
     *
     * @return  HashMap with Doubles as keys (the masses) and Doubles as values (the intensities).
     */
    public HashMap getPeaks(int spectraNumber) {
        return (HashMap) peaks.get(spectraNumber);
    }

    public String getTitle(int spectraNumber) {
        return (String) titles.get(spectraNumber);
    }

    /**
     * This method reports on the precursor M/Z
     *
     * @return  double with the precursor M/Z
     */
    public double getPrecursorMZ(int spectraNumber) {
        return ((Double) precursorMzs.get(spectraNumber)).doubleValue();
    }

    /**
     * This method sets the charge of the precursor ion. When the charge is not known,
     * it should be set to '0'.
     *
     * @param aCharge   int with the charge of the precursor ion.
     */
    public void setCharge(int aCharge) {
        this.iCharge = aCharge;
    }

    /**
     * This method sets the filename for the file.
     *
     * @param aFilename String with the filename for the file.
     */
    public void setFilename(String aFilename) {
        this.iFilename = aFilename;
    }

    /**
     * This method sets the intensity of the precursor ion.
     *
     * @param aIntensity double with the intensity of the precursor ion.
     */
    public void setIntensity(double aIntensity) {
        this.iIntensity = aIntensity;
    }

    /**
     * This method sets the peaks on the spectrum.
     * Doubles for the masses as keys in the HashMap, and the intensities
     * for each peak as Double value for that mass key.
     *
     * @param aPeaks HashMap with Doubles as keys (the masses) and Doubles as values (the intensities).
     */
    public void setPeaks(HashMap aPeaks) {
        this.iPeaks = aPeaks;
    }

    /**
     * This method sets the precursor M/Z on the file.
     *
     * @param aPrecursorMZ  double with the precursor M/Z
     */
    public void setPrecursorMZ(double aPrecursorMZ) {
        this.iPrecursorMz = aPrecursorMZ;
    }
    
    public int getSpectraCount(){
        return numberOfSpectra;
    }
}
