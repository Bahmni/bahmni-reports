package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class SnomedDiagnosisReportConfig implements Config {
    private String snomedParentCode;
    private List<String> codes;

    public List<String> getCodes() {
        return codes;
    }

    public void setCodes(List<String> codes) {
        this.codes = codes;
    }

    public String getSnomedParentCode() {
        return snomedParentCode;
    }

    public void setSnomedParentCode(String snomedParentCode) {
        this.snomedParentCode = snomedParentCode;
    }
}
