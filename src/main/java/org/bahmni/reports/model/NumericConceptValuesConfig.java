package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NumericConceptValuesConfig implements Config {
    private List<String> conceptNames;
    private String ageGroupName;
    private String rangeGroupName;
    private Boolean countOncePerPatient;


    public String getAgeGroupName() {
        return ageGroupName;
    }

    public String getRangeGroupName() {
        return rangeGroupName;
    }

    public List<String> getConceptNames() {
        return conceptNames;
    }

    public void setConceptNames(List<String> conceptNames) {
        this.conceptNames = conceptNames;
    }

    public void setAgeGroupName(String ageGroupName) {
        this.ageGroupName = ageGroupName;
    }

    public void setRangeGroupName(String rangeGroupName) {
        this.rangeGroupName = rangeGroupName;
    }

    public Boolean isCountOncePerPatient() {
        return countOncePerPatient;
    }

    public void setCountOncePerPatient(Boolean countOncePerPatient) {
        this.countOncePerPatient = countOncePerPatient;
    }

    public Boolean getCountOncePerPatient() { return countOncePerPatient; }
}