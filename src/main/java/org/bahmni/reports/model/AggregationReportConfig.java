package org.bahmni.reports.model;

import java.util.List;


public class AggregationReportConfig implements Config {

    private List<String> rowGroups;
    private List<String> columnGroups;
    private List<String> distinctGroups;
    private Report report;

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public List<String> getRowGroups() {
        return this.rowGroups;
    }

    public void setRowGroups(List<String> rowGroups) {
        this.rowGroups = rowGroups;
    }

    public List<String> getColumnGroups() {
        return columnGroups;
    }

    public void setColumnGroups(List<String> columnGroups) {
        this.columnGroups = columnGroups;
    }

    public List<String> getDistinctGroups() {
        return distinctGroups;
    }

    public void setDistinctGroups(List<String> distinctGroups) {
        this.distinctGroups = distinctGroups;
    }

}
