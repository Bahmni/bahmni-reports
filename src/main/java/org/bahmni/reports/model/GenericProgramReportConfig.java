package org.bahmni.reports.model;


import java.util.List;

public class GenericProgramReportConfig implements Config{

    private boolean forDataAnalysis = false;
    private boolean showAllStates = false;
    private List<String> patientAttributes;
    private List<String> programAttributes;
    private List<String> visitAttributes;
    private String applyDateRangeFor;
    private List<String> patientAddresses;
    private List<String> programNamesToFilter;

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

    public List<String> getProgramAttributes() {
        return programAttributes;
    }

    public void setProgramAttributes(List<String> programAttributes) {
        this.programAttributes = programAttributes;
    }

    public List<String> getProgramNamesToFilter() {
        return programNamesToFilter;
    }

    public void setProgramNamesToFilter(List<String> programNamesToFilter) {
        this.programNamesToFilter = programNamesToFilter;
    }

    public boolean isShowAllStates() {
        return showAllStates;
    }

    public void setShowAllStates(boolean showAllStates) {
        this.showAllStates = showAllStates;
    }
}
