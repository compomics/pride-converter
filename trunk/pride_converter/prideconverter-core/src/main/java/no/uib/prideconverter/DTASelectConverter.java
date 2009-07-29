package no.uib.prideconverter;

import no.uib.prideconverter.util.Util;
import no.uib.prideconverter.gui.ProgressDialog;

import javax.swing.*;
import java.util.*;
import java.util.regex.Pattern;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import uk.ac.ebi.pride.model.implementation.mzData.CVLookupImpl;
import uk.ac.ebi.pride.model.implementation.mzData.CvParamImpl;
import uk.ac.ebi.pride.model.implementation.mzData.UserParamImpl;
import uk.ac.ebi.pride.model.implementation.mzData.MzDataImpl;
import uk.ac.ebi.pride.model.interfaces.mzdata.Spectrum;
import uk.ac.ebi.pride.model.interfaces.mzdata.CVLookup;
import uk.ac.ebi.pride.model.interfaces.mzdata.CvParam;
import uk.ac.ebi.pride.model.interfaces.mzdata.UserParam;

/**
 * @author Florian Reisinger
 *         Date: 17-Jul-2009
 * @since $version
 */
public class DTASelectConverter {

    /**
     * This method transforms spectra from DTASelect projects, returning
     * a HashMap that maps the filenames to their mzData spectrumID.
     *
     * When completed "identification" contains all the identifications, and mzDataFile contains
     * the spectra as mzData.
     *
     * @param    aTransformedSpectra   ArrayList that will contain the transformed
     *                                 mzData spectra. Please note that this is a
     *                                 reference parameter.
     * @return a HashMap
     */
    protected static HashMap<String, Long> transformSpectraFromDTASelectProjects(ArrayList<Spectrum> aTransformedSpectra) {

        // temporary variables
        ProgressDialog progressDialog = PRIDEConverter.getProgressDialog();
        no.uib.prideconverter.util.Properties properties = PRIDEConverter.getProperties();
        no.uib.prideconverter.util.UserProperties userProperties = PRIDEConverter.getUserProperties();
        boolean debug = PRIDEConverter.isDebug();

        final String SEPARATOR = "\t";
        final Pattern START_WITH_A_LETTER = Pattern.compile("^[a-zA-Z].*");

        progressDialog.setString(null);
        progressDialog.setTitle("Transforming Spectra. Please Wait...");
        progressDialog.setIntermidiate(true);
        progressDialog.setString(null);

        HashMap<String, Long> filenameToMzDataIDMapping = new HashMap<String, Long>();
        PRIDEConverter.setTotalPeptideCount(0);

        // get the list of spectrum files
        File[] spectrumFiles = new File(properties.getSpectrumFilesFolderName()).listFiles();

        // the name of the database used for the peptide and protein identifications
        String databaseName = "unknown";

        try {
            // The first thing we'll do is get all the spectra, read them in and transform them in mzData format.
            // Since PRIDE is actually mzData grouped together with an identification part, we'll also have to
            // be able to linke the spectra to the corresponding identifications. For this, we'll give each
            // spectrum a number, and we'll create a lookup table with 'filename -> number' on the way.

            // load all the spectra from the input folder and transform them
            // (at present only the array containing the files is not empty)
            aTransformedSpectra = new ArrayList<Spectrum>();
            PRIDEConverter.readMS2Spectra(spectrumFiles, aTransformedSpectra, filenameToMzDataIDMapping);

            // extract the modification details and database name from sequest.params
            BufferedReader br = new BufferedReader(new FileReader(properties.getSequestParamFile()));

            properties.setAlreadyChoosenModifications(new ArrayList<String>());

            String line = br.readLine();

            HashMap<String, Double> variableModifications = new HashMap<String, Double>();
            HashMap<String, Double> fixedModifications = new HashMap<String, Double>();

            while (line != null) {

                // extract database name
                if (line.startsWith("database_name = ")) {
                    databaseName = line.substring(line.indexOf("= ") + 2);
                }

                // extract variable modifications
                // NB: assumes that a given amino acid appears only once.
                //     which seems to be in accordance with Sequest
                if (line.startsWith("diff_search_options = ")) {

                    String temp = line.substring(line.indexOf("= ") + 2);

                    StringTokenizer tok = new StringTokenizer(temp);

                    while (tok.hasMoreTokens()) {

                        Double tempModificationMass = new Double(tok.nextToken());
                        String tempModifiedAminoAcids = tok.nextToken();

                        for (int i = 0; i < tempModifiedAminoAcids.length(); i++) {

                            if (!tempModificationMass.toString().equalsIgnoreCase("0.0")) {
                                variableModifications.put(
                                        "" + tempModifiedAminoAcids.charAt(i), tempModificationMass);
                            }
                        }
                    }
                }

                // extract fixed modifications.
                // FIX ME: does not handle fixed terminal modifications...
                String temp;

                if (line.startsWith("add_G_Glycine = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("G", new Double(temp));
                    }

                } else if (line.startsWith("add_A_Alanine = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("A", new Double(temp));
                    }

                } else if (line.startsWith("add_S_Serine = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("S", new Double(temp));
                    }

                } else if (line.startsWith("add_P_Proline = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("P", new Double(temp));
                    }

                } else if (line.startsWith("add_V_Valine = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("V", new Double(temp));
                    }

                } else if (line.startsWith("add_T_Threonine = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("T", new Double(temp));
                    }

                } else if (line.startsWith("add_C_Cysteine = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("C", new Double(temp));
                    }

                } else if (line.startsWith("add_L_Leucine = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("L", new Double(temp));
                    }

                } else if (line.startsWith("add_I_Isoleucine = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("I", new Double(temp));
                    }

                } else if (line.startsWith("add_X_LorI = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("X", new Double(temp));
                    }

                } else if (line.startsWith("add_N_Asparagine = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("N", new Double(temp));
                    }

                } else if (line.startsWith("add_O_Ornithine = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("O", new Double(temp));
                    }

                } else if (line.startsWith("add_B_avg_NandD = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("B", new Double(temp));
                    }

                } else if (line.startsWith("add_D_Aspartic_Acid = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("D", new Double(temp));
                    }

                } else if (line.startsWith("add_Q_Glutamine = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("Q", new Double(temp));
                    }

                } else if (line.startsWith("add_K_Lysine = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("K", new Double(temp));
                    }

                } else if (line.startsWith("add_Z_avg_QandE = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("Z", new Double(temp));
                    }

                } else if (line.startsWith("add_E_Glutamic_Acid = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("E", new Double(temp));
                    }

                } else if (line.startsWith("add_M_Methionine = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("M", new Double(temp));
                    }

                } else if (line.startsWith("add_H_Histidine = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("H", new Double(temp));
                    }

                } else if (line.startsWith("add_F_Phenyalanine = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("F", new Double(temp));
                    }

                } else if (line.startsWith("add_R_Arginine = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("R", new Double(temp));
                    }

                } else if (line.startsWith("add_Y_Tyrosine = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("Y", new Double(temp));
                    }

                } else if (line.startsWith("add_W_Tryptophan = ")) {
                    temp = line.substring(line.indexOf("= ") + 2);
                    temp = temp.substring(0, temp.indexOf(";"));
                    temp = temp.trim();

                    if (new Double(temp) > 0 || new Double(temp) < 0) {
                        fixedModifications.put("W", new Double(temp));
                    }
                }

                line = br.readLine();
            }

            br.close();


            //******************PARSING THE IDENTIFICATIONS FILE********************************

            // We'll start by creating a HashMap that will hold all identifications
            // (keyed by protein accession number in this case)
            HashMap<String, PRIDEConverter.InnerID> allIds = new HashMap<String, PRIDEConverter.InnerID>();

            progressDialog.setString(null);
            progressDialog.setTitle("Parsing DTASelect File. Please Wait...");
            progressDialog.setIntermidiate(true);
            progressDialog.setString(null);

            // Now we'll fill it out while reading the input file.
            br = new BufferedReader(new FileReader(properties.getDtaSelectFileName()));
            line = null;

            // In this particular case, it is also neccessary to get track of the previous line
            String previousLine = null;

            // This counter counts every single line, used for reporting errors (as in: line x has an error).
            int lineCount = 0;

            // Protein related data to be parsed:
            String accessionNumber = null; // locus name
            Integer sequenceCount = null;
            Integer spectrumCount = null;

            String initialCoverage = null;
            Double coverage = null;

            Integer proteinLength = null;
            Double molecularWeight = null;
            Double pI = null;
            String validationStatus = null;
            String proteinDescription = null;

            // For the peptides:
            String unique = null; // ToDo: never used !?  // Future potential reference, not used now

            // Information taken from the file name:
            String fileName = null;
            String spectrumFile = null;
            Long scanNumber = null;
            Integer charge = null;

            Double sequestXcorr = null;
            Double sequestDelta = null;
            Double peptideMass = null;
            Double calculatedPeptideMass = null;
            Double totalIntensity = null;
            Integer sequestRSp = null;
            Double sequestSp = null;
            Double ionProportion = null;
            Integer redundancy = null;

            // Information about the peptides
            String preDigSiteAA = null;
            String peptideSequence = null;
            String postDigSiteAA = null;

            // We are creating a protein InnerID and initializing it to null.
            PRIDEConverter.InnerID protein = null;

            // We have to create an ArrayList to be able to deal with multiple proteins that have the same peptides assigned
            ArrayList<PRIDEConverter.InnerID> proteins = new ArrayList<PRIDEConverter.InnerID>();

            //Flag to decide where the file is parsed
            boolean startParsing = false;

            while ((line = br.readLine()) != null && !PRIDEConverter.isConversionCanceled()) {

                //For all the lines, do two things:
                // 1. Count the line.
                lineCount++;

                if (debug) {
                    System.out.println("The line is: " + lineCount);
                }

                // 2. Remove leading and trailing spaces.
                line = line.trim();

                // Switch the flag here:
                if (previousLine != null && previousLine.startsWith("Unique")) {
                    startParsing = true;
                }

                // We do not need the last lines of the file
                if (line.contains("Proteins") && line.contains("Peptide IDs")) {
                    startParsing = false;
                }

                //Start parsing here
                if (startParsing) {

                    // Skip the first line/s as it is the header line/s.
                    // Okay, non-empty, non-comment, non-header line.
                    // We need to process this.
                    // If the line does not start with a */ " " (the lines that contain the PROTEIN information)

                    // ** THIS WILL ONLY WORK IF THE GENE NAMES DO NOT START WITH A NUMBER!!

                    if (START_WITH_A_LETTER.matcher(line).matches() || line.startsWith("2") || line.startsWith("3") || line.startsWith("4")) {
                        // We have initialized InnedID (Protein) to 'null'. Unless it is the first 'protein' line in the file, the protein
                        // will be different from 'null'.

                        // There are places in the file that contain alternate proteins (alternate accession numbers or locus in this particular case).
                        // In these cases, they will not have peptides
                        if (protein != null && proteins.size() == 0) {

                            // We were already assembling a protein. So we need to store that one before proceeding with a new one. We add it to the HashMap
                            Object temp = allIds.put(protein.getIAccession(), protein); // ToDo: never used !?
//                            if(temp != null) {
//                                throw new RuntimeException("Protein '" + protein.getIAccession() + "' was reported twice in the file!." +
//                                        "\nThe values for this protein are: Description: " + protein.getIDescription() +
//                                        "\nCoverage: " + protein.getICoverage() +
//                                        "\nMolecular Weight: " + protein.getIMolecularWeight());
//                            }
                        }

                        // There will be already peptides here in this protein (getIPeptides().size()!=0)
                        if (protein != null && proteins.size() != 0 && protein.getIPeptides().size() != 0) {

                            Object temp = allIds.put(protein.getIAccession(), protein);  // ToDo: never used !?
//                            if(temp != null) {
//                                throw new RuntimeException("Protein '" + protein.getIAccession() + "' was reported twice in the file!." +
//                                        "\nThe values for this protein are: Description: " + protein.getIDescription() +
//                                        "\nCoverage: " + protein.getICoverage() +
//                                        "\nMolecular Weight: " + protein.getIMolecularWeight());
//                            }

                            // We were already assembling a protein. So we need to store that one before proceeding with a new one. We add it to the HashMap
                            for (PRIDEConverter.InnerID prot : proteins) {
                                prot.setIPeptides(protein.getIPeptides());
                                allIds.put(prot.getIAccession(), prot);
//                                if(allIds.containsKey(prot)) {
//                                    throw new RuntimeException("Protein '" + protein.getIAccession() + "' was reported twice in the file!." +
//                                            "\nThe values for this protein are: Description: " + protein.getIDescription() +
//                                            "\nCoverage: " + protein.getICoverage() +
//                                            "\nMolecular Weight: " + protein.getIMolecularWeight());
//                                }
                            }

                            //Empty the collection till the next cycle
                            proteins.clear();
                        }

                        // If it has no peptides, it will be necessary to copy the collection of proteins of the last created protein:
                        if (protein != null && protein.getIPeptides().size() == 0) {
                            proteins.add(protein);
                        }

                        // WE ARE USEING STRINGTOKENIZER HERE SINCE THERE ARE MISSING VALUES THAT WILL BE
                        // SKIPPED (EMPTY CELLS IN THE MIDDLE OF A LINE, FOR INSTANCE)
                        String[] proteinTokens = line.split(SEPARATOR);

                        // We have to take into account that the last token can be empty:
                        // In fact this is erroneus since if the last token is empty proteinTokens[9] will give and
                        // IndexOutOfBoundsException. Here, it works since there is no case in which this happens.
                        if (!(proteinTokens.length == 9)) {

                            if (debug) {
                                System.out.println("Current split protein tokens:");

                                for (String proteinToken : proteinTokens) {
                                    System.out.println(proteinToken);
                                }
                            }
                            PRIDEConverter.setCancelConversion(true);
                            throw new IOException("There were " + proteinTokens.length +
                                    " elements (instead of the expected 9) on line " + lineCount +
                                    " in your file ('" + properties.getDtaSelectFileName() + "')!");
                        } else {

                            accessionNumber = proteinTokens[0].trim(); //locus name is accession name

                            try {
                                sequenceCount = new Integer(proteinTokens[1].trim());
                            } catch (NumberFormatException nfe) {
                                Util.writeToErrorLog("Warning: The sequence count was not specified in the correct format!! " +
                                        "The token was: '" + proteinTokens[1] + "'");
                                nfe.printStackTrace();

                                // If there is no coverage, coverage will be null.
                                sequenceCount = null;
                            }

                            try {
                                spectrumCount = new Integer(proteinTokens[2].trim());
                            } catch (NumberFormatException nfe) {
                                Util.writeToErrorLog("Warning: The spectrum count count was not specified in the correct format!! " +
                                        "The token was: '" + proteinTokens[2] + "'");
                                nfe.printStackTrace();

                                // If there is no coverage, coverage will be null.
                                spectrumCount = null;
                            }

                            initialCoverage = proteinTokens[3].trim();

                            try {
                                coverage = new Double(initialCoverage.substring(0, initialCoverage.indexOf("%")));

                                // Coverage must be between 0 and 1 for the XML to be validated
                                coverage = coverage / 100;
                            } catch (NumberFormatException nfe) {
                                Util.writeToErrorLog("Warning: The coverage was not specified in the correct format!! " +
                                        "The token was: '" + proteinTokens[3] + "'");
                                nfe.printStackTrace();

                                // If there is no coverage, coverage will be null.
                                coverage = null;
                            }

                            try {
                                proteinLength = new Integer(proteinTokens[4].trim());

                            } catch (NumberFormatException nfe) {
                                Util.writeToErrorLog("Warning: The protein length count was not specified in the correct format!! " +
                                        "The token was: '" + proteinTokens[4] + "'");
                                nfe.printStackTrace();

                                // If there is no coverage, coverage will be null.
                                proteinLength = null;
                            }

                            molecularWeight = new Double(proteinTokens[5].trim());
                            pI = new Double(proteinTokens[6].trim());
                            validationStatus = proteinTokens[7].trim();
                            proteinDescription = proteinTokens[8].trim();

                            //We create a new protein instance:
                            protein = new PRIDEConverter.InnerID(accessionNumber, sequenceCount, spectrumCount, coverage, proteinLength,
                                    molecularWeight, pI, validationStatus, proteinDescription, "Sequest + DTASelect", databaseName);
                        }

                    // Now, the information for the PEPTIDES included in each protein identification (if(line.startsWith(" "))
                    } else {

                        String[] peptidesTokens = line.split(SEPARATOR);

                        // make sure we have the correct number of elements. As the first token is empty, we have to start
                        // taking into account what would be token 2 in principle (it is a bug in the string.split() method)
                        // The method does not consider empty spaces at the beginning and at the end of each line
                        // WE HAVE TO TAKE INTO ACCOUNT THE EXISTENCE OF 10 TOKENS FIRST (all of them since we are always going to skip the
                        // first empty one, it is always empty). But WE HAVE TO CONSIDER AS WELL THE EXISTENCE OF 9 TOKENS,
                        // SINCE EMPTY SPACES ARE FOUND SOMETIMES FOR THE LAST ONE ("Count").

                        // NB: Duplication of code exists here:
                        if (peptidesTokens.length == 12) {

                            // All seems to be OK. Let's parse the relevant information.
                            unique = peptidesTokens[0].trim(); // we do not really need this // ToDo: never used !?

                            // we will need to get the fileName information
                            fileName = peptidesTokens[1].trim();

                            // In order to use "." in the String split() method, it is neccesary to escape it twice.
                            String[] fileInfo = fileName.split("\\.");

                            // In most cases the name of the files has an appended "-a"
                            if (fileInfo[0].endsWith("-a")) {
                                spectrumFile = fileInfo[0].substring(0, fileInfo[0].indexOf("-a")).trim();
                            } else {
                                spectrumFile = fileInfo[0].trim();
                            }

                            scanNumber = new Long(fileInfo[1].trim());

                            // tokens 2 and 3 will be the same, we do not need this
                            charge = new Integer(fileInfo[3].trim());

                            sequestXcorr = new Double(peptidesTokens[2].trim());

                            // Again, we have to handle here the existence of a missing value for that parameter in the text file
                            try {
                                sequestDelta = new Double(peptidesTokens[3].trim());
                            } catch (NumberFormatException nfe) {
                                Util.writeToErrorLog("Warning: The sequestDelta parameter was not specified in the correct format!! " +
                                        "The token was: '" + peptidesTokens[3] + "'");
                                nfe.printStackTrace();
                                sequestDelta = null;
                            }

                            peptideMass = new Double(peptidesTokens[4].trim());
                            calculatedPeptideMass = new Double(peptidesTokens[5].trim());
                            totalIntensity = new Double(peptidesTokens[6].trim());

                            // The same again (possible missing value)
                            try {
                                sequestRSp = Integer.parseInt(peptidesTokens[7].trim());
                            } catch (NumberFormatException nfe) {
                                Util.writeToErrorLog("Warning: The RSp parameter was not specified in the correct format!! " +
                                        "The token was: '" + peptidesTokens[7] + "'");
                                nfe.printStackTrace();
                                sequestRSp = null;
                            }

                            sequestSp = new Double(peptidesTokens[8].trim());
                            ionProportion = new Double(peptidesTokens[9].trim());
                            redundancy = new Integer(peptidesTokens[10].trim());

                            // Peptide sequence:
                            String peptide = peptidesTokens[11].trim();

                            // In order to use "." in the String split() method, it is neccesary to escape it twice.
                            String[] peptideAA = peptide.split("\\.");
                            preDigSiteAA = null;
                            peptideSequence = null;
                            postDigSiteAA = null;

                            // In some cases the post aa after digestion is not indicated.
                            preDigSiteAA = peptideAA[0].trim();
                            peptideSequence = peptideAA[1].trim();

                            if (peptideAA.length == 3) {
                                postDigSiteAA = peptideAA[2].trim();
                            } else {
                                postDigSiteAA = "";
                            }


                            // *** HERE IT IS WHEN THE JOINT IS MADE WITH THE SPECTRA. I HAVE SEEN TWO POSSIBILITIES TO DO IT:
                            // Check if the spectrumFile_scanNumber is not present in the HashMap

                            // In this case, it is the second one:
                            String key = spectrumFile + "_" + scanNumber;

                            Long specRef = filenameToMzDataIDMapping.get(key);

                            if (debug) {
                                System.out.println("testing long value. The value is : " + specRef);
                            }

                            // check if the spectrum exists or not
                            // Remember that the code is duplicated below!!

                            if (specRef == null) {
                                JOptionPane.showMessageDialog(null,
                                        "The spectrum file " + spectrumFile + ".ms2" +
                                        "\ndoes not contain the spectrum with scan number " + scanNumber +
                                        "\nas referenced in the DTASelect txt file!\n\n" +
                                        "PRIDE XML file not created.",
                                        "Spectrum Not Found",
                                        JOptionPane.ERROR_MESSAGE);
                                Util.writeToErrorLog("Error when parsing DTASelect file. Unknown scan number: " + scanNumber + " in file " + spectrumFile);
                                PRIDEConverter.setCancelConversion(true);
                            }

                            if (!PRIDEConverter.isConversionCanceled()) {
                                // add the modifications
                                ArrayList peptideModifications = new ArrayList();

                                // adds the modification details to the peptideModifications list
                                // and returns the unmodified sequence
                                peptideSequence = PRIDEConverter.addModificationDetails(peptideModifications, peptideSequence,
                                        variableModifications, fixedModifications);

                                //Add information about each peptide to the protein instance
                                protein.addPeptide(specRef, charge, sequestXcorr, sequestDelta, peptideMass, calculatedPeptideMass,
                                        totalIntensity, sequestRSp, sequestSp, ionProportion, redundancy, peptideSequence,
                                        preDigSiteAA, postDigSiteAA, peptideModifications, null, null);

                                PRIDEConverter.setTotalPeptideCount(PRIDEConverter.getTotalPeptideCount() + 1); // increase the peptide counter
                            }
                        // If the first token is empty, the split method will not take it into account
                        } else if (peptidesTokens.length == 11) {

                            // All seems to be OK. Let's parse the relevant information.
                            unique = peptidesTokens[0].trim(); // we do not really need this  // ToDo: never used !?

                            // we will need to get the fileName information
                            fileName = peptidesTokens[0].trim();

                            // In order to use "." in the String split() method, it is neccesary to escape it twice.
                            String[] fileInfo = fileName.split("\\.");

                            // In most cases the name of the files has an appended "-a"
                            if (fileInfo[0].endsWith("-a")) {
                                spectrumFile = fileInfo[0].substring(0, fileInfo[0].indexOf("-a")).trim();
                            } else {
                                spectrumFile = fileInfo[0].trim();
                            }

                            scanNumber = new Long(fileInfo[1].trim());

                            // tokens 2 and 3 will be the same, we do not need this
                            charge = new Integer(fileInfo[3].trim());

                            sequestXcorr = new Double(peptidesTokens[1].trim());

                            // Again, we have to handle here the existence of a missing value for that parameter in the text file
                            try {
                                sequestDelta = new Double(peptidesTokens[2].trim());
                            } catch (NumberFormatException nfe) {
                                Util.writeToErrorLog("Warning: The sequestDelta parameter was not specified in the correct format!! " +
                                        "The token was: '" + peptidesTokens[2] + "'");
                                nfe.printStackTrace();
                                sequestDelta = null;
                            }

                            peptideMass = new Double(peptidesTokens[3].trim());
                            calculatedPeptideMass = new Double(peptidesTokens[4].trim());
                            totalIntensity = new Double(peptidesTokens[5].trim());

                            // The same again (possible missing value)
                            try {
                                sequestRSp = Integer.parseInt(peptidesTokens[6].trim());
                            } catch (NumberFormatException nfe) {
                                Util.writeToErrorLog("Warning: The RSp parameter was not specified in the correct format!! " +
                                        "The token was: '" + peptidesTokens[6] + "'");
                                nfe.printStackTrace();
                                sequestRSp = null;
                            }

                            sequestSp = new Double(peptidesTokens[7].trim());

                            ionProportion = new Double(peptidesTokens[8].trim());
                            redundancy = new Integer(peptidesTokens[9].trim());
                            String peptide = peptidesTokens[10].trim(); // peptide sequence

                            // In order to use "." in the String split() method, it is neccesary to escape it twice.
                            String[] peptideAA = peptide.split("\\.");
                            preDigSiteAA = null;
                            peptideSequence = null;
                            postDigSiteAA = null;

                            // In some cases the post aa after digestion is not indicated.
                            preDigSiteAA = peptideAA[0].trim();
                            peptideSequence = peptideAA[1].trim();

                            if (peptideAA.length == 3) {
                                postDigSiteAA = peptideAA[2].trim();
                            } else {
                                postDigSiteAA = "";
                            }

                            // Here the Link between the spectrum and the corresponding peptide (fileName does not contain "-a"):

                            // In this case, it is the second one:
                            String key = spectrumFile + "_" + scanNumber;
                            Long specRef = filenameToMzDataIDMapping.get(key);

                            if (debug) {
                                System.out.println("testing long value. The value is : " + specRef);
                            }

                            // check if the spectrum exists or not
                            // Remember that the code is duplicated above!!

                            if (specRef == null) {
                                JOptionPane.showMessageDialog(null,
                                        "The spectrum file " + spectrumFile + ".ms2" +
                                        "\ndoes not contain the spectrum with scan number " + scanNumber +
                                        "\nas referenced in the DTASelect txt file!\n\n" +
                                        "PRIDE XML file not created.",
                                        "Spectrum Not Found",
                                        JOptionPane.ERROR_MESSAGE);
                                Util.writeToErrorLog("Error when parsing DTASelect file. Unknown scan number: " + scanNumber + " in file " + spectrumFile);
                                PRIDEConverter.setCancelConversion(true);
                            }

                            if (!PRIDEConverter.isConversionCanceled()) {

                                // add the modifications
                                ArrayList peptideModifications = new ArrayList();

                                // adds the modification details to the peptideModifications list
                                // and returns the unmodified sequence
                                peptideSequence = PRIDEConverter.addModificationDetails(peptideModifications, peptideSequence,
                                        variableModifications, fixedModifications);

                                //Add information about each peptide to the protein instance
                                protein.addPeptide(specRef, charge, sequestXcorr, sequestDelta, peptideMass, calculatedPeptideMass,
                                        totalIntensity, sequestRSp, sequestSp, ionProportion, redundancy, peptideSequence,
                                        preDigSiteAA, postDigSiteAA, peptideModifications, null, null);

                                PRIDEConverter.setTotalPeptideCount(PRIDEConverter.getTotalPeptideCount() + 1); // increase the peptide counter
                            }
                        } else {

                            // In case the number of tokens is different:
                            if (debug) {
                                System.out.println("Current split peptide tokens: ");

                                for (String peptidesToken : peptidesTokens) {
                                    System.out.println(peptidesToken);
                                }

//                            throw new RuntimeException("Protein '" + protein.getIAccession() + "' was reported twice in the file!." +
//                                    "\nThe values for this protein are: Description: " + protein.getIDescription() +
//                                    "\nCoverage: " + protein.getICoverage() +
//                                    "\nMolecular Weight: " + protein.getIMolecularWeight());
                            }
                        }
                    }
                }

                // This is necesasry to add peptides to proteins that have alternative locus names.
                previousLine = line;
            }

            // FENCE-POST: fix last protein (which would be discarded otherwise).
            if (protein != null && proteins.size() == 0 && !PRIDEConverter.isConversionCanceled()) {

                // We were already assembling a protein. So we need to store that one before proceeding with a new one. We add it to the HashMap
                Object temp = allIds.put(protein.getIAccession(), protein); // ToDo: never used !?
//                if(temp != null) {
//                    throw new RuntimeException("Protein '" + protein.getIAccession() + "' was reported twice in the file!." +
//                            "\nThe values for this protein are: Description: " + protein.getIDescription() +
//                            "\nCoverage: " + protein.getICoverage() +
//                            "\nMolecular Weight: " + protein.getIMolecularWeight());
//                }
            }

            // There will be already peptides here in this protein (getIPeptides().size()!=0)
            if (protein != null && proteins.size() != 0 && protein.getIPeptides().size() != 0 && !PRIDEConverter.isConversionCanceled()) {

                // There will be already peptides here in this protein (getIPeptides().size()!=0)
                Object temp = allIds.put(protein.getIAccession(), protein); // ToDo: never used !?
//                if(temp != null) {
//                    throw new RuntimeException("Protein '" + protein.getIAccession() + "' was reported twice in the file!." +
//                            "\nThe values for this protein are: Description: " + protein.getIDescription() +
//                            "\nCoverage: " + protein.getICoverage() +
//                            "\nMolecular Weight: " + protein.getIMolecularWeight());
//                }

                // We were already assembling a protein. So we need to store that one before proceeding with a new one. We add it to the HashMap
                for (PRIDEConverter.InnerID prot : proteins) {
                    prot.setIPeptides(protein.getIPeptides());
                    Object temp2 = allIds.put(prot.getIAccession(), prot); // ToDo: never used !?
//                    if(temp2 != null) {
//                        throw new RuntimeException("Protein '" + protein.getIAccession() + "' was reported twice in the file!." +
//                                "\nThe values for this protein are: Description: " + protein.getIDescription() +
//                                "\nCoverage: " + protein.getICoverage() +
//                                "\nMolecular Weight: " + protein.getIMolecularWeight());
//                    }
                }

                //Empty the collection
                proteins.clear();
            }

            //If they have not peptides, it will be necessary to copy the collection of proteins of the last created protein:
            if (protein != null && protein.getIPeptides().size() == 0 && !PRIDEConverter.isConversionCanceled()) {
                proteins.add(protein);
            }

            // Right, all done reading.
            // Close reader.
            br.close();

            // Transform our temporary identifications into PRIDE Identification instances.
            PRIDEConverter.setIdentifications(new ArrayList<uk.ac.ebi.pride.model.interfaces.core.Identification>(allIds.size()));
            Iterator<PRIDEConverter.InnerID> iter = allIds.values().iterator();
            while (iter.hasNext() && !PRIDEConverter.isConversionCanceled()) {
                PRIDEConverter.InnerID lInnerID = iter.next();
                PRIDEConverter.getIdentifications().add(lInnerID.getGelFreeIdentification());
            }

            //******************FINISHED PARSING THE IDENTIFICATIONS FILE********************************************


            if (!PRIDEConverter.isConversionCanceled()) {

                // adding the CV and user params inserted in the previous steps of the converter

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

                // Create mzData instance.
                properties.setMzDataFile(
                        new MzDataImpl(
                        aTransformedSpectra,
                        null, // software compeletion time
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
                    null, "An error occured when parsing a DTASelect project.\n" +
                    "See ../Properties/ErrorLog.txt for more details.",
                    "Error Parsing DTASelect Project", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error Parsing DTASelect Project: ");
            e.printStackTrace();
        }

        if (PRIDEConverter.isConversionCanceled()) {
            progressDialog.setVisible(false);
            progressDialog.dispose();
        }

        return filenameToMzDataIDMapping;
    }
}
