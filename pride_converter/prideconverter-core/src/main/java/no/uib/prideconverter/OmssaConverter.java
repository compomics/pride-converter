package no.uib.prideconverter;

import no.uib.prideconverter.util.*;
import no.uib.prideconverter.util.OmssaModification;
import no.uib.prideconverter.gui.ModificationMapping;
import no.uib.prideconverter.gui.ProgressDialog;

import java.util.*;
import java.io.IOException;
import java.io.File;

import de.proteinms.omxparser.util.*;
import de.proteinms.omxparser.OmssaOmxFile;
import uk.ac.ebi.pride.model.interfaces.core.FragmentIon;
import uk.ac.ebi.pride.model.interfaces.core.MassDelta;
import uk.ac.ebi.pride.model.interfaces.mzdata.*;
import uk.ac.ebi.pride.model.implementation.mzData.*;
import uk.ac.ebi.pride.model.implementation.core.MonoMassDeltaImpl;
import uk.ac.ebi.pride.model.implementation.core.ModificationImpl;
import uk.ac.ebi.pride.model.implementation.core.FragmentIonImpl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.swing.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Florian Reisinger
 *         Date: 17-Jul-2009
 * @since $version
 */
public class OmssaConverter {

    /**
     * This method transforms spectra from OMSSA files, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     * @return a HashMap
     * @throws java.io.IOException in case of an error reading the input file.
     */
    protected static HashMap<String, Long> transformSpectraFromOMSSA(ArrayList<Spectrum> aTransformedSpectra) throws IOException {

        // temporary variables
        ProgressDialog progressDialog = PRIDEConverter.getProgressDialog();
        no.uib.prideconverter.util.Properties properties = PRIDEConverter.getProperties();
        no.uib.prideconverter.util.UserProperties userProperties = PRIDEConverter.getUserProperties();


        HashMap<String, Long> mapping = new HashMap<String, Long>();

        PRIDEConverter.setEmptySpectraCounter(0);
        int idCounter = 1;

        double[][] arrays;
        Collection<Precursor> precursors;
        Collection<CvParam> ionSelection;
        String chargeString, fileName;

        boolean matchFound = false;
        StringTokenizer tok;

        PRIDEConverter.setIds(new ArrayList<IdentificationGeneral>());

        int start;
        String accession, peptideSequence, upstreamFlankingSequence, downstreamFlankingSequence, database;

        MSPepHit currentMSPepHit;
        MSHits currentMSHit;

        ArrayList<CvParam> cVParams;
        ArrayList<UserParam> userParams;
        List<Integer> mzValues, intensityValues;
        int omssaAbundanceScale;
        Collection<SpectrumDescComment> spectrumDescriptionComments;

        MSModHit currentMSModHit;

        int modType, modSite;

        Iterator<MSModHit> modsIterator;
        ArrayList<CvParam> modificationCVParams;
        ArrayList<MassDelta> monoMasses;
        ArrayList<ModificationImpl> peptideModifications;
        ArrayList<FragmentIon> fragmentIons;

        MSHits tempMSHit;

        Iterator<MSHits> msHitIterator;
        double lowestEValue;
        List<MSHits> allMSHits;

        DocumentBuilderFactory dbf;
        DocumentBuilder db;
        Document dom;

        Element docEle;

        NodeList nodes, modNodes, residueNodes, tempNodes;
        String modName = "";
        Vector<String> modResidues;

        Integer modNumber = -1;
        Double modMonoMass = 0.0;

        HashMap<Integer, OmssaModification> omssaModificationDetails = new HashMap<Integer, OmssaModification>();

        OmssaOmxFile omxFile;

        List<Integer> fixedModifications, variableModifications;
        HashMap<MSSpectrum, MSHitSet> results;
        Iterator<MSSpectrum> iterator;
        MSSpectrum tempSpectrum;

        Vector<String> modifiedResidues;
        Spectrum fragmentation;

        Double mzRangeStart, mzRangeStop;

        //read the mods.xml file
        if (!PRIDEConverter.isConversionCanceled()) {
            try {
                if (userProperties.getOmssaInstallDir() != null) {
                    File mods = new File(userProperties.getOmssaInstallDir() + "mods.xml");

                    //get the factory
                    dbf = DocumentBuilderFactory.newInstance();

                    dbf.setValidating(false);
                    dbf.setAttribute("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
                    dbf.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                    dbf.setAttribute("http://xml.org/sax/features/validation", false);

                    //Using factory get an instance of document builder
                    db = dbf.newDocumentBuilder();

                    //parse using builder to get DOM representation of the XML file
                    dom = db.parse(mods);

                    //get the root elememt
                    docEle = dom.getDocumentElement();

                    nodes = docEle.getChildNodes();

                    for (int i = 0; i < nodes.getLength() && !PRIDEConverter.isConversionCanceled(); i++) {

                        if (nodes.item(i).getNodeName().equalsIgnoreCase("MSModSpec")) {

                            modNodes = nodes.item(i).getChildNodes();
                            modNumber = -1;
                            modName = "";
                            modMonoMass = 0.0;
                            modResidues = new Vector<String>();

                            for (int j = 0; j < modNodes.getLength() && !PRIDEConverter.isConversionCanceled(); j++) {

                                if (modNodes.item(j).getNodeName().equalsIgnoreCase("MSModSpec_mod")) {

                                    tempNodes = modNodes.item(j).getChildNodes();

                                    for (int m = 0; m < tempNodes.getLength(); m++) {
                                        if (tempNodes.item(m).getNodeName().equalsIgnoreCase("MSMod")) {
                                            modNumber = new Integer(tempNodes.item(m).getTextContent());
                                        }
                                    }
                                } else if (modNodes.item(j).getNodeName().equalsIgnoreCase("MSModSpec_name")) {
                                    modName = modNodes.item(j).getTextContent();
                                } else if (modNodes.item(j).getNodeName().equalsIgnoreCase("MSModSpec_monomass")) {
                                    modMonoMass = new Double(modNodes.item(j).getTextContent());
                                } else if (modNodes.item(j).getNodeName().equalsIgnoreCase("MSModSpec_residues")) {
                                    residueNodes = modNodes.item(j).getChildNodes();

                                    modResidues = new Vector<String>();

                                    for (int m = 0; m < residueNodes.getLength(); m++) {

                                        if (residueNodes.item(m).getNodeName().equalsIgnoreCase(
                                                "MSModSpec_residues_E")) {

                                            modResidues.add(residueNodes.item(m).getTextContent());
                                        }
                                    }
                                }
                            }

                            if (modMonoMass == 0.0) {
                                modMonoMass = null;
                            }

                            omssaModificationDetails.put(modNumber,
                                    new OmssaModification(modNumber, modName,
                                    modMonoMass, modResidues));
                        }
                    }
                }
            } catch (Exception e) {
                Util.writeToErrorLog("Error parsing the mods.xml: ");
                e.printStackTrace();

                JOptionPane.showMessageDialog(null,
                        "The mods.xml file could not be parsed.\n" +
                        "See ../Properties/ErrorLog.txt for more details.",
                        "Error Parsing File", JOptionPane.ERROR_MESSAGE);
            }
        }

        //read the usermods.xml file
        if (!PRIDEConverter.isConversionCanceled()) {
            try {
                if (userProperties.getOmssaInstallDir() != null) {
                    File mods = new File(userProperties.getOmssaInstallDir() + "usermods.xml");

                    //get the factory
                    dbf = DocumentBuilderFactory.newInstance();

                    dbf.setValidating(false);
                    dbf.setAttribute("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
                    dbf.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                    dbf.setAttribute("http://xml.org/sax/features/validation", false);

                    //Using factory get an instance of document builder
                    db = dbf.newDocumentBuilder();

                    //parse using builder to get DOM representation of the XML file
                    dom = db.parse(mods);

                    //get the root elememt
                    docEle = dom.getDocumentElement();

                    nodes = docEle.getChildNodes();

                    for (int i = 0; i < nodes.getLength() && !PRIDEConverter.isConversionCanceled(); i++) {

                        if (nodes.item(i).getNodeName().equalsIgnoreCase("MSModSpec")) {

                            modNodes = nodes.item(i).getChildNodes();
                            modNumber = -1;
                            modName = "";
                            modMonoMass = 0.0;
                            modResidues = new Vector<String>();

                            for (int j = 0; j < modNodes.getLength() && !PRIDEConverter.isConversionCanceled(); j++) {

                                if (modNodes.item(j).getNodeName().equalsIgnoreCase("MSModSpec_mod")) {

                                    tempNodes = modNodes.item(j).getChildNodes();

                                    for (int m = 0; m <
                                            tempNodes.getLength(); m++) {
                                        if (tempNodes.item(m).getNodeName().equalsIgnoreCase("MSMod")) {
                                            modNumber = new Integer(tempNodes.item(m).getTextContent());
                                        }
                                    }
                                } else if (modNodes.item(j).getNodeName().equalsIgnoreCase("MSModSpec_name")) {
                                    modName = modNodes.item(j).getTextContent();
                                } else if (modNodes.item(j).getNodeName().equalsIgnoreCase("MSModSpec_monomass")) {
                                    modMonoMass = new Double(modNodes.item(j).getTextContent());
                                } else if (modNodes.item(j).getNodeName().equalsIgnoreCase("MSModSpec_residues")) {
                                    residueNodes = modNodes.item(j).getChildNodes();

                                    modResidues = new Vector<String>();

                                    for (int m = 0; m < residueNodes.getLength(); m++) {

                                        if (residueNodes.item(m).getNodeName().equalsIgnoreCase("MSModSpec_residues_E")) {
                                            modResidues.add(residueNodes.item(m).getTextContent());
                                        }
                                    }
                                }
                            }

                            if (modMonoMass == 0.0) {
                                modMonoMass = null;
                            }

                            omssaModificationDetails.put(modNumber,
                                    new OmssaModification(modNumber, modName,
                                    modMonoMass, modResidues));
                        }
                    }
                }
            } catch (Exception e) {
                Util.writeToErrorLog("Error parsing the usermods.xml: ");
                e.printStackTrace();

                JOptionPane.showMessageDialog(null,
                        "The usermods.xml file could not be parsed.\n" +
                        "See ../Properties/ErrorLog.txt for more details.",
                        "Error Parsing File", JOptionPane.ERROR_MESSAGE);
            }
        }

        progressDialog.setIntermidiate(false);
        progressDialog.setValue(0);
        int progressCounter = 0;

        // read the omssa files
        for (int k = 0; k < properties.getSelectedSourceFiles().size() && !PRIDEConverter.isConversionCanceled(); k++) {

            PRIDEConverter.setCurrentFileName(new File(properties.getSelectedSourceFiles().get(k)).getName());

            progressDialog.setIntermidiate(true);
            progressCounter = 0;
            progressDialog.setValue(0);
            progressDialog.setString(PRIDEConverter.getCurrentFileName() + " (" + (k + 1) + "/" +
                    properties.getSelectedSourceFiles().size() + ")");

            // @TODO move the parsing of the mods.xml and usermodsxml into OmssaOmxFile
            omxFile = new OmssaOmxFile(properties.getSelectedSourceFiles().get(k), null, null);

            fixedModifications =
                    omxFile.getParserResult().MSSearch_request.MSRequest.get(0).MSRequest_settings.MSSearchSettings.MSSearchSettings_fixed.MSMod;
            variableModifications =
                    omxFile.getParserResult().MSSearch_request.MSRequest.get(0).MSRequest_settings.MSSearchSettings.MSSearchSettings_variable.MSMod;

            // note: defaults to 100 if not found (as described in the OMSSA xsd file)
            int omssaResponseScale =
                    omxFile.getParserResult().MSSearch_response.MSResponse.get(0).MSResponse_scale;

            double ionCoverageErrorMargin =
                    omxFile.getParserResult().MSSearch_request.MSRequest.get(0).MSRequest_settings.MSSearchSettings.MSSearchSettings_msmstol;

            // fixed modifications
            for (Integer fixedModification1 : fixedModifications) {

                new ModificationMapping(PRIDEConverter.getOutputFrame(), true, progressDialog, "" +
                        omssaModificationDetails.get(fixedModification1).getModName(),
                        omssaModificationDetails.get(fixedModification1).getModResiduesAsString(),
                        omssaModificationDetails.get(fixedModification1).getModMonoMass(),
                        (CvParamImpl) userProperties.getCVTermMappings().get(
                                omssaModificationDetails.get(fixedModification1).getModName()), true);
            }

            // variable modifications
            for (Integer variableModification : variableModifications) {

                new ModificationMapping(PRIDEConverter.getOutputFrame(), true, progressDialog, "" +
                        omssaModificationDetails.get(variableModification).getModName(),
                        omssaModificationDetails.get(variableModification).getModResiduesAsString(),
                        omssaModificationDetails.get(variableModification).getModMonoMass(),
                        (CvParamImpl) userProperties.getCVTermMappings().get(
                                omssaModificationDetails.get(variableModification).getModName()), false);
            }

            results = omxFile.getSpectrumToHitSetMap();
            iterator = results.keySet().iterator();

            progressDialog.setIntermidiate(false);
            progressDialog.setMax(results.keySet().size());
            progressDialog.setValue(0);

            while (iterator.hasNext()) {

                progressDialog.setValue(progressCounter++);

                tempSpectrum = iterator.next();

                // @TODO: OMSSA question: possible with more than one file name per spectrum??
                //
                // spectrum name is not mandatory, use spectrum number if no name is given
                if (tempSpectrum.MSSpectrum_ids.MSSpectrum_ids_E.size() == 0) {
                    fileName = "" + tempSpectrum.MSSpectrum_number;
                } else {
                    fileName = tempSpectrum.MSSpectrum_ids.MSSpectrum_ids_E.get(0);
                }

                matchFound = false;

                PRIDEConverter.setSpectrumKey(fileName + "_" + tempSpectrum.MSSpectrum_number);

                if (properties.getSelectedSpectraKeys().size() > 0) {
                    for (int i = 0; i < properties.getSelectedSpectraKeys().size() && !matchFound; i++) {
                        matchFound = ((String) ((Object[]) properties.getSelectedSpectraKeys().get(i))[0]).equalsIgnoreCase(
                                fileName);
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

                        while (tok.hasMoreTokens() && !matchFound) {

                            tempToken = tok.nextToken();
                            tempToken = tempToken.trim();

                            if (properties.isSelectionCriteriaFileName()) {
                                if (fileName.lastIndexOf(tempToken) != -1) {
                                    matchFound = true;
                                }
                            } else {
                                if (("" + tempSpectrum.MSSpectrum_number).lastIndexOf(tempToken) != -1) {
                                    matchFound = true;
                                }
                            }
                        }
                    } else {
                        matchFound = true;
                    }
                }

                if (matchFound) {

                    start = -1;
                    accession = null;
                    database = null;
                    peptideSequence = null;
                    upstreamFlankingSequence = null;
                    downstreamFlankingSequence = null;
                    peptideModifications = null;

                    mzValues = tempSpectrum.MSSpectrum_mz.MSSpectrum_mz_E;
                    intensityValues = tempSpectrum.MSSpectrum_abundance.MSSpectrum_abundance_E;

                    omssaAbundanceScale = tempSpectrum.MSSpectrum_iscale;

                    arrays = new double[2][mzValues.size()];

                    for (int j = 0; j < mzValues.size(); j++) {
                        arrays[0][j] = mzValues.get(j) / omssaResponseScale;
                        arrays[1][j] = intensityValues.get(j) / omssaAbundanceScale;
                    }

                    // Precursor collection.
                    precursors = new ArrayList<Precursor>(1);

                    // Ion selection parameters.
                    ionSelection = new ArrayList<CvParam>(4);

                    // @TODO: OMSSA question: possible with more than one charge per spectrum??
                    chargeString = "" + tempSpectrum.MSSpectrum_charge.MSSpectrum_charge_E.get(0);
                    chargeString = chargeString.replaceFirst("\\+", "");

                    if (!chargeString.equalsIgnoreCase("0")) {
                        ionSelection.add(new CvParamImpl("PSI:1000041", "PSI",
                                "ChargeState", ionSelection.size(), chargeString));
                    }

                    ionSelection.add(new CvParamImpl("PSI:1000040", "PSI",
                            "MassToChargeRatio", ionSelection.size(),
                            "" + tempSpectrum.MSSpectrum_precursormz / omssaResponseScale));

                    precursors.add(new PrecursorImpl(null, null, ionSelection, null, 1, 0, 0));

                    // Spectrum description comments.
                    spectrumDescriptionComments = new ArrayList<SpectrumDescComment>();

                    if (results.get(tempSpectrum).MSHitSet_hits.MSHits.size() > 0) {
                        spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Identified"));

                        cVParams = new ArrayList<CvParam>();

                        // find (and select) the MSHit with the lowest e-value
                        allMSHits = results.get(tempSpectrum).MSHitSet_hits.MSHits;
                        msHitIterator = allMSHits.iterator();
                        lowestEValue = Double.MAX_VALUE;
                        currentMSHit = null;

                        while (msHitIterator.hasNext()) {

                            tempMSHit = msHitIterator.next();

                            if (tempMSHit.MSHits_evalue < lowestEValue) {
                                lowestEValue = tempMSHit.MSHits_evalue;
                                currentMSHit = tempMSHit;
                            }
                        }

                        cVParams.add(new CvParamImpl("PRIDE:0000185", "PRIDE",
                                "OMSSA E-value", cVParams.size(), "" +
                                currentMSHit.MSHits_evalue));

                        cVParams.add(new CvParamImpl("PRIDE:0000186", "PRIDE",
                                "OMSSA P-value", cVParams.size(), "" +
                                currentMSHit.MSHits_pvalue));

                        downstreamFlankingSequence = currentMSHit.MSHits_pepstart;
                        upstreamFlankingSequence = currentMSHit.MSHits_pepstop;

                        if (upstreamFlankingSequence != null) {
                            cVParams.add(new CvParamImpl("PRIDE:0000065",
                                    "PRIDE", "Upstream flanking sequence",
                                    cVParams.size(), "" +
                                    upstreamFlankingSequence.toUpperCase()));
                        }

                        if (downstreamFlankingSequence != null) {
                            cVParams.add(new CvParamImpl("PRIDE:0000066",
                                    "PRIDE", "Downstream flanking sequence",
                                    cVParams.size(), "" +
                                    downstreamFlankingSequence.toUpperCase()));
                        }

                        peptideSequence = currentMSHit.MSHits_pepstring;

                        // @TODO: OMSSA question: how to handle protein isoforms?
                        // Currently handled by simply selection the first peptide hit (in the xml file)
                        currentMSPepHit = currentMSHit.MSHits_pephits.MSPepHit.get(0);

                        //String accession = currentMSHit.MSHits_libaccession;
                        if (currentMSPepHit.MSPepHit_accession != null) {
                            accession = currentMSPepHit.MSPepHit_accession;
                        } else {
                            accession = "gi|" + currentMSPepHit.MSPepHit_gi;
                        }

                        if (omxFile.getParserResult().MSSearch_request.MSRequest.get(0).MSRequest_settings.MSSearchSettings.MSSearchSettings_db != null) {
                            database = omxFile.getParserResult().MSSearch_request.MSRequest.get(0).MSRequest_settings.MSSearchSettings.MSSearchSettings_db;
                        }

                        start = currentMSPepHit.MSPepHit_start + 1;

                        // handle the modifications
                        // @TODO: OMSSA question: more than one MSRequest??

                        // fixed modifications
                        if (fixedModifications.size() > 0) {

                            peptideModifications = new ArrayList<ModificationImpl>();

                            for (Integer fixedModification : fixedModifications) {
                                modifiedResidues = omssaModificationDetails.get(fixedModification).getModResidues();

                                for (String modifiedResidue : modifiedResidues) {

                                    int index = peptideSequence.indexOf(modifiedResidue);

                                    while (index != -1) {

                                        CvParamImpl tempCvParam =
                                                (CvParamImpl) userProperties.getCVTermMappings().get(
                                                        omssaModificationDetails.get(fixedModification).getModName());

                                        modificationCVParams = new ArrayList<CvParam>();
                                        modificationCVParams.add(tempCvParam);

                                        monoMasses = new ArrayList<MassDelta>();
                                        // get the modification mass (DiffMono) retrieved from PSI-MOD
                                        if (tempCvParam.getValue() != null) {
                                            monoMasses.add(new MonoMassDeltaImpl(
                                                    Double.parseDouble(tempCvParam.getValue())));
                                        } else {
                                            // if the DiffMono is not found for the PSI-MOD the mass
                                            // from the file is used
                                            monoMasses.add(
                                                    new MonoMassDeltaImpl(omssaModificationDetails.get(
                                                            fixedModification).getModMonoMass()));
                                            //monoMasses = null;
                                        }

                                        peptideModifications.add(new ModificationImpl(
                                                tempCvParam.getAccession(),
                                                index + 1,
                                                tempCvParam.getCVLookup(),
                                                null,
                                                monoMasses,
                                                null,
                                                modificationCVParams,
                                                null));

                                        index = peptideSequence.indexOf(modifiedResidue, index + 1);
                                    }
                                }
                            }

                            if (peptideModifications.size() == 0) {
                                peptideModifications = null;
                            }
                        }

                        // variable modifications
                        modsIterator = currentMSHit.MSHits_mods.MSModHit.iterator();

                        if (peptideModifications == null) {
                            if (currentMSHit.MSHits_mods.MSModHit.size() > 0) {
                                peptideModifications = new ArrayList<ModificationImpl>();
                            }
                        }

                        while (modsIterator.hasNext()) {

                            currentMSModHit = modsIterator.next();

                            modType = currentMSModHit.MSModHit_modtype.MSMod;
                            modSite = currentMSModHit.MSModHit_site;

                            modificationCVParams = new ArrayList<CvParam>();
                            modificationCVParams.add(userProperties.getCVTermMappings().get(
                                    omssaModificationDetails.get(modType).getModName()));

                            monoMasses = new ArrayList<MassDelta>();
                            monoMasses.add(new MonoMassDeltaImpl(omssaModificationDetails.get(modType).getModMonoMass()));

                            peptideModifications.add(new ModificationImpl(
                                    userProperties.getCVTermMappings().get(
                                    omssaModificationDetails.get(modType).getModName()).getAccession(),
                                    modSite + 1,
                                    userProperties.getCVTermMappings().get(
                                    omssaModificationDetails.get(modType).getModName()).getCVLookup(),
                                    null,
                                    monoMasses,
                                    null,
                                    modificationCVParams,
                                    null));
                        }

                        //calculate itraq values
                        iTRAQ iTRAQValues = null;

                        if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
                            iTRAQValues = new iTRAQ(arrays,
                                    tempSpectrum.MSSpectrum_precursormz / omssaResponseScale,
                                    tempSpectrum.MSSpectrum_charge.MSSpectrum_charge_E.get(0),
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
                            iTraqNorm = (String[]) iTRAQValues.getAllNorms().get(0);
                            iTraqUT = (String[]) iTRAQValues.getAllUTs().get(0);
                            iTraqRatio = (String[][]) iTRAQValues.getAllRatios().get(0);
                        }

                        if (properties.getSampleDescriptionCVParamsQuantification().size() > 0) {
                            PRIDEConverter.addItraqCVTerms(cVParams, iTraqNorm);
                            userParams = PRIDEConverter.addItraqUserTerms(iTraqRatio);
                        } else {
                            userParams = null;
                        }

                        if (currentMSHit.MSHits_pvalue >= properties.getPeptideScoreThreshold()) {

                            // find and add the fragment ions
                            fragmentIons = new ArrayList<FragmentIon>();

                            // get the list of fragment ions for the current peptide identification
                            List<MSMZHit> currentFragmentIons = currentMSHit.MSHits_mzhits.MSMZHit;

                            // iterate the fragment ions, detect the type and create CV params for each of them
                            for (MSMZHit currentFragmentIon : currentFragmentIons) {

                                // ion type is [0-10], 0 is a-ion, 1 is b-ion etc
                                // see FragmentIonMappings.prop for complete list
                                int msIonType = currentFragmentIon.MSMZHit_ion.MSIonType;

                                // tag used for netutal loss and immonium ion type
                                String msIonNeutralLossOrImmoniumIonTag = "";

                                int msIonNeutralLossType = currentFragmentIon.MSMZHit_moreion.MSIon.MSIon_neutralloss.MSIonNeutralLoss;

                                // -1 means no neutral loss reported
                                if (msIonNeutralLossType != -1) {
                                    msIonNeutralLossOrImmoniumIonTag += "_" + msIonNeutralLossType;
                                } else {

                                    // check for immonium ions
                                    // note: assumes that a immonium ion can not have a neutral loss
                                    if (currentFragmentIon.MSMZHit_moreion.MSIon.MSIon_immonium.MSImmonium.MSImmonium_parent != null) {
                                        msIonNeutralLossOrImmoniumIonTag += "_"
                                                + currentFragmentIon.MSMZHit_moreion.MSIon.MSIon_immonium.MSImmonium.MSImmonium_parent;
                                    }
                                }

                                // map the reported fragment ion to its corresponding CV term by using the
                                // mappings given in the FragmentIonsMapping.prop file
                                FragmentIonMappedDetails fragmentIonMappedDetails =
                                        PRIDEConverter.getFragmentIonMappings().getCVTermMappings().get(properties.getDataSource() +
                                                "_" + msIonType + msIonNeutralLossOrImmoniumIonTag);

                                // check if a mapping was found or not
                                if (fragmentIonMappedDetails == null) {
                                    JOptionPane.showMessageDialog(PRIDEConverter.getOutputFrame(),
                                            "Unknown fragment ion \'" + currentFragmentIon.MSMZHit_ion.MSIonType + "\'. Ion not included in annotation.\n" +
                                                    "Please contact the PRIDE support team at pride-support@ebi.ac.uk.",
                                            "Unknown Fragment Ion",
                                            JOptionPane.INFORMATION_MESSAGE);
                                } else {

                                    // -1 means that a mapping was found but that that this particular fragment
                                    // ion type is currently not being used. Update FragmentIonsMapping.prop if
                                    // you want to add a mapping for this fragment ion type.
                                    if (!fragmentIonMappedDetails.getCvParamAccession().equalsIgnoreCase("-1")) {

                                        // Now we have to map the reported fragment ion to its corresponding peak.
                                        // Note that the values given in the OMSSA file are scaled.
                                        int fragmentIonMzValueUnscaled = currentFragmentIon.MSMZHit_mz;

                                        mzValues = tempSpectrum.MSSpectrum_mz.MSSpectrum_mz_E;
                                        intensityValues = tempSpectrum.MSSpectrum_abundance.MSSpectrum_abundance_E;

                                        double currentIntensityScale = tempSpectrum.MSSpectrum_iscale;
                                        double fragmentIonIntensityScaled = -1;
                                        double observedPeakMzValue = -1;
                                        double fragmentIonMassError = -1;

                                        // Iterate the peaks and find the values within the fragment ion error range.
                                        // If more than one match, use the most intense.
                                        for (int j = 0; j < mzValues.size(); j++) {

                                            // check if the fragment ion is within the mass error range
                                            if (Math.abs(mzValues.get(j) - fragmentIonMzValueUnscaled) <= (ionCoverageErrorMargin * omssaResponseScale)) {

                                                // select this peak if it's the most intense peak withing range
                                                if ((intensityValues.get(j).doubleValue() / currentIntensityScale) > fragmentIonIntensityScaled) {
                                                    fragmentIonIntensityScaled = intensityValues.get(j).doubleValue() / currentIntensityScale;

                                                    // calculate the fragmet ion mass
                                                    fragmentIonMassError = (mzValues.get(j).doubleValue() - fragmentIonMzValueUnscaled)
                                                            / omssaResponseScale; // @TODO: or the other way around?? The order decides the sign.
                                                    observedPeakMzValue = mzValues.get(j) / omssaResponseScale;
                                                }
                                            }
                                        }

                                        // check if any peaks in the spectrum matched the fragment ion
                                        if (fragmentIonIntensityScaled == -1) {

                                            JOptionPane.showMessageDialog(PRIDEConverter.getOutputFrame(),
                                                    "Unable to map the fragment ion \'" +
                                                            currentFragmentIon.MSMZHit_ion.MSIonType + " " + currentFragmentIon.MSMZHit_number + "\'. Ion not included in annotation.\n" +
                                                            "Please contact the PRIDE support team at pride-support@ebi.ac.uk.",
                                                    "Unable To Map Fragment Ion",
                                                    JOptionPane.INFORMATION_MESSAGE);
                                        } else {

                                            // create the list of CV Params for the fragment ion
                                            ArrayList<CvParam> currentCvTerms =
                                                    PRIDEConverter.createFragmentIonCvParams(
                                                            fragmentIonMappedDetails,
                                                            observedPeakMzValue,
                                                            currentFragmentIon.MSMZHit_charge,
                                                            currentFragmentIon.MSMZHit_number,
                                                            fragmentIonIntensityScaled,
                                                            fragmentIonMassError, // @TODO: use absolute value?
                                                            null);

                                            // add the created fragment ion to the list of all fragment ions
                                            fragmentIons.add(new FragmentIonImpl(currentCvTerms, null));
                                        }
                                    }
                                }
                            }

                            PRIDEConverter.getIds().add(new IdentificationGeneral(
                                    fileName, // spectrum file name
                                    accession, // spectrum accession
                                    "OMSSA", // search engine
                                    database, // database
                                    null, // database version
                                    peptideSequence.toUpperCase(), // sequence
                                    start, // start
                                    currentMSHit.MSHits_pvalue, // score
                                    null, // threshold
                                    iTraqNorm, iTraqUT, iTraqRatio, // iTRAQ values
                                    cVParams, // cv params
                                    userParams, // user params
                                    peptideModifications, // list of modifications
                                    fragmentIons)); // list of fragment ions
                        }
                    } else {
                        spectrumDescriptionComments.add(new SpectrumDescCommentImpl("Not identified"));
                    }

                    if (arrays[properties.MZ_ARRAY].length > 0) {

                        mzRangeStart = arrays[properties.MZ_ARRAY][0];
                        mzRangeStop = arrays[properties.MZ_ARRAY][arrays[properties.MZ_ARRAY].length - 1];

                        spectrumDescriptionComments = PRIDEConverter.addUserSpectrumComments(spectrumDescriptionComments,
                                properties.getSpectrumCvParams().get(PRIDEConverter.getSpectrumKey()),
                                properties.getSpectrumUserParams().get(PRIDEConverter.getSpectrumKey()));

                        // Create new mzData spectrum for the fragmentation spectrum.
                        fragmentation =
                                new SpectrumImpl(
                                new BinaryArrayImpl(arrays[properties.INTENSITIES_ARRAY],
                                BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                mzRangeStart,
                                new BinaryArrayImpl(arrays[properties.MZ_ARRAY],
                                BinaryArrayImpl.LITTLE_ENDIAN_LABEL),
                                2,
                                null,
                                mzRangeStop,
                                null,
                                idCounter, precursors, spectrumDescriptionComments,
                                null, null,
                                null, null);

                        // Store (spectrumfileid, spectrumid) mapping.
                        //mapping.put(new Long(idCounter), new Long(idCounter++));

                        mapping.put(fileName, (long) idCounter);
                        idCounter++;

                        // Store the transformed spectrum.
                        aTransformedSpectra.add(fragmentation);
                    }
                }
            }
        }

        PRIDEConverter.setTotalNumberOfSpectra(idCounter - 1);
        return mapping;
    }


}
