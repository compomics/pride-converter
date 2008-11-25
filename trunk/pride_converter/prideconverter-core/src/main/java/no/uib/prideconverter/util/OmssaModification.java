package no.uib.prideconverter.util;

import java.util.Vector;

/**
 * This class contains the details on a OMSSA modification.
 *
 * @author  Harald Barsnes
 * 
 * Created August 2008
 */
public class OmssaModification {

    private Integer modNumber;
    private String modName;
    private Double modMonoMass;
    private Vector<String> modResidues;

    /**
     * Creates a new OmssaModification object.
     * 
     * @param modNumber
     * @param modName
     * @param modMonoMass
     * @param modResidues
     */
    public OmssaModification(Integer modNumber, String modName,
            Double modMonoMass, Vector<String> modResidues) {
        this.modNumber = modNumber;
        this.modName = modName;
        this.modMonoMass = modMonoMass;
        this.modResidues = modResidues;
    }
    
    /**
     * Returns the modification number
     * 
     * @return the modification number
     */
    public Integer getModNumber(){
        return modNumber;
    }
    
    /**
     * Returns the modification name
     * 
     * @return the modification name
     */
    public String getModName(){
        return modName;
    }
    
    /**
     * Returns the modification mass
     * 
     * @return the modification mass
     */
    public Double getModMonoMass(){
        return modMonoMass;
    }
    
    /**
     * Returns the modified residues
     * 
     * @return the modified residues
     */
    public Vector<String> getModResidues(){
        return modResidues;
    }
    
    /**
     * Returns the modified residues as a String
     * 
     * @return the modified residues
     */
    public String getModResiduesAsString(){
        
        String temp = "";
        
        for(int i=0; i<modResidues.size(); i++){
            temp += modResidues.get(i) + ", ";
        }
        
        temp = temp.substring(0, temp.length()-2);
        
        return temp;
    }
}
