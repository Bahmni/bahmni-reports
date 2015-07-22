package org.bahmni.reports.model;

import java.util.List;

public class DiagnosisReportConfig implements Config {

    private String ageGroupName;

    private List<String> visitTypes;

    public String getAgeGroupName() {
        return ageGroupName;
    }

    public void setAgeGroupName(String ageGroupName) {
        this.ageGroupName = ageGroupName;
    }

    public List<String> getVisitTypes() {
        return visitTypes;
    }

    public void setVisitTypes(List<String> visitTypes) {
        this.visitTypes = visitTypes;
    }
}

