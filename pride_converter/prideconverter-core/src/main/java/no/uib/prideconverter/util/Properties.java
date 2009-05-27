package no.uib.prideconverter.util;

import be.proteomics.mascotdatfile.util.mascot.ProteinHit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import uk.ac.ebi.jmzml.model.mzml.MzML;
import uk.ac.ebi.pride.model.interfaces.mzdata.CvParam;
import uk.ac.ebi.pride.model.interfaces.mzdata.MzData;
import uk.ac.ebi.pride.model.interfaces.mzdata.SourceFile;
import uk.ac.ebi.pride.model.interfaces.mzdata.UserParam;

/**
 * This class contains many of the properties that are used during the 
 * conversion, but that are not stored in the UserProperties.prop file 
 * between each run of the program.
 *
 * @author  Harald Barsnes
 * 
 * Created October 2008
 */
public class Properties {

    public final int FRAME_WIDTH = 650;  //the hardcoded width of all frames
    public final int FRAME_HEIGHT = 600; //the hardcoded height of all frames
    public final int PROTEIN_ISOFORMS_ALWAYS_SELECT_FIRST = 0;
    public final int PROTEIN_ISOFORMS_MAUNAL_SELECTION = 1;
    public final int PROTEIN_ISOFORMS_PROVIDE_LIST = 2;
    public final int ZERO_FILLED_NUMBER_LENGTH = 5;
    public final int MZ_ARRAY = 0;
    public final int INTENSITIES_ARRAY = 1;
    public final int FRAGMENT_ION_CHARGE_STATES_ARRAY = 2;
    public final int NUMBER_OF_BYTES_PER_MEGABYTE = 1048576;
    public final double MAX_MASCOT_DAT_FILESIZE_BEFORE_INDEXING = 40; //in megabytes
    public final double PROTON_MASS = 1.00727646677;
    public final double HYDROGEN_MASS = 1.00794;
    
    private boolean useCommaAsDecimalSymbol = false;
    private boolean roundMascotScoreAndThresholdDownToNearestInteger = false;
    private boolean isoFormsSelectionTypeSelected = false;
    private boolean resubmission = false;
    private String prideAccessionNumber = null; //only used when resubmitting
    private boolean selectAllSpectra = true;
    private boolean selectAllIdentifiedSpectra = false;
    private String spectraSelectionCriteria = null;
    private boolean selectionCriteriaIsFileName = true;
    private double peptideScoreThreshold = 0.0;
    private String dataSource = ""; //e.g. Mascot Dat Files, PKL Files etc.
    private ArrayList<String> selectedSourceFiles = null;
    private ArrayList<String> selectedIdentificationFiles = null;
    private String proteinProphetFileName = null;
    private String peptideProphetFileName = null;
    private String dtaSelectFileName = null;
    private String spectrumFilesFolderName = null;
    private ArrayList selectedSpectraKeys;
    private ArrayList selectedIsoformAccessions;
    private ArrayList selectedIsoformPeptideSequences;
    private int proteinIsoformSelectionType;
    private double mascotConfidenceLevel = 95;
    private double proteinProphetThreshold = 0.9;
    private double peptideProphetThreshold = 0.9;
    private boolean instrumentDetailsExtracted = false;
    private boolean contactInfoExtracted = false;
    private boolean sampleDetailsExtracted = false;
    private boolean dataFileHasBeenLoaded = false;
    private ArrayList alreadyChoosenModifications;
    private ArrayList<Long> projectIds;
    private String passwordDatabase;
    private DefaultTableModel spectrumTableModel = null;
    private MzData mzDataFile;
    private MzML mzML;
    private boolean isGelFree = true;
    private FileFilter lastUsedFileFilter;

    private HashMap<String, ArrayList<CvParam>> spectrumCvParams;
    private HashMap<String, ArrayList<UserParam>> spectrumUserParams;
    private String proteinIdentificationFilter = "";
    private String sequestParamFile = "";
    
    // contact information
    private Collection contacts;
    
    // instrument source CV parameters
    private String accessionInstrumentSourceParameter = "";
    private String cVLookupInstrumentSourceParameter = "";
    private String nameInstrumentSourceParameter = "";
    private String valueInstrumentSourceParameter = null;
    
    // instrument detector parameters
    private String accessionInstrumentDetectorParamater = "";
    private String cVLookupInstrumentDetectorParamater = "";
    private String nameInstrumentDetectorParamater = "";
    private String valueInstrumentDetectorParamater = null;
    
    // analyzer collection
    private Collection analyzerParams;
    private Collection analyzerList;
    
    // processing methods
    private Collection processingMethod;
    
    // sample stuff
    private Collection sampleDescriptionCVParams;
    private ArrayList sampleDescriptionCVParamsQuantification;
    private ArrayList currentQuantificationSelection = null;
    private ArrayList sampleDescriptionUserSubSampleNames;
    
    // mzData
    private String softwareCompletionTime = null;
    private String instrumentName = "";
    private Collection processingMethodUserParams = null;
    private SourceFile sourceFile = null;
    private Collection instrumentAdditionalCvParams = null;
    private Collection instrumentAdditionalUserParams = null;
    private Collection instrumentDetectorUserParams = null;
    private String sampleDescriptionComment = null;
    private String softwareComments = null;
    private Collection instrumentSourceUserParams = null;
    private String softwareVersion = "";
    private String softwareName = "";
    
    // references
    private Collection references;
    
    // experiment proper
    private String experimentTitle = "";
    private String experimentDescription = "";
    private String experimentLabel = "";
    private String experimentProject = "";
    private Collection experimentProtocolSteps = null;
    private Collection protocolStepsCVParams = null;
    private Collection protocolStepsUserParams = null;
    
    // user parameters
    private Collection experimentUserParameters;
    private String mzDataVersion = "1.05";
    private String mzDataAccessionNumber = "0";
    
    // CV lookup stuff
    private String cvVersion = "1.0.0";
    private String cvFullName = "The PSI Ontology";
    private String cvLabel = "PSI";
    private String cvAddress = "http://psidev.sourceforge.net/ontology/";
    private ProteinHit tempProteinHit;
    private ArrayList selectedProteinHits;

    /**
     * Creates a new (mostly empty) Properties object.
     */
    public Properties() {
        projectIds = new ArrayList();
        selectedSpectraKeys = new ArrayList();
        processingMethod = new ArrayList();
        sampleDescriptionCVParams = new ArrayList();
        sampleDescriptionCVParamsQuantification = new ArrayList();
        sampleDescriptionUserSubSampleNames = new ArrayList();
        experimentUserParameters = new ArrayList();
        experimentProtocolSteps = new ArrayList();
        contacts = new ArrayList();
        analyzerList = new ArrayList();
        references = new ArrayList();
        selectedSourceFiles = new ArrayList();
        spectrumCvParams = new HashMap<String, ArrayList<CvParam>>();
        spectrumUserParams = new HashMap<String, ArrayList<UserParam>>();
    }

    /**
     * Returns true if comma is to by used as the decimal symbol.
     * 
     * @return true if comma is to by used as the decimal symbol
     */
    public boolean isCommaTheDecimalSymbol() {
        return useCommaAsDecimalSymbol;
    }

    /**
     * Sets if comma is to be used as the decimal symbol
     * 
     * @param useCommaAsDecimalSymbol
     */
    public void useCommaAsDecimalSymbol(boolean useCommaAsDecimalSymbol) {
        this.useCommaAsDecimalSymbol = useCommaAsDecimalSymbol;
    }

    /**
     * Returns the current ms_lims database password.
     * 
     * @return the current ms_lims database password
     */
    public String getPassWord() {
        return passwordDatabase;
    }

    /**
     * Set the current ms_lims database password
     * 
     * @param passWord
     */
    public void setPassWord(String passWord) {
        this.passwordDatabase = passWord;
    }

    /**
     * Returns true if Mascot scores and thresholds should be rounded down 
     * to the nearest integer. 
     * 
     * @return true if Mascot scores and thresholds should be rounded down 
     *         to the nearest integer
     */
    public boolean roundMascotScoreAndThresholdDownToNearestInteger() {
        return roundMascotScoreAndThresholdDownToNearestInteger;
    }

    /**
     * Sets if Mascot scores and thresholds should be rounded down 
     * to the nearest integer.
     * 
     * @param aRoundMascotScoreAndThresholdDownToNearestInteger
     */
    public void setRoundMascotScoreAndThresholdDownToNearestInteger(
            boolean aRoundMascotScoreAndThresholdDownToNearestInteger) {
        roundMascotScoreAndThresholdDownToNearestInteger =
                aRoundMascotScoreAndThresholdDownToNearestInteger;
    }

    /**
     * Returns true if isoform selection type is selected.
     * 
     * @return true if isoform selection type is selected
     */
    public boolean isIsoFormsSelectionTypeSelected() {
        return isoFormsSelectionTypeSelected;
    }

    /**
     * Sets if the isoform selection type is selected.
     * 
     * @param aIsoFormsSelectionTypeSelected
     */
    public void setIsoFormsSelectionTypeSelected(boolean aIsoFormsSelectionTypeSelected) {
        isoFormsSelectionTypeSelected = aIsoFormsSelectionTypeSelected;
    }

    /**
     * Returns true if the current submission is a resubmission.
     * 
     * @return true if the current submission is a resubmission
     */
    public boolean isResubmission() {
        return resubmission;
    }

    /**
     * Sets if the current submission is a resubmission.
     * 
     * @param aResubmission
     */
    public void setResubmission(boolean aResubmission) {
        resubmission = aResubmission;
    }

    /**
     * Returns the current PRIDE accession number.
     * 
     * @return the current PRIDE accession number
     */
    public String getPrideAccessionNumber() {
        return prideAccessionNumber;
    }

    /**
     * Sets the the current PRIDE accession number.
     * 
     * @param aPrideAccessionNumber
     */
    public void setPrideAccessionNumber(String aPrideAccessionNumber) {
        prideAccessionNumber = aPrideAccessionNumber;
    }

    /**
     * Returns true if all the spectra should be selected.
     * 
     * @return true if all the spectra should be selected.
     */
    public boolean selectAllSpectra() {
        return selectAllSpectra;
    }

    /**
     * Sets if all the spectra should be selected.
     * 
     * @param aSelectAllSpectra
     */
    public void setSelectAllSpectra(boolean aSelectAllSpectra) {
        selectAllSpectra = aSelectAllSpectra;
    }

    /**
     * Returns true if all identified spectra should be selected.
     * 
     * @return true if all identified spectra should be selected
     */
    public boolean selectAllIdentifiedSpectra() {
        return selectAllIdentifiedSpectra;
    }

    /**
     * Sets if all identified spectra should be selected.
     * 
     * @param aSelectAllIdentifiedSpectra
     */
    public void setSelectAllIdentifiedSpectra(boolean aSelectAllIdentifiedSpectra) {
        selectAllIdentifiedSpectra = aSelectAllIdentifiedSpectra;
    }

    /**
     * Returns the spectra selection criteria.
     * 
     * @return the spectra selection criteria
     */
    public String getSpectraSelectionCriteria() {
        return spectraSelectionCriteria;
    }

    /**
     * Sets the spectra selection criteria.
     * 
     * @param aSelectionCriteria
     */
    public void setSpectraSelectionCriteria(String aSelectionCriteria) {
        spectraSelectionCriteria = aSelectionCriteria;
    }

    /**
     * Returns true if the spectra selection criteria are filenames, 
     * and false if its spectra id.
     * 
     * @return true if the spectra selection criteria are filenames, 
     *         and false if its spectra id
     */
    public boolean isSelectionCriteriaFileName() {
        return selectionCriteriaIsFileName;
    }

    /**
     * Sets if the spectra selection criteria are filenames. Setting to false 
     * means that its spectra id.
     * 
     * @param aSelectionCriteriaIsFileName
     */
    public void setSelectionCriteriaIsFileName(boolean aSelectionCriteriaIsFileName) {
        selectionCriteriaIsFileName = aSelectionCriteriaIsFileName;
    }

    /**
     * Return the peptide score threshold.
     * 
     * @return the peptide score threshold
     */
    public double getPeptideScoreThreshold() {
        return peptideScoreThreshold;
    }

    /**
     * Sets the peptide score threshold.
     * 
     * @param aPeptideScoreThreshold
     */
    public void setPeptideScoreThreshold(double aPeptideScoreThreshold) {
        peptideScoreThreshold = aPeptideScoreThreshold;
    }
    
    /**
     * Return the PeptideProphet threshold.
     * 
     * @return the PeptideProphet threshold
     */
    public double getPeptideProphetThreshold() {
        return peptideProphetThreshold;
    }

    /**
     * Sets the PeptideProphet threshold.
     * 
     * @param aPeptideProphetThreshold
     */
    public void setPeptideProphetThreshold(double aPeptideProphetThreshold) {
        peptideProphetThreshold = aPeptideProphetThreshold;
    }
    
    /**
     * Return the ProteinProphet threshold.
     * 
     * @return the ProteinProphet threshold
     */
    public double getProteinProphetThreshold() {
        return proteinProphetThreshold;
    }

    /**
     * Sets the ProteinProphet threshold.
     * 
     * @param aProteinProphetThreshold
     */
    public void setProteinProphetThreshold(double aProteinProphetThreshold) {
        proteinProphetThreshold = aProteinProphetThreshold;
    }

    /**
     * Returns the current data source (e.g. PKL Files or Mascot Dat Files).
     * 
     * @return the current data source
     */
    public String getDataSource() {
        return dataSource;
    }

    /**
     * Set the current data source (e.g. PKL Files or Mascot Dat Files).
     * 
     * @param aDataSource
     */
    public void setDataSource(String aDataSource) {
        dataSource = aDataSource;
    }

    /**
     * Returns the selected source files.
     * 
     * @return the selected source files
     */
    public ArrayList<String> getSelectedSourceFiles() {
        return selectedSourceFiles;
    }

    /**
     * Sets the selected source files.
     * 
     * @param aSelectedSourceFiles
     */
    public void setSelectedSourceFiles(ArrayList<String> aSelectedSourceFiles) {
        selectedSourceFiles = aSelectedSourceFiles;
    }

    /**
     * Returns the selected identification files.
     * 
     * @return the selected identification files
     */
    public ArrayList<String> getSelectedIdentificationFiles() {
        return selectedIdentificationFiles;
    }

    /**
     * Set the selected identification files.
     * 
     * @param aSelectedIdentificationFiles
     */
    public void setSelectedIdentificationFiles(ArrayList<String> aSelectedIdentificationFiles) {
        selectedIdentificationFiles = aSelectedIdentificationFiles;
    }

    /**
     * Returns the selected spectra keys.
     *
     * @return the selected spectra keys
     */
    public ArrayList getSelectedSpectraKeys() {
        return selectedSpectraKeys;
    }

    /**
     * Sets the selected spectra keys.
     * 
     * @param aSelectedSpectraKeys
     */
    public void setSelectedSpectraKeys(ArrayList aSelectedSpectraKeys) {
        selectedSpectraKeys = aSelectedSpectraKeys;
    }

    /**
     * Returns the selected protein isoform accesion numbers.
     * 
     * @return the selected protein isoform accesion numbers
     */
    public ArrayList getSelectedIsoformAccessions() {
        return selectedIsoformAccessions;
    }

    /**
     * Sets the selected protein isoform accesion numbers.
     * 
     * @param aSelectedIsoformAccessions
     */
    public void setSelectedIsoformAccessions(ArrayList aSelectedIsoformAccessions) {
        selectedIsoformAccessions = aSelectedIsoformAccessions;
    }

    /**
     * Returns the selected isoform peptide sequences.
     * 
     * @return the selected isoform peptide sequences
     */
    public ArrayList getSelectedIsoformPeptideSequences() {
        return selectedIsoformPeptideSequences;
    }

    /**
     * Sets the selected isoform peptide sequences.
     * 
     * @param aSelectedIsoformPeptideSequences
     */
    public void setSelectedIsoformPeptideSequences(ArrayList aSelectedIsoformPeptideSequences) {
        selectedIsoformPeptideSequences = aSelectedIsoformPeptideSequences;
    }

    /**
     * Returns the protein isoform selection type. 
     * 
     * PROTEIN_ISOFORMS_ALWAYS_SELECT_FIRST = 0;
     * PROTEIN_ISOFORMS_MAUNAL_SELECTION = 1;
     * PROTEIN_ISOFORMS_PROVIDE_LIST = 2;
     * 
     * @return the protein isoform selection type: 0, 1 or 2.
     */
    public int getProteinIsoformSelectionType() {
        return proteinIsoformSelectionType;
    }

    /**
     * Sets the protein isoform selection type.
     * 
     * PROTEIN_ISOFORMS_ALWAYS_SELECT_FIRST = 0;
     * PROTEIN_ISOFORMS_MAUNAL_SELECTION = 1;
     * PROTEIN_ISOFORMS_PROVIDE_LIST = 2;
     * 
     * @param aProteinIsoformSelectionType the protein isoform selection type: 0, 1 or 2.
     */
    public void setProteinIsoformSelectionType(int aProteinIsoformSelectionType) {
        proteinIsoformSelectionType = aProteinIsoformSelectionType;
    }

    /**
     * Returns the Mascot confidence level.
     * 
     * @return the Mascot confidence level
     */
    public double getMascotConfidenceLevel() {
        return mascotConfidenceLevel;
    }

    /**
     * Sets the Mascot confidence level.
     * 
     * @param aMascotConfidenceLevel
     */
    public void setMascotConfidenceLevel(double aMascotConfidenceLevel) {
        mascotConfidenceLevel = aMascotConfidenceLevel;
    }

    /**
     * Returns true if the instrument details are extracted.
     * 
     * @return true if the instrument details are extracted
     */
    public boolean areInstrumentDetailsExtracted() {
        return instrumentDetailsExtracted;
    }

    /**
     * Sets if the instrument details have been extracted.
     * 
     * @param aInstrumentDetailsExtracted
     */
    public void setInstrumentDetailsExtracted(boolean aInstrumentDetailsExtracted) {
        instrumentDetailsExtracted = aInstrumentDetailsExtracted;
    }

    /**
     * Returns the list of already mapped modifications. The ArrayList contains the 
     * modification names.
     * 
     * @return the list of already mapped modifications
     */
    public ArrayList getAlreadyChoosenModifications() {
        return alreadyChoosenModifications;
    }

    /**
     * Sets the list of already mapped modifications. The ArrayList contains the 
     * modification names.
     * 
     * @param aAlreadyChoosenModifications
     */
    public void setAlreadyChoosenModifications(ArrayList aAlreadyChoosenModifications) {
        alreadyChoosenModifications = aAlreadyChoosenModifications;
    }

    /**
     * Returns the selected ms_lims project IDs.
     * 
     * @return the selected ms_lims project IDs
     */
    public ArrayList<Long> getProjectIds() {
        return projectIds;
    }

    /**
     * Sets the selected ms_lims project IDs.
     * 
     * @param aProjectIds
     */
    public void setProjectIds(ArrayList<Long> aProjectIds) {
        projectIds = aProjectIds;
    }

    /**
     * Returns the current spectrum table model for the spectra selection frame.
     * 
     * @return the current spectrum table model
     */
    public DefaultTableModel getSpectrumTableModel() {
        return spectrumTableModel;
    }

    /**
     * Sets the current spectrum table model for the spectra selection frame.
     * 
     * @param spectrumTableModel
     */
    public void setSpectrumTableModel(DefaultTableModel spectrumTableModel) {
        this.spectrumTableModel = spectrumTableModel;
    }

    /**
     * Returns the protocol step user parameters.
     * 
     * @return the protocol step user parameters
     */
    public Collection getProtocolStepsUserParams() {
        return protocolStepsUserParams;
    }

    /**
     * Sets the protocol step user parameters.
     * 
     * @param protocolStepsUserParams
     */
    public void setProtocolStepsUserParams(Collection protocolStepsUserParams) {
        this.protocolStepsUserParams = protocolStepsUserParams;
    }

    /**
     * Returns the experiment user parameters.
     * 
     * @return the experiment user parameters
     */
    public Collection getExperimentUserParameters() {
        return experimentUserParameters;
    }

    /**
     * Sets the experiment user parameters.
     * 
     * @param experimentUserParameters
     */
    public void setExperimentUserParameters(Collection experimentUserParameters) {
        this.experimentUserParameters = experimentUserParameters;
    }

    /**
     * Returns the mzData version.
     * 
     * @return the mzData version
     */
    public String getMzDataVersion() {
        return mzDataVersion;
    }

    /**
     * Returns the mzData accession number.
     * 
     * @return the mzData accession number
     */
    public String getMzDataAccessionNumber() {
        return mzDataAccessionNumber;
    }

    /**
     * Returns the version number of the currently included CV (PSI). 
     * 
     * @return the version number of the currently included CV (PSI)
     */
    public String getCvVersion() {
        return cvVersion;
    }

    /**
     * Returns the full name of the currently included CV (PSI).
     * 
     * @return the full name of the currently included CV (PSI)
     */
    public String getCvFullName() {
        return cvFullName;
    }

    /**
     * Returns the label of the currently included CV (PSI).
     * 
     * @return the label of the currently included CV (PSI)
     */
    public String getCvLabel() {
        return cvLabel;
    }

    /**
     * Returns the address of the currently included CV (PSI).
     * 
     * @return the address of the currently included CV (PSI)
     */
    public String getCvAddress() {
        return cvAddress;
    }

    /**
     * Returns the instrument additional CV parameters.
     * 
     * @return the instrument additional CV parameters
     */
    public Collection getInstrumentAdditionalCvParams() {
        return instrumentAdditionalCvParams;
    }

    /**
     * Sets the instrument additional CV parameters.
     * 
     * @param instrumentAdditionalCvParams
     */
    public void setInstrumentAdditionalCvParams(Collection instrumentAdditionalCvParams) {
        this.instrumentAdditionalCvParams = instrumentAdditionalCvParams;
    }

    /**
     * Returns the instrument additional user parameters.
     * 
     * @return the instrument additional user parameters
     */
    public Collection getInstrumentAdditionalUserParams() {
        return instrumentAdditionalUserParams;
    }

    /**
     * Sets the instrument additional user parameters.
     * 
     * @param instrumentAdditionalUserParams
     */
    public void setInstrumentAdditionalUserParams(Collection instrumentAdditionalUserParams) {
        this.instrumentAdditionalUserParams = instrumentAdditionalUserParams;
    }

    /**
     * Returns the instrument detector user parameters.
     * 
     * @return the instrument detector user parameters
     */
    public Collection getInstrumentDetectorUserParams() {
        return instrumentDetectorUserParams;
    }

    /**
     * Sets the instrument detector user parameters.
     * 
     * @param instrumentDetectorUserParams
     */
    public void setInstrumentDetectorUserParams(Collection instrumentDetectorUserParams) {
        this.instrumentDetectorUserParams = instrumentDetectorUserParams;
    }

    /**
     * Returns the sample description comment.
     * 
     * @return the sample description comment
     */
    public String getSampleDescriptionComment() {
        return sampleDescriptionComment;
    }

    /**
     * Sets the sample description comment.
     * 
     * @param sampleDescriptionComment
     */
    public void setSampleDescriptionComment(String sampleDescriptionComment) {
        this.sampleDescriptionComment = sampleDescriptionComment;
    }

    /**
     * Returns the software comments.
     * 
     * @return the software comments
     */
    public String getSoftwareComments() {
        return softwareComments;
    }

    /**
     * Sets the software comments.
     * 
     * @param softwareComments
     */
    public void setSoftwareComments(String softwareComments) {
        this.softwareComments = softwareComments;
    }

    /**
     * Returns the instrument source user parameters.
     * 
     * @return the instrument source user parameters
     */
    public Collection getInstrumentSourceUserParams() {
        return instrumentSourceUserParams;
    }

    /**
     * Sets the instrument source user parameters.
     * 
     * @param instrumentSourceUserParams
     */
    public void setInstrumentSourceUserParams(Collection instrumentSourceUserParams) {
        this.instrumentSourceUserParams = instrumentSourceUserParams;
    }

    /**
     * Returns the software version.
     * 
     * @return the software version
     */
    public String getSoftwareVersion() {
        return softwareVersion;
    }

    /**
     * Sets the software version.
     * 
     * @param softwareVersion
     */
    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    /**
     * Returns the software name.
     * 
     * @return the software name
     */
    public String getSoftwareName() {
        return softwareName;
    }

    /**
     * Sets the software name.
     * 
     * @param softwareName
     */
    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }

    /**
     * Returns the references.
     * 
     * @return the references
     */
    public Collection getReferences() {
        return references;
    }

    /**
     * Sets the references.
     * 
     * @param references
     */
    public void setReferences(Collection references) {
        this.references = references;
    }

    /**
     * Returns the experiment title.
     * 
     * @return the experiment title
     */
    public String getExperimentTitle() {
        return experimentTitle;
    }

    /**
     * Sets the experiment title.
     * 
     * @param experimentTitle
     */
    public void setExperimentTitle(String experimentTitle) {
        this.experimentTitle = experimentTitle;
    }

    /**
     * Returns the experiment description.
     *
     * @return the experiment description
     */
    public String getExperimentDescription() {
        return experimentDescription;
    }

    /**
     * Sets the experiment description.
     *
     * @param experimentDescription
     */
    public void setExperimentDescription(String experimentDescription) {
        this.experimentDescription = experimentDescription;
    }

    /**
     * Returns the experiment label.
     * 
     * @return the experiment label
     */
    public String getExperimentLabel() {
        return experimentLabel;
    }

    /**
     * Sets the experiment label.
     * 
     * @param experimentLabel
     */
    public void setExperimentLabel(String experimentLabel) {
        this.experimentLabel = experimentLabel;
    }

    /**
     * Returns the experiment project name.
     * 
     * @return the experiment project name
     */
    public String getExperimentProject() {
        return experimentProject;
    }

    /**
     * Sets the experiment project name.
     * 
     * @param experimentProject
     */
    public void setExperimentProject(String experimentProject) {
        this.experimentProject = experimentProject;
    }

    /**
     * Returns the experiment protocol steps.
     * 
     * @return the experiment protocol steps
     */
    public Collection getExperimentProtocolSteps() {
        return experimentProtocolSteps;
    }

    /**
     * Sets the experiment protocol steps.
     * 
     * @param experimentProtocolSteps
     */
    public void setExperimentProtocolSteps(Collection experimentProtocolSteps) {
        this.experimentProtocolSteps = experimentProtocolSteps;
    }

    /**
     * Returns the protocol steps CV parameters.
     * 
     * @return the protocol steps CV parameters
     */
    public Collection getProtocolStepsCVParams() {
        return protocolStepsCVParams;
    }

    /**
     * Sets the protocol steps CV parameters.
     * 
     * @param protocolStepsCVParams
     */
    public void setProtocolStepsCVParams(Collection protocolStepsCVParams) {
        this.protocolStepsCVParams = protocolStepsCVParams;
    }

    /**
     * Returns the value instrument source parameter.
     * 
     * @return the value instrument source parameter
     */
    public String getValueInstrumentSourceParameter() {
        return valueInstrumentSourceParameter;
    }

    /**
     * Sets the value instrument source parameter.
     * 
     * @param valueInstrumentSourceParameter
     */
    public void setValueInstrumentSourceParameter(String valueInstrumentSourceParameter) {
        this.valueInstrumentSourceParameter = valueInstrumentSourceParameter;
    }

    /**
     * Returns the accession number for the instrument detector.
     * 
     * @return the accession number for the instrument detector
     */
    public String getAccessionInstrumentDetectorParamater() {
        return accessionInstrumentDetectorParamater;
    }

    /**
     * Sets the accession number for the instrument detector.
     * 
     * @param accessionInstrumentDetectorParamater
     */
    public void setAccessionInstrumentDetectorParamater(String accessionInstrumentDetectorParamater) {
        this.accessionInstrumentDetectorParamater = accessionInstrumentDetectorParamater;
    }

    /**
     * Returns the ontology for the instrument detector.
     * 
     * @return the ontology for the instrument detector
     */
    public String getCVLookupInstrumentDetectorParamater() {
        return cVLookupInstrumentDetectorParamater;
    }

    /**
     * Sets the ontology for the instrument detector.
     * 
     * @param cVLookupInstrumentDetectorParamater
     */
    public void setCVLookupInstrumentDetectorParamater(String cVLookupInstrumentDetectorParamater) {
        this.cVLookupInstrumentDetectorParamater = cVLookupInstrumentDetectorParamater;
    }

    /**
     * Returns the name of the instrument detctor.
     * 
     * @return the name of the instrument detctor
     */
    public String getNameInstrumentDetectorParamater() {
        return nameInstrumentDetectorParamater;
    }

    /**
     * Sets the name of the instrument detctor.
     * 
     * @param nameInstrumentDetectorParamater
     */
    public void setNameInstrumentDetectorParamater(String nameInstrumentDetectorParamater) {
        this.nameInstrumentDetectorParamater = nameInstrumentDetectorParamater;
    }

    /**
     * Returns the value for the instrument detector.
     * 
     * @return the value for the instrument detector
     */
    public String getValueInstrumentDetectorParamater() {
        return valueInstrumentDetectorParamater;
    }

    /**
     * Sets the value for the instrument detector.
     * 
     * @param valueInstrumentDetectorParamater
     */
    public void setValueInstrumentDetectorParamater(String valueInstrumentDetectorParamater) {
        this.valueInstrumentDetectorParamater = valueInstrumentDetectorParamater;
    }

    /**
     * Returns the analyzer parameters.
     * 
     * @return the analyzer parameters
     */
    public Collection getAnalyzerParams() {
        return analyzerParams;
    }

    /**
     * Sets the analyzer parameters.
     * 
     * @param analyzerParams
     */
    public void setAnalyzerParams(Collection analyzerParams) {
        this.analyzerParams = analyzerParams;
    }

    /**
     * Returns the list of analyzers.
     * 
     * @return the list of analyzers
     */
    public Collection getAnalyzerList() {
        return analyzerList;
    }

    /**
     * Sets the list of analyzers.
     * 
     * @param analyzerList
     */
    public void setAnalyzerList(Collection analyzerList) {
        this.analyzerList = analyzerList;
    }

    /**
     * Returns the list of processing methods.
     * 
     * @return the list of processing methods
     */
    public Collection getProcessingMethod() {
        return processingMethod;
    }

    /**
     * Sets the list of processing methods. 
     *
     * @param processingMethod
     */
    public void setProcessingMethod(Collection processingMethod) {
        this.processingMethod = processingMethod;
    }

    /**
     * Returns the sample description CV parameters.
     * 
     * @return the sample description CV parameters
     */
    public Collection getSampleDescriptionCVParams() {
        return sampleDescriptionCVParams;
    }

    /**
     * Sets the the sample description CV parameters.
     * 
     * @param sampleDescriptionCVParams
     */
    public void setSampleDescriptionCVParams(Collection sampleDescriptionCVParams) {
        this.sampleDescriptionCVParams = sampleDescriptionCVParams;
    }

    /**
     * Returns the sample description CV parameters for the quantification.
     * 
     * @return the sample description CV parameters for the quantification
     */
    public ArrayList getSampleDescriptionCVParamsQuantification() {
        return sampleDescriptionCVParamsQuantification;
    }

    /**
     * Sets the sample description CV parameters for the quantification.
     * 
     * @param sampleDescriptionCVParamsQuantification
     */
    public void setSampleDescriptionCVParamsQuantification(ArrayList sampleDescriptionCVParamsQuantification) {
        this.sampleDescriptionCVParamsQuantification = sampleDescriptionCVParamsQuantification;
    }

    /**
     * Returns the list if currently used quantification labels.
     * 
     * @return the list if currently used quantification labels
     */
    public ArrayList getCurrentQuantificationSelection() {
        return currentQuantificationSelection;
    }

    /**
     * Sets the list if currently used quantification labels.
     * 
     * @param currentQuantificationSelection
     */
    public void setCurrentQuantificationSelection(ArrayList currentQuantificationSelection) {
        this.currentQuantificationSelection = currentQuantificationSelection;
    }

    /**
     * Returns the sample description user subsample names.
     * 
     * @return the sample description user subsample names
     */
    public ArrayList getSampleDescriptionUserSubSampleNames() {
        return sampleDescriptionUserSubSampleNames;
    }

    /**
     * Sets the sample description user subsample names.
     * 
     * @param sampleDescriptionUserSubSampleNames
     */
    public void setSampleDescriptionUserSubSampleNames(ArrayList sampleDescriptionUserSubSampleNames) {
        this.sampleDescriptionUserSubSampleNames = sampleDescriptionUserSubSampleNames;
    }

    /**
     * Returns the software completion time.
     * 
     * @return the software completion time
     */
    public String getSoftwareCompletionTime() {
        return softwareCompletionTime;
    }

    /**
     * Sets the software completion time.
     * 
     * @param softwareCompletionTime
     */
    public void setSoftwareCompletionTime(String softwareCompletionTime) {
        this.softwareCompletionTime = softwareCompletionTime;
    }

    /**
     * Returns the instrument name.
     * 
     * @return the instrument name
     */
    public String getInstrumentName() {
        return instrumentName;
    }

    /**
     * Sets the instrument name.
     * 
     * @param instrumentName
     */
    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    /**
     * Returns the processing methods user parameters.
     * 
     * @return the processing methods user parameters
     */
    public Collection getProcessingMethodUserParams() {
        return processingMethodUserParams;
    }

    /**
     * Sets the processing methods user parameters.
     * 
     * @param processingMethodUserParams
     */
    public void setProcessingMethodUserParams(Collection processingMethodUserParams) {
        this.processingMethodUserParams = processingMethodUserParams;
    }

    /**
     * Returns the current source file.
     * 
     * @return the current source file
     */
    public SourceFile getSourceFile() {
        return sourceFile;
    }

    /**
     * Returns the list of contacts.
     * 
     * @return the list of contacts
     */
    public Collection getContacts() {
        return contacts;
    }

    /**
     * Sets the list of contacts.
     * 
     * @param contacts
     */
    public void setContacts(Collection contacts) {
        this.contacts = contacts;
    }

    /**
     * Returns the accession numbers for the instrument source.
     * 
     * @return the accession numbers for the instrument source
     */
    public String getAccessionInstrumentSourceParameter() {
        return accessionInstrumentSourceParameter;
    }

    /**
     * Sets the accession numbers for the instrument source.
     * 
     * @param accessionInstrumentSourceParameter
     */
    public void setAccessionInstrumentSourceParameter(String accessionInstrumentSourceParameter) {
        this.accessionInstrumentSourceParameter = accessionInstrumentSourceParameter;
    }

    /**
     * Returns the ontology for the instrument source.
     * 
     * @return the ontology for the instrument source
     */
    public String getCVLookupInstrumentSourceParameter() {
        return cVLookupInstrumentSourceParameter;
    }

    /**
     * Sets the ontology for the instrument source.
     * 
     * @param cVLookupInstrumentSourceParameter
     */
    public void setCVLookupInstrumentSourceParameter(String cVLookupInstrumentSourceParameter) {
        this.cVLookupInstrumentSourceParameter = cVLookupInstrumentSourceParameter;
    }

    /**
     * Returns the name of the instrument source.
     * 
     * @return the name of the instrument source
     */
    public String getNameInstrumentSourceParameter() {
        return nameInstrumentSourceParameter;
    }

    /**
     * Sets the name of the instrument source.
     * 
     * @param nameInstrumentSourceParameter
     */
    public void setNameInstrumentSourceParameter(String nameInstrumentSourceParameter) {
        this.nameInstrumentSourceParameter = nameInstrumentSourceParameter;
    }

    /**
     * Returns the current protein hit.
     * 
     * @return the current protein hit
     */
    public ProteinHit getTempProteinHit() {
        return tempProteinHit;
    }

    /**
     * Set the current protein hit.
     * 
     * @param tempProteinHit
     */
    public void setTempProteinHit(ProteinHit tempProteinHit) {
        this.tempProteinHit = tempProteinHit;
    }

    /**
     * Returns the selected protein hits.
     * 
     * @return the selected protein hits
     */
    public ArrayList getSelectedProteinHits() {
        return selectedProteinHits;
    }

    /**
     * Sets the selected protein hits.
     * 
     * @param selectedProteinHits
     */
    public void setSelectedProteinHits(ArrayList selectedProteinHits) {
        this.selectedProteinHits = selectedProteinHits;
    }

    /**
     * Returns true if the current data file has already been loaded.
     * 
     * @return true if the current data file has already been loaded
     */
    public boolean hasDataFileBeenLoaded() {
        return dataFileHasBeenLoaded;
    }

    /**
     * Sets if data file has been loaded.
     * 
     * @param dataFileHasBeenLoaded
     */
    public void setDataFileHasBeenLoaded(boolean dataFileHasBeenLoaded) {
        this.dataFileHasBeenLoaded = dataFileHasBeenLoaded;
    }

    /**
     * Returns true if the contact details has already been extracted.
     * 
     * @return true if the contact details has already been extracted
     */
    public boolean isContactInfoExtracted() {
        return contactInfoExtracted;
    }

    /**
     * Sets if the contact details has been extracted.
     * 
     * @param contactInfoExtracted
     */
    public void setContactInfoExtracted(boolean contactInfoExtracted) {
        this.contactInfoExtracted = contactInfoExtracted;
    }

    /**
     * Returns true if the sample details have been extracted.
     * 
     * @return true if the sample details have been extracted
     */
    public boolean areSampleDetailsExtracted() {
        return sampleDetailsExtracted;
    }

    /**
     * Sets if the sample details have been extracted.
     * 
     * @param sampleDetailsExtracted
     */
    public void setSampleDetailsExtracted(boolean sampleDetailsExtracted) {
        this.sampleDetailsExtracted = sampleDetailsExtracted;
    }

    /**
     * Returns the current mzDataFile.
     * 
     * @return the current mzDataFile
     */
    public MzData getMzDataFile() {
        return mzDataFile;
    }

    /**
     * Set the mzData file.
     * 
     * @param mzDataFile
     */
    public void setMzDataFile(MzData mzDataFile) {
        this.mzDataFile = mzDataFile;
    }
    
    /**
     * Returns the current mzML file.
     * 
     * @return the current mzML file
     */
    public MzML getMzMlFile() {
        return mzML;
    }

    /**
     * Set the mzML file.
     * 
     * @param mzML file
     */
    public void setMzMlFile(MzML mzML) {
        this.mzML = mzML;
    }

    /**
     * Returns the name of the ProteinProphet file.
     * 
     * @return the name of the ProteinProphet file
     */
    public String getProteinProphetFileName() {
        return proteinProphetFileName;
    }

    /**
     * Sets the name of the ProteinProphet file.
     * 
     * @param proteinProphetFileName
     */
    public void setProteinProphetFileName(String proteinProphetFileName) {
        this.proteinProphetFileName = proteinProphetFileName;
    }

    /**
     * Returns the name of the PeptideProphet file.
     * 
     * @return the name of the PeptideProphet file
     */
    public String getPeptideProphetFileName() {
        return peptideProphetFileName;
    }

    /**
     * Sets the name of the PeptideProphet file.
     * 
     * @param peptideProphetFileName
     */
    public void setPeptideProphetFileName(String peptideProphetFileName) {
        this.peptideProphetFileName = peptideProphetFileName;
    }

    /**
     * Returns the name of the DTASelect file.
     *
     * @return the name of the DTASelect file
     */
    public String getDtaSelectFileName() {
        return dtaSelectFileName;
    }

    /**
     * Sets the name of the DTASelect file.
     *
     * @param dtaSelectFileName
     */
    public void setDtaSelectFileName(String dtaSelectFileName) {
        this.dtaSelectFileName = dtaSelectFileName;
    }

    /**
     * Returns the name of the spectrum file folder (used for TPP and DTASelect).
     *
     * @return the name of the spectrum file folder
     */
    public String getSpectrumFilesFolderName() {
        return spectrumFilesFolderName;
    }

    /**
     * Sets the name of the spectrum file folder (used for TPP and DTASelect).
     *
     * @param spectrumFilesFolderName
     */
    public void setSpectrumFilesFolderName(String spectrumFilesFolderName) {
        this.spectrumFilesFolderName = spectrumFilesFolderName;
    }

    /**
     * Returns true of the identifications are gel free, false otherwise (means 2D gel).
     *
     * @return true of the identifications are gel free, false otherwise (means 2D gel).
     */
    public boolean isGelFree() {
        return isGelFree;
    }

    /**
     * Sets if the identifications are gel free or 2D gel.
     *
     * @param isGelFree true of the identifications are gel free, false otherwise (means 2D gel).
     */
    public void setGelFree(boolean isGelFree) {
        this.isGelFree = isGelFree;
    }

    /**
     * Returns the spectrum cv parameters
     *
     * @return the spectrumCvParams
     */
    public HashMap<String, ArrayList<CvParam>> getSpectrumCvParams() {
        return spectrumCvParams;
    }

    /**
     * Sets the spectrum cv parameters
     *
     * @param spectrumCvParams the spectrumCvParams to set
     */
    public void setSpectrumCvParams(HashMap<String, ArrayList<CvParam>> spectrumCvParams) {
        this.spectrumCvParams = spectrumCvParams;
    }

    /**
     * Returns the spectrum user parameters
     *
     * @return the spectrumUserParams
     */
    public HashMap<String, ArrayList<UserParam>> getSpectrumUserParams() {
        return spectrumUserParams;
    }

    /**
     * Sets the spectrum user parameters
     *
     * @param spectrumUserParams the spectrumUserParams to set
     */
    public void setSpectrumUserParams(HashMap<String, ArrayList<UserParam>> spectrumUserParams) {
        this.spectrumUserParams = spectrumUserParams;
    }

    /**
     * Returns the proteinIdentificationFilter
     *
     * @return the proteinIdentificationFilter
     */
    public String getProteinIdentificationFilter() {
        return proteinIdentificationFilter;
    }

    /**
     * Sets the proteinIdentificationFilter
     *
     * @param proteinIdentificationFilter the proteinIdentificationFilter to set
     */
    public void setProteinIdentificationFilter(String proteinIdentificationFilter) {
        this.proteinIdentificationFilter = proteinIdentificationFilter;
    }

    /**
     * Returns the path to the sequest.param file (used for DTASelect)
     *
     * @return the path to the sequest.param file
     */
    public String getSequestParamFile() {
        return sequestParamFile;
    }

    /**
     * Sets the path to the sequest.param file (used for DTASelect)
     *
     * @param sequestParamFile path to the sequest.param file
     */
    public void setSequestParamFile(String sequestParamFile) {
        this.sequestParamFile = sequestParamFile;
    }

    /**
     * Returns the last selected file filter in the data file selection with two
     * data file types, for the spectra file selection.
     *
     * @return lastUsedFileFilter
     */
    public FileFilter getLastUsedFileFilter() {
        return lastUsedFileFilter;
    }

    /**
     * Sets the last selected file filter in the data file selection with two
     * data file types, for the spectra file selection.
     *
     * @param lastUsedFileFilter
     */
    public void setLastUsedFileFilter(FileFilter lastUsedFileFilter) {
        this.lastUsedFileFilter = lastUsedFileFilter;
    }
}
