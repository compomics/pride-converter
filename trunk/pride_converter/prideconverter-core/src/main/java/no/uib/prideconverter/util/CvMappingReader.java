package no.uib.prideconverter.util;

import no.uib.prideconverter.gui.SampleDetails;
import psidev.psi.tools.cvrReader.CvRuleReader;
import psidev.psi.tools.cvrReader.CvRuleReaderException;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMapping;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMappingRule;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvReference;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvTerm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: rafael
 * Date: 22-Jun-2011
 * Time: 10:49:50
 */
public class CvMappingReader {
    private CvMapping cvMapping;

    public CvMappingReader(String cvMappingXmlFilePath) {
        InputStream cvConfig = null;
        try {
            if(cvMappingXmlFilePath.indexOf("http://") != -1){
                try{
                    URL cvMappingUrl = new URL(cvMappingXmlFilePath);
                    URLConnection cvMappingConn = cvMappingUrl.openConnection();
                    cvConfig = cvMappingConn.getInputStream();
                } catch (Exception e){
                    Util.writeToErrorLog(e.getMessage());
                } finally {
                    if(cvConfig == null){
                        String cvMappingFileLocal = new CvMappingProperties().getPropertyValue("cvMappingFileLocal");
                        Util.writeToErrorLog("Error: Could not get mapping information from: " + cvMappingXmlFilePath);
                        Util.writeToErrorLog("Warning: Getting local cv-mapping file instead: " + cvMappingFileLocal);
                        cvConfig = SampleDetails.class.getResource(cvMappingFileLocal).openStream();
                    }
                }
            } else {
                cvConfig = SampleDetails.class.getResource(cvMappingXmlFilePath).openStream();
            }
            CvRuleReader cvRulesReader = new CvRuleReader();
            CvMapping cvMapping = null;
            try {
                cvMapping = cvRulesReader.read(cvConfig);
                setCvMapping(cvMapping);
            } catch (CvRuleReaderException e) {
                e.printStackTrace();
                Util.writeToErrorLog(e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Util.writeToErrorLog(e.getMessage());
        }
    }

    public Map<String, List<String>> getPreselectedTerms(String cvMappingRuleIdKeyword){
        Map<String, List<String>> preselectedTerms = new HashMap<String, List<String>>();
        List<CvTerm> cvTerms = new ArrayList<CvTerm>();
        List<CvMappingRule> cvMappingRules = cvMapping.getCvMappingRuleList().getCvMappingRule();
        for(CvMappingRule cvMappingRule:cvMappingRules){
            if(cvMappingRule.getId().indexOf(cvMappingRuleIdKeyword) != -1){
                cvTerms.addAll(cvMappingRule.getCvTerm());
            }
        }

        for(CvTerm cvTerm:cvTerms){
            String cvTermAccession = cvTerm.getTermAccession();
            String cvIdentifier = ((CvReference) cvTerm.getCvIdentifierRef()).getCvIdentifier();
            if(preselectedTerms.containsKey(cvIdentifier)){
                if(!preselectedTerms.get(cvIdentifier).contains(cvTermAccession)){
                    preselectedTerms.get(cvIdentifier).add(cvTermAccession);
                }
            } else {
                    List<String> newTerms = new ArrayList<String>();
                    newTerms.add(cvTermAccession);
                    preselectedTerms.put(cvIdentifier, newTerms);
            }
        }
        return preselectedTerms;
    }

    public CvMapping getCvMapping() {
        return cvMapping;
    }

    private void setCvMapping(CvMapping cvMapping) {
        if (cvMapping == null){
            String body = "The CvMapping cannot be null.";
            NullPointerException e = new NullPointerException(body);
            Util.writeToErrorLog(e.getMessage());
            throw e;
        }
        else if (cvMapping.getCvMappingRuleList() == null){
            String body = "The list of cv-mapping rules cannot be null.";
            NullPointerException e = new NullPointerException(body);
            Util.writeToErrorLog(e.getMessage());
            throw e;
        }
        else if (cvMapping.getCvReferenceList() == null){
            String body = "The cv-mapping reference list cannot be null.";
            NullPointerException e = new NullPointerException(body);
            Util.writeToErrorLog(e.getMessage());
            throw e;
        }
        this.cvMapping = cvMapping;
    }
}
