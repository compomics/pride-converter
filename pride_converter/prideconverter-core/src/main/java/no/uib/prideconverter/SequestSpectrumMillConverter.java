package no.uib.prideconverter;

import no.uib.prideconverter.util.IdentificationGeneral;
import no.uib.prideconverter.util.Util;
import no.uib.prideconverter.util.iTRAQ;
import no.uib.prideconverter.gui.ModificationMapping;
import no.uib.prideconverter.gui.ProgressDialog;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;

import uk.ac.ebi.pride.model.interfaces.core.FragmentIon;
import uk.ac.ebi.pride.model.interfaces.core.MassDelta;
import uk.ac.ebi.pride.model.interfaces.mzdata.*;
import uk.ac.ebi.pride.model.implementation.mzData.*;
import uk.ac.ebi.pride.model.implementation.core.MonoMassDeltaImpl;
import uk.ac.ebi.pride.model.implementation.core.ModificationImpl;

import javax.swing.*;

/**
 * @author Florian Reisinger
 *         Date: 20-Jul-2009
 * @since $version
 */
public class SequestSpectrumMillConverter {

    /**
     * This method transforms spectra from Spectrum Mill and SEQUEST files,
     * returning a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     * @return a HashMap
     * @throws java.io.IOException in case of an error reading the input file.
     */
    protected static HashMap<String, Long> transformSpectraFromSequestAndSpectrumMill(
            ArrayList<Spectrum> aTransformedSpectra) throws IOException {

        // temporary variables
        ProgressDialog progressDialog = PRIDEConverter.getProgressDialog();
        no.uib.prideconverter.util.Properties properties = PRIDEConverter.getProperties();
        no.uib.prideconverter.util.UserProperties userProperties = PRIDEConverter.getUserProperties();

        HashMap<String, Long> mapping = new HashMap<String, Long>();

        double[][] arrays = null;
        Collection<Precursor> precursors;
        Collection<CvParam> ionSelection;

        int charge;
        String fileName;

        PRIDEConverter.setIds(new ArrayList<IdentificationGeneral>());
        StringTokenizer tok;
        String accession, database, sequence = null;
        String prospectorVersion = null; // currently not used
        String coverage_map = null;
        Integer start = null;
        Double score = null;
        Double deltaCn = null;
        Double sp = null;
        String rankSp = null;
        Double mH = null;
        String ions = null;
        String upstreamFlankingSequence = null;
        String downstreamFlankingSequence = null;
        String fragmentIonMap = null;
        int totalSpectraCounter = 0;
        String currentLine;

        String modificationLine;
        Vector<String> allLines;
        boolean hitFound;

        Pattern pattern;
        Matcher matcher;
        String modificationName, currentModification;

        Integer modificationLocation;
        String modificationNameShort;
        double modificationMass;
        ArrayList<CvParam> modificationCVParams;
        ArrayList<MassDelta> monoMasses;
        ArrayList<ModificationImpl> peptideModifications;
        ArrayList<FragmentIon> fragmentIons;

        boolean errorDetected;
        FileReader f;
        BufferedReader b;
        boolean emptyLineFound;
        double precursorMh, precursorMz;
        double precursorIntensty;
        int precursorCharge;
        Vector<Double> masses, intensities;
        File identificationFile;
        Collection<SpectrumDescComment> spectrumDescriptionComments;
        String identified;
        boolean matchFound;
        int numberOfHits, index, currentIndex;
        HashMap<String, Integer> spectrumMillColumnHeaders;
        Vector<String> spectrumMillValues;
        StringTokenizer startTok, modTok;
        String currentMod, tempFileName;
        ArrayList<CvParam> cVParams;
        ArrayList<UserParam> userParams;
        String variableModificationPattern = "[(]\\w.[ ][+-]\\d*[.]\\d{4}[)]";
        String fixedModificationPattern = "\\w[=]\\d*[.]\\d{4}";
        String[] sequenceArray;
        Spectrum fragmentation;
        String[] values;
        Double mzRangeStart, mzRangeStop;
        Integer msLevel = 2;

        progressDialog.setIntermidiate(false);
        progressDialog.setValue(0);
        progressDialog.setMax(properties.getSelectedSourceFiles().size());

        for (int j = 0; j < properties.getSelectedSourceFiles().size() && !PRIDEConverter.isConversionCanceled(); j++) {

            fileName = properties.getSelectedSourceFiles().get(j).substring(
                    properties.getSelectedSourceFiles().get(j).lastIndexOf(File.separator) + 1);
            PRIDEConverter.setCurrentFileName(fileName);

            progressDialog.setValue(j);
            progressDialog.setString(PRIDEConverter.getCurrentFileName() + " (" + (j + 1) + "/" +
                    properties.getSelectedSourceFiles().size() + ")");

            errorDetected = false;

            boolean isSelected = false;
            msLevel = 2;

            if (properties.getSelectedSpectraKeys().size() > 0) {
                for (int k = 0; k < properties.getSelectedSpectraKeys().size() && !isSelected && !PRIDEConverter.isConversionCanceled(); k++) {

                    Object[] temp = (Object[]) properties.getSelectedSpectraKeys().get(k);

                    if (((String) temp[0]).equalsIgnoreCase(PRIDEConverter.getCurrentFileName())) {
                        isSelected = true;
                        PRIDEConverter.setSpectrumKey( PRIDEConverter.generateSpectrumKey(temp) );

                        if (properties.getDataSource().equalsIgnoreCase("SEQUEST DTA File")) {
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
                if (properties.getSpectraSelectionCriteria() != null) {
                    if (userProperties.getCurrentFileNameSelectionCriteriaSeparator().length() == 0) {
                        tok = new StringTokenizer(properties.getSpectraSelectionCriteria());
                    } else {
                        tok = new StringTokenizer(properties.getSpectraSelectionCriteria(),
                                userProperties.getCurrentFileNameSelectionCriteriaSeparator());
                    }

                    String tempToken;

                    while (tok.hasMoreTokens() && !isSelected) {

                        tempToken = tok.nextToken();
                        tempToken = tempToken.trim();

                        if (properties.isSelectionCriteriaFileName()) {
                            if (fileName.lastIndexOf(tempToken) != -1) {
                                isSelected = true;
                            }
                        } else {
                            isSelected = false; // SEQUEST don't have identification ids
                            break;
                        }
                    }
                } else {
                    isSelected = true;
                }
            }

            if (isSelected) {

                precursorMh = -1.0;
                precursorMz = -1.0;
                precursorIntensty = -1.0;
                precursorCharge = -1;

                try {
                    f = new FileReader(new File(properties.getSelectedSourceFiles().get(j)));
                    b = new BufferedReader(f);

                    currentLine = b.readLine();

                    if (properties.isCommaTheDecimalSymbol()) {
                        currentLine = currentLine.replaceAll(",", ".");
                    }

                    tok = new StringTokenizer(currentLine);

                    if (properties.getDataSource().equalsIgnoreCase("Spectrum Mill")) {
                        precursorMz = Double.parseDouble(tok.nextToken());
                    }

                    if (properties.getDataSource().equalsIgnoreCase("SEQUEST DTA File") ||
                            properties.getDataSource().equalsIgnoreCase("SEQUEST Result File")) {
                        precursorMh = Double.parseDouble(tok.nextToken());
                    }

                    precursorIntensty = -1;

                    //intensity not included in pkl files
                    if (properties.getDataSource().equalsIgnoreCase("Spectrum Mill")) {
                        precursorIntensty = Double.parseDouble(tok.nextToken());
                    }

                    precursorCharge = Integer.parseInt(tok.nextToken());

                    currentLine = b.readLine();

                    masses = new Vector<Double>();
                    intensities = new Vector<Double>();

                    emptyLineFound = false;

                    while (currentLine != null && !emptyLineFound) {

                        if (currentLine.equalsIgnoreCase("")) {
                            emptyLineFound = true;
                        //System.out.println("Empty line found!!");
                        } else {

                            if (properties.isCommaTheDecimalSymbol()) {
                                currentLine = currentLine.replaceAll(",", ".");
                            }

                            tok = new StringTokenizer(currentLine);
                            masses.add(new Double(tok.nextToken()));
                            intensities.add(new Double(tok.nextToken()));

                            currentLine = b.readLine();
                        }
                    }

                    arrays = new double[2][masses.size()];

                    for (int i = 0; i < masses.size(); i++) {
                        arrays[0][i] = masses.get(i);
                        arrays[1][i] = intensities.get(i);
                    }

                    b.close();
                    f.close();
                } catch (Exception e) {

                    errorDetected = true;
                    String fileType = "";

                    if (properties.getDataSource().equalsIgnoreCase("SEQUEST Result File") ||
                            properties.getDataSource().equalsIgnoreCase("SEQUEST DTA File")) {
                        fileType = "SEQUEST DTA file";
                    } else if (properties.getDataSource().equalsIgnoreCase("Spectrum Mill")) {
                        fileType = "Micromass PKL file";
                    }

                    if (!PRIDEConverter.isConversionCanceled()) {

                        Util.writeToErrorLog("Error parsing " + fileType + ": ");
                        e.printStackTrace();

                        JOptionPane.showMessageDialog(null,
                                "The following file could not parsed as a " +
                                fileType + ":\n " +
                                new File(properties.getSelectedSourceFiles().get(j)).getName() +
                                "\n\n" +
                                "See ../Properties/ErrorLog.txt for more details.",
                                "Error Parsing File", JOptionPane.ERROR_MESSAGE);
                    }
                }

                if (!errorDetected) {

                    identificationFile = null;

                    try {
                        // Spectrum description comments.
                        spectrumDescriptionComments = new ArrayList<SpectrumDescComment>();

                        identified = "Not identified";

                        matchFound = false;

                        //have to check if it's identified or not
                        if (properties.getDataSource().equalsIgnoreCase("Spectrum Mill")) {

                            identificationFile = new File("");

                            for (int i = 0; i < properties.getSelectedIdentificationFiles().size() &&
                                    !matchFound && !PRIDEConverter.isConversionCanceled(); i++) {

                                tempFileName = properties.getSelectedIdentificationFiles().get(i);

                                tempFileName = tempFileName.substring(
                                        tempFileName.lastIndexOf(File.separator) + 1);

                                matchFound = tempFileName.equalsIgnoreCase(PRIDEConverter.getCurrentFileName() + ".spo");

                                if (matchFound) {
                                    identificationFile = new File(properties.getSelectedIdentificationFiles().get(i));
                                }
                            }

                            if (matchFound) {
                                identified = "Identified";

                                accession = null;
                                database = null;
                                sequence = null;
                                start = null;
                                score = null;
                                prospectorVersion = null;
                                deltaCn = null;
                                sp = null;
                                ions = null;
                                upstreamFlankingSequence = null;
                                downstreamFlankingSequence = null;
                                rankSp = null;
                                mH = null;

                                peptideModifications = new ArrayList<ModificationImpl>();
                                fragmentIons = new ArrayList<FragmentIon>();

                                f = new FileReader(identificationFile);
                                b = new BufferedReader(f);

                                currentLine = b.readLine();

                                String massTolerance; // to be used for the fragment ion annotation to calculate the ions
                                while (currentLine != null) {

                                    // first we try to extract some of the information from the header part of the file
                                    if (currentLine.startsWith("database") && !currentLine.startsWith("database_date")) {
                                        tok = new StringTokenizer(currentLine);
                                        tok.nextToken();
                                        database = tok.nextToken();
                                    } else if (currentLine.startsWith("prospector_version")) {
                                        tok = new StringTokenizer(currentLine);
                                        tok.nextToken();
                                        prospectorVersion = tok.nextToken(); // ToDo: never used !?
                                    } else if (currentLine.startsWith("fragment_mass_tolerance")) {
                                        tok = new StringTokenizer(currentLine);
                                        tok.nextToken();
                                        massTolerance = tok.nextToken();
                                    } else if (currentLine.startsWith("num_hits_to_report")) {

                                        // parse the identifications

                                        tok = new StringTokenizer(currentLine);

                                        tok.nextToken();
                                        numberOfHits = Integer.parseInt(tok.nextToken());

                                        if (numberOfHits > 0) {

                                            currentLine = b.readLine();
                                            tok = new StringTokenizer(currentLine, "\t");

                                            index = 0;

                                            // contains all the column headers (score, sequence, etc)
                                            // which makes is easy to extract the value of a given column
                                            // later on
                                            spectrumMillColumnHeaders = new HashMap<String, Integer>();

                                            // parse the column header titles
                                            while (tok.hasMoreTokens()) {
                                                spectrumMillColumnHeaders.put(tok.nextToken(), index++);
                                            }

                                            modificationName = null;
                                            modificationLocation = null;
                                            modificationNameShort = null;

                                            modificationCVParams = new ArrayList<CvParam>();


                                            // ToDo: where does it read the next lines if numberOfHits > 1 ??
                                            // ToDo: we only take the fist on into account? If so, this should be documented!
                                            currentLine = b.readLine();
                                            tok = new StringTokenizer(currentLine, "\t");

                                            spectrumMillValues = new Vector<String>();

                                            while (tok.hasMoreTokens()) {
                                                spectrumMillValues.add(tok.nextToken());
                                            }

                                            score = new Double(spectrumMillValues.get(
                                                    spectrumMillColumnHeaders.get("score").intValue()));

                                            upstreamFlankingSequence = spectrumMillValues.get(
                                                    spectrumMillColumnHeaders.get("previous_aa").intValue());

                                            sequence = spectrumMillValues.get(
                                                    spectrumMillColumnHeaders.get("sequence").intValue());

                                            downstreamFlankingSequence = spectrumMillValues.get(
                                                    spectrumMillColumnHeaders.get("next_aa").intValue());

                                            fragmentIonMap = spectrumMillValues.get(
                                                    spectrumMillColumnHeaders.get("fragmentIonMap"));

                                            fragmentIons = new ArrayList<FragmentIon>();

                                            // BEGIN Fragment Ion extraction
                                            // @TODO: Extract fragmention ion annotations (Spectrum Mill).

                                            // 1 - use the fragmentIonMap to get the fragment ions
                                            //     - generate the ions from the fragementIonMap,
                                            //     - calculate the masses and charges
                                            //       - use arrays variable for peak masses and intensities
                                            //       - parse 'fragment_mass_tolerance' term from input file for mass delta
                                            //       - parse 'it a b b_nh3 b_h2o b_plus_h2o y y_nh3 y_h2o' term from input file for ion type definition
                                            // 2 - add them to the fragmentIons list (see Mascot Dat File for how to do this)
                                            // 3 - add new mappings to the FragmentIonsMapping.prop file
                                            // 4 - that's it, the rest is taken care of :)

                                            /* fragment ion annotation stuff (needs to be finished)
                                            if (fragmentIonMap.length() != sequence.length()-1) {
                                                throw new IllegalStateException("Extracted fragment ion map does not have the required length for peptide: " + sequence);
                                            }

                                            String peptide = sequence.toUpperCase();
                                            String fragmentMap = fragmentIonMap.toUpperCase();
                                            System.out.println("peptide: " + peptide);
                                            System.out.println("mapping: " + fragmentMap);

                                            ArrayList<String> yIons = new ArrayList<String>();
                                            ArrayList<String> bIons = new ArrayList<String>();
                                            int fillPepetideSequenceLength = peptide.length();
                                            // N terminus  -> C terminus
                                            // 0 - no matched ions at the position
                                            // S - (pink vertical bar) both N and C terminal type ions
                                            // N - (blue forward slash) only N-terminal type ions
                                            // C - (red back slash) only C-terminal type ions
                                            for (int i = 0; i < fragmentMap.length(); i++) {
                                                char c = fragmentMap.charAt(i);
                                                if (c == 'C') {
                                                    // only C-terminal type ions
                                                    yIons.add( peptide.substring(i+1, peptide.length()) );
                                                } else if (c == 'S') {
                                                    // both N and C terminal type ions
                                                    yIons.add( peptide.substring(i+1, peptide.length()) );
                                                    bIons.add( peptide.substring(0, i+1) );
                                                } else if (c == 'N') {
                                                    // only N-terminal type ions
                                                    bIons.add( peptide.substring(0, i+1) );
                                                } else if (c == '0') {
                                                    // nothing to do, no ions at this position
                                                } else {
                                                    throw new IllegalStateException("Not supported fragment ion map code: " + c);
                                                }

                                            }
                                            */
                                            // we need the ion masses to find the correxponding peak in the spectrum 
                                            // before we can calculate the correct masses for the ions,
                                            // we need to have the modifications...
                                            // this is done further down in this method.
                                            // END Fragemnt Ion extraction


                                            coverage_map = spectrumMillValues.get(
                                                    spectrumMillColumnHeaders.get("coverage_map"));

                                            startTok = new StringTokenizer(coverage_map, "+");
                                            startTok.nextToken();

                                            start = new Integer(startTok.nextToken()) + 1;

                                            // parse the variable modifications, if any
                                            if (spectrumMillColumnHeaders.containsKey("varMods")) {

                                                modificationName = spectrumMillValues.get(
                                                        spectrumMillColumnHeaders.get("varMods").intValue());

                                                if (!modificationName.equalsIgnoreCase(" ")) {

                                                    modTok = new StringTokenizer(modificationName);

                                                    while (modTok.hasMoreTokens() && !PRIDEConverter.isConversionCanceled()) {

                                                        currentMod = modTok.nextToken();

                                                        modificationNameShort = currentMod.substring(0, currentMod.indexOf(":"));
                                                        modificationName = currentMod.substring(currentMod.indexOf(":") + 1);

                                                        currentIndex = 0;

                                                        while (sequence.indexOf(modificationNameShort, currentIndex) !=
                                                                -1 && !PRIDEConverter.isConversionCanceled()) {

                                                            modificationLocation = sequence.indexOf(
                                                                    modificationNameShort, currentIndex) + 1;

                                                            currentIndex = sequence.indexOf(
                                                                    modificationNameShort, currentIndex) + 1;

                                                            if ( !properties.getAlreadyChoosenModifications().contains(modificationName) ) {
                                                                // ToDo: why create a new ModificationMapping??
                                                                new ModificationMapping(PRIDEConverter.getOutputFrame(),
                                                                        true, progressDialog,
                                                                        modificationName,
                                                                        modificationNameShort.toUpperCase(),
                                                                        null,
                                                                        (CvParamImpl) userProperties.getCVTermMappings().
                                                                        get(modificationName), false);

                                                                properties.getAlreadyChoosenModifications().add(modificationName);
                                                            } else {
                                                                // do nothing, mapping already choosen
                                                            }

                                                            CvParamImpl tempCvParam =
                                                                    (CvParamImpl) userProperties.getCVTermMappings().get(modificationName);

                                                            modificationCVParams = new ArrayList<CvParam>();
                                                            modificationCVParams.add(tempCvParam);

                                                            monoMasses = new ArrayList<MassDelta>();

                                                            // get the modification mass (DiffMono) retrieved from PSI-MOD
                                                            if (tempCvParam.getValue() != null) {
                                                                monoMasses.add(new MonoMassDeltaImpl(
                                                                        Double.parseDouble(tempCvParam.getValue())));
                                                            } else {
                                                                monoMasses = null;
                                                            }

                                                            peptideModifications.add(new ModificationImpl(
                                                                    tempCvParam.getAccession(),
                                                                    modificationLocation,
                                                                    tempCvParam.getCVLookup(),
                                                                    null,
                                                                    monoMasses,
                                                                    null,
                                                                    modificationCVParams,
                                                                    null));
                                                        }
                                                    }
                                                }
                                            }

                                            modificationName = null;
                                            modificationLocation = null;

                                            // parse the fixed modifications, if any
                                            if (spectrumMillColumnHeaders.containsKey("fixedMods") && !PRIDEConverter.isConversionCanceled()) {
                                                modificationName = spectrumMillValues.get(
                                                        spectrumMillColumnHeaders.get("fixedMods").intValue());

                                                if (!modificationName.equalsIgnoreCase(" ")) {

                                                    modTok = new StringTokenizer(modificationName);

                                                    while (modTok.hasMoreTokens() && !PRIDEConverter.isConversionCanceled()) {

                                                        currentMod = modTok.nextToken();
                                                        modificationNameShort = currentMod.substring(0, currentMod.indexOf(":"));
                                                        modificationName = currentMod.substring(currentMod.indexOf(":") + 1);

                                                        currentIndex = 0;

                                                        while (sequence.indexOf(modificationNameShort,
                                                                currentIndex) != -1 && !PRIDEConverter.isConversionCanceled()) {

                                                            modificationLocation = sequence.indexOf(modificationNameShort, currentIndex) + 1;
                                                            currentIndex = sequence.indexOf(modificationNameShort, currentIndex) + 1;

                                                            if (!properties.getAlreadyChoosenModifications().contains(modificationName)) {
                                                                new ModificationMapping(PRIDEConverter.getOutputFrame(),
                                                                        true, progressDialog,
                                                                        modificationName,
                                                                        modificationNameShort.toUpperCase(),
                                                                        null,
                                                                        (CvParamImpl) userProperties.getCVTermMappings().get(modificationName),
                                                                        true);

                                                                properties.getAlreadyChoosenModifications().add(modificationName);
                                                            } else {
                                                                //do nothing, mapping already choosen
                                                            }

                                                            CvParamImpl tempCvParam =
                                                                    (CvParamImpl) userProperties.getCVTermMappings().get(modificationName);

                                                            modificationCVParams = new ArrayList<CvParam>();
                                                            modificationCVParams.add(tempCvParam);

                                                            monoMasses = new ArrayList<MassDelta>();

                                                            // get the modification mass (DiffMono) retrieved from PSI-MOD
                                                            if (tempCvParam.getValue() != null) {
                                                                monoMasses.add(new MonoMassDeltaImpl(
                                                                        Double.parseDouble(tempCvParam.getValue())));
                                                            } else {
                                                                monoMasses = null;
                                                            }

                                                            peptideModifications.add(new ModificationImpl(
                                                                    tempCvParam.getAccession(),
                                                                    modificationLocation,
                                                                    tempCvParam.getCVLookup(),
                                                                    null,
                                                                    monoMasses,
                                                                    null,
                                                                    modificationCVParams,
                                                                    null));
                                                        }
                                                    }
                                                }
                                            }

                                            precursorCharge = new Integer(spectrumMillValues.get(
                                                    spectrumMillColumnHeaders.get("parent_charge").intValue()));

                                            accession = spectrumMillValues.get(
                                                    spectrumMillColumnHeaders.get("accession_number").intValue());
                                        }
                                    }

                                    currentLine = b.readLine();
                                }

                                b.close();
                                f.close();

                                //calculate itraq values
                                iTRAQ iTRAQValues = null;

                                if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
                                    iTRAQValues = new iTRAQ(arrays, (precursorMz * precursorCharge),
                                            precursorCharge,
                                            userProperties.getPeakIntegrationRangeLower(),
                                            userProperties.getPeakIntegrationRangeUpper(),
                                            userProperties.getReporterIonIntensityThreshold(),
                                            userProperties.getPurityCorrections());
                                    iTRAQValues.calculateiTRAQValues();
                                }

                                String[] iTraqNorm = null;
                                String[] iTraqUT = null;
                                String[][] iTraqRatio = null;

                                if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
                                    //iTraq-stuff
                                    iTraqNorm = (String[]) iTRAQValues.getAllNorms().get(0);
                                    iTraqUT = (String[]) iTRAQValues.getAllUTs().get(0);
                                    iTraqRatio = (String[][]) iTRAQValues.getAllRatios().get(0);
                                }

                                cVParams = new ArrayList<CvParam>();

                                cVParams.add(new CvParamImpl("PRIDE:0000177",
                                        "PRIDE", "Spectrum Mill Peptide Score",
                                        cVParams.size(), "" + score));

                                if (upstreamFlankingSequence != null) {

                                    upstreamFlankingSequence = upstreamFlankingSequence.trim().substring(1, 2);

                                    if (!upstreamFlankingSequence.equalsIgnoreCase("-")) {
                                        cVParams.add(new CvParamImpl("PRIDE:0000065",
                                                "PRIDE",
                                                "Upstream flanking sequence",
                                                cVParams.size(),
                                                "" +
                                                upstreamFlankingSequence.toUpperCase()));
                                    }
                                }

                                if (downstreamFlankingSequence != null) {

                                    downstreamFlankingSequence = downstreamFlankingSequence.trim().substring(1, 2);

                                    if (!downstreamFlankingSequence.equalsIgnoreCase("-")) {
                                        cVParams.add(new CvParamImpl("PRIDE:0000066",
                                                "PRIDE",
                                                "Downstream flanking sequence",
                                                cVParams.size(),
                                                "" +
                                                downstreamFlankingSequence.toUpperCase()));
                                    }
                                }

                                if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
                                    PRIDEConverter.addItraqCVTerms(cVParams, iTraqNorm);
                                    userParams = PRIDEConverter.addItraqUserTerms(iTraqRatio);
                                } else {
                                    userParams = null;
                                }

                                if (score >= properties.getPeptideScoreThreshold()) {

                                    PRIDEConverter.getIds().add(new IdentificationGeneral(
                                            PRIDEConverter.getCurrentFileName(),//spectrumFileID
                                            accession, //accession
                                            "Spectrum Mill",//search engine
                                            database, //database
                                            null, //databaseversion
                                            sequence.toUpperCase(), //sequence
                                            start, //start
                                            score, //score
                                            null, //threshold
                                            iTraqNorm, iTraqUT, iTraqRatio, // iTRAQ values
                                            cVParams, // cv params
                                            userParams, // user params
                                            peptideModifications,
                                            fragmentIons)); // list of fragment ions
                                }
                            }
                        } else if (properties.getDataSource().equalsIgnoreCase("SEQUEST Result File")) {

                            identificationFile = new File("");

                            for (int i = 0; i < properties.getSelectedIdentificationFiles().size() && !matchFound
                                    && !PRIDEConverter.isConversionCanceled(); i++) {

                                tempFileName = properties.getSelectedIdentificationFiles().get(i);

                                tempFileName = tempFileName.substring(
                                        tempFileName.lastIndexOf(File.separator) + 1,
                                        tempFileName.length() - 4) + ".dta";

                                matchFound = tempFileName.equalsIgnoreCase(PRIDEConverter.getCurrentFileName());

                                if (matchFound) {
                                    identificationFile = new File(properties.getSelectedIdentificationFiles().get(i));
                                }
                            }

                            if (matchFound) {
                                identified = "Identified";

                                accession = null;
                                database = null;
                                sequence = null;
                                start = null;
                                score = null;
                                prospectorVersion = null;
                                sp = null;
                                ions = null;
                                upstreamFlankingSequence = null;
                                downstreamFlankingSequence = null;
                                rankSp = null;
                                mH = null;
                                peptideModifications = null;
                                fragmentIons = null; // Sequence does not include this information

                                f = new FileReader(identificationFile);
                                b = new BufferedReader(f);

                                currentLine = b.readLine();

                                allLines = new Vector<String>();
                                allLines.add(currentLine);

                                String tempString;

                                hitFound = false;
                                boolean outFileIsEmpty = false;

                                while (currentLine != null && !hitFound) {

                                    allLines.add(currentLine);

                                    // extract the name of the database
                                    if (currentLine.lastIndexOf("# amino acids") != -1) {

                                        if (currentLine.lastIndexOf("\\") != -1) {
                                            database = currentLine.substring(currentLine.lastIndexOf("\\") + 1);
                                        } else if (currentLine.lastIndexOf("/") != -1) {
                                            database = currentLine.substring(currentLine.lastIndexOf("/") + 1);
                                        } else if (currentLine.lastIndexOf(" ") != -1) {
                                            database = currentLine.substring(currentLine.lastIndexOf(" ") + 1);
                                        } else {
                                            database = null;
                                        }
                                    }

                                    tempString = currentLine.trim();
                                    tempString = tempString.replaceAll(" ", "");

                                    if (tempString.startsWith("#Rank/Sp")) {

                                        String columnHeader = tempString;

                                        modificationLine = allLines.get(allLines.size() - 3);

                                        //variable modifications
                                        pattern = Pattern.compile(variableModificationPattern);
                                        matcher = pattern.matcher(modificationLine);

                                        while (matcher.find() && !PRIDEConverter.isConversionCanceled()) {

                                            currentModification = matcher.group().substring(1, matcher.group().length() - 1);

                                            modificationName = currentModification.substring(0, currentModification.indexOf(" "));
                                            modificationNameShort = modificationName.substring(0, 1);
                                            modificationMass = new Double(currentModification.substring(
                                                    currentModification.indexOf(" ") + 1));

                                            if (!properties.getAlreadyChoosenModifications().contains(modificationName)) {
                                                new ModificationMapping(PRIDEConverter.getOutputFrame(),
                                                        true, progressDialog, modificationName,
                                                        modificationNameShort,
                                                        modificationMass,
                                                        (CvParamImpl) userProperties.getCVTermMappings().get(modificationName),
                                                        false);

                                                properties.getAlreadyChoosenModifications().add(modificationName);
                                            } else {
                                                //do nothing, mapping already choosen
                                            }
                                        }

                                        //fixed modifications
                                        pattern = Pattern.compile(fixedModificationPattern);
                                        matcher = pattern.matcher(modificationLine);

                                        while (matcher.find() && !PRIDEConverter.isConversionCanceled()) {

                                            currentModification = matcher.group();

                                            modificationName = currentModification.substring(0, currentModification.indexOf("="));
                                            modificationNameShort = modificationName.substring(0, 1);
                                            modificationMass = new Double(currentModification.substring(
                                                    currentModification.indexOf("=") + 1));

                                            if (!properties.getAlreadyChoosenModifications().contains(modificationName)) {
                                                new ModificationMapping(PRIDEConverter.getOutputFrame(),
                                                        true, progressDialog, modificationName,
                                                        modificationNameShort,
                                                        modificationMass,
                                                        (CvParamImpl) userProperties.getCVTermMappings().get(modificationName),
                                                        true);

                                                properties.getAlreadyChoosenModifications().add(modificationName);
                                            } else {
                                                //do nothing, mapping already choosen
                                            }
                                        }

                                        currentLine = b.readLine();
                                        currentLine = b.readLine();

                                        // handle protein isoforms
                                        // not currently used and not finished.
                                        // see Mascot Dat file for example.
//                                        String nextLine = b.readLine();
//
//                                        // check for protein isoforms
//                                        while(!nextLine.startsWith("  2.")){
//
//                                            tok = new StringTokenizer(nextLine);
//                                            tok.nextToken();
//                                            String isoformAccession = tok.nextToken();
//
//                                            nextLine = b.readLine();
//                                        }


                                        // check if the file contains any identifications at all
                                        if (currentLine.length() == 0) {

                                            outFileIsEmpty = true;

                                        } else {

                                            tok = new StringTokenizer(currentLine);
                                            tok.nextToken(); // # column

                                            rankSp = tok.nextToken() + tok.nextToken();

                                            if (rankSp.endsWith("/")) {
                                                rankSp += tok.nextToken();
                                            }

                                            // the old version of sequest files does
                                            // not contain the Id# column
                                            if (columnHeader.lastIndexOf("Id#") != -1) {
                                                tok.nextToken(); //Id#
                                            }

                                            mH = Double.parseDouble(tok.nextToken()); //(M+H)+
                                            deltaCn = Double.parseDouble(tok.nextToken()); //deltCn
                                            score = Double.parseDouble(tok.nextToken()); //XCorr
                                            sp = Double.parseDouble(tok.nextToken()); //Sp
                                            ions = tok.nextToken(); //Ions

                                            if (ions.endsWith("/")) {
                                                ions += tok.nextToken();
                                            }

                                            accession = tok.nextToken(); //Accession number
                                            sequence = tok.nextToken(); //Peptide

                                            //skip the protein isoform counter
                                            String isoformsPattern = "\\+[\\d]";

                                            pattern = Pattern.compile(isoformsPattern);
                                            matcher = pattern.matcher(sequence);

                                            if (matcher.find()) {
                                                sequence = tok.nextToken(); //Peptide
                                            }

                                            if (sequence.indexOf(".") == 1) {
                                                upstreamFlankingSequence = sequence.substring(0, 1).toUpperCase();
                                            }

                                            if (sequence.lastIndexOf(".") == sequence.length() - 2) {
                                                downstreamFlankingSequence = sequence.substring(sequence.length() -
                                                        1, sequence.length()).toUpperCase();
                                            }

                                            sequence = sequence.substring(2, sequence.length() - 2);
                                            sequenceArray = new String[sequence.length()];

                                            index = 0;

                                            for (int i = 0; i < sequence.length() && !PRIDEConverter.isConversionCanceled(); i++) {
                                                if (i == sequence.length() - 1) {
                                                    sequenceArray[index++] = sequence.substring(sequence.length() -
                                                            1, sequence.length());
                                                } else {
                                                    if (properties.getAlreadyChoosenModifications().contains(sequence.substring(i, i + 2))) {
                                                        sequenceArray[index++] = sequence.substring(i, i + 2);
                                                        i++;

                                                    } else {
                                                        sequenceArray[index++] = sequence.substring(i, i + 1);
                                                    }
                                                }
                                            }

                                            sequence = "";
                                            peptideModifications = null;

                                            for (int i = 0; i < sequenceArray.length && !PRIDEConverter.isConversionCanceled(); i++) {

                                                if (sequenceArray[i] != null) {
                                                    if (sequenceArray[i].length() == 2) {
                                                        if (properties.getAlreadyChoosenModifications().contains(sequenceArray[i])) {

                                                            if (peptideModifications == null) {
                                                                peptideModifications = new ArrayList<ModificationImpl>();
                                                            }

                                                            CvParamImpl tempCvParam =
                                                                    (CvParamImpl) userProperties.getCVTermMappings().get(sequenceArray[i]);

                                                            modificationCVParams = new ArrayList<CvParam>();
                                                            modificationCVParams.add(tempCvParam);

                                                            monoMasses = new ArrayList<MassDelta>();

                                                            // get the modification mass (DiffMono) retrieved from PSI-MOD
                                                            if (tempCvParam.getValue() != null) {
                                                                monoMasses.add(new MonoMassDeltaImpl(
                                                                        Double.parseDouble(tempCvParam.getValue())));
                                                            } else {
                                                                // it would be possible to use the mass from the data file
                                                                // but this is currently not used, as this could be incorrect
                                                                // relative to properties of the PSI-MOD modification
//                                                    monoMasses.add(new MonoMassDeltaImpl(((Double) modifications.get(
//                                                            sequenceArray[i])).doubleValue()));
                                                                monoMasses = null;
                                                            }

                                                            peptideModifications.add(new ModificationImpl(
                                                                    tempCvParam.getAccession(),
                                                                    i + 1,
                                                                    tempCvParam.getCVLookup(),
                                                                    null,
                                                                    monoMasses,
                                                                    null,
                                                                    modificationCVParams,
                                                                    null));
                                                        }
                                                    }
                                                }

                                                // check if the current amino acid contains a fixed modification
                                                if (sequenceArray[i] != null) {
                                                    if (properties.getAlreadyChoosenModifications().contains(sequenceArray[i].substring(0, 1))) {

                                                        if (peptideModifications == null) {
                                                            peptideModifications = new ArrayList<ModificationImpl>();
                                                        }

                                                        CvParamImpl tempCvParam =
                                                                (CvParamImpl) userProperties.getCVTermMappings().get(sequenceArray[i].substring(0, 1));

                                                        modificationCVParams = new ArrayList<CvParam>();
                                                        modificationCVParams.add(tempCvParam);

                                                        monoMasses = new ArrayList<MassDelta>();

                                                        // get the modification mass (DiffMono) retrieved from PSI-MOD
                                                        if (tempCvParam.getValue() != null) {
                                                            monoMasses.add(new MonoMassDeltaImpl(
                                                                    Double.parseDouble(tempCvParam.getValue())));
                                                        } else {
                                                            // it would be possible to use the mass from the data file
                                                            // but this is currently not used, as this could be incorrect
                                                            // relative to properties of the PSI-MOD modification
//                                                    monoMasses.add(new MonoMassDeltaImpl(((Double) modifications.get(
//                                                            sequenceArray[i])).doubleValue()));
                                                            monoMasses = null;
                                                        }

                                                        peptideModifications.add(new ModificationImpl(
                                                                tempCvParam.getAccession(),
                                                                i + 1,
                                                                tempCvParam.getCVLookup(),
                                                                null,
                                                                monoMasses,
                                                                null,
                                                                modificationCVParams,
                                                                null));
                                                    }
                                                }

                                                if (sequenceArray[i] != null) {
                                                    sequence += sequenceArray[i].substring(0, 1);
                                                }
                                            }

                                            sequence = sequence.toUpperCase();
                                            hitFound = true;
                                        }
                                    }

                                    currentLine = b.readLine();
                                }

                                b.close();
                                f.close();

                                if (!outFileIsEmpty) {

                                    //calculate itraq values
                                    iTRAQ iTRAQValues = null;

                                    if (properties.getSampleDescriptionCVParamsQuantification().
                                            size() > 0 && !PRIDEConverter.isConversionCanceled()) {
                                        iTRAQValues =
                                                new iTRAQ(arrays, ((precursorMh - properties.HYDROGEN_MASS + precursorCharge * properties.HYDROGEN_MASS) / precursorCharge),
                                                precursorCharge,
                                                userProperties.getPeakIntegrationRangeLower(),
                                                userProperties.getPeakIntegrationRangeUpper(),
                                                userProperties.getReporterIonIntensityThreshold(),
                                                userProperties.getPurityCorrections());
                                        iTRAQValues.calculateiTRAQValues();
                                    }

                                    String[] iTraqNorm = null;
                                    String[] iTraqUT = null;
                                    String[][] iTraqRatio = null;

                                    if (properties.getSampleDescriptionCVParamsQuantification().size() > 0 && !PRIDEConverter.isConversionCanceled()) {
                                        iTraqNorm = (String[]) iTRAQValues.getAllNorms().get(0);
                                        iTraqUT = (String[]) iTRAQValues.getAllUTs().get(0);
                                        iTraqRatio = (String[][]) iTRAQValues.getAllRatios().get(0);
                                    }

                                    cVParams = new ArrayList<CvParam>();
                                    cVParams.add(new CvParamImpl("PRIDE:0000053",
                                            "PRIDE", "SEQUEST Score", cVParams.size(), "" + score));

                                    if (deltaCn != null) {
                                        cVParams.add(new CvParamImpl("PRIDE:0000012",
                                                "PRIDE", "Delta Cn", cVParams.size(), "" + deltaCn));
                                    }

                                    if (sp != null) {
                                        cVParams.add(new CvParamImpl("PRIDE:0000054",
                                                "PRIDE", "Sp", cVParams.size(), "" + sp));
                                    }

                                    if (ions != null) {
                                        cVParams.add(new CvParamImpl("PRIDE:0000055",
                                                "PRIDE", "Ions", cVParams.size(), "" + ions));
                                    }

                                    if (upstreamFlankingSequence != null) {
                                        if (!upstreamFlankingSequence.equalsIgnoreCase("-")) {
                                            cVParams.add(new CvParamImpl("PRIDE:0000065",
                                                    "PRIDE", "Upstream flanking sequence",
                                                    cVParams.size(), "" + upstreamFlankingSequence.toUpperCase()));
                                        }
                                    }

                                    if (downstreamFlankingSequence != null) {
                                        if (!downstreamFlankingSequence.equalsIgnoreCase("-")) {
                                            cVParams.add(new CvParamImpl("PRIDE:0000066",
                                                    "PRIDE", "Downstream flanking sequence",
                                                    cVParams.size(), "" + downstreamFlankingSequence.toUpperCase()));
                                        }
                                    }

                                    if (rankSp != null) {
                                        cVParams.add(new CvParamImpl("PRIDE:0000050",
                                                "PRIDE", "Rank/Sp", cVParams.size(), "" + rankSp));
                                    }

                                    if (mH != null) {
                                        cVParams.add(new CvParamImpl("PRIDE:0000051",
                                                "PRIDE", "(M+H)+", cVParams.size(), "" + mH));
                                    }

                                    if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
                                        PRIDEConverter.addItraqCVTerms(cVParams, iTraqNorm);
                                        userParams = PRIDEConverter.addItraqUserTerms(iTraqRatio);
                                    } else {
                                        userParams = null;
                                    }

                                    if (score >= properties.getPeptideScoreThreshold()) {

                                        PRIDEConverter.getIds().add(new IdentificationGeneral(
                                                PRIDEConverter.getCurrentFileName(), // spectrumFileID
                                                accession, // accession
                                                "SEQUEST", // search engine
                                                database, // database
                                                null, // databaseversion
                                                sequence.toUpperCase(), // sequence
                                                null, // start
                                                score, // score
                                                null, // threshold
                                                iTraqNorm, iTraqUT, iTraqRatio, // iTRAQ values
                                                cVParams, // cv params
                                                userParams, // user params
                                                peptideModifications,
                                                fragmentIons)); // list of fragment ions
                                    }
                                }
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

                        //intensity not given in SEQUEST DTA File
                        if (precursorIntensty != -1) {
                            ionSelection.add(new CvParamImpl("PSI:1000042",
                                    "PSI", "Intensity", ionSelection.size(),
                                    Double.toString(precursorIntensty)));
                        }

                        if (properties.getDataSource().equalsIgnoreCase("SEQUEST DTA File") ||
                                properties.getDataSource().equalsIgnoreCase("SEQUEST Result File")) {

                            // calculated precursor m/z
                            ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                                    "MassToChargeRatio", ionSelection.size(), Double.toString(
                                    ((precursorMh - properties.HYDROGEN_MASS + precursorCharge * properties.HYDROGEN_MASS) / precursorCharge))));

                            // precursor MH+
                            ionSelection.add(new CvParamImpl("PRIDE:0000051",
                                    "PRIDE", "(M+H)+", ionSelection.size(), Double.toString(precursorMh)));
                        }

                        if (properties.getDataSource().equalsIgnoreCase("Spectrum Mill")) {
                            ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                                    "MassToChargeRatio", ionSelection.size(), Double.toString(precursorMz)));
                        }

                        precursors.add(new PrecursorImpl(null, null,
                                ionSelection, null, msLevel - 1, 0, 0));

                        spectrumDescriptionComments.add(new SpectrumDescCommentImpl(identified));

                        spectrumDescriptionComments = PRIDEConverter.addUserSpectrumComments(spectrumDescriptionComments,
                                properties.getSpectrumCvParams().get(PRIDEConverter.getSpectrumKey()),
                                properties.getSpectrumUserParams().get(PRIDEConverter.getSpectrumKey()) );

                        if (arrays[properties.MZ_ARRAY].length > 0) {
                            mzRangeStart = arrays[properties.MZ_ARRAY][0];
                            mzRangeStop = arrays[properties.MZ_ARRAY][arrays[properties.MZ_ARRAY].length - 1];

                            // Create new mzData spectrum for the fragmentation spectrum.
                            fragmentation = new SpectrumImpl(
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

                            if (properties.getDataSource().equalsIgnoreCase("Spectrum Mill") ||
                                    properties.getDataSource().equalsIgnoreCase("SEQUEST Result File")) {

                                if (properties.selectAllSpectra()) {
                                    // Store (spectrumfileid, spectrumid) mapping.
                                    Long xTmp = mapping.put(PRIDEConverter.getCurrentFileName(), (long) totalSpectraCounter);

                                    if (xTmp != null) {
                                        // we already stored a result for this ID!!!
                                        JOptionPane.showMessageDialog(null, "Ambiguous spectrum mapping. Please consult " +
                                                "the error log file for details.", "Mapping Error", JOptionPane.ERROR_MESSAGE);
                                        Util.writeToErrorLog("Ambiguous spectrum mapping for spectrum file '"
                                                + PRIDEConverter.getCurrentFileName() + "'." );
                                        PRIDEConverter.setCancelConversion(true);
                                    }

                                    // Store the transformed spectrum.
                                    aTransformedSpectra.add(fragmentation);
                                } else if (properties.selectAllIdentifiedSpectra() &&
                                        identified.equalsIgnoreCase("Identified")) {
                                    // Store (spectrumfileid, spectrumid) mapping.
                                    Long xTmp = mapping.put(PRIDEConverter.getCurrentFileName(), (long) totalSpectraCounter);

                                    if (xTmp != null) {
                                        // we already stored a result for this ID!!!
                                        JOptionPane.showMessageDialog(null, "Ambiguous spectrum mapping. Please consult " +
                                                "the error log file for details.", "Mapping Error", JOptionPane.ERROR_MESSAGE);
                                        Util.writeToErrorLog("Ambiguous spectrum mapping for spectrum file '"
                                                + PRIDEConverter.getCurrentFileName() + "'." );
                                        PRIDEConverter.setCancelConversion(true);
                                    }

                                    // Store the transformed spectrum.
                                    aTransformedSpectra.add(fragmentation);
                                } else {

                                    boolean spectraSelected = false;

                                    for (int k = 0; k < properties.getSelectedSpectraKeys().size() &&
                                            !spectraSelected && !PRIDEConverter.isConversionCanceled(); k++) {

                                        Object[] temp = (Object[]) properties.getSelectedSpectraKeys().get(k);

                                        if (((String) temp[0]).equalsIgnoreCase( PRIDEConverter.getCurrentFileName() )) {
                                            spectraSelected = true;
                                        }
                                    }

                                    if (spectraSelected) {

                                        // Store (spectrumfileid, spectrumid) mapping.
                                        Long xTmp = mapping.put(PRIDEConverter.getCurrentFileName(), (long) totalSpectraCounter);

                                        if (xTmp != null) {
                                            // we already stored a result for this ID!!!
                                            JOptionPane.showMessageDialog(null, "Ambiguous spectrum mapping. Please consult " +
                                                    "the error log file for details.", "Mapping Error", JOptionPane.ERROR_MESSAGE);
                                            Util.writeToErrorLog("Ambiguous spectrum mapping for spectrum file '"
                                                    + PRIDEConverter.getCurrentFileName() + "'." );
                                            PRIDEConverter.setCancelConversion(true);
                                        }

                                        // Store the transformed spectrum.
                                        aTransformedSpectra.add(fragmentation);
                                    }
                                }
                            } else {
                                // Store (spectrumfileid, spectrumid) mapping.
                                Long xTmp = mapping.put(PRIDEConverter.getCurrentFileName(), (long) totalSpectraCounter);

                                if (xTmp != null) {
                                    // we already stored a result for this ID!!!
                                    JOptionPane.showMessageDialog(null, "Ambiguous spectrum mapping. Please consult " +
                                            "the error log file for details.", "Mapping Error", JOptionPane.ERROR_MESSAGE);
                                    Util.writeToErrorLog("Ambiguous spectrum mapping for spectrum file '"
                                            + PRIDEConverter.getCurrentFileName() + "'." );
                                    PRIDEConverter.setCancelConversion(true);
                                }

                                // Store the transformed spectrum.
                                aTransformedSpectra.add(fragmentation);
                            }
                        }
                    } catch (FileNotFoundException ex) {
                        JOptionPane.showMessageDialog(null, "The file named " +
                                properties.getSelectedSourceFiles().get(j) +
                                "\ncould not be found.",
                                "File Not Found", JOptionPane.ERROR_MESSAGE);
                        Util.writeToErrorLog("File not found: ");
                        ex.printStackTrace();
                    } catch (Exception e) {
                        String fileType = "";

                        if (properties.getDataSource().equalsIgnoreCase("SEQUEST Result File")) {
                            fileType = "SEQUEST *.out file";
                        } else if (properties.getDataSource().equalsIgnoreCase("Spectrum Mill")) {
                            fileType = "Spectrum Mill *.pkl.spo file";
                        }

                        if (!PRIDEConverter.isConversionCanceled()) {

                            Util.writeToErrorLog("Error parsing " + fileType + ": ");
                            e.printStackTrace();

                            JOptionPane.showMessageDialog(null,
                                    "The following file could not parsed as a " +
                                    fileType + ":\n " +
                                    identificationFile.getPath() +
                                    "\n\n" +
                                    "See ../Properties/ErrorLog.txt for more details.",
                                    "Error Parsing File",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        }

        PRIDEConverter.setTotalNumberOfSpectra(totalSpectraCounter);

        return mapping;
    }


    private double calculateMonoIsotopicPeptideMass(String peptide) {
        double mass = 0D;
        for (char c : peptide.toCharArray()) {
            mass += aaMasses.get(c);
        }
        // use map of single AA masses to sum up the mass of the peptide
        // only theoretical masses of unmodified peptides are used at this stage
        return mass;
    }

    private static final HashMap<Character, Double> aaMasses = new HashMap<Character, Double>();
    static {
        aaMasses.put('A', 71.037114D);
        aaMasses.put('R', 156.101111D);
        aaMasses.put('N', 114.042927D);
        aaMasses.put('D', 115.026943D);
        aaMasses.put('C', 103.009185D);
        aaMasses.put('E', 129.042593D);
        aaMasses.put('Q', 128.058578D);
        aaMasses.put('G', 57.021464D);
        aaMasses.put('H', 137.058912D);
        aaMasses.put('I', 113.084064D);
        aaMasses.put('L', 113.084064D);
        aaMasses.put('K', 128.094963D);
        aaMasses.put('M', 131.040485D);
        aaMasses.put('F', 147.068414D);
        aaMasses.put('P', 97.052764D);
        aaMasses.put('S', 87.032028D);
        aaMasses.put('T', 101.047679D);
        aaMasses.put('U', 150.95363D);
        aaMasses.put('W', 186.079313D);
        aaMasses.put('Y', 163.06332D);
        aaMasses.put('V', 99.068414D);
    }


}
