/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 22-Sep-2006
 * Time: 13:06:42
 */
package uk.ac.ebi.tpp_to_pride.wrappers.proteinprophet;

import java.util.HashMap;
/*
 * CVS information:
 *
 * $Revision: 1.1.1.1 $
 * $Date: 2007/01/12 17:17:10 $
 */

/**
 * This class represents summary information from the ProteinProphet XML output.
 *
 * @author martlenn
 * @version $Id: ProteinProphetSummary.java,v 1.1.1.1 2007/01/12 17:17:10 lmartens Exp $
 */
public class ProteinProphetSummary {

    /**
     * The name (and verison) of the source database.
     */
    private String sourceDatabase = null;

    /**
     * The version of the DB.
     */
    private String dbVersion = null;

    /**
     * The organism for which the searches were performed.
     */
    private String organism = null;

    /**
     * HashMap with potential substitions. Can be 'null' for no substitions.
     */
    private HashMap substitions = null;

    /**
     * The software application used (should probably be ProteinProphet...)
     */
    private String software = null;

    /**
     * Te version of the software used.
     */
    private String software_version = null;

    /**
     * This constructor takes all relevant attributes for the summary.
     *
     * @param aSourceDatabase   String with the source database.
     * @param aDBVersion   String with the version of the source database.
     * @param aOrganism String with the organism.
     * @param aSoftware String with the software version
     * @param aSoftwareVersion  String with the software version
     * @param aSubstitions  HashMap with the substitutions (can be 'null' for none).
     */
    public ProteinProphetSummary(String aSourceDatabase, String aDBVersion, String aOrganism, String aSoftware, String aSoftwareVersion, HashMap aSubstitions) {
        sourceDatabase = aSourceDatabase;
        dbVersion = aDBVersion;
        organism = aOrganism;
        software = aSoftware;
        software_version = aSoftwareVersion;
        substitions = aSubstitions;
     }

    public String getSourceDatabase() {
        return sourceDatabase;
    }

    public void setSourceDatabase(String aSourceDatabase) {
        sourceDatabase = aSourceDatabase;
    }

    public String getOrganism() {
        return organism;
    }

    public void setOrganism(String aOrganism) {
        organism = aOrganism;
    }

    public HashMap getSubstitions() {
        return substitions;
    }

    public void setSubstitions(HashMap aSubstitions) {
        substitions = aSubstitions;
    }

    public String getSoftware() {
        return software;
    }

    public void setSoftware(String aSoftware) {
        software = aSoftware;
    }

    public String getSoftware_version() {
        return software_version;
    }

    public void setSoftware_version(String aSoftware_version) {
        software_version = aSoftware_version;
    }

    public String getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(String aDbVersion) {
        dbVersion = aDbVersion;
    }
}
