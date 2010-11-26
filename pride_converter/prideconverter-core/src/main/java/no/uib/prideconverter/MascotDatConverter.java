package no.uib.prideconverter;

import com.compomics.mascotdatfile.util.interfaces.MascotDatfileInf;
import com.compomics.mascotdatfile.util.interfaces.Modification;
import com.compomics.mascotdatfile.util.interfaces.QueryToPeptideMapInf;
import com.compomics.mascotdatfile.util.mascot.*;
import com.compomics.mascotdatfile.util.mascot.enumeration.MascotDatfileType;
import com.compomics.mascotdatfile.util.mascot.factory.MascotDatfileFactory;
import com.compomics.mascotdatfile.util.mascot.iterator.QueryEnumerator;
import no.uib.prideconverter.gui.ModificationMapping;
import no.uib.prideconverter.gui.ProteinIsoFormSelection;
import no.uib.prideconverter.gui.ProteinIsoforms;
import no.uib.prideconverter.util.FragmentIonMappedDetails;
import no.uib.prideconverter.util.IdentificationGeneral;
import no.uib.prideconverter.util.Util;
import no.uib.prideconverter.util.iTRAQ;
import uk.ac.ebi.pride.model.implementation.core.FragmentIonImpl;
import uk.ac.ebi.pride.model.implementation.core.ModificationImpl;
import uk.ac.ebi.pride.model.implementation.core.MonoMassDeltaImpl;
import uk.ac.ebi.pride.model.implementation.mzData.*;
import uk.ac.ebi.pride.model.interfaces.core.FragmentIon;
import uk.ac.ebi.pride.model.interfaces.core.MassDelta;
import uk.ac.ebi.pride.model.interfaces.mzdata.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Florian Reisinger
 *         Date: 16-Jul-2009
 * @since $version
 */
public class MascotDatConverter {


    /**
     * This method transforms spectra from Mascot dat files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     * @return a HashMap
     * @throws IOException
     */
    protected static HashMap<String, Long> transformSpectraFromMascotDatFile(ArrayList<Spectrum> aTransformedSpectra)
            throws IOException {

        // ToDo: option to not fill the list of spectra, but marshall out instead 


        HashMap<String, Long> mapping = new HashMap<String, Long>();

        int totalSpectraCounter = 0, progressCounter = 0;

        QueryToPeptideMapInf queryToPeptideMap;
        QueryEnumerator queries;
        Query currentQuery;

        PeptideHit tempPeptideHit;

        Peak[] peakList;
        double[][] arrays;
        Collection<Precursor> precursors;
        Collection<CvParam> ionSelection;
        Collection<SpectrumDescComment> spectrumDescriptionComments;
        String chargeString;

        String fileName;

        boolean matchFound = false;
        int iconScore, threshold;
        StringTokenizer tok;

        Double mzStartRange = null, mzStopRange = null;
        File tempFile;
        double intensity;
        Spectrum fragmentation;

        PRIDEConverter.setIds(new ArrayList<IdentificationGeneral>());
        PRIDEConverter.getProperties().setSelectedIsoforms(new HashMap<String, String>());
        PRIDEConverter.setCancelConversion(false);
        PRIDEConverter.getProperties().setIsoFormsSelectionTypeSelected(false);
        boolean alreadySelected, peptideIsIdentified;

        Vector fixedModifications, variableModifications;
        String modName;

        ArrayList<CvParam> cVParams;
        ArrayList<UserParam> userParams;
        ArrayList<ModificationImpl> peptideModifications;
        ArrayList<CvParam> modificationCVParams;
        ArrayList<MassDelta> monoMasses;
        ArrayList<FragmentIon> fragmentIons;

        PRIDEConverter.getProgressDialog().setIntermidiate(false);
        MascotDatfileInf tempMascotDatfile;

        boolean calculateITraq = false;

        if (PRIDEConverter.getProperties().getSampleDescriptionCVParamsQuantification().size() > 0) {
            calculateITraq = true;
        }

        for (int k = 0; k < PRIDEConverter.getProperties().getSelectedSourceFiles().size() && !PRIDEConverter.isConversionCanceled(); k++) {

            tempFile = new File(PRIDEConverter.getProperties().getSelectedSourceFiles().get(k));
            PRIDEConverter.setCurrentFileName(tempFile.getName());

            PRIDEConverter.getProgressDialog().setIntermidiate(true);
            PRIDEConverter.getProgressDialog().setValue(0);
            PRIDEConverter.getProgressDialog().setString(PRIDEConverter.getCurrentFileName() + " (" + (k + 1) + "/" +
                    PRIDEConverter.getProperties().getSelectedSourceFiles().size() + ")");

            double size = (double) tempFile.length() /
                    PRIDEConverter.getProperties().NUMBER_OF_BYTES_PER_MEGABYTE;

            if (size > PRIDEConverter.getProperties().MAX_MASCOT_DAT_FILESIZE_BEFORE_INDEXING) {
                //if file is large
                tempMascotDatfile = MascotDatfileFactory.create(tempFile.getPath(),
                        MascotDatfileType.INDEX);
            } else {
                tempMascotDatfile = MascotDatfileFactory.create(tempFile.getPath(),
                        MascotDatfileType.MEMORY);
            }

            // handle the modifications
            fixedModifications = tempMascotDatfile.getModificationList().getFixedModifications();
            variableModifications = tempMascotDatfile.getModificationList().getVariableModifications();

            for (int i = 0; i < fixedModifications.size() && !PRIDEConverter.isConversionCanceled(); i++) {
                modName = ((FixedModification) fixedModifications.get(i)).getType();

                if (!PRIDEConverter.getProperties().getAlreadyChoosenModifications().contains(modName)) {
                    new ModificationMapping(PRIDEConverter.getOutputFrame(), true, PRIDEConverter.getProgressDialog(),
                            (Modification) fixedModifications.get(i),
                            (CvParamImpl) PRIDEConverter.getUserProperties().getCVTermMappings().get(modName), true);

                    PRIDEConverter.getProperties().getAlreadyChoosenModifications().add(modName);
                } else {
                    //do nothing, mapping already choosen
                }
            }

            for (int i = 0; i < variableModifications.size() && !PRIDEConverter.isConversionCanceled(); i++) {

                modName = ((VariableModification) variableModifications.get(i)).getType();

                if (!PRIDEConverter.getProperties().getAlreadyChoosenModifications().contains(modName)) {
                    new ModificationMapping(PRIDEConverter.getOutputFrame(), true, PRIDEConverter.getProgressDialog(),
                            (Modification) variableModifications.get(i),
                            (CvParamImpl) PRIDEConverter.getUserProperties().getCVTermMappings().get(modName), false);
                    PRIDEConverter.getProperties().getAlreadyChoosenModifications().add(modName);
                } else {
                    //do nothing, mapping already choosen
                }
            }

            //calculate iTraq values
            iTRAQ iTRAQValues = null;

            if (calculateITraq) {
                iTRAQValues =
                        new iTRAQ(tempFile.getPath(),
                        PRIDEConverter.getUserProperties().getPeakIntegrationRangeLower(),
                        PRIDEConverter.getUserProperties().getPeakIntegrationRangeUpper(),
                        PRIDEConverter.getUserProperties().getReporterIonIntensityThreshold(),
                        PRIDEConverter.getUserProperties().getPurityCorrections());
                iTRAQValues.calculateiTRAQValues();
            }

            queryToPeptideMap = tempMascotDatfile.getQueryToPeptideMap();
            queries = tempMascotDatfile.getQueryEnumerator();

            progressCounter = 0;
            PRIDEConverter.getProgressDialog().setIntermidiate(false);
            PRIDEConverter.getProgressDialog().setMax(tempMascotDatfile.getNumberOfQueries());
            PRIDEConverter.getProgressDialog().setValue(0);

            while (queries.hasMoreElements() && !PRIDEConverter.isConversionCanceled()) {

                PRIDEConverter.getProgressDialog().setValue(progressCounter++);

                currentQuery = queries.nextElement();

                matchFound = false;
                fileName = currentQuery.getFilename();
                PRIDEConverter.setSpectrumKey(fileName + "_" + tempMascotDatfile.getFileName() + "_" + currentQuery.getQueryNumber());

                if (PRIDEConverter.getProperties().getSelectedSpectraKeys().size() > 0) {
                    for (int i = 0; i < PRIDEConverter.getProperties().getSelectedSpectraKeys().size() && !matchFound; i++) {
                        matchFound = ((String) ((Object[]) PRIDEConverter.getProperties().getSelectedSpectraKeys().get(i))[1]).equalsIgnoreCase(
                                (PRIDEConverter.getCurrentFileName() + "_" + currentQuery.getQueryNumber()));
                    }
                } else {
                    if (PRIDEConverter.getProperties().getSpectraSelectionCriteria() != null) {
                        if (PRIDEConverter.getUserProperties().getCurrentFileNameSelectionCriteriaSeparator().length() == 0) {
                            tok = new StringTokenizer(PRIDEConverter.getProperties().getSpectraSelectionCriteria());
                        } else {
                            tok = new StringTokenizer(PRIDEConverter.getProperties().getSpectraSelectionCriteria(),
                                    PRIDEConverter.getUserProperties().getCurrentFileNameSelectionCriteriaSeparator());
                        }

                        String tempToken;

                        while (tok.hasMoreTokens() && !matchFound) {

                            tempToken = tok.nextToken();
                            tempToken = tempToken.trim();

                            if (PRIDEConverter.getProperties().isSelectionCriteriaFileName()) {
                                if (fileName.lastIndexOf(tempToken) != -1) {
                                    matchFound = true;
                                }
                            } else {
                                matchFound = false; // mascot dat files don't have identification ids
                                break;
                            }
                        }
                    } else {
                        matchFound = true;
                    }
                }

                if (matchFound) {

                    peptideIsIdentified = false;
                    tempPeptideHit = null;

                    try {
                        peakList = currentQuery.getPeakList();
                        arrays = new double[2][peakList.length];

                        for (int j = 0; j < peakList.length; j++) {
                            arrays[0][j] = peakList[j].getMZ();
                            arrays[1][j] = peakList[j].getIntensity();
                        }

                        if (peakList.length > 0) {
                            mzStartRange = currentQuery.getMinMZ();
                            mzStopRange = currentQuery.getMaxMZ();

                            // Precursor collection.
                            precursors = new ArrayList<Precursor>();

                            // Ion selection parameters.
                            ionSelection = new ArrayList<CvParam>();

                            // See if we know the precursor charge, and if so, include it.
                            chargeString = currentQuery.getChargeString();

                            chargeString = chargeString.replaceFirst("\\+", "");

                            if (!chargeString.equalsIgnoreCase("0")) {
                                ionSelection.add(new CvParamImpl("PSI:1000041",
                                        "PSI", "ChargeState", ionSelection.size(), chargeString));
                            }

                            // See if we know the precursor intensity
                            intensity = currentQuery.getPrecursorIntensity();

                            if (intensity > 1) {
                                ionSelection.add(new CvParamImpl("PSI:1000042",
                                        "PSI", "Intensity", ionSelection.size(),
                                        Double.toString(intensity)));
                            }

                            ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                                    "MassToChargeRatio", ionSelection.size(),
                                    Double.toString(currentQuery.getPrecursorMZ())));

                            precursors.add(new PrecursorImpl(null, null, ionSelection, null, 1, 0, 0));

                            // Spectrum description comments.
                            spectrumDescriptionComments = new ArrayList<SpectrumDescComment>();

                            tempPeptideHit = queryToPeptideMap.getPeptideHitOfOneQuery(currentQuery.getQueryNumber());

                            if (tempPeptideHit != null) {

                                //Added to make up for the "bug" in old version of the Mascot Perl script.
                                //This script rounds down (!) to the lower integer both the threshold and
                                //the score, and if the result is >= threshold it is labeled identified.
                                if (PRIDEConverter.getProperties().roundMascotScoreAndThresholdDownToNearestInteger()) {

                                    iconScore = new Double(tempPeptideHit.getIonsScore()).intValue();
                                    threshold = new Double(tempPeptideHit.calculateIdentityThreshold((100 -
                                            PRIDEConverter.getProperties().getMascotConfidenceLevel()) / 100)).intValue();

                                    if (iconScore >= threshold) {
                                        spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Identified"));
                                        peptideIsIdentified = true;
                                    } else {
                                        spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Not identified"));
                                        peptideIsIdentified = false;
                                    }
                                } else {
                                    if (queryToPeptideMap.getPeptideHitsAboveIdentityThreshold(currentQuery.getQueryNumber(),
                                            (100 - PRIDEConverter.getProperties().getMascotConfidenceLevel()) / 100).size() > 0) {
                                        spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Identified"));
                                        peptideIsIdentified = true;
                                    } else {
                                        spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Not identified"));
                                        peptideIsIdentified = false;
                                    }
                                }
                            } else {
                                spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Not identified"));
                                peptideIsIdentified = false;
                            }

                            boolean addPeptide = true;

                            // special case for when the peptide is not identified and the select identified
                            // spectra option is selected
                            if (!peptideIsIdentified && PRIDEConverter.getProperties().selectAllIdentifiedSpectra()) {
                                addPeptide = false;
                            }

                            // additional test for Peptide Score Threshold
                            if (peptideIsIdentified && tempPeptideHit != null) {

                                iconScore = new Double(tempPeptideHit.getIonsScore()).intValue();

                                if (iconScore < PRIDEConverter.getProperties().getPeptideScoreThreshold()) {
                                    addPeptide = false;
                                }
                            }

                            if (addPeptide) {

                                spectrumDescriptionComments = PRIDEConverter.addUserSpectrumComments(spectrumDescriptionComments,
                                        PRIDEConverter.getProperties().getSpectrumCvParams().get(PRIDEConverter.getSpectrumKey()),
                                        PRIDEConverter.getProperties().getSpectrumUserParams().get(PRIDEConverter.getSpectrumKey()));

                                // Create new mzData spectrum for the fragmentation spectrum.
                                fragmentation = new SpectrumImpl(
                                        new BinaryArrayImpl(arrays[PRIDEConverter.getProperties().INTENSITIES_ARRAY],
                                        BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                        mzStartRange,
                                        new BinaryArrayImpl(arrays[PRIDEConverter.getProperties().MZ_ARRAY],
                                        BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                        2,
                                        null,
                                        mzStopRange,
                                        null,
                                        ++totalSpectraCounter, precursors,
                                        spectrumDescriptionComments,
                                        null, null,
                                        null, null);

                                // Store (spectrumfileid, spectrumid) mapping.
                                int qnum = currentQuery.getQueryNumber();
                                String uFileName = tempFile.getAbsolutePath(); // TODO: Isn't filename enough? Is absolute file name needed?

                                Long xTmp = mapping.put(uFileName + "_" + qnum, (long) totalSpectraCounter);

                                if (xTmp != null) {
                                    // we already stored a result for this ID!!!
                                    JOptionPane.showMessageDialog(null, "Ambiguous spectrum mapping. Consult " +
                                            "the error log file for details.", "Mapping Error", JOptionPane.ERROR_MESSAGE);
                                    Util.writeToErrorLog("Ambiguous spectrum mapping for ID '" + totalSpectraCounter +
                                            "' and spectrum file '" + currentQuery.getFilename() + "'.");
                                    PRIDEConverter.setCancelConversion(true);
                                }

                                // Store the transformed spectrum.
                                aTransformedSpectra.add(fragmentation);

                                // memory clean up
                                arrays = null;
                                peakList = null;

                                if (!PRIDEConverter.isConversionCanceled()) {

                                    // extract the peptide identifications
                                    if (peptideIsIdentified) {
                                        PRIDEConverter.getProperties().setTempProteinHit((ProteinHit) tempPeptideHit.getProteinHits().get(0));

                                        if (tempPeptideHit.getProteinHits().size() > 1) {

                                            if (!PRIDEConverter.getProperties().isIsoFormsSelectionTypeSelected()) {
                                                new ProteinIsoforms(PRIDEConverter.getOutputFrame(), true, PRIDEConverter.getProgressDialog());
                                                PRIDEConverter.getProperties().setIsoFormsSelectionTypeSelected(true);
                                            }

                                            if (PRIDEConverter.getProperties().getProteinIsoformSelectionType() ==
                                                    PRIDEConverter.getProperties().PROTEIN_ISOFORMS_ALWAYS_SELECT_FIRST) {

                                                // set the protein hit to use
                                                PRIDEConverter.getProperties().setTempProteinHit(
                                                        (ProteinHit) tempPeptideHit.getProteinHits().get(0));

                                            } else if (PRIDEConverter.getProperties().getProteinIsoformSelectionType() ==
                                                    PRIDEConverter.getProperties().PROTEIN_ISOFORMS_MANUAL_SELECTION) {

                                                alreadySelected = false;

                                                // check if the protein accession number has already been used and
                                                // indicated as 'always select this accession number if in list'
                                                for (int w = 0; w < tempPeptideHit.getProteinHits().size() && !alreadySelected; w++) {
                                                    if (PRIDEConverter.getProperties().getSelectedProteinIsoforms().containsValue(
                                                            ((ProteinHit) tempPeptideHit.getProteinHits().get(w)).getAccession())) {

                                                        // set the protein hit to use
                                                        PRIDEConverter.getProperties().setTempProteinHit(
                                                            (ProteinHit) tempPeptideHit.getProteinHits().get(w));
                                                        alreadySelected = true;
                                                    }
                                                }

                                                // not mapped. do manual mapping
                                                if (!alreadySelected) {

                                                    if (PRIDEConverter.isConversionCanceled()) {
                                                        PRIDEConverter.getOutputFrame().setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                                                        PRIDEConverter.getOutputFrame().setConvertButtonEnabled(true);
                                                        return null;
                                                    }

                                                    new ProteinIsoFormSelection(PRIDEConverter.getOutputFrame(), true,
                                                            tempPeptideHit, PRIDEConverter.getProgressDialog());
                                                }
                                            } else {//list provided

                                                // check if the peptide is in the list of peptide sequence to protein accession numbers
                                                if (PRIDEConverter.getProperties().getSelectedProteinIsoforms().containsKey(tempPeptideHit.getSequence())) {

                                                    // iterate the protein hits and find the correct protein hit
                                                    for (int h = 0; h < tempPeptideHit.getProteinHits().size(); h++) {
                                                        if (((ProteinHit) tempPeptideHit.getProteinHits().get(h)).getAccession().equalsIgnoreCase(
                                                                PRIDEConverter.getProperties().getSelectedProteinIsoforms().get(tempPeptideHit.getSequence()))) {
                                                            PRIDEConverter.getProperties().setTempProteinHit((ProteinHit) tempPeptideHit.getProteinHits().get(h));
                                                        }
                                                    }
                                                } else { // not found. do manual selection

                                                    alreadySelected = false;

                                                    // check if the protein accession number has already been used and
                                                    // indicated as 'always select this accession number if in list'
                                                    for (int w = 0; w < tempPeptideHit.getProteinHits().size() && !alreadySelected; w++) {
                                                        if (PRIDEConverter.getProperties().getSelectedProteinIsoforms().containsValue(
                                                                ((ProteinHit) tempPeptideHit.getProteinHits().get(w)).getAccession())) {

                                                            // set the protein hit to use
                                                            PRIDEConverter.getProperties().setTempProteinHit(
                                                                (ProteinHit) tempPeptideHit.getProteinHits().get(w));
                                                            alreadySelected = true;
                                                        }
                                                    }

                                                    // not mapped. do manual mapping
                                                    if (!alreadySelected) {

                                                        if (PRIDEConverter.isConversionCanceled()) {
                                                            PRIDEConverter.getOutputFrame().setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                                                            PRIDEConverter.getOutputFrame().setConvertButtonEnabled(true);
                                                            return null;
                                                        }

                                                        new ProteinIsoFormSelection(PRIDEConverter.getOutputFrame(), true,
                                                                tempPeptideHit, PRIDEConverter.getProgressDialog());
                                                    }
                                                }
                                            }
                                        }

                                        cVParams = new ArrayList<CvParam>();
                                        cVParams.add(new CvParamImpl("PRIDE:0000069", "PRIDE",
                                                "Mascot Score", 0, "" + tempPeptideHit.getIonsScore()));

                                        String[] iTraqNorm = null;
                                        String[] iTraqUT = null;
                                        String[][] iTraqRatio = null;
                                        userParams = null;

                                        if (calculateITraq) {
                                            iTraqNorm = (String[]) iTRAQValues.getAllNorms().get(currentQuery.getQueryNumber());
                                            iTraqUT = (String[]) iTRAQValues.getAllUTs().get(currentQuery.getQueryNumber());
                                            iTraqRatio = (String[][]) iTRAQValues.getAllRatios().get(currentQuery.getQueryNumber());

                                            PRIDEConverter.addItraqCVTerms(cVParams, iTraqNorm);
                                            userParams = PRIDEConverter.addItraqUserTerms(iTraqRatio);
                                        }

                                        peptideModifications = null;

                                        int location;

                                        if (tempPeptideHit.getModifications() != null) {

                                            peptideModifications = new ArrayList<ModificationImpl>();

                                            for (int m = 0; m < tempPeptideHit.getModifications().length; m++) {

                                                if (tempPeptideHit.getModifications()[m] != null) {
                                                    if (PRIDEConverter.getUserProperties().getCVTermMappings().get(
                                                            (tempPeptideHit.getModifications()[m]).getType()) != null) {

                                                        CvParamImpl tempCvParam =
                                                                (CvParamImpl) PRIDEConverter.getUserProperties().getCVTermMappings().get(
                                                                (tempPeptideHit.getModifications()[m]).getType());

                                                        modificationCVParams = new ArrayList<CvParam>();
                                                        modificationCVParams.add(tempCvParam);

                                                        monoMasses = new ArrayList<MassDelta>();

                                                        // get the modification mass (DiffMono) retrieved from PSI-MOD
                                                        if (tempCvParam.getValue() != null) {
                                                            monoMasses.add(new MonoMassDeltaImpl(
                                                                    Double.parseDouble(tempCvParam.getValue())));
                                                        } else {
                                                            // if the DiffMono is not found for the PSI-MOD the mass
                                                            // from the file is used
                                                            monoMasses.add(new MonoMassDeltaImpl(
                                                                    tempPeptideHit.getModifications()[m].getMass()));
                                                        //monoMasses = null;
                                                        }

                                                        location = m;

//                                            //n-terminal modification
//                                            if (location ==
//                                                    0) {
//                                                location = 1;
//                                            }
//
//                                            //c-terminal modification
//                                            if (location ==
//                                                    tempPeptideHit.getModifications().length) {
//                                                location -= 1;
//                                            }

                                                        peptideModifications.add(new ModificationImpl(
                                                                tempCvParam.getAccession(),
                                                                location,
                                                                tempCvParam.getCVLookup(),
                                                                null,
                                                                monoMasses,
                                                                null,
                                                                modificationCVParams,
                                                                null));
                                                    } else {
                                                        Util.writeToErrorLog("Unknown modifications! - Should not happen!!! Modification: " +
                                                                (tempPeptideHit.getModifications()[m]).getType());
                                                    }
                                                }
                                            }
                                        }


                                        // add fragment ion annotation
                                        fragmentIons = new ArrayList<FragmentIon>();

                                        // get the peptide annotations from the file
                                        PeptideHitAnnotation peptideHitAnnotations =
                                                tempPeptideHit.getPeptideHitAnnotation(
                                                tempMascotDatfile.getMasses(), tempMascotDatfile.getParametersSection());

                                        // get the fragment ions
                                        Vector currentFragmentIons = peptideHitAnnotations.getFusedMatchedIons(
                                                currentQuery.getPeakList(), tempPeptideHit.getPeaksUsedFromIons1(), currentQuery.getMaxIntensity(), 0.05D);

                                        // iterate the fragment ions, detect the type and create CV params for each of them
                                        for (Object currentFragmentIon1 : currentFragmentIons) {

                                            // Note: 'FragmentIon' is included in several projects so the complete path is required
                                            com.compomics.mascotdatfile.util.mascot.fragmentions.FragmentIonImpl currentFragmentIon =
                                                    (com.compomics.mascotdatfile.util.mascot.fragmentions.FragmentIonImpl) currentFragmentIon1;

                                            // map the reported fragment ion to its corresponding CV term by using the
                                            // mappings given in the FragmentIonsMapping.prop file
                                            FragmentIonMappedDetails fragmentIonMappedDetails =
                                                    PRIDEConverter.getFragmentIonMappings().getCVTermMappings().get(
                                                    PRIDEConverter.getProperties().getDataSource() + "_" + currentFragmentIon.getType());

                                            // check if a mapping was found or not
                                            if (fragmentIonMappedDetails == null) {
                                                JOptionPane.showMessageDialog(PRIDEConverter.getOutputFrame(),
                                                        "Unknown fragment ion \'" + currentFragmentIon.getType() + "\'. Ion not included in annotation.\n" +
                                                        "Please contact the PRIDE support team at pride-support@ebi.ac.uk.",
                                                        "Unknown Fragment Ion",
                                                        JOptionPane.INFORMATION_MESSAGE);
                                            } else {

                                                // -1 means that a mapping was found but that that this particular fragment
                                                // ion type is currently not being used. Update FragmentIonsMapping.prop if
                                                // you want to add a mapping for this fragment ion type.
                                                if (!fragmentIonMappedDetails.getCvParamAccession().equalsIgnoreCase("-1")) {

                                                    // create the list of CV Params for the fragment ion
                                                    ArrayList<CvParam> currentCvTerms =
                                                            PRIDEConverter.createFragmentIonCvParams(
                                                            fragmentIonMappedDetails,
                                                            currentFragmentIon.getMZ() + currentFragmentIon.getTheoreticalExperimantalMassError(),
                                                            fragmentIonMappedDetails.getCharge(),
                                                            currentFragmentIon.getNumber(),
                                                            currentFragmentIon.getIntensity(),
                                                            currentFragmentIon.getTheoreticalExperimantalMassError(), // @TODO: use absolute value?
                                                            null);

                                                    // add the created fragment ion to the list of all fragment ions
//                                                    fragmentIons.add(new FragmentIonImpl(currentCvTerms, null));
                                                    FragmentIon tmpIon = new FragmentIonImpl(currentCvTerms, null);
                                                    if (!PRIDEConverter.containsFragmentIon(fragmentIons, tmpIon)) {
                                                        fragmentIons.add(tmpIon);
                                                    } else {
                                                        Util.writeToErrorLog("Ignoring duplicated fragment ion for spectrum accession "
                                                                + PRIDEConverter.getProperties().getTempProteinHit().getAccession()
                                                                + " with sequence: " + tempPeptideHit.getSequence()
                                                                + " and spectrumKey: " + tempFile.getAbsolutePath() + "_" + currentQuery.getQueryNumber());
                                                    }
                                                }
                                            }
                                        }


                                        PRIDEConverter.getIds().add(new IdentificationGeneral(
                                                tempFile.getAbsolutePath() + "_" + currentQuery.getQueryNumber(), // spectrum key
                                                PRIDEConverter.getProperties().getTempProteinHit().getAccession(), // accession
                                                "Mascot", // search engine
                                                tempMascotDatfile.getParametersSection().getDatabase(), // database
                                                tempMascotDatfile.getHeaderSection().getVersion(),// database version
                                                tempPeptideHit.getSequence(), // sequence
                                                PRIDEConverter.getProperties().getTempProteinHit().getStart(), // start
                                                tempPeptideHit.getIonsScore(), // score
                                                tempPeptideHit.calculateIdentityThreshold((100 -
                                                PRIDEConverter.getProperties().getMascotConfidenceLevel()) /
                                                100), // threshold
                                                iTraqNorm, iTraqUT, iTraqRatio, // iTRAQ values
                                                cVParams, // cv params
                                                userParams, // user params
                                                peptideModifications, // list of modifications
                                                fragmentIons)); // list of fragment ions
                                    }
                                }
                            }
                        }
                    } catch (NullPointerException e) {
//                        Util.writeToErrorLog("Query without peak list: Exception caught..." +
//                                e.toString());
                        // simply ignore
//                        PRIDEConverter.setEmptySpectraCounter( PRIDEConverter.getEmptySpectraCounter() + 1);
//                        e.printStackTrace();
                    }
                }
            }
        }

        PRIDEConverter.setTotalNumberOfSpectra(totalSpectraCounter);
        return mapping;
    }
}
