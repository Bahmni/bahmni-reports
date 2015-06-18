package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientsWithLabtestResultsConfig implements Config{
    private String conceptNames;
    private String testOutcome;

    public String getTestOutcome() {
        return testOutcome;
    }

    public void setTestOutcome(String testOutcome) {
        this.testOutcome = testOutcome;
    }

    public String getConceptNames() {
        return conceptNames;
    }

    public void setConceptNames(String conceptNames) {
        this.conceptNames = conceptNames;
    }
}