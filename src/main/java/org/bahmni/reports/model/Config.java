package org.bahmni.reports.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config extends ArrayList<ReportConfig> {
    
}
