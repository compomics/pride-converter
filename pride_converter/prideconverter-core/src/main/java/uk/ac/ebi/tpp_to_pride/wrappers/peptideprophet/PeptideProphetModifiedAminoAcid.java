/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 13-Sep-2006
 * Time: 16:42:21
 */
package uk.ac.ebi.tpp_to_pride.wrappers.peptideprophet;
/*
 * CVS information:
 *
 * $Revision: 1.1.1.1 $
 * $Date: 2007/01/12 17:17:10 $
 */

/**
 * This class wraps a single modified amino acid.
 *
 * @author martlenn
 * @version $Id: PeptideProphetModifiedAminoAcid.java,v 1.1.1.1 2007/01/12 17:17:10 lmartens Exp $
 */
public class PeptideProphetModifiedAminoAcid {

    /**
     * The position of the modification.
     */
    private int position = -1;

    /**
     * The mass of the modified amino acid.
     */
    private double mass = -1.0;

    public PeptideProphetModifiedAminoAcid(int aPosition, double aMass) {
        position = aPosition;
        mass = aMass;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double aMass) {
        mass = aMass;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int aPosition) {
        position = aPosition;
    }
}
