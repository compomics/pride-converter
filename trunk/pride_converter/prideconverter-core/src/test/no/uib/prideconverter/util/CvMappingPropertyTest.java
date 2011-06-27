package no.uib.prideconverter.util;
import junit.framework.Assert;
import org.junit.Test;

/**
 * User: rafael
 * Date: 22-Jun-2011
 * Time: 15:06:22
 */
public class CvMappingPropertyTest {
    @Test
    public void testGetProperty(){
        String value = new CvMappingProperties().getDefaultMappingFile();
        Assert.assertTrue(value.length() > 0);
    }
}
