package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TSIntegrationDiagnosisLineReportConfig extends TSIntegrationDiagnosisReportConfig implements Config {
    private List<String> patientAttributes;
    private List<String> patientAddresses;
    private List<String> extensions;

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

    public List<String> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }
}