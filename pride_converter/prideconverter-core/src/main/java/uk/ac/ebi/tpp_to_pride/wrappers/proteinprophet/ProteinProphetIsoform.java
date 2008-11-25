/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 21-Aug-2006
 * Time: 16:05:09
 */
package uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet;
/*
 * CVS information:
 *
 * $Revision: 1.1.1.1 $
 * $Date: 2007/01/12 17:17:10 $
 */

/**
 * This class represents the minimalist information reported by
 * ProteinProphet for an 'indistinguishable protein'.
 *
 * @author martlenn
 * @version $Id: ProteinProphetIsoform.java,v 1.1.1.1 2007/01/12 17:17:10 lmartens Exp $
 */
public class ProteinProphetIsoform {

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

    public ProteinProphetIsoform(String aAccession, String aDescription) {
        accession = aAccession;
        description = aDescription;
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
}
