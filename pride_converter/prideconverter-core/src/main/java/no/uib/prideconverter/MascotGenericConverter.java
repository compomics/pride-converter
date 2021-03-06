package no.uib.prideconverter;

import no.uib.prideconverter.util.Util;
import uk.ac.ebi.pride.model.implementation.mzData.BinaryArrayImpl;
import uk.ac.ebi.pride.model.implementation.mzData.CvParamImpl;
import uk.ac.ebi.pride.model.implementation.mzData.PrecursorImpl;
import uk.ac.ebi.pride.model.implementation.mzData.SpectrumImpl;
import uk.ac.ebi.pride.model.interfaces.mzdata.CvParam;
import uk.ac.ebi.pride.model.interfaces.mzdata.Precursor;
import uk.ac.ebi.pride.model.interfaces.mzdata.Spectrum;
import uk.ac.ebi.pride.model.interfaces.mzdata.SpectrumDescComment;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * @author Florian Reisinger
 *         Date: 17-Jul-2009
 * @since $version
 */
public class MascotGenericConverter {


    
    /**
     * This method transforms spectra from Mascot Generic files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     * @return a HashMap
     * @throws IOException in case of errors while reading the input files.
     */
    protected static HashMap<String, Long> transformSpectraFromMascotGenericFile(ArrayList<Spectrum> aTransformedSpectra)
            throws IOException {

        HashMap<String, Long> mapping = new HashMap<String, Long>();

        double[][] arrays;
        Collection<Precursor> precursors;
        Collection<CvParam> ionSelection;
        Collection<SpectrumDescComment> spectrumDescriptionComments;

        int totalSpectraCounter = 0; // the total spectra count
        int currentSpectraCounter = 0; // the spectra count for each mgf file
        String filePath;

        Spectrum fragmentation;

        boolean matchFound = false;

        Vector<Double> masses, intensities;
        Vector<Integer> charges;
        String[] values;
        Double precursorMz = null, precursorIntensity = null;
        double precursorRetentionTime = -1;
        Integer precursorCharge = null;
        Double mzRangeStart, mzRangeStop;

        PRIDEConverter.getProgressDialog().setString(null);
        PRIDEConverter.getProgressDialog().setIntermidiate(true);

        for (int j = 0; j < PRIDEConverter.getProperties().getSelectedSourceFiles().size() && !PRIDEConverter.isConversionCanceled(); j++) {

            filePath = PRIDEConverter.getProperties().getSelectedSourceFiles().get(j);

            PRIDEConverter.setCurrentFileName(filePath.substring(filePath.lastIndexOf(File.separator) + 1));

            PRIDEConverter.getProgressDialog().setIntermidiate(true);
            PRIDEConverter.getProgressDialog().setString(PRIDEConverter.getCurrentFileName() + " (" + (j + 1) + "/" +
                    PRIDEConverter.getProperties().getSelectedSourceFiles().size() + ")");

            BufferedReader br = new BufferedReader(new FileReader(filePath));

            String line = null;
            int lineCount = 0;
            masses = new Vector<Double>();
            intensities = new Vector<Double>();
            charges = new Vector<Integer>();
            boolean inSpectrum = false;
            Integer msLevel = 2; // defaults to MS/MS

            // reset the local spectra counter
            currentSpectraCounter = 0;

            while ((line = br.readLine()) != null && !PRIDEConverter.isConversionCanceled()) {
                // Advance line count.
                lineCount++;
                // Delete leading/trailing spaces.
                line = line.trim();
                // Skip empty lines.
                if (line.equals("")) {
                    continue;
                }
                // First line can be 'CHARGE'.
                if (lineCount == 1 && line.startsWith("CHARGE")) {
                    continue;
                }

                // BEGIN IONS marks the start of the real file.
                if (line.equals("BEGIN IONS")) {
                    inSpectrum = true;
                } // END IONS marks the end.
                else if (line.equals("END IONS")) {
                    inSpectrum = false;

                    matchFound = false;

                    if (PRIDEConverter.getProperties().getSelectedSpectraKeys().size() > 0) {
                        for (int k = 0; k < PRIDEConverter.getProperties().getSelectedSpectraKeys().size() &&
                                !matchFound && !PRIDEConverter.isConversionCanceled(); k++) {

                            Object[] temp = (Object[]) PRIDEConverter.getProperties().getSelectedSpectraKeys().get(k);

                            // ToDo: this does not guarantee uniquenes!! take optional TITLE parameter into account! store as temp[1] = ID column

                            if (((String) temp[0]).equalsIgnoreCase(PRIDEConverter.getCurrentFileName())) {
                                if (((Double) temp[2]).doubleValue() == precursorMz) {
                                    if (temp[3] != null && precursorCharge != null) {
                                        if (((Integer) temp[3]).intValue() == precursorCharge) {
                                            matchFound = true;
                                            PRIDEConverter.setSpectrumKey(PRIDEConverter.generateSpectrumKey(temp));
                                        }
                                    } else {
                                        matchFound = true;
                                        PRIDEConverter.setSpectrumKey(PRIDEConverter.generateSpectrumKey(temp));
                                    }
                                }
                            }

                            // ms level
                            if (temp[4] != null) {
                                msLevel = (Integer) temp[4];
                            } else {
                                // defaults to MS2
                                msLevel = 2;
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
                        precursors = new ArrayList<Precursor>();

                        // Ion selection parameters.
                        ionSelection = new ArrayList<CvParam>();

                        if (precursorCharge != null) {
                            if (precursorCharge > 0) {
                                ionSelection.add(new CvParamImpl(Util.MS_CHARGESTATE_ACC,
                                        Util.MS_CV, Util.MS_CHARGESTATE_TERM, 0,
                                        Integer.toString(precursorCharge)));
                            }
                        }

                        if (precursorIntensity != 0) {
                            if (precursorIntensity > 1) {
                                ionSelection.add(new CvParamImpl(Util.MS_INTENSITY_ACC,
                                        Util.MS_CV, Util.MS_INTENSITY_TERM, 1,
                                        Double.toString(precursorIntensity)));
                            }
                        }

                         if (precursorRetentionTime != -1) {
                                ionSelection.add(new CvParamImpl("PRIDE:0000203",
                                        "PRIDE", "Parent ion retention time", ionSelection.size(),
                                        Double.toString(precursorRetentionTime)));
                            }

                        if (precursorMz != null) {
                            ionSelection.add(new CvParamImpl(Util.MS_M2ZRATIO_ACC, Util.MS_CV,
                                    Util.MS_M2ZRATIO_TERM, 2, Double.toString(
                                    precursorMz)));
                        }

                        if (ionSelection.size() > 0) {
                            precursors.add(new PrecursorImpl(null, null,
                                    ionSelection, null, msLevel - 1, 0, 0));
                        } else {
                            precursors = null;


                        }

                        // Spectrum description comments.
                        spectrumDescriptionComments = new ArrayList<SpectrumDescComment>();

                        if (arrays[PRIDEConverter.getProperties().MZ_ARRAY].length > 0) {

                            mzRangeStart = arrays[PRIDEConverter.getProperties().MZ_ARRAY][0];
                            mzRangeStop = arrays[PRIDEConverter.getProperties().MZ_ARRAY][arrays[PRIDEConverter.getProperties().MZ_ARRAY].length - 1];

                            spectrumDescriptionComments = PRIDEConverter.addUserSpectrumComments(spectrumDescriptionComments,
                                    PRIDEConverter.getProperties().getSpectrumCvParams().get(PRIDEConverter.getSpectrumKey()),
                                    PRIDEConverter.getProperties().getSpectrumUserParams().get(PRIDEConverter.getSpectrumKey()));

                            fragmentation =
                                    new SpectrumImpl(
                                    new BinaryArrayImpl(arrays[PRIDEConverter.getProperties().INTENSITIES_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    mzRangeStart,
                                    new BinaryArrayImpl(arrays[PRIDEConverter.getProperties().MZ_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    msLevel, null,
                                    mzRangeStop,
                                    null,
                                    ++totalSpectraCounter, precursors,
                                    spectrumDescriptionComments,
                                    null, null,
                                    null, null);

                            // Store (spectrumfileid, spectrumid) mapping.
                            Long xTmp = mapping.put(PRIDEConverter.getCurrentFileName() + "_" +
                                    Integer.toString(++currentSpectraCounter), (long) totalSpectraCounter);

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
                    charges = new Vector<Integer>();

                    precursorMz = null;
                    precursorIntensity = null;
                    precursorCharge = null;
                    precursorRetentionTime = -1;

                } // Read embedded parameters.
                else if (inSpectrum && (line.indexOf("=") >= 0)) {
                    // Find the starting location of the value (which is one beyond the location
                    // of the '=').
                    int equalSignIndex = line.indexOf("=");

                    if (line.startsWith("PEPMASS")) {
                        // PEPMASS line found.
                        String value = line.substring(equalSignIndex + 1);//.trim();
                        //StringTokenizer st = new StringTokenizer(value, " \t");

                        if (PRIDEConverter.getProperties().isCommaTheDecimalSymbol()) {
                            value = value.replaceAll(",", ".");
                        }

                        values = value.split("\\s");
                        precursorMz = Double.parseDouble(values[0]);

                        if (values.length > 1) {
                            precursorIntensity = Double.parseDouble(values[1]);
                        } else {
                            precursorIntensity = 0.0;
                        }
                    } else if (line.startsWith("CHARGE")) {
                        // CHARGE line found.
                        // Note the extra parsing to read a Mascot Generic File charge (eg., 1+).
                        precursorCharge = Util.extractCharge(line.substring(equalSignIndex + 1));
                    } else if (line.startsWith("RTINSECONDS")) {
                        String value = line.substring(equalSignIndex + 1);
                        String[] split_values =  Pattern.compile("\\D").split(value);
                        value=split_values[0];
                        precursorRetentionTime = new Double(value);

                    }

                } // Read peaks, minding the possibility of charge present!
                else if (inSpectrum) {
                    // We're inside the spectrum, with no '=' in the line, so it should be
                    // a peak line.
                    // A peak line should be either of the following two:
                    // 234.56 789
                    // 234.56 789   1+

                    if (PRIDEConverter.getProperties().isCommaTheDecimalSymbol()) {
                        line = line.replaceAll(",", ".");
                    }

                    values = line.split("\\s+");

                    int count = values.length;

                    if (count == 2 || count == 3) {

                        masses.add(Double.parseDouble(values[0]));
                        intensities.add(new Double(values[1]));

                        if (count == 3) {
                            charges.add(Util.extractCharge(values[2]));
                        }
                    } else {
                        System.err.println("\n\nUnrecognized line at line number " +
                                lineCount + ": '" + line + "'!\n");
                    }
                }
            }

            br.close();
        }

        PRIDEConverter.setTotalNumberOfSpectra(totalSpectraCounter);

        return mapping;
    }
}
