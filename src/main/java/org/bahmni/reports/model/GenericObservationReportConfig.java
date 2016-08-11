package org.bahmni.reports.model;

import java.util.List;

import static org.bahmni.reports.util.BahmniReportUtil.emptyListIfNull;

public class GenericObservationReportConfig extends GenericReportsConfig implements Config {
    private String applyDateRangeFor;
    private List<String> locationTagsToFilter;
    private Boolean showProvider = false;
    private boolean showVisitInfo = false;
    private List<String> conceptClassesToFilter;
    private List<String> programsToFilter;
    private List<String> conceptNamesToFilter;
    private boolean encounterPerRow = false;
    private List<String> visitTypesToFilter;
    private String conceptNameDisplayFormat;

    public List<String> getVisitTypesToFilter() {
        return emptyListIfNull(visitTypesToFilter);
    }

    public void setVisitTypesToFilter(List<String> visitTypesToFilter) {
        this.visitTypesToFilter = visitTypesToFilter;
    }


    public String getApplyDateRangeFor() {
        return applyDateRangeFor;
    }

    public void setApplyDateRangeFor(String applyDateRangeFor) {
        this.applyDateRangeFor = applyDateRangeFor;
    }

    public List<String> getLocationTagsToFilter() {
        return emptyListIfNull(locationTagsToFilter);
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
        return emptyListIfNull(conceptClassesToFilter);
    }

    public void setConceptClassesToFilter(List<String> conceptClassesToFilter) {
        this.conceptClassesToFilter = conceptClassesToFilter;
    }

    public List<String> getProgramsToFilter() {
        return emptyListIfNull(programsToFilter);
    }

    public void setProgramsToFilter(List<String> programsToFilter) {
        this.programsToFilter = programsToFilter;
    }

    public List<String> getConceptNamesToFilter() {
        return emptyListIfNull(conceptNamesToFilter);
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

    public String getConceptNameDisplayFormat() {
        return conceptNameDisplayFormat;
    }

    public void setConceptNameDisplayFormat(String conceptNameDisplayFormat) {
        this.conceptNameDisplayFormat = conceptNameDisplayFormat;
    }
}