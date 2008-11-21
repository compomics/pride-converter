/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 21-Aug-2006
 * Time: 14:26:24
 */
package uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet;

import java.util.List;
/*
 * CVS information:
 *
 * $Revision: 1.1.1.1 $
 * $Date: 2007/01/12 17:17:10 $
 */

/**
 * This class represents a PeptideProphet peptide ID.
 *
 * @author martlenn
 * @version $Id: ProteinProphetPeptideID.java,v 1.1.1.1 2007/01/12 17:17:10 lmartens Exp $
 */
public class ProteinProphetPeptideID {

    private String sequence = null;

    private int charge = -1;

    private int count = -1;

    private String modified_sequence = null;

    private List alternativeParentProteins = null;

    public ProteinProphetPeptideID(int aCharge, int aCount, String aModified_sequence, String aSequence, List aAlternativeParentProteins) {
        charge = aCharge;
        count = aCount;
        modified_sequence = aModified_sequence;
        sequence = aSequence;
        alternativeParentProteins = aAlternativeParentProteins;
    }

    public int getCharge() {
        return charge;
    }

    public void setCharge(int aCharge) {
        charge = aCharge;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int aCount) {
        count = aCount;
    }

    public String getModified_sequence() {
        return modified_sequence;
    }

    public void setModified_sequence(String aModified_sequence) {
        modified_sequence = aModified_sequence;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String aSequence) {
        sequence = aSequence;
    }

    public List getAlternativeParentProteins() {
        return alternativeParentProteins;
    }

    public void setAlternativeParentProteins(List aAlternativeParentProteins) {
        alternativeParentProteins = aAlternativeParentProteins;
    }
}
