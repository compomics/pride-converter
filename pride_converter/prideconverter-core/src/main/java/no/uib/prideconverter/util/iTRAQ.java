package no.uib.prideconverter.util;

import com.compomics.mascotdatfile.util.mascot.MascotDatfile;
import com.compomics.mascotdatfile.util.mascot.Peak;
import com.compomics.mascotdatfile.util.mascot.Query;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.*;

/**
 * A class that lets you extract iTRAQ values for a given dataset. Translated 
 * from Perl code from i-Tracker.
 *
 * @author  Harald Barsnes
 * 
 * Created July 2008
 */
public class iTRAQ {

    private String fileName;
    private double lowerRange,  upperRange;
    private double threshold;
    private double[] purityCorrections;
    private static final int MZ_ARRAY = 0;
    private static final int INTENSITIES_ARRAY = 1;
    private String areaUT1,  areaUT2,  areaUT3,  areaUT4;
    private String[] r21,  r31,  r41,  r32,  r42,  r43;
    private String r12,  r13,  r14,  r23,  r24,  r34;
    private MascotDatfile mascotDatFile;
    private File file;
    private String[][] ratios;
    private Vector allNorms = new Vector();
    private Vector allUTs = new Vector();
    private Vector allRatios = new Vector();
    private String err1 = "NA",  err2 = "NA",  err3 = "NA",  err4 = "NA";
    private double i114max = 0,  i115max = 0,  i116max = 0,  i117max = 0;
    private double[][] arrays;
    private double precursorMz,  precursorCharge;

    /**
     * Creates a new iTRAQ object that holds all the information needed for 
     * the calculations.
     * 
     * @param arrays
     * @param precursorMz
     * @param precursorCharge
     * @param lowerRange
     * @param upperRange
     * @param threshold
     * @param corrections
     */
    public iTRAQ(double[][] arrays, double precursorMz, int precursorCharge,
            double lowerRange, double upperRange,
            double threshold, double[] corrections) {

        this.arrays = arrays;
        this.precursorMz = precursorMz;
        this.precursorCharge = precursorCharge;

        this.lowerRange = lowerRange;
        this.upperRange = upperRange;
        this.threshold = threshold;
        purityCorrections = corrections;

//        if (lowerRange + upperRange > 1) {
//            System.out.println("The input peak integration ranges cross over between peaks " +
//                    "(sum is > 1).\nPlease re-run program.");
//        }
    }

    /**
     * Creates a new iTRAQ object that holds all the information needed for 
     * the calculations.
     * 
     * @param mzValues
     * @param intValues 
     * @param precursorMz
     * @param precursorCharge
     * @param lowerRange
     * @param upperRange
     * @param threshold
     * @param corrections 
     */
    public iTRAQ(double[] mzValues, double[] intValues, double precursorMz, int precursorCharge,
            double lowerRange, double upperRange,
            double threshold, double[] corrections) {

        arrays = new double[2][mzValues.length];

        for (int j = 0; j < mzValues.length; j++) {
            arrays[0][j] = mzValues[j];
            arrays[1][j] = intValues[j];
        }

        this.precursorMz = precursorMz;
        this.precursorCharge = precursorCharge;

        this.lowerRange = lowerRange;
        this.upperRange = upperRange;
        this.threshold = threshold;
        purityCorrections = corrections;

//        if (lowerRange + upperRange > 1) {
//            System.out.println("The input peak integration ranges cross over between peaks " +
//                    "(sum is > 1).\nPlease re-run program.");
//        }
    }

    /**
     * Creates a new iTRAQ object that holds all the information needed for 
     * the calculations. Only supports Mascot DAT Files.
     * 
     * @param fileName
     * @param lowerRange
     * @param upperRange
     * @param threshold
     * @param corrections
     */
    public iTRAQ(String fileName, double lowerRange, double upperRange,
            double threshold, double[] corrections) {

        this.fileName = fileName;

        this.lowerRange = lowerRange;
        this.upperRange = upperRange;
        this.threshold = threshold;
        purityCorrections = corrections;

//        if (lowerRange + upperRange > 1) {
//            System.out.println("The input peak integration ranges cross over between peaks " +
//                    "(sum is > 1).\nPlease re-run program.");
//        }

        //Read data file
        file = new File(fileName);

        if (file.getPath().endsWith(".dat")) {
            //read dat file
            mascotDatFile = new MascotDatfile(file.getPath());
        } else {
            System.out.println("Only Mascot DAT Files are supported!");
        }
    }

    /**
     * Find the determinant.
     * 
     * @param purityCorrections
     * @return the determinant
     */
    public double findDeterminant(double[] purityCorrections) {

        double[] corrs = purityCorrections;

        double w = (100 - (corrs[0] + corrs[1] + corrs[2] + corrs[3]));
        double x = (100 - (corrs[4] + corrs[5] + corrs[6] + corrs[7]));
        double y = (100 - (corrs[8] + corrs[9] + corrs[10] + corrs[11]));
        double z = (100 - (corrs[12] + corrs[13] + corrs[14] + corrs[15]));

        double[][] A = {{w, corrs[5], corrs[8], 0},
            {corrs[2], x, corrs[9], corrs[12]},
            {corrs[3], corrs[6], y, corrs[13]},
            {0, corrs[7], corrs[10], z}};

        double determinant = calculateDeterminant(A);

        if (determinant == 0.0) {
            System.out.println("WARNING: (itracker) No-unique solutions to purity " +
                    "correction calculations.\nWARNING! Proceeding without purity correction.\n");
        }

        return determinant;
    }

    /**
     * Calclulate the purity corrections.
     * 
     * @param t1Area
     * @param t2Area
     * @param t3Area
     * @param t4Area
     * @param purityCorrections
     * @return the purity corrections
     */
    public double[] purityCorrection(double t1Area, double t2Area, double t3Area,
            double t4Area, double[] purityCorrections) {

        double[] corrs = purityCorrections;

        double w = (100 - (corrs[0] + corrs[1] + corrs[2] + corrs[3]));
        double x = (100 - (corrs[4] + corrs[5] + corrs[6] + corrs[7]));
        double y = (100 - (corrs[8] + corrs[9] + corrs[10] + corrs[11]));
        double z = (100 - (corrs[12] + corrs[13] + corrs[14] + corrs[15]));

        double[][] A = {{w, corrs[5], corrs[8], 0},
            {corrs[2], x, corrs[9], corrs[12]},
            {corrs[3], corrs[6], y, corrs[13]},
            {0, corrs[7], corrs[10], z}};

        double determinantA = calculateDeterminant(A);

        double[][] one = {{t1Area, t2Area, t3Area, t4Area},
            {corrs[5], x, corrs[6], corrs[7]},
            {corrs[8], corrs[9], y, corrs[10]},
            {0, corrs[12], corrs[13], z}};

        double determinantOne = calculateDeterminant(one);

        double[][] two = {{w, corrs[2], corrs[3], 0},
            {t1Area, t2Area, t3Area, t4Area},
            {corrs[8], corrs[9], y, corrs[10]},
            {0, corrs[12], corrs[13], z}};

        double determinantTwo = calculateDeterminant(two);

        double[][] three = {{w, corrs[2], corrs[3], 0},
            {corrs[5], x, corrs[6], corrs[7]},
            {t1Area, t2Area, t3Area, t4Area},
            {0, corrs[12], corrs[13], z}};

        double determinantThree = calculateDeterminant(three);

        double[][] four = {{w, corrs[2], corrs[3], 0},
            {corrs[5], x, corrs[6], corrs[7]},
            {corrs[8], corrs[9], y, corrs[10]},
            {t1Area, t2Area, t3Area, t4Area}};

        double determinantFour = calculateDeterminant(four);

        double[] corrections = {
            determinantOne / determinantA,
            determinantTwo / determinantA,
            determinantThree / determinantA,
            determinantFour / determinantA
        };

        double totalArea = corrections[0] + corrections[1] + corrections[2] +
                corrections[3];

        double area0 = 0, area1 = 0, area2 = 0, area3 = 0;

        if (totalArea == 0) {
            //do nothing
        } else {
            area0 = roundToThreeDecimalPlaces(corrections[0] / totalArea);
            area1 = roundToThreeDecimalPlaces(corrections[1] / totalArea);
            area2 = roundToThreeDecimalPlaces(corrections[2] / totalArea);
            area3 = roundToThreeDecimalPlaces(corrections[3] / totalArea);
        }

        double[] normalizedCorrections = {
            area0,
            area1,
            area2,
            area3
        };

        //Area or UT?...  NB For this version, read "area" as intensity.
        areaUT1 = aUT(i114max, normalizedCorrections[0]);
        areaUT2 = aUT(i115max, normalizedCorrections[1]);
        areaUT3 = aUT(i116max, normalizedCorrections[2]);
        areaUT4 = aUT(i117max, normalizedCorrections[3]);

        //Calculate ratios
        r21 = ratio(normalizedCorrections[1], normalizedCorrections[0], i115max, i114max);
        r31 = ratio(normalizedCorrections[2], normalizedCorrections[0], i116max, i114max);
        r41 = ratio(normalizedCorrections[3], normalizedCorrections[0], i117max, i114max);
        r32 = ratio(normalizedCorrections[2], normalizedCorrections[1], i116max, i115max);
        r42 = ratio(normalizedCorrections[3], normalizedCorrections[1], i117max, i115max);
        r43 = ratio(normalizedCorrections[3], normalizedCorrections[2], i117max, i116max);

        //if a number -> call threeDecimalPlaces
        if (!r21[0].equalsIgnoreCase("NA") && !r21[0].equalsIgnoreCase("UT")) {
            r21[0] = "" + roundToThreeDecimalPlaces(new Double(r21[0]).doubleValue());
        }
        if (!r31[0].equalsIgnoreCase("NA") && !r31[0].equalsIgnoreCase("UT")) {
            r31[0] = "" + roundToThreeDecimalPlaces(new Double(r31[0]).doubleValue());
        }
        if (!r41[0].equalsIgnoreCase("NA") && !r41[0].equalsIgnoreCase("UT")) {
            r41[0] = "" + roundToThreeDecimalPlaces(new Double(r41[0]).doubleValue());
        }
        if (!r32[0].equalsIgnoreCase("NA") && !r32[0].equalsIgnoreCase("UT")) {
            r32[0] = "" + roundToThreeDecimalPlaces(new Double(r32[0]).doubleValue());
        }
        if (!r42[0].equalsIgnoreCase("NA") && !r42[0].equalsIgnoreCase("UT")) {
            r42[0] = "" + roundToThreeDecimalPlaces(new Double(r42[0]).doubleValue());
        }
        if (!r43[0].equalsIgnoreCase("NA") && !r43[0].equalsIgnoreCase("UT")) {
            r43[0] = "" + roundToThreeDecimalPlaces(new Double(r43[0]).doubleValue());
        }

        //Calculate ratio reciprocals
        r12 = "UT";
        r13 = "UT";
        r14 = "UT";
        r23 = "UT";
        r24 = "UT";
        r34 = "UT";

        if (!r21[0].equalsIgnoreCase("NA") && !r21[0].equalsIgnoreCase("UT")) {
            r12 = "" + roundToThreeDecimalPlaces(1 /
                    new Double(r21[0]).doubleValue());
        }
        if (!r31[0].equalsIgnoreCase("NA") && !r31[0].equalsIgnoreCase("UT")) {
            r13 = "" + roundToThreeDecimalPlaces(1 /
                    new Double(r31[0]).doubleValue());
        }
        if (!r41[0].equalsIgnoreCase("NA") && !r41[0].equalsIgnoreCase("UT")) {
            r14 = "" + roundToThreeDecimalPlaces(1 /
                    new Double(r41[0]).doubleValue());
        }
        if (!r32[0].equalsIgnoreCase("NA") && !r32[0].equalsIgnoreCase("UT")) {
            r23 = "" + roundToThreeDecimalPlaces(1 /
                    new Double(r32[0]).doubleValue());
        }
        if (!r42[0].equalsIgnoreCase("NA") && !r42[0].equalsIgnoreCase("UT")) {
            r24 = "" + roundToThreeDecimalPlaces(1 /
                    new Double(r42[0]).doubleValue());
        }
        if (!r43[0].equalsIgnoreCase("NA") && !r43[0].equalsIgnoreCase("UT")) {
            r34 = "" + roundToThreeDecimalPlaces(1 /
                    new Double(r43[0]).doubleValue());
        }

        // Errors will be the same, so no need to recalc, 
        // except for indiv error components
        err1 = "NA";
        err2 = "NA";
        err3 = "NA";
        err4 = "NA";

        if (i114max > 0) {
            err1 = "" + round((0.5 / i114max) * 100);
        }
        if (i115max > 0) {
            err2 = "" + round((0.5 / i115max) * 100);
        }
        if (i116max > 0) {
            err3 = "" + round((0.5 / i116max) * 100);
        }
        if (i117max > 0) {
            err4 = "" + round((0.5 / i117max) * 100);
        }

        return (normalizedCorrections);
    }

    /**
     * Calculate the ratios.
     * 
     * @param normCorrectionA
     * @param normCorrectionB
     * @param tMaxA
     * @param tMaxB
     * @return
     */
    private String[] ratio(double normCorrectionA, double normCorrectionB,
            double tMaxA, double tMaxB) {

        double top = normCorrectionA;
        double bottom = normCorrectionB;
        double topmax = tMaxA;
        double bottommax = tMaxB;

        String ratio = "0";
        String totalError = "NA";

        if (topmax <= threshold) {
            ratio = "UT";
        } else if (bottommax <= threshold) {
            ratio = "UT";
        } else if (bottom == 0) {
            ratio = "NA";
        } else {
            ratio = "" + top / bottom;

            double topError = (0.5 / topmax); //Quantisation error at max
            double bottomError = (0.5 / bottommax);

            totalError = "" + round((topError + bottomError) * 100);
        }

        return new String[]{ratio, totalError};
    }

    /**
     * Round the given value to three decimal places.
     * 
     * @param value
     * @return
     */
    private double roundToThreeDecimalPlaces(double value) {
        value = round(value * 1000);
        value = (value / 1000);
        return value;
    }

    /**
     * Round to nearest integer.
     * 
     * @param value
     * @return
     */
    private int round(double value) {
        return (int) Math.round(value);
    }

    /**
     * Return the area as a String.
     * 
     * @param maxIntensity
     * @param area
     * @return
     */
    private String aUT(double maxIntensity, double area) {

        String areaAsString = "" + area;

        if (maxIntensity <= threshold) {
            areaAsString = "UT";
        }

        return (areaAsString);
    }

    /**
     * Calculate the determinant.
     * 
     * @param mat
     * @return
     */
    private double calculateDeterminant(double[][] mat) {

        double result = 0;

        if (mat.length == 1) {
            result = mat[0][0];
            return result;
        }

        if (mat.length == 2) {
            result = mat[0][0] * mat[1][1] - mat[0][1] * mat[1][0];
            return result;
        }

        for (int i = 0; i <
                mat[0].length; i++) {
            double temp[][] = new double[mat.length - 1][mat[0].length - 1];
            for (int j = 1; j <
                    mat.length; j++) {
                for (int k = 0; k <
                        mat[0].length; k++) {
                    if (k < i) {
                        temp[j - 1][k] = mat[j][k];
                    } else if (k > i) {
                        temp[j - 1][k - 1] = mat[j][k];
                    }

                }
            }

            result += mat[0][i] * Math.pow(-1, (double) i) *
                    calculateDeterminant(temp);
        }

        return result;
    }

    /**
     * Returns all the normalized values.
     * 
     * @return the normalized values
     */
    public Vector getAllNorms() {
        return allNorms;
    }

    /**
     * Returns all the UT values.
     * 
     * @return the UT values
     */
    public Vector getAllUTs() {
        return allUTs;
    }

    /**
     * Returns all the ratios.
     * 
     * @return the ratios
     */
    public Vector getAllRatios() {
        return allRatios;
    }

    /**
     * Calculates the iTRAQ values.
     */
    public void calculateiTRAQValues() {

        allNorms = new Vector();
        allUTs = new Vector();
        allRatios = new Vector();

        if (mascotDatFile != null) {

            Query tempQuery;
            Peak[] peakList;
            Vector queries;

            for (int i = 1; i <
                    mascotDatFile.getQueryToPeptideMap().getNumberOfQueries(); i++) {

                queries = mascotDatFile.getQueryList();
                tempQuery = (Query) queries.get(i - 1);
                peakList = tempQuery.getPeakList();

                arrays = new double[2][peakList.length];

                for (int j = 0; j < peakList.length; j++) {

                    arrays[0][j] = peakList[j].getMZ();
                    arrays[1][j] = peakList[j].getIntensity();
                }

                calculateRatios(tempQuery.getPrecursorMZ());
            }
        }
        if (arrays != null) {
            calculateRatios(precursorMz);
        } else {

            // Tries to read the file as a space separated text file of 
            // precursor and intensities.
            try {
                FileReader f = new FileReader(file);
                BufferedReader b = new BufferedReader(f);

                String currentLine = b.readLine();

                StringTokenizer tok = new StringTokenizer(currentLine);

                precursorMz = new Double(tok.nextToken()).doubleValue();
                precursorCharge = new Integer(tok.nextToken()).intValue();

                currentLine = b.readLine();

                Vector masses = new Vector();
                Vector intensities = new Vector();

                while (currentLine != null) {
                    tok = new StringTokenizer(currentLine);
                    masses.add(new Double(tok.nextToken()));
                    intensities.add(new Double(tok.nextToken()));

                    currentLine = b.readLine();
                }

                arrays = new double[2][masses.size()];

                for (int i = 0; i < masses.size(); i++) {
                    arrays[0][i] = ((Double) masses.get(i)).doubleValue();
                    arrays[1][i] = ((Double) intensities.get(i)).doubleValue();
                }

                calculateRatios(precursorMz);

            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(
                        null, "The file " + file + " could not be found.",
                        "File Not Found", JOptionPane.ERROR_MESSAGE);
                Util.writeToErrorLog("File not found: ");
                ex.printStackTrace();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        null, "An error occured when reading " + file + ".",
                        "Error Reading File", JOptionPane.ERROR_MESSAGE);
                Util.writeToErrorLog("Error when reading file: ");
                e.printStackTrace();
            }
        }
    }

    /**
     * Calculate the ratios.
     * 
     * @param precursorMZ
     */
    private void calculateRatios(double precursorMZ) {

        double tempIntensity, tempMass;
        double[] tempNorms;
        HashMap i114, i115, i116, i117;
        i114max = 0;
        i115max = 0;
        i116max = 0;
        i117max = 0;
        double i114area = 0, i115area = 0, i116area = 0, i117area = 0;

        i114 = new HashMap();
        i115 = new HashMap();
        i116 = new HashMap();
        i117 = new HashMap();

        for (int k = 0; k < arrays[0].length; k++) {
            tempIntensity = arrays[INTENSITIES_ARRAY][k];
            tempMass = arrays[MZ_ARRAY][k];

            if (tempMass >= (114.1 - lowerRange) && (tempMass <=
                    (117.1 + upperRange))) {
                if (tempMass >= (117.1 - lowerRange)) {
                    i117.put(tempMass, tempIntensity);
                } else if (tempMass <= (116.1 + upperRange)) {
                    if (tempMass >= (116.1 - lowerRange)) {
                        i116.put(tempMass, tempIntensity);
                    } else if (tempMass <= (115.1 +
                            upperRange)) {
                        if (tempMass >= (115.1 - lowerRange)) {
                            i115.put(tempMass, tempIntensity);
                        } else if (tempMass <= (114.1 +
                                upperRange)) {
                            i114.put(tempMass, tempIntensity);
                        }
                    }
                }
            }
        }

        double[] temp;

        temp = peakArea(i114);
        i114area = temp[0];
        i114max = temp[1];

        temp = peakArea(i115);
        i115area = temp[0];
        i115max = temp[1];

        temp = peakArea(i116);
        i116area = temp[0];
        i116max = temp[1];

        temp = peakArea(i117);
        i117area = temp[0];
        i117max = temp[1];

        tempNorms = purityCorrection(i114area, i115area, i116area, i117area, purityCorrections);

        String tempString;

        DecimalFormat formater = new DecimalFormat("#0.0000");

        tempString = formater.format(precursorMZ);
        tempString = tempString.replaceAll(",", ".");


        // Code for printing the results to the terminal.
//                System.out.println("MASCOT 0 " +
//                        tempQuery.getChargeString() + "+ ");
//                System.out.println("PEPMASS=" + tempString);
//                System.out.println("Areas,114, 115, 116, 117,, Ratios, 114, 115, 116, 117,,Errors, 114,115,116,117\n" +
//                        "Norm," + tempNorms[0] + "," + tempNorms[1] + "," +
//                        tempNorms[2] + "," + tempNorms[3] + ",,114,1," + r21[0] +
//                        "," + r31[0] + "," + r41[0] + ",,114," + err1 + "," +
//                        r21[1] + "," + r31[1] + "," + r41[1] +
//                        "\nUT?," + areaUT1 + "," + areaUT2 + "," + areaUT3 + "," +
//                        areaUT4 + ",,115," + r12 + ",1," + r32[0] + "," + r42[0] +
//                        ",,115," + r21[1] + "," + err2 + "," + r32[1] + "," +
//                        r42[1] + "\n" +
//                        ",,,,,,116," + r13 + "," + r23 + ",1," + r43[0] +
//                        ",,116," + r31[1] + "," + r32[1] + "," + err3 + "," +
//                        r43[1] + "\n" +
//                        ",,,,,,117," + r14 + "," + r24 + "," + r34 + ",1" +
//                        ",,117," + r41[1] + "," + r42[1] + "," + r43[1] + "," +
//                        err4 + "\n>");

        allNorms.add(new String[]{"" + tempNorms[0], "" + tempNorms[1],
                    "" + tempNorms[2], "" + tempNorms[3]
                });

        allUTs.add(new String[]{areaUT1, areaUT2, areaUT3, areaUT4});

        ratios = new String[][]{
                    {"" + 1, "" + r21[0], "" + r31[0], "" + r41[0]},
                    {"" + r12, "" + 1, "" + r32[0], "" + r42[0]},
                    {"" + r13, "" + r23, "" + 1, "" + r43[0]},
                    {"" + r14, "" + r24, "" + r34, "" + 1}};

        allRatios.add(ratios);
    }

    /**
     * Calculate the peak area.
     * 
     * @param map
     * @return
     */
    private double[] peakArea(HashMap map) {

        double max = 0;
        double area = 0;
        double tempMass1, tempMass2, tempIntensity1, tempIntensity2, height, width;

        Map sortedMap = new TreeMap(map);

        Iterator iterator = sortedMap.keySet().iterator();

        if (iterator.hasNext()) {
            tempMass1 = ((Double) iterator.next()).doubleValue();
            tempIntensity1 = ((Double) sortedMap.get(tempMass1)).doubleValue();

            if (tempIntensity1 > max) {
                max = tempIntensity1;
            }

            while (iterator.hasNext()) {
                tempMass2 = ((Double) iterator.next()).doubleValue();
                tempIntensity2 = ((Double) sortedMap.get(tempMass2)).doubleValue();

                height = ((tempIntensity1 + tempIntensity2) / 2);
                width = (tempMass2 - tempMass1);
                area = (area + (height * width));

                if (tempIntensity1 > max) {
                    max = tempIntensity1;
                }

                tempMass1 = tempMass2;
                tempIntensity1 = tempIntensity2;
            }
        }

        double[] temp = {area, max};

        return temp;
    }

    /**
     * Rounds of a double value to the wanted number of decimalplaces
     *
     * @param d the double to round of
     * @param places number of decimalplaces wanted
     * @return double - the new double
     */
    public static double roundDouble(double d, int places) {
        return Math.round(d * Math.pow(10, (double) places)) /
                Math.pow(10, (double) places);
    }
}
