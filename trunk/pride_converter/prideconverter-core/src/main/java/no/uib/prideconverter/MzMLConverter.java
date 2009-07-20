package no.uib.prideconverter;

import uk.ac.ebi.pride.model.interfaces.mzdata.Spectrum;

import javax.swing.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;

/**
 * @author Florian Reisinger
 *         Date: 17-Jul-2009
 * @since $version
 */
public class MzMLConverter {

    /**
     * This method transforms spectra from mxML files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     * @return a HashMap
     * @throws IOException in case of an error reading the input file.
     */
    protected static HashMap<String, Long> transformSpectraFromMzML(ArrayList<Spectrum> aTransformedSpectra)
            throws IOException {

        // ToDo: NOT YET IMPLEMENTED!! implement!

        JOptionPane.showMessageDialog(null, "Currently not supported!");

        return new HashMap<String, Long>();

//        //double[][] arrays;
//        float[][] arraysFloat;
//        Collection precursors;
//
//        Collection ionSelection;
//
//        int precursorCharge;
////        double precursorIntensity;
//        float precursorMz;
//        //Collection activation;
//        int spectraCounter = 1;
//        String fileName;
//
//        boolean matchFound = false;
//        //MSXMLParser msXMLParser;
//        MzMLUnmarshaller mzMLParser;
//        MzML mzMLFile;
//
//        int scanCount;
//        //Scan scan;
//
//        int msLevel;
//        //Spectrum spectrum;
//        uk.ac.ebi.jmzml.model.mzml.Spectrum spectrum;
//        BinaryDataArray mzValues, intensities;
//
//        List<uk.ac.ebi.jmzml.model.mzml.Spectrum> spectra;
//        Collection spectrumDescriptionComments;
//        String identified;
//
//        for (int j = 0; j <
//                selectedSourceFiles.size() && !isConversionCanceled(); j++) {
//
//            fileName =
//                    new File(selectedSourceFiles.get(j)).getName();
//            currentFileName = fileName;
//
//            progressDialog.setCurrentFileName(currentFileName);
//
//            mzMLParser =
//                    new MzMLUnmarshaller(new File(selectedSourceFiles.get(j)));
//            mzMLFile = mzMLParser.unmarshall();
//
//            spectra = mzMLFile.getRun().getSpectrumList().getSpectrum();
//            scanCount = spectra.size();
//
//            for (int i = 1; i <=
//                    scanCount; i++) {
//
//                spectrum = spectra.get(i);
//
//                matchFound = false;
//
//                if (selectedSpectraNames.size() > 0) {
//                    for (int k = 0; k <
//                            selectedSpectraNames.size() &&
//                            !matchFound; k++) {
//
//                        if (((String) ((Object[]) selectedSpectraNames.get(k))[0]).equalsIgnoreCase(fileName)) {
//                            if (((Integer) ((Object[]) selectedSpectraNames.get(k))[1]).intValue() ==
//                                    spectrum.getIndex().longValue()) {
//                                matchFound = true;
//                            }
//                        }
//                    }
//                } else {
//                    matchFound = true;
//                }
//
//                if (matchFound) {
//
//                    // find the mz and intensity values
//                    for (int k = 0; k <
//                            spectrum.getBinaryDataArrayList().getBinaryDataArray().size(); k++) {
//                        for (int m = 0; m <
//                                spectrum.getBinaryDataArrayList().getBinaryDataArray().get(i).getCvParam().size(); m++) {
//
//                            if (spectrum.getBinaryDataArrayList().getBinaryDataArray().get(i).getCvParam().get(m).
//        getName().equalsIgnoreCase("m/z array")) {
//                                mzValues = spectrum.getBinaryDataArrayList().getBinaryDataArray().get(i);
//                            } else if (spectrum.getBinaryDataArrayList().getBinaryDataArray().get(i).getCvParam().get(m).
//        getName().equalsIgnoreCase("intensity array")) {
//                                intensities = spectrum.getBinaryDataArrayList().getBinaryDataArray().get(i);
//                            }
//                        }
//                    }
//
//
////                    // Precursor collection.
////                    precursors =
////                            new ArrayList(1);
////
////                    // Ion selection parameters.
////                    ionSelection =
////                            new ArrayList(4);
//
////                    // See if we know the precursor charge, and if so, include it.
////                    if (precursorCharge > 0) {
////                        ionSelection.add(new CvParamImpl("PSI:1000041", "PSI", "ChargeState", 0, Integer.toString(precursorCharge)));
////                    }
//
//// See if we know the precursor intensity
////                    if (precursorIntensity > 1) {
////                        ionSelection.add(new CvParamImpl("PSI:1000042", "PSI", "Intensity", 1, Double.toString(precursorIntensity)));
////                    }
////                    ionSelection.add(new CvParamImpl("PSI:1000040", "PSI", "MassToChargeRatio", 2, Float.toString(
////                            precursorMz)));
//
////                    precursors.add(new PrecursorImpl(null, null, ionSelection, null, 1, spectraCounter, 0));
//
//                    // Spectrum description comments.
//                    spectrumDescriptionComments = new ArrayList();
//
//                    identified = "Not identified";
//                    spectrumDescriptionComments.add(new SpectrumDescCommentImpl(identified));
//
//                      spectrumDescriptionComments = addUserSpectrumComments(spectrumDescriptionComments,
//                          properties.getSpectrumCvParams().get(spectrumKey),
//                          properties.getSpectrumUserParams().get(spectrumKey));
//
////                    new BinaryArrayImpl(mzValues.get, orderIndexInstrumentSourceParameter, dataSource, softwareVersion)
////
////                    if (msLevel == 1) {
////                        spectrum = new SpectrumImpl(
////                                new BinaryArrayImpl(arraysFloat[INTENSITIES_ARRAY], BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
////                                new Double(arraysFloat[MZ_ARRAY][0]), //null
////                                new BinaryArrayImpl(arraysFloat[MZ_ARRAY], BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
////                                1, null,
////                                new Double(arraysFloat[MZ_ARRAY][arraysFloat[MZ_ARRAY].length -
////                                1]), //null,
////                                null,
////                                spectraCounter, null, spectrumDescriptionComments, null, null, null, null);
////                    } else {//msLevel == 2){
////                        spectrum = new SpectrumImpl(
////                                new BinaryArrayImpl(arraysFloat[INTENSITIES_ARRAY], BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
////                                new Double(arraysFloat[MZ_ARRAY][0]), //null,
////                                new BinaryArrayImpl(arraysFloat[MZ_ARRAY], BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
////                                2, null,
////                                new Double(arraysFloat[MZ_ARRAY][arraysFloat[MZ_ARRAY].length -
////                                1]), //null,
////                                null,
////                                spectraCounter, precursors, spectrumDescriptionComments, null, null, null, null);
////                    }
//
////                    double min = Double.MAX_VALUE;
////                    double max = 0.0;
////
////                    for(int k=0; k< arraysFloat[0].length; k++){
////
////                        if(arraysFloat[0][k] > max){
////                            max = arraysFloat[0][k];
////                        }
////                        if(arraysFloat[0][k] < min){
////                            min = arraysFloat[0][k];
////                        }
////                    }
//
//
////                    System.out.println(scan.getLowMz() +  " " + new Double(arraysFloat[MZ_ARRAY][0]) + " " + min +
////                    " " + scan.getHighMz() + " " + new Double(arraysFloat[MZ_ARRAY][arraysFloat[MZ_ARRAY].length - 1]) + " " + max);
//
//
//// Create new mzData spectrum for the fragmentation spectrum.
//// Store (spectrumfileid, spectrumid) mapping.
//                    mapping.put(new Long(spectraCounter), new Long(spectraCounter));
//                    spectraCounter++;
//
//// Store the transformed spectrum.
//                    aTransformedSpectra.add(spectrum);
//                }
//            }
//        }
//
//        totalNumberOfSpectra = spectraCounter - 1;
//
//        return mapping;
    }


}
