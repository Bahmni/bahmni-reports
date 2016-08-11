package org.bahmni.reports.model;

import java.util.ArrayList;
import java.util.List;

import static org.bahmni.reports.util.BahmniReportUtil.emptyListIfNull;

public class GenericReportsConfig {
    private List<String> excludeColumns = new ArrayList<>();
    private boolean forDataAnalysis = false;
    private List<String> patientAttributes;
    private List<String> visitAttributes;
    private List<String> patientAddresses;
    private List<String> additionalPatientIdentifiers;
    private String ageGroupName;

    public List<String> getExcludeColumns() {
        return emptyListIfNull(excludeColumns);
    }

    public void setExcludeColumns(List<String> excludeColumns) {
        this.excludeColumns = excludeColumns;
    }

    public boolean isForDataAnalysis() {
        return forDataAnalysis;
    }

    public void setForDataAnalysis(boolean forDataAnalysis) {
        this.forDataAnalysis = forDataAnalysis;
    }

    public List<String> getPatientAttributes() {
        return emptyListIfNull(patientAttributes);
    }

    public void setPatientAttributes(List<String> patientAttributes) {
        this.patientAttributes = patientAttributes;
    }

    public List<String> getVisitAttributes() {
        return emptyListIfNull(visitAttributes);
    }

    public void setVisitAttributes(List<String> visitAttributes) {
        this.visitAttributes = visitAttributes;
    }

    public List<String> getPatientAddresses() {
        return emptyListIfNull(patientAddresses);
    }

    public void setPatientAddresses(List<String> patientAddresses) {
        this.patientAddresses = patientAddresses;
    }

    public List<String> getAdditionalPatientIdentifiers() {
        return emptyListIfNull(additionalPatientIdentifiers);
    }

    public void setAdditionalPatientIdentifiers(List<String> additionalPatientIdentifiers) {
        this.additionalPatientIdentifiers = additionalPatientIdentifiers;
    }

    public String getAgeGroupName() {
        return ageGroupName;
    }

    public void setAgeGroupName(String ageGroupName) {
        this.ageGroupName = ageGroupName;
    }
}
