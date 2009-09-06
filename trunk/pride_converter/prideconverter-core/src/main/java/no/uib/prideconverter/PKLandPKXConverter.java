package no.uib.prideconverter;

import uk.ac.ebi.pride.model.interfaces.mzdata.*;
import uk.ac.ebi.pride.model.implementation.mzData.*;

import java.util.*;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;

import no.uib.prideconverter.util.Util;
import no.uib.prideconverter.gui.ProgressDialog;

import javax.swing.*;

/**
 * @author Florian Reisinger
 *         Date: 20-Jul-2009
 * @since $version
 */
public class PKLandPKXConverter {

    /**
     * This method transforms spectra from PKL and PKX files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     * @return a HashMap
     * @throws java.io.IOException in case of an error reading the input file.
     */
    protected static HashMap<String, Long> transformSpectraFromPKLAndPKXFiles(ArrayList<Spectrum> aTransformedSpectra)
            throws IOException {

        // temporary variables
        ProgressDialog progressDialog = PRIDEConverter.getProgressDialog();
        no.uib.prideconverter.util.Properties properties = PRIDEConverter.getProperties();

        HashMap<String, Long> mapping = new HashMap<String, Long>();

        double[][] arrays = null;
        Collection<Precursor> precursors;
        Collection<CvParam> ionSelection;
        int charge;
        String fileName, currentLine;
        StringTokenizer tok;
        Double mzRangeStart, mzRangeStop;

        Collection<SpectrumDescComment> spectrumDescriptionComments;
        ArrayList<SupDataArrayBinary> supDataArrays;
        ArrayList<CvParam> cVParams;
        ArrayList<SupDesc> supDescArrays;

        FileReader f;
        BufferedReader b;
        double precursorMz, precursorIntensty, precursorRetentionTime;
        int precursorCharge;
        Vector<Double> masses, intensities, fragmentIonChargeStates;

        Spectrum fragmentation;
        int totalSpectraCounter = 0; // the total spectra count
        int currentSpectraCounter = 0; // the spectra count for each file

        boolean matchFound;
        Integer msLevel = 2;

        progressDialog.setIntermidiate(false);
        progressDialog.setValue(0);
        progressDialog.setMax(properties.getSelectedSourceFiles().size());

        for (int j = 0; j < properties.getSelectedSourceFiles().size() && !PRIDEConverter.isConversionCanceled(); j++) {

            // reset the local spectra counter
            currentSpectraCounter = 0;

            fileName = new File(properties.getSelectedSourceFiles().get(j)).getName();
            PRIDEConverter.setCurrentFileName(fileName);

            progressDialog.setValue(j);
            progressDialog.setString(PRIDEConverter.getCurrentFileName() + " (" + (j + 1) + "/" +
                    properties.getSelectedSourceFiles().size() + ")");

            matchFound = false;

            precursorMz = -1.0;
            precursorIntensty = -1.0;
            precursorCharge = -1;
            precursorRetentionTime = -1;
            msLevel = 2;

            try {
                f = new FileReader(new File(properties.getSelectedSourceFiles().get(j)));
                b = new BufferedReader(f);

                currentLine = b.readLine();
                masses = new Vector<Double>();
                intensities = new Vector<Double>();
                fragmentIonChargeStates = new Vector<Double>();

                boolean firstSpectraFound = false;

                // find the first spectra parent ion
                while (currentLine != null && !firstSpectraFound && !PRIDEConverter.isConversionCanceled()) {

                    if (properties.isCommaTheDecimalSymbol()) {
                        currentLine = currentLine.replaceAll(",", ".");
                    }

                    tok = new StringTokenizer(currentLine);

                    if ((properties.getDataSource().equalsIgnoreCase("VEMS") &&
                            tok.countTokens() == 4) ||
                            (properties.getDataSource().equalsIgnoreCase("Micromass PKL File") &&
                            tok.countTokens() == 3)) {

                        precursorMz = Double.parseDouble(tok.nextToken());
                        precursorIntensty = Double.parseDouble(tok.nextToken());
                        precursorCharge = Integer.parseInt(tok.nextToken());
                        precursorRetentionTime = -1;

                        if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                            precursorRetentionTime = Double.parseDouble(tok.nextToken());
                        }

                        firstSpectraFound = true;
                    }

                    currentLine = b.readLine();
                }

                // find the rest of the spectra
                while (currentLine != null && !PRIDEConverter.isConversionCanceled()) {

                    if (properties.isCommaTheDecimalSymbol()) {
                        currentLine = currentLine.replaceAll(",", ".");
                    }

                    tok = new StringTokenizer(currentLine);

                    if ((properties.getDataSource().equalsIgnoreCase("VEMS") &&
                            tok.countTokens() == 4) ||
                            (properties.getDataSource().equalsIgnoreCase("Micromass PKL File") &&
                            tok.countTokens() == 3)) {

                        // check if the spectra is selected or not
                        matchFound = false;

                        if (properties.getSelectedSpectraKeys().size() > 0) {
                            for (int k = 0; k < properties.getSelectedSpectraKeys().size() && !matchFound; k++) {

                                Object[] temp = (Object[]) properties.getSelectedSpectraKeys().get(k);

                                if (((String) temp[0]).equalsIgnoreCase(fileName)) {
                                    if ((Double) temp[2] == precursorMz) {
                                        if (temp[3] != null) {
                                            if ((Integer) temp[3] == precursorCharge) {
                                                matchFound = true;
                                                PRIDEConverter.setSpectrumKey( PRIDEConverter.generateSpectrumKey(temp) );
                                            }
                                        } else {
                                            matchFound = true;
                                            PRIDEConverter.setSpectrumKey( PRIDEConverter.generateSpectrumKey(temp) );
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

                        if (matchFound) {

                            arrays = new double[3][masses.size()];

                            for (int i = 0; i < masses.size() && !PRIDEConverter.isConversionCanceled(); i++) {
                                arrays[0][i] = masses.get(i);
                                arrays[1][i] = intensities.get(i);

                                if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                                    arrays[2][i] = fragmentIonChargeStates.get(i);
                                }
                            }

                            // Precursor collection.
                            precursors = new ArrayList<Precursor>(1);

                            // Ion selection parameters.
                            ionSelection = new ArrayList<CvParam>(4);

                            // See if we know the precursor charge, and if so, include it.
                            charge = precursorCharge;
                            if (charge > 0) {
                                ionSelection.add(new CvParamImpl("PSI:1000041",
                                        "PSI", "ChargeState", ionSelection.size(),
                                        Integer.toString(charge)));
                            }

                            // precursor intensity
                            ionSelection.add(new CvParamImpl("PSI:1000042",
                                    "PSI", "Intensity", ionSelection.size(),
                                    Double.toString(precursorIntensty)));

                            // precursor m/z
                            ionSelection.add(new CvParamImpl("PSI:1000040",
                                    "PSI", "MassToChargeRatio", ionSelection.size(), Double.toString(
                                    precursorMz)));

                            // precursor retention time
                            if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                                ionSelection.add(new CvParamImpl("PRIDE:0000203",
                                        "PRIDE", "Parent ion retention time", ionSelection.size(), Double.toString(
                                        precursorRetentionTime)));
                            }

                            precursors.add(new PrecursorImpl(null, null,
                                    ionSelection, null, msLevel - 1, 0, 0));

                            spectrumDescriptionComments = new ArrayList<SpectrumDescComment>();
                            spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Not identified"));

                            if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                                supDataArrays = new ArrayList<SupDataArrayBinary>(1);
                                supDataArrays.add(
                                        new SupDataArrayBinaryImpl("Fragment ion charge states",
                                        0,
                                        new BinaryArrayImpl(
                                        arrays[properties.FRAGMENT_ION_CHARGE_STATES_ARRAY],
                                        BinaryArrayImpl.LITTLE_ENDIAN_LABEL)));

                                cVParams = new ArrayList<CvParam>(1);
                                cVParams.add(new CvParamImpl("PRIDE:0000204",
                                        "PRIDE", "fragment ion charge", 0, null));

                                supDescArrays = new ArrayList<SupDesc>(1);
                                supDescArrays.add(new SupDescImpl(null, 0, cVParams, null, null));
                            } else {
                                supDataArrays = null;
                                supDescArrays = null;
                            }

                            if (arrays[properties.MZ_ARRAY].length > 0) {

                                mzRangeStart = arrays[properties.MZ_ARRAY][0];
                                mzRangeStop = arrays[properties.MZ_ARRAY][arrays[properties.MZ_ARRAY].length - 1];

                                spectrumDescriptionComments = PRIDEConverter.addUserSpectrumComments(spectrumDescriptionComments,
                                        properties.getSpectrumCvParams().get(PRIDEConverter.getSpectrumKey()),
                                        properties.getSpectrumUserParams().get( PRIDEConverter.getSpectrumKey() ) );

                                // Create new mzData spectrum for the fragmentation spectrum.
                                fragmentation = new SpectrumImpl(
                                        new BinaryArrayImpl(arrays[properties.INTENSITIES_ARRAY],
                                        BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                        mzRangeStart,
                                        new BinaryArrayImpl(arrays[properties.MZ_ARRAY],
                                        BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                        msLevel,
                                        supDataArrays,
                                        mzRangeStop,
                                        null,
                                        ++totalSpectraCounter, precursors,
                                        spectrumDescriptionComments,
                                        null, null,
                                        null,
                                        supDescArrays);

                                // store the spectrum file, spectrum counter mapping
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
                                aTransformedSpectra.add(fragmentation);
                            }
                        }

                        masses = new Vector<Double>();
                        intensities = new Vector<Double>();
                        fragmentIonChargeStates = new Vector<Double>();
                        msLevel = 2;

                        precursorMz = Double.parseDouble(tok.nextToken());
                        precursorIntensty = Double.parseDouble(tok.nextToken());
                        precursorCharge = Integer.parseInt(tok.nextToken());
                        precursorRetentionTime = -1;

                        if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                            precursorRetentionTime = Double.parseDouble(tok.nextToken());
                        }

                    } else if (!currentLine.equalsIgnoreCase("")) {
                        tok = new StringTokenizer(currentLine);
                        masses.add(new Double(tok.nextToken()));
                        intensities.add(new Double(tok.nextToken()));

                        if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                            fragmentIonChargeStates.add(new Double(tok.nextToken()));
                        }
                    }

                    currentLine = b.readLine();
                }

                // add the last spectra
                // check if the spectra is selected or not
                matchFound = false;

                if (properties.getSelectedSpectraKeys().size() > 0) {
                    for (int k = 0; k < properties.getSelectedSpectraKeys().size() && !matchFound; k++) {

                        Object[] temp = (Object[]) properties.getSelectedSpectraKeys().get(k);

                        if (((String) temp[0]).equalsIgnoreCase(fileName)) {
                            if ((Double) temp[2] == precursorMz) {
                                if (temp[3] != null) {
                                    if ((Integer) temp[3] == precursorCharge) {
                                        matchFound = true;
                                        PRIDEConverter.setSpectrumKey( PRIDEConverter.generateSpectrumKey(temp) );
                                    }
                                } else {
                                    matchFound = true;
                                    PRIDEConverter.setSpectrumKey( PRIDEConverter.generateSpectrumKey(temp) );
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
                }

                if (matchFound) {

                    arrays = new double[3][masses.size()];

                    for (int i = 0; i < masses.size() && !PRIDEConverter.isConversionCanceled(); i++) {
                        arrays[0][i] = masses.get(i);
                        arrays[1][i] = intensities.get(i);

                        if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                            arrays[2][i] = fragmentIonChargeStates.get(i);
                        }
                    }

                    // Precursor collection.
                    precursors = new ArrayList<Precursor>(1);

                    // Ion selection parameters.
                    ionSelection = new ArrayList<CvParam>(4);

                    // See if we know the precursor charge, and if so, include it.
                    charge = precursorCharge;
                    if (charge > 0) {
                        ionSelection.add(new CvParamImpl("PSI:1000041", "PSI",
                                "ChargeState", ionSelection.size(), Integer.toString(charge)));
                    }

                    // precursor intensity
                    ionSelection.add(new CvParamImpl("PSI:1000042", "PSI",
                            "Intensity", ionSelection.size(), Double.toString(precursorIntensty)));

                    // precursor mass
                    ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                            "MassToChargeRatio", ionSelection.size(), Double.toString(
                            precursorMz)));

                    // precursor retention time
                    if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                        ionSelection.add(new CvParamImpl("PRIDE:0000203",
                                "PRIDE", "Parent ion retention time", ionSelection.size(), Double.toString(
                                precursorRetentionTime)));
                    }

                    precursors.add(new PrecursorImpl(null, null, ionSelection, null, msLevel - 1, 0, 0));

                    spectrumDescriptionComments = new ArrayList<SpectrumDescComment>();
                    spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Not identified"));

                    if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                        supDataArrays = new ArrayList<SupDataArrayBinary>(1);
                        supDataArrays.add(
                                new SupDataArrayBinaryImpl("Fragment ion charge states",
                                0,
                                new BinaryArrayImpl(
                                arrays[properties.FRAGMENT_ION_CHARGE_STATES_ARRAY],
                                BinaryArrayImpl.LITTLE_ENDIAN_LABEL)));

                        cVParams = new ArrayList<CvParam>(1);
                        cVParams.add(new CvParamImpl("PRIDE:0000204", "PRIDE", "Fragment ion charge", 0, null));

                        supDescArrays = new ArrayList<SupDesc>(1);
                        supDescArrays.add(
                                new SupDescImpl(null, 0, cVParams, null, null));
                    } else {
                        supDataArrays = null;
                        supDescArrays = null;
                    }

                    if (arrays[properties.MZ_ARRAY].length > 0) {

                        mzRangeStart = arrays[properties.MZ_ARRAY][0];
                        mzRangeStop = arrays[properties.MZ_ARRAY][arrays[properties.MZ_ARRAY].length - 1];

                        spectrumDescriptionComments = PRIDEConverter.addUserSpectrumComments(spectrumDescriptionComments,
                                properties.getSpectrumCvParams().get(PRIDEConverter.getSpectrumKey()),
                                properties.getSpectrumUserParams().get(PRIDEConverter.getSpectrumKey()) );

                        // Create new mzData spectrum for the fragmentation spectrum.
                        fragmentation = new SpectrumImpl(
                                new BinaryArrayImpl(arrays[properties.INTENSITIES_ARRAY],
                                BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                mzRangeStart,
                                new BinaryArrayImpl(arrays[properties.MZ_ARRAY],
                                BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                msLevel,
                                supDataArrays,
                                mzRangeStop,
                                null,
                                ++totalSpectraCounter, precursors,
                                spectrumDescriptionComments,
                                null, null,
                                null, supDescArrays);

                        // store the spectrum file, spectrum counter mapping
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
                        aTransformedSpectra.add(fragmentation);
                    }
                }

                b.close();
                f.close();

            } catch (Exception e) {

                String fileType = "";

                if (properties.getDataSource().equalsIgnoreCase("Micromass PKL File")) {
                    fileType = "Micromass PKL file";
                } else if (properties.getDataSource().equalsIgnoreCase("VEMS")) {
                    fileType = "VEMS PKX file";
                }

                if (!PRIDEConverter.isConversionCanceled()) {

                    Util.writeToErrorLog("Error parsing " + fileType + ": ");
                    e.printStackTrace();

                    JOptionPane.showMessageDialog(null,
                            "The file could not parsed as a " +
                            fileType + ":\n " +
                            new File(properties.getSelectedSourceFiles().get(j)).getName() +
                            "\n\n" +
                            "See ../Properties/ErrorLog.txt for more details.",
                            "Error Parsing File", JOptionPane.ERROR_MESSAGE);
                }
            }

            PRIDEConverter.setTotalNumberOfSpectra(totalSpectraCounter);
        }

        return mapping;
    }
}
