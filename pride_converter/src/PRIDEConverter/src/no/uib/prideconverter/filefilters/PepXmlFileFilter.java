package no.uib.prideconverter.filefilters;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
 * File filter for *.pep.xml and *.pepxml files.
 *
 * Created March 2008
 * 
 * @author  Harald Barsnes
 */
public class PepXmlFileFilter extends FileFilter {
    
    /**
     * Accept all directories, *.pep.xml and *.pepxml files.
     *
     * @param f
     * @return boolean
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        
        if(f.getPath().endsWith(FileFilterUtils.pep_xml) || 
                f.getPath().endsWith(FileFilterUtils.PEP_XML) ||
                f.getPath().endsWith(FileFilterUtils.PEP_XML) ||
                f.getPath().endsWith(FileFilterUtils.pepxml)){
            return true;
        } else{
            return false;
        }
    }
    
    /**
     * The description of this filter
     *
     * @return String
     */
    public java.lang.String getDescription() {
        return "*.pep.xml";
    }
}