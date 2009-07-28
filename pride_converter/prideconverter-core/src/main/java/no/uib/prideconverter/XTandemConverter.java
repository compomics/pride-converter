package no.uib.prideconverter;

import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.systemsbiology.jrap.MSXMLParser;
import org.systemsbiology.jrap.Scan;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.swing.*;
import java.util.*;
import java.io.*;

import no.uib.prideconverter.util.IdentificationGeneral;
import no.uib.prideconverter.util.Util;
import no.uib.prideconverter.util.iTRAQ;
import no.uib.prideconverter.gui.ModificationMapping;
import uk.ac.ebi.pride.model.interfaces.core.FragmentIon;
import uk.ac.ebi.pride.model.interfaces.core.MassDelta;
import uk.ac.ebi.pride.model.interfaces.mzdata.*;
import uk.ac.ebi.pride.model.implementation.mzData.CvParamImpl;
import uk.ac.ebi.pride.model.implementation.mzData.PrecursorImpl;
import uk.ac.ebi.pride.model.implementation.mzData.SpectrumImpl;
import uk.ac.ebi.pride.model.implementation.mzData.BinaryArrayImpl;
import uk.ac.ebi.pride.model.implementation.core.MonoMassDeltaImpl;
import uk.ac.ebi.pride.model.implementation.core.ModificationImpl;

/**
 * @author Florian Reisinger
 *         Date: 16-Jul-2009
 * @since $version
 */
public class XTandemConverter {

    /**
     * This method transforms spectra from X!Tandem files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     * @return a HashMap
     * @throws IOException if a error reading the input files occured
     */
    protected static HashMap<String, Long> transformSpectraFromXTandemDataFile(ArrayList<Spectrum> aTransformedSpectra) throws IOException {

        // @TODO: the xml parsing in this method is very far from perfect and should be reimplemented

        HashMap<String, Long> mapping = new HashMap<String, Long>();

        double[][] arrays;
        Collection<Precursor> precursors;
        Collection<CvParam> ionSelection;

        int charge; // ToDo: check usage, compare with precursorCharge and charges
        File file;

        Integer precursorCharge = 0; // ToDo: check usage, compare with charge and charges

        NodeList idNodes, proteinNodes, peptideNodes;
        DocumentBuilderFactory dbf;
        DocumentBuilder db;
        Document dom;
        Element docEle;

        int totalSpectraCounter = 0; // the total spectra count
        int currentSpectraCounter = 0; // the spectra count for each file

        String fileName;
        boolean matchFound = false;
        NodeList nodes, parameterNodes;
        int spectrumID = -1;
        Double precursorMz = 0.0;
        Double precursorIntensty = 0.0;// ToDo: check usage
        double hyperscore = 0.0;

        String upstreamFlankingSequence = null;
        String downstreamFlankingSequence = null;

        String label = "", peptideSequence = "";
        int start = -1;
        Integer msLevel;
        PRIDEConverter.setIds(new ArrayList<IdentificationGeneral>());

        String modificationName, modificationLocation;
        double modificationMass;
        NamedNodeMap modificationMap;
        ArrayList<CvParam> modificationCVParams;
        ArrayList<MassDelta> monoMasses;
        ArrayList<ModificationImpl> peptideModifications;
        ArrayList<FragmentIon> fragmentIons;

        float[][] arraysFloat;

        ArrayList<CvParam> cVParams;
        ArrayList<UserParam> userParams;
        Collection<SpectrumDescComment> spectrumDescriptionComments;
        Spectrum fragmentation, spectrum;

        Double mzRangeStart, mzRangeStop;

        String[] values;
        Double precursorIntensity = null;

        PRIDEConverter.getProgressDialog().setIntermidiate(true);

        Vector<Double> masses, intensities;
        Vector<Integer> charges; // ToDo: never used, compare with precursorCharge and charge
        ArrayList<String> identifiedSpectraIds = new ArrayList<String>();
        String spectrumTag = "";

        if (PRIDEConverter.getProperties().selectAllIdentifiedSpectra()) {

            // @TODO: If 'select identified spectra' is selected, we need to find out which spectra
            // are identified. So we need to parse the X!Tandem files before parsing the
            // spectra. This means the X!Tandem files are parsed more than once, so maybe
            // this can be done in a better way.

            for (int j = 0; j < PRIDEConverter.getProperties().getSelectedIdentificationFiles().size() && !PRIDEConverter.isConversionCanceled(); j++) {

                // get the spectrumTag
                file = new File(PRIDEConverter.getProperties().getSelectedIdentificationFiles().get(j));

                PRIDEConverter.getProgressDialog().setString(file.getName() + " (" + (j + 1) +
                        "/" + PRIDEConverter.getProperties().getSelectedIdentificationFiles().size() + ")");

                try {
                    // should be replaced by better and simpler xml parsing

                    //get the factory
                    dbf = DocumentBuilderFactory.newInstance();

                    dbf.setValidating(false);
                    dbf.setAttribute("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
                    dbf.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                    dbf.setAttribute("http://xml.org/sax/features/validation", false);

                    //Using factory get an instance of document builder
                    db = dbf.newDocumentBuilder();

                    //parse using builder to get DOM representation of the XML file
                    dom = db.parse(file);

                    //get the root elememt
                    docEle = dom.getDocumentElement();

                    nodes = docEle.getChildNodes();

                    boolean spectrumTagFound = false;

                    // find the spectrum, path tag
                    for (int i = 0; i < nodes.getLength() && !spectrumTagFound; i++) {
                        if (nodes.item(i).getAttributes() != null) {
                            if (nodes.item(i).getAttributes().getNamedItem("type") != null) {
                                if (nodes.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase(
                                        "parameters") &&
                                        nodes.item(i).getAttributes().getNamedItem("label").getNodeValue().equalsIgnoreCase(
                                        "input parameters")) {
                                    parameterNodes = nodes.item(i).getChildNodes();

                                    for (int m = 0; m < parameterNodes.getLength() && !spectrumTagFound; m++) {
                                        if (parameterNodes.item(m).getAttributes() != null) {
                                            if (parameterNodes.item(m).getAttributes().getNamedItem("label").toString().equalsIgnoreCase("label=\"spectrum, path\"")) {
                                                spectrumTag = parameterNodes.item(m).getTextContent();
                                                spectrumTagFound = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!spectrumTagFound) {

                        JOptionPane.showMessageDialog(null,
                                "The X!Tandem file " + file.getPath() + "\n" +
                                "does not contain the reference to the original spectra!\n" +
                                "The file can not be parsed.");
                        PRIDEConverter.setCancelConversion(true);
                    }


                    // parse the rest of the X!Tandem file and find the identifications
                    for (int i = 0; i < nodes.getLength() && !PRIDEConverter.isConversionCanceled(); i++) {

                        if (nodes.item(i).getAttributes() != null) {

                            if (nodes.item(i).getAttributes().getNamedItem("type") != null) {

                                if (nodes.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("model")) {

                                    if (nodes.item(i).getAttributes().getNamedItem("id") != null) {
                                        spectrumID = new Integer(nodes.item(i).getAttributes().getNamedItem("id").getNodeValue());
                                        identifiedSpectraIds.add(spectrumTag + "_" + spectrumID);
                                    }
                                }
                            }
                        }
                    }
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, "The file named " +
                            file.getName() +
                            "\ncould not be found.",
                            "File Not Found", JOptionPane.ERROR_MESSAGE);
                    Util.writeToErrorLog("Error when reading X!Tandem file: ");
                    ex.printStackTrace();
                } catch (Exception e) {

                    Util.writeToErrorLog("Error parsing X!Tandem file: ");
                    e.printStackTrace();

                    JOptionPane.showMessageDialog(null,
                            "The following file could not parsed as an X!Tandem file:\n " +
                            file.getName() +
                            "\n\n" +
                            "See ../Properties/ErrorLog.txt for more details.",
                            "Error Parsing File", JOptionPane.ERROR_MESSAGE);
                }
            }
        }


        // get the spectra
        for (int j = 0; j < PRIDEConverter.getProperties().getSelectedSourceFiles().size() && !PRIDEConverter.isConversionCanceled(); j++) {

            file = new File(PRIDEConverter.getProperties().getSelectedSourceFiles().get(j));
            BufferedReader br = new BufferedReader(new FileReader(file));
            fileName = file.getName();
            PRIDEConverter.setCurrentFileName(fileName);

            PRIDEConverter.getProgressDialog().setIntermidiate(true);
            PRIDEConverter.getProgressDialog().setString(PRIDEConverter.getCurrentFileName() + " (" + (j + 1) + "/" +
                    PRIDEConverter.getProperties().getSelectedSourceFiles().size() + ")");

            try {
// .mgf files
                if (file.getName().toLowerCase().endsWith(".mgf")) {
                    String line;
                    int lineCount = 0;
                    masses = new Vector<Double>();
                    intensities = new Vector<Double>();
                    charges = new Vector<Integer>();
                    boolean inSpectrum = false;
                    msLevel = 2; // defaults to MS/MS
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
                            currentSpectraCounter++;
                        } // END IONS marks the end.
                        else if (line.equals("END IONS")) {
                            inSpectrum = false;

                            matchFound = false;

                            PRIDEConverter.setSpectrumKey(PRIDEConverter.getCurrentFileName() + "_" + currentSpectraCounter);

                            if (PRIDEConverter.getProperties().getSelectedSpectraKeys().size() > 0) {
                                for (int k = 0; k < PRIDEConverter.getProperties().getSelectedSpectraKeys().size() &&
                                        !matchFound && !PRIDEConverter.isConversionCanceled(); k++) {

                                    Object[] temp = (Object[]) PRIDEConverter.getProperties().getSelectedSpectraKeys().get(k);

                                    if (((String) temp[0]).equalsIgnoreCase(PRIDEConverter.getCurrentFileName())) {
                                        if (((String) temp[1]).equalsIgnoreCase("" + currentSpectraCounter)) {
                                            matchFound = true;
                                        }
                                    }
                                }
                            } else {
                                if (PRIDEConverter.getProperties().selectAllIdentifiedSpectra()) {
                                    if (identifiedSpectraIds.contains((PRIDEConverter.getCurrentFileName() + "_" + currentSpectraCounter))) {
                                        matchFound = true;   
                                    }
                                } else {
                                    matchFound = true;
                                }
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
                                        ionSelection.add(new CvParamImpl("PSI:1000041",
                                                "PSI", "ChargeState", ionSelection.size(),
                                                Integer.toString(precursorCharge)));
                                    }
                                }

                                if (precursorIntensity != 0) {
                                    if (precursorIntensity > 1) {
                                        ionSelection.add(new CvParamImpl("PSI:1000042",
                                                "PSI", "Intensity", ionSelection.size(),
                                                Double.toString(precursorIntensity)));
                                    }
                                }

                                if (precursorMz != null) {
                                    ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                                            "MassToChargeRatio", ionSelection.size(), Double.toString(
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

                                    spectrumDescriptionComments = PRIDEConverter.addUserSpectrumComments(spectrumDescriptionComments,
                                            PRIDEConverter.getProperties().getSpectrumCvParams().get(PRIDEConverter.getSpectrumKey()),
                                            PRIDEConverter.getProperties().getSpectrumUserParams().get(PRIDEConverter.getSpectrumKey()));

                                    mzRangeStart = arrays[PRIDEConverter.getProperties().MZ_ARRAY][0];
                                    mzRangeStop = arrays[PRIDEConverter.getProperties().MZ_ARRAY][arrays[PRIDEConverter.getProperties().MZ_ARRAY].length - 1];

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

                                    // Store (spectrumKey, spectrumid) mapping.
                                    Long xTmp = mapping.put(PRIDEConverter.getSpectrumKey(), (long) totalSpectraCounter);

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

                            masses = new Vector<Double>();
                            intensities = new Vector<Double>();
                            charges = new Vector<Integer>();

                            precursorMz = null;
                            precursorIntensity = null;
                            precursorCharge = null;

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
                                intensities.add(Double.parseDouble(values[1]));

                                if (count == 3) {
                                    charges.add(Util.extractCharge(values[2]));
                                }
                            } else {
                                System.err.println("\n\nUnrecognized line at line number " +
                                        lineCount + ": '" + line + "'!\n");
                            }
                        }
                    }
                } else if (file.getName().toLowerCase().endsWith(".dta")) {
// .dta files
                    String currentLine = br.readLine();
                    masses = new Vector<Double>();
                    intensities = new Vector<Double>();
                    StringTokenizer tok;
                    msLevel = 2;
                    currentSpectraCounter = 0;
                    double precursorMh = 0;

                    boolean firstSpectraFound = false;

                    // find the first spectra parent ion
                    while (currentLine != null && !firstSpectraFound && !PRIDEConverter.isConversionCanceled()) {

                        if (PRIDEConverter.getProperties().isCommaTheDecimalSymbol()) {
                            currentLine = currentLine.replaceAll(",", ".");
                        }

                        tok = new StringTokenizer(currentLine);

                        if (tok.countTokens() == 2) {
                            precursorMh = new Double(tok.nextToken());
                            precursorCharge = new Integer(tok.nextToken());
                            firstSpectraFound = true;
                        }

                        currentLine = br.readLine();
                    }

                    // find the rest of the spectra
                    while (currentLine != null && !PRIDEConverter.isConversionCanceled()) {

                        if (PRIDEConverter.getProperties().isCommaTheDecimalSymbol()) {
                            currentLine = currentLine.replaceAll(",", ".");
                        }

                        // at the end of one spectrum
                        if (currentLine.trim().length() == 0) {

                            // empty spectra are ignored by X!Tandem
                            if (masses.size() > 0) {
                                currentSpectraCounter++;

                                PRIDEConverter.setSpectrumKey(PRIDEConverter.getCurrentFileName() + "_" + currentSpectraCounter);

                                // check if the spectra is selected or not
                                matchFound = false;

                                if (PRIDEConverter.getProperties().getSelectedSpectraKeys().size() > 0) {
                                    for (int k = 0; k < PRIDEConverter.getProperties().getSelectedSpectraKeys().size() &&
                                            !matchFound && !PRIDEConverter.isConversionCanceled(); k++) {

                                        Object[] temp = (Object[]) PRIDEConverter.getProperties().getSelectedSpectraKeys().get(k);

                                        if (((String) temp[0]).equalsIgnoreCase(PRIDEConverter.getCurrentFileName())) {
                                            if (((String) temp[1]).equalsIgnoreCase("" + currentSpectraCounter)) {
                                                matchFound = true;
                                            }
                                        }
                                    }
                                } else {
                                    if (PRIDEConverter.getProperties().selectAllIdentifiedSpectra()) {
                                        if (identifiedSpectraIds.contains((PRIDEConverter.getCurrentFileName() + "_" + currentSpectraCounter))) {
                                            matchFound = true;
                                        }
                                    } else {
                                        matchFound = true;
                                    }
                                }


                                if (matchFound) {

                                    arrays = new double[2][masses.size()];

                                    for (int i = 0; i < masses.size() && !PRIDEConverter.isConversionCanceled(); i++) {
                                        arrays[0][i] = masses.get(i);
                                        arrays[1][i] = intensities.get(i);
                                    }

                                    // Precursor collection.
                                    precursors = new ArrayList<Precursor>();

                                    // Ion selection parameters.
                                    ionSelection = new ArrayList<CvParam>();

                                    // See if we know the precursor charge, and if so, include it.
                                    charge = precursorCharge;

                                    if (charge > 0) {
                                        ionSelection.add(new CvParamImpl("PSI:1000041",
                                                "PSI", "ChargeState", ionSelection.size(),
                                                Integer.toString(charge)));
                                    }

                                    // calculated precursor m/z
                                    ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                                            "MassToChargeRatio", ionSelection.size(), Double.toString(
                                            ((precursorMh - PRIDEConverter.getProperties().HYDROGEN_MASS + precursorCharge * PRIDEConverter.getProperties().HYDROGEN_MASS) / precursorCharge))));

                                    // precursor MH+
                                    ionSelection.add(new CvParamImpl("PRIDE:0000051",
                                            "PRIDE", "(M+H)+", ionSelection.size(), Double.toString(precursorMh)));

                                    precursors.add(new PrecursorImpl(null, null, ionSelection, null, msLevel - 1, 0, 0));

                                    // Spectrum description comments.
                                    spectrumDescriptionComments = new ArrayList<SpectrumDescComment>();

                                    if (arrays[PRIDEConverter.getProperties().MZ_ARRAY].length > 0) {

                                        mzRangeStart = arrays[PRIDEConverter.getProperties().MZ_ARRAY][0];
                                        mzRangeStop = arrays[PRIDEConverter.getProperties().MZ_ARRAY][arrays[PRIDEConverter.getProperties().MZ_ARRAY].length - 1];

                                        spectrumDescriptionComments = PRIDEConverter.addUserSpectrumComments(spectrumDescriptionComments,
                                                PRIDEConverter.getProperties().getSpectrumCvParams().get(PRIDEConverter.getSpectrumKey()),
                                                PRIDEConverter.getProperties().getSpectrumUserParams().get(PRIDEConverter.getSpectrumKey()));

                                        // Create new mzData spectrum for the fragmentation spectrum.
                                        fragmentation = new SpectrumImpl(
                                                new BinaryArrayImpl(arrays[PRIDEConverter.getProperties().INTENSITIES_ARRAY],
                                                BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                                mzRangeStart,
                                                new BinaryArrayImpl(arrays[PRIDEConverter.getProperties().MZ_ARRAY],
                                                BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                                msLevel,
                                                null,
                                                mzRangeStop,
                                                null,
                                                ++totalSpectraCounter, precursors,
                                                spectrumDescriptionComments,
                                                null, null,
                                                null, null);

                                        // Store (spectrumKey, spectrumid) mapping.
                                        Long xTmp = mapping.put(PRIDEConverter.getSpectrumKey(), (long) totalSpectraCounter);

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

                            masses = new Vector<Double>();
                            intensities = new Vector<Double>();
                            msLevel = 2;

                            // read the first line in the next spectrum
                            currentLine = br.readLine();

                            tok = new StringTokenizer(currentLine);
                            precursorMh = new Double(tok.nextToken());
                            precursorCharge = new Integer(tok.nextToken());

                        } else {
                            tok = new StringTokenizer(currentLine);
                            masses.add(Double.parseDouble(tok.nextToken()));
                            intensities.add(Double.parseDouble(tok.nextToken()));
                        }

                        currentLine = br.readLine();
                    }


                    // empty spectra are ignored by X!Tandem
                    if (masses.size() > 0) {
                        currentSpectraCounter++;

                        PRIDEConverter.setSpectrumKey(PRIDEConverter.getCurrentFileName() + "_" + currentSpectraCounter);

                        // add the last spectra
                        // check if the spectra is selected or not
                        matchFound = false;

                        if (PRIDEConverter.getProperties().getSelectedSpectraKeys().size() > 0) {
                            for (int k = 0; k < PRIDEConverter.getProperties().getSelectedSpectraKeys().size() &&
                                    !matchFound && !PRIDEConverter.isConversionCanceled(); k++) {

                                Object[] temp = (Object[]) PRIDEConverter.getProperties().getSelectedSpectraKeys().get(k);

                                if (((String) temp[0]).equalsIgnoreCase(PRIDEConverter.getCurrentFileName())) {
                                    if (((String) temp[1]).equalsIgnoreCase("" + currentSpectraCounter)) {
                                        matchFound = true;
                                    }
                                }
                            }
                        } else {
                            if (PRIDEConverter.getProperties().selectAllIdentifiedSpectra()) {
                                if (identifiedSpectraIds.contains((PRIDEConverter.getCurrentFileName() + "_" + currentSpectraCounter))) {
                                    matchFound = true;
                                }
                            } else {
                                matchFound = true;
                            }
                        }

                        if (matchFound) {

                            arrays = new double[2][masses.size()];

                            for (int i = 0; i < masses.size() && !PRIDEConverter.isConversionCanceled(); i++) {
                                arrays[0][i] = masses.get(i);
                                arrays[1][i] = intensities.get(i);
                            }

                            // Precursor collection.
                            precursors = new ArrayList<Precursor>();

                            // Ion selection parameters.
                            ionSelection = new ArrayList<CvParam>();

                            // See if we know the precursor charge, and if so, include it.
                            charge = precursorCharge;
                            if (charge > 0) {
                                ionSelection.add(new CvParamImpl("PSI:1000041", "PSI",
                                        "ChargeState", ionSelection.size(), Integer.toString(charge)));
                            }

                            // calculated precursor m/z
                            ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                                    "MassToChargeRatio", ionSelection.size(), Double.toString(
                                    ((precursorMh - PRIDEConverter.getProperties().HYDROGEN_MASS + precursorCharge * PRIDEConverter.getProperties().HYDROGEN_MASS) / precursorCharge))));

                            // precursor MH+
                            ionSelection.add(new CvParamImpl("PRIDE:0000051",
                                    "PRIDE", "(M+H)+", ionSelection.size(), Double.toString(precursorMh)));

                            precursors.add(new PrecursorImpl(null, null, ionSelection, null, msLevel - 1, 0, 0));

                            spectrumDescriptionComments = new ArrayList<SpectrumDescComment>();

                            if (arrays[PRIDEConverter.getProperties().MZ_ARRAY].length > 0) {

                                mzRangeStart = arrays[PRIDEConverter.getProperties().MZ_ARRAY][0];
                                mzRangeStop = arrays[PRIDEConverter.getProperties().MZ_ARRAY][arrays[PRIDEConverter.getProperties().MZ_ARRAY].length - 1];

                                spectrumDescriptionComments = PRIDEConverter.addUserSpectrumComments(spectrumDescriptionComments,
                                        PRIDEConverter.getProperties().getSpectrumCvParams().get(PRIDEConverter.getSpectrumKey()),
                                        PRIDEConverter.getProperties().getSpectrumUserParams().get(PRIDEConverter.getSpectrumKey()));

                                // Create new mzData spectrum for the fragmentation spectrum.
                                fragmentation = new SpectrumImpl(
                                        new BinaryArrayImpl(arrays[PRIDEConverter.getProperties().INTENSITIES_ARRAY],
                                        BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                        mzRangeStart,
                                        new BinaryArrayImpl(arrays[PRIDEConverter.getProperties().MZ_ARRAY],
                                        BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                        msLevel,
                                        null,
                                        mzRangeStop,
                                        null,
                                        ++totalSpectraCounter, precursors,
                                        spectrumDescriptionComments,
                                        null, null,
                                        null, null);

                                // Store (spectrumKey, spectrumid) mapping.
                                Long xTmp = mapping.put(PRIDEConverter.getSpectrumKey(), (long) totalSpectraCounter);

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
                    // ToDo: precursorCharge has not been reset! the value might be carried on
                    // ToDo: check for cases of variables being "carried on" !?
                } else if (file.getName().toLowerCase().endsWith(".pkl")) {
// .pkl files
                    String currentLine = br.readLine();
                    masses = new Vector<Double>();
                    intensities = new Vector<Double>();
                    StringTokenizer tok;
                    msLevel = 2;
                    currentSpectraCounter = 0;

                    boolean firstSpectraFound = false;

                    // find the first spectra parent ion
                    while (currentLine != null && !firstSpectraFound && !PRIDEConverter.isConversionCanceled()) {

                        if (PRIDEConverter.getProperties().isCommaTheDecimalSymbol()) {
                            currentLine = currentLine.replaceAll(",", ".");
                        }

                        tok = new StringTokenizer(currentLine);

                        if (tok.countTokens() == 3) {

                            precursorMz = Double.parseDouble(tok.nextToken());
                            precursorIntensty = Double.parseDouble(tok.nextToken());
                            precursorCharge = Integer.parseInt(tok.nextToken());

                            firstSpectraFound = true;
                        }

                        currentLine = br.readLine();
                    }

                    // find the rest of the spectra
                    while (currentLine != null && !PRIDEConverter.isConversionCanceled()) {

                        if (PRIDEConverter.getProperties().isCommaTheDecimalSymbol()) {
                            currentLine = currentLine.replaceAll(",", ".");
                        }

                        tok = new StringTokenizer(currentLine);

                        if (tok.countTokens() == 3) {

                            // empty spectra are ignored by X!Tandem
                            if (masses.size() > 0) {
                                currentSpectraCounter++;
                            }

                            PRIDEConverter.setSpectrumKey(PRIDEConverter.getCurrentFileName() + "_" + currentSpectraCounter);

                            // check if the spectra is selected or not
                            matchFound = false;

                            if (PRIDEConverter.getProperties().getSelectedSpectraKeys().size() > 0) {
                                for (int k = 0; k < PRIDEConverter.getProperties().getSelectedSpectraKeys().size() &&
                                        !matchFound && !PRIDEConverter.isConversionCanceled(); k++) {

                                    Object[] temp = (Object[]) PRIDEConverter.getProperties().getSelectedSpectraKeys().get(k);

                                    if (((String) temp[0]).equalsIgnoreCase(PRIDEConverter.getCurrentFileName())) {
                                        if (((String) temp[1]).equalsIgnoreCase("" + currentSpectraCounter)) {
                                            matchFound = true;
                                        }
                                    }
                                }
                            } else {
                                if (PRIDEConverter.getProperties().selectAllIdentifiedSpectra()) {
                                    if (identifiedSpectraIds.contains((PRIDEConverter.getCurrentFileName() + "_" + currentSpectraCounter))) {
                                        matchFound = true;
                                    }
                                } else {
                                    matchFound = true;
                                }
                            }


                            if (matchFound) {

                                arrays = new double[2][masses.size()];

                                for (int i = 0; i < masses.size() && !PRIDEConverter.isConversionCanceled(); i++) {
                                    arrays[0][i] = masses.get(i);
                                    arrays[1][i] = intensities.get(i);
                                }

                                // Precursor collection.
                                precursors = new ArrayList<Precursor>();

                                // Ion selection parameters.
                                ionSelection = new ArrayList<CvParam>();

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
                                        "PSI", "MassToChargeRatio", ionSelection.size(), Double.toString(precursorMz)));

                                precursors.add(new PrecursorImpl(null, null, ionSelection, null, msLevel - 1, 0, 0));

                                spectrumDescriptionComments = new ArrayList<SpectrumDescComment>();

                                if (arrays[PRIDEConverter.getProperties().MZ_ARRAY].length > 0) {

                                    mzRangeStart = arrays[PRIDEConverter.getProperties().MZ_ARRAY][0];
                                    mzRangeStop = arrays[PRIDEConverter.getProperties().MZ_ARRAY][arrays[PRIDEConverter.getProperties().MZ_ARRAY].length - 1];

                                    spectrumDescriptionComments = PRIDEConverter.addUserSpectrumComments(spectrumDescriptionComments,
                                            PRIDEConverter.getProperties().getSpectrumCvParams().get(PRIDEConverter.getSpectrumKey()),
                                            PRIDEConverter.getProperties().getSpectrumUserParams().get(PRIDEConverter.getSpectrumKey()));

                                    // Create new mzData spectrum for the fragmentation spectrum.
                                    fragmentation = new SpectrumImpl(
                                            new BinaryArrayImpl(arrays[PRIDEConverter.getProperties().INTENSITIES_ARRAY],
                                            BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                            mzRangeStart,
                                            new BinaryArrayImpl(arrays[PRIDEConverter.getProperties().MZ_ARRAY],
                                            BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                            msLevel,
                                            null,
                                            mzRangeStop,
                                            null,
                                            ++totalSpectraCounter, precursors,
                                            spectrumDescriptionComments,
                                            null, null,
                                            null, null);

                                    // Store (spectrumKey, spectrumid) mapping.
                                    Long xTmp = mapping.put(PRIDEConverter.getSpectrumKey(), (long) totalSpectraCounter);

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

                            masses = new Vector<Double>();
                            intensities = new Vector<Double>();
                            msLevel = 2;

                            precursorMz = new Double(tok.nextToken());
                            precursorIntensty = new Double(tok.nextToken());
                            precursorCharge = new Integer(tok.nextToken());

                        } else if (!currentLine.equalsIgnoreCase("")) {
                            tok = new StringTokenizer(currentLine);
                            masses.add(Double.parseDouble(tok.nextToken()));
                            intensities.add(Double.parseDouble(tok.nextToken()));
                        }

                        currentLine = br.readLine();
                    }


                    // empty spectra are ignored by X!Tandem (and PRIDE)
                    if (masses.size() > 0) {
                        currentSpectraCounter++;
                    }

                    PRIDEConverter.setSpectrumKey(PRIDEConverter.getCurrentFileName() + "_" + currentSpectraCounter);

                    // add the last spectra
                    // check if the spectra is selected or not
                    matchFound = false;

                    if (PRIDEConverter.getProperties().getSelectedSpectraKeys().size() > 0) {
                        for (int k = 0; k < PRIDEConverter.getProperties().getSelectedSpectraKeys().size() &&
                                !matchFound && !PRIDEConverter.isConversionCanceled(); k++) {

                            Object[] temp = (Object[]) PRIDEConverter.getProperties().getSelectedSpectraKeys().get(k);

                            if (((String) temp[0]).equalsIgnoreCase(PRIDEConverter.getCurrentFileName())) {
                                if (((String) temp[1]).equalsIgnoreCase("" + currentSpectraCounter)) {
                                    matchFound = true;
                                }
                            }
                        }
                    } else {
                        if (PRIDEConverter.getProperties().selectAllIdentifiedSpectra()) {
                            if (identifiedSpectraIds.contains((PRIDEConverter.getCurrentFileName() + "_" + currentSpectraCounter))) {
                                matchFound = true;
                            }
                        } else {
                            matchFound = true;
                        }
                    }

                    if (matchFound) {

                        arrays = new double[2][masses.size()];

                        for (int i = 0; i < masses.size() && !PRIDEConverter.isConversionCanceled(); i++) {
                            arrays[0][i] = masses.get(i);
                            arrays[1][i] = intensities.get(i);
                        }

                        // Precursor collection.
                        precursors = new ArrayList<Precursor>();

                        // Ion selection parameters.
                        ionSelection = new ArrayList<CvParam>();

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

                        precursors.add(new PrecursorImpl(null, null, ionSelection, null, msLevel - 1, 0, 0));

                        spectrumDescriptionComments = new ArrayList<SpectrumDescComment>();

                        if (arrays[PRIDEConverter.getProperties().MZ_ARRAY].length > 0) {

                            mzRangeStart = arrays[PRIDEConverter.getProperties().MZ_ARRAY][0];
                            mzRangeStop = arrays[PRIDEConverter.getProperties().MZ_ARRAY][arrays[PRIDEConverter.getProperties().MZ_ARRAY].length - 1];

                            spectrumDescriptionComments = PRIDEConverter.addUserSpectrumComments(spectrumDescriptionComments,
                                    PRIDEConverter.getProperties().getSpectrumCvParams().get(PRIDEConverter.getSpectrumKey()),
                                    PRIDEConverter.getProperties().getSpectrumUserParams().get(PRIDEConverter.getSpectrumKey()));

                            // Create new mzData spectrum for the fragmentation spectrum.
                            fragmentation = new SpectrumImpl(
                                    new BinaryArrayImpl(arrays[PRIDEConverter.getProperties().INTENSITIES_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    mzRangeStart,
                                    new BinaryArrayImpl(arrays[PRIDEConverter.getProperties().MZ_ARRAY],
                                    BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                    msLevel,
                                    null,
                                    mzRangeStop,
                                    null,
                                    ++totalSpectraCounter, precursors,
                                    spectrumDescriptionComments,
                                    null, null,
                                    null, null);

                            // Store (spectrumKey, spectrumid) mapping.
                            Long xTmp = mapping.put(PRIDEConverter.getSpectrumKey(), (long) totalSpectraCounter);

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
                } else if (file.getName().toLowerCase().endsWith(".mzdata")) {
// .mzdata file
                    PRIDEConverter.setMzData( PRIDEConverter.combineMzDataFiles(mapping, aTransformedSpectra) );

                } else if (file.getName().toLowerCase().endsWith(".mzxml")) {
// .mzxml file
                    MSXMLParser msXMLParser = new MSXMLParser(
                            new File(PRIDEConverter.getProperties().getSelectedSourceFiles().get(j)).getAbsolutePath());

                    int scanCount = msXMLParser.getScanCount();

                    PRIDEConverter.getProgressDialog().setIntermidiate(false);
                    PRIDEConverter.getProgressDialog().setMax(scanCount);

                    for (int i = 1; i <= scanCount && !PRIDEConverter.isConversionCanceled(); i++) {

                        PRIDEConverter.getProgressDialog().setValue(i);

                        Scan scan = msXMLParser.rap(i);
                        precursorCharge = scan.getPrecursorCharge();
                        precursorMz = new Float(scan.getPrecursorMz()).doubleValue();
                        msLevel = scan.getMsLevel();

                        matchFound = false;

                        if (PRIDEConverter.getProperties().getSelectedSpectraKeys().size() > 0) {
                            for (int k = 0; k < PRIDEConverter.getProperties().getSelectedSpectraKeys().size() &&
                                    !matchFound && !PRIDEConverter.isConversionCanceled(); k++) {

                                Object[] temp = (Object[]) PRIDEConverter.getProperties().getSelectedSpectraKeys().get(k);

                                if (((String) temp[0]).equalsIgnoreCase(PRIDEConverter.getCurrentFileName())) {
                                    if (((String) temp[1]).equalsIgnoreCase("" + scan.getNum())) {
                                        matchFound = true;
                                        PRIDEConverter.setSpectrumKey(PRIDEConverter.getCurrentFileName() + "_" + scan.getNum());
                                    }
                                }
                            }
                        } else {
                            if (PRIDEConverter.getProperties().selectAllIdentifiedSpectra()) {
                                if (identifiedSpectraIds.contains((PRIDEConverter.getCurrentFileName() + "_" + scan.getNum()))) {
                                    matchFound = true;
                                    PRIDEConverter.setSpectrumKey(PRIDEConverter.getCurrentFileName() + "_" + scan.getNum());
                                }
                            } else {
                                matchFound = true;
                                PRIDEConverter.setSpectrumKey(PRIDEConverter.getCurrentFileName() + "_" + scan.getNum());
                            }
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
                                ionSelection.add(new CvParamImpl("PSI:1000041", "PSI",
                                        "ChargeState", 0,
                                        Integer.toString(precursorCharge)));
                            }

                            ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                                    "MassToChargeRatio", 2, precursorMz.toString()));

                            precursors.add(new PrecursorImpl(null, null, ionSelection, null, 1, 0, 0));

                            // Spectrum description comments.
                            spectrumDescriptionComments = new ArrayList<SpectrumDescComment>();

                            if (arraysFloat[PRIDEConverter.getProperties().MZ_ARRAY].length > 0) {

                                totalSpectraCounter++;

                                mzRangeStart = (double) arraysFloat[PRIDEConverter.getProperties().MZ_ARRAY][0];
                                mzRangeStop = (double) arraysFloat[PRIDEConverter.getProperties().MZ_ARRAY][arraysFloat[PRIDEConverter.getProperties().MZ_ARRAY].length - 1];

                                spectrumDescriptionComments = PRIDEConverter.addUserSpectrumComments(spectrumDescriptionComments,
                                        PRIDEConverter.getProperties().getSpectrumCvParams().get(PRIDEConverter.getSpectrumKey()),
                                        PRIDEConverter.getProperties().getSpectrumUserParams().get(PRIDEConverter.getSpectrumKey()));

                                if (msLevel == 1) {
                                    spectrum = new SpectrumImpl(
                                            new BinaryArrayImpl(arraysFloat[PRIDEConverter.getProperties().INTENSITIES_ARRAY],
                                            BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                            mzRangeStart,
                                            new BinaryArrayImpl(arraysFloat[PRIDEConverter.getProperties().MZ_ARRAY],
                                            BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                            1, null,
                                            (double) arraysFloat[PRIDEConverter.getProperties().MZ_ARRAY]
                                                                [arraysFloat[PRIDEConverter.getProperties().MZ_ARRAY].length -1], //null,
                                            null,
                                            totalSpectraCounter, null,
                                            spectrumDescriptionComments,
                                            null, null,
                                            null, null);
                                } else {//msLevel == 2){
                                    spectrum = new SpectrumImpl(
                                            new BinaryArrayImpl(arraysFloat[PRIDEConverter.getProperties().INTENSITIES_ARRAY],
                                            BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                            mzRangeStart,
                                            new BinaryArrayImpl(arraysFloat[PRIDEConverter.getProperties().MZ_ARRAY],
                                            BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                            2, null,
                                            mzRangeStop,
                                            null,
                                            totalSpectraCounter, precursors,
                                            spectrumDescriptionComments,
                                            null, null,
                                            null, null);
                                }

                                // Create new mzData spectrum for the fragmentation spectrum.
                                // Store (spectrumfileid, spectrumid) mapping.
                                Long xTmp = mapping.put(PRIDEConverter.getSpectrumKey(), (long) totalSpectraCounter);

                                if (xTmp != null) {
                                    // we already stored a result for this ID!!!
                                    JOptionPane.showMessageDialog(null, "Ambiguous spectrum mapping. Please consult " +
                                            "the error log file for details.", "Mapping Error", JOptionPane.ERROR_MESSAGE);
                                    Util.writeToErrorLog("Ambiguous spectrum mapping for ID '" + currentSpectraCounter
                                            + "' and spectrum file '" + PRIDEConverter.getCurrentFileName() + "'." );
                                }

                                // Store the transformed spectrum.
                                aTransformedSpectra.add(spectrum);
                            }
                        }
                    }
                }

                br.close();

            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(null, "The file named " +
                        file.getName() +
                        "\ncould not be found.",
                        "File Not Found", JOptionPane.ERROR_MESSAGE);
                Util.writeToErrorLog("Error when reading X!Tandem file: ");
                ex.printStackTrace();
            } catch (Exception e) {

                Util.writeToErrorLog("Error parsing X!Tandem file: ");
                e.printStackTrace();

                JOptionPane.showMessageDialog(null,
                        "The following file could not parsed as an X!Tandem spetrum file:\n " +
                        file.getName() +
                        "\n\n" +
                        "See ../Properties/ErrorLog.txt for more details.",
                        "Error Parsing File", JOptionPane.ERROR_MESSAGE);
            }

            if (!file.getName().toLowerCase().endsWith(".mzdata")) {
                PRIDEConverter.setTotalNumberOfSpectra(totalSpectraCounter);
            }
        }


        // parse the identifications
        for (int j = 0; j < PRIDEConverter.getProperties().getSelectedIdentificationFiles().size() && !PRIDEConverter.isConversionCanceled(); j++) {

            // get the spectrumTag
            file = new File(PRIDEConverter.getProperties().getSelectedIdentificationFiles().get(j));

            PRIDEConverter.getProgressDialog().setIntermidiate(true);
            PRIDEConverter.getProgressDialog().setString(file.getName() + " (" + (j + 1) +
                    "/" + PRIDEConverter.getProperties().getSelectedIdentificationFiles().size() + ")");

            spectrumID = -1;
            Double precursorMh = 0.0;
            precursorCharge = 0;
            hyperscore = 0.0;

            upstreamFlankingSequence = null;
            downstreamFlankingSequence = null;

            // @TODO: should be replaced by better and simpler xml parsing
            try {
                //get the factory
                dbf = DocumentBuilderFactory.newInstance();

                dbf.setValidating(false);
                dbf.setAttribute("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
                dbf.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                dbf.setAttribute("http://xml.org/sax/features/validation", false);

                //Using factory get an instance of document builder
                db = dbf.newDocumentBuilder();

                //parse using builder to get DOM representation of the XML file
                dom = db.parse(file);

                //get the root elememt
                docEle = dom.getDocumentElement();
                nodes = docEle.getChildNodes();

                spectrumTag = "";

                // find the spectrum, path tag
                for (int i = 0; i < nodes.getLength() && !PRIDEConverter.isConversionCanceled(); i++) {
                    if (nodes.item(i).getAttributes() != null) {
                        if (nodes.item(i).getAttributes().getNamedItem("type") != null) {
                            if (nodes.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("parameters") &&
                                    nodes.item(i).getAttributes().getNamedItem("label").getNodeValue().equalsIgnoreCase("input parameters")) {
                                parameterNodes = nodes.item(i).getChildNodes();

                                for (int m = 0; m < parameterNodes.getLength(); m++) {
                                    if (parameterNodes.item(m).getAttributes() != null) {
                                        if (parameterNodes.item(m).getAttributes().getNamedItem("label").toString().equalsIgnoreCase("label=\"spectrum, path\"")) {
                                            //System.out.println(parameterNodes.item(m).getTextContent());
                                            //identifiedSpectraIds.add(parameterNodes.item(m).getTextContent() + "_" + spectrumID);
                                            spectrumTag = parameterNodes.item(m).getTextContent();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (spectrumTag.equalsIgnoreCase("")) {

                    JOptionPane.showMessageDialog(null,
                            "The X!Tandem file " + PRIDEConverter.getProperties().getSelectedIdentificationFiles().get(j) + "\n" +
                            "does not contain the reference to the original spectra!\n" +
                            "The file can not be parsed.");
                    PRIDEConverter.setCancelConversion(true);
                } else {
                    // get the identifications

                    PRIDEConverter.getProgressDialog().setIntermidiate(false);
                    PRIDEConverter.getProgressDialog().setMax(nodes.getLength());

                    for (int i = 0; i < nodes.getLength() && !PRIDEConverter.isConversionCanceled(); i++) {

                        PRIDEConverter.getProgressDialog().setValue(i);

                        if (nodes.item(i).getAttributes() != null) {

                            if (nodes.item(i).getAttributes().getNamedItem("type") != null) {

                                peptideModifications = null;
                                fragmentIons = new ArrayList<FragmentIon>();



                                // @TODO: Extract fragmention ion annotations (X!Tandem).

                                // 1 - get the fragment ions from the X!Tandem xml file somehow
                                //     (when you know where to find this information, you might want to move the
                                //      extraction of the info into one of the if's further down in this code)
                                // 2 - add them to the fragmentIons list (see Mascot Dat File for how to do this)
                                // 3 - add new mappings to the FragmentIonsMapping.prop file
                                // 4 - that's it, the rest is taken care of :)



                                if (nodes.item(i).getAttributes().getNamedItem("type").
                                        getNodeValue().equalsIgnoreCase("model")) {

                                    if (nodes.item(i).getAttributes().getNamedItem("id") != null) {
                                        spectrumID = Integer.parseInt(nodes.item(i).getAttributes().getNamedItem("id").getNodeValue());
                                    }

                                    if (nodes.item(i).getAttributes().getNamedItem("mh") != null) {
                                        precursorMh = Double.parseDouble(nodes.item(i).getAttributes().getNamedItem("mh").getNodeValue());
                                    }

                                    if (nodes.item(i).getAttributes().getNamedItem("z") != null) {
                                        precursorCharge = Integer.parseInt(nodes.item(i).getAttributes().getNamedItem("z").getNodeValue());
                                    }

                                    if (nodes.item(i).getAttributes().getNamedItem("label") != null) {
                                        label = nodes.item(i).getAttributes().getNamedItem("label").getNodeValue();
                                    }

                                    matchFound = false;
                                    PRIDEConverter.setSpectrumKey(spectrumTag + "_" + spectrumID);

                                    if (PRIDEConverter.getProperties().getSelectedSpectraKeys().size() > 0) {
                                        for (int k = 0; k < PRIDEConverter.getProperties().getSelectedSpectraKeys().size() && !matchFound; k++) {

                                            Object[] temp = (Object[]) PRIDEConverter.getProperties().getSelectedSpectraKeys().get(k);

                                            if (((String) temp[0]).equalsIgnoreCase(spectrumTag)) {
                                                if (((String) temp[1]).equalsIgnoreCase("" + spectrumID)) {
                                                    matchFound = true;
                                                }
                                            }
                                        }
                                    } else {
                                        if (PRIDEConverter.getProperties().getSpectraSelectionCriteria() != null) {

                                            StringTokenizer tok;

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
                                                    if (spectrumTag.lastIndexOf(tempToken) != -1) {
                                                        matchFound = true;
                                                    }
                                                } else {
                                                    if (("" + spectrumID).lastIndexOf(tempToken) != -1) {
                                                        matchFound = true;
                                                    }
                                                }
                                            }
                                        } else {
                                            matchFound = true;
                                        }
                                    }

                                    if (matchFound) {

                                        idNodes = nodes.item(i).getChildNodes();

                                        for (int k = 0; k < idNodes.getLength(); k++) {

                                            if (idNodes.item(k).getNodeName().equalsIgnoreCase("protein")) {

                                                proteinNodes = idNodes.item(k).getChildNodes();

                                                for (int m = 0; m < proteinNodes.getLength(); m++) {

                                                    if (proteinNodes.item(m).getNodeName().equalsIgnoreCase("peptide")) {

                                                        peptideNodes = proteinNodes.item(m).getChildNodes();

                                                        for (int n = 0; n < peptideNodes.getLength(); n++) {

                                                            if (peptideNodes.item(n).getNodeName().equalsIgnoreCase("domain")) {

                                                                start = Integer.parseInt(peptideNodes.item(n).getAttributes().getNamedItem("start").getNodeValue());
                                                                peptideSequence =
                                                                        peptideNodes.item(n).getAttributes().getNamedItem("seq").getNodeValue();

                                                                hyperscore = Double.parseDouble(peptideNodes.item(n).getAttributes().getNamedItem("hyperscore").getNodeValue());

                                                                upstreamFlankingSequence =
                                                                        peptideNodes.item(n).getAttributes().getNamedItem("pre").getNodeValue();

                                                                downstreamFlankingSequence =
                                                                        peptideNodes.item(n).getAttributes().getNamedItem("post").getNodeValue();

                                                                peptideModifications = new ArrayList<ModificationImpl>();
                                                                modificationCVParams = new ArrayList<CvParam>();

                                                                for (int c = 0; c < peptideNodes.item(n).getChildNodes().getLength(); c++) {

                                                                    if (peptideNodes.item(n).getChildNodes().item(c).getNodeName().equalsIgnoreCase("aa")) {

                                                                        modificationMap = peptideNodes.item(n).getChildNodes().item(c).getAttributes();

                                                                        modificationName = modificationMap.getNamedItem("type").getNodeValue();
                                                                        modificationLocation = "" +
                                                                                (new Integer(modificationMap.getNamedItem("at").getNodeValue()) - start + 1);
                                                                        modificationMass = new Double(modificationMap.getNamedItem("modified").getNodeValue());

                                                                        modificationName += "=" + modificationMass;

                                                                        if (!PRIDEConverter.getProperties().getAlreadyChoosenModifications().contains(modificationName)) {
                                                                            new ModificationMapping(PRIDEConverter.getOutputFrame(),
                                                                                    true, PRIDEConverter.getProgressDialog(),
                                                                                    modificationName,
                                                                                    "-",
                                                                                    modificationMass,
                                                                                    (CvParamImpl) PRIDEConverter.getUserProperties().getCVTermMappings().
                                                                                    get(modificationName), false);

                                                                            PRIDEConverter.getProperties().getAlreadyChoosenModifications().add(modificationName);
                                                                        } else {
                                                                            //do nothing, mapping already choosen
                                                                        }

                                                                        CvParamImpl tempCvParam =
                                                                                (CvParamImpl) PRIDEConverter.getUserProperties().getCVTermMappings().get(modificationName);

                                                                        modificationCVParams.add(tempCvParam);

                                                                        monoMasses = new ArrayList<MassDelta>();
                                                                        // get the modification mass (DiffMono) retrieved from PSI-MOD
                                                                        if (tempCvParam.getValue() != null) {
                                                                            monoMasses.add(new MonoMassDeltaImpl(Double.parseDouble(tempCvParam.getValue())));
                                                                        } else {
                                                                            // if the DiffMono is not found for the PSI-MOD the mass
                                                                            // from the file is used
                                                                            monoMasses.add(new MonoMassDeltaImpl(modificationMass));
                                                                        //monoMasses = null;
                                                                        }

                                                                        peptideModifications.add(new ModificationImpl(
                                                                                tempCvParam.getAccession(),
                                                                                new Integer(modificationLocation),
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
                                                    }
                                                }
                                            }
                                        }

                                        Spectrum tempSpectrumImpl = null;

                                        try {
                                            tempSpectrumImpl = aTransformedSpectra.get( mapping.get(PRIDEConverter.getSpectrumKey()).intValue() - 1);
                                        } catch (NullPointerException e) {

                                            PRIDEConverter.setCancelConversion(true);
                                            PRIDEConverter.getProgressDialog().setVisible(false);
                                            PRIDEConverter.getProgressDialog().dispose();

                                            JOptionPane.showMessageDialog(PRIDEConverter.getOutputFrame(),
                                                    "The X!Tandem file references an unknown MS spectrum.\n" +
                                                    "Are you sure you are using the correct spectrum file?\n\n" +
                                                    "See ../Properties/ErrorLog.txt for more details.\n\n" +
                                                    "PRIDE XML file not created.",
                                                    "Error Parsing File", JOptionPane.ERROR_MESSAGE);
                                            Util.writeToErrorLog("Spectrum key \'" + PRIDEConverter.getSpectrumKey() + "\' was not found!");
                                            e.printStackTrace();
                                        }

                                        if (!PRIDEConverter.isConversionCanceled()) {

                                            //calculate itraq values
                                            iTRAQ iTRAQValues = null;

                                            if (PRIDEConverter.getProperties().getSampleDescriptionCVParamsQuantification().size() > 0) {

                                                iTRAQValues = new iTRAQ(tempSpectrumImpl.getMzArrayBinary().getDoubleArray(),
                                                        tempSpectrumImpl.getIntenArrayBinary().getDoubleArray(),
                                                        ((precursorMh - PRIDEConverter.getProperties().HYDROGEN_MASS + precursorCharge * PRIDEConverter.getProperties().HYDROGEN_MASS) / precursorCharge),
                                                        precursorCharge,
                                                        PRIDEConverter.getUserProperties().getPeakIntegrationRangeLower(),
                                                        PRIDEConverter.getUserProperties().getPeakIntegrationRangeUpper(),
                                                        PRIDEConverter.getUserProperties().getReporterIonIntensityThreshold(),
                                                        PRIDEConverter.getUserProperties().getPurityCorrections());
                                                iTRAQValues.calculateiTRAQValues();
                                            }

                                            String[] iTraqNorm = null;
                                            String[] iTraqUT = null;
                                            String[][] iTraqRatio = null;

                                            if (PRIDEConverter.getProperties().getSampleDescriptionCVParamsQuantification().size() > 0) {
                                                //iTraq-stuff
                                                iTraqNorm = (String[]) iTRAQValues.getAllNorms().get(0);
                                                iTraqUT = (String[]) iTRAQValues.getAllUTs().get(0);
                                                iTraqRatio = (String[][]) iTRAQValues.getAllRatios().get(0);
                                            }

                                            cVParams = new ArrayList<CvParam>();
                                            cVParams.add(new CvParamImpl("PRIDE:0000176",
                                                    "PRIDE", "X!Tandem Hyperscore",
                                                    cVParams.size(), "" + hyperscore));

                                            if (upstreamFlankingSequence != null) {
                                                cVParams.add(new CvParamImpl("PRIDE:0000065", "PRIDE",
                                                        "Upstream flanking sequence", cVParams.size(),
                                                        "" + upstreamFlankingSequence.toUpperCase()));
                                            }
                                            if (downstreamFlankingSequence != null) {
                                                cVParams.add(new CvParamImpl("PRIDE:0000066", "PRIDE",
                                                        "Downstream flanking sequence", cVParams.size(),
                                                        "" + downstreamFlankingSequence.toUpperCase()));
                                            }

                                            if (PRIDEConverter.getProperties().getSampleDescriptionCVParamsQuantification().size() > 0) {
                                                PRIDEConverter.addItraqCVTerms(cVParams, iTraqNorm);
                                                userParams = PRIDEConverter.addItraqUserTerms(iTraqRatio);
                                            } else {
                                                userParams = null;
                                            }

                                            if (hyperscore >= PRIDEConverter.getProperties().getPeptideScoreThreshold()) {

                                                PRIDEConverter.getIds().add(new IdentificationGeneral(
                                                        PRIDEConverter.getSpectrumKey(), //spectrumFileID
                                                        label, //accession
                                                        "X!Tandem", //search engine
                                                        null, //database
                                                        null, //database version
                                                        peptideSequence.toUpperCase(), //sequence
                                                        start, //start
                                                        hyperscore, //score
                                                        null, //threshold
                                                        iTraqNorm, iTraqUT, iTraqRatio, // iTRAQ values
                                                        cVParams, // identification cv params
                                                        userParams, // identification user params
                                                        peptideModifications, // list of aa modifications
                                                        fragmentIons)); // list of fragment ions
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(null, "The file named " +
                        PRIDEConverter.getProperties().getSelectedIdentificationFiles().get(j) +
                        "\ncould not be found.",
                        "File Not Found", JOptionPane.ERROR_MESSAGE);
                Util.writeToErrorLog("Error when reading X!Tandem file: ");
                ex.printStackTrace();
            } catch (Exception e) {

                Util.writeToErrorLog("Error parsing X!Tandem file: ");
                e.printStackTrace();

                JOptionPane.showMessageDialog(null,
                        "The following file could not parsed as an X!Tandem file:\n " +
                        PRIDEConverter.getProperties().getSelectedIdentificationFiles().get(j) +
                        "\n\n" +
                        "See ../Properties/ErrorLog.txt for more details.",
                        "Error Parsing File", JOptionPane.ERROR_MESSAGE);
            }
        }

        return mapping;
    }
}
