package no.uib.prideconverter.util;
import org.junit.Test;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMapping;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * User: rafael
 * Date: 22-Jun-2011
 * Time: 11:56:32
 */
public class CvMappingReaderTest {
    String cvMappingFile;
    List<String> cvMappingRuleIdKeywords;

    public CvMappingReaderTest() {
        CvMappingProperties prop = new CvMappingProperties();
        this.cvMappingFile = prop.getDefaultMappingFile();
        cvMappingRuleIdKeywords = new ArrayList<String>();
        cvMappingRuleIdKeywords.add(prop.getPropertyValue("sampleKeyword"));
        cvMappingRuleIdKeywords.add(prop.getPropertyValue("protocolKeyword"));
        cvMappingRuleIdKeywords.add(prop.getPropertyValue("instrumentProcessingMethodKeyword"));
        cvMappingRuleIdKeywords.add(prop.getPropertyValue("instrumentSourceKeyword"));
        cvMappingRuleIdKeywords.add(prop.getPropertyValue("instrumentAnalyzerKeyword"));
        cvMappingRuleIdKeywords.add(prop.getPropertyValue("instrumentDetectorKeyword"));
    }

    @Test
    public void testCvMapping(){
        CvMapping cvMapping = new CvMappingReader(cvMappingFile).getCvMapping();
        Assert.assertTrue("There are no rules in " + cvMappingFile, cvMapping.getCvMappingRuleList().getCvMappingRule().size() > 0);
    }

    @Test
    public void testPreselectedTerms(){
        CvMappingReader cvMappingReader = new CvMappingReader(cvMappingFile);
        for(String cvMappingRuleIdKeyword:cvMappingRuleIdKeywords){
            Map<String, List<String>> preselectedTerms = cvMappingReader.getPreselectedTerms(cvMappingRuleIdKeyword);
            Assert.assertTrue(cvMappingRuleIdKeyword + " rules should have some preselected terms", preselectedTerms.size() > 0);
        }
    }
    
}
