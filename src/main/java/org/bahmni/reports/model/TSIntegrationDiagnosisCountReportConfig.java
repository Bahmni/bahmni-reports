package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
<<<<<<<< HEAD:src/main/java/org/bahmni/reports/model/TSIntegrationDiagnosisCountReportConfig.java
public class TSIntegrationDiagnosisCountReportConfig implements Config {
========
public class TSIntegrationDiagnosisReportConfig {
>>>>>>>> e1ab372 (BS 54 | renamed existing count report files name to show the intent):src/main/java/org/bahmni/reports/model/TSIntegrationDiagnosisReportConfig.java
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
