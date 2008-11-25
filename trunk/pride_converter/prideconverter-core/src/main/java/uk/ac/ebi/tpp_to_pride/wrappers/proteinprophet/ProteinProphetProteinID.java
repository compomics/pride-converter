/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 21-Aug-2006
 * Time: 10:30:15
 */
package uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet;

import java.util.Collection;
import java.util.HashMap;
/*
 * CVS information:
 *
 * $Revision: 1.1.1.1 $
 * $Date: 2007/01/12 17:17:10 $
 */

/**
 * This class represents a ProteinProphet protein ID.
 *
 * @author martlenn
 * @version $Id: ProteinProphetProteinID.java,v 1.1.1.1 2007/01/12 17:17:10 lmartens Exp $
 */
public class ProteinProphetProteinID {

    /**
     * The primary protein accession number
     * (primary meaning: the accession number
     * from the database the identifications were done in)
     */
    private String accession = null;

    /**
     * The protein accession version.
     */
    private String version = null;

    /**
     * The protein sequence as retrieved form the original database file.
     */
    private String sequence = null;

    /**
     * The description line corresponding with the protein
     * accession number in the source database.
     */
    private String description = null;

    /**
     * Collection of Isoform instances, if any.
     */
    private Collection isoforms = null;

    /**
     * The probability score attributed by ProteinProphet
     */
    private double probability = -1.0;

    /**
     *  The protein sequence coverage, in percent.
     */
    private double percent_coverage = -1.0;

    /**
     * HahsMap with the peptides, with the modified sequence as
     * key, and a Collection of Peptide instances as values.
     */
    private HashMap peptides = null;


    /**
     * This constructor takes all relevant information necessary to
     * construct an instance.
     *
     * @param aAccession    String with the primary accession number in the
     *                      source databases.
     * @param aDescription  String with the protein description for the
     *                      accession number in the source database.
     * @param aPeptides     HashMap with the supporting peptides -
     *                      keyed by modified sequence and with a Collection
     *                      of Peptide instances as values.
     * @param aPercent_coverage double with the protein sequence coverage, in percent.
     * @param aProbability  double with the ProteinProphet probability.
     * @param aIsoforms Collection with the isoforms, if any (can be 'null' for none).
     */
    public ProteinProphetProteinID(String aAccession, String aDescription, HashMap aPeptides, double aPercent_coverage, double aProbability, Collection aIsoforms) {
        accession = aAccession;
        description = aDescription;
        peptides = aPeptides;
        percent_coverage = aPercent_coverage;
        probability = aProbability;
        isoforms = aIsoforms;
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

    public Collection getIsoforms() {
        return isoforms;
    }

    public void setIsoforms(Collection aIsoforms) {
        isoforms = aIsoforms;
    }

    public HashMap getPeptides() {
        return peptides;
    }

    public void setPeptides(HashMap aPeptides) {
        peptides = aPeptides;
    }

    public double getPercent_coverage() {
        return percent_coverage;
    }

    public void setPercent_coverage(double aPercent_coverage) {
        percent_coverage = aPercent_coverage;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double aProbability) {
        probability = aProbability;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String aSequence) {
        sequence = aSequence;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String aVersion) {
        version = aVersion;
    }
}
