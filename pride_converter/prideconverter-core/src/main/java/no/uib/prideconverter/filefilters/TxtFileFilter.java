package no.uib.prideconverter.filefilters;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * File filter for *.txt files.
 * 
 * Created February 2009
 * 
 * @author  Harald Barsnes
 */
public class TxtFileFilter extends FileFilter {

    /**
     * Accept all directories, *.txt files.
     *
     * @param f
     * @return boolean
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = FileFilterUtils.getExtension(f);
        if (extension != null) {
            if (extension.equals(FileFilterUtils.txt) ||
                    extension.equals(FileFilterUtils.TXT)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * The description of this filter
     *
     * @return String
     */
    public java.lang.String getDescription() {
        return "*.txt";
    }
}
