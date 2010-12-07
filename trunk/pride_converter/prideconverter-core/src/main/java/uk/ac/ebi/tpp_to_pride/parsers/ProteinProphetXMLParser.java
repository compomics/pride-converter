/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 21-Aug-2006
 * Time: 13:26:36
 */
package uk.ac.ebi.tpp_to_pride.parsers;

import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet.ProteinProphetIsoform;
import uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet.ProteinProphetPeptideID;
import uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet.ProteinProphetProteinID;
import uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet.ProteinProphetSummary;

import java.io.IOException;
import java.util.*;
/*
 * CVS information:
 *
 * $Revision: 1.1.1.1 $
 * $Date: 2007/01/12 17:17:10 $
 */

/**
 * This class uses the XmlPullParser to read a ProteinProphet output XML
 * file into an object model.
 *
 * @author martlenn
 * @version $Id: ProteinProphetXMLParser.java,v 1.1.1.1 2007/01/12 17:17:10 lmartens Exp $
 * Modified by Harald Barsnes (November 2008)
 */
public class ProteinProphetXMLParser {

    private static final String PROTEIN_SUMMARY_HEADER = "protein_summary_header";
    private static final String PROTEIN = "protein";
    private static final String PROTEIN_GROUP = "protein_group";
    private static final String PEPTIDE = "peptide";
    private static final String MODIFICATION_INFO = "modification_info";
    private static final String PEPTIDE_PARENT_PROTEIN = "peptide_parent_protein";
    private static final String ANALYSIS_RESULT = "analysis_result";
    private static final String PARAMETER = "parameter";

    /**
	 * Define a static logger variable.
	 */
	private static Logger logger = Logger.getLogger(ProteinProphetXMLParser.class);

    /**
     * Protein prophet summary information is kept here.
     */
    private ProteinProphetSummary proteinProphetSummary = null;

    /**
     * This method is the workhorse of this class. It takes an initialized XmlPullParser
     * instance to start reading the ProteinProphet output file from.
     *
     * @param aParser   XmlPullParserPlus from which the output shall be read.
     * @param aThreshold    double with the 'probability' threshold for acceptance
     *                      of a protein identification - any protein with a probability
     *                      equal to or greater than this value will be retained in the
     *                      resulting Collection.
     * @return  Collection of ProteinProphetProteinID instances with all the proteins.
     * @throws XmlPullParserException   when the XML parsing generated an error.
     * @throws IOException  when the output file could not be read.
     */
    public Collection readProteinProphet(XmlPullParserPlus aParser, double aThreshold) throws XmlPullParserException, IOException {
        ArrayList result = new ArrayList();

        int eventType = -1;
        while ((eventType = aParser.getEventType()) != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    logger.debug("Document start encountered.");
                    aParser.next();
                    break;
                case XmlPullParser.START_TAG:
                    String start = aParser.getName();
                    // Note that we check the version here.
                    if(PROTEIN_SUMMARY_HEADER.equals(start)) {
                        // Get the attributes.
                        String reference_database = aParser.getAttributeValue(null, "reference_database");
                        // Strip out paths.
                        if(reference_database.indexOf("/") >= 0) {
                            reference_database = reference_database.substring(reference_database.lastIndexOf("/")+1);
                        }
                        // If possible, split reference database in name and version.
                        String db_version = null;
                        if(reference_database.indexOf(".v") >= 0) {
                            db_version = reference_database.substring(reference_database.indexOf(".v") + 2);
                            // Also remove some clutter from the remainder.
                            reference_database = reference_database.substring(0, reference_database.lastIndexOf(".", reference_database.indexOf(".v")-2));
                        }
                        String residue_substitution_list = aParser.getAttributeValue(null, "residue_substitution_list");
                        // Let's parse these.
                        StringTokenizer st = new StringTokenizer(residue_substitution_list, ",");
                        HashMap substitutions = null;
                        while(st.hasMoreTokens()) {
                            String substitution = st.nextToken().trim();
                            if(substitutions == null) {
                                substitutions = new HashMap();
                            }
                            StringTokenizer stInner = new StringTokenizer(substitution, "-> ");
                            String key = stInner.nextToken().trim();
                            String value = stInner.nextToken().trim();
                            substitutions.put(key, value);
                        }
                        String organism = aParser.getAttributeValue(null, "organism");
                        // We'll also read the next tag, which is the software summary.
                        aParser.moveToNextStartTag(true);
                        String program = aParser.getAttributeValue(null, "analysis");
                        String version = aParser.getAttributeValue(null, "version");
                        // Init the summary.
                        proteinProphetSummary = new ProteinProphetSummary(reference_database, db_version, organism, program, version, substitutions);
                        aParser.next();
                    } else if(PROTEIN_GROUP.equals(start)) {
                        aParser.moveToNextStartTag(true);
                        Collection proteinIDs = parseProteinGroup(aParser, aThreshold);
                        result.addAll(proteinIDs);
                    } else {
                        aParser.next();
                    }
                    break;
                case XmlPullParser.END_TAG:
                    aParser.next();
                    break;
                case XmlPullParser.TEXT:
                    aParser.next();
                    break;
                default:
                    aParser.next();
                    break;
            }
        }
        // Signal completion.
        logger.debug("Document end encountered.");

        return result;
    }

    /**
     * This method reports on the ProteinProphet summary.
     *
     * @return  ProteinProphetSummary instance with some general information.
     */
    public ProteinProphetSummary getProteinProphetSummary() {
        return proteinProphetSummary;
    }

    /**
     * This method specifically parses a 'protein_group' element into a Collection
     * of ProteinProphetProteinID instances. All 'protein' elements will be parsed,
     * but only those that pass the 'probability' threshold (probability equal to
     * or greater than the one specified) will be retained in the resulting Collection.
     *
     * @param aParser   XmlPullParserPlus set on the first 'protein' element in the group.
     * @param aThreshold    double with the 'probability' threshold for acceptance
     *                      of a protein identification - any protein with a probability
     *                      equal to or greater than this value will be retained in the
     *                      resulting Collection.
     * @return  Collection of ProteinProphetProteinID instances that pass the threshold.
     * @throws XmlPullParserException   when the XML parsing generated an error.
     * @throws IOException  when the output file could not be read.
     */
    private Collection parseProteinGroup(XmlPullParserPlus aParser, double aThreshold) throws XmlPullParserException, IOException {
        ArrayList proteinIDs = new ArrayList();

        // Chances are there are multiple proteins to be picked up here.
        // We'll parse all of them in full, and perform the filtering out at the point
        // of inclusion into the arraylist.
        while(PROTEIN.equals(aParser.getName())) {
            ProteinProphetProteinID proteinID = parseProtein(aParser);
            // Filtering is applied here.
            // Cutoff is currently set at strictly less than 0.9 in probability.
            if(proteinID.getProbability() >= aThreshold) {
                proteinIDs.add(proteinID);
            }
        }

        // OK, we're through!
        return proteinIDs;
    }

    /**
     * This method specifically parses a 'protein' element into a ProteinProphetProteinID
     * instance.
     *
     * @param aParser   XmlPullParserPlus set on the 'protein' element to parse.
     * @return  Collection of ProteinProphetProteinID instances that pass the threshold.
     * @throws XmlPullParserException   when the XML parsing generated an error.
     * @throws IOException  when the output file could not be read.
     */
    private ProteinProphetProteinID parseProtein(XmlPullParserPlus aParser) throws XmlPullParserException, IOException {
        // Now we're centered on the 'protein' tag.
        // We'll need to collect attributes here.
        String accession = aParser.getAttributeValue(null, "protein_name");
        int isoformCount = Integer.parseInt(aParser.getAttributeValue(null, "n_indistinguishable_proteins"));
        double probability = Double.parseDouble(aParser.getAttributeValue(null, "probability"));
        double percent_coverage = -1;
//        // see if we have a 'percent_coverage' attribute. If so, parse it.
//        if(aParser.getAttributeValue(null, "percent_coverage") != null) {
//            percent_coverage = Double.parseDouble(aParser.getAttributeValue(null, "percent_coverage"));
//            if (percent_coverage < 0 || percent_coverage > 1) {
//                throw new IllegalStateException("Coverage has to be a double value between 0 and 1! But is: " + percent_coverage);
//            }
//        }
        String peptidesLine = aParser.getAttributeValue(null, "unique_stripped_peptides");
        int totPeptides = Integer.parseInt(aParser.getAttributeValue(null, "total_number_peptides"));

        aParser.moveToNextStartTag(true);
        
        // skip any parameter and analysis result tags
        while(!aParser.getName().equalsIgnoreCase("annotation")) {
            aParser.moveToNextStartTag(true);
        }
                
        // This tag should be 'annotation'.
        // Read attributes here as well.
        String description = aParser.getAttributeValue(null, "protein_description");

        // Now, if we have any isoforms (number of indistinguishable proteins > 1),
        // we'll have to read those here as well.
        ArrayList isoforms = null;
        int isoformCountDown = isoformCount;
        while(isoformCountDown > 1) {
            if(isoforms == null) {
                isoforms = new ArrayList(isoformCount-1);
            }
            aParser.moveToNextStartTag(true);

            // This should be an indistinguishable protein tag.
            String isoform_accession = aParser.getAttributeValue(null, "protein_name");
            // Move on to the annotation for this element.
            aParser.moveToNextStartTag(true);
            String isoform_description = aParser.getAttributeValue(null, "protein_description");
            isoforms.add(new ProteinProphetIsoform(isoform_accession, isoform_description));
            isoformCountDown--;
        }

        // Now we start with the peptides section.
        HashMap peptides = new HashMap();
        aParser.moveToNextStartTag(true);

        while(PEPTIDE.equals(aParser.getName())) {
            parsePeptide(aParser, peptides);
        }
        // OK, all peptides parsed.
        // We should now be on the next start tag that is not 'peptide',
        // which is precisely what the outer loop expects - so we're done!
        ProteinProphetProteinID proteinID = new ProteinProphetProteinID(accession, description, peptides, percent_coverage, probability, isoforms);

        return proteinID;
    }

    /**
     * This method specifically parses a 'peptide' element into a ProteinProphetPeptideID
     * instance and then adds this peptide to the specified HashMap, keyed by
     * 'charge' [space] 'modified_sequence' (eg.: '3 M[147]SFVATER')
     *
     * @param aParser   XmlPullParserPlus set on the first 'protein' element in the group.
     * @param aPeptides HashMap to which the parsed ProteinProphetPeptideID instance
     *                  will be added, keyed by:
     *                  'charge' [space] 'modified_sequence' (eg.: '3 M[147]SFVATER').
     * @throws XmlPullParserException   when the XML parsing generated an error.
     * @throws IOException  when the output file could not be read.
     */
    private void parsePeptide(XmlPullParserPlus aParser, HashMap aPeptides) throws XmlPullParserException, IOException {
        
        // We are on a peptide tag here, so we should
        // start by gathering attributes.
        String sequence = aParser.getAttributeValue(null, "peptide_sequence");

        Integer charge = null;

        if(aParser.getAttributeValue(null, "charge") != null){
            charge = Integer.parseInt(aParser.getAttributeValue(null, "charge"));
        }
        
        int count = Integer.parseInt(aParser.getAttributeValue(null, "n_instances"));

        String modified_sequence = null;

        // Next tag is modifications, if any.
        aParser.moveToNextStartTag(true);
        
        // Check whether it is a modification.
        if(MODIFICATION_INFO.equals(aParser.getName())) {
            // Get the correct attribute tag.
            modified_sequence = aParser.getAttributeValue(null, "modified_peptide");
            aParser.moveToNextStartTagAfterTag(MODIFICATION_INFO);
        } else {
            modified_sequence = sequence;
        }

        List parentProteins = new ArrayList();
        // Now see if we're on the peptide parent protein element.
        while(PEPTIDE_PARENT_PROTEIN.equals(aParser.getName())) {
            String parent_protein = aParser.getAttributeValue(null, "protein_name");
            parentProteins.add(parent_protein);
            aParser.moveToNextStartTag(true);
        }
        
        // OK, peptide read.
        ProteinProphetPeptideID peptideID = new ProteinProphetPeptideID(charge, count, modified_sequence, sequence, parentProteins);

        aPeptides.put(sequence, peptideID);
    }
}
