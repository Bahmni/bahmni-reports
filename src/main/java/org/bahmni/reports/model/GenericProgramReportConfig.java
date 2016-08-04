package org.bahmni.reports.model;


import java.util.List;

public class GenericProgramReportConfig extends GenericReportsConfig implements Config{

    private boolean showAllStates = false;
    private List<String> programAttributes;
    private String applyDateRangeFor;
    private List<String> programNamesToFilter;


    public String getApplyDateRangeFor() {
        return applyDateRangeFor;
    }

    public void setApplyDateRangeFor(String applyDateRangeFor) {
        this.applyDateRangeFor = applyDateRangeFor;
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
