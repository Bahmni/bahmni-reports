package org.bahmni.reports.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.reports.model.Config;
import org.bahmni.reports.model.ReportConfig;

import java.io.File;
import java.io.IOException;

public class ConfigReaderUtil {

    public ReportConfig findConfig(String reportName, String reportPropertiesPath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Config config = objectMapper.readValue(new File(reportPropertiesPath), Config.class);
        for (ReportConfig reportConfig : config) {
            if (reportName.equals(reportConfig.getName())) {
                return reportConfig;
            }
        }
        return null;
    }
}
