
package no.uib.prideconverter.gui;

import java.awt.Window;
import uk.ac.ebi.pride.model.implementation.mzData.ContactImpl;

/**
 * An interface for all frames and dialogs using the New Contact dialog.
 *
 * @author Harald Barsnes
 *
 * Created July 2009
 */
public interface ContactInputable {

    /**
     * Returns true if the contact is already in the table, fase otherwise.
     *
     * @param contactName the contact to add to the table
     * @return true if the contact is already in the table, fase otherwise.
     */
     public boolean contactAlreadyInTable(String contactName);

     /**
      * Adds or updates the given contact to/in the list.
      *
      * @param contact the new or updated contact
      * @param selectedRow the row to update, -1 if new contact
      */
     public void addContact(ContactImpl contact, int selectedRow);

    /**
     * Returns a reference to the frame or dialog to where the information
     * is inserted. Used by the OLS dialog to set the location relative to
     * its parent.
     *
     * @return a reference to the frame or dialog
     */
    public Window getWindow();
}
