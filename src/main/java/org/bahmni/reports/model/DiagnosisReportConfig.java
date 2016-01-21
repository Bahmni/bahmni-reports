package org.bahmni.reports.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.util.StringUtils.isEmpty;


public class DiagnosisReportConfig implements Config {
    public static enum DateRangeTarget {
        visitStopDate,
        diagnosisDate
    }


    public static enum VisitCharacteristics {
        open,
        closed,
        all
    }

    private String ageGroupName;

    private List<String> visitTypes;

    private Boolean dateRangeRequired;

    private DateRangeTarget applyDateRangeFor;

    private VisitCharacteristics visitsToConsider;

    private List<String> rowsGroupBy;

    private List<String> columnsGroupBy;

    private String concept;

    private String icd10ConceptSource;

    private List<String> locationTagNames;

    public String getAgeGroupName() {
        return ageGroupName;
    }

    public String getAgeGroupName(boolean getDefaultIfNotAvailable) {
        return isEmpty(getAgeGroupName()) ? Constants.ALL_AGES_AGEGROUP : getAgeGroupName();
    }

    public void setAgeGroupName(String ageGroupName) {
        this.ageGroupName = ageGroupName;
    }

    public List<String> getVisitTypes() {
        return visitTypes;
    }

    public void setVisitTypes(List<String> visitTypes) {
        this.visitTypes = visitTypes;
    }

    public Boolean getDateRangeRequired() {
        return dateRangeRequired == null ? true : dateRangeRequired;
    }

    public void setDateRangeRequired(Boolean dateRangeRequired) {
        this.dateRangeRequired = dateRangeRequired;
    }

    public DateRangeTarget getApplyDateRangeFor() {
        return applyDateRangeFor;
    }

    public void setApplyDateRangeFor(DateRangeTarget applyDateRangeFor) {
        this.applyDateRangeFor = applyDateRangeFor;
    }

    public VisitCharacteristics getVisitsToConsider() {
        return visitsToConsider == null ? VisitCharacteristics.all : visitsToConsider;
    }

    public void setVisitsToConsider(VisitCharacteristics visitsToConsider) {
        this.visitsToConsider = visitsToConsider;
    }

    public List<String> getRowsGroupBy() {
        return replaceNullWithEmptyList(rowsGroupBy);
    }

    public void setRowsGroupBy(List<String> rowsGroupBy) {
        this.rowsGroupBy = rowsGroupBy;
    }

    public List<String> getColumnsGroupBy() {
        return replaceNullWithEmptyList(columnsGroupBy);
    }

    private List replaceNullWithEmptyList(List<String> list) {
        return list == null ? Collections.EMPTY_LIST : list;
    }

    public void setColumnsGroupBy(List<String> columnsGroupBy) {
        this.columnsGroupBy = columnsGroupBy;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getIcd10ConceptSource() {
        return icd10ConceptSource;
    }

    public void setIcd10ConceptSource(String icd10ConceptSource) {
        this.icd10ConceptSource = icd10ConceptSource;
    }

    public List<String> getLocationTagNames() {
        return locationTagNames;
    }

    public void setLocationTagNames(List<String> locationTagNames) {
        this.locationTagNames = locationTagNames;
    }

    public boolean retrieveOnlyOpenVisits() {
        return getVisitsToConsider().equals(VisitCharacteristics.open);
    }

    public boolean retrieveOnlyClosedVisits() {
        return getVisitsToConsider().equals(VisitCharacteristics.closed);
    }

    public boolean retrieveAllVisits() {
        return getVisitsToConsider().equals(VisitCharacteristics.all);
    }

    public boolean retrieveBasedOnDiagnosisDatetime() {
        return specifiedDateRangeTarget(DateRangeTarget.diagnosisDate);
    }

    public boolean retrieveBasedOnVisitStopDate() {
        return specifiedDateRangeTarget(DateRangeTarget.visitStopDate) || defaultDateRangeRequired();
    }

    private boolean defaultDateRangeRequired() {
        return getDateRangeRequired() && getApplyDateRangeFor() == null;
    }

    private boolean specifiedDateRangeTarget(DateRangeTarget visitStopDate) {
        DateRangeTarget dateRangeTarget = getApplyDateRangeFor();
        return getDateRangeRequired() && dateRangeTarget != null && dateRangeTarget.equals(visitStopDate);
    }

    public List<String> getRowsGroupBy(boolean getDefaultIfNotAvailable) {
        return getDefaultIfNotAvailable ? groupingSpecified() ? getRowsGroupBy() : getDefaultRowsGroupBy() : getRowsGroupBy();
    }

    public List<String> getColumnsGroupBy(boolean getDefaultIfNotAvailable) {
        return getDefaultIfNotAvailable ?
                groupingSpecified() ?
                        getColumnsGroupBy() :
                        getDefaultColumnsGroupBy() :
                getColumnsGroupBy();
    }

    private boolean groupingSpecified() {
        return !(getRowsGroupBy().isEmpty() && getColumnsGroupBy().isEmpty());
    }

    public List<String> getDefaultColumnsGroupBy() {
        return Arrays.asList("agegroup_name");
    }

    private List<String> getDefaultRowsGroupBy() {
        return Arrays.asList("header_concept_name", "leaf_concept_name");
    }

    public boolean visitTypesPresent() {
        return !(getVisitTypes() == null || getVisitTypes().isEmpty());
    }
}