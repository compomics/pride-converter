package no.uib.prideconverter.util;

/**
 * Includes help methods that are used by the other classes.
 * 
 * @author  Harald Barsnes
 * 
 * Created April 2005
 */
public final class Util {


    public static final String MS_CV = "MS";

    public static final String MS_M2ZRATIO_ACC = "MS:1000040";
    public static final String MS_M2ZRATIO_TERM = "MassToChargeRatio";

    public static final String MS_CHARGESTATE_ACC = "MS:1000041";
    public static final String MS_CHARGESTATE_TERM = "ChargeState";

    public static final String MS_INTENSITY_ACC = "MS:1000042";
    public static final String MS_INTENSITY_TERM = "Intensity";

    public static final String MS_CRG_DECON_ACC = "MS:1000034";
    public static final String MS_CRG_DECON_TERM = "ChargeDeconvolution";

    public static final String MS_DEISO_ACC = "MS:1000033";
    public static final String MS_DEISO_TERM = "Deisotoping";

    public static final String MS_CENTROID_ACC = "MS:1000127";
    public static final String MS_CENTROID_TERM = "centroid spectrum";

    public static final String MS_SCAN_POL_ACC = "MS:1000465";
    public static final String MS_SCAN_POL_TERM = "scan polarity";

    public static final String MS_POS_SCAN_ACC = "MS:1000130";
    public static final String MS_POS_SCAN_TERM = "positive scan";

    public static final String MS_NEG_SCAN_ACC = "MS:1000129";
    public static final String MS_NEG_SCAN_TERM = "negative scan";

    public static final String MS_RET_TIME_ACC = "MS:1000894";
    public static final String MS_RET_TIME_TERM = "retention time";




    /**
     * Makes sure that all writing to the ErrorLog has a uniform appearence.
     *
     * Writes the given String to the errorLog.
     * Adds date and time of entry.
     *
     * @param logEntry
     */
    public static void writeToErrorLog(String logEntry) {
        System.out.println(new java.util.Date(System.currentTimeMillis()).toString() + ": " + logEntry);
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
     * Adds zeros to the begining of the ms_lims project id so that all project
     * ids will have equal length, making sorting on file name easier.
     *
     * @param id the id to zero fill
     * @param length the length if the number after adding the zeros
     * @return the zero filled id
     */
    public static String zerofill(long id, int length) {
        String result = "" + id;

        while (result.length() < length) {
            result = "0" + result;
        }

        return result;
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
    public static int extractCharge(String aCharge) {
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
        if (trimmedCharge.indexOf(",") >= 0 || trimmedCharge.indexOf("and") >= 0) {
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
}
