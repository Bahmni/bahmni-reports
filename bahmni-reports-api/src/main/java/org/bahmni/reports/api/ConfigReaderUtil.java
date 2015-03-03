package org.bahmni.reports.api;

import org.apache.log4j.Logger;
import org.bahmni.reports.api.model.Config;
import org.bahmni.reports.api.model.ReportConfig;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileReader;
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
