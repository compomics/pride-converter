package no.uib.prideconverter.filefilters;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
 * File filter for *.prot.xml and *.protxml files.
 *
 * Created March 2008
 * 
 * @author  Harald Barsnes
 */
public class ProtXmlFileFilter extends FileFilter {
    
    /**
     * Accept all directories, *.prot.xml and *.protxml files.
     *
     * @param f
     * @return boolean
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        
        if(f.getPath().endsWith(FileFilterUtils.prot_xml) || 
                f.getPath().endsWith(FileFilterUtils.PROT_XML) ||
                f.getPath().endsWith(FileFilterUtils.PROT_XML) ||
                f.getPath().endsWith(FileFilterUtils.protxml)){
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
        return "*.prot.xml";
    }
}