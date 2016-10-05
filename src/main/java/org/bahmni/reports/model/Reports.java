package org.bahmni.reports.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
