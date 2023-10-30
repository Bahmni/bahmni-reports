package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TSIntegrationDiagnosisCountReportConfig extends TSIntegrationDiagnosisReportConfig implements Config {
    private Boolean displayGenderGroup;

    public Boolean isDisplayGenderGroup() {
        return displayGenderGroup;
    }

    public void setDisplayGenderGroup(Boolean displayGenderGroup) {
        this.displayGenderGroup = displayGenderGroup;
    }
}