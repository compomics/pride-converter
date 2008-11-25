/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 13-Sep-2006
 * Time: 16:04:42
 */
package uk.ac.ebi.tpp_to_pride.wrappers.peptideprophet;
/*
 * CVS information:
 *
 * $Revision: 1.1.1.1 $
 * $Date: 2007/01/12 17:17:10 $
 */

/**
 * This class wraps a protien as described by the PeptideProphet
 * output file.
 *
 * @author martlenn
 * @version $Id: PeptideProphetProteinID.java,v 1.1.1.1 2007/01/12 17:17:10 lmartens Exp $
 */
public class PeptideProphetProteinID {

    /**
     * The primary protein accession number
     * (primary meaning: the accession number
     * from the database the identifications were done in)
     */
    private String accession = null;

    /**
     * The description line corresponding with the protein
     * accession number in the source database.
     */
    private String description = null;

    private int numTolTerm = -1;

    public PeptideProphetProteinID(String aAccession, String aDescription, int aNumTolTerm) {
        accession = aAccession;
        description = aDescription;
        numTolTerm = aNumTolTerm;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String aAccession) {
        accession = aAccession;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String aDescription) {
        description = aDescription;
    }

    public int getNumTolTerm() {
        return numTolTerm;
    }

    public void setNumTolTerm(int aNumTolTerm) {
        numTolTerm = aNumTolTerm;
    }
}
