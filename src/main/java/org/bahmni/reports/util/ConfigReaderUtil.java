package org.bahmni.reports.util;

import org.bahmni.reports.model.Config;
import org.bahmni.reports.model.ReportConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class ConfigReaderUtil {

    private static final String CONFIG_FILE = "/var/www/bahmni_config/openmrs/apps/reports/reports.json";

    public static ReportConfig findConfig(String reportName) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Config config = objectMapper.readValue(new File(CONFIG_FILE), Config.class);
        for (ReportConfig reportConfig : config) {
            if (reportName.equals(reportConfig.getName())) {
                return reportConfig;
            }
        }
        return null;
    }
}
