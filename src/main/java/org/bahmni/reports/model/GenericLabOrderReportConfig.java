package org.bahmni.reports.model;

import java.util.List;
import java.util.Map;

public class GenericLabOrderReportConfig implements Config {
    private boolean forDataAnalysis = false;
    private List<String> patientAttributes;
    private List<String> visitAttributes;
    private List<String> patientAddresses;
    private Boolean showProvider = false;
    private boolean showVisitInfo = false;
    private List<String> programsToFilter;
    private List<String> conceptNamesToFilter;
    private List<String> excludeColumns;
    private List<String> conceptValuesToFilter;
    private boolean showOrderDateTime;

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

    public Boolean showProvider() {
        return showProvider;
    }

    public void setShowProvider(Boolean showProvider) {
        this.showProvider = showProvider;
    }

    public boolean showVisitInfo() {
        return showVisitInfo;
    }

    public void setShowVisitInfo(boolean showVisitInfo) {
        this.showVisitInfo = showVisitInfo;
    }

    public List<String> getProgramsToFilter() {
        return programsToFilter;
    }

    public void setProgramsToFilter(List<String> programsToFilter) {
        this.programsToFilter = programsToFilter;
    }

    public List<String> getConceptNamesToFilter() {
        return conceptNamesToFilter;
    }

    public void setConceptNamesToFilter(List<String> conceptNamesToFilter) {
        this.conceptNamesToFilter = conceptNamesToFilter;
    }

    public List<String> getExcludeColumns() {
        return excludeColumns;
    }

    public void setExcludeColumns(List<String> excludeColumns) {
        this.excludeColumns = excludeColumns;
    }

    public List<String> getConceptValuesToFilter() {
        return conceptValuesToFilter;
    }

    public void setConceptValuesToFilter(List<String> conceptValuesToFilter) {
        this.conceptValuesToFilter = conceptValuesToFilter;
    }

    public boolean isShowOrderDateTime() {
        return showOrderDateTime;
    }

    public void setShowOrderDateTime(boolean showOrderDateTime) {
        this.showOrderDateTime = showOrderDateTime;
    }
}