package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class SnomedDiagnosisReportConfig implements Config {
    private String snomedParentCode;

    public String getSnomedParentCode() {
        return snomedParentCode;
    }

    public void setSnomedParentCode(String snomedParentCode) {
        this.snomedParentCode = snomedParentCode;
    }
}
