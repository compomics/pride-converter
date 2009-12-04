package no.uib.prideconverter;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.math.BigDecimal;

import uk.ac.ebi.tpp_to_pride.parsers.XmlPullParserPlus;
import uk.ac.ebi.tpp_to_pride.parsers.PeptideProphetXMLParser;
import uk.ac.ebi.tpp_to_pride.parsers.ProteinProphetXMLParser;
import uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet.ProteinProphetSummary;
import uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet.ProteinProphetProteinID;
import uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet.ProteinProphetIsoform;
import uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet.ProteinProphetPeptideID;
import uk.ac.ebi.tpp_to_pride.wrappers.peptideprophet.*;
import uk.ac.ebi.pride.model.implementation.mzData.CvParamImpl;
import uk.ac.ebi.pride.model.implementation.mzData.CVLookupImpl;
import uk.ac.ebi.pride.model.implementation.mzData.UserParamImpl;
import uk.ac.ebi.pride.model.implementation.mzData.MzDataImpl;
import uk.ac.ebi.pride.model.implementation.core.*;
import uk.ac.ebi.pride.model.interfaces.mzdata.Spectrum;
import uk.ac.ebi.pride.model.interfaces.mzdata.CvParam;
import uk.ac.ebi.pride.model.interfaces.mzdata.UserParam;
import uk.ac.ebi.pride.model.interfaces.mzdata.CVLookup;
import uk.ac.ebi.pride.model.interfaces.core.MassDelta;
import uk.ac.ebi.pride.model.interfaces.core.Peptide;
import uk.ac.ebi.pride.model.interfaces.core.Modification;
import no.uib.prideconverter.gui.ModificationMapping;
import no.uib.prideconverter.gui.ProgressDialog;
import no.uib.prideconverter.util.Util;

import javax.swing.*;

/**
 * @author Florian Reisinger
 *         Date: 17-Jul-2009
 * @since $version
 */
public class TPPConverter {

    /**
     * This method transforms spectra from TPP files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     * @return a HashMap
     */
    protected static HashMap<String, Long> transformSpectraFromTPPProjects(ArrayList<Spectrum> aTransformedSpectra) {

        // temporary variables
        ProgressDialog progressDialog = PRIDEConverter.getProgressDialog();
        no.uib.prideconverter.util.Properties properties = PRIDEConverter.getProperties();
        no.uib.prideconverter.util.UserProperties userProperties = PRIDEConverter.getUserProperties();
        boolean debug = PRIDEConverter.isDebug();


        progressDialog.setString(null);
        progressDialog.setTitle("Reading PeptideProphet File. Please Wait...");
        progressDialog.setIntermidiate(true);
        progressDialog.setString(null);

        HashMap<String, Long> mapping = new HashMap<String, Long>(); // is not used

        // ToDo: insert a try catch clause to intercept any problems related to ill-formated XML
        // ToDo: insert Juan's code to deal with ill-formated XML

        // Start reading the files!
        XmlPullParserFactory factory = null;
        XmlPullParser xpp = null;

        PRIDEConverter.setTotalPeptideCount(0);

        try {
            factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();

            // First build a HashMap of all the peptides identified by PeptideProphet.
            BufferedReader br = new BufferedReader(new FileReader(properties.getPeptideProphetFileName()));
            xpp.setInput(br);
            XmlPullParserPlus xppp = new XmlPullParserPlus(xpp);
            // currentFile = properties.getPeptideProphetFileName(); // currently not used
            PeptideProphetXMLParser ppxp = new PeptideProphetXMLParser(
                    new File(properties.getSpectrumFilesFolderName()), true);

            HashMap runs = ppxp.readPeptideProphet(xppp, properties.getPeptideProphetThreshold());
            br.close();

            progressDialog.setString(null);
            progressDialog.setTitle("Reading ProteinProphet File. Please Wait...");
            progressDialog.setIntermidiate(true);
            progressDialog.setString(null);

            // Now build all proteins from ProteinProphet, associating them to the peptides.
            br = new BufferedReader(new FileReader(properties.getProteinProphetFileName()));
            xpp.setInput(br);
            // currentFile = properties.getProteinProphetFileName();  // currently not used
            ProteinProphetXMLParser ppp = new ProteinProphetXMLParser();

            Collection proteins = ppp.readProteinProphet(xppp, properties.getProteinProphetThreshold());
            ProteinProphetSummary ppSummary = ppp.getProteinProphetSummary();
            br.close();

            progressDialog.setString(null);
            progressDialog.setTitle("Creating Protein Map. Please Wait...");
            progressDialog.setIntermidiate(true);
            progressDialog.setString(null);

            // Key all queries by the peptide found in them as well
            // (again, charge + ' ' + modified sequence)
            // Also make a list of the identified scans per run (per file).
            HashMap<String, Collection<PeptideProphetQuery>> peptidesToQueries = new HashMap<String, Collection<PeptideProphetQuery>>();

            Iterator iter = runs.values().iterator();

            progressDialog.setString(null);
            progressDialog.setTitle("Creating Lookup Tables. Please Wait...");
            progressDialog.setIntermidiate(true);
            progressDialog.setString(null);

            while (iter.hasNext() && !PRIDEConverter.isConversionCanceled()) {
                PeptideProphetMSMSRun lRun = (PeptideProphetMSMSRun) iter.next();

                Iterator innerIt = lRun.getQueries().iterator();

                while (innerIt.hasNext() && !PRIDEConverter.isConversionCanceled()) {
                    PeptideProphetQuery lQuery = (PeptideProphetQuery) innerIt.next();
                    PeptideProphetSearchHit ppsh = lQuery.getSearchHit();

                    String key = ppsh.getSequence();

                    // Create the peptide (modseq + charge) to query map.
                    Collection<PeptideProphetQuery> queries = null;

                    if (peptidesToQueries.containsKey(key)) {
                        queries = peptidesToQueries.get(key);
                    } else {
                        queries = new ArrayList<PeptideProphetQuery>();
                        peptidesToQueries.put(key, queries);
                    }

                    queries.add(lQuery);
                }
            }

            // Now we have the proteins and the peptides in their LC runs.
            // Create PRIDE protein objects.
            PRIDEConverter.setIdentifications(new ArrayList<uk.ac.ebi.pride.model.interfaces.core.Identification>());
            HashMap<String, Integer> runAndScanToSpectrumID = new HashMap<String, Integer>();
            List<Spectrum> mzDataSpectra = new ArrayList<Spectrum>();

            progressDialog.setString(null);
            progressDialog.setTitle("Transforming Spectra. Please Wait...");
            progressDialog.setIntermidiate(true);
            progressDialog.setString(null);

            // Load all spectra.
            ppxp.addAllSpectra(mzDataSpectra, runAndScanToSpectrumID, progressDialog);

            progressDialog.setString(null);
            progressDialog.setTitle("Extracting Identifications. Please Wait...");
            progressDialog.setIntermidiate(false);
            progressDialog.setValue(0);
            progressDialog.setMax(proteins.size());

            int counter = 0;

            for (Iterator lIterator = proteins.iterator(); lIterator.hasNext() && !PRIDEConverter.isConversionCanceled();) {

                progressDialog.setValue(counter++);

                ProteinProphetProteinID protein = (ProteinProphetProteinID) lIterator.next();

                // Report proteins without a sequence.
                String protSequence = protein.getSequence();
                if (protSequence == null) {
                    if (debug) {
                        System.out.println("Protein '" + protein.getAccession() + "' did not have an associated sequence.");
                    }
                }

                Collection<Peptide> PRIDE_peptides = new ArrayList<Peptide>();
                HashMap <String, ProteinProphetPeptideID > peptides = protein.getPeptides();

                Iterator innerIt = peptides.keySet().iterator();

                while (innerIt.hasNext() && !PRIDEConverter.isConversionCanceled()) {
                    String chargeModSeq = (String) innerIt.next();

                    // Find the corresponding queries.
                    Collection queries = peptidesToQueries.get(chargeModSeq);

                    // Sanity check.
                    if (queries == null) {
                        if (debug) {
                            System.out.println("Could not find any queries for peptide '" + chargeModSeq +
                                    "', assigned to protein '" + protein.getAccession() + "'!");
                        }
                    } else {
                        // Collect all peptide info for each query.
                        for (Iterator lIterator1 = queries.iterator(); lIterator1.hasNext() && !PRIDEConverter.isConversionCanceled();) {
                            PeptideProphetQuery lQuery = (PeptideProphetQuery) lIterator1.next();
                            PeptideProphetSearchHit hit = lQuery.getSearchHit();

                            // Create the peptide (modseq + charge) to query map.
                            // Alright, so since this query is identified and linked to an identified protein,
                            // the corresponding spectrum (and its parent(s)) needs to be retrieved and
                            // the spectrumidentifier should be entered in the lookup HashMap if it is novel.
                            String runName = lQuery.getRun();

                            int id = -1;

                            // First check if we have already encountered this spectrum.

                            // have to be handled differently for mzXML and mgf files
                            if (lQuery.getSpectrumTitle() != null) { // MGF
                                PRIDEConverter.setSpectrumKey(runName + " " + lQuery.getSpectrumTitle());
                            } else { // mzXML
                                PRIDEConverter.setSpectrumKey(runName + " " + lQuery.getStartScan());
                            }

                            if ( runAndScanToSpectrumID.containsKey( PRIDEConverter.getSpectrumKey() ) ) {
                                id = runAndScanToSpectrumID.get(PRIDEConverter.getSpectrumKey());
                            } else {
                                if (debug) {
                                    System.out.println("Unable to find pre-parsed spectrum for key '" + PRIDEConverter.getSpectrumKey() + "'!");
                                }

                                id = ppxp.addMzSpectrumForSpecifiedScan(runName, Integer.parseInt(lQuery.getStartScan()), mzDataSpectra);
                                // It is a new ID. Add it to the runAndScanToSpectrumID HashMap.
                                runAndScanToSpectrumID.put(PRIDEConverter.getSpectrumKey(), id);
                            }

                            // Sanity check on the number of scans for this query.
                            if (!lQuery.getStartScan().equals(lQuery.getEndScan())) {
                                if (debug) {
                                    System.out.println("Query '" + lQuery.getRun() + " (" + lQuery.getStartScan() +
                                            " - " + lQuery.getEndScan() + ")" + "' consists of more than one scan!");
                                }
                            }

                            Long specRef = (long) id;

                            // Get the hit sequence.
                            String pepSequence = hit.getSequence();

                            // Determine start and stop location if possible.
                            Integer pepStart = null;

                            if (protSequence != null) {

                                int start = protSequence.indexOf(pepSequence);

                                if (start >= 0) {
                                    start += 1;
                                    pepStart = start;
                                } else {
                                    if (debug) {
                                        System.out.println("Could not find start position of '" + pepSequence +
                                                "' in '" + protein.getAccession() + "' (protein sequence '" +
                                                protSequence + "')!");
                                    }
                                }
                            } else {
                                if (debug) {
                                    System.out.println("Unable to determine start and stop position of '" + pepSequence + "' in '" + protein.getAccession() +
                                            "' because no protein sequence was found.");
                                }
                            }

                            Collection<Modification> PRIDE_modifications = null;

                            if (hit.getModifiedAminoAcids() != null && hit.getModifiedAminoAcids().size() > 0) {

                                // Do modification stuff.
                                Collection foundMods = hit.getModifiedAminoAcids();
                                PRIDE_modifications = new ArrayList<Modification>(foundMods.size());

                                // Get the ms/ms run - the modifications are listed there.
                                PeptideProphetMSMSRun run = (PeptideProphetMSMSRun) runs.get(lQuery.getRun());
                                PeptideProphetSearch search_info = run.getSearch_info();

                                // The modifications are keyed by their symbol
                                // (which is not used anywhere else in the XML files).
                                HashMap modificationMap = search_info.getModifications();

                                // Cycle across all modifications to create a lookup table based on the mass
                                // and the amino acid affected.
                                Iterator modIter = modificationMap.values().iterator();
                                HashMap<String, PeptideProphetModification> modificationByMass = new HashMap<String, PeptideProphetModification>(modificationMap.size());

                                while (modIter.hasNext()) {
                                    PeptideProphetModification mod = (PeptideProphetModification) modIter.next();

                                    // Now add the mass as key and the modification as value to the lookup table.
                                    // Please note that we check for uniqueness of the key. If the masses are not unique,
                                    // issue an error.
                                    Object duplicate = modificationByMass.put(mod.getMass() + "_" + mod.getAminoacid(), mod);

                                    if (duplicate != null) {
                                        if (debug) {
                                            System.out.println(
                                                    "Modifications with non-unique combination of mass and amino acid (" + mod.getMass() + " " + mod.getAminoacid() + ") found!");
                                        }
                                    }
                                }

                                // Now cycle through the found modifications and annotate them in PRIDE style using OLS
                                for (Object foundMod : foundMods) {
                                    PeptideProphetModifiedAminoAcid ppma = (PeptideProphetModifiedAminoAcid) foundMod;
                                    double mass = ppma.getMass();
                                    char residue = pepSequence.charAt(ppma.getPosition() - 1);
                                    String key = mass + "_" + residue;

                                    // We have to introduce a temporary mass in order to deal with a bug in the rounding:
                                    Double tempRoundedMass = null;

                                    // Now look this up; precision differs between the definitions however :(,
                                    // so we have to find the correct result.
                                    // We had to introduce the counter as well in order to deal with the rounding problem:
                                    int count_scale =1;
                                    while (modificationByMass.get(key) == null) {
                                        
                                        BigDecimal bd = new BigDecimal(Double.toString(mass));
                                        // If we round ALL decimals, something is horribly wrong!
                                        bd = bd.setScale(bd.scale() - count_scale, BigDecimal.ROUND_HALF_UP);
                                        if (bd.scale() == 0) {
                                            if (debug) {
                                                System.out.println("Rounded down modification mass for '" + ppma.getMass() + "_" + residue + "' down to '" + bd.doubleValue() +
                                                        "' without finding a match!");
                                            }
                                            break;
                                        }
                                        tempRoundedMass = bd.doubleValue();
                                        key = tempRoundedMass + "_" + residue;
                                        count_scale++;
                                    }

                                    // Okay, we can now try to retrieve the modification.
                                    PeptideProphetModification modInfo = modificationByMass.get(key);

                                    // map the modification to the correct PSI-MOD modification using OLS if needed
                                    if (!properties.getAlreadyChoosenModifications().contains(key)) {
                                        new ModificationMapping(PRIDEConverter.getOutputFrame(), true, progressDialog,
                                                key,
                                                modInfo.getAminoacid(),
                                                modInfo.getMassDiff(),
                                                (CvParamImpl) userProperties.getCVTermMappings().get(key),
                                                false);
                                        properties.getAlreadyChoosenModifications().add(key);
                                    } else {
                                        //do nothing, mapping already choosen
                                    }

                                    if (userProperties.getCVTermMappings().get((key)) != null) {

                                        CvParamImpl tempCvParam =
                                                (CvParamImpl) userProperties.getCVTermMappings().get(key);

                                        ArrayList<CvParam> modificationCVParams = new ArrayList<CvParam>();
                                        modificationCVParams.add(tempCvParam);

                                        ArrayList<MassDelta> monoMasses = new ArrayList<MassDelta>();
                                        // get the modification mass (DiffMono) retrieved from PSI-MOD
                                        if (tempCvParam.getValue() != null) {
                                            monoMasses.add(new MonoMassDeltaImpl(
                                                    Double.parseDouble(tempCvParam.getValue())));
                                        } else {
                                            // if the DiffMono is not found for the PSI-MOD the mass
                                            // from the file is used
                                            monoMasses.add(new MonoMassDeltaImpl(modInfo.getMassDiff()));
                                            //monoMasses = null;
                                        }

                                        PRIDE_modifications.add(new ModificationImpl(
                                                tempCvParam.getAccession(),
                                                ppma.getPosition(),
                                                tempCvParam.getCVLookup(),
                                                null,
                                                monoMasses,
                                                null,
                                                modificationCVParams,
                                                null));
                                    } else {
                                        Util.writeToErrorLog("Unknown modifications! - Should not happen!!! Modification: " + key);
                                    }
                                }
                            }

                            Collection<CvParam> additionalParams = new ArrayList<CvParam>();

                            // Add the PeptideProphet probability.
                            additionalParams.add(new CvParamImpl(
                                    "PRIDE:0000099", "PRIDE", "PeptideProphet probability score",
                                    0, hit.getProbability() + ""));

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
                                if (debug) {
                                    System.out.println("No dotproduct score found for peptide " + hit.getModifiedSequence());
                                }
                            }

                            if (delta != null) {
                                additionalParams.add(new CvParamImpl("PRIDE:0000180", "PRIDE", "delta", 2, delta));
                            } else {
                                if (debug) {
                                    System.out.println("No delta found for peptide " + hit.getModifiedSequence());
                                }
                            }

                            if (deltastar != null) {
                                additionalParams.add(new CvParamImpl("PRIDE:0000181", "PRIDE", "deltastar", 3, deltastar));
                            } else {
                                if (debug) {
                                    System.out.println("No deltastar found for peptide " + hit.getModifiedSequence());
                                }
                            }

                            if (zscore != null) {
                                additionalParams.add(new CvParamImpl("PRIDE:0000182", "PRIDE", "zscore", 4, zscore));
                            } else {
                                if (debug) {
                                    System.out.println("No zscore score found for peptide " + hit.getModifiedSequence());
                                }
                            }

                            if (expect != null) {
                                additionalParams.add(new CvParamImpl("PRIDE:0000183", "PRIDE", "expect", 5, expect));
                            } else {
                                if (debug) {
                                    System.out.println("No expect found for peptide " + hit.getModifiedSequence());
                                }
                            }

                            PRIDEConverter.setTotalPeptideCount(PRIDEConverter.getTotalPeptideCount() + 1);

                            // Create the peptide.
                            PRIDE_peptides.add(new PeptideImpl(specRef, pepSequence, pepStart, PRIDE_modifications, additionalParams, null));

                            // Data integrity check.
                            if (hit.getRoundedProbability() < properties.getPeptideProphetThreshold()) {
                                if (debug) {
                                    System.out.println("Output peptide '" + chargeModSeq +
                                            "' with a PeptideProphet probability lower than " +
                                            "properties.getPeptideProphetThreshold(): " + hit.getProbability() + ".");
                                }
                            }
                        }
                    }
                }

                // Adding protein annotations.
                Collection<CvParam> additionalCVParams = new ArrayList<CvParam>();
                if (protSequence != null) {
                    additionalCVParams.add(new CvParamImpl("PRIDE:0000041", "PRIDE",
                            "Search database protein sequence", -1, protein.getSequence()));
                }

                if (protein.getDescription() != null && !protein.getDescription().trim().equals("")) {
                    additionalCVParams.add(new CvParamImpl("PRIDE:0000063", "PRIDE",
                            "Protein description line", -1, protein.getDescription()));
                }

                additionalCVParams.add(new CvParamImpl("PRIDE:0000100", "PRIDE",
                        "ProteinProphet probability score", -1, protein.getProbability() + ""));
                Collection isoforms = protein.getIsoforms();

                if (isoforms != null && !isoforms.isEmpty()) {
                    for (Object isoform1 : isoforms) {
                        ProteinProphetIsoform isoform = (ProteinProphetIsoform) isoform1;
                        additionalCVParams.add(new CvParamImpl("PRIDE:0000098", "PRIDE",
                                "Indistinguishable alternative protein accession", -1, isoform.getAccession()));
                    }
                }

                // iTraq calculations
                // not yet finished. some links between the identification and the spectra are missing.
//                ArrayList userParams = new ArrayList();
//
//                if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
//                    iTRAQ iTRAQValues = new iTRAQ(
//                            ((BinaryArrayImpl) ((Spectrum) aTransformedSpectra.get(
//                            i)).getMzArrayBinary()).getDoubleArray(),
//                            ((BinaryArrayImpl) ((Spectrum) aTransformedSpectra.get(
//                            i)).getIntenArrayBinary()).getDoubleArray(),
//                            tempIdentification.getExp_mass().doubleValue(),
//                            tempIdentification.getCharge(),
//                            userProperties.getPeakIntegrationRangeLower(),
//                            userProperties.getPeakIntegrationRangeUpper(),
//                            userProperties.getReporterIonIntensityThreshold(),
//                            userProperties.getPurityCorrections());
//                    iTRAQValues.calculateiTRAQValues();
//
//                    additionalCVParams = addItraqCVTerms(additionalCVParams, iTRAQValues);
//                    userParams = addItraqUserTerms(iTRAQValues);
//                } else {
//                    userParams = null;
//                }

                uk.ac.ebi.pride.model.interfaces.core.Identification PRIDE_protein;

                if (properties.isGelFree()) {

                    //GelFreeIdentificationImpl PRIDE_protein = new GelFreeIdentificationImpl(
                    PRIDE_protein =
                            new GelFreeIdentificationImpl(
                            protein.getAccession(), //protein accession
                            protein.getVersion(), // accession version
                            null, // spliceforms
                            ppSummary.getSourceDatabase(), // database
                            PRIDE_peptides, // the peptides
                            additionalCVParams, // cv params
                            null, // user params
                            ppSummary.getSoftware(), // search engine
                            ppSummary.getDbVersion(), // database version
                            (protein.getPercent_coverage() / 100), // sequence coverage
                            protein.getProbability(), // score
                            properties.getProteinProphetThreshold(), // threshold
                            null); // spectrum reference
                } else {
                    PRIDE_protein = new TwoDimensionalIdentificationImpl(
                            protein.getAccession(), //protein accession
                            protein.getVersion(), // accession version
                            null, // spliceforms
                            ppSummary.getSourceDatabase(), // database
                            ppSummary.getDbVersion(), // database version
                            PRIDE_peptides, // the peptides
                            additionalCVParams, // cv params
                            null, // user params
                            null, // PI
                            null, // MW
                            null, // sequence coverage
                            null, // the gel
                            null, // x coordinate
                            null, // y Coordinate
                            null, // spectrum reference
                            ppSummary.getSoftware(), // search engine
                            protein.getProbability(), // score
                            properties.getProteinProphetThreshold()); // threshold
                }

                if (properties.getProteinIdentificationFilter().length() > 0) {
                    if (protein.getAccession().lastIndexOf(properties.getProteinIdentificationFilter()) == -1) {
                        PRIDEConverter.getIdentifications().add(PRIDE_protein);
                    }
                } else {
                    PRIDEConverter.getIdentifications().add(PRIDE_protein);
                }
            }

            if (!PRIDEConverter.isConversionCanceled()) {

                progressDialog.setString(null);
                progressDialog.setTitle("Creating mzData File. Please Wait...");
                progressDialog.setIntermidiate(true);
                progressDialog.setString(null);

                // The CV lookup stuff. (NB: currently hardcoded to PSI only)
                Collection<CVLookup> cvLookups = new ArrayList<CVLookup>(1);
                cvLookups.add(new CVLookupImpl(properties.getCvVersion(),
                        properties.getCvFullName(),
                        properties.getCvLabel(),
                        properties.getCvAddress()));

                // Instrument source CV parameters.
                Collection<CvParam> instrumentSourceCVParameters = new ArrayList<CvParam>(1);
                instrumentSourceCVParameters.add(new CvParamImpl(
                        properties.getAccessionInstrumentSourceParameter(),
                        properties.getCVLookupInstrumentSourceParameter(),
                        properties.getNameInstrumentSourceParameter(),
                        0,
                        properties.getValueInstrumentSourceParameter()));

                // Instrument detector parameters.
                Collection<CvParam> instrumentDetectorParamaters = new ArrayList<CvParam>(1);
                instrumentDetectorParamaters.add(new CvParamImpl(
                        properties.getAccessionInstrumentDetectorParamater(),
                        properties.getCVLookupInstrumentDetectorParamater(),
                        properties.getNameInstrumentDetectorParamater(),
                        0,
                        properties.getValueInstrumentDetectorParamater()));

                // sample details
                ArrayList<UserParam> sampleDescriptionUserParams = new ArrayList<UserParam>(
                        properties.getSampleDescriptionUserSubSampleNames().size());

                for (int i = 0; i < properties.getSampleDescriptionUserSubSampleNames().size(); i++) {
                    sampleDescriptionUserParams.add(new UserParamImpl(
                            "SUBSAMPLE_" + (i + 1),
                            i, (String) properties.getSampleDescriptionUserSubSampleNames().get(i)));
                }

                for (int i = 0; i < properties.getSampleDescriptionCVParamsQuantification().size(); i++) {
                    properties.getSampleDescriptionCVParams().add(
                            properties.getSampleDescriptionCVParamsQuantification().get(i));
                }

                PRIDEConverter.setTotalNumberOfSpectra(mzDataSpectra.size());

                // Create mzData instance.
                properties.setMzDataFile(
                        new MzDataImpl(
                        mzDataSpectra,
                        null, // software completion time
                        properties.getContacts(),
                        properties.getInstrumentName(),
                        properties.getProcessingMethod(),
                        properties.getProcessingMethodUserParams(),
                        properties.getSourceFile(),
                        userProperties.getCurrentSampleSet(),
                        properties.getInstrumentAdditionalCvParams(),
                        properties.getInstrumentAdditionalUserParams(),
                        cvLookups,
                        properties.getMzDataVersion(),
                        instrumentDetectorParamaters,
                        properties.getInstrumentDetectorUserParams(),
                        properties.getSampleDescriptionComment(),
                        properties.getMzDataAccessionNumber(),
                        properties.getAnalyzerList(),
                        properties.getSoftwareComments(),
                        instrumentSourceCVParameters,
                        properties.getInstrumentSourceUserParams(),
                        properties.getSoftwareVersion(),
                        properties.getSampleDescriptionCVParams(),
                        sampleDescriptionUserParams,
                        properties.getSoftwareName()));
            }
        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    null, "An error occured when parsing a Peptide-/ProteinProphet project.\n" +
                    "See ../Properties/ErrorLog.txt for more details.",
                    "Error Parsing Peptide-/ProteinProphet", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error Parsing Peptide-/ProteinProphet: ");
            e.printStackTrace();
        }

        return mapping;
    }
}
