package org.bahmni.reports.model;

import java.util.List;

public class CodedObsByCodedObsReportConfig implements Config {

    private String ageGroupName;
    private List<String> conceptPair;
    private List<String> rowsGroupBy;
    private List<String> columnsGroupBy;
    private List<String> locationTagNames;
    public List<String> getLocationTagNames() { return locationTagNames; }

    public void setLocationTagNames(List<String> locationTagNames) {
        this.locationTagNames = locationTagNames;
    }

    public String getAgeGroupName() {
        return ageGroupName;
    }

    public void setAgeGroupName(String ageGroupName) {
        this.ageGroupName = ageGroupName;
    }

    public List<String> getConceptPair() {
        return conceptPair;
    }

    public void setConceptPair(List<String> conceptPair) {
        this.conceptPair = conceptPair;
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

    public String firstConcept() {
        return conceptPair.get(0);
    }

    public String secondConcept() {
        return conceptPair.get(1);
    }

    public String getColumnName(String groupByConfigName) {
        if (groupByConfigName.equals(firstConcept())) {
            return "first_concept_name";
        }
        if (groupByConfigName.equals(secondConcept())) {
            return "second_concept_name";
        }
        return groupByConfigName;
    }
}
