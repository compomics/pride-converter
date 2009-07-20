package no.uib.prideconverter;

import java.io.InputStream;
import java.io.IOException;

/**
 * @author Florian Reisinger
 *         Date: 15-Jul-2009
 * @since $version
 */
public abstract class AbstractPrideConverter {

    private static String wizardName = "PRIDE Converter";
    private static String prideConverterVersionNumber = "v" + getVersion();
    private static boolean cancelConversion = false;


    /**
     * Looks up the current PRIDEConverter version from the properties file:
     *   prideconverter.properties
     *
     * @return the current PRIDEConverter version number.
     */
    private static String getVersion() {

        java.util.Properties p = new java.util.Properties();
        try {
            InputStream is = PRIDEConverter.class.getClassLoader().getResourceAsStream("prideconverter.properties");
            p.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return p.getProperty("converter.version");
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
     * Returns the version number of PRIDE Converter.
     *
     * @return the version number of PRIDE Converter
     */
    public static String getPrideConverterVersionNumber() {
        return prideConverterVersionNumber;
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


    
    
}
