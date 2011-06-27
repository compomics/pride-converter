package no.uib.prideconverter.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.Properties;

/**
 * User: rafael
 * Date: 22-Jun-2011
 * Time: 14:42:38
 */
public final class CvMappingProperties {
    java.util.Properties properties;

    public CvMappingProperties() {
        properties = new Properties();
        try {
//            properties.load(CvMappingProperties.class.getResource("cvMapping.properties").openStream());
            properties.load(getClass().getResourceAsStream("/cvMapping.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            Util.writeToErrorLog(e.getMessage());
        }
    }

    public String getPropertyValue(String propertyKey) {
        return properties.getProperty(propertyKey);
    }
    public String getDefaultMappingFile(){
        return properties.getProperty(properties.getProperty("cvMappingFileDefault"));
    }
}
