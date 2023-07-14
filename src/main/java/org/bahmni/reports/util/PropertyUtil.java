package org.bahmni.reports.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtil {
    static Logger logger = LogManager.getLogger(PropertyUtil.class);

    public static Properties loadProperties(String propertiesFileName) {
        try (InputStream in = PropertyUtil.class.getClassLoader().getResourceAsStream(propertiesFileName)) {
            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (IOException e) {
            logger.error("Could not load properties from: " + propertiesFileName, e);
            throw new RuntimeException(e);
        }
    }
}
