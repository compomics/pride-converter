/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 25-Aug-2006
 * Time: 18:54:55
 */
package uk.ac.ebi.tpp_to_pride.wrappers.peptideprophet;
/*
 * CVS information:
 *
 * $Revision: 1.1.1.1 $
 * $Date: 2007/01/12 17:17:10 $
 */

/**
 * This class wraps a PeptideProphet modification element.
 *
 * @author martlenn
 * @version $Id: PeptideProphetModification.java,v 1.1.1.1 2007/01/12 17:17:10 lmartens Exp $
 */
public class PeptideProphetModification {

    private String aminoacid = null;
    private double massDiff = 0.0;
    private double mass = 0.0;
    private boolean variable = false;
    private String symbol = null;

    public PeptideProphetModification(String aAminoacid, double aMass, double aMassDiff, String aSymbol, boolean aVariable) {
        aminoacid = aAminoacid;
        mass = aMass;
        massDiff = aMassDiff;
        symbol = aSymbol;
        variable = aVariable;
    }

    public String getAminoacid() {
        return aminoacid;
    }

    public void setAminoacid(String aAminoacid) {
        aminoacid = aAminoacid;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double aMass) {
        mass = aMass;
    }

    public double getMassDiff() {
        return massDiff;
    }

    public void setMassDiff(double aMassDiff) {
        massDiff = aMassDiff;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String aSymbol) {
        symbol = aSymbol;
    }

    public boolean isVariable() {
        return variable;
    }

    public void setVariable(boolean aVariable) {
        variable = aVariable;
    }
}
