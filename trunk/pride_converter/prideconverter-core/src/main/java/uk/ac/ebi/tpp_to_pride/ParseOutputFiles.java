/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 21-Aug-2006
 * Time: 12:15:41
 */
package uk.ac.ebi.tpp_to_pride;

import org.apache.log4j.Logger;
import org.systemsbiology.jrap.MSInstrumentInfo;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import uk.ac.ebi.tpp_to_pride.parsers.PeptideProphetXMLParser;
import uk.ac.ebi.tpp_to_pride.parsers.ProteinProphetXMLParser;
import uk.ac.ebi.tpp_to_pride.parsers.XmlPullParserPlus;
import uk.ac.ebi.tpp_to_pride.wrappers.peptideprophet.*;
import uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet.ProteinProphetIsoform;
import uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet.ProteinProphetProteinID;
import uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet.ProteinProphetSummary;
import uk.ac.ebi.pride.model.implementation.core.*;
import uk.ac.ebi.pride.model.implementation.mzData.*;
import uk.ac.ebi.pride.model.interfaces.mzdata.Analyzer;
import uk.ac.ebi.pride.model.interfaces.mzdata.MzData;
import uk.ac.ebi.pride.xml.XMLMarshaller;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class reads the ProteinProphet and PeptideProphet output files,
 * and assembles them into an object model.
 *
 * @author martlenn
 * @version $Id: ParseOutputFiles.java,v 1.1.1.1 2007/01/12 17:17:10 lmartens Exp $
 * Modified by Harald Barsnes (December 2008)
 */
public class ParseOutputFiles {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named "ParseOutputFiles_Harald".
     */
    private static Logger logger = Logger.getLogger(ParseOutputFiles.class);
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    private static double pepProphetThreshold = 0.9;
    private static double protProphetThreshold = 0.9;
    private static final String MZDATA_CONTACT_LIST = "MZDATA_CONTACT_LIST";
    private static final String MZDATA_SOURCE_CV_ACCESSION = "MZDATA_SOURCE_CV_ACCESSION";
    private static final String MZDATA_SOURCE_CV_NAME = "MZDATA_SOURCE_CV_NAME";
    private static final String MZDATA_DETECTOR_CV_ACCESSION = "MZDATA_DETECTOR_CV_ACCESSION";
    private static final String MZDATA_DETECTOR_CV_NAME = "MZDATA_DETECTOR_CV_NAME";
    private static final String MZDATA_ANALYZERLIST_CV_ACCESSIONS = "MZDATA_ANALYZERLIST_CV_ACCESSIONS";
    private static final String MZDATA_ANALYZERLIST_CV_NAMES = "MZDATA_ANALYZERLIST_CV_NAMES";
    private static final String MZDATA_SAMPLE = "MZDATA_SAMPLE";
    private static final String MZDATA_SAMPLE_DESCRIPTION = "MZDATA_SAMPLE_DESCRIPTION";
    private static final String REFERENCES = "REFERENCES";
    private static final String PROTOCOL_NAME = "PROTOCOL_NAME";
    private static final String PROJECT_TITLE = "PROJECT_TITLE";
    private static final String PROJECT_SHORT_TITLE = "PROJECT_SHORT_TITLE";
    private static final String PROJECT_CV_PARAMETER = "PROJECT_CV_PARAMETER";
    private static final String OUTPUT_FILE = "OUTPUT_FILE";

    /**
     * The main method is the entry point for the application.
     *
     * @param args String[] with the start-up arguments.
     */
    public static void main(String[] args) {

        //long startTimer = System.currentTimeMillis();

        // Activity log.
        logger.debug("Application started at " + dateTimeFormat.format(new Date()) + ".");

        // Check the start-up args.
        if (args == null || args.length != 4) {
            printUsage();
        }

        // Check for the existence of the inputfiles.
        File peptidesFile = new File(args[0]);
        if (!peptidesFile.exists()) {
            printError("Unable to read the PeptideProphet output file you specified ('" + args[0] + "')!");
        }

        File proteinsFile = new File(args[1]);
        if (!proteinsFile.exists()) {
            printError("Unable to read the ProteinProphet output file you specified ('" + args[1] + "')!");
        }

        File spectraFolder = new File(args[2]);
        if (!spectraFolder.exists()) {
            printError("Unable to locate the mzXML input folder you specified ('" + args[2] + "')!");
        }

        File propertiesfile = new File(args[3]);
        if (!propertiesfile.exists()) {
            printError("Unable to read the project properties file you specified ('" + args[3] + "')!");
        }

        logger.debug("Checked input files (" + dateTimeFormat.format(new Date()) + ").");

        // OK, we have valid input files.

        // Load the project properties file.
        Properties projectProperties = new Properties();
        try {
            InputStream is = new FileInputStream(propertiesfile.getCanonicalPath());
            if (is != null) {
                projectProperties.load(is);
            } else {
                throw new IOException("Properties file '" + propertiesfile.getCanonicalPath() + "' not found!");
            }
            logger.info("Loaded properties from '" + propertiesfile.getCanonicalPath() + "'.");
            is.close();
        } catch (Exception e) {
            logger.error("Unable to load properties file '" + propertiesfile.getAbsolutePath() + "': " + e.getMessage());
        }

        // Start reading the files!
        XmlPullParserFactory factory = null;
        XmlPullParser xpp = null;
        String currentFile = null;
        try {
            factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();
            logger.debug("XML Pull Parser created (" + dateTimeFormat.format(new Date()) + ").");

            // First build a HashMap of all the peptides identified by PeptideProphet.
            BufferedReader br = new BufferedReader(new FileReader(peptidesFile));
            xpp.setInput(br);
            XmlPullParserPlus xppp = new XmlPullParserPlus(xpp);
            currentFile = peptidesFile.getAbsolutePath();

            // check if the input files are mzXML or MGF
            File[] spectraFiles = spectraFolder.listFiles();

            boolean spectraAreMzXml = true;

            for (int i = 0; i < spectraFiles.length; i++) {
                if (spectraFiles[i].getName().toLowerCase().endsWith("mgf")) {
                    spectraAreMzXml = false;
                }
            }

            PeptideProphetXMLParser ppxp = new PeptideProphetXMLParser(spectraFolder, true);
            logger.debug("Parsing PeptideProphet file (" + dateTimeFormat.format(new Date()) + ")...");
            HashMap runs = ppxp.readPeptideProphet(xppp, pepProphetThreshold);
            br.close();
            logger.debug("PeptideProphet file parsing complete (" + dateTimeFormat.format(new Date()) + ").");

            // Now build all proteins from ProteinProphet, associating them to the peptides.
            br = new BufferedReader(new FileReader(proteinsFile));
            xpp.setInput(br);
            currentFile = proteinsFile.getAbsolutePath();
            ProteinProphetXMLParser ppp = new ProteinProphetXMLParser();
            logger.debug("Parsing ProteinProphet file (" + dateTimeFormat.format(new Date()) + ")...");
            Collection proteins = ppp.readProteinProphet(xppp, protProphetThreshold);
            ProteinProphetSummary ppSummary = ppp.getProteinProphetSummary();
            br.close();
            logger.debug("ProteinProphet file parsing complete (" + dateTimeFormat.format(new Date()) + ").");

            logger.debug("Creating protein lookup table (" + dateTimeFormat.format(new Date()) + ")...");

            // Create a HashMap of proteins for quick retrieval.
            HashMap forProteinSequences = new HashMap();

            for (Iterator lIterator = proteins.iterator(); lIterator.hasNext();) {
                ProteinProphetProteinID protein = (ProteinProphetProteinID) lIterator.next();
                forProteinSequences.put(protein.getAccession(), protein);
            }

            logger.debug("Created protein lookup table (" + dateTimeFormat.format(new Date()) + ").");

            // Key all queries by the peptide found in them as well
            // (again, charge + ' ' + modified sequence)
            // Also make a list of the identified scans per run (per mzXML file).
            logger.debug("Creating peptide lookup tables (" + dateTimeFormat.format(new Date()) + ")...");

            HashMap peptidesToQueries = new HashMap();
            Iterator iter = runs.values().iterator();

            while (iter.hasNext()) {
                PeptideProphetMSMSRun lRun = (PeptideProphetMSMSRun) iter.next();
                Iterator innerIt = lRun.getQueries().iterator();
                while (innerIt.hasNext()) {
                    PeptideProphetQuery lQuery = (PeptideProphetQuery) innerIt.next();
                    PeptideProphetSearchHit ppsh = lQuery.getSearchHit();
                    String key = ppsh.getSequence();
                    // Create the peptide (modseq + charge) to query map.
                    Collection queries = null;
                    if (peptidesToQueries.containsKey(key)) {
                        queries = (Collection) peptidesToQueries.get(key);
                    } else {
                        queries = new ArrayList();
                        peptidesToQueries.put(key, queries);
                    }
                    queries.add(lQuery);
                }
            }
            logger.debug("Created peptide and identified queries lookup tables (" + dateTimeFormat.format(new Date()) + ").");

            // Now we have the proteins and the peptides in their LC runs.
            // Create PRIDE protein objects.
            logger.debug("Creating PRIDE protein objects (" + dateTimeFormat.format(new Date()) + ")...");
            Collection PRIDE_proteins = new ArrayList();
            HashMap runAndScanToSpectrumID = new HashMap();
            List mzDataSpectra = new ArrayList();
            // Load all mzXML spectra.
            ppxp.addAllSpectra(mzDataSpectra, runAndScanToSpectrumID);

            for (Iterator lIterator = proteins.iterator(); lIterator.hasNext();) {
                ProteinProphetProteinID protein = (ProteinProphetProteinID) lIterator.next();
                // Report proteins without a sequence.
                String protSequence = protein.getSequence();
                if (protSequence == null) {
                    logger.info("Protein '" + protein.getAccession() + "' did not have an associated sequence.");
                }

                Collection PRIDE_peptides = new ArrayList();
                HashMap peptides = protein.getPeptides();
                Iterator innerIt = peptides.keySet().iterator();
                while (innerIt.hasNext()) {
                    String chargeModSeq = (String) innerIt.next();
                    // Find the corresponding queries.
                    Collection queries = (Collection) peptidesToQueries.get(chargeModSeq);
                    // Sanity check.
                    if (queries == null) {
                        logger.error("Could not find any queries for peptide '" + chargeModSeq +
                                "', assigned to protein '" + protein.getAccession() + "'!");
                    } else {
                        // Collect all peptide info for each query.
                        for (Iterator lIterator1 = queries.iterator(); lIterator1.hasNext();) {
                            PeptideProphetQuery lQuery = (PeptideProphetQuery) lIterator1.next();
                            PeptideProphetSearchHit hit = lQuery.getSearchHit();
                            // Create the peptide (modseq + charge) to query map.
                            // Alright, so since this query is identified and linked to an identified protein,
                            // the corresponding spectrum (and its parent(s)) needs to be retrieved and
                            // the spectrumidentifier should be entered in the lookup HashMap if it is novel.
                            String runName = lQuery.getRun();
                            int id = -1;

                            // First check if we have already encountered this spectrum.
                            String spectrumKey;

                            // have to be handled differently for mzXML and mgf files
                            if (lQuery.getSpectrumTitle() != null) { // MGF
                                spectrumKey = runName + " " + lQuery.getSpectrumTitle();
                            } else { // mzXML
                                spectrumKey = runName + " " + lQuery.getStartScan();
                            }

                            if (runAndScanToSpectrumID.containsKey(spectrumKey)) {
                                id = ((Integer) runAndScanToSpectrumID.get(spectrumKey)).intValue();
                            } else {
                                System.out.println("Unable to find pre-parsed spectrum for key '" + spectrumKey + "'!");
                                logger.warn("Unable to find pre-parsed spectrum for key '" + spectrumKey + "'!");
                                id = ppxp.addMzSpectrumForSpecifiedScan(runName, Integer.parseInt(lQuery.getStartScan()), mzDataSpectra);
                                // It is a new ID. Add it to the runAndScanToSpectrumID HashMap.
                                runAndScanToSpectrumID.put(spectrumKey, new Integer(id));
                            }

                            // Sanity check on the number of scans for this query.
                            if (!lQuery.getStartScan().equals(lQuery.getEndScan())) {
                                logger.warn("Query '" + lQuery.getRun() + " (" + lQuery.getStartScan() + " - " +
                                        lQuery.getEndScan() + ")" + "' consists of more than one scan!");
                            }

                            Long specRef = new Long(id);
                            // Get the hit sequence.
                            String pepSequence = hit.getSequence();
                            // Determine start and stop location if possible.
                            Integer pepStart = null;
                            if (protSequence != null) {
                                int start = protSequence.indexOf(pepSequence);
                                if (start >= 0) {
                                    start += 1;
                                    pepStart = new Integer(start);
                                } else {
                                    logger.error("Could not find start position of '" + pepSequence + "' in '"
                                            + protein.getAccession() + "' (protein sequence '" + protSequence + "')!");
                                }
                            } else {
                                logger.info("Unable to determine start and stop position of '" + pepSequence + "' in '"
                                        + protein.getAccession() + "' because no protein sequence was found.");
                            }

                            Collection PRIDE_modifications = null;

                            if (hit.getModifiedAminoAcids() != null && hit.getModifiedAminoAcids().size() > 0) {
                                // Do modification stuff.
                                Collection foundMods = hit.getModifiedAminoAcids();
                                PRIDE_modifications = new ArrayList(foundMods.size());
                                // Get the ms/ms run - the modifications are listed there.
                                PeptideProphetMSMSRun run = (PeptideProphetMSMSRun) runs.get(lQuery.getRun());
                                PeptideProphetSearch search_info = run.getSearch_info();
                                // The modifications are keyed by their symbol
                                // (which is not used anywhere else in the XML files).
                                HashMap modificationMap = search_info.getModifications();
                                // Cycle across all modifications to create a lookup table based on the mass
                                // and the amino acid affected.
                                Iterator modIter = modificationMap.values().iterator();
                                HashMap modificationByMass = new HashMap(modificationMap.size());
                                while (modIter.hasNext()) {
                                    PeptideProphetModification mod = (PeptideProphetModification) modIter.next();
                                    // Now add the mass as key and the modification as value to the lookup table.
                                    // Please note that we check for uniqueness of the key. If the masses are not unique,
                                    // issue an error.
                                    Object duplicate = modificationByMass.put(mod.getMass() + "_" + mod.getAminoacid(), mod);
                                    if (duplicate != null) {
                                        logger.error("Modifications with non-unique combination of mass and amino acid ("
                                                + mod.getMass() + " " + mod.getAminoacid() + ") found!");
                                    }
                                }

                                // Now cycle through the found modifications and annotate them in PRIDE style
                                // using OLS!
                                for (Iterator lIterator2 = foundMods.iterator(); lIterator2.hasNext();) {
                                    PeptideProphetModifiedAminoAcid ppma = (PeptideProphetModifiedAminoAcid) lIterator2.next();
                                    double mass = ppma.getMass();
                                    char residue = pepSequence.charAt(ppma.getPosition() - 1);
                                    String key = mass + "_" + residue;
                                    // Now look this up; precision differs between the definitions however :(,
                                    // so we have to find the correct result.
                                    while (modificationByMass.get(key) == null) {
                                        BigDecimal bd = new BigDecimal(Double.toString(mass));
                                        // If we round ALL decimals, something is horribly wrong!
                                        bd = bd.setScale(bd.scale() - 1, BigDecimal.ROUND_HALF_UP);
                                        if (bd.scale() == 0) {
                                            logger.error("Rounded down modification mass for '" + ppma.getMass() + "_" + residue + "' down to '" + bd.doubleValue() + "' without finding a match!");
                                            break;
                                        }
                                        mass = bd.doubleValue();
                                        key = mass + "_" + residue;
                                    }
                                    // Okay, we can now try to retrieve the modification.
                                    PeptideProphetModification modInfo = (PeptideProphetModification) modificationByMass.get(key);

                                    // Locate the modification using the properties.
                                    String modification = projectProperties.getProperty(key);
                                    // Split the String into the accession and the name.
                                    String modAccession = modification.substring(0, modification.indexOf(";"));
                                    String modName = modification.substring(modification.indexOf(";") + 1);
                                    Collection modMonoDeltas = new ArrayList();
                                    modMonoDeltas.add(new MonoMassDeltaImpl(modInfo.getMassDiff()));
                                    Collection modCVParams = new ArrayList();
                                    modCVParams.add(new CvParamImpl(modAccession, "PSI-MOD", modName, 0, null));
                                    PRIDE_modifications.add(new ModificationImpl(modAccession, new Integer(ppma.getPosition()), "PSI-MOD", "1.0", modMonoDeltas, null, modCVParams, null));
                                }
                            }

                            Collection additionalParams = new ArrayList();
                            // Add the PeptideProphet probability.
                            additionalParams.add(new CvParamImpl("PRIDE:0000099", "PRIDE", "PeptideProphet probability score", 0, hit.getProbability() + ""));
                            // Retrieval of the constituent scores.
                            String dotproduct = (String) hit.getSearchScores().get("dotproduct");
                            String delta = (String) hit.getSearchScores().get("delta");
                            String deltastar = (String) hit.getSearchScores().get("deltastar");
                            String zscore = (String) hit.getSearchScores().get("zscore");
                            String expect = (String) hit.getSearchScores().get("expect");

                            // Only add the parameter if it can be found.
                            // Since these should be found, signal all missing
                            // params as a logger warning!
                            if (dotproduct != null) {
                                additionalParams.add(new CvParamImpl("PRIDE:0000179", "PRIDE", "dotproduct", 1, dotproduct));
                            } else {
                                logger.warn("No dotproduct score found for peptide " + hit.getModifiedSequence());
                            }
                            if (delta != null) {
                                additionalParams.add(new CvParamImpl("PRIDE:0000180", "PRIDE", "delta", 2, delta));
                            } else {
                                logger.warn("No delta found for peptide " + hit.getModifiedSequence());
                            }
                            if (deltastar != null) {
                                additionalParams.add(new CvParamImpl("PRIDE:0000181", "PRIDE", "deltastar", 3, deltastar));
                            } else {
                                logger.warn("No deltastar found for peptide " + hit.getModifiedSequence());
                            }
                            if (zscore != null) {
                                additionalParams.add(new CvParamImpl("PRIDE:0000182", "PRIDE", "zscore", 4, zscore));
                            } else {
                                logger.warn("No zscore score found for peptide " + hit.getModifiedSequence());
                            }
                            if (expect != null) {
                                additionalParams.add(new CvParamImpl("PRIDE:0000183", "PRIDE", "expect", 5, expect));
                            } else {
                                logger.warn("No expect found for peptide " + hit.getModifiedSequence());
                            }

                            // Create the peptide.
                            PRIDE_peptides.add(new PeptideImpl(specRef, pepSequence, pepStart, PRIDE_modifications, additionalParams, null));
                            // Data integrity check.
                            if (hit.getRoundedProbability() < pepProphetThreshold) {
                                logger.warn("Output peptide '" + chargeModSeq + "' with a PeptideProphet probability lower than 0.9: " + hit.getProbability() + ".");
                            }
                        }
                    }
                }

                // Adding protein annotations.
                Collection additionalCVParams = new ArrayList();
                if (protSequence != null) {
                    additionalCVParams.add(new CvParamImpl("PRIDE:0000041", "PRIDE", "Search database protein sequence", -1, protein.getSequence()));
                }

                if (protein.getDescription() != null && !protein.getDescription().trim().equals("")) {
                    additionalCVParams.add(new CvParamImpl("PRIDE:0000063", "PRIDE", "Protein description line", -1, protein.getDescription()));
                }

                additionalCVParams.add(new CvParamImpl("PRIDE:0000100", "PRIDE", "ProteinProphet probability score", -1, protein.getProbability() + ""));
                Collection isoforms = protein.getIsoforms();

                if (isoforms != null && !isoforms.isEmpty()) {
                    for (Iterator lIterator1 = isoforms.iterator(); lIterator1.hasNext();) {
                        ProteinProphetIsoform isoform = (ProteinProphetIsoform) lIterator1.next();
                        additionalCVParams.add(new CvParamImpl("PRIDE:0000098", "PRIDE", "Indistinguishable alternative protein accession", -1, isoform.getAccession()));
                    }
                }

                GelFreeIdentificationImpl PRIDE_protein = new GelFreeIdentificationImpl(protein.getAccession(), protein.getVersion(), null, ppSummary.getSourceDatabase(), PRIDE_peptides, additionalCVParams, null, ppSummary.getSoftware(), ppSummary.getDbVersion(), new Double(protein.getPercent_coverage() / 100), new Double(protein.getProbability()), new Double(0.9), null);
                PRIDE_proteins.add(PRIDE_protein);
            }
            logger.debug("Created PRIDE protein objects (" + dateTimeFormat.format(new Date()) + ").");

            // Create contacts.
            Collection contacts = new ArrayList();
            // @TODO Check contacts.
            String[] contactStrings = projectProperties.getProperty(MZDATA_CONTACT_LIST).split("\\[\\*\\]");
            if (contactStrings == null || contactStrings.length == 0) {
                logger.error("No contact details found in the project properties file ('" + propertiesfile.getAbsolutePath() + "')!");
            }

            // Add each contact.
            for (int i = 0; i < contactStrings.length; i++) {
                // Each contact string consists of three parts.
                String lContactString = contactStrings[i];
                String[] contactDetails = lContactString.split("\\[@\\]");
                if (contactDetails.length != 3) {
                    logger.error("Invalid contact details found in '" + propertiesfile.getAbsolutePath() + "': expected 3 parts, but found " + contactDetails.length + " (contact was: " + lContactString + ")!");
                }
                contacts.add(new ContactImpl(contactDetails[0], contactDetails[1], contactDetails[2]));
            }

            // Process instrument info.

            // @TODO for MGF files all instrument details has to be extracted from the projectProperties file

            MSInstrumentInfo instrumentInfo = null;

            String instrumentName = "unknown";
            String ionization = "unknown";

            if (spectraAreMzXml) {
                instrumentInfo = ppxp.getInstrumentInfo();
                // Instrument name.
                instrumentName = instrumentInfo.getManufacturer() + " " + instrumentInfo.getModel();
                // Instrument source.
                ionization = instrumentInfo.getIonization();
            } else {
                // MGF files
                // @TODO extract from projectProperties file
            }

            Collection instrumentSourceCVParams = new ArrayList();
            // @TODO  Check instrument source stuff.
            instrumentSourceCVParams.add(new CvParamImpl(projectProperties.getProperty(MZDATA_SOURCE_CV_ACCESSION), "PSI", projectProperties.getProperty(MZDATA_SOURCE_CV_NAME), 0, null));
            Collection instrumentSourceUserParams = new ArrayList(1);
            instrumentSourceUserParams.add(new UserParamImpl("Original mzXML instrument ionisation description", 0, ionization));

            String detector = "unknown";

            // Instrument detector.
            if (spectraAreMzXml) {
                detector = instrumentInfo.getDetector();
            } else {
                // MGF files
                // @TODO extract from projectProperties file
            }

            Collection instrumentDetectorCVParams = new ArrayList();
            // @TODO  Check instrument detector stuff.
            instrumentDetectorCVParams.add(new CvParamImpl(projectProperties.getProperty(MZDATA_DETECTOR_CV_ACCESSION), "PSI", projectProperties.getProperty(MZDATA_DETECTOR_CV_NAME), 0, null));
            Collection instrumentDetectorUserParams = new ArrayList();
            instrumentDetectorUserParams.add(new UserParamImpl("Original mzXML instrument detector description", 0, detector));

            // Instrument analyzer
            String analyzer = "unknown";

            // Instrument analyzer
            if (spectraAreMzXml) {
                analyzer = instrumentInfo.getMassAnalyzer();
            } else {
                // MGF files
                // @TODO extract from projectProperties file
            }

            // @TODO  Check instrument analyzer list stuff.
            String[] analyzerAccessions = projectProperties.getProperty(MZDATA_ANALYZERLIST_CV_ACCESSIONS).split("\\[\\*\\]");
            String[] analyzerNames = projectProperties.getProperty(MZDATA_ANALYZERLIST_CV_NAMES).split("\\[\\*\\]");

            if (analyzerAccessions.length != analyzerNames.length) {
                logger.error("Mismatch in the number of analyzers when comparing accessions (" + analyzerAccessions.length + ") with names (" + analyzerNames.length + ")!");
            }

            Collection analyzerList = new ArrayList(analyzerAccessions.length);
            for (int i = 0; i < analyzerAccessions.length; i++) {
                Collection analyzerCVParams = new ArrayList(1);
                analyzerCVParams.add(new CvParamImpl(analyzerAccessions[i], "PSI", analyzerNames[i], i, null));
                Collection analyzerUserParams = new ArrayList();
                analyzerUserParams.add(new UserParamImpl("Original mzXML instrument analyzer description", 0, analyzer));
                Analyzer mzDataAnalyzer = new AnalyzerImpl(analyzerCVParams, analyzerUserParams, 0);
                analyzerList.add(mzDataAnalyzer);
            }

            // Sample name.
            // @TODO Check sample.
            String sample = projectProperties.getProperty(MZDATA_SAMPLE);

            // Sample description params.
            Collection sampleDescriptionParams = new ArrayList();
            String[] sampleDescriptors = projectProperties.getProperty(MZDATA_SAMPLE_DESCRIPTION).split("\\[\\*\\]");
            for (int i = 0; i < sampleDescriptors.length; i++) {
                String lSampleDescriptor = sampleDescriptors[i];
                String[] term = lSampleDescriptor.split("\\[@\\]");
                if (term.length != 3) {
                    logger.error("Invalid sample descriptor details found in '" + propertiesfile.getAbsolutePath() + "': expected 3 parts, but found " + term.length + " (descriptor was: " + lSampleDescriptor + ")!");
                }
                sampleDescriptionParams.add(new CvParamImpl(term[1], term[0], term[2], i, null));
            }

            // CV lookup stuff.
            Collection cvLookups = new ArrayList(1);
            // @TODO  Check CV lookups.
            cvLookups.add(new CVLookupImpl("1.0.0", "The PSI Ontology", "PSI", "http://psidev.sourceforge.net/ontology/"));
            cvLookups.add(new CVLookupImpl("2006-07-07", "The NEWT Ontology", "NEWT", "http://www.ebi.ac.uk/newt/"));
            cvLookups.add(new CVLookupImpl("1.0", "The PRIDE Ontology", "PRIDE", "http://www.ebi.ac.uk/pride"));

            // @TODO for MGF files software details has to be extracted from the projectProperties file

            // softwareName
            String softwareName = "unknown";

            if (spectraAreMzXml) {
                softwareName = instrumentInfo.getSoftwareInfo().name;
            } else {
                // MGF files
                // @TODO extract from projectProperties file
            }

            // softwareVersion
            String softwareVersion = "unknown";

            if (spectraAreMzXml) {
                softwareVersion = instrumentInfo.getSoftwareInfo().version;
            } else {
                // MGF files
                // @TODO extract from projectProperties file
            }

            // Create mzData instance.
            MzData mzData = new MzDataImpl(mzDataSpectra, null, contacts, instrumentName, null, null, null, sample,
                    null, null, cvLookups, "1.05", instrumentDetectorCVParams, instrumentDetectorUserParams, null,
                    "1", analyzerList, null, instrumentSourceCVParams, instrumentSourceUserParams, softwareVersion,
                    sampleDescriptionParams, null, softwareName);

            // Create references.
            Collection references = new ArrayList();
            // @TODO  Check references.
            // Get all the references.
            String[] referenceStrings = projectProperties.getProperty(REFERENCES).split("\\[\\*\\]");
            // Go through the references.
            for (int i = 0; i < referenceStrings.length; i++) {
                // The reference String consists of two parts. A pubmed identifier and a reference proper.
                // The pubmed id can be absent.
                String lReferenceString = referenceStrings[i];
                String[] referenceComponents = lReferenceString.split("\\[@\\]");
                Collection referenceCvParams = null;
                // If there is a pubmed ID, add it.
                int indexRefLine = 0;
                if (referenceComponents.length == 2) {
                    referenceCvParams = new ArrayList();
                    referenceCvParams.add(new CvParamImpl(referenceComponents[0], "PubMed", referenceComponents[0], 0, null));
                    indexRefLine++;
                }
                references.add(new ReferenceImpl(referenceComponents[indexRefLine], referenceCvParams, null));
            }

            // Protocol name.
            // @TODO  Check protocol name.
            String protocolName = projectProperties.getProperty(PROTOCOL_NAME);
            // Create protocol steps.
            Collection protocolSteps = new ArrayList();
            // @TODO  Check protocolsteps.

            // @TODO  Check experiment title.
            String title = projectProperties.getProperty(PROJECT_TITLE);

            // @TODO  Check experiment short title.
            String shortTitle = projectProperties.getProperty(PROJECT_SHORT_TITLE);

            // Experiment CV parameters.
            Collection experimentCvParams = new ArrayList();
            String project_cv_param = projectProperties.getProperty(PROJECT_CV_PARAMETER);
            if (project_cv_param != null && !project_cv_param.trim().equals("")) {
                experimentCvParams.add(new CvParamImpl("PRIDE:0000097", "PRIDE", "Project", 0, project_cv_param));
            }

            // Create a PRIDE experiment.
            ExperimentImpl experiment = new ExperimentImpl(title, references, shortTitle, protocolSteps,
                    PRIDE_proteins, protocolName, mzData, experimentCvParams, null);
            Collection experiments = new ArrayList(1);
            experiments.add(experiment);
            // Output file.
            // @TODO  Check output file.
            // Marshall experiments in chunks.
            String outputFile = "D:\\PRIDE_ms_lims\\Data\\TPP\\tppResult.xml";
            //projectProperties.getProperty(OUTPUT_FILE) + peptidesFile.getName().substring(0, peptidesFile.getName().indexOf(".pep.xml")) + ".prideXML";
            XMLMarshaller marshaller = new XMLMarshaller(true);
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
            marshaller.marshallExperiments(experiments, bw);
            bw.flush();
            bw.close();
        } catch (Exception e) {
            // Not good. Print line and dump stacktrace.
            if (factory == null) {
                logger.error(" *** Unable to create XmlPullParserFactory!");
            } else if (xpp == null) {
                logger.error(" *** Unable to create XmlPullParser!");
            } else {
                logger.error(" *** Error parsing line " + xpp.getLineNumber() + " in file '" + currentFile + "'!");
            }
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }

//        long end = System.currentTimeMillis();
//        System.out.println("Done: " + (end - startTimer) + "\n");
    }

    /**
     * This method prints the usage information for this class to the standard
     * error stream and exits with the error flag raised to '1'.
     */
    private static void printUsage() {
        printError("Usage:\n\n\t" +
                "ParseOutputFiles <PeptideProphet_output_file> <ProteinProphet_output_file> <mzXML_files_input_folder> <project_properties_configuration_file>");
    }

    /**
     * This method prints two blank lines followed by the the specified error message and another two empty lines
     * to the standard error stream and exits with the error flag raised to '1'.
     *
     * @param aMsg String with the message to print.
     */
    private static void printError(String aMsg) {
        System.err.println("\n\n" + aMsg + "\n\n");
        System.exit(1);
    }
}
