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

    public static  Report find(String reportName, String reportConfigUrl,HttpClient httpClient) throws IOException, URISyntaxException {
        String data=httpClient.get(new URI(reportConfigUrl));
        ObjectMapper mapper=new ObjectMapper();
        Reports reports=mapper.readValue(data,Reports.class);
        for (Report report : reports.values()) {
            if (reportName.equals(report.getName())) {
                return report;
            }
        }
        return null;
    }
}
