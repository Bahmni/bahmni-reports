package org.bahmni.reports.model;

import java.util.List;

public class GenericVisitReportConfig extends GenericReportsConfig implements Config {
    private String applyDateRangeFor;
    private List<String> visitTypesToFilter;
    private List<String> locationTagsToFilter;


    public String getApplyDateRangeFor() {
        return applyDateRangeFor;
    }

    public void setApplyDateRangeFor(String applyDateRangeFor) {
        this.applyDateRangeFor = applyDateRangeFor;
    }

    public List<String> getVisitTypesToFilter() {
        return visitTypesToFilter;
    }

    public void setVisitTypesToFilter(List<String> visitTypesToFilter) {
        this.visitTypesToFilter = visitTypesToFilter;
    }

    public List<String> getLocationTagsToFilter() {
        return locationTagsToFilter;
    }

    public void setLocationTagsToFilter(List<String> locationTagsToFilter) {
        this.locationTagsToFilter = locationTagsToFilter;
    }
}