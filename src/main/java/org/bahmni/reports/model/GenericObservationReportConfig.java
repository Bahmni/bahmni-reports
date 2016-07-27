package org.bahmni.reports.model;

import java.util.ArrayList;
import java.util.List;

public class GenericObservationReportConfig implements Config {
    private boolean forDataAnalysis = false;
    private List<String> patientAttributes;
    private List<String> visitAttributes;
    private String applyDateRangeFor;
    private List<String> patientAddresses;
    private List<String> locationTagsToFilter;
    private Boolean showProvider = false;
    private boolean showVisitInfo = false;
    private List<String> conceptClassesToFilter;
    private List<String> programsToFilter;
    private List<String> conceptNamesToFilter;
    private boolean encounterPerRow = false;
    private List<String> visitTypesToFilter;
    private List<String> excludeColumns = new ArrayList<>();

    public List<String> getVisitTypesToFilter() {
        return visitTypesToFilter;
    }

    public void setVisitTypesToFilter(List<String> visitTypesToFilter) {
        this.visitTypesToFilter = visitTypesToFilter;
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

    public String getApplyDateRangeFor() {
        return applyDateRangeFor;
    }

    public void setApplyDateRangeFor(String applyDateRangeFor) {
        this.applyDateRangeFor = applyDateRangeFor;
    }

    public List<String> getPatientAddresses() {
        return patientAddresses;
    }

    public void setPatientAddresses(List<String> patientAddresses) {
        this.patientAddresses = patientAddresses;
    }

    public List<String> getLocationTagsToFilter() {
        return locationTagsToFilter;
    }

    public void setLocationTagsToFilter(List<String> locationTagsToFilter) {
        this.locationTagsToFilter = locationTagsToFilter;
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

    public List<String> getConceptClassesToFilter() {
        return conceptClassesToFilter;
    }

    public void setConceptClassesToFilter(List<String> conceptClassesToFilter) {
        this.conceptClassesToFilter = conceptClassesToFilter;
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

    public boolean isEncounterPerRow() {
        return encounterPerRow;
    }

    public void setEncounterPerRow(boolean encounterPerRow) {
        this.encounterPerRow = encounterPerRow;
    }

    public List<String> getExcludeColumns() {
        return excludeColumns != null ? excludeColumns : new ArrayList<String>();
    }

    public void setExcludeColumns(List<String> excludeColumns) {
        this.excludeColumns = excludeColumns;
    }
}