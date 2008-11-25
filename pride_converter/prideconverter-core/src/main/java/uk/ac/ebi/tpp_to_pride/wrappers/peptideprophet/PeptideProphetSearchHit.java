/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 13-Sep-2006
 * Time: 15:59:44
 */
package uk.ac.ebi.tpp_to_pride.wrappers.peptideprophet;

import java.util.Collection;
import java.util.HashMap;
/*
 * CVS information:
 *
 * $Revision: 1.1.1.1 $
 * $Date: 2007/01/12 17:17:10 $
 */

/**
 * This class wraps the data encapsulated by the 'search_hit' tag in a
 *  PeptideProphet output file.
 *
 * @author martlenn
 * @version $Id: PeptideProphetSearchHit.java,v 1.1.1.1 2007/01/12 17:17:10 lmartens Exp $
 */
public class PeptideProphetSearchHit {

    /**
     * The hit rank for this hit.
     */
    private int hitRank = -1;
    /**
     * The sequence for this peptide hit.
     */
    private String sequence = null;
    /**
     * The amino acid directly N-terminal to the
     * sequence of this hit.
     */
    private String previous_AA = null;
    /**
     * The amino acid directly C-terminal to the
     * sequence of this hit.
     */
    private String next_AA = null;
    /**
     * The total number of proteins associated with this
     * peptide hit.
     */
    private int proteinCount = -1;
    /**
     * The main protein entry for this peptide hit.
     */
    private PeptideProphetProteinID protein = null;
    /**
     * Any additional proteins (as PeptideProphetProteinID instances)
     * associated with this peptide (if any).
     */
    private Collection additionalProteins = null;
    /**
     * The number of matched ions in this hit.
     */
    private int matchedIons = -1;
    /**
     * The total number of ions in this hit.
     */
    private int totalIons = -1;
    /**
     * The calculated mass for this peptide hit.
     */
    private double calculatedMass = -1.0;
    /**
     * The sign-prefixed mass difference for this peptide hit.
     */
    private String massDifference = null;
    /**
     * The number of missed cleavages for this hit.
     */
    private int num_missed_cleavages = -1;
    /**
     * Number indicating whether this peptide hit was rejected.
     */
    private int isRejected = 0;
    /**
     * The String with the modified sequence.
     */
    private String modifiedSequence = null;
    /**
     * This collection holds all modified amino acids (if any)
     * as PeptideProphetModifiedAminoAcid instances.
     */
    private Collection modifiedAminoAcids = null;
    /**
     * The different search scores obtained for this identification,
     * sorted by name and value as (key, value) pairs.
     */
    private HashMap searchScores = null;
    /**
     * The analysis performed.
     */
    private String analysis = null;
    /**
     * The resulting probability.
     */
    private double probability = -1.0;
    /**
     * The probability rounded to one decimal number.
     */
    private double roundedProbability = -1.0;
    /**
     * A summary of probabilities, separated by comma's.
     */
    private String all_ntt_prob = null;
    /**
     * The search score summary. (key,value) pairs represent
     * score name and value.
     */
    private HashMap searchScoreSummary = null;

    public PeptideProphetSearchHit(Collection aAdditionalProteins, String aAll_ntt_prob, String aAnalysis, double aCalculatedMass, int aHitRank, int aIsRejected, String aMassDifference, int aMatchedIons, Collection aModifiedAminoAcids, String aModifiedSequence, String aNext_AA, int aNum_missed_cleavages, String aPrevious_AA, double aProbability, double aRoundedProbability, PeptideProphetProteinID aProtein, int aProteinCount, HashMap aSearchScores, HashMap aSearchScoreSummary, String aSequence, int aTotalIons) {
        additionalProteins = aAdditionalProteins;
        all_ntt_prob = aAll_ntt_prob;
        analysis = aAnalysis;
        calculatedMass = aCalculatedMass;
        hitRank = aHitRank;
        isRejected = aIsRejected;
        massDifference = aMassDifference;
        matchedIons = aMatchedIons;
        modifiedAminoAcids = aModifiedAminoAcids;
        modifiedSequence = aModifiedSequence;
        next_AA = aNext_AA;
        num_missed_cleavages = aNum_missed_cleavages;
        previous_AA = aPrevious_AA;
        probability = aProbability;
        roundedProbability = aRoundedProbability;
        protein = aProtein;
        proteinCount = aProteinCount;
        searchScores = aSearchScores;
        searchScoreSummary = aSearchScoreSummary;
        sequence = aSequence;
        totalIons = aTotalIons;
    }

    public Collection getAdditionalProteins() {
        return additionalProteins;
    }

    public void setAdditionalProteins(Collection aAdditionalProteins) {
        additionalProteins = aAdditionalProteins;
    }

    public String getAll_ntt_prob() {
        return all_ntt_prob;
    }

    public void setAll_ntt_prob(String aAll_ntt_prob) {
        all_ntt_prob = aAll_ntt_prob;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String aAnalysis) {
        analysis = aAnalysis;
    }

    public double getCalculatedMass() {
        return calculatedMass;
    }

    public void setCalculatedMass(double aCalculatedMass) {
        calculatedMass = aCalculatedMass;
    }

    public int getHitRank() {
        return hitRank;
    }

    public void setHitRank(int aHitRank) {
        hitRank = aHitRank;
    }

    public int getsRejected() {
        return isRejected;
    }

    public void setsRejected(int aSRejected) {
        isRejected = aSRejected;
    }

    public String getMassDifference() {
        return massDifference;
    }

    public void setMassDifference(String aMassDifference) {
        massDifference = aMassDifference;
    }

    public int getMatchedIons() {
        return matchedIons;
    }

    public void setMatchedIons(int aMatchedIons) {
        matchedIons = aMatchedIons;
    }

    public Collection getModifiedAminoAcids() {
        return modifiedAminoAcids;
    }

    public void setModifiedAminoAcids(Collection aModifiedAminoAcids) {
        modifiedAminoAcids = aModifiedAminoAcids;
    }

    public String getModifiedSequence() {
        return modifiedSequence;
    }

    public void setModifiedSequence(String aModifiedSequence) {
        modifiedSequence = aModifiedSequence;
    }

    public String getNext_AA() {
        return next_AA;
    }

    public void setNext_AA(String aNext_AA) {
        next_AA = aNext_AA;
    }

    public int getNum_missed_cleavages() {
        return num_missed_cleavages;
    }

    public void setNum_missed_cleavages(int aNum_missed_cleavages) {
        num_missed_cleavages = aNum_missed_cleavages;
    }

    public String getPrevious_AA() {
        return previous_AA;
    }

    public void setPrevious_AA(String aPrevious_AA) {
        previous_AA = aPrevious_AA;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double aProbability) {
        probability = aProbability;
    }

    public PeptideProphetProteinID getProtein() {
        return protein;
    }

    public void setProtein(PeptideProphetProteinID aProtein) {
        protein = aProtein;
    }

    public int getProteinCount() {
        return proteinCount;
    }

    public void setProteinCount(int aProteinCount) {
        proteinCount = aProteinCount;
    }

    public HashMap getSearchScores() {
        return searchScores;
    }

    public void setSearchScores(HashMap aSearchScores) {
        searchScores = aSearchScores;
    }

    public HashMap getSearchScoreSummary() {
        return searchScoreSummary;
    }

    public void setSearchScoreSummary(HashMap aSearchScoreSummary) {
        searchScoreSummary = aSearchScoreSummary;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String aSequence) {
        sequence = aSequence;
    }

    public int getTotalIons() {
        return totalIons;
    }

    public void setTotalIons(int aTotalIons) {
        totalIons = aTotalIons;
    }

    public double getRoundedProbability() {
        return roundedProbability;
    }

    public void setRoundedProbability(double aRoundedProbability) {
        roundedProbability = aRoundedProbability;
    }
}
