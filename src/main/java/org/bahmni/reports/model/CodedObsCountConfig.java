package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CodedObsCountConfig implements Config{
    private String conceptNames;
    private String ageGroupName;
    private String visitTypes;


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

    public String getVisitTypes() {
        return visitTypes;
    }

    public void setVisitTypes(String visitTypes) {
        this.visitTypes = visitTypes;
    }
}
