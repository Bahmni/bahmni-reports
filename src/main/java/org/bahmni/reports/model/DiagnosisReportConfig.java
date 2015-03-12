package org.bahmni.reports.model;

import java.util.List;

public class DiagnosisReportConfig implements Config {

    private String ageGroupName;

    private String visitTypes;

    public String getAgeGroupName() {
        return ageGroupName;
    }

    public void setAgeGroupName(String ageGroupName) {
        this.ageGroupName = ageGroupName;
    }

    public void setVisitTypes(String visitTypes) {
        this.visitTypes = visitTypes;
    }

    public String getVisitTypes() {
        return visitTypes;
    }
}

