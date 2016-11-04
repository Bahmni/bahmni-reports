package org.bahmni.reports.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GenericReportsConfig {
    private List<String> excludeColumns = new ArrayList<>();
    private boolean forDataAnalysis = false;
    private List<String> patientAttributes;
    private List<String> visitAttributes;
    private List<String> patientAddresses;
    private List<String> additionalPatientIdentifiers;
    private String ageGroupName;
    private List<String> preferredColumns;
    private List<HashMap<String,String>> sortBy;

    public List<String> getExcludeColumns() {
        return excludeColumns;
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
        return patientAttributes;
    }

    public void setPatientAttributes(List<String> patientAttributes) {
        this.patientAttributes = patientAttributes;
    }

    public List<String> getVisitAttributes() {
        return visitAttributes;
    }

    public void setVisitAttributes(List<String> visitAttributes) {
        this.visitAttributes = visitAttributes;
    }

    public List<String> getPatientAddresses() {
        return patientAddresses;
    }

    public void setPatientAddresses(List<String> patientAddresses) {
        this.patientAddresses = patientAddresses;
    }

    public List<String> getAdditionalPatientIdentifiers() {
        return additionalPatientIdentifiers;
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

    public List<String> getPreferredColumns() {
        return preferredColumns;
    }

    public void setPreferredColumns(List<String> preferredColumns) {
        this.preferredColumns = preferredColumns;
    }

    public List<HashMap<String, String>> getSortBy() {
        return sortBy;
    }

    public void setSortBy(List<HashMap<String, String>> sortBy) {
        this.sortBy = sortBy;
    }
}
