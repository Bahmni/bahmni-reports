package org.bahmni.reports.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpClient;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Reports extends HashMap<String, Report> {

    public static Report find(String reportName, String reportConfigUrl) throws IOException {
        HttpClient httpClient=new HttpClient(new ConnectionDetails(null,null,null,5000,5000));
        Reports reports=httpClient.get(reportConfigUrl,Reports.class);
        for (Report report : reports.values()) {
            if (reportName.equals(report.getName())) {
                return report;
            }
        }
        return null;
    }
}
