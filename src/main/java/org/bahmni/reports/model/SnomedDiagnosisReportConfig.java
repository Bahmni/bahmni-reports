package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class SnomedDiagnosisReportConfig implements Config {
    private String snomedParentCode;

    private Boolean displaySnomedCode;

    private Boolean displayGenderGroup;

    public String getSnomedParentCode() {
        return snomedParentCode;
    }

    public void setSnomedParentCode(String snomedParentCode) {
        this.snomedParentCode = snomedParentCode;
    }

    public Boolean isDisplaySnomedCode() {
        return displaySnomedCode;
    }

    public void setDisplaySnomedCode(Boolean displaySnomedCode) {
        this.displaySnomedCode = displaySnomedCode;
    }

    public Boolean isDisplayGenderGroup() {
        return displayGenderGroup;
    }

    public void setDisplayGenderGroup(Boolean displayGenderGroup) {
        this.displayGenderGroup = displayGenderGroup;
    }
}
