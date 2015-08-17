package org.bahmni.reports.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Reports extends HashMap<String, Report> {

    public static Report find(String reportName, String reportPropertiesPath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Reports reports = objectMapper.readValue(new File(reportPropertiesPath), Reports.class);
        for (Report report : reports.values()) {
            if (reportName.equals(report.getName())) {
                return report;
            }
        }
        return null;
    }
}
