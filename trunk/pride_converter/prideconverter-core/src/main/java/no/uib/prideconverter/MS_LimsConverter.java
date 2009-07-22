package no.uib.prideconverter;

import be.proteomics.lims.db.accessors.Spectrumfile;
import be.proteomics.lims.db.accessors.Identification;
import be.proteomics.lims.db.accessors.Fragmention;
import be.proteomics.lims.util.fileio.MascotGenericFile;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;

import uk.ac.ebi.pride.model.interfaces.mzdata.*;
import uk.ac.ebi.pride.model.interfaces.core.FragmentIon;
import uk.ac.ebi.pride.model.implementation.mzData.*;
import uk.ac.ebi.pride.model.implementation.core.FragmentIonImpl;
import uk.ac.ebi.pride.model.implementation.core.ModificationImpl;
import no.uib.prideconverter.util.IdentificationGeneral;
import no.uib.prideconverter.util.iTRAQ;
import no.uib.prideconverter.util.FragmentIonMappedDetails;
import no.uib.prideconverter.util.Util;
import no.uib.prideconverter.gui.ModificationMapping;
import no.uib.prideconverter.gui.ProgressDialog;

import javax.swing.*;

/**
 * @author Florian Reisinger
 *         Date: 20-Jul-2009
 * @since $version
 */
public class MS_LimsConverter {

    /**
     * This method transforms spectra from ms_lims, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     * @return a HashMap
     * @throws java.io.IOException in case of an error reading the input file.
     */
    protected static HashMap<String, Long> transformSpectraFrom_ms_lims(ArrayList<Spectrum> aTransformedSpectra) throws IOException {

        // temporary variables
        ProgressDialog progressDialog = PRIDEConverter.getProgressDialog();
        no.uib.prideconverter.util.Properties properties = PRIDEConverter.getProperties();
        no.uib.prideconverter.util.UserProperties userProperties = PRIDEConverter.getUserProperties();

        HashMap<String, Long> mapping = new HashMap<String, Long>();

        //Get the selected spectra
        ArrayList selectedSpectra = new ArrayList();
        selectedSpectra = PRIDEConverter.getSelectedSpectraFromDatabase();

        int idCounter = 0;
        Iterator iter = selectedSpectra.iterator();
        Spectrumfile dbSpectrum;

        String filename, file;
        MascotGenericFile lSpectrumFile;

        double[][] arrays;
        Collection<Precursor> precursors;
        Collection<CvParam> ionSelection;

        int charge;
        double intensity;
        Collection<SpectrumDescComment> spectrumDescriptionComments;

        long identificationCount;
        String identified;
        Spectrum fragmentation;
        Double mzRangeStart, mzRangeStop;

        TreeSet treeSet = new TreeSet();

        progressDialog.setIntermidiate(false);
        progressDialog.setValue(0);
        progressDialog.setMax(selectedSpectra.size());

        int progressCounter = 0;

        while (iter.hasNext() && !PRIDEConverter.isConversionCanceled()) {
            dbSpectrum = (Spectrumfile) iter.next();
            filename = dbSpectrum.getFilename();

            PRIDEConverter.setSpectrumKey(filename + "_null");

            progressDialog.setValue(progressCounter++);
            progressDialog.setString(filename + " (" + (progressCounter) + "/" + selectedSpectra.size() + ")");

            file = new String(dbSpectrum.getUnzippedFile());
            lSpectrumFile = new MascotGenericFile(filename, file);

            // Transform peaklist into double arrays.
            arrays = transformPeakListToArrays_ms_lims(lSpectrumFile.getPeaks(), treeSet);

            // Precursor collection.
            precursors = new ArrayList<Precursor>(1);

            // Ion selection parameters.
            ionSelection = new ArrayList<CvParam>(4);

            // See if we know the precursor charge, and if so, include it.
            charge = lSpectrumFile.getCharge();

            if (charge > 0) {
                ionSelection.add(new CvParamImpl("PSI:1000041", "PSI",
                        "ChargeState", ionSelection.size(), Integer.toString(charge)));
            }

            // See if we know the precursor intensity
            intensity = lSpectrumFile.getIntensity();
            if (intensity > 1) {
                ionSelection.add(new CvParamImpl("PSI:1000042", "PSI",
                        "Intensity", ionSelection.size(), Double.toString(intensity)));
            }

            ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                    "MassToChargeRatio", ionSelection.size(),
                    Double.toString(lSpectrumFile.getPrecursorMZ())));

            precursors.add(new PrecursorImpl(null, null, ionSelection, null, 1, 0, 0));
            idCounter++;

            // Spectrum description comments.
            spectrumDescriptionComments = new ArrayList<SpectrumDescComment>();
            identificationCount = dbSpectrum.getIdentified();
            identified = (identificationCount > 0 ? "Identified" : "Not identified");
            spectrumDescriptionComments.add(new SpectrumDescCommentImpl(identified));

            if (arrays[properties.MZ_ARRAY].length > 0) {
                mzRangeStart = arrays[properties.MZ_ARRAY][0];
                mzRangeStop = arrays[properties.MZ_ARRAY][arrays[properties.MZ_ARRAY].length - 1];

                spectrumDescriptionComments = PRIDEConverter.addUserSpectrumComments(spectrumDescriptionComments,
                        properties.getSpectrumCvParams().get(PRIDEConverter.getSpectrumKey()),
                        properties.getSpectrumUserParams().get(PRIDEConverter.getSpectrumKey()));

                fragmentation =
                        new SpectrumImpl(
                        new BinaryArrayImpl(arrays[properties.INTENSITIES_ARRAY], BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                        mzRangeStart,
                        new BinaryArrayImpl(arrays[properties.MZ_ARRAY], BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                        2,
                        null,
                        mzRangeStop,
                        null,
                        idCounter, precursors, spectrumDescriptionComments,
                        null, null,
                        null, null);

                // Store (spectrumfileid, spectrumid) mapping.
                mapping.put("" + dbSpectrum.getSpectrumfileid(), (long) idCounter);

                // Store the transformed spectrum.
                aTransformedSpectra.add(fragmentation);
                idCounter++;
            }
        }

        // peptide identifications
        PRIDEConverter.setIds(new ArrayList<IdentificationGeneral>());
        Identification tempIdentification;

        boolean calculateITraq = false;
        iTRAQ iTRAQValues = null;

        if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
            calculateITraq = true;
        }

        ArrayList<CvParam> cVParams;
        ArrayList<UserParam> userParams;
        ArrayList<ModificationImpl> peptideModifications;
        ArrayList<CvParam> modificationCVParams;
        ArrayList<FragmentIon> fragmentIons;

        progressDialog.setString(null);
        progressDialog.setIntermidiate(false);
        progressDialog.setValue(0);
        progressDialog.setMax(selectedSpectra.size());
        progressDialog.setTitle("Getting Spectra IDs. Please Wait...");

        for (int i = 0; i < selectedSpectra.size() && !PRIDEConverter.isConversionCanceled(); i++) {

            try {
                progressDialog.setValue(i);
                tempIdentification = Identification.getIdentification(PRIDEConverter.getConn(),
                        ((Spectrumfile) selectedSpectra.get(i)).getFilename());

                if (tempIdentification != null) {

                    if (tempIdentification.getScore() >= properties.getPeptideScoreThreshold()) {

                        cVParams = new ArrayList<CvParam>(1);
                        cVParams.add(new CvParamImpl("PRIDE:0000069", "PRIDE", "Mascot Score", 0, "" +
                                tempIdentification.getScore()));

                        // iTraq calculations
                        if (calculateITraq) {
                            iTRAQValues = new iTRAQ(
                                    aTransformedSpectra.get(
                                    i).getMzArrayBinary().getDoubleArray(),
                                    aTransformedSpectra.get(
                                    i).getIntenArrayBinary().getDoubleArray(),
                                    tempIdentification.getExp_mass().doubleValue(),
                                    tempIdentification.getCharge(),
                                    userProperties.getPeakIntegrationRangeLower(),
                                    userProperties.getPeakIntegrationRangeUpper(),
                                    userProperties.getReporterIonIntensityThreshold(),
                                    userProperties.getPurityCorrections());
                            iTRAQValues.calculateiTRAQValues();

                            cVParams = PRIDEConverter.addItraqCVTerms(cVParams, iTRAQValues);
                            userParams = PRIDEConverter.addItraqUserTerms(iTRAQValues);
                        } else {
                            userParams = null;
                        }

                        // modifications
                        String modifiedSequence = tempIdentification.getModified_sequence();

                        String nTerm = modifiedSequence.substring(0, modifiedSequence.indexOf("-"));
                        String cTerm = modifiedSequence.substring(modifiedSequence.lastIndexOf("-") + 1);

                        String peptideSequence = modifiedSequence.substring(modifiedSequence.indexOf("-") +
                                1, modifiedSequence.lastIndexOf("-"));

                        String modificationName;

                        peptideModifications = null;


                        // add the fragment ions
                        fragmentIons = new ArrayList<FragmentIon>();

                        // extract the fragment ions from the database
                        Collection fragments =
                                Fragmention.getAllFragmentions(PRIDEConverter.getConn(), tempIdentification.getIdentificationid());

                        // iterate the fragment ions, detect the type and create CV params for each of them
                        for (Object fragment : fragments) {

                            Fragmention currentFragmentIon = (Fragmention) fragment;

                            // map the reported fragment ion to its corresponding CV term by using the
                            // mappings given in the FragmentIonsMapping.prop file
                            FragmentIonMappedDetails fragmentIonMappedDetails = PRIDEConverter.getFragmentIonMappings().getCVTermMappings().get(
                                    properties.getDataSource() + "_" + currentFragmentIon.getIonname());

                            // check if a mapping was found or not
                            if (fragmentIonMappedDetails == null) {
                                JOptionPane.showMessageDialog(PRIDEConverter.getOutputFrame(),
                                        "Unknown fragment ion \'" + currentFragmentIon.getIonname() + "\'. Ion not included in annotation.\n" +
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
                                                    currentFragmentIon.getMz().doubleValue() + currentFragmentIon.getMassdelta().doubleValue(),
                                                    fragmentIonMappedDetails.getCharge(),
                                                    new Long(currentFragmentIon.getFragmentionnumber()).intValue(),
                                                    new Long(currentFragmentIon.getIntensity()).doubleValue(),
                                                    currentFragmentIon.getMassdelta().doubleValue(), // @TODO: use absolute value?
                                                    null);

                                    // add the created fragment ion to the list of all fragment ions
                                    fragmentIons.add(new FragmentIonImpl(currentCvTerms, null));
                                }
                            }
                        }


                        if (!nTerm.equalsIgnoreCase("NH2")) {

                            StringTokenizer tok = new StringTokenizer(nTerm, ",");

                            while (tok.hasMoreTokens() && !PRIDEConverter.isConversionCanceled()) {

                                modificationName = tok.nextToken() + " (N-terminal)";

                                if (!properties.getAlreadyChoosenModifications().
                                        contains(modificationName)) {
                                    new ModificationMapping(PRIDEConverter.getOutputFrame(),
                                            true, progressDialog, modificationName,
                                            "N-terminal", null,
                                            (CvParamImpl) userProperties.getCVTermMappings().get(modificationName), false);
                                    properties.getAlreadyChoosenModifications().add(modificationName);
                                } else {
                                    //do nothing, mapping already choosen
                                }

                                if (peptideModifications == null) {
                                    peptideModifications = new ArrayList<ModificationImpl>();
                                }

                                modificationCVParams = new ArrayList<CvParam>();
                                modificationCVParams.add(userProperties.getCVTermMappings().get(modificationName));

                                peptideModifications.add(new ModificationImpl(
                                        userProperties.getCVTermMappings().get(modificationName).getAccession(),
                                        0,
                                        userProperties.getCVTermMappings().get(modificationName).getCVLookup(),
                                        null,
                                        null,
                                        null,
                                        modificationCVParams,
                                        null));
                            }
                        }

                        if (!cTerm.equalsIgnoreCase("COOH")) {
                            StringTokenizer tok = new StringTokenizer(cTerm, ",");

                            while (tok.hasMoreTokens() && !PRIDEConverter.isConversionCanceled()) {

                                modificationName = tok.nextToken() + " (C-terminal)";

                                if (!properties.getAlreadyChoosenModifications().
                                        contains(modificationName)) {
                                    new ModificationMapping(PRIDEConverter.getOutputFrame(),
                                            true, progressDialog, modificationName,
                                            "C-terminal", null,
                                            (CvParamImpl) userProperties.getCVTermMappings().get(modificationName), false);
                                    properties.getAlreadyChoosenModifications().add(modificationName);
                                } else {
                                    //do nothing, mapping already choosen
                                }

                                if (peptideModifications == null) {
                                    peptideModifications = new ArrayList<ModificationImpl>();
                                }

                                modificationCVParams = new ArrayList<CvParam>();
                                modificationCVParams.add(userProperties.getCVTermMappings().get(modificationName));

                                peptideModifications.add(new ModificationImpl(
                                        userProperties.getCVTermMappings().get(modificationName).getAccession(),
                                        peptideSequence.length() + 1,
                                        userProperties.getCVTermMappings().get(modificationName).getCVLookup(),
                                        null,
                                        null,
                                        null,
                                        modificationCVParams,
                                        null));
                            }
                        }

                        String modificationPattern = "[<][^<]*[>]";

                        Pattern pattern = Pattern.compile(modificationPattern);
                        Matcher matcher = pattern.matcher(peptideSequence);

                        int index = 0;
                        int modificationTagLengthSum = 0;

                        while (matcher.find() && !PRIDEConverter.isConversionCanceled()) {

                            String internalModification = matcher.group();

                            //remove '<' and '>'
                            internalModification =
                                    internalModification.substring(1,
                                    internalModification.length() - 1);

                            index = matcher.start() - modificationTagLengthSum;

                            StringTokenizer tok = new StringTokenizer(internalModification, ",");

                            while (tok.hasMoreTokens() && !PRIDEConverter.isConversionCanceled()) {

                                modificationName = tok.nextToken();

                                if (!properties.getAlreadyChoosenModifications().
                                        contains(modificationName)) {
                                    new ModificationMapping(PRIDEConverter.getOutputFrame(),
                                            true, progressDialog, modificationName, "-", null,
                                            (CvParamImpl) userProperties.getCVTermMappings().
                                            get(modificationName), false);
                                    properties.getAlreadyChoosenModifications().add(modificationName);
                                } else {
                                    //do nothing, mapping already choosen
                                }

                                if (peptideModifications == null) {
                                    peptideModifications = new ArrayList<ModificationImpl>();
                                }

                                modificationCVParams = new ArrayList<CvParam>();
                                modificationCVParams.add(userProperties.getCVTermMappings().
                                        get(modificationName));

                                peptideModifications.add(new ModificationImpl(
                                        userProperties.getCVTermMappings().
                                        get(modificationName).getAccession(),
                                        index,
                                        userProperties.getCVTermMappings().
                                        get(modificationName).getCVLookup(),
                                        null,
                                        null,
                                        null,
                                        modificationCVParams,
                                        null));
                            }

                            modificationTagLengthSum += internalModification.length() + 2;
                        }

                        PRIDEConverter.getIds().add(new IdentificationGeneral(
                                "" + tempIdentification.getL_spectrumfileid(), // spectrum file id
                                tempIdentification.getAccession(), // peptide accession number
                                "Mascot " + tempIdentification.getMascot_version(), // search engine
                                tempIdentification.getDb(), // database
                                null, // database version
                                tempIdentification.getSequence(), // peptide sequence
                                new Long(tempIdentification.getStart()).intValue(), // start
                                (double) tempIdentification.getScore(), // score
                                (1 - tempIdentification.getConfidence().doubleValue()) * 100, // theshold
                                null, null, null, // iTRAQ values
                                cVParams, // cv params
                                userParams, // user params
                                peptideModifications, // list of modifications
                                fragmentIons)); // list of fragment ions

                        //assumed to be the same for all identifications in the selected project
                        properties.setMascotConfidenceLevel(((1 -
                                tempIdentification.getConfidence().doubleValue()) * 100));
                    }
                }
            } catch (java.sql.SQLException e) {
                JOptionPane.showMessageDialog(null, "An error occured when accessing the database.\n" +
                        "See ../Properties/ErrorLog.txt for more details.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                Util.writeToErrorLog("An error occured when accessing the database: ");
                e.printStackTrace();
            }
        }

        return mapping;
    }


    /**
     * This method takes a HashMap comprising a peaklist (mapping: (m/z, intensity))
     * and transforms it into two double arrays - one with the m/z values, the other with
     * the corresponding intensities (the two double arrays are indexed in the first
     * dimension of the result by MZ_ARRAY and INTENSITIES_ARRAY). Please note that the
     * arrays will be sorted by m/z.
     *
     * @param aPeakList HashMap with the (m/z, intensity) mapping.
     * @return  double[][] with two double arrays, one (indexed in the first dimension
     *                     by the MZ_DATA constant defined on this class) comprising the
     *                     m/z values, the other (indexed in the first dimension by the
     *                     INTENSITIES_DATA constant defined on this class) holding the
     *                     corresponding intensities. Note that the arrays will be sorted
     *                     by m/z.
     */
    private static double[][] transformPeakListToArrays_ms_lims(HashMap aPeakList, TreeSet treeSet) {

        no.uib.prideconverter.util.Properties properties = PRIDEConverter.getProperties();

        double[][] result = new double[2][aPeakList.size()];

        // Use a TreeSet to sort the keys (m/z values).
        treeSet.clear();
        treeSet.addAll(aPeakList.keySet());

        Iterator treeSetIterator = treeSet.iterator();

        int counter = 0;
        Double mz, intensity;

        while (treeSetIterator.hasNext()) {
            // Extract m/z and corresponding intensity.
            mz = (Double) treeSetIterator.next();
            intensity = (Double) aPeakList.get(mz);

            // Store the m/z and intensity.
            result[properties.MZ_ARRAY][counter] = mz;
            result[properties.INTENSITIES_ARRAY][counter] = intensity;
            // Increment counter.
            counter++;
        }

        return result;
    }
}
