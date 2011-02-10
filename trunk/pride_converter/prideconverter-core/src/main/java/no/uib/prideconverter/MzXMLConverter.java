package no.uib.prideconverter;

import org.systemsbiology.jrap.stax.MSXMLParser;
import org.systemsbiology.jrap.stax.Scan;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.io.IOException;
import java.io.File;

import javax.swing.JOptionPane;
import no.uib.prideconverter.util.Util;
import uk.ac.ebi.pride.model.interfaces.mzdata.Spectrum;
import uk.ac.ebi.pride.model.interfaces.mzdata.CvParam;
import uk.ac.ebi.pride.model.interfaces.mzdata.Precursor;
import uk.ac.ebi.pride.model.interfaces.mzdata.SpectrumDescComment;
import uk.ac.ebi.pride.model.implementation.mzData.*;

/**
 * @author Florian Reisinger
 *         Date: 17-Jul-2009
 * @since $version
 */
public class MzXMLConverter {

    /**
     * This method transforms spectra from mzXML files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     * @return a HashMap
     * @throws IOException in case of an error reading the input file.
     */
    protected static HashMap<String, Long> transformSpectraFromMzXML(ArrayList<Spectrum> aTransformedSpectra)
            throws IOException {

        HashMap<String, Long> mapping = new HashMap<String, Long>();

        double[][] arraysFloat;
        Collection<Precursor> precursors;
        Collection<CvParam> ionSelection;

        int precursorCharge;
        float precursorMz;
        int totalSpectraCounter = 0; // the total spectra count
        int currentSpectraCounter = 0; // the spectra count for each file
        String fileName;

        boolean matchFound = false;
        MSXMLParser msXMLParser;

        int scanCount, msLevel;
        Scan scan;
        no.uib.prideconverter.util.Properties properties = PRIDEConverter.getProperties();

        Spectrum spectrum;
        Double mzRangeStart, mzRangeStop;
        Collection<SpectrumDescComment> spectrumDescriptionComments;
        String identified;

        PRIDEConverter.getProgressDialog().setIntermidiate(false);
        PRIDEConverter.getProgressDialog().setValue(0);

        for (int j = 0; j < properties.getSelectedSourceFiles().size() && !PRIDEConverter.isConversionCanceled(); j++) {

            // reset the local spectra counter
            currentSpectraCounter = 0;

            fileName = new File(properties.getSelectedSourceFiles().get(j)).getName();

            PRIDEConverter.setCurrentFileName(fileName);

            PRIDEConverter.getProgressDialog().setValue(0);
            PRIDEConverter.getProgressDialog().setString(PRIDEConverter.getCurrentFileName() + " (" + (j + 1) + "/" +
                    properties.getSelectedSourceFiles().size() + ")");

            msXMLParser = new MSXMLParser(
                    new File(properties.getSelectedSourceFiles().get(j)).getAbsolutePath());

            scanCount = msXMLParser.getScanCount();

            PRIDEConverter.getProgressDialog().setMax(scanCount);

            for (int i = 1; i <= scanCount; i++) {

                PRIDEConverter.getProgressDialog().setValue(i);

                scan = msXMLParser.rap(i);
                precursorCharge = scan.getHeader().getPrecursorCharge();
                precursorMz = scan.getHeader().getPrecursorMz();
                msLevel = scan.getHeader().getMsLevel();

                matchFound = false;

                if (properties.getSelectedSpectraKeys().size() > 0) {
                    for (int k = 0; k < properties.getSelectedSpectraKeys().size() && !matchFound; k++) {

                        Object[] temp = (Object[]) properties.getSelectedSpectraKeys().get(k);

                        if (((String) temp[0]).equalsIgnoreCase(fileName)) {
                            if ((Integer) temp[1] == scan.getHeader().getNum()) {
                                matchFound = true;
                                PRIDEConverter.setSpectrumKey(PRIDEConverter.generateSpectrumKey(temp));
                            }
                        }
                    }
                } else {
                    matchFound = true;
                }

                if (matchFound) {

                    // Transform peaklist into float arrays.
                    arraysFloat = scan.getMassIntensityList();

                    // Precursor collection.
                    precursors = new ArrayList<Precursor>();

                    // Ion selection parameters.
                    ionSelection = new ArrayList<CvParam>();

                    // See if we know the precursor charge, and if so, include it.
                    if (precursorCharge > 0) {
                        ionSelection.add(new CvParamImpl(Util.MS_CHARGESTATE_ACC, Util.MS_CV,
                                Util.MS_CHARGESTATE_TERM, ionSelection.size(),
                                Integer.toString(precursorCharge)));
                    }

                    ionSelection.add(new CvParamImpl(Util.MS_M2ZRATIO_ACC, Util.MS_CV,
                            Util.MS_M2ZRATIO_TERM, ionSelection.size(), Float.toString(precursorMz)));

                    precursors.add(new PrecursorImpl(null, null, ionSelection, null, 1, 0, 0));

                    // Spectrum description comments.
                    spectrumDescriptionComments = new ArrayList<SpectrumDescComment>();

                    identified = "Not identified";
                    spectrumDescriptionComments.add(new SpectrumDescCommentImpl(identified));

                    if (arraysFloat[properties.MZ_ARRAY].length > 0) {

                        mzRangeStart = (double) arraysFloat[properties.MZ_ARRAY][0];
                        mzRangeStop = (double) arraysFloat[properties.MZ_ARRAY][arraysFloat[properties.MZ_ARRAY].length - 1];

                        spectrumDescriptionComments = PRIDEConverter.addUserSpectrumComments(spectrumDescriptionComments,
                                properties.getSpectrumCvParams().get(PRIDEConverter.getSpectrumKey()),
                                properties.getSpectrumUserParams().get(PRIDEConverter.getSpectrumKey()));

                        if (msLevel == 1) {
                            spectrum = new SpectrumImpl(
                                    new BinaryArrayImpl(arraysFloat[properties.INTENSITIES_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    mzRangeStart,
                                    new BinaryArrayImpl(arraysFloat[properties.MZ_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    1, null,
                                    (double) arraysFloat[properties.MZ_ARRAY][arraysFloat[properties.MZ_ARRAY].length -1],
                                    null,
                                    ++totalSpectraCounter, null,
                                    spectrumDescriptionComments,
                                    null, null,
                                    null, null);
                        } else { //msLevel == 2){
                            spectrum = new SpectrumImpl(
                                    new BinaryArrayImpl(arraysFloat[properties.INTENSITIES_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    mzRangeStart,
                                    new BinaryArrayImpl(arraysFloat[properties.MZ_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    2, null,
                                    mzRangeStop,
                                    null,
                                    ++totalSpectraCounter, precursors,
                                    spectrumDescriptionComments,
                                    null, null,
                                    null, null);
                        }

                        // Create new mzData spectrum for the fragmentation spectrum.
                        // Store (spectrumfileid, spectrumid) mapping.
                        Long xTmp = mapping.put(PRIDEConverter.getCurrentFileName() + "_"
                                + Integer.toString(++currentSpectraCounter), (long) totalSpectraCounter);

                        if (xTmp != null) {
                            // we already stored a result for this ID!!!
                            JOptionPane.showMessageDialog(null, "Ambiguous spectrum mapping. Consult " +
                                    "the error log file for details.", "Mapping Error", JOptionPane.ERROR_MESSAGE);
                            Util.writeToErrorLog("Ambiguous spectrum mapping for ID '" + currentSpectraCounter
                                    + "' and spectrum file '" + PRIDEConverter.getCurrentFileName() + "'." );
                            PRIDEConverter.setCancelConversion(true);
                        }

                        // Store the transformed spectrum.
                        aTransformedSpectra.add(spectrum);
                    }
                }
            }
        }

        PRIDEConverter.setTotalNumberOfSpectra(totalSpectraCounter);

        return mapping;
    }
}
