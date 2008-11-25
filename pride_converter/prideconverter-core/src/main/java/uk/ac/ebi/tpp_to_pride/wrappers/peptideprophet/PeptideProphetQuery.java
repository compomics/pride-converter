/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 13-Sep-2006
 * Time: 14:56:10
 */
package uk.ac.ebi.tpp_to_pride.wrappers.peptideprophet;
/*
 * CVS information:
 *
 * $Revision: 1.1.1.1 $
 * $Date: 2007/01/12 17:17:10 $
 */

/**
 * This class wraps a PeptideProphet query and its associated search result.
 *
 * @author martlenn
 * @version $Id: PeptideProphetQuery.java,v 1.1.1.1 2007/01/12 17:17:10 lmartens Exp $
 */
public class PeptideProphetQuery {
    /**
     * The name of the MS run this query belongs to.
     */
    private String run = null;
    /**
     * The scan at which the spectrum recording commenced.
     */
    private String startScan = null;
    /**
     * The scan at which the spectrum recording ended.
     * Can be the same as 'startScan' for querioes derived
     * from a single scan.
     */
    private String endScan = null;

    /**
     * The experimental mass of the uncharged precursor
     */
    private double precursorMass = -1.0;

    /**
     * The charge state as deduced from the original spectrum.
     */
    private int assumed_charge = 0;

    /**
     * The index for this query in the original PeptideProphet output file.
     */
    private int index = -1;

    /**
     * The associated identification.
     */
    private PeptideProphetSearchHit searchHit = null;

    public PeptideProphetQuery(int aAssumed_charge, String aEndScan, int aIndex, double aPrecursorMass, String aRun, PeptideProphetSearchHit aSearchHit, String aStartScan) {
        assumed_charge = aAssumed_charge;
        endScan = aEndScan;
        index = aIndex;
        precursorMass = aPrecursorMass;
        run = aRun;
        searchHit = aSearchHit;
        startScan = aStartScan;
    }

    public int getAssumed_charge() {
        return assumed_charge;
    }

    public void setAssumed_charge(int aAssumed_charge) {
        assumed_charge = aAssumed_charge;
    }

    public String getEndScan() {
        return endScan;
    }

    public void setEndScan(String aEndScan) {
        endScan = aEndScan;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int aNdex) {
        index = aNdex;
    }

    public double getPrecursorMass() {
        return precursorMass;
    }

    public void setPrecursorMass(double aPrecursorMass) {
        precursorMass = aPrecursorMass;
    }

    public String getRun() {
        return run;
    }

    public void setRun(String aRun) {
        run = aRun;
    }

    public PeptideProphetSearchHit getSearchHit() {
        return searchHit;
    }

    public void setSearchHit(PeptideProphetSearchHit aSearchHit) {
        searchHit = aSearchHit;
    }

    public String getStartScan() {
        return startScan;
    }

    public void setStartScan(String aStartScan) {
        startScan = aStartScan;
    }
}
