package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientsWithLabtestResultsConfig implements Config {
    private List<String> conceptNames;
    private List<String> testOutcome;

    public List<String> getTestOutcome() {
        return testOutcome;
    }

    public void setTestOutcome(List<String> testOutcome) {
        this.testOutcome = testOutcome;
    }

    public List<String> getConceptNames() {
        return conceptNames;
    }

    public void setConceptNames(List<String> conceptNames) {
        this.conceptNames = conceptNames;
    }
}