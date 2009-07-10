
package no.uib.prideconverter.util;

/**
 * Takes care of saving and retrieving the fragmentat ion ion CV mappings.
 *
 * @author  Harald Barsnes
 *
 * Created July 2009
 */
public class FragmentIonMappedDetails {

    private String cvParamName;
    private String cvParamAccession;
    private Integer charge;

    public FragmentIonMappedDetails(String name, String cvParamAccession, Integer charge){
        this.cvParamName = name;
        this.cvParamAccession = cvParamAccession;
        this.charge = charge;
    }

    /**
     * @return the name
     */
    public String getName() {
        return cvParamName;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.cvParamName = name;
    }

    /**
     * @return the charge
     */
    public Integer getCharge() {
        return charge;
    }

    /**
     * @param charge the charge to set
     */
    public void setCharge(Integer charge) {
        this.charge = charge;
    }

    /**
     * @return the cvParamAccession
     */
    public String getCvParamAccession() {
        return cvParamAccession;
    }

    /**
     * @param cvParamAccession the cvParamAccession to set
     */
    public void setCvParamAccession(String cvParamAccession) {
        this.cvParamAccession = cvParamAccession;
    }
}
