package no.uib.prideconverter.util;

/**
 * Makes sure that all writing to the ErrorLog has a uniform appearence.
 * 
 * @author  Harald Barsnes
 * 
 * Created April 2005
 */
public final class Util {

    /**
     * Writes the given String to the errorLog. 
     * Adds date and time of entry.
     *
     * @param logEntry
     */
    public static void writeToErrorLog(String logEntry) {
        System.out.println(
                new java.util.Date(System.currentTimeMillis()).toString() + ": " + logEntry);
    }
}
