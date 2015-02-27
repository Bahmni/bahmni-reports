package org.bahmni.reports.api;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.http.HttpServletResponse;
import java.io.FileReader;
import java.io.IOException;

public class ConfigReaderUtil {
    private static final Logger logger = Logger.getLogger(ConfigReaderUtil.class);
    private static final String CONFIG_FILE = "/var/www/bahmni_config/openmrs/apps/reports/reports.json";

    public static JSONObject findConfig(String reportName, HttpServletResponse response) {
        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(CONFIG_FILE));
            for (Object obj : jsonArray) {
                if (reportName.equals(((JSONObject) obj).get("name"))) {
                    return (JSONObject) obj;
                }
            }
        } catch (IOException | ParseException e) {
            logger.error("Error finding config for report " + reportName, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return null;
    }
}
