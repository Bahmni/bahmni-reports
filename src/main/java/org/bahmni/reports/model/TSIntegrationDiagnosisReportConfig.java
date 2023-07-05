package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TSIntegrationDiagnosisReportConfig {
    private String tsConceptSource;
    private String conceptNameDisplayFormat;
    private String terminologyParentCode;
    private String terminologyColumnName;
    private Boolean displayTerminologyCode;

    public String getTsConceptSource() {
        return tsConceptSource;
    }

    public void setTsConceptSource(String tsConceptSource) {
        this.tsConceptSource = tsConceptSource;
    }

    public String getConceptNameDisplayFormat() {
        return conceptNameDisplayFormat;
    }

    public void setConceptNameDisplayFormat(String conceptNameDisplayFormat) {
        this.conceptNameDisplayFormat = conceptNameDisplayFormat;
    }

    public String getTerminologyParentCode() {
        return terminologyParentCode;
    }

    public void setTerminologyParentCode(String terminologyParentCode) {
        this.terminologyParentCode = terminologyParentCode;
    }

    public Boolean isDisplayTerminologyCode() {
        return displayTerminologyCode;
    }

    public void setDisplayTerminologyCode(Boolean displayTerminologyCode) {
        this.displayTerminologyCode = displayTerminologyCode;
    }

    public String getTerminologyColumnName() {
        return terminologyColumnName;
    }

    public void setTerminologyColumnName(String terminologyColumnName) {
        this.terminologyColumnName = terminologyColumnName;
    }


}
