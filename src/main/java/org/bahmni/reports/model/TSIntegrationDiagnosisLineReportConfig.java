package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TSIntegrationDiagnosisLineReportConfig implements Config {
    private String tsConceptSource;
    private String terminologyParentCode;
    private String terminologyColumnName;
    private Boolean displayTerminologyCode;
    private List<String> patientAttributes;
    private List<String> patientAddresses;

    public String getTsConceptSource() {
        return tsConceptSource;
    }

    public void setTsConceptSource(String tsConceptSource) {
        this.tsConceptSource = tsConceptSource;
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

    public List<String> getPatientAttributes() {
        return patientAttributes;
    }

    public void setPatientAttributes(List<String> patientAttributes) {
        this.patientAttributes = patientAttributes;
    }

    public List<String> getPatientAddresses() {
        return patientAddresses;
    }

    public void setPatientAddresses(List<String> patientAddresses) {
        this.patientAddresses = patientAddresses;
    }
}