/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 29-Aug-2006
 * Time: 09:08:35
 */
package uk.ac.ebi.tpp_to_pride.wrappers.peptideprophet;

import java.util.HashMap;
/*
 * CVS information:
 *
 * $Revision: 1.1.1.1 $
 * $Date: 2007/01/12 17:17:10 $
 */

/**
 * This class wraps a PeptideProphet summary search element.
 *
 * @author martlenn
 * @version $Id: PeptideProphetSearch.java,v 1.1.1.1 2007/01/12 17:17:10 lmartens Exp $
 */
public class PeptideProphetSearch {

    /**
     * String zith the name of the search engine used.
     */
    private String search_engine = null;

    /**
     * String with the precursor mass type.
     */
    private String precursor_mass_type = null;

    /**
     * String with the fragment mass type.
     */
    private String fragment_mass_type = null;

    /**
     * String with the name of the search database.
     */
    private String search_db_name = null;

    /**
     * String with the type of the search database.
     */
    private String search_db_type = null;

    /**
     * The name of the enzyme used.
     */
    private String enzyme_name = null;

    /**
     * The maximum number of internal cleavages allowed for the enzyme.
     */
    private int max_number_internal_cleavages = -1;

    /**
     * The maximum number of internal cleavages allowed for the enzyme.
     */
    private int min_number_termini = -1;

    /**
     * HashMap of the modifications used, keyed by their symbol.
     */
    private HashMap modifications = null;

    /**
     * HashMap of the parameters recorded, keyed by name, value is value.
     */
    private HashMap parameters = null;

    /**
     * This constructor takes arguments read from the 'search_summary' section
     * in a PeptideProphet output file.
     *
     * @param aPrecursor_mass_type  String with the precursor mass type
     *                              ('average' or 'monoisotopic').
     * @param aFragment_mass_type   String with the fragment mass type.
     *                              ('average' or 'monoisotopic').
     * @param aEnzyme_name  String with the name of the search enzyme used.
     * @param aMax_number_internal_cleavages    int with the maximal number of
     *                                          allowed internal cleavages for
     *                                          the search enzyme used.
     * @param aMin_number_termini   int with the minimal number of correct enzymatic
     *                              termini a peptide should contain.
     * @param aModifications    HashMap with the modifications used. Keys should be the
     *                          modification code, values should be
     *                          PeptideProphetModification instances.
     * @param aParameters   HashMap with parameters. The keys should be parameter names,
     *                      the values parameter values.
     * @param aSearch_db_name   String with the database name.
     * @param aSearch_db_type   String with the databse type ('AA' or 'NA').
     * @param aSearch_engine    String with the name of he search engine used.
     */
    public PeptideProphetSearch(String aPrecursor_mass_type, String aFragment_mass_type, String aEnzyme_name, int aMax_number_internal_cleavages, int aMin_number_termini, HashMap aModifications, HashMap aParameters, String aSearch_db_name, String aSearch_db_type, String aSearch_engine) {
        precursor_mass_type = aPrecursor_mass_type;
        fragment_mass_type = aFragment_mass_type;
        enzyme_name = aEnzyme_name;
        max_number_internal_cleavages = aMax_number_internal_cleavages;
        min_number_termini = aMin_number_termini;
        modifications = aModifications;
        parameters = aParameters;
        search_db_name = aSearch_db_name;
        search_db_type = aSearch_db_type;
        search_engine = aSearch_engine;
    }

    public String getFragment_mass_type() {
        return fragment_mass_type;
    }

    public void setFragment_mass_type(String aFragment_mass_type) {
        fragment_mass_type = aFragment_mass_type;
    }

    public int getMax_number_internal_cleavages() {
        return max_number_internal_cleavages;
    }

    public void setMax_number_internal_cleavages(int aMax_number_internal_cleavages) {
        max_number_internal_cleavages = aMax_number_internal_cleavages;
    }

    public int getMin_number_termini() {
        return min_number_termini;
    }

    public void setMin_number_termini(int aMin_number_termini) {
        min_number_termini = aMin_number_termini;
    }

    public HashMap getModifications() {
        return modifications;
    }

    public void setModifications(HashMap aModifications) {
        modifications = aModifications;
    }

    public HashMap getParameters() {
        return parameters;
    }

    public void setParameters(HashMap aParameters) {
        parameters = aParameters;
    }

    public String getPrecursor_mass_type() {
        return precursor_mass_type;
    }

    public void setPrecursor_mass_type(String aPrecursor_mass_type) {
        precursor_mass_type = aPrecursor_mass_type;
    }

    public String getSearch_db_name() {
        return search_db_name;
    }

    public void setSearch_db_name(String aSearch_db_name) {
        search_db_name = aSearch_db_name;
    }

    public String getSearch_db_type() {
        return search_db_type;
    }

    public void setSearch_db_type(String aSearch_db_type) {
        search_db_type = aSearch_db_type;
    }

    public String getSearch_engine() {
        return search_engine;
    }

    public void setSearch_engine(String aSearch_engine) {
        search_engine = aSearch_engine;
    }

    public String getEnzyme_name() {
        return enzyme_name;
    }

    public void setEnzyme_name(String aEnzyme_name) {
        enzyme_name = aEnzyme_name;
    }
}
