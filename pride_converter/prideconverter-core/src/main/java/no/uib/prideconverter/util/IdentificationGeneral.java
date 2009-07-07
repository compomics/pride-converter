package no.uib.prideconverter.util;

import java.util.ArrayList;
import uk.ac.ebi.pride.model.implementation.core.ModificationImpl;
import uk.ac.ebi.pride.model.implementation.mzData.CvParamImpl;
import uk.ac.ebi.pride.model.implementation.mzData.UserParamImpl;
import uk.ac.ebi.pride.model.interfaces.core.FragmentIon;

/**
 * This class contains information about an identification.
 *
 * @author  Harald Barsnes
 * 
 * Created March 2008
 */
public class IdentificationGeneral {

    private String spectrumFileId;
    private String peptideAccession;
    private String searchEngine;
    private String database;
    private String databaseVersion;
    private String sequence;
    private Integer peptideStart;
    private Double score;
    private Double threshold;
    private String[] iTraqNorm;
    private String[] iTraqUT;
    private String[][] iTraqRatio;
    private ArrayList<CvParamImpl> cvParams;
    private ArrayList<ModificationImpl> modifications;
    private ArrayList<UserParamImpl> userParams;
    private ArrayList<FragmentIon> fragmentIons;

    /**
     * Creates a new IdentificationGeneral object that store details on a 
     * peptide identification.
     * 
     * @param spectrumFileId the spectra file id (filename or index)
     * @param peptideAccession accession number of the identified peptide
     * @param searchEngine the name of the searchEngine
     * @param database the name of the database used during identification
     * @param databaseVersion the version of the database
     * @param sequence the sequence of the peptide
     * @param start the start index if the peptide
     * @param score the score of the detected peptide
     * @param threshold the score threshold used during identification
     * @param iTraqNorm the iTRAQ normalization values
     * @param iTraqUT the iTRAQ UT values
     * @param iTraqRatio the iTRAQ ratios
     * @param cvParams cv parameters associated with the identification
     * @param userParameters user parameters associated with the identification
     * @param modifications the peptide modifications
     * @param fragmentIons the fragment ions
     */
    public IdentificationGeneral(String spectrumFileId, String peptideAccession,
            String searchEngine, String database, String databaseVersion,
            String sequence, Integer start,
            Double score, Double threshold,
            String[] iTraqNorm, String[] iTraqUT, String[][] iTraqRatio,
            ArrayList<CvParamImpl> cvParams,
            ArrayList<UserParamImpl> userParameters,
            ArrayList<ModificationImpl> modifications,
            ArrayList<FragmentIon> fragmentIons) {

        this.spectrumFileId = spectrumFileId;
        this.peptideAccession = peptideAccession;
        this.searchEngine = searchEngine;
        this.database = database;
        this.databaseVersion = databaseVersion;
        this.sequence = sequence;
        this.peptideStart = start;
        this.score = score;
        this.threshold = threshold;
        this.iTraqNorm = iTraqNorm;
        this.iTraqUT = iTraqUT;
        this.iTraqRatio = iTraqRatio;
        this.cvParams = cvParams;
        this.userParams = userParameters;
        this.modifications = modifications;
        this.fragmentIons = fragmentIons;
    }

    /**
     * Returns the spectrum file ID
     * 
     * @return the spectrum file ID
     */
    public String getSpectrumFileId() {
        return spectrumFileId;
    }

    /**
     * Sets the spectrum file ID
     * 
     * @param spectrumFileId
     */
    public void setSpectrumFileId(String spectrumFileId) {
        this.spectrumFileId = spectrumFileId;
    }

    /**
     * Returns the peptide accession
     * 
     * @return the peptide accession
     */
    public String getPeptideAccession() {
        return peptideAccession;
    }

    /**
     * Sets the peptide accession
     * 
     * @param peptideAccession
     */
    public void setPeptideAccession(String peptideAccession) {
        this.peptideAccession = peptideAccession;
    }

    /**
     * Returns the name if the searchEngine
     * 
     * @return the name if the searchEngine
     */
    public String getSearchEngine() {
        return searchEngine;
    }

    /**
     * Sets the name if the searchEngine
     * 
     * @param searchEngine
     */
    public void setSearchEngine(String searchEngine) {
        this.searchEngine = searchEngine;
    }

    /**
     * Returns the name of the database
     * 
     * @return the name of the database
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Sets the name of the database
     * 
     * @param database
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * Returns the database version
     * 
     * @return the database version
     */
    public String getDatabaseVersion() {
        return databaseVersion;
    }

    /**
     * Sets the database version
     * 
     * @param databaseVersion
     */
    public void setDatabaseVersion(String databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    /**
     * Returns the peptide sequence
     * 
     * @return the peptide sequence
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Sets the peptide sequence
     * 
     * @param sequence
     */
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    /**
     * Returns the peptide start index
     * 
     * @return the peptide start index
     */
    public Integer getPeptideStart() {
        return peptideStart;
    }

    /**
     * Sets the peptide start index
     * 
     * @param peptideStart
     */
    public void setPeptideStart(Integer peptideStart) {
        this.peptideStart = peptideStart;
    }

    /**
     * Returns the identification score
     * 
     * @return the identification score
     */
    public Double getScore() {
        return score;
    }

    /**
     * Sets the identification score
     * 
     * @param score
     */
    public void setScore(Double score) {
        this.score = score;
    }

    /**
     * Returns the identification threshold
     * 
     * @return the identification threshold
     */
    public Double getThreshold() {
        return threshold;
    }

    /**
     * Sets the identification threshold
     * 
     * @param threshold
     */
    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    /**
     * Returns the iTRAQ norm values
     * 
     * @return the iTRAQ norm values
     */
    public String[] getITraqNorm() {
        return iTraqNorm;
    }

    /**
     * Sets the iTRAQ norm values
     * 
     * @param iTraqNorm
     */
    public void setITraqNorm(String[] iTraqNorm) {
        this.iTraqNorm = iTraqNorm;
    }

    /**
     * Returns the iTRAQ UT values
     * 
     * @return the iTRAQ UT values
     */
    public String[] getITraqUT() {
        return iTraqUT;
    }

    /**
     * Sets the iTRAQ UT values
     * 
     * @param iTraqUT
     */
    public void setITraqUT(String[] iTraqUT) {
        this.iTraqUT = iTraqUT;
    }

    /**
     * Returns the iTRAQ ratios
     * 
     * @return the iTRAQ ratios
     */
    public String[][] getITraqRatio() {
        return iTraqRatio;
    }

    /**
     * Sets the iTRAQ ratios
     * 
     * @param iTraqRatio
     */
    public void setITraqRatio(String[][] iTraqRatio) {
        this.iTraqRatio = iTraqRatio;
    }

    /**
     * Returns the associated CV parameters
     * 
     * @return the associated CV parameters
     */
    public ArrayList<CvParamImpl> getCvParams() {
        return cvParams;
    }

    /**
     * Sets the associated CV parameters
     * 
     * @param cvParams
     */
    public void setCvParams(ArrayList<CvParamImpl> cvParams) {
        this.cvParams = cvParams;
    }
    
    /**
     * Returns the associated user parameters
     * 
     * @return the associated user parameters
     */
    public ArrayList<UserParamImpl> getUserParams() {
        return userParams;
    }

    /**
     * Sets the associated user parameters
     * 
     * @param userParams
     */
    public void setUserParams(ArrayList<UserParamImpl> userParams) {
        this.userParams = userParams;
    }

    /**
     * Returns the peptide modifications
     * 
     * @return the peptide modifications
     */
    public ArrayList<ModificationImpl> getModifications() {
        return modifications;
    }

    /**
     * Sets the peptide modifications
     * 
     * @param modifications
     */
    public void setModifications(ArrayList<ModificationImpl> modifications) {
        this.modifications = modifications;
    }

    /**
     * Returns the fragment ions
     *
     * @return the fragmentIons
     */
    public ArrayList<FragmentIon> getFragmentIons() {
        return fragmentIons;
    }

    /**
     * Sets the fragment ions
     *
     * @param fragmentIons the fragmentIons to set
     */
    public void setFragmentIons(ArrayList<FragmentIon> fragmentIons) {
        this.fragmentIons = fragmentIons;
    }
}
