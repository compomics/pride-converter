/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 29-Aug-2006
 * Time: 08:58:33
 */
package uk.ac.ebi.tpp_to_pride.wrappers.peptideprophet;

import java.util.Collection;
/*
 * CVS information:
 *
 * $Revision: 1.1.1.1 $
 * $Date: 2007/01/12 17:17:10 $
 */

/**
 * This class wraps the MSMS run element of a PeptideProphet output XML file.
 *
 * @author martlenn
 * @version $Id: PeptideProphetMSMSRun.java,v 1.1.1.1 2007/01/12 17:17:10 lmartens Exp $
 */
public class PeptideProphetMSMSRun {

    private String name = null;

    private String massSpectrometerManufacturer = null;

    private String massSpectrometerModel = null;

    private String massSpectrometerIonSource = null;

    private String massSpectrometerMassAnalyzer = null;

    private String massSpectrometerDetector = null;

    private PeptideProphetEnzyme enzyme = null;

    private PeptideProphetSearch search_info = null;

    private Collection queries = null;


    public PeptideProphetMSMSRun(String aName, PeptideProphetEnzyme aEnzyme, String aMassSpectrometerDetector, String aMassSpectrometerIonSource, String aMassSpectrometerManufacturer, String aMassSpectrometerMassAnalyzer, String aMassSpectrometerModel, PeptideProphetSearch aSearchInfo, Collection aQueries) {
        name = aName;
        enzyme = aEnzyme;
        massSpectrometerDetector = aMassSpectrometerDetector;
        massSpectrometerIonSource = aMassSpectrometerIonSource;
        massSpectrometerManufacturer = aMassSpectrometerManufacturer;
        massSpectrometerMassAnalyzer = aMassSpectrometerMassAnalyzer;
        massSpectrometerModel = aMassSpectrometerModel;
        search_info = aSearchInfo;
        queries = aQueries;
    }

    public PeptideProphetEnzyme getEnzyme() {
        return enzyme;
    }

    public void setEnzyme(PeptideProphetEnzyme aEnzyme) {
        enzyme = aEnzyme;
    }

    public String getMassSpectrometerDetector() {
        return massSpectrometerDetector;
    }

    public void setMassSpectrometerDetector(String aMassSpectrometerDetector) {
        massSpectrometerDetector = aMassSpectrometerDetector;
    }

    public String getMassSpectrometerIonSource() {
        return massSpectrometerIonSource;
    }

    public void setMassSpectrometerIonSource(String aMassSpectrometerIonSource) {
        massSpectrometerIonSource = aMassSpectrometerIonSource;
    }

    public String getMassSpectrometerManufacturer() {
        return massSpectrometerManufacturer;
    }

    public void setMassSpectrometerManufacturer(String aMassSpectrometerManufacturer) {
        massSpectrometerManufacturer = aMassSpectrometerManufacturer;
    }

    public String getMassSpectrometerMassAnalyzer() {
        return massSpectrometerMassAnalyzer;
    }

    public void setMassSpectrometerMassAnalyzer(String aMassSpectrometerMassAnalyzer) {
        massSpectrometerMassAnalyzer = aMassSpectrometerMassAnalyzer;
    }

    public String getMassSpectrometerModel() {
        return massSpectrometerModel;
    }

    public void setMassSpectrometerModel(String aMassSpectrometerModel) {
        massSpectrometerModel = aMassSpectrometerModel;
    }

    public String getName() {
        return name;
    }

    public void setName(String aName) {
        name = aName;
    }

    public Collection getQueries() {
        return queries;
    }

    public void setQueries(Collection aQueries) {
        queries = aQueries;
    }

    public PeptideProphetSearch getSearch_info() {
        return search_info;
    }

    public void setSearch_info(PeptideProphetSearch aSearch_info) {
        search_info = aSearch_info;
    }
}
