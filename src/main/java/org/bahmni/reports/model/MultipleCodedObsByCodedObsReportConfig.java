package org.bahmni.reports.model;

import java.util.List;

public class MultipleCodedObsByCodedObsReportConfig implements Config {

    private String ageGroupName;
    private List<String> rowsGroupBy;
    private List<String> columnsGroupBy;

    public String getAgeGroupName() {
        return ageGroupName;
    }

    public void setAgeGroupName(String ageGroupName) {
        this.ageGroupName = ageGroupName;
    }

    public List<String> getRowsGroupBy() {
        return rowsGroupBy;
    }

    public void setRowsGroupBy(List<String> rowsGroupBy) {
        this.rowsGroupBy = rowsGroupBy;
    }

    public List<String> getColumnsGroupBy() {
        return columnsGroupBy;
    }

    public void setColumnsGroupBy(List<String> columnsGroupBy) {
        this.columnsGroupBy = columnsGroupBy;
    }
}
