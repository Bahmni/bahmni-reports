package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CodedObsCountConfig implements Config{
    private String conceptNames;
    private String ageGroupName;


    public String getConceptNames() {
        return conceptNames;
    }

    public void setConceptNames(String conceptNames) {
        this.conceptNames = conceptNames;
    }

    public String getAgeGroupName() {
        return ageGroupName;
    }

    public void setAgeGroupName(String ageGroupName) {
        this.ageGroupName = ageGroupName;
    }
}
