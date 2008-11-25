/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 29-Aug-2006
 * Time: 08:51:36
 */
package uk.ac.ebi.tpp_to_pride.wrappers.peptideprophet;
/*
 * CVS information:
 *
 * $Revision: 1.1.1.1 $
 * $Date: 2007/01/12 17:17:10 $
 */

/**
 * This class wraps the data coming from an enzyme element in PeptideProphet output XML.
 *
 * @author martlenn
 * @version $Id: PeptideProphetEnzyme.java,v 1.1.1.1 2007/01/12 17:17:10 lmartens Exp $
 */
public class PeptideProphetEnzyme {

    /**
     * The name for the enzyme used.
     */
    private String enzymeName = null;

    /**
     * The amino acids where the enzyme cuts.
     */
    private String cut = null;

    /**
     * The amino acids that inhibit cutting.
     */
    private String noCut = null;

    /**
     * The 'cut sense', ie. C-terminal or N-terminal.
     */
    private String sense = null;


    /**
     * This constructor takes all required variables to create an enzyme.
     *
     * @param aEnzymeName   String with the name of the enzyme.
     * @param aCut  String with the amino acids where the enzyme cuts.
     * @param aNoCut    String with the amino acids that inhibit cutting.
     * @param aSense    String with the sense of cut ('C' or 'N')
     */
    public PeptideProphetEnzyme(String aEnzymeName, String aCut, String aNoCut, String aSense) {
        cut = aCut;
        enzymeName = aEnzymeName;
        noCut = aNoCut;
        sense = aSense;
    }

    public String getCut() {
        return cut;
    }

    public void setCut(String aCut) {
        cut = aCut;
    }

    public String getEnzymeName() {
        return enzymeName;
    }

    public void setEnzymeName(String aEnzymeName) {
        enzymeName = aEnzymeName;
    }

    public String getNoCut() {
        return noCut;
    }

    public void setNoCut(String aNoCut) {
        noCut = aNoCut;
    }

    public String getSense() {
        return sense;
    }

    public void setSense(String aSense) {
        sense = aSense;
    }


}
