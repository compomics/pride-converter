package no.uib.prideconverter;

import uk.ac.ebi.pride.model.interfaces.mzdata.Spectrum;
import uk.ac.ebi.pride.model.interfaces.mzdata.Precursor;
import uk.ac.ebi.pride.model.interfaces.mzdata.CvParam;
import uk.ac.ebi.pride.model.interfaces.mzdata.SpectrumDescComment;
import uk.ac.ebi.pride.model.implementation.mzData.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.swing.JOptionPane;
import no.uib.prideconverter.gui.ProgressDialog;
import no.uib.prideconverter.util.Util;

/**
 * @author Florian Reisinger
 *         Date: 17-Jul-2009
 * @since $version
 */
public class Ms2Converter {

    /**
     * This method transforms spectra from MS2 files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     * @return a HashMap
     * @throws java.io.IOException in case of an error reading the input file.
     */
    protected static HashMap<String, Long> transformSpectraFromMS2Files(ArrayList<Spectrum> aTransformedSpectra)
            throws IOException {

        // temporary variables
        ProgressDialog progressDialog = PRIDEConverter.getProgressDialog();
        no.uib.prideconverter.util.Properties properties = PRIDEConverter.getProperties();
        no.uib.prideconverter.util.UserProperties userProperties = PRIDEConverter.getUserProperties();


        HashMap<String, Long> mapping = new HashMap<String, Long>();

        double[][] arrays;
        Collection<Precursor> precursors;
        Collection<CvParam> ionSelection;

        int totalSpectraCounter = 0; // the total spectra count
        int currentSpectraCounter = 0; // the spectra count for each file
        String filePath;

        Collection<SpectrumDescComment> spectrumDescriptionComments;
        String identified;
        Spectrum fragmentation;

        boolean matchFound = false;

        Vector<Double> masses, intensities;

        String[] values;
        double precursorMz = 0.0, precursorRetentionTime = -1;
        int precursorCharge = -1;
        Double mzRangeStart, mzRangeStop;
        Integer msLevel = 2;

        progressDialog.setString(null);
        progressDialog.setIntermidiate(true);

        for (int j = 0; j < properties.getSelectedSourceFiles().size() && !PRIDEConverter.isConversionCanceled(); j++) {

            filePath = properties.getSelectedSourceFiles().get(j);

            PRIDEConverter.setCurrentFileName(filePath.substring(filePath.lastIndexOf(File.separator) + 1));

            progressDialog.setIntermidiate(true);
            progressDialog.setString(PRIDEConverter.getCurrentFileName() + " (" + (j + 1) + "/" +
                    properties.getSelectedSourceFiles().size() + ")");

            BufferedReader br = new BufferedReader(new FileReader(filePath));

            String line = null;
            int lineCount = 0;
            masses = new Vector<Double>();
            intensities = new Vector<Double>();
            boolean multipleCharges = false;

            // reset the local spectra counter
            currentSpectraCounter = 0;

            while ((line = br.readLine()) != null && !PRIDEConverter.isConversionCanceled()) {
                // Advance line count.
                lineCount++;

                // Skip empty lines.
                if (line.equals("")) {
                    continue;
                }

                // ignore header
                if (line.startsWith("H")) {
                    continue;
                }

                // charge dependent analysis
                if (line.startsWith("D")) {
                    continue;
                }

                // 'S' marks the start of a new spectrum
                if (line.startsWith("S")) {

                    // store the previous spectrum
                    if (masses.size() > 0) {

                        matchFound = false;

                        if (properties.getSelectedSpectraKeys().size() > 0) {
                            for (int k = 0; k <
                                    properties.getSelectedSpectraKeys().size() &&
                                    !matchFound && !PRIDEConverter.isConversionCanceled(); k++) {

                                Object[] temp = (Object[]) properties.getSelectedSpectraKeys().get(k);

                                if (((String) temp[0]).equalsIgnoreCase(PRIDEConverter.getCurrentFileName())) {

                                    if ((Double) temp[2] == precursorMz) {

                                        if (temp[3] != null) {
                                            if ((Integer) temp[3] == precursorCharge) {
                                                matchFound = true;
                                                PRIDEConverter.setSpectrumKey(PRIDEConverter.generateSpectrumKey(temp));
                                            }
                                        } else {
                                            matchFound = true;
                                            PRIDEConverter.setSpectrumKey(PRIDEConverter.generateSpectrumKey(temp));
                                        }

                                        // ms level
                                        if (temp[4] != null) {
                                            msLevel = (Integer) temp[4];
                                        } else {
                                            // defaults to MS2
                                            msLevel = 2;
                                        }
                                    }
                                }
                            }
                        } else {
                            matchFound = true;
                            msLevel = 2;
                        }

                        if (matchFound && !PRIDEConverter.isConversionCanceled()) {

                            arrays = new double[2][masses.size()];

                            for (int i = 0; i < masses.size() && !PRIDEConverter.isConversionCanceled(); i++) {
                                arrays[0][i] = masses.get(i);
                                arrays[1][i] = intensities.get(i);
                            }

                            // Precursor collection.
                            precursors = new ArrayList<Precursor>(1);

                            // Ion selection parameters.
                            ionSelection = new ArrayList<CvParam>(3);

                            if (precursorCharge > 0) {
                                ionSelection.add(new CvParamImpl("PSI:1000041",
                                        "PSI", "ChargeState", ionSelection.size(),
                                        Integer.toString(precursorCharge)));
                            }

//                            if (precursorIntensity > 1) {
//                                ionSelection.add(new CvParamImpl("PSI:1000042",
//                                        "PSI", "Intensity", ionSelection.size(),
//                                        Double.toString(precursorIntensity)));
//                            }

                            // precursor retention time
                            if (precursorRetentionTime != -1) {
                                ionSelection.add(new CvParamImpl("PRIDE:0000203",
                                        "PRIDE", "Parent ion retention time", ionSelection.size(), Double.toString(
                                        precursorRetentionTime)));
                            }

                            ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                                    "MassToChargeRatio", ionSelection.size(), Double.toString(
                                    precursorMz)));

                            precursors.add(new PrecursorImpl(null, null,
                                    ionSelection, null, msLevel - 1, 0, 0));

                            // Spectrum description comments.
                            spectrumDescriptionComments = new ArrayList<SpectrumDescComment>();

                            identified = "Not identified";
                            spectrumDescriptionComments.add(new SpectrumDescCommentImpl(identified));

                            if (arrays[properties.MZ_ARRAY].length > 0) {

                                mzRangeStart = arrays[properties.MZ_ARRAY][0];
                                mzRangeStop = arrays[properties.MZ_ARRAY][arrays[properties.MZ_ARRAY].length - 1];

                                spectrumDescriptionComments = PRIDEConverter.addUserSpectrumComments(spectrumDescriptionComments,
                                        properties.getSpectrumCvParams().get(PRIDEConverter.getSpectrumKey()),
                                        properties.getSpectrumUserParams().get(PRIDEConverter.getSpectrumKey()));

                                fragmentation =
                                        new SpectrumImpl(
                                        new BinaryArrayImpl(arrays[properties.INTENSITIES_ARRAY],
                                        BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                        mzRangeStart,
                                        new BinaryArrayImpl(arrays[properties.MZ_ARRAY],
                                        BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                        msLevel, null,
                                        mzRangeStop,
                                        null,
                                        ++totalSpectraCounter, precursors,
                                        spectrumDescriptionComments,
                                        null, null,
                                        null, null);

                                // Store (spectrumfileid, spectrumid) mapping.
                                Long xTmp = mapping.put(PRIDEConverter.getCurrentFileName() + "_"
                                        + Integer.toString(++currentSpectraCounter), (long) totalSpectraCounter);

                                if (xTmp != null) {
                                    // we already stored a result for this ID!!!
                                    JOptionPane.showMessageDialog(null, "Ambiguous spectrum mapping. Please consult " +
                                            "the error log file for details.", "Mapping Error", JOptionPane.ERROR_MESSAGE);
                                    Util.writeToErrorLog("Ambiguous spectrum mapping for ID '" + currentSpectraCounter
                                            + "' and spectrum file '" + PRIDEConverter.getCurrentFileName() + "'." );
                                }

                                // Store the transformed spectrum.
                                aTransformedSpectra.add(fragmentation);
                            }
                        }

                        multipleCharges = false;
                        precursorRetentionTime = -1;
                        precursorCharge = -1;
                        masses = new Vector<Double>();
                        intensities = new Vector<Double>();
                    }

                    if (properties.isCommaTheDecimalSymbol()) {
                        line = line.replaceAll(",", ".");
                    }

                    values = line.split("\\s");

                    precursorMz = new Double(values[3]);

                } else if (line.startsWith("I")) {
                    if (line.lastIndexOf("RetTime") != -1) {

                        if (properties.isCommaTheDecimalSymbol()) {
                            line = line.replaceAll(",", ".");
                        }

                        values = line.split("\\s");
                        precursorRetentionTime = new Double(values[2]);
                    }
                } else if (line.startsWith("Z")) {
                    if (precursorCharge != -1) {
                        multipleCharges = true;
                        precursorCharge = 0;
                    } else {
                        values = line.split("\\s");
                        precursorCharge = Integer.parseInt(values[1]);
                    }
                } else { // read the peaks

                    if (properties.isCommaTheDecimalSymbol()) {
                        line = line.replaceAll(",", ".");
                    }

                    values = line.split("\\s");

                    int count = values.length;

                    if (count == 2) {

                        masses.add(Double.parseDouble(values[0]));
                        intensities.add(new Double(values[1]));

                    } else {
                        System.err.println("\n\nUnrecognized line at line number " +
                                lineCount + ": '" + line + "'!\n");
                    }
                }
            }

            br.close();

            // store the last spectrum
            if (!PRIDEConverter.isConversionCanceled()) {

                if (masses.size() > 0) {

                    matchFound = false;

                    if (properties.getSelectedSpectraKeys().size() > 0) {
                        for (int k = 0; k < properties.getSelectedSpectraKeys().size() && !matchFound && !PRIDEConverter.isConversionCanceled(); k++) {

                            Object[] temp = (Object[]) properties.getSelectedSpectraKeys().get(k);

                            if (((String) temp[0]).equalsIgnoreCase(PRIDEConverter.getCurrentFileName())) {
                                if ((Double) temp[2] == precursorMz) {
                                    if (temp[3] != null) {
                                        if ((Integer) temp[3] == precursorCharge) {
                                            matchFound = true;
                                            PRIDEConverter.setSpectrumKey(PRIDEConverter.generateSpectrumKey(temp));
                                        }
                                    } else {
                                        matchFound = true;
                                        PRIDEConverter.setSpectrumKey(PRIDEConverter.generateSpectrumKey(temp));
                                    }
                                }
                            }
                        }
                    } else {
                        matchFound = true;
                    }

                    if (matchFound && !PRIDEConverter.isConversionCanceled()) {

                        arrays = new double[2][masses.size()];

                        for (int i = 0; i < masses.size() && !PRIDEConverter.isConversionCanceled(); i++) {
                            arrays[0][i] = masses.get(i);
                            arrays[1][i] = intensities.get(i);
                        }

                        // Precursor collection.
                        precursors = new ArrayList<Precursor>(1);

                        // Ion selection parameters.
                        ionSelection = new ArrayList<CvParam>(3);

                        if (precursorCharge > 0) {
                            ionSelection.add(new CvParamImpl("PSI:1000041",
                                    "PSI", "ChargeState", ionSelection.size(),
                                    Integer.toString(precursorCharge)));
                        }

//                            if (precursorIntensity > 1) {
//                                ionSelection.add(new CvParamImpl("PSI:1000042",
//                                        "PSI", "Intensity", ionSelection.size(),
//                                        Double.toString(precursorIntensity)));
//                            }

                        // precursor retention time
                        if (precursorRetentionTime != -1) {
                            ionSelection.add(new CvParamImpl("PRIDE:0000203",
                                    "PRIDE", "Parent ion retention time", ionSelection.size(), Double.toString(
                                    precursorRetentionTime)));
                        }

                        ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                                "MassToChargeRatio", ionSelection.size(), Double.toString(precursorMz)));

                        precursors.add(new PrecursorImpl(null, null, ionSelection, null, 1, 0, 0));

                        // Spectrum description comments.
                        spectrumDescriptionComments = new ArrayList<SpectrumDescComment>();

                        identified = "Not identified";
                        spectrumDescriptionComments.add(new SpectrumDescCommentImpl(identified));

                        if (arrays[properties.MZ_ARRAY].length > 0) {

                            mzRangeStart = arrays[properties.MZ_ARRAY][0];
                            mzRangeStop = arrays[properties.MZ_ARRAY][arrays[properties.MZ_ARRAY].length - 1];

                            spectrumDescriptionComments = PRIDEConverter.addUserSpectrumComments(spectrumDescriptionComments,
                                    properties.getSpectrumCvParams().get(PRIDEConverter.getSpectrumKey()),
                                    properties.getSpectrumUserParams().get(PRIDEConverter.getSpectrumKey()));

                            fragmentation =
                                    new SpectrumImpl(
                                    new BinaryArrayImpl(arrays[properties.INTENSITIES_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    mzRangeStart,
                                    new BinaryArrayImpl(arrays[properties.MZ_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    2, null,
                                    mzRangeStop,
                                    null,
                                    ++totalSpectraCounter, precursors,
                                    spectrumDescriptionComments,
                                    null, null,
                                    null, null);

                            // Store (spectrumfileid, spectrumid) mapping.
                            Long xTmp = mapping.put(PRIDEConverter.getCurrentFileName() + "_"
                                    + Integer.toString(++currentSpectraCounter), (long) totalSpectraCounter);

                            if (xTmp != null) {
                                // we already stored a result for this ID!!!
                                JOptionPane.showMessageDialog(null, "Ambiguous spectrum mapping. Please consult " +
                                        "the error log file for details.", "Mapping Error", JOptionPane.ERROR_MESSAGE);
                                Util.writeToErrorLog("Ambiguous spectrum mapping for ID '" + currentSpectraCounter
                                        + "' and spectrum file '" + PRIDEConverter.getCurrentFileName() + "'." );
                            }
                            
                            // Store the transformed spectrum.
                            aTransformedSpectra.add(fragmentation);
                        }
                    }
                }
            }
        }

        PRIDEConverter.setTotalNumberOfSpectra(totalSpectraCounter);

        return mapping;
    }
}
