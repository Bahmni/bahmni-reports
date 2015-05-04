package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NumericConceptValuesConfig implements Config{
    private String conceptNames;
    private String ageGroupName;
    private String rangeGroupName;


    public String getAgeGroupName() {
        return ageGroupName;
    }

    public void setAgeGroupName(String ageGroupName) {
        this.ageGroupName = ageGroupName;
    }

    public String getRangeGroupName() {
        return rangeGroupName;
    }

    public void setRangeGroupName(String rangeGroupName) {
        this.rangeGroupName = rangeGroupName;
    }

    public String getConceptNames() {
        return conceptNames;
    }

    public void setConceptNames(String conceptNames) {
        this.conceptNames = conceptNames;
    }
}