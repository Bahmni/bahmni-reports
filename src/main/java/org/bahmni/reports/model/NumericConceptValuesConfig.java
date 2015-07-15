package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NumericConceptValuesConfig implements Config{
    private String conceptNames;
    private String ageGroupName;
    private String rangeGroupName;
    private Boolean countOncePerPatient;


    public String getAgeGroupName() {
        return ageGroupName;
    }

    public String getRangeGroupName() {
        return rangeGroupName;
    }

    public String getConceptNames() {
        return conceptNames;
    }

    public Boolean getCountOncePerPatient() { return countOncePerPatient; }
}