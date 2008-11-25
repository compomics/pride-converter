package no.uib.prideconverter.util;

/**
 * Interface for interaction with a frame with a combo box.
 * 
 * @author Harald Barsnes
 * 
 * Created March 2008
 */
public interface ComboBoxInputable{

    /**
     * Insert the given text into the combo box.
     *  
     * @param text
     */
    public void insertIntoComboBox(String text);
    
    /**
     * Reset the combo box to the default value.
     */
    public void resetComboBox();
    
    /**
     * Test if the given text is already in the combo box.
     * 
     * @param text
     * @return true if already inserted, false otherwise.
     */
    public boolean alreadyInserted(String text);
}
