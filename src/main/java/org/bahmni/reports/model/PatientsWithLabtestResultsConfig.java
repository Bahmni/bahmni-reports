package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientsWithLabtestResultsConfig implements Config{
    private String conceptNames;
    private String abnormalityTypes;

    public String getAbnormalityTypes() {
        return abnormalityTypes;
    }

    public void setAbnormalityTypes(String abnormalityTypes) {
        this.abnormalityTypes = abnormalityTypes;
    }

    public String getConceptNames() {
        return conceptNames;
    }

    public void setConceptNames(String conceptNames) {
        this.conceptNames = conceptNames;
    }
}