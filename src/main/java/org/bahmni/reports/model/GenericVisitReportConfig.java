package org.bahmni.reports.model;

import java.util.List;

public class GenericVisitReportConfig extends GenericReportsConfig implements Config {
    private DateRange applyDateRangeFor ;
    private List<String> visitTypesToFilter;
    private List<String> locationTagsToFilter;


    public DateRange getApplyDateRangeFor() {
        return applyDateRangeFor;
    }

    public void setApplyDateRangeFor(DateRange applyDateRangeFor) {
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
    public enum DateRange {
        visitStartDate("v.date_started"),
        visitStopDate("v.date_stopped"),
        dateOfAdmission("admission_details.admission_date"),
        dateOfDischarge("admission_details.discharge_date");
        private String columnName;
        DateRange(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }
    }
}