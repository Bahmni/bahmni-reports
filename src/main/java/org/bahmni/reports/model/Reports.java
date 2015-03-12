package org.bahmni.reports.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Reports extends ArrayList<Report> {

    public static Report find(String reportName, String reportPropertiesPath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Reports reports = objectMapper.readValue(new File(reportPropertiesPath), Reports.class);
        for (Report report : reports) {
            if (reportName.equals(report.getName())) {
                return report;
            }
        }
        return null;
    }
}
